#!/usr/bin/env bash
# Patches extensions-source for JVM compilation.
# Run from the repo root (extensions-desktop/).
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
EXT_SRC="${EXT_SRC:-$REPO_ROOT/extensions-source}"

echo "▶ Patching extensions-source for JVM compilation..."

# 1. Copy android-compat into extensions-source so it can be used as a subproject
rm -rf "$EXT_SRC/android-compat"
cp -r "$REPO_ROOT/android-compat" "$EXT_SRC/android-compat"

# 1b. Copy desktop-api into extensions-source so JVM builds can avoid Android-only AARs
rm -rf "$EXT_SRC/desktop-api"
cp -r "$REPO_ROOT/desktop-api" "$EXT_SRC/desktop-api"

# 2. Replace common.gradle with JVM version
cp "$REPO_ROOT/patches/common-jvm.gradle" "$EXT_SRC/common.gradle"

# 3. Replace core/build.gradle.kts with JVM version
cp "$REPO_ROOT/patches/core-build-jvm.gradle.kts" "$EXT_SRC/core/build.gradle.kts"

# 4. Replace core's Preferences.kt with JVM implementation
cp "$REPO_ROOT/patches/preferences-jvm.kt" \
   "$EXT_SRC/core/src/main/kotlin/keiyoushi/utils/Preferences.kt"

# 5. Replace lib-multisrc build files with JVM versions,
#    preserving any project(":lib:...") dependencies from the original.
for dir in "$EXT_SRC/lib-multisrc"/*/; do
    name=$(basename "$dir")
    build_file="$dir/build.gradle.kts"
    # Extract extra dependencies from git (original, unpatched version):
    # - project(":lib:...") references (excluding i18n which is bundled)
    # - direct Maven coordinates like "org.brotli:dec:0.1.2"
    extra_deps=""
    maven_deps=""
    original=$(cd "$EXT_SRC" && git show "HEAD:lib-multisrc/$name/build.gradle.kts" 2>/dev/null || true)
    if [ -n "$original" ]; then
        extra_deps=$(echo "$original" | grep -oE 'project\(":lib:[^"]+"\)' | grep -v ':lib:i18n' | sort -u || true)
        # Capture direct Maven dep strings: "group:artifact:version" patterns inside implementation(...)
        maven_deps=$(echo "$original" | grep -oE '"[a-zA-Z0-9._-]+:[a-zA-Z0-9._-]+:[0-9][^"]*"' | sort -u || true)
    fi
    cp "$REPO_ROOT/patches/lib-multisrc-build-jvm.gradle.kts" "$build_file" 2>/dev/null || true
    # Append extra deps if any
    if [ -n "$extra_deps" ] || [ -n "$maven_deps" ]; then
        echo "" >> "$build_file"
        echo "dependencies {" >> "$build_file"
        while IFS= read -r dep; do
            [ -n "$dep" ] && echo "    implementation($dep)" >> "$build_file"
        done <<< "$extra_deps"
        while IFS= read -r dep; do
            [ -n "$dep" ] && echo "    implementation($dep)" >> "$build_file"
        done <<< "$maven_deps"
        echo "}" >> "$build_file"
    fi
done

# 6. Replace the buildSrc lib-android convention plugin with a JVM version
cp "$REPO_ROOT/patches/lib-android-build-jvm.gradle.kts" \
   "$EXT_SRC/buildSrc/src/main/kotlin/lib-android.gradle.kts"

# 7. Replace lib build files (simple kotlin libs)
for dir in "$EXT_SRC/lib"/*/; do
    name=$(basename "$dir")
    build_file="$dir/build.gradle.kts"
    if [ -f "$build_file" ]; then
        # Replace Android library plugin with JVM
        sed -i.bak \
            -e 's/id("com\.android\.library")/kotlin("jvm")/g' \
            -e 's/apply plugin: .com\.android\.library./apply plugin: '\''org.jetbrains.kotlin.jvm'\''/g' \
            -e '/compileSdk/d' \
            -e '/minSdk/d' \
            -e '/namespace/d' \
            -e '/android {/,/^}/d' \
            "$build_file"
        rm -f "${build_file}.bak"
    fi
done

# 8. Replace settings.gradle.kts with a JVM-aware version that supports
# selective module inclusion via -Pdesktop.modules=...
cp "$REPO_ROOT/patches/settings-jvm.gradle.kts" "$EXT_SRC/settings.gradle.kts"

# 9. Replace root build.gradle.kts with a JVM-safe version that still provides
# the Kotlin Gradle plugin via buildscript, which common.gradle applies.
cat > "$EXT_SRC/build.gradle.kts" << 'EOF'
allprojects {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

buildscript {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath(libs.gradle.kotlin)
        classpath(libs.gradle.serialization)
    }
}
EOF

# 10. Apply targeted source-level patches for JVM smart-cast / null-safety issues
# 10a. lib/publus: assign url.fragment to local val before Base64.decode to allow smart cast
PUBLUS="$EXT_SRC/lib/publus/src/keiyoushi/lib/publus/Publus.kt"
if [ -f "$PUBLUS" ]; then
    python3 - "$PUBLUS" << 'PYEOF'
import sys, re
path = sys.argv[1]
src = open(path).read()
# Replace the null-check + decode pattern to use a local val
src = src.replace(
    'if (url.fragment.isNullOrEmpty()) {\n                return chain.proceed(request)\n            }',
    'val urlFragment = url.fragment\n            if (urlFragment.isNullOrEmpty()) {\n                return chain.proceed(request)\n            }'
)
src = src.replace('Base64.decode(url.fragment, Base64.URL_SAFE)', 'Base64.decode(urlFragment, Base64.URL_SAFE)')
open(path, 'w').write(src)
PYEOF
fi

# 10b. ru/nudemoon: add null-safe call for getCookie()
NUDEMOON_FILE=$(find "$EXT_SRC/src/ru/nudemoon" -name "*.kt" 2>/dev/null | head -1)
if [ -n "$NUDEMOON_FILE" ]; then
    python3 - "$NUDEMOON_FILE" << 'PYEOF'
import sys
path = sys.argv[1]
src = open(path).read()
src = src.replace(
    'cookieManager.getCookie(baseUrl).contains("fusion_user").not()',
    '(cookieManager.getCookie(baseUrl)?.contains("fusion_user") != true)'
)
open(path, 'w').write(src)
PYEOF
fi

echo "✓ Patch complete."

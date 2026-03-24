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

# 5. Replace lib-multisrc build files with JVM versions
for dir in "$EXT_SRC/lib-multisrc"/*/; do
    name=$(basename "$dir")
    # Each lib-multisrc module has a build.gradle.kts
    cp "$REPO_ROOT/patches/lib-multisrc-build-jvm.gradle.kts" "$dir/build.gradle.kts" 2>/dev/null || true
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
    }
}
EOF

echo "✓ Patch complete."

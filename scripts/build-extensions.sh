#!/usr/bin/env bash
# Builds all extensions as JVM JARs.
# Outputs JARs to out/apk/ and metadata JSON to out/index.json
# Run from extensions-source/ directory after patch.sh.
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
OUT_DIR="$SCRIPT_DIR/../out"
APK_DIR="$OUT_DIR/apk"
mkdir -p "$APK_DIR"

EXT_SRC="$SCRIPT_DIR/../extensions-source"
cd "$EXT_SRC"

PASS=0
FAIL=0
SKIP=0
declare -a METADATA=()

echo "▶ Building JVM extensions..."

# Iterate over all src/<lang>/<ext> modules
for lang_dir in src/*/; do
    lang=$(basename "$lang_dir")
    for ext_dir in "$lang_dir"*/; do
        ext=$(basename "$ext_dir")
        module=":src:${lang}:${ext}"

        # Try to build
        if ./gradlew "${module}:jar" --quiet --continue 2>/dev/null; then
            # Find the produced JAR
            jar_file=$(find "$ext_dir/build/libs" -name "*.jar" 2>/dev/null | head -1)
            if [ -z "$jar_file" ]; then
                ((SKIP++)) || true
                continue
            fi

            jar_name=$(basename "$jar_file")
            cp "$jar_file" "$APK_DIR/$jar_name"

            # Extract metadata from build.gradle
            version_code=$(grep -oP 'extVersionCode\s*=\s*\K[0-9]+' "$ext_dir/build.gradle" 2>/dev/null || echo "1")
            ext_name=$(grep -oP "extName\s*=\s*['\"]?\K[^'\"]+(?=['\"])" "$ext_dir/build.gradle" 2>/dev/null || echo "$ext")
            nsfw=$(grep -q "isNsfw.*true" "$ext_dir/build.gradle" 2>/dev/null && echo "1" || echo "0")
            pkg="eu.kanade.tachiyomi.extension.${lang}.${ext}"
            version_name="1.4.${version_code}"

            entry="{\"name\":\"Tachiyomi: ${ext_name}\",\"pkg\":\"${pkg}\",\"apk\":\"${jar_name}\",\"lang\":\"${lang}\",\"code\":${version_code},\"version\":\"${version_name}\",\"nsfw\":${nsfw},\"sources\":[]}"
            METADATA+=("$entry")
            ((PASS++)) || true
            echo "  ✓ $lang/$ext"
        else
            ((FAIL++)) || true
            echo "  ✗ $lang/$ext (compile error)"
        fi
    done
done

# Write index
printf '[' > "$OUT_DIR/index.min.json"
first=1
for entry in "${METADATA[@]}"; do
    if [ $first -eq 0 ]; then printf ',' >> "$OUT_DIR/index.min.json"; fi
    printf '%s' "$entry" >> "$OUT_DIR/index.min.json"
    first=0
done
printf ']' >> "$OUT_DIR/index.min.json"

echo ""
echo "✓ Done: ${PASS} built, ${FAIL} failed, ${SKIP} skipped"
echo "  Index: $OUT_DIR/index.min.json"
echo "  JARs:  $APK_DIR/"

#!/usr/bin/env bash
# Builds extensions as JVM JARs with per-module isolation.
# Outputs JARs to out/apk/, metadata JSON to out/index.min.json,
# and failures to out/failed_modules.txt.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
EXT_SRC="${EXT_SRC:-$REPO_ROOT/extensions-source}"
OUT_DIR="$REPO_ROOT/out"
APK_DIR="$OUT_DIR/apk"
FAILURES_FILE="$OUT_DIR/failed_modules.txt"
LOG_DIR="$OUT_DIR/logs"

mkdir -p "$APK_DIR" "$LOG_DIR"
rm -f "$APK_DIR"/*.jar "$OUT_DIR/index.min.json" "$FAILURES_FILE"
rm -rf "$LOG_DIR"/*

if [ ! -d "$EXT_SRC/src" ]; then
    echo "extensions-source/src not found: $EXT_SRC" >&2
    exit 1
fi

cd "$EXT_SRC"

mapfile -t module_files < <(
    find src -mindepth 3 -maxdepth 3 -type f \( -name 'build.gradle' -o -name 'build.gradle.kts' \) | sort
)

if [ "${#module_files[@]}" -eq 0 ]; then
    echo "No extension module build files found under $EXT_SRC/src" >&2
    exit 1
fi

PASS=0
FAIL=0

echo "▶ Building ${#module_files[@]} JVM extension modules with isolated settings..."

for file in "${module_files[@]}"; do
    module="${file%/*}"
    module_path="${module//\//:}"
    module_task=":${module_path}:jar"
    safe_name="${module_path//:/__}"
    log_file="$LOG_DIR/${safe_name}.log"

    rm -rf "${module}/build"

    if ./gradlew -Pdesktop.modules="${module_path}" "${module_task}" --quiet >"$log_file" 2>&1; then
        echo "PASS ${module_path}"
        PASS=$((PASS + 1))
    else
        echo "FAIL ${module_path}" | tee -a "$FAILURES_FILE"
        FAIL=$((FAIL + 1))
    fi
done

python3 - "$EXT_SRC" "$APK_DIR" "$OUT_DIR/index.min.json" <<'PY'
import json
import re
import shutil
import sys
from pathlib import Path

ext_src = Path(sys.argv[1])
apk_dir = Path(sys.argv[2])
index_path = Path(sys.argv[3])
apk_dir.mkdir(parents=True, exist_ok=True)

entries = []

for build_file in sorted(ext_src.glob("src/*/*/build.gradle")) + sorted(ext_src.glob("src/*/*/build.gradle.kts")):
    lang = build_file.parent.parent.name
    ext = build_file.parent.name
    libs_dir = build_file.parent / "build" / "libs"
    jars = sorted(p for p in libs_dir.glob("*.jar") if p.is_file())
    if not jars:
        continue

    jar_path = jars[0]
    jar_name = jar_path.name
    shutil.copy2(jar_path, apk_dir / jar_name)

    content = build_file.read_text(encoding="utf-8", errors="ignore")
    version_match = re.search(r"(?:extVersionCode|overrideVersionCode)\s*=\s*(\d+)", content)
    version_code = int(version_match.group(1)) if version_match else 1
    name_match = re.search(r"extName\s*=\s*['\"]([^'\"]+)['\"]", content)
    ext_name = name_match.group(1) if name_match else ext
    nsfw = 1 if re.search(r"isNsfw\s*=\s*true", content) else 0
    pkg = f"eu.kanade.tachiyomi.extension.{lang}.{ext}"

    entries.append({
        "name": f"Tachiyomi: {ext_name}",
        "pkg": pkg,
        "apk": jar_name,
        "lang": lang,
        "code": version_code,
        "version": f"1.4.{version_code}",
        "nsfw": nsfw,
        "sources": [],
    })

index_path.write_text(json.dumps(entries, separators=(",", ":")), encoding="utf-8")
print(f"Built {len(entries)} extensions")
PY

COUNT=$(find "$APK_DIR" -name "*.jar" | wc -l | tr -d ' ')
echo "✓ Done: ${COUNT} JARs collected, ${PASS} module tasks passed, ${FAIL} failed"

if [ "$COUNT" -eq 0 ]; then
    echo "No JVM extension JARs were produced." >&2
    exit 1
fi

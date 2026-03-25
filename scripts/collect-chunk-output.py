#!/usr/bin/env python3
"""
Collect build output from a chunk of extension modules.

Usage:
    python collect-chunk-output.py <extensions-source-path> <output-dir> [module1 module2 ...]

Scans src/*/*/build/libs/ for JARs, copies them to <output-dir>/apk/,
generates <output-dir>/index-partial.json, and writes success/failed lists.
Also writes <output-dir>/versions.txt with module:version_code per line
for both successful and failed modules.

If module arguments are provided, only those modules are checked.
Otherwise, all modules with JARs are collected.
"""
import json
import re
import shutil
import sys
from pathlib import Path


def parse_build_gradle(build_file: Path) -> dict:
    """Extract extension metadata from build.gradle."""
    content = build_file.read_text(encoding="utf-8", errors="ignore")

    version_match = re.search(
        r"(?:extVersionCode|overrideVersionCode)\s*=\s*(\d+)", content
    )
    version_code = int(version_match.group(1)) if version_match else 1

    name_match = re.search(r"extName\s*=\s*['\"]([^'\"]+)['\"]", content)
    ext_name = name_match.group(1) if name_match else build_file.parent.name

    nsfw = 1 if re.search(r"isNsfw\s*=\s*true", content) else 0

    return {
        "version_code": version_code,
        "ext_name": ext_name,
        "nsfw": nsfw,
    }


def main():
    if len(sys.argv) < 3:
        print(
            f"Usage: {sys.argv[0]} <extensions-source-path> <output-dir> [modules...]",
            file=sys.stderr,
        )
        sys.exit(1)

    ext_src = Path(sys.argv[1])
    output_dir = Path(sys.argv[2])
    requested_modules = set(sys.argv[3:]) if len(sys.argv) > 3 else None

    apk_dir = output_dir / "apk"
    apk_dir.mkdir(parents=True, exist_ok=True)

    entries = []
    success = []
    failed = []
    # Maps module_path -> version_code (for both success and failed)
    versions: dict[str, int] = {}

    # Scan all extension directories
    for build_file in sorted(ext_src.glob("src/*/*/build.gradle")):
        lang = build_file.parent.parent.name
        ext = build_file.parent.name
        module_path = f"src:{lang}:{ext}"

        # If specific modules requested, skip others
        if requested_modules is not None and module_path not in requested_modules:
            continue

        meta = parse_build_gradle(build_file)
        versions[module_path] = meta["version_code"]

        libs_dir = build_file.parent / "build" / "libs"
        jars = sorted(p for p in libs_dir.glob("*.jar") if p.is_file()) if libs_dir.exists() else []

        if not jars:
            if requested_modules is not None:
                failed.append(module_path)
            continue

        jar_path = jars[0]
        jar_name = jar_path.name
        shutil.copy2(jar_path, apk_dir / jar_name)

        pkg = f"eu.kanade.tachiyomi.extension.{lang}.{ext}"

        entries.append(
            {
                "name": f"Tachiyomi: {meta['ext_name']}",
                "pkg": pkg,
                "apk": jar_name,
                "lang": lang,
                "code": meta["version_code"],
                "version": f"1.4.{meta['version_code']}",
                "nsfw": meta["nsfw"],
                "sources": [],
            }
        )
        success.append(module_path)

    # Write outputs
    (output_dir / "index-partial.json").write_text(
        json.dumps(entries, separators=(",", ":")), encoding="utf-8"
    )
    (output_dir / "success.txt").write_text(
        "\n".join(success) + "\n" if success else "", encoding="utf-8"
    )
    (output_dir / "failed.txt").write_text(
        "\n".join(failed) + "\n" if failed else "", encoding="utf-8"
    )

    # Write versions.txt: module_path:version_code for all processed modules
    versions_lines = [
        f"{module_path}:{version_code}"
        for module_path, version_code in sorted(versions.items())
    ]
    (output_dir / "versions.txt").write_text(
        "\n".join(versions_lines) + "\n" if versions_lines else "",
        encoding="utf-8",
    )

    print(f"Collected {len(entries)} JARs, {len(failed)} failed")


if __name__ == "__main__":
    main()

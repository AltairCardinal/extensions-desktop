#!/usr/bin/env python3
"""
Merge incremental build artifacts into the repo branch.

Usage:
    python merge-repo.py <repo-dir> <artifacts-dir> <delete-json> <upstream-sha>

- repo-dir: checked-out repo branch directory
- artifacts-dir: directory containing chunk-*/apk/*.jar and chunk-*/index-partial.json
- delete-json: JSON array of module pkg suffixes to delete (e.g. ["en.aeinscans"])
- upstream-sha: SHA to write into upstream-commit.txt
"""
import json
import re
import shutil
import sys
from datetime import date
from pathlib import Path


def load_known_failures(scripts_dir: Path) -> dict:
    """Load known-failures.json from the scripts directory."""
    known_failures_path = scripts_dir / "known-failures.json"
    if known_failures_path.is_file():
        try:
            return json.loads(known_failures_path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError):
            pass
    return {}


def load_versions_from_chunks(artifacts_dir: Path) -> dict[str, int]:
    """Aggregate versions.txt from all chunks into a module -> version_code map."""
    versions: dict[str, int] = {}
    for chunk_dir in sorted(artifacts_dir.glob("chunk-*")):
        versions_file = chunk_dir / "versions.txt"
        if not versions_file.is_file():
            continue
        for line in versions_file.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line:
                continue
            # Format: src:lang:ext:version_code
            parts = line.rsplit(":", 1)
            if len(parts) == 2:
                module_path, version_str = parts
                try:
                    versions[module_path] = int(version_str)
                except ValueError:
                    pass
    return versions


def parse_existing_first_failed_dates(readme_path: Path) -> dict[str, str]:
    """
    Parse the existing README.md to extract 'First Failed' dates per extension.
    Returns a dict of module display name -> date string.
    We key on 'lang:ext' parsed from the table rows.
    """
    first_failed: dict[str, str] = {}
    if not readme_path.is_file():
        return first_failed

    content = readme_path.read_text(encoding="utf-8")
    # Match table rows: | ext_name | lang | vN | reason | date |
    row_re = re.compile(
        r"^\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|\s*v(\d+)\s*\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|",
        re.MULTILINE,
    )
    for m in row_re.finditer(content):
        ext_name = m.group(1).strip()
        lang = m.group(2).strip()
        first_date = m.group(5).strip()
        # Key by "lang:ext_name" — ext_name here is the display name from the table
        key = f"{lang}:{ext_name}"
        first_failed[key] = first_date

    return first_failed


def module_path_to_display(module_path: str) -> tuple[str, str]:
    """Convert 'src:lang:ext' to (ext_display_name, lang)."""
    parts = module_path.split(":")
    if len(parts) == 3:
        return parts[2], parts[1]
    return module_path, "unknown"


def generate_readme(
    repo_dir: Path,
    all_success: list[str],
    all_failed: list[str],
    build_state: dict,
    known_failures: dict,
    versions: dict[str, int],
) -> str:
    """Generate the README.md content."""
    today = date.today().isoformat()

    # Parse existing README to preserve first-failed dates
    readme_path = repo_dir / "README.md"
    existing_first_failed = parse_existing_first_failed_dates(readme_path)

    total_extensions = len(list((repo_dir / "apk").glob("*.jar"))) if (repo_dir / "apk").is_dir() else 0
    fail_count = len(all_failed)

    # Count permanently unsupported (WebView/QuickJS) vs potentially fixable
    permanently_unsupported = sum(
        1 for m in all_failed
        if "WebView" in known_failures.get(m, "") or "QuickJS" in known_failures.get(m, "")
    )
    fixable_count = fail_count - permanently_unsupported

    # Build failure table rows
    # Combine known failures from build_state (failed status) for a full picture
    failed_in_state = {
        module: info
        for module, info in build_state.items()
        if info.get("status") == "failed"
    }
    # Also add newly failed from this run
    all_failed_set = set(all_failed) | set(failed_in_state.keys())

    table_rows = []
    for module_path in sorted(all_failed_set):
        ext_name, lang = module_path_to_display(module_path)
        version_code = versions.get(module_path)
        if version_code is None:
            # Try from build_state
            state = build_state.get(module_path, {})
            version_code = state.get("version")
        version_str = f"v{version_code}" if version_code is not None else "unknown"

        reason = known_failures.get(module_path, "compilation error")

        # Determine first-failed date
        # Key used in existing table: "lang:ext_name"
        lookup_key = f"{lang}:{ext_name}"
        if lookup_key in existing_first_failed:
            first_date = existing_first_failed[lookup_key]
        else:
            # Check build_state for existing timestamp
            state = build_state.get(module_path, {})
            first_date = state.get("timestamp", today)

        table_rows.append((ext_name, lang, version_str, reason, first_date))

    # Sort by lang then ext name
    table_rows.sort(key=lambda r: (r[1], r[0]))

    table_lines = []
    for ext_name, lang, version_str, reason, first_date in table_rows:
        table_lines.append(
            f"| {ext_name} | {lang} | {version_str} | {reason} | {first_date} |"
        )

    table_str = "\n".join(table_lines) if table_lines else "_None_"

    readme = f"""\
# Mihon Desktop Extensions

JVM-compatible extensions for [Mihon Desktop](https://github.com/AltairCardinal/mihon).

## Stats

- Total extensions: {total_extensions}
- Compilation failures: {fail_count} ({fixable_count} fixable, {permanently_unsupported} permanently unsupported)

## Installation

Add this URL to Mihon Desktop settings → Extensions → Extension Repos:
`https://raw.githubusercontent.com/AltairCardinal/extensions-desktop/repo`

## Compilation Failures

The following extensions fail to compile on JVM. Extensions known to be permanently unsupported (WebView/QuickJS-dependent) will not be retried unless their source is updated. New failures will be retried on next version update.

| Extension | Language | Version | Reason | First Failed |
|-----------|----------|---------|--------|--------------|
{table_str}
"""
    return readme


def main():
    if len(sys.argv) != 5:
        print(
            f"Usage: {sys.argv[0]} <repo-dir> <artifacts-dir> <delete-json> <upstream-sha>",
            file=sys.stderr,
        )
        sys.exit(1)

    repo_dir = Path(sys.argv[1])
    artifacts_dir = Path(sys.argv[2])
    to_delete: list[str] = json.loads(sys.argv[3])
    upstream_sha = sys.argv[4]

    # Determine scripts directory (same directory as this script)
    scripts_dir = Path(__file__).parent

    apk_dir = repo_dir / "apk"
    apk_dir.mkdir(parents=True, exist_ok=True)

    index_path = repo_dir / "index.min.json"

    # Load existing index
    if index_path.is_file():
        existing_index: list[dict] = json.loads(
            index_path.read_text(encoding="utf-8")
        )
    else:
        existing_index = []

    # 1. Delete old JARs and index entries for modules being rebuilt/deleted
    if to_delete:
        for module_suffix in to_delete:
            # module_suffix is like "en.aeinscans"
            jar_pattern = f"eu.kanade.tachiyomi.extension.{module_suffix}-v*.jar"
            for jar_file in apk_dir.glob(jar_pattern):
                print(f"Removing old JAR: {jar_file.name}")
                jar_file.unlink()

        # Remove from index
        delete_pkgs = {
            f"eu.kanade.tachiyomi.extension.{s}" for s in to_delete
        }
        existing_index = [
            item
            for item in existing_index
            if item["pkg"] not in delete_pkgs
        ]

    # 2. Collect new JARs and partial indices from all chunks
    new_entries: list[dict] = []
    all_failed: list[str] = []
    all_success: list[str] = []

    for chunk_dir in sorted(artifacts_dir.glob("chunk-*")):
        # Copy JARs
        chunk_apk_dir = chunk_dir / "apk"
        if chunk_apk_dir.is_dir():
            for jar_file in chunk_apk_dir.glob("*.jar"):
                shutil.copy2(jar_file, apk_dir / jar_file.name)
                print(f"Added: {jar_file.name}")

        # Collect partial index
        partial_index = chunk_dir / "index-partial.json"
        if partial_index.is_file():
            entries = json.loads(partial_index.read_text(encoding="utf-8"))
            new_entries.extend(entries)

        # Collect success/failed
        failed_file = chunk_dir / "failed.txt"
        if failed_file.is_file():
            content = failed_file.read_text(encoding="utf-8").strip()
            if content:
                all_failed.extend(content.splitlines())

        success_file = chunk_dir / "success.txt"
        if success_file.is_file():
            content = success_file.read_text(encoding="utf-8").strip()
            if content:
                all_success.extend(content.splitlines())

    # 3. Merge index: new entries replace old entries with same pkg
    new_pkgs = {entry["pkg"] for entry in new_entries}
    merged_index = [
        item for item in existing_index if item["pkg"] not in new_pkgs
    ]
    merged_index.extend(new_entries)
    merged_index.sort(key=lambda x: x["pkg"])

    # 4. Write index and upstream-commit
    index_path.write_text(
        json.dumps(merged_index, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )

    (repo_dir / "upstream-commit.txt").write_text(
        upstream_sha + "\n", encoding="utf-8"
    )

    # Ensure repo.json exists (required by Mihon Desktop's CreateExtensionRepo validation)
    repo_meta_path = repo_dir / "repo.json"
    if not repo_meta_path.exists():
        repo_meta = {
            "meta": {
                "name": "Mihon Desktop Extensions",
                "shortName": "MihonDesktop",
                "website": "https://github.com/AltairCardinal/extensions-desktop",
                "signingKeyFingerprint": "0000000000000000000000000000000000000000000000000000000000000000",
            }
        }
        repo_meta_path.write_text(
            json.dumps(repo_meta, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )

    if all_failed:
        (repo_dir / "failed_modules.txt").write_text(
            "\n".join(all_failed) + "\n", encoding="utf-8"
        )
    elif (repo_dir / "failed_modules.txt").is_file():
        # Clear old failure list if everything succeeded
        (repo_dir / "failed_modules.txt").unlink()

    # 5. Update build-state.json
    today = date.today().isoformat()
    build_state_path = repo_dir / "build-state.json"
    if build_state_path.is_file():
        try:
            build_state: dict = json.loads(build_state_path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError):
            build_state = {}
    else:
        build_state = {}

    known_failures = load_known_failures(scripts_dir)
    versions = load_versions_from_chunks(artifacts_dir)

    for module_path in all_success:
        version_code = versions.get(module_path)
        entry: dict = {"status": "built", "timestamp": today}
        if version_code is not None:
            entry["version"] = version_code
        build_state[module_path] = entry

    for module_path in all_failed:
        version_code = versions.get(module_path)
        reason = known_failures.get(module_path, "compilation error")
        entry = {"status": "failed", "timestamp": today, "reason": reason}
        if version_code is not None:
            entry["version"] = version_code
        build_state[module_path] = entry

    build_state_path.write_text(
        json.dumps(build_state, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    # 6. Generate README.md
    readme_content = generate_readme(
        repo_dir=repo_dir,
        all_success=all_success,
        all_failed=all_failed,
        build_state=build_state,
        known_failures=known_failures,
        versions=versions,
    )
    (repo_dir / "README.md").write_text(readme_content, encoding="utf-8")

    total_jars = len(list(apk_dir.glob("*.jar")))
    print(
        f"\nMerge complete: {total_jars} total JARs in repo, "
        f"{len(new_entries)} new/updated, {len(all_failed)} failed"
    )


if __name__ == "__main__":
    main()

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
import shutil
import sys
from pathlib import Path


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

    # 4. Write outputs
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

    total_jars = len(list(apk_dir.glob("*.jar")))
    print(
        f"\nMerge complete: {total_jars} total JARs in repo, "
        f"{len(new_entries)} new/updated, {len(all_failed)} failed"
    )


if __name__ == "__main__":
    main()

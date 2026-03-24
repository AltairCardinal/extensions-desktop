#!/usr/bin/env python3
"""
Compute which extension modules need rebuilding based on upstream diff.

Usage:
    python generate-modules.py <extensions-source-path> <last-upstream-sha> <is-full-rebuild>

Outputs (to GITHUB_OUTPUT if CI=true, else stdout):
    matrix   - JSON build matrix with chunked module lists
    delete   - JSON list of module pkg suffixes to remove from repo
    upstream_sha - HEAD SHA of extensions-source
    should_build - "true" or "false"

Adapted from keiyoushi/extensions-source/.github/scripts/generate-build-matrices.py
"""
from __future__ import annotations

import json
import os
import re
import subprocess
import sys
from pathlib import Path
from typing import Optional

EXTENSION_REGEX = re.compile(r"^src/(?P<lang>\w+)/(?P<extension>\w+)")
MULTISRC_LIB_REGEX = re.compile(r"^lib-multisrc/(?P<multisrc>\w+)")
LIB_REGEX = re.compile(r"^lib/(?P<lib>\w+)")
CORE_FILES_REGEX = re.compile(
    r"^(buildSrc/|core/|gradle/|build\.gradle\.kts|common\.gradle|gradle\.properties|settings\.gradle\.kts)"
)


def batched(iterable, n):
    """Backport of itertools.batched (Python 3.12+)."""
    it = iter(iterable)
    while True:
        batch = []
        for _ in range(n):
            try:
                batch.append(next(it))
            except StopIteration:
                if batch:
                    yield batch
                return
        yield batch


def run_command(command: str, cwd: Optional[str] = None) -> str:
    result = subprocess.run(
        command, capture_output=True, text=True, shell=True, cwd=cwd
    )
    if result.returncode != 0:
        print(f"Command failed: {command}", file=sys.stderr)
        print(result.stderr.strip(), file=sys.stderr)
        sys.exit(result.returncode)
    return result.stdout.strip()


def resolve_dependent_libs(libs: set) -> set:
    """Recursively find all libs that depend on any of the given libs."""
    if not libs:
        return set()

    all_dependent_libs: set = set()
    to_process = set(libs)

    while to_process:
        current_libs = to_process
        to_process = set()

        lib_dependency = re.compile(
            rf"project\([\"']:(?:lib):({'|'.join(map(re.escape, current_libs))})[\"']\)"
        )

        for lib in Path("lib").iterdir():
            if lib.name in all_dependent_libs or lib.name in libs:
                continue

            build_file = lib / "build.gradle.kts"
            if not build_file.is_file():
                continue

            content = build_file.read_text("utf-8")
            if lib_dependency.search(content):
                all_dependent_libs.add(lib.name)
                to_process.add(lib.name)

    return all_dependent_libs


def resolve_multisrc_lib(libs: set) -> set:
    """Find all multisrc themes that depend on any of the given libs."""
    if not libs:
        return set()

    lib_dependency = re.compile(
        rf"project\([\"']:(?:lib):({'|'.join(map(re.escape, libs))})[\"']\)"
    )

    multisrcs: set = set()
    for multisrc in Path("lib-multisrc").iterdir():
        build_file = multisrc / "build.gradle.kts"
        if not build_file.is_file():
            continue
        content = build_file.read_text("utf-8")
        if lib_dependency.search(content):
            multisrcs.add(multisrc.name)

    return multisrcs


def resolve_ext(multisrcs: set, libs: set) -> set:
    """Find all extensions that depend on any of the given multisrcs or libs."""
    if not multisrcs and not libs:
        return set()

    patterns = []
    if multisrcs:
        multisrc_pattern = "|".join(map(re.escape, multisrcs))
        patterns.append(rf"themePkg\s*=\s*['\"]({multisrc_pattern})['\"]")
    if libs:
        lib_pattern = "|".join(map(re.escape, libs))
        patterns.append(rf"project\([\"']:(?:lib):({lib_pattern})[\"']\)")

    regex = re.compile("|".join(patterns))

    extensions: set = set()
    for lang in Path("src").iterdir():
        if not lang.is_dir():
            continue
        for extension in lang.iterdir():
            build_file = extension / "build.gradle"
            if not build_file.is_file():
                continue
            content = build_file.read_text("utf-8")
            if regex.search(content):
                extensions.add((lang.name, extension.name))

    return extensions


def get_all_modules() -> tuple[list, list]:
    """List all extension modules."""
    modules = []
    deleted = []
    for lang in sorted(Path("src").iterdir()):
        if not lang.is_dir():
            continue
        for extension in sorted(lang.iterdir()):
            if not (extension / "build.gradle").is_file():
                continue
            modules.append(f"src:{lang.name}:{extension.name}")
            deleted.append(f"{lang.name}.{extension.name}")
    return modules, deleted


def get_module_list(ref: str) -> tuple[list, list]:
    """Compute changed modules by diffing against ref."""
    diff_output = run_command(f"git diff --name-status {ref}").splitlines()

    changed_files = [
        file for line in diff_output for file in line.split("\t", 2)[1:]
    ]

    modules: set = set()
    multisrcs: set = set()
    libs: set = set()
    deleted: set = set()
    core_files_changed = False

    for file in map(lambda x: Path(x).as_posix(), changed_files):
        if CORE_FILES_REGEX.search(file):
            core_files_changed = True

        elif match := EXTENSION_REGEX.search(file):
            lang = match.group("lang")
            extension = match.group("extension")
            if Path("src", lang, extension).is_dir():
                modules.add(f"src:{lang}:{extension}")
            deleted.add(f"{lang}.{extension}")

        elif match := MULTISRC_LIB_REGEX.search(file):
            multisrc = match.group("multisrc")
            if Path("lib-multisrc", multisrc).is_dir():
                multisrcs.add(multisrc)

        elif match := LIB_REGEX.search(file):
            lib = match.group("lib")
            if Path("lib", lib).is_dir():
                libs.add(lib)

    if core_files_changed:
        all_modules, all_deleted = get_all_modules()
        modules.update(all_modules)
        deleted.update(all_deleted)
        return list(modules), list(deleted)

    # Resolve transitive dependencies
    libs.update(resolve_dependent_libs(libs))
    multisrcs.update(resolve_multisrc_lib(libs))
    extensions = resolve_ext(multisrcs, libs)
    modules.update(
        [f"src:{lang}:{extension}" for lang, extension in extensions]
    )
    deleted.update(
        [f"{lang}.{extension}" for lang, extension in extensions]
    )

    return list(modules), list(deleted)


def main():
    if len(sys.argv) != 4:
        print(
            f"Usage: {sys.argv[0]} <extensions-source-path> <last-sha> <is-full>",
            file=sys.stderr,
        )
        sys.exit(1)

    ext_source_path = sys.argv[1]
    last_sha = sys.argv[2]
    is_full = sys.argv[3].lower() == "true"

    os.chdir(ext_source_path)

    upstream_sha = run_command("git rev-parse HEAD")

    if is_full or not last_sha:
        print("Full rebuild requested", file=sys.stderr)
        modules, deleted = get_all_modules()
    elif last_sha == upstream_sha:
        print("No upstream changes", file=sys.stderr)
        modules, deleted = [], []
    else:
        print(f"Incremental build: {last_sha[:8]}..{upstream_sha[:8]}", file=sys.stderr)
        modules, deleted = get_module_list(last_sha)

    should_build = len(modules) > 0
    chunk_size = int(os.getenv("CI_CHUNK_SIZE", "50"))

    chunked = {
        "chunk": [
            {"number": i + 1, "modules": list(chunk_modules)}
            for i, chunk_modules in enumerate(
                batched(modules, chunk_size)
            )
        ]
    }

    print(
        f"Modules to build: {len(modules)}, to delete: {len(deleted)}, "
        f"chunks: {len(chunked['chunk'])}",
        file=sys.stderr,
    )

    if os.getenv("CI") == "true":
        github_output = os.getenv("GITHUB_OUTPUT", "")
        with open(github_output, "a") as out_file:
            out_file.write(f"matrix={json.dumps(chunked)}\n")
            out_file.write(f"delete={json.dumps(deleted)}\n")
            out_file.write(f"upstream_sha={upstream_sha}\n")
            out_file.write(f"should_build={'true' if should_build else 'false'}\n")
    else:
        print(f"\nmatrix={json.dumps(chunked, indent=2)}")
        print(f"\ndelete={json.dumps(deleted, indent=2)}")
        print(f"\nupstream_sha={upstream_sha}")
        print(f"\nshould_build={'true' if should_build else 'false'}")


if __name__ == "__main__":
    main()

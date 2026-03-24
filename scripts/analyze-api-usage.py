#!/usr/bin/env python3

from __future__ import annotations

import re
import sys
from collections import defaultdict
from pathlib import Path


IMPORT_RE = re.compile(r"^import\s+([A-Za-z0-9_.*]+)\s*$")
TARGET_PREFIXES = (
    "eu.kanade.tachiyomi.network",
    "eu.kanade.tachiyomi.util",
    "tachiyomi.core",
)
QUICKJS_IMPORT = "app.cash.quickjs.QuickJs"


def module_name(path: Path, root: Path) -> str:
    rel = path.relative_to(root)
    parts = rel.parts
    if len(parts) >= 4 and parts[0] in {"src", "lib-multisrc"}:
        return ":".join(parts[:3])
    if len(parts) >= 3 and parts[0] == "lib":
        return ":".join(parts[:2])
    return str(rel.parent)


def main() -> int:
    root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("extensions-source")
    if not root.exists():
        print(f"extensions-source root not found: {root}", file=sys.stderr)
        return 1

    import_to_modules: dict[str, set[str]] = defaultdict(set)
    quickjs_modules: set[str] = set()

    for path in sorted(root.rglob("*.kt")):
        mod = module_name(path, root)
        for line in path.read_text(encoding="utf-8", errors="ignore").splitlines():
            match = IMPORT_RE.match(line.strip())
            if not match:
                continue
            imp = match.group(1)
            if imp == QUICKJS_IMPORT:
                quickjs_modules.add(mod)
            if imp.startswith(TARGET_PREFIXES):
                import_to_modules[imp].add(mod)

    print("# Desktop API Usage Report")
    print()
    print(f"root: {root}")
    print(f"tracked_imports: {len(import_to_modules)}")
    print(f"quickjs_modules: {len(quickjs_modules)}")
    print()

    print("## Import Summary")
    for imp in sorted(import_to_modules):
        print(imp)
    print()

    print("## QuickJS Modules")
    for mod in sorted(quickjs_modules):
        print(mod)
    print()

    print("## Imported APIs")
    for imp in sorted(import_to_modules):
        modules = sorted(import_to_modules[imp])
        print(f"{imp} :: {len(modules)}")
        for mod in modules:
            print(f"  {mod}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())

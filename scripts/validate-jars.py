#!/usr/bin/env python3
"""
Validate compiled extension JARs for Mihon Desktop.

For each JAR in the apk directory this script checks:
  1. The JAR is a valid zip archive.
  2. It contains META-INF/services/eu.kanade.tachiyomi.source.Source.
  3. The service file names exactly one class.
  4. The declared class is present in the JAR as a .class file.

Exit code: 0 if all JARs pass, 1 if any fail.

Usage:
    python validate-jars.py <apk-dir> [--fail-fast]
"""
import argparse
import sys
import zipfile
from pathlib import Path


SERVICE_KEY = "META-INF/services/eu.kanade.tachiyomi.source.Source"


def validate_jar(jar_path: Path) -> list[str]:
    """Return a list of error strings (empty list = pass)."""
    errors: list[str] = []

    # 1. Is it a valid zip?
    if not zipfile.is_zipfile(jar_path):
        errors.append("not a valid zip/JAR file")
        return errors

    with zipfile.ZipFile(jar_path, "r") as zf:
        names = set(zf.namelist())

        # 2. Service file must exist
        if SERVICE_KEY not in names:
            errors.append(f"missing {SERVICE_KEY}")
            return errors

        # 3. Read declared class name(s)
        raw = zf.read(SERVICE_KEY).decode("utf-8", errors="replace").strip()
        class_names = [ln.strip() for ln in raw.splitlines() if ln.strip() and not ln.startswith("#")]

        if not class_names:
            errors.append("service file is empty — no class declared")
            return errors

        if len(class_names) > 1:
            errors.append(f"service file declares {len(class_names)} classes (expected 1): {class_names}")

        # 4. Each declared class must exist in the JAR
        for cls in class_names:
            class_file = cls.replace(".", "/") + ".class"
            if class_file not in names:
                errors.append(f"declared class {cls} not found as {class_file}")

    return errors


def main():
    parser = argparse.ArgumentParser(description="Validate Mihon Desktop extension JARs")
    parser.add_argument("apk_dir", help="Directory containing .jar files")
    parser.add_argument("--fail-fast", action="store_true", help="Stop at first failure")
    args = parser.parse_args()

    apk_dir = Path(args.apk_dir)
    if not apk_dir.is_dir():
        print(f"ERROR: {apk_dir} is not a directory", file=sys.stderr)
        sys.exit(1)

    jars = sorted(apk_dir.glob("*.jar"))
    if not jars:
        print(f"WARNING: no .jar files found in {apk_dir}")
        sys.exit(0)

    passed = 0
    failed = 0
    failures: list[tuple[str, list[str]]] = []

    for jar in jars:
        errors = validate_jar(jar)
        if errors:
            failed += 1
            failures.append((jar.name, errors))
            print(f"  FAIL  {jar.name}")
            for err in errors:
                print(f"         → {err}")
            if args.fail_fast:
                break
        else:
            passed += 1
            print(f"  PASS  {jar.name}")

    total = passed + failed
    print()
    print(f"Results: {passed}/{total} passed", end="")
    if failed:
        print(f", {failed} FAILED")
    else:
        print(" — all OK")

    if failures:
        print("\nFailed JARs:")
        for name, errs in failures:
            print(f"  {name}")
            for e in errs:
                print(f"    - {e}")
        sys.exit(1)
    else:
        sys.exit(0)


if __name__ == "__main__":
    main()

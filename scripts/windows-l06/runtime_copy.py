"""H02 runtime copies: exact scoped source/destination inventory, not supply-chain attestation."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import stat
import sys

EXCLUDED_LIB_DIRS = {"site-packages", "test", "idlelib", "tkinter", "__pycache__", "ensurepip"}


def plain(path):
    """Refuse links/reparse points, including ancestors; do not follow directory links."""
    for p in (path, *path.parents):
        s = p.lstat()
        if stat.S_ISLNK(s.st_mode) or getattr(s, "st_file_attributes", 0) & 0x400:
            raise ValueError("link")


def digest(path):
    plain(path)
    with path.open("rb") as f:
        h = hashlib.sha256()
        size = 0
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            size += len(chunk)
            h.update(chunk)
        return (size, h.hexdigest())


def inventory(root, mode="all"):
    root = Path(os.path.abspath(root))
    plain(root)
    if not root.is_dir():
        raise ValueError("not directory")
    result = {}
    folded = set()

    def add(path, record):
        key = path.relative_to(root).as_posix()
        if key.casefold() in folded:
            raise ValueError("case collision")
        folded.add(key.casefold())
        result[key] = record

    def walk(directory):
        for p in sorted(directory.iterdir(), key=lambda x: x.name):
            # The excluded Python Lib subtrees are never copied or traversed.
            if mode == "lib" and p.name.casefold() in EXCLUDED_LIB_DIRS and p.is_dir():
                continue
            if mode == "bin" and p.suffix.lower() not in {".exe", ".dll"}:
                continue
            plain(p)
            if p.is_dir() and mode != "bin":
                add(p, ("dir",))
                walk(p)
            elif p.is_file():
                add(p, ("file", *digest(p)))
            else:
                raise ValueError("unsupported entry")
    walk(root)
    if not result:
        raise ValueError("empty inventory")
    return result


def copy_verified(source, target, mode):
    source, target = map(lambda p: Path(os.path.abspath(p)), (source, target))
    if source == target or source in target.parents or target in source.parents:
        raise ValueError("overlap")
    before = inventory(source, mode)
    plain(target.parent)
    if target.exists():
        plain(target)
        if not target.is_dir() or any(target.iterdir()):
            raise ValueError("nonempty destination")
    else:
        target.mkdir()
    for key, record in sorted(before.items(), key=lambda item: (item[0].count("/"), item[0])):
        src, dst = source / key, target / key
        plain(src)
        plain(dst.parent)
        if record[0] == "dir":
            dst.mkdir()
        else:
            with src.open("rb") as inp, dst.open("xb") as out:
                shutil.copyfileobj(inp, out)
    if inventory(source, mode) != before:
        raise ValueError("source changed")
    if inventory(target) != before:
        raise ValueError("destination mismatch")
    return before


def main(args):
    if len(args) != 3 or args[0] not in {"all", "lib", "bin"}:
        return 1
    try:
        records = copy_verified(args[1], args[2], args[0])
        raw = json.dumps(records, sort_keys=True, ensure_ascii=True, separators=(",", ":")).encode("ascii")
        print(f"entries={len(records)} sha256={hashlib.sha256(raw).hexdigest()}")
    except (OSError, ValueError):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))

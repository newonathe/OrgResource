"""Exact seven migrations (bytes equal to git blobs) and packaged assets; same rule as the Linux runner."""
import pathlib, re, subprocess, sys, zipfile
repo, sha, jar = sys.argv[1:]
try:
    prefix = "extended/backend/src/main/resources/db/migration/"
    paths = subprocess.check_output(["git", "-C", repo, "ls-tree", "-r", "--name-only", sha, "--", prefix], text=True).splitlines()
    versions = [re.fullmatch(r"V([1-7])__.+\.sql", pathlib.PurePosixPath(p).name) for p in paths]
    if not (len(paths) == 7 and all(versions) and {m[1] for m in versions} == set("1234567")): raise ValueError
    with zipfile.ZipFile(jar) as z:
        names = z.namelist()
        actual = [n for n in names if n.startswith("BOOT-INF/classes/db/migration/") and n.endswith(".sql")]
        expected = ["BOOT-INF/classes/db/migration/" + pathlib.PurePosixPath(p).name for p in paths]
        if sorted(actual) != sorted(expected): raise ValueError
        for path, member in zip(paths, expected):
            if z.read(member) != subprocess.check_output(["git", "-C", repo, "cat-file", "blob", sha + ":" + path]): raise ValueError
        if names.count("BOOT-INF/classes/static/index.html") != 1: raise ValueError
        if not any(n.startswith("BOOT-INF/classes/static/assets/") and n.endswith(".js") for n in names): raise ValueError
except Exception:
    sys.exit(1)

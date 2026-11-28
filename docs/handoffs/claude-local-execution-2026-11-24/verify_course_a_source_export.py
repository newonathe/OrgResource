#!/usr/bin/env python3
"""Validate a temporary exact-source candidate; never publish or upload it."""
import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import stat
import subprocess
import tempfile
import warnings
import zipfile

FROZEN = "7f8c0746da18fd54b47f430ac1435ac36e605d67"
EXTRA = set(["postman/OrgResource-Course-A-28-Routes.postman_collection.json","postman/OrgResource-Course-A-Image-HTTP.postman_collection.json","postman/OrgResource-Course-A-Local.postman_environment.json","postman/OrgResource-O1-Skeleton.postman_collection.json","postman/OrgResource-O1-Local.postman_environment.json","postman/OrgResource-O3-CRUD.postman_collection.json","postman/OrgResource-O3-Department-Update.postman_collection.json","postman/OrgResource-O4-O5-Allocation-Expiry.postman_collection.json","postman/OrgResource-O14-Dependency-Integrity.postman_collection.json","postman/fixtures/academic-demo-image.png","scripts/run-api-acceptance.ps1","scripts/course-a-demo.sql","scripts/start-portable-demo.ps1","docs/PORTABLE_FIRST_RUN.md","docs/COURSE_A_FINAL_DEMO_GUIDE.md"])
PREFIX = "OrgResource/"
ROOT = Path(__file__).resolve().parents[3]
MANIFEST = Path(__file__).with_name("17_COURSE_A_SOURCE_ALLOWLIST.json")

def require(ok, message):
    if not ok:
        raise ValueError(message)

def git(*args):
    env = dict(os.environ, GIT_NO_REPLACE_OBJECTS="1")
    return subprocess.check_output(["git", "-C", str(ROOT), *args], env=env)

def allowed(path):
    parts = path.split("/")
    return (bool(re.fullmatch(r"[A-Za-z0-9._/-]+", path))
            and not path.startswith("/")
            and all(p not in ("", ".", "..") and not p.endswith(".")
                    and p.upper().split(".")[0] not in
                    {"CON", "PRN", "AUX", "NUL", *("COM"+str(i) for i in range(1,10)), *("LPT"+str(i) for i in range(1,10))}
                    for p in parts)
            and (path.startswith(("backend/", "frontend/")) or path in EXTRA)
            and not any(p.lower() in {"target", "dist", "node_modules", ".git", ".env"} for p in parts))

def blob_id(data):
    return hashlib.sha1(("blob %d\0" % len(data)).encode("ascii") + data).hexdigest()

def load_manifest():
    doc = json.loads(MANIFEST.read_text(encoding="utf-8"))
    require(doc["source_commit"] == FROZEN and doc["file_count"] == 120, "wrong frozen source or count")
    rows = doc["entries"]
    expected = {r["path"]: r for r in rows}
    require(len(rows) == len(expected) == 120 and len({p.lower() for p in expected}) == 120, "duplicate paths")
    require(all(allowed(p) and r["mode"] == "100644" and re.fullmatch(r"[0-9a-f]{40}", r["sha"])
                and type(r["size"]) is int and 0 <= r["size"] <= 2_000_000
                for p, r in expected.items()), "unsafe manifest")
    require(EXTRA <= expected.keys(), "missing explicit dependencies")
    require(sum(r["size"] for r in rows) == doc["total_bytes"] == 793613, "byte count mismatch")
    require(git("rev-parse", FROZEN + "^{commit}").decode().strip() == FROZEN, "wrong commit")
    actual = {}
    for raw in git("ls-tree", "-r", "-l", "-z", FROZEN).split(b"\0"):
        if not raw:
            continue
        head, name = raw.split(b"\t", 1)
        mode, typ, sha, size = head.decode("ascii").split()
        path = name.decode("utf-8")
        if path.startswith(("backend/", "frontend/")) or path in EXTRA:
            require(typ == "blob" and mode == "100644" and allowed(path), "unsafe selected tree object")
            actual[path] = {"path": path, "mode": mode, "sha": sha, "size": int(size)}
    require(actual == expected, "frozen tree differs from allowlist")
    return expected

def write_zip(path, data):
    with zipfile.ZipFile(path, "x", compression=zipfile.ZIP_DEFLATED) as z:
        for name, payload in sorted(data.items()):
            info = zipfile.ZipInfo(PREFIX + name, (1980, 1, 1, 0, 0, 0))
            info.create_system = 3
            info.external_attr = 0o100644 << 16
            info.compress_type = zipfile.ZIP_DEFLATED
            z.writestr(info, payload)

def verify_zip(path, expected):
    data = {}
    with zipfile.ZipFile(path, "r") as z:
        infos = z.infolist()
        require(len(infos) == len(expected), "archive count mismatch")
        for info in infos:
            require(info.filename.startswith(PREFIX), "invalid archive root")
            name = info.filename[len(PREFIX):]
            require(allowed(name) and name in expected and name not in data, "extra or duplicate archive path")
            row = expected[name]
            require(not info.is_dir() and info.create_system == 3 and
                    stat.S_ISREG(info.external_attr >> 16) and
                    not (info.flag_bits & 1) and info.file_size == row["size"], "invalid archive member")
            with z.open(info) as member:
                payload = member.read(row["size"] + 1)
            require(len(payload) == row["size"] and blob_id(payload) == row["sha"], "archive member changed")
            data[name] = payload
    require(data.keys() == expected.keys(), "missing archive member")
    return data

def self_test(work):
    a, b = "backend/pom.xml", "frontend/package.json"
    payload = {a: b"one", b: b"two"}
    expected = {p: {"sha": blob_id(v), "size": len(v)} for p, v in payload.items()}
    good = work / "valid.zip"
    write_zip(good, payload)
    require(verify_zip(good, expected) == payload, "valid fixture refused")
    for label, files in (
        ("missing", {a: b"one"}),
        ("extra", {a: b"one", "ChatGPT-Project-Pack/private.txt": b"two"}),
        ("traversal", {a: b"one", "backend/../escape": b"two"}),
        ("tampered", {a: b"bad", b: b"two"}),
        ("case", {a: b"one", "frontend/PACKAGE.json": b"two"}),
    ):
        candidate = work / (label + ".zip")
        write_zip(candidate, files)
        try:
            verify_zip(candidate, expected)
        except ValueError:
            pass
        else:
            raise AssertionError("accepted " + label)
    for label in ("duplicate", "symlink"):
        candidate = work / (label + ".zip")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore", UserWarning)
            with zipfile.ZipFile(candidate, "x") as z:
                for name, mode, data in ((a, 0o100644, b"one"),
                    (a if label == "duplicate" else b, 0o120777 if label == "symlink" else 0o100644, b"two")):
                    info = zipfile.ZipInfo(PREFIX + name)
                    info.create_system = 3
                    info.external_attr = mode << 16
                    z.writestr(info, data)
        try:
            verify_zip(candidate, expected)
        except ValueError:
            pass
        else:
            raise AssertionError("accepted " + label)
    print("R04_ARCHIVE_NEGATIVE_OK:missing,extra,traversal,tampered,case,duplicate,symlink", flush=True)

def check(build):
    expected = load_manifest()
    with tempfile.TemporaryDirectory(prefix="orgresource-r04-") as temp:
        work = Path(temp)
        self_test(work)
        source = {}
        for name, row in expected.items():
            payload = git("cat-file", "blob", row["sha"])
            require(len(payload) == row["size"] and blob_id(payload) == row["sha"], "Git blob changed")
            source[name] = payload
        archive = work / "course-a-source-candidate.zip"
        write_zip(archive, source)
        recovered = verify_zip(archive, expected)
        extracted = work / "extracted" / "OrgResource"
        for name, payload in recovered.items():
            target = extracted.joinpath(*PurePosixPath(name).parts)
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(payload)
        actual = {x.relative_to(extracted).as_posix() for x in extracted.rglob("*") if x.is_file()}
        require(actual == expected.keys(), "extracted path mismatch")
        for name, row in expected.items():
            require(blob_id((extracted / name).read_bytes()) == row["sha"], "extracted blob mismatch")
        environment = json.loads((extracted / "postman/OrgResource-Course-A-Local.postman_environment.json").read_text())
        image_path = next(v["value"] for v in environment["values"] if v["key"] == "imageFile")
        require(image_path == "postman/fixtures/academic-demo-image.png" and (extracted / image_path).is_file(),
                "missing standalone image dependency")
        print("R04_EXACT_SOURCE_ARCHIVE_OK:files=120 bytes=793613 archive_bytes=%d" % archive.stat().st_size, flush=True)
        if build:
            version = subprocess.check_output(["javac", "-version"], stderr=subprocess.STDOUT, text=True)
            require(version.startswith("javac 1.8."), "real Java 8 compiler required")
            subprocess.run(["npm", "ci"], cwd=extracted / "frontend", check=True)
            subprocess.run(["npm", "run", "build"], cwd=extracted / "frontend", check=True)
            subprocess.run(["bash", "mvnw", "-B", "-Pbundle-react-ui", "-DskipTests", "clean", "package"],
                           cwd=extracted / "backend", check=True)
            jar = extracted / "backend/target/orgresource-0.1.0-SNAPSHOT.jar"
            with zipfile.ZipFile(jar) as z:
                names = z.namelist()
                require("BOOT-INF/classes/static/index.html" in names and
                        any(n.startswith("BOOT-INF/classes/static/assets/index-") and n.endswith(".js") for n in names),
                        "clean extraction JAR missing assets")
            print("R04_EXTRACTED_BUILD_OK:java8=true react=true packaged_assets=true runtime_not_run=true", flush=True)
    print("R04_TEMP_CANDIDATE_CLEANED:no_archive_uploaded=true", flush=True)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--build", action="store_true", help="also build extracted source with local Java 8 and npm; no DB/runtime")
    args = parser.parse_args()
    check(args.build)

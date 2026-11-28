#!/usr/bin/env python3
"""CI-only temporary standalone Course A candidate; never publish or distribute."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import stat
import subprocess
import tempfile
import zipfile

from verify_course_a_source_export import (
    FROZEN, EXTRA, ROOT, blob_id, git, load_manifest, require, verify_zip, write_zip,
)

MANIFEST = "RELEASE-MANIFEST.json"
SUMS = "SHA256SUMS.txt"
JAR = "OrgResource.jar"
FULL = "OrgResource-complete-source.zip"
JAVA = "OrgResource-java-source.zip"
IMAGE = "postman/fixtures/academic-demo-image.png"
ENV = "postman/OrgResource-Course-A-Local.postman_environment.json"
IMAGE_COLLECTION = "postman/OrgResource-Course-A-Image-HTTP.postman_collection.json"
PAYLOADS = EXTRA | {JAR, FULL, JAVA}

def sha(data):
    return hashlib.sha256(data).hexdigest()

def put(root, name, data):
    path = root / name
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("xb") as f:
        f.write(data)

def inventory(root):
    found = set()
    for path in root.rglob("*"):
        require(not path.is_symlink(), "symlink in release")
        if path.is_dir():
            continue
        require(stat.S_ISREG(path.lstat().st_mode), "nonregular release entry")
        found.add(path.relative_to(root).as_posix())
    return found

def expected_rows(data):
    return [{"path": name, "sizeBytes": len(data[name]), "sha256": sha(data[name])}
            for name in sorted(data)]

def manifest_bytes(data):
    return (json.dumps({
        "schemaVersion": 1, "sourceCommit": FROZEN,
        "classification": "TEMPORARY_CI_CANDIDATE_NOT_DISTRIBUTION_CLEARANCE",
        "proposalDocx": "NOT_INCLUDED_OWNER_LOCAL_VERIFICATION_REQUIRED",
        "artifacts": expected_rows(data)
    }, indent=2) + "\n").encode()

def sum_bytes(data):
    return "".join(sha(data[name]) + "  " + name + "\n" for name in sorted(data)).encode("ascii")

def verify(root, expected):
    require(expected.keys() == PAYLOADS, "wrong independent expected set")
    require(inventory(root) == PAYLOADS | {MANIFEST, SUMS}, "release path set mismatch")
    require((root / MANIFEST).read_bytes() == manifest_bytes(expected), "manifest differs from independent expected inventory")
    require((root / SUMS).read_bytes() == sum_bytes(expected), "checksum list differs")
    for name, payload in expected.items():
        data = (root / name).read_bytes()
        require(len(data) == len(payload) and sha(data) == sha(payload), "release payload differs")
    env = json.loads((root / ENV).read_bytes())
    values = {v["key"]: v["value"] for v in env["values"]}
    require(values["imageFile"] == IMAGE and (root / IMAGE).is_file(), "relative image fixture missing")
    require(values["baseUrl"] == "http://127.0.0.1:8080", "nonloopback environment")
    with zipfile.ZipFile(root / JAR) as z:
        names = z.namelist()
        require(names.count("BOOT-INF/classes/static/index.html") == 1
                and any(n.startswith("BOOT-INF/classes/static/assets/index-") and n.endswith(".js") for n in names),
                "missing packaged UI")
        main = z.read("BOOT-INF/classes/app/SpringBootApp.class")
        require(main[:4] == b"\xca\xfe\xba\xbe" and int.from_bytes(main[6:8], "big") == 52,
                "not Java 8 app bytecode")

def java_zip(path, source):
    selected = {k[len("backend/"):]: v for k,v in source.items() if k.startswith("backend/")}
    require(len(selected) == 75, "wrong Java source count")
    with zipfile.ZipFile(path, "x", compression=zipfile.ZIP_DEFLATED) as z:
        for name, data in sorted(selected.items()):
            info = zipfile.ZipInfo(name, (1980,1,1,0,0,0))
            info.create_system = 3
            info.external_attr = 0o100644 << 16
            info.compress_type = zipfile.ZIP_DEFLATED
            z.writestr(info,data)
    with zipfile.ZipFile(path) as z:
        require(len(z.infolist()) == len(selected) and set(z.namelist()) == selected.keys(),
                "Java archive path set mismatch")
        for member in z.infolist():
            require(stat.S_ISREG(member.external_attr >> 16) and member.file_size == len(selected[member.filename])
                    and z.read(member) == selected[member.filename], "Java archive member differs")
    require({".project",".classpath","pom.xml","mvnw","mvnw.cmd",
             ".mvn/wrapper/maven-wrapper.properties"} <= selected.keys(), "Eclipse or wrapper metadata absent")

def negative_tests(root, expected, work):
    for label in ("missing-image","tampered-image","extra-file","manifest","sums","symlink"):
        clone = work / label
        shutil.copytree(root,clone)
        if label == "missing-image":
            (clone / IMAGE).unlink()
        elif label == "tampered-image":
            (clone / IMAGE).write_bytes(b"tampered")
        elif label == "extra-file":
            put(clone, "unapproved.txt", b"fictional")
        elif label == "manifest":
            (clone / MANIFEST).write_bytes(b"{}")
        elif label == "sums":
            (clone / SUMS).write_bytes(b"wrong")
        else:
            (clone / IMAGE).unlink()
            (clone / IMAGE).symlink_to(root / IMAGE)
        try:
            verify(clone,expected)
        except ValueError:
            pass
        else:
            raise AssertionError("accepted " + label)
        shutil.rmtree(clone)
    print("R04_STANDALONE_NEGATIVES_OK:missing_image,tampered_image,extra_file,manifest,sums,symlink",flush=True)

def run(jar):
    require(os.environ.get("GITHUB_ACTIONS") == "true" and os.environ.get("RUNNER_OS") == "Linux",
            "CI fixture only")
    require(os.environ.get("ORG_TEST_DB_URL") ==
        "jdbc:mysql://127.0.0.1:3306/orgresource_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "isolated Course A test DB required")
    require(jar.resolve() == ROOT / "backend/target/orgresource-0.1.0-SNAPSHOT.jar"
            and jar.is_file() and not jar.is_symlink(), "exact CI-built jar required")
    frozen = load_manifest()
    # Ensure the current build inputs match immutable source before accepting the jar.
    for name,row in frozen.items():
        if name.startswith(("backend/","frontend/")):
            require(blob_id(git("cat-file","blob","HEAD:"+name)) == row["sha"], "build input differs from frozen")
            require(blob_id((ROOT / name).read_bytes()) == row["sha"], "working build input differs from frozen")
    source = {name: git("cat-file","blob",row["sha"]) for name,row in frozen.items()}
    require(all(blob_id(data) == frozen[name]["sha"] and len(data) == frozen[name]["size"]
                for name,data in source.items()),"source hash mismatch")
    with tempfile.TemporaryDirectory(prefix="orgresource-r04-standalone-") as temporary:
        work = Path(temporary)
        root = work / "release"
        root.mkdir()
        write_zip(work / FULL, source)
        require(verify_zip(work / FULL, frozen) == source,"complete source archive differs")
        java_zip(work / JAVA,source)
        expected = {name: source[name] for name in EXTRA}
        expected.update({JAR:jar.read_bytes(), FULL:(work/FULL).read_bytes(), JAVA:(work/JAVA).read_bytes()})
        for name,data in expected.items():
            put(root,name,data)
        put(root,MANIFEST,manifest_bytes(expected))
        put(root,SUMS,sum_bytes(expected))
        verify(root,expected)
        negative_tests(root,expected,work)
        # Existing guarded Java8 job owns the already running test-profile JAR.
        # Invoke actual image acceptance from the standalone folder, with its
        # own collection and fixture; no dependency on the checkout's PNG.
        subprocess.run(["npm","exec","--yes","--package=newman@6.2.3","--","newman","run",
            str(root / IMAGE_COLLECTION),"--env-var","baseUrl=http://127.0.0.1:8080",
            "--env-var","imageFile="+str(root / IMAGE),"--working-dir",str(root)],cwd=root,check=True)
        verify(root,expected)
        print("R04_STANDALONE_OK:payloads=18 java_source_files=75 full_source_files=120 image_fixture_actual_newman=true hashes_verified=true proposal_docx=NOT_RUN windows_launcher=NOT_RUN",flush=True)
    print("R04_STANDALONE_CLEANED:no_release_upload=true",flush=True)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar",required=True,type=Path)
    run(parser.parse_args().jar)

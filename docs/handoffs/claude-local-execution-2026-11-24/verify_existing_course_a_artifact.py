"""Inspect one pre-existing Course A CI artifact; no execution or extraction."""
import hashlib
import io
import pathlib
import re
import stat
import sys
import zipfile

EXPECTED = "37db61e0ff82954d88a7158f7f92b12f6a5dfd4f3c84dac5e900bc59ca6dffc9"
NAME = "orgresource-0.1.0-SNAPSHOT.jar"

def require(value, message):
    if not value:
        raise SystemExit(message)

def inspect(raw):
    require(len(raw) == 37371675, "outer size mismatch")
    require(hashlib.sha256(raw).hexdigest() == EXPECTED, "outer digest mismatch")
    with zipfile.ZipFile(io.BytesIO(raw)) as outer:
        entries = outer.infolist()
        require(len(entries) == 1 and entries[0].filename == NAME and not entries[0].is_dir(),
                "unexpected outer inventory")
        mode = entries[0].external_attr >> 16
        require(stat.S_IFMT(mode) in (0, stat.S_IFREG), "outer member not regular")
        require(entries[0].file_size <= 128 * 1024 * 1024, "inner size exceeds limit")
        require(outer.testzip() is None, "outer CRC failure")
        jar = outer.read(entries[0])
    with zipfile.ZipFile(io.BytesIO(jar)) as inner:
        entries = inner.infolist()
        names = [x.filename for x in entries]
        require(len(names) == len(set(names)), "duplicate JAR members")
        require(sum(x.file_size for x in entries) <= 512 * 1024 * 1024, "JAR expansion exceeds limit")
        require(all(not n.startswith("/") and "\\" not in n and ".." not in n.split("/") for n in names),
                "unsafe JAR member")
        require(inner.testzip() is None, "JAR CRC failure")
        classes = [n for n in names if n.startswith("BOOT-INF/classes/") and n.endswith(".class")]
        require("BOOT-INF/classes/app/SpringBootApp.class" in classes, "missing application entry class")
        for n in classes:
            head = inner.read(n)[:8]
            require(len(head) == 8 and head[:4] == b"\xca\xfe\xba\xbe" and int.from_bytes(head[6:8], "big") == 52,
                    "non-Java8 application class")
        index = "BOOT-INF/classes/static/index.html"
        require(index in names, "missing index")
        html = inner.read(index).decode("utf-8")
        assets = set(re.findall(r'(?:src|href)=["\x27](/?assets/[^"\x27]+\.(?:js|css))["\x27]', html))
        require(any(x.endswith(".js") for x in assets) and any(x.endswith(".css") for x in assets),
                "missing JS/CSS references")
        require(all("BOOT-INF/classes/static/" + x.lstrip("/") in names for x in assets), "referenced asset missing")
        require("META-INF/MANIFEST.MF" in names, "missing manifest")
        manifest = inner.read("META-INF/MANIFEST.MF").decode("utf-8")
        require("Start-Class: app.SpringBootApp" in manifest, "unexpected application start class")
        print("COURSE_A_EXISTING_ARTIFACT_BYTES_OK:"
              f"outer_sha256={EXPECTED} outer_bytes={len(raw)} jar_sha256={hashlib.sha256(jar).hexdigest()} "
              f"jar_bytes={len(jar)} jar_entries={len(entries)} java8_application_classes={len(classes)} "
              f"referenced_assets={len(assets)} crc_verified=true runtime_not_run=true")
if __name__ == "__main__":
    require(len(sys.argv) == 2, "expected exact artifact ZIP path")
    path = pathlib.Path(sys.argv[1])
    require(path.is_file() and not path.is_symlink() and path.stat().st_size == 37371675, "artifact size/path mismatch")
    inspect(path.read_bytes())

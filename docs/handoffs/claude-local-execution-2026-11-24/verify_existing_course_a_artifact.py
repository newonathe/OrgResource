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


def notice_kind(name):
    # Inventory only: presence and byte hashes are not legal classification.
    n = name.lower()
    if "/" in n and not n.startswith("meta-inf/"):
        return None
    tail = n.rsplit("/", 1)[-1]
    if re.fullmatch(r"(?:license|licence|copying)(?:[-_.][a-z0-9._-]+)?", tail):
        return "license"
    if re.fullmatch(r"(?:notice|copyright|dependencies|third[-_]party)(?:[-_.][a-z0-9._-]+)?", tail):
        return "notice"
    if n.startswith(("meta-inf/licenses/", "meta-inf/licences/")) and tail:
        return "license"
    return None

def inventory_notices(jar_zip, referenced_assets):
    require(notice_kind("LICENSE") == "license"
            and notice_kind("META-INF/LICENSE-2.0.txt") == "license"
            and notice_kind("META-INF/NOTICE.txt") == "notice"
            and notice_kind("META-INF/licenses/apache.txt") == "license"
            and notice_kind("foo/LICENSE") is None
            and notice_kind("META-INF/LICENSE.class") == "license"
            and notice_kind("META-INF/licensed.class") is None,
            "notice classifier fixture failed")
    # The classifier deliberately counts filename candidates, not valid license texts.
    entries = jar_zip.infolist()
    libs = [x for x in entries if not x.is_dir() and x.filename.startswith("BOOT-INF/lib/")]
    require(0 < len(libs) <= 300, "unexpected embedded library count")
    require(sum(x.file_size for x in libs) <= 128 * 1024 * 1024, "embedded library bytes exceed bound")
    total_expanded = 0
    with_license = with_notice = with_pom = 0
    for info in sorted(libs, key=lambda x: x.filename):
        name = info.filename.removeprefix("BOOT-INF/lib/")
        require(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._+-]*\.jar", name), "unexpected library entry")
        raw = jar_zip.read(info)
        with zipfile.ZipFile(io.BytesIO(raw)) as library:
            members = library.infolist()
            require(len(members) <= 100000 and len({x.filename for x in members}) == len(members),
                    "nested member count or uniqueness failure")
            expanded = sum(x.file_size for x in members)
            total_expanded += expanded
            require(expanded <= 256 * 1024 * 1024 and total_expanded <= 1024 * 1024 * 1024,
                    "nested library expansion exceeds bound")
            candidates = [x for x in members if not x.is_dir() and notice_kind(x.filename)]
            require(len(candidates) <= 128 and sum(x.file_size for x in candidates) <= 4 * 1024 * 1024,
                    "notice candidate bytes exceed bound")
            counts = {"license": 0, "notice": 0}
            for candidate in candidates:
                # Read candidate bytes for CRC and nonempty check; never emit the text.
                if library.read(candidate).strip():
                    counts[notice_kind(candidate.filename)] += 1
            poms = sum(1 for x in members if not x.is_dir()
                       and x.filename.startswith("META-INF/maven/") and x.filename.endswith("/pom.xml"))
            with_license += bool(counts["license"])
            with_notice += bool(counts["notice"])
            with_pom += bool(poms)
            print(f"COURSE_A_LIBRARY_NOTICE: {name} sha256={hashlib.sha256(raw).hexdigest()} "
                  f"license_candidates={counts['license']} notice_candidates={counts['notice']} embedded_poms={poms}")
    outer_candidates = [x for x in entries if not x.is_dir() and notice_kind(x.filename)]
    require(len(outer_candidates) <= 128 and sum(x.file_size for x in outer_candidates) <= 4 * 1024 * 1024,
            "outer notice candidate bytes exceed bound")
    outer_count = sum(bool(jar_zip.read(x).strip()) for x in outer_candidates)
    js = sorted(x for x in referenced_assets if x.endswith(".js"))
    markers = 0
    for asset in js:
        data = jar_zip.read("BOOT-INF/classes/static/" + asset.lstrip("/"))
        markers += data.lower().count(b"@license") + data.lower().count(b"@preserve")
    sidecars = sum(1 for x in entries if not x.is_dir()
                   and x.filename.startswith("BOOT-INF/classes/static/")
                   and re.search(r"(?:license|licence|notice|copyright)", x.filename.rsplit("/", 1)[-1], re.I))
    print(f"COURSE_A_NOTICE_INVENTORY_OK: libraries={len(libs)} "
          f"with_license_candidates={with_license} with_notice_candidates={with_notice} "
          f"with_embedded_pom={with_pom} outer_candidates={outer_count} "
          f"referenced_js={len(js)} js_comment_markers={markers} static_notice_sidecars={sidecars} "
          "legal_review=NOT_RUN actual_license_text_classification=NOT_RUN")

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
        require(hashlib.sha256(jar).hexdigest() == "773f3418d72ad066ace066c35bc359a4eeec1ec18c4b9a73d8b3adcdc8889d3e",
                "previously verified inner digest mismatch")
        inventory_notices(inner, assets)
        print("COURSE_A_EXISTING_ARTIFACT_BYTES_OK:"
              f"outer_sha256={EXPECTED} outer_bytes={len(raw)} jar_sha256={hashlib.sha256(jar).hexdigest()} "
              f"jar_bytes={len(jar)} jar_entries={len(entries)} java8_application_classes={len(classes)} "
              f"referenced_assets={len(assets)} crc_verified=true runtime_not_run=true")
if __name__ == "__main__":
    require(len(sys.argv) == 2, "expected exact artifact ZIP path")
    path = pathlib.Path(sys.argv[1])
    require(path.is_file() and not path.is_symlink() and path.stat().st_size == 37371675, "artifact size/path mismatch")
    inspect(path.read_bytes())

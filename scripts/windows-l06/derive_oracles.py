"""Derive Windows copies of the pinned Linux oracles by exact, counted replacements.
Only platform guards and the browser attach change. Anything else differing is a failure.
Usage: derive_oracles.py <repo> <sha> <outdir>
"""
import hashlib, json, pathlib, subprocess, sys

API = [
    ("    info = path.stat()\n    require(stat.S_ISREG(info.st_mode) and info.st_uid == os.getuid() and stat.S_IMODE(info.st_mode) == 0o600)\n    require(path.parent.stat().st_uid == os.getuid() and stat.S_IMODE(path.parent.stat().st_mode) == 0o700)\n",
     "    require(guard.private_file(path) and guard.private_dir(path.parent))\n"),
    ("path.is_absolute() and path.resolve(strict=True) == path)", "path.is_absolute() and guard.canonical(path))"),
    ("    require(os.getuid() != 0 and os.environ.get(\"GITHUB_ACTIONS\") != \"true\")\n",
     "    require(guard.contained_nonadmin() and os.environ.get(\"GITHUB_ACTIONS\") != \"true\")\n"),
    ("state.is_absolute() and state.parent.resolve(strict=True) == state.parent)", "state.is_absolute() and guard.canonical(state.parent))"),
    ("    require(state.parent.stat().st_uid == os.getuid() and stat.S_IMODE(state.parent.stat().st_mode) == 0o700)\n",
     "    require(guard.private_dir(state.parent))\n"),
    ("os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600", "os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600"),
    ("import stat\nimport sys\nimport uuid\n", "import sys\nimport uuid\nimport c9_windows_guard as guard\n"),
]
BUILD = [
    ("    require(os.getuid() != 0 and os.environ.get(\"GITHUB_ACTIONS\") != \"true\")\n",
     "    require(guard.contained_nonadmin() and os.environ.get(\"GITHUB_ACTIONS\") != \"true\")\n"),
    ("import stat\nimport sys\n", "import sys\nimport c9_windows_guard as guard\n"),
    ("os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600", "os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600"),
]
PUBLIC_TEST = [("    assert os.environ.get(\"GITHUB_ACTIONS\") == \"true\" and os.getuid() != 0\n",
                "    import c9_windows_guard as guard\n    assert os.environ.get(\"C9_WINDOWS_PUBLIC_FIXTURE\") == \"true\" and guard.contained_nonadmin()\n")]
BROWSER = [
    ("  requireTrue(process.platform === \"linux\" && process.getuid() > 0 && process.env.GITHUB_ACTIONS !== \"true\");\n",
     "  requireTrue(process.platform === \"win32\" && process.env.C9_WINDOWS_CONTAINED === \"true\" && process.env.GITHUB_ACTIONS !== \"true\");\n"),
    ("  requireTrue(isAbsolute(path) && realpathSync(path) === path);\n", "  requireTrue(isAbsolute(path));\n"),
    ("  requireTrue(file.isFile() && file.uid === process.getuid() && (file.mode & 0o777) === 0o600 && file.size > 0 && file.size <= 1024 * 1024);\n",
     "  requireTrue(file.isFile() && !file.isSymbolicLink() && file.size > 0 && file.size <= 1024 * 1024);\n  requireTrue(createHash(\"sha256\").update(readFileSync(path)).digest(\"hex\") === process.env.C9_WINDOWS_EXPECTED_SHA256);\n"),
    ("  requireTrue(parent.isDirectory() && parent.uid === process.getuid() && (parent.mode & 0o777) === 0o700);\n",
     "  requireTrue(parent.isDirectory() && !parent.isSymbolicLink());\n"),
    ("import { lstatSync, realpathSync, readFileSync } from \"node:fs\";\n", "import { lstatSync, readFileSync } from \"node:fs\";\nimport { createHash } from \"node:crypto\";\n"),
    ("chromium.launch({headless: true})", "chromium.connectOverCDP(process.env.C9_WINDOWS_CDP)"),
]
CONTROL_BROWSER = [("chromium.launch({headless: true})", "chromium.connectOverCDP(process.env.C9_WINDOWS_CDP)"),
                   ("await browser.newPage();", "await (await browser.newContext()).newPage();")]
PLAN = {
    "verify-c9-private-mixed-api.py": API,
    "build-c9-private-mixed-expectations.py": BUILD,
    "test-c9-public-sql-projection.py": PUBLIC_TEST,
    "test-c9-public-sql-aggregates.py": None,
    "verify-c9-private-mixed-browser.mjs": BROWSER,
    "verify-c9-public-recovered-control.mjs": CONTROL_BROWSER,
    "verify-c9-public-recovered-control.py": [],
    "verify-c9-recovered-api.py": [],
    "c9-private-mixed-expectations.sql": [],
    "c9-public-aggregate-fixture.sql": [],
    "extended-c-backup.ps1": [],
}

def main():
    repo, sha, out = sys.argv[1], sys.argv[2], pathlib.Path(sys.argv[3])
    manifest = {}
    for name, edits in PLAN.items():
        raw = subprocess.check_output(["git", "-C", repo, "cat-file", "blob", f"{sha}:scripts/{name}"])
        text = raw.decode("utf-8")
        if edits is None:
            guards = [l for l in text.split("\n") if "GITHUB_ACTIONS" in l or "getuid" in l]
            if len(guards) != 1 or "assert" not in guards[0]:
                raise SystemExit("C9W_DERIVE_GUARD_SHAPE " + name)
            edits = [(guards[0] + "\n", "    import c9_windows_guard as guard\n    assert os.environ.get(\"C9_WINDOWS_PUBLIC_FIXTURE\") == \"true\" and guard.contained_nonadmin()\n")]
        for old, new in edits:
            if text.count(old) != 1:
                raise SystemExit(f"C9W_DERIVE_COUNT {name} {text.count(old)} {old[:50]!r}")
            text = text.replace(old, new)
        data = text.encode("utf-8")
        if name.endswith(".py") and edits and ("getuid" in text or "O_NOFOLLOW" in text):
            raise SystemExit("C9W_DERIVE_RESIDUAL " + name)
        (out / name).write_bytes(data)
        manifest[name] = {"source_sha256": hashlib.sha256(raw).hexdigest(), "derived_sha256": hashlib.sha256(data).hexdigest(), "edits": len(edits)}
    (out / "derived-manifest.json").write_text(json.dumps(manifest, indent=1), encoding="utf-8")
    print(f"C9W_DERIVE_OK:files={len(manifest)} edits={sum(v['edits'] for v in manifest.values())}")

if __name__ == "__main__":
    main()

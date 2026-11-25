"""Browser tooling must match the pinned lockfile; prints nothing on success."""
import json, pathlib, subprocess, sys
repo, sha, modules, chromium = sys.argv[1:]
try:
    lock = json.loads(subprocess.check_output(["git", "-C", repo, "show", f"{sha}:extended/frontend/package-lock.json"]))
    for name in ("@playwright/test", "playwright", "playwright-core"):
        want = lock["packages"]["node_modules/" + name]["version"]
        have = json.loads((pathlib.Path(modules) / name / "package.json").read_text(encoding="utf-8"))["version"]
        if want != have: raise ValueError(name)
    rev = next(b["revision"] for b in json.loads((pathlib.Path(modules) / "playwright-core/browsers.json").read_text(encoding="utf-8"))["browsers"] if b["name"] == "chromium-headless-shell")
    if pathlib.Path(chromium).name != f"chromium_headless_shell-{rev}": raise ValueError("chromium")
except Exception:
    sys.exit(1)

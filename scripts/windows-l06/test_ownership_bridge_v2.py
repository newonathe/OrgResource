"""Fictional v2 transport tests, not native publication evidence."""
import base64
import hashlib
import json
from pathlib import Path
import subprocess
import py_compile
import shutil
import sys
import tempfile
import unittest
import ownership_journal as v1
from ownership_bridge_v2 import SCHEMA, TRANSPORT, transform

CTX = {"userSid": "S-1-5-21-1-2-3-1001", "authenticationId": "0000000000001234",
       "sessionId": 2, "logonTime": "134000000000000000", "bootId": None}
def fid(n):
    return "0000000000000001:" + f"{n:032x}"
REQ = {"schema": SCHEMA, "operation": "init", "token": "abcdef012345",
       "root": r"C:\fictional", "rootId": fid(1), "controlId": fid(2), "context": CTX}


def env(req, current=None):
    return json.dumps({"schema": TRANSPORT, "request": req, "current": current},
                      separators=(",", ":")).encode("ascii")


def call(req, current=None):
    return json.loads(transform(env(req, current)))


class V2TransportTests(unittest.TestCase):
    def step(self, previous, resource, state, fields):
        return call(dict(REQ, operation="transition", expectedSha256=previous["response"]["sha256"],
                         resource=resource, state=state, fields=fields), previous["replacement"])

    def test_init_read_exact_snapshot(self):
        first = call(REQ)
        raw = base64.b64decode(first["replacement"])
        self.assertEqual(first["response"]["sha256"], hashlib.sha256(raw).hexdigest())
        read = call(dict(REQ, operation="read"), first["replacement"])
        self.assertIsNone(read["replacement"])
        self.assertEqual(read["response"]["journal"], json.loads(raw))
        self.assertIsNone(read["response"]["journal"]["context"]["bootId"])

    def test_work_and_profile_bound_transitions(self):
        a = self.step(call(REQ), "work", "intent", {})
        a = self.step(a, "work", "created", {"id": fid(3)})
        a = self.step(a, "source", "intent", {"name": "orgresource.c9.000000000001"})
        storage = {"packagesPath": r"C:\fictional\Packages", "packagesId": fid(4),
                   "packageId": fid(5), "appDataId": fid(6)}
        a = self.step(a, "source", "created", {"sid": "S-1-15-2-1-2-3-4-5-6-7", "storage": storage})
        a = self.step(a, "source", "removing", {})
        a = self.step(a, "source", "removed", {"evidence": {"kind": "api_success_and_package_absent",
                                                           "hresult": 0, "packagesId": fid(4)}})
        rec = json.loads(base64.b64decode(a["replacement"]))
        self.assertEqual(rec["profileStorage"]["source"], storage)
        self.assertEqual(rec["ownership"]["profiles"]["source"]["state"], "removed")

    def test_stale_and_context_mismatch(self):
        first = call(REQ)
        second = self.step(first, "work", "intent", {})
        stale = dict(REQ, operation="transition", expectedSha256=first["response"]["sha256"],
                     resource="work", state="created", fields={"id": fid(3)})
        with self.assertRaises(ValueError):
            call(stale, second["replacement"])
        for k, v in (("sessionId", 3), ("bootId", "1"*32), ("logonTime", "134000000000000001")):
            with self.assertRaises(ValueError):
                call(dict(REQ, operation="read", context=dict(CTX, **{k:v})), first["replacement"])

    def test_legacy_refusal_no_conversion(self):
        old = v1.encode(v1.fresh(REQ["token"], REQ["root"], REQ["rootId"], REQ["controlId"]))
        encoded = base64.b64encode(old).decode("ascii")
        for op in ("init", "read"):
            with self.assertRaises(ValueError):
                call(dict(REQ, operation=op), encoded)
        self.assertEqual(v1.encode(v1.parse(old)), old)

    def test_mapping_recovery_work_removal_refused(self):
        a = self.step(call(REQ), "work", "intent", {})
        a = self.step(a, "work", "created", {"id": fid(3)})
        for resource, state, fields in (("mapping", "intent", {"drive": "Q:"}), ("work", "removing", {})):
            with self.assertRaises(ValueError):
                self.step(a, resource, state, fields)
        for op in ("plan", "compare", "upgrade"):
            with self.assertRaises(ValueError):
                call(dict(REQ, operation=op), a["replacement"])

    def test_strict_envelope_and_canonical_snapshot(self):
        a = call(REQ)
        raw = base64.b64decode(a["replacement"])
        for bad in (env(REQ)[:-1]+b',"Current":null}', env(REQ)[:-1]+b',"path":"x"}',
                    env(REQ, "!!!!"), b" " * 65537):
            with self.assertRaises(ValueError):
                transform(bad)
        with self.assertRaises(ValueError):
            call(dict(REQ, operation="read"), base64.b64encode(raw+b" ").decode("ascii"))

    def test_isolated_cli_no_file_transport(self):
        script = Path(__file__).resolve().with_name("ownership_bridge_v2.py")
        with tempfile.TemporaryDirectory() as root:
            cmd = [sys.executable, "-I", "-S", "-B", str(script)]
            proc = subprocess.run(cmd, input=env(REQ), cwd=root, capture_output=True, timeout=15)
            self.assertEqual(proc.returncode, 0, proc.stderr)
            self.assertEqual(proc.stderr, b"")
            self.assertEqual(json.loads(proc.stdout), call(REQ))
            refused = subprocess.run(cmd+[root], input=env(REQ), cwd=root, capture_output=True, timeout=15)
            self.assertEqual(refused.returncode, 1)
            self.assertEqual(refused.stdout, b"")
            self.assertEqual(refused.stderr, b"C9W_JOURNAL_V2_BRIDGE_REFUSED\n")
            self.assertEqual(list(Path(root).iterdir()), [])


    def test_sibling_unchecked_hash_cache_is_not_executed(self):
        source_dir = Path(__file__).resolve().parent
        with tempfile.TemporaryDirectory() as root:
            root = Path(root)
            for name in ("ownership_bridge_v2.py", "ownership_journal.py", "ownership_journal_v2.py"):
                shutil.copyfile(source_dir/name, root/name)
            for name in ("ownership_journal.py", "ownership_journal_v2.py"):
                path = root/name
                original = path.read_bytes()
                path.write_text("raise RuntimeError('fictional cached module')\n", encoding="ascii")
                py_compile.compile(str(path), doraise=True,
                                   invalidation_mode=py_compile.PycInvalidationMode.UNCHECKED_HASH)
                path.write_bytes(original)
            proc = subprocess.run([sys.executable, "-I", "-S", "-B", str(root/"ownership_bridge_v2.py")],
                                  input=env(REQ), cwd=root, capture_output=True, timeout=15)
            self.assertEqual(proc.returncode, 0, proc.stderr)
            self.assertEqual(json.loads(proc.stdout), call(REQ))


if __name__ == "__main__":
    unittest.main()

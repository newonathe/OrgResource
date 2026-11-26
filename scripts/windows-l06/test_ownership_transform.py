"""Pure CLI transport tests; no native lock or durability proof."""
import base64
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from ownership_bridge import SCHEMA, TRANSPORT, transform

REQ = {
    "schema": SCHEMA, "operation": "init", "token": "a123456789ab",
    "root": r"C:\fictional", "rootId": "0000000000000001:" + "1" * 32,
    "controlId": "0000000000000001:" + "2" * 32,
}


def envelope(request, current=None):
    return json.dumps({"schema": TRANSPORT, "request": request, "current": current},
                      separators=(",", ":")).encode("ascii")


class PureTransportTest(unittest.TestCase):
    def test_init_and_read_pure(self):
        initial = json.loads(transform(envelope(REQ)))
        raw = base64.b64decode(initial["replacement"], validate=True)
        self.assertEqual(initial["response"]["sha256"], hashlib.sha256(raw).hexdigest())
        read = json.loads(transform(envelope(dict(REQ, operation="read"), initial["replacement"])))
        self.assertIsNone(read["replacement"])
        self.assertEqual(read["response"]["journal"]["token"], REQ["token"])

    def test_transition_stale_refusal(self):
        init = json.loads(transform(envelope(REQ)))
        request = dict(REQ, operation="transition", resource="work", state="intent",
                       fields={}, expectedSha256=init["response"]["sha256"])
        next_ = json.loads(transform(envelope(request, init["replacement"])))
        self.assertNotEqual(init["replacement"], next_["replacement"])
        with self.assertRaises(ValueError):
            transform(envelope(request, next_["replacement"]))

    def test_strict_envelope(self):
        raw = envelope(REQ)
        for bad in (raw[:-1] + b',"Current":null}', raw[:-1] + b',"path":"x"}',
                    envelope(REQ, "!!!!"), b"x" * 65537):
            with self.subTest(bad=bad[:30]), self.assertRaises((ValueError, TypeError)):
                transform(bad)

    def test_cli_no_path_writes_and_old_args_refused(self):
        script = Path(__file__).resolve().with_name("ownership_bridge.py")
        with tempfile.TemporaryDirectory() as root:
            # CLI accepts only bytes and cannot use cwd or argument as a journal destination.
            proc = subprocess.run([sys.executable, "-I", "-B", str(script)], input=envelope(REQ),
                                  cwd=root, capture_output=True, timeout=15)
            self.assertEqual(proc.returncode, 0, proc.stderr)
            self.assertEqual(proc.stderr, b"")
            self.assertEqual(json.loads(proc.stdout), json.loads(transform(envelope(REQ))))
            self.assertEqual(list(Path(root).iterdir()), [])
            refused = subprocess.run([sys.executable, "-I", "-B", str(script), root],
                                     input=envelope(REQ), cwd=root, capture_output=True, timeout=15)
            self.assertEqual(refused.returncode, 1)
            self.assertEqual(refused.stdout, b"")
            self.assertEqual(refused.stderr, b"C9W_JOURNAL_BRIDGE_REFUSED\n")
            self.assertEqual(list(Path(root).iterdir()), [])


if __name__ == "__main__":
    unittest.main()

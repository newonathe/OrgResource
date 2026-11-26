"""Portable fictional bridge cases; no native ownership or OS lock proof."""
import copy
import json
from pathlib import Path
import tempfile
import unittest
from ownership_bridge import execute_request, parse_request, SCHEMA

BASE = {
    "schema": SCHEMA, "token": "a123456789ab", "root": r"C:\fictional",
    "rootId": "0000000000000001:" + "1" * 32,
    "controlId": "0000000000000001:" + "2" * 32,
}
WORK_ID = "0000000000000001:" + "3" * 32


class BridgeTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.path = Path(self.tmp.name) / "ownership.json"

    def invoke(self, operation, **fields):
        return execute_request(dict(BASE, operation=operation, **fields), self.path)

    def test_init_read_and_transitions(self):
        first = self.invoke("init")["sha256"]
        self.assertEqual(self.invoke("read")["sha256"], first)
        second = self.invoke("transition", expectedSha256=first, resource="work",
                             state="intent", fields={})["sha256"]
        third = self.invoke("transition", expectedSha256=second, resource="work",
                            state="created", fields={"id": WORK_ID})["sha256"]
        self.assertEqual(self.invoke("read")["journal"]["work"]["id"], WORK_ID)
        self.assertNotEqual(first, third)

    def test_refuses_stale_and_preserves_exact_bytes(self):
        first = self.invoke("init")["sha256"]
        self.invoke("transition", expectedSha256=first, resource="work", state="intent", fields={})
        before = self.path.read_bytes()
        with self.assertRaises(ValueError):
            self.invoke("transition", expectedSha256=first, resource="work", state="created", fields={"id": WORK_ID})
        self.assertEqual(self.path.read_bytes(), before)

    def test_refuses_second_init(self):
        self.invoke("init")
        before = self.path.read_bytes()
        with self.assertRaises(ValueError):
            self.invoke("init")
        self.assertEqual(self.path.read_bytes(), before)

    def test_refuses_missing_record(self):
        with self.assertRaises(ValueError):
            self.invoke("read")
        self.assertFalse(self.path.exists())

    def test_identity_mismatch_preserves(self):
        self.invoke("init")
        before = self.path.read_bytes()
        for field, value in (("token", "b123456789ab"), ("root", r"C:\other"),
                             ("rootId", "0000000000000001:" + "4" * 32),
                             ("controlId", "0000000000000001:" + "5" * 32)):
            with self.subTest(field=field):
                req = dict(BASE, operation="read")
                req[field] = value
                with self.assertRaises(ValueError):
                    execute_request(req, self.path)
                self.assertEqual(self.path.read_bytes(), before)

    def test_unknown_and_duplicate_fields(self):
        raw = json.dumps(dict(BASE, operation="init")).encode()
        self.assertEqual(parse_request(raw)["operation"], "init")
        for bad in (raw[:-1] + b',"Token":"other"}', raw[:-1] + b',"extra":1}',
                    raw[:-1] + b',"token":"other"}'):
            with self.assertRaises(ValueError):
                parse_request(bad)

    def test_refuses_invalid_transition_without_write(self):
        sha = self.invoke("init")["sha256"]
        before = self.path.read_bytes()
        for fields in ({"id": WORK_ID}, {"j": {}}, {"state": "created"}):
            with self.subTest(fields=fields), self.assertRaises(ValueError):
                self.invoke("transition", expectedSha256=sha, resource="work", state="created", fields=fields)
        self.assertEqual(self.path.read_bytes(), before)

    def test_plan_is_read_only_and_ambiguous_intent_refuses(self):
        sha = self.invoke("init")["sha256"]
        sha = self.invoke("transition", expectedSha256=sha, resource="work", state="intent", fields={})["sha256"]
        observed = {k: BASE[k] for k in ("token", "root", "rootId", "controlId")}
        observed.update(workId=WORK_ID, profiles={}, mapping=None)
        before = self.path.read_bytes()
        with self.assertRaises(ValueError):
            self.invoke("plan", expectedSha256=sha, observed=observed)
        self.assertEqual(self.path.read_bytes(), before)
        sha = self.invoke("transition", expectedSha256=sha, resource="work", state="created",
                          fields={"id": WORK_ID})["sha256"]
        before = self.path.read_bytes()
        self.assertEqual(self.invoke("plan", expectedSha256=sha, observed=observed)["plan"],
                         {"profiles": {}, "mapping": None, "workDeletion": False})
        self.assertEqual(self.path.read_bytes(), before)

    def test_refuses_corrupt_journal(self):
        self.path.write_bytes(b'{"broken":true}')
        with self.assertRaises(ValueError):
            self.invoke("init")
        self.assertEqual(self.path.read_bytes(), b'{"broken":true}')


if __name__ == "__main__":
    unittest.main()

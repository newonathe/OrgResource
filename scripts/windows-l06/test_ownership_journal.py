import copy
import json
from pathlib import Path
import tempfile
import unittest
import ownership_journal as o


class JournalTests(unittest.TestCase):
    def setUp(self):
        self.token = "abcdef012345"
        self.root = r"C:\fictional\private"
        self.ids = [f"{i:016x}:{i:032x}" for i in (1, 2, 3)]
        self.sid = "S-1-15-2-1-2-3-4-5-6-7"
        self.name = "orgresource.c9.000000000001"
        self.j = o.fresh(self.token, self.root, *self.ids[:2])

    def created(self):
        j = o.transition(self.j, "work", "intent")
        j = o.transition(j, "work", "created", id=self.ids[2])
        j = o.transition(j, "source", "intent", name=self.name)
        j = o.transition(j, "source", "created", sid=self.sid)
        j = o.transition(j, "mapping", "intent", drive="Q:")
        return o.transition(j, "mapping", "created")

    def observations(self):
        return {"token": self.token, "root": self.root, "rootId": self.ids[0], "controlId": self.ids[1],
                "workId": self.ids[2], "profiles": {self.name: self.sid},
                "mapping": {"drive": "Q:", "target": self.root + "\\orgresource-c9w-" + self.token}}

    def test_roundtrip(self):
        self.assertEqual(o.parse(o.encode(self.created())), self.created())

    def test_exact_selection(self):
        p = o.recovery_plan(self.created(), self.observations())
        self.assertEqual(p, {"profiles": {self.name: self.sid}, "mapping": self.observations()["mapping"], "workDeletion": False})

    def test_unknown_profile(self):
        obs = self.observations(); obs["profiles"]["orgresource.c9.000000000002"] = self.sid
        with self.assertRaises(ValueError): o.recovery_plan(self.created(), obs)

    def test_missing_profile(self):
        obs = self.observations(); obs["profiles"] = {}
        with self.assertRaises(ValueError): o.recovery_plan(self.created(), obs)

    def test_wrong_sid(self):
        obs = self.observations(); obs["profiles"][self.name] += "8"
        with self.assertRaises(ValueError): o.recovery_plan(self.created(), obs)

    def test_mapping_replaced(self):
        obs = self.observations(); obs["mapping"]["target"] = r"C:\unrelated"
        with self.assertRaises(ValueError): o.recovery_plan(self.created(), obs)

    def test_identity_substitutions(self):
        for k in ("token", "root", "rootId", "controlId", "workId"):
            with self.subTest(k=k):
                obs = self.observations(); obs[k] = "wrong"
                with self.assertRaises(ValueError): o.recovery_plan(self.created(), obs)

    def test_ambiguous_intent(self):
        j = o.transition(self.created(), "restore", "intent", name="orgresource.c9.000000000002")
        with self.assertRaises(ValueError): o.recovery_plan(j, self.observations())

    def test_ambiguous_removal(self):
        j = o.transition(self.created(), "source", "removing")
        with self.assertRaises(ValueError): o.recovery_plan(j, self.observations())

    def test_partial_cleanup(self):
        j = o.transition(self.created(), "source", "removing")
        j = o.transition(j, "source", "removed")
        obs = self.observations(); obs["profiles"] = {}
        self.assertEqual(o.recovery_plan(j, obs)["profiles"], {})
        with self.assertRaises(ValueError): o.recovery_plan(j, self.observations())

    def test_completed(self):
        j = self.created()
        for r in ("source", "mapping", "work"):
            j = o.transition(j, r, "removing"); j = o.transition(j, r, "removed")
        obs = self.observations(); obs.update(workId=None, profiles={}, mapping=None)
        self.assertEqual(o.recovery_plan(j, obs), {"profiles": {}, "mapping": None, "workDeletion": False})

    def test_no_skip_or_rebind(self):
        for args in ((self.j,"work","created",{"id":self.ids[2]}),(self.created(),"source","removing",{"name":"changed"}),(self.created(),"work","removing",{})):
            with self.assertRaises(ValueError): o.transition(*args[:3], **args[3])

    def test_duplicate_json(self):
        raw = o.encode(self.created())
        for key in (b"token", b"Token"):
            with self.assertRaises(ValueError): o.parse(b'{"'+key+b'":"bad",'+raw[1:])

    def test_malformed(self):
        for raw in (b"", b"{", b"[]", b"\xff", b" "*16385):
            with self.assertRaises(ValueError): o.parse(raw)

    def test_wrong_shape(self):
        for field in ("schema", "token", "mapping", "profiles"):
            j = self.created(); del j[field]
            with self.assertRaises(ValueError): o.validate(j)
        j = self.created(); j["extra"] = True
        with self.assertRaises(ValueError): o.validate(j)

    def test_duplicate_profile_identity(self):
        j = self.created(); j["profiles"]["restore"] = copy.deepcopy(j["profiles"]["source"])
        with self.assertRaises(ValueError): o.validate(j)

    def test_bad_root(self):
        for root in (r"C:\foo\..\bar", r"\\server\share", r"C:\foo\bar.", "C:\\", "relative"):
            with self.assertRaises(ValueError): o.fresh(self.token, root, *self.ids[:2])

    def test_atomic_snapshot(self):
        with tempfile.TemporaryDirectory() as tmp:
            p = Path(tmp)/"journal.json"
            o.save(p, self.j, initial=True)
            with self.assertRaises(FileExistsError): o.save(p, self.created(), initial=True)
            self.assertEqual(o.parse(p.read_bytes()), self.j)
            o.save(p, self.created())
            self.assertEqual(o.parse(p.read_bytes()), self.created())
            self.assertEqual(list(Path(tmp).iterdir()), [p])


if __name__ == "__main__": unittest.main()

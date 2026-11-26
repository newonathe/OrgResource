"""Fictional v2 data-contract tests; no native or publication proof."""
import copy
import json
import unittest
import ownership_journal as v1
import ownership_journal_v2 as v2


def fid(n, volume=1):
    return f"{volume:016x}:{n:032x}"


CTX = {"userSid": "S-1-5-21-1-2-3-1001", "authenticationId": "0000000000001234",
       "sessionId": 2, "logonTime": "134000000000000000", "bootId": "1"*32}
ROOT = r"C:\fictional\private"
TOKEN = "abcdef012345"
NAME = "orgresource.c9.000000000001"
SID = "S-1-15-2-1-2-3-4-5-6-7"
STORAGE = {"packagesPath": r"C:\fictional\Packages", "packagesId": fid(4),
           "packageId": fid(5), "appDataId": fid(6)}


class DurableBindingTests(unittest.TestCase):
    def initial(self, context=None):
        return v2.fresh(TOKEN, ROOT, fid(1), fid(2), CTX if context is None else context)

    def step(self, j, resource, state, **fields):
        return v2.transition(j, j["context"], resource, state, **fields)

    def work(self, context=None):
        j = self.step(self.initial(context), "work", "intent")
        return self.step(j, "work", "created", id=fid(3))

    def created(self, context=None):
        j = self.step(self.work(context), "source", "intent", name=NAME)
        j = self.step(j, "source", "created", sid=SID, storage=STORAGE)
        j = self.step(j, "mapping", "intent", drive="Q:")
        return self.step(j, "mapping", "created",
                         observedTargets=[j["mappingBinding"]["target"]], mappedWorkId=fid(3))

    def observed(self, j):
        base = j["ownership"]
        result = {k: base[k] for k in ("token", "root", "rootId", "controlId")}
        result.update(context=copy.deepcopy(j["context"]),
                      workId=fid(3) if base["work"]["state"] == "created" else None,
                      profiles={}, mapping=None)
        for role, p in base["profiles"].items():
            if p["state"] == "created":
                result["profiles"][p["name"]] = {"sid": p["sid"], "storage": copy.deepcopy(j["profileStorage"][role])}
        if base["mapping"]["state"] == "created":
            result["mapping"] = dict(copy.deepcopy(j["mappingBinding"]), drive=base["mapping"]["drive"])
        return result

    def test_roundtrip_no_effect_authority(self):
        j = self.created()
        self.assertEqual(v2.parse(v2.encode(j)), j)
        self.assertEqual(v2.compare_observation(j, self.observed(j)),
                         {"identityComparison": "MATCH", "effectsAuthorized": False, "workDeletion": False,
                          "completeProfileAbsenceProven": False})

    def test_legacy_refused_not_rewritten(self):
        old = v1.encode(v1.fresh(TOKEN, ROOT, fid(1), fid(2)))
        with self.assertRaises(ValueError):
            v2.parse(old)
        self.assertEqual(v1.encode(v1.parse(old)), old)
        with self.assertRaises(ValueError):
            v1.parse(v2.encode(self.initial()))

    def test_missing_boot_witness_refuses_comparison(self):
        j = self.created(dict(CTX, bootId=None))
        self.assertIsNone(v2.parse(v2.encode(j))["context"]["bootId"])
        with self.assertRaises(ValueError):
            v2.compare_observation(j, self.observed(j))

    def test_context_mismatch_refuses_transition_and_compare(self):
        j = self.created()
        before = v2.encode(j)
        for key, value in {"bootId": "2"*32, "authenticationId": "0000000000001235",
                           "userSid": "S-1-5-21-1-2-3-1002", "sessionId": 3,
                           "logonTime": "134000000000000001"}.items():
            with self.subTest(key=key):
                ctx = dict(CTX, **{key: value})
                obs = self.observed(j)
                obs["context"] = ctx
                with self.assertRaises(ValueError):
                    v2.compare_observation(j, obs)
                with self.assertRaises(ValueError):
                    v2.transition(j, ctx, "source", "removing")
                self.assertEqual(v2.encode(j), before)

    def test_invalid_context_types(self):
        for key, value in (("sessionId", True), ("sessionId", 0), ("logonTime", 123),
                           ("logonTime", "0"), ("bootId", "0"*32),
                           ("userSid", "S-1-5-0001"), ("authenticationId", "0"*16)):
            with self.subTest(key=key, value=value), self.assertRaises(ValueError):
                self.initial(dict(CTX, **{key: value}))

    def test_profile_created_requires_actual_storage_fields(self):
        j = self.step(self.work(), "source", "intent", name=NAME)
        with self.assertRaises(ValueError):
            self.step(j, "source", "created", sid=SID)
        for bad in (dict(STORAGE, packageId=fid(3)), dict(STORAGE, appDataId=fid(5)),
                    dict(STORAGE, packageId=fid(5, 2)), dict(STORAGE, packagesPath=r"C:\fictional\Other")):
            with self.subTest(bad=bad), self.assertRaises(ValueError):
                self.step(j, "source", "created", sid=SID, storage=bad)

    def test_profile_identity_substitution_and_unknown_refused(self):
        j = self.created()
        for key in ("packagesId", "packageId", "appDataId"):
            obs = self.observed(j)
            obs["profiles"][NAME]["storage"][key] = fid(90)
            with self.assertRaises(ValueError):
                v2.compare_observation(j, obs)
        obs = self.observed(j)
        obs["profiles"]["orgresource.c9.000000000002"] = copy.deepcopy(obs["profiles"][NAME])
        with self.assertRaises(ValueError):
            v2.compare_observation(j, obs)

    def test_mapping_requires_exact_single_target_and_actual_work_id(self):
        j = self.step(self.work(), "mapping", "intent", drive="Q:")
        target = j["mappingBinding"]["target"]
        for stack, fid_ in (([], fid(3)), ([target, target], fid(3)),
                            ([r"\??\C:\foreign"], fid(3)), ([target], fid(9))):
            with self.subTest(stack=stack, fid=fid_), self.assertRaises(ValueError):
                self.step(j, "mapping", "created", observedTargets=stack, mappedWorkId=fid_)
        good = self.step(j, "mapping", "created", observedTargets=[target], mappedWorkId=fid(3))
        obs = self.observed(good)
        obs["mapping"]["observedTargets"].append(target)
        with self.assertRaises(ValueError):
            v2.compare_observation(good, obs)

    def test_uncertain_intent_or_removal_refuses_all(self):
        for j in (self.step(self.work(), "source", "intent", name=NAME),
                  self.step(self.created(), "source", "removing"),
                  self.step(self.work(), "mapping", "intent", drive="Q:"),
                  self.step(self.created(), "mapping", "removing")):
            with self.assertRaises(ValueError):
                v2.compare_observation(j, self.observed(j))

    def test_removed_requires_bound_observation_not_api_success_alone(self):
        j = self.step(self.created(), "source", "removing")
        for evidence in (None, {"hresult": 0},
                         {"kind": "api_success_and_package_absent", "hresult": False, "packagesId": fid(4)},
                         {"kind": "api_success_and_package_absent", "hresult": 0, "packagesId": fid(90)}):
            with self.subTest(evidence=evidence), self.assertRaises(ValueError):
                self.step(j, "source", "removed", evidence=evidence)
        done = self.step(j, "source", "removed",
                         evidence={"kind": "api_success_and_package_absent", "hresult": 0, "packagesId": fid(4)})
        self.assertFalse(v2.compare_observation(done, self.observed(done))["completeProfileAbsenceProven"])

    def test_cleanup_order_and_bound_absence(self):
        j = self.created()
        with self.assertRaises(ValueError):
            self.step(j, "work", "removing")
        j = self.step(j, "source", "removing")
        j = self.step(j, "source", "removed", evidence={"kind":"api_success_and_package_absent","hresult":0,"packagesId":fid(4)})
        j = self.step(j, "mapping", "removing")
        binding = j["mappingBinding"]
        evidence = {"kind": "namespace_absence", "drive": "Q:", "target": binding["target"], "workId": fid(3)}
        with self.assertRaises(ValueError):
            self.step(j, "mapping", "removed", evidence=dict(evidence, drive="R:"))
        j = self.step(j, "mapping", "removed", evidence=evidence)
        j = self.step(j, "work", "removing")
        with self.assertRaises(ValueError):
            self.step(j, "work", "removed", evidence={"kind":"work_absence","id":fid(8)})
        j = self.step(j, "work", "removed", evidence={"kind":"work_absence","id":fid(3)})
        self.assertFalse(v2.compare_observation(j, self.observed(j))["effectsAuthorized"])

    def test_exact_shape_and_duplicate_fields(self):
        raw = v2.encode(self.created())
        for invalid in (b'{"Schema":"bad",'+raw[1:], b'{"schema":"bad",'+raw[1:], b" "*16385):
            with self.assertRaises(ValueError):
                v2.parse(invalid)
        j = self.created()
        j["context"]["extra"] = True
        with self.assertRaises(ValueError):
            v2.validate(j)

    def test_same_volume_and_immutable_inputs(self):
        with self.assertRaises(ValueError):
            v2.fresh(TOKEN, ROOT, fid(1), fid(2, 2), CTX)
        j = self.step(self.initial(), "work", "intent")
        with self.assertRaises(ValueError):
            self.step(j, "work", "created", id=fid(3, 2))
        ctx = copy.deepcopy(CTX)
        fresh = self.initial(ctx)
        ctx["sessionId"] += 1
        self.assertEqual(fresh["context"], CTX)
        self.assertEqual(self.created()["profileStorage"]["source"], STORAGE)


if __name__ == "__main__":
    unittest.main()

"""Fictional observations; no MySQL, GCM authentication or native custody."""
import copy
import json
import unittest
import retention_contract as r
import retention_evidence as e
from test_retention_contract import RetentionContract

class EvidenceTests(unittest.TestCase):
    def setUp(self):
        fixture = RetentionContract()
        fixture.setUp()
        self.m = copy.deepcopy(fixture.m)
        self.m["serializer"]["timeZone"] = "+00:00"
        self.ids = {role: dict(serverUuid=f"00000000-0000-0000-0000-{i:012d}",
            schema="orgresource_c9_local_0123456789ab_"+role, port=24000+i)
            for i,role in enumerate(e.ROLES,1)}
        self.expected = {k:copy.deepcopy(self.m[k]) for k in
            ("scope","runId","sourceSha","harnessSha","jarSha256","serializer",
             "archiveSha256","archiveBytes","postflightReceiptSha256")}
        self.expected["identities"] = copy.deepcopy(self.ids)
        self.before = dict(identity=copy.deepcopy(self.ids["source"]),tables=list(e.TABLES),
                           domainSha256="a"*64, sessionRows=[0,0])
        self.ev = dict(schema="orgresource.c9.retention.evidence.v1",scope="FICTIONAL_TEST",
            runId=self.m["runId"], manifestSha256="", originalBefore=copy.deepcopy(self.before),
            originalAfter=copy.deepcopy(self.before),phases={},
            backupStartedUtc="2026-11-27T00:00:01Z",backupCompletedUtc="2026-11-27T00:00:02Z",
            restoreCompletedUtc="2026-11-27T00:00:03Z",
            finalPostflightReceiptSha256=self.m["postflightReceiptSha256"])
        for name,sec in zip(e.PHASES,(0,4,5,6)):
            self.ev["phases"][name]=dict(identity=copy.deepcopy(
                self.ids["restore" if name.startswith("backup") else "mixed"]),
                tables=list(e.TABLES),counts=dict(zip(e.COUNT_KEYS,e.COUNTS["FICTIONAL_TEST"])),
                domainSha256=self.m["domainSha256"],archiveSha256=self.m["archiveSha256"],
                archiveBytes=self.m["archiveBytes"],serializer=copy.deepcopy(self.m["serializer"]),
                sessionTimeZone="+00:00",snapshotUtc=f"2026-11-27T00:00:0{sec}Z")
        self.raw = r.canonical(self.m)
        self.expected["manifestSha256"]=r.digest(self.raw)
        self.ev["manifestSha256"]=r.digest(self.raw)
    def bind(self, ev=None, expected=None):
        return e.bind(self.raw,r.canonical(self.ev if ev is None else ev),
                      self.expected if expected is None else expected)
    def test_roundtrip(self):
        result=json.loads(self.bind())
        self.assertEqual(result["manifestSha256"],r.digest(self.raw))
        self.assertEqual(result["evidenceSha256"],r.digest(r.canonical(self.ev)))
        self.assertEqual(result["schema"],"orgresource.c9.retention.bound-candidate.v1")
    def test_independent_manifest_pins(self):
        for key,value in (("runId","ffffffffffff"),("sourceSha","f"*40),
                          ("archiveBytes",self.m["archiveBytes"]+1),
                          ("postflightReceiptSha256","f"*64)):
            x=copy.deepcopy(self.expected); x[key]=value
            with self.assertRaises(ValueError): self.bind(expected=x)
    def test_distinct_and_owned_identity(self):
        for key,value in (("serverUuid",self.ids["source"]["serverUuid"]),
                          ("port",self.ids["source"]["port"]),("schema","other"),
                          ("port",True)):
            x=copy.deepcopy(self.expected); x["identities"]["mixed"][key]=value
            with self.assertRaises(ValueError): self.bind(expected=x)
        x=copy.deepcopy(self.ev); x["phases"]["restored"]["identity"]=self.ids["restore"]
        with self.assertRaises(ValueError): self.bind(ev=x)
    def test_exact_inventory_and_counts(self):
        for key,value in (("tables",list(e.TABLES[:-1])),("tables",list(e.TABLES)+[e.TABLES[0]]),
                          ("counts",dict(zip(e.COUNT_KEYS,e.COUNTS["APPROVED_IMPORT"])))):
            x=copy.deepcopy(self.ev); x["phases"]["restored"][key]=value
            with self.assertRaises(ValueError): self.bind(ev=x)
        for k in e.COUNT_KEYS:
            x=copy.deepcopy(self.ev); x["phases"]["backupSourceFinal"]["counts"][k]+=1
            with self.assertRaises(ValueError): self.bind(ev=x)
    def test_full_domain_and_archive_equality(self):
        for name in e.PHASES:
            for key,value in (("domainSha256","f"*64),("archiveSha256","f"*64),
                              ("archiveBytes",self.m["archiveBytes"]+1),
                              ("sessionTimeZone","SYSTEM")):
                x=copy.deepcopy(self.ev); x["phases"][name][key]=value
                with self.assertRaises(ValueError): self.bind(ev=x)
        x=copy.deepcopy(self.ev); x["originalAfter"]["domainSha256"]="b"*64
        with self.assertRaises(ValueError): self.bind(ev=x)
    def test_timestamp_and_postflight_binding(self):
        for key,value in (("backupStartedUtc","2026-11-26T00:00:01Z"),
                          ("restoreCompletedUtc","2026-11-27T00:00:07Z"),
                          ("finalPostflightReceiptSha256","f"*64)):
            x=copy.deepcopy(self.ev); x[key]=value
            with self.assertRaises(ValueError): self.bind(ev=x)
    def test_exact_canonical_and_duplicate(self):
        raw=r.canonical(self.ev)
        for bad in (b" "+raw,b'{"scope":"FICTIONAL_TEST",'+raw[1:],
                    b'{"Scope":"FICTIONAL_TEST",'+raw[1:],b"x"*32769):
            with self.assertRaises(ValueError): e.bind(self.raw,bad,self.expected)
    def test_no_approved_scope_substitution(self):
        x=copy.deepcopy(self.expected); x["scope"]="APPROVED_IMPORT"
        with self.assertRaises(ValueError): self.bind(expected=x)
        x=copy.deepcopy(self.ev); x["scope"]="APPROVED_IMPORT"
        with self.assertRaises(ValueError): self.bind(ev=x)
if __name__=="__main__":
    unittest.main()

"""Fictional bytes only. No C5 authentication, native IO or owner attestation."""
import copy
import unittest
import retention_contract as r

class RetentionContract(unittest.TestCase):
    def setUp(self):
        self.archive = r.HEADER + bytes(12 + 16) + b"fictional-not-encrypted"
        self.m = dict(schema="orgresource.c9.retention.v1", scope="FICTIONAL_TEST",
            runId="0123456789ab", sourceSha="1"*40, harnessSha="2"*40,
            jarSha256="3"*64, archiveSha256=r.digest(self.archive),
            archiveBytes=len(self.archive), domainSha256="4"*64, domainTables=16,
            sessionRows=[0,0], snapshotUtc="2026-11-27T00:00:00Z",
            serializer=dict(clientSha256="5"*64, clientVersion="8.0.46",
                serverVersion="8.0.46", platform="Windows", lowerCaseTableNames=1,
                collation="utf8mb4_0900_ai_ci", timeZone="SYSTEM",
                flags=list(r.SERIALIZER), excludedTables=list(r.EXCLUSIONS)),
            vaultReceiptId="vr-"+"6"*32, postflightReceiptSha256="7"*64)
    def read(self, m=None, scope="FICTIONAL_TEST"):
        raw = r.canonical(self.m if m is None else m)
        return r.read_manifest(raw, scope, r.digest(raw))
    def test_roundtrip(self):
        self.assertEqual(self.read(), self.m)
        r.check_archive_bytes(self.archive, self.m, "FICTIONAL_TEST")
    def test_scope_and_external_digest(self):
        with self.assertRaises(ValueError): self.read(scope="APPROVED_IMPORT")
        with self.assertRaises(ValueError):
            r.read_manifest(r.canonical(self.m), "FICTIONAL_TEST", "0"*64)
    def test_duplicate_and_noncanonical(self):
        raw = r.canonical(self.m)
        for prefix in (b'{"scope":"FICTIONAL_TEST",', b'{"Scope":"FICTIONAL_TEST",'):
            bad = prefix + raw[1:]
            with self.assertRaises(ValueError):
                r.read_manifest(bad, "FICTIONAL_TEST", r.digest(bad))
        for bad in (b" "+raw, raw+b"{}", b"x"*8193):
            with self.assertRaises(ValueError):
                r.read_manifest(bad, "FICTIONAL_TEST", r.digest(bad))
    def test_nested_duplicate(self):
        raw=r.canonical(self.m).replace(b'"serializer":{', b'"serializer":{"clientVersion":"8.0.46",')
        with self.assertRaises(ValueError):
            r.read_manifest(raw, "FICTIONAL_TEST", r.digest(raw))
    def test_secret_fields_and_wrong_types(self):
        for key,value in (("key","fictional"), ("archiveBytes",True),
            ("domainTables",15), ("sessionRows",[False,0]), ("snapshotUtc","2026-02-30T00:00:00Z"),
            ("vaultReceiptId","C:/fictional"), ("archiveBytes",r.MAX_ARCHIVE+1),
            ("archiveBytes",r.OVERHEAD)):
            m=copy.deepcopy(self.m); m[key]=value
            with self.assertRaises(ValueError): self.read(m)
    def test_serializer(self):
        for key,value in (("flags",[]), ("excludedTables",["SPRING_SESSION"]),
                          ("serverVersion","8.0.45"), ("platform","Linux"),
                          ("lowerCaseTableNames",True), ("clientSha256","x"*64)):
            m=copy.deepcopy(self.m); m["serializer"][key]=value
            with self.assertRaises(ValueError): self.read(m)
    def test_archive_change(self):
        for data in (self.archive[:-1], self.archive[:-1]+b"x", b"X"+self.archive[1:]):
            with self.assertRaises(ValueError):
                r.check_archive_bytes(data,self.m,"FICTIONAL_TEST")
        bad=b"X"+self.archive[1:]; m=copy.deepcopy(self.m); m["archiveSha256"]=r.digest(bad)
        with self.assertRaises(ValueError): r.check_archive_bytes(bad,m,"FICTIONAL_TEST")
    def test_commit_candidate_bindings(self):
        raw=r.canonical(self.m); sha=r.digest(raw)
        record=r.json.loads(r.commit_record(raw,"FICTIONAL_TEST",sha,"8"*64,"9"*64))
        self.assertEqual(record["manifestSha256"],sha)
        self.assertEqual(record["schema"],"orgresource.c9.retention.commit-candidate.v1")
        for custody,cleanup in ((None,"9"*64),("8"*64",None),("8"*64,"8"*64),(sha,"9"*64)):
            with self.assertRaises(ValueError):
                r.commit_record(raw,"FICTIONAL_TEST",sha,custody,cleanup)
if __name__ == "__main__":
    unittest.main()

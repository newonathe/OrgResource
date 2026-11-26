"""H01-A pre-staging evidence binding, with no filesystem, database or vault effects.
Only native independently collected observations may be inputs to operational use.
V1 manifest is preserved as data; the binding is a separate required candidate record.
No automatic upgrade of a previously written manifest or acceptance marker.
"""
import datetime
import hmac
import json
import retention_contract as r

TABLES = ("account_recovery_code", "allocation", "audit_event", "department",
          "flyway_schema_history", "idempotency_key", "invitation", "local_credential",
          "membership", "organization", "ownership_transfer", "project", "resource",
          "resource_requirement", "spring_session", "spring_session_attributes",
          "stock_movement", "user_account")
ROLES = ("source", "restore", "mixed")
COUNT_KEYS = ("organizations", "taggedExact", "taggedDistinctReferences",
              "originalExact", "ownerActiveMemberships", "allocations",
              "taggedAuditEvents", "sessions", "sessionAttributes")
COUNTS = {
    "APPROVED_IMPORT": (98, 96, 96, 2, 97, 21060, 67370, 0, 0),
    "FICTIONAL_TEST": (2, 0, 0, 2, 1, 0, 0, 0, 0),
}
PHASES = ("backupSource", "restored", "backupSourceFinal", "restoredFinal")
IDENTITY_FIELDS = {"serverUuid", "schema", "port"}
OBS_FIELDS = {"identity", "tables", "counts", "domainSha256", "archiveSha256",
              "archiveBytes", "serializer", "sessionTimeZone", "snapshotUtc"}
EVIDENCE_FIELDS = {"schema", "scope", "runId", "manifestSha256", "originalBefore",
                   "originalAfter", "phases", "backupStartedUtc", "backupCompletedUtc",
                   "restoreCompletedUtc", "finalPostflightReceiptSha256"}
ORIGINAL_FIELDS = {"identity", "tables", "domainSha256", "sessionRows"}
EXPECTED_FIELDS = {"scope", "runId", "sourceSha", "harnessSha", "jarSha256",
                   "identities", "serializer", "archiveSha256", "archiveBytes",
                   "manifestSha256", "postflightReceiptSha256"}
MAX_EVIDENCE = 32768

def timestamp(value):
    r.identifier(value, "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z")
    try:
        return datetime.datetime.strptime(value, "%Y-%m-%dT%H:%M:%SZ")
    except ValueError:
        raise ValueError("H01_RETENTION_CONTRACT_REFUSED") from None

def identity(value, role, run_id):
    r.exact(value, IDENTITY_FIELDS)
    r.identifier(value["serverUuid"],
                 "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    r.require(value["serverUuid"] != "00000000-0000-0000-0000-000000000000")
    r.require(value["schema"] == "orgresource_c9_local_" + run_id + "_" + role)
    r.require(type(value["port"]) is int and 1 <= value["port"] <= 65535)

def inventory(tables):
    # Input comes from ordered LOWER(table_name) query; exact order also catches duplicates.
    r.require(type(tables) is list and tables == list(TABLES))

def original(value, expected):
    r.exact(value, ORIGINAL_FIELDS)
    r.require(r.canonical(value["identity"]) == r.canonical(expected))
    inventory(value["tables"])
    r.hex_value(value["domainSha256"], 64)
    r.require(type(value["sessionRows"]) is list and len(value["sessionRows"]) == 2
              and all(type(x) is int and x == 0 for x in value["sessionRows"]))

def phase(value, expected, scope, manifest):
    r.exact(value, OBS_FIELDS)
    r.require(r.canonical(value["identity"]) == r.canonical(expected))
    inventory(value["tables"])
    counts = value["counts"]
    r.exact(counts, set(COUNT_KEYS))
    r.require(all(type(counts[k]) is int and counts[k] == v
                  for k,v in zip(COUNT_KEYS, COUNTS[scope])))
    r.hex_value(value["domainSha256"], 64)
    r.require(value["domainSha256"] == manifest["domainSha256"])
    # The phase envelope binds later archive observations to the row snapshot;
    # backupSource row capture precedes the archive creation, not the archive hash.
    r.require(value["archiveSha256"] == manifest["archiveSha256"])
    r.require(type(value["archiveBytes"]) is int and value["archiveBytes"] == manifest["archiveBytes"])
    r.require(r.canonical(value["serializer"]) == r.canonical(manifest["serializer"]))
    # The owned mysqld is configured --default-time-zone=+00:00.
    # Session observation alone does not prove the dump process used the same timezone.
    r.require(value["sessionTimeZone"] == "+00:00")
    return timestamp(value["snapshotUtc"])

def bind(manifest_bytes, evidence_bytes, expected):
    """Validate independently supplied expected identities and canonical observations.
    Output binds bytes, not the truth of any claimed native observation.
    """
    r.exact(expected, EXPECTED_FIELDS)
    scope = expected["scope"]
    r.require(type(scope) is str and scope in r.SCOPES)
    m = r.read_manifest(manifest_bytes, scope, expected["manifestSha256"])
    for k in ("runId", "sourceSha", "harnessSha", "jarSha256", "serializer",
              "archiveSha256", "archiveBytes", "postflightReceiptSha256"):
        r.require(r.canonical(m[k]) == r.canonical(expected[k]))
    r.require(m["serializer"]["clientVersion"] == "8.0.46")
    r.require(m["serializer"]["timeZone"] == "+00:00")
    ids = expected["identities"]
    r.exact(ids, set(ROLES))
    for role in ROLES:
        identity(ids[role], role, m["runId"])
    for k in ("serverUuid", "schema", "port"):
        r.require(len({ids[role][k] for role in ROLES}) == len(ROLES))
    r.require(type(evidence_bytes) is bytes and 0 < len(evidence_bytes) <= MAX_EVIDENCE)
    try:
        e = json.loads(evidence_bytes.decode("ascii"), object_pairs_hook=r.pairs,
                       parse_constant=lambda _: r.require(False))
        r.exact(e, EVIDENCE_FIELDS)
        r.require(r.canonical(e) == evidence_bytes)
        r.require(e["schema"] == "orgresource.c9.retention.evidence.v1"
                  and e["scope"] == scope and e["runId"] == m["runId"])
        r.require(hmac.compare_digest(e["manifestSha256"], expected["manifestSha256"])
                  and e["finalPostflightReceiptSha256"] == m["postflightReceiptSha256"])
        original(e["originalBefore"], ids["source"])
        original(e["originalAfter"], ids["source"])
        r.require(r.canonical(e["originalBefore"]) == r.canonical(e["originalAfter"]))
        phases = e["phases"]
        r.exact(phases, set(PHASES))
        stamps = [phase(phases[name], ids["restore" if name.startswith("backup") else "mixed"],
                        scope, m) for name in PHASES]
        started, backed, restored = (timestamp(e[k]) for k in
            ("backupStartedUtc", "backupCompletedUtc", "restoreCompletedUtc"))
        # Manifest timestamp denotes cutoff of quiescent source observation, not RPO proof.
        r.require(stamps[0] == timestamp(m["snapshotUtc"]) <= started <= backed <= restored
                  <= stamps[1] <= stamps[2] and stamps[1] <= stamps[3])
        return r.canonical({"schema": "orgresource.c9.retention.bound-candidate.v1",
                            "scope": scope, "runId": m["runId"],
                            "manifestSha256": expected["manifestSha256"],
                            "evidenceSha256": r.digest(evidence_bytes)})
    except (UnicodeError, ValueError, TypeError, KeyError, RecursionError, OverflowError):
        raise ValueError("H01_RETENTION_CONTRACT_REFUSED") from None

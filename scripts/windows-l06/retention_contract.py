"""H01-A candidate: data-only retention contract, not custody or execution authority.
No paths, filesystem operations, vault, encryption/decryption, or OS cleanup.
All evidence inputs MUST come from independently bound native parent observations.
A valid record or marker is NOT evidence that any observation actually occurred.
"""
import datetime
import hashlib
import hmac
import json
import re

HEADER = b"ORGRESOURCE-C5-AES256GCM-1"
OVERHEAD = len(HEADER) + 12 + 16
MAX_ARCHIVE = 64 * 1024 * 1024 + OVERHEAD
MAX_RECORD = 8192
SERIALIZER = [
    "--no-defaults", "--protocol=tcp", "--host=127.0.0.1", "--user=root",
    "--skip-comments", "--skip-dump-date", "--hex-blob",
    "--skip-lock-tables", "--skip-add-drop-table", "--no-tablespaces",
    "--set-gtid-purged=OFF", "--skip-column-statistics", "--single-transaction",
    "--quick", "--no-create-info", "--order-by-primary", "--skip-add-locks",
    "--skip-disable-keys",
]
# Port and schema are run-specific; the caller must bind them to the owned server.
EXCLUSIONS = ["SPRING_SESSION", "SPRING_SESSION_ATTRIBUTES",
              "spring_session", "spring_session_attributes"]
SCOPES = ("FICTIONAL_TEST", "APPROVED_IMPORT")
FIELDS = {"schema", "scope", "runId", "sourceSha", "harnessSha", "jarSha256",
          "archiveSha256", "archiveBytes", "domainSha256", "domainTables",
          "sessionRows", "snapshotUtc", "serializer", "vaultReceiptId",
          "postflightReceiptSha256"}
SER_FIELDS = {"clientSha256", "clientVersion", "serverVersion", "platform",
              "lowerCaseTableNames", "collation", "timeZone", "flags", "excludedTables"}

def require(condition):
    if not condition:
        raise ValueError("H01_RETENTION_CONTRACT_REFUSED")

def exact(obj, fields):
    require(type(obj) is dict and set(obj) == fields)

def hex_value(value, n):
    require(type(value) is str and re.fullmatch("[0-9a-f]{"+str(n)+"}", value) is not None)

def identifier(value, pattern):
    require(type(value) is str and re.fullmatch(pattern, value) is not None)

def pairs(values):
    result = {}
    seen = set()
    for key, value in values:
        require(key.casefold() not in seen)
        seen.add(key.casefold())
        result[key] = value
    return result

def canonical(obj):
    return json.dumps(obj, sort_keys=True, separators=(",", ":"), ensure_ascii=True,
                      allow_nan=False).encode("ascii")

def digest(data):
    return hashlib.sha256(data).hexdigest()

def validate_manifest(m, expected_scope):
    require(expected_scope in SCOPES)
    exact(m, FIELDS)
    require(m["schema"] == "orgresource.c9.retention.v1" and m["scope"] == expected_scope)
    identifier(m["runId"], "[0-9a-f]{12}")
    for key in ("sourceSha", "harnessSha"):
        hex_value(m[key], 40)
    for key in ("jarSha256", "archiveSha256", "domainSha256", "postflightReceiptSha256"):
        hex_value(m[key], 64)
    n = m["archiveBytes"]
    require(type(n) is int and OVERHEAD < n <= MAX_ARCHIVE)
    require(type(m["domainTables"]) is int and m["domainTables"] == 16)
    require(type(m["sessionRows"]) is list and len(m["sessionRows"]) == 2
            and all(type(x) is int and x == 0 for x in m["sessionRows"]))
    identifier(m["snapshotUtc"], "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z")
    datetime.datetime.strptime(m["snapshotUtc"], "%Y-%m-%dT%H:%M:%SZ")
    # Opaque random identifier, never a vault path, key, password or recovery code.
    identifier(m["vaultReceiptId"], "vr-[0-9a-f]{32}")
    s = m["serializer"]
    exact(s, SER_FIELDS)
    hex_value(s["clientSha256"], 64)
    identifier(s["clientVersion"], "[0-9]+[.][0-9]+[.][0-9]+")
    require(s["serverVersion"] == "8.0.46" and s["platform"] == "Windows")
    require(type(s["lowerCaseTableNames"]) is int and s["lowerCaseTableNames"] in (0, 1, 2))
    identifier(s["collation"], "[a-z0-9_]{1,64}")
    # Native adapter must independently observe the actual session timezone.
    identifier(s["timeZone"], r"(?:SYSTEM|UTC|[+-][0-9]{2}:[0-9]{2})")
    require(s["flags"] == SERIALIZER and s["excludedTables"] == EXCLUSIONS)
    return m

def read_manifest(data, expected_scope, independently_expected_sha):
    hex_value(independently_expected_sha, 64)
    require(type(data) is bytes and 0 < len(data) <= MAX_RECORD)
    require(hmac.compare_digest(digest(data), independently_expected_sha))
    try:
        obj = json.loads(data.decode("ascii"), object_pairs_hook=pairs,
                         parse_constant=lambda _: require(False))
        validate_manifest(obj, expected_scope)
        require(canonical(obj) == data)
        return obj
    except (UnicodeError, ValueError, TypeError, RecursionError, OverflowError):
        raise ValueError("H01_RETENTION_CONTRACT_REFUSED") from None

def check_archive_bytes(data, manifest, expected_scope):
    """Bounded fictional/in-memory oracle. Header/hash do not authenticate GCM."""
    validate_manifest(manifest, expected_scope)
    require(type(data) is bytes and len(data) == manifest["archiveBytes"])
    require(data.startswith(HEADER))
    require(hmac.compare_digest(digest(data), manifest["archiveSha256"]))

def commit_record(manifest_data, expected_scope, sealed_manifest_sha,
                  custody_receipt_sha, cleanup_receipt_sha):
    """Construct candidate bytes only AFTER caller proves custody and cleanup.
    No signature, receipt generation, write, success marker, or acceptance here.
    Trusted parent must check actual receipt semantics and bind all run identities.
    """
    m = read_manifest(manifest_data, expected_scope, sealed_manifest_sha)
    hex_value(custody_receipt_sha, 64)
    hex_value(cleanup_receipt_sha, 64)
    require(len({sealed_manifest_sha, custody_receipt_sha, cleanup_receipt_sha,
                 m["postflightReceiptSha256"]}) == 4)
    return canonical({"schema": "orgresource.c9.retention.commit-candidate.v1",
                      "scope": expected_scope, "runId": m["runId"],
                      "manifestSha256": sealed_manifest_sha,
                      "custodyReceiptSha256": custody_receipt_sha,
                      "cleanupReceiptSha256": cleanup_receipt_sha})

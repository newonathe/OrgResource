# Ateneo demo seeding — deterministic algorithm, contracts, mathematical validation and acceptance tests

**Edition:** 2026-11-19. **Status:** DESIGN RECOMMENDATION; no generated history, current importer, hosted service, tests or adoption measurements exist from this document. Obey full AGENTS.md and QUALITY_EVIDENCE_CONTINUOUS_GATE.md, plus 08_ATENEO_HOSTED_DEMO_REQUIREMENTS.md and 10_CODEX_DEMO_DATA_IMPLEMENTATION_PROMPT.md. Do not implement without explicit C8-D approval.


> **SUPERSEDING 2026-11-19 high-volume cohort policy:** This algorithm now applies to `data/reference/ateneo-recweek-2026-expanded-directory.json` and `data/demo/ateneo-volume-profiles-2026.json`: **96 default source-backed directory references** (72 mixed COA-M page entries + 24 verified 2026 LIONS), eight historic-only names excluded. MISA and Sanggunian are highest-volume flagships; 14 featured entities receive broad synthetic scenarios based on demo design, never measured membership size. Read `11_EXPANDED_CAMPUS_ROSTER_AND_HIGH_VOLUME_PLAN.md` in full. Earlier 12-org/6+6 numbers are historical **v1 pilot-profile illustrations**, not full hosted acceptance criteria. Every existing source privacy, ledger, temporal, no-live-SMS, independent QA and academic immutability condition still applies.

## 1. Grounding: C5 implementation inspected

At C5 implementation reference 9755ef63bd4c02a7042cb48823bf759e609357cd, inspected source includes:
- extended/backend/src/main/java/edu/orgresource/extended/identity/C1BootstrapService.java: bootstrap(String slug, String organizationName, String username, String displayName, String password) requires **both organization and account tables empty**, creates one organization, one owner and credentials/recovery, serializes with MySQL GET_LOCK. It must not be called repeatedly to add 96 organizations.
- extended/backend/src/main/java/edu/orgresource/extended/identity/Organization.java: organization fields id, publicId, slug, displayName, status, version, createdAt, updatedAt; **no COA/LIONS classification column currently exists**.
- extended/backend/src/main/java/edu/orgresource/extended/identity/UserAccount.java and Membership.java: fictional user identity, distinct membership roles OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER.
- extended/backend/src/main/java/edu/orgresource/extended/domain/Department.java: organizationId, name, nameKey, version; uniqueness per organization/name_key.
- Project.java: organizationId, departmentId, name, startAt, endAt, version and composite org relationship.
- Resource.java: organizationId, name, nameKey, REUSABLE/CONSUMABLE type, integer totalQuantity, version, optional bounded image blob.
- ResourceRequirement.java: organizationId, projectId, resourceId, positive quantity, version, composite per-org FK, unique per project/resource.
- Allocation.java: org/requirement/resource IDs, quantity, borrowerName, encrypted phone fields, smsConsent, status, timestamps for events, reminder state, version; status enum PENDING/APPROVED/ISSUED/RETURNED/CONSUMED/CANCELLED.
- extended/backend/src/main/java/edu/orgresource/extended/service/InventoryService.java: maintains real transactional stock movements and idempotency; includes real initial movement on resource create. **C8-D MUST review the FULL relevant service methods, SQL and tests** before specifying exact transition and locking details; the above source summary is not whole-file audit.
- Existing Flyway V1–V5 may not be modified. Any extra versioned demo metadata persistence needs a new **Extended-only** migration with a dedicated migration writer; read old migrations and verify checksums first.

The current source does **not** establish that a safe multi-org historical seed importer already exists; build it only after phase authorization. API/DTO naming, exact movement kinds, return/consume transitions, local datetime details and backdating mechanism must be verified against active source before coding.

## 2. Schema and provenance contract (proposed)

**Avoid source course changes:** Frozen Course A remains byte-identical, with its exact five domain JPA entity types. Demo metadata belongs only in Extended C (new support tables/config, not course academic entity count).

Suggested minimal Extended supporting metadata for audited new Flyway Vn, if accepted by DBA/architect:
- demo_dataset: dataset_id VARCHAR(100) PK; generator_version VARCHAR(32); reference_sha256 CHAR(64); seed_manifest_sha256 CHAR(64); seed_fixed_as_of_date DATE; simulation_start DATE; simulation_end DATE; mode ENUM-like constrained VARCHAR(16) values DEMO; imported_at_utc DATETIME(6); imported_by_actor_ref VARCHAR(100) sanitized; status VARCHAR(20) PREPARING/READY/FAILED; row counts/hash JSON safe bounded. This table is *only an example contract*, finalize schema with verified MySQL types/constraints and no old migration edits.
- demo_organization_reference: dataset_id and public_reference_key composite unique; organization_id org FK; listing_year SMALLINT; listing_sector VARCHAR(20) COA_M/LIONS/OTHER_VERIFIED_INDEPENDENT; source_url VARCHAR(1000); source_observed_date DATE; fact_truth_level PUBLIC_ROSTER_ONLY; synthetic_activity BOOLEAN forced TRUE; original source SHA. Do not store copied source page bodies.
- demo_seed_operation (optional, if required for retry): dataset_id + deterministic operation_key unique; domain object type/local key; logical event sequence; event_hash; committed_at. **Do not** duplicate or substitute for the existing stock movement/durable receipt audit mechanism; prefer filesystem + existing transaction idempotency if sufficient.
- No mandatory column added to every business table solely for provenance if demo schema/database is independent, but API/UI must derive dataset-level demo labeling from secure server configuration and verified dataset metadata. Do not accept client-provided isDemo to bypass auth.
- Status DEMO/PUBLIC_ROSTER is not a verified user-role or personal identity class; avoid conflating with organizations' own status ACTIVE/FROZEN.

Fields should be implemented only if justified by actual audited schema and interface closure; lower-cost alternatives are an external signed manifest + database allowlist, provided they meet tenant mapping and cross-run persistence acceptance.

## 3. Input manifests

Input A: versioned 2026 **fact reference** file data/reference/ateneo-recweek-2026-expanded-directory.json with 72 mixed-category COA-M RecWeek directory entries and 24 2026 LIONS org references, exact source URLs/observed date/listing evidence, eight older-only candidates excluded, zero real inventories or people. Older ateneo-public-organizations-2026.json is v1 sample and must not limit the full release. Refresh citations on implementation date; source conflicts recorded.
Input A2: data/reference/ateneo-public-programs-2026.json is a curated 2026 **public program-title context** manifest: 12 public program titles across 6 organizations, each linked to its own organization profile. No occurrence dates/stock/usage are verified; show them only in a factual profile panel, NEVER attach simulated stock movements to the actual program names as if they occurred.
Input B: approved **synthetic scenario catalog**, preferably small versioned YAML or JSON with original fictional department/resource archetypes grouped by theme (performing arts, publishing, technical events, outreach, etc.). These are *scenario hypotheses*, never assertions about any named organization's actual gear or usage.
Input C: deterministic generator parameter object:
```json
{
  "dataset_id": "orgresource-ateneo-demo-2026-v2-full96",
  "generator_version": "1.0.0",
  "reference_snapshot": "data/reference/ateneo-recweek-2026-expanded-directory.json",
  "fixed_seed": 20261119,
  "zone": "Asia/Manila",
  "window_start": "2025-12-01",
  "window_end": "2026-11-18",
  "as_of_date": "2026-11-19",
  "profile": "FULL_96_ORG_SHOWCASE",
  "simulation_only": true,
  "external_messages": "DISABLED"
}
```
Do not treat this sample schema as code-ready until names/types/paths are confirmed by the C8-D owner. Never dynamically use current wall-clock without changing dataset version and source provenance.

## 4. Deterministic event generation algorithm

### Stage G0 — Validate before any DB connection

1. Read and canonicalize both manifests, check strict schema and enum, source URLs https, public roster names unique, categories year-scoped and no source age assumption; reject unknown fields, malformed/duplicate keys and unexpected personal info.
2. Validate date window, Manila calendar and offsets, no simulated event in future relative to fixed as_of, no real phone/email, no stolen images/brand assets, no non-demo target. Generate a canonical SHA-256 of sorted normalized JSON and generator identifier. Record source hash and targets.
3. **Dry run** produces side-effect-free manifest of 96 current-source reference entries, properly typed by institution_kind, predicted tenant/workflow counts and event graph; warnings for every missing source/rights/decision.

### Stage G1 — Construct a referentially valid graph in memory

For each deterministic reference_key sorted lexicographically:
- Create organization display name based on public fact; demo-only presentation suffix/labeled metadata, not a fake "officially onboarded" identity.
- Assign unique demo slug `demo-<reference_key>` normalized for length and C1Bootstrap SLUG regex; if incompatible, fail input validation. Unique synthetic person IDs/usernames cannot overlap real account usernames; e.g. `demo<orgIndex>-owner`, `demo<orgIndex>-cust`.
- Generate the department count from versioned tier policy (Sanggunian 10–18, MISA 8–15, featured 5–10, regular COA 2–6, verified LIONS 2–4); create that many fictional Department names (e.g. "Demo Events Team"), not actual org officer hierarchy.
- Generate the versioned tier's project count (Sanggunian 45–90, MISA 40–75, featured 18–40, regular COA 5–15, LIONS 4–12), as original fictional Project names with realistic but *invented* names/dates. Include active and completed project timelines, at least one overdue/ended and one current future-ending project for as-of-date dynamics.
- Generate the tier's resource count (Sanggunian 75–150, MISA 65–130, featured 30–65, regular COA 10–30, LIONS 8–24) of generic Resources per organization; choose REUSABLE/CONSUMABLE, integer initial counts within real service limits, no zero/negative mismatch, unique case-folded name_key per org. Generate 2–5 Requirements per project with unique project+resource and quantity >=1.
- Generate fictional personas and role memberships. Synthetic names only; no real student ID, @ateneo.edu email, phone, password embedded in repo or project data. Credential provisioning is via protected operator input/ephemeral delegated access rather than public default passwords.

### Stage G2 — Schedule 11½ months without fabricated real events

Use deterministic PRNG with independently derived sub-seeds:
`orgSeed = SHA256(seed || reference_key || generator_version)`,
`sliceSeed = SHA256(orgSeed || project_local_key || month_number)`.
Derive stable UUIDv5-style or SHA-derived IDs with cryptographic collision checks and reproducible operation keys. These IDs are for DEMO only, not account secrets; do not reuse seeds as passwords or encryption keys.

Schedule example generated event **types**, subject to actual InventoryService permitted transitions:
- Resource initial movement at create and bounded replenishment.
- Allocation requested PENDING; some transition to APPROVED, some to CANCELLED.
- Approved reusable custody may transition ISSUED then RETURNED; approved consumable requests may transition as current service permits to CONSUMED; some remain ISSUED at as_of where project end rules allow.
- Include stale version attempted **negative test fixtures only**, not permanent corrupt history.
- Project and resource constraints: event at or within permitted project date/time; policy for approved holds at project end is applied by actual service; do not hardcode incorrect due windows.
- No actual provider send; reminder statuses simulated only if test-mode real service supports it, else simply show no reminders and label simulation.

Timeline distribution: across all twelve calendar months represented by Dec 2025–Nov 2026; vary by org and project using a documented deterministic stochastic schedule. An increase around generic student recruitment/academic activity is only an ASSUMPTION and never a claim of measured campus demand. Fix at least one quiet period and one active period to make charts useful, but do not create fake production metrics.

### Stage G3 — Simulate against actual business invariants before writing

Take an in-memory forecast of physical total, holds and issued custody as an early refusal check; **database service remains the only authority**. At each candidate event in chronological order, verify desired preconditions and allowed transitions. If candidate invalid, deterministically generate another valid action or a clearly intentional refusal test; never force a row to illegal state or clamp availability.
For each resource at every event:
- physical quantity total >=0;
- approved eligible holds + issued reusable quantity <= current physical total, according to actual code's derived balance method;
- stock movement deltas exactly reconcile with saved physical total;
- never double issue, return or consume;
- distinct replay idempotency key per logical mutation and reused identical key on retries;
- resource and requirement always share organization_id and valid foreign keys.
- project end, expiry and clock semantics match code; no future events beyond fixed as_of.
Freeze the ordered planned event sequence and canonical hash before import. The event graph must include state transitions, not merely final-state snapshots; rebuild must not skip journaling.

### Stage G4 — One-time import into a disposable demo-only environment

A separate guarded operator CLI runs once against a **new empty** demo schema. Two explicit checks: connection URL/db name is prefixed/allowlisted for demo and persistent demo environment marker is present, plus operator confirmation (script noninteractive mode requires separately approved deployment pipeline and immutable target ID). Verify Flyway version, empty imported-data state, secrets and runtime schema grants.

Use audited Extended application/service interfaces with a controlled injectable **business-event Clock**; keep identity session/token/crypto actual time independent and secure. If business clock is global, do not interleave imports with user traffic: execute importer as separate isolated bootstrap process and stop it before normal Java server starts. Never rewrite historical timestamp columns or existing append-only movement/audit rows after commit to counterfeit usage. A privileged fixture-only importer is permitted only on fresh isolated demo DB and must still use the real invariant checks; all changes transactional and reconciled. No direct arbitrary UPDATE/DELETE of stock_movement/audit_event. When normal service and import clock cannot safely produce past events, mark design BLOCKED and implement a separately reviewed import architecture rather than bypassing business rules.

On exception: commit per safe transaction group or roll back all (based on audited MySQL DDL/tx boundaries), mark status FAILED and refuse public serving; do not leave READY until complete. Prefer write into a completely new isolated target and atomically switch deployment pointer after verification, instead of deleting or mutating existing demo dataset.

### Stage G5 — Reconciliation and published metadata

After import, independently query actual MySQL tables/tenant rows and verify expected counts, graph uniqueness, correct role memberships, every org resource stock ledger, holds/custody, status histories, authorizations and event timestamps. Compute a canonical **logical checksum**, excluding auto-increment DB IDs, runtime bcrypt salts, import wall timestamps and randomized crypto nonces. The same seed should give identical logical event graph/checksum; storage ciphertext need not be byte-identical. Require zero real contact/phone records, zero outbound provider attempts and zero unauthorized access. Mark READY only when independent reviewer corroborates.

Expose a sanitized `GET /demo/metadata` or other agreed read endpoint (exact path and auth policy must be chosen in C8-D) with dataset_id, generated_as_of, public reference source date, synthetic=true, date window, version, checked metrics, and no internal DB IDs/secrets. All application screens read this metadata from server; do not merely hardcode DEMO banner in React.

## 5. Computational efficiency and correctness

Let O=org count, P=projects, R=resource types, A=allocation state transitions, M=stock ledger movements, K=page size. Plan graph assembly O(O+P+R+A), plus O(A log A) event ordering when events are not generated in sorted order. Hash lookup per local key is expected O(1); actual uniqueness is enforced by database constraints. Avoid O(A×R) loops or per-transaction full-ledger rescans; maintain provisional balances during generation, then perform independent periodic full database reconciliation. Add indexes only after EXPLAIN on real fixture.

Import can batch independent reads and create rows per valid dependency group, but serialize transactions that mutate a common resource; lock order sorted by stable organization/resource ID as actual service requires. No concurrent historical mutation of the same stock resource merely to reduce wall time. Keep sample/full/stress profiles distinct and safe. Record total import wall time, p50/p95 event execution latency, DB/index bytes, source manifest bytes, heap/CPU, cold/warm build impact and compressed/encrypted backup size. No invented absolute optimization.

## 6. Complete acceptance test matrix

Test ID | Expected setup/action | Evidence / mandatory assertion
--- | --- | ---
DEM-SRC-01 | Validate expanded 96-reference roster | All 72 COA-M directory entries individually verified against 2026 source, 24 LIONS corroborated by 2026 point-person list/profiles, 8 historic-only excluded; list 55/53 count contradiction and avoid assigning Sanggunian normal COA accreditation
DEM-SRC-02 | Broken/stale URL, unsupported category, conflicting roster count | Fail/flag provenance, no silent fact fabrication or blanket 55/53 claim
DEM-SRC-03 | Public project/program context | Match 12 source-linked program names to six curated orgs; no unverified event dates, resource ownership, allocations or implied historical use
DEM-PII-01 | Scan input and resulting DB | No real individual names, personal emails, phones, student IDs, images, provider credentials
DEM-PROV-01 | Anonymous/read/export, all React routes | Persistent demo label and machine-readable synthetic provenance; no implied real use/endorsement
DEM-GEN-01 | Generate twice same seed/as_of/version | Exact canonical event-graph hash and org/reference links equal; zero DB side effects from dry run
DEM-GEN-02 | Different seed/version | Different manifest hash; existing data untouched until explicit reviewed replacement
DEM-GEN-03 | Allocation transitions timeline | All state-machine edges valid; no future or out-of-project timestamps; real status names correct
DEM-GEN-04 | Twelve-month month buckets | Dec 2025 through Nov 2026 represented by dated simulated events with truthful label
DEM-DB-01 | Fresh allowlisted schema import | Separate bounded operator process; same 96 source profiles with exact institution type and valid full fixture; status READY only on reconciliation
DEM-DB-02 | Non-demo/shared/Course A schema target | Fail closed before DDL/DML with exact safe refusal and no changed rows
DEM-DB-03 | Existing initialized organization/accounts | No second bootstrap misuse; repeat same dataset no-op; mismatch fail closed
DEM-DB-04 | Fail mid-import, kill process and re-run | Zero unauthorized partial public READY, target isolation and safe reimport/new target
DEM-DB-05 | Per-resource ledger/current balance reconciliation | Physical/holds/issued matches movement rows and service view; no negative availability
DEM-DB-06 | Concurrent same stock, duplicate/replayed requests | One logical mutation; durable stock movement and receipt replay; no double consume
DEM-DB-07 | Every organization role/foreign ID | Org isolation in DB and API; lead/member denied where expected
DEM-AUTH-01 | Public guest access | Sanitized read-only data only; no shared writable seeded tenant/default owner password
DEM-AUTH-02 | Interactive trial, if approved | Isolated per-visitor tenant, bounded TTL cleanup; no read/write spillover
DEM-API-01 | Packaged authenticated Extended REST/Postman | All 28 amended academic operations remain correct plus chosen demo metadata endpoints
DEM-UI-01 | Desktop/tablet/mobile/keyboard/reduced motion | All visible screens truthful, playable approved workflows, correct history and source note
DEM-UI-02 | Empty/error/partial/stale/lost response | Honest states and one idempotency identity; no false chart or duplicate movement
DEM-OPS-01 | Restart/resume after successful import | Dataset stable, no automatic re-seed, no extra messages, permissions intact
DEM-OPS-02 | Backup and independent off-host restore | All 96 current-source demo org references, users/roles, images, timeline, stock ledger, checksums restored; actual off-host if hosted claim
DEM-OPS-03 | Test live provider disabled | No outbound SMS/email request or real address
DEM-PERF-01 | Tiny vs full/benchmark fixture | Record real elapsed time, bytes, p95 list/graphs, MySQL plans; no fictional measurements
DEM-A-01 | Protected frozen Course A and original course source | Exact tree/hash parity and Java 8 regression; no Extended changes to course examples
DEM-SEC-01 | Injection, bogus manifest, excessive fixture size, path traversal | Strict refusal, bounded memory, sanitized logs, no execution/shell interpolation

## 7. Required evidence per approved code pass

Under evidence/C8-DEMO/ produce: SOURCE_ATTRIBUTION_AND_RIGHTS.csv, PROVENANCE_CONTRACT.md, EXACT_FILE_COVERAGE.csv, CLASSIFICATION_CONFLICTS.md, GENERATOR_ALGORITHM_AND_HASHES.md, FULL_MANIFEST_AND_HISTORICAL_GRAPH.json (FICTIONAL ONLY), IMPORTER_PRECONDITIONS_AND_DRY_RUN.md, DOMAIN_TRANSACTION_AND_LEDGER_RECEIPTS.md, TEST_MATRIX.csv, UI_API_PROVENANCE_SHOTS.md (sanitized), SECURITY_PRIVACY_RED_TEAM.md, COLD_WARM_IMPORT_AND_STORAGE.csv, CI_RUNS_AND_SHA.md, INDEPENDENT_CRITIQUE.md and PHASE_REPORT.md. File-by-file records must include SHA, code/line ranges, transitive dependencies, real negative tests, observed outcome, keep/fix/defer and residual gaps. Every stage is blocked by unresolved P0/P1 or unexecuted mandatory acceptance, not concealed with aesthetics.

## 8. Final deployment handoff

C10-D must read generated manifest and exact tested SHA, provision a **separate** approved hosted demo database, import once, verify banner, roles, real browser/HTTPS and login flows, demonstrate backup and off-host restore, and confirm no live provider/PII. C11 must audit deployment's demo data and reject any statement implying a real year of Ateneo OrgResource operation.

**No automatic merge, public deployment, paid service, live SMS, real user info, email to actual organizations, academic submission or Course Final A change.**

## 9. Mandatory full-cohort high-volume additions (not replaced by compact CI samples)

Before C8-D implementation, read `11_EXPANDED_CAMPUS_ROSTER_AND_HIGH_VOLUME_PLAN.md` in full and inspect both expanded JSON reference and tier files. Additional mandatory test IDs:

| Test ID | Expected outcome |
| --- | --- |
| DEM-RSTR-72 | Exactly 72 2026 RecWeek COA-M **directory** names matched individually, correct mixed institutional type and explicit non-equivalence to COA accreditation count |
| DEM-RSTR-24 | At least 24 2026 LIONS point-person/profile corroborated names, eight older-only excluded by default, each cross-source/alias conflict recorded |
| DEM-RSTR-MISA | MISA COA directory record and independent source profile, distinct demo tenant, tier `FLAGSHIP_MISA` |
| DEM-RSTR-SANGGU | Sanggunian correctly STUDENT_GOVERNMENT, no false ordinary COA membership, distinct demo tenant and `FLAGSHIP_SANGGUNIAN` tier |
| DEM-SCALE-PROFILES | All 96 v2 reference keys map exactly once to a supported tier, 14 featured by design purpose not member size, no orphan/unexpected duplicated tenant |
| DEM-SCALE-VOLUME | When C8-D full fixture is actually generated and imported, each tier satisfies approved bounded event/project/resource range or a documented owner-approved revised target; expected total range 21,060–56,260 **fictional** allocation requests only |
| DEM-SCALE-LEDGER | Independent SQL per-resource reconciliation across **every** populated tenant and temporal transition; no double issue/consume/return, role isolation preserved |
| DEM-SCALE-RESTORE | Test clean-source large import, restart, index query plans, p95 history pagination, snapshot size including original 64 MiB bound, and complete **off-host** restore before hosted claim |
| DEM-SCALE-BANNER | An ordinary guest sees simulation notice and source/year boundaries on government, MISA, featured, standard, analytics and export views; no implied actual student records |
| DEM-SCALE-ACADEMIC | Frozen Java 8 course A unchanged, existing 28 amended academic operations still accepted, plus Java 21 real MySQL/HTTP/React/Playwright fixture test receipts |

Do not call the JSON planning files an imported dataset, server-ready seed script or proof of an operational hosted product. These require new authorized implementation and executed acceptance, with the complete independent QA gate and precise stopped/not-run statuses.

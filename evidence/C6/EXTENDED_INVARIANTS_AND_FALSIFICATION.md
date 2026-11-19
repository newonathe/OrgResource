# C6 Extended C5 inventory/security/test risk register — actual code findings

Date 2026-11-19. Tested/package implementation reference 14576b57fff44c6f0112fd37d8c237ca0122efb4, C5 report SHA 9755ef63bd4c02a7042cb48823bf759e609357cd. **SOURCE_ONLY** on code and **REPORTED_ONLY** on previous CI/runtime; no current MySQL/HTTP/Chrome execution.

## Conservation model (verified in selected Java source ranges)

Location: extended/backend/src/main/java/edu/orgresource/extended/service/InventoryService.java Git blob 878fa6bae4ac8ff08f57916b12c77b794bf12003.

- Lines 705–728 `balance()`: one bounded SQL query recomputes `ledgerTotal = SUM(stock_movement.delta_total)`, `ledgerIssued = SUM(delta_issued)`, `holds = SUM(APPROVED allocation quantity with project.end_at > sampledNow)`, `issued = SUM(ISSUED allocation quantity)`.
- Stored `resource.totalQuantity` must equal ledgerTotal and lie 0..1,000,000. For REUSABLE, ledgerIssued must equal issued and total >= holds+issued. For CONSUMABLE, both issued measures must be zero and total >= holds. Any violation throws INVENTORY_INTEGRITY_FAILURE rather than masking negative available.
- Lines 731–736: reusable available = physicalTotal - holds - issued; consumable available = physicalTotal - holds, with range/integrity validation.
- Lines 519–585: APPROVE requires PENDING + project future end + requirements not overcovered + enough stock, ISSUE reusable only from APPROVED and active project, RETURN reusable only from ISSUED and writes a negative delta_issued, CONSUME approved consumable and writes negative delta_total, CANCEL pending/approved only. Transactional row locking and caller role authorizations require full caller closure review.
- Lines 174–203: RECEIVE checks org role, resource row lock, body operation ID/hash, idempotency key, ETag and bounded total before saving resource total and append-only movement. Code path source observed, not SQL transaction rollback proven this turn.
- Lines 616–703: idempotency_key records with 24-hour expiry, operation ID to request fingerprint, completed HTTP response replays and durable movement fallback for purged receipts. **Important design boundary:** fallback returns a MovementReceipt with HTTP 200 even where original RECEIVE may have been HTTP 201; this is a documented/reported contract divergence, not automatically a defect. C6 critic should verify client compatibility, test assertions, authorization, clock/restart and replay semantics for entire lifetime of movement.
- Lines 244–315: modern and legacy transitions share stock logic but differ in operation IDs/version requirements; legacy `UUID.randomUUID()` is per request. This is intentional legacy compatibility requiring precise test of uncertain-response retries and no duplicate use of already-invalid state, especially for consumables.

Location: V1–V5 Flyway source blobs:
- V1 `22b1e0f589ab05d2b0721e5de39bb29b371a2382` defines organization/account/membership/5 domain tables and organization-scoped FKs.
- V2 `4dd537e2bcfb22898fb144860cb5881afe904790` defines Spring Session persistence.
- V3 `37ce7fffc3a2a5aae6556aec22f5c8f7755b363c` creates stock_movement, idempotency receipts and movement delta CHECK/FK constraints.
- V4 `56d09c0c7f46a23d0d29f8fd5eb236548e86f29d` adds encrypted contact envelope/image/reminder fields.
- V5 `ff9f7330de52b7f6aba6f4f21c29382896eeb5a5` refines reminder claim outcome.
These were inspected as DDL ranges only. Do **not** alter old migrations or claim their checksum was runtime verified in this pass.

## C6 adversarial tests and statuses

| Test ID | Failure hypothesis | Exact safe test on approved disposable fresh MySQL 8 | This pass |
| --- | --- | --- | --- |
| C6-STOCK-01 | Concurrent last-unit approval overbooks | Two independently authenticated CUSTODIANs approve separate PENDING requests for last quantity, assert <=1 approved and movement/physical/available reconciled | NOT_RUN |
| C6-STOCK-02 | Lost HTTP response duplicates a receipt | RECEIVE with fixed operationId + idempotency key, drop response after commit, retry same key and body, assert one delta and replay identity/status policy | NOT_RUN |
| C6-STOCK-03 | Changed body under reused receipt commits silently | Same operation/key changed quantity; assert 409 and no stock changes | NOT_RUN |
| C6-STOCK-04 | Receipt pruned after 24h loses permanent replay | Remove expired idempotency record through approved disposable test harness, issue same operation, assert durable movement receipt and no duplicate | NOT_RUN |
| C6-STOCK-05 | Corrupt ledger hides negative available | Inject inconsistent ledger in disposable elevated fixture; read availability returns explicit integrity failure, no clamp | NOT_RUN |
| C6-STOCK-06 | Transaction rolls back partial stock/movement | Inject safe exception between total update and stock_movement insert, assert both roll back and no receipt completed | NOT_RUN |
| C6-STOCK-07 | Wrong-tenant FK bypass | Link resource and requirement/project of distinct organizations, assert DB composite FK and API safe denial | NOT_RUN |
| C6-STOCK-08 | Race: expiry, return and revoke consent | Safe clock at project end, returned issue remains in permanent custody record; no double issue/return/reminder | NOT_RUN |
| C6-AUTH-01 | Server-side Origin spoof through reverse proxy | Actual future HTTPS trusted proxy, crafted Origin/Host/X-Forwarded; CSRF and secure cookie correctly applied | BLOCKED_HOST_NOT_AVAILABLE |
| C6-AUTH-02 | Brute-force attempts reset by restart | Test new JVM login attempts with `C1AttemptThrottle` in-memory; decide if persistent limitation acceptable for approved single-instance scope | NOT_RUN |
| C6-TEST-01 | Test-suite order hides coupling | Run critical `C1IdentityAcceptanceTest` methods standalone and reversed order using independent fresh schemas; note class `@TestInstance(PER_CLASS)` and `@TestMethodOrder` in source lines 87–88 | NOT_RUN |
| C6-UI-01 | 96-org seeded demo means actual adoption | Actual UI/API/export show sourcing for names and SIMULATED for every resource/custody/metric; cannot expose real owner/student data | BLOCKED_FEATURE_UNIMPLEMENTED |
| C6-RECOVERY-01 | Local two MySQL processes mislabeled off-host | Restore real encrypted archive to a separate **host/storage**, confirm RPO/RTO, all stock/role/identity, negative tamper | BLOCKED_HOST_NOT_AVAILABLE |

## Current code and environment risks

1. **P0 for production scope:** Public TLS proxy, host/header/CSRF trust boundary and secure cookies untested. `C1SameOriginFilter.java` Git blob 0048ac6cd808c0da0d4b5a11ecadceb31acdb184, lines 34–48 compare servlet scheme/host/port to Origin. This is safe source context for local loopback; cannot infer behavior behind an arbitrary ingress proxy.
2. **P0 for claims of final production:** C5 report states successful recovery with independent processes/data directories on **same workstation**, not independent off-host disaster recovery. Actual host key custody, backup retention, 64 MiB SQL limit and operator restore need documented acceptance and proof.
3. **P1 security review:** `C1AttemptThrottle.java` Git blob 4837f45f832d3525285f375704019b81894bf7e8 uses a synchronized `LinkedHashMap`. Restart clearing/rate-limit behavior needs explicit threat model. It is not proof an attacker exploited anything.
4. **P1 testing evidence:** `C1IdentityAcceptanceTest.java` Git blob b96bf581428fcaec64b7eb4d707fde3fd037f73f, 2,781 lines, uses ordered/per-class lifecycle. Some tests may deliberately build successive states; isolation must be independently demonstrated for selected critical tests rather than assumed.
5. **P1 feature scope:** current C5 source is *local release* with no actual 96-org historical seed; `C1BootstrapService` blob 8ac57ea2d90fc2bacb57bc0c1f369990e1c70799 permits first-organization bootstrap only; its one-time flow cannot simply be looped 96 times.
6. **P1 Extended product scope:** Borrower linked-account ownership, LEAD project assignment policy, complete v2 CRUD/history cursor UI and public demo guest model remain user decisions / observed partial integration boundaries, not proven complete products.
7. **P1 external dependencies:** Reported development Picomatch advisory and production-only audit from C5 were not independently refreshed against current registry/lockfile in this pass; a current dated advisory review is required before release.

## Independent critique requirements and selection

C6 self-challenge: each positive source statement above has a specific falsification test, owner and status. **This is not an independent second-agent review**. Root should run one bounded independent `org_review` at Sol-medium only when fresh source contract and reproducible evidence support it (actual model availability and token budget verified), otherwise mark review NOT_RUN and keep C6 open. Do not burn many high-reasoning agents on the same 867-line service; inspect exact hard invariants and transitive closure first.

**C6 integrity gate: SOURCE_ONLY; TESTS NOT_RUN; independence check PENDING.** No authorization to start C7 until C6 accepted by owner.

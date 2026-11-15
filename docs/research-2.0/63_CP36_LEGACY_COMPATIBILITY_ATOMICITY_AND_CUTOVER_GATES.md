# CP36 — Original 26-route compatibility adapters, retry safety and migration acceptance

**Dated:** Nov 16, 2026 PHT. **Status:** DESIGN RECOMMENDATION and new independent critique; NOT implemented or tested. Read CP31–35 first. **Source:** actual pinned O14 controllers/DTOs/Components at `2dd336ed39cecd417a8bce3388b303e688e95dee`; original 25-route concise proposal and 3-page assignment. Addresses the central tension in CP34/CP35: original JSON/body cannot gain mandatory modern fields while still being called original.

## 1. Real compatibility claim, no conflation

**COURSE REQUIREMENT:** Old GET/POST, JSON, Spring `@Autowired` delegation, JPA/MySQL, Javax validation, Retrofit, scheduler, Postman and justified five entities must run. **ORIGINAL O14 SOURCE:** base `/api`, `Long` numeric IDs, Philippine wall-time JSON, 26 Jersey handlers including A09, original create/update/transition bodies and unwrapped DTOs. No original password/login, `If-Match`, orgId or `operationId` fields.

**DESIGN RECOMMENDATION:** Protected compatibility routes preserve **original method/path/JSON/response** while surrounding them with genuine secure session, CSRF, explicit activeOrgId and server-side operation adapter. The additional authorization and safe-history guards are disclosed intentional behavior extensions. Do not claim original O14 itself has authenticated sessions or modern ledger. These routes must not bypass the same five domain entities and the same MySQL stock invariants as public v2.

**Non-negotiable boundary:** Do NOT require `If-Match`, `Idempotency-Key`, `operationId`, public UUID or body `orgId` in an **original /api request**. Those would break the original Postman signatures. Require CSRF/session for any POST because public unauthenticated access is unsafe; new authenticated Postman collection must first login/select org. Original Postman files can be upgraded with a pre-request authentication environment while keeping individual original operation bodies/paths intact.

## 2. Server-issued operation identity for old no-body stock transitions

- **A04 issue**, **A05 return**, **A06 consume** have **NO original body**. Under lock, verify allowed current state; issue one immutable `stock_movement` with a **server-issued** stable operation ID scoped to `(org,allocation,transitionKind)` and immutable fingerprint. The old lifecycle permits each transition only once. A repeat POST after success sees no allowed source state and receives the original-style `409 INVALID_ALLOCATION_STATE` rather than a second stock movement. The public v2 receives a caller-supplied `operationId` and can return the original durable receipt on retry. Both share the **same movement once** in the ledger.
- **R01 create Resource** body only `{name,type,totalQuantity}`; create T>0 with a generated INITIAL operation ID in same transaction. Source original normalized resource-name uniqueness makes duplicate same-name POST 409 (it does not make the entire original POST protocol exactly-once for all request patterns). T0 creates no INITIAL. Public v2 takes caller-supplied stable operationId.
- **R03 legacy update Resource** body only `{name,totalQuantity,expectedTotalQuantity}`. Under resource lock compare `expectedTotalQuantity` with current T; if it differs 409 `STALE_RESOURCE_QUANTITY` before mutation. If T changes, compute delta and create **server-issued ADJUST operation ID** inside same transaction (no caller ID, reason code `ACADEMIC_COMPAT_UPDATE`, actor logged). On response loss and immediate identical retry, old expected T no longer matches and 409 prevents duplicate physical change. It is **not** a successful replay receipt like public v2 and must not be advertised as one. If T unchanged, update name only and create no movement. This relies on compare-and-set for quantity and transaction/version ordering; names alone are not a stock movement.
- **A01 create Allocation** original body only `{requirementId,quantity,borrowerName,phoneNumber?,smsConsent?}`. No caller idempotency key; identical ambiguous network retry can create multiple PENDING requests. PENDING does not hold stock; no double stock mutation. Future UI and public v2 MUST use per-domain Idempotency-Key and resolve uncertain status before repeating create. The academic compatibility suite explicitly tests this limitation rather than promising exactly-once all original POSTs.
- **A07 cancel**, **A08 delete**, **A09 revoke SMS** do not physically move stock, so no movement `operationId`. A09 repeated call genuinely succeeds in current O14 source and always leaves consent false/contact null; it never implies an SMS already sent can be undone.

**Consistency criteria:** If old no-body transition request and new v2 request race for same allocation, org/resource/requirement/allocation locking and status checks guarantee exactly one terminal transition and one movement. The loser receives a safe 409 (or v2 same-ID receipt if truly identical completed operation), not a second issue/consume/return. Generate IDs using a stable uniqueness strategy and DB constraint, not an unchecked client string; test collisions and snapshot/restart behavior.

## 3. Intentional incompatibilities — never mark exact parity without evidence

| Dimension | Original O14 | ONE-product secured adapter | Acceptance category |
|---|---|---|---|
| Base API | `/api`, no session | same `/api` routes, requires secure login/activeOrg/CSRF | **Intentional auth extension** |
| IDs | Long global ID | same numeric ID but tenant-scoped lookup/authorization | **Meaning preserved** |
| Dates | `LocalDateTime` Philippine wall clock | input/from UTC with `Asia/Manila` conversion, same JSON output | **Contract preservation** after round-trip test |
| Errors | `{code,message}` with O14 status | preserve observed HTTP/domain mapping plus 401/403/ORG_CONTEXT_REQUIRED | **Extended responses** |
| R01 create | no operationId | internally generate INITIAL for nonzero total | **Meaning preserved** |
| R03 update | total and expectedTotal CAS | preserve body and 409 stale; internal ADJUST on changed T | **Meaning preserved**, not public retry parity |
| R04 delete | zero T and no Requirements; no ledger in source | refuse if ever physically moved (even now T0); archived via separate v2 route | **KNOWN DEVIATION**; written course-equivalence check |
| Q03 update | protect coverage + largest PENDING | same guards, current clock and same-org lock | **Meaning preserved** |
| A04/5/6 | no-body transitions | internally create unique movement per transition; original success/failure body/HTTP | **Meaning preserved** |
| A01 | free-text borrower name; no user account | snapshot name and optional member link; no identity claim | **Meaning preserved** with accountable actor |
| A09 | repeat clears phone/consent, no HTTP provider send | keep exact original idempotent semantics | **Meaning preserved** |
| API surface 26 | 25 original + A09 | all 26 real Jersey in same product, not separate mock route | **Must pass** |
| Course Javax | literal `javax.validation` | must execute actually, no silent Jakarta synonym | **Runtime proof still OPEN** |

**Course R04 significance:** the academic assignment requires web endpoints that really delete eligible rows; the original O14 R04 can delete Resource T0 if no Requirement, regardless of now-absent historical stock movements. New product may preserve **one real physical delete path** (never-used T0), but it still diverges on a formerly moved, adjusted-back-to-zero resource. This is an explicit **course acceptance uncertainty**, not a technical contradiction we can wish away. If instructor insists on identical legacy deletion semantics for such records, investigate an archive-ledger design that retains independently referenced movement snapshots and *genuinely* deletes the resource only after user-approved, integrity-preserving redesign. Do not silently erase ledger rows or claim soft delete is physical delete.

## 4. Legacy Postman bootstrap and exact real-path tests (future)

Separate collection setup `ACADEMIC_AUTH_SETUP`: (1) anonymous GET csrf (2) POST login with fictional first owner and CSRF, stored cookie (3) GET orgs (4) POST active-org with selected orgId/CSRF (5) call 26 original endpoints with **unchanged business request JSON**, including no-body A04/A05/A06. Credential values sourced from local test environment only, not committed. Use a distinct synthetic per-suite organization; tests clean only their own fixtures, no shared database reset.

| Test ID | Race/negative | Required expected |
|---|---|---|
| COMPAT-L01 | Original 26 route inventory diff against real controller annotations at protected SHA | 26 present, no phantom GET-by-ID counted as original |
| COMPAT-L02 | Old R01 T5, create again same name | first 201, exactly one INITIAL +5; second 409, no second ledger |
| COMPAT-L03 | Old A03 two q4 on T5 | exactly one approved, H4,I0,T5,available1 |
| COMPAT-L04 | Old A04 issue bodyless and public v2 issue concurrently | exactly one committed ISSUE(+q I), no two transitions |
| COMPAT-L05 | Old A05 return after end | T unchanged, I drops once, covered remains q |
| COMPAT-L06 | Old A06 consume bodyless and repeated | T decreases exactly q once; repeated 409; one CONSUME |
| COMPAT-L07 | Old R03 change T5→T7 then lost response and identical repeat | first ADJUST+2; repeat 409 stale, T7, one movement; **do not claim successful retry receipt** |
| COMPAT-L08 | Old R03 rename with unchanged T | no ADJUST, name changed, no ghost history |
| COMPAT-L09 | Old A01 network repeat two identical PENDING creates | document that two PENDING rows are possible without a new header; no stock double-hold; UI uses v2 |
| COMPAT-L10 | Old R04 virgin T0 delete; formerly moved then adjusted to T0 delete | virgin physically deleted; formerly moved 409 and ledger survives; flagged parity deviation |
| COMPAT-L11 | Old A09 call twice (before reminder send) | both succeed; contact null; consent false; no further dispatch |
| COMPAT-L12 | Two orgs; call legacy ID from foreign org | 404/no foreign rows; cannot force org through caller-supplied request header |
| COMPAT-L13 | Original old body Philippine naive date to new UTC DB and back | exact semantic wall time preserved for PHT with controlled clock; year 2040 valid |
| COMPAT-L14 | Current O14 error code and JSON source fixtures vs protected endpoint | original errors retained except explicit auth/history changes; no public envelope leaked |
| COMPAT-L15 | No authenticated session or missing activeOrg or bad CSRF for legacy POST | 401/ORG_CONTEXT_REQUIRED/403; no data mutation |
| COMPAT-L16 | New product original 5 JPA entity CRUD + direct `javax.validation` invalid saving + source-based course sample | actual evidence only; NOT RUN in this planning phase |

**Source-level history:** O14 original CI has 55 Java tests and 15 UI tests observed green at pinned O14 SHA, local Newman 96/142 assertions and five Playwright journeys author-reported, **not** evidence that any above unified-product tests passed. The known intermittent availability test expected5 observed8 remains unresolved until independently repeated and diagnosed.

## 5. Schema cutover, deployment safety and release invariants

- Versioned new schema on isolated empty DB with original five **names** and all justified new identity/ledger/history tables; no automatic migration of O14 synthetic test data or original raw phone.
- `ddl-auto=validate` (if selected framework supports expected behavior) rather than `update` publicly; test migrations from empty and successive versions, then clean restore.
- All POST routes hit one authorized transactional domain layer and one authoritative `resource.total_quantity`/allocation state; no duplicated domain implementations in old and new API controllers.
- Do not ship Java8/Boot2 old runtime Internet-facing under a reverse proxy as “modern” by marketing. Supported modern compatibility test and actual vulnerability/dependency scan remain execution gates.
- Project scheduling: the source O14 project `startAt` must be future on creation; APPROVE is allowed before start, ISSUE/CONSUME only during `[startAt,endAt)`; RETURN after end. Expired APPPROVED effective hold removed before cleanup; ISSUED custody is never returned by scheduler.
- For every transaction and audit, record committed version/operation result and no-PII request correlation. Do not print password/SMS/phone; preserve synthetic-only seed.
- No PR merge, release tag, production deploy, real Twilio use, payment, Canvas or student authorship tasks are authorized by research documentation.

## 6. Final review classification

**Paper-level RECONCILED:** legacy bodyless stock mutations vs durable ledger, legacy R03 CAS vs public idempotency, original numeric DTO vs secure public UUID, old dates, old R04 safety deviation, A09 real repeated revoke, auth Postman bootstrap, and one authoritative database. **Still needs real proof:** compatibility compilation/JPA Javax, concurrency, exact protected JSON snapshots, O14 flaky test cause, instructor R04/CLO9/Twilio interpretation, 2026 free-host accounts and student Canvas. Nothing in CP36 is an executed check. If full requirement preservation is not feasible, halt full build and return to owner with exact evidence rather than modifying course assumptions silently.

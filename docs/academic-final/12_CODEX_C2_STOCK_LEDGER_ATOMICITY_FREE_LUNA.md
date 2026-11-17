# OrgResource — C2 Implementation-Only Master: C1 Closure + Durable Stock Ledger / Atomic Inventory

**Issued 2026-11-17 Asia/Manila.** **Execution agent:** free Codex/Luna, highest reasoning level actually available. **CODE ONLY:** audit local/remote source, implement, test, debug, package, commit, push, PR. **NO open-ended vendor/product/security research, faculty inquiry, independent architecture selection, public cloud deployment, SMS send, purchases, Canvas action or C3 work.**

**Repository**: private \`newonathe/OrgResource\`.
**Last audited C1 parent**: \`feat/extended-c-c1-identity-tenancy-2026-11-17\`, HEAD \`7c349a6a818f2aafd21598e76d3eb8b9faa9a8ef\`, draft PR #14 based on C0 PR #13. C1 tested source commit \`61cd551a230420d3c4516d42daa92cae3058170d\`. **C1 Java21/MySQL tests are locally reported 17/17; existing GitHub green CI tests Java8 Course A + frontend, NOT Java21 C1.** Always check fresh ref; numbers are context not permission to assume latest.
**Course A immutable source K**: \`7f8c0746da18fd54b47f430ac1435ac36e605d67\` (draft PR #12). NEVER modify its source or artifact bytes. Original 25 proposal Jersey routes + A09 privacy amendment + 2 image endpoints, real Java8/Jersey2/Javax/MySQL/JPQL/Retrofit/Gson/schedulers and React remain operational on A. C will independently reprove every course function in later C3/C6; C2 is not complete course parity.

**Specification authority**: current exact source; original assignment/syllabus and historical proposal; latest two-edition user decision; approved research \`docs/research-2.0/61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md\` §2–4, \`62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md\` §2–3.3/3.4, \`63_CP36_LEGACY_COMPATIBILITY_ATOMICITY_AND_CUTOVER_GATES.md\` §1–4, \`68_CP41_SOURCE_TO_SQL_INVARIANT_GUARDS_AND_PROOF_CASES.md\` §2/4/5/6, \`32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md\`, and canonical Course A specs \`docs/academic-final/02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md\` / \`07_COURSE_A_EXECUTABLE_SPEC_FREEZE_2026-11-16.md\` on academic docs branch. Read specific source sections only when editing their contracts, not entire 91-file research tree. This is **approved C2 implementation scope**, not permission to adopt unimplemented optional future designs.

## Mission

Produce the first real tenant-authorized, durable, replay-safe inventory core inside the **one maintained** \`extended/backend/\` application. Preserve and reuse C1's identity, session, CSRF, organization, five-domain JPA/MySQL, Retrofit and Javax runtime; add transactionally consistent physical stock history and bounded HTTP receipt handling; prove concurrency, rollback, restart and cross-tenant denial on actual MySQL.

**Dependency-coherent scope:** one C1 hardening preflight + stock schema + stock service/selected HTTP routes + full integration/regression. Do NOT attempt all remaining 28 migrated academic APIs, full v2 CRUD, new UI, image, export, public hosting, or course submission in this cycle. Three internal checkpoints are not three separately required coding sessions.

Optimize for **maximum correct working tested implementation** per free-Luna tokens/time, while treating security, correctness, accuracy, completeness and source preservation as constraints. No fashionable infra. One primary code integrator; at most 2 real scoped helpers if actually available and net worthwhile, never invented: (1) fast read-only route/SQL test gap audit, (2) medium read-only independent race/security review; no same-file concurrent writes. Highest available reasoning for transactions, uniqueness, failure injection and Git.

## 0. Mandatory first actions and live source audit

Before any edits:
\`\`\`bash
git status --short --branch
git remote -v
git fetch origin --prune
git worktree list
git branch -vv
git ls-remote --heads origin
git log -12 --oneline --decorate
\`\`\`
Inspect PR#14 exact head/base, CI job titles/logs, new branch changes, C1 evidence \`evidence/C1/PHASE_REPORT.md\`, and C1 root \`AGENTS.md\`. Pin SHA of C1 parent; branch \`feat/extended-c-c2-ledger-atomicity-2026-11-17\` (or collision-free equivalent) from latest legitimate C1 HEAD; draft PR targeting C1 branch unless merged. No work off old \`main\`, Course A K, docs/research, or C0 alone. Preserve dirty/unrelated files; no force/reset/clean/auto-merge/cherry-pick whole docs branch.

**Current AGENTS conflict:** C1 root \`AGENTS.md\` is still an O14-era file saying O14-only and only \`orgresource_test\`. On C2 NEW branch only, update root \`AGENTS.md\` minimally to the actual protected A and maintained Extended C rules, with safe disposable C2 test schema requirement and role split. Do not rewrite history or change K. Codex handles implementation, not external research.

**Verified audit finding to fix early:** \`extended/backend/src/main/java/edu/orgresource/extended/identity/C1SessionService.java\` \`establish(...chooseOrganization)\` currently selects the first ACTIVE organization at login/new-account creation. The approved CP32/CP35 auth contract explicitly requires **NO implicit default first organization**. Remove implicit selection, require explicit \`POST /api/v2/session/active-org\`; legacy \`/api\` must fail \`ORG_CONTEXT_REQUIRED\` until selected. Add tests with one/two orgs, login/new account, revoked membership, org switch and after logout. Do not broaden into a new login UI in C2. Reconcile C1 no-unneeded-default behavior with existing tests and report changes truthfully.

**CI evidence gap**: remote green \`Verify\` jobs for C1 only exercise academic Java8 and React. Add a *distinct* Java21/real-MySQL C1+C2 CI workflow/job with a narrow test scope and timeout if allowed without payment, preserving academic jobs. Do not assert paid Actions capacity or bypass billing. Avoid unnecessary duplicate npm jobs and duplicate push+PR triggers where possible. A failed/unavailable remote gate must be marked NOT_RUN/BLOCKED, not silently replaced by older green CI.

## 1. C2-A — Physical schema, migration and strict invariants

Create **append-only** \`stock_movement\` plus 24h HTTP \`idempotency_key\` receipt tables in a **new reviewed Flyway V3 (and V4 only if a genuine independent schema change requires it)**. Do not rewrite V1/V2 migration checksums already used by C1. Use same 5 core entity types, MySQL/InnoDB, JPA/Spring Data/Hibernate, physical \`resource.type\` and \`allocation.borrower_name\`. New stock-related columns only as justified; return/consumed timestamps and reasons may need additive migration to C1 \`allocation\`. If adding \`allocation.resource_id\` for a composite FK, derive it from the stored same-org Requirement reference for existing *fictional* rows and validate, never invent physical stock history. Ensure migrations refuse inconsistent existing data instead of deleting it.

Minimum \`stock_movement\` contract:
- \`id BIGINT PK, public_id [UUID binary/CHAR with exact mapping], org_id, resource_id, allocation_id NULL, actor_membership_id [required for authored operations], operation_public_id UUID, request_fingerprint BINARY(32), kind INITIAL/RECEIPT/ADJUST/ISSUE/RETURN/CONSUME, quantity INT, delta_total BIGINT, delta_issued BIGINT, occurred_at DATETIME(6) UTC\`.
- \`UNIQUE(org_id,operation_public_id)\` for permanent replay; **independent \`UNIQUE(org_id,allocation_id,kind)\`** for once-only ISSUE/RETURN/CONSUME. Multiple NULL \`allocation_id\` rows for INITIAL/RECEIPT/ADJUST must be permitted. CHECKs enforce positive quantity, allowed kind and coherent deltas; INIT/RECEIVE/ADJUST require NULL allocation; ISSUE/RETURN/CONSUME require it. Per-tenant FK to Resource; composite FK for \`(org,allocation_id,resource_id)\` if adding safe \`allocation.resource_id\`; same-org actor membership composite FK; no cascade history deletion.
- Existing \`audit_event\` remains append-only; no duplicate parallel audit table.
- \`idempotency_key\` columns \`org_id,actor_user_id,route_fingerprint,key_hash BINARY(32),body_hash BINARY(32),state, committed http_status,response_json,created_at,expires_at\`; unique composite on \`(org_id,actor_user_id,route_fingerprint,key_hash)\`. Prefer a **single transaction** for claim+business operation+COMPLETED receipt so a rollback cannot commit ghost CLAIMED; no infinite replay loop or success before commit. A distinct permanent stock operation ID is NOT the transient 24h HTTP idempotency key.
- Add necessary bounded query indexes only after actual query plan/unique/FK need; no generic hundreds of indexes.

**Privilege proof:** production runtime MySQL account may SELECT/INSERT but **must be denied UPDATE/DELETE** on \`stock_movement\` and \`audit_event\`; separate, test-scoped elevated migration/fixture credentials execute Flyway/setup/teardown safely. Verify direct runtime SQL UPDATE and DELETE are refused while legitimate INSERT succeeds. A Java comment \`immutable\` does NOT establish append-only SQL.

**Data migration safety:** C1 represents local synthetic test data, not an authorized legacy customer database. On clean install begin with no historical movement. **DO NOT** backfill C1 nonzero existing Resources with fictitious INITIAL/RECEIPT events and claim they actually happened. An existing schema with nonzero Resources lacking ledger requires explicit detected **LEGACY_STOCK_HISTORY_UNRECONCILED** migration/acceptance blocker and separate owner-reviewed opening-balance strategy. All tests use fresh disposable C2 MySQL; no resetting Course A, the user's shared MySQL, or real DB.

**Invariant oracle** at one injected \`sampledNow\`:
- \`T = Resource.totalQuantity\`, persisted physical quantity.
- \`H = SUM(APPROVED allocation quantity where Project.endAt > now)\`.
- \`I = SUM(ISSUED reusable quantity)\`, even beyond project end.
- REUSABLE available \`T-H-I\`, CONSUMABLE available \`T-H\`.
- \`Covered = effective APPROVED + ISSUED + RETURNED + CONSUMED\`, shortage \`max(0,Q-Covered)\`.
- For clean ledger-backed Resource \`T == SUM(stock_movement.delta_total)\`. For REUSABLE \`I == SUM(delta_issued)\`; for CONSUMABLE \`I == 0\`. Source of current truth is Resource total and Allocation states; immutable movements reconcile history. **No persisting available, holds or issued as duplicate mutable totals.**
- \`T >= H+I\` for reusable; \`T >= H\` for consumable; all nonnegative, within 0..1,000,000 bounds. **Never \`Math.max(0, T-H-I)\` on a corrupt invariant**. Existing C0/C1 helper clamps negative arithmetic; change to a controlled invariant failure (sanitized response, transaction rollback, redacted alert) rather than fabricated zero availability; test direct injected inconsistency.
- Period \`startAt <= now < endAt\`; approve can reserve early while \`endAt>now\`; issue/consume require active; expired approved hold drops H at end even before scheduler; issued I never auto-returns; late manual full return allowed.
- All aggregates SQL-scoped by server-authorized org ID; no \`findAll\` or unqualified \`findById\` on caller-visible path, including original numeric \`/api\`.

**Core model business rules**: Create Resource T>0 inserts exactly one INITIAL movement in same transaction; virgin T0 no movement. Receive q>0 increments T once, RECEIPT; adjust to changed newTotal emits ADJUST delta=newT-oldT, no-op is controlled 422, never below H+I; legacy Resource update with expectedTotalQuantity uses old source CAS semantics. Approve reserves without movement; Issue moves H→I and emits ISSUE \`delta_issued=+q\` without changing T; Return moves ISSUED→RETURNED and emits RETURN \`delta_issued=-q\` with T unchanged; Consume from APPROVED consumable decreases T once and emits CONSUME \`delta_total=-q\`; Cancel/expire release only effective holds, **no stock movement**; pending expiry idempotent; never auto-return ISSUED. A resource with movement history cannot physically delete, even if T returns to zero; virgin T0 unused can genuinely delete. Preserve historical Course A R04 semantics as separately noted intentional safety deviation, not falsely declare byte-for-byte behavior parity.

## 2. C2-B — One safe service path, locks and selected ACTUAL REST routes

Implement one reusable \`InventoryService\`/transaction coordinator rather than parallel mutable stock paths. All stock writers (including existing C1 representative legacy resource creation) must either call this service or be expressly blocked until ported. **No way** for a current authenticated controller, scheduler or repository save path to mutate \`Resource.totalQuantity\` or status outside the ledger transaction. No direct HTTP/SMS inside stock transaction. Retrofit/Gson and existing identity/session/Jersey/Javax remain functional.

**Lock order on READ_COMMITTED transaction:** Organization (row lock, reauthorize current user, role and org active under lock) → parent Project (if relevant) → Resource (ordered if multiple) → Requirement → Allocation → receipt/movement. After waiting for locks re-read current status, version, time and aggregate; no stale snapshot. Preserve unique-index DB backstop even if service race check regresses. Lock correctness must be PROVEN under concurrent independent JDBC connections/transactions, not inferred from annotations. Deadlocks roll back the whole transaction and return controlled transient error; only retry when the same stable operation identity is known and business result can be reconciled. No SQL snapshot mixing that counts expired holds incorrectly.

**Authorized role for physical stock:** OWNER or CUSTODIAN; ADMIN alone does NOT get stock power. Other callers are 403 (foreign ID 404). Require fresh ACTIVE user+membership+org checks, session CSRF and server org-scope; do not trust caller orgId/role/status/availability fields. Owned Project LEAD/ADMIN may have request/metadata rights later but never quietly approve/issue stock in C2.

**Minimal working REST surfaces for executable C2 proof** (use existing Jersey, not MVC):
- Preserve existing protected \`POST /api/resources\` source-equivalent create body and response; internally generate INITIAL operation ID. Preserve existing \`/api\` response convention and selected academic lifecycle routes \`POST /api/allocations/{id}/approve\`, \`/issue\`, \`/return\`, \`/consume\`, \`/cancel\` with their O14 bodyless transition semantics where applicable; tenant safe and no extra mandatory modern fields. Other full 28 A APIs are NOT yet ported; do not claim full parity.
- Add versioned selected stock routes per CP35: \`POST /api/v2/orgs/{orgId}/resources/{id}/receive\`, \`/adjust\`, \`GET /resources/{id}/movements\`, \`GET /operations/{operationId}\`, and \`POST /api/v2/orgs/{orgId}/allocations/{id}/approve|issue|return|consume|cancel\` as needed for same-store exercise. Public success \`{data,requestId}\`, error \`{code,message,requestId}\`. CSRF and same-origin on mutations; public v2 id is UUID, original /api is numeric. Never create a parallel stock store.
- Public domain POST must use \`Idempotency-Key\` (random >=128-bit, 24h durable receipt). Existing-resource mutations require strong \`If-Match: "<version>"\` 428 missing/412 stale; original /api does NOT gain these headers. Physical stock commands use stable public UUID \`operationId\` in body; original bodyless issue/return/consume receive **server-generated operation identity**, source-style repeat 409 rather than invented replay receipt. A reauthorized same-key/same-body public retry returns committed receipt even when the originally submitted ETag is stale; same key changed payload=409; new key stale ETag=412. If duplicate operationId same fingerprint returns original durable receipt after CURRENT authorization; changed fingerprint 409. After an unknown HTTP response the caller can GET the existing operation receipt and must not auto-create a fresh logical operation.
- \`GET /operations/{operationId}\`: only original actor or OWNER/CUSTODIAN with same org; never leak a receipt or foreign presence. Bounded cursor pagination for movement lists and no unbounded history reads. Role checks occur also on replay and receipt read. Do not return JPA internal fields.
- Maintain exact source \`@Autowired\` Controller→Spring Component delegation; original \`javax.validation\` BVal DTO validation remains genuinely executed; native Jakarta provider remains. Security HTTP + original five domain/JPA/JPQL/Retrofit/scheduled code must still compile/test.

Avoid adopting optional future-slot reservation, partial/damaged/lost return, cross-org stock transfer, finance, email or cloud; only whole-batch status semantics. Add few focused DTOs, not a generic event-sourcing framework. No redis/queues/microservices.

## 3. C2-C — falsification tests and final integration (actual MySQL)

Use C1 script \`scripts/verify-extended-c1.ps1\` as **one evolving verifier**; reuse its MySQL bootstrap, JDK21, NPM/React and session/CSRF helpers. An optional minimal wrapper is fine, not a duplicate 600-line script. Build using fresh schema from zero, JPA ddl-auto validate, Flyway V1/V2/V3, test runtime and elevated migration accounts. No access to user-owned or academic DB.

Named acceptance IDs, ALL mandatory unless impossibility precisely proven:

- **C1FIX-01** Login with 2 orgs leaves \`activeOrgId=null\`; original /api requires explicit select; selecting then switching to another permitted org updates scope, clears stale data; nonexistent/foreign org refused; logout resets. New account join does not implicitly select.
- **CI-01** Independent Java21/MySQL Extended C CI actually executes tests on current source SHA, distinct from Java8/React; or honest NOT_RUN if free provider blocked. No false green.
- **SCHEMA-01** Fresh MySQL real V1/V2/V3, named FKs/unique/index/check; 256-bit operation ID and receipt storage types correct; direct cross-org malformed movement FK fails.
- **SCHEMA-02** two ISSUE writes for same Allocation (different operation IDs) -> DB UQ denies second, exactly one history; multiple NULL-allocation receipt/adjust rows allowed; runtime credential cannot SQL UPDATE/DELETE movement/audit; migration account has controlled DDL.
- **T/INV-01** reusable T10,Q6,pending q4: availability10/covered0 → APPROVED6/4 → ISSUED6/4, I4 → RETURNED10/4,I0. No T deduction on ISSUE or RETURN.
- **T/INV-02** consumable T100,Q40,pending q30 → APPROVED available70/covered30 → CONSUMED T70 available70/covered30; repeat cannot double deduct.
- **T/INV-03** T10 approved q3 + ISSUED q2 + expired APPROVED q4 at \`now=endAt\` -> H3,I2,availability5; scheduler not run yet same result. Overdue ISSUED still blocks.
- **T/INV-04** two q4 concurrent approvals against T5: one commit, one 409, available1; repeat 100 races if practical without destroying test runtime, record actual count. Cross-org same Resource numeric collision attempt refused without leaking B.
- **T/INV-05** cancellation/expiry makes no movement; ISSUED never auto-returned; full late RETURN allowed; APPROVED early permitted, ISSUE before start refused.
- **LEDGER-01** create T0 no INITIAL; create T7 exactly one INITIAL; RECEIVE(+5), ADJUST new T9, CONSUME as eligible; T == SUM delta_total, I == SUM delta_issued at every committed step and after restart.
- **LEDGER-02** no-op/undercommitted ADJUST rejected and no movement; previously moved Resource T0 protected from physical deletion, virgin unused T0 actually deletable. No history cascades.
- **IDEMP-01** same HTTP Idempotency-Key/body returns one receipt and unchanged T/status after restart; changed body 409; same UUID op after receipt TTL still no duplicate movement; foreign/suspended actor cannot replay receipt.
- **IDEMP-02** Same op/body lost HTTP response scenario: committed once, GET durable receipt, not new movement; failure injected between status/T and movement/receipt rolls back EVERYTHING; no ghost success CLAIMED record.
- **IDEMP-03** Two independent callers race distinct operationId for same Allocation ISSUE; one approved transaction, one controlled conflict; DB constraint forbids doubling even if code path mistake.
- **AUTH-01** OWNER and CUSTODIAN stock allowed; ADMIN/LEAD/MEMBER forbidden; session role/suspension checked at mutation and replay; two orgs same Resource name allowed; cross-org numeric and UUID access 404.
- **C1-REGRESS** real C1 ID01–ID17 (may adjust tests for explicit org selection), Javax/Jakarta provider identity, login/recovery/transfer/last OWNER, session restart, no secret exposure and no real SMS.
- **A-IMMUTABLE** Course Final A K exact code and artifacts unchanged; original five course families and 28 A source routes still runnable at preserved K. Do not claim C2 itself 28-route parity.

Each test asserts actual status, JSON, database rows/unique constraints and after-restart receipts. Independent transactions/latches, no sleep-only “concurrency tests.” If a transaction can return success with incomplete movement, fail the phase.

**Accuracy/security critique:** every test asks: Which role/tenant owns the row? What did MySQL actually commit? Did a second HTTP attempt duplicate? Which timestamp controls expiry? Is the same-JVM javax provider used? Do returned DTOs redact phone/secrets? If movement DB rows are edited/deleted externally, does the verification catch divergence? How much data/page can one list read? Did a previous request change active org? What remains NOT_RUN?

## 4. Technical efficiency, evidence and hard stops

Checkpoint order: C2-A C1 regression/explicit org/CI + migration; C2-B transaction core/selected routes; C2-C real races/replay/packaged restart. One primary writer for POM, SQL migrations, security config and verifier. Delegate read-only narrowly if truly available on free Luna; no fake subagents. Keep working code/tests per phase; targeted run per change, one final full gate. No exhaustive code archaeology or new research.

**STOP** dependent work if (a) cannot preserve C1 auth/CSRF/tenant safety, (b) Flyway destroys data or requires inventing physical stock history, (c) literal Javax validator becomes nonfunctional, (d) deadlock/duplicate movement or negative stock survives concurrent commit, (e) Course A paths are modified, (f) runtime MySQL privileges cannot enforce append-only and no safe isolation alternative is already approved. Push safe partial reproducible work and flag exact blocker; never weaken assertions, fake passing CI, or silently select another architecture.

Add one concise \`evidence/C2/PHASE_REPORT.md\` with:
- Fresh audit parent C1/C0/A3, protected K, branch, PR ancestry and exact code SHA.
- Change paths, migrations (checksums), precise stock math and movement kind/delta table.
- Actual test table IDs, commands, Java/Node/MySQL versions, result counts/DB postconditions, CI job URL where actually run, failure injection and concurrency/restart metrics.
- Verified root cause/fix for first-org auto-selection, existing C1 runtime and updated tests.
- Course preservation + C-only selected routes vs full 28 route parity NOT_RUN.
- Credential/append-only direct SQL privilege negative test; secret/PII scan, port binding.
- Unresolved test, hosting, privacy, provider, image/UI/full API gaps and next C3 dependencies.
- Local commit, remote verified SHA, draft PR URL.

**Git mandatory**: fresh status, preserve user work, no clean/reset/force/merge. Stage ALL legitimate source/test/Flyway/safe docs/CI, exclude secrets, password dumps, JAR, node_modules, target/dist, local DB, raw SMS/phone. \`git diff --check\`, commit/push feature branch, verify remote SHA, draft PR against current C1 branch (if unmerged), no auto merge. **No public deployment, cloud trial, payments, live Twilio, real Ateneo data, official user outreach, Canvas, C3 work, or frozen A alteration.**

**Verdict exactly one**:
- \`C2 VERIFIED — ATOMIC INVENTORY/LEDGER AND REPLAY GATES PASS, READY FOR C3 REVIEW\`
- \`C2 CONDITIONAL — EXACT FAILED/NOT_RUN GATES\`
- \`C2 BLOCKED — MATERIAL DATA/SECURITY/STACK INTEGRITY ISSUE\`.

Start the software build now, not a documentation-only research phase; stop after C2 and provide the actual evidence.

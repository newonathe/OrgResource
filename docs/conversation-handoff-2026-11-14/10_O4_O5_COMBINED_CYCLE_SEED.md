# Prepared next LARGE coherent Codex build: O4+O5 Allocation Engine + Expiry Scheduling
**USE ONLY AFTER A FRESH GITHUB AUDIT AND CONFIRMED O3 COMPLETION/REPAIR.** This is a seed contract for the NEXT full Codex prompt, not a claim O4/O5 implemented.

## 1. Goal and cycle size
A single, substantial backend cycle should deliver all remaining six original Allocation state-changing routes A03–A08, the separately proposed A09 consent revocation if authorized (yes in reviewed handoff but original proposal 25, keep amendment explicit), correct atomic availability/coverage and in-DB transactions, clock-based boundary semantics, and actual expiry scheduler. Group these closely dependent features together because repeated approval/expiry/availability logic and shared tests make artificially separate sessions wasteful. **Internal test gates** at states/transactions/scheduler, but do not stop after each method if following steps are unblocked.

## 2. Mandatory start and sources
Fresh connected GitHub remote audit (main/feature branch/PR/CI) before crafting execution prompt; Codex fresh local git status/remote/working tree audit before edits; do not assume O3 19/25 passed. Read repo `AGENTS.md`, actual current Java source/route parity, `docs/ORGRESOURCE_MASTER_HANDOFF.md` relevant passages only, `docs/complete-handoff/04_DATA_MODEL_AND_INVARIANTS.md`, `05_API_26_ROUTES_AND_RESPONSES.md`, `06_PERSISTENCE_TRANSACTIONS_JPQL.md`, `07_RETROFIT_SCHEDULING_SECURITY.md` for expiry, `09_TEST_POSTMAN_DEMO_ACCEPTANCE.md`, `evidence/O3` latest, relevant course source examples/assignment as needed. Do not read nine-entity historical contracts.

## 3. Exact state and API operations
A03 POST `/api/allocations/{id}/approve`, no body. PENDING only, project not ended, enough Resource available and Requirement uncovered qty; approvedAt timestamp, status APPROVED; early approval before project start allowed; reserve globally, physical total unchanged. Errors missing404, invalid state/stock/demand/end409; no partial write.
A04 POST `/api/allocations/{id}/issue`, no body. Status APPROVED, ResourceType REUSABLE, active Project [startAt,endAt), status ISSUED, issuedAt timestamp. Reservation H decreases q while I increases q, same available; no second stock deduction; repeat409.
A05 POST `/api/allocations/{id}/return`, no body. Status ISSUED and REUSABLE, whole batch confirmed physically returned usable, status RETURNED, returnedAt timestamp; late return permitted; I decreases q => available rises q; Coverage remains historically fulfilled. Repeat409. No partial/damaged scenario.
A06 POST `/api/allocations/{id}/consume`, no body. APPROVED CONSUMABLE active Project; status CONSUMED, consumedAt, physical total T decremented q exactly once IN SAME transaction; approved H decrements q simultaneously; available stays same. Repeat409, physical total unchanged on refusal and forced rollback.
A07 POST `/api/allocations/{id}/cancel`, no body. Only PENDING or APPROVED; if before Project end USER_REQUEST, after end EXPIRED, set cancelledAt and status CANCELLED; PENDING never stock hold, APPROVED effective hold released only once and no physical total change; preserve approvedAt; repeat409, completed states protected.
A08 POST `/api/allocations/{id}/delete`, no body. Physical DELETE only PENDING never-approved or CANCELLED never-approved, historical approvedAt!=null blocks forever; ISSUED/RETURNED/CONSUMED blocked. 404 nonexistent, 409 HISTORY_PROTECTED or INVALID_ALLOCATION_STATE as contract.
A09 proposed POST `/api/allocations/{id}/revoke-sms-consent`, no body. For existing any status, set smsConsent false, clear phoneNumber; no inventory/status mutation. Repeat safe, no invented SMS cancellation semantics after dispatch; protected public DTO. Label additional route separately in route count.

## 4. Required transactions and locking
All inventory-mutating A03–A08 should use Spring `@Transactional` service methods. Lock order Project PESSIMISTIC_WRITE then Resource PESSIMISTIC_WRITE then Allocation PESSIMISTIC_WRITE (or contractually safe equivalent if validated) to prevent deadlocks; after locks re-read/refresh entities/associations to avoid JPA persistence-context staleness. Do not rely on self-invocation proxies or @PostConstruct to activate transaction. Determine ancestor identities for lock path safely, revalidate after lock; sample one Asia/Manila Clock now per operation. Recompute H/I/Covered inside correct atomic transaction; check overflow/available>=0 and coverage<=requirement; update status and Resource total via flush so response and fresh GET agree. MySQL InnoDB authoritative, not H2. Avoid holding locks across remote HTTP (SMS comes O6). Recognized contention may 503 RETRY_REQUIRED, not blind non-idempotent POST retry. No new JPA entity, table, or generic event bus.

A03 preconditions: `status==PENDING`, project.endAt>now, qty+currentCoverage<=requirement.quantity; available>=qty. If two different requirements compete for same Resource, Resource lock serializes; if same Requirement, Project lock serializes and coverage updated. Future Project start does not prevent APPROVE. After approval, T unchanged, H increases q and available decreases q.
A04 ISSUE: change Approved to Issued during active interval, T unchanged, H decreases, I increases. A05 RETURN: issued custody released even after end, no T decrease. A06 CONSUME: under lock ensure T>=q; set CONSUMED and T->T-q, H decreases => available not double deducted.
After Project.endAt: H zero automatically; expired unfulfilled statuses not currently effective coverage; cleanup changes stored states but must not alter physical totals or negative H a second time.

## 5. O5 time/expiry scheduler implementation
- Existing `SpringBootApp.java` has `@EnableScheduling` but O1 no real @Scheduled expiry job.
- Add small bounded scheduled scanning component (no new entity) with configurable interval and `@Scheduled` actual invocation, plus separate transactional worker component method for processing one candidate; avoid self-invocation transaction pitfalls.
- `AllocationRepository.findExpiredCandidateIds(now,pageable)`: ID list PENDING or APPROVED whose Requirement.Project.endAt<=now, ORDER BY id, bounded page. Process each ID in its own transaction; lock Project→Resource→Allocation; recheck current status and endAt; update status CANCELLED, cancellationReason EXPIRED, cancelledAt now. approvedAt retains historical marker. If approval already expired, H was already effective zero; do NOT subtract stock. PENDING expiry no stock effect. ISSUED overdue remains ISSUED and physically unavailable pending genuine return.
- Concurrency: user cancel vs scheduled expiry, user return vs expiry, approval at end boundary, double scheduled worker, process crash mid-loop; idempotent after restart, not repeated double release. Use fixed-clock boundary tests before, at, after.
- Scheduling must actually run under real Spring context and be observed with bounded demo interval; calling helper only is NOT proof of @Scheduled. No exposed public time modification route, no unsafe DB resets. Work with Asia/Manila local semantics.

## 6. Exact performance and storage guardrails
- No added service/database/StockMovement. Existing pool of five may remain until measured need. Avoid N+1 list queries where relevant using narrow aggregate queries/indexed joins; do not add unnecessary indexes. Bound scheduler queries and transactions, no full-table scan after every request; filter by status/end relationships. Derived H/I/Covered fields remain computed, not persisted.
- Use correct query cardinality and Long SUM accumulation, null->0, avoid integer overflow.
- Use DB unique/FK constraints and locks rather than Java-only check for concurrency.
- Preserve redacted phone in all DTOs, no real contacts, secrets, or user-specific local credentials. Loopback only default.
- No extra observability tooling, queues, Redis, SaaS services; no performance benchmarks without actual measurements.

## 7. Focused acceptance / real execution tests
A. Java8 clean Maven package+tests, Jersey startup, JPA query initialization; previous O1/O3 regressions.
B. Actual isolated fictional MySQL state:
   - reusable T10, Req6, Allocation A qty4: PENDING available10/covered0; APPROVED avail6/covered4; ISSUED avail6/covered4; RETURNED avail10/covered4.
   - another reusable B qty2: APPROVED avail8/covered6; ISSUED avail8/covered6; RETURNED avail10/covered6.
   - consumable T100, Req40, Allocation qty30: PENDING avail100/covered0, APPROVED avail70/covered30/total100, CONSUMED avail70/covered30/total70, repeated CONSUME 409 total70.
   - cancellation PENDING no stock effect; cancellation APPROVED removes hold but never decreases total; cancellation after approval cannot be deleted even when status CANCELLED.
   - APPROVED future early hold yes; issue/consume outside active period 409; return after end permitted.
   - expired APPROVED before cleanup no hold/coverage, cleanup cancellation no extra release; expired PENDING cleanup harmless.
   - overdue ISSUED remains issued/unavailable after expiry scanner.
C. **Real parallel JDBC/HTTP** approving two qty4 allocations against stock5, exactly one succeeds; against Requirement6, exactly one succeeds; Resource stock correction vs approval/consume, Project update vs approval race; demonstrate DB consistency. Forced exception/SQL rollback during CONSUME must revert both Allocation and Resource changes.
D. Repeated calls to all endpoints return controlled 400/404/409 as required and unchanged DB, including malformed ID and protected history.
E. Scheduled job actually observed in running Spring context, real DB mutated and restart idempotent.
F. Dynamic-ID Postman collection for A03–A09 and expiry interactions; execute via Newman; no assumption IDs=1; ephemeral fixtures and no destructive cleanup of prior user data.
G. One end-of-cycle regression O1, O3, O4+O5, Java tests; evidence report explicit PASS/FAIL/BLOCKED/NOT_RUN. If Codex Cloud lacks user's MySQL credentials, do not pretend live MySQL tests ran; isolated integration env if available else exact safe local instructions.

## 8. Git, handoff, reporting
Codex must begin with local and remote git audit, use dedicated feature branch, preserve old user edits and demo scripts, never merge main automatically. At cycle end review diff, secret scan, stage ALL legitimate task-related code/test/Postman/docs/evidence, commit, **PUSH** to remote feature branch, verify remote SHA, open/update PR. No unauthorized force push/main merge/DB reset/Canvas or real SMS. Report exact current original route count 25/25 if all six original A03–A08 added to O3 19; A09 optional extra separately 26th. Report code-level tests, real MySQL concurrency/expiry evidence, remaining risks, and next recommended O6+O7 coherent build. Do not claim O4+O5 DONE until proper gates execute.

## 9. What not to include in O4+O5
Do NOT implement Retrofit SMS provider or frontend in this combined cycle; O6+O7 is next. Do not add images unless course requires confirmed; defer to O12. No new table/users/audit ledger, no self-service auth, no deployment infra, no handmade video. Avoid splitting O4 state transition into six costly separate chat prompts: implement and test as one coherent transactional engine.
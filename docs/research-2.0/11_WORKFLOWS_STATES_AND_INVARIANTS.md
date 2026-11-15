# V2 workflows, reservation windows, custody states and concurrency invariants
**Status:** PROVISIONAL RECOMMENDATION — algorithm specification; no implementation/tests. **Date:** 2026-11-15. Sources C03/G06–G09 (academic V1), design F08–F23.

## Time semantics and capacity truth
- All stored instants UTC with frontend display `Asia/Manila`. User enters start/end with explicit zone, normalized at service edge. `W=[s,e)` with s<e. Overlap function `a.start < b.end AND b.start < a.end`; adjacent edges not overlapping.
- Never treat planned release as physically observed return. Date-specific availability for a reusable **reservation decision**:
  `requestedReusableHeadroom(W) = T - outstandingIssued - SUM(APPROVED.q where overlaps(APPROVED.W,W))`.
  Outstanding issued is counted once and conservatively blocks **every** future period until usable return is recorded. This can reject legitimate distant reservations, but avoids making unverified return assumptions. Stock accounting must not subtract an ISSUED allocation again as an APPROVED hold.
- **Consumable** reservation headroom: `T - SUM(q of all APPROVED nonconsumed reservations)` irrespective of windows, because consumed stock never becomes reusable after end. Consume operation atomically subtracts q from both T and approved hold; headroom stays constant at the consume instant.
- For reusable, approved disjoint reservations may both occupy full T. Once the first is issued and remains outstanding, an *already approved* later nonoverlapping reservation becomes **AT_RISK**. This is an operational exception, **not** a historic retroactive overbooking of the planned windows. At its issue time refuse issue if real stock unavailable; notify custodian and affected project. No application can guarantee future timely physical returns.
- Effective reservation expiration: an approved hold whose project window has ended does not count in newly requested future windows, even before worker cleanup; expired awaiting cleanup still retains immutable approval history and is marked EXPIRED transactionally later.

### Worked timeline, total 5 reusable tripods
| Time and action | Approved effective windows | Outstanding issued | New W availability/explanation |
|---|---|---|---|
| Nov 16, before use: approve A q4 [Nov20 09,12) | A4 | 0 | A headroom 1 |
| Before use: request B q3 [Nov20 11,13) | A4 | 0 | B overlap headroom 1 → **409** |
| Before use: approve C q5 [Nov20 12,14) | A4,C5 (disjoint) | 0 | C headroom 5, approved |
| Nov20 09: issue A4 | C5 | 4 | 1 physical usable immediately; C at risk unless return before 12 |
| Nov20 11: return A3 usable | C5 | 1 | Now 4 physically usable; C still at risk for q5 |
| Nov20 12: attempt issue C5 | C5 | 1 | **409 insufficient physical stock**; cannot override C reservation into a phantom issue |
| Nov20 12: return final A1 | C5 | 0 | 5 physically usable; C issue can proceed if within C window |
This is **fictional acceptance arithmetic**, not Ateneo inventory. T is 5 throughout; issue/return does not change T. Retiring/loss of outstanding unit would require separate authorized event and audit (deferred).

### Example consumable, T=100 sheets
Approve future q30: T100, approved30, headroom70. Consume approved q30 during active project: T70, approved0, headroom70. Duplicate consume 409, T stays70. Cancel approved q30 instead: T100, approved0, headroom100. Never use calendar disjointness to promise future consumed units again.

## Exact state transition catalog
| Entity | Legal transitions | Refused transitions/status |
|---|---|---|
| Organization | ACTIVE→CLOSING→CLOSED; CLOSING→ACTIVE controlled recovery | CLOSED no new members/resource writes |
| Membership | INVITED is invitation not membership; membership ACTIVE↔SUSPENDED, ACTIVE→LEFT | Last OWNER cannot leave/demote |
| Invitation | INVITED→ACCEPTED/EXPIRED/REVOKED | No replay after terminal state |
| Project | DRAFT→ACTIVE/CANCELLED; ACTIVE→CLOSED/CANCELLED subject commitments | Changing committed interval refused |
| Requirement | created→quantity increase/decrease if >=committed; archived only when no history | No foreign-org resource or negative quantities |
| Allocation reusable | PENDING→APPROVED or CANCELLED/EXPIRED; APPROVED→ISSUED or CANCELLED/EXPIRED; ISSUED→PARTIALLY_RETURNED or RETURNED; PARTIALLY_RETURNED→PARTIALLY_RETURNED or RETURNED (only strictly positive return) | RETURNED terminal, no automatic return; consume refused |
| Allocation consumable | PENDING→APPROVED or CANCELLED/EXPIRED; APPROVED→CONSUMED or CANCELLED/EXPIRED | No ISSUE/RETURN; CONSUMED terminal |
| Stock movements | Append new movement event only, never update existing | Editing history/negative total/replayed key refused |

**P0 simplification:** Usable partial returns supported as quantities, not individual serial-number custody; defective/lost returns are **explicitly unsupported** and cannot be silently recorded as usable. User decisions may defer partial and force whole batch, in which case adjust F19/ERD/API/tests coherently, not just the screen copy.

## Concurrency protocol — InnoDB candidate
1. Validate request syntax/auth and org role before transaction; never expose raw tenant rows.
2. Begin DB transaction, lock `resource (org_id,id) FOR UPDATE` first. Fetch & revalidate bound project/requirement, lock requirement then allocation in fixed order. Avoid lock inversion across stock updates, expiry and cancellation.
3. Validate clock time, allocation state, quantity, demand cover, any existing idempotency result, overlap-window holds under serialized resource lock. **Every mutation on a resource must acquire the same row lock**; preventing concurrent race does not depend on a fragile locking range scan.
4. Persist state and append stock/audit/idempotency records atomically; update `resource.version` on any quantity mutation; commit.
5. Build response from committed state; never call SMS, email or third-party network while holding row lock.
6. Deadlock / serialization failure: rollback entire transaction, issue bounded retry only for internally safe idempotency-keyed operation, then 409/503 with retry guidance. Do not convert to success.
7. Unknown client response: client re-fetches by idempotency receipt/operation status, **never blind resubmission**.

**SQLite/D1 caveat:** Row-level `FOR UPDATE` is not portable to D1. Any edge implementation must use supported serialized transactional statements / compare-and-swap and documented D1 batch transaction guarantees; no Java/InnoDB code reuse claim. If atomic approve across multiple rows cannot be proved with D1 tooling, D1 is **NOT ACCEPTED** for production bookings.

## Invariants to test after every mutation
- `T>=0`; `I>=0`; `I<=T` unless an explicit reconciled exceptional state blocks new writes.
- `0<=returnedQty<=issuedQty<=allocationQty`.
- `SUM(ledger.delta_total)=T`; `SUM(ledger.delta_issued)=I`.
- An approved allocation quantity contributes to exactly one stock reservation class; issued is not double counted as approved.
- Per resource, simultaneous overlapping approved windows never exceed capacity **at time of approval**. New future attempts respect unresolved issued custody.
- Historical covered quantity calculation includes fulfilled RETURNED/CONSUMED and excludes PENDING/CANCELLED, expired unfulfilled APPROVED; requirement shortage=max(0,q-covered) without inventing extra stock.
- Idempotency duplicate does not append extra ledger/audit or change quantities.
- No tenant FK references a different org.

## Race and boundary tests
T1 approve q4/q4 concurrently on T5: exactly one success. T2 approve q4/q4 same requirement demand6: exactly one success. T3 issue/cancel same approved row: one winner. T4 return q2 and q3 concurrently for outstanding4: only valid aggregate <=4. T5 scheduler expires approved while custodian issues: exactly one terminal path. T6 lost HTTP response after consume: retry by same idempotency key no double deduction. T7 approval at `now=end` rejected; `now=start` allowed issue; half-open overlap adjacency tested. T8 overdue issued blocks a later booking's physical issue despite prior nonoverlap approval. T9 inject DB failure after ledger insert -> full rollback. T10 cross-tenant ID substitutions fail in both service and composite FK constraints. Record logs, actual DB version and transaction isolation; **NOT EXECUTED**.

## CP2 MySQL isolation and replay correction — research only (2026-11-15)
**Course technology preservation:** O14 academic reusable allocation return remains WHOLE BATCH. V2 PARTIALLY_RETURNED is an unapproved optional extension, never automatic replacement of the original Jersey/Javax/MySQL/Retrofit and five-entity contract. [Conflict report](28_STRICT_COURSE_STACK_COMPATIBILITY_AND_CP2_CONFLICTS.md).
**MC-03 stale aggregates:** The draft resource FOR UPDATE strategy requires a chosen MySQL isolation mode, because an earlier non-locking SELECT within a REPEATABLE READ transaction can establish a stale consistent snapshot. All writers must lock the resource row **before any capacity-related SELECT**, then evaluate a current aggregate under separately proven READ COMMITTED or correctly locking current-read semantics. Apply one global lock ordering to approval, issue, return, stock adjustment and expiry. MySQL primary documentation: [transaction isolation](https://dev.mysql.com/doc/refman/8.4/en/innodb-transaction-isolation-levels.html); [locking reads](https://dev.mysql.com/doc/refman/8.4/en/innodb-locking-reads.html). No executable proof currently exists.
**MC-04 late replay:** Expiring HTTP idempotency receipts after 24h must not allow a duplicate physical ISSUE, RETURN or CONSUME. Define a durable operation identity (unique within org) bound to the corresponding immutable stock movement and state transition. Require test after receipt expiry, power loss after commit, duplicate concurrent requests and operation-key/body mismatch.
**Acceptance to be actually run by engineering later:** stock5, two q4 same-window approvals exactly one succeeds; 100 simultaneous requests no negative/phantom stock; transaction with prior ordinary SELECT never accepts stale booked capacity; cancel-vs-issue and expiry-vs-issue one winner; cross-tenant FK violations rejected at DB level; ledger checksums reconcile after clean restore. Current outcome NOT RUN. The last original five entity APIs remain independently runnable.

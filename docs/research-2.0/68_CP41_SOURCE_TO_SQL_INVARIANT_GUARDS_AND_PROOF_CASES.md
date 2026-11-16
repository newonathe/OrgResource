# CP41 — Last source-to-schema corrections, physical integrity guards, and executable acceptance oracles

**Date:** 2026-11-16 (PHT). **Classification:** **VERIFIED SOURCE** for O14 column/endpoint mapping; **VERIFIED EXTERNAL FACT** for cited MySQL syntax; **DESIGN RECOMMENDATION** for actual new DDL/locking/replay/test details. **NO code, migration, SQL, MySQL tests or deployed runtime was executed in this planning turn.** This is a targeted contradiction fix, not a new product or permission to run sealed Codex prompts. Read [CP34](61_CP34_CANONICAL_SCHEMA_AND_CUTOVER_BLUEPRINT.md), [CP35](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md), [CP36](63_CP36_LEGACY_COMPATIBILITY_ATOMICITY_AND_CUTOVER_GATES.md), [CP39](66_CP39_PINNED_CI_TRIAGE_AND_LAST_CONTRACT_CORRECTIONS.md), [CP40](67_CP40_PROVIDER_TRIAL_SECURITY_AND_DEADLINE_REALITY_CHECK.md).

## 1. Five original JPA entities: complete physical column compatibility proof

**VERIFIED O14 SOURCE:** examined all five `backend/src/main/java/app/entities/{Department,Project,Resource,ResourceRequirement,Allocation}.java` at academic source SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`. Identified **35 original named physical columns**, including JoinColumn FKs. A machine-readable per-column source→target mapping was committed as [35-column JSON crosswalk](contracts/O14_TO_UNIFIED_FIVE_ENTITY_COLUMN_MAPPING_CANDIDATE.json).

### Two **actual** source/draft naming conflicts resolved by paper decision

| Source O14 SQL | Erroneous previous CP34 candidate | Corrected recommended new SQL | Public v2 JSON |
|---|---|---|---|
| `resource.type` | `resource.kind` | **`resource.type`** | `ResourceView.kind` |
| `allocation.borrower_name` | `allocation.borrower_name_snapshot` | **`allocation.borrower_name`** immutable name snapshot | `AllocationView.borrowerNameSnapshot` |

The source class names and original 26 business JSON contracts remain separate from public JSON naming; a public DTO property rename does **not** require a physical database rename. **Other intentional modernization**: `allocation.phone_number` original plaintext becomes `phone_ciphertext/phone_nonce/phone_key_id` or is absent when real SMS disabled; original legacy `borrowerName/phoneNumber` JSON input handling is adapted by secure component, not by passing the original JPA entity directly over HTTP. Source `LocalDateTime` PHT becomes UTC `DATETIME(6)` and explicit time conversion. No real historical O14 database is claimed migrated. The literal O14 `javax.persistence` mapping must be adapted/experimentally proven for a maintained Jakarta provider; reusing an identical entity classname alone does not prove compatible JPA.

**Course requirement vs implementation:** the assignment requires five justified entities and JPA/Javax Validation, not unmodified physical column names. Retaining unchanged names is a **risk-reducing recommendation**, not a fabricated instructor demand. Full protected old GET/POST source signatures and 26-route static crosswalk remain in [CP39](66_CP39_PINNED_CI_TRIAGE_AND_LAST_CONTRACT_CORRECTIONS.md).

## 2. Inventory conservation needs a measurable SQL oracle

For a **freshly installed** unified schema, every Resource r begins either `T=0` with no movement, or `T=q>0` with exactly one committed INITIAL(`delta_total=+q`). Later every legitimate RECEIPT/ADJUST/CONSUME changes both `resource.total_quantity` and one immutable movement atomically. ISSUE/RETURN change `I` through the Allocation status and custody movement `delta_issued`, not `T`. Query allocations at one DB/clock instant:

`T = SUM(delta_total)` over that Resource's ledger; `I = SUM(delta_issued)` for reusable only; `H = SUM(APPROVED quantities with Project.endAt > now)`; available = `T-H-I` reusable or `T-H` consumable.

**Required assertions in S4/QA:** (1) `T>=0, H>=0,I>=0,available>=0`; (2) reusable `T>=H+I`; consumable `I=0,T>=H`; (3) one INITIAL for nonzero create, zero INITIAL for virgin T0; (4) exactly one ISSUE, RETURN or CONSUME per allocation lifecycle; (5) approvals/expiry/cancel create **no** physical movement; (6) after Project.endAt exclusive, an APPPROVED hold stops counting **even before** scheduler physically cancels; ISSUED custody persists until RETURN; (7) if a write fails, both ledger and current row roll back and results remain reconciled after restart; (8) no retroactive invented history when later authorized import does not exist in P0.

**Permanent duplicate prevention:** CP34's `UNIQUE(org_id,operation_public_id)` protects replay of a caller-defined physical operationId. Add **separate** `UNIQUE(org_id,allocation_id,kind)` on `stock_movement`. For ISSUE/RETURN/CONSUME, allocation_id is required and permits one event of each kind per Allocation. For INITIAL/RECEIPT/ADJUST, allocation_id is NULL; MySQL unique keys permit multiple NULL values. This is a DB-invariant backstop even if authorization/status logic regresses. The service still uses one org-root/Resource/Requirement/Allocation lock protocol and validates the transition under lock.

**Ledger non-editability:** `stock_movement` and `audit_event` are INSERT+SELECT for the **runtime** DB principal, not UPDATE/DELETE; migration principal alone has separately controlled DDL privileges. A table comment, Java `final` or an unprivileged UI is insufficient to prove append-only protection. A genuine SQL UPDATE/DELETE attempt by the runtime credential must fail in S3/S4 integration checks.

**Not optional:** prevent cascading deletion of ledger/audit sources, preserve movement actor Membership ID, allocation Resource ID and tenant relationship, and log safe target references for record deletion with no FK to deleted historical target. Cross-org compound FKs must be verified by actual direct SQL negative test, not only HTTP filters.

## 3. The “one pending ownership transfer” promise needs a real database constraint

CP34 says at most one PENDING transfer per org but previously only promised application locks. A plain `UNIQUE(org_id,status)` is wrong: it would permit only one ACCEPTED or REVOKED historical transfer per org.

**Suggested MySQL 8.4 DDL fragment (design candidate, NEVER executed here):**

```sql
-- Illustrative ALTER: only use in an actual reviewed versioned migration.
ALTER TABLE ownership_transfer
  ADD COLUMN pending_org_id BIGINT
    GENERATED ALWAYS AS (
      CASE WHEN status = 'PENDING' THEN org_id ELSE NULL END
    ) STORED,
  ADD CONSTRAINT uq_ownership_transfer_one_pending
    UNIQUE (pending_org_id);
```

MySQL permits UNIQUE generated-column indexes and multiple NULLs, supporting one PENDING/org and many historical terminal rows. [MySQL generated columns](https://dev.mysql.com/doc/refman/8.4/en/create-table-generated-columns.html), [unique index and NULL handling](https://dev.mysql.com/doc/refman/8.4/en/create-index.html). **Actual server+version must test it before freeze.** Under tenant lock, when pending transfer is past expiresAt, atomically mark EXPIRED before creating a new request; do not bypass uniqueness. Last effective ACTIVE OWNER is separate cross-row invariant and requires serialized service transitions plus real concurrent tests. Generated indexes do not establish those transitions automatically.

## 4. Idempotency keys and HTTP preconditions must not contradict A09

- **Public domain POST**: 24h HTTP idempotency receipt `Idempotency-Key` + request fingerprint. The claim, authorized business write and durable receipt should commit atomically where practical. If a claim can commit separately, define an explicit stuck-claim recovery policy and never equate stale `CLAIMED` with a successful result. Rollback must not leave a ghost success receipt. No HTTP provider call while stock locks are held.
- **Physical movement**: stable UUID operationId has **permanent** uniqueness independent of HTTP receipt TTL; same UUID+same fingerprint returns the original committed movement only after current-role reauthorization; changed fingerprint =>409. The public browser queries `GET /operations/{operationId}` after unknown response before new attempts. Legacy bodyless A04/A05/A06 use server-issued physical operation IDs and existing state/lock guards, not non-existent new request fields.
- **Zero-stock new Resource**: no INITIAL movement; therefore the long-lived **stock** movement table does not provide a permanent create receipt for T0. Public create's HTTP Idempotency-Key only covers its stated 24h TTL; duplicate name uniqueness protects the still-existing Resource, but after deletion or rename and beyond TTL no guarantee of lifelong duplicate-free zero-stock create. This is a stated edge, not a contradiction.
- **A09 revoke SMS**: O14 legacy POST is repeated-success at source, but public v2 requires `If-Match` on mutable existing resources. Correct API priority: same `Idempotency-Key` retry can return prior committed result **after reauthorization**, even with stale original ETag; new operation with stale If-Match is 412; new operation with current If-Match on already-revoked allocation is 200 idempotent no-op with **no version increment**. Protected legacy A09 remains repeat 200 without If-Match, no further eligible SMS; neither API can retract provider request already in flight.

## 5. Focused acceptance and fail-stop matrix (not executed)

| Test ID | Method | Required actual result |
|---|---|---|
| MAP41-01 | Inspect/compare physical MySQL columns and Java mappings | SQL `resource.type`, `allocation.borrower_name`, actual old DTOs `type/borrowerName`, new v2 `kind/borrowerNameSnapshot`; no ghost columns |
| MAP41-02 | Run literal `javax.validation` and actual Jakarta JPA entity registration, queries and invalid-field saves | Report exact supported path, positive and negative; if unsupported STOP |
| DB41-01 | Raw SQL second PENDING transfer same org while first pending | unique violation; previous terminal record coexistence permitted |
| DB41-02 | Concurrent owner handoffs+account disable+role mutation | at all commit boundaries ≥1 ACTIVE OWNER for ACTIVE org; no double pending/owner gap |
| DB41-03 | Two workers attempt ISSUE one Allocation with distinct operation IDs | one committed ISSUE event `UNIQUE(org,allocation,kind)`; no double custody |
| DB41-04 | Unrelated RECEIVE/ADJUST records with NULL allocation_id | multiple allowed by unique triple, no false reject |
| DB41-05 | Runtime credential direct UPDATE/DELETE on stock_movement and audit_event | denied; legitimate append succeeds and read remains permitted |
| INV41-01 | T10 reusable, APPROVED3, ISSUED2, expired APPROVED4 | T10,H3,I2,available5 before scheduler; no movement on expired hold |
| INV41-02 | ISSUE->RETURN after project end; consumable APPROVED5->CONSUME | sum ledger deltas exactly equal T and I; no duplicate movement on retry |
| INV41-03 | Cross-tenant direct SQL invalid allocation resource/requirement linkage | rejected by compound FKs (not only API) |
| IDEM41-01 | Same physical operation replay after HTTP receipt expiry and restart | no duplicate movement; read original durable receipt if authorized |
| IDEM41-02 | Crash between HTTP receipt claim and business write; retry | no ghost committed success, no double state write, bounded recovery behavior |
| IDEM41-03 | Public A09 repeat with same key, stale new key, current ETag | receipt replay 200; stale new op 412; current already-revoked 200 no-op; no version bump |
| AUTH41-01 | Suspended member replays a previously successful idempotent stock operation | current authorization denies it; receipt does not leak cross-tenant history |
| CORE41-01 | Virgin T0 Resource eligible delete vs formerly moved T0 | first physically removed; second rejected with history protected; R04 instructor edge documented |

**Future evidence fields** `testId, exact code SHA, MySQL version, migration checksum, ORM/validator versions, run command, SQL/HTTP request, expected and actual row counts, source fixture, CI job URL, PASS/FAIL/BLOCKED/NOT_RUN`. All rows currently **NOT_RUN** on the proposed product.

## 6. Primary-source security finding: existing JPA repository methods are GLOBAL

**VERIFIED O14 SOURCE** at the same pinned academic SHA, directly read the actual five `backend/src/main/java/app/repositories/*Repository.java` classes:

| Original repository/source method | Why unchanged reuse violates new multi-org contract | Recommended secure replacement/query pattern |
|---|---|---|
| Department `existsByNameKey(String)` | Rejects valid same-name Department in two different orgs; old `@UniqueConstraint(name_key)` is global | `existsByOrg_IdAndNameKey(orgId,nameKey)`; change DB UQ to `(org_id,name_key)` |
| Project `findByDepartment_Id(Long)` | Caller may pass a foreign tenant's numeric Department ID; unscoped listing exposes it | `findByOrg_IdAndDepartment_Id(orgId,departmentId)` after authorized same-org Department lookup |
| Resource `existsByNameKey`, `existsByNameKeyAndIdNot`, `findByType` | Global duplicate rejection, global type list, and potential foreign-tenant lookup | `existsByOrg_IdAndNameKey`, `existsByOrg_IdAndNameKeyAndIdNot`, `findByOrg_IdAndType` |
| ResourceRequirement `findByProject_Id`, `existsByProject_IdAndResource_Id` | Unscoped foreign project/resource pairing or disclosure | `findByOrg_IdAndProject_Id`, `existsByOrg_IdAndProject_IdAndResource_Id`; enforce composite FK for both parents |
| Allocation `findByRequirement_Id`, `findByStatus`, `findByRequirement_IdAndStatus` | Foreign-tenant Allocation list possible, even under a logged-in user | `findByOrg_IdAndRequirement_Id`, `findByOrg_IdAndStatus`, `findByOrg_IdAndRequirement_IdAndStatus` with role/Project scope |
| Inherited `JpaRepository.findAll/findById/deleteById`, each original `lockById` | Global methods remain available despite new org_id column; controller-only filtering is insufficient | Ban unqualified repository paths from user-request Components; use `findByOrg_IdAndId` and tenant-scoped `SELECT...FOR UPDATE`; only separately authorized internal job may span orgs with explicit system scope |
| Allocation grouped JPQL `sumEffectiveHolds`, `sumIssuedCustody`, `sumResourceCommitments` | A future bug passing wrong resource IDs can mix tenant sums; original JPQL has no orgId predicate | Add **orgId** to every grouping/filter and join via related org ID; inject scoped Resource IDs; test result and DB postcondition |

**Essential actual mapping:** O14 Source `Resource` and `Department` use `@Table` global unique `name_key`; the new table must replace those constraints with `UQ(org_id,name_key)`, not retain both. `ddl-auto=update` is unacceptable as a migration policy: it may leave old global indexes and untested old schema artifacts. Run a real new empty MySQL schema with reviewed explicit DDL migration and JPA validation. **Do not claim the old Component methods can be copied verbatim and magically gain organization scoping.**

**Protected numeric /api** lacks an orgId path; resolve active org from server session, not `X-Org-Id` supplied by the client. Every old controller must delegate to a tenant-aware Component whose repository query contains server-authorized orgId. A foreign numeric resource, allocation or Department ID returns 404 (when concealment applies) without disclosing whether it exists. Public `/api/v2/orgs/{orgId}` additionally validates matching server membership/role, no implicit cross-tenant superuser.

**Required additional tests (UNEXECUTED):**
- `TENANT41-01`: org A and org B create identical normalized Department and Resource name keys—both accepted; same org duplicate rejected by DB UQ.
- `TENANT41-02`: actor A lists all five entity families, filters Resource by type, or lists Allocations by status while B has data—only A returned, including derived holds/movements.
- `TENANT41-03`: actor A supplies B numeric IDs to all 26 protected original routes—no B data read or mutated; 404/403 as source-safe, server org context not forgable.
- `TENANT41-04`: A ResourceRequirement with A Project + B Resource rejected directly by DB composite FK; B Allocation and A Resource mismatch similarly rejected.
- `TENANT41-05`: static repository call audit forbids `findAll`, unqualified `findById`, and `lockById` in user-request Component paths; authorization rechecked after org switch and before idempotency receipt lookup.

The broad unit-of-work org lock is a performance tradeoff but never substitutes for tenant-aware **SELECT/JPQL predicates** and restrictive FK constraints.

## 7. Genuine connected-user-workflow gaps closed without new entities

**INDEPENDENT DESIGN CRITIQUE:** CP35 allowed an assigned LEAD to `POST /allocations` using required reusable `borrowerMembershipId`, but its only prior membership list `GET /members` was OWNER/ADMIN only. An assigned LEAD's connected UI therefore had **no authorized member-ID discovery path**. Fix in [CP35](62_CP35_API_AUTH_UI_WORKFLOW_FREEZE_CANDIDATE.md): minimal `GET /api/v2/orgs/{orgId}/borrowers` returns only ACTIVE same-org `{membershipId,displayName}` with bounded cursor/search. OWNER/ADMIN/CUSTODIAN may query within their authorized org. LEAD must supply an assigned same-org `projectId` and cannot use the endpoint to enumerate other projects' participants; membership itself is still fictional/unverified. This is an extra **public v2** route and not falsely counted as a 27th original academic operation. A MEMBER does not get the directory. No personal phone, password, email or recovery metadata is sent.

**ACCOUNT-RECOVERY GAP:** CP35 had one-time offline recovery code redemption and code rotation but did not specify initial issuance when a new account is created. Corrected: new-account invitation redemption creates high-entropy recovery code hashes and displays plaintext codes **once** in that user's creation response/UI, not in logs, URL, storage or a later GET. Offline first OWNER bootstrap also needs a similarly controlled one-time recovery-code handoff. Existing-account invite acceptance does not issue new codes. Later authenticated `/account/recovery-codes/rotate` invalidates old codes after current-password confirmation. A UI acknowledgement cannot prove the user truly saved the codes. If all are lost, no username-only unauthenticated bypass is added; manual owner-controlled recovery remains a separate security decision.

**New unexecuted tests:**
- `UI41-01`: LEAD assigned Project A obtains only two fields via `/borrowers?projectId=A`, then creates a reusable Allocation with an authorized ACTIVE borrower membership; no access to ADMIN `GET /members`.
- `UI41-02`: LEAD tries `projectId=B` not assigned, MEMBER calls `/borrowers`, foreign organization member is supplied to A01, or injected borrowerNameSnapshot is submitted; all safely rejected.
- `AUTH41-02`: brand-new fictional account gets recovery codes once at accepted invitation; replay of invite cannot reveal or regenerate them; account recovery invalidates prior sessions.
- `AUTH41-03`: existing-account join does not regenerate recovery codes; offline bootstrap hands first OWNER codes through controlled local-only channel with no secret in Git/CI logs.
  
## 8. Verdict, source and precedence

**This pass resolves two source/SQL naming errors and identifies additional DB-enforced integrity safeguards**. It does **not** establish that the 18-table model was implemented, that a supported dual Javax/Jakarta provider exists, that the current O14 intermittent query has a known cause, or that current Canvas/instructor and hosting conditions are met.

The only changed precedence is: CP41 corrects CP34 physical column aliases and adds these future acceptance guards; the underlying CP31–CP40 one-product decision and sealed S0–S10 authorization boundary remain unchanged. No user/instructor signature is inferred from publishing documentation.

### Sources
- Actual GitHub O14 five JPA entity sources at SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`.
- [Source-to-target 35-column JSON mapping](contracts/O14_TO_UNIFIED_FIVE_ENTITY_COLUMN_MAPPING_CANDIDATE.json).
- [MySQL 8.4 generated columns](https://dev.mysql.com/doc/refman/8.4/en/create-table-generated-columns.html).
- [MySQL 8.4 CREATE INDEX, unique NULL rules](https://dev.mysql.com/doc/refman/8.4/en/create-index.html).
- Assignment `09_ESP_PROJECT_ASSIGNMENT.pdf`, p. 1, original 26-handler source crosswalk [CSV](contracts/ACADEMIC_26_ROUTE_ACCEPTANCE_NOT_RUN.csv) and [JSON](contracts/ACADEMIC_26_SOURCE_ANNOTATION_CROSSWALK.json).

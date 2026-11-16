# CP34 — Canonical physical schema, data provenance, migrations and conservation blueprint

> **2026-11-16 provider/identity addendum:** [CP40](67_CP40_PROVIDER_TRIAL_SECURITY_AND_DEADLINE_REALITY_CHECK.md) clarifies current Twilio free-trial template and recipient restrictions, conditional $0 OCI/GitHub operation, the P0 anonymous bearer invitation model (no intended_user_id), and optional phone encryption/consent field design. These are recommendations, **not implemented encryption or verified provider acceptance**.


**Research/planning date:** 2026-11-16 (PHT). **State:** DESIGN RECOMMENDATION, pre-Codex candidate; NO SQL migration, app code, actual MySQL trial, source DB inspection, deployment, or private-data transfer was run. Applies as a specific correction to [CP31](58_CP31_PRE_CODEX_FINALIZATION_PACKET.md), [CP32](59_CP32_FINAL_CONFLICT_ADJUDICATION_AND_CONTRACT_FREEZE.md), and [CP33](60_CP33_ACCEPTANCE_TRACEABILITY_RISK_AND_REVIEW_GATE.md). Changes documentation **only**, under draft research PR #7.

## 0. Grounded source compatibility and design priority

**COURSE REQUIREMENT:** Original assignment asks for at least five *justified* entity types with runtime create/read and eligible deletion using GET/POST and Jersey/Autowired/JPA/Javax/MySQL; it does not cap tables at five or mandate production import of fictional skeleton data. **VERIFIED O14 SOURCE:** five JPA classes `Department`, `Project`, `Resource`, `ResourceRequirement`, `Allocation`, with physical tables `department`, `project`, `resource`, **`resource_requirement`**, `allocation`. The CP29/CP31 shorthand `requirement` should NOT silently replace the actual source table name. Public URL remains `/requirements`.

Original O14 uses Philippine-local `LocalDateTime`, numeric `Long` internal IDs, generated DDL (`ddl-auto=update`), no organization identity, no membership/stock ledger and raw optional phone in Allocation. **Critical provenance rule:** these five tables cannot truthfully be declared a losslessly transferable 18-table production database without an audited migration. Their meaning, CRUD and DTOs are retained as course regression contracts, not an invented verified user-history corpus.

**P0 DECISION RECOMMENDATION:** fresh **isolated** production-schema installation and fictional initial data. Maintain O14 source SHA `2dd336ed39cecd417a8bce3388b303e688e95dee` unmodified as a reference. Use actual modern schema migrations in the eventual app. Do **not** copy all original test rows automatically, import phone numbers, create fake account ownership, or synthesize original historical inventory movements. A legacy import is deferred to a separately approved, separately tested requirement if actual non-fictional preservation becomes necessary. No original O14 database is deleted/reset.

## 1. Physical table ledger: 18 tables, candidate types and ownership

These names/types are a **draft DDL contract**, not executed `CREATE TABLE` or a guarantee that a chosen ORM maps them. All transactional tables InnoDB. Id `BIGINT` internal primary key; public `CHAR(36)` lowercase UUID with fixed binary-exact collation; `version BIGINT UNSIGNED NOT NULL DEFAULT 0` on mutable rows; instants stored as UTC `DATETIME(6)`; source academic API converts Philippine wall time explicitly. Check signed/unsigned FK types match exactly. Stored hash types `BINARY(32)` for SHA-256/HMAC outputs. Avoid nondeterministic CHECK expressions and cross-table CHECK claims.

| Table | Canonical mandatory/optional columns beyond id; critical keys | Purpose and delete rule |
|---|---|---|
| `organization` | public_id UQ, slug UQ, display_name VARCHAR(100), status ACTIVE/FROZEN, version, created_at, updated_at | Isolated fictional tenant. No public self-creation or hard-delete in P0 |
| `user_account` | public_id UQ, username VARCHAR(100), username_key UQ, display_name VARCHAR(100), status ACTIVE/DISABLED, credential_version BIGINT, created_at, updated_at | Global principal; never assume verified university email/affiliation |
| `local_credential` | user_id PK/FK, password_hash VARCHAR(255), changed_at | Adaptive password storage; no plaintext; controlled revoke |
| `account_recovery_code` | user_id FK, code_hash BINARY(32) UQ, created_at, expires_at, used_at?, revoked_at? | One-use high-entropy offline recovery; cleanup policy |
| `membership` | public_id UQ, org_id, user_id, role OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER, status ACTIVE/SUSPENDED/LEFT, version, created_at, updated_at; UQ(org_id,user_id), UQ(org_id,id) | One role per org; never delete a referenced account/member history |
| `invitation` | public_id UQ, org_id, invited_by_membership_id, redeemed_by_user_id?, role, token_hash BINARY(32) UQ, status INVITED/ACCEPTED/REVOKED/EXPIRED, expires_at, created_at, accepted_at?, version | P0 invitation is a one-time 256-bit bearer token with <=24h expiry; **no** intended_user_id or verified personal identity |
| `ownership_transfer` | public_id UQ, org_id, from_membership_id, to_membership_id, status PENDING/ACCEPTED/REVOKED/EXPIRED, expires_at, accepted_at?, version | Recipient-accepted owner transfer under org-root lock; at most one PENDING/org |
| `department` | public_id UQ, org_id, name VARCHAR(100), name_key VARCHAR(100), version, created_at; UQ(org_id,name_key), UQ(org_id,id) | Actual original Department class; physically delete only if no Project |
| `project` | public_id UQ, org_id, department_id, lead_membership_id?, name VARCHAR(100), start_at DATETIME(6), end_at DATETIME(6), version, created_at; UQ(org_id,id) | Actual original Project; **NO** persisted project workflow status; computed UPCOMING/IN_PROGRESS/ENDED |
| `resource` | public_id UQ, org_id, name, name_key, kind REUSABLE/CONSUMABLE, unit_label VARCHAR(24), total_quantity INT, status ACTIVE/ARCHIVED, version, created_at; UQ(org_id,name_key), UQ(org_id,id) | Actual original Resource; current T, no stored available |
| **`resource_requirement`** | public_id UQ, org_id, project_id, resource_id, quantity INT, version, created_at; UQ(org_id,project_id,resource_id), UQ(org_id,id,resource_id) | Actual `ResourceRequirement`; project + resource in **same tenant** |
| `allocation` | public_id UQ, org_id, requirement_id, resource_id, borrower_name_snapshot VARCHAR(100) NOT NULL, borrower_membership_id?, requester_membership_id, quantity INT, status, approved_at?, issued_at?, returned_at?, consumed_at?, cancelled_at?, cancellation_reason?, created_at, version; phone_ciphertext VARBINARY(256)?,phone_nonce BINARY(12)?,phone_key_id VARCHAR(40)?,sms_consent BOOLEAN,sms_consent_at DATETIME(6)?,sms_consent_source VARCHAR(32)?,reminder_claimed_at?, reminder_outcome, provider_http_status?, provider_sid?; UQ(org_id,id,resource_id) | Original allocation lifecycle, adds secure actor/tenant metadata; no exposed plaintext contacts; public v2 derives stable snapshot server-side from member/actor fictional display name, legacy stores supplied borrowerName |
| `stock_movement` | public_id UQ, org_id, resource_id, allocation_id?, actor_membership_id, operation_public_id CHAR(36), request_fingerprint BINARY(32), kind INITIAL/RECEIPT/ADJUST/ISSUE/RETURN/CONSUME, quantity INT, delta_total BIGINT, delta_issued BIGINT, occurred_at; UQ(org_id,operation_public_id) | Immutable physical ledger; never cascade/delete as ordinary CRUD |
| `audit_event` | org_id, actor_membership_id?, actor_kind, action, target_type, target_public_id, request_id, redacted_fields_json?, created_at; index(org_id,created_at,id) | Append-only security/history tombstone; no FK to physically deletable target |
| `idempotency_key` | org_id, actor_user_id, route_fingerprint, key_hash BINARY(32), body_hash BINARY(32), state CLAIMED/COMPLETED/FAILED, http_status?, response_json?, created_at, expires_at; UQ(org_id,actor_user_id,route_fingerprint,key_hash) | 24h **domain** HTTP replay receipt, NOT permanent stock dedup source |
| `resource_image` | public_id UQ, org_id, resource_id, content_type, sha256 BINARY(32), width_px, height_px, bytes MEDIUMBLOB, byte_count, version, created_at; UQ(org_id,resource_id) | Optional one secure <=2MiB resource image; explicit replacement/delete, no external fetching |
| `SPRING_SESSION` | **Library-defined**, not hand-coded; exact selected Spring Session JDBC schema | Revocable HTTP session |
| `SPRING_SESSION_ATTRIBUTES` | **Library-defined**, not hand-coded | No plaintext password, invite or recovery secrets |

**Physical total = 18 tables**, not “18 publicly deletable entities.” Actual final entity/table count is conditional on the chosen maintained runtime/session implementation and real schema migration. Keep actual original five course JPA entity classes, with five protected controller families.

## 2. Explicit constraints and composite FK specification

**Database constraints, must eventually be expressed as named migrations with matching types and index order:**

- `project(org_id,department_id) → department(org_id,id)`. Optional `project(org_id,lead_membership_id) → membership(org_id,id)`.
- `resource_requirement(org_id,project_id) → project(org_id,id)` and `resource_requirement(org_id,resource_id) → resource(org_id,id)`.
- `allocation(org_id,requirement_id,resource_id) → resource_requirement(org_id,id,resource_id)`; `allocation(org_id,requester_membership_id) → membership(org_id,id)`, optional borrower analogous.
- `stock_movement(org_id,resource_id) → resource(org_id,id)`; when allocation_id non-NULL also `(org_id,allocation_id,resource_id) → allocation(org_id,id,resource_id)`. ISSUE/RETURN/CONSUME require allocation_id. For INITIAL/RECEIPT/ADJUST it must be NULL.
- `resource_image(org_id,resource_id) → resource(org_id,id)`; organization-scoped invitation/transfer references `membership(org_id,id)`.
- `UQ(org_id,id)` (and requirement triple) must exist **on parent**, not merely an arbitrary nonunique index. Child indexes begin with the same FK columns/order. Avoid MySQL `MATCH` and `ON DELETE CASCADE` on history; use `RESTRICT`.
- CHECK `0 <= resource.total_quantity <= 1e6`, `1 <= requirement/allocation.quantity <= 1e6`, `project.end_at>start_at`, `delta_total/delta_issued` coherent with movement kind, `kind`/status whitelist. Service checks type-dependent and multirow invariants. A SQL CHECK cannot aggregate other table rows, enforce effective last OWNER, or act as a transaction lock.
- Names: Unicode NFC + trim + `Locale.ROOT` casefold candidate; store `name_key`, binary-exact unique. Test Turkish I, accented input and normalization; don't advertise full Unicode confusable-name security. A database unique index is the final arbiter of same-key races.

**Official MySQL 8.4 source:** https://dev.mysql.com/doc/refman/8.4/en/create-table-foreign-keys.html and https://dev.mysql.com/doc/refman/8.4/en/create-table-check-constraints.html (checked Nov 16, 2026). MySQL does NOT defer FK checks, `MATCH` directives can discard `ON DELETE` behavior, and CHECK is row-local; do not rely on theoretical SQL-standard features. These are documented vendor properties, **not an actual installed schema test**.

## 3. Physical ledger arithmetic, ordering and no-ghost-history guarantees

Source-of-truth for **current state** is `resource.total_quantity` and open allocation statuses; immutable movements reconcile changes across history. Same transaction persists row state and movement. `available` is a query result only. Let T = total_quantity, H = sum APPROVED q where project.endAt > transaction `now`; I = sum ISSUED reusable q. Then `available(REUSABLE)=T-H-I`; `available(CONSUMABLE)=T-H`. `covered(requirement)` counts effective APPROVED plus ISSUED/RETURNED/CONSUMED. Use one injected clock sample and long exact sums.

For clean records:
- `CreateResource(T=0)`: **no INITIAL movement**, eligible for physical deletion when unused/unreferenced.
- `CreateResource(T>0)`: single INITIAL movement `delta_total=+T,delta_issued=0` in create transaction with stable operationId.
- `Receive(+q)`: T + q and RECEIPT `delta_total=+q`; require max <=1e6.
- `Adjust(newT)`: `delta_total=(newT-oldT)`; reject if no change, impossible negative, below effective H + I or beyond upper bound. Any change creates one ADJUST.
- `Issue reusable(q)`: H decreases from status change, I increases; ISSUE `delta_total=0, delta_issued=+q`.
- `Return reusable(q)`: I decreases, T unchanged, RETURN `delta_total=0,delta_issued=-q` even if project ended. Covered quantity remains q.
- `Consume consumable(q)`: H decreases, T decreases, CONSUME `delta_total=-q,delta_issued=0`.
- `Approve/Cancel/Expire`: reservation-only states; **no** stock movement. Scheduler cancellation reason EXPIRED; no auto-return.
- Rename-only `R03`: no movement. If original R03 changes absolute total, produce ADJUST only if T changed and preserve original `expectedTotalQuantity` concurrency check.

**Strict replay:** stable public `operationId` is unique per organization with fingerprint of actor, action, resource/allocation, quantity and purpose. Exactly one committed movement per operation. Same ID/same fingerprint returns the committed result only after current auth/permission check; same ID/different fingerprint gives 409. Distinguish transient HTTP `Idempotency-Key` (24h receipt) from permanent movement uniqueness. On ambiguous connection failure, browser must query the operation receipt; never generate a fresh operationId automatically. If any movement/row write fails, transaction is rolled back; no partial inventory credit/deduction. A genuinely new action needs new operationId.

**Deletion reconciliation:** never-used T0 Resource with no Requirements, movement, or ResourceImage may be *physically* deleted by original R04. A previously moved Resource cannot be physically erased, even if T=0; use an explicit public archive path with no active H/I and zero physical total, retaining audit/ledger. This changes one O14 deletion edge; document the intentional safety extension and seek course equivalence decision rather than claiming exact historical drop-in. Provide image-delete if an image blocks an otherwise eligible delete; archived resource remains available in audit/export but not requestable.

**Concurrency:** conservative initial lock plan: tenant organization row → Project row (when any) → Resource row → Requirement row → Allocation row → operation/movement and receipt rows, in deterministic ascending IDs. Once locks acquired, re-evaluate current membership, project time/status, demand, H/I/T and If-Match. No HTTP calls while locks held. This org-root serialization intentionally favors correctness over throughput for small P0; benchmark and reconsider if contention becomes unacceptable. Password reset locks account/credential only and never goes on to acquire org root; global account disable locks all org roots in ascending ID, then memberships/account. Commit must preserve >=1 effective ACTIVE OWNER per ACTIVE org or freeze according explicit recovery policy. Handle deadlock as whole-transaction failure; bounded retry only when safe stable operation key exists.

## 4. Migration and release baseline — explicit clean-install recommendation

**VERIFIED SOURCE:** O14 five-entity test schema does not have org_id, account identity, membership, stock ledger or encrypted contact; it stores raw optional phone numbers and PHT LocalDateTime. This does **not** prove a historic production customer dataset exists. **RECOMMENDED P0:**
1. Pin actual O14 source SHA and store **fictional** reference fixtures/expected response shapes separately; don't use the `orgresource_test` DB as a production source of truth.
2. Run new versioned migrations on a **new empty** database/schema, with non-production CI and demo environments separate. Deny production startup on unapplied/failed migration. Set validation/DDL startup to `validate` or none according exact version; prohibit `ddl-auto=update`.
3. Create exactly one fictional org and first OWNER by offline controlled bootstrap, using one-time secret delivery/recovery. No open web registration, no campus officer verification claim.
4. Populate synthetic demo only by test fixture/seed command under explicit `demo` flag, never in production by default. Source all timestamps from injected clock offsets rather than hardcoding already-ended dates.
5. Do not import legacy rows silently or translate borrower names into verified account identities. Do not send SMS from demo or CI. Legacy source remains archived and testable on its original controlled DB.
6. Verify empty-schema migrations, bootstrap re-run refusal, non-destructive restart, schema checksum/constraints and rollback/restore behavior. Dump clean data under a separate backup process; encrypt off-provider and test restore before P-ACCEPT.

**Future optionally authorized LEGACY-IMPORT gate (NOT P0):** If real O14 records must be carried over, inventory exactly which records/data are real, obtain owner and privacy authority, hash/backup source, transform PHT date strings to UTC with known timezone, map to an explicitly authorized fictional/real organization, reject rather than infer borrower membership, treat prior stock as **opening balance at cutover** with honest provenance marker (not invented historical movements), retain original IDs in migration map, reconcile open ISSUED custody, validate checksums, dry-run then formally authorize cutover with backout. Do not claim this import exists.

## 5. Concrete acceptance cards for future execution (not run)

| ID | Input | Expected result |
|---|---|---|
| SCHEMA-01 | Empty MySQL8.4 schema | 18 intended tables and precisely named FK/index/CHECK, SQL strict mode |
| SCHEMA-02 | Attempt resource_requirement reference org A Project + org B Resource via raw SQL | FK reject, no partial child row |
| SCHEMA-03 | Allocation resource does not match its Requirement resource in same org | Compound FK reject |
| SCHEMA-04 | Movement points to org A Allocation and org B Resource | Compound FK reject |
| SCHEMA-05 | Issue/return/consume movement without allocation_id | CHECK reject |
| SCHEMA-06 | Resource T0 create, no movement; then eligible hard delete | Real physical delete and stable audit tombstone |
| SCHEMA-07 | Initial T5 create, then eligible status operations | Exactly one +5 movement, T/H/I reconciled |
| SCHEMA-08 | q4 + q4 concurrent for T5 | Exactly one approve succeeds, H4 and available1 |
| SCHEMA-09 | EndAt boundary before cleanup | effective APPROVED H=0 and covered=0, ISSUE denied, scheduler makes CANCELLED(EXPIRED) once |
| SCHEMA-10 | Invalid/negative stock or wrong resource type and FK | Database/service rejection, never negative |
| SCHEMA-11 | 48h replay of RECEIVE stable operation after HTTP receipt expiry + restart | No second +q, fingerprint mismatch 409 |
| SCHEMA-12 | Two orgs same name, same org duplicate name_key | Two orgs succeed, same org fails |
| SCHEMA-13 | Recovery reset vs global disable vs transfer under concurrency | No orphaned ACTIVE tenant, no unauthorized session recovery |
| SCHEMA-14 | JPA typed mappings against actual database + migrations | No schema mismatch; direct invalid javax entity save evidence |
| SCHEMA-15 | Clean deployment/backup/restore; then distinct test DB | No leakage, old sessions revoked, ledger totals equal; measured result |
| SCHEMA-16 | Optional future attempted legacy import | Remains NOT_RUN unless explicitly approved; no fake inferred event history |

No specific SQL migration, UQ, JPA mapping or test is represented as already written. The plan is precise enough to implement after evidence-approved runtime/version choice.

## 6. Verdict and issues that cannot be paper-certified

**Paper decision recommended:** keep the real five physical table names including `resource_requirement`, one production schema/ledger, clean fictional initial install, 18-table candidate and explicit protected history, not speculative legacy-data reconstruction. **Full architecture/runtime remains NOT PROVEN**. The selected Flyway/Liquibase version, Spring Session table DDL, Hibernate JPA + literal javax lifecycle, MySQL server version/collation, and free host quotas require actual compatibility and account checks. A read-only schema design cannot certify any of them.

**STOP for source parity:** if new protected R04 cannot delete a previously used now-zero Resource while legacy could, explicitly classify as a safety-driven compatibility exception; don't quietly call all O14 edge cases identical. Preserve 25 original+ A09 real routes and record the course/instructor clarification. The final current Canvas may modify course scope; do not invent that decision.

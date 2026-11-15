# CP29 — Proposed MySQL integrity, identity lock, history and disaster-recovery contract

**2026-11-16. SOURCE-GROUNDED DESIGN RECOMMENDATION; NO DDL MIGRATION OR SECURITY/RUNTIME TEST EXECUTED.** Decisions OD04–OD10/OD19–OD22 in [51](51_CP29_OWNER_DECISIONS_AND_CONTRACT_FREEZE_GATE.md) are UNSIGNED. Exact public API candidate in [52](52_CP29_PUBLIC_ROUTE_DTO_ROLE_ERROR_CONTRACT_CANDIDATE.md). Academic O14 five-entity schema and source commit remain separate/unmodified.

## 1. Public physical schema candidate (17 tables conditional)
Internal primary IDs `BIGINT` and public `CHAR(36)` UUID unique are suggested for first MySQL8.4/ORM proof, not a version pin. For tenant-scoped parents, `UNIQUE(org_id,id)` permits composite scoped FKs; all mandatory child FK components NOT NULL. New public instants use UTC `DATETIME(6)`; MySQL session and JVM UTC for persistence. All mutable data have `version BIGINT NOT NULL DEFAULT 0`. Use signed BIGINT for deltas with checked math, quantity 0..1,000,000 initial and positive action quantity <=1,000,000. No floating stock or silent overflow. Explicit migration tool, no Hibernate `ddl-auto=update` in production.

| Table | Selected candidate columns, uniqueness / indexes | FK and rules |
|---|---|---|
| `organization` | id PK, public_id UQ, slug UQ, name, status ACTIVE/CLOSING/CLOSED, version, created_at,updated_at | Tenant root, close serialized; actual university affiliation unverified/omitted |
| `user_account` | id PK, public_id UQ, auth_provider LOCAL, auth_subject UQ by provider, status ACTIVE/DISABLED, credential_version BIGINT, created_at,updated_at | Global user across orgs, no claim of verified campus identity |
| `local_credential` | user_id PK, password_hash VARCHAR(255), changed_at | user FK, adaptive scheme; no plaintext |
| `account_recovery_code` | id PK, user_id, code_hash BINARY(32) UQ, batch_id, created_at, used_at?, revoked_at? | one-use >=128-bit random material, hash only; no email required |
| `membership` | id PK, public_id UQ, org_id,user_id,role OWNER/ADMIN/CUSTODIAN/LEAD/MEMBER,status ACTIVE/SUSPENDED/LEFT,version,created_at,updated_at, UNIQUE(org_id,user_id), UNIQUE(org_id,id) | org FK, user FK; effective owner also requires active account and org |
| `invitation` | id PK,public_id UQ,org_id,invited_by_membership_id, intended_user_id?,accepted_by_user_id?,role,status INVITED/ACCEPTED/REVOKED/EXPIRED,token_hash BINARY(32) UQ,expires_at,created_at,accepted_at?,version | same-org inviter composite FK, one-use; role cannot OWNER; bearer invite not identity proof |
| `ownership_transfer` | id PK,public_id UQ,org_id,from_membership_id,to_membership_id,status PENDING/ACCEPTED/REVOKED/EXPIRED,expires_at,accepted_at?,version | both membership composite same-org FKs; `CHECK(from<>to)`; at most one pending/org under org root lock and tested unique slot |
| `department` | id PK,public_id UQ,org_id,name,name_key,version, created_at, UNIQUE(org_id,name_key),UNIQUE(org_id,id) | must stay distinct from organization; projects same-org |
| `project` | id PK,public_id UQ,org_id,department_id,lead_membership_id,name,start_at,end_at,status DRAFT/ACTIVE/CANCELLED/CLOSED,version,created_at, UNIQUE(org_id,id) | composite same-org Department and lead Membership; end_at>start_at |
| `resource` | id PK,public_id UQ,org_id,name,name_key,kind REUSABLE/CONSUMABLE,unit_label,total_quantity,status ACTIVE/ARCHIVED,version,created_at,UNIQUE(org_id,name_key),UNIQUE(org_id,id) | name_key reserved even on archive in P0; kind/unit immutable after initial movement |
| `requirement` | id PK,public_id UQ,org_id,project_id,resource_id,quantity,version,created_at, UNIQUE(org_id,project_id,resource_id),UNIQUE(org_id,id,resource_id) | composite same-org project/resource; positive quantity |
| `allocation` | id PK,public_id UQ,org_id,requirement_id,resource_id,borrower_membership_id?,requester_membership_id,quantity,status PENDING/APPROVED/ISSUED/RETURNED/CONSUMED/CANCELLED, cancellation_reason?, approved_at?,issued_at?,returned_at?,consumed_at?,cancelled_at?,created_at,version, UNIQUE(org_id,id,resource_id) | FK `(org_id,requirement_id,resource_id)` to matching Requirement triple; requester and borrower same-org memberships; borrowed active at request time; no PARTIALLY_RETURNED |
| `stock_movement` | id PK,public_id UQ,org_id,resource_id,allocation_id?,operation_public_id UUID,kind INITIAL/RECEIPT/ADJUST/ISSUE/RETURN/CONSUME,quantity,delta_total,delta_issued,actor_membership_id?,occurred_at,request_fingerprint, UNIQUE(org_id,operation_public_id) | composite FK to same-org Resource and `(org_id,allocation_id,resource_id)` to Allocation; ISSUE/RETURN/CONSUME **require** allocation_id NOT NULL; INITIAL/RECEIPT/ADJUST allocation_id NULL; immutable after commit |
| `audit_event` | id PK,org_id,actor_membership_id?,action,target_type,target_public_id,request_id,redacted_fields_json?,created_at, INDEX(org_id,created_at,id) | append-only retained as audit tombstone even for safely deletable never-used record; avoid FK to physically removable targets |
| `idempotency_key` | id PK,org_id,actor_user_id,route_fingerprint,key_hash,body_hash,state CLAIMED/COMPLETED/FAILED,response_code,response_json?,created_at,expires_at, UNIQUE(org_id,actor_user_id,route_fingerprint,key_hash) | HTTP receipt 24h candidate distinct from immutable operation uniqueness; replay post-purge still movement-safe |
| `SPRING_SESSION` | **Use actual version-matched official Spring Session JDBC/MySQL schema** | library-managed session; never hand-invent storage or unsafe serializer |
| `SPRING_SESSION_ATTRIBUTES` | **Use actual version-matched official Spring Session JDBC/MySQL schema** | attributes must contain no raw passwords, recovery codes or token secrets |

**All names and columns above remain proposed.** Missing join/index migration detail is a full-build freeze blocker, not a reason to pretend SQL code exists.

## 2. FK/constraint examples, requirements for future actual DDL
- `project (org_id,department_id) REFERENCES department (org_id,id)`; `project(org_id,lead_membership_id) REFERENCES membership(org_id,id)`.
- `requirement (org_id,project_id) REFERENCES project(org_id,id)`; `requirement(org_id,resource_id) REFERENCES resource(org_id,id)`.
- `allocation (org_id,requirement_id,resource_id) REFERENCES requirement(org_id,id,resource_id)` with parent `UNIQUE(org_id,id,resource_id)`.
- `stock_movement (org_id,allocation_id,resource_id) REFERENCES allocation(org_id,id,resource_id)`, plus resource composite FK; for movement kinds with required allocation ensure `CHECK` also enforces allocation_id NOT NULL. A NULL optional composite FK otherwise skips lookup.
- `CHECK (quantity > 0)` for ordinary receipt/adjust/issue/return/consume movements; `INITIAL` alone may use quantity=0 so Resource created with zero stock still has a durable initial-operation record; `delta_total=0` for zero initial. Kind-specific sign/issued delta CHECKs must be named and tested.
- Application-level stock equalities **cannot be enforced** by a MySQL CHECK spanning multiple tables: `T = SUM(delta_total)`, `I=SUM(delta_issued)`, reusable `T>=H+I>=0`, consumable `T>=H>=0`. Enforce via transaction locks + reconciliation tests.
- Prohibit unreviewed `ON DELETE CASCADE` for history-bearing rows; use RESTRICT and controlled cleanup for expired session/recovery/receipt records. `MATCH` must not be used in MySQL FKs.
- Case-insensitive names: candidate trim, normalize Unicode consistently, `Locale.ROOT` lowercase key and DB binary-exact `name_key` unique comparison; test mixed case, diacritics, Unicode normalization and archival/recreation; record exact normalization choice before SQL freeze.
- Physical UUID `CHAR(36)` versus `BINARY(16)` and UUID collation/indices remain owner/implementation review, not verified DB performance.

## 3. Deletion and history preservation rule (proposed)
**Public P0 differs from the original academic DELETE behavior** by design; academic still independently runs its 26 routes.

| Target | Allowed physical delete only when... | Otherwise |
|---|---|---|
| Department | no Project or other mandatory FK record; keep non-FK audit tombstone | 409 dependency, no partial change |
| Project | no Requirements, allocations, durable movements or audit mandatory dependency | 409; separate CANCEL/CLOSE state, no accidental cascade |
| Resource | **never** after initial movement, including zero-stock INITIAL; it has an append-only physical history by definition | use `ARCHIVED` if no live holds/custody; preserve ledger/exports |
| Requirement | no Allocation exists/history and no mandatory dependency; audit tombstone independent of FK | 409 otherwise |
| Allocation | `approved_at IS NULL`, `status IN (PENDING,CANCELLED)`, no `stock_movement`, no other mandatory child reference; no audit FK preventing delete | 409 HISTORY_PROTECTED; audit tombstone preserved after eligible deletion |

Do **not** interpret an audit event with target_public_id as a live FK or reconstruct a deleted object from audit alone; preserve enough non-sensitive description to explain the deletion without retaining prohibited personal data. User must approve which public deletion capabilities remain hard deletes versus archive/tombstone. Returned/consumed/approved-then-cancelled records are never eligible physical deletes.

## 4. Effective holds and requirement coverage — one predicate
Given `now` at one injected UTC clock sample in a transaction:

`effectiveApproved(a,now) = (a.status=APPROVED AND project.endAt>now)`.

Then:
`H(resource,now) = SUM(a.quantity FOR effectiveApproved(a,now) with matching resource)`.
`I(resource) = SUM(ISSUED reusable allocations outstanding)` (whole batch P0).
`completed(a) = a.status IN (ISSUED, RETURNED, CONSUMED)`.
`covered(requirement,now)=SUM(quantity WHERE completed(a) OR effectiveApproved(a,now))`.
`shortage=max(0,requiredQuantity-covered)`.
`reusable available=T-H-I`; `consumable available=T-H`.

At `project.endAt` exactly:
1. Approved persisted rows may still say APPROVED until scheduler; API emits `expiredPendingCleanup=true`, `isEffectiveHold=false`.
2. Availability and coverage BOTH exclude the expired approved amount immediately.
3. POST issue/consume must fail 409 PROJECT_ENDED even before cleanup.
4. Scheduler transitions eligible PENDING/APPROVED → CANCELLED with `cancellationReason=EXPIRED` exactly once. It does **not** create physical stock movement because no physical custody changed. Old issued records remain issued.
5. Historic RETURNED/CONSUMED allocation still contributes coverage. No false restoration of covered quantity from a physical RETURN.
6. Simultaneous expiry/approve/issue/return must serialize and recheck under lock. Do not subtract H twice: derived from statuses/effective predicate, not writable counter unless proven to reconcile.

**Original proposal discrepancy:** its prose says approval hold persists until cancellation/return/consume, while actual O14 JPQL already excludes ended approved project. This public P0 policy is a proposed owner decision OD04, not authorization to rewrite academic O14.

## 5. Transaction and deadlock risk
**Proposed key locks for public writes:** `organization(id)` → resource IDs ascending → relevant project/requirement/allocation rows ordered deterministically → operation/ledger/receipt. Identity membership/role/owner updates: organization IDs ascending → membership IDs ascending → affected account rows → invitation/transfer. Re-read account+membership+org ACTIVE **inside** write transaction. No provider call while locks held. Critical transactions use real InnoDB row locks; never calculate reservation availability against a stale pre-lock repeatable-read snapshot.

**CP28-MC75 correction, proposed acyclic lock discipline:**
- Credential reset/recovery locks **only account+credential+recovery rows**, no subsequent organization lock inside that transaction; it increments credential_version, revokes sessions, does NOT reactivate a DISABLED account.
- Global account disable first enumerates impacted orgs, locks **all org roots ascending**, then membership/account rows and re-reads effective last-owner counts under lock. If enumeration can become stale, check membership changes under same org root and/or re-query before commit; any ambiguity aborts.
- Organization membership/transfer transactions **never** lock an account row before acquiring org locks; hence a reset holding account may make disable wait, but cannot itself wait for an org row already held by disable.
- Login checks ACTIVE current account and rechecks status before issuing a session; a subsequently disabled account must be denied by per-request server-side status checks. Password reset does not update memberships.
- If transaction deadlocks or times out, abort/rollback **whole** write; retry only bounded operations protected by stable business `operationId`, and never return success before commit.
- This is a deadlock-prevention candidate, not a formal proof or runtime result. Test password-reset+role-transfer+multi-org-disable races on real selected MySQL.

## 6. Identity and recovery after actual disaster
A database backup is not sufficient to restore **security state** at the most recent instant. A restored earlier snapshot could reactivate a disabled account, old password, already-used invite, recovery code or session. **OD20 recommended fail-closed procedure:**
1. Freeze public ingress and stop the old primary/split-brain writes. Verify backup integrity and a compatible clean target.
2. Restore authoritative Organization/Department/Project/Resource/Requirement/Allocation/Membership/Ledger/Audit and durable operation uniqueness, then reconcile counts and hashes. Never blindly overwrite current data from a stale backup without explicit recovery decision.
3. **Do not resume public authentication using restored old sessions or tokens.** Purge/invalidate all Spring Session rows on recovered host, revoke pending invitations, and revoke saved account recovery codes; rotate CSRF/session/signing material.
4. A restored old password/credential_version may be less secure than the account state just before disaster. Keep recovered tenant **FROZEN** until a separately controlled fictional identity re-provision/reset procedure has been verified for each designated owner. No public anonymous first-owner claim or assumed real campus identity verification.
5. Provide new protected temporary credentials/recovery factors to known fictional test principals using an approved offline procedure; rotate before returning to service. If safe identity recovery cannot be completed, NO-GO to public traffic.
6. Restore key custody from independently controlled secrets and prove a complete audit/stock read-back, no credential/session replay, no late duplicate RECEIVE, and repeatable future encrypted backups.
7. Measure RPO/RTO against the *actual restore* and maintain backup/secret copies across independent failure domains; no hard-coded recovery promise.

This is stringent and may make a no-cost production target infeasible; that is an explicit tradeoff, not permission to silently restore stale sessions. Real-user identity recovery would require a separately authorized procedure.

## 7. Future test matrix — ALL NOT RUN
| Test | Setup / acceptance |
|---|---|
| DB29-01 | In 2 orgs, same Resource name allowed; same-org casefold collision 409; fake foreign Department project FK rejected raw SQL |
| DB29-02 | StockMovement references valid Allocation A but Resource B in same org => composite FK rejects, no partial insert |
| DB29-03 | Stock ISSUE with allocation_id NULL => named CHECK rejects; INITIAL with quantity0 allowed only kind INITIAL |
| DB29-04 | T5, concurrent approve q4+q4 => exactly one committed, available1, no double ledger |
| DB29-05 | REUSABLE T5 q4 issue/return entire => T5, H4→0, I0→4→0; physical issue/return events exactly once |
| DB29-06 | CONSUMABLE T10 q4 approval/consume => T6,H0, available6, duplicate denied |
| DB29-07 | RECEIVE +3 with same durable ID after 48h, HTTP receipt purged and restart => only one +3; new payload ID reused =>409 |
| DB29-08 | q4 APPROVED ends at injected timestamp, before scheduler: exclude H & coverage; issue denied; after scheduler CANCELLED/EXPIRED no extra stock |
| DB29-09 | Whole batch return after end allowed only from ISSUED; never auto-return. Partial/damaged return rejected |
| DB29-10 | OWNER transfer and global disable across two orgs concurrent with recovery reset => never zero effective owners in ACTIVE org; no deadlock success/half commit |
| DB29-11 | Attempt delete historically approved CANCELLED Allocation or Resource with INITIAL ledger =>409; export/audit remains coherent |
| DB29-12 | Unauthorized read, export, operation receipt, member update, and raw tenant FK injection => proper 401/403/404/FK denial |
| DR29-01 | Restore stale backup in isolated target; all old sessions/invites/codes unusable; old credentials not accepted until controlled reset, owner regains safe access |
| DR29-02 | Clean restore keeps stock ledger totals, durable operation IDs, audit chronology and new encrypted backup ability; RPO/RTO actually recorded |
| SEC29-01 | Through actual Jersey browser HTTP missing CSRF→403; wrong-role→403; foreign ID→404; disabled account after concurrent login denied |
| REL29-01 | Exact academic original 26 GET/POST routes and separate javax DTO & entity persistence still execute; no public legacy API |

## 8. Remaining physical-schema freeze questions
UUID CHAR vs BINARY, exact charset/collation/name-key function, named FK/UK/CHECK definitions and migration tool, replay fingerprint canonicalization, treatment of audited eligible hard deletion, requested unit-label policy, archive rule for nonzero stock, one-role membership management, emergency offline credential provisioning, actual Spring Session JDBC schema/version, MySQL chosen isolation, public/academic error-status differences, and REST contract [52] must receive owner/engineering review.

**Final: public physical schema DRAFT; migration NOT WRITTEN; 17-table count CANDIDATE; acceptance NOT RUN.**

# OrgResource — CP27 canonical pre-coding handoff (DRAFT 1)

> **CP29 precision overlay (2026-11-16, PROPOSED/NOT APPROVED):** The exact public route/DTO/role/error proposal in [52](52_CP29_PUBLIC_ROUTE_DTO_ROLE_ERROR_CONTRACT_CANDIDATE.md), candidate MySQL/history/DR model in [53](53_CP29_MYSQL_IDENTITY_HISTORY_DR_CONTRACT_CANDIDATE.md), and owner decision sheet [51](51_CP29_OWNER_DECISIONS_AND_CONTRACT_FREEZE_GATE.md) clarify this earlier draft. They supersede conflicting *provisional* CP27 public sketches only. **No user sign-off, implemented public API, DDL or Codex authorization.**


**Date:** November 16, 2026 Asia/Manila.  
**STATUS:** RESEARCH CONSOLIDATED, NOT OWNER-APPROVED; no Codex implementation authorized.  
**Academic source SHA:** `2dd336ed39cecd417a8bce3388b303e688e95dee` (draft PR #8).  
**Research source before this upload:** `856a2fceabcbbf95b569d8965bd6b4a23d418c54` (draft PR #7).  
**Printed academic deadline:** December 5, 2026; current Canvas can change instructions.  

## 1. Authority and evidence
1. Current CSCI180.05 assignment/syllabus and verified current Canvas for course rules.
2. USER DECISIONS D-001–D-031: two active executable tracks, original academic course preservation, $0 mandatory spending, research-first signoff, O14 baseline→supported public engineering→real P-ACCEPT→separate A-FINAL→student-only Canvas submission.
3. Original 12-page proposal and pinned O14 *observed* source for academic behavior; A09 is an extra consent-revoke operation.
4. CP13–CP26 dated findings supersede inconsistent historical V2 proposals; unapproved options remain DESIGN RECOMMENDATION.
5. External vendor evidence is date-bound; provider descriptions never equal executed account or system acceptance.

Every claim must distinguish COURSE REQUIREMENT, SYLLABUS FACT, USER DECISION, VERIFIED SOURCE/EXTERNAL FACT, DESIGN RECOMMENDATION, ASSUMPTION, OPEN QUESTION, CI OBSERVED, LOCAL REPORTED, and NOT RUN.

## 2. Problem and beneficiaries
**Problem hypothesis, not measured Ateneo demand:** Small organization officers may lack a transactionally consistent record of project resource needs, approved stock, issued custody, usable returns and consumable use. Public P0 uses fictional private organization workspaces and synthetic seed data; no invented Ateneo endorsement, actual inventory, interviews, workflow, adoption, official organization or campus SSO.

Candidate users: OWNER governance, ADMIN member administration, CUSTODIAN stock/fulfillment, LEAD project/request planning, MEMBER permitted read/own custody, OPERATOR infrastructure without automatic tenant-data access. A real-org pilot and legal/institutional authorization are deferred.

**Alternatives:** spreadsheets (low friction, weak concurrency/audit), current inventory tools such as Snipe-IT (mature; hands-on fit unverified), original academic-only app (already runnable but unauthenticated/unsafe for public), supported custom modular monolith (best course-inspired exact domain potential but highest build/maintenance burden). No ungrounded claim that alternatives fail.

## 3. Academic immutable contract and proof
Original proposal defines five JPA entities: **Department, Project, Resource, ResourceRequirement, Allocation**. Original 25 operations plus separately documented O14 A09 consent-revocation yields **26** real Jersey handlers: D4/P4/R5/Q4/A9. Numeric academic IDs, GET reads, POST writes, JSON and Spring `@Autowired` controller→component forwarding.

**COURSE REQUIREMENT:** Jersey REST, JSON, Spring annotations, JPA/Spring Data, literal Javax Validation, Retrofit/Gson external service at least Twilio wording, scheduling, >=5 justified entity types, runtime create/read/delete and all-route Postman. Syllabus includes MySQL/Hibernate, JPQL, Eclipse and CLO9 image HTTP. Current CLO9 and Twilio acceptance unresolved. Real academic `javax.validation` DTO and **direct invalid entity JPA save/flush** must be demonstrated separately. O14 FakeTwilio loopback HTTP is not real Twilio provider; virtual mode disabled. Exact current Canvas/instructor rule must be checked, not invented.

Academic original JDK8/Boot2.1/Jersey2/javax/MySQL/Retrofit/Gson/schedulers remains independently executable, restricted from Internet exposure; old packages are source pins, not necessarily course-mandated numeric versions. No academic A-FINAL, current Canvas, original release ZIP custody, recorded final video, authorship receipt or instructor pass is claimed.

## 4. Public P0 candidate scope — UNAPPROVED
Include operator-provisioned fictional Organizations; distinct Departments; Projects; Resources; Requirements; Allocations; global quantity holds; whole usable reusable issue/return, whole consumable use; immutable stock movements; durable user-reused operation ID; local credential, saved recovery codes, one-use manual invitation, server session/current-role authorization; scheduler expiry and internal activity; connected responsive React UI; versioned MySQL schema; audited export, HTTPS and independent encrypted restore if and only if provider authorized/proven.

Defer time-window future reservations, PARTIALLY_RETURNED, damage/loss/serial numbers, barcode, predictive analytics, campus SSO, real email/SMS, campus system API integration, public anonymous mutable demo, payment, microservices/brokers and unneeded public file upload. Public application is distinct from academic 26-route execution.

## 5. Public entity/state dictionary (candidate, no DDL)
- Organization: `ACTIVE,CLOSING,CLOSED`; tenant root.
- UserAccount: `ACTIVE,DISABLED`; global auth subject and credential_version.
- Membership: role OWNER,ADMIN,CUSTODIAN,LEAD,MEMBER; status ACTIVE,SUSPENDED,LEFT; account AND membership must be active for effective OWNER.
- Invitation: `INVITED,ACCEPTED,EXPIRED,REVOKED`; hashed single-use token, scope+expiry; no fake verified email.
- OwnershipTransfer: `PENDING,ACCEPTED,REVOKED,EXPIRED`; recipient active admin acceptance.
- Department: name unique within org; separate from Organization.
- Project: org/department, positive bounded dates, permitted state; internal allocation approval is not institutional event approval.
- Resource: `REUSABLE,CONSUMABLE`; active/archive, whole nonnegative `T`, unit; no cross-tenant stock.
- Requirement: positive `quantity` for project+resource; one per pair.
- Allocation: `PENDING,APPROVED,ISSUED,RETURNED,CONSUMED,CANCELLED`; optional `cancellationReason=EXPIRED` instead of separate `EXPIRED` state (academic source-aligned candidate).
- StockMovement: append-only physical stock/custody delta, same resource as referenced allocation, durable `operation_public_id`.
- AuditEvent: bounded redacted actor/action/target/request/time.
- IdempotencyKey: short-lived HTTP response receipt, distinct from immutable physical operation uniqueness.
- LocalCredential, AccountRecoveryCode, Spring Session JDBC two tables are conditional identity implementation details.

These entities are an **at least 17 physical table** estimate (prior 11 public draft + Department + 3 local identity tables + 2 Spring Session tables), not schema already created. Do not equate this with course-required minimum five academic entities.

## 6. Quantity and state invariants
For each tenant Resource, T = total usable/remaining stock; H = effective APPROVED holds; I = outstanding physically ISSUED reusable qty. Reusable available = **T−H−I** and T>=H+I>=0. Consumable available = **T−H** and T>=H>=0. PENDING/CANCELLED hold zero. Returned and consumed still fulfill historic Requirement; shortage = max(0, requested − covered effective/fulfilled allocation quantities). Never silently clamp negative actual inventory.

Approve q: PENDING→APPROVED, H+=q only if capacity/remaining demand. Issue reusable q: APPROVED→ISSUED, H-=q, I+=q, T unchanged. Whole usable return: ISSUED→RETURNED, I-=q, T unchanged. Consume q: APPROVED→CONSUMED, T-=q and H-=q atomically. Cancel: PENDING or APPROVED→CANCELLED, release hold only once. Ended PENDING/APPROVED can be CANCELLED with reason EXPIRED by bounded scheduler; issued custody persists until actual return; no automatic return. No partial returns or damage treated as usable in P0.

**OPEN project end discrepancy:** Original 12pp proposal says global approved holds last until transition; O14 JPQL counts APPROVED only when project.endAt>now. Proposed public P0: effective holds cease at exclusive end, issue/consume at >=end refuse, scheduler persists cancellation afterward; owner/instructor review required, no silent academic code change.

Deterministic examples: REUSABLE T5 approve4→available1, issue4→T5/H0/I4/available1, return entire→T5/H0/I0/available5; CONSUMABLE T10 approve4→available6, consume4→T6/H0/available6.

## 7. MySQL data integrity contract — DDL NOT FROZEN
Candidate internal IDs BIGINT and public opaque UUID (physical `CHAR(36)` vs `BINARY(16)` OPEN), public time UTC `DATETIME(6)` (not academic PHT `LocalDateTime`), positive integer action quantities, `BIGINT` version. Require exact named UNIQUE and CHECK constraints, RESTRICT history FKs, tenant composites `(org_id,parent_id)` and resource-consistent `(org_id,allocation_id,resource_id)` StockMovement→Allocation FK; mandatory child org/parent keys NOT NULL. MySQL FK `MATCH` omitted. Single-row CHECK cannot enforce cross-row available stock or effective owner counts.

All dangerous writes lock org root then resource(s) in stable order then requirement/allocation/ledger/receipt; current membership/status and capacity rechecked inside transaction. Global account disable locks impacted orgs by ID and refuses orphaning any active org. Acquire resource lock BEFORE capacity query; choose/verify isolation against stale REPEATABLE READ snapshot. Deadlock and lock timeout rollback complete transaction. Root lock throughput cost deliberately accepted only after benchmarks.

Separate unique `(org_id,operation_public_id)` with caller-reused stable 128-bit physical operation ID and immutable request fingerprint. A 24h idempotency receipt alone cannot stop a second RECEIVE +q at 48h. Same business ID+same request readbacks original result after restart; changed payload →409; actual physical identity lost → operator reconciliation, never fictitious exactly-once guarantee.

No destructive migration from original academic DB. Explicit MySQL8.4 migration, indexes, FK negative raw SQL, 2040 date tests, empty DB, prior-version migration rehearsal, backup+restore and dependent row reconciliation required.

## 8. Candidate public HTTP/API contract — NOT IMPLEMENTED
Versioned public prefix `/api/v2`; authenticated org resource routes `/orgs/{orgId}/...`; opaque IDs, HTTPS, per-request current membership. HTTP: 200 read/transition, 201 create, 202 only actual async, 400 syntax, 401 unauthenticated, 403 forbidden, 404 invisible/foreign tenant, 409 capacity/state/duplicate/last owner/replay mismatch, 412 stale `If-Match`, 422 semantic, 429 throttle, 503 unavailable/quota, sanitized 500. Example error fields `code,message,requestId,details?`. Browser session via **Cookie** header, not Authorization; anti-CSRF header for mutations.

Minimum domain mapping against the preserved academic 26 (public equivalents are NOT bytewise URL/API parity):
| Family | Public operations proposed | Differences needing approval |
|---|---|---|
| D01–D04 | create/list/update/delete unreferenced Department | Tenant auth and public UUID |
| P01–P04 | create/list department filtered/update/delete unreferenced Project | Tenant auth, dates and role |
| R01–R05 | create/list/update(+audited stock movement)/archive/availability | R04 archive vs academic hard delete; R03 exact update rule |
| Q01–Q04 | create/list project filtered/update/delete unreferenced Requirement | Security, versioning |
| A01–A09 | create/list/approve/issue/return/consume/cancel/delete eligible | A09 academic SMS consent revocation has NO public equivalent (outbound SMS deferred); A08 durable history policy to decide |

No public capability should be advertised as completed until implemented and tested. The original **academic 26** remain required separately. Public A09 difference, R04 archive and deletion/history need explicit owner signoff. New identity routes include local login/logout/recover, session/current memberships, issue/redeem/revoke invitation, member list/change/suspend, two-party ownership transfer, tenant operation receipt; public org self-creation disabled in suggested P0.

All routes, list filters, exports/audit/activity/operation receipts and download links must enforce tenant boundaries, not just a front-end route guard.

## 9. Component and scheduling architecture
Candidate modular monolith: identity, organization, department, planning, inventory, allocation, audit, notification, ops; Jersey REST controller→injected Spring component→JPA repository; business transactions in services, no direct controller persistence. One MySQL source of truth and React assets, no extra broker/cache/service without measured need.

Expiry bounded scheduler scans eligible ended PENDING/APPROVED, locks/rechecks then CANCELLED/EXPIRED reason; never touches issued physical custody. Internal due-activity scheduler creates deduplicated in-app notices; no paid outbound SMS/email. Inject Clock; bounded batches, restart, two-worker races, stale-work and partial-failure tests. O14 academic Retrofit/Gson provider proof remains a separate course gate.

## 10. Security, privacy and fictional governance
Operator-only offline initial OWNER bootstrap; no first-visitor OWNER or unsupported institutional affiliation. Local adaptive password hashing; one-use >=128-bit codes shown once/hashed; invitation token expiry/revoke/single-use, manually transferred only to fictional identities, not verified email. Current account, membership and org states enforced at GET, POST, export, operation receipts. Transfer recommendation recipient ACTIVE ADMIN accepts, becomes OWNER while old OWNER→ADMIN atomically; lost/compromised sole owner with no valid recovery → freeze tenant, not unauthorized takeover. These exact choices require owner approval.

Same-origin HTTPS, Secure HttpOnly SameSite session cookies, CSRF including login/logout and mutations, session ID rotation, logout/recovery invalidation, rate-limit and generic auth errors, IDOR/BOLA 2-tenant tests, role escalation denial, CSP/XSS, no secrets/logged raw tokens, bounded requests, dependency scanning. No personal phone, actual student IDs, real inventory or campus SSO in fictional P0. Philippine data protection duties require independent review before any real persons/pilot; no invented Ateneo controller role or endorsement.

## 11. Supported stack / operations candidate, NOT PROVEN
Academic remains Java8/Boot2.1/Jersey2/javax, loopback/private. Public investigate Java17+/Java21 LTS, Spring Boot4.1.2, Jersey4.0.3 and actual supported JSON provider (do NOT assume Jackson2 only), native Jakarta Validation/JPA/Hibernate, Connector/J and MySQL8.4 InnoDB, Spring Security and Spring Session JDBC, React/TypeScript v5. Actual BOM/provider/security compatibility and ARM64 runtime are not tested. Native Jakarta does not satisfy literal original Javax; two active executables mandatory.

Investigate **authorized** no-mandatory-charge OCI Always Free A1 VM + controllable self-hosted MySQL8.4, reverse proxy IP certificate/renewal and firewall; OCI MySQL.Free cannot be assumed version pinned or restore-compatible. Actual free quota/card/overage, VM availability, HTTPS renewal, 3306 private access, backup independently off account, encrypted key custody, compatible clean restore and measured RPO/RTO are NOT RUN. No provider signup or paid fallback authorized in research.

## 12. Acceptance, release and sequencing
Evidence record per test: caseId, requirement/source, phase O14/COMPAT/P0/P-ACCEPT/A-FINAL, source SHA, JDK/Jersey/MySQL exact versions, isolated fixture/clock, command/HTTP, expected+actual HTTP and database postcondition, redacted logs and artifact digest, date/time, reviewer, pass/fail/waiver. A described test is NOT RUN.

- **O14 baseline:** exact JAR and source package reproducible; 55 Java/MySQL and 15 front tests PASS on pinned O14 CI, but intermittent earlier expected5/actual8 resource boundary issue root cause OPEN. Reported local 96 Newman requests/142 assertions and five Playwright journeys are **author-reported**, not CI-observed. Direct invalid Javax JPA save/flush; Twilio/CLO9 and Canvas/student requirements open.
- **COMPAT-01 local feasibility:** only after explicit owner grant, eight cases: Java/Boot/Jersey start, Jersey+Spring JSON injection, Jakarta DTO failure, JPA/MySQL rollback, real HTTP session 401/403/CSRF, conflicting DB writes, pinned dependency/SBOM/security logs, O14 unchanged. No full build/host.
- **Public P-ACCEPT:** real auth/recovery/tenant role, MySQL FK/replay/concurrency/ledger, exact API, browser E2E responsive/a11y, actual protected host HTTPS and renewal, controlled no-cost provider, encrypted off-account clean restore, maintainability/operator acceptance. **NOT RUN**.
- **A-FINAL after public PASS:** independently rerun original five entities/26 Jersey routes/Postman, Javax DTO and JPA entity, MySQL/JPQL, Retrofit/Gson properly labeled simulator versus actual provider, scheduled jobs, clean source/Eclipse extraction, current Canvas CLO9 and Twilio rule, video, certificate and outside/AI assistance. **NOT RUN**.
- **Canvas:** student-only manual submission and receipt, printed Dec5 unless verified change.

Backlog depends: B0 approved research/decisions → B1 isolated COMPAT-01 → B2 actual SQL/identity/tenancy → B3 stock/ledger/expiry → B4 public API/security → B5 real UI → B6 authorized hosting/DR → B7 P-ACCEPT → B8 A-FINAL → B9 student Canvas. B6 can research eligibility earlier but does not imply enrollment without authorization. No stage complete from source generation alone.

## 13. Material open decisions
OD01 exact academic/public validation and business-capability boundary; OD02 global holds; OD03 whole batch; OD04 expiry endAt; OD05 operator fictional org bootstrap; OD06 transfer recipient old OWNER role; OD07 compromised sole-owner freeze/recovery; OD08 local credentials/SPRING_SESSION; OD09 R04 archive and A09 public omission plus delete semantics; OD10 UUID physical type; OD11 supported effective version set; OD12 $0 host/DR; OD13 CLO9 image; OD14 Twilio provider; OD15 final O14 release artifact hashes; OD16 COMPAT-01 authorization only; OD17 full build authorization later. All not explicitly approved remain **OPEN**.

## 14. Risk/stop conditions
NO PUBLIC GO on BOLA/CSRF, bad effective owner, double stock, wrong-resource FK, stale snapshot, missing client operation identity, unpatched critical runtime CVE, paid-required enrollment, untrusted TLS, no off-host clean restore, or unverified operator. NO A-FINAL claim without actual original mandatory tech, complete Postman and student/instructor evidence. No course video frames observed from unattached recordings; no real Ateneo pilot. Historic 37–75 expert person-days and only 19 calendar days Nov16→Dec5 make schedule RED, not promise. Cut optional time booking, partial/damage, SMS, barcodes, analytics first, NEVER required auth/DB/DR/course acceptance to claim production PASS.

## 15. Source and supersession index
**Read this file and [49 gate register](49_CP27_APPROVAL_EVIDENCE_AND_CODEX_STAGES.md) first, then original assignment/syllabus/proposal, [30 route crosswalk](30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md), [40 conflict register](40_CP13_CANONICAL_SRS_EERD_API_CONFLICT_REGISTER.md), [41 acceptance mapping](41_CP14_REQUIREMENTS_TEST_EVIDENCE_TRACEABILITY.md), [42 intermittent CI](42_CP14_O14_JAVA_MYSQL_INTERMITTENT_CI_FAILURE.md), [43–47 updated audit](43_CP15_CP21_RESEARCH_CONTINUITY_AND_ARCHITECTURE_GATE.md). Historical `08/10/11/12/17` must not be used as active implementation specification in conflicting sections.

**Final status: CANDIDATE READY FOR OWNER REVIEW, NOT SIGNED, NO PUBLIC APPLICATION IMPLEMENTED.**
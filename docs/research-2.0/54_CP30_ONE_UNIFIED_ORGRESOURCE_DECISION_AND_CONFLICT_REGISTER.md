# CP30 — One unified OrgResource: dated user decision, supersession and conflict register

**Date:** 2026-11-16, Asia/Manila.  
**Authority:** User's latest direct instruction following discussion of a single app.  
**Status:** **USER-DECIDED END GOAL; ENGINEERING ARCHITECTURE AND FULL-BUILD AUTHORIZATION STILL OPEN.**  
**Change scope:** Research documents and proposed release contract only; no product source changes, commits to main, merges, paid services, hosted deploys, tests, SMS, institutional outreach, or Canvas submissions.

## 1. What the user actually directed

The user asked whether two versions were necessary, stated preference for just one, then directed: **"log everything discussed, found, realized, research and established to the repo. also, everything in the course is complied to and preserved but built to be production and working ready. like literally everything must be preserved but the whole thing must work completely and production ready end to end with free tools and free continuous deployment"**.

### D-098 — USER DECISION — final product count changed
Target **ONE user-facing OrgResource product**, one coherent workflow, one UI, one deployment/release line and one authoritative transactional database, rather than two independently maintained final products. Multiple packages/modules or temporarily separate proofs are permissible *only as engineering internals*, not a hidden permanent second public product; whether even a second runtime can be part of one product is **OPEN** and must be presented transparently to the user before selection.

### D-099 — USER DECISION — comprehensive functional and course preservation
Retain **every actual applicable course technical capability**, and the original 5-entity, 25-proposal-operation + O14 A09 route/domain behavior, as **working executable functionality**, not as screenshots, mock endpoints, inactive archived code, or an untested historical branch. Add the required security, tenant/isolation, integrity, recovery, UI, and operational controls without removing those abilities. Distinguish actual source amendments from original proposal.

### D-100 — USER DECISION — production readiness and zero mandatory spending
The intended *same product* must eventually pass real end-to-end API/DB/browser/scheduler/provider/course tests, production security, repeatable deployment and rollback, HTTPS, monitoring, durability and independent recovery. Implement with **no mandatory recurring payment or paywall** and a **free or allowance-bounded CI/CD pipeline**. Treat 24/7 uptime, sustained no-charge account availability, and unlimited free real SMS as **unproven external conditions**, not available guarantees. No paid auto-upgrade.

### D-101 — USER DECISION / SCOPE OF AUTHORIZATION
The instruction to **log findings to GitHub** is permission for research documentation changes on existing research branch and research PR. It is **NOT**, by itself, an explicit authorization to modify product code, commit to main, enroll cloud/Twilio accounts, provision a server, expose the legacy API, send SMS, create a GitHub deployment workflow, or begin an unrestricted Codex build. Prior research-only D-025 remains in force until a separate explicit transition permission.

### D-102 — PRESERVATION / SUPERSESSION OF PRIOR USER DECISION D-019
D-019 previously required two independently executable **final tracks** due to the Javax/Jakarta incompatibility risk. **Supersede only the final two-product count with D-098**. Preserve the stronger underlying obligation from D-019 and D-026: every literal course capability **must actually run**, and merely archived original code does not prove unified-runtime compliance. Pin O14 SHA `2dd336ed39cecd417a8bce3388b303e688e95dee` as immutable *migration comparison, rollback and acceptance fixture*, not as an intended second permanently maintained user product. Until one product passes all gates, do not delete or weaken that source.

### D-103 — MATERIAL ENGINEERING UNCERTAINTY
No integrated supported single modern runtime has been demonstrated executing **both literal `javax.validation` (DTO and JPA-entity persistence checks)** and modern supported Jakarta-native Jersey/Spring/Hibernate, *plus* all course Retrofit/Gson/Twilio/scheduler features. This is a **feasibility hypothesis**, not a compatibility PASS. Do not silently relabel `jakarta.validation` as Javax or claim manual DTO validation is equivalent to JPA lifecycle validation. [Technical options](56_CP30_SINGLE_RUNTIME_AND_ZERO_COST_CICD_FEASIBILITY.md).

### D-104 — TIME / EVIDENCE LIMIT
Printed assignment final December 5, 2026; current Canvas not accessible/verified here. As of Nov16 19 calendar days. The prior user-defined order **O14 baseline → real P-ACCEPT → independent A-FINAL → student's manual Canvas** remains unless explicitly changed. One final product simplifies operations but increases course/runtime compatibility risk; cannot guarantee deadline.

## 2. The **ONE PRODUCT** acceptance definition

**One product** means: one coherent identity, UI, business source of truth, resource lifecycle, versioned API service boundary, production release artifact/deployment, backup/restore process, and one public address. A single backend process is strongly preferred. It may expose **both protected legacy academic GET/POST endpoints** (e.g. `/api/...`) and more secure versioned business endpoints (e.g. `/api/v2/...`) within one secured application, but this does **not** mean two user-facing products or two independent databases. Nothing permits anonymous Internet access to the legacy endpoint.

The original five entities remain *genuinely present and justified*, even if additional organization/auth/session/ledger entities are needed. Every original operation has a real testable equivalent with exact preserved course input/output/status semantics in the course compatibility interface unless the instructor explicitly authorizes a departure. Full public UI need not expose all academic diagnostic routes by name, but they must remain runnable for Postman and demo.

At runtime, literal Javax validation means real `javax.validation.Validator` invocation and reject-on-save evidence; this is not proved by Jakarta annotations or a database NOT NULL failure. If JPA pre-insert lifecycle validation is specifically required by applicable course instructions, test it rather than inventing a waiver.

The public environment must gate all old and new routes through authenticated authorization with correct 401/403/404 and CSRF, and should not expose classroom Twilio credentials/real phone data. This may require an authenticated Postman demo collection and separate protected course test mode **within the one application**, not a permanently unauthenticated second server.

## 3. Preservation inventory and statuses

| Contract | Preserved artifact and acceptance |
|---|---|
| Original 5 JPA entities | Department, Project, Resource, ResourceRequirement, Allocation; actual MySQL database mappings and runtime create/read/delete |
| Original 25 operations + A09 = 26 O14 handlers | Exact course route crosswalk, successful positive/negative Postman and state tests; A09 explicitly an O14 amendment, not originally proposed |
| Jersey REST and Spring `@Autowired` component forwarding | Actual Jersey endpoints with controllers delegating to injected components |
| `javax.validation` | Actual API and entity persistence validation in the chosen supported product; distinct from Jakarta |
| MySQL/Hibernate/Spring Data/JPQL | Real queries, constraints, transactions, no fake in-memory substitute |
| Retrofit/Gson/Twilio-related workflow | Actual Retrofit+Gson HTTP requests, provider-specific mode separated from simulator/test credentials; scheduled due-reminders operate with no false SMS-delivered assertion |
| Scheduling | Bounded recurring job, injected clock, durable attempt/event semantics, startup/restart/duplicates |
| HTTP images CLO9 | Course syllabus says receive/serve; final-project interpretation still Canvas/instructor-owned. Preserve when applicable, implement securely and test rather than silent omission |
| Bonus UI and original look | Functional original Academic Glass Commons design direction, adapted for connected role-aware inventory workflows and access |
| Postman/academic recording/source/COA | Test all operations, real source bundle/replay; student video/submission/acknowledgment truthfully done |
| Production | Identity, tenant isolation, stock conservation/idempotency, TLS, host, no required payment, CI/CD, off-failure-domain encrypted backup/restore and rollback |
| Product identity | One web app/service, one workflow and one production data source rather than two separate sites |

## 4. Historical decisions now amended, not erased

| Earlier record | New treatment |
|---|---|
| D-018 release order | **Retained.** Production technical acceptance precedes academic final acceptance |
| D-019 two *final* runnable products | **Superseded only as final product count**; full executable course compliance still required |
| D-020, D-021 independent P-ACCEPT/A-FINAL | **Retained** as two test suites against the **same final product**, not two separately maintained apps |
| D-025 research-only role split | **Retained** until user explicitly authorizes Codex implementation |
| D-026 literal course tech, 26 handlers, Postman | **Strengthened** by one-product runtime and regression requirements |
| CP13/CP27/CP29 provisional public P0, old V2 API and 17-table proposal | **Research candidates**, not automatically accepted migration or product feature scope |
| CP25 COMPAT-01 dual-artifact experiment | **Superseded as the first target by UNIFIED-COMPAT-01**; re-test the original course capabilities and security in one deployable product |
| O14 Java8 historical source | **Protected reference** until migration and course acceptance succeed; legacy public hosting prohibited |
| 2025/early 2026 temporal/partial V2 sketches | **Historical**, unless later explicitly approved; original global/whole features cannot be removed |
| "Production ready"/"free continuous deployment" | **Exit criteria**, not claims of what exists now or what providers guarantee |

## 5. New high-risk conflict register

| ID | Severity | Conflict | Gate |
|---|---|---|---|
| U-MC01 | BLOCKER | Literal `javax.validation` JPA lifecycle vs Jakarta-only modern ORM/validation | Real UNIFIED-COMPAT-01; explicit user decision if unsupported |
| U-MC02 | BLOCKER | Modern public security wrapping 26 numeric/GET-POST compatibility routes without breaking intended demonstrability | Single product authenticated Postman/JSON/system tests |
| U-MC03 | BLOCKER | Twilio real live SMS cannot be promised indefinitely for free; trial ends after 30 days, template/verified/country restrictions | Transparent T-A simulator, T-B provider virtual, T-C actual carrier; Canvas confirmation |
| U-MC04 | BLOCKER | OCI A1 capacity and idle reclamation; 24/7 availability not assured even while free | Real authorized account, restart/recovery and uptime evidence; no uptime SLA promise |
| U-MC05 | HIGH | Private GitHub Actions included budget/plan-specific approval rules may prevent free unlimited CD | Choose capped Actions or safely isolated runner and fail-closed deployment |
| U-MC06 | HIGH | Stable domain-free HTTPS needs IP cert requiring frequent renewal and trusted public address | Real 160h cert issue, renewal/reload, alert, IP-change runbook |
| U-MC07 | BLOCKER | Same production database must support historical functional continuity plus tenant isolation without exposing other tenants | Exact migration, composite FK and original 26-route regression |
| U-MC08 | HIGH | Off-provider encrypted DR cannot be replaced with same-account snapshot, GitHub repo export or CSV alone | Real independent clean restore with auth and stock invariants |
| U-MC09 | HIGH | CP29 lists public A09 omission and archival R04; user now insists "literally everything preserved" | No omission of original A09 course handler; protected archival can coexist with course deletion behavior; agree exact semantics |
| U-MC10 | HIGH | More complete production features may exceed Dec5 high-risk critical path | Stage gates, early stop and explicit user schedule trade-off without false acceptance |
| U-MC11 | HIGH | Original archived files contain sensitive/academic content; private repo must remain private; publication only after review | No public repo flip to obtain free CI minutes |

## 6. Evidence sources and review boundaries
- Original assignment: `09_ESP_PROJECT_ASSIGNMENT.pdf`, syllabus `08_CSCI180_SYLLABUS.pdf`, concise proposal `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` (reopened Nov16).
- Actual academic O14 commit `2dd336ed39cecd417a8bce3388b303e688e95dee`, source and [CP23 evidence](45_CP23_CI_POSTMAN_AND_ACCEPTANCE_EVIDENCE_RECONCILIATION.md): 55 Java/MySQL and 15 front CI passes, local 96 Newman/142 assertions and five browser journeys author-reported, actual final acceptance open.
- [CP13 conflict](40_CP13_CANONICAL_SRS_EERD_API_CONFLICT_REGISTER.md), [CP27 canonical draft](48_CP27_CANONICAL_PRECODING_HANDOFF_DRAFT.md), [CP28 review](50_CP28_RESEARCH_ACCEPTANCE_REVIEW.md), [CP29 decision](51_CP29_OWNER_DECISIONS_AND_CONTRACT_FREEZE_GATE.md), [CP29 API](52_CP29_PUBLIC_ROUTE_DTO_ROLE_ERROR_CONTRACT_CANDIDATE.md), [CP29 SQL/DR](53_CP29_MYSQL_IDENTITY_HISTORY_DR_CONTRACT_CANDIDATE.md).
- Official sources and dates documented in [56 technical feasibility](56_CP30_SINGLE_RUNTIME_AND_ZERO_COST_CICD_FEASIBILITY.md). Raw video frames not observed; source archive +1 path unresolved; Canvas account inaccessible. No real pilot or instructor approval.

## 7. Adjudication gate
**USER DECIDED:** ONE product, full course preservation, target end-to-end and no mandatory fee.  
**NOT DECIDED:** exact supported runtime, how genuine Javax entity/JPA lifecycle works there, Twilio actual paid-vs-simulated operational semantics, cloud account/provider and secure implementation, database/API contract changes, and permission to start Codex.  
**NO-GO:** unrestricted build and public production release until independent runtime/provider evidence and specific approval.  
**GO CANDIDATE:** authorize only an isolated `UNIFIED-COMPAT-01` experiment under [57 plan](57_CP30_UNIFIED_ARCHITECTURE_PROOF_AND_RELEASE_ROADMAP.md).

No source-code changes are implied by this research decision.
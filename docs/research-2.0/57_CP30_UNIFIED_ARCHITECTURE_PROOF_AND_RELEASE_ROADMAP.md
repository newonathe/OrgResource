# CP30 — Unified architecture proof, staged engineering and one-product release roadmap

**Dated:** November 16, 2026 Asia/Manila. **Status:** PLAN / FUTURE TESTS ONLY; engineering authorization OPEN. **Goal:** ONE complete, secured, course-compliant, truly end-to-end functioning OrgResource product with bounded no-mandatory-charge CI/CD and deployment, without two separately maintained live products.

## 1. No code until the exact first authorization
The user's latest message authorizes logging/research repository work. It **does not** authorize a whole-product Codex build or provider/paid actions under research-first D-025/D-031. An explicit transition in the separate orchestration chat should specify **UNIFIED-COMPAT-01 ONLY** first. The earlier COMPAT-01 two-final-product design no longer represents the desired outcome.

### Research handoff package to read in order
1. [54 single-product decision/conflict](54_CP30_ONE_UNIFIED_ORGRESOURCE_DECISION_AND_CONFLICT_REGISTER.md) (latest goal, supersedes final two-product count).
2. [55 exact course/production acceptance](55_CP30_ONE_PRODUCT_COURSE_AND_PRODUCTION_COMPLIANCE_MATRIX.md) (all literal course capabilities and original 26).
3. [56 official compatibility/CI/CD/free provider research](56_CP30_SINGLE_RUNTIME_AND_ZERO_COST_CICD_FEASIBILITY.md) (namespace and SMS hard limits).
4. [57 this roadmap](57_CP30_UNIFIED_ARCHITECTURE_PROOF_AND_RELEASE_ROADMAP.md).
5. Original assignment/syllabus/proposal; historical O14 code pinned; [30 exact 26-route crosswalk](30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md).
6. [40 CP13 source/design conflicts](40_CP13_CANONICAL_SRS_EERD_API_CONFLICT_REGISTER.md), [45 O14 evidence](45_CP23_CI_POSTMAN_AND_ACCEPTANCE_EVIDENCE_RECONCILIATION.md), [46 archive/export](46_CP24_RELEASE_AND_ORIGINAL_ARCHIVE_PROVENANCE.md), [52 public DTO route proposal](52_CP29_PUBLIC_ROUTE_DTO_ROLE_ERROR_CONTRACT_CANDIDATE.md), [53 MySQL/DR](53_CP29_MYSQL_IDENTITY_HISTORY_DR_CONTRACT_CANDIDATE.md).
Old CP27/CP29 statements that final deliverable must be **two separate products** are **historical/superseded**, but their functional/domain/security research survives pending owner review.

## 2. Precise first engineering proof — UNIFIED-COMPAT-01 (NOT AUTHORIZED/RUN)
**Purpose:** falsify or prove whether **ONE supported runtime and DB** can execute actual course-mandated components *and* modern public security, before attempting feature build. Not a release; disposable local fixtures, no live accounts or SMS.

### Inputs and containment
- Pin repository SHA for O14 `2dd336ed39cecd417a8bce3388b303e688e95dee` and research SHA at approval time; preserve both as read-only comparison, no in-place edits.
- Create separate isolated **spike branch/worktree** based on owner-approved lineage, not historical docs PR #7 or `main` by guess. No production accounts, public endpoint, paid host/provider, data migration or Twilio outreach.
- Provisional target: maintained Java17+/Java21, Boot4.x + Jersey4.x + Jakarta native ORM/MySQL8.4 and actual literal `javax.validation` module. Pin actual released versions and unpatched CVEs after source inspection. Do NOT silently swap Jersey, Spring, JPA, Javax or MySQL for alternative.
- Use local fictional data; no privileged CI deploy, repo/org secret or real user roster.

### Proof cases (one-JVM aim; one MySQL)
| ID | Real executable check | PASS only if... |
|---|---|---|
| UC01 | Maven/Java compile and boot pinned supported Spring/Jersey from actual dependency graph | One app process; Jersey `@Path` registered with actual HTTP |
| UC02 | Spring `@Autowired` Jersey controller→Component→real Spring Data repo | Real GET/POST JSON through Jersey delegate; successful MySQL write |
| UC03 | Real `javax.validation.Validator` (not Jakarta) invalid DTO | Invalid field rejected with javax violation, no DB write, source still uses literal `javax.validation` |
| UC04 | Invalid JPA entity `saveAndFlush` | Actual annotation rejection before successful DB commit and independent of only DB NOT NULL; inspect whether JPA lifecycle autovalidation or explicitly evidenced equivalent; if course requires automatic lifecycle and only manual bridge runs, mark CONDITIONAL/FAIL, not PASS |
| UC05 | Modern native Jakarta validation and Jersey security coexist with separate javax provider | Actual HTTP valid/invalid JSON, 401, wrong role403, missing CSRF403; session changes enforced through same front door |
| UC06 | MySQL real JPQL custom query/relationships and exception rollback | Five original entity types and controller delegation modeled, plus negative FK/rollbacks |
| UC07 | Real Retrofit2/Gson HTTP request to a *separate local FakeTwilio* service and Spring scheduled job | Gson response parsed, repeat/failure/clock observed, NOT REAL TWILIO |
| UC08 | 26 academic GET/POST operation definitions compile/register under secured compatibility namespace | Actual public access denied without auth; Postman 26/26 route behavior verified (not just swagger count) |
| UC09 | Original global holds/whole return and resource shortage tested after endpoint call | Reusable T-H-I, consumable T-H, pending/expired/returned rules; no duplicate quantity |
| UC10 | Legacy fixture + modern secured endpoint share **one** transactional MySQL source of truth | Same records under both interfaces, no parallel inconsistent DB or fake bridge |
| UC11 | Actual runtime dependency/SBOM security assessment | No unsupported **unresolved critical production-exposed** provider risk; JV/HV classloader conflict and provider discovery documented |
| UC12 | Full reproduction packet | Source SHA, versions, build commands, tests, actual HTTP+DB logs, FAIL/PASS, teardown; no account or deployment touches |

**Critical rule:** Do not count a manually invoked javax Validator as proof of JPA lifecycle hook if it does not actually fire. Do not mark "Spring Boot4 supports Javax" merely because the JAR compiles. STOP and report if UC03/UC04/UC08/UC10 cannot be completed or if unsafe unsupported dependencies remain.

### Spike decision outcomes
- **PASS:** propose supported one-runtime one-product architecture for explicit owner review and subsequent full engineering authorization.
- **PARTIAL:** list exact failing course or security condition, assess one-product internal compatibility process as fallback **only after user choice** (it would still be one user-facing system, but two runtimes internally, a meaningful compromise).
- **FAIL:** maintain protected O14 reference and report one-product zero-cost requirement cannot yet be satisfied on tested stacks; do not silently reduce course scope, adopt paid support or deploy unsafe Boot2 runtime.

## 3. Canonical proposed final application architecture (conditional on proof)
`Browser (single web URL) → Reverse proxy trusted HTTPS → secured Jersey REST controllers (academic compat + modern endpoint namespaces) → Spring components/services with auth/current tenant checks → Spring Data JPA + actual MySQL8.4`.
React/TypeScript responsive UI built as assets of same release; no second production website. Additional entity tables for Organization/Membership/credentials/ledger/DR are justified; original five types remain real. All course operations have authenticated Postman/demo paths. `Retrofit+Gson→Twilio provider adapter / simulator` and `@Scheduled` reminder logic in same deployment. Secure image receiving/serving under applicable course scope and storage quotas. Never use two live inconsistent stock ledgers.

If identity scope requires separating public and academic endpoint DTOs, keep separate REST route namespaces **within the same application**, with identical one-database authoritative inventory; never publicly enable insecure classroom request flows.

## 4. Staged Codex engineering sequence AFTER explicit approvals
| Gate | Dependency | Concrete future work | Required exit artifact |
|---|---|---|---|
| U0 | latest source/decision acknowledgment | Read-only SHA, branch, all-file coverage, scope/no-secret audit | Complete baseline report, no product edit |
| U1 | explicit owner permission, source pinned | **UNIFIED-COMPAT-01** only | UC01–UC12 result matrix with logs |
| U2 | U1 PASS + approval of final public role/physical schema | Choose one-runtime architecture; write exact single-product API/DB migration and original 26 regression fixtures | Signed architecture ADR/DDL/API, not arbitrary "fast MVP" |
| U3 | U2 | Organization/Department/project/resource/requirement/allocation tenant model, local identity and durable operations | Real MySQL FK/auth/session tests; original five work |
| U4 | U3 | Reusable and consumable engine; global holds, whole returns, coverage, lock order, historical deletion and resource ledger | T5/q4/q4 concurrent pass, same-resource FK, replay>48h |
| U5 | U3–4 | Secure original 26 Jersey GET/POST route compatibility + modern API; scheduler, Retrofit/Gson/Twilio adapter, CLO9 images if applicable | Real Postman, javax DTO+JPA, job, provider-mode smoke |
| U6 | U5 | Existing visual design adapted to connected React UI with accessible role-aware workflows | Playwright 360/768/1440, loading/retry/error, no mock acceptance |
| U7 | U4–6 and authorized provider | GitHub Actions free-limit CI + approved-release CD; secure free VM/MySQL/HTTPS and off-provider encrypted backup+restore | Real SHA-pinned pipeline/deploy/rollback, billing safe, TLS renew + clean restore |
| U8 | U3–7 | Production P-ACCEPT (one product) | Independently signed true runtime security/DB/UI/host/DR evidence |
| U9 | U8 PASS | Academic A-FINAL **on same product** | All course source/runtime/Postman/Javax/Retrofit/Twilio/scheduler/CLO9 criteria verified, student's truthfully made demo |
| U10 | U9 PASS | Student manual Canvas upload | Real student receipt; not an agent action |

No change to D-018 production-before-academic acceptance ordering. **D-019's prior demand for two separately runnable *final products* is superseded by D-098**; no deletion of original academic functionality is authorized.

### Cut hierarchy and unacceptable substitutions
Optional only: future time-window optimization, partial/damaged returns, real org SSO, payment, analytics, barcodes, third-party email beyond Twilio, microservices/brokers. Do not cut original five types, any 26 API operations, javax validation, Jersey, JPA/JPQL/MySQL, Retrofit+Gson/Twilio adapter, scheduling, applicable CLO9, full Postman, functional UI, tenant auth, stock integrity, backups or secure deployment while claiming "fully compliant production ready". For real unrestricted SMS, admit impossible indefinite free proof rather than substituting simulated delivery silently.

## 5. GitHub Actions continuous deployment acceptance (proposed)
- Private repository stays private; verified owner budget/allowance, no unlimited free assertion. Deploy on reviewed release tag or protected approved main merge, not PR.
- Jobs: checkout pinned → license/CVE/secret checks → compile Java and frontend → Javax+Jakarta+26-route+MySQL integration → Newman and Playwright E2E → immutable artifact SHA/signature if available → stage/smoke → approved deploy via least-privilege secret → health/readiness and real persistent read/write → rollback app artifact if failed. Schema rollback requires migration-safe plan.
- Test artifacts redacted and limited by free GitHub storage; retention/usage monitored with fail-closed threshold, no paid overages. GitHub-hosted runners preferred over long-lived runner on production host.
- Production host: pinned controlled JVM/MySQL, systemd/restart or service manager, private DB port, least-privilege users, firewall, Linux updates, backup automation, observable logs, 160h Let's Encrypt IP cert renewal+reload, alerts and recovery runbook.
- Only actual authorized local/prod runs support PASS. No "CI green" proxy for actual deployed state.

## 6. Immediate human and research evidence requirements
- **Student/current Canvas:** applicability of CLO9, expected Twilio T-A/T-B/T-C level, deadline/video/peer instructions, AI/assistance disclosure; do not infer instructor answer.
- **Owner:** authorize isolated UNIFIED-COMPAT-01; confirm one product with one JVM preference and what to do if Javax cannot be safely supported; choose P0 fictional/global/whole and identity/DR policies; consent to specific hosting/account later.
- **Provider account:** verified Always Free eligibility, A1 capacity, credit card policy, billing protections, GitHub Actions private free-budget, IP HTTPS readiness and off-provider backup setup; not available pre-auth.
- **Source:** exact one-path ZIP manifest reconciliation, and O14 flaky test expected5/actual8 root cause to debug before final.
- **Academic integrity:** student reviews/understands authored work; no fabricated interview, pilot, peer review, test evidence, submission or original video-frame inspection.

## 7. Stop/go definition
**At present:** research updates authorized, no compatibility code authorized.  
**Near-term next action:** explicit permission for UNIFIED-COMPAT-01 to be prepared/executed in the separate Codex workflow; approval does not authorize whole product.  
**Full engineering:** only after spike evidence and precise schema/API/security owner signoff.  
**Public production:** NO-GO until actual free provider, zero-paid fallback, web app, auth, runtime, HTTPS renew, backup restore and tests pass.  
**Academic final:** NO-GO until real production acceptance under recorded user order and full course real technology evidence.  
**Date risk:** Dec5 printed deadline is ambitious and no delivery guarantee/Canvas extension is established.

## 8. Copy-ready bounded *research-to-orchestration* handoff, NOT an instruction to execute product code now

> Read the private OrgResource research branch's latest commit and `docs/research-2.0/54`–`57` plus original CSCI assignment/syllabus/proposal and academic O14 SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`. The latest user decision is ONE final live product retaining ALL original runnable Jersey/Spring/Javax/JPA/MySQL/JPQL/Retrofit/Gson/Twilio/scheduled/5-entity/26-route/Postman course functionality while adding full secure real production deployment. Before any feature build, prepare an isolated, permission-bounded `UNIFIED-COMPAT-01` for owner authorization, proving UC01–UC12 in a maintained single-runtime candidate. Do not change O14 source, merge, pay, provision cloud, send Twilio or claim runtime PASS without actual evidence. Report all blockers and exact source SHA. Preserve private repo and the Dec5 printed/release order; request a separate authorization for implementation.

This paragraph is an orchestration **draft**, not implicit user approval to run Codex.

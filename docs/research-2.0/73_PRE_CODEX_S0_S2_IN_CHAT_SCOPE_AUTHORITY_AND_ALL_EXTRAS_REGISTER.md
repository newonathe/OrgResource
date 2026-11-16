# OrgResource pre-Codex work division and full non-course change register

> **OWNER ADOPTION UPDATE 2026-11-16:** Previously this register listed A (course-only), B (middle), and C (full) as choices. The user **has now explicitly chosen full Option C PLUS an independently finished course-only edition**: first freeze the fully academic compliant A, then derive the full C extension B from A preserving course code/behavior. This is not a request to cut or replace C. [CP44](74_CP44_TWO_EDITION_COURSE_IMMUTABILITY_AND_FULL_C_HANDOFF_DECISION.md) supersedes §E alternatives and the former single-artifact production-first chronology. [Final two-edition prompt 75](75_FINAL_TWO_EDITION_CODEX_HANDOFF_MASTER_PROMPT.txt) supersedes historic 71. Individual extra features E01–E39 remain design additions, not newly invented professor mandates. All executable tests/builds remain undone.


**16 November 2026, PHT. USER'S CURRENT REQUEST:** conduct **ALL research, S0 source audit, S1 paper feasibility/alternative examination, S2 scope/architecture decisions and Codex prompt authoring in THIS ChatGPT research workspace**. Deliver **only the actual build and executable proof** to Codex after final review. Never suggest that static research passed a runtime integration test. No implementation or production branches changed here. **Source audit below is read-only and evidence-limited.**

## A. Binding source hierarchy and crucial correction

1. **COURSE REQUIREMENT:** `09_ESP_PROJECT_ASSIGNMENT.pdf` pp.1–3 (the supplied ESP course project specification). It names Jersey REST/JSON/Spring annotations/JPA+Spring Data/Javax Validation/RetroJit [sic; syllabus calls Retrofit]/Twilio/scheduling; 5+ justified entities; true runtime create/read/delete and controller→Autowired Component; complete Postman; detailed proposal; skeleton/final artifacts and video/peer; Dec5 final.
2. **SYLLABUS FACT:** `08_CSCI180_SYLLABUS.pdf` p2 requires Jersey, Postman, Hibernate/MySQL, JPQL, Javax Validation, Retrofit/Gson, and image receive+serve (CLO9); p3 image-in-database lesson; p4 rubric; pp5–6 Canvas/admin and authorship/assistance/peer; Dec5 in assessment. Its p4 assessment CLO-number mapping differs from p2 CLO descriptions; include a minimal image feature conservatively. No unseen extra requirement or instructor waiver may be assumed.
3. **PRIOR PROPOSAL, NOT COURSE MANDATE:** `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` pp1–12, original 25 REST operations, one organization's five domain entities, specific statuses/stock rules, hourly scheduled SMS through Twilio, whole-batch issue/return/consume, Philippine local timestamps, original JSON; current O14 amendment adds an A09 revocation handler = **26 CURRENT SOURCE OPERATIONS**. The course did **not** prescribe 25 or 26 endpoints as a universal assignment requirement; preserving 26 is prior proposal/code continuity and user decision.
4. **LATEST USER DECISION** supplied syllabus+assignment are the only final written course instructions; current Canvas submission still required by their own text, but no unseen Canvas/instructor *design* blockers.
5. **LATEST USER DECISION** ONE real final product, with original course features retained, no mandatory paid service; production-first verification order was previously a user choice, not an instructor mandate. User now additionally says all *research and S0–S2 planning* should happen here, only the final actual software implementation to Codex.

## B. S0 — read-only source and branch audit performed IN CHAT (not a test run)

| Evidence | Direct check / status |
|---|---|
| GitHub branch main | `main` at `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`; earlier state, not current academic O14 |
| GitHub academic draft | PR #8, `feat/o14-academic-final-acceptance-2026-11-15` at `2dd336ed39cecd417a8bce3388b303e688e95dee`, unmerged |
| GitHub research draft | PR #7, `docs/orgresource2-research-continuity-2026-11-15` (latest SHA must be refetched), unmerged |
| Actual O14 backend main | `backend/pom.xml`: Spring Boot parent **2.1.0.RELEASE**, Java **1.8**, Jersey **2.26**, MySQL Connector/J **8.0.13**, Retrofit **2.4.0** and Gson converter **2.1.0** |
| Actual O14 entities | `Department`, `Project`, `Resource`, `ResourceRequirement`, `Allocation` five genuine `@Entity` families (additional enum source files are **NOT additional entity types**) |
| Actual O14 controllers | `DepartmentController` 4 Jersey methods; `ProjectController` 4; `ResourceController` 5; `RequirementController` 4; `AllocationController` 9; **4+4+5+4+9=26** actual handlers |
| Actual O14 repositories | 5 real repository interfaces. Existing `findByDepartment_Id`, `findByType`, `findByStatus`, `lockById`, sum JPQL and `findAll` have **NO org discriminator**. Not a vulnerability for *one-local-organization* course model, but cannot be reused unchanged in a multi-tenant design. |
| Actual O14 frontend | 8 source pages: Overview, Departments, Projects, Resources, Requirements, Allocations, Reminders and About; functional source, not mocked screenshots |
| Reminder transport | `app/reminders/FakeTwilioTransport.java` exists and uses Retrofit/Gson to local server; nonexistent `TwilioSmsTransport.java` path tested as NOT_FOUND in O14. Do not claim actual official provider transport exists. |
| O14 reported tests | `evidence/O14/FINAL_ACCEPTANCE_REPORT.md` describes 55 Java tests, 15 frontend tests, 5 browser journeys, 96 Newman requests/142 assertions passed locally. This P0 audit **did not execute or revalidate** them. Historical same-source CI yielded one expected5/actual8 aggregation failure and one full pass; cause undiagnosed (CP39). |
| Images/auth/ledger/host | O14 README explicitly says no real image feature, authenticated accounts, multi-org access, ledger, secure hosted product or real SMS; cannot claim these exist |
| Original ZIP/path coverage | Prior source register has one outer-entry discrepancy; original complete ZIP bytes were not exhaustively reopened/checked in this audit. Do not label full archive inventory or original-video frames verified. |
| Local Windows repo | No connection to user's actual Windows directory; did NOT update or inspect it. No local compile/DB/runtime logs here. |

**S0 status:** `SOURCE_STATIC_AUDIT_PERFORMED_IN_CHAT / EXECUTION_AND_COMPLETE_ARCHIVE_NOT_RUN`. Enough to make a source-preserving design decision; not an all-source exhaustive source-archive verification nor a runtime acceptance. No additional separate Codex S0 stage is needed merely to redo the same static facts; Codex must still independently pin baseline and verify exact checkout at execution start.

## C. S1 — what research CAN settle vs what a real Java/MySQL run MUST settle

**VERIFIED EXTERNAL FACTS as of 2026-11-16:** official [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html) state 4.1.2 requires Java17+; official [managed dependency list](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html) includes Jersey4.0.3; [Hibernate Validator 9.1](https://hibernate.org/validator/releases/9.1/) implements **Jakarta Validation3.1** and supports Boot4.1; [Validator6.2](https://hibernate.org/validator/releases/6.2/) retains **javax** packages but limited support and only lists older Boot2.4–2.7. These **do not prove** a single supported Hibernate/JPA auto-validation path accepts both. The assignment requires *Javax Validation*, but does **not literally require automatic JPA saveAndFlush event enforcement*; that was a rigorous planned proof check, not an instructor text claim.

**S1 decisions possible here:** prioritize all literal course functions; choose candidate modern Java17+/Boot4/Jersey4/Jakarta ORM with an **explicit, transparently-tested javax.validation adapter for original course DTO/entity validation**, while keeping native Jakarta validation for ORM. Treat this as a **hypothesis**, not a proven stack. An alternate maintained one-product/two-internal-runtime design would require owner's special adoption and tests; legacy Boot2.1 public Internet exposure is not an acceptable silent shortcut. Port each original source handler rather than assuming javax.ws.rs or javax.persistence annotation classes work unchanged with Jersey4/Jakarta. Real tests must show controller to component to MySQL, invalid javax DTO, invalid persisted entity, transaction/JPQL, retrofit/scheduling, auth/CSRF, bundled React, Five representative route probes and ledger clock arithmetic. Full 26-route runtime compliance belongs to final acceptance, not to the bounded feasibility experiment.

**S1 boundary:** Can complete research and decisive recommendation here; **cannot mark S1 PASS or freeze production dependencies/feasibility without executable build/run**, which belongs to Codex's **first internal go/no-go checkpoint on the final handoff**. If that first experiment fails, Codex must STOP and return evidence here for redesign before implementing whole product. No promise the full software can be produced solely through a one-shot prompt with no reviews.

## D. Full scope-change register — compared with syllabus + assignment + original proposal and actual O14

Classification codes:
- `M`: explicitly in supplied syllabus or assignment (course). Original proposal usually implements this concretely.
- `P`: specifically established by original OrgResource concise proposal or existing O14; not universally required by course.
- `E`: outside course AND original proposed O14; new OrgResource production/product enhancement.
- `Q`: implementation-quality/safety/operations recommended but not specifically course-mandated.
- `X`: explicit user preference independent of course; no implication of instructor approval.
Some rows have multiple codes. **These are ALL proposed decisions, not features built, unless O14-existing is stated.**

### Original academic/previous-proposal functionality to preserve
| ID | Feature | Classification | What exactly is and isn't source-required |
|---|---|---|---|
| C01 | Useful fictional organization resource service | M/P | Assignment asks for a service useful to community, but does not attest measured demand/official inventory |
| C02 | Five domain entities, JDBC/MySQL/JPA, Hibernate, Spring repos, custom JPQL | M/P | Five minimum, not maximum; no mandate for extra identity/ledger tables |
| C03 | Jersey REST/JSON/GET/POST/Autowired Component and entity CRUD+eligible delete | M/P | Required; full 26 specific routes are **original proposal+O14/user continuation**, not professor-imposed route count |
| C04 | Literal `javax.validation`, runtime validation errors | M | Explicit; no direct wording demanding automatic saveAndFlush event listener |
| C05 | Retrofit/Gson, external service at least Twilio, scheduling | M/P | Actual Twilio-specific HTTP contract should be implemented conservatively; no obligation for real charged/delivered SMS in printed text |
| C06 | Actual receive/serve image over HTTP | M (syllabus CLO9), user conservative inclusion | Not yet present in O14; printed assignment all-tools wording + syllabus support adding minimal real image endpoint |
| C07 | Postman for every REST call, accurate Word proposal, source/export, genuine video and peer as applicable, COA and help acknowledgment, Canvas final | M | No fabricated instructor approval, peer review if solo, video or submission receipt |
| C08 | Reusable vs consumable stock, quantities, approvals, whole-batch returns | P | Proposal pp1,5–10. No partial returns, future-slot booking, barcode or financial rules |
| C09 | 25 original explicit routes + O14 A09 =26 current | P/X | Already O14; preserve because user requests existing working course behavior, not assignment numeric requirement |
| C10 | Original Philippine-local dates and Long IDs / original JSON shapes | P | Existing compatibility; new UUID/UTC is extra product transport, not required for course |
| C11 | Local eight-screen React UI and glass visual direction | P/M optional bonus | Assignment calls the UI **optional bonus**; UI is already real and should be reused |
| C12 | Fictional SMS contacts, one-shot scheduled due reminder and consent | P | Original proposal p10 Twilio reminder; current O14 FakeTwilio and A09 amendment; live phone delivery not built |

### Additions NOT required by syllabus/assignment/proposal/O14 — show owner BEFORE Codex
| ID | New product feature / change | Class | Scope/impact and cut recommendation |
|---|---|---|---|
| E01 | Multiple independent organizations in one instance | E/X proposed full-product goal | **HIGH:** requires tenant-scoped every table, query, access, export, image and lock; not demanded by course |
| E02 | Member accounts, login/logout, password management | E/Q public safety | **HIGH:** needed if public multi-user, unnecessary for purely local course prototype |
| E03 | Session cookies, CSRF, password hashing, login throttling | E/Q | **HIGH:** tied to E02; must be genuine if shipping authenticated public product |
| E04 | Invitation-only signup, one-time 24h bearer tokens | E | **MED-HIGH:** custom identity lifecycle, no campus identity guarantee |
| E05 | Account recovery codes, one-time generation/rotation | E | **MED-HIGH:** custom credentials/recovery tests |
| E06 | Five roles (OWNER,ADMIN,CUSTODIAN,LEAD,MEMBER) | E | **HIGH:** detailed authorization matrix, tests across original+new API |
| E07 | Assigned Project Leads and role-filtered borrower list | E | **MED:** extra association/accessor/lookup workflow |
| E08 | Organization switching / activeOrgId | E | **MED-HIGH:** data-context and UI-cache security |
| E09 | Ownership transfer, last-owner constraint and pending lifecycle | E | **HIGH:** extra state machine and concurrency |
| E10 | Member suspension, role changes, organization FROZEN state | E | **MED:** extra authorization/recovery complexity |
| E11 | Public `/api/v2` API alongside **protected** 26 legacy Jersey routes | E/X one-product compatibility design | **VERY HIGH:** second transport contract, errors, mapping and tests; **not** a separate product |
| E12 | UUID public IDs, UTC time, JSON envelope, cursor/page | E | **MED-HIGH:** mapping/version migration and cross-API parity; course requires none |
| E13 | ETags/If-Match/HTTP412, Idempotency-Key receipts | E/Q | **MED-HIGH:** concurrency/retry protocol; beyond assignment though reliability benefit |
| E14 | Permanent per-operation IDs, duplicate ISSUE/RETURN/CONSUME guard | E/Q | **MED-HIGH:** integrity safeguard for public product; full durable receipts beyond proposal |
| E15 | Immutable physical stock-movement ledger plus SQL reconciliation | E/Q | **VERY HIGH:** new write model and history; original proposal only had Allocation statuses and total |
| E16 | Receiving stock and reasoned stock adjustments as distinct actions | E | **MED:** new stock workflows, beyond original totalQuantity update |
| E17 | Historical resource archive/unarchive instead of eligible physical delete after stock movements | E/Q | **MED:** intentionally changes original R04 edge behavior; preserve actual eligible delete test to meet written assignment |
| E18 | Detailed activity/audit feed and audit tables | E/Q | **MED:** actor provenance, redact sensitive data; not required by course |
| E19 | Organization-scoped exports CSV/JSON | E | **LOW-MED:** optional, can defer without breaking course |
| E20 | Resource image per-org permissions, upload limit 2MiB, 20MiB/org | M image; E/Q limits and tenancy | Image transport itself source-aligned; arbitrary limits/tenant handling are design decisions |
| E21 | In-app due indicators and provider outcome distinctions | E/Q | Some due list already O14; richer delivery status not course mandate |
| E22 | Encryption at rest for optional phone, key rotation policies | E/Q | **MED-HIGH:** adds key/ops burden; necessary if retaining real sensitive contacts in public product, avoid storing phone until authorized |
| E23 | Initial fictional owner operator bootstrap | E | **MED:** needed only with invitation-only E01–E06 |
| E24 | Additional UI Login, Invite, Workspace, Members, Account, Activity, Exports, movements | E | **HIGH:** 7+ new experiences atop eight O14 pages |
| E25 | New domain supporting tables: organization, user_account, credential, recovery, membership, invitation, transfer, movement, audit, idempotency, image, Spring Session | E/Q | **VERY HIGH:** current 18-table **candidate count** is not mandatory; five originals remain |
| E26 | Database migrations and clean-install schema instead of O14 automatic table generation | E/Q | **MED:** appropriate for production safety, not specified in course |
| E27 | Modern Java17+/Boot4/Jersey4/Jakarta migration while preserving literal javax | E/Q | **VERY HIGH** risk/compatibility; written course does not demand modern versions; no proven single-runtime solution |
| E28 | Secure Internet hosting, domain, HTTPS and public service availability | E/X full-product aspiration | **HIGH:** assignment does not require public deployment; cost/account permissions remain unverified |
| E29 | Free-forever/zero-mandatory-cost cloud optimization | X | User preference, **NOT** a verified provider guarantee; no charges without approval |
| E30 | GitHub Actions CI/CD, PR gating, version pinning, artifact checksums and SBOM | E/Q | **MED:** strong engineering practice, not a rubric feature |
| E31 | Encrypted off-host backups, independent restore, RPO/RTO, deploy rollback | E/Q | **HIGH:** production reliability, not explicit course requirement |
| E32 | Multi-worker scheduling, race/load tests and exhaustive attack tests | E/Q | **MED-HIGH:** strong acceptance, beyond class rubric |
| E33 | Formal independent production acceptance before academic acceptance | X | Earlier user workflow choice, **not** professor requirement; severe Dec5 scheduling risk |
| E34 | Unit labels, resource publicId/version/status, richer audit metadata | E | **MED:** changes original 35-column physical model + DTOs |
| E35 | Consent/borrower identity via membership rather than free-text | E | **MED:** significant new public workflow; protected old route must still accept original source format |
| E36 | Approval-hold automatic expiry at Project.endAt | O14-adjacent E/P contract correction | **BEHAVIOR CHANGE from original proposal p8** (“reservation remains until cancelled, returned or consumed”); O14 introduces expiry. Must explicitly test end-boundary semantics and not portray this as original professor requirement |
| E37 | Official Twilio test credential/Virtual Phone external account check | Q (method), not added course feature | Written source requires Twilio integration, **not** paid SMS or a real handset-delivered message; test credentials and Virtual Phone are separate from FakeTwilio |
| E38 | Public identity assertions about Ateneo membership, SSO, university-owned stock or real user deployment | NOT AUTHORIZED | Explicitly outside scope; no invented institution approval |
| E39 | QR/barcodes, partial/damaged/lost returns, time-slot scheduling, accounting/procurement, unrestricted signup, advanced analytics, push notifications | DEFER | Not in supplied assignment/proposal; do not add to academic P0 |

**This table is a requirements-coverage inventory of the latest CP31–CP43 design, not a guarantee that every field or UI microinteraction has been fully enumerated.** The underlying per-route and per-table details remain specified in CP34–CP35, not all duplicated here.

## E. What should be approved vs cut before Codex?

**RECOMMENDATION, course-first smallest functioning deliverable:** Preserve the academic O14 one-org five-domain, all 26 functions and real eight-screen UI, add true image upload/get, actual Twilio Messages API adapter (disabled real sending, truthful tests), resolve flaky DB test, verify accurate proposal/Postman, portable clean start, source ZIP, student video/COA/peer. This is closest to syllabus/assignment and has the best chance by Dec5. It is NOT the previously chosen *full production-style multi-org scope*, and adopting it as FINAL would require an explicit **USER DECISION CHANGE** from the previously stated one-product production-first goal.

**Alternative, full original user product ambition:** Keep E01–E35, the v2 route suite, supported runtime, secure hosting, verified zero-cost/backup gate and production-first acceptance. This is the existing planning candidate, **not course-required**, and is high-risk against the remaining 19 calendar days. A fully honest feasible approach might require more time or owner-approved phased acceptance; do not silently cut requirements to make status look green.

**Middle ground to propose, not silently adopt:** One complete connected product for **one** fictional organization with basic safe local auth, full source features, safe stock log and photos, but defer multi-org/ownership recovery/second public v2/host. This is an **amendment** to earlier user direction and still needs explicit approval.

**Stop doing before Codex:** no more general CPnn surveys, no retroactive fake interviews or sources, no unapproved extra workflows, no public Boot2 deployment and no claims that a researched runtime has passed compile/DB tests.

## F. S2 decisions this workspace can finish as paper contracts

**Already grounded and adoptable for planning:** the five named entity relationships, their original 26 operation signatures/JSON, whole-batch PENDING→APPROVED→ISSUED→RETURNED (reusable) and APPROVED→CONSUMED (consumable), scoped total/holds/issued mathematics, project time boundary and expiry difference register, no false SMS delivery, phone consent revocation, no lost/damaged/partial, one real app, all needed Postman and syllabus CLO9 image HTTP, programmatic acceptance cases, source-consistent `resource.type`/`allocation.borrower_name`.

**Conditional/non-course and not user-approved:** multi-org role model, custom identity recovery, two API contracts, 18 table candidate, immutable ledger, full host/backup/DR, UTC/UUID, opaque idempotency receipts. These require an explicit one-page scope authorization before final handoff.

**Cannot be honestly frozen from documents alone:** the actual modern Javax/Jakarta validation behavior; effective compiled dependency/servlet/Jersey runtime; full MySQL migration constraints; cross-API real transactions; genuine CI flake root cause; guaranteed free hosted account/backup/restore; real Twilio provider availability; student local Windows repo state. The first **Codex execution gate** after approved final handoff must be a **small fail-fast technology proof**; after it, return any blocker to this ChatGPT workspace for user scope/architecture resolution rather than proceeding with an unsupported guess.

## G. Practical local repo boundary

Cloud Codex cannot update the user's Windows folder itself. The research prompt may include safe instructions, but only a **local** action by user/local Codex can inspect `git status`, fetch, and fast-forward the exact intended branch or create a separate worktree. O14 and research PRs are unmerged; `git pull main` is not a way to obtain latest O14. Never `reset --hard`, `clean -fd`, force push or overwrite dirty local files. The full stage-gated handoff is [71](71_CODEX_MASTER_HANDOFF_STAGED_EXECUTION_AND_LOCAL_WINDOWS_SYNC.txt).

## H. Recommendation / clear decision to user

**S0:** source code and requirements can be audited here and are substantially identified; live runtime/root CI cannot be proven here. **S1:** only research and a conditional architecture recommendation here; mandatory executable probe inside authorized final Codex build, with hard stop. **S2:** finalize one *selected* scope and API/database blueprint here before handing code; exact software version lock conditional on S1 execution. **Do not call any stage fully passed without its own required evidence.**

**Owner choices still necessary:** (1) Full production-style E01–E35 by December5 **vs** minimal course-first **vs** approved middle scope; (2) whether to retain production-first hard ordering despite deadline; (3) whether any actual Twilio-account testing, public hosting, paid-tier option, local machine changes are allowed—defaults all NO. Until choices, all expensive unmandated features are labeled PROPOSED rather than approved.

## Evidence

- Directly re-read **09 assignment pp1–3**, **08 syllabus pp2–6**, **10 concise proposal pp1–12**, O14 `backend/pom.xml`, 5 Jersey controllers, 5 repository interfaces, frontend pages/tree, O14 README and prior evidence report, CP35/42/43 and sealed CP37.
- Official dated primary documentation checked November16 2026: [Spring Boot 4.1.2 system requirements](https://docs.spring.io/spring-boot/system-requirements.html), [Boot dependency list](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html), [Hibernate Validator 9.1](https://hibernate.org/validator/releases/9.1/), [Validator 6.2](https://hibernate.org/validator/releases/6.2/), [Twilio test credentials](https://www.twilio.com/docs/iam/test-credentials) (new credentials can no longer be created via new Console; existing legacy test credentials may still work), [Twilio Virtual Phone](https://www.twilio.com/docs/messaging/guides/guide-to-using-the-twilio-virtual-phone).
- No Java/SQL/MySQL, local Windows commands, actual Twilio API requests, merges, deployments or academic submissions were run in this research turn.

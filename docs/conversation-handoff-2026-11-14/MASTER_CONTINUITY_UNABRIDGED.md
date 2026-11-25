# ORGRESOURCE — COMPLETE NEW-CONVERSATION CONTINUITY MASTER

**Continuity snapshot:** 2026-11-14 (Asia/Manila). **Live O3 state:** IN PROGRESS when user requested this pack; DO NOT assume completion. **O2 video:** user reported recording DONE, not independently opened. **Current repo truth:** must be freshly audited at new chat start.

**Source provenance:** The 12 following component files were read directly from dedicated GitHub documentation branch `docs/conversation-continuity-2026-11-14` and reproduced in this single file in the stated order. The additional source text after them is the established preexisting `docs/ORGRESOURCE_MASTER_HANDOFF.md` from verified snapshot commit `16e781c`, incorporated for full planning context. This is a source-merged reading artifact, not an actual fresh code/test audit. Primary course files and current GitHub code retain authority.



---

# INCLUDED SOURCE 01/12: 00_READ_THIS_FIRST.md

# OrgResource: new-conversation continuity pack (2026-11-14, Asia/Manila)

## Purpose and how to use
This repository directory preserves the substantive project decisions, engineering instructions, evidence, scope, current build-in-progress, user requirements, and continuation procedure from the ChatGPT conversation ending with the request to transfer everything to a new conversation in the same OrgResource ChatGPT Project.

**This is a continuity overlay, not a substitute for the current repository, its source code, the primary course assignment/syllabus, or the original proposals and course files.** The complete prior 188,934-byte implementation planning packet already exists at `docs/ORGRESOURCE_MASTER_HANDOFF.md` and a byte-equivalent copy at `docs/complete-handoff/ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md`. This overlay refers to that full packet instead of copying the same material redundantly. The new conversation must read the source-of-truth files and re-audit the current branch/commits, especially since Codex O3 implementation was explicitly **still running** when this continuity pack was requested.

Suggested reading order for the new conversation:
1. `00_READ_THIS_FIRST.md` — authority and use.
2. `01_CONVERSATION_DECISIONS_AND_HISTORY.md` — exact sequence, user priorities, delivery context, and explicit authorizations.
3. `02_GITHUB_SNAPSHOT_AND_EVIDENCE.md` — independently checked remote snapshot and known code/evidence limits.
4. `03_COURSE_DOMAIN_API_CONTRACT.md` — course authority, five-entity design, all APIs, domain calculations, error handling, and academic integrity.
5. `04_PHASES_TESTS_RELEASE.md` — roadmap, acceptance, SMS/scheduling, UI, DevOps and final deliverables.
6. `05_O3_ACTIVE_BUILD_COMPLETION_GATE.md` — current Codex job, exact acceptance conditions, what to inspect immediately after completion.
7. `06_TOKEN_PERFORMANCE_PUSH_POLICY.md` — standing instructions that take priority for planning the NEXT Codex prompt.
8. `07_NEW_CHAT_COPY_READY_PROMPT.md` — first message to copy into a new conversation.
9. `08_SOURCE_DIRECTORY_EVIDENCE_REGISTER.md` — paths, provenance, historical material and availability.
10. `09_CONTINUATION_CHECKLIST_AND_DECISION_LOG.md` — audit template and open questions.
11. `10_O4_O5_COMBINED_CYCLE_SEED.md` — conditional combined O4+O5 implementation contract.
12. `11_O2_VIDEO_DEMO_RUNBOOK_HISTORY.md` — preserved OBS, VS Code, course-linked narration and commands.

**Single-file alternative:** `MASTER_CONTINUITY_UNABRIDGED.md` will concatenate every file in this directory into a single Markdown reading layer.

## Snapshot, not timeless status
At the last successful connected GitHub audit during the handoff request:
- Private repository: `newonathe/OrgResource`.
- Remote `main`: `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`; last committed 2026-11-14 13:56:55 UTC.
- On `main`, 13/25 original proposal routes implemented, plus proposed A09 NOT implemented.
- One older draft PR #1 from `demo/o2-recording-kit-2026-11-14` exists and should not be blindly merged; no GitHub Actions runs.
- Codex was executing the O3 completion prompt at the user's report; **do not presume its success, push, tests, or route count before auditing remote and local evidence**.
- User explicitly reported that they had **finished recording the O2 skeleton video**. Actual video file content, uploaded Canvas receipt, sharing access and submission status were NOT independently verified by this chat. The earlier checked-in `evidence/O1/VIDEO_NOT_CREATED.md` is older than this user report and is a **stale report**, not proof that a recording still does not exist.

## Authority hierarchy
COURSE REQUIREMENT: actual current `09_ESP_PROJECT_ASSIGNMENT.pdf`; SYLLABUS FACT: `08_CSCI180_SYLLABUS.pdf`; current Canvas updates may supersede printed dates but have not been verified. USER DECISION: the user's live instructions in the conversation. The 12-page five-entity `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` defines original 25 operations. The 26th consent-withdrawal route is a reviewed amendment. The earlier nine-entity/68-operation package is historical research, not the selected current scope.

Project: one fictional student organization's resource planning system, NOT a verified official Ateneo app, not a deployed/piloted campus system, not evidence of measured demand, adoption, or integration. Preserve honest academic assistance disclosures. Source corpus may include extracted audio transcripts but **no raw video screen content was analyzed**.

## Very important role distinction
This ChatGPT Project = research, review, continuity, audit, and Codex prompt authoring. Codex = actual implementation. Do not claim ChatGPT implemented Java or ran the user's local MySQL unless a tool actually did so. Do not send Codex another full project plan when a scoped, actionable larger-cycle prompt is needed.

## New conversation behavior
Immediately after this pack is supplied, perform a new read-only connected GitHub audit: current main and relevant feature heads, open PRs, recent commits, changed files, route parity, evidence and tests. Don't reuse the fixed snapshot as current truth. Then provide a precise completion review of the ongoing O3 build and, when warranted, the **combined O4+O5 large coherent coding prompt** using the rules in file 06. If O3 is still not complete, offer a targeted repair/finish prompt and preserve work. Never automatically merge to main or submit to Canvas.

## What “complete/lossless” means and does NOT mean
The pack attempts comprehensive transfer of all MATERIAL instructions, design decisions, history, conflicts and continuity state visible in the conversation. It does **not** claim to be a byte-for-byte transcript of every private thought, an exhaustive reread of every original ZIP entry, a substitute for all original course files, a current Git checkout, a runtime test, or a video. For source-level losslessness, the original committed source and docs MUST remain canonical and be reopened at the precise version currently on GitHub. No secrets or password data are replicated into this handoff.


---

# INCLUDED SOURCE 02/12: 01_CONVERSATION_DECISIONS_AND_HISTORY.md

# Conversation history, decisions, and exact intent register

## A. High-level course/project history
The user is preparing a real CSCI 180.05 academic project at Ateneo, titled OrgResource / Student Organization Resource Planning and Allocation System. The ChatGPT Project is the **planning and review workspace**; Codex builds the product sequentially. The current assignment governs course requirements, the syllabus governs course facts, and Canvas can update dates/instructions. Project materials comprise the assignment, syllabus, source map, context dossier, historical proposals, course examples and archives, transcript corpus, and current GitHub repository. Prior concepts are drafts, not irrevocable choices.

Earlier work assembled a substantial **planning packet**: 37-file handoff, approximately 188,934-byte consolidated `docs/ORGRESOURCE_MASTER_HANDOFF.md`, full source audit/decisions/data/API/testing/frontend/release chapters, JSON/CSV Postman templates, implementation prompts, and staged acceptance. The old contracts for 9 entities/68 operations are HISTORICAL; the chosen small complete prototype uses 5 JPA entities and 25 proposal routes, plus separate proposed A09 consent-withdrawal route. Do not allow a future agent to implement the historical design by accident. Prior planning itself was NOT proof of compiled code.

## B. Actual implementation and audits in this conversation
1. The initial repository audit checked the PRIVATE connected GitHub repository `newonathe/OrgResource`. Baseline `acc301992da820089ff9b2567ee095241cf8c307`, O1 Java skeleton, 12 original routes, 5 actual entity classes, controllers, components and repositories, Spring Boot/Jersey/JPA/MySQL, a 2026-11-13 source export ZIP, recorded Maven/MySQL/Postman evidence, **no independently rerun local runtime**. Latent error discovered in `sumCoveredQuantity()`: it counts expired APPROVED Allocation coverage while availability excludes expired holds. Mentioned potentially imprecise exception mapping. User explicitly asked whether “code-level” work actually runs: the answer distinguished static source inspection, saved logs and independent execution.
2. Asked what to submit for the November 14 skeleton milestone: source export + genuine screen video showing successful startup, five tables, test data, working HTTP/JSON; separate peer reviews for actual other members if applicable; check Canvas. Final December 5 functionality not required for O2.
3. User reported new commits and demanded a fast, complete, nontechnical, click-by-click video demo. Subsequent GitHub audit found `39482cc` Department update, `f576cf8` O1 preflight setup, `16e781c` PowerShell 5 fix. Added eight Java tests and demo scripts `scripts/start-skeleton-demo.ps1`, `scripts/run-skeleton-demo-sql.ps1`, `scripts/skeleton-demo.sql`, a focused 2-request 4-assertion Newman collection; source ZIP became `deliverables/OrgResource-Java-Skeleton-2026-11-14.zip`. Saved O3 evidence: D03 tests 11 requests/19 assertions passed, O1 regression 15 requests/29 assertions passed. O3 route count 13 of original 25.
4. User demanded nontechnical yet course-grounded narration explaining what each real code layer does and its significance. Prepared scripts explaining Spring Boot startup, MySQL tables, JPA/Hibernate entities, Spring Data findBy/JPQL, Jersey `@Path @GET @POST` controller, `@Autowired` Spring component separation, Postman/Newman create-and-read and JSON. Grounded in actual course Calculator and Animal Sighting/Jersey examples; did NOT claim unfinished approval/SMS/UI work works.
5. User said OBS Studio would record VS Code while this chat remained visible privately. Prepared full OBS Window Capture guide, not Display Capture, plus exact commands and file paths, detailed recording narration, API GET via `Invoke-RestMethod` in VS Code Terminal B, source ZIP and MP4 naming. User subsequently explicitly stated: **“done with the video.”** This is a USER REPORT; no actual recording file was verified in this chat. Don't perpetuate stale “video missing” as a current fact.
6. User requested the “next master prompt and files/context”. Audited GitHub and produced detailed post-O2 O3 continuation Codex master: finish Project P03/P04; Resource R03/R04; Requirement Q03/Q04; fix expired APPROVED coverage. Keep original five entities and 25 routes. Later phase plan O4–O14. At that moment current remote HEAD still `16e781c`.
7. User specified: “note that I am using Luna 6 Model at extra high reasoning mode to build all these which is a lower model with lower capabilities and reasoning power so EVERYTHING in the prompt has to be VERY SPECIFIC. but still, I WANT TO BE VERY TOKEN EFFICIENT ON BUILDING ALL THESE, Detailed, thorough, precise, accurate, complete but not overdoing it, overcomplicating or going overboard. I also want it to be very fast, scalable, and storage efficient in terms of the actual build and the actual software. Also, again, VERY TOKEN EFFICIENT IN THE BUILD WITH NO WASTAGE.” This is a **request about prompt design**; do not make any independent claim about model capability. Revised O3 prompt to exact file read order, minimal redundant reads, fixed route contracts and detailed acceptance, careful performance/storage rules, and explicit smaller cycle target 19/25.
8. User further corrected the strategy: “okay do that for this slice but for the next ones, I WANT TO BALANCE BIG BUILD AND TOKEN EFFICIENCY. Goal still is to be full maximum token efficiency but sometimes u tend to overscope it down to specific tasks to the point that it takes way too long to build which costs more cause there's so many complications. also always audit github repo before doing anything then tell in the prompt to push everything always.” IMPORTANT: **Do not continue artificially splitting one feature per session.** Maximize verified working software per session and token. Larger coherent, dependency-aware builds with internal test gates. Each new prompt must begin with a fresh remote+local Git audit. Each Codex cycle should commit and push all legitimate task-related changes to a **remote feature branch**, verify remote SHA, and preferably open/update PR; do NOT merge to main, force-push, reset user work, commit secrets or submit academically without separate approval.
9. Agreed upcoming major build grouping: current O3 stays as existing scoped prompt; NEXT cycle O4+O5 (transactional Allocation lifecycle + expiry scheduling); following O6+O7 (Retrofit/Gson FakeTwilio reminders + React foundation with separate modules); following O8–O10 (complete real frontend workflows); final O11–O14 (integrated testing, conditional image interpretation, packaging/export/release). Groupings are flexible engineering cycles, **not** mandates to split unnecessarily or bypass checks; allow one session to complete a whole grouping where feasible. The first priority remains correctness, source compatibility, real tests and persistent evidence.
10. User's current request: the previous O3 Codex prompt is **STILL BUILDING**. Once it finishes, user will open a **NEW CONVERSATION in this same OrgResource ChatGPT Project** and wants a thorough, high-fidelity handoff/master prompt. This pack is that continuation bridge. The new conversation should NOT immediately issue another O3 implementation prompt before checking the completed Codex push/results. It must **freshly audit** GitHub and the actual O3 task outcome, perform quality review, then prepare the next combined large-cycle prompt.

## C. Binding user preferences and decision register
USER DECISION — real software over documentation-only. No fabricated code, tests, data, campus need, adoption, approvals, or pilot. Do not claim source archives were exhaustively read unless proven. Explain exactly what the code does, distinguish current implementation and future desired behavior.

USER DECISION — Java 8 course-compatible Spring Boot 2.1.0/Jersey/Javax/Spring Data JPA/Hibernate/MySQL, VS Code development; preserve Eclipse-compatible source export. Do not unilaterally migrate to Spring Boot 3/Jakarta or introduce new backend.

USER DECISION — final real, working React/TypeScript/Vite frontend, even though course calls UI optional bonus. Responsive/accessible, real REST calls, no fake dashboards or localStorage authoritative stock, designed for practical user workflows.

USER DECISION — exactly five JPA entities; 25 original paths, plus separately tracked optional A09 consent withdrawal; resource types REUSABLE/CONSUMABLE; whole-batch workflow. Don't introduce unrequested sixth entity/StockMovement or heavy infrastructure.

USER DECISION — optimize total working, tested product delivered per token, not token count alone. Avoid excessive granularity. Write detailed implementation contracts with clear acceptance but do NOT make agent reread entire 188k handoff on every coding turn. Read once, then inspect relevant sections/deltas. Smallest *coherent* tested batch, large enough to avoid many costly sessions. No repeated planning in place of implementation.

USER DECISION — for EVERY new coding-cycle prompt, first independently audit GitHub remote and instruct coding agent to inspect local Git state too. Do not rely on old SHA to decide what's pending. **Automatically commit and push all task-related changes to remote feature branch**, verify SHA and PR. Distinguish safe pushes from merges; never merge to main/force push/overwrite user work without approval. User phrase: “push everything always.” Interpret as relevant version-controlled source/test/docs/evidence, NOT secrets, caches, unrelated local data.

USER DECISION — keep communication explicit, grounded, precise, comprehensive, efficient, with up-to-date audit, exact file paths, states, methods, JSON, errors, acceptance criteria and realistic effort. No generic filler, broad unverified claims, or endless clarifying questions when work can proceed.

## D. User-facing mode and academic integrity
The ChatGPT planning conversation should prepare vetted prompts and audit resulting GitHub commits. Codex is the implementation worker. User may employ a named model at Extra High reasoning; don't imply automatic superior correctness. Source-first and direct official docs for changeable facts. “VERIFIED” only if observed. Label material claims COURSE REQUIREMENT, SYLLABUS FACT, USER DECISION, DESIGN RECOMMENDATION, ASSUMPTION, OPEN QUESTION or verified external fact. Check syllabus Certificate of Authorship, outside-assistance acknowledgement, and Canvas generative-AI instructions; don't invent a course AI policy. Solo member implies peer reviews only if actual other members; don't invent fake reviews.

## E. Audio/video/source limitations
The two COMPLETE_SOURCE_NONVIDEO ZIPs preserve original non-video filesystem entries and paths; course corpus provides searchable reading. A manifest previously reported 2,611 paths and 852 unique recorded hashes; that tally was not independently rehashed by the assistant. `06_VIDEO_TRANSCRIPTS.md` is timestamped AUDIO transcripts only. No raw video screen frames are available. Never claim to have watched unattached recordings or visual screen information inferred from audio alone. If visual-only detail matters, use corroborating slide/lab code or user local inspection at timestamp.


---

# INCLUDED SOURCE 03/12: 02_GITHUB_SNAPSHOT_AND_EVIDENCE.md

# GitHub repository: verified snapshot, current code and evidence boundaries

AUDIT SNAPSHOT: 2026-11-14, approximately 23:35 Asia/Manila at handoff request. This was a connected GitHub **READ** of private repo state. Local Codex workspace and uncommitted files inaccessible. **Audit again before decisions.**

## Repository and branch state
- Owner/repo: `newonathe/OrgResource` (PRIVATE).
- Default branch: `main`.
- Remote HEAD `main` = `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`, commit subject `fix: support PowerShell 5 in skeleton demo launcher`, committed 2026-11-14 13:56:55 UTC.
- Prior commits: `f576cf8dda939c67ac7c0e69934e0e7699f8d18c` demo preflight; `39482cca36d1f147cadec25c2ed9a93e23ec3739` D03 update; `acc301992da820089ff9b2567ee095241cf8c307` O1 baseline.
- Remote branches at audit: `main`; `demo/o2-recording-kit-2026-11-14` head `89d61934b145a9139085d782dd4ddfc38f17b474`.
- PR #1 open DRAFT, title “O2: prepare one-click Java skeleton video demo (no agent needed)”, head old demo branch and base `acc3019`; historical/overlapping alternative video-kit. DO NOT blindly merge with latest main scripts.
- GitHub Actions workflow run count: zero at audit; CI success not claimed.
- Repo tree had 434 entries, not a complete exhaustive semantic code review of all entries. No `frontend/` app source. 34 `.java` files including two Java test classes; no `evidence/O4/` on `main`.
- While this overlay was being written, user stated ongoing Codex O3 build. It may create/push new feature branches/commits after the audit. Use no static count once it finishes.

## Actual source present in `backend/`
Configuration:
`pom.xml` Spring Boot parent `2.1.0.RELEASE`, `java.version=1.8`, Jersey `2.26`, starter Jersey/data-jpa/validation/test, Jersey Jackson 2.26, `mysql:mysql-connector-java:8.0.13`; Maven wrapper and Eclipse .project/.classpath/.settings; `application.properties` server binds 127.0.0.1:8080, DB URL/user/password from ORG_DB_* environment, Hibernate MySQL57Dialect, InnoDB, local-only `ddl-auto=update`, connection pool max 5, open-in-view false.
Actual app: `backend/src/main/java/app/SpringBootApp.java` SpringBootApplication and EnableScheduling (annotation exists; no actual reminder/expiry jobs yet).
Jersey: `app/config/JerseyConfig.java` @ApplicationPath("/api"), registers five controller classes, exception mapper and Jackson.
Clock: `app/config/ClockConfiguration.java` injected Asia/Manila clock.
Entities: Department, Project, Resource, ResourceRequirement, Allocation; enums AllocationStatus, ResourceType, CancellationReason, ReminderOutcome.
Repositories: DepartmentRepository, ProjectRepository, ResourceRepository, ResourceRequirementRepository, AllocationRepository.
Components: DepartmentComponent, ProjectComponent, ResourceComponent, RequirementComponent, AllocationComponent, NameNormalizer, RequestValidation, ApiException.
Controllers: DepartmentController, ProjectController, ResourceController, RequirementController, AllocationController.
DTOs: `app/dto/ApiDtos.java`, public fields for narrow JSON; AllocationView intentionally redacts raw phoneNumber and provides contactPresent.
Tests: `backend/src/test/java/app/components/DepartmentComponentTest.java`, `backend/src/test/java/app/config/ApiExceptionMapperTest.java`, total eight tests recorded by O3 phase.

## 13/25 original routes present on current audited main
D01 POST /api/departments (create)
D02 GET /api/departments (list)
D03 POST /api/departments/{id}/update (newly implemented in O3)
D04 POST /api/departments/{id}/delete (guarded)
P01 POST /api/projects (create)
P02 GET /api/projects?departmentId= (list/filter)
R01 POST /api/resources (create)
R02 GET /api/resources?type= (list/filter)
R05 GET /api/resources/{id}/availability (exact 2-field JSON)
Q01 POST /api/requirements (create)
Q02 GET /api/requirements?projectId= (list/filter)
A01 POST /api/allocations (create PENDING only)
A02 GET /api/allocations?requirementId=&status= (list/filter)
MISSING at audit: P03/P04, R03/R04, Q03/Q04, A03-A08 and optional A09. Total exactly 13 original routes currently present; the current Codex O3 job targets 19/25 and is NOT YET VERIFIED. These are method/path pairs, not mere document entries.

## Code-level behavior and defects confirmed from direct reads
- `DepartmentComponent.create`: normalize display/key, reject duplicate names, DB save, DTO. `update`: positive ID, normalized uniqueness, `saveAndFlush`, named DB constraint handles race. `delete`: rejects if referenced by Project. Department controller @Path, GET/POST, injected component.
- `ProjectComponent.create`: name, future start, end > start, valid Department FK, save. `list`: optionally filter by Department ID. `ProjectController` currently only POST/GET; O3 must add update/delete.
- `ResourceComponent.create/list/getAvailability`: validates nonnegative stock, ResourceType, normalized uniqueness. Available reusable = total - effective APPROVED holds - ISSUED custody; consumable = total - effective APPROVED holds. Throws 500 for impossible negative/invariant values rather than silently clamping. Mutating Resource update/delete absent before O3.
- `RequirementComponent.create/list/view`: quantity 1..1e6, Project and Resource references, Project end guard, unique Project/Resource pair. Coverage computed via AllocationRepository; shortage from qty minus sum.
- `AllocationComponent.create/list`: records PENDING qty<=requirement qty, project not ended, borrowerName, optional E.164-like contact retained only when consent true. Redacted API DTO; lists by requirement/status. Does not yet approve/reserve/issue/return/consume/cancel/delete at audit.
- **KNOWN DEFECT:** `AllocationRepository.sumCoveredQuantity` sums `APPROVED,ISSUED,RETURNED,CONSUMED` unconditionally and erroneously counts expired APPROVED status for coverage. `sumEffectiveHolds` correctly filters expired Project end. O3 MUST correct coverage JPQL with time bound and fixed-clock tests.
- Error mapper at O3 handles named Department unique conflict, Jersey unknown-route 404, malformed route param 400, javax validation and generic errors. More exhaustive error handling remains a target; no claim all concurrent failures map correctly.
- Data-protection boundary: application loopback-only, no real auth, fictional data, raw phone not serialized; not production secured.
- Current system is a genuine Java skeleton with recorded successful local runtime, not complete application.

## Recorded evidence under `evidence/O1`
`PHASE_REPORT.md`: JDK Temurin 8u512; Maven Wrapper 3.3.4/Maven 3.10.0; Spring Boot 2.1.0; MySQL 8.0.46; five InnoDB tables; 4 FKs and 3 uniqueness constraints. O1 final Newman recorded 15 requests, 29 assertions, 0 failed. Restart persistence verified in saved HTTP evidence; phone not leaked. Clean source archive Maven build recorded successful. Originally zero Java test files in O1; later O3 added eight tests. The existence of evidence files is NOT a fresh independent rerun in the new ChatGPT conversation.

`evidence/O3/PHASE_REPORT.md`: 8 Java tests passing, D03 Newman 11 requests/19 assertions passing, O1 regression 15 requests/29 assertions passing, JSON 404 unknown route and 400 malformed ID. Clean extracted `OrgResource-Java-Skeleton-2026-11-14.zip` built/tested, 47 files, 37,507 bytes, SHA-256 `875E361A2D7E50649A7383EA6E6A808002892FD3BE4D5923BDA3CB929BF91B30`. Actual Eclipse IDE GUI import not verified. Source ZIP is not the final complete app.
`evidence/O1/O1-SKELETON-DEMO-PREFLIGHT-2026-11-14.md`: Java8 launcher and database SQL script successful on user workstation. Protected local credentials use `%LOCALAPPDATA%\OrgResourceDev`; app 127.0.0.1:8080, MySQL 127.0.0.1:3308 `orgresource` using `orgresource_dev`. The two Workbench profiles on 3306/3307 are NOT this DB. SQL is read-only. Running O1 focused Newman collection produced two requests/four passing assertions. Passwords never committed.
`evidence/O1/VIDEO_NOT_CREATED.md` and O3 phase report say no O2 video as of those older checks; user LATER said video recording DONE. Treat the older notes as stale.

## O2 demo commands already used by user
From repo root in VS Code terminal:
```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-skeleton-demo.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-skeleton-demo-sql.ps1
npm exec --yes --package=newman -- newman run postman\OrgResource-O1-Skeleton-Demo.postman_collection.json --reporters cli
```
OBS Window Capture recorded only VS Code while user read ChatGPT privately. User reported finishing the video. Do not recreate O2 by default.

## New conversation audit sequence
At time of continuation:
1. Connected GitHub remote: fetch repo metadata, main recent commits, all relevant branches, PRs and changed files, CI checks.
2. Identify which branch Codex used for active O3, and compare to baseline and main. If no remote push, say no remote evidence and ask user for Codex output or inspect local via available connector where authorized.
3. Read new O3 `PHASE_REPORT`, `ROUTE_PARITY`, actual changed Java source, tests, Postman collections, repository code and locks. Verify content matches prompt and report. Saved test PASS is evidence but not identical to independently rerunning.
4. Count original routes exact method/path 25; track A09 separate. Never infer code works from route table alone.
5. Review correctness: transactions, error mapping, expected stock precondition, date-history protection, coverage expiry, query behavior, tests and DB/HTTP evidence, no secrets or unrelated changes.
6. Return acceptance verdict `PASS/CONDITIONALLY_READY/BLOCKED/FAIL` with exact gaps, then prepare next prompt accordingly.
7. Do not merge to main automatically or ignore feature-branch head merely because `main` is older.


---

# INCLUDED SOURCE 04/12: 03_COURSE_DOMAIN_API_CONTRACT.md

# Controlling course, domain, API, data and integrity contract

**Use this as an index and precise continuity copy. Reopen the actual assignment, syllabus, 12-page proposal and current source before making changed-interface decisions.** Source files may change with new Codex work. Source authority takes precedence over this snapshot where evidence conflicts.

## 1. Project identity, problem and scope
Project name: OrgResource — Student Organization Resource Planning and Allocation System.
Use case: one **hypothetical** student organization with Departments coordinating Projects; stock of Resources (equipment and consumables); project-specific ResourceRequirements; and Allocation records of requested quantities and lifecycle states. Local trusted officer manages data. Borrower is a descriptive fictional representative; no self-service login or account. No verified campus rollout, interviews, adoption, integrations, measured savings or instructor endorsement.
Compare alternatives: spreadsheet is simpler but manual integrity; existing systems not surveyed; 9-entity ERP/68 routes and 6-entity stock ledger are historical alternatives beyond current deadline. Current five-entity model selected as small complete academic prototype. No procurement, partial/damaged returns, payments, supplier APIs, production auth, campuses, multi-tenancy, microservice infrastructure, or real-person data.

## 2. COURSE REQUIREMENT — assignment 09_ESP_PROJECT_ASSIGNMENT.pdf
Directly consulted 3-page printed assignment:
- Build a service useful to a proposed Ateneo community use case, group 1–3.
- Use Jersey REST, JSON, Spring annotations, JPA/Spring Data repositories, Javax Validation, printed "RetroJit" (syllabus and course sample clearly Retrofit and GSON) for external-service communication at least Twilio, and Scheduling.
- >=5 justified entity types. Optional @PostConstruct data initialization, not substitute for runtime CRUD.
- One or more web controllers with several operations; **GET retrieval and POST submission** (course slide mention of PUT does not override assignment). Basic create/read for every entity, real runtime create/delete of rows.
- **Controller MUST forward to Spring component via @Autowired**; controllers handle external I/O rather than owning business rules.
- Postman definition for **all REST calls**.
- Part I Word proposal printed Oct 30; group, description, every operation URL/inputs/JSON/errors/component method, component method steps, entities/fields/types/null/validations, repository custom methods; applicable genuine peer review for other actual group members.
- Part II skeleton implementation due Nov 13, Canvas exported project + genuine demo video due Nov 14 showing no fatal errors, generated DB tables, seeded test data, repositories/controller-component accessible and returning JSON URLs in Postman; video was user-reported complete.
- Part III final full logic, finalized project, video of group discussing (accessible link permissions where applicable), printed Dec 5. UI is course-optional bonus but USER has made final React UI required for this project.
- Current Canvas deadlines/policies MUST be checked directly, not invented.

## 3. SYLLABUS FACT — 08_CSCI180_SYLLABUS.pdf
Course CSCI 180.05 Enterprise Systems Programming; CLO1 Eclipse/debug, CLO2 Java project/deps, CLO3 Spring Boot/Jersey, CLO4 Postman, CLO5 Spring Data JPA/Hibernate/MySQL, CLO6 JPQL, CLO7 Javax Validation, CLO8 Retrofit/Gson JSON/HTTP, CLO9 receive/serve images over HTTP, CLO10 final integrated project. CLO9 images may be mandatory via syllabus but are **not explicitly a required route** in 25-route proposal. Flag OPEN QUESTION; if confirmed needed, two Resource image endpoints and nullable data/mime columns, not a sixth entity, with real byte round trip.
Syllabus requires Certificate of Authorship/outside assistance acknowledgement. It alone does not establish an explicit AI-use prohibition; verify current Canvas.
Course examples: Java 8/Jersey 2.26/Spring Boot 2.1.0.RELEASE/Spring Data JPA 2.1.2/Hibernate 5.3.7/MySQL Connector 8.0.13; JAX-RS @Path @GET @POST @Produces/@Consumes; Spring @Component/@Autowired component delegation; @Repository JpaRepository derived `findBy`, @Query JPQL, javax.validation, Retrofit + Gson forms to FakeTwilio; @EnableScheduling/@Scheduled. Do NOT copy demo SchedulingApp's deliberate DataSource/JPA exclusions. Source corpus and actual ZIP sources control when exact dependency/interface matters.

## 4. Five-domain-entity contract, fields, significance
**Department:** `id: Long` DB-generated positive PK; `name:String` 1–100 display normalized; `nameKey:String` lower-case Locale.ROOT normalized, NOT NULL unique DB index. Owns Projects; refuse delete if referenced. Duplicate create/rename 409. Should not store arbitrary invented Ateneo organizational data.
**Project:** id Long; required `department: Department @ManyToOne`; `name:String` 1–100 trimmed; `startAt, endAt: LocalDateTime` Philippine wall times `YYYY-MM-DDTHH:mm:ss`; on create future start, end>start. Active interval **[startAt,endAt)**. On update department immutable; name-only may change even if historical dates; changed dates require future new start and **no Allocation historically approved** beneath it (`approvedAt != null` regardless of current status). Delete only if no Requirements.
**Resource:** id; `name` 1–100 trimmed; normalized unique nameKey; `type: REUSABLE|CONSUMABLE` immutable; `totalQuantity: Integer` range 0..1,000,000. For reusable it means usable quantity owned (including issued on loan); for consumable it means physical remaining stock. `availableQuantity` computed, never persisted. Update accepts name,totalQuantity,**expectedTotalQuantity** version-like expected-value guard; conflict 409 STALE_RESOURCE_QUANTITY. Type not editable. Delete only total==0 and no Requirement references. No required unit field; put measurement in descriptive name, e.g. "A4 paper sheets".
**ResourceRequirement:** id; required `project`, required `resource`, integer quantity 1..1e6, database unique(project_id,resource_id). One Project–Resource pair per Requirement, a Project may need multiple Resources. Derived `allocatedQuantity` coverage and `shortageQuantity`. Cannot reduce quantity below max(effective covered, largest one live PENDING Allocation quantity); cannot update after Project ended; physical delete only if zero Allocation history.
**Allocation:** id; required Requirement; `quantity` integer 1..1e6 <= Requirement qty, immutable after create; `borrowerName` 1–100 fictional label; nullable phoneNumber only when valid E.164-like +[1-9][0-9]{7,14} and smsConsent true; consent=false discards phone; `smsConsent:Boolean`; `status` persisted ENUM STRING PENDING/APPROVED/ISSUED/RETURNED/CONSUMED/CANCELLED; createdAt, approvedAt, issuedAt, returnedAt, consumedAt, cancelledAt nullable lifecycle markers (createdAt not null); cancellationReason USER_REQUEST|EXPIRED; reminderClaimedAt and reminderOutcome NONE/CLAIMED/SUPPRESSED/SIMULATED/PROVIDER_ACCEPTED/REJECTED/UNKNOWN; optional reminderHttpStatus/providerMessageSid with only true provider values. Public response NEVER exposes phoneNumber; supplies `contactPresent`, `reminderAttempted`, `expiredPendingCleanup` derived fields. Status/timestamps server-controlled.

## 5. Stock, coverage, time and state invariants
Sample one injected Asia/Manila `Clock` time per operation. Java `LocalDateTime` PHT wall time, no accidental browser UTC conversion. Active Project [startAt,endAt): start inclusive, end exclusive. Early approval before Project start allowed (conservative global hold) if end in future. No speculative future return-based capacity.

Resource total T.
H = SUM APPROVED allocations whose associated Project `endAt > now` (even if project hasn't started).
I = SUM ISSUED reusable allocations (even after project end until real RETURN).
REUSABLE Available = T - H - I.
CONSUMABLE Available = T - H. On CONSUME q, total T->T-q while H->H-q in the same transaction; AVAILABLE must remain unchanged at exact consume transition, not double decremented. Never clamp negative available to 0; invariant breach is server error.

Requirement Covered = SUM effective APPROVED (end>now) plus ISSUED plus RETURNED plus CONSUMED. Exclude PENDING, CANCELLED, expired unfulfilled APPROVED. Shortage = max(0, requirement.quantity - Covered). Coverage maximum <= Requirement qty; empty aggregate -> zero. Returned equipment becomes globally available but **the original Project's requirement remains fulfilled**. Project end releases unissued APPROVED reservation as an effective hold immediately, even before scheduled cleanup. ISSUED custody remains physically unavailable after end.

Allocation state machine:
- New → PENDING, no hold, optional consent.
- PENDING → APPROVED only if project not ended, sufficient available stock, enough uncovered requirement; reserve q and approvedAt, no physical deduction.
- PENDING → CANCELLED (USER_REQUEST) or expired cleanup CANCELLED(EXPIRED), no hold.
- PENDING → DELETE permitted if never approved.
- APPROVED reusable → ISSUED only during active Project, no second availability deduction; set issuedAt.
- ISSUED reusable → RETURNED at any time including late; explicit whole-batch usable return, set returnedAt, custody released, original requirement coverage retained.
- APPROVED consumable → CONSUMED during active Project, one atomic total stock decrement, set consumedAt, refuse repeat.
- APPROVED → CANCELLED before end USER_REQUEST or after end EXPIRED; remove hold if still effective, preserve approvedAt; no physical stock change.
- APPPROVED expiry cleanup after Project end → CANCELLED(EXPIRED); hold was already ineffective, no double release.
- CANCELLED never-approved → DELETE allowed; CANCELLED with approvedAt nonnull → delete forbidden. All ISSUED/RETURNED/CONSUMED cannot cancel or physically delete. Repeated invalid transitions 409 no state change.
- Proposed A09 revoke SMS consent: clear phone and set smsConsent false for any existing Allocation, independent of inventory status; repeat idempotent; no phantom SMS.

Concurrency and transaction:
Lock Project → Resource → Allocation in same order for allocation/inventory mutations, revalidate after locks, ensure DB source of truth. Resource updates lock Resource before comparing expectedTotalQuantity. Requirement reduction guards both covered/max single pending. Transactions atomic, no double reserve/consume; parallel approval cannot overbook resource or overcover requirement. All documented race tests run on actual MySQL InnoDB, not only H2 or mocks. Keep external HTTP OUTSIDE transaction. No retry non-idempotent POST automatically after unknown outcome; re-fetch/reconcile.
Database schema uses five tables: department, project, resource, resource_requirement, allocation; FKs and unique constraints named/verified where applicable; MySQL-only ddl-auto=update accepted for local skeleton, reviewed migrations prior to broader release.

## 6. EXACT 25 original method/path contracts (+ A09)
Base `http://127.0.0.1:8080/api` local by runtime setting. POST create returns 201; other success 200. GET list empty [] rather than 404 for valid filter with no matches. Successful deletion `{"message":"Record deleted."}`. Errors have `{"code":"...","message":"..."}` (code additive amendment to original message). IDs generated, not client-writable; public DTOs not entities.

| ID | HTTP method/path | Input and controlling behavior |
|---|---|---|
| D01 | POST /departments | {"name":"Logistics"} create unique normalized, 201 |
| D02 | GET /departments | list Department[] |
| D03 | POST /departments/{id}/update | {"name":"Operations"} unique rename, 200 |
| D04 | POST /departments/{id}/delete | delete if no referencing Projects |
| P01 | POST /projects | {"departmentId":1,"name":"Workshop","startAt":"2026-12-10T09:00:00","endAt":"2026-12-10T12:00:00"} future interval/valid department |
| P02 | GET /projects?departmentId=1 | optional Department filter; [] if no matches |
| P03 | POST /projects/{id}/update | {"name":"Updated Workshop","startAt":"2026-12-10T09:00:00","endAt":"2026-12-10T12:00:00"} name/date update; Department unchanged; historic approvals lock changed dates |
| P04 | POST /projects/{id}/delete | delete only with no Requirements |
| R01 | POST /resources | {"name":"Extension cords","type":"REUSABLE","totalQuantity":10}; no unit |
| R02 | GET /resources?type=REUSABLE | optional enum filter |
| R03 | POST /resources/{id}/update | {"name":"A4 paper sheets","totalQuantity":120,"expectedTotalQuantity":70}; lock/expected guard/commitment bound, type immutable |
| R04 | POST /resources/{id}/delete | only total0 and no Requirements |
| R05 | GET /resources/{id}/availability | {"resourceId":id,"availableQuantity":n}; **exactly two fields** |
| Q01 | POST /requirements | {"projectId":2,"resourceId":3,"quantity":6}; unique Project/Resource pair, Project not ended |
| Q02 | GET /requirements?projectId=2 | optional filter, returns allocatedQuantity and shortageQuantity |
| Q03 | POST /requirements/{id}/update | {"quantity":8}; max(covered,largest live pending) bound; project not ended |
| Q04 | POST /requirements/{id}/delete | no Allocation references |
| A01 | POST /allocations | {"requirementId":4,"quantity":4,"borrowerName":"Fictional representative","smsConsent":false}; optional valid phone only with consent; creates PENDING, no reservation |
| A02 | GET /allocations?requirementId=4&status=PENDING | both filters optional/conjunctive; redacted responses |
| A03 | POST /allocations/{id}/approve | PENDING→APPROVED, capacity/coverage/end checks |
| A04 | POST /allocations/{id}/issue | APPROVED reusable→ISSUED in active interval, no double deduction |
| A05 | POST /allocations/{id}/return | ISSUED reusable→RETURNED, full usable batch, late allowed |
| A06 | POST /allocations/{id}/consume | APPROVED consumable→CONSUMED, physical stock deducted exactly once |
| A07 | POST /allocations/{id}/cancel | PENDING/APPROVED→CANCELLED only, appropriate reason/history |
| A08 | POST /allocations/{id}/delete | only never-approved PENDING/CANCELLED physical delete |
| A09 | POST /allocations/{id}/revoke-sms-consent | OPTIONAL 26th proposed privacy route, clears phone/consent, no status effect |

Response DTO exact shapes:
```json
{"id":1,"name":"Logistics"}
{"id":2,"departmentId":1,"name":"Welcome Workshop","startAt":"2026-12-10T09:00:00","endAt":"2026-12-10T12:00:00"}
{"id":3,"name":"Extension cords","type":"REUSABLE","totalQuantity":10,"availableQuantity":6}
{"resourceId":3,"availableQuantity":6}
{"id":4,"projectId":2,"resourceId":3,"quantity":6,"allocatedQuantity":4,"shortageQuantity":2}
{"id":5,"requirementId":4,"quantity":4,"borrowerName":"Fictional representative","contactPresent":false,"smsConsent":false,"status":"PENDING","createdAt":"2026-11-13T11:00:00","approvedAt":null,"issuedAt":null,"returnedAt":null,"consumedAt":null,"cancelledAt":null,"cancellationReason":null,"expiredPendingCleanup":false,"reminderAttempted":false,"reminderOutcome":"NONE"}
```
Example dates in historic planning docs are ILLUSTRATIVE and can be in the past; test setup MUST derive future dates or use injected fixed clock. Do not send literal 2026-11-20 dates as if still future.

Shared status/error taxonomy:
400 VALIDATION_FAILED for malformed JSON, invalid IDs/enums/fields/quantities/client status; 400 INVALID_PROJECT_PERIOD for end<=start or changed start not future; 404 NOT_FOUND for required missing target/parent; 409 DUPLICATE_NAME, DUPLICATE_REQUIREMENT, DEPENDENCY_EXISTS, RESOURCE_NOT_EMPTY, INSUFFICIENT_STOCK, REQUIREMENT_ALREADY_COVERED, QUANTITY_BELOW_COMMITMENTS, STALE_RESOURCE_QUANTITY, INVALID_ALLOCATION_STATE, RESOURCE_TYPE_MISMATCH, OUTSIDE_PROJECT_PERIOD, PROJECT_ENDED, PROJECT_DATES_LOCKED, HISTORY_PROTECTED. 503 RETRY_REQUIRED only recognized transient lock contention/deadlocks; sanitize 500 INTERNAL_ERROR and never leak SQL, stacktrace or secrets. Client invalid input vs server-side validation failure must not be blindly conflated. Transaction refused -> unchanged DB.

## 7. Existing source quirks and conflicts
- O1 `sumCoveredQuantity` erroneously counts expired APPROVED; fix in current O3 batch.
- Proposal originally contained response phoneNumber, but reviewed privacy amendment redacts it; current code already uses contactPresent.
- Resource update original proposal lacked expectedTotalQuantity; reviewed optimistic expected-value guard adds it. Not a full version counter; ABA possible; state limitation.
- Historical original proposal may imply holds indefinitely; reviewed amendment expires effective APPROVED at Project end.
- Printed assignment spells external "RetroJit" but syllabus/course code use Retrofit/GSON; use actual source-era Retrofit/Gson, document interpretation.
- Jersey teaching slides have a preliminary GET/PUT mention; final assignment says GET reads and POST submits; preserve GET/POST.
- Intro lecture controller direct repo injection is NOT sufficient for final assignment; must inject Spring Component and delegate.
- Syllabus CLO9 images conflicts with proposal omission; resolve near release with evidence rather than silently claiming waived.
- Current status docs from 2026-11-13 are planning-era snapshots and may explicitly say "Java unimplemented", despite later O1/O3 code; current actual source/recent evidence outrank stale descriptive text.

## 8. Privacy/security/quality
Only loopback 127.0.0.1 by default; no real auth; label local fictional prototype. No credentials in repo or logs. Postman raw phone never in GET/response. No real students/phone numbers, Twilio live sends or fake authority. Focus on no overbooking, no double-deduct, stable transactional requests, predictable errors, persistent MySQL, JDK8 compatibility, responsive UI. No invented adoption, enterprise scale metrics, benchmarks, campus endorsement. Maintain real course-compatible API and tests.


---

# INCLUDED SOURCE 05/12: 04_PHASES_TESTS_RELEASE.md

# Full build roadmap, integration details, acceptance, performance and academic release

## Strategic rule
User does NOT want dozens of tiny agent tasks with repeated startup, audit, and prompts. Deliver **largest coherent fully testable group** per Codex coding cycle, with *internal* staged checks. Optimize total verified functionality per session/token rather than arbitrary small code slices. Do not overpack unrelated high-risk changes into one untested commit. New cycle sizes flexible based on dependencies, coverage, environment and real test evidence. Planned groupings after active O3:
1. O4 + O5 = transactional Allocation lifecycle + time/expiry scheduling.
2. O6 + O7 = real Retrofit/Gson FakeTwilio reminder integration + React/TypeScript/Vite foundation, separate modules/worktrees if safe.
3. O8–O10 = integrated functional UI (Departments, Projects, Resources, Requirements, Allocations, Dashboard, reminder activity).
4. O11–O14 = integrated test/fix, conditional image resolution, packaging, release and academic evidence.
These groupings are engineering cycles, **not** mandatory separate prompts per endpoint/page. Do not let stage labels force unnecessary rereads or sessions.

## O3 active/current (while user is awaiting Codex completion)
Completes P03/P04, R03/R04, Q03/Q04 and fixes expired APPROVED coverage. Target 19/25 original routes. Do not issue next prompt before verifying current Codex changes on remote branch, actual tests and current user intent. Full acceptance in file `05_O3_ACTIVE_BUILD_COMPLETION_GATE.md`.

## O4: transactional stock engine, six routes A03–A08, optional A09
Implement approve, issue reusable, return reusable, consume consumable, cancel, guarded delete, independent revoke consent. Lock order Project→Resource→Allocation; real InnoDB. Revalidate after lock; persisted protected history and atomic stock. No negative availability, duplicate consume, double issue deduction, partial usable return or phantom deletion. Test concurrency:
- two approvals 4+4 with stock5 -> exactly one success;
- two approvals 4+4 for requirement demand6 -> exactly one success;
- consumable q30 of T100: approval availability70/total100; consume T70/available70; second consume 409 and T70.
- reusable T10 approve4=>avail6, issue=>6, return=>10 while requirement covered4.
- Resource correction while consume & approval; Project-date edit while approval; failure rollback.
- Pending no hold, expiry effective hold0, historically approved deletion blocked.
- fixed-clock Project active interval, early approve allowed, late return allowed.
- test real SQL state not just mocked return JSON. Wrong lock order, stale persistence context, unexpected JPA refresh/JPQL syntax, and MySQL unique races are critical review points.

## O5: time and scheduling
@Inject Clock Asia/Manila; active [startAt,endAt). `@EnableScheduling` already at application class but no genuine job at baseline. Implement @Scheduled background Project expiry with bounded ID scan, small independent transactions per candidate; PENDING/APPROVED after end → CANCELLED(EXPIRED), retain historically approvedAt, never auto-return ISSUED, no physical stock double-release. Observe actual scheduled method invocation (test and local demo), not just call method manually. Fixed-clock tests at just-before, exact start/end and after; idempotent restart; logger avoids private data. Keep cron/interval configurable, normal hourly, optionally faster local demo profile.

## O6: Retrofit/Gson external communication and SMS reminders
Assignment "RetroJit" resolved to Retrofit/Gson by syllabus/course examples. Source FakeTwilio has POST `/fake_twilio/send`, `application/x-www-form-urlencoded`: fields `To`, `MessagingServiceSid`, `Body`; `Authorization` header (simulator may print it, so use **dummy only**). Course simulator often port 9997; check actually running configured port. Real HTTP response parsing with GsonConverterFactory and `@SerializedName("messaging_service_sid")` where needed. Simulator accepted HTTP may be 200, real API may be 201; treat observed response not hardcoded guarantee. DISABLED startup default, no external calls; FAKE_TWILIO mode with controlled fictional/dummy data; real provider TWILIO_VIRTUAL only after explicit authorization/account verification, no claims handset delivery.
Eligible reminder: REUSABLE Allocation status ISSUED, SMS consent true, valid stored phone, Project end in [now,now+24h), reminderClaimedAt null. Claim in short transaction with Allocation lock; commit durable CLAIMED; recheck eligibility/return/consent before dispatch; outbound HTTP OUTSIDE transaction; persist SUPPRESSED/SIMULATED/PROVIDER_ACCEPTED/REJECTED/UNKNOWN appropriately. Network timeout/unknown -> no automatic resend. Do not overwrite returned status from stale object. Revocation clears destination independently. No SMS for overdue, returned, consumed, cancelled, consent false. Two scheduler jobs expiry and SMS independent.

## O7: frontend foundation
Actual React + TypeScript strict + Vite, pinned npm lockfile, real typed API models, Vite proxy same `/api`, HashRouter for deployed no server-route conflict, responsive sidebar and mobile drawer, accessible interactions, shared tables/forms/dialogs, loading/empty/error/retry/confirmation states; no fake business data or a Node backend. Bundled build later into Java static resources. Do not choose unsupported Node/Vite version without checking current local versions; React tooling independent of Java8 build. No premature auth provider, SSR framework, Redux boilerplate, GraphQL or CSS bloat.

## O8–O10: integrated, real functional React UI
User explicitly requires real UI despite optional bonus in assignment; do not defer into fake wireframe.
Screens:
- Dashboard: real GET data; Departments count, Resource types/count not sum incomparable quantities, active/upcoming Projects, pending requests, issued/overdue batches, positive shortages, consumed batches; partial failed request shows unavailable not fake zero; cross-endpoint not atomic snapshot, include refresh.
- Departments: list, search, create, rename, guarded delete, 409 duplicate and dependencies.
- Projects: Department selector, PHT `datetime-local` strings not `toISOString` UTC shift, create/edit/filter, immutable department, historical-date lock, Requirements and Allocations linked list, safe delete.
- Resources: reusable/consumable, create, total/available, update with expectedTotalQuantity and stale 409 re-fetch/review not blind overwrite, zero/unreferenced delete.
- Requirements: select Project/Resource unique, qty, coverage/shortage, update pending/coverage bound, delete only no allocations.
- Allocations: create PENDING with fictional borrower, optional controlled consent, list filter, approve, issue/return/consume/cancel/delete/revoke consent exact valid action gating but server authoritative. Re-fetch related data after confirmed mutation; no auto repeat on ambiguous lost POST. Confirm whole usable batch return and irreversible full consume. Status/timestamps/reminder outcome redacted. No direct phone in read UI.
- Reminder Activity: derive due/overdue and outcome from existing Allocation/Project list APIs; no invented notification log or manual SMS endpoint.
- About: local fictional academic prototype no campus endorsement/auth/real SMS claim.
Quality: 375px mobile & desktop, keyboard focus, forms, dialog confirmation/focus restore, readable tables with overflow, no broken nav. Real browser E2E with live Java/MySQL; mocks only for isolated unit tests. No invented metrics.

## O11–O14: acceptance, images, export, academic release
O11 real full regression:
- 5 InnoDB tables, FKs/unique indexes, 25 original API paths + approved A09, DTO/TS parity, validation and sanitized errors, transaction rollback, concurrency and exact stock invariants; startup/restart and JDBC.
- Tests for @Scheduled time/expiry idempotence and real FakeTwilio HTTP/Gson parse.
- Postman collection covering every actual route with generated IDs, deterministic negative cases, not 68-route historical templates; run actual Newman. Browser E2E against real backend; responsive/keyboard, outage, persistence, no fake success.
- Secret scan, no real contact or unauthorized SMS, no broad public server/cors.
O12 CONDITIONAL syllabus CLO9 image handling: if authoritative final policy requires receive/serve images, add Resource.imageData (byte[]), imageMimeType nullable and POST/GET `/api/resources/{id}/image`, validate PNG/JPEG byte signature/mime/limit, store real DB blob and prove download byte equality and UI display. Exactly five entities still, 28 routes if original25+A09+2 images; don't invent instructor waiver.
O13 packaging: Java JDK8 Maven clean verify, MySQL, npm ci/test/lint/build, bundled React dist into Spring Boot static, actual executable JAR GET `/`, `/assets`, `/api/departments`, HashRouter reload. Eclipse source ZIP + full Java/React source ZIP, tested clean extraction, portable m2e metadata and actual Eclipse import if available (NOT_RUN if not), SHA256 manifest; no target,node_modules,secrets,class caches or copyrighted course archives copied without authorization.
O14 review assignment/syllabus/verified Canvas, proposal amendments vs 25 original operations, actual final academic video & permission where required, sole/actual group membership and peer review applicability, Certificate of Authorship and outside assistance honesty, actual source ZIP/Canvas receipt. Do not claim submission/acceptance without receipt.

## Complete baseline acceptance scenarios from canonical pack
Java/DB:
BE01 fresh start 5 tables; BE02 Dept201 persisted; BE03 GET Dept; BE04 duplicate normalization409; BE05 referenced Department delete409; BE06 invalid Project interval400; BE07 missing Department404; BE08 old-dates name-only Project edit; BE09 historical approval date edit409; BE10 zero stock resource; BE11 approve 4 of5 stock; BE12 parallel resource approvals 4+4 of5 exactly one; BE13 parallel requirement approvals 4+4 of6 exactly one; BE14 pending no hold; BE15 issue no double deduction; BE16 return releases custody; BE17 repeat return409; BE18 consume once; BE19 repeat consume409; BE20 reduce requirement below coverage409; BE21 pending9 under requirement10 reduce5->409; BE22 stale Resource update409; BE23 expired APPROVED no effective hold; BE24 expired PENDING cleanup; BE25 expired APPROVED cleanup no double release; BE26 overdue ISSUED still unavailable; BE27 approved-then-cancelled delete409; BE28 never-approved cancelled/pending delete works; BE29 positive stock Resource delete409; BE30 zero unreferenced Resource delete; BE31 duplicate Requirement pair409; BE32 forced consume failure rollback; BE33 empty aggregate zero; BE34 missing parent404/no save; BE35 malformed JSON400; BE36 generated ID/status client spoof refused/ignored; BE37 restart persistence; BE38 clean JDK8 import/build; BE39 parallel duplicate unique DB; BE40 Project date edit vs approval race; BE41 Resource total correction vs consume race; BE42 status/time-boundary JPQL coherence; BE43 multiquery snapshot disclaimers; BE44 lost POST response no blind repeat.
SMS/Scheduler: SMS01 disabled no credentials; SMS02 independent expiry; SMS03 exact window; SMS04 durable once-only claim; SMS05 return suppression; SMS06 consent revocation suppression; SMS07 return/revoke before HTTP send; SMS08 real fake simulator accept HTTP parsed; SMS09 definitive HTTP reject; SMS10 timeout UNKNOWN no resend; SMS11 restart no duplicate; SMS12 overdue no reminder; SMS13 accepted != handset delivered; SMS14 missing destination; SMS15 dummy Authorization/secret check; SMS16 response persistence not overwrite status; SMS17/18 expiry cleanup no stock double effects.
UI: UI01 genuine empty DB; UI02 real Department create; UI03 PHT round trip; UI04 Resources; UI05 Requirements; UI06 pending no hold; UI07 approve reserve; UI08 issue no double deduction; UI09 return release; UI10 consume once; UI11 stale resource409+refresh; UI12 safe delete; UI13 revoke; UI14 reload persistence; UI15 backend restart; UI16 backend outage no false success; UI17 keyboard; UI18 narrow mobile; UI19 real backend E2E; UI20 final JAR UI/API; UI21 partial dashboard errors not fake zeros; UI22 lost POST no auto retry; UI23 state/type action gating; UI24 table overflow/dialog focus.
These are TEST PLANS, not already executed. Only mark PASS on observed actual result.

## Efficient engineering workflow and acceptance gates
Per coherent coding cycle: start fresh remote + local Git audit; read current phase report and relevant code; inspect controlling source only as needed; identify actual completed delta. Implement grouped related features; focused tests during edits; end-of-cycle full relevant regression + actual DB/HTTP (and browser/provider when relevant). Never rerun expensive Maven/Node full checks after trivial edits; never use unexecuted mock as MySQL/browser proof. Use compact report with actual commands, environment, failed/warnings, changed files, current route count, risk/remaining gaps, remote commit/PR. Commit and PUSH legitimate task-related changes to feature branch; verify remote SHA, do not merge main by default.

Performance: one Java backend, one MySQL, one React app; no extra services without proven necessity. Correct concurrent transactions, indexed bounded joins/queries, avoid N+1, small JSON DTOs, no derived persisted quantities, no fake caching; optimize demonstrated bottlenecks rather than invented benchmarks. Connection pool already configured, no tuning without evidence. Minimize dependencies, disk artifacts, logs, repeated ZIPs and build caches, but do not omit actual tests/security. General-release soft scope cuts: cosmetics/animations and extra analytics are first to defer; mandatory course backend, Retrofit/scheduling, accurate persistence and user-mandatory functional UI cannot silently vanish.

## Existing demo and submission status
User said O2 video recording DONE; exact file, Canvas submission status unverified here. Avoid unnecessary repeat. Printed final Dec 5, current Canvas may change. Final deliverables include full source, Eclipse-compatible source, real Postman, functional application and genuine final video; instructor-visible permission for externally shared video if applicable. Honest authorship and outside help disclosure.


---

# INCLUDED SOURCE 06/12: 05_O3_ACTIVE_BUILD_COMPLETION_GATE.md

# Exact O3 in-progress build: preconditions, implementation tasks, acceptance and handoff procedure

## Critical status boundary
When this continuity pack was requested, user wrote: **“okay actually right now, the prev prompt is still being built, after that finishes, I want to transfer to a new conversation here in this project and continue there what we are doing here right now.”** The preceding prompt is the optimized complete-O3 Codex implementation prompt authored in the conversation. Its work is **IN PROGRESS**, not reported finished. No new O3 remote branch/commit was visible when this pack's GitHub audit checked main at 16e781c. Another agent may push during/after this pack creation. Never assume the current observed 13/25 route count remains correct once Codex finishes. The next conversation must first refresh all remote state and inspect new code/tests/diff.

## O3 objective
Finish 6 missing CRUD method/path pairs plus expired APPROVED coverage correction:
- P03 Project POST /api/projects/{id}/update.
- P04 Project POST /api/projects/{id}/delete.
- R03 Resource POST /api/resources/{id}/update.
- R04 Resource POST /api/resources/{id}/delete.
- Q03 Requirement POST /api/requirements/{id}/update.
- Q04 Requirement POST /api/requirements/{id}/delete.
- Correct `AllocationRepository.sumCoveredQuantity` / `RequirementComponent.view`: APPROVED expired at Project end not covered; ISSUED/RETURNED/CONSUMED historical coverage persists; PENDING/CANCELLED excluded.
- Expected result after actual code/tests: original routes 13+6=19/25; remaining A03–A08 six original routes. A09 proposed 26th still separate, not part of original count.

## Authority and source reading protocol
Inspect git status/remote/local branches/PRs before edits, then `AGENTS.md`, `evidence/O3/ROUTE_PARITY.md`, `evidence/O3/PHASE_REPORT.md`, `backend/README.md`; read focused docs/complete-handoff 04/05/06/09 sections and only relevant source. Do not repeatedly reread duplicate 188k master and 2MB course corpus. Original controlling PDF/DOCX as needed for changed API behavior. Older nine-entity 68-route contracts never source of truth. User wants actual code, not a new plan.

## Exact Project implementation
P03 body `{"name":"Updated Workshop","startAt":"<future Philippine local datetime>","endAt":"<later Philippine local datetime>"}`. Preserve existing Project DTO `{id,departmentId,name,startAt,endAt}`. Positive path id; 404 missing; name normalized 1–100; dates nonnull end>start. If either proposed date differs from existing, new start must be future; if dates unchanged, allow only name change even when original Project dates already past. Do not change Department ID. Reject date changes when **any Allocation under any Requirement of Project has nonnull `approvedAt`**, even if status CANCELLED; use aggregate historical count, not currently approved count. Error 409 PROJECT_DATES_LOCKED. Ensure actual transaction/lock ordering is safe against concurrent approval; tests for Project edit vs approval race ideally in O4 if approval not yet implemented.
P04: 404 target missing; 409 DEPENDENCY_EXISTS if ANY ResourceRequirement references Project; physically remove only unreferenced; no cascade/history deletion; 200 `{"message":"Record deleted."}`.

Inspect actual `Project.java`, `ProjectController.java`, `ProjectComponent.java`, `ProjectRepository.java`, `ResourceRequirementRepository.java`, `AllocationRepository.java`, `ApiDtos.java`, `ApiExceptionMapper.java`, and test fixtures. Do not add unused DTO fields/routes. Existing P01/P02 must remain unchanged and pass regressions.

## Exact Resource implementation
R03 body `{"name":"A4 paper sheets","totalQuantity":120,"expectedTotalQuantity":70}`. `expectedTotalQuantity` required numeric 0–1e6; check stored total under Resource pessimistic lock. Mismatch => 409 STALE_RESOURCE_QUANTITY, no mutation. Name 1–100 normalized, database named unique constraint handles racing duplicates with controlled 409 DUPLICATE_NAME. Resource type immutable, not in request. Current commitments H/I calculated under safe lock and consistent clock:
REUSABLE newTotal >= H+I; CONSUMABLE newTotal >= H. Update must not silently reduce below obligations. Database authoritative and transactional. Return established Resource DTO with recomputed available (not persist derived fields). Duplicate check under concurrent requests backed by unique constraint.
R04: 404 absent, 409 DEPENDENCY_EXISTS for any Requirement references; 409 RESOURCE_NOT_EMPTY if totalQuantity>0, even if no references; only zero-stock unreferenced physical delete; no cascade.

## Exact Requirement implementation
Q03 body `{"quantity":8}`, positive id, missing 404, qty 1–1e6, Project and Resource FKs immutable. Project end guard -> 409 PROJECT_ENDED. Compute current effective coverage from correct expiry-aware JPQL and `MAX` largest one PENDING quantity. New required >= max(covered,maxPending); do NOT sum all pending. If violated 409 QUANTITY_BELOW_COMMITMENTS and original row unchanged. Use Project→Resource lock when required for concurrency with Approval/Requirement races; revalidate current values. Return existing `RequirementView` with current allocated and shortage.
Q04: 404 missing, 409 DEPENDENCY_EXISTS if any Allocation historical reference; physically delete only when no Allocation; preserve FK integrity.

## Exact coverage correction
Current `AllocationRepository.sumCoveredQuantity(requirementId,statuses)` includes expired APPROVED, unlike `sumEffectiveHolds(resourceId,approved,now)`. Replace with safe JPQL equivalent:
```java
@Query("SELECT SUM(a.quantity) FROM Allocation a "
 + "WHERE a.requirement.id = :requirementId "
 + "AND (a.status IN :completed "
 + "OR (a.status = :approved AND a.requirement.project.endAt > :now))")
Long sumCovered(
  @Param("requirementId") Long requirementId,
  @Param("completed") Collection<AllocationStatus> completed,  // ISSUED,RETURNED,CONSUMED
  @Param("approved") AllocationStatus approved,
  @Param("now") LocalDateTime now);
```
Actual name can differ; preserve compatibility and correct callers. Validate JPQL compiles/executes on Spring Boot 2.1/Hibernate/MySQL and no empty collection; SUM null->0; Clock Asia/Manila. At exactly endAt APPROVED no longer coverage, ISSUED etc remain. Shortage max(0,Q-Covered). Do NOT implement expiry scheduled cleanup here; that is O5. Do NOT claim query fixed from static text only.

## Minimal full acceptance matrix for O3
P03: permitted rename; future date edit; same historical dates name-only; bad dates/name/id; missing Project 404; historical approvedAt prevents date edit; test DB unchanged on refusal.
P04: dependent Requirement 409 and row retained; unreferenced deletion 200 and row absent; missing 404.
R03: valid name+qty; duplicate normalizer/collation 409; no implicit type mutation; negative/>1e6 400; stale expected 409 unchanged; total below H/I blocked; revised availability correct; read after commit consistent.
R04: nonzero stock 409; referenced even zero stock 409; zero unreferenced delete; missing.
Q03: valid, below 1/>1e6, after Project end, below covered, below largest live pending, unrelated PENDING no sum; rejected state unchanged.
Q04: any Allocation including CANCELLED/RETURNED history blocks; no refs permits; missing.
COVERAGE: pre-end approved counted, exact-end approved not counted, complete statuses still count, pending/cancelled zero, aggregate empty zero, shortage correct, time fixed.
REGRESSION: Java8 Maven tests/clean package; O1 and D03 previous Newman tests; new HTTP/Postman dynamic generated IDs; live MySQL if accessible; no invented success if remote environment cannot connect local 3308; no H2 substitution as real MySQL.
SECURITY: secrets protected, raw phone not exposed, controller->component separation, errors JSON correct, no destructive local DB reset or dropping tables.
GIT: feature branch current remote+local audit; safe task-related commits pushed, remote SHA verified, PR created/updated as appropriate, **no automatic merge to main**; all explicit user uncommitted data preserved.

## Files of special interest
Backend `src/main/java/app/{components/ProjectComponent,components/ResourceComponent,components/RequirementComponent,components/AllocationComponent,config/ApiExceptionMapper,dto/ApiDtos,entities/Project,entities/Resource,entities/ResourceRequirement,entities/Allocation,repositories/ProjectRepository,repositories/ResourceRepository,repositories/ResourceRequirementRepository,repositories/AllocationRepository,rest/controllers/ProjectController,rest/controllers/ResourceController,rest/controllers/RequirementController}.java`.
Existing two tests; new tests under `backend/src/test/java`.
Actual Java project lives under repo `backend/` so adapt path.
Evidence: `evidence/O3/ROUTE_PARITY.md`, `evidence/O3/PHASE_REPORT.md`, new non-destructive test reports; `postman/OrgResource-O1-Skeleton.postman_collection.json` and `postman/OrgResource-O3-Department-Update.postman_collection.json`. Avoid overwriting O1 historical reports.

## Exactly how new conversation should judge O3 Codex response
1. Ask nothing if GitHub results contain the needed new branch, commits and tests. Read newest remote O3 report and diff.
2. Determine `main` vs active feature branch vs old demo branch. A newly pushed feature branch can have O3 even if main still shows 13/25. Don't mistakenly say no work solely from main.
3. Identify exact endpoints from real `@Path`+`@POST/@GET` source; route table alone not enough.
4. Inspect Java code for silent errors/lock order, destructive deletes, transaction semantics, coverage/time, error mapping, schema validity.
5. Evaluate test evidence by type (unit, real MySQL, live HTTP/Newman, concurrency); count PASS/FAIL/NOT_RUN/BLOCKED separately. Verify recorded statuses from report/JSON but don't pretend rerun local app.
6. Identify outstanding bugs. Return an O3 gate decision: PASS (code and required checks verified); CONDITIONAL if code passes subset but real DB/browser absent; FAIL if correctness flaw; BLOCKED if missing tools.
7. On PASS move to the larger O4+O5 combined prompt. If not PASS, produce ONE cohesive targeted repair prompt, then reassess. Do not fracture six CRUD endpoints into six repetitive sessions unless a real blocker requires it.
8. Check that Codex **pushed** the actual code and evidence; if missing, instruct push feature branch and verify SHA/PR.
9. Do not treat user-reported O2 video as a GitHub source file or Canvas submission receipt.

## User's optimized O3 prompt was previously delivered
It contained fixed architecture, 17 execution rules, exact file read order, performance/storage requirements, O3 Task A..G, focused tests and one end-of-batch regression; promised current 19/25 if passed. This file preserves the essential behavioral contract. Any slight variation in task sequencing is acceptable if coverage and tests are complete.


---

# INCLUDED SOURCE 07/12: 06_TOKEN_PERFORMANCE_PUSH_POLICY.md

# Permanent user execution preferences — token-efficient, substantial builds and push-every-cycle

This is the authoritative standing policy to embed in EVERY *future* Codex prompt after O3. This policy was explicitly clarified after the earlier long O3 coding prompt was already running.

## Key user instructions (verbatim excerpts)
1. “note that I am using Luna 6 Model at extra high reasoning mode to build all these which is a lower model with lower capabilities and reasoning power so EVERYTHING in the prompt has to be VERY SPECIFIC. but still, I WANT TO BE VERY TOKEN EFFICIENT ON BUILDING ALL THESE, Detailed, thorough, precise, accurate, complete but not overdoing it, overcomplicating or going overboard. I also want it to be very fast, scalable, and storage efficient in terms of the actual build and the actual software. Also, again, VERY TOKEN EFFICIENT IN THE BUILD WITH NO WASTAGE.”
2. “okay do that for this slice but for the next ones, I WANT TO BALANCE BIG BUILD AND TOKEN EFFICIENCY. Goal still is to be full maximum token efficiency but sometimes u tend to overscope it down to specific tasks to the point that it takes way too long to build which costs more cause there's so many complications. also always audit github repo before doing anything then tell in the prompt to push everything always.”
3. “okay actually right now, the prev prompt is still being built, after that finishes, I want to transfer to a new conversation here in this project and continue there what we are doing here right now.”

Do NOT assert anything independent about the named model's capabilities. Translate the requested care into **deterministic contracts, exact paths, test matrices, state transitions and failure paths**, not long meandering commentary.

## Primary optimization objective
Maximize: **correct, tested, remotely preserved, functional software produced per total agent tokens / tool calls / elapsed sessions**.

Optimize TOTAL COST, not only shortest prompt length. A 200-word prompt causing 10 rounds of follow-ups, 10 repeated audits, 10 builds and 10 missing edge cases is less efficient than one well-specified, coherent phase. Conversely, a 30k-token prompt rehashing all course files and specifying 14 unrelated cycles in one shot is also waste.

## How to size tasks
Select the **largest dependency-coherent implementation batch** that can be coded, tested and debugged with bounded context in the current coding session. Preferred after O3: O4+O5 as one cycle; O6+O7 coordinated; O8–O10 functional UI; O11–O14 QA/release. Internally checkpoint real tests after logical subcomponents but do not stop after one route/page and force expensive new prompts unless necessary. Isolate high-risk concurrency and external-provider steps within same batch with acceptance gates; don't merge unrelated infrastructure. Agent can make multiple local commits as needed but one pushed remote branch/PR per coherent cycle (or sensible stable subcommits).

## Required beginning of EVERY future coding prompt — audit
1. GitHub connected remote repo `newonathe/OrgResource` recent main/feature commits, branches, open/merged PRs, CI runs, files/diff, issues if relevant. **Fresh** audit, not earlier snapshot.
2. Local agent git status, HEAD, current branch, working tree, remotes, uncommitted work, merge bases. Do not lose local user changes.
3. Identify latest implemented route count, exact code and reports; previous phase status, failing tests, missing source/DB setup.
4. Read relevant source/primary authority and latest phase report, not all 188k+ files repeatedly.
5. One short precise scoped plan, then write actual code. Do not stop at planning.

## Token-saving engineering protocol
- Read `AGENTS.md`, latest relevant evidence/route parity, compact source sections once per session. Read duplicated master only one location, targeted headings only after initial complete architecture grounding. Inspect actual file being changed.
- Avoid repeated full repo scans unless Git changed, repeated full Maven/npm builds on every edit, duplicate files, duplicate docs, reprinting huge logs or rewriting successful modules.
- Batch similar endpoints/tests/validation while sharing setup and fixtures. Targeted unit/integration tests during development; one appropriate full regression at cycle gate.
- No generic "give me permission" checks for safe routine local changes; no fictional background execution. Complete unblocked work in this response/session. Stop for real blocker or unsafe destructive action only.
- Do not use many agents/worktrees for trivial independent modules; parallelize only when ownership strongly separated and integration benefit outweighs overhead.
- Prefer direct code reading and existing tests to broad speculative web browsing. Use updated official sources only where API/provider/security versions actually matter.
- Report concise meaningful verdict: starting HEAD, changed files, features/routes, exact commands/results, failures, confirmed pushed SHA/PR, next cycle. Avoid copy-pasting giant planning document in each final report.
- Use exact data and interface contracts including malformed input/empty/404/409, concurrency, locks, PHT, and state machine to reduce low-capability coding mistakes.

## Software speed / scalability / storage
- One Java8/Jersey/SpringBoot app + one MySQL InnoDB + one React/Vite client. No microservices, Redis, Kafka, managed SaaS, SSR, second Node API or additional database.
- Correct reservation/coverage/total values before clever caching. One authoritative DB; narrow JSON DTOs, no phone responses. Persist only essential fields, compute derived quantities on demand.
- Preserve DB constraints and appropriate indexes for actual predicates; avoid redundant indexes, N+1 list queries, unbounded scans, needless object graphs, repeated query phases inside transactions.
- Use short DB transactions with correct lock order; no remote HTTP while DB lock. Avoid double-commit, failure partial state, blind network-retry writes.
- Use bound batch sizes in schedulers; no immediate need to tune default pool of five without measurements. No invented performance SLA, benchmark, traffic or dataset.
- Frontend: strict typed API, efficient requests, reusable simple components, loading/error/empty, no unnecessary CSS/UI libraries or animations, mobile/desktop/keyboard. Actual user-perceived responsiveness with real data.
- Storage: .gitignore node_modules/target/cache/dumps/secrets/logs, no repetitive source ZIP packaging every implementation cycle; generate final exports at release and preserve necessary historical evidence.
- Portability: Java8/Eclipse-compatible source export, and pinned Node/Vite dependencies at actual UI phase.

## Mandatory push at end of EACH cycle
User explicitly requested “push everything always” and the assistant interpreted as automatic push to **feature branch**. Therefore future Codex prompts MUST authorize/require:
1. Review all git changes and stage **all legitimate task-related** code/tests/config/docs/Postman/test-evidence. Preserve user unrelated/untracked files.
2. Exclude passwords, .env, DPAPI files, private phone/student data, node_modules, target, DB dumps, caches, raw course ZIPs unauthorized for repo.
3. Execute appropriate checks; label failed/unrun honestly.
4. Commit descriptively; push to remote feature branch, verify actual remote SHA and branch, open/update PR if available.
5. For partial/blocking work, push safe WIP to clearly labeled feature branch with truthful status; no fake acceptance.
6. Do NOT auto-merge into `main`, force-push, reset/discard existing work, publish repo, change Git history destructively, delete/reset MySQL, submit Canvas, or send real SMS without separate authorization.
7. If remote auth fails, report actual failure and preserved local state, not “pushed successfully”.
8. Initial GitHub remote audit in ChatGPT planning and in Codex coding environment separately; the first does not replace the second.

## Integration and agent roles
ChatGPT (this Project) = project supervisor and connected GitHub read-only verifier/prompt author. Codex (user runs separately) = code editor. Avoid claiming code has been changed from a planning response. If user opts for another coding agent, avoid two writers to same files/worktree. Review/merge is separate human gate. Git pushes are intended to preserve code, not auto-accept it.

## Output form for next large-cycle prompt
Use a tightly organized single Codex prompt:
- Scope and starting audit.
- Authority & minimal read paths.
- Exact endpoints/events/fields/state transitions.
- Precise algorithms/locks/DB queries/errors.
- Grouped technical tasks with dependencies.
- Shared performance/storage constraints.
- Concrete test matrix (positive/negative/concurrency/rollback/real HTTP).
- Files or package boundaries; no unnecessary additional architecture.
- Evidence and Git commit/push/PR.
- Done/partial/blocked decision gate and continuation.
Avoid huge duplication of root master; point to exact docs. Avoid overly narrow per-method sessions. Larger testable group accepted if all required gates pass.

## Next likely prompt
After O3 passes, deliver **O4+O5 combined transactional allocation lifecycle AND expiry scheduler implementation**. O6+O7 then, etc. If O3 fails, one scoped repair batch; no entire app rewrite. Ensure new prompt always starts with live GitHub audit and ends with automatic feature-branch push mandate.


---

# INCLUDED SOURCE 08/12: 07_NEW_CHAT_COPY_READY_PROMPT.md

# COPY ALL THE TEXT BELOW AS THE FIRST USER MESSAGE IN THE NEXT CHATGPT PROJECT CONVERSATION

You are continuing the **OrgResource CSCI 180.05** project from a previous long conversation, not starting a new project.

I deliberately waited for an existing Codex O3 coding prompt to finish before moving to this new conversation. The previous ChatGPT conversation produced a comprehensive continuity pack and committed it to a SEPARATE documentation branch of my private GitHub repo:

Repository: https://github.com/newonathe/OrgResource
Continuity branch: `docs/conversation-continuity-2026-11-14`
Continuity directory: `docs/conversation-handoff-2026-11-14/`

Read ALL twelve numbered continuity files under that directory (00–11), OR read the losslessly concatenated `MASTER_CONTINUITY_UNABRIDGED.md` plus the live repository sources, including their detailed chronology, technical contracts, domain and API, repo/evidence status, current O3 build scope, standing performance/token/push policy, source index, and checklists. The directory is committed to the GitHub feature branch, not necessarily merged to main. Use the connected GitHub account `newonathe` to read files by explicit branch ref. The files are:
- 00_READ_THIS_FIRST.md
- 01_CONVERSATION_DECISIONS_AND_HISTORY.md
- 02_GITHUB_SNAPSHOT_AND_EVIDENCE.md
- 03_COURSE_DOMAIN_API_CONTRACT.md
- 04_PHASES_TESTS_RELEASE.md
- 05_O3_ACTIVE_BUILD_COMPLETION_GATE.md
- 06_TOKEN_PERFORMANCE_PUSH_POLICY.md
- 07_NEW_CHAT_COPY_READY_PROMPT.md (this prompt)
- 08_SOURCE_DIRECTORY_EVIDENCE_REGISTER.md
- 09_CONTINUATION_CHECKLIST_AND_DECISION_LOG.md
- 10_O4_O5_COMBINED_CYCLE_SEED.md
- 11_O2_VIDEO_DEMO_RUNBOOK_HISTORY.md

These files preserve the previous conversation's substantive intent. **Do not assume the dated GitHub snapshot is current.**

Then read the current repository's actual source of truth and controlling files, using the most recent appropriate ref:
`AGENTS.md`;
`ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf`;
`ChatGPT-Project-Pack/UPLOAD/08_CSCI180_SYLLABUS.pdf`;
`ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx`;
`docs/ORGRESOURCE_MASTER_HANDOFF.md` (the full 188,934-byte canonical planning packet; do NOT unnecessarily reread the byte-duplicate `docs/complete-handoff/ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md`);
focused `docs/complete-handoff/04_DATA_MODEL_AND_INVARIANTS.md`, `05_API_26_ROUTES_AND_RESPONSES.md`, `06_PERSISTENCE_TRANSACTIONS_JPQL.md`, `07_RETROFIT_SCHEDULING_SECURITY.md`, `08_UI_REACT_TYPESCRIPT.md`, `09_TEST_POSTMAN_DEMO_ACCEPTANCE.md`, `10_GITHUB_DEVOPS_BACKLOG_RELEASE.md`, and the actual backend source, Postman collections, and latest evidence.

**MANDATORY FIRST ACTION:** Freshly audit my **live** connected private GitHub `newonathe/OrgResource`: latest main commit, all relevant feature branches (including the in-progress O3 Codex output), recent commit history, open PRs (including stale O2 draft PR #1), CI/workflow results, exact changed files, route parity, latest evidence, test outputs, and any unmerged code. Do not only look at main; Codex was required to push feature branches. Distinguish GitHub remote code from local uncommitted work, which you cannot see from GitHub. If a local Codex task completed but did not push, explain what evidence is missing and give a targeted push/inspection instruction.

**WHAT HAPPENED BEFORE THIS CHAT:** The initial Java8 SpringBoot2.1/Jersey/Javax/JPA/Hibernate/MySQL backend skeleton genuinely existed with five JPA entity types and 12 original routes. O3 D03 Department update was added, making 13/25; saved evidence showed eight Java unit tests, actual MySQL and Newman successes (15/29 O1 regression, 11/19 D03). A code defect was identified: expired APPROVED allocations counted by Requirement coverage though no longer stock holds. Latest independently audited main at handoff time was `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`. The user recorded the Nov14 skeleton demo using OBS/VSCode and explicitly reported **VIDEO DONE**; don't treat older `VIDEO_NOT_CREATED.md` as current truth and do not claim Canvas submission without receipt.

**THE CURRENT IN-PROGRESS TASK THAT MUST BE REVIEWED:** I was running the optimized O3 Codex master prompt to implement Project P03/P04 update/delete, Resource R03/R04 update/delete (with expectedTotalQuantity), Requirement Q03/Q04 update/delete (coverage and largest individual pending guard), and fix expired APPROVED requirement coverage JPQL/time semantics, with real tests, regression, safe data changes and a feature-branch push. Expected original route count on success: 19/25, but this is a TARGET, not a verified result. Use the actual latest commits and results to decide whether it passed. Review code at method/query/transaction/test level, not merely README claims. Identify remaining defects or unrun checks honestly.

**WHAT I WANT FROM THIS NEW CHAT:** Continue our engineering planning, GitHub audits, Codex prompts, and verification seamlessly. First report the fresh O3 completion audit with objective gate PASS/CONDITIONALLY_READY/FAIL/BLOCKED, exact route count, relevant Java changes, unit/MySQL/HTTP evidence and deficits, actual pushed commit/branch/PR. If O3 is correct and adequately tested, prepare the next extremely specific but *token-efficient and substantial* combined **O4 + O5 Allocation lifecycle + expiry/scheduling** Codex implementation prompt, ready for copy/paste. If O3 needs repair, create one coherent targeted repair prompt instead of splitting every function into separate sessions. Do NOT implement code merely by inventing a new plan. Future phases group O6+O7, O8–O10, O11–O14 flexibly by dependency.

**ABSOLUTE STANDING USER PREFERENCES:**
- Every new cycle: you FIRST audit GitHub using the connector. The Codex prompt must ALSO require Codex to audit local/remote git before edits.
- Every coding cycle: Codex MUST stage, commit and **PUSH all legitimate task-related changes automatically to a safe feature branch**, verify remote SHA, and create/update PR if available. No auto-merge main, force-push, discarding user changes, public posting, actual Canvas submission, live SMS, credential changes or MySQL reset without authorization.
- I use a lower-capability coding model at Extra High reasoning (as I described it), so prompts must be explicit: exact files, methods, inputs/outputs, statuses, dates, SQL/JPQL, locks, concurrency, error handling, tests, evidence and acceptance. Do not assume coding agent infers requirements correctly.
- But **maximize TOTAL efficiency**: largest coherent tested build per coding session/token, not excessively tiny slices. Avoid repetitive master rereads, unnecessary tool calls, duplicate source/docs, dozens of micro-prompts, speculative research and overengineering. Use complete grouped scope with internal checkpoints.
- Product must be fast, appropriately scalable, storage-efficient, understandable and simple: one Java backend, one MySQL DB, one real React/TypeScript/Vite UI. No fashionable unnecessary infrastructure. No invented benchmarks or user volumes.
- Source-first: course assignment/syllabus/current verified Canvas, five-entity 12-page proposal, original course examples and current code. 9-entity/68-route materials are historical. Exactly 5 entities and 25 original method/path routes, separately track A09 privacy addition.
- All claims must distinguish **PLAN vs CODE PRESENT vs TEST EXECUTED vs USER REPORTED vs UNVERIFIED**. No fabricated authorship, policy, tests, video, deployment, business need, supplier/provider approval or student data. Video transcript corpus is AUDIO only.
- Be academically compliant: Java8, Jersey REST/JSON, Spring @Autowired components, JPA/SpringData, Javax Validation, Retrofit/Gson, @Scheduled, MySQL, Postman, Eclipse-compatible source. Final real React UI is a USER DECISION despite optional course bonus. Syllabus CLO9 images remains a conditional open question.
- Don't ask repetitive clarification when sources and code suffice. Give direct actionable implementation and thoughtful independent critique. Do not generate bloated prose when specific contracts plus test gates suffice.

**DELIVERABLE FROM YOU NOW:** After the fresh GitHub audit, state exactly what O3 finished, what is verified, what is broken or still unverified, what changed relative to the old snapshot, then give the recommended next implementation prompt with a strict but efficient acceptance/push contract. If Codex is still running, give an exact check/status and a safe continuation procedure rather than guessing. Do not automatically merge or claim the final application is done.

IMPORTANT: This message is the new-conversation starter. The complete technical detail is in the committed continuity files and canonical master handoff. Use them as literal evidence, not as permission to skip fresh code/source inspection.


---

# INCLUDED SOURCE 09/12: 08_SOURCE_DIRECTORY_EVIDENCE_REGISTER.md

# Exact source directory, file inventory, evidence provenance and historical artifact register

## 1. User-supplied primary Project files available in ChatGPT Project (or GitHub mirrored reading layer)
- `01_KICKOFF_PROMPT.md` — original kickoff and goals; also GitHub `ChatGPT-Project-Pack/UPLOAD/01_KICKOFF_PROMPT.md` where present.
- `02_MASTER_CONTEXT_DOSSIER.md` — consolidated history, notes, source map; GitHub `ChatGPT-Project-Pack/UPLOAD/02_MASTER_CONTEXT_DOSSIER.md` where present.
- `03_COURSE_REFERENCE_CORPUS.txt` — ~2MB searchable extracted original course source/code/slides and ZIP entries, GitHub `ChatGPT-Project-Pack/UPLOAD/03_COURSE_REFERENCE_CORPUS.txt`. This is the search layer, not full proof every original visual or binary was inspected.
- `04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md` — extensive historical research, manifest, earlier designs, GitHub `ChatGPT-Project-Pack/UPLOAD/04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md`. History not binding.
- `06_VIDEO_TRANSCRIPTS.md` — ~470KB timestamped AUDIO transcription only, GitHub `ChatGPT-Project-Pack/UPLOAD/06_VIDEO_TRANSCRIPTS.md`. No video frames/visuals.
- `08_CSCI180_SYLLABUS.pdf` — nine-page syllabus, GitHub `ChatGPT-Project-Pack/UPLOAD/08_CSCI180_SYLLABUS.pdf`, mirror `sources/CS-DISCS-CSCI180.05-JONGKO_J-O-2026-1.pdf`.
- `09_ESP_PROJECT_ASSIGNMENT.pdf` — three-page controlling assignment, GitHub `ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf`, mirror `sources/ESP Project Proposal 2026.pdf`.
- `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` — 12-page five-entity original 25-route scope, GitHub `ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx`, mirror `OrgResource-Concise-Submission-Proposal.docx` and .md.
- `11_COMPLETE_SOURCE_NONVIDEO_PART1.zip` and `12_COMPLETE_SOURCE_NONVIDEO_PART2.zip` — full original non-video filesystem archive partitions; provided within ChatGPT Project/uploads, NOT necessarily committed in GitHub. Their presence does not prove every archive entry byte inspected. Do not ask to reupload videos by default, do not request unnecessary full archive upload to Codex if course corpus suffices.
- Course original slides: `0 - Installation.ppt`, `1 - Java and Eclipse.ppt`, `2 - Java Spring Annotations 2020.ppt`, `3 - Jersey 2020.ppt`, `6 - ORMHibernate 2020.ppt`, `7 - JPA Repository Magic Query and JPQL.ppt`, `8 -Springboot + JSON.ppt`, `10 - Inter-Service communication.ppt`, `10 - Retrofit ESP.ppt`, `Misc JPA -- Cardinality.pptx`, `Misc JPA --Validation.pptx`, `Twilio SMS Notification Setup.pptx`, plus `spring-data-jpa-reference.pdf`, `javax.validation.pdf`. Access within project/raw course ZIP as appropriate; no claim every figure/text in all files fully reviewed. Actual original labs include calculator, Animal Sighting, Jersey demo, Hibernate/JPA, Retrofit FakeTwilio, scheduling, image examples. Check actual snippets before changing Java interfaces/dependency versions.
- `COA_ProjectProposal_EthanOwenTaruc.pdf` and academic deliverables/proposal drafts in repo: personal submission evidence, not proof of current final submission. Avoid exposing personal identifiers unnecessarily.

## 2. Canonical repo planning sources (existing on main, not created by this conversation pack)
- `AGENTS.md` — project rules, exact five entities, course stack and Git/testing constraints. Note it disallows unauthorized push by default; LATER user explicitly authorized **safe feature-branch automatic pushes for all future Codex tasks**, while main merges/destructive actions remain prohibited. When preparing Codex prompt, explicitly state user's newer authorization and ask Codex to respect branch boundaries.
- `docs/ORGRESOURCE_MASTER_HANDOFF.md` — 188,934-byte consolidated planning packet, ~20k words, canonical.
- `docs/complete-handoff/ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md` — byte-identical copy at last audit; don't waste tokens reading both.
- `docs/complete-handoff/00_START_HERE.md` through `11_SOURCE_AUDIT_DECISIONS_RISKS.md` — source hierarchy, architecture, data, 26 route contract, JPQL/transaction, Retrofit/scheduling/security, React UI, Postman/acceptance, Git/DevOps/backlog, risks/decisions.
- `docs/complete-handoff/PROMPTS/O0_TO_O6_BACKEND.md`, `PROMPTS/O7_TO_O14_UI_TEST_RELEASE.md`, `PROMPTS/REVIEW_AND_REPAIR.md`, `PROMPTS/00_FIRST_PROMPT.md`; legacy generic stage instructions, superseded by user's later **larger-cycle grouping** and automatic push preference. Reuse technical content without forcing old tiny session sizes.
- `docs/complete-handoff/MATRICES/ALL_26_ROUTES.csv` and `ACCEPTANCE_TESTS.csv`; original 25 + optional A09, test plans not execution evidence.
- `docs/complete-handoff/postman/OrgResource-UNEXECUTED-26-Route-Template.postman_collection.json`; explicitly template, NOT proof.
- `contracts/domain-model.json`, `contracts/operations.json`, `postman/OrgResource-API.postman_collection.json`, `postman/OrgResource-Demo.postman_collection.json` — **older nine-entity/68-operation historical generated scope**. Do NOT use as current backend truth.
- `OrgResource-Formal-Proposal.docx`, `OrgResource-Specification-Package.zip`, root numbered `01-SOURCE-AND-REQUIREMENTS.md` through `10-IMPLEMENTATION-HANDOFF.md`: historical expansive research/docs with possible placeholder members, not selected current 12-page proposal. Don't invent assignment submission.
- `OrgResource-Concise-Submission-Proposal.md` and docx: current 5-entity proposal, still differentiate original 25 vs handoff amendments.

## 3. Actual backend code at snapshot
All under `backend/`:
`pom.xml`, Maven wrappers `mvnw`/`mvnw.cmd`/`.mvn/wrapper/*`, portable Eclipse metadata.
`src/main/java/app/SpringBootApp.java`
`src/main/java/app/config/{JerseyConfig,ClockConfiguration,ApiExceptionMapper}.java`
`src/main/java/app/entities/{Department,Project,Resource,ResourceRequirement,Allocation,ResourceType,AllocationStatus,CancellationReason,ReminderOutcome}.java`
`src/main/java/app/repositories/{DepartmentRepository,ProjectRepository,ResourceRepository,ResourceRequirementRepository,AllocationRepository}.java`
`src/main/java/app/components/{DepartmentComponent,ProjectComponent,ResourceComponent,RequirementComponent,AllocationComponent,NameNormalizer,RequestValidation,ApiException}.java`
`src/main/java/app/rest/controllers/{DepartmentController,ProjectController,ResourceController,RequirementController,AllocationController}.java`
`src/main/java/app/dto/ApiDtos.java`
`src/main/resources/application.properties`
`src/test/java/app/components/DepartmentComponentTest.java`
`src/test/java/app/config/ApiExceptionMapperTest.java`
Current Codex O3 may add many tests/source files beyond snapshot; re-audit.

## 4. Postman and demo evidence, per recorded scope
- `postman/OrgResource-O1-Skeleton.postman_collection.json`: 15 recorded O1 requests/29 assertions passing.
- `postman/OrgResource-O1-Skeleton-Demo.postman_collection.json`: 2 requests/4 assertions passing in preflight; ephemeral uniquely named fictional Department.
- `postman/OrgResource-O3-Department-Update.postman_collection.json`: 11 requests/19 assertions passing in saved O3 report.
- `postman/OrgResource-O1-Local.postman_environment.json`: no secrets, local template. Other historical 68-operation Postman collections are NOT current implementation.
- `evidence/O1/PHASE_REPORT.md` and `O1-SKELETON-DEMO-PREFLIGHT-2026-11-14.md`; O1 `newman-results-final.json`, `schema-evidence.txt`, `restart-persistence-http.json`, `zip-clean-extract-build.txt` etc.
- `evidence/O3/PHASE_REPORT.md`, `ROUTE_PARITY.md`, `newman-d03-results-final.json`, `newman-o1-regression-final.json`, `error-mapping-http.json`, `db-state-after-o3.txt`, `zip-clean-extract-test.txt`.
- `deliverables/OrgResource-Java-Skeleton-2026-11-14.zip` with SHA256 per O3 report; current source export, NOT final app.
- `scripts/start-skeleton-demo.ps1` auto JDK8/Maven/start Java; `scripts/run-skeleton-demo-sql.ps1` safe read-only DB, `scripts/skeleton-demo.sql` read-only schema/data inspection.
- Old PR #1 contains alternative O2 scripts and 12-request Postman on a **branch based on initial commit**; not merged and not safe to treat as canonical.
- Video: user reported recorded using OBS, but actual file/receipt not verified. Do not request video upload unless analyzing video is now necessary.

## 5. Primary source coverage caveats
The original files and route meanings in this pack were corroborated through course assignment pages, syllabus learning outcomes, actual proposal excerpts, course corpus relevant Java snippets, current connected GitHub Java source and saved evidence. **Not all original source ZIP entries or lecture slides, timestamped audio segments, or video frames were personally reverified when compiling this overlay.** Keep source-coverage register truthful. Previous report's 2,611 paths / 852 distinct hashes are RECORD-REPORTED figures not recomputed now. Asset references in repository history are NOT equivalent to raw bytes being presently accessible in every agent runtime. GitHub ZIP binary downloads may require normal GitHub interface rather than text connector.

## 6. Helpful source precedence crosscheck
Primary assignment>syllabus/current verified Canvas>user decisions>actual course examples for versions>original 25-route concise proposal>reviewed amendments>historical ERP/docs. The **current request** is about how to work; current working GitHub source is authoritative for what exists. Never interpret handoff markdown as code/test proof.



---

# INCLUDED SOURCE 10/12: 09_CONTINUATION_CHECKLIST_AND_DECISION_LOG.md

# Continuation audit checklist, state reconciliation, decisions and risks

## 1. Exact status by provenance
- **COURSE REQUIREMENT:** Assignment dated 2026 course project, 5 entities, Jersey+Spring+JPA+Javax+Retrofit/Twilio+Scheduling, GET/POST, controller injection, Postman, Part II export/video, final project due printed Dec5. Current Canvas may supersede.
- **SYLLABUS FACT:** Eclipse/Java, Jersey, JPA/Hibernate/MySQL, JPQL, validation, Retrofit/GSON, image receive/serve CLO9, authorship/assistance obligation.
- **USER DECISION:** Current O3 prompt still running at handoff request. O2 video DONE by user report (not independently opened or submitted). VS Code, Codex coding, private newonathe repo, final working React UI, exact five entities, efficient grouped builds, mandatory fresh GitHub audits and feature-branch push of all legitimate changes.
- **OBSERVED CONNECTED GITHUB:** Main `16e781c`, 13/25 original routes, O3 D03 tested, eight Java tests saved, 0 CI runs, draft PR #1 old O2 branch, no implemented frontend. This is a point-in-time snapshot.
- **SAVED EXECUTION EVIDENCE:** O1 Maven/MySQL/Newman/restart, O3 Maven/8 tests/newman tests and clean source export, current demo preflight real startup; saved records not equal to fresh independent rerun by new ChatGPT conversation.
- **WORK IN PROGRESS:** O3 six CRUD routes + coverage correction, completed/pushed status unknown.
- **NOT YET OBSERVED/OPEN:** full Allocation lifecycle, actual @Scheduled jobs and FakeTwilio HTTP, React app, live MySQL concurrency, final Eclipse GUI import, final bundled UI/JAR, CLI/CD, image obligation, current Canvas, user-authored original O2 video file/Canvas receipt, final Dec5 academic deliverables.

## 2. First 15-minute remote inspection in new conversation
1. Check GitHub account `newonathe` selected (other connected GitHub accounts exist; don't use wrong account).
2. Fetch repo metadata and `main` HEAD, recent commits (date + subject), branch list and HEADs, open PRs including draft #1, workflows/checks.
3. Find O3 feature branches with recent commit and compare diff from old head. Do not use old branch as merged unless Git actually shows it.
4. Read exact updated `evidence/O3/PHASE_REPORT.md`, `evidence/O3/ROUTE_PARITY.md` from its branch; if not updated search for `evidence/O3*` new files or Codex response.
5. Enumerate actual `@POST` and `@GET` paths across five controller Java files. Count 25 original paths; separately A09 (and conditional image routes). Show implemented/untested/missing.
6. Read actual modified code: Project/Resource/Requirement components/repositories/DTO/controller and JPQL, tests, Postman; pay special attention to `expectedTotalQuantity` and effective COVERED query.
7. Inspect test types and saved outputs to distinguish Java unit, MySQL integration, HTTP Newman, concurrency, clean packaging. If changes not pushed despite local Codex output, request push/commit and don't treat repo stale as code failure.
8. Check PR status and branch protection; no automatic merges.
9. Return succinct acceptance verdict with critical issues and exact next step. If current O3 STILL actively running, don't interfere or send overlapping new coding prompt until user is ready.

## 3. O3 audit verdict format
```
GitHub audit time (PHT):
Repository / visibility:
Remote main SHA:
O3 feature branch SHA / PR:
User local modifications visible? (yes/no; evidence):
Changed Java/API/DB source:
Routes now implemented: D__/P__/R__/Q__/A__, total original __/25, extra A09 __.
Tests: Java unit __ PASS/__ FAIL/__ NOT_RUN; MySQL __; Newman __; concurrency __; clean package __; Eclipse import __.
Coverage query: Correct / Incorrect / Not verified; evidence:
Resource stale guard: Correct / Incorrect / Not verified:
Project historical approval date lock: Correct / Incorrect / Not verified:
Requirement below max(PENDING,covered): Correct / Incorrect / Not verified:
Current unresolved P0/P1 defects:
Remote push verified SHA:
Decision: PASS / CONDITIONALLY_READY / FAIL / BLOCKED.
Next batch: O4+O5 or one coherent O3 repair.
```

## 4. Working decision register
ADR-01 Exact 5 entities, no StockMovement. [USER DECISION]
ADR-02 Original 25 method/path contract, added A09 separately. [USER DECISION/AMENDMENT]
ADR-03 Whole batch reusable/consumable, no damaged/partial returns. [DESIGN RECOMMENDATION]
ADR-04 Reusable totals owned vs consumable remaining stock. [DESIGN RECOMMENDATION]
ADR-05 Effective APPROVED hold expires at Project end, not indefinitely. [AMENDMENT]
ADR-06 Pending never holds; approved future Project holds globally. [AMENDMENT]
ADR-07 Issued custody persists after Project end; late manual return. [AMENDMENT]
ADR-08 Historical approvedAt never erased; protects Project date edits and physical deletion. [AMENDMENT]
ADR-09 Resource expectedTotalQuantity guard, no version table; do not auto retry lost HTTP. [AMENDMENT]
ADR-10 Requirement lower bound effective Covered and max individual pending. [AMENDMENT]
ADR-11 Public API redacted phone and derived contactPresent; only store when consent. [PRIVACY]
ADR-12 A09 revoke consent clears destination independent of lifecycle. [PRIVACY]
ADR-13 SMS claim once, no automatic retry UNKNOWN, simulated != delivered. [SAFETY]
ADR-14 Local trusted operator, no fake auth; bind loopback. [PROTOTYPE LIMIT]
ADR-15 Java8 SpringBoot2.1/Jersey/Javax, VSCode, Eclipse export. [USER DECISION/COURSE]
ADR-16 React strict TS/Vite real UI despite optional bonus. [USER DECISION]
ADR-17 Conditional CLO9 Resource image extension only if authoritative requirement established. [OPEN QUESTION]
ADR-18 One master source read then targeted; larger coherent grouped cycles. [USER DECISION]
ADR-19 Every NEW cycle fresh GitHub remote and Codex local audit, every cycle pushes all legitimate work to safe feature branch and verifies remote. [USER DECISION]
ADR-20 No blind merge main, forced Git destructive operations, DB reset, public release, Canvas submission or real SMS without authorization. [SAFETY]
ADR-21 No real-world Ateneo institution/workflow/adoption/participant data inferred. [ACADEMIC]
ADR-22 Don't request raw videos; audio transcript not frame data. [SOURCE LIMIT]
ADR-23 No fake code/test/evidence; independent rerun statuses separately. [ACADEMIC]

## 5. Critical risks to keep actively tested
R1 MySQL JDK8/Jersey/Hibernate dependency conflicts; course source compatibility.
R2 MVC refactor breaks required Jersey and explicit @Autowired component structure.
R3 Racing approvals overbook stock/requirement; flawed Project→Resource→Allocation lock order/deadlocks.
R4 Incorrect expired approved coverage and H/I at endAt boundary; stale query after lock.
R5 Consumable double deduction on consume; duplicate network POST after timeout.
R6 Resource expected-value guard race; naive stock total update or inconsistent response.
R7 Project historical approvedAt/date edit vs approval race.
R8 Requirements pending largest vs sum, and actual constrained coverage.
R9 Deletion cascades erase immutable history; FKs not respected.
R10 Scheduler auto-releases issued custody or assumes return; background job duplicate claims.
R11 Retrofit FakeTwilio vs real Twilio response differences and leaked Authorization in simulator logs.
R12 Time zone conversion PHT↔UTC across JS browser and Java LocalDateTime.
R13 Frontend fake demo data, frontend local stock authority, missing mobile/focus and no actual live API.
R14 CI/Java export/Eclipse compatibility untested but claimed.
R15 Historical 9-entity/68-route docs accidentally used as specification.
R16 Canvas policy/date and CLO9 image uncertainty.
R17 Secrets/private contacts committed by automated agents; broad CORS/auth misrepresented.
R18 Git concurrent writers, old O2 PR rebased into latest main, accidental main merge.
R19 Lossless continuity request: primary source files and actual code must be reopened, not replaced by this overlay.

## 6. Evidence labeling convention
**PLAN/SPECIFIED** = design only; **IMPLEMENTED** = actual source present; **TEST REPORTED** = saved result evidence inspected but not independently rerun in this chat; **VERIFIED EXECUTION** = appropriate test actually executed under tool-observed environment; **USER REPORTED** = explicit user's action claim; **FAILED** = executed and failed; **BLOCKED** = dependency unavailable; **NOT_RUN** = no execution attempted; **OPEN QUESTION** = source conflict/decision. Do not compress these into a generic “working”.

## 7. Git branch and merge practice
A feature branch push is authorized as a **coding-cycle** end action. Main merge and release remain separate review gates. PR #1 is OLD O2 demo kit (main is newer); don't merge by default. This continuity pack itself is on `docs/conversation-continuity-2026-11-14` specifically to avoid interference with active Codex O3 edits; it does not contain Java implementation changes and doesn't assert application tests passed. User can use its links in a new ChatGPT conversation in the same Project.

## 8. What the new chat should DO immediately, not do
DO audit actual changed GitHub branch and source, review O3 code-level behavior/test evidence, then draft grouped O4+O5 prompt if ready. DO call connected GitHub tool as necessary. DO preserve separate primary source authority. DO optimize for actual verified software per token, performance and storage, and automatic push. DON'T start editing code from ChatGPT without user ask, DON'T re-explain O2 video by default, DON'T make plans as if 13/25 still current after Codex push, DON'T claim O3 checks passed from old reports, DON'T merge or release without explicit instruction.

## 9. User handoff UX
Simplest new conversation starter: copy file `07_NEW_CHAT_COPY_READY_PROMPT.md` into new chat, or point new chat directly to private GitHub repo branch/directory and ask it to read `MASTER_CONTINUITY_UNABRIDGED.md` plus fresh GitHub audit. This is the smallest repeated context input; detailed evidence remains in committed files. If a GitHub connector isn't connected in the new chat, attaching downloaded master markdown also works. Never guess sandbox download path.


---

# INCLUDED SOURCE 11/12: 10_O4_O5_COMBINED_CYCLE_SEED.md

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


---

# INCLUDED SOURCE 12/12: 11_O2_VIDEO_DEMO_RUNBOOK_HISTORY.md

# O2 Skeleton video: completed user action, runbook, exact commands and course-grounded explanation

This retains the presentation engineering context, even though it is NOT the next coding task. USER later explicitly reported: **“done with the video.”** No video file, recording playback or Canvas submission receipt was independently inspected during this chat. The repo's older `VIDEO_NOT_CREATED.md` represents a dated stale state before the user recording.

## What user asked and why the detailed runbook existed
User nontechnical, requested a **very fast but complete, unambiguous** genuine Part II video. After initial straightforward instructions, user asked to **elaborate what the code actually does grounded in course materials and actual project significance**, without going overboard. Then user requested **literally all clicks, pasteable commands and filename paths** integrated into narration while keeping this ChatGPT conversation open and hidden from OBS recording.
The script used a 6–8 minute target, VS Code only, OBS Window Capture rather than Display Capture (so user could read the ChatGPT window without recording it), microphone audio test, Save MKV/Hybrid MP4 and Remux if needed. It used existing PowerShell scripts and a two-request Newman collection; no fake console screenshot, invented rows, raw passwords or live SMS. All code shown was actual five-entity skeleton; speech explained implemented vs proposed functionalities honestly.

## OBS Studio click path
1. Open OBS; Scenes + → name `OrgResource Demo`.
2. Sources + → Window Capture → `VS Code Only`, select actual Visual Studio Code OrgResource window.
3. Source context menu Transform → Fit to Screen. Disable any Display Capture source (eye icon); test ChatGPT overlap does not appear.
4. Audio Mixer check Mic/Aux meter. Settings→Audio if needed. Settings→Output→Recording path; Hybrid MP4 if available or MKV→File→Remux Recordings later.
5. Do a 10-second actual test including moving this ChatGPT browser window over VS Code, speak into mic, stop, replay to ensure only VS Code and audio captured. Keep VS Code not minimized.
6. During recording, use mouse/Alt+Tab to read ChatGPT privately while OBS captures only VS Code window. If user switched to browser Chrome, it would not appear, so instead REST JSON was shown via VS Code integrated PowerShell. Test actual preview, don't assume Window Capture worked.

## Workstation/environment snapshot for the O2 demo
VS Code open repository root:
`C:\Users\ethan\Downloads\Academic\4th Year\CSCI 180.05\OrgResource`.
Select File→Open Folder if necessary. Explorer must show `backend`, `scripts`, `postman`, `deliverables`, `evidence`. Terminal→New Terminal, two integrated PowerShell terminals; Terminal A keeps Java running, Terminal B SQL/Newman. Terminal start location must be repository root, NOT backend. If mislocated:
```powershell
Set-Location 'C:\Users\ethan\Downloads\Academic\4th Year\CSCI 180.05\OrgResource'
```
MySQL target `127.0.0.1:3308`, database `orgresource`, user `orgresource_dev`, protected credentials via `%LOCALAPPDATA%\OrgResourceDev`; NOT Workbench profiles 3306/3307. Java8 was configured via local Temurin JDK and launcher, HTTP `127.0.0.1:8080`. Never type password into video.

## Commands copied into actual integrated terminals
Terminal A launch (kept running until recording ended):
```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-skeleton-demo.ps1
```
Recorded criteria: Java 8 selected; Maven build `BUILD SUCCESS`, 8 unit tests pass in O3 context, Spring Boot `Started SpringBootApp` without fatal exception. If actual command fails, do NOT say pass; script checks protected config/3308/8080 and refuses unsafe port takeover/DB reset.

Terminal B read-only database SQL:
```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-skeleton-demo-sql.ps1
```
Shows database name orgresource; five InnoDB tables `allocation`, `department`, `project`, `resource`, `resource_requirement`; FK relationships Project→Department, Requirement→Project+Resource, Allocation→Requirement; row counts and fictional sample records (counts can differ after POST). No customer phone/borrower name selected by SQL script. Do not assume exact previous saved row counts in new run.

Browser GET was initially suggested, but to keep OBS capturing VS Code, final script used Terminal B:
```powershell
Invoke-RestMethod 'http://127.0.0.1:8080/api/departments' | Select-Object -First 3 | ConvertTo-Json -Depth 4
Invoke-RestMethod 'http://127.0.0.1:8080/api/resources' | Select-Object -First 3 | ConvertTo-Json -Depth 4
```
Prove real HTTP GET and JSON from running Jersey/Java/MySQL. Details visible may vary; when empty, mention no matching rows honestly.

Terminal B focused Postman/Newman:
```powershell
npm exec --yes --package=newman -- newman run postman\OrgResource-O1-Skeleton-Demo.postman_collection.json --reporters cli
```
Two actual operations: 1. POST /api/departments with unique fictional `Skeleton Demo Department ...`, expect 201+generated ID; 2. GET /api/departments and assert same ID/name persisted. Four assertions; preflight saved PASS 2 requests/4 assertions/0 failures. The video must show fresh results before saying pass. Collection uses timestamp/random suffix, creates an additional safe fiction Department and intentionally does not delete it.

Source code opened through VS Code `Ctrl+P` + paste relative repository path:
- `backend/src/main/java/app/entities/Project.java`: @Entity/@Id/JPA associations, Department FK, Project start/end.
- `backend/src/main/java/app/entities/ResourceRequirement.java`: Project+Resource and quantity; distinct need vs inventory.
- `backend/src/main/java/app/entities/Allocation.java`: requirement quantity, borrower name and PENDING etc.
- `backend/src/main/java/app/repositories/AllocationRepository.java`: `findByRequirement_Id`, `findByStatus`, `findByRequirement_IdAndStatus`; JPQL `sumCoveredQuantity` etc. Do NOT imply full inventory engine done; expired approval coverage bug open.
- `backend/src/main/java/app/repositories/ResourceRequirementRepository.java`: `findByProject_Id` and `existsByProject_IdAndResource_Id`, supports unique Project/Resource.
- `backend/src/main/java/app/rest/controllers/DepartmentController.java`: Jersey `@Path("/departments")`, GET/POST, Spring `@Autowired`, delegate `component.create/list`; the new D03 update method.
- `backend/src/main/java/app/components/DepartmentComponent.java`: process normalize duplicate, use repository save/list/update.
- `backend/src/main/java/app/repositories/DepartmentRepository.java`: extends `JpaRepository`.
- `backend/src/main/java/app/config/JerseyConfig.java`: `@ApplicationPath("/api")`, individually registers all five controllers.
- Show `deliverables/OrgResource-Java-Skeleton-2026-11-14.zip`: Maven/Eclipse-compatible source ZIP, clean extraction Maven tests reported; actual Eclipse GUI import not verified.

## Course-grounded spoken narration highlights
Introduction: OrgResource is a hypothetical student organization's shared equipment/supply planning service, not campus-endorsed. Example a workshop needs extension cords and paper. Resource is what organization has, Requirement is project need, Allocation is request/commitment. Five tables justified by relationships.
Spring Boot: Java8 app dependency injection and startup, build tests and connect to MySQL. JPA/Hibernate Java object mappings and relational FK. Course CLO3 Spring/Jersey, CLO5 persistence.
Repositories: Spring Data `findBy` methods and JPQL aggregates for holds/custody/coverage; not simply hardcoded return values, but full lifecycle still forthcoming. Course CLO6.
Jersey/Components: `@Path` maps route, GET read, POST write, `@Autowired` delegates thin controllers to Spring service components, repositories persist; same course Calculator/Animal Sighting patterns. Course CLO3, project explicit requirement.
Postman/JSON: actual GET JSON through PowerShell, Newman POST201/GET200, generated ID persisted, demonstrates realistic create/read. Course CLO4.
Conclusion: part II establishes startup, 5 connected entities/tables, components, repos, live JSON. The complete allocation transitions, scheduling, Retrofit/Twilio, React frontend and final project are future work (not demonstrated).
End: OBS Stop Recording; File→Show Recordings; if MKV File→Remux Recordings; save suggested `OrgResource-PartII-Skeleton-Demo-2026-11-14.mp4`; replay to check audio/legibility/no ChatGPT; Terminal A Ctrl+C to gracefully stop Java; source ZIP and actual MP4 to Canvas subject Canvas acceptance. User later stated recording DONE; do not require replay in new chat unless user asks.

## Relevance to next conversation
Do not spend development tokens on a new O2 demo, video narration or OBS configuration unless user specifically asks for revisions. This history exists to preserve the accomplished milestone and its technical/academic evidence boundary. Focus on actual O3 Codex completion and next O4+O5 build.


---

# INCLUDED PREEXISTING ORIGINAL CANONICAL PLANNING HANDOFF (HISTORICAL AS OF 2026-11-13)

The next section reproduces `docs/ORGRESOURCE_MASTER_HANDOFF.md` as fetched from the baseline GitHub commit. Statements there explicitly saying code/video/tests were NOT implemented describe the planning archive's date and can be stale relative to later O1/O3 implementation and user-reported O2 video. DO NOT mistake old planning status for current GitHub state.

# ORGRESOURCE — COMPLETE SINGLE-FILE MASTER HANDOFF

**PLANNING SPECIFICATION / AGENT PROMPTS / SETUP / API / UI / TESTS / EXPORT**

**Date:** 2026-11-13 (Asia/Manila)  
**Status:** PLANNING ONLY — Java source, DB tests, Postman runs, real UI, videos, Eclipse exports and Canvas submissions are NOT verified in this archive.

This file consolidates every companion chapter, setup guide, prompt, template and Postman/acceptance matrix contained in this ZIP into one readable file. An exact machine-readable copy of the JSON/CSV files is separately included for agent use; their contents are reproduced near the end here.

**User decisions:** VS Code exclusively for editing, Codex + Claude Code as coding agents, planned GitHub newonathe, complete functioning React UI, instructor-compatible Eclipse Java source export, urgent skeleton ZIP and genuine demonstration video.

**Course authorities:** 09_ESP_PROJECT_ASSIGNMENT.pdf and 08_CSCI180_SYLLABUS.pdf, with live Canvas updates unverified. Original 10_ORGRESOURCE_CONCISE_PROPOSAL.docx defines five JPA entities and 25 operations; the 26th consent-revocation route is a reviewed amendment. This archive cannot replace the actual primary source bytes; mount them to Codex when available.

**No fabricated evidence:** The enclosed route templates and Java/TypeScript snippets have not been run as an OrgResource application. Nothing in this file is a school submission, runtime screenshot, provider acceptance or successful Eclipse import.

## Navigation

The sections below reproduce files in logical reading order. Start with setup, skeleton, domain, API, transactions, UI, tests, GitHub/release, source audit, then the copy-ready first prompt and all phases. Use the individual files when feeding agents smaller portions. Read the entire master when taking architectural decisions.

---


---

# MASTER SECTION 1: 00_START_HERE.md

# OrgResource — COMPLETE handoff archive

Version: PLAN-2026-11-13. This is a planning and implementation-instruction ZIP, not the completed Java app or submitted academic work.

USER DECISIONS: Develop in VS Code (not Eclipse). Have Codex and Claude Code implement and independently review. Store reviewed implementation under GitHub account newonathe. Build a genuine responsive React/TypeScript UI. Deliver a Java source archive importable to an Eclipse-based course environment.

SOURCE OF TRUTH: 09_ESP_PROJECT_ASSIGNMENT.pdf -> 08_CSCI180_SYLLABUS.pdf/verified Canvas -> user decisions -> inspected course examples and dependencies -> original five-entity proposal -> this reviewed handoff. Earlier six-/nine-entity drafts are history only.

ARCHIVE CONTENT: 'ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md' concatenates ALL detailed chapters below into ONE file. Supporting chapters, setup instructions, first-paste prompt, staged implementation prompts, review scripts, JSON Postman route template, acceptance matrix and manifest are included separately for targeted agent loading.

HOW TO BEGIN:
1. Read '01_SETUP_VSCODE_GITHUB.md' and '02_IMMEDIATE_SKELETON_VIDEO.md'.
2. Extract the folder and place 'ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md' into your future repository at 'docs/ORGRESOURCE_MASTER_HANDOFF.md'. Ensure Codex can open this actual file.
3. Give Codex original assignment, syllabus, concise proposal, and actual course code; it must inspect and report inaccessible sources, not infer them.
4. Paste the entire 'PROMPTS/00_FIRST_PROMPT.md' into Codex. Let it inspect environment and implement the urgent skeleton (O0–O2 sequential gates) without falsely claiming tests passed.
5. Use Claude Code's review prompt independently and only in a separate worktree if writing.
6. Proceed O3–O14 toward real backend, React UI, transactional tests, Retrofit, scheduler, and final export.
7. Check live Canvas dates and requirements. Do not let agents submit, push, merge or expose secrets without your explicit authorization.

MAJOR BOUNDARIES: Five entities (Department, Project, Resource, ResourceRequirement, Allocation). Original 25 routes + proposed one consent withdrawal = 26. Whole-batch resource workflow, conservative global reservations. JAX-RS/Jersey/Javax/Spring Data JPA/MySQL + Retrofit/Gson + Scheduling. Real UI is mandatory by user decision but must not delay immediate backend skeleton. Optional image receive/serve feature only if final assignment/syllabus interpretation requires; can add two routes without sixth entity.

EVIDENCE LIMIT: planning math/code examples are not successful Java/Postman/MySQL runs. Current GitHub repo/Canvas status unknown. Complete original ZIP byte review not established. Video transcripts are AUDIO ONLY.


---

# MASTER SECTION 2: 01_SETUP_VSCODE_GITHUB.md

# 01 — Setup from zero: Windows VS Code, Java, Maven, MySQL, Node, GitHub, Codex, Claude Code

These are instructions to run on your local machine; they were NOT executed here. Use the exact OS shell. For macOS/Linux translate PowerShell and wrapper commands. Inspect existing files before creating or replacing anything.

## A. Inspect workspace and machine first

Open VS Code terminal (PowerShell on Windows). Choose where OrgResource will be developed, e.g. C:\Development\OrgResource, but use actual user-preferred path. Do not overwrite any existing repository.

~~~powershell
Get-Location
Get-ChildItem -Force
git status --short --branch
git remote -v
git --version
java -version
javac -version
mvn -version
node --version
npm --version
mysql --version
code --version
gh --version
where.exe java
where.exe javac
where.exe mvn
where.exe node
~~~

Missing commands are environment findings, not proof work was done. MySQL client version is not server version. Do not print tokens.

## B. Install and configure as needed

1. VS Code: https://code.visualstudio.com/ ; Java Extension Pack, Maven for Java, Java test/debug support, GitHub PRs (optional), ESLint (for React) and official Codex/Claude integrations if authorized.
2. Java: inspected course-era libs are Java 8/Jersey 2/Boot 2.1 based. Install a JDK 8 toolchain for backend compilation and runtime after checking actual lab examples. VS Code Java language server may run on newer JDK; configure project JDK separately via Java runtime settings. Do not confuse IDE JDK with backend JDK.
3. Maven: https://maven.apache.org/install.html. A Maven wrapper is recommended after actual project generation. It does not exist until created; do not claim '.\mvnw.cmd' already works.
4. MySQL server and SQL client: https://dev.mysql.com/downloads/ ; verify server with SELECT VERSION(); check InnoDB, foreign keys, unique indexes and datasource privileges.
5. Git: https://git-scm.com/downloads . GitHub CLI optional https://cli.github.com/. Verify account newonathe before any repo creation, remote, push or pull request. Prefer private visibility until user approves publication.
6. Node/npm: https://nodejs.org . Choose actual Vite-supported Node LTS; official Vite docs list Node 20.19+ or 22.12+ as base requirement, with some templates requiring more. Modern Node is separate from Java 8 backend.
7. Codex/Claude: follow current authenticated vendor instructions, give access only to chosen repo/source files, keep secrets outside prompts. Never let agents write same worktree at once.

## C. Workspace creation

If repository already exists remotely, use authenticated git clone/gh repo clone on its verified URL. If folder already has work, inspect before copying files. If NO repo and user authorizes creation:
~~~powershell
New-Item -ItemType Directory -Force -Path .\OrgResource
Set-Location .\OrgResource
git init -b main
git status --short --branch
code .
~~~
The above is an example, not a command to run without first checking path. Do NOT silently create a public repo.

Unzip handoff to 'docs/handoff/', copy its complete master to 'docs/ORGRESOURCE_MASTER_HANDOFF.md', use AGENTS.md and CLAUDE.md only after checking existing instructions. Do not automatically commit private original course ZIPs/slides to a public repo.

## D. Database first

Use dedicated fictional-data schema. After connecting to actual MySQL server:
~~~sql
SELECT VERSION() AS mysql_server_version;
SHOW VARIABLES LIKE 'default_storage_engine';
CREATE DATABASE IF NOT EXISTS orgresource
 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
~~~
Create dedicated local app user with least permissions using your DB admin, credentials only in local environment. The exact SQL user creation depends on version/security preferences. No real password in Git or chat.

Suggested app environment variables:
- ORG_DB_URL
- ORG_DB_USER
- ORG_DB_PASSWORD
- ORG_SMS_MODE (DISABLED by default)
- ORG_FAKE_TWILIO_BASE_URL
- ORG_FAKE_TWILIO_SERVICE_ID
- ORG_FAKE_TWILIO_DUMMY_AUTH
- ORG_REMINDER_SCAN_MS, ORG_EXPIRY_SCAN_MS

Example terminal env values (placeholders must be replaced LOCALLY; never publish):
~~~powershell
$env:ORG_DB_URL="jdbc:mysql://127.0.0.1:3306/orgresource?serverTimezone=Asia/Manila"
$env:ORG_DB_USER="<LOCAL_DB_USER>"
$env:ORG_DB_PASSWORD="<LOCAL_SECRET>"
$env:ORG_SMS_MODE="DISABLED"
~~~

Application properties template:
~~~properties
server.address=127.0.0.1
server.port=8080
spring.datasource.url=${ORG_DB_URL}
spring.datasource.username=${ORG_DB_USER}
spring.datasource.password=${ORG_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
orgresource.sms.mode=DISABLED
orgresource.reminder.scan-ms=3600000
orgresource.expiry.scan-ms=3600000
~~~
The 'update' schema mode is academic convenience only; do not use it as migration system. Do not switch to destructive drop/create in ordinary demo config. Prove LocalDateTime round trips and actual JDBC driver.

## E. Backend build

Recommended backend/ with pom.xml, Maven Wrapper, src/main/java/app and src/main/resources. Original course library inventory includes Boot 2.1.0.RELEASE, Jersey 2.26, Hibernate 5.3.7, Spring Data JPA 2.1.2, MySQL Connector/J 8.0.13; select a compatible dependency graph only after inspecting original code. Java 8 source/target, javax.* imports, Jersey not Spring MVC.

Once project generated:
~~~powershell
Set-Location backend
.\mvnw.cmd -version
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
~~~
Use 'mvn' if the wrapper was not generated and local Maven is verified. On macOS/Linux use './mvnw'. Runtime target API is http://127.0.0.1:8080/api/departments. Wrong '/api/api' mapping is a common error: ResourceConfig @ApplicationPath("/api"), controllers @Path("/departments").

## F. React frontend after skeleton

Frontend should be Vite + React + TypeScript with pinned package-lock.json. Scaffold when O7 begins; do not claim it exists beforehand. Use 'npm ci' only after lockfile creation.
~~~powershell
Set-Location frontend
npm ci
npm run dev
npm run build
npm run test
~~~
Vite dev target http://127.0.0.1:5173, proxy '/api' -> Java server 127.0.0.1:8080. Never use frontend localStorage as inventory authority. Final static assets bundled into backend only after verifying packaged / and /api routes. Use React HashRouter for simple Java servlet coexistence.

## G. Multi-agent workflow

Codex primary backend, Claude independent reviewer/React track, but one writer per worktree. Use separate git branches/worktrees for simultaneous edits. Review Git diff and tests before merge. Agents may make useful local edits, run commands and prepare export/video if tools support them. They must not invent video, Canvas receipt, test result, repository or submission. Ask user permission for push/merge/submission/destructive actions. Do not claim continuous GitHub monitoring without actually creating a monitoring workflow.

## H. Final setup checkboxes

[ ] Git, author, branch, remote and visibility verified.
[ ] Original assignment/syllabus/proposal accessible to agents.
[ ] Correct course JDK, Maven and dependencies identified.
[ ] Actual MySQL server, InnoDB and dedicated schema work.
[ ] Jersey/Spring/Repository startup proven with real POST/GET.
[ ] Five principal JPA tables and FKs observed.
[ ] npm and React setup reserved for after skeleton gate.
[ ] No credentials in project or logs.
[ ] Eclipse-compatible source archive strategy explicitly verified.


---

# MASTER SECTION 3: 02_IMMEDIATE_SKELETON_VIDEO.md

# 02 — IMMEDIATE SUBMISSION: imported Eclipse Java project + genuine video

COURSE SOURCE: 09_ESP_PROJECT_ASSIGNMENT.pdf states project skeleton due Nov 13, Canvas exported project and recording due Nov 14, 2026; check live Canvas for updates. User specifically wants recorded startup, created tables/test data, and repository/controller overview. Assignment additionally wants URLs returning JSON in Postman. This is immediate P0 work, distinct from final React UI due Dec 5.

## What must be real

1. Java backend starts with no fatal errors.
2. MySQL connection is real.
3. Five entity tables generated from JPA: department, project, resource, resource_requirement, allocation (names subject to actual @Table mapping).
4. Actual foreign keys and unique constraints inspected.
5. Test data genuinely loaded, if enabled; no invented rows.
6. Spring Data repositories initialize; custom query names/JPQL don't prevent application launch.
7. Five Jersey controllers and Spring @Autowired components are reachable.
8. GET/POST return actual appropriate JSON; skeleton stubs may exist but must be labeled.
9. Exported Java project contains source/build/dependencies and has an Eclipse-compatible import route.
10. Video depicts actual terminal, SQL client, source and Postman; no fabricated success.
11. Actual submission must be done by user or explicitly authorized tools. No claim of Canvas receipt without proof.

## Video recording storyboard (suggested 5–7 min, not instructor mandate)

00:00 Show VS Code folder tree, purpose of OrgResource and Java backend.
00:25 Start backend from terminal and show successful startup. Show port and data source initialized; no fatal exception.
01:10 SQL: SELECT VERSION(), SHOW TABLES, SHOW CREATE TABLE for five entities and FK structure.
02:25 SELECT sample rows if present. Explain fictional organization and data scope.
03:10 Show repository source including one custom derived query and JPQL method; show actual result of a route/test invoking it.
04:05 Show Jersey @Path / @GET / @POST controller, @Autowired component delegation, and repository call. Explain service separation.
05:00 Postman POST /api/departments with fictional record, then GET /api/departments, prove matching generated ID in DB. Optionally demonstrate safe delete.
06:05 Show ZIP content/source and import procedure, distinguish verified Maven build from actual Eclipse import. State unfinished final lifecycle/UI honestly.
Do not record secrets, actual phone numbers, notifications, unrelated private browser tabs or student IDs. Show source from same tested Git commit.

## SQL inspection script

~~~sql
SELECT VERSION() AS mysql_server_version;
USE orgresource;
SHOW TABLES;
SHOW CREATE TABLE department;
SHOW CREATE TABLE project;
SHOW CREATE TABLE resource;
SHOW CREATE TABLE resource_requirement;
SHOW CREATE TABLE allocation;
SELECT COUNT(*) FROM department;
SELECT COUNT(*) FROM project;
SELECT COUNT(*) FROM resource;
SELECT COUNT(*) FROM resource_requirement;
SELECT COUNT(*) FROM allocation;
SELECT * FROM department ORDER BY id;
SELECT * FROM project ORDER BY id;
SELECT * FROM resource ORDER BY id;
SELECT * FROM resource_requirement ORDER BY id;
SELECT id, requirement_id, quantity, status FROM allocation ORDER BY id;
~~~
These are candidate names; inspect actual schema and update script rather than presenting failing SQL as proof.

## HTTP proof

POST http://127.0.0.1:8080/api/departments
Content-Type application/json
Body:
~~~json
{"name":"Fictional Logistics Demo"}
~~~
Expected status 201 only when implemented, and actual generated ID. Then GET /api/departments should include matching database row. If controller returns a placeholder, label it as skeleton stub; do not claim persistence.

## Eclipse-compatible source export from VS Code

The Java source archive MUST contain all Java .java source, pom.xml (or tested portable course JAR setup), necessary project config, dependencies resolution and README. If course demands literal Eclipse .project/.classpath/.settings, generate correct portable metadata and test import if feasible; otherwise state NOT_RUN. Maven+m2e import is one route but not proof of literal instructor interpretation. Exclude real credentials, local absolute paths, compiled caches, node_modules and unrelated source archive ZIPs. Build from clean extraction. Record ZIP SHA-256 and tested commit.

## Mandatory skeleton acceptance evidence

S01 source exists; S02 Java compile; S03 startup; S04 DB connection; S05 five tables; S06 fictional test data if any; S07 repository query initializes and runs; S08 controller paths accessible; S09 injected Component called; S10 JSON returned from real HTTP; S11 runtime create/delete; S12 restart persists; S13 exported source ZIP produced; S14 actual Eclipse import PASS or explicitly NOT_RUN; S15 actual video produced or NOT_CREATED; S16 no exposed secrets; S17 correct current Canvas instruction manually checked.

## Adaptable narration

"This is OrgResource, a proposed academic resource-planning application for student organizations. The project is developed in VS Code and its Java source is prepared for Eclipse-compatible import. [Show successful startup.] The application connects to MySQL and defines Department, Project, Resource, ResourceRequirement and Allocation tables. [Show real tables and fictional data.] Spring Data JPA repositories provide persistence, while Jersey controllers forward HTTP requests to injected Spring components. [Show real code and Postman response.] This is the skeleton milestone. Its unfinished business logic and React interface will be implemented and verified in subsequent phases. [Show export artifact.] Thank you."

If evidence differs from narration, revise narration. Neither Codex nor Claude may fabricate a video from screenshots of expected output. If recording capability missing, provide exact shot list and mark VIDEO NOT_CREATED.


---

# MASTER SECTION 4: 03_AUTHORITY_SCOPE_ARCHITECTURE.md

# 03 — Authority, source map, scope, architecture and compatibility

## Primary evidence

COURSE REQUIREMENT: 09_ESP_PROJECT_ASSIGNMENT.pdf governs course project requirements and printed due dates. SYLLABUS FACT: 08_CSCI180_SYLLABUS.pdf governs course context, CL learning outcomes, rubric, Canvas and Certificate of Authorship. Current Canvas announcements might change dates/policies and have not been verified. USER DECISION: VS Code, Codex/Claude, GitHub newonathe, real React UI, local source-export compatibility.

Original proposal: 10_ORGRESOURCE_CONCISE_PROPOSAL.docx (12 pages) with five entities, 25 original HTTP operations, whole-batch reusable/consumable workflows and scheduled reminder. Prior nine-entity and six-entity designs are exploratory history, not binding.

Source corpus: 03_COURSE_REFERENCE_CORPUS.txt searchable extracted code; 04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md source map; 01_KICKOFF_PROMPT.md, 02_MASTER_CONTEXT_DOSSIER.md; 06_VIDEO_TRANSCRIPTS.md audio-only, not video frames; COMPLETE_SOURCE_NONVIDEO_PART1/2.zip original file archives; course labs/slides and installed JAR inventories. Earlier manifest reports 2,611 paths, 852 distinct recorded hashes; full independent ZIP-entry reread and rehash NOT established. Review actual originals before asserting a particular classpath or build succeeds.

Representative original course source:
- Spring Boot 2.1.0, Jersey 2.26, Spring Data JPA 2.1.2, Hibernate 5.3.7, javax.persistence 2.2, MySQL Connector/J 8.0.13, Java 8 compiler, Jersey validator/Java-time support in dependency bundle.
- Separate Retrofit example uses interface with @FormUrlEncoded, fields To/MessagingServiceSid/Body and @Header("Authorization"), Gson converter initially commented out in testing example; FakeTwilio controller receives POST /fake_twilio/send; its Authorization is printed (never send real provider credentials).
- Scheduling example @EnableScheduling/@Scheduled but disables DB auto-config intentionally for scheduling-only teaching example; DO NOT copy its DB exclusions into OrgResource.
- Course JPA sample uses @Component controller with @Autowired component and Spring Data repositories.

## Problem definition (assumption, not validated survey)

One hypothetical student organization maintains shared equipment and consumable supplies. Different internal Departments plan Projects and request quantities. An officer records owned/remaining Resource stock, project Requirements, allocations, issuance, whole-batch usable returns and consumable use. Application computes availability/shortages and can send return reminders in controlled test environment.

No empirical interviews, Ateneo campus adoption, actual borrowing records, centralized university inventory workflow, CFMO integration, measured savings, budget, school endorsement or pilot. Academic demonstration with fictional data. Does not replace existing institutional systems.

## Personas and user stories

- Trusted local organizational officer: can create Departments/Projects/Resources/Requirements; approve requests; record issued/returned/consumed batches; correct quantities with stale-update guard; see commitments and shortages; correct data, safely delete eligible records.
- Borrower: represented by a descriptive label and optional fictional SMS destination, not a self-service account. No login or permissions.
- Instructor/reviewer: imports Java source, inspects five entity tables and wiring, uses Postman, watches real startup/DB/code UI demo and confirms compatibility.
- Coding-agent reviewer: checks actual source/diff and reproducible evidence; never invents tests or merges without permission.

Core user journeys: create resource pool -> create future Project -> add Requirements -> submit PENDING Allocation -> APPROVE within capacity -> ISSUE reusable/CONSUME consumable during active period -> RETURN entire usable batch even if overdue -> update stock guard -> read coverage/shortage -> scheduler expiry/reminders -> restart/persistence.

## Alternatives and decision

- Spreadsheet: low adoption cost but manual concurrent integrity.
- Existing organizational inventory tool: possible and not evaluated directly; no claim unavailable.
- Nine-entity ERP and 68 routes: too large for current deadline.
- Six entities with StockMovement event log: improved stock audit but added entity/migrations/routes/test complexity.
- Five-entity conservative inventory service: selected small complete course-aligned prototype. No authenticated roles, no partial damaged/lost stock, no procurement, supplier integration, payment, locations, event ledger, cloud hosting or production readiness.
- UI is a USER DECISION even though course says bonus optional. Build real React pages once backend skeleton is evidenced.

## Quality requirements

Correct database constraints and transactions, no stock overbooking, no double consumption, predictable 400/404/409/500 errors, Javax Validation, full POST/GET route coverage, one truth for availability, restart-safe DB, reproducible Eclipse-compatible export, no secrets/public personal data, typed frontend API, responsive/accessible UI, observable scheduled outbound HTTP. No invented throughput number/SLAs.

## Architecture

Browser React/TypeScript/Vite -> /api JSON -> Jersey controller (@Path, @GET/@POST, @Autowired) -> Spring component (@Transactional) -> Spring Data JpaRepository/JPQL -> MySQL InnoDB. Two scheduled components: expiry cleanup and due-return reminders. Retrofit/Gson Java gateway sends to local FakeTwilio or authorized Twilio Virtual provider, disabled by default. No second backend or separate DB.

Backend may be Maven-built Java 8 with legacy Spring Boot 2.1.0.RELEASE and javax.ws.rs; choose resolved transitive versions only after checking original course code. If Maven impossible or literally incompatible with expected export, use a portable classpath with original JARs and document alternative. Do not migrate to Spring Boot 3/Jakarta.
Frontend modern Node/Vite toolchain independent of Java8; pin lockfile. Use Vite dev proxy /api -> 127.0.0.1:8080, HashRouter. Final Java serves static UI at root; Jersey exclusively /api. Register endpoints individually because packaged Jersey class scanning unreliable. Test actual JAR for / and /api, not just Vite dev server.

## Time and scope amendment

All dates API YYYY-MM-DDTHH:mm:ss Asia/Manila wall time, active Project interval startAt <= now < endAt. Early approval holds globally regardless of future start; unissued APPROVED hold expires at Project end. Issued physical custody remains until genuine recorded RETURN. Original proposal reserved indefinitely: expiration amendment must be disclosed. Original Resource update had only name/total; amended to expectedTotalQuantity precondition. Original response included phoneNumber; amended to public redacted contactPresent and reminder outcome. Original 25 paths preserved, plus proposed A09 consent revocation.

## Conditional images

Syllabus CLO9 image handling may be expected; assignment does not prescribe exact image routes. Treat as OPEN QUESTION. If mandatory, extend Resource with imageData/imageMimeType and POST/GET /api/resources/{id}/image, enforce content type/signature/size, actual SQL bytes and browser display, without sixth entity. Route count then 28. Do not delay imminent skeleton unless course explicitly requires it.

## Official references

https://docs.spring.io/spring-boot/docs/2.1.0.RELEASE/reference/htmlsingle/
https://code.visualstudio.com/docs/java/java-project
https://vite.dev/guide/
https://react.dev/
https://maven.apache.org/
https://dev.mysql.com/doc/
https://www.twilio.com/docs/messaging
https://docs.github.com/
https://schema.postman.com/json/collection/v2.1.0/docs/index.html


---

# MASTER SECTION 5: 04_DATA_MODEL_AND_INVARIANTS.md

# 04 — Exhaustive domain and relational data contract

## Schema: exactly five JPA entities

Department (1) -> many Project.
Project (1) -> many ResourceRequirement.
Resource (1) -> many ResourceRequirement.
ResourceRequirement (1) -> many Allocation.
A Project may need several Resource types; uniqueness is one Requirement for each (project_id,resource_id) pair. Allocation is a request/commitment toward one Requirement. Do not introduce StockMovement, User, Notification, AuditLog or other JPA entity for baseline.

Use explicit @Table physical names and @Column names for repeatable video/SQL evidence: department, project, resource, resource_requirement, allocation. Actual schema must be inspected, not assumed.

Entity: Department
- id Long generated positive PK NOT NULL, never client writable.
- name String 1–100, trim + normalize display.
- nameKey String server-computed normalized unique value, NOT NULL, indexed unique.
Meaning: a fictional organization department/team that owns Projects.
Creation uniqueness must be enforced by DB, not only a preflight SELECT. Update duplicate check excludes same record.

Entity: Project
- id Long generated PK.
- department @ManyToOne Department, required FK.
- name String 1–100 trimmed, NOT NULL.
- startAt LocalDateTime NOT NULL, future on create or when date changed; local Philippine time.
- endAt LocalDateTime NOT NULL, strictly after startAt.
Meaning: organizational activity interval. On update department is immutable. A name-only edit retains unchanged dates even if historical; changing either date requires a future new start and NO historically approved Allocation under Project. Delete only if no Requirements.

Entity: Resource
- id Long generated PK.
- name String 1–100 trimmed, unique normalized nameKey.
- nameKey String NOT NULL unique index.
- type ResourceType REUSABLE | CONSUMABLE, NOT NULL, immutable after create.
- totalQuantity Integer 0–1,000,000, NOT NULL.
Meaning: fungible whole units. REUSABLE total is usable units owned (including out on loan); CONSUMABLE total is current remaining stock. No mandatory unit column (the original 12-page proposal did not include one). Name must clarify measurement, e.g. "A4 paper sheets".
- availableQuantity derived in output, NOT stored.
- Delete only with totalQuantity=0 AND zero Requirement references (intentional stricter rule than original, which only checked Requirements).
- Resource updates must send expectedTotalQuantity from last read; after Resource lock, reject mismatch and refused lower-than-commitment total. This is optimistic expected-value protection, NOT full idempotency or movement audit.

Entity: ResourceRequirement
- id Long generated PK.
- project required @ManyToOne Project FK.
- resource required @ManyToOne Resource FK.
- quantity Integer 1–1,000,000.
- unique(project_id,resource_id) in DB.
- allocatedQuantity/shortageQuantity derived in JSON, NOT stored.
- Cannot reduce below MAX(current effective coverage, largest live PENDING Allocation quantity).
- Cannot update quantity after Project ended; can delete only if no Allocation records.
- Once a never-approved CANCELLED Allocation is deleted, it no longer constrains demand; historically returned/consumed count remains.

Entity: Allocation
- id Long PK.
- requirement required @ManyToOne ResourceRequirement.
- quantity Integer 1–1,000,000, <= Requirement quantity at create, IMMUTABLE thereafter.
- borrowerName String 1–100 trimmed, NOT NULL; a fictional label, no verified identity.
- phoneNumber String nullable; if non-null must pass E.164-like + plus 8–15 digits, first digit 1–9; require for smsConsent true. With consent false, do not retain a phone number. Use fictional controlled test destination, no actual person.
- smsConsent Boolean NOT NULL default false. The fact a JSON Boolean says true does NOT prove actual human consent; demo only.
- status AllocationStatus NOT NULL one of PENDING APPROVED ISSUED RETURNED CONSUMED CANCELLED, persisted as STRING.
- createdAt LocalDateTime NOT NULL, server set.
- approvedAt LocalDateTime nullable, immutable historical marker after first approval.
- issuedAt LocalDateTime nullable, set once.
- returnedAt LocalDateTime nullable, set once.
- consumedAt LocalDateTime nullable, set once.
- cancelledAt LocalDateTime nullable, set when closed/cancelled.
- cancellationReason Enum nullable: USER_REQUEST or EXPIRED.
- reminderClaimedAt LocalDateTime nullable, durable mark for one automatic SMS attempt.
- reminderOutcome Enum NOT NULL default NONE: NONE, CLAIMED, SUPPRESSED, SIMULATED, PROVIDER_ACCEPTED, REJECTED, UNKNOWN.
- reminderHttpStatus Integer nullable, actual received code.
- providerMessageSid String nullable, only when actual provider returns it; no invented SID.
Derived response:
- contactPresent Boolean = stored valid destination present.
- reminderAttempted Boolean = reminderClaimedAt != null.
- expiredPendingCleanup Boolean when status PENDING/APPROVED and now >= associated Project.endAt.
The public response must never return phoneNumber. Do not persist derived fields unnecessarily.

### Java enums

ResourceType = REUSABLE, CONSUMABLE.
AllocationStatus = PENDING, APPROVED, ISSUED, RETURNED, CONSUMED, CANCELLED.
CancellationReason = USER_REQUEST, EXPIRED.
ReminderOutcome = NONE, CLAIMED, SUPPRESSED, SIMULATED, PROVIDER_ACCEPTED, REJECTED, UNKNOWN.
Use EnumType.STRING, NOT ORDINAL.

### Name normalization

Input "  Logistics   " -> display "Logistics", nameKey "logistics".
Collapse internal multi-spaces consistently; lower-case with fixed Locale.ROOT and trim. Protect DB unique index and handle constraint violation races. Do not use case-insensitive Java check as a substitute for DB uniqueness. Verify collation and Unicode behavior; document chosen comparator.

### Date and time

Public Project strings format YYYY-MM-DDTHH:mm:ss interpreted as Asia/Manila local time. Use an injected java.time.Clock for time-dependent test determinism and ZoneId.of("Asia/Manila"). Do not call local Java clock separately in repeated query phases and inadvertently cross end boundary. Sample a common 'now' per business operation. A Project's active time is [startAt,endAt): inclusive start, exclusive end. Project creation requires future start; name-only update does not require moving existing dates into the future.

### Reservation and stock mathematics

Let T = Resource.totalQuantity.
Let H = sum quantity of Allocation where status APPROVED and Project.endAt > now, regardless of Project startAt.
Let I = sum quantity of ISSUED reusable Allocations, even after Project end.
Reusable: available = T - H - I.
Consumable: available = T - H (physical total deducted on consume).
Never clamp negative availability to zero. Negative is DB integrity failure. For an APPROVED consumable of quantity q, on CONSUME total drops T->T-q and H->H-q so available stays unchanged at that instant, not deducted twice.

For Requirement Q:
Covered = effective APPROVED + ISSUED + RETURNED + CONSUMED amounts.
Shortage = max(0, Q.quantity - Covered).
PENDING and CANCELLED count zero. Effective APPROVED stops counting when Project ends even if stored status remains APPROVED awaiting cleanup. RETURNED/CONSUMED remain historical fulfillment after end.
Invariant: 0 <= Covered <= Q.quantity, 0 <= Available <= T subject to reusable issued custody.
Important semantics: returned reusable equipment is available for another Project but the first Project's Requirement stays covered. Approved future Project reserves globally even before it begins; this is conservative capacity behavior and no double future booking based on hoped return.

### Allocation transitions

None -> create -> PENDING (no hold).
PENDING -> approve -> APPROVED (requires Project not ended, sufficient stock and uncovered demand; timestamp approvedAt).
PENDING -> cancel -> CANCELLED/USER_REQUEST.
PENDING -> expiry cleanup after Project end -> CANCELLED/EXPIRED.
PENDING -> delete allowed (never approved).
APPROVED reusable -> issue -> ISSUED during Project active interval; no second availability deduction; timestamp issuedAt.
ISSUED reusable -> return -> RETURNED at any time, including after end; officer explicitly declares whole batch returned usable; timestamp returnedAt; custody removed; coverage preserved.
APPROVED consumable -> consume -> CONSUMED during active interval; subtract Resource.totalQuantity exactly q once in same transaction; timestamp consumedAt.
APPROVED -> cancel -> CANCELLED/USER_REQUEST before expiry; after end mark reason EXPIRED (no second stock release); preserve approvedAt.
APPROVED expired -> expiry cleanup -> CANCELLED/EXPIRED, effective hold already zero.
CANCELLED never-approved -> delete allowed when approvedAt null.
CANCELLED historically approved -> never physically delete.
RETURNED, CONSUMED and ISSUED cannot cancel/delete through current API.
A09 withdraw consent changes no stock lifecycle status; clears phone and sets smsConsent=false; repeated revocation on existing record harmless.
All prohibited transitions return controlled 409 without change.

### Guarded Resource correction

Request: name,totalQuantity,expectedTotalQuantity. Lock Resource and check actual total==expected, else 409 STALE_RESOURCE_QUANTITY. Compute H/I at same time after lock. For reusable new T >= H+I; consumable new T >= H. Proposed total in 0..1e6. Preserve type. On lost HTTP response, do NOT blindly retry modifying POST, because commit might have succeeded. Re-fetch and reconcile.

### Guarded Requirement reduction

Require 1..1e6 and Project not ended. Lock Project then Resource, obtain accurate covered and MAX single live PENDING quantity. New Q >= max(Covered,maxPending). Pending sum not used since competing pending requests do not reserve demand. A historically CANCELLED/never-approved Allocation does not constrain Q after cancellation.

### Protected Project dates

If either date changes: new start future, new end>new start, no Allocation with nonnull approvedAt under any Requirement of Project. This preserves historical approved context even if previously approved Allocation now CANCELLED. Name-only update may preserve old date, subject to name validation. No department change.

### Deletion constraints

Department: no Projects.
Project: no Requirements.
Resource: totalQuantity==0 and no Requirements.
Requirement: no Allocation records.
Allocation: status PENDING or CANCELLED with approvedAt==null.
The database enforces foreign-key integrity; components return domain conflicts when known, and must handle DB integrity races without inappropriate 500 if recognized.

### Historical limitations and truthful UI descriptions

No partial return/damage, physical verification, stock movement ledger, serialized devices, procurement or multi-org ownership. A RETURN is officer-recorded usable whole-batch receipt. No real authentication or identity verification. A Resource total correction lacks event ledger; Git history is not data audit. Clearly disclose in About/Demo.

### Invariant IDs for tests

INV01 Resource total nonnegative.
INV02 Resource availability never negative.
INV03 Requirement coverage <= demand.
INV04 Allocation quantity immutable.
INV05 ISSUE once.
INV06 RETURN once.
INV07 CONSUME once.
INV08 RETURN only after valid ISSUE.
INV09 RETURN removes custody, not historical coverage.
INV10 CONSUME deducts physical total exactly once.
INV11 CANCELLED never reserves.
INV12 approvedAt remains historical.
INV13 expired unfulfilled approved hold zero.
INV14 overdue ISSUED custody remains unavailable.
INV15 stale total correction refuses overwrite.
INV16 business errors/rollback do not partially commit.
INV17 Project date edit doesn't alter historical approved periods.
INV18 FK references valid.
INV19 restart persistence.
INV20 scheduler one automatic claim per Allocation, not guaranteed handset delivery.


---

# MASTER SECTION 6: 05_API_26_ROUTES_AND_RESPONSES.md

# 05 — Complete HTTP/JSON contract: 25 original routes + 1 proposed additional route

All external URLs begin with http://127.0.0.1:8080/api (actual port configured at runtime). GET for retrieval, POST for submissions/updates/deletes per original course assignment and Word proposal. Components are Spring-managed, injected via @Autowired by Jersey @Component controllers; business rules not written in controllers. Every create returns 201; other successful calls 200; list none returns []; successful delete response {"message":"Record deleted."}; errors have 'code' (new optional additive field) and 'message' (original required field).

Entity response formats

Department:
~~~json
{"id":1,"name":"Logistics"}
~~~
Project (dates PHT local string):
~~~json
{"id":2,"departmentId":1,"name":"Welcome Workshop","startAt":"2026-11-20T09:00:00","endAt":"2026-11-20T12:00:00"}
~~~
Resource:
~~~json
{"id":3,"name":"Extension cords","type":"REUSABLE","totalQuantity":10,"availableQuantity":6}
~~~
Availability: MUST PRESERVE original TWO fields:
~~~json
{"resourceId":3,"availableQuantity":6}
~~~
Requirement:
~~~json
{"id":4,"projectId":2,"resourceId":3,"quantity":6,"allocatedQuantity":4,"shortageQuantity":2}
~~~
Allocation REDACTED revised DTO:
~~~json
{"id":5,"requirementId":4,"quantity":4,"borrowerName":"Fictional representative","contactPresent":false,"smsConsent":false,"status":"PENDING","createdAt":"2026-11-13T11:00:00","approvedAt":null,"issuedAt":null,"returnedAt":null,"consumedAt":null,"cancelledAt":null,"cancellationReason":null,"expiredPendingCleanup":false,"reminderAttempted":false,"reminderOutcome":"NONE"}
~~~
Never return full phoneNumber in GET list or response. 'contactPresent' boolean, 'reminderAttempted' derived from claim; 'expiredPendingCleanup' time-derived. Provider message SID should not be unnecessarily surfaced in public UI.

DepartmentController — 4 routes
D01 POST /api/departments. Input {"name":"Logistics"}. Component DepartmentComponent.create(name). Response 201 Department. 400 invalid name; 409 DUPLICATE_NAME normalized unique name.
D02 GET /api/departments. Input none. DepartmentComponent.list(). Response 200 array (or []). No anonymous list authentication in local prototype.
D03 POST /api/departments/{id}/update. Input {"name":"Operations"}. DepartmentComponent.update(id,name). Response 200 Department. 400 invalid; 404 target absent; 409 duplicate.
D04 POST /api/departments/{id}/delete. No body. DepartmentComponent.delete(id). Response 200 deletion message. 404 absent; 409 DEPENDENCY_EXISTS if Projects reference it.

ProjectController — 4 routes
P01 POST /api/projects. Input {"departmentId":1,"name":"Workshop","startAt":"2026-11-20T09:00:00","endAt":"2026-11-20T12:00:00"}. ProjectComponent.create(departmentId,name,startAt,endAt). Response 201 Project. 400 invalid name/date, start not future or end <= start; 404 Department absent.
P02 GET /api/projects?departmentId=1 (optional filter). ProjectComponent.list(departmentId). Response 200 array; a syntactically valid missing filter ID => []; invalid nonpositive filter => 400.
P03 POST /api/projects/{id}/update. Input {"name":"Workshop updated","startAt":"2026-11-20T09:00:00","endAt":"2026-11-20T12:00:00"}; Department cannot change. ProjectComponent.update(id,name,startAt,endAt). 200 Project; 404 absent; 400 invalid dates; 409 PROJECT_DATES_LOCKED on changed dates after any historical approval. Name-only edit with unchanged past date allowed.
P04 POST /api/projects/{id}/delete. No body. ProjectComponent.delete(id). Response 200 deletion message; 404 missing; 409 DEPENDENCY_EXISTS while Requirements exist.

ResourceController — 5 routes
R01 POST /api/resources. Input {"name":"Extension cords","type":"REUSABLE","totalQuantity":10}. ResourceComponent.create(name,type,totalQuantity). Response 201 Resource with initially equal available. 400 invalid type/quantity/name; 409 DUPLICATE_NAME. NO REQUIRED 'unit' property.
R02 GET /api/resources?type=REUSABLE (optional filter). ResourceComponent.list(type). Response 200 Resource[]; invalid enum => 400; no matches => [].
R03 POST /api/resources/{id}/update. Input {"name":"A4 paper sheets","totalQuantity":120,"expectedTotalQuantity":70}. ResourceComponent.update(id,name,totalQuantity,expectedTotalQuantity). Response 200 Resource; 404 missing; 400 invalid; 409 DUPLICATE_NAME, STALE_RESOURCE_QUANTITY, QUANTITY_BELOW_COMMITMENTS. Type unchanged. Expected field is intentional CHANGE from original proposal; update proposal and Postman accordingly. This expected-value guard is NOT a full revision counter; ABA reuse possible if same total recurs.
R04 POST /api/resources/{id}/delete. No body. ResourceComponent.delete(id). 200 deletion; 404 absent; 409 DEPENDENCY_EXISTS if Requirement reference or RESOURCE_NOT_EMPTY if totalQuantity>0.
R05 GET /api/resources/{id}/availability. ResourceComponent.getAvailability(id). Response exactly {"resourceId":id,"availableQuantity":n}; 404 missing; impossible negative state sanitized 500 INTERNAL_ERROR. No expanded approvedQuantity/issuedQuantity fields.

RequirementController — 4 routes
Q01 POST /api/requirements. Input {"projectId":2,"resourceId":3,"quantity":6}. RequirementComponent.create(projectId,resourceId,quantity). 201 Requirement with covered 0, shortage 6. 404 parent missing; 400 invalid quantity; 409 DUPLICATE_REQUIREMENT, PROJECT_ENDED. DB unique pair enforced.
Q02 GET /api/requirements?projectId=2 (optional). RequirementComponent.list(projectId). 200 array with derived covered, shortage; unknown positive filter => []; invalid filter =>400.
Q03 POST /api/requirements/{id}/update. Input {"quantity":8}. RequirementComponent.update(id,quantity). 200 Requirement; 404 missing; 400 invalid; 409 PROJECT_ENDED or QUANTITY_BELOW_COMMITMENTS if below effective covered or largest live PENDING request.
Q04 POST /api/requirements/{id}/delete. No body. RequirementComponent.delete(id). 200 deletion; 404 missing; 409 DEPENDENCY_EXISTS if any Allocation history.

AllocationController — 8 original + one added = 9 routes
A01 POST /api/allocations. Required input {"requirementId":4,"quantity":4,"borrowerName":"Fictional representative"}; optional "smsConsent":false, optional "phoneNumber" (may only be stored when consent true and valid). AllocationComponent.create(requirementId,quantity,borrowerName,phoneNumber,smsConsent). 201 Allocation with PENDING, stock unaffected. 400 invalid fields or quantity > requirement, missing destination when consent true; 404 Requirement missing; 409 PROJECT_ENDED.
A02 GET /api/allocations?requirementId=4&status=PENDING (both optional; both filters conjunctive). AllocationComponent.list(requirementId,status). 200 redacted Allocation[]; valid no matches []; invalid status 400.
A03 POST /api/allocations/{id}/approve. No body. AllocationComponent.approve(id). 200 APPROVED. 404 missing; 409 if not PENDING, Project ended, insufficient stock, Requirement overcoverage. Early approval before start allowed.
A04 POST /api/allocations/{id}/issue. No body. AllocationComponent.issue(id). 200 ISSUED. 409 if not APPROVED reusable or now outside Project period. Reserve/issued transition DOES NOT double-deduct availability.
A05 POST /api/allocations/{id}/return. No body. AllocationComponent.returnItems(id). 200 RETURNED. 409 if not currently ISSUED REUSABLE. Allows late return after Project ended; operator declares full batch usable, no partial/damaged.
A06 POST /api/allocations/{id}/consume. No body. AllocationComponent.consume(id). 200 CONSUMED. 409 if not APPROVED consumable or outside active period; deduct Resource total atomically once.
A07 POST /api/allocations/{id}/cancel. No body. AllocationComponent.cancel(id). 200 CANCELLED with reason USER_REQUEST when before end; EXPIRED after Project end. 409 for ISSUED/RETURNED/CONSUMED or previously CANCELLED. PENDING cancellation never changes stock. APPROVED cancellation removes effective hold, not physical total.
A08 POST /api/allocations/{id}/delete. No body. AllocationComponent.delete(id). 200 deletion. 409 unless never-approved PENDING/CANCELLED with approvedAt null. Physical deletion protects historical approvals.
A09 POST /api/allocations/{id}/revoke-sms-consent. No body. AllocationComponent.revokeSmsConsent(id). 200 Allocation; clear phoneNumber and smsConsent false; lifecycle/inventory unchanged; repeated existing-record revocation harmless. 404 if missing. This is one OPTIONAL additional privacy operation relative to original 25.

Shared HTTP error taxonomy (proposed addition to original message-only error)
- 400 VALIDATION_FAILED: malformed JSON, missing required field, nonpositive IDs, invalid enum/quantity/date/client-controlled field.
- 400 INVALID_PROJECT_PERIOD: end<=start or invalid new start.
- 404 NOT_FOUND: path target or required parent FK absent.
- 409 DUPLICATE_NAME, DUPLICATE_REQUIREMENT, DEPENDENCY_EXISTS, RESOURCE_NOT_EMPTY.
- 409 INSUFFICIENT_STOCK, REQUIREMENT_ALREADY_COVERED, QUANTITY_BELOW_COMMITMENTS, STALE_RESOURCE_QUANTITY.
- 409 INVALID_ALLOCATION_STATE, RESOURCE_TYPE_MISMATCH, OUTSIDE_PROJECT_PERIOD, PROJECT_ENDED, PROJECT_DATES_LOCKED, HISTORY_PROTECTED.
- 503 RETRY_REQUIRED only for recognized transient contention/deadlock, no automatic non-idempotent POST retry.
- 500 INTERNAL_ERROR sanitized unexpected server defect, including invariant breach; input constraint violations 400 versus server return-value violation 500.
Example:
~~~json
{"code":"INVALID_ALLOCATION_STATE","message":"Only approved reusable allocations can be issued."}
~~~
Delete:
~~~json
{"message":"Record deleted."}
~~~

Creation 201, other success 200, empty list [] and unknown positive filter => 200 []. Unknown required FK => 404. No raw ORM entity JSON. UTC conversion must not shift declared PHT wall times. Reject or safely ignore unknown JSON fields deliberately and test; do not silently permit server fields.

Component method signatures proposed
DepartmentResponse create(String name); List<DepartmentResponse> list(); DepartmentResponse update(Long id,String name); DeleteResponse delete(Long id).
ProjectResponse create(Long departmentId,String name,LocalDateTime startAt,LocalDateTime endAt); List<ProjectResponse> list(Long departmentId); ProjectResponse update(Long id,String name,LocalDateTime startAt,LocalDateTime endAt); DeleteResponse delete(Long id).
ResourceResponse create(String name,ResourceType type,Integer totalQuantity); List<ResourceResponse> list(ResourceType type); ResourceResponse update(Long id,String name,Integer totalQuantity,Integer expectedTotalQuantity); DeleteResponse delete(Long id); ResourceAvailabilityResponse getAvailability(Long id).
RequirementResponse create(Long projectId,Long resourceId,Integer quantity); List<RequirementResponse> list(Long projectId); RequirementResponse update(Long id,Integer quantity); DeleteResponse delete(Long id).
AllocationResponse create(Long requirementId,Integer quantity,String borrowerName,String phoneNumber,Boolean smsConsent); List<AllocationResponse> list(Long requirementId,AllocationStatus status); AllocationResponse approve(Long id), issue(Long id), returnItems(Long id), consume(Long id), cancel(Long id), revokeSmsConsent(Long id); DeleteResponse delete(Long id).

Frontend public TypeScript model must match JSON, not persisted entity graph. Frontend derives valid UI action availability but server always rechecks transactionally. Frontend should not synthesize new GET-by-id endpoints; use list and local lookup. GET lists are not atomic cross-endpoint snapshots; show refresh/retry and avoid false claims of snapshot consistency.


---

# MASTER SECTION 7: 06_PERSISTENCE_TRANSACTIONS_JPQL.md

# 06 — Java components, repositories, JPQL, locking, concurrency, atomicity

## Course-compatible structure

Jersey JAX-RS @Component Controller (@Path, @GET, @POST, @Consumes JSON, @Produces JSON) -> @Autowired Spring Component (validation, business rule, @Transactional) -> JpaRepository (derived and @Query JPQL) -> MySQL InnoDB. Controller does NOT directly update ORM graph. DTO request/response classes avoid exposing phoneNumber or bidirectional JPA relations and prevent clients writing server-computed fields.

Jersey ResourceConfig register each @Component controller individually to work in packaged archives. Example @ApplicationPath("/api") + controller @Path("/departments"), no double prefix. @EnableScheduling on app, but do NOT copy SchedulingApp teaching example's exclusions disabling DataSource/JPA. Spring @Transactional uses proxy semantics; do not rely on self-invocation or @PostConstruct proxied transaction. Use a separate Spring bean for @Transactional expiry-one and reminder-claim methods.

## Repository contracts

DepartmentRepository extends JpaRepository<Department,Long>:
- existsByNameKey(String nameKey).

ProjectRepository:
- findByDepartment_Id(Long departmentId).
- existsByDepartment_Id(Long departmentId).
- Optional<Project> lockById(Long id) @Lock(PESSIMISTIC_WRITE) and explicit JPQL; verify actual behavior.

ResourceRepository:
- existsByNameKey(String nameKey).
- findByType(ResourceType type).
- Optional<Resource> lockById(Long id) @Lock(PESSIMISTIC_WRITE), explicit query.

ResourceRequirementRepository:
- findByProject_Id(Long projectId).
- existsByProject_Id(Long projectId).
- existsByResource_Id(Long resourceId).
- existsByProject_IdAndResource_Id(Long projectId,Long resourceId).
- Appropriate locked query or reload for update/delete, respecting Project then Resource.

AllocationRepository:
- findByRequirement_Id(Long requirementId).
- findByStatus(AllocationStatus status).
- findByRequirement_IdAndStatus(Long requirementId,AllocationStatus status).
- existsByRequirement_Id(Long requirementId).
- sumEffectiveHolds(resourceId,approved,now).
- sumIssuedCustody(resourceId,issued).
- sumCoveredQuantity(requirementId,completedStatuses,approved,now).
- maxPendingQuantity(requirementId,pending).
- countHistoricallyApproved(projectId).
- findExpiredCandidateIds(now,limit).
- findReminderCandidateIds(now,limit).
- explicit lockById of Allocation.
Do NOT redeclare JpaRepository inherited save, findById, findAll, delete unnecessarily.
JPQL references Java property names, not physical SQL columns.
Aggregates SUM(Integer) should be Long; handle null as zero with safe long arithmetic. Empty collection repository returns empty list; ordinary scalar aggregate can return null.

## Candidate JPQL (must compile and run on actual Spring Data/JPA stack)

Effective holds:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.resource.id = :resourceId " +
       "AND a.status = :approved " +
       "AND a.requirement.project.endAt > :now")
Long sumEffectiveHolds(@Param("resourceId") Long resourceId,
                       @Param("approved") AllocationStatus approved,
                       @Param("now") LocalDateTime now);
~~~

Issued custody:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.resource.id = :resourceId " +
       "AND a.status = :issued")
Long sumIssuedCustody(@Param("resourceId") Long resourceId,
                      @Param("issued") AllocationStatus issued);
~~~

Requirement covered:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.id = :requirementId " +
       "AND (a.status IN :completed " +
       "OR (a.status = :approved " +
       "AND a.requirement.project.endAt > :now))")
Long sumCovered(@Param("requirementId") Long requirementId,
                @Param("completed") Collection<AllocationStatus> completed,
                @Param("approved") AllocationStatus approved,
                @Param("now") LocalDateTime now);
~~~
Completed = ISSUED, RETURNED, CONSUMED exactly. Verify 'IN :completed' binding syntax and no empty collection (always 3 values).

Max pending:
~~~java
@Query("SELECT MAX(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.id = :requirementId " +
       "AND a.status = :pending")
Integer maxPendingQuantity(@Param("requirementId") Long requirementId,
                           @Param("pending") AllocationStatus pending);
~~~

Historical approved:
~~~java
@Query("SELECT COUNT(a) FROM Allocation a " +
       "WHERE a.requirement.project.id = :projectId " +
       "AND a.approvedAt IS NOT NULL")
Long countHistoricallyApproved(@Param("projectId") Long projectId);
~~~

Expiry candidate query:
~~~java
@Query("SELECT a.id FROM Allocation a " +
       "WHERE a.status IN :unfulfilled " +
       "AND a.requirement.project.endAt <= :now " +
       "ORDER BY a.id")
List<Long> findExpiredCandidateIds(
  @Param("unfulfilled") Collection<AllocationStatus> unfulfilled,
  @Param("now") LocalDateTime now,
  Pageable pageable);
~~~
unfulfilled = PENDING, APPROVED. Use PageRequest.of(0, boundedNumber) API version compatible with chosen Spring Data. Query candidates then process one ID per small transaction; lock after lookup and recheck.

Reminder candidate selection should JOIN through Requirement/Resource/Project and filter ISSUED, ResourceType.REUSABLE, consent=true, phone not null, reminderClaimedAt null and endAt within [now,now+24h). Query returns IDs only; selection is NOT final eligibility proof. Recheck after locking Allocation and using same clock sample.

Pessimistic locks:
~~~java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Resource r WHERE r.id = :id")
Optional<Resource> lockById(@Param("id") Long id);
~~~
Analogous Project/Allocation. Test Hibernate SQL generated and race results; do not assume all lock methods compile by reading source only.

## Correct lock order and transaction boundaries

Inventory mutating commands:
1. Determine associated Project/Resource identifiers (prelookup can be stale; never trust after locks).
2. Start Spring transactional method (prefer verified READ_COMMITTED with InnoDB).
3. Lock Project row.
4. Lock Resource row.
5. Reload/refresh Requirement, Allocation and related associations, and lock Allocation when changing it.
6. Sample one Clock instant in Asia/Manila.
7. Re-evaluate current state, FK identity, project dates, available H/I and Requirement coverage.
8. Persist all related modifications.
9. Commit; on refusal or exception rollback.

Resource total update locks Resource only and computes obligations under that lock. Reminder-only claim/revoke locks Allocation without then acquiring Project/Resource WRITE locks. This avoids reverse lock-order cycle; inventory commands lock Project/Resource first, then Allocation. Queries that read parents without write locks in SMS are advisory; final pre-dispatch recheck cannot absolutely eliminate send-return race once outbound call starts.
Revalidate entities after waiting for lock. JPA's persistence context can retain stale prelock instances; explicit refresh/re-query/clear as appropriate, verified in tests. Ensure completion statuses/timestamps included in fresh DB aggregate (flush/refresh), and compare immediate DTO to a fresh GET after commit.

Approve:
- Require PENDING and Project.endAt > now; early start allowed.
- require alloc.quantity <= availableResource AND <= Requirement.quantity - covered.
- Save APPROVED and approvedAt once.
- Failures leave PENDING unchanged.

Issue:
- Require APPROVED, REUSABLE, startAt <= now < endAt.
- Save ISSUED/issuedAt, no stock total decrement.

Return:
- Require ISSUED REUSABLE; allow late; mark RETURNED/returnedAt; no stock total adjustment.
- Officer declares whole batch usable, not objective physical proof.

Consume:
- Require APPROVED, CONSUMABLE, active period, underlying Resource.total >= quantity.
- Resource.totalQuantity -= quantity; Allocation.status=CONSUMED; consumedAt=now in SAME transaction.
- After commit availability remains old available as hold disappears and stock drops same q.
- Force test exception after one write and prove both rollback.

Cancel:
- PENDING/APPROVED only; after expiry reason EXPIRED, otherwise USER_REQUEST. Preserve approvedAt. No second physical stock decrease.
Delete:
- Only PENDING or CANCELLED and approvedAt null; actual DB deletion.
Requirement update:
- Project and Resource locks; newQ >= max(covered,largest live pending); refuse Project ended.
Project date update:
- Project lock and check countHistoricallyApproved>0; changed dates require new future start/end; unchanged dates allow name-only.
Project/Resource/Requirement create and delete must respect same lock conventions for races. Department delete vs Project create depends on DB FK and appropriate locking; catch recognized integrity collision and map to 409 when supportable, not generic success.

## Race tests

1. Resource five units, two PENDING Allocation requests qty four for different Requirements: parallel APPROVE, exactly one success, available >=0.
2. Same Requirement qty six, two PENDING qty four, different rows: parallel APPROVE, exactly one success, covered<=6.
3. Concurrent consume and Resource stock correction with expectedTotalQuantity stale: one correct winner, no overwritten stock.
4. Concurrent Requirement update and approval: never coverage > demand.
5. Project date edit and approval: historical approval prevents date change if approval happens first; if date changed first, subsequent approval must use new times.
6. Expiry cleanup vs return/issue: lock and reload; no fraudulent physical return.
7. Reminder result recording vs RETURN must not overwrite state via stale detached entity.
8. Concurrent Resource create with same normalized name: DB unique index wins, no duplicate.
9. Concurrent Requirement create for same pair: DB unique index wins.
10. Lost HTTP response after successful COMSUME: retry state CONSUMED refused; never double deduct. (Correct spelling CONSUME in source code.)

## Error/rollback semantics

Validation exceptions from request => 400; server-generated DTO constraint defect => 500; missing parent/target => 404; business conflict => 409; recognized lock deadlock/timeout => 503 or mapped safe retry; unexpected DB integrity internal defect => 500 sanitized unless known duplicate/reference collision; never print SQL stack in public JSON.
A rejected business operation should not commit partial writes. A missing HTTP response DOES NOT establish DB rollback; client must re-fetch.
No automatic POST retries by frontend or scheduler after uncertain commit. Ensure exception classes are unchecked or @Transactional(rollbackFor=...) for checked domain exceptions as appropriate.

## Test evidence

For each state-changing operation check HTTP code, persisted state, and independent invariant. Sequential Postman can exercise route behavior; only real parallel DB tests prove concurrency. Test Java compiler/JPA initialization/SQL/timeout details with actual environment, not just this document.


---

# MASTER SECTION 8: 07_RETROFIT_SCHEDULING_SECURITY.md

# 07 — Retrofit/Gson, SMS, scheduling, security, privacy, demo-only policy

## Course interface

Printed assignment says "RetroJit", but course examples and syllabus use Retrofit and Gson. Implement actual outbound Retrofit from Java backend; frontend fetch to OrgResource API does not satisfy external-service requirement.

Course FakeTwilioController maps @Path("/fake_twilio"), @POST @Path("/send"), consumes application/x-www-form-urlencoded fields To, MessagingServiceSid, Body; Authorization header required and PRINTED by simulator (never send real auth). Local configured port in original course files 9997, verify actual startup. Simulator returns JSON accepted status typically HTTP 200; original Retrofit tester looked for HTTP 201 for real Twilio API, so do not assume 201 in simulator. Gson converter must be enabled and actual reply parsed. Response key messaging_service_sid needs @SerializedName.

## Modes

DISABLED default: no send, no provider credential requirement; core and expiry work.
FAKE_TWILIO: form-encoded HTTP to local simulator, dummy auth only, outcome SIMULATED when actual HTTP response accepted.
TWILIO_VIRTUAL conditional: actual authorized Twilio Messaging API to user's eligible controlled Virtual Phone destination, provider-accepted result only, NOT handset delivered. Trial/template restrictions are changeable and must be verified against current Twilio primary docs and actual account; never invent provider entitlement.

No real student/borrower phone number or live SMS without explicit safe authorization. A Boolean in demo JSON cannot establish real-world legally valid consent. Prototype uses only fictional/controlled destinations.

## Suggested interface

~~~java
public interface FakeTwilioApi {
 @FormUrlEncoded
 @POST("fake_twilio/send")
 Call<FakeTwilioReply> send(
   @Field("To") String to,
   @Field("MessagingServiceSid") String sid,
   @Field("Body") String body,
   @Header("Authorization") String dummyAuth
 );
}
~~~
Initialize Retrofit with verified baseUrl ending '/' and GsonConverterFactory. DTO fields body,to,status,messaging_service_sid via @SerializedName. Observe actual HTTP status, body and error stream; status acceptance != delivery.

## Return reminder eligibility

Project ends in upcoming 24h: endAt -24h <= now < endAt.
Status ISSUED (reusable only), smsConsent true, valid phoneNumber, reminderClaimedAt null.
Avoid REMINDERS for RETURNED/CANCELLED/CONSUMED or overdue after end. ISSUED after end remains custody but no new upcoming reminder; this is faithful to original proposal and should not silently become overdue SMS.

## Claim/dispatch lifecycle

Scanner @Scheduled query selects bounded IDs.
Transaction claimOne(id): lock Allocation, re-read status/consent and relevant current Project end, check unclaimed, set reminderClaimedAt=now and outcome CLAIMED, COMMIT.
Outside DB tx, last eligibility and consent check; if newly RETURNED/revoked, mark SUPPRESSED. If still eligible, form POST through Retrofit. Rejected definitive response -> REJECTED. Fake accepted -> SIMULATED. Real provider accepts -> PROVIDER_ACCEPTED. Network timeout, crash after claim -> UNKNOWN (don't automatically resend). Result persistence should only update reminder fields while locking Allocation; never save stale Allocation object and overwrite returned status.
Do NOT hold open database transaction while calling remote HTTP. Do NOT guarantee perfectly synchronized revocation/return and external dispatch after it starts; document unavoidable race.
Do NOT automatically retry uncertain send. Stale CLAIMED record can be reconciled to UNKNOWN without another send.

## Expiry job

@Scheduled separately from SMS; actual @EnableScheduling on Spring Boot app; DO NOT copy example's excluded datasource autoconfiguration. At/after Project.endAt, find PENDING or APPROVED, lock Project -> Resource -> Allocation, recheck current status/end, set CANCELLED with reason EXPIRED and cancelledAt. For APPROVED, stock effective hold was already zero at end; cleanup should not release again. For PENDING, no stock effect. For ISSUED overdue, no auto return. Preserve approvedAt historical marker.
Demonstrate actual invocation; a manually called method alone doesn't establish Scheduling requirement.
Configurable interval, ordinary 1h, short local demo profile for observed scan. Inject Clock for deterministic tests; no public time-modification endpoint.

## Security boundary

No authentication/authorization in baseline; one trusted local operator. Server bind 127.0.0.1 by default, no unrestricted public host. No claims of production security. UI About/Demo Guide labels as fictional academic prototype, not official Ateneo service. Do not show fake login/role UI implying real access control.

Data minimization: borrowerName fictional; phone only if consent true; revocation clears destination; API responses never expose raw number; logs never print destination or auth; prevent frontend state echo of persisted contact values.
Secrets via environment, not repo, README, Postman exported environment or CI log. Gitignore .env, test output, dumps, node_modules, target, credentials, user-specific settings. Avoid blanket ignoring required Maven wrapper or Eclipse metadata if needed for reproducible import.
No unsafe CORS wildcard + credentials; same-origin UI with Vite local proxy. Browser cannot override server-controlled IDs/status or computed values; DTOs narrow, server validation and DB constraints still apply.
Handle malformed JSON, invalid enum, invalid LocalDateTime, overflow/out-of-range quantity, duplicate normalized names, unknown FKs, concurrency, invariant failure, SQL errors with sanitized responses.
Do not expose stack traces/SQL in UI; technical logs use non-sensitive IDs. Browser must show errors without falsely claiming success.
Conditional image feature: validate actual PNG/JPEG signature, MIME, size; no filesystem paths or untrusted SVG/scripts, store bytes in Resource if confirmed required.

## Test controls

SEC01 loopback listener; SEC02 credentials excluded; SEC03 redacted Allocation JSON; SEC04 consent=false destination null; SEC05 server-controlled fields protected; SEC06 DB FKs; SEC07 no broad CORS; SEC08 simulator dummy Authorization; SEC09 no SQL leaks; SEC10 stale update refused; SEC11 frontend escapes text; SEC12 no fake success on lost POST; SEC13 demo seed non-destructive; SEC14 no unsolicited provider sends; SEC15 upload safe if added; SEC16 GitHub CI logs sanitized.

SMS01 disabled startup; SMS02 expiry independent; SMS03 eligible claim one; SMS04 second scan none; SMS05 return before claim; SMS06 revocation before claim; SMS07 return between claim and pre-dispatch recheck; SMS08 real simulator accepted JSON; SMS09 error status; SMS10 timeout unknown; SMS11 restart no duplicate; SMS12 window boundaries; SMS13 no physical delivery claim; SMS14 missing destination suppression; SMS15 no real secret in simulator logs.

## Privacy and academic integrity

No real campus inventory, demand measurements, interviews, real user accounts or provider approval asserted. No actual project Java code/DB/SMS tests are claimed by this file. Document outside AI/research assistance truthfully as required by syllabus Certificate of Authorship. Check Canvas for explicit generative-AI policies; cannot infer from syllabus absence.


---

# MASTER SECTION 9: 08_UI_REACT_TYPESCRIPT.md

# 08 — Complete React/TypeScript UI specification (real functional application)

USER DECISION: Actual working UI is mandatory as a final outcome even though course calls UI a bonus. It cannot replace backend Jersey requirements and should not delay imminent skeleton ZIP/video. No fake dashboard/placeholder database presented as complete. React must use real HTTP/JSON and server-authored stock/coverage.

## Framework architecture

Vite + React + TypeScript (strict mode) with pinned npm lockfile. HashRouter to make deployed routes '/#/projects' possible while backend owns '/api/*'. Dev Vite proxy '/api' -> http://127.0.0.1:8080 (preserve prefix). Bundled production assets into Spring Boot static classpath, Jersey @ApplicationPath("/api") and individual controller registration. Verify real packaged JAR serves '/' and '/api/departments', CSS/JS and refresh. If legacy servlet mapping fails, documented two-process local run fallback until corrected, not fake bundled success.

Suggested frontend dirs: src/api (typed client, mutations), src/types (DTO), src/components (layout, cards, table, modal, form), src/features/dashboard,departments,projects,resources,requirements,allocations,reminders, src/styles, src/tests, e2e.

No separate Node backend, Next.js server, Supabase, Firebase, DB or auth provider. No manual send SMS endpoint; reminder is scheduled Java backend.

## Design system

Professional restrained academic operations app:
- Light neutral background; white cards; primary deep blue/indigo; restrained green success, amber warning, red destructive.
- Accessible sans-serif body 14–16px; visible heading hierarchy; 4/8/12/16/24/32px spacing.
- Consistent border radius ~10–14px, subtle borders, little shadow.
- Desktop 240–260px sidebar; mobile collapsible drawer and no page overflow; responsive grids and table horizontal scroll if necessary.
- Persistent app name "OrgResource", contextual "Local academic prototype" notice, accessible nav and keyboard focus.
- Buttons descriptive, no generic unlabeled icons; non-color status text; WCAG-minded contrast, logical labels/aria where appropriate.
- Loading skeletons only when actually loading; empty state with next action; error with Retry; success after real response; no fabricated sample stats.

## Screen architecture and functions

Dashboard:
- Load GET /departments,/projects,/resources,/requirements,/allocations (parallel where useful).
- Count Departments, Resource TYPES, active Projects, upcoming Projects, pending Allocation REQUESTS, outstanding ISSUED BATCHES, overdue ISSUED BATCHES, Requirements with positive shortage, CONSUMED Allocation records. Do NOT sum 10 cords + 100 sheets as one meaningful "110 resources available" metric.
- Show recent Allocation rows, upcoming/overdue lists, actions linking to real workflows.
- Active Project derived startAt<=now<endAt in Asia/Manila; upcoming start>now, ended end<=now.
- Partial failure must show affected metrics as unavailable, not fake zero/false fresh success. Cross-endpoint reads may not form atomic snapshot; label refresh time and provide refresh.

Departments:
- GET list, create POST, rename POST, delete POST only after user confirms.
- Search/filter locally by display name, stable sort.
- Duplicate normalized name 409, dependency 409, missing ID 404, empty state, responsive form and table.
- Do not remove row until server returns success.

Projects:
- GET list filter departmentId, creation form with Department picker, name, PHT start/end.
- HTML datetime-local input sends YYYY-MM-DDTHH:mm:ss *local wall time* directly, not toISOString converting UTC unexpectedly.
- UI Upcoming/Active/Ended derived labels; not stored enum.
- Edit name and dates; name-only update preserves unchanged old dates; date changes may be 409 if historic approval.
- Project detail view composes Requirements/Allocations data using existing list APIs (no invented GET-by-ID).
- Delete only when no Requirements; explain refusal.

Resources:
- GET list/filter type, create Resource name,type,total; display totalQuantity vs availableQuantity and meaning by REUSABLE/CONSUMABLE.
- Do not display unit column unless deriving from name; no required 'unit' field.
- Update name/total using expectedTotalQuantity set from last server observation; if 409 stale, re-fetch current, explain conflict, force user to review (never automatic overwrite).
- Safe delete total=0 and no Requirements, confirm identity, error feedback.

Requirements:
- GET list/project filter, create with Project and Resource picker; unique pair; show required, allocated, shortage. PENDING not counted.
- Update quantity with server enforcing >= coverage and max individual pending; show explanatory 409, allow cancel pending first.
- Delete only no Allocations; no erasing history.

Allocations:
- List/filter Requirement,status; show Project name and Resource through related read-only maps; borrower label, quantity, status, lifecycle times, consent presence and reminder result.
- Create PENDING with borrower label, quantity and optional fictional test contact/consent. No real student numbers.
- On PENDING approve/cancel and delete if never approved.
- On APPROVED REUSABLE issue/cancel; on APPROVED CONSUMABLE consume/cancel; on ISSUED reusable return; RETURNED/CONSUMED read-only; CANCELLED previously approved read-only; CANCELLED never approved deletable.
- Consent revocation action independent of lifecycle (if consent currently true); POST /revoke-sms-consent clears saved phone; don't show full phone in UI.
- Return confirmation explicitly declares entire batch physically received usable. Consume confirmation clearly states full batch deduction and no undo.
- Post state-changing command must await actual result and re-fetch Allocations, Resources, Requirements, Project when relevant. No optimistic guessed balance.
- HTTP timeout or lost result: show uncertain outcome, re-fetch row/list, don't automatically retry non-idempotent POST.
- Show 409 state/race conflicts accurately, no fabricated success.

Reminders / Activity:
- Use Allocation and Project lists, not invented notification-log API. ISSUED batches due soon (end within 24h), overdue issued; show recorded reminderClaimed/Outcome and consent.
- SIMULATED label "Accepted by local simulator", PROVIDER_ACCEPTED "Provider accepted request", UNKNOWN "Outcome uncertain", not "Delivered".
- No manual Send SMS UI unless new authorized backend contract explicitly agreed.

About/Demo:
- Explain hypothetical org, five entities, stock formulas, limitations: whole-batch returns, no damage/loss, no auth, fictional local data, no campus endorsement or live SMS claim.
- Provide documentation/dev version and accessible usage help, not fake Admin account.

## Shared states every screen must handle

INITIAL_LOADING, EMPTY, READY, VALIDATION_ERROR, HTTP_404, HTTP_409, NETWORK_ERROR, SERVER_ERROR, REFRESHING, MUTATING, SUCCESS_CONFIRMED, STALE_DATA. Retry GET safely; no automatic POST retry. Modal focus trap and restore, keyboard tab order, visible focus, accessible labels, screen-reader status; test at 375px and desktop widths. Do not use disabled action as security; server revalidates.

## Typed API contract

Department {id:number,name:string}.
Project {id,departmentId,name,startAt,endAt} date strings PHT.
Resource {id,name,type:"REUSABLE"|"CONSUMABLE",totalQuantity,availableQuantity}.
ResourceRequirement {id,projectId,resourceId,quantity,allocatedQuantity,shortageQuantity}.
Allocation {id,requirementId,quantity,borrowerName,contactPresent,smsConsent,status,createdAt,approvedAt,issuedAt,returnedAt,consumedAt,cancelledAt,cancellationReason,expiredPendingCleanup,reminderAttempted,reminderOutcome} nullable timestamps and enums. Never phoneNumber in read DTO.
Error {code,message}. Availability {resourceId,availableQuantity}. Delete {message:"Record deleted."}.

HTTP client:
- relative /api paths, JSON Accept/Content-Type, AbortSignal support for GET, parse JSON failures separately, throw typed ApiError(status,code,message) for non-2xx, do not use null "success".
- Prevent two simultaneous mutation submissions from same UI form where possible, still backend is authoritative.
- No localStorage as inventory database, no mock intercepts in final browser E2E.
- Handle 200 [] from valid unmatched list filters vs 404 required FK.

## Visual quality acceptance

UI01 dashboard empty DB shows no fabricated stats.
UI02 Department create persists.
UI03 Project dates round-trip PHT.
UI04 Resource create/list and availability.
UI05 Requirement shortage.
UI06 Allocation PENDING stock unchanged.
UI07 Approve reserves.
UI08 ISSUE no second deduction.
UI09 RETURN releases custody preserves coverage.
UI10 CONSUME once.
UI11 Stale expected total 409.
UI12 Unsafe delete refused with informative message.
UI13 Consent revoke no contact leak.
UI14 browser refresh persistence.
UI15 backend restart persistence.
UI16 backend unreachable shows correct offline/error, not success.
UI17 keyboard-only core workflows.
UI18 narrow viewport all controls usable.
UI19 real backend browser E2E not all mocks.
UI20 final bundled assets work from Java JAR.

## Testing

Vitest and React Testing Library (unit/component), Playwright browser E2E after actual API live. Frontend build, lint, unit tests, E2E. Mock fetch is allowed for isolated component tests but must NOT replace real-backend acceptance. Mobile layout and accessibility manual checks plus automated semantic queries. No actual screen or performance measurement claim until tested.


---

# MASTER SECTION 10: 09_TEST_POSTMAN_DEMO_ACCEPTANCE.md

# 09 — Complete acceptance testing, fixtures, Postman and demonstration

## Test philosophy

A planning specification or mock reference model is not a running Java test. Each required code feature must be accepted based on real execution: compiler, actual relational DB, HTTP/Postman, real browser, actual scheduler, external HTTP simulator, and clean export. Every test result PASS / FAIL / BLOCKED / NOT_RUN with command, timestamp, output evidence and commit. A generated Postman JSON file is not evidence that any request executed. Static route coverage can be checked independently from runtime.

## Reproducible fictional fixture

Department "Fictional Logistics" and Project "Welcome Workshop" created with startAt in future and endAt>startAt. TWO Resources: REUSABLE "Extension cords" totalQuantity 10, CONSUMABLE "A4 paper sheets" totalQuantity 100. Requirements: 6 cords and 40 sheets. Allocations: A=4 cords, B=2 cords, C=30 sheets. A and B cannot be issued until Project actually becomes active; do not run setup and fulfillment back-to-back expecting future-start to have passed. Use injected fixed Clock in automated integration tests and real clock in video.

Expected state checkpoints:
- Initial cords available 10, covered 0; paper available100,covered0.
- A PENDING no change. A APPROVED: cords available6,covered4.
- A ISSUED: cords available6,covered4 (no second deduction).
- A RETURNED: cords available10,covered4.
- B APPROVED: cords available8,covered6.
- B ISSUED: cords available8,covered6. Leave B ISSUED through real scheduled FakeTwilio demonstration if eligible.
- B RETURNED: cords available10,covered6.
- C APPROVED qty30 paper: paper available70, covered30, total still100.
- C CONSUMED: paper available70, covered30, total becomes70 (not 40!).
- Paper Resource updated with total=120 expectedTotalQuantity=70: paper available120,covered30.
- Requirement shortage: cords0, paper10.
No callback can imply physical return when only SQL status updated.

## Disposable CRUD fixture

Separate new Department, new Project, zero-stock Resource, Requirement, never-approved Allocation. Create/list/rename each, cancel pending, revoke SMS if testing optional route, delete Allocation -> Requirement -> Resource -> Project -> Department; verify DB rows gone and main workflow unchanged. Resource delete requires total0 and no Requirement reference. This sequence was statically dependency checked, not run in Java.

## Route coverage

Department D01-D04; Project P01-P04; Resource R01-R05; Requirement Q01-Q04; Allocation A01-A09. Original proposal 25 + reviewed A09=26. See 05_API_26_ROUTES_AND_RESPONSES.md for exact path, body and errors. Postman template supplied in ZIP is importable scaffolding only; agents must add generated-ID scripts, negative assertions and run it against actual backend.

Postman environment variable names:
baseUrl; runTag; projectStartAt; projectEndAt;
crudDepartmentId,crudProjectId,crudResourceId,crudRequirementId,crudAllocationId;
workflowDepartmentId,workflowProjectId,cordResourceId,paperResourceId,cordRequirementId,paperRequirementId,allocationAId,allocationBId,paperAllocationId.
Store created IDs from real JSON response, never assume 1. For 'create' expected 201; list expected 200 array; mutators 200. Responses include code/message on failure, deletion message on success. Use unique runTag per run to avoid duplicates; check no residual environment IDs from a failed earlier run; never automatically replay POST after network uncertainty.

Example Postman post-response script for Department create:
~~~javascript
pm.test("Department created", function() {
  pm.response.to.have.status(201);
  const body = pm.response.json();
  pm.expect(body.id).to.be.a("number").above(0);
  pm.expect(body.name).to.be.a("string");
});
if (pm.response.code === 201) {
  const body = pm.response.json();
  if (Number.isInteger(body.id) && body.id>0)
    pm.environment.set("workflowDepartmentId",String(body.id));
}
~~~

Example availability:
~~~javascript
pm.test("Four approved cords reserve exactly four",function(){
 pm.response.to.have.status(200);
 const r=pm.response.json();
 pm.expect(r.availableQuantity).to.eql(6);
 pm.expect(r.resourceId).to.eql(Number(pm.environment.get("cordResourceId")));
});
~~~
Availability JSON has ONLY resourceId and availableQuantity. To independently prove Resource.total and commitments, query Resource GET and DB/Allocation history, not same function twice.

Example refusal:
~~~javascript
pm.test("Repeat CONSUME refused",function(){
 pm.response.to.have.status(409);
 pm.expect(pm.response.json().code).to.eql("INVALID_ALLOCATION_STATE");
});
~~~
Then query DB Resource total to ensure unchanged on repeat. This is not an injected rollback test.

### Time fixture

Project must be created with future start and later end. Postman variables can be generated dynamically in Philippine wall-time, but avoid browser/device timezone assumptions; use Intl DateTimeFormat with Asia/Manila or explicitly offset for demo date only. Use a 3-minute future start and a few hours duration for video after waiting; never pretend immediate fulfillment allowed. Fixed Clock tests use exact instants without wall clock wait.

## Backend test cases

BE01 Fresh startup five tables.
BE02 Department 201 and persisted.
BE03 GET Department row.
BE04 normalized duplicate 409.
BE05 referenced Department delete 409.
BE06 Project invalid interval 400.
BE07 Project foreign key absent 404.
BE08 Project rename while historical dates remain unchanged allowed.
BE09 Project date change after historically approved Allocation 409.
BE10 Resource zero-stock create, available0.
BE11 Approval 4 of available5 reserves4.
BE12 Parallel Resource approvals qty4+4 of total5: exactly one success.
BE13 Parallel Requirement approvals qty4+4 of demand6: exactly one success.
BE14 Pending Allocation doesn't reserve.
BE15 Reusable ISSUE no double deduction.
BE16 Return releases custody once.
BE17 Second return 409/no change.
BE18 Consumable CONSUME decrements total once.
BE19 Second consume 409/no change.
BE20 Reduce Requirement below current coverage 409.
BE21 Requirement10 with pending9, reduce to5 =>409.
BE22 stale Resource update =>409/no overwrite.
BE23 expired APPROVED before cleanup =>effective hold0.
BE24 expired PENDING cleanup => CANCELLED/EXPIRED no stock effect.
BE25 expired APPROVED cleanup=>CANCELLED/EXPIRED no second release.
BE26 overdue ISSUED remains unavailable.
BE27 approved then cancelled deletion =>409.
BE28 never-approved cancelled/pending deletion succeeds.
BE29 positive stock Resource delete =>409.
BE30 zero-stock unreferenced Resource delete succeeds.
BE31 duplicate Project+Resource Requirement =>409.
BE32 forced DB exception during consume rolls back Resource and Allocation.
BE33 empty aggregate quantity0.
BE34 nonexistent required parent FK =>404 no save.
BE35 malformed JSON=>400.
BE36 client-controlled generated ID/status rejected or safely ignored per documented contract, never applied.
BE37 restart preserves all data.
BE38 clean Java source import/build.
BE39 duplicate Resource/Department create concurrently => single row due unique constraint.
BE40 Project update vs approval race preserves historical-date rule.
BE41 Resource correction vs consume race preserves invariant.
BE42 Query results across status and endAt boundary coherent.
BE43 Read-only response across multiqueries stable or documented snapshot.
BE44 Client lost response cannot trigger blind duplicate mutation.

## SMS/scheduler tests

SMS01 Disabled startup with no SMS config.
SMS02 Expiry scheduled independent of SMS.
SMS03 Eligibility exactly [endAt-24h,endAt).
SMS04 One durable claim and no second scanner claim.
SMS05 RETURNED before claim suppress.
SMS06 Revoked consent before claim suppress.
SMS07 Return/revoke observed before dispatch suppress.
SMS08 Actual FakeTwilio accepted HTTP and typed Gson parsed => SIMULATED.
SMS09 Definitive HTTP rejection => REJECTED.
SMS10 Timeout/uncertain => UNKNOWN and no automatic resend.
SMS11 Restart with CLAIMED/UNKNOWN no repeated send.
SMS12 Overdue endAt<=now no upcoming reminder.
SMS13 Real provider success != handset delivery.
SMS14 Missing destination no call.
SMS15 Only simulator dummy Authorization; check no tokens in log.
SMS16 Reminder result persistence never overwrites RETURNED status.
SMS17 Expired PENDING cleanup no stock change.
SMS18 Expired APPROVED cleanup no double stock change.

## UI acceptance tests

UI01 Empty DB shows empty state/no fake stats.
UI02 Department create through real API.
UI03 Project date input PHT roundtrip.
UI04 Resource types and availability.
UI05 Requirement coverage/shortage.
UI06 Pending allocation stock unchanged.
UI07 Approval reserves.
UI08 Issue doesn't double deduct.
UI09 Return restores custody.
UI10 Consume reduces physical once.
UI11 Stale Resource update shows 409 and refresh.
UI12 Unsafe delete explanatory 409.
UI13 Consent revoke clears destination.
UI14 Browser reload shows persistent rows.
UI15 Backend restart data retained.
UI16 Backend outage yields error, no false success.
UI17 Keyboard-only core workflows.
UI18 Mobile/narrow controls accessible.
UI19 Browser E2E uses actual backend, not only mocks.
UI20 Bundled JAR serves UI and API.
UI21 Partial dashboard fetch failure not silently zero.
UI22 Lost POST response does not auto-retry.
UI23 Exact action gating by Allocation status/resource type.
UI24 Responsive table scroll, dialogs and focus restore.

## Acceptance levels and evidence

- STATIC: source reads.
- COMPILE: javac/Maven output, exit code.
- DB: real schema and query.
- HTTP: real Postman/curl against backend.
- CONCURRENCY: parallel requests and final persistent state.
- SCHEDULE: observed @Scheduled invocation.
- EXTERNAL: actual Retrofit HTTP to simulator/provider.
- BROWSER: real React UI and Java backend.
- PACKAGING: real source ZIP, checksums and tested import.
- SUBMISSION: actual Canvas receipt/video link, not performed here.

Use independent assertions, not simply calling the same helper function that could be wrong. If CHECK constraint unsupported by older MySQL, Javax validation + DB FKs/unique still required and document exact enforcement.

## Postman source-generation caveat

The included postman collection is a ROUTE TEMPLATE, not a final full test suite. Every endpoint definition has method, path, placeholder body where needed. Coding agents must amend dynamic IDs, create-flow scripts, negative tests and actual requests after implementation. Report import status separately from execution status.


---

# MASTER SECTION 11: 10_GITHUB_DEVOPS_BACKLOG_RELEASE.md

# 10 — GitHub, agent coordination, CI, backlog, packaging, demonstration and release

## Repository and file layout

Proposed repo: newonathe/OrgResource (not created in this handoff). Prefer private until user approves publication. Verify actual remote before push.

OrgResource/
  AGENTS.md
  CLAUDE.md
  README.md
  .gitignore
  .editorconfig
  .github/workflows/
  backend/pom.xml
  backend/.mvn/, backend/mvnw, backend/mvnw.cmd
  backend/src/main/java/app/SpringBootApp.java
  backend/src/main/java/app/config/
  backend/src/main/java/app/entities/
  backend/src/main/java/app/repositories/
  backend/src/main/java/app/components/
  backend/src/main/java/app/rest/controllers/
  backend/src/main/java/app/rest/errors/
  backend/src/main/java/app/dto/request/
  backend/src/main/java/app/dto/response/
  backend/src/main/java/app/integration/
  backend/src/main/java/app/scheduling/
  backend/src/main/resources/application.properties
  backend/src/main/resources/static/
  backend/src/test/java/
  frontend/package.json, package-lock.json, vite.config.ts, tsconfig.json
  frontend/src/api,types,components,features,styles,tests
  frontend/e2e
  postman/
  docs/ORGRESOURCE_MASTER_HANDOFF.md
  docs/source-audit, decisions, implementation-status, test-plan, proposal-changes
  scripts/
  evidence/skeleton,integration,frontend,release

Only create modules as actual phase needs. Do not generate empty fake app modules or README claiming features.

## Agent responsibilities and worktree isolation

Codex primary: actual course source/environment, Java/JPA/Jersey, DB, transaction engine, time/scheduler/Retrofit, Java tests, export/packaging.
Claude Code primary: independent read-only reviews, React shell and UI/UX in separate worktree, browser tests, user-facing quality, approved targeted fixes.
Roles can swap after explicit task assignment. No two writers on same worktree/file. Source of truth 'docs/ORGRESOURCE_MASTER_HANDOFF.md'; AGENTS and CLAUDE are small references, not competing specs.
If one agent opens PR on feature/backend-skeleton, other agent can review without changing branch, or use distinct branch. No merge to main, no push or destructive git operation without user permission. Check git status/remote before every task and log commit SHA.

## Branches and PR checklists

Branches may be feature/backend-skeleton, feature/crud, feature/allocation-engine, feature/scheduler, feature/ui-shell, feature/ui-workflows, test/full-regression. Use Git worktrees when parallel.
PR includes scope, source evidence, changed files, API/DB contract changes, commands executed, tests PASS/FAIL/NOT_RUN, actual screenshots only if real UI, security review, regressions, reversible rollback, reviewer signoff; no fake checks.

## CI build policy

GitHub Actions: backend JDK 8 + Maven verify, isolated MySQL 8.0 InnoDB service and test profile as needed; frontend Node compatible with Vite, npm ci, lint, unit, build; browser E2E with actual backend when available. Use current official actions versions verified at implementation (2026 docs show actions/checkout@v7, setup-java@v6, setup-node version verify). Never pin deprecated syntax blindly or leak credentials.
Do not claim CI green as proof of tests not configured. Real DB integration tests must use actual MySQL engine, not only in-memory H2. Java compile target class major version 52 for Java 8, verify javap. Frontend output must be copied into backend static classpath and packaged JAR tested. Credentials from GitHub Actions secrets only for actual providers; default SMS DISABLED.
Suggested jobs: java-build, java-db-integration, frontend-build, browser-e2e, release-packaging (later). Avoid Docker deployment, cloud registry, Kubernetes, Redis, tracing, payment API, microservices without requirements.

## Dated backlog (printed source deadlines; current Canvas unverified)

ORG001 Source+environment inspection (Nov13 P0).
ORG002 Java/Maven/Jersey bootstrap (Nov13 P0).
ORG003 Five JPA entities/DB (Nov13 P0).
ORG004 Repositories/query smoke test (Nov13 P0).
ORG005 Five controllers/components JSON basic create-read (Nov13 P0).
ORG006 Exported Eclipse-compatible source ZIP + genuine skeleton video (Nov14 P0).
ORG007 CRUD, validation, safe deletion (Nov15-17 P1).
ORG008 Transactional Allocation and stock math (Nov18-22 P1).
ORG009 Clock/Project expiry scheduling (Nov23-24 P1).
ORG010 Retrofit/Gson/FakeTwilio reminders (Nov24-26 P1).
ORG011 React shell/design system (parallel after API stable P1).
ORG012 Departments/Projects/Resources UI (after CRUD P1).
ORG013 Requirements/Allocations UI (after lifecycle P1).
ORG014 Dashboard/reminder UI (after SMS P1).
ORG015 All-routes Postman, Java regression and concurrency (Nov27-30 P1).
ORG016 Browser E2E, accessibility, responsive checks (Nov27-Dec2 P1).
ORG017 Conditional image feature only if required and confirmed.
ORG018 UI static bundling, Eclipse import and full release export (Dec1-3 P1).
ORG019 Final proposal/source audit, Certificate of Authorship and external help (Dec1-3 P1).
ORG020 Final video, actual evidence, release and academic submission by printed Dec5, subject Canvas confirmation.
Dates are proposed sequence, not actual completed milestones.

## Critical path

Environment -> working Java/Jersey/DB -> five real entity tables/repos -> controllers/JSON -> skeleton export+video.
Final backend: CRUD -> transaction correctness -> time/expiry -> external Retrofit/SMS -> integration tests.
Final frontend: API DTO contract -> React shell -> CRUD screens -> Allocation workflows -> dashboard/reminders -> actual browser tests -> bundled UI.
Agent may parallelize frontend shell after DTOs stable, but testing must be rerun when shared API changed.

## Build and release gates

Gate0 actual source/toolchain and repository state documented.
Gate1 one Jersey->Component->Repository->DB POST/GET observed.
Gate2 five mapped entities and real skeleton export/demo.
Gate3 all original CRUD/validation/deletion correct.
Gate4 concurrent approvals, consume rollback, stock formulas correct.
Gate5 clock/expiration and actual scheduler.
Gate6 outbound Retrofit/Gson HTTP to simulator and honest statuses.
Gate7 React UI integrates real API with correct UX states.
Gate8 comprehensive Postman, DB concurrency, browser E2E.
Gate9 packaged JAR / and /api serve, clean Eclipse import/export, source ZIP and checksums.
Gate10 video/source/assistance/Canvas submission reviewed; actual receipt only if verified.
Every gate can be PASS/FAIL/BLOCKED/NOT_RUN, not "looks good".

## Packaging required

- OrgResource-Eclipse-Project.zip (Java source, pom.xml and any verified portable .project/.classpath/.settings needed, wrapper, no secrets, import guide).
- OrgResource-Full-Source.zip (Java + React + docs + Postman + scripts + lockfile; no generated caches, private lecture archives unless authorized).
- Runnable Java JAR with React production static assets, if bundled build verified.
- All-Routes and Demo Postman collection/environment, non-secret.
- README/setup/run, API/DB/UI/docs, actual tests and logs, latest decision/conflict register, Video link/file, screenshots only when genuine, SHA-256 manifest, exact commit.
- Current course/syllabus final deliverables and Canvas instructions manual verification.
An Eclipse source ZIP is NOT the same as executable JAR. Maven-based Eclipse m2e import can suffice only if accepted; if literal .project export required, generate and actually test portable metadata or disclose limitation.

Package flow:
1. Freeze reviewed Git commit, inspect diff/secret scan.
2. Java JDK8 Maven clean verify + actual MySQL tests.
3. Node npm ci, frontend unit/lint/build.
4. Copy dist assets to Java src/main/resources/static (or configured asset path) and repackage.
5. Run resulting JAR, verify real GET / and GET /api/departments and HashRouter, browser E2E.
6. Build two source ZIPs with exact include/exclude list.
7. Re-extract to temp, check source/config, try clean import, compute SHA-256.
8. Record results, video/Canvas status, limitations. No invented success.

## Contingency cuts

Protect course mandatory five entities/Jersey/JSON/Spring JPA/Javax/Retrofit/Schedule/Postman/Export first. User-selected real UI remains final required scope, but polish/animations/analytics charts can be cut before functional screens. Defer sixth movement ledger, auth/public hosting, procurement, partial damaged returns, automatic anticipated-return booking, third-party campus integration, cloud deploy, complex observability. Conditional image feature only if course interpretation requires; do not pretend waived. Do not substitute fake data or stubs for required final logic.

## Risk register

R1 Legacy JDK/JAR/Maven incompatibility (critical): actual env + course code inspection, portable fallback.
R2 MySQL auth/driver/tables fail: real server checks and schema inspection.
R3 Jersey path /api/api or static root broken: explicit registration/mapping smoke.
R4 JPA derived query typo: startup+query tests.
R5 Approval concurrency stock overbooking: Resource locks + parallel DB tests.
R6 Requirement overcoverage: same lock+JPQL test.
R7 Double consumable deduction: single atomic transaction, repeat refusal and rollback.
R8 Stale stock update: expectedTotalQuantity guarded under lock.
R9 Expiry frees issued custody falsely: status/calc tests.
R10 SMS duplicate/lost response: claim/outcome persisted and no auto retry.
R11 Real Twilio auth in FakeTwilio logs: dummy only.
R12 Frontend fake-success/UTC shift: typed client and PHT tests.
R13 React bundled / served incorrectly: packaged JAR smoke.
R14 Export cannot clean import: early cross-workspace import; reveal limitation.
R15 Video fabricated or unreadable: genuine capture script and checks.
R16 Course Canvas updates unknown: student check.
R17 AI assistance misunderstood: truthful acknowledgment.
R18 ZIP source archive completeness unverified: never claim every original entry reviewed.
R19 Git worktree conflict: one writer per file and reviewed branch merge.
R20 Scope inflation/UI distraction from skeleton deadline: prioritize P0 gates.


---

# MASTER SECTION 12: 11_SOURCE_AUDIT_DECISIONS_RISKS.md

# 11 — Source-coverage and conflict audit; requirements traceability; open decisions

## Source coverage

09_ESP_PROJECT_ASSIGNMENT.pdf: directly read three pages, governing requirements including technologies, group size, >=5 entity types, components @Autowired, runtime create/delete, Postman, proposal description, skeleton exported project/video, final date/video.
08_CSCI180_SYLLABUS.pdf: directly consulted outcomes/rubric/academic integrity/Canvas policy; CLO9 image handling cited but final-project route obligation ambiguous; includes Certificate of Authorship and outside help acknowledgment.
10_ORGRESOURCE_CONCISE_PROPOSAL.docx: 12 pages indexed/extracted; 25 exact route definitions verified unique, 4+4+5+4+8, components, repository methods, five entities, consent and reminder.
03_COURSE_REFERENCE_CORPUS.txt: selected code and dependency inventory inspected. Includes Lab2/Lab3 animal sighting Jersey/Spring JPA, FakeTwilio controller, Retrofit demo, SchedulingApp, dependency JARs. Searchable text is not proof every binary original byte or picture was inspected.
01_KICKOFF_PROMPT.md, 02_MASTER_CONTEXT_DOSSIER.md, 04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md: project history and file map used; earlier extensive plans treated as drafts. Manifest earlier reported 2,611 paths and 852 unique recorded SHA-256 hashes; not independently recalculated here.
06_VIDEO_TRANSCRIPTS.md: timestamped generated AUDIO transcription only. Not actual video screen frames; technical details checked against course slides/code as possible. No request to reupload raw videos.
11_COMPLETE_SOURCE_NONVIDEO_PART1.zip and 12_COMPLETE_SOURCE_NONVIDEO_PART2.zip: provided original complete non-video archives; full original byte inspection unavailable in current agent runtime; agents should inspect exact originals when they have them.
Course slides and references: original Spring/Java/Jersey/JPA/validation/JSON/Retrofit/Twilio/scheduling sources available in project; not every slide or archive entry independently reviewed. Do not assert exhaustive semantic review of all sources.
Public official docs: Spring Boot 2.1.0, VS Code Java, Vite, MySQL, Twilio, React Router, GitHub Actions/Postman schema; date of consultation is Nov13 2026, reevaluate current changeable API/provider behavior when implementing.

## Conflict resolution / ADR registry

ADR01 Exactly five JPA entities Department,Project,Resource,ResourceRequirement,Allocation; no six-entity ledger, no nine-entity ERP.
ADR02 Preserve original 25 route method/path pairs.
ADR03 Propose A09 consent withdrawal as only 26th route.
ADR04 Original Resource create name,type,totalQuantity; NO mandatory unit column.
ADR05 Original availability response two fields only (resourceId,availableQuantity).
ADR06 Resource update add expectedTotalQuantity and 409 stale check as documented amendment.
ADR07 Global conservative holds, approval before future Project start allowed; no future return-based capacity promise.
ADR08 Whole-batch issue/return/consume; no damage/lost/partial statuses.
ADR09 LocalDateTime PHT [startAt,endAt); fixed/injected Clock for test.
ADR10 APPROVED effective hold expires at Project end (intentional behavioral amendment vs original indefinite hold).
ADR11 Expire both PENDING and APPROVED unfulfilled; never auto-return issued.
ADR12 Historical approvedAt retained and protects deletion/date change.
ADR13 Requirement lower bound includes max live PENDING and effective covered (intentional amendment).
ADR14 One durable reminder claim; no automatic retry on uncertain SMS.
ADR15 Reminder only upcoming [endAt-24h,endAt), no overdue extension (matches original).
ADR16 Simulated FakeTwilio HTTP accepted != real provider accepted != handset delivered.
ADR17 Phone number redacted from API and cleared on consent false/revoke (intentional privacy amendment).
ADR18 Javax-era Spring Boot/Jersey/JPA preserved; Maven conditional verified build path; no Spring 3/Jakarta rewrite.
ADR19 VS Code exclusive development, Eclipse-compatible exported Java source for course.
ADR20 Real React/Vite UI mandatory by user decision, but immediate skeleton P0 first.
ADR21 HashRouter initial UI navigation, /api Jersey, root static assets; verify packaged.
ADR22 No auth, one local trusted operator, fictional data, loopback only.
ADR23 Image receive/serve conditional course clarification, not invented requirement/waiver.
ADR24 No public cloud, microservices, extra infrastructure.
ADR25 Single source spec and stage-gated two-agent workflow; no unverified pass/merge.
ADR26 Strict source evidence, full archive and Canvas uncertainty visible.

## 33 course/academic traceability entries

C01 useful Ateneo service/group 1-3 -> hypothetical single-org problem, truthful member list -> Word proposal.
C02 Jersey REST -> JerseyConfig/controller registration -> actual HTTP.
C03 JSON -> DTOs and Jackson -> request/response test.
C04 Spring @Autowired -> controller->component -> source and call test.
C05 JPA/Spring Data -> five entity tables/repositories -> DB tests.
C06 Javax Validation -> constraints/request refusal -> 400/no row.
C07 Retrofit external/Twilio -> Java Retrofit/Gson adapter -> actual simulator HTTP.
C08 Scheduling -> @EnableScheduling + @Scheduled jobs -> observed run.
C09 >=5 justified entities -> domain responsibilities -> 5 real tables.
C10 optional @PostConstruct seeding -> controlled fictional initializer -> restart-safe if used.
C11 web-triggered operations -> 26 route map -> no 404 for declared paths.
C12 GET read POST submit -> method audit and actual calls.
C13 create/read all entities -> runtime create and list per each.
C14 runtime row create/delete -> disposable CRUD reverse dependency.
C15 Postman all calls -> 26-route collection imported and run.
C16 Word proposal prose/group -> actual reviewed docx.
C17 per-operation URL/input/output/errors/component -> API section.
C18 component methods steps -> algorithms/repo section.
C19 entity types/null/ranges/queries -> data dictionary and actual schema.
C20 Oct30 printed proposal date -> actual Canvas receipt if known.
C21 peer reviews other members -> real applicable review, solo interpretation check, no invention.
C22 skeleton DB/tables/data/repos -> SQL evidence.
C23 skeleton controller/components -> Postman.
C24 Nov13/14 printed dates -> Canvas verify.
C25 exported source and skeleton video -> real ZIP/video and import test.
C26 final Dec5 complete logic -> regression and submission.
C27 final recorded group discussion / accessible link -> actual video permissions.
C28 optional UI bonus -> now user-mandatory React and real browser tests.
C29 early presentation alternative -> only if actually accepted.
C30 syllabus authorship and help disclosure -> truthful statement.
C31 Canvas announcements -> manual current course check.
C32 lecture JPQL/Retrofit/JSON -> source-era compatible queries and HTTP evidence.
C33 CLO9 image handling ambiguous -> conditional two-route Resource image feature if confirmed.

## Remaining unknowns (DO NOT GUESS)

- Actual user's OS/install paths, repo preexisting state, GitHub permission/visibility.
- Actual MySQL server/JDK/Maven/Vite versions, current dependency resolutions.
- Current Canvas updates, actual upload receipts, peer review applicability.
- Whether exact Eclipse .project/.classpath vs Maven import is demanded; test/report both where possible.
- Real Twilio Virtual eligibility/trial constraints and approved destinations; FakeTwilio is course-compatible testing route.
- Whether syllabus image CLO9 is mandatory in final assignment.
- Actual original source ZIP full byte coverage; video frame observations not available.
- Actual Java/API/UI/DB code/test evidence: NONE claimed in planning ZIP.
- Actual final proposal amendment approval: NOT established.
- Actual UI implementation, screenshot/video, deployment: NOT established.

## Evidence statuses

SPECIFIED = plan defines it.
IMPLEMENTED = code exists in actual repo.
VERIFIED = observed appropriate running proof.
FAILED = check performed and failed.
BLOCKED = cannot currently run due prerequisite.
NOT_RUN = no execution attempted.
Academic submission needs actual receipt; only user/authorized tools can submit. Source path references from ChatGPT are not local agent files unless copied and mounted explicitly.

## External rationale constraints

Ateneo community need is a prototype hypothesis, not measured demand. Existing orgs may already have inventory systems, and campus facility reservations involve separate official authority; do not claim OrgResource replaces them or has API access. Only fictional seed data. No invented interviews/benefits/costs/policies.

## Acceptance gate before Codex

This planning archive can be used to start Phase O0 if transferred into accessible repo. An agent must NOT assume this archive contains a compiled project, actual tests, or direct Canvas/GitHub authentication. The master is a recommended implementation contract, not a course-approved revised proposal.


---

# MASTER SECTION 13: ORIGINAL_SOURCE_FILES_REQUIRED_FOR_CODEX.md

# Original primary source attachments that coding agents must actually be able to inspect

This handoff ZIP is a COMPLETE PLANNING PACKET, not a redistribution of the user's original uploaded course archives. Those original source bytes are not copied into this archive. To let Codex reproduce the course-compatible implementation, attach/mount original sources when permitted:

- 09_ESP_PROJECT_ASSIGNMENT.pdf
- 08_CSCI180_SYLLABUS.pdf
- 10_ORGRESOURCE_CONCISE_PROPOSAL.docx
- 03_COURSE_REFERENCE_CORPUS.txt
- 04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md
- 02_MASTER_CONTEXT_DOSSIER.md
- 01_KICKOFF_PROMPT.md
- 06_VIDEO_TRANSCRIPTS.md (timestamped AUDIO only)
- 11_COMPLETE_SOURCE_NONVIDEO_PART1.zip
- 12_COMPLETE_SOURCE_NONVIDEO_PART2.zip
- The original class/JAR example files: Jersey, Spring/JPA, Lab2/Lab3, Retrofit, FakeTwilio, Scheduling, Validation, Hibernate and Image demo if required.
- Relevant slides/PDFs already supplied in ChatGPT Project.

Do not publicly upload third-party course archives or real personal data without permission. A source filename in this packet does not mean the file is inside this ZIP. Each agent must report exact files actually opened. Do not infer video screen content from audio transcripts, or claim all original source archive entries independently reviewed without evidence.


---

# MASTER SECTION 14: PROMPTS/00_FIRST_PROMPT.md

# THE FIRST PROMPT — COPY EVERYTHING BELOW INTO CODEX

You are the lead engineer and execution auditor for OrgResource, a real academic Java enterprise systems project for CSCI 180.05. The user's development environment is VS Code, not Eclipse. You may use terminal/CLI build tools. The final product must include a real responsive React/TypeScript UI, but IMMEDIATE PRIORITY is the instructor's five-entity Java skeleton, Eclipse-compatible exported Java source project and truthful demonstration evidence. Your partner Claude Code will independently review and later implement UI slices in isolated worktrees.

READ BEFORE ACTION:
1. Read the ENTIRE accessible file docs/ORGRESOURCE_MASTER_HANDOFF.md (this archive's ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md); do not assume it is present without verifying its path and actual content.
2. Read the original current CSCI 180.05 assignment PDF, syllabus PDF and 12-page five-entity OrgResource concise proposal, plus actual Spring Boot/Jersey/JPA/Retrofit/FakeTwilio/Scheduling course source files where accessible. The project may have these attached in a separate workspace; list what you actually see, not what you wish existed.
3. Preserve the exact source-authority hierarchy: assignment > syllabus/current verified Canvas > user's VS Code/React/GitHub decisions > original compatible course code > original 25-route proposal > reviewed changes in master > historical alternatives.

NON-NEGOTIABLE TARGET:
- Exactly five JPA entities: Department, Project, Resource, ResourceRequirement, Allocation.
- Original 25 GET/POST routes preserved, 26th optional POST /api/allocations/{id}/revoke-sms-consent.
- Java 8-compatible legacy Spring Boot/Jersey javax.ws.rs, Spring @Autowired components, Spring Data JPA/JPQL, Javax Validation, MySQL InnoDB, Retrofit/Gson, @Scheduled tasks, Postman.
- No StockMovement entity, no historical nine-entity ERP, no mandatory unit field, no expanded availability DTO, no overdue SMS extension, no Spring MVC/Jakarta switch.
- Actual final UI: React+TypeScript+Vite, real API-backed data and workflows, responsive/loading/empty/error/retry, not fake mock dashboard.
- All implementation must be reviewable on GitHub under intended account newonathe, BUT DO NOT create/push/merge/publish repository without confirmed authorization and exact remote.
- Academic source exported as Eclipse-compatible project even though all daily editing is in VS Code.
- Fictional data and locally bound server. No public deployment or real user contact data.

ACTION IN THIS SESSION — FAST TRACK O0 → O1 → O2:
O0 INSPECT: Inspect current working directory and Git status/remotes, installed java/javac/maven/node/npm/mysql server/JDBC, available course files, original project .classpath/.project and JAR versions. Verify whether Java 8/Boot 2.1/Jersey 2.x/MySQL/Maven source can be compiled, and how to export an Eclipse-compatible source project. Do not rewrite frameworks based on assumptions.

If O0 has a verified workable stack, PROCEED without asking for nonessential permission:

O1 IMPLEMENT IMMEDIATE SKELETON:
- Build backend using verified course-compatible Javax/Jersey/Spring Boot.
- Establish real MySQL connection (never fabricate one).
- Add five mapped entities and generated DB tables with FK/unique constraints.
- Add five repositories, valid derived/JPQL methods.
- Add Spring components and five Jersey controller families using @Autowired forwarding.
- Basic real runtime create/read for each entity via JSON; validation; representative safe create/delete.
- Fictional seed data if needed, restart-safe and demo-only.
- Verify actual javac/Maven build, startup, actual MySQL tables/rows and live Jersey->Component->Repository->DB POST/GET. Show exact commands/results and distinguish stubs.
- Do not spend this gate building React or full Allocation engine. Do not declare phase passed from generated Java alone.

If O1 VERIFIED, PROCEED:

O2 PACKAGE IMMEDIATE COURSE DELIVERABLES:
- Create a truly Eclipse-importable Java source project ZIP with source/build dependencies and portable .project/.classpath if required; actually try clean import if environment supports it. Do not claim an untested ZIP imports.
- Prepare real Postman skeleton requests with actual responses.
- Prepare video evidence showing startup no fatal errors, database tables/fictional data, repository query/controller/component code and genuine JSON Postman. Capture real video only if authorized recording tool actually available; otherwise produce exact shot list and mark VIDEO NOT_CREATED.
- Prepare README, actual artifact paths/checksums and what is implemented vs stub.
- DO NOT SUBMIT TO CANVAS OR PUSH TO REMOTE WITHOUT USER AUTHORIZATION.

STOP conditions:
- An essential original source is missing and its precise content is needed to avoid incompatible API.
- Required toolchain/DB/port unavailable after bounded troubleshooting.
- Destructive database/git operation would be needed.
- Auth or remote access requires user credentials.
- An instructor requirement is contradictory and cannot be safely resolved.
- A test fails at a mandatory gate; fix within approved scope or report BLOCKED, do not pretend passed.

QUALITY:
- Preserve Javax/Boot/Jersey course architecture.
- Actual Spring transactions/business rules deferred from skeleton must be labeled unfinished.
- No invented interviews, Ateneo policies, real provider delivery, test evidence, video, commits, instructor approval or submission.
- No real passwords/tokens/phone numbers in source, logs or Git.
- Inspect existing changes before editing; never overwrite unknown user work.
- Do not silently modify API/entity count or transfer historical six-/nine-entity plans.

REQUIRED OUTPUT AFTER FAST TRACK:
1. Complete source/environment inventory and actual versions.
2. Original course files accessed and gaps.
3. Exact Git status/repository/remote.
4. Actual files created/changed.
5. Actual build commands and exit codes.
6. Actual database schema and sample query evidence.
7. Actual HTTP requests and JSON response evidence.
8. Exported source ZIP path/checksum and import status PASS/FAIL/NOT_RUN.
9. Video path and genuine evidence OR VIDEO NOT_CREATED with shot list.
10. Tests PASS/FAIL/BLOCKED/NOT_RUN and blockers.
11. Clear next-phase recommendation (O3) once skeleton evidence is complete.

At the end, do NOT declare completed academic submission unless verified. Ask only essential missing information and continue useful local work wherever safe.


---

# MASTER SECTION 15: PROMPTS/O0_TO_O6_BACKEND.md

# Staged backend prompts O0–O6 — use one at a time if not using fast-track

Every phase inherits master contract and must produce actual phase report. Never create a completion claim without running appropriate test. No default permission to push/merge/submit.

## O0 — Source and environment inspection (READ ONLY)

Inspect real repo directory, source map, assignment, syllabus, original concise proposal, available course ZIP examples, JDK, Java compiler, Maven, JDBC/MySQL server, Node, Vite, Git, VS Code settings, ports, .project/.classpath, classpath JARs. Verify Javax/Jersey/Spring Data compatibility and exact 25 routes. Identify existing OrgResource code, branches, secrets without displaying them, source access limitations. Test an environment smoke if safe but do not modify application files. Report valid build/import path, version matrix, potential classes/annotations, and exact O1 implementation plan. Stop on essential blocker.

## O1 — Five-entity working skeleton (P0)

Implement actual Spring Boot application, Jersey Config with individually registered @Component controllers, MySQL datasource and five entities Department, Project, Resource, ResourceRequirement, Allocation, JPA enums string/FK/index/unique, five JpaRepositories and component layer, request/response DTOs, create and list per entity. Ensure @Autowired controller->component forwarding. Validate essential fields, generated IDs, date serialization, and one representative custom query. Optional restart-safe fictional test data in demo profile. Final lifecycle functions can be unfinished stubs if accurately labeled. Do not fake persistence. Run actual Java 8 build, Spring Boot startup, five tables/FKs, repository initialization, live HTTP POST/GET on each entity, restart preserving DB, basic errors. Gate requires real evidence. Stop or progress O2 only after PASS.

## O2 — Immediate exported Java project and genuine video (P0)

Prepare Eclipse-compatible Java source ZIP, Maven/m2e import and portable .project/.classpath/.settings if literal expected. Include dependencies and README, no secrets; run clean extraction/import/build as feasible, record true status. Prepare actual Postman requests, DB SQL inspection, source walkthrough, startup video. If screen recorder not accessible, provide shot list and mark video absent; no fabricated media or Canvas submission. Record actual ZIP checksums/paths and tested commit. Gate ends only with honest export/video/evidence report.

## O3 — Full CRUD and validation

Finish all Department, Project, Resource, Requirement and Allocation create/list/update/delete operations, dynamic computed Resource/Requirement DTOs; normalized unique keys DB-enforced; referential integrity; expectedTotalQuantity Resource update; Department/Project/Resource/Requirement deletion constraints; Project future dates and name-only edits; Requirement >= covered and largest PENDING; redacted Allocation DTO; wrong input vs return-value validation mapping. Preserve original route names and original two-field availability response. Do not implement optional whole UI in this phase. Test positive/negative JSON for each operation, empty array filters, 404 missing targets/parent, 400 malformed data, 409 business conflicts, restart. No fake controller success. Gate real DB/HTTP.

## O4 — Transactional Allocation stock engine

Implement global conservative reservations, effective approved holds/issued custody and Requirement coverage formulas, @Transactional components, pessimistic Project->Resource->Allocation lock order and post-lock refresh, independent availability consistency. Implement PENDING→APPROVED, APPROVED reusable→ISSUED, ISSUED reusable→RETURNED, APPROVED consumable→CONSUMED with atomic Resource decrement, cancel and protected history. Prevent repeated fulfillment. Guard Resource expected quantity updates and Requirement reductions. Real parallel approval/Requirement race tests, stale stock correction vs consume, forced exception rollback, early approval/future Project, expiry boundary arithmetic. Report actual DB states. No StockMovement/partial return/real SMS within transactions. Gate actual concurrency/rollback PASS.

## O5 — Clock/Project expiration and actual Scheduling

Inject Clock/ZoneId Asia/Manila. Implement Project active [start,end), approved future early holds, endAt effective hold expiration, late RETURN allowed, expired PENDING+APPROVED cleanup to CANCELLED/EXPIRED only; no issued auto-return, no physical total manipulation. @EnableScheduling in Spring Boot without disabling DB auto-config. Candidate-ID scanning bounded, each candidate processed in separate transactional component call with proper lock order and state recheck. Fixed-clock tests before/at/after start/end; real @Scheduled invocation, restart idempotency, approvedAt preserved. No HTTP time-control route. Gate clock tests and observed scheduler PASS.

## O6 — Retrofit/Gson, FakeTwilio, SMS consent and one-claim reminders

Implement DISABLED startup, FAKE_TWILIO adapter using form POST To/MessagingServiceSid/Body and dummy Authorization, GsonConverterFactory typed response including @SerializedName("messaging_service_sid"), actual simulator status; optional verified TWILIO_VIRTUAL authorization separate and never fake delivery claim. Reminder window [endAt-24h,endAt), ISSUED reusable, consent, valid fictional destination, no prior claim. Short transaction commits claim; later HTTP outside transaction, then record outcome under Allocation lock without overwriting lifecycle. Handle definitive reject, timeout UNKNOWN, post-claim crash, no auto resend, return/consent suppression. Implement A09 POST revoke-sms-consent, clearing destination, same lifecycle. Actually observe scheduled scan, HTTP and DB outcome; no provider credentials in logs. Gate external/DB/scheduler PASS. Do not claim real handset delivery.


---

# MASTER SECTION 16: PROMPTS/O7_TO_O14_UI_TEST_RELEASE.md

# Staged frontend, integration, conditional image, release prompts O7–O14

All prompts inherit and require actually readable docs/ORGRESOURCE_MASTER_HANDOFF.md and current repo. A phase can begin only when its dependencies are verified. You may work on separate worktrees in parallel after API DTO contract is frozen. Each phase ends with full report: scope, source consulted, actual changes, commands/results, DB/HTTP/UI evidence, failures, security, diff and Git state, gate PASS/FAIL/BLOCKED/NOT_RUN. No unauthorized pushes, merges, Canvas submissions or fabricated screenshots.

## O7 — React/Vite/TypeScript foundation

AGENT: Claude Code or Codex assigned separate frontend worktree.
PRECONDITION: At least stable backend DTO contract and Java basic CRUD.
GOAL: Genuine modern React application skeleton using Vite, TypeScript strict, HashRouter (hash not sent to server), relative /api JSON client, Vite dev proxy preserving /api, responsive layout, sidebar/nav, header, 8 screen routes, error/loading/empty/retry/confirmation components, reusable forms/tables/dialogs, accessible focus and keyboard handling. All screen placeholders must be clearly labeled not connected. Do not fake numbers. Pin npm lockfile and verify node/Vite versions. Do not introduce Node backend, second database, Next.js, authentication, payments or a sixth JPA entity. Frontend does not calculate authoritative stock. Run npm ci after lockfile creation, TypeScript build, lint/unit tests, route navigation, 375px mobile and desktop usability, keyboard focus. Gate real frontend build and shell behavior; no claim backend integration without actual HTTP.

## O8 — Functional Departments, Projects and Resources screens

AGENT: frontend writer; backend CRUD O3 verified.
Implement Department list/search/create/update/delete via original D01-D04, confirm server response and explain 409 duplicate/dependency.
Projects list/filter, create future PHT period, name-only edit preserving dates, guarded date changes, read linked Requirements and Allocations using existing GET lists, safe delete and 400/404/409.
Resources list/filter REUSABLE/CONSUMABLE, create (NO unit field), show server total/available and semantics, update with expectedTotalQuantity precondition, 409 stale refresh and force user review rather than overwriting, delete total0/unreferenced only.
State: loading, empty, field errors, server unavailable, retry, pending mutation, success only after confirmed response, stale warning, destructive confirmation. No invented GET-by-ID routes, no localStorage stock. Run real backend browser/API tests, keyboard/mobile, refresh/persistence. Gate screens actually work against Java.

## O9 — Functional Requirements and Allocations workflows

AGENT: frontend writer; O4 business engine verified.
Requirements create/select Project and Resource; one pair unique; display quantity/allocated/shortage; modify demand with largest-pending rule; safe delete only no history.
Allocations list/filter by Requirement/status, link to related Project and Resource through existing list endpoints; create PENDING with borrower fictional and optional controlled test SMS destination/consent; approve/issue/return/consume/cancel/delete/revoke consent only valid actions. Always server-authoritative despite client-side button states. Confirm whole usable batch on RETURN and whole quantity on CONSUME; avoid pretending partial/damaged handling. Refresh all relevant data after mutating POST. Handle 409 state and 503 contention, 404, 400; after network timeout do NOT auto POST retry, re-fetch and reconcile.
Display lifecycle timestamps, consent boolean, outcomes without phone. Browser E2E: pending no hold, approve reserve, issue no second deduction, return restores, consume once, repeated commands refused, history deletion blocked. No fake success. Gate real UI+DB evidence.

## O10 — Dashboard, reminder activity and responsive finishing

AGENT: frontend writer; backend O6 and O9 dependencies.
Dashboard actual GET data; departments count, Resource TYPES (not sum unlike physical quantities), active/upcoming Projects, PENDING count, ISSUED batches, overdue ISSUED, Requirements shortage count, CONSUMED batch count. Show recent allocations and contextual actions; no fabricated metrics, no inconsistent hidden errors. The dashboard may read multiple endpoints at different instants; provide refresh and don't claim one transaction snapshot.
Reminders Activity: compute due/overdue from issued Project end, show persisted reminderOutcome SIMULATED vs PROVIDER_ACCEPTED vs UNKNOWN, no invented send-history endpoint/manual SMS button. About screen clearly says fictional local academic prototype without auth/public deployment or campus endorsement.
Polish visual hierarchy, accessible focus, mobile drawer, responsive table, positive/warning/error labels; test small screens and keyboard. Gate all UI screens connected and real browser tests.

## O11 — Whole system acceptance and contradiction audit

AGENT: Codex primary QA, Claude independent review read-only.
Do not add features. Validate 5 entity DB schema, unique FKs, exactly 26 intended routes, DTO JSON/TypeScript equality, Java JPA JPQL initialization, Javax validation and return-value error mapping, DB transactions/parallel approval/rollback, stale Resource update, Requirement coverage/shortage, PHT time boundaries, expiry scheduler, Retrofit/Gson FakeTwilio actual request, consent no duplicate, Postman import/real 26 routes plus negative cases, React component/browser E2E with real backend, restart persistence, HashRouter final jar, clean source import, security/credentials, no false evidence. Run tests, log test identifiers, commands, outputs, failures and unrun. Gate no unresolved critical correctness/academic/security defect. Static reference-model checks from planning do NOT count as Java tests.

## O12 — Conditional syllabus image feature (not automatic)

ONLY IF current final-project Canvas/instructor instruction requires receiving and serving images, add nullable Resource.imageData byte[] / imageMimeType String (same five JPA types) and POST /api/resources/{id}/image + GET /api/resources/{id}/image. Test PNG/JPEG magic bytes, MIME, small upload size, genuine database byte round trip, correct Content-Type, 404 no image, invalid/large payload, UI display. Follow actually inspected course multipart/Jersey image examples. Update proposal, Postman and route count 26->28. Do not add cloud object store or sixth entity. Gate real upload/download byte equality. If ambiguity unresolved, record decision as OPEN QUESTION, do not assert instructor waived it.

## O13 — Reproducible bundled app, Eclipse-compatible source export and ZIPs

AGENT: Codex with Claude review.
Freeze and record tested Git commit. Run real Maven JDK8 clean verify, MySQL integration, frontend npm ci/lint/test/build, browser E2E, Postman. Copy built React static assets into packaged Java app; verify executable JAR GET /, /assets, /api/departments, HashRouter refresh/navigation. Generate Java-only source ZIP containing all .java/POM/wrapper/config/example deps, tested m2e Eclipse route and portable .project/.classpath/.settings if course literal, no absolute machine paths. Generate full source ZIP Java+React+docs+Postman+lockfile. SHA-256 checksums, re-extract, secret scan, actual fresh import if feasible; mark import NOT_RUN if not. No unsupported "Eclipse import PASS" claims. Do not include node_modules, target, secrets, private student data, original course archives unless authorized.
Report file paths, sizes, checksums, exact command outputs, failures. Gate reproducible artifacts.

## O14 — Final academic evidence, proposal reconciliation and release

AGENT: Codex primary, Claude review.
Check current assignment/syllabus/Canvas if accessible; preserve original 25 endpoints and explicitly amended 26th, Resource update expectedTotalQuantity, phone redaction, expiry etc. Document actual implementation matches proposal or list deviations. Verify tested final release has course required Jersey JSON Spring JPA Javax Retrofit/Gson Scheduling Postman, 5 entities, real UI, actual restart, DB, concurrency, SMS simulator. Create truthful video storyboard and capture genuine recording only with authorized screen tool. Ensure accessible final recording link, correct instructor permissions (if applicable), Certificate of Authorship and outside AI help acknowledgments; no invented receipt, peer review, instructor approval or pilot. No final release claims without real evidence.
Final output artifact inventory and gate VERIFIED/CONDITIONALLY_READY/BLOCKED, commit SHA, checksums, setup, test matrix, limitations, instructions for actual student Canvas submission. Stop; do not submit or push without user approval.

## General acceptance verdict

PASS/VERIFIED only when suitable observed evidence exists. FAIL if executed and failed. BLOCKED if dependency/tool unavailable. NOT_RUN if never attempted. Report specific tests, no generic "everything tested" claim. Keep test scope precise: static code inspection, compilation, real DB transaction, HTTP, browser E2E, scheduler, external provider, actual import/export and academic submission are distinct evidence classes.


---

# MASTER SECTION 17: PROMPTS/REVIEW_AND_REPAIR.md

# Copy-ready independent review, repair, repo-resume and final challenge prompts

## A — Claude Code independent READ ONLY review of Codex work

Review the current OrgResource implementation against docs/ORGRESOURCE_MASTER_HANDOFF.md, the original current CSCI 180.05 assignment, syllabus, proposal and course examples. Inspect actual Git diff, test output and touched source files. DO NOT WRITE OR COMMIT during this review.

Check:
- Is source Java 8-compatible legacy Spring Boot/Jersey/Javax, not Spring MVC/Jakarta substitution?
- Exactly five entity types? Correct FKs/indexes/unique names/project-resource pair? Validation/nulls/ranges?
- Controllers truly delegate via @Autowired components, no business logic in Jersey?
- Are all original 25 route paths/methods preserved plus proposed A09? Only two-field availability response? No mandatory Resource unit?
- Do project active/expires/timezone rules work? Is historical approvedAt protected?
- Does stock math avoid double hold/consume? Are RETURNED resources available while old Requirement remains fulfilled?
- Does concurrency overbook resource or Requirement? Does stale expectedTotalQuantity guard actually reject? Does rollback work with checked exceptions and lock order?
- Are Project edits, Requirement reductions and deletions race safe?
- Does SMS genuinely use Retrofit+Gson+actual form HTTP, one claim, optional actual provider, fake acceptance not delivery, no real secrets in simulator logs?
- Is @Scheduled genuinely invoked, without disabled DB auto configuration?
- Is React UI real API-backed, responsive, keyboard accessible, with error/loading/stale flows and no fake numbers? Are datetime-local values PHT without UTC shift? Is public /api independent of static route?
- Is Maven/Eclipse-compatible source import actually verified or only documented? Does packaged JAR serve UI and API?
- Are Postman and browser tests run against actual DB or only mocks?
- Is original course-required video/export genuinely present? Any false PASS or fake screenshots? Any credentials/student identifiers in public repo?
For each finding: severity BLOCKER/HIGH/MEDIUM/LOW, exact file/line or repro, violated requirement/invariant, smallest fix, test to prove fix, and whether already demonstrated. End ACCEPT/REJECT/CONDITIONAL with explicit NOT_RUN evidence. Do not introduce unrelated framework rewrite.

## B — Codex targeted repair of accepted findings

Read the independent review report, inspect current branch, reproduce each accepted BLOCKER/HIGH defect, modify smallest set of relevant files, add regression test, execute real check, inspect diff and side effects. Preserve 5 entity schema/route contract unless explicit approved amendment. Do not change unrelated UI design or framework. Report before/after result, test failures and commit; no unauthorized merge/push.

## C — Resume once GitHub newonathe repository exists

Inspect exact verified remote newonathe/OrgResource (or actual name), main, all relevant PRs and feature branches, latest accepted commit, working-tree conflicts, docs master, AGENTS/CLAUDE. Don't trust earlier chat statements as proof current code exists. Gather environment/build/DB/test evidence, route coverage, UI implementation, known blockers, changed files and PR tests. Produce truthful implementation status and next smallest unblocked phase with copy-ready prompt. Do not merge/push/change unrelated user repositories.

## D — Full release adversarial audit (read only)

Treat project as actual course software, not proposal. Test or inspect independently where tools permit:
1. Can all registered Jersey routes execute, not just appear in source?
2. Can all five tables persist through real MySQL and restart?
3. Does Resource lock prevent concurrent stock overbook?
4. Does Requirement lock/coverage prevent concurrent excess demand?
5. Can stale Resource corrections overwrite consume?
6. Can one Allocation be issued/returned/consumed twice?
7. Can cancelled/expired approvals still reserve?
8. Can overdue ISSUED equipment incorrectly become available?
9. Can Project dates change after historical approval?
10. Can physical deletion erase approvals/completed history?
11. Can JPQL SUM/null or flush timing produce wrong totals?
12. Can DB failure partially commit consumable deduction?
13. Can scheduler duplicate SMS claims after restart?
14. Can returned/revoked Allocation still be dispatched once HTTP started (document race)?
15. Can fake SMS accidentally log real credentials?
16. Can public responses expose destination?
17. Can frontend clients set computed IDs/status?
18. Can UI claim success after lost POST response?
19. Does PHT datetime-local preserve wall-time semantics?
20. Does UI survive reload/backend restart?
21. Does UI work keyboard and mobile?
22. Does packaged JAR serve /, /assets and /api?
23. Can Java source import into Eclipse-compatible tool without absolute paths?
24. Does source ZIP contain all build files/lockfile?
25. Does Postman route template map actual 26 paths and dynamic fixture?
26. Are all course technologies evidenced, not just compiled dependencies?
27. Are any unrun tests reported PASS?
28. Is video genuine?
29. Are all proposal amendments visible and scholarly assistance acknowledged?
30. Does source manifest overclaim complete raw ZIP or video review?
Report source, actual commands, PASS/FAIL/BLOCKED/NOT_RUN, exact defect severity/repro, remediation, verdict. No edits absent authorization.


---

# MASTER SECTION 18: AGENTS.md

# OrgResource — Repository rules for Codex and other coding agents

1. Read docs/ORGRESOURCE_MASTER_HANDOFF.md completely, and relevant original course assignment/syllabus/concise proposal and code, before changing interfaces. Source authority: assignment > syllabus/current verified Canvas > user decisions > original course examples > 25-route proposal > reviewed amendments.
2. User develops in VS Code; produce Eclipse-compatible Java source export, actual MySQL backend, real React UI.
3. Exactly five JPA entities: Department, Project, Resource, ResourceRequirement, Allocation. 25 original routes plus proposed A09 consent withdrawal. Do NOT add StockMovement, a unit field, expanded availability JSON, overdue reminder extension, fake Admin auth or second backend.
4. Java backend is course-compatible Java 8 Spring Boot/Jersey, Spring @Autowired components, JPA/Spring Data/Javax, Retrofit/Gson, @Scheduled, MySQL. Modern React/TypeScript/Vite frontend has independent Node toolchain.
5. Stock math and Allocation transitions defined in master. Database transaction is source of truth. No double consume, no overbook, no unsafe deletion, no fake DB effects.
6. Implement actual responsive UI with loading/empty/error/retry and keyboard/mobile states, real server data, no fabricated analytics.
7. Local-only fictional data, SMS disabled by default; no real contacts or provider credentials in logs, commits, screenshots or simulator.
8. Inspect git status/remotes and existing user edits. One writer per file/worktree. No unauthorized push/merge/reset/database drop/Canvas submission.
9. Every phase report actual files, commands, tests PASS/FAIL/BLOCKED/NOT_RUN, schema/HTTP/UI proof, warnings, diff, commit status and next gate. Never claim static code or sample JSON is runtime proof.
10. Stop on essential blocker; otherwise implement the smallest tested slice without repeatedly seeking permission for routine local edits.


---

# MASTER SECTION 19: CLAUDE.md

# OrgResource — Claude Code instructions

The single controlling source is docs/ORGRESOURCE_MASTER_HANDOFF.md.
Also read AGENTS.md and the actual current CSCI 180.05 assignment/syllabus/proposal.
Default Claude role: independent read-only reviewer and/or React/TypeScript/UI implementation in an isolated worktree; Codex default Java/backend/DB writer. Roles may be swapped explicitly.
Never edit Codex's working tree concurrently. Before edits inspect files/diffs/branch. Preserve original five-entity, 25+1 route contracts and Jersey/Javax course stack.
React UI must actually call the Java API, handle all status/errors/empty/loading/keyboard/mobile states, not show fake data. Do not create second backend/auth/DB.
Report executed tests and evidence honestly. No invented code/video/Canvas/GitHub state; no secrets, personal contacts, false campus adoption.
Do not merge/push/reset or perform destructive operations without authorization. If assigned review-only, do not modify files.


---

# MASTER SECTION 20: TEMPLATES/UNVERIFIED_BACKEND_POM_CANDIDATE.xml

~~~text
<?xml version="1.0" encoding="UTF-8"?>
<!-- PLANNING TEMPLATE ONLY. Not compiled, resolved, or executed. Confirm actual source-era JARs and Maven dependency tree first. -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
 <modelVersion>4.0.0</modelVersion>
 <parent><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-parent</artifactId><version>2.1.0.RELEASE</version><relativePath/></parent>
 <groupId>edu.ateneo.csci180</groupId><artifactId>orgresource</artifactId><version>0.1.0-SNAPSHOT</version><packaging>jar</packaging>
 <properties><java.version>1.8</java.version></properties>
 <dependencies>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-jersey</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.glassfish.jersey.media</groupId><artifactId>jersey-media-json-jackson</artifactId></dependency>
  <dependency><groupId>org.glassfish.jersey.ext</groupId><artifactId>jersey-bean-validation</artifactId></dependency>
  <dependency><groupId>mysql</groupId><artifactId>mysql-connector-java</artifactId><scope>runtime</scope></dependency>
  <dependency><groupId>com.squareup.retrofit2</groupId><artifactId>retrofit</artifactId><version>2.1.0</version></dependency>
  <dependency><groupId>com.squareup.retrofit2</groupId><artifactId>converter-gson</artifactId><version>2.1.0</version></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
 </dependencies>
 <build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build>
</project>

~~~


---

# MASTER SECTION 21: TEMPLATES/application.properties.example

~~~text
# TEMPLATE ONLY. Copy into actual backend after confirming JDK/DB/settings.
server.address=127.0.0.1
server.port=8080
spring.datasource.url=${ORG_DB_URL}
spring.datasource.username=${ORG_DB_USER}
spring.datasource.password=${ORG_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
orgresource.sms.mode=DISABLED
orgresource.reminder.scan-ms=3600000
orgresource.expiry.scan-ms=3600000
# Do not use destructive DDL modes for normal demo or store secrets here.

~~~


---

# MASTER SECTION 22: TEMPLATES/frontend_api_types.ts

~~~text
// Proposed public DTO types, not implemented/tested React source.
export type ResourceType = "REUSABLE" | "CONSUMABLE";
export type AllocationStatus = "PENDING" | "APPROVED" | "ISSUED" | "RETURNED" | "CONSUMED" | "CANCELLED";
export type ReminderOutcome = "NONE" | "CLAIMED" | "SUPPRESSED" | "SIMULATED" | "PROVIDER_ACCEPTED" | "REJECTED" | "UNKNOWN";
export type CancellationReason = "USER_REQUEST" | "EXPIRED";
export interface Department { id:number; name:string }
export interface Project { id:number; departmentId:number; name:string; startAt:string; endAt:string }
export interface Resource { id:number; name:string; type:ResourceType; totalQuantity:number; availableQuantity:number }
export interface ResourceAvailability { resourceId:number; availableQuantity:number }
export interface ResourceRequirement { id:number; projectId:number; resourceId:number; quantity:number; allocatedQuantity:number; shortageQuantity:number }
export interface Allocation {
  id:number; requirementId:number; quantity:number; borrowerName:string;
  contactPresent:boolean; smsConsent:boolean; status:AllocationStatus;
  createdAt:string; approvedAt:string|null; issuedAt:string|null;
  returnedAt:string|null; consumedAt:string|null; cancelledAt:string|null;
  cancellationReason:CancellationReason|null; expiredPendingCleanup:boolean;
  reminderAttempted:boolean; reminderOutcome:ReminderOutcome;
}
export interface ApiErrorBody { code:string; message:string }
export interface DeleteResponse { message:"Record deleted." }

~~~


---

# MASTER SECTION 23: TEMPLATES/frontend_api_client.ts

~~~text
// Candidate code only. Test with real Jersey API before relying on it.
export class ApiError extends Error {
  constructor(public status:number, public code:string, message:string) {
    super(message); this.name="ApiError";
  }
}
async function request<T>(path:string, method:"GET"|"POST", body?:unknown, signal?:AbortSignal):Promise<T> {
  const res=await fetch("/api"+path, {
    method, signal,
    headers: body===undefined ? {Accept:"application/json"} : {Accept:"application/json","Content-Type":"application/json"},
    body: body===undefined ? undefined : JSON.stringify(body)
  });
  const text=await res.text(); let parsed:unknown=null;
  if (text.length) {
    try { parsed=JSON.parse(text); }
    catch { throw new ApiError(res.status,"INVALID_SERVER_RESPONSE","Server returned invalid JSON."); }
  }
  if (!res.ok) {
    const err = parsed && typeof parsed==="object" ? parsed as Partial<{code:string,message:string}> : null;
    throw new ApiError(res.status,err?.code??"REQUEST_FAILED",err?.message??"Request failed.");
  }
  return parsed as T;
}
export function apiGet<T>(path:string,signal?:AbortSignal){return request<T>(path,"GET",undefined,signal);}
export function apiPost<T>(path:string,body?:unknown){return request<T>(path,"POST",body);}

~~~


---

# MASTER SECTION 24: TEMPLATES/vite.config.ts

~~~text
// Proposed Vite dev proxy only. Requires actual React/Vite package setup.
import {defineConfig} from "vite";
import react from "@vitejs/plugin-react";
export default defineConfig({
 plugins:[react()],
 server:{
   host:"127.0.0.1",
   port:5173,
   proxy:{"/api":{target:"http://127.0.0.1:8080",changeOrigin:true}}
 }
});

~~~


---

# MASTER SECTION 25: TEMPLATES/PHASE_REPORT.md

# OrgResource Phase Evidence Report (fill with ACTUAL results only)

Phase ID:
Goal:
Agent:
Source files actually inspected:
Inaccessible source files:
Repository URL/account:
Branch/worktree:
Base commit / head commit:
Git status/diff before:
Files created:
Files modified:
Files removed:
DB schema change:
API response/request change:
Frontend change:
Dependencies added/changed:
Security/privacy changes:
Build commands actually executed:
Exit codes and result:
JDK/Maven/Node/DB server versions:
Real DB queries executed and observed rows:
Real HTTP/Postman calls executed and observed status/JSON:
Real scheduler/Retrofit request observed:
Real browser UI observed:
Tests PASSED (names and output):
Tests FAILED (names and output):
Tests BLOCKED (reason):
Tests NOT_RUN:
Clean source import test PASS/FAIL/NOT_RUN:
Export ZIP path and SHA-256:
Video path or VIDEO NOT_CREATED:
Secrets scan:
Course requirement IDs:
Spec deviations, reasons and explicit approval:
Open risks/next action:
Final phase gate VERIFIED/FAILED/BLOCKED/NOT_RUN:
Do not claim an unrun action is done.


---

# MASTER SECTION 26: TEMPLATES/PROPOSAL_AMENDMENTS.md

# Revised-proposal amendment log (not evidence of submission or approval)

Baseline: 10_ORGRESOURCE_CONCISE_PROPOSAL.docx, 12 pages, 5 entities, 25 unique HTTP routes.

1. Keep five entity names and 25 original method/path pairs unchanged.
2. Resource creation does NOT add mandatory unit.
3. Resource availability response remains exactly resourceId + availableQuantity.
4. Resource update adds expectedTotalQuantity and stale-state 409 behavior; align component method and Postman.
5. Resource delete stricter: require totalQuantity=0 in addition to no Requirements.
6. Department/Resource use normalized nameKey unique DB index.
7. ResourceRequirement update lower bound includes largest live PENDING Allocation in addition to current effective coverage.
8. Allocation persistence adds lifecycle timestamps, approvedAt history, cancellation reason, durable SMS claim/outcome and provider response metadata.
9. Allocation output redacts phone; adds contactPresent, expiredPendingCleanup, reminderOutcome; reminderAttempted derived.
10. Allocation delete protects any historical approvedAt, even after cancellation.
11. Approval holds expire at Project end (behavioral change to original proposal's indefinitely reserved stock), expiry cleanup both PENDING and APPROVED.
12. Reminder stays in upcoming 24-hour window only, no overdue extension.
13. SMS modes disabled/fake/conditional virtual; no delivery claim; no actual provider token in simulator.
14. Add proposed A09 consent-withdrawal route only if retained; original 25 remain.
15. Public errors keep original message property and add stable code.
16. Maven build, VS Code workspace and React UI are user implementation decisions; still export Eclipse-compatible source.
17. Optional image upload/download only if final course interpretation requires; update routes to 28 if added.
18. Explain factual evidence limits: fictional use case, no interviews/adoption, user-approved outside assistance acknowledgment.
19. Update controller method descriptions, request/response JSON, errors, components, entity fields/repositories, Postman to one coherent contract. Do not claim proposal submitted/approved unless actual receipt exists.


---

# MASTER SECTION 27: TEMPLATES/DEMO_CHECKLIST.md

# Genuine skeleton and final demonstration checklist

SKELETON:
[ ] Working commit noted.
[ ] VS Code Java source visible.
[ ] Actual backend startup successful, no fatal errors.
[ ] Actual MySQL server connection and version observed.
[ ] Five JPA tables and FKs observed through SQL.
[ ] Fictional seed data verified if used.
[ ] Actual repository methods and query output shown.
[ ] Jersey controller + @Autowired component code shown.
[ ] Postman real create/list JSON demonstrated.
[ ] Eclipse-compatible source ZIP exists; import status honestly disclosed.
[ ] No credentials or personal data in footage.
[ ] Video file actually captured and playable; not simulated.

FINAL:
[ ] Complete running UI against real Java backend.
[ ] Departments/Projects/Resources/Requirements create/read/edit.
[ ] Allocation pending->approved->issued->returned.
[ ] Consumable approved->consumed; total decremented once.
[ ] Stale stock update 409 and UI refresh.
[ ] Real scheduled FakeTwilio request/outcome.
[ ] Invalid operation refused; DB unchanged.
[ ] Browser/backend restart persistence.
[ ] All-route Postman results and tests referenced accurately.
[ ] Built UI from packaged Java root / verified.
[ ] Complete source/evidence/ZIP/checksums.
[ ] Authorship and assistance acknowledgment prepared truthfully.
[ ] Canvas and instructor access manually verified.


---

# MASTER SECTION 28: .gitignore.example

~~~text
# EXAMPLE — review with actual repository before using
.env
.env.*
!.env.example
*.log
*.tmp
node_modules/
frontend/dist/
backend/target/
target/
.vscode/settings.json
.idea/
.DS_Store
Thumbs.db
*.class
*.jar
# Do not blindly ignore required Maven wrappers, source .project/.classpath/.settings,
# frontend package-lock.json or documented portable dependencies.
# Avoid committing SQL dumps with actual people/private data.

~~~


---

# MASTER SECTION 29: postman/README.md

# Postman starter artifacts — not runtime proof

The collection contains 26 original/proposed route definitions with request examples and simple response-status checks. It is explicitly NOT a safe full-order runner: create, edit, destructive delete, and mutually exclusive lifecycle commands need separate fixtures/ordering. In particular, an Allocation cannot both be CONSUMED and RETURNED; a Project created with future start cannot be immediately fulfilled; deleting a Resource with positive stock is refused. Agents must replace this starter with complete all-routes and demo collections, dynamic generated IDs, negative tests and real execution evidence. The front-end UI uses the same 26 routes.


---

# MASTER SECTION 30: postman/OrgResource-UNEXECUTED-26-Route-Template.postman_collection.json

~~~json
{
  "info": {
    "name": "OrgResource 26 Routes — UNEXECUTED API TEMPLATE",
    "description": "PLANNING TEMPLATE ONLY. Importable route definitions, not a tested or safe end-to-end runner. Destructive routes and mutually exclusive lifecycle routes cannot be executed consecutively on one record. Provide correct active Project times, separate disposable fixtures, and run actual DB/HTTP tests before acceptance.",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Departments",
      "item": [
        {
          "name": "D01 POST /api/departments",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Fictional Logistics {{runTag}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"departmentId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "D02 GET /api/departments",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "D03 POST /api/departments/{{departmentId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments/{{departmentId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Fictional Operations {{runTag}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "D04 POST /api/departments/{{departmentId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments/{{departmentId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Projects",
      "item": [
        {
          "name": "P01 POST /api/projects",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"departmentId\": {{departmentId}},\n  \"name\": \"Fictional Workshop {{runTag}}\",\n  \"startAt\": \"{{projectStartAt}}\",\n  \"endAt\": \"{{projectEndAt}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"projectId\",String(b.id));}"
                ]
              }
            },
            {
              "listen": "prerequest",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "function pht(d){return new Intl.DateTimeFormat(\"sv-SE\",{timeZone:\"Asia/Manila\",year:\"numeric\",month:\"2-digit\",day:\"2-digit\",hour:\"2-digit\",minute:\"2-digit\",second:\"2-digit\",hour12:false}).format(d).replace(\" \",\"T\");}",
                  "pm.environment.set(\"projectStartAt\",pht(new Date(Date.now()+3*60000)));",
                  "pm.environment.set(\"projectEndAt\",pht(new Date(Date.now()+4*60*60000)));"
                ]
              }
            }
          ]
        },
        {
          "name": "P02 GET /api/projects?departmentId={{departmentId}}",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects?departmentId={{departmentId}}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "P03 POST /api/projects/{{projectId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects/{{projectId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Workshop Updated {{runTag}}\",\n  \"startAt\": \"{{projectStartAt}}\",\n  \"endAt\": \"{{projectEndAt}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "P04 POST /api/projects/{{projectId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects/{{projectId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Resources",
      "item": [
        {
          "name": "R01 POST /api/resources",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Extension cords {{runTag}}\",\n  \"type\": \"REUSABLE\",\n  \"totalQuantity\": 10\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"resourceId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "R02 GET /api/resources?type=REUSABLE",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources?type=REUSABLE"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "R03 POST /api/resources/{{resourceId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources/{{resourceId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Extension cords {{runTag}}\",\n  \"totalQuantity\": 10,\n  \"expectedTotalQuantity\": 10\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "R04 POST /api/resources/{{resourceId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources/{{resourceId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "R05 GET /api/resources/{{resourceId}}/availability",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources/{{resourceId}}/availability"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R05 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Requirements",
      "item": [
        {
          "name": "Q01 POST /api/requirements",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"projectId\": {{projectId}},\n  \"resourceId\": {{resourceId}},\n  \"quantity\": 6\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"requirementId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "Q02 GET /api/requirements?projectId={{projectId}}",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements?projectId={{projectId}}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "Q03 POST /api/requirements/{{requirementId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements/{{requirementId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"quantity\": 8\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "Q04 POST /api/requirements/{{requirementId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements/{{requirementId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Allocations",
      "item": [
        {
          "name": "A01 POST /api/allocations",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"requirementId\": {{requirementId}},\n  \"quantity\": 4,\n  \"borrowerName\": \"Fictional Representative\",\n  \"smsConsent\": false\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"allocationId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "A02 GET /api/allocations?requirementId={{requirementId}}",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations?requirementId={{requirementId}}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A03 POST /api/allocations/{{allocationId}}/approve",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/approve"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A04 POST /api/allocations/{{allocationId}}/issue",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/issue"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A05 POST /api/allocations/{{allocationId}}/return",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/return"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A05 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A06 POST /api/allocations/{{allocationId}}/consume",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/consume"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A06 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A07 POST /api/allocations/{{allocationId}}/cancel",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/cancel"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A07 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A08 POST /api/allocations/{{allocationId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A08 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A09 POST /api/allocations/{{allocationId}}/revoke-sms-consent",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/revoke-sms-consent"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A09 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://127.0.0.1:8080"
    }
  ]
}
~~~


---

# MASTER SECTION 31: postman/OrgResource-Local-Template.postman_environment.json

~~~json
{
  "name": "OrgResource Local \u2014 PLACEHOLDERS / NO SECRETS",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://127.0.0.1:8080",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "runTag",
      "value": "DEMO",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "departmentId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "projectId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "resourceId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "requirementId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "allocationId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "projectStartAt",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "projectEndAt",
      "value": "",
      "enabled": true,
      "type": "default"
    }
  ],
  "_postman_variable_scope": "environment"
}
~~~


---

# MASTER SECTION 32: MATRICES/ALL_26_ROUTES.csv

~~~text
route_id,group,http_method,path,success_status,body_example,template_unexecuted
D01,Departments,POST,/api/departments,201,"{
  ""name"": ""Fictional Logistics {{runTag}}""
}",True
D02,Departments,GET,/api/departments,200,,True
D03,Departments,POST,/api/departments/{{departmentId}}/update,200,"{
  ""name"": ""Fictional Operations {{runTag}}""
}",True
D04,Departments,POST,/api/departments/{{departmentId}}/delete,200,,True
P01,Projects,POST,/api/projects,201,"{
  ""departmentId"": {{departmentId}},
  ""name"": ""Fictional Workshop {{runTag}}"",
  ""startAt"": ""{{projectStartAt}}"",
  ""endAt"": ""{{projectEndAt}}""
}",True
P02,Projects,GET,/api/projects?departmentId={{departmentId}},200,,True
P03,Projects,POST,/api/projects/{{projectId}}/update,200,"{
  ""name"": ""Workshop Updated {{runTag}}"",
  ""startAt"": ""{{projectStartAt}}"",
  ""endAt"": ""{{projectEndAt}}""
}",True
P04,Projects,POST,/api/projects/{{projectId}}/delete,200,,True
R01,Resources,POST,/api/resources,201,"{
  ""name"": ""Extension cords {{runTag}}"",
  ""type"": ""REUSABLE"",
  ""totalQuantity"": 10
}",True
R02,Resources,GET,/api/resources?type=REUSABLE,200,,True
R03,Resources,POST,/api/resources/{{resourceId}}/update,200,"{
  ""name"": ""Extension cords {{runTag}}"",
  ""totalQuantity"": 10,
  ""expectedTotalQuantity"": 10
}",True
R04,Resources,POST,/api/resources/{{resourceId}}/delete,200,,True
R05,Resources,GET,/api/resources/{{resourceId}}/availability,200,,True
Q01,Requirements,POST,/api/requirements,201,"{
  ""projectId"": {{projectId}},
  ""resourceId"": {{resourceId}},
  ""quantity"": 6
}",True
Q02,Requirements,GET,/api/requirements?projectId={{projectId}},200,,True
Q03,Requirements,POST,/api/requirements/{{requirementId}}/update,200,"{
  ""quantity"": 8
}",True
Q04,Requirements,POST,/api/requirements/{{requirementId}}/delete,200,,True
A01,Allocations,POST,/api/allocations,201,"{
  ""requirementId"": {{requirementId}},
  ""quantity"": 4,
  ""borrowerName"": ""Fictional Representative"",
  ""smsConsent"": false
}",True
A02,Allocations,GET,/api/allocations?requirementId={{requirementId}},200,,True
A03,Allocations,POST,/api/allocations/{{allocationId}}/approve,200,,True
A04,Allocations,POST,/api/allocations/{{allocationId}}/issue,200,,True
A05,Allocations,POST,/api/allocations/{{allocationId}}/return,200,,True
A06,Allocations,POST,/api/allocations/{{allocationId}}/consume,200,,True
A07,Allocations,POST,/api/allocations/{{allocationId}}/cancel,200,,True
A08,Allocations,POST,/api/allocations/{{allocationId}}/delete,200,,True
A09,Allocations,POST,/api/allocations/{{allocationId}}/revoke-sms-consent,200,,True

~~~


---

# MASTER SECTION 33: MATRICES/ACCEPTANCE_TESTS.csv

~~~text
test_id,category,acceptance_goal,planned_status,observed_command,observed_result
BE01,backend,five JPA schema tables startup,NOT_RUN,,
BE02,backend,Department persisted 201,NOT_RUN,,
BE03,backend,Department GET list,NOT_RUN,,
BE04,backend,normalized duplicate conflict,NOT_RUN,,
BE05,backend,referenced Department delete refused,NOT_RUN,,
BE06,backend,invalid Project date rejected,NOT_RUN,,
BE07,backend,missing department FK rejected,NOT_RUN,,
BE08,backend,name only Project update with past unchanged dates,NOT_RUN,,
BE09,backend,historically approved Project date edit rejected,NOT_RUN,,
BE10,backend,zero stock Resource available zero,NOT_RUN,,
BE11,backend,approved hold reduces available,NOT_RUN,,
BE12,backend,parallel Resource approvals no overbook,NOT_RUN,,
BE13,backend,parallel Requirement approvals no overcover,NOT_RUN,,
BE14,backend,pending request no hold,NOT_RUN,,
BE15,backend,issue no double reserve,NOT_RUN,,
BE16,backend,return restores custody,NOT_RUN,,
BE17,backend,repeat return refused,NOT_RUN,,
BE18,backend,consume decrements once,NOT_RUN,,
BE19,backend,repeat consume refused,NOT_RUN,,
BE20,backend,Requirement reduction below coverage refused,NOT_RUN,,
BE21,backend,Requirement reduction below pending refused,NOT_RUN,,
BE22,backend,stale Resource correction conflict,NOT_RUN,,
BE23,backend,expired approved effective hold zero before cleanup,NOT_RUN,,
BE24,backend,expired pending cleanup,NOT_RUN,,
BE25,backend,expired approved cleanup,NOT_RUN,,
BE26,backend,overdue issued custody stays unavailable,NOT_RUN,,
BE27,backend,approved then cancelled deletion refused,NOT_RUN,,
BE28,backend,never approved deletion succeeds,NOT_RUN,,
BE29,backend,positive stock Resource delete refused,NOT_RUN,,
BE30,backend,zero stock unreferenced delete,NOT_RUN,,
BE31,backend,unique requirement pair conflict,NOT_RUN,,
BE32,backend,forced SQL error consumption rollback,NOT_RUN,,
BE33,backend,empty aggregates zero,NOT_RUN,,
BE34,backend,unknown FK 404 no save,NOT_RUN,,
BE35,backend,malformed JSON 400,NOT_RUN,,
BE36,backend,protected client field rejected/safe ignore,NOT_RUN,,
BE37,backend,restart persisted records,NOT_RUN,,
BE38,backend,clean import,NOT_RUN,,
BE39,backend,parallel duplicate name DB unique,NOT_RUN,,
BE40,backend,Project edit vs approval race,NOT_RUN,,
BE41,backend,Resource correction vs consume race,NOT_RUN,,
BE42,backend,time-boundary read consistency,NOT_RUN,,
BE43,backend,consistent read-only report or documented snapshot,NOT_RUN,,
BE44,backend,lost response no automatic retry,NOT_RUN,,
SMS01,sms,disabled startup,NOT_RUN,,
SMS02,sms,expiry independent,NOT_RUN,,
SMS03,sms,24h window edges,NOT_RUN,,
SMS04,sms,claim only once,NOT_RUN,,
SMS05,sms,return before claim,NOT_RUN,,
SMS06,sms,revocation before claim,NOT_RUN,,
SMS07,sms,return after claim before dispatch,NOT_RUN,,
SMS08,sms,simulator accepted typed reply,NOT_RUN,,
SMS09,sms,definitive reject,NOT_RUN,,
SMS10,sms,timeout UNKNOWN,NOT_RUN,,
SMS11,sms,restart no resend,NOT_RUN,,
SMS12,sms,overdue suppress,NOT_RUN,,
SMS13,sms,provider accepted not delivery,NOT_RUN,,
SMS14,sms,missing destination suppress,NOT_RUN,,
SMS15,sms,dummy auth only,NOT_RUN,,
SMS16,sms,result record not overwrite RETURNED,NOT_RUN,,
SMS17,sms,expired pending no inventory change,NOT_RUN,,
SMS18,sms,expired approved no double release,NOT_RUN,,
UI01,ui,empty DB,NOT_RUN,,
UI02,ui,Department real create,NOT_RUN,,
UI03,ui,PHT datetime roundtrip,NOT_RUN,,
UI04,ui,Resource availability,NOT_RUN,,
UI05,ui,Requirement shortage,NOT_RUN,,
UI06,ui,PENDING stock unchanged,NOT_RUN,,
UI07,ui,APPROVE stock reserve,NOT_RUN,,
UI08,ui,ISSUE no double deduction,NOT_RUN,,
UI09,ui,RETURN stock restore,NOT_RUN,,
UI10,ui,CONSUME stock once,NOT_RUN,,
UI11,ui,stale update 409,NOT_RUN,,
UI12,ui,unsafe delete 409,NOT_RUN,,
UI13,ui,revoke consent,NOT_RUN,,
UI14,ui,browser reload,NOT_RUN,,
UI15,ui,backend restart,NOT_RUN,,
UI16,ui,backend outage error,NOT_RUN,,
UI17,ui,keyboard support,NOT_RUN,,
UI18,ui,mobile viewport,NOT_RUN,,
UI19,ui,real backend browser E2E,NOT_RUN,,
UI20,ui,bundled JAR UI,NOT_RUN,,
UI21,ui,dashboard partial failure,NOT_RUN,,
UI22,ui,lost POST response,NOT_RUN,,
UI23,ui,actions by state/type,NOT_RUN,,
UI24,ui,responsive table/dialog focus,NOT_RUN,,
S01,skeleton,source exists,NOT_RUN,,
S02,skeleton,Java compiles,NOT_RUN,,
S03,skeleton,startup no fatal error,NOT_RUN,,
S04,skeleton,real MySQL connection,NOT_RUN,,
S05,skeleton,five JPA tables and FKs,NOT_RUN,,
S06,skeleton,fictional test rows,NOT_RUN,,
S07,skeleton,repository query runs,NOT_RUN,,
S08,skeleton,controller registration,NOT_RUN,,
S09,skeleton,Autowired delegation,NOT_RUN,,
S10,skeleton,JSON live HTTP,NOT_RUN,,
S11,skeleton,runtime create/delete,NOT_RUN,,
S12,skeleton,restart persists,NOT_RUN,,
S13,skeleton,Eclipse source ZIP,NOT_RUN,,
S14,skeleton,actual Eclipse import status,NOT_RUN,,
S15,skeleton,real video evidence,NOT_RUN,,
S16,skeleton,no secrets,NOT_RUN,,
S17,skeleton,source and checksums,NOT_RUN,,
S18,skeleton,Canvas checked,NOT_RUN,,

~~~


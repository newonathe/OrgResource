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

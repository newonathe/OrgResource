# Academic canonical specification — Independent static QA and change log

**Review:** 2026-11-16 PHT. **Status:** DOCUMENTATION QA PASS / SOFTWARE EXECUTION NOT RUN.  
**Git:** parent research snapshot 14635f5f3c9e213636f39b544c5627fcdbe3a0c2; O14 source snapshot 2dd336ed39cecd417a8bce3388b303e688e95dee. This audit does not merge PRs, modify application source, run Java/MySQL/Newman/Playwright locally, deploy or submit coursework.

## Direct sources inspected in this academic-specification pass

| Source | Coverage and provenance |
|---|---|
| Attached 09_ESP_PROJECT_ASSIGNMENT.pdf | All 3 pages opened; complete written tools, five-entity threshold, Controller/Component, Postman, proposal content, skeleton, final/demo/peer/Canvas dates |
| Attached 08_CSCI180_SYLLABUS.pdf | CLO and course outline/rubric pp2–4; Canvas/submission/integrity/Certificate pp5–6; remaining syllabus pages were not used to infer extra functionality |
| Attached 10_ORGRESOURCE_CONCISE_PROPOSAL.docx | All 12 pages opened; 25 operations, five entities, Component sequence/repository names, original sample JSON and lifecycle assumptions |
| Actual GitHub root AGENTS.md | Opened on main, O14 and research branches. Research/main AGENTS root was obsolete (“exactly five”, no ledger); corrected on this independent documentation branch, NOT on O14 implementation |
| O14 five JPA classes and enum/DTO definitions | All five actual files plus enum/ApiDtos.java inspected, fields, FK/unique/index, timestamps and redacted DTO documented |
| O14 five Jersey controllers | All handlers inspected; D4+P4+R5+Q4+A9; six GET + twenty POST |
| O14 five principal Components | Method names, validation, domain conflict, stock math, transaction state inspected; relevant complete Resource/Allocation and reminder/expiry sections reviewed |
| O14 five Spring Data repositories | All five actual source files opened; 27 additional method signatures independently counted (1+3+4+5+14) |
| O14 ApiExceptionMapper | Actual file inspected; 400/404/409/503/500 envelope and DB-specific constraint/lock handling |
| O14 evidence/O14/ROUTE_ACCEPTANCE_MATRIX.md and FINAL_ACCEPTANCE_REPORT.md | Previous evidence reviewed as HISTORICAL/REPORTED, not newly executed |
| Research CP30/31/37/39/41/42/43 and recent handoff | Current later decision and known technical conflicts reviewed; older specifications are not treated as universally binding |

## Mechanical cross-checks actually performed

- 02_DOMAIN_26_ROUTE_COMPONENT_CONTRACT.md lists exactly **26 distinct** operation IDs: D01–D04, P01–P04, R01–R05, Q01–Q04, A01–A09.
- 03_TEST_POSTMAN_DEMO_AND_SUBMISSION.md contains the exact same **26** per-operation acceptance rows, no missing IDs.
- 05_EERD_REPOSITORIES_AND_METHOD_SEQUENCES.md documents **27** non-inherited repository methods from actual O14 source: Department1, Project3, Resource4, ResourceRequirement5, Allocation14.
- All five relationships and source table names were verified: department, project, resource, resource_requirement, allocation. The specification **does not** pretend proposal-added infrastructure tables already exist.
- 01 distinguishes **course assignment** (CR), **syllabus fact** (SF), **project design history** (PD), **user decision** (UD), **current implementation source** (EV), and **student-only act** (ST).
- Exact known amendments are described: A09, expectedTotalQuantity CAS, raw phone redaction, effective endAt approval hold, structured ErrorView.
- Root AGENTS.md is updated to the latest explicit ONE-product/AT-LEAST-five requirement and scoped build-efficiency policy. O14 code/AGENTS remains unchanged on its PR.

**QA limitation:** This is a manual/repository-static review, not a complete code proof or automated all-links semantic test. GitHub's documentation CI passing cannot establish that the NEW unified application functions; the actual secure supported runtime and Twilio/image features are NOT implemented by this packet.

## Observed source-to-spec discrepancies deliberately NOT hidden

| ID | Historical/actual discrepancy | Resolution for current specification |
|---|---|---|
| QA-01 | “RetroJit” written in assignment; Retrofit/GSON named in syllabus | Preserve exact source wording and use the evidence-supported Retrofit/Gson interpretation |
| QA-02 | Proposal 25 routes vs O14 26 | A09 separately labeled privacy amendment |
| QA-03 | Proposal Resource update {name,totalQuantity} vs O14 additional expectedTotalQuantity | Preserve O14 stale-write compare-and-set and explain amendment |
| QA-04 | Proposal Allocation view exposes phoneNumber vs O14 redacts it | Protect phone privacy, do not regrow it in public JSON |
| QA-05 | Proposal reserves approved stock until action, O14 drops effective approved hold at endAt | Adopt current tested effective hold rule as a later amendment, label provenance |
| QA-06 | Main/research legacy AGENTS claims “exactly five; never ledger”; assignment says at least five | Update root AGENTS policy on docs branch while preserving five genuine core entity families |
| QA-07 | O14 55-test CI can pass; older exact-SHA MySQL availability test once returned 8 instead of 5 | Mark ROOT CAUSE OPEN; require repeated deterministic real MySQL boundary checks, do not change assertion |
| QA-08 | O14 FakeTwilio is not a real Twilio REST integration | Require honest official API-shaped adapter protocol tests, no paid/live send without approval |
| QA-09 | CLO9 present in syllabus, no O14 images | Add real authenticated bounded image receive/store/serve in final build, classify user conservative inclusion |
| QA-10 | Final supported one-runtime Javax/Jakarta/Jersey/JPA compatibility unexecuted | Mark technical blocker and require engineering S1 actual compiled and runtime evidence |
| QA-11 | O14 old repositories query globally, but final product must be tenant-private | Require SQL/JPA tenant-bound rewrite before public use, not UI-only filtering |
| QA-12 | “All new infrastructure entities must have public CRUD” is not explicitly written | Justify added tables and separately document which five core user-facing entity operations satisfy course CRUD; avoid false assignment interpretation |

## Outstanding proof and decision gates

1. **ENGINEERING:** Actual supported one-JVM Javax/Jakarta/Spring/Jersey/MySQL/Retrofit/React feasibility — NOT RUN.
2. **ENGINEERING:** Full secure new entity/API/SQL migrations, tenant isolation, history and original 26 live routes on final product — NOT IMPLEMENTED.
3. **ENGINEERING:** Java/MySQL concurrency and intermittent grouped-query bug diagnosis, actual negative backend tests — ROOT CAUSE OPEN.
4. **ENGINEERING:** Real image receive/serve and official Twilio REST protocol adapter — NOT IMPLEMENTED.
5. **DEPLOYMENT:** Eligible $0 provider account, real HTTPS/reload, off-host encrypted backup/restore, quota gates and recovery — NOT RUN; no guarantee “free for life”.
6. **ACADEMIC STUDENT:** Final Word proposal after code freeze, real group/recording, COA+assistance statement, applicable peer reviews, actual Canvas receipt — NOT VERIFIED.
7. **SOURCE PROVENANCE:** Historical original ZIP one-path manifest discrepancy unresolved; not incorrectly reported as all original binaries fully checked.

**Release ordering:** research and academic requirements → S1 compatibility proof → reviewed complete product implementation → P-ACCEPT → actual A-FINAL technical/academic verification → student-run Canvas actions. Printed final deadline Dec 5, 2026; no speculative extension.

## Change log

- **2026-11-16 A-CANON-1.0:** Rebuilt course matrix, original five-entity/26-route API and Component contracts, actual repository inventory/EERD, per-route Postman/DB/demo acceptance, student deliverable checklist, and token/speed/storage-efficient optional delegation policy. Replaced obsolete research-root AGENTS.md with current instructions on the docs branch. No application code changed.
- **Next:** Any implementation-detected inconsistency must create a dated amendment with direct source path, diff, affected test and owner decision. Do not silently overwrite proposal history or claim more completion from prose.

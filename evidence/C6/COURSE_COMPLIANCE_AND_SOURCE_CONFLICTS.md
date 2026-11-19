# C6 course authority, frozen-A source mapping and Extended conflicts

**2026-11-19 · C6 READ-ONLY partial audit · SOURCE_ONLY unless explicitly stated.** This document reports actual source files examined in this pass. It does not indicate local Java, MySQL, Postman, Chrome or CI execution. The immutable reference is Course A `7f8c0746da18fd54b47f430ac1435ac36e605d67`. C5's tested/package source was `14576b57fff44c6f0112fd37d8c237ca0122efb4`; C5 report HEAD was `9755ef63bd4c02a7042cb48823bf759e609357cd`. PR #23 planning head audited: `d794e8d2a9db1167729a54dcab844f03642d2c36`. These are three different purposes, never collapse their evidence.

## Academic authority retrieved from original supplied files

**COURSE REQUIREMENT — 09_ESP_PROJECT_ASSIGNMENT.pdf, all 3 pages read:**
- Service useful to Ateneo community, Jersey REST, JSON, Spring annotations, JPA/Spring Data, Javax Validation, `RetroJit` wording (interpreted with syllabus CLO8's explicit **Retrofit and Gson**), scheduling.
- At least **five justified entity types**. Initializer may populate starting data but **runtime create/read for all entity types and create/delete rows** needed. One or more web controllers, GET for retrieval, POST for submits, controller forwards to component via Spring `@Autowired`; Postman definition for all calls.
- Proposal, skeleton and final stages. Printed final **Dec 5, 2026**; Canvas announcements may change date. Final video/link/group discussion, peer reviews and documented Canvas submission are **student-only / NOT_VERIFIED**, not automatically satisfied by source code.
- Optional UI bonus; actual React UI is a user/design addition, not evidence the instructor approved any modern replacement stack.

**SYLLABUS FACT — 08_CSCI180_SYLLABUS.pdf, all 9 pages read:**
- CLO1 Eclipse, CLO3 Spring Boot/Jersey, CLO4 Postman, CLO5 Spring Data JPA/Hibernate/MySQL, CLO6 JPQL, CLO7 Javax Validation, CLO8 Retrofit/Gson JSON over HTTP, CLO9 receive/serve images via HTTP, CLO10 final project.
- Class policy requires Certificate of Authorship and acknowledgment of outside sources/assistance. The supplied syllabus does **not** independently establish a specific generative-AI permission/prohibition. Current Canvas/student course guidance remains OPEN.
- University LMS Canvas is required for course updates; do not represent this audit as instructor approval.

## Frozen Course A — direct source-to-course mapping

| ID | Required/verified source | Direct file / exact observed range | C6 verdict / missing runtime evidence |
| --- | --- | --- | --- |
| A-FIVE | 5 justified entities | `backend/src/main/java/app/entities/{Department,Project,Resource,ResourceRequirement,Allocation}.java` JPA `@Entity` + annotated fields; exact blobs in SELECTED_SOURCE_REVIEW_REGISTER.csv | SOURCE_ONLY: 5 actual JPA domain classes; schema creation and DB joins not rerun |
| A-CRUD | Runtime create/list/delete for each, additional updates/allocations | `backend/src/main/java/app/rest/controllers/{Department,Project,Resource,Requirement,Allocation}Controller.java`; corresponding `app/components/*Component.java` method signatures and `@Transactional`; 28-source route matrix | SOURCE_ONLY: **28/28** Postman method/path entries map to real frozen controller annotations; execution NOT_RUN |
| A-HTTP | GET retrieval, POST submissions and components | `DepartmentController` lines 22–53, `ProjectController` 22–54, `RequirementController` 23–55, `ResourceController` 30–103, `AllocationController` 22–84. Each declares Spring `@Autowired` component | SOURCE_ONLY; negative, error payload and actual Jersey startup not independently run |
| A-FIELD | Data validation | JPA field `@NotNull`, `@Min`, `@Column` and associated component checks in five frozen entities, `ResourceImageSupport` read/validation methods | SOURCE_ONLY; malformed/overflow/Unicode and MySQL constraints need actual checks |
| A-POSTMAN | All REST definitions | `postman/OrgResource-Course-A-28-Routes.postman_collection.json` blob `2619f53ca5df2d245ce0c24f8a5735cead253449` | EXACT DOCUMENTARY MATCH: **21 POST + 7 GET = 28**, consisting of 26 business and two image operations; **28/28 matched** to annotated frozen A code. No Newman execution this pass |
| A-IMG | CLO9 binary image HTTP | `ResourceController.java` POST multipart `/resources/{id}/image` lines 70–90; GET binary `/resources/{id}/image` lines 92–102. `ResourceImageSupport.java` bounded input/validation/SHA methods | SOURCE_ONLY; actual round-trip exact image bytes NOT_RUN |
| A-SMS | External Retrofit/Gson JSON HTTP, timer | `backend/src/main/java/app/reminders/TwilioRestTransport.java` (Retrofit/Gson construction and send), `ReminderScheduler.java` fixed-delay scans/reconciliation and `ExpiryScheduler.java` fixed-delay expiry | SOURCE_ONLY; fake/disabled mode exists; **no live carrier delivery** claimed or requested |
| A-DEMO | Source/Eclipse import | `backend/pom.xml`, controller/entity/component package and frozen source; syllabus CLO1–CLO2 | SOURCE_ONLY; Eclipse import, packaged Java8/JDK8/MySQL build and video NOT_RUN |
| A-AUTH | Certificate + outside assistance, peer reviews, Canvas submission | Syllabus p6 and assignment pp1–3 | STUDENT_CHECK, **not performed or claimed** |

**Concrete as-built route counts confirmed from Java and collection:** Department **4**, Project **4**, Resource **7** (two image), Requirement **4**, Allocation **9**, total **28**. The assignment itself **does not require exactly 28**; that is a verified *as-built amendment* beyond the original proposal's 25 business routes. Never call 28 an official course count.

### Sources that must still be audited for course-complete C6

Complete all 5 frozen components' internal branches, all 5 repositories' custom methods, `ApiDtos`, validation/error handling, Java8/test dependencies and source import instructions; map actual Postman assertions and responses, 61 historic CI test names and gaps, image-byte negative cases and real MySQL schema semantics. Relevant uploaded original lecture/lab source must be inspected at exact paths and course ZIP manifest reconciled. A method-name and route-annotation scan is insufficient to certify comprehensive behavior.

## Extended C5 source inspected (distinct product lane)

- **VERIFIED SOURCE:** Five **domain** JPA classes reappear in Extended Java21 with `organizationId`, field validation and version columns, composite scoping FKs. Supporting identity, sessions, receipt, audit and stock-ledger tables exist and do not retroactively change frozen Course A's entity-count requirement.
- **VERIFIED SOURCE:** Five Flyway migration files exist; `V1` defines identity/domain and FK checks; `V2` Spring Session JDBC; `V3` `stock_movement`, `idempotency_key` and operation receipts; `V4` encrypted contacts, images and reminder outcome; `V5` reminder claim boundary. Blobs and inspected SQL DDL are in SELECTED_SOURCE_REVIEW_REGISTER.csv. Their actual Flyway execution and checksum stability were NOT independently rerun here.
- **VERIFIED SOURCE:** `InventoryService.java` lines 705–736 explicitly computes physical total from movement deltas, approved holds, outstanding reusable issued custody, verifies consistency and refuses negative/invalid availability. Lines 519–585 implement APPROVE/ISSUE/RETURN/CONSUME/CANCEL with state checks and movement facts. Lines 616–703 implement idempotency claim, receipt replay and permanent movement fallback. Actual race/rollback/database grant proof remains NOT_RUN.
- **VERIFIED SOURCE:** `C1AttemptThrottle.java` uses a synchronized in-memory `LinkedHashMap`, resetting with process lifetime; public distributed threat suitability is OPEN for C7. `C1SameOriginFilter.java` compares request scheme/server name/port with Origin; real reverse-proxy handling remains NOT_RUN. `C1BootstrapService.java` protects one-time first owner and is **not a 96-org fixture importer**.
- **REPORTED_ONLY (historical C5 PHASE_REPORT.md):** Java21/MySQL8 44 tests, React 24 tests, Newman 48 requests/90 assertions over 28 amended academic operations, Chromium 11 journeys, Java8/MySQL 61 tests, and same-workstation separate MySQL-instance backup/recovery all reported as passed for tested code SHA `14576b57...`. This audit read those recorded results, **not independently re-executed them**.
- **QUALITY CONCERN / VERIFIED SOURCE:** `C1IdentityAcceptanceTest.java` lines 87–88 uses `@TestInstance(PER_CLASS)` and ordered execution, with 44 test cases in one file. This is an explicit **test-independence risk**, **not proof the tests are wrong**. C9 and C6 critic must test selected high-risk cases alone/in reversed order with independently fresh fixtures. `extended/frontend/e2e/orgresource.spec.ts` contains a range of denial, lost-response and browser assertions; this is source evidence, not a newly run browser test.
- **REPORTED_ONLY:** `docs/EXTENDED_C_RELEASE.md` states loopback one-instance release, current backup raw SQL cap **64 MiB**, and same-computer recovery; this does not establish public HTTPS, trusted reverse proxy, off-host disaster recovery, user traffic or production secrets/key custody.

## C6 conflicts and classification

| Conflict | Classification | Resolution now | What is still needed |
| --- | --- | --- | --- |
| Assignment `RetroJit` vs syllabus `Retrofit/Gson` | COURSE REQUIREMENT / SYLLABUS FACT | Follow syllabus CLO8 and actual course transport source; do not infer a separate client named RetroJit | Original lesson source API and instructor/Canvas check if ambiguity remains material |
| Original 25 business routes vs A as-built 26+2 | PROPOSAL HISTORY / VERIFIED CODE SOURCE | 28 verified as-built mapping; preserve immutable A | Independently rerun Postman on proper Java8/actual MySQL |
| Five academic domain types vs many Extended identity/support tables | COURSE REQUIREMENT / USER DESIGN | Isolate A vs C release; no retroactive rewrite | Full Extended entity count/classification & schema test |
| Java8 javax A vs Java21 Extended Jakarta+javax | COURSE REQUIREMENT / CODE SOURCE | Keep both independent lanes | Effective Maven tree, binary/tests on correct JDK |
| C5 green CI versus public release readiness | REPORTED_ONLY / OPEN QUESTION | Local reported proofs; public host and off-host restore NOT_RUN | C7–C11 authorized implementation and real independent runtime tests |
| 72 COA RecWeek directory entries vs accredited COA totals 53/55 and 24 LIONS subset | VERIFIED SOURCE / OPEN QUESTION | 96 names are current-source **demo references**, **not complete accredited roster**; Sanggunian government separately typed | Revalidate complete LIONS roster, aliases, official rights and source date |
| MISA/Sanggunian huge fictional history | USER DECISION / DESIGN | Explicit synthetic and non-endorsed 2025-12-01–2026-11-18 plan | Real guarded 96-tenant importer, SQL/HTTP/UI and backup test; no adoption claims |
| CI zero-step failures and user account minutes | USER-REPORTED / CODE EVIDENCE | User reports private free CI allowance exhausted, consistent with zero-step jobs; not a Java test failure | Public runner support if made public; **publication safety gate** first |
| GitHub publication vs preserved course pack and personal/student materials | NEW OWNER INTENT / SAFETY | **STOP PUBLICATION pending review**; read separate PUBLICATION_SAFETY_GATE.md | Privacy/permissions/history/license/secret scan and owner decision |

## Work remaining before C6 passes its own gate

The exact 699-file SHA inventory is **METADATA**, not semantic review. The 55-path selected-review register records **RELEVANT_RANGES only**, with material transitive dependencies still unreviewed. Full repository/course ZIP source audit, 28 handler→component→repository→test/response mapping, role×route matrix, React complete screens, old migration checksums, current CI and independent adversarial reviewer are still incomplete. No model/token or build-performance benchmark ran. These are gates, not cosmetic checkboxes.

**C6 verdict:** **IN PROGRESS / SOURCE-ONLY FINDINGS / NOT ACCEPTED**. No C7 implementation authority or release status from this report.

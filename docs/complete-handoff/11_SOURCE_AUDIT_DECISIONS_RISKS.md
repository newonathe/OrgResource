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

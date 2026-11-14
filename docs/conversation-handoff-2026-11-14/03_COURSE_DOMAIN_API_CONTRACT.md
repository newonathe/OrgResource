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
Course examples: Java 8/Jersey 2.26/Spring Boot 2.1.0.RELEASE/Spring Data JPA 2.1.2/Hibernate 5.3.7/MySQL Connector 8.0.13; JAX-RS @Path @GET @POST @Produces/@Consumes; Spring @Component/@Autowired component delegation; @Repository JpaRepository derived \`findBy\`, @Query JPQL, javax.validation, Retrofit + Gson forms to FakeTwilio; @EnableScheduling/@Scheduled. Do NOT copy demo SchedulingApp's deliberate DataSource/JPA exclusions. Source corpus and actual ZIP sources control when exact dependency/interface matters.

## 4. Five-domain-entity contract, fields, significance
**Department:** \`id: Long\` DB-generated positive PK; \`name:String\` 1–100 display normalized; \`nameKey:String\` lower-case Locale.ROOT normalized, NOT NULL unique DB index. Owns Projects; refuse delete if referenced. Duplicate create/rename 409. Should not store arbitrary invented Ateneo organizational data.
**Project:** id Long; required \`department: Department @ManyToOne\`; \`name:String\` 1–100 trimmed; \`startAt, endAt: LocalDateTime\` Philippine wall times \`YYYY-MM-DDTHH:mm:ss\`; on create future start, end>start. Active interval **[startAt,endAt)**. On update department immutable; name-only may change even if historical dates; changed dates require future new start and **no Allocation historically approved** beneath it (\`approvedAt != null\` regardless of current status). Delete only if no Requirements.
**Resource:** id; \`name\` 1–100 trimmed; normalized unique nameKey; \`type: REUSABLE|CONSUMABLE\` immutable; \`totalQuantity: Integer\` range 0..1,000,000. For reusable it means usable quantity owned (including issued on loan); for consumable it means physical remaining stock. \`availableQuantity\` computed, never persisted. Update accepts name,totalQuantity,**expectedTotalQuantity** version-like expected-value guard; conflict 409 STALE_RESOURCE_QUANTITY. Type not editable. Delete only total==0 and no Requirement references. No required unit field; put measurement in descriptive name, e.g. "A4 paper sheets".
**ResourceRequirement:** id; required \`project\`, required \`resource\`, integer quantity 1..1e6, database unique(project_id,resource_id). One Project–Resource pair per Requirement, a Project may need multiple Resources. Derived \`allocatedQuantity\` coverage and \`shortageQuantity\`. Cannot reduce quantity below max(effective covered, largest one live PENDING Allocation quantity); cannot update after Project ended; physical delete only if zero Allocation history.
**Allocation:** id; required Requirement; \`quantity\` integer 1..1e6 <= Requirement qty, immutable after create; \`borrowerName\` 1–100 fictional label; nullable phoneNumber only when valid E.164-like +[1-9][0-9]{7,14} and smsConsent true; consent=false discards phone; \`smsConsent:Boolean\`; \`status\` persisted ENUM STRING PENDING/APPROVED/ISSUED/RETURNED/CONSUMED/CANCELLED; createdAt, approvedAt, issuedAt, returnedAt, consumedAt, cancelledAt nullable lifecycle markers (createdAt not null); cancellationReason USER_REQUEST|EXPIRED; reminderClaimedAt and reminderOutcome NONE/CLAIMED/SUPPRESSED/SIMULATED/PROVIDER_ACCEPTED/REJECTED/UNKNOWN; optional reminderHttpStatus/providerMessageSid with only true provider values. Public response NEVER exposes phoneNumber; supplies \`contactPresent\`, \`reminderAttempted\`, \`expiredPendingCleanup\` derived fields. Status/timestamps server-controlled.

## 5. Stock, coverage, time and state invariants
Sample one injected Asia/Manila \`Clock\` time per operation. Java \`LocalDateTime\` PHT wall time, no accidental browser UTC conversion. Active Project [startAt,endAt): start inclusive, end exclusive. Early approval before Project start allowed (conservative global hold) if end in future. No speculative future return-based capacity.

Resource total T.
H = SUM APPROVED allocations whose associated Project \`endAt > now\` (even if project hasn't started).
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
Base \`http://127.0.0.1:8080/api\` local by runtime setting. POST create returns 201; other success 200. GET list empty [] rather than 404 for valid filter with no matches. Successful deletion \`{"message":"Record deleted."}\`. Errors have \`{"code":"...","message":"..."}\` (code additive amendment to original message). IDs generated, not client-writable; public DTOs not entities.

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
\`\`\`json
{"id":1,"name":"Logistics"}
{"id":2,"departmentId":1,"name":"Welcome Workshop","startAt":"2026-12-10T09:00:00","endAt":"2026-12-10T12:00:00"}
{"id":3,"name":"Extension cords","type":"REUSABLE","totalQuantity":10,"availableQuantity":6}
{"resourceId":3,"availableQuantity":6}
{"id":4,"projectId":2,"resourceId":3,"quantity":6,"allocatedQuantity":4,"shortageQuantity":2}
{"id":5,"requirementId":4,"quantity":4,"borrowerName":"Fictional representative","contactPresent":false,"smsConsent":false,"status":"PENDING","createdAt":"2026-11-13T11:00:00","approvedAt":null,"issuedAt":null,"returnedAt":null,"consumedAt":null,"cancelledAt":null,"cancellationReason":null,"expiredPendingCleanup":false,"reminderAttempted":false,"reminderOutcome":"NONE"}
\`\`\`
Example dates in historic planning docs are ILLUSTRATIVE and can be in the past; test setup MUST derive future dates or use injected fixed clock. Do not send literal 2026-11-20 dates as if still future.

Shared status/error taxonomy:
400 VALIDATION_FAILED for malformed JSON, invalid IDs/enums/fields/quantities/client status; 400 INVALID_PROJECT_PERIOD for end<=start or changed start not future; 404 NOT_FOUND for required missing target/parent; 409 DUPLICATE_NAME, DUPLICATE_REQUIREMENT, DEPENDENCY_EXISTS, RESOURCE_NOT_EMPTY, INSUFFICIENT_STOCK, REQUIREMENT_ALREADY_COVERED, QUANTITY_BELOW_COMMITMENTS, STALE_RESOURCE_QUANTITY, INVALID_ALLOCATION_STATE, RESOURCE_TYPE_MISMATCH, OUTSIDE_PROJECT_PERIOD, PROJECT_ENDED, PROJECT_DATES_LOCKED, HISTORY_PROTECTED. 503 RETRY_REQUIRED only recognized transient lock contention/deadlocks; sanitize 500 INTERNAL_ERROR and never leak SQL, stacktrace or secrets. Client invalid input vs server-side validation failure must not be blindly conflated. Transaction refused -> unchanged DB.

## 7. Existing source quirks and conflicts
- O1 \`sumCoveredQuantity\` erroneously counts expired APPROVED; fix in current O3 batch.
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
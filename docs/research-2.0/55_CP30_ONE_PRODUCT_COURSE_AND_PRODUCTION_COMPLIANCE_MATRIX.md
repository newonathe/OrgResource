# CP30 — One-product course + production compliance and regression matrix

**Date:** 2026-11-16 (Asia/Manila). **Status:** SPECIFIED ACCEPTANCE ONLY; no unified implementation/test/deployment exists.  
**Authority:** actual `09_ESP_PROJECT_ASSIGNMENT.pdf` pp1–3, `08_CSCI180_SYLLABUS.pdf` CLO1–10 and integrity policy, `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` pp1–12, pinned O14 source SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`. Current Canvas/instructor directives may change applicability but cannot be invented.

## 1. Course requirement to actual single-product runtime acceptance

| ID | Classification, course capability | Required **same-product** executable evidence | Current evidence/unknown |
|---|---|---|---|
| C01 | COURSE REQUIREMENT Jersey REST + JSON | Real Jersey `@Path` GET/POST endpoints produce/consume correct JSON and HTTP errors in the unified secured backend | O14 source/CI; modern unified NOT RUN |
| C02 | COURSE REQUIREMENT Spring-based annotations and `@Autowired` | Every Jersey controller delegates to an injected Component, no unauthorized direct repository business logic in controllers | O14 source; unified NOT RUN |
| C03 | COURSE REQUIREMENT >=5 justified entity types | Actual MySQL JPA Department, Project, Resource, ResourceRequirement, Allocation (not replaced by entirely unrelated tables); additional auth/tenant/ledger allowed | O14 existing; unified migrations NOT RUN |
| C04 | COURSE REQUIREMENT runtime create/read/delete ALL entities, GET for retrieval and POST submissions | Actual authenticated compat handlers, seed-independent runtime CRUD, DB postconditions and referential delete safeguards, with usable Postman collection | O14 26 handlers source, local 96/142 author report; unified NOT RUN |
| C05 | COURSE REQUIREMENT literal Javax Validation; SYLLABUS CLO7 | Prove runtime `javax.validation.Validator` handles invalid DTO and actual entity; prove invalid JPA entity save/flush rejection or explicit instructor-approved interpretation of lifecycle boundary | **BLOCKER**: Jakarta runtime default does not prove this |
| C06 | SYLLABUS CLO5–6 Spring Data JPA/Hibernate/MySQL/JPQL | Real MySQL SQL persistence/transactions, JPQL custom queries and magic findBy methods; initial and after restart; constraints | O14 CI some; new backend NOT RUN |
| C07 | SYLLABUS CLO8 Retrofit/Gson JSON over HTTP | Real HTTP request and Gson parse against sandbox or provider; API failures/retries handled | O14 FakeTwilio loopback source+CI; public unified NOT RUN |
| C08 | COURSE REQUIREMENT external service at least Twilio | Demonstrate adapter calling Twilio API as required by current course, distinguish: T-A fake loopback; T-B real Twilio Virtual Phone/test credentials; T-C actual carrier SMS. Never claim test == delivery | T-A evidence; T-B/T-C and actual course requirement **OPEN** |
| C09 | COURSE REQUIREMENT scheduling | Actual `@Scheduled` due-reminder/expiry, fake/real clock tests, durable attempt, 2 workers/restart, no double stock or SMS | O14 source/CI; public unified NOT RUN |
| C10 | SYLLABUS CLO9 HTTP image receive/serve | If applicable final, bounded allowed formats, safe path/object storage, no traversal, actual HTTP upload/read, content type and access controls | Syllabus CLO9 confirmed, final-project Canvas interpretation OPEN |
| C11 | COURSE REQUIREMENT complete Postman | 26 exact academic IDs/routes GET/POST + additional public auth/tenant cases, negative/role/concurrency/Postman exported with env README | 26 routes existing; actual unified NOT RUN |
| C12 | SYLLABUS CLO1–2 Eclipse/Java development | Source import/build instructions, no environment secret, reproducible from clean machine | Academic source verified; unified NOT RUN |
| C13 | COURSE optional UI bonus; USER DECISION functional visual system | Original Academic Glass Commons design retained/updated; real connected browser CRUD/stock/role flows, responsive and accessible | Existing UI source/15 CI tests, 5 browser local author report; unified NOT RUN |
| C14 | ASSIGNMENT final code/video; syllabus COA/outside help | Final source and runnable release, student-recorded video, truthful Certificate of Authorship and AI/outside support acknowledgment, peer reviews if applicable, student's Canvas receipt | Student/instructor steps NOT performed |
| C15 | Proposal source contract | Preserve original 25 functions + separate O14 amendment A09, numeric academic DTOs, PHT times, original whole/global semantics or explicitly approved source changes | O14 source fact; one-product compatibility contract NOT RUN |
| C16 | Original actual source amendments | Preserve R03 expectedTotalQuantity, R04 extra zero stock guard, Q03 largest pending guard, A08 approved history protection, expiredPendingCleanup and CANCELLED/EXPIRED reason | O14 source verified CP29; unified NOT RUN |

## 2. The full academic route inventory — 25 original + A09

These actual O14 operations must survive as genuine invokable Jersey GET/POST calls under a secured compatibility surface in the ONE product; the modern UI may use versioned endpoints but not omit this course proof:

| Family | Operation IDs | Required functions |
|---|---|---|
| Department | D01,D02,D03,D04 | create,list,update,delete |
| Project | P01,P02,P03,P04 | create,list(filter departmentId),update,delete |
| Resource | R01,R02,R03,R04,R05 | create,list(filter type),update,delete,availability |
| Requirement | Q01,Q02,Q03,Q04 | create,list(filter projectId),update,delete |
| Allocation | A01,A02,A03,A04,A05,A06,A07,A08,A09 | create,list,approve,issue,return,consume,cancel,delete,revokeSmsConsent |

Original inputs/outputs/errors and actual source amendments must be snapshotted from O14 controllers/DTOs and approved; blindly aliasing new public DTOs is NOT parity. The protected compatibility API may require a signed-in test role; assignment does not ban authentication, but current instructor demo guidance must be checked. Never expose the original unauthenticated O14 app to the Internet.

## 3. One-product functional parity and additions

| Behavior | Academic preservation | Additional production acceptance |
|---|---|---|
| Departments/projects | All five original entity interactions, project dates PHT semantics | Org scope/roles, UTC boundary and prevention of wrong-org FK |
| Resources | Reusable and consumable, derived `availableQuantity`, proper update/delete | Immutable ledger, atomic stock receipt/adjust, archive without loss of course delete capability |
| Requirements | Unique project/resource, allocated+shortage | Current coverage effective predicate and largest pending request invariant |
| Allocations | PENDING→APPROVED→ISSUED→RETURNED, or APPROVED→CONSUMED, PENDING/APPROVED→CANCELLED, guarded delete, revocation A09 | Per-tenant authorization, concurrency and never double stock; no partial return in first P0 |
| Reminders | Spring scheduler+Retrofit/Gson Twilio path preserved | Non-paying in-app fallback operates continuously even if Twilio account unavailable; no claim of real SMS sent when not |
| Images | Course CLO9 if applicable | Authenticated upload/serve, max size/type, storage quota, no file execution/path traversal |
| UI | Existing v5 direction preserved | Real role-aware actions, no mocked final acceptance, test on mobile/desktop |
| Durable data | Real MySQL/JPQL | Tenant composite FKs, resource-consistent stock movement, secure identities, encrypted off-provider DR |
| Postman | Original academic all-call suite | Production secure negative/performance/restore end-to-end matrix |

**Material unknown:** the original proposal explicitly states global holds persist until cancellation/return/consume, while O14's effective hold JPQL ceases at project end. The unified product must record and test the selected rule, not silently claim both semantics. See [CP29 expiry](53_CP29_MYSQL_IDENTITY_HISTORY_DR_CONTRACT_CANDIDATE.md).

## 4. Production exit criteria — ALL NOT RUN for unified app

| ID | Hard acceptance | Evidence |
|---|---|---|
| U01 | One production URL, connected authenticated UI, backend and MySQL, no second user-facing product | Actual deployed URL + browser/API/DB tests |
| U02 | Real course Jersey + Spring delegation + Javax DTO/entity/JPQL and 26 handlers on same chosen service | Integration logs/DB state/Postman; not static grep |
| U03 | Retrofit/Gson real HTTP and Twilio provider mode distinctions | Outbound test evidence safely redacted; no simulated-delivery claim |
| U04 | Scheduler/expiry/reminders survive restart, avoid duplicate effects | Injected/real clock + scheduled worker evidence |
| U05 | Two fictional organizations no cross-tenant read/write, operation receipt leaks or owner escalation | BOLA/CSRF/roles HTTP tests and DB negative FK |
| U06 | T5/q4/q4 approval race commits only one; stock/ledger conservation, 48h replay safe | Real InnoDB stress with recorded isolation and count |
| U07 | Effective owner/disabled/recovery/DR old-session fail closed | Concurrency, revocation and backup restore |
| U08 | Browser actual API flows complete + responsive 360/768/1440, keyboard, error/retry | Playwright + visual a11y audit |
| U09 | Public HTTPS trusted certificate, automatic short-lived IP cert renewal/reload/alert | Real certificate + multiple renewal-cycle observations |
| U10 | Actual eligible $0 no mandatory paid checkout; no paid overage, CI quota stop-on-exhaustion | Actual authorized provider screens/billing controls and CI records |
| U11 | Independent encrypted off-host/off-account recovery and rollback | Clean target restored and checked, key custody and actual measured RPO/RTO |
| U12 | Full authorized CI/CD: checked source→test→build immutable artifact→approved deploy→health probe→rollback | Real GitHub Actions run/digests/logs, no arbitrary auto-merge |
| U13 | Clean release/source package, Eclipse/Java source, Postman and truthful student academic evidence | Reproduced artifacts SHA, manual video/COA/Canvas steps |

**No-go if any mandatory U01–U13 remains unproven.** An authenticated one-application design is not production-ready simply because it boots or its GitHub workflow succeeds. Real user onboarding, campus data access and actual Ateneo approval remain deliberately outside fictional technical demo.

## 5. Prior decisions superseded and preserved
D-098–D-104 in [54](54_CP30_ONE_UNIFIED_ORGRESOURCE_DECISION_AND_CONFLICT_REGISTER.md) supersede D-019 only in final count, not the runnable compliance requirement. CP29 `51/52/53` remain detailed **drafts**, subject to one-product runtime, original A09 and secure compatibility changes. Final syllabus/instructor scope subject to current Canvas verification.

## 6. Course and honesty constraints
No fabricated peer reviews, Canvas submissions, instructor approvals, videos watched or pilots. Course syllabus Certificate of Authorship and outside help acknowledgment preserved; AI help must be acknowledged honestly as required. Original raw video frames remain unobserved. The student's accessible instructor-provided course materials, not our conjecture, decide any waiver or change.

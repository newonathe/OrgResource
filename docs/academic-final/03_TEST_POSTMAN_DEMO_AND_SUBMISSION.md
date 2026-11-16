# OrgResource — Executable academic acceptance, Postman, proposal, demo and submission

**Version:** A-CANON-1.0, 2026-11-16. **Status:** SPECIFIED, NOT EXECUTED AGAINST A NEW FINAL PRODUCT.  
**Authority:** 09_ESP_PROJECT_ASSIGNMENT.pdf pp1–3, 08_CSCI180_SYLLABUS.pdf pp2–6, 10_ORGRESOURCE_CONCISE_PROPOSAL.docx pp1–12, O14 controllers and O14 route acceptance source evidence. Final-product implementation and student actions are strictly distinct.

## 1. Evidence grading and immutable release identity

A PASS requires an actual action against the exact reviewed commit and environment, with expected vs observed result, source path, test class/collection case, date and command. Annotated Java methods alone are SOURCE VERIFIED, not HTTP execution. A CI green build alone does not prove local Playwright, Newman, Twilio-provider acceptance, actual student recording or production operation. Use: PASS / FAIL / BLOCKED / NOT_RUN / SOURCE_VERIFIED / REPORTED_PASS / STUDENT_ACTION.

Every final test receipt must include: exact Git SHA, JDK/Node/MySQL versions, schema identifier (fictional isolated test only), executable JAR SHA-256, dependency baseline, environment mode (FakeTwilio DISABLED or explicit simulator), test name, command, results and sanitized logs, DB postcondition and rollback when relevant. Never store credentials, real personal numbers, public secrets, or full private SQL dumps. No uncontrolled delete/drop/truncate of non-disposable schemas.

**O14 baseline only:** GitHub CI previously verified Java 8/MySQL and React build; O14 author report claims 55 Java tests, 15 frontend unit tests, five local Playwright journeys and 96 Newman requests/142 assertions. Those are not a fresh run from this doc-only phase. Current older MySQL intermittent result expected 5 / observed 8 is an explicit unresolved regression risk, not a test to weaken.

## 2. Per-route acceptance registry (exactly 26 existing academic operations)

Use unique fictional run IDs; create dependent records in order, then safely delete only eligible fixtures in reverse order. Preserve 409 history on protected rows. Check response JSON structure AND persisted state after forced reload/restart where material. Include missing IDs and invalid values; an empty collection is a valid JSON [] only when nothing matches.

| ID | Positive acceptance | Critical negative and independent postcondition |
|---|---|---|
| D01 | POST Department name →201 ID/name persisted | blank name 400, duplicate normalized name 409, no extra row |
| D02 | GET list →200 array includes new row | empty database [] and no raw internals |
| D03 | POST rename →200 persisted normalized name | duplicate 409, missing 404, unchanged original row |
| D04 | Delete unreferenced Department →200 {message:"Record deleted."} | referenced Department 409, row still present |
| P01 | Create future Project under Department →201 valid PHT start/end | invalid period/start 400, unknown Department 404, no orphan row |
| P02 | GET optional departmentId →200 correct only subset | malformed filter 400, no unrelated projects |
| P03 | Name-only edit after approved history and valid unrestricted date change →200 | changed dates after historic approval 409, original schedule unchanged |
| P04 | Delete unreferenced Project →200 then absent | Project with requirement 409, no cascade |
| R01 | Create named REUSABLE or CONSUMABLE Resource including T=0 →201 | bad enum/negative/>1e6 400, duplicate normalized name 409 |
| R02 | GET optional type →200 + correct derived availability | incorrect type filter rejected; neither hidden raw phone nor invented aggregate |
| R03 | CAS update with expectedTotalQuantity →200; verify new total | stale expectedTotalQuantity 409 and commitment floor 409; persisted original untouched |
| R04 | Delete new never-used zero-stock Resource →200 | referenced Resource or positive stock 409; future historical ledger row must also block deletion |
| R05 | GET availability →200 {resourceId,availableQuantity} | missing ID 404; H/I and end-boundary cases exact |
| Q01 | Create unique (projectId,resourceId) requirement →201 Q and shortage | duplicate pair 409, ended Project 409, missing parent 404 |
| Q02 | GET ?projectId filtered →200 accurate allocated/shortage | no foreign Project's requirement after tenant feature; invalid ID 400 |
| Q03 | Edit requirement Q →200 recalculated shortage | Q<effective covered OR largest pending guard 409, after Project end 409, persisted Q unchanged |
| Q04 | Delete requirement with no allocation →200 then absent | any Allocation history 409; no dependent deletion |
| A01 | POST request →201 PENDING, no stock reserve, contactPresent boolean | quantity > requirement, invalid consent/E.164 400, ended Project 409; no raw phone returned |
| A02 | GET optional requirementId/status →200 correct intersection | invalid enum 400, GET never exposes phoneNumber |
| A03 | Approve PENDING →200 APPROVED reserved exactly once | insufficient stock, already covered, ended Project or repeated approve 409 |
| A04 | Issue APPROVED REUSABLE in active period →200 ISSUED | wrong type, wrong status, pre-start/end 409; no double physical deduction |
| A05 | Return full usable ISSUED REUSABLE →200 RETURNED | repeated return/wrong type 409; requirement coverage remains |
| A06 | Consume APPROVED CONSUMABLE active →200 CONSUMED, T decreases exactly once | repeated consume/wrong type/outside period 409; DB rollback atomic |
| A07 | Cancel PENDING/APPROVED →200 CANCELLED, appropriate reason | ISSUED/RETURNED/CONSUMED rejected 409 and history intact |
| A08 | Delete never-approved PENDING/CANCELLED →200 then absent | approvedAt non-null or fulfilled row 409 HISTORY_PROTECTED |
| A09 | Revoke consent any state →200 phone null, consent false, idempotent | repeat must not alter stock or leak old phone; verify DB and public redaction |

**Coverage accounting:** 26 method/path rows, not 26 successful tests merely because rows are written. Static route crosswalk docs/research-2.0/contracts/ACADEMIC_26_SOURCE_ANNOTATION_CROSSWALK.json and executable collection cases under postman/ are prior evidence inputs. All new final API routes, including login/invitation/images, MUST be in final importable Postman; do not present the O14 reference collection alone as a complete final end-to-end suite.

## 3. Cross-route and inventory test cases

| Test ID | Setup/action | Expected authoritative database and HTTP result |
|---|---|---|
| INV-01 | REUSABLE T10, Q6, Allocation q4 PENDING→APPROVED→ISSUED→RETURNED | available 10→10→6→6→10; covered 0→0→4→4→4; T remains 10 |
| INV-02 | CONSUMABLE T100, Q40, q30 PENDING→APPROVED→CONSUMED | available 100→100→70→70; T 100→100→100→70; covered 0→0→30→30 |
| INV-03 | Competing approvals q4+q4 for total available 5 | Exactly one success, one controlled conflict; final available1, never negative; 100+ actual iterations if feasible |
| INV-04 | Competing approvals against remaining demand 6 | Exactly one q4 approved, no overcoverage; transaction/reload |
| INV-05 | Resource correction while consumption/approval is concurrent | Same row lock order, no lost stock update; rollback correctly handled |
| INV-06 | APPROVED active q3, ISSUED q2, APPROVED ending at exactly now q4, T10 | available exactly 5, H3/I2; identify SQL/log discrepancy if flake appears |
| INV-07 | After end: PENDING/APPROVED expiry worker; ISSUED late custody | Open cancelled EXPIRED; ISSUED unchanged; effective hold ends at boundary before worker |
| INV-08 | Failed consume after induced exception midway | Allocation remains APPROVED, T unchanged; no partial persisted transaction |
| INV-09 | Delete referenced Requirement/Resource/Project/Department | 409; all dependencies/history still exist after fresh read |
| INV-10 | Create/issue/return/consume then stop/restart app | DB values/state persist; no duplicate scheduling or dispatch |
| INV-11 | Unknown outcome after a non-idempotent POST | Client does not automatically replay; reconcile via authoritative GET/durable receipt when available |
| INV-12 | Revoke contact between reminder claim and send | Recheck under lock suppresses send; no stored public phone or automatic replay |
| INV-13 | Reminder due window [now, now+24h) | Exactly now included; just before +24 included; exactly +24 excluded; no expired row |
| INV-14 | Project window [startAt,endAt) | Approve may be early if end future; issue/consume active only; return late permitted |
| INV-15 | Resource list aggregated query on multiple Resources | All correct H/I sums in one clock sample, no N+1 or cross-resource contamination |
| INV-16 | Long/large quantity boundary | 0 allowed only physical stock, q requirements and allocations 1..1e6; no integer overflow or negative stock |

**Future unified-product extensions (user objective, not explicit course requirement):** auth/CSRF/org tenant isolation, immutable stock ledger, membership and invitations, operationId idempotency, image privacy and secure sessions. Each requires actual MySQL/HTTP negative testing; separate reviewed v2 SRS defines final status codes. Legacy global repository findAll/findById/JPQL must NOT be copied unchanged into a multi-org app. See research CP41.

## 4. Stack and course-tool execution proof

| Gate | Minimum reproducible executed evidence |
|---|---|
| JAVA-01 | Java compile/run; exact supported JDK and Maven versions; original Java8 source export still reproducible |
| JERSEY-01 | Real @Path GET/POST in Jersey ResourceConfig; Spring @Autowired Component invoked, JSON responses |
| MYSQL-01 | Actual MySQL/InnoDB schema contains five original business types with correct FKs and constraints |
| JPA-01 | Save/find/delete legitimate rows, rollback, custom repository method, JPQL aggregation |
| JAVAX-01 | Runtime literal javax.validation.Validator + invalid javax-annotated DTO; record actual provider |
| JAVAX-02 | Invalid javax-annotated entity on saveAndFlush; explicitly distinguish automatic JPA listener, service-level javax validation and database constraint |
| RETROFIT-01 | Real Retrofit/Gson HTTP request+response to isolated FakeTwilio with sanitized auth/test values |
| TWILIO-01 | Separate official Twilio Messages API-shaped Retrofit/Gson POST: path, x-www-form-urlencoded From/To/Body, Basic auth from injected test secrets, SID/error parsing and timeouts; local protocol server, not real provider |
| TWILIO-02 | Optional authorized official test-credential/provider call only if permitted account exists, free/consent safeguards; mark blocked otherwise; never claim actual handset delivery from simulator |
| SCHED-01 | Actual @Scheduled invocation for expiry/reminders, on/off config and two-worker/concurrent tests where appropriate |
| IMAGE-01 | POST authenticated tenant-scoped Resource image multipart (JPEG/PNG only, <=2 MiB), content sniff+size bound, DB storage, GET bytes with proper MIME, checksum equality |
| IMAGE-02 | Missing image 404; wrong tenant/role denied; oversized/invalid magic/SVG/script/decompression content rejected; no remote fetch or arbitrary filesystem access; no private photo in public demo |
| WEB-01 | Bundled React eight core pages from same app, live JSON operations, responsive light/dark/reduced-motion, no pure mock endpoints |
| COURSE-26 | Full 26 existing operations complete on the one actual final product, not only static annotations or archived code |
| POSTMAN-ALL | Importable complete collection includes all actual Jersey and added v2/image/auth routes, successful Newman run with dynamic fixture IDs and no secrets |
| CLEAN-01 | Clean source extraction, build, offline-safe/portable first-run (document dependencies), executable/JAR assets served, hash manifest, actual restart and tested MySQL configuration |

**No missing provider account should trigger unauthorized live message or payment.** TWILIO-01 satisfies a legitimate code-level official HTTP protocol test; provider-side acceptance and carrier delivery remain separate evidence claims.

## 5. Student-facing final Word proposal specification

The original 12-page concise proposal is a **historical submission draft**. Generate a revised Word proposal only AFTER the final executable contracts are stable. It must be accurate to code and cover:

1. Project title, factual group member roster, date/version/SHA and concise Ateneo-community problem **hypothesis** (not fabricated user study).
2. System purpose, actual actors/permissions, limitations, synthetic data, non-official Ateneo identity.
3. Full list of EACH actual controller operation: method, full route, path/query/body parameters, sample success JSON/status, error code/message/status and forwarded Spring Component/method (original 26 plus all added routes).
4. ALL Components: job descriptions and ordered steps of public methods; transaction, external HTTP, scheduler actions, validation and failures.
5. ALL declared core and justified extra entities: purpose, fields/types/nullability/defaults, relationships/cardinality/constraints, validation, deletion/retention.
6. ALL repositories and custom JPQL/magic methods: exact names/parameters, purposes, tenant condition and locking.
7. Actual stack/versions showing Jersey/Javax/Retrofit/Gson/MySQL/scheduling in runtime; do not call a Jakarta-only substitution compliant.
8. Figures only if generated from actual implemented ERD/architecture/UI (illustrative mock data labeled).
9. Explicit privacy/security and $0 hosting assumptions, recovery limits, source access.
10. Testing/evidence table tied to exact release SHA; known failures honestly marked.
11. Honest research/AI/outside assistance acknowledgment as applicable, without forged certificate/signature.

**Don't claim this Word proposal is already created, submitted, signed, or instructor approved.** Any prior Word proposal cannot silently be overwritten to hide historical differences.

## 6. Demonstration and release evidence script

A concise course demonstration should show, in this order:

1. Exact final Git commit and toolchains; Spring Boot starts, Jersey routes registered, React loads.
2. Actual five core JPA tables/FKs; use read-only SQL show/create inspection and fictional seed.
3. Run clean Postman create/get for Department, Project, Resource, Requirement, Allocation.
4. Show active inventory availability before/after approve, issue, full return.
5. Show CONSUMABLE physical T changes once on consume; verify both returned JSON and DB state.
6. Demonstrate invalid date, duplicate name, oversubscription and protected deletion returning correct JSON/409.
7. Demonstrate successful eligible runtime delete of the five original types in safe dependency order.
8. Show live invoked scheduled expiry/reminder and Retrofit/Gson FakeTwilio HTTP, with sending disabled by default.
9. Show official Twilio REST transport only against local protocol server unless a separately authorized provider test exists; never pretend SMS delivered.
10. Upload a synthetic safe JPEG/PNG then GET exact bytes and MIME, plus one size/type/security refusal.
11. Use real React frontend for meaningful workflow, mobile/light/dark where applicable.
12. If final product adds secure multi-org: show login/session/role, organization switching, foreign record denial and real stock history.
13. Restart Java and verify persistence; show portable source ZIP/JAR hash and environment/backup instructions.
14. Explicitly explain system non-affiliation, synthetic data, user acceptance and external-service limitations.

The video must be **actually made by the student/group**, with truthful roles and accessible permission to the instructor per assignment. A generated storyboard is not proof the video exists. An early in-person option is conditional on the instructor's published dates and actual participation, not silently assumed.

## 7. Academic submission and human-only checklist

| Action | Required actual human evidence | State in this specification |
|---|---|---|
| Verify actual group members and previous submissions | User's actual course records | NOT VERIFIED |
| Use supplied syllabus/assignment as requirements and check Canvas administrative module | Real student's Canvas view | NOT VERIFIED |
| Complete final code and release files | Exact SHA, source ZIP, Java/Eclipse import path, JAR, Postman, hashes | NOT RUN on final unified product |
| Full executable final technical acceptance | Passed final production + course acceptance matrix | NOT RUN |
| Record genuine group discussion or applicable early presentation | Actual video/link/access or real event | NOT VERIFIED |
| Assign real peer reviews of other actual members, if any | Student's genuine review/email action | NOT VERIFIED |
| Certificate of Authorship | Student-signed truthful certification and outside/Web/AI disclosure | NOT VERIFIED |
| Upload via Canvas and check teacher access | Actual submission receipt/links | NOT VERIFIED |

**Dates:** Original assignment: proposal Oct 30; partial skeleton Nov 13 / skeleton submission Nov 14; final Dec 5, 2026. Syllabus allows date changes and has general late-policy wording; no user/instructor-approved extension should be invented. The student requested production technical acceptance before final submission; scheduling risk remains real.

## 8. Final release decision

A-FINAL TECHNICAL PASS requires genuine full-stack runtime evidence for every CR item in 01 and every required operation in this file. Production acceptance additionally requires secure identity/tenancy, supported runtime, HTTPS, backups/recovery and no unauthorized provider charges. The two gates are distinct but apply to the **same final OrgResource product**.

A-FINAL student completion additionally requires actual human academic acts and final Canvas receipt. No assistant or coding agent may fabricate or submit them.

**Never weaken or disable a failing invariant test to meet a date.** If Dec 5 becomes impossible under the user's production-first scope, report the schedule/technical status and request a separate user decision rather than silently submit a partial product or claim an extension.

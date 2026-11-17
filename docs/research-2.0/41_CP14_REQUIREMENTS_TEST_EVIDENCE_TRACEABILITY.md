# CP14 — requirement-to-evidence traceability and academic/public release acceptance

**Dated November 15, 2026 (Asia/Manila).** This is a **research-only proposed acceptance contract**, not test execution. V2 app is UNBUILT; architecture BLOCKED. Original academic O14 PR#8 unchanged. All executable conditions marked NOT RUN unless independently supported evidence identified.

## Key contradiction in existing testing document

Existing [17 Test Validation](17_TEST_VALIDATION_AND_RELEASE_GATES.md) deterministic Postman steps 5–10 prescribe three overlapping/future bookings and RETURN q3 then q1 with `PARTIALLY_RETURNED`—the **historical V2 optional temporal/partial** system. Under [40 canonical scope](40_CP13_CANONICAL_SRS_EERD_API_CONFLICT_REGISTER.md), first public course-compatible P0 recommends **global reservations and whole-batch returns**, not those tests. DO NOT run historical sequence as the P0 acceptance contract; preserve it only as optional future feature test. The original 26-route full source regression is separate and mandatory.

## Evidence taxonomy and artifact contract

Statuses: **COURSE SOURCE VERIFIED**, **STATIC ORIGINAL SOURCE VERIFIED**, **PR CI REPORTED SUCCESS**, **AUTHOR-REPORTED LOCAL RUN**, **INDEPENDENT TEST PASS**, **NOT RUN**, **OWNER APPROVAL OPEN**, **EXTERNAL SERVICE/COURSE UNKNOWN**, **BLOCKED**. Never convert author-reported/CI success into independent verified runtime pass. For each executable case record: `caseId`, requirement/course source reference, phase `O14|P0|A-FINAL`, commit SHA and branch, target server/JDK/Jersey/Validator/MySQL versions, exact isolated test fixtures, command/HTTP request, expected status+DB postcondition, actual observed status+counts, UTC/PHT date, redacted logs/collection/screenshot/hash paths, tester/reviewer, failure/waiver and user-signed exception. An unsigned test plan is NOT evidence.

## Traceability matrix — source, execution, acceptance, current gap

| ID | Requirement / authority | Execution environment | Precise observable PASS | Required evidence | Status |

|---|---|---|---|---|---|

| A-01 | COURSE Jersey Spring Boot, JSON, @Autowired forwarding | 09 assignment p1, syllabus CLO3, proposal controllers | O14 pinned local course binary | All 26 original Jersey handlers visible and correct component forwarding; valid/invalid JSON; no Spring MVC replacement | Exact SHA + clean build + code-map + Postman/HTTP output | EXECUTION NOT RERUN |

| A-02 | Original >=5 justified JPA entities and MySQL | Assignment p1, syllabus CLO5/6, 12pp proposal | O14 + clean isolated MySQL schema | Exactly Department/Project/Resource/ResourceRequirement/Allocation entities usable at runtime; JPQL + derived repository query; create/list/delete | SHOW TABLES, SQL & Postman counts, Eclipse clean import | EXECUTION NOT RERUN |

| A-03 | Literal javax.validation on DTO and JPA lifecycle | Assignment p1, syllabus CLO7, Misc JPA Validation slides, O14 source | O14 Java8; chosen public-compatible runtime separately | Invalid DTO rejected, invalid direct JPA entity save refused and rolled back, not just annotations or Jakarta-only provider | Provider class/dep tree, JUnit/HTTP logs, DB 0-row | O14 source verified; running NOT RERUN |

| A-04 | Retrofit+Gson external Twilio integration | Assignment p1, syllabus CLO8, Twilio course deck, Lab4 fake | O14 FakeTwilio, real Twilio Virtual Phone only with authorized provider | Fake transport test labeled T-A, actual Twilio provider T-B separately; successful Retrofit HTTP; no claim of real delivery from virtual | Redacted request/response, simulator/virtual inbox screenshot if permitted | T-A author report only; T-B unknown |

| A-05 | Scheduling and expiry/reminder behavior | Assignment p1, O14 Expiry/Reminder scheduled jobs | O14 injected clock and controlled fake transport | Clock at start/end, repeated scheduler run, restart, no repeated physical hold release, no unexpected real SMS | Scheduler output + DB state + notification attempt tally | NOT RERUN |

| A-06 | Runtime GET/POST CRUD for each entity, complete Postman | Assignment p1; 26-route crosswalk report 30 | O14 real MySQL & Newman collection | 26 enumerated 4 Department/4 Project/5 Resource/4 Requirement/9 Allocation; create/read/delete and expected 200/201/400/404/409; A09 included | Collection JSON, environment no secrets, new Newman report tied SHA | Author report, independent run OPEN |

| A-07 | Word proposal, skeleton and final submission/authorship | Assignment pp1-3, syllabus p6 | Local documented artifact and student Canvas | Word fields/methods/validation mapping, skeleton demo, final group video, truthful certificate/outside assistance and peer reviews only if needed | Human visual review, exact files and student-submitted Canvas receipt | Canvas and student actions OPEN |

| A-08 | CLO9 image receive/serve | Syllabus p2 vs assignment project scope | Course Canvas/instructor authoritative check | If mandatory, actual multipart image receive/serve and secure decoding; if nonmandatory, dated clarification, not invented waiver | Instructor/Canvas citation + image HTTP test if applicable | COURSE INTERPRETATION OPEN |

| P-01 | Fictional user/workspace governance | R3, reports 31/32/40; not course requirement | Chosen local/JDBC or approved OIDC | No anonymous public OWNER; manual controlled invitations, recovery codes, effective last owner and no campus affiliation claim | Identity matrix ID-01..16, MySQL state, browser capture | DESIGN OPEN / NOT RUN |

| P-02 | Org membership/authorization/privacy | R6, SRS 08, threat model 13 | Jersey secured public API + actual MySQL | 2-tenant GET/POST/list/export/receipt BOLA denied 404/403, role/suspension takes effect instantly, no raw PII in logs | All route role matrix, test output, log scan | NOT RUN |

| P-03 | Schema FK and stock ledger integrity | R5, reports 32/33/40 | MySQL8.4 on actual chosen supported runtime | Named unique, NOT NULL composite FK org+department/requirement/resource and same-resource ledger; no MATCH; reject direct bad SQL | Migration SQL, SHOW CREATE TABLE, raw SQL failed insert, row snapshot | DESIGN PARTIAL / NOT RUN |

| P-04 | Global/whole inventory and temporal option separation | Original proposal + report 33 + CP13 | Chosen MySQL service and model oracle | Reusable T-H-I; consumable T-H; T5 two concurrent q4 only one approved; full usable return, consume one batch; optional time windows NOT mistakenly tested as P0 | Unit property seed + 100-way DB concurrency + ledger equality | P0 approval OPEN / NOT RUN |

| P-05 | Durable POST retry/idempotency | R5, report 33, CX-12 | Current MySQL transaction and simulated network fault | Same reused physical operationId q3 RECEIVE after >48h/receipt cleanup and app restart causes only one +3; body mismatch conflict; lost response can be queried | Unique DB row and audit/operation IDs, 409 and stable balances | NOT RUN |

| P-06 | HTTP security session/CSRF/provider CVEs | R1/R2/R6, 35-37, 32 | Supported Jersey/Spring/Javax stack and browser HTTPS | Session Cookie not Authorization, 401/403/404 correct, CSRF forbidden/no-token, logout/recovery invalidation, no critical CVEs including validation provider | SBOM, dependency scan, browser/security report | ARCHITECTURE BLOCKED |

| P-07 | End-to-end UI and accessibility | NFR 09, UI report, test 17 | Real API/DB at 360/768/1440, light/dark | No clipped elements, keyboard accessible, loading/error/retry, no demo-user cross session state | Playwright and manual screenshots with exact SHA | NOT RUN |

| P-08 | Host/no mandatory fees/TLS renewal | reports 38/39, official OCI/Let's Encrypt docs | Authorized eligible OCI account with A1 + chosen MySQL | Free quota/card hold and bill verified; public IP HTTPS, cert auto renew/reload before 160h expiry, DB 3306 private; no paid auto-upgrade | Provider dashboard and 2 consecutive renewal cycles/expiry alert, 0-charge invoice | ACCOUNT AUTHORIZATION OPEN |

| P-09 | Independent backup/restore | reports 38/39, test gate 17 | Off-provider encrypted backup + clean alternate MySQL | Tenant lost simulation, separate key, checksum, complete role/session/ledger counts; RPO/RTO measured vs targets; MySQL version compatibility | Encrypted-manifest hash, restore logs and recomputed invariants | NO RESTORE PERFORMED |

| P-10 | Nonfictional scope and workload claims | Original assignment use case, NFR 09 | Fictional fixtures, synthetic load generator | No actual Ateneo workflows, adoption/interviews, performance or cost claimed absent evidence; 50-org synthetic load visibly labeled | Fixture catalog, experiment metadata, signed disclosure | RESEARCH-ONLY |

| G-01 | Pre-build research acceptance | R0-R10 in report 29 | Independent source/review register + user signoff | No unapproved stack/identity/host or course interpretation change; remaining runtime-only tests bounded; user explicitly approves transition | Dated decision register and explicit authorization | BLOCKED |

| G-02 | P-ACCEPT public technical acceptance | User-required gate order, report 17/18 | Actual hosted binary+MySQL/free account | P-01..P-09 acceptance evidence under exact SHA, zero high-severity gaps, no assumed uptime or account billing | Versioned release candidate evidence packet, independent reviewer | NOT RUN |

| G-03 | A-FINAL independent academic acceptance | User order and original assignment | Original course executable/five entities/26 API/Postman, after P-ACCEPT | Reproduce academic stack/SQL/Retrofit/Twilio/schedule and student demonstration; current instructor/Canvas confirmed | Course acceptance matrix with local video/import artifact | NOT RUN |

| G-04 | Final student-only Canvas/authorship | Assignment pp2-3, syllabus p6, Canvas authority | Student manual submission | Certificate/outside assistance truthful, final video permission and peer review if relevant, timestamped Canvas submission by actual LMS rule | Student-attested Canvas receipt—not assistant claim | UNOBSERVED |

## Research prebuild R0–R10 status

| Gate | Domain | What is evidenced | Missing to close | Verdict |

|---|---|---|---|---|

| R0 | Course/ZIP/originals | Assignment/syllabus/proposal/O14 source and archive #22; current Canvas absent | Source-grounded partial; raw ZIP inventory discrepancy OPEN; Canvas user check | OPEN |

| R1 | Original executable course tech | Original Javax DTO+JPA/Twilio and 26 routes; original course preserved | Static source and some CI reported; no new full local execution | BLOCKED |

| R2 | Supported public architecture | Boot/Jersey old Javax + modern JPA/provider support | Multiple candidates, none integrated/support proven; explicit user boundary decision missing | BLOCKED |

| R3 | Users/ownership | Fictional bootstrap, manual invites and recovery proposal | Auth and transfer final role not selected | OPEN |

| R4 | Domain/API | 21 conflicts plus 26-route table | Global/whole public scope candidate, exact public route/error parity still unsigned | OPEN |

| R5 | MySQL EERD and transactions | FK, durable op IDs, DATETIME6, UUID type | Physical SQL/indexes/locks not frozen, tests not run | OPEN |

| R6 | Security/privacy | Threat model, candidate session/CSRF/tenant checks | No real auth/browser security proof, no real user authority | BLOCKED |

| R7 | Hosting and DR | OCI self-hosted vs MySQL.Free, IP TLS, offsite storage | Account eligibility/card/capacity, SSL and clean DR untested | EXTERNAL OPEN |

| R8 | Test design/evidence | Test gate 17 + new CP14 traceability table | Evidence taxonomy and expected outcomes specified; public P0 matrices need owner approval | PARTIAL |

| R9 | Independent critique | Old-versus-current conflicts identified in CP13 | Canonical artifact and signed design critique incomplete | OPEN |

| R10 | Owner authorization | Product start requires explicit user agreement | No stack/domain/host acceptance sign-off | NOT APPROVED |

## Revised deterministic acceptance journeys (future; NOT RUN)

**J-A original course:** fresh Java8/O14 source and Eclipse import, isolated MySQL; seed only explicitly controlled; all 26 GET/POST and component forwarding, JPQL, original DTOs, invalid `javax` DTO/JPA entity, Retrofit/Gson FakeTwilio then real Twilio Virtual Phone only if authorized, scheduler repeated at injected time, original errors and whole usable returns, exact data row verification. Preserve A09 revoke-sms-consent. Do not invent instructor acceptance of FakeTwilio or claim footage watched.

**J-P0 corrected public global:** two fictional orgs A/B; operator-controlled owner, member/lead/custodian roles; create Department in A, Project associated same department, q5 REUSABLE and q100 CONSUMABLE; q4 A PENDING→APPROVED makes reusable available 1; second q4 same resource gets 409 even if planned project windows do not overlap (global hold); issue first q4 within project period moves H4→I4 and available remains1; whole-batch RETURN with **no partial quantity** restores available5 and ends RETURNED; consumable q30 approve then consume lowers T100→70, H30→0, headroom stays70; double consume refused. Cross-org list/ID query/exports hidden; durable RECEIVE q3 retried after 48h changes stock only once. Demand shortage includes RETURNED/CONSUMED historical fulfillment, subject to original exact source rule.

**J-OPTION optional temporal/partial (DEFERRED):** old proposed [10:00,12:00) and [12:00,14:00) nonoverlap with later crossing new interval requires maximum simultaneous holds, and RETURN q3+q1 tests PARTIALLY_RETURNED. **Not part of currently recommended P0** and cannot be used to declare course parity. Any user approval changes 08/10/11/12/17 specs together.

**J-DR restore:** account-specific free host eligibility first, no automatic paid upgrade; 443 valid trusted certificate and first renewal, 3306 not public; consistent encrypted backup with separately stored key and independent provider/destination, restore into clean target compatible MySQL, compare Department→Project→Requirement→Allocation tenant FK, sessions/users/custody ledger, shortage, idempotency and RPO/RTO. Simply saving an SQL file is not a passing restore.

## Failure and evidence escalation

**STOP full product engineering** on unapproved literal Javax/Jersey security boundary, missing owner authority, or changes to mandatory technologies. **STOP public release** on any BOLA/CSRF/last owner exploit, double stock event, invalid MySQL FK, failed off-provider restore, unpatched critical dependency, unexpectedly paid account, or untrusted TLS. **STOP academic completion claims** without independently reproducible original source+complete Postman+course-specific Twilio decision+Canvas/student video/authorship. A date target cannot override a stop condition.

**Calendar:** original assignment prints December 5, 2026; current Canvas updates not accessed. User requires O14 baseline→real P-ACCEPT→separate A-FINAL→manual Canvas. These cannot be claimed complete before tests actually run.

## Remaining estimate and unresolved paper decisions

CP13 ~4–8 passes remained. This CP14 closes the evidence-map structure and identifies the obsolete Postman plan, but the original-to-public 26-route contracts, supported Javax integration and final owner choices remain. **Approximately 3–7 further focused research passes** (not days) if no major new source conflict. Real full E2E tests cannot be completed before separately authorized engineering; no actual code/test/provider access authorized here.

## Primary sources and related records

Original attached assignment `09_ESP_PROJECT_ASSIGNMENT.pdf`, syllabus `08_CSCI180_SYLLABUS.pdf`, proposal `10_ORGRESOURCE_CONCISE_PROPOSAL.docx` re-opened; [03 course requirements](03_COURSE_COMPLIANCE_PRESERVATION.md), [17 tests](17_TEST_VALIDATION_AND_RELEASE_GATES.md), [18 roadmap](18_SDLC_BACKLOG_DEPENDENCIES_AND_ROADMAP.md), [29 research gate](29_PRE_BUILD_RESEARCH_ACCEPTANCE_GATE.md), [30 original 26 crosswalk](30_ORIGINAL_26_ROUTE_CROSSWALK_AND_RUNTIME_CORRECTIONS.md), [32 identity test matrix](32_IDENTITY_STATE_MACHINE_MYSQL_SCHEMA_AND_SECURITY_TESTS.md), [33 MySQL tests](33_TENANT_MYSQL_RESERVATIONS_LEDGER_AND_GLOBAL_COMPATIBILITY.md), [35–39 technical reports](README.md), [40 conflict register](40_CP13_CANONICAL_SRS_EERD_API_CONFLICT_REGISTER.md). External provider facts are dated in corresponding reports. All tests in this matrix remain unrun for V2.
# CP22 — Academic compliance and course-example audit

**As of 2026-11-16. SOURCE INSPECTION; no independent new test execution.**

## Original authority and mandatory technology
The **3-page assignment** explicitly requires useful Ateneo-community service, Jersey REST, JSON, Spring annotations, JPA/Spring Data, Javax Validation, printed "RetroJit" for external services "at the very least Twilio", scheduling, five or more justified entities, GET retrieval / POST submission, CRUD during runtime, `@Autowired` controller→component forwarding, and Postman definitions **for all REST calls**. Word proposal Part I Oct 30, skeleton implementation Part II Nov 13 with Canvas upload printed Nov 14, final logic and Canvas Part III printed Dec 5. Group size one to three; peer reviews and video instructions must be applied to actual group and current Canvas. UI is optional bonus.

The **9-page syllabus** CLO3–8 cover Jersey, Postman, MySQL/Hibernate, JPQL, Javax Validation, Retrofit/Gson; CLO9 says receiving/serving images through HTTP, CLO10 final project. Course outline maps final to CLO10, while assessment table maps proposal/implementation to CLO9: unresolved internal course ambiguity. Certificate of Authorship and acknowledgment of outside help are explicit; no independent explicit generative-AI rule found in syllabus, so check current Canvas. Its general expected-date grace policy does not prove an automatic final deadline extension.

## Five-entity original and operations
The submitted-looking *source proposal*, not evidence of actual Canvas upload, is the 12-page `10_ORGRESOURCE_CONCISE_PROPOSAL.docx`. It specifies `Department, Project, Resource, ResourceRequirement, Allocation`, numeric keys, 25 explicit GET/POST operations, global hold of approved stock regardless of project dates, entire-batch reusable ISSUE/RETURN, consumable CONSUME, and hourly SMS reminders. O14 adds A09 SMS-consent revocation for **26** actual handlers: Department4, Project4, Resource5, Requirement4, Allocation9. Historical nine-entity/68-operation draft is not the controlling course proposal.

## Source-level course examples and implications
| Source inspected | What it demonstrates | What it does NOT establish |
|---|---|---|
| `3 - Jersey 2020.ppt` / `03_COURSE_REFERENCE_CORPUS.txt` | `ResourceConfig`, Jersey annotations and Spring controller/service examples | Some old lecture examples directly inject repositories; assignment's stricter controller→component wins |
| `Misc JPA --Validation.pptx` | Literal Javax annotations and entity-save JPA validation discussions | DTO validation alone does not prove JPA lifecycle validation |
| `06_VIDEO_TRANSCRIPTS.md` V03 ~534–596 s | Timestamped *audio* reference for saving validated JPA entities | No original video frames observed |
| `Twilio SMS Notification Setup.pptx` | Retrofit integration with an actual Twilio-facing service/Virtual Phone teaching example | Does not authorize use of paid account or imply FakeTwilio equals provider |
| Lab4 FakeTwilio and O14 `FakeTwilioTransport` | Real Retrofit/Gson HTTP roundtrip to separate local simulator | No provider SMS, Virtual Phone inbox or handset delivery |
| `SchedulingApp` corpus sample | `@EnableScheduling` and `@Scheduled(fixedRate=...)` | Its DB exclusions are example-specific and must NOT be copied to OrgResource |
| `ImageServerDemo` corpus | Multipart upload and JPEG serve using local disk and filename record | Unsafe path and overwrite patterns must not be copied; CLO9 applicability remains instructor-owned |
| V10 timestamp ~2843–2873 s | Audio lecture describes images as optional topics | Historical comment is not a 2026 instructor waiver |

## CP22 new findings MC57–MC63
- MC57: Published Twilio new-trial country list did not show Philippines at this review; this is **not** a categorical account ban; actual trial eligibility must be checked if needed.
- MC58: O14 `TWILIO_VIRTUAL` runtime enum path intentionally fails with a disabled-mode exception; actual Twilio is NOT implemented.
- MC59: Historical image lecture called image processing optional; 2026 syllabus CLO9 remains ambiguous.
- MC60: Old image demo stores filenames and writes bytes to local disk, not automatically safe/production-compliant.
- MC61: Independent scheduling sample excludes DB autoconfiguration; an integrated OrgResource must keep JPA/MySQL alive.
- MC62: Separate acceptance tests for `javax.validation.Validator.validate(request)` and direct invalid annotated JPA `saveAndFlush` with rollback/no persisted row.
- MC63: Date/syllabus/Canvas ambiguity; printed final Dec5 and current Canvas require student confirmation.

## Academic acceptance requirements, NOT run in CP22
AC22-01..17: exact five entity/JPA rows; original 26 route/Newman and real HTTP errors; `@Autowired` forwarding; DTO and JPA Javax negative tests; JPQL/aggregation; global stock T-H-I and T-H; scheduled expiry/restart and repeat runs; Retrofit simulator; **conditional** real Twilio provider proof; **conditional** image receipt/serve; portable source/Eclipse/MySQL; browser optional; actual final video; authorship/AI disclosure; Canvas/peer review and receipt.

**Source provenance:** original uploaded assignment/syllabus/proposal/course slides/corpus/transcripts plus O14 SHA `2dd336ed39cecd417a8bce3388b303e688e95dee`. The raw source recordings were not watched. See [O14 compliance matrix](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/docs/compliance/FINAL_COURSE_COMPLIANCE_MATRIX.md).
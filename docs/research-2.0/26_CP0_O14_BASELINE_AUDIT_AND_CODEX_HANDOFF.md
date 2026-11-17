# CP0 O14 live academic baseline audit and Codex verification handoff
**Status: CONDITIONAL technical academic baseline identified, but neither final academic acceptance nor production acceptance. Audit: 2026-11-15.** No local tests run by this researcher; no product code or deployment changed.

## Newer observed GitHub state (supersedes earlier same-day 'no O14' observation)
- PR #8 O14 interim academic baseline: https://github.com/newonathe/OrgResource/pull/8, DRAFT/OPEN/unmerged, head 2dd336ed39cecd417a8bce3388b303e688e95dee, base feat/o11-o13-system-acceptance-release-2026-11-15 (PR #6 head fd34a8a66242f795dda758d45f220f2c331472db).
- GitHub Verify runs 43691405530 and 43691401787 SUCCESS on O14 head. Jobs actually perform Java 8/MySQL compilation/tests, React install/typecheck/unit/lint/build, bundled JAR root/static/five-list smoke and artifact upload. These jobs do not run Newman or Playwright.
- O14 author report evidence/O14/FINAL_ACCEPTANCE_REPORT.md records 55 Java tests, 15 frontend unit tests, 5 Playwright flows, 96 Newman requests/142 assertions (including new 23/37 dependency suite) and portable source/JAR packaging. These local outcomes are author-reported, NOT independently executed by the research author.
- main remains 16e781c837dd7e1f0481fb7a01e49c27d80a91e8; PR #7 research is separately based on O8/O10 PR #5, not PR #8. No PR merges/Canvas submission/instructor approval shown.

## Primary course and implementation trace
- Reopened exact attached 09_ESP_PROJECT_ASSIGNMENT.pdf pages 1–3: Jersey REST, JSON, Spring annotations/Autowired controller-to-component, JPA/Spring Data, Javax Validation, external Retrofit/Twilio wording, scheduling, at least 5 justified entities, runtime CRUD, Postman, final video and Dec 5, 2026 Canvas date.
- Reopened exact 08_CSCI180_SYLLABUS.pdf pages 2–4 and 6: CLO1 Eclipse, CLO3 Jersey, CLO4 Postman, CLO5 JPA/MySQL, CLO6 JPQL, CLO7 Javax, CLO8 Retrofit/Gson, CLO9 image receive/serve, CLO10 final; p.4 project mapped to CLO9 despite p.3 final-project mapping to CLO10; Certificate of Authorship/explicit outside help. Current Canvas unknown.
- Reopened 10_ORGRESOURCE_CONCISE_PROPOSAL.docx: 5 entities and 25 original operations, whole-batch allocation; no image API. Current controllers have extra A09: D4 + P4 + R5 + Requirement4 + Allocation9 = 26 handlers.
- Direct O14 source: backend/pom.xml Java8 Boot2.1.0 Jersey2.26 javax REST, validation, JPA, MySQL Connector8.0.13, Retrofit2.4/Gson converter2.1; 5 @Entity classes; all 5 controller classes @Autowired component; SpringBootApp @EnableScheduling; ExpiryScheduler/ReminderScheduler @Scheduled; FakeTwilioTransport loopback-only, mode DISABLED by default.
- Academic Allocation entity has optional phoneNumber and smsConsent: DO NOT claim schema cannot store contact data. Fictional tests only; V2 privacy model is distinct.

## CP0 acceptance/gap matrix
| Area | Classification | Required verification or action |
|---|---|---|
| Java/Jersey/5 entities/26 routes/scheduler/Retrofit | Source verified; GitHub CI green | Preserve separate runnable course executable; no old public deployment |
| Local full tests (55/15/5; 96 requests/142 asserts) | AUTHOR-REPORTED | Independent reproducibility or NOT_RUN when environment inaccessible |
| Release JAR, complete-source and Java/Eclipse ZIP, SHA256SUMS, release manifest | Script and report verified; actual ignored bytes NOT INSPECTED | Confirm generated output from exact PR#8 HEAD, SHA and clean extraction |
| CI Java8/MySQL/JAR smoke and React checks | SUCCESS confirmed GitHub runs | Not a substitute for full Newman/Playwright, portable first-run or hosted prod |
| Standalone new empty orgresource schema and Eclipse import/debug on another machine | NOT_RUN in O14 report | Perform non-destructively or state NOT_RUN |
| CLO9 image receiving/serving | UNRESOLVED | Read Canvas/instructor; do not guess waiver or add speculative routes |
| Actual Twilio versus course FakeTwilio protocol | Simulator technically verified; instructor acceptance UNRESOLVED | Current Canvas/instructor decision; no paid messaging |
| Group/video/peer/authorship/Canvas | STUDENT MANUAL, unverified | Real action/receipt only; no generated or fabricated attestation |
| Supported V2 auth, tenancy, ledger, security, $0 host, restore | NOT IMPLEMENTED/NOT RUN | Separate CP1–P6 after O14 baseline; before A-FINAL and Canvas |

## Critical next actions and Codex starting reference
1. Target current O14 PR#8 head rather than recreate O14 from PR#6. Re-query heads and inspect AGENTS.md before any changes.
2. Independently reproduce safe existing backend/UI/Postman and release tests on fictional isolated orgresource_test only; confirm package manifest points to current final source SHA, not just 286c556 prior implementation commit.
3. Do not reset/delete user databases; do not invent unavailable Windows local protected credentials or an instructor/Canvas permission. Clean portable new-schema setup only with owner authorization.
4. File a precise evidence gap table: PASS/FAIL/BLOCKED/NOT_RUN/UNRESOLVED per requirement, build SHA, actual commands and sanitized logs; preserve old academic code.
5. Do not implement V2, merge, deploy, pay, communicate outside, tag final or submit. Next independent phase CP1 supported Java/Jakarta production BOM, secure auth and actual eligible $0 host/TLS/DR feasibility.

**Authoritative sequence:** O14 preserved baseline → supported V2 production engineering → real P-ACCEPT → final fresh academic A-FINAL → student's manual Canvas submission, printed Dec 5 subject to live Canvas. No production/academic acceptance inferred from O14 technical report.
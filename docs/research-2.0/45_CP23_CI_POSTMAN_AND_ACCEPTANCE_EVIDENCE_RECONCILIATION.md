# CP23 — O14 actual test-source, GitHub CI and release evidence reconciliation

**Dated 2026-11-16. Research-only; no tests launched and no changes to the academic app.**
**Exact academic head:** `2dd336ed39cecd417a8bce3388b303e688e95dee` (draft PR #8).
**Research head before subsequent upload:** `856a2fceabcbbf95b569d8965bd6b4a23d418c54` (draft PR #7).
**Main at time of inspection:** `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`.

## Evidence grades
S = inspected source/test definition; C = actual inspected CI job/log; L = O14 author-reported local execution, not independently rerun; U = unresolved or no supplied proof; N = not run. A green CI run can support only the things its workflow actually executes.

## Exact O14 CI, not research branch CI
- O14 push [run 43691401787](https://github.com/newonathe/OrgResource/actions/runs/43691401787) and PR [run 43691405530](https://github.com/newonathe/OrgResource/actions/runs/43691405530): actual logs inspected; both jobs PASS at the pinned O14 SHA.
- Java 8 + real MySQL 8.0.46: 55 Java tests PASS. Frontend: 15 tests across 3 files PASS. Executable-JAR smoke tests PASS, artifact uploaded.
- Academic workflow builds/tests packages/smokes; it does **not** itself run the full five Newman collections, 5 Playwright journeys, entire release ZIP extraction, independent Eclipse import, Twilio provider, or Canvas upload.
- O14 release report says local execution of 96 Newman requests and 142 assertions and 5 Playwright journeys PASSED. Their definitions exist, but this pass did not rerun them. **L**, not independent C.
- O14 CI executable artifact (Actions) is retrievable; it does not by itself include the tracked full source release archives/manifest.

## CI nonuniformity
At earlier identical research-branch commit `f224e00...`, push [run 43732859018](https://github.com/newonathe/OrgResource/actions/runs/43732859018) failed **one of 55 Java/MySQL tests** while PR [run 43732864033](https://github.com/newonathe/OrgResource/actions/runs/43732864033) passed all 55. Failure:
`ResourceListAggregationMySqlTest.oneClockSampleAndOneGroupedQueryProduceExactAvailabilityAtProjectEndBoundary` line 107 expected 5, observed 8. Fixture: T10, effective APPROVED q3, ISSUED q2 and a separately approved q4 ending exactly at fixed clock; correct expected =10−3−2=5. Later runs green do not prove root cause; no root-cause test or code change authorized. See research [42](42_CP14_O14_JAVA_MYSQL_INTERMITTENT_CI_FAILURE.md).

## Actual route and Postman inventory
Five Jersey controller classes expose **26 annotated handlers**:
D01–D04=4, P01–P04=4, R01–R05=5, Q01–Q04=4, A01–A09=9.
The static reference `postman/OrgResource-Current-26-Routes.postman_collection.json` catalogs those 26; it is NOT a safe sequential acceptance suite.

The five executable source collections together have:
| Collection | Requests | Static `pm.test` declarations |
|---|---:|---:|
| O1 Skeleton | 15 | 29 |
| O3 CRUD | 20 | 30 |
| O3 Department Update | 11 | 19 |
| O4/O5 Allocation/Expiry | 27 | 27 |
| O14 Dependency Integrity | 23 | 37 |
| **Total** | **96** | **142** |

Every 26 academic handler is represented across executable suites. These are independently parsed **definitions**, not proof of 96 successful HTTP outcomes in this review.

## Course requirement ledger
| Item | Evidence | Gap |
|---|---|---|
| Jersey, JSON, Spring `@Autowired` | S+C | Full 26-route runtime L |
| Five JPA entities, MySQL/JPQL | S+C (55 Java integration tests) | Independent clean workstation and schema inspection L/U |
| Javax DTO validation | S+C/L | Distinct direct invalid JPA `saveAndFlush` test U |
| Retrofit/Gson | S+C local simulator transport | Twilio provider and acceptance U |
| Scheduling/expiry/reminders | S+C code/tests | Real operational scheduling and provider modes L/U |
| 26 route Postman coverage | S, 26/26 | Actual Newman L |
| React bonus UI | S+C, 15 tests | Local Playwright L; independent browser U |
| Image receive/serve CLO9 | Syllabus source | Applicability U |
| Proposal/export/video/peer/COA/Canvas | Uploaded proposal S, report L | Student actions U/N |
| Release ZIP/manifest | Packaging script S, report L | Final package custody U |

**MC64** Do not treat workflow green as all local-suite execution.
**MC65** An invalid JPA entity-save Javax test is a separate gate, not established by DTO validation or DB NOT NULL.
**MC66** Full release bundle not tracked at pinned O14 SHA; CI JAR artifact only is verified.

## Next required academic evidence
Repeat pinned suite, isolate intermittent expected5/actual8 failure without altering expected answer, get clean JPA Javax invalid save+flush, actual Newman reports/DB postconditions, independent extracted package hashes/Eclipse, course CLO9/Twilio confirmation and student video/authorship/Canvas. Public P-ACCEPT and academic A-FINAL remain NOT RUN.

See [O14 acceptance report](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/evidence/O14/FINAL_ACCEPTANCE_REPORT.md) and [O14 route matrix](https://github.com/newonathe/OrgResource/blob/2dd336ed39cecd417a8bce3388b303e688e95dee/evidence/O14/ROUTE_ACCEPTANCE_MATRIX.md).
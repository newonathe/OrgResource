# OrgResource O3 continuation report

Captured: 2026-11-14 (Asia/Manila)

## Scope and source audit

This continuation resumed from the O1/O2 Java skeleton. Read the O1 phase report, the complete OrgResource handoff instructions/master and API/data/persistence/test chapters, the assignment PDF, syllabus PDF, concise proposal DOCX, existing Java backend, course source corpus and timestamped video transcript pack. The original video files were not reprocessed.

Authority was applied as specified: assignment requirements, syllabus and verified course context, user decisions, compatible course code, original five-entity/25-route proposal, then reviewed handoff amendments. The assignment requires Jersey/JSON, Spring `@Autowired` components, Spring Data JPA, Javax Validation, basic create/read for all entities, runtime row creation and deletion, and Postman definitions. The skeleton already meets those Part II structure gates. The syllabus lists image receiving/serving under CLO9, but the concise proposal has no image routes; that discrepancy remains open. No image endpoint or sixth entity was added.

Route parity is recorded in `ROUTE_PARITY.md`: the O1 implementation had 12 of the original 25 proposal operations; this slice adds D03, leaving 13 of 25 original operations to implement. The optional handoff-only A09 route is tracked separately. The source is the current evidence for implementation; handoff examples are specification, not proof of behavior.

## Chosen slice: D03 Department rename

Added `POST /api/departments/{id}/update` with a `{ "name": "Operations" }` JSON body. The Jersey controller validates and delegates through its injected `DepartmentComponent`. The component rejects nonpositive IDs and blank/overlong names, normalizes display/key values, returns 404 for a missing Department, allows a case-only rename of the same normalized name, and returns 409 `DUPLICATE_NAME` for another Department's normalized name. It calls `saveAndFlush`; the database's named `uk_department_name_key` unique constraint remains authoritative for races, and its Hibernate violation is mapped to the same JSON 409.

Added regression behavior for Jersey unknown routes and malformed path parameters: an unknown route returns controlled JSON 404 `NOT_FOUND`; a nonnumeric path ID returns JSON 400 `VALIDATION_FAILED` rather than `INTERNAL_ERROR` 500.

Changed files:

- `.gitignore` (ignore workstation-specific VS Code settings and the preserved extracted old ZIP folder)
- `backend/.classpath`, `backend/.project`, and `backend/.settings/*` (Java/Eclipse workspace metadata generated during local project opening)
- `backend/src/main/java/app/dto/ApiDtos.java`
- `backend/src/main/java/app/components/DepartmentComponent.java`
- `backend/src/main/java/app/rest/controllers/DepartmentController.java`
- `backend/src/main/java/app/config/ApiExceptionMapper.java`
- `backend/src/test/java/app/components/DepartmentComponentTest.java`
- `backend/src/test/java/app/config/ApiExceptionMapperTest.java`
- `backend/README.md`
- `deliverables/OrgResource-Java-Skeleton-2026-11-13.zip` (superseded archive removed from the current tree)
- `deliverables/OrgResource-Java-Skeleton-2026-11-14.zip` and `.sha256`
- `postman/OrgResource-O3-Department-Update.postman_collection.json`
- `evidence/O1/PHASE_REPORT.md` (points to this O3 continuation)
- `evidence/O3/ROUTE_PARITY.md`
- O3 result JSON, HTTP, database and clean-export evidence under `evidence/O3/`
- `evidence/O3/PHASE_REPORT.md`

## Verification

Toolchain: Temurin JDK 8u512, Maven wrapper 3.3.4/Maven 3.10.0, MySQL 8.0.46 isolated on `127.0.0.1:3308`, Newman 6.2.3. All examples use local fictional data only.

| Check | Command/evidence | Outcome |
|---|---|---|
| Java unit tests | `cd backend; .\mvnw.cmd -o -B -ntp clean test` with Java 8 | PASS: 8 tests, 0 failures/errors/skips. Five `DepartmentComponentTest` cases and three `ApiExceptionMapperTest` cases. |
| Clean package | `cd backend; .\mvnw.cmd -o -B -ntp clean package` with Java 8 | PASS: executable JAR packaged; same 8 tests pass. |
| D03 HTTP | `newman run postman/OrgResource-O3-Department-Update.postman_collection.json -e postman/OrgResource-O1-Local.postman_environment.json -r cli,json` | PASS: 11 requests, 11 scripts, 19 assertions, 0 failures. Covers create fixtures, normalized rename, same-key case change, duplicate conflict, missing, nonpositive and malformed IDs, blank-name validation, and cleanup. Raw report: `newman-d03-results-final.json`. |
| O1 HTTP regression | `newman run postman/OrgResource-O1-Skeleton.postman_collection.json -e postman/OrgResource-O1-Local.postman_environment.json -r cli,json` | PASS: 15 requests, 15 scripts, 29 assertions, 0 failures. Raw report: `newman-o1-regression-final.json`. |
| Unknown API path and malformed ID | `error-mapping-http.json` | PASS: unknown route yields JSON 404 `NOT_FOUND`; nonnumeric Department path ID yields JSON 400 `VALIDATION_FAILED`. |
| Repository/file hygiene | See Git review before commit | No source archives or original video files staged; no credentials/environment files staged; Maven output ignored. |

During verification, early D03 collection attempts had a Postman variable collision with the O1 environment and a mistaken expected-case assertion; both test-definition issues were fixed and the final focused run passed. The first O3 app integration attempt exposed the unknown-route 500, and an additional malformed-ID probe later exposed another 500; both mappings were corrected and regression-tested before final runs. One Maven clean was blocked because the running Java process held the JAR; the app was stopped and the subsequent clean package passed. These were verified/fixed work-in-progress failures, not final test failures.

Maven logs include nonfatal metadata warnings from old repositories (`maven.java.net` certificate and `repo.spring.io` 401). Artifact resolution completed and all final checks above passed.

## Database and fixture state

D03's two generated fictional Departments were removed by collection cleanup; `db-state-after-o3.txt` records 0 remaining `O3 %` Department names. The O1 regression collection creates fictional project/resource/requirement/allocation sample rows; they remain local in the dedicated OrgResource database for development. Current counts are allocation 7, department 8, project 7, resource 7 and resource_requirement 7. No existing MySQL80 service/database was modified.

## GitHub and handoff status

The repository is `https://github.com/newonathe/OrgResource`, initialized as a **private** GitHub repository on `main`. The O1 baseline commit was pushed before D03. The refreshed O3 source ZIP is `../deliverables/OrgResource-Java-Skeleton-2026-11-14.zip` (47 files, 37,507 bytes, SHA-256 `875E361A2D7E50649A7383EA6E6A808002892FD3BE4D5923BDA3CB929BF91B30`). It was extracted to a new temporary directory and its Maven wrapper ran offline `clean test` with JDK 8: `BUILD SUCCESS`, all 8 tests passed. The captured clean-export log is `zip-clean-extract-test.txt`; archive contents are listed in `source-zip-file-list.txt`. The archive includes Eclipse metadata and tests, excludes target output and credentials. The video remains `VIDEO_NOT_CREATED`; a shot list exists in `../O1/VIDEO_NOT_CREATED.md`. Eclipse itself was unavailable, so an actual Eclipse import remains NOT_RUN. No Canvas submission, publishing, merge, deployment or real SMS was performed.

## Next bounded step

Move to Project P03/P04 while keeping `Department` stable: verify the project's future-period rules and historical approval dependency before updating dates/deleting. Then continue Resource, Requirement and Allocation routes in dependency order from `ROUTE_PARITY.md`. Keep the CLO9 question open until the authoritative assignment/instructor/Canvas interpretation settles it.

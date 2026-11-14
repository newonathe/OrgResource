# OrgResource Java backend (O1–O6)

This backend is the first, deliberately bounded implementation slice for the CSCI 180.05 OrgResource project. It uses the supplied course-era stack: Java 8, Spring Boot 2.1.0, Jersey/JAX-RS `javax`, Spring Data JPA, Javax Validation, and MySQL. The Eclipse metadata supports Java 8 and m2e; day-to-day editing can remain in VS Code.

## Implemented and verified through O6

- Exactly five JPA entity types: `Department`, `Project`, `Resource`, `ResourceRequirement`, and `Allocation`.
- Five Spring Data repositories, five Spring components, and five Jersey controller families.
- JSON create and list endpoints for all five entities, Department rename, Resource availability, and guarded Department delete.
- Generated positive IDs, explicit table/column names, InnoDB foreign keys, unique normalized Department/Resource names, and the unique Project/Resource Requirement pair.
- DTOs separate the public API from the persistence graph. Allocation responses never contain `phoneNumber`.
- Javax Validator checks JSON request DTOs. Project date-times are local Philippine wall times in `YYYY-MM-DDTHH:mm:ss` format.
- The local MySQL connection is environment-configured. No password is stored in source or this project folder.
- Upcoming reusable return reminders use actual Retrofit/Gson HTTP to the supplied FakeTwilio contract. `DISABLED` is the default; `FAKE_TWILIO` is loopback-only with dummy Authorization and records `SIMULATED`, never message delivery.
- Reminder claims commit before HTTP. Eligibility is rechecked before send, results are saved under a fresh Allocation lock, and stale or uncertain claims become `UNKNOWN` without automatic resend. External HTTP is outside database transactions.
- Resource listing uses one grouped commitments query and one Asia/Manila Clock sample instead of per-resource aggregate queries.
- Integration tests use a guarded, separately configured `orgresource_test` database and cannot initialize the regular development schema.

Implemented routes:

| Method | Route | Current behavior |
| --- | --- | --- |
| POST / GET | `/api/departments` | Create and list |
| POST | `/api/departments/{id}/update` | Rename; normalized duplicate conflicts return JSON 409 |
| POST / GET | `/api/projects` | Create; list optionally filtered by `departmentId` |
| POST | `/api/projects/{id}/update` | Update name and/or valid Project dates |
| POST | `/api/projects/{id}/delete` | Delete only when no Requirements reference the Project |
| POST / GET | `/api/resources` | Create; list optionally filtered by `type` |
| POST | `/api/resources/{id}/update` | Guarded name/total correction with server-derived availability |
| POST | `/api/resources/{id}/delete` | Delete only when stock is zero and no Requirements reference it |
| GET | `/api/resources/{id}/availability` | Original two-field availability response |
| POST / GET | `/api/requirements` | Create; list optionally filtered by `projectId`; computed coverage query |
| POST | `/api/requirements/{id}/update` | Change quantity only when current commitments remain valid |
| POST | `/api/requirements/{id}/delete` | Delete only when no Allocation history references it |
| POST / GET | `/api/allocations` | Create; redacted list optionally filtered by `requirementId` and `status` |
| POST | `/api/allocations/{id}/approve` | Approve only when live Resource stock and Requirement demand permit |
| POST | `/api/allocations/{id}/issue` | Issue approved reusable stock during the Project interval |
| POST | `/api/allocations/{id}/return` | Record a complete usable return, including after Project end |
| POST | `/api/allocations/{id}/consume` | Atomically consume approved consumable stock once |
| POST | `/api/allocations/{id}/cancel` | Cancel a live request or approval with the correct reason |
| POST | `/api/allocations/{id}/delete` | Physically delete only never-approved pending/cancelled records |
| POST | `/api/allocations/{id}/revoke-sms-consent` | Clear consent/contact; optional A09 amendment outside original route count |
| POST | `/api/departments/{id}/delete` | Safe example: refuses deletion while Projects reference the Department |

Create responses use HTTP 201; list and delete successes use HTTP 200. Invalid request fields return a JSON 400; duplicate normalized names and known dependency conflicts return JSON 409.

The O3 project/resource/requirement CRUD routes are covered by `../postman/OrgResource-O3-CRUD.postman_collection.json`; Department rename is covered by `../postman/OrgResource-O3-Department-Update.postman_collection.json`. The O1 routes are covered by `../postman/OrgResource-O1-Skeleton.postman_collection.json`.

Allocation lifecycle routes A03–A08 and the separate A09 consent revocation route are exercised by `../postman/OrgResource-O4-O5-Allocation-Expiry.postman_collection.json`. It generates unique fictional fixture names; returned and consumed history remains because A08 protects historically approved Allocations.

## Not part of this implementation slice

All 25 original API operations are implemented. A09 consent revocation is implemented separately; O6 adds no API route or JPA entity. See `../evidence/O4-O5/ROUTE_PARITY.md` for the route map and `../evidence/O6-O7/PHASE_REPORT.md` for this phase's evidence. The current React UI covers Department and Resource workflows only; later screens remain future scope. Real Twilio provider integration, image upload/serve behavior, reviewed production migrations, and hosted deployment are not implemented. CLO9 image scope remains unresolved.

The syllabus maps the final project to CLO9 (receiving and serving images over HTTP), while the concise proposal's 25-route API does not specify image endpoints. The instructor/Canvas requirement remains unresolved. The image feature is therefore not silently added to this O1 skeleton; resolve the scope before final-project acceptance.

`spring.jpa.hibernate.ddl-auto=update` is used for the local academic prototype schema. Replace it with reviewed migrations before broader development/release.

## Test-only database setup

For local MySQL integration tests, set `ORG_TEST_DB_URL`, `ORG_TEST_DB_USER`, and `ORG_TEST_DB_PASSWORD` from a protected local credential store, then run `.\mvnw.cmd clean verify` from this folder. The workstation helper `%LOCALAPPDATA%\OrgResourceDev\Set-OrgResourceTestEnvironment.ps1` loads the dedicated test-schema credentials into the current PowerShell process without printing them. A startup guard refuses schemas other than `orgresource_test`. Never substitute `ORG_DB_*` for tests.

## Local build and run

Use a JDK 8 installation. Maven 3.9+ runs on Java 8. The project build targets Java 8 bytecode and the executable JAR uses the supplied database environment variables.

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk8'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:ORG_DB_URL = 'jdbc:mysql://127.0.0.1:3308/orgresource?useSSL=false&serverTimezone=UTC'
$env:ORG_DB_USER = 'orgresource_dev'
$env:ORG_DB_PASSWORD = '<set locally; never commit it>'
.\mvnw.cmd clean package
java -jar target\orgresource-0.1.0-SNAPSHOT.jar
# Run the Java unit tests
.\mvnw.cmd test
```

The HTTP server binds to `127.0.0.1:8080`. For this workstation, the database and password are provisioned separately under `%LOCALAPPDATA%\OrgResourceDev`; its DPAPI-protected credential file is not portable and is intentionally excluded from source archives. The O1 report records the actual schema, HTTP responses, build result, and restart check.

## Reminder modes

`ORGRESOURCE_REMINDERS_MODE` defaults to `DISABLED`. To exercise only the course simulator, run the supplied FakeTwilio app bound to `127.0.0.1:9997` and set `ORGRESOURCE_REMINDERS_MODE=FAKE_TWILIO` before starting the backend. The adapter uses dummy Authorization and refuses non-loopback HTTP targets. The course simulator prints its request header, destination, and body to its own console; use only fictional test data and never configure real provider credentials here. `TWILIO_VIRTUAL` fails closed because a live-provider adapter is outside this phase.

## Source constraints

The course assignment and syllabus outrank proposal and planning documents. The O1 implementation preserves the five proposed entity types and the original proposal's route shapes where implemented. The broader handoff is a reviewed design plan, not proof that those future features already exist.

# OrgResource Java backend

This is the current five-entity service, not the earlier O1 skeleton. It uses Java 8-compatible bytecode, Spring Boot 2.1, Jersey/JAX-RS `javax`, Spring Data JPA/Hibernate, Javax Validation, MySQL/InnoDB, Retrofit/Gson, and Spring scheduling. Jersey controllers are Spring components; each forwards through `@Autowired` to a Spring component. The bundled React application is served by the same executable JAR.

## Current API

The API has the 25 operations from the current concise proposal, the separately documented A09 consent-revocation addition, and Course A's two Resource image operations. The importable [28-operation Postman reference](../postman/OrgResource-Course-A-28-Routes.postman_collection.json) is the current route inventory. See [`../docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md`](../docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md) for the original 26-operation request/response and invariant contract.

| Controller | Component | Scope |
|---|---|---|
| `DepartmentController` | `DepartmentComponent` | Department create/list/update/delete |
| `ProjectController` | `ProjectComponent` | Project create/list/update/delete |
| `ResourceController` | `ResourceComponent` | Resource create/list/update/delete and availability; multipart image upload and raw image retrieval |
| `RequirementController` | `RequirementComponent` | Requirement create/list/update/delete and derived coverage |
| `AllocationController` | `AllocationComponent` | Allocation create/list, lifecycle transitions, protected deletion, A09 consent revocation |

Create operations return 201. Reads, updates, lifecycle operations, and successful deletes return 200. Errors use a JSON body with `code` and `message`; validation is 400, missing records 404, and known state/dependency conflicts 409. Allocation DTOs never expose `phoneNumber`. Resource availability, Requirement coverage, and shortage are derived from persisted state rather than stored as duplicate totals.

### Resource images

- `POST /api/resources/{id}/image` accepts one multipart field named `file`; PNG/JPEG bytes are verified against the declared media type and decoded within a 16,777,216-pixel safety bound. Upload size is at most 2,097,152 bytes.
- `GET /api/resources/{id}/image` returns stored bytes with their validated `image/png` or `image/jpeg` media type, `no-store` caching, and `nosniff`. Missing Resource/image is 404; invalid content is 400; oversize is 413.
- Image bytes and media type use nullable columns on the existing `resource` table; there is no sixth JPA entity or image table. The list uses a targeted scalar projection, and Hibernate 5.3 build-time enhancement makes the BLOB field genuinely lazy for ordinary entity reads and locked inventory updates. The MySQL test verifies it remains unloaded through a pessimistic read and refresh, then loads only when the image accessor is called.
- The course demo binds to loopback and uses only the tracked synthetic fixture. This is not a public upload service or private-image access-control implementation.

## Build and tests

Use JDK 8 for the course toolchain. The project Maven Wrapper is included.

```powershell
.\mvnw.cmd -B -Pbundle-react-ui clean verify
```

The integration suite requires `ORG_TEST_DB_URL`, `ORG_TEST_DB_USER`, and `ORG_TEST_DB_PASSWORD` for a disposable, fictional-data database named `orgresource_test`. The test-profile guard refuses any other schema. The local developer machine may load those values from `%LOCALAPPDATA%\OrgResourceDev\Set-OrgResourceTestEnvironment.ps1`; the helper is private and is not included in source exports. Never substitute `ORG_DB_*` or the regular development database for tests. Tests do not reset the schema and remove only their own uniquely named fixtures.

Build the React client first when using the bundle profile:

```powershell
Push-Location ..\frontend
npm ci
npm run typecheck
npm test
npm run lint
npm run build
Pop-Location
```

Then run Maven from this folder. The Course A release procedure and its exact commands are in [`../docs/RELEASE_AND_DEMO.md`](../docs/RELEASE_AND_DEMO.md). `scripts/package-release.ps1` runs both sets of checks, creates the executable JAR and source archives, and writes SHA-256 sums after the source commit is clean.

## MySQL and runtime configuration

The ordinary application reads `ORG_DB_URL`, `ORG_DB_USER`, and `ORG_DB_PASSWORD`. Keep these values outside Git and do not print them into logs or reports. It binds to `127.0.0.1:8080`; `/` serves the bundled React app and `/api/` serves JSON. Use the [portable first-run guide](../docs/PORTABLE_FIRST_RUN.md) to create a new, non-destructive local schema/account and start a release without the developer workstation credential script.

The test profile reads only `ORG_TEST_DB_*` and includes a guard that requires the `orgresource_test` schema. Do not rename or remove that guard to make local setup appear portable.

## Reminder behavior

`orgresource.reminders.mode` defaults to `DISABLED`. `FAKE_TWILIO` uses Retrofit/Gson with a loopback-only fictional simulator and dummy authorization. The separate `TWILIO_VIRTUAL` transport implements the Twilio Messages API form and Basic-auth protocol. It requires `ORGRESOURCE_TWILIO_BASE_URL`, `ORGRESOURCE_TWILIO_ACCOUNT_SID`, `ORGRESOURCE_TWILIO_AUTH_TOKEN`, and `ORGRESOURCE_TWILIO_FROM_NUMBER` from process environment; it is not enabled or exercised against a provider by the acceptance suite. Local tests use only a loopback protocol server and synthetic values. Reminder claims persist before network I/O, redirects and automatic request retries are disabled, uncertain outcomes are not replayed, and `PROVIDER_ACCEPTED` is not carrier delivery.

## Current boundaries

- Course A has five JPA entities: `Department`, `Project`, `Resource`, `ResourceRequirement`, and `Allocation`, with 28 operations (the 25 proposal routes, A09, and two image routes). The assignment says **at least five**, not at most five; this course edition's count is not a cap on a later separately reviewed product.
- A09 is an additional operation, not an additional entity.
- No production authentication, campus integration, real SMS delivery, migration framework, or cloud deployment is claimed.
- CLO9 image receive/serve is implemented and has local Java 8/MySQL/HTTP byte-verification evidence; current Canvas or instructor acceptance remains **NOT VERIFIED**. See [`../docs/compliance/CLO9_IMAGE_DECISION.md`](../docs/compliance/CLO9_IMAGE_DECISION.md) and the Course A acceptance report.
- The root `OrgResource-Formal-Proposal.docx` and `OrgResource-Specification-Package.zip` are historical planning artifacts. Their nine-entity/68-operation material is **HISTORICAL, NOT CURRENT IMPLEMENTATION**.
- This Java 8/Spring Boot 2.1 academic runtime is not a supported public production service. It lacks authentication, tenant isolation, account recovery, a stock-movement audit ledger, production migrations, backup/restore operations, and an approved public demo/runtime. Do not expose it publicly. Production development is deferred until a separate reviewed prompt; research documents are proposals, not implementation authorization.

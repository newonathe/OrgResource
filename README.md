# OrgResource

OrgResource is a local academic application for planning one student organization’s projects, resource requirements, inventory commitments, and allocation history. It is a CSCI 180.05 course project, not an Ateneo-endorsed or production campus service.

## Current implementation

- **Five JPA entities:** Department, Project, Resource, ResourceRequirement, and Allocation.
- **28 current API operations:** the original 25 proposal operations, the separately documented A09 SMS-consent revocation amendment, and two Resource image HTTP operations for Course A.
- **Eight React screens:** Overview, Allocations, Resource Library, Projects, Requirements, Departments, Reminder Activity, and About.
- **Stack:** Java 8-compatible Spring Boot 2.1, Jersey/JAX-RS `javax`, Spring Data JPA/Hibernate, Javax Validation, MySQL/InnoDB, and React/TypeScript/Vite.
- **Inventory:** the server computes availability and requirement coverage from current persisted commitments. Pending allocations do not reserve stock. Reusable and consumable stock follow different lifecycle rules.
- **Reminders:** disabled by default. FakeTwilio remains a loopback simulator. A separate official Twilio Messages protocol adapter is covered by a fictional loopback server test; no provider account or SMS was used. `SIMULATED` and `PROVIDER_ACCEPTED` do not mean a phone received a message.
- **Images:** `POST` multipart upload and raw-byte `GET` under `/api/resources/{id}/image`; PNG/JPEG only, maximum 2 MiB, content and dimensions validated, bytes stored on the existing Resource row. See the Course A acceptance report for actual MySQL/HTTP evidence.

The assignment requires **at least five justified entity types**. The O14 baseline implemented five and preserved the proposal’s 25 operations plus A09; Course A adds only the two authorized image operations. This academic scope is not a maximum for a later, separately reviewed production product. Course A does not begin that production expansion.

The original project-design contract is [the 26-operation API specification](docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md); Course A adds the two specified image operations. The importable current [Postman reference contains all 28 operations](postman/OrgResource-Course-A-28-Routes.postman_collection.json). Fresh implementation evidence is in the [Course A A1+A2 acceptance report](evidence/COURSE-A/A1_A2_ACCEPTANCE_REPORT.md); O14 evidence remains a historical 26-operation baseline.

## Run the packaged application

The executable JAR is delivered separately from the source archives. For a developer-machine demo, the existing `scripts/start-release-demo.ps1` uses the protected local `orgresource_test` credentials. For another computer, follow the non-destructive [portable first-run guide](docs/PORTABLE_FIRST_RUN.md) and use `scripts/start-portable-demo.ps1`; it prompts for a local database password without saving it.

The web interface is served at <http://127.0.0.1:8080/> and the JSON API is under `/api/`. Stop the Java process with **Ctrl+C** in the PowerShell window that started it. MySQL remains available.

## Build from source

Requirements: JDK 8 for the course-compatible Java build, MySQL 8 with InnoDB, and Node.js 20.19+ for the React build. From the repository root in PowerShell:

```powershell
Push-Location frontend
npm ci
npm run typecheck
npm test
npm run lint
npm run build
Pop-Location
Push-Location backend
.\mvnw.cmd -B -Pbundle-react-ui clean verify
Pop-Location
```

The Maven integration tests require protected `ORG_TEST_DB_*` values for the isolated `orgresource_test` schema. Do not point them at the regular development schema. Portable database setup and the JAR runtime command are documented in the [first-run guide](docs/PORTABLE_FIRST_RUN.md). The release packaging script is `scripts/package-release.ps1`; it requires a clean, committed HEAD and a stopped OrgResource process before it runs the verified build and writes release archives.

## Acceptance and course status

See the [course compliance matrix](docs/compliance/FINAL_COURSE_COMPLIANCE_MATRIX.md), the [CLO9 implementation record](docs/compliance/CLO9_IMAGE_DECISION.md), and the [Course A A1+A2 acceptance report](evidence/COURSE-A/A1_A2_ACCEPTANCE_REPORT.md). Local tests establish software behavior only. They do not establish Canvas submission, instructor approval, group or peer requirements, a real recording, or academic authorship certification.

## Historical material — not the current implementation

The older [formal proposal](OrgResource-Formal-Proposal.docx), [specification package](OrgResource-Specification-Package.zip), and earlier handoff chapters are retained for provenance. Some describe a nine-entity, 68-operation concept. Those counts and requirements are **HISTORICAL, NOT CURRENT IMPLEMENTATION**. Course A has five JPA entities and 28 HTTP operations; the current concise proposal remains the source for the original 25 design operations. The proposal is retained at `ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx`; its existence does not establish that it was submitted or approved.

## Production track — deferred

This branch is an academic baseline, not the finished multi-organization product or a production-ready service. It has no real authentication, account recovery, tenant isolation, organization-specific roles/membership, stock-movement audit ledger, reviewed migration/backup/restore system, supported public runtime, public synthetic-data demo, or validated $0 operating plan. It must not be exposed publicly. Production implementation is deferred until the user supplies the next separately reviewed master prompt; `docs/research-2.0/` and its separate PR are proposals and are not automatically approved requirements.

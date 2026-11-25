# OrgResource O4–O5 phase report

Captured: 2026-11-15 (Asia/Manila)

Base: O3 commit `6ced417b0d1ab84fe4f88dceff5fdc79d2d4dd22`

Branch: `feat/o4-o5-allocation-expiry-2026-11-15`

## Scope delivered

- Implemented the six remaining original Allocation routes A03–A08. The original proposal now has 25/25 routes implemented.
- Implemented optional amendment A09 (`POST /api/allocations/{id}/revoke-sms-consent`) separately from the original route count.
- Added O5 expiry processing for bounded batches of PENDING/APPROVED allocations. Each candidate is rechecked and changed in its own transaction through the proxied Allocation component. The default delay is one hour and batch size is 100. ISSUED allocations are not auto-returned.
- Kept the five-entity Java 8 / Spring Boot 2.1 / Jersey / JPA / MySQL architecture. Inventory and coverage remain derived from persisted records. Multi-record lifecycle changes use Project → Resource → Requirement → Allocation pessimistic locks and READ_COMMITTED transactions.
- Updated route parity, README, focused MySQL/unit tests, and the standard Postman v2.1 O4/O5 collection.

## Verification

| Check | Result |
|---|---|
| `backend`: `mvnw -o -B -ntp clean test` using JDK 8 and the protected local DB configuration | PASS — 47 tests, 0 failures, 0 errors, 0 skipped; Maven `BUILD SUCCESS` |
| MySQL integration tests | PASS — 5 lifecycle tests cover approval races, stock/demand races, Resource/Project/Requirement update races, rollback after a Resource flush, cancellation/expiry/action races, and the actual Spring `@Scheduled` scan at a 100 ms test delay. The scheduler expired PENDING/APPROVED fixtures and preserved ISSUED custody. |
| Fixture-cleanup rerun: `mvnw -o -B -ntp -Dtest=AllocationLifecycleMySqlTest test` | PASS — 5/5 MySQL tests; actual scheduler-thread expiry log observed; follow-up queries found 0 O4 integration-test Projects, Resources, or Departments remaining. |
| `backend`: `mvnw -o -B -ntp -DskipTests package` | PASS — executable `backend/target/orgresource-0.1.0-SNAPSHOT.jar` created |
| Packaged service HTTP smoke | PASS — GET `/api/departments`, `/projects`, `/resources`, `/requirements`, and `/allocations` each returned HTTP 200 |
| O4/O5 Newman collection | PASS — 27 requests, 27 assertions, 0 failures; [raw report](newman-o4-o5-results.json) |
| O1 Newman regression | PASS — 15 requests, 29 assertions, 0 failures; [raw report](newman-o1-regression.json) |
| O3 Newman regression | PASS — 20 requests, 30 assertions, 0 failures; [raw report](newman-o3-regression.json) |
| D03 Newman regression | PASS — 11 requests, 19 assertions, 0 failures; [raw report](newman-d03-regression.json) |
| Database | PASS — MySQL 8.0.46; exactly five tables (`allocation`, `department`, `project`, `resource`, `resource_requirement`), all InnoDB |
| Final database state | PASS — row counts: Department 13, Project 11, Resource 11, Requirement 11, Allocation 11; no expired open Allocations; O4 demonstration retains one RETURNED and one CONSUMED Allocation. Three earlier isolated O4 integration-test fixture sets were identified by their generated `O4 Project` / `O4 Resource` / `O4 Department` names and removed with FK-safe deletes; the O4 demonstration records and other fictional sample data were preserved. |
| Shutdown | PASS — sent Ctrl+C to the packaged Java process; Spring scheduler, JPA and Hikari shut down cleanly. MySQL remained available. |

The O4/O5 Postman collection is `postman/OrgResource-O4-O5-Allocation-Expiry.postman_collection.json`. It uses the standard Postman v2.1 `item` array and runtime-generated unique fixture names.

## Remaining scope and known uncertainty

- CLO9 image receiving/serving remains unresolved between the syllabus and concise proposal. No image route, storage, or sixth entity was inferred or added.
- Retrofit/Gson SMS delivery, React UI, packaging for the complete application, reviewed migrations, and release/deployment remain future phases.
- No remote merge or deployment was performed. The O4/O5 branch is intended to depend on O3 branch `codex/o3-crud-integrity` while PR #2 remains unmerged.

## Next phase

Proceed to O6 only after this branch is reviewed: disabled-by-default Retrofit/Gson SMS integration, FakeTwilio HTTP verification, consent withdrawal behavior, durable reminder claims, duplicate-send prevention, and credential protection. Preserve the CLO9 scope question as unresolved.

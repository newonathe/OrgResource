# O3 CRUD implementation continuation

Captured: 2026-11-15 (Asia/Manila)
Baseline: `16e781c837dd7e1f0481fb7a01e49c27d80a91e8`
Working branch at capture: `codex/o3-crud-integrity`; final commit and remote publication status are recorded in the O4–O5 phase report.

## Scope and decisions

Implemented and verified P03/P04, R03/R04 and Q03/Q04. The original route count is now 19/25; A03–A08 remain. Optional A09 remains separate. The CLO9 image requirement is still unresolved against the concise proposal, so this batch adds no image route/entity. The fixed Java 8, Spring Boot 2.1, Jersey/JAX-RS, Spring Data JPA and MySQL architecture remains unchanged.

Project updates normalize names, preserve Department, allow unchanged historical dates, require future dates when dates change, and reject date changes after any historical approval. Project deletion refuses existing Requirements. Resource updates lock the row, enforce the expected-total guard and normalized uniqueness, preserve type, derive commitments from current Allocation state and return server-calculated availability. Resource deletion requires no Requirements and zero stock. Requirement updates lock in Project → Resource → Requirement order, require an active Project and preserve both references; lower bounds use effective coverage and the largest single live PENDING Allocation. Requirement deletion refuses any Allocation history. Coverage now counts APPROVED only before Project end and counts ISSUED/RETURNED/CONSUMED regardless of Project expiry; PENDING/CANCELLED do not count. One Asia/Manila Clock sample is reused per Requirement view/list operation, and null sums become zero.

Actual HTTP verification found that a lazily loaded Project proxy can expose a null public `endAt` field even when MySQL has the date. Requirement and Allocation lifecycle checks now call `Project.getEndAt()`, which initializes the proxy. The MySQL repository regression test covers this lookup path.

## Changed files

- `backend/src/main/java/app/components/ProjectComponent.java`, `ResourceComponent.java`, `RequirementComponent.java`: O3 update/delete rules and transaction boundaries.
- `backend/src/main/java/app/components/AllocationComponent.java`: shared parent lock order and proxy-safe Project end-date access for existing A01 creation.
- `backend/src/main/java/app/repositories/ProjectRepository.java`, `ResourceRepository.java`, `ResourceRequirementRepository.java`, `AllocationRepository.java`: row locks, dependency/commitment queries and time-aware coverage query.
- `backend/src/main/java/app/dto/ApiDtos.java`: existing-shape update request DTOs.
- `backend/src/main/java/app/rest/controllers/ProjectController.java`, `ResourceController.java`, `RequirementController.java`: six POST routes.
- `backend/src/main/java/app/config/ApiExceptionMapper.java`: controlled duplicate and FK conflict mappings.
- `backend/src/test/java/app/components/ProjectComponentTest.java`, `ResourceComponentTest.java`, `RequirementComponentTest.java`: focused mutation, validation and integrity cases.
- `backend/src/test/java/app/repositories/AllocationRepositoryMySqlTest.java`: actual MySQL aggregate boundaries, null sums, pending maximum, historical approvals, row locks and lazy-parent initialization.
- `postman/OrgResource-O3-CRUD.postman_collection.json`: isolated 20-request O3 HTTP suite; collection variables are self-contained and it removes only its uniquely named fixtures.
- `evidence/O3/ROUTE_PARITY.md` and this report: current route/evidence state.
- `evidence/O3/newman-o3-crud-results.json`, `newman-o1-regression-o3.json`, `newman-d03-regression-o3.json`: actual Newman outputs for this continuation.

## Verification evidence

All Java checks used Temurin JDK 8u512 and the existing Maven wrapper. MySQL-backed tests connected to the provisioned MySQL 8 database using the existing DPAPI-protected local configuration; credentials were not printed or placed in files.

| Check | Result | Evidence |
|---|---|---|
| `mvnw -o -B -ntp clean test` | PASS: 38 tests, 0 failures/errors/skips, including the actual MySQL test | `backend/target/surefire-reports/` |
| `mvnw -o -B -ntp -DskipTests package` | PASS: executable Spring Boot JAR created after the passing test run | `backend/target/orgresource-0.1.0-SNAPSHOT.jar` |
| `newman run postman/OrgResource-O3-CRUD.postman_collection.json` | PASS: 20 requests, 30 assertions, 0 failures; confirms persistence, stale-write rejection, dependency protection, zero-stock deletion and cleanup | `newman-o3-crud-results.json` |
| O1 skeleton Newman collection | PASS: 15 requests, 29 assertions, 0 failures | `newman-o1-regression-o3.json` |
| O3 Department D03 Newman collection | PASS: 11 requests, 19 assertions, 0 failures | `newman-d03-regression-o3.json` |
| MySQL O3 database check | PASS: `SHOW TABLES` returned exactly `allocation`, `department`, `project`, `resource`, `resource_requirement`; aggregate rows were 11/8/8/8/8 respectively | Live MySQL CLI query using the protected local environment |
| O3 CRUD fixture cleanup | PASS: zero rows matched `O3 CRUD Department %`, `O3 CRUD Project %` / `O3 Updated Project %`, or `O3 CRUD Resource %` / `O3 Updated Resource %` | Live MySQL count query after Newman runs |

The O1 regression suite intentionally leaves its fictional example rows in the local development database. No database reset or deletion of prior records was done; O3 and D03 collection fixtures were cleaned. The live O3 service was started from the packaged JAR and all three Newman suites ran against `127.0.0.1:8080`.

## Git and next phase

The isolated worktree remains on `codex/o3-crud-integrity`, based on the verified reference commit. Changes are uncommitted. Nothing was pushed or merged. The original O1/O2 working checkout and its unrelated user changes were left untouched.

Next phase: O4 Allocation lifecycle, beginning with A03 approval and continuing through A08 guarded deletion; preserve the Project → Resource → Requirement → Allocation lock order and add rollback and real concurrency coverage. Keep A09 separate. CLO9 remains unresolved.

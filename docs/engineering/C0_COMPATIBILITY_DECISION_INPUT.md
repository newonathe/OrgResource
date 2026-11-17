# C0 Compatibility Decision Input

This note records measured results from the isolated C0 runtime experiment. It is input for a later user-approved engineering decision, not an architecture decision and not a Course A change.

## Experiment boundary

- Source parent: Course Final A3 commit `7f8c0746da18fd54b47f430ac1435ac36e605d67`.
- Experiment source: `experiments/extended-c-runtime-compat/`.
- A3 remains frozen at its original commit and branch.
- The experiment uses the existing five business entity concepts and isolated schema; it is not integrated into Course A and does not replace its Java 8 build.
- Runtime: Java 21.0.3, Spring Boot 4.1.2, Jersey 4.0.3, Spring Framework 7.0.10, Spring Security 7.1.2, Hibernate ORM 7.4.13.Final, Hibernate Validator 9.1.5.Final, MySQL 8.0.46.
- Validation: Jakarta Validation 3.1.1 with Hibernate Validator and literal `javax.validation:validation-api` 2.0.1.Final with Apache BVal 2.0.6 were both loaded in one JVM. The Javax DTO/HTTP path used Apache BVal; the Jakarta probe used Hibernate Validator.
- Integration: Flyway 12.4.0, MySQL Connector/J 9.7.0, Retrofit 2.11.0, Gson 2.11.0, OkHttp 4.12.0.

## Observed results

- Eleven MySQL-backed JUnit integration tests passed in a clean source extraction.
- The run exercised Jersey HTTP, Spring component delegation, JPA commits and queries, Javax/Jakarta validation, database constraints and rollback, Spring Security roles/CSRF, five-table schema/FKs, transactional inventory and competing approvals, Retrofit/Gson request encoding, loopback-only provider fakes, scheduler invocation, and compatibility with the legacy and versioned route families.
- The executable JAR served the built React assets and JSON API, accepted an authenticated CSRF-protected create, restarted, and returned the persisted record.
- SMS remained disabled; provider tests were loopback-only and did not contact Twilio.
- No material runtime incompatibility remained in the tested experiment after wiring corrections. That result is limited to this isolated code and the tested versions; it does not establish production readiness or authorize a migration.

See `evidence/C0/COMPATIBILITY_REPORT.md` for test details, effective dependencies, release provenance, exact SHAs, CI, and limitations.

## Open decision boundary

The C0 experiment does not choose whether Extended C should adopt these versions or add these controls. Before changing Course A or starting a later build phase, the project owner must review the evidence and authorize any separate scope. No C1 work is included here.

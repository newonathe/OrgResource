# OrgResource O1 / O2 handoff report

O1 baseline captured: 2026-11-13 (Asia/Manila). See `../O3/PHASE_REPORT.md` for the current continuation and route status.

## Outcome

The O1 Java backend skeleton is implemented and locally verified. O2 handoff artifacts include an Eclipse/m2e-compatible Java source ZIP, the saved Postman collection and local environment, raw Newman results, database schema evidence, restart-persistence evidence, and a video shot list. No video was recorded. The initial O1 snapshot implemented 12 of the original 25 proposal routes; D03 Department rename was added in the O3 continuation. This is not the complete OrgResource project and is not a course submission.

## Source basis and scope boundaries

Implementation decisions follow the supplied authority order: assignment requirements, syllabus/current verified course direction, explicit user decisions, original compatible course source, original proposal, reviewed master handoff, and historical alternatives. The reviewed master handoff is planning material; it is not evidence of an existing implementation.

The reviewed inputs included the complete `OrgResource_COMPLETE_Handoff/ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md`, the assignment and syllabus PDFs, the concise proposal, the supplied Spring Boot dependency example and original Java course examples. The project-pack transcript index contains timestamped speech transcripts for 12 unique course videos; the duplicate Lab 3 copies are identified there. The transcripts preserve spoken material, not on-screen code or all visual demonstrations. The syllabus assigns CLO9 image receiving/serving, while the original 25-route proposal does not define image operations. That scope conflict remains open for instructor/Canvas clarification and has not been silently resolved by adding image routes to O1.

O1 preserves the five proposed entities and starts the original route set with create/list for each entity, a representative coverage query and one safe-delete example. The remaining original update/delete/state-transition endpoints and full allocation stock lifecycle, concurrency handling, Retrofit/Gson integrations, scheduled business work, React UI, image upload/serve, migration-based production schema and hosting are not implemented.

## Implemented source

Portable project root: `OrgResource/backend/`.

- Java 8, Spring Boot 2.1.0, Jersey/JAX-RS `javax`, Spring Data JPA/JPQL, Javax Validation, MySQL Connector/J 8.0.13.
- Exactly five JPA entities: Department, Project, Resource, ResourceRequirement and Allocation.
- Five Spring Data repositories, five components and five Jersey controller families; 32 Java source files compile.
- Generated IDs, explicit relational constraints, MySQL InnoDB foreign keys, unique normalized Department/Resource names, and unique Project/Resource requirement pairs.
- Create/list routes for all five entities, resource availability, and guarded Department deletion. Allocation JSON omits `phoneNumber`.
- Local settings bind HTTP to `127.0.0.1:8080`; the dedicated database binds to `127.0.0.1:3308`. Credentials are DPAPI-protected outside the project under `%LOCALAPPDATA%\OrgResourceDev` and are not in the source archive.
- `spring.jpa.hibernate.ddl-auto=update` is limited to this local skeleton. Use reviewed migrations before broader development or release.

## Verification evidence

### Build and runtime

- JDK: Eclipse Temurin 8u512. Maven wrapper: 3.3.4. Maven distribution: 3.10.0.
- Command: `mvnw.cmd -B -ntp clean package -DskipTests` with `JAVA_HOME` explicitly set to the portable JDK 8 installation.
- Result: `BUILD SUCCESS`; 32 source files compiled; executable JAR created at `backend/target/orgresource-0.1.0-SNAPSHOT.jar`.
- Runtime: Spring Boot 2.1.0, embedded Tomcat 9.0.12, Hibernate 5.3.7.Final, MySQL Server 8.0.46. Startup log reports five repositories, successful Hikari connection, initialized JPA entity manager, Jersey servlet at `/api/*`, and Tomcat on port 8080.
- Unit-test execution was skipped (`-DskipTests`); there are no source test files in this O1 slice. The separate Postman/Newman HTTP assertions below were executed.

### Database schema

`schema-evidence.txt` records the live schema inspection after the final Newman run:

- Exactly five base tables, all InnoDB: allocation, department, project, resource, resource_requirement.
- Four foreign keys: Allocation→ResourceRequirement; Project→Department; ResourceRequirement→Project; ResourceRequirement→Resource.
- Unique indexes: Department `name_key`; Resource `name_key`; ResourceRequirement `(project_id, resource_id)`.
- Local test data after the run: 4 allocations, 5 departments, 4 projects, 4 resources and 4 requirements. These are local development/demo fixtures, not organization records or evidence of user demand.

### HTTP/Postman

`../postman/OrgResource-O1-Skeleton.postman_collection.json` was run using Newman 6.2.3 against the rebuilt app. `newman-results-final.json` is the raw machine-readable run report.

- 15 requests, 15 test scripts, 29 assertions; 0 failed.
- Covered all five create/list families, filtered reads, resource availability, generated IDs and parent IDs, coverage query, redaction of allocation phone number, duplicate rejection (JSON 409), invalid quantity (JSON 400), and safe deletion (200).
- A direct HTTP verification also confirmed deletion of a referenced Department returns JSON 409 `DEPENDENCY_EXISTS`.

### Restart persistence

The app was stopped after the clean package, restarted from the generated JAR against the same local database, then queried through all five list endpoints. Each returned HTTP 200 and retained rows (Department 5; each other entity 4). `restart-persistence-http.json` records those IDs, verifies the allocation response has no `phoneNumber`, and records the referenced-department delete response.

### IDE and source archive

- `.project`, `.classpath`, `.settings`, Maven files and the Maven wrapper are included for Eclipse/m2e import and JavaSE-1.8 configuration.
- Eclipse was not found on PATH, so a real Eclipse import/build was not run. The Maven wrapper build is the verified build path.
- The source ZIP was extracted into a new temporary directory and rebuilt independently with the bundled Maven wrapper and JDK 8: `BUILD SUCCESS`, 32 source files compiled, executable JAR created. `zip-clean-extract-build.txt` is the captured build log. Maven emitted nonfatal metadata warnings for old third-party repositories; the build completed successfully using the available dependencies.
- Final source archive: `../deliverables/OrgResource-Java-Skeleton-2026-11-13.zip` (43 files; 33,827 bytes; SHA-256 `A525D0A33CBA501782AD711A9E201E2C1826D64FC68D7CB8C8FD58A447EE0846`). It includes Eclipse metadata, Maven wrapper and source; it excludes build output and credentials. `source-zip-file-list.txt` records the archive entries.
- This course folder is not a Git repository and has no configured remote. No push, publication, hosted deployment or submission was made.

## Open items and next phase

1. Resolve the syllabus CLO9 image requirement against the assignment/instructor/Canvas before locking final acceptance scope.
2. Continue with the next bounded implementation phase only after reviewing this O1 boundary; do not infer that O1 satisfies the full proposal or rubric.
3. Develop the remaining routes and lifecycle with explicit validation, transaction/locking rules, audit/history behavior, tests and migrations, then add the Retrofit/Gson client and scheduling only where supported by the course materials and clarified requirements.
4. Add a real video only if it is requested and a suitable recorder is available; the present deliverable is explicitly marked `VIDEO_NOT_CREATED`.

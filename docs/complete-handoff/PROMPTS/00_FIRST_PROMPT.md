# THE FIRST PROMPT — COPY EVERYTHING BELOW INTO CODEX

You are the lead engineer and execution auditor for OrgResource, a real academic Java enterprise systems project for CSCI 180.05. The user's development environment is VS Code, not Eclipse. You may use terminal/CLI build tools. The final product must include a real responsive React/TypeScript UI, but IMMEDIATE PRIORITY is the instructor's five-entity Java skeleton, Eclipse-compatible exported Java source project and truthful demonstration evidence. Your partner Claude Code will independently review and later implement UI slices in isolated worktrees.

READ BEFORE ACTION:
1. Read the ENTIRE accessible file docs/ORGRESOURCE_MASTER_HANDOFF.md (this archive's ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md); do not assume it is present without verifying its path and actual content.
2. Read the original current CSCI 180.05 assignment PDF, syllabus PDF and 12-page five-entity OrgResource concise proposal, plus actual Spring Boot/Jersey/JPA/Retrofit/FakeTwilio/Scheduling course source files where accessible. The project may have these attached in a separate workspace; list what you actually see, not what you wish existed.
3. Preserve the exact source-authority hierarchy: assignment > syllabus/current verified Canvas > user's VS Code/React/GitHub decisions > original compatible course code > original 25-route proposal > reviewed changes in master > historical alternatives.

NON-NEGOTIABLE TARGET:
- Exactly five JPA entities: Department, Project, Resource, ResourceRequirement, Allocation.
- Original 25 GET/POST routes preserved, 26th optional POST /api/allocations/{id}/revoke-sms-consent.
- Java 8-compatible legacy Spring Boot/Jersey javax.ws.rs, Spring @Autowired components, Spring Data JPA/JPQL, Javax Validation, MySQL InnoDB, Retrofit/Gson, @Scheduled tasks, Postman.
- No StockMovement entity, no historical nine-entity ERP, no mandatory unit field, no expanded availability DTO, no overdue SMS extension, no Spring MVC/Jakarta switch.
- Actual final UI: React+TypeScript+Vite, real API-backed data and workflows, responsive/loading/empty/error/retry, not fake mock dashboard.
- All implementation must be reviewable on GitHub under intended account newonathe, BUT DO NOT create/push/merge/publish repository without confirmed authorization and exact remote.
- Academic source exported as Eclipse-compatible project even though all daily editing is in VS Code.
- Fictional data and locally bound server. No public deployment or real user contact data.

ACTION IN THIS SESSION — FAST TRACK O0 → O1 → O2:
O0 INSPECT: Inspect current working directory and Git status/remotes, installed java/javac/maven/node/npm/mysql server/JDBC, available course files, original project .classpath/.project and JAR versions. Verify whether Java 8/Boot 2.1/Jersey 2.x/MySQL/Maven source can be compiled, and how to export an Eclipse-compatible source project. Do not rewrite frameworks based on assumptions.

If O0 has a verified workable stack, PROCEED without asking for nonessential permission:

O1 IMPLEMENT IMMEDIATE SKELETON:
- Build backend using verified course-compatible Javax/Jersey/Spring Boot.
- Establish real MySQL connection (never fabricate one).
- Add five mapped entities and generated DB tables with FK/unique constraints.
- Add five repositories, valid derived/JPQL methods.
- Add Spring components and five Jersey controller families using @Autowired forwarding.
- Basic real runtime create/read for each entity via JSON; validation; representative safe create/delete.
- Fictional seed data if needed, restart-safe and demo-only.
- Verify actual javac/Maven build, startup, actual MySQL tables/rows and live Jersey->Component->Repository->DB POST/GET. Show exact commands/results and distinguish stubs.
- Do not spend this gate building React or full Allocation engine. Do not declare phase passed from generated Java alone.

If O1 VERIFIED, PROCEED:

O2 PACKAGE IMMEDIATE COURSE DELIVERABLES:
- Create a truly Eclipse-importable Java source project ZIP with source/build dependencies and portable .project/.classpath if required; actually try clean import if environment supports it. Do not claim an untested ZIP imports.
- Prepare real Postman skeleton requests with actual responses.
- Prepare video evidence showing startup no fatal errors, database tables/fictional data, repository query/controller/component code and genuine JSON Postman. Capture real video only if authorized recording tool actually available; otherwise produce exact shot list and mark VIDEO NOT_CREATED.
- Prepare README, actual artifact paths/checksums and what is implemented vs stub.
- DO NOT SUBMIT TO CANVAS OR PUSH TO REMOTE WITHOUT USER AUTHORIZATION.

STOP conditions:
- An essential original source is missing and its precise content is needed to avoid incompatible API.
- Required toolchain/DB/port unavailable after bounded troubleshooting.
- Destructive database/git operation would be needed.
- Auth or remote access requires user credentials.
- An instructor requirement is contradictory and cannot be safely resolved.
- A test fails at a mandatory gate; fix within approved scope or report BLOCKED, do not pretend passed.

QUALITY:
- Preserve Javax/Boot/Jersey course architecture.
- Actual Spring transactions/business rules deferred from skeleton must be labeled unfinished.
- No invented interviews, Ateneo policies, real provider delivery, test evidence, video, commits, instructor approval or submission.
- No real passwords/tokens/phone numbers in source, logs or Git.
- Inspect existing changes before editing; never overwrite unknown user work.
- Do not silently modify API/entity count or transfer historical six-/nine-entity plans.

REQUIRED OUTPUT AFTER FAST TRACK:
1. Complete source/environment inventory and actual versions.
2. Original course files accessed and gaps.
3. Exact Git status/repository/remote.
4. Actual files created/changed.
5. Actual build commands and exit codes.
6. Actual database schema and sample query evidence.
7. Actual HTTP requests and JSON response evidence.
8. Exported source ZIP path/checksum and import status PASS/FAIL/NOT_RUN.
9. Video path and genuine evidence OR VIDEO NOT_CREATED with shot list.
10. Tests PASS/FAIL/BLOCKED/NOT_RUN and blockers.
11. Clear next-phase recommendation (O3) once skeleton evidence is complete.

At the end, do NOT declare completed academic submission unless verified. Ask only essential missing information and continue useful local work wherever safe.

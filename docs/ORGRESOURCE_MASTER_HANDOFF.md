# ORGRESOURCE — COMPLETE SINGLE-FILE MASTER HANDOFF

**PLANNING SPECIFICATION / AGENT PROMPTS / SETUP / API / UI / TESTS / EXPORT**

**Date:** 2026-11-13 (Asia/Manila)  
**Status:** PLANNING ONLY — Java source, DB tests, Postman runs, real UI, videos, Eclipse exports and Canvas submissions are NOT verified in this archive.

This file consolidates every companion chapter, setup guide, prompt, template and Postman/acceptance matrix contained in this ZIP into one readable file. An exact machine-readable copy of the JSON/CSV files is separately included for agent use; their contents are reproduced near the end here.

**User decisions:** VS Code exclusively for editing, Codex + Claude Code as coding agents, planned GitHub newonathe, complete functioning React UI, instructor-compatible Eclipse Java source export, urgent skeleton ZIP and genuine demonstration video.

**Course authorities:** 09_ESP_PROJECT_ASSIGNMENT.pdf and 08_CSCI180_SYLLABUS.pdf, with live Canvas updates unverified. Original 10_ORGRESOURCE_CONCISE_PROPOSAL.docx defines five JPA entities and 25 operations; the 26th consent-revocation route is a reviewed amendment. This archive cannot replace the actual primary source bytes; mount them to Codex when available.

**No fabricated evidence:** The enclosed route templates and Java/TypeScript snippets have not been run as an OrgResource application. Nothing in this file is a school submission, runtime screenshot, provider acceptance or successful Eclipse import.

## Navigation

The sections below reproduce files in logical reading order. Start with setup, skeleton, domain, API, transactions, UI, tests, GitHub/release, source audit, then the copy-ready first prompt and all phases. Use the individual files when feeding agents smaller portions. Read the entire master when taking architectural decisions.

---


---

# MASTER SECTION 1: 00_START_HERE.md

# OrgResource — COMPLETE handoff archive

Version: PLAN-2026-11-13. This is a planning and implementation-instruction ZIP, not the completed Java app or submitted academic work.

USER DECISIONS: Develop in VS Code (not Eclipse). Have Codex and Claude Code implement and independently review. Store reviewed implementation under GitHub account newonathe. Build a genuine responsive React/TypeScript UI. Deliver a Java source archive importable to an Eclipse-based course environment.

SOURCE OF TRUTH: 09_ESP_PROJECT_ASSIGNMENT.pdf -> 08_CSCI180_SYLLABUS.pdf/verified Canvas -> user decisions -> inspected course examples and dependencies -> original five-entity proposal -> this reviewed handoff. Earlier six-/nine-entity drafts are history only.

ARCHIVE CONTENT: 'ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md' concatenates ALL detailed chapters below into ONE file. Supporting chapters, setup instructions, first-paste prompt, staged implementation prompts, review scripts, JSON Postman route template, acceptance matrix and manifest are included separately for targeted agent loading.

HOW TO BEGIN:
1. Read '01_SETUP_VSCODE_GITHUB.md' and '02_IMMEDIATE_SKELETON_VIDEO.md'.
2. Extract the folder and place 'ORGRESOURCE_COMPLETE_MASTER_HANDOFF.md' into your future repository at 'docs/ORGRESOURCE_MASTER_HANDOFF.md'. Ensure Codex can open this actual file.
3. Give Codex original assignment, syllabus, concise proposal, and actual course code; it must inspect and report inaccessible sources, not infer them.
4. Paste the entire 'PROMPTS/00_FIRST_PROMPT.md' into Codex. Let it inspect environment and implement the urgent skeleton (O0–O2 sequential gates) without falsely claiming tests passed.
5. Use Claude Code's review prompt independently and only in a separate worktree if writing.
6. Proceed O3–O14 toward real backend, React UI, transactional tests, Retrofit, scheduler, and final export.
7. Check live Canvas dates and requirements. Do not let agents submit, push, merge or expose secrets without your explicit authorization.

MAJOR BOUNDARIES: Five entities (Department, Project, Resource, ResourceRequirement, Allocation). Original 25 routes + proposed one consent withdrawal = 26. Whole-batch resource workflow, conservative global reservations. JAX-RS/Jersey/Javax/Spring Data JPA/MySQL + Retrofit/Gson + Scheduling. Real UI is mandatory by user decision but must not delay immediate backend skeleton. Optional image receive/serve feature only if final assignment/syllabus interpretation requires; can add two routes without sixth entity.

EVIDENCE LIMIT: planning math/code examples are not successful Java/Postman/MySQL runs. Current GitHub repo/Canvas status unknown. Complete original ZIP byte review not established. Video transcripts are AUDIO ONLY.


---

# MASTER SECTION 2: 01_SETUP_VSCODE_GITHUB.md

# 01 — Setup from zero: Windows VS Code, Java, Maven, MySQL, Node, GitHub, Codex, Claude Code

These are instructions to run on your local machine; they were NOT executed here. Use the exact OS shell. For macOS/Linux translate PowerShell and wrapper commands. Inspect existing files before creating or replacing anything.

## A. Inspect workspace and machine first

Open VS Code terminal (PowerShell on Windows). Choose where OrgResource will be developed, e.g. C:\Development\OrgResource, but use actual user-preferred path. Do not overwrite any existing repository.

~~~powershell
Get-Location
Get-ChildItem -Force
git status --short --branch
git remote -v
git --version
java -version
javac -version
mvn -version
node --version
npm --version
mysql --version
code --version
gh --version
where.exe java
where.exe javac
where.exe mvn
where.exe node
~~~

Missing commands are environment findings, not proof work was done. MySQL client version is not server version. Do not print tokens.

## B. Install and configure as needed

1. VS Code: https://code.visualstudio.com/ ; Java Extension Pack, Maven for Java, Java test/debug support, GitHub PRs (optional), ESLint (for React) and official Codex/Claude integrations if authorized.
2. Java: inspected course-era libs are Java 8/Jersey 2/Boot 2.1 based. Install a JDK 8 toolchain for backend compilation and runtime after checking actual lab examples. VS Code Java language server may run on newer JDK; configure project JDK separately via Java runtime settings. Do not confuse IDE JDK with backend JDK.
3. Maven: https://maven.apache.org/install.html. A Maven wrapper is recommended after actual project generation. It does not exist until created; do not claim '.\mvnw.cmd' already works.
4. MySQL server and SQL client: https://dev.mysql.com/downloads/ ; verify server with SELECT VERSION(); check InnoDB, foreign keys, unique indexes and datasource privileges.
5. Git: https://git-scm.com/downloads . GitHub CLI optional https://cli.github.com/. Verify account newonathe before any repo creation, remote, push or pull request. Prefer private visibility until user approves publication.
6. Node/npm: https://nodejs.org . Choose actual Vite-supported Node LTS; official Vite docs list Node 20.19+ or 22.12+ as base requirement, with some templates requiring more. Modern Node is separate from Java 8 backend.
7. Codex/Claude: follow current authenticated vendor instructions, give access only to chosen repo/source files, keep secrets outside prompts. Never let agents write same worktree at once.

## C. Workspace creation

If repository already exists remotely, use authenticated git clone/gh repo clone on its verified URL. If folder already has work, inspect before copying files. If NO repo and user authorizes creation:
~~~powershell
New-Item -ItemType Directory -Force -Path .\OrgResource
Set-Location .\OrgResource
git init -b main
git status --short --branch
code .
~~~
The above is an example, not a command to run without first checking path. Do NOT silently create a public repo.

Unzip handoff to 'docs/handoff/', copy its complete master to 'docs/ORGRESOURCE_MASTER_HANDOFF.md', use AGENTS.md and CLAUDE.md only after checking existing instructions. Do not automatically commit private original course ZIPs/slides to a public repo.

## D. Database first

Use dedicated fictional-data schema. After connecting to actual MySQL server:
~~~sql
SELECT VERSION() AS mysql_server_version;
SHOW VARIABLES LIKE 'default_storage_engine';
CREATE DATABASE IF NOT EXISTS orgresource
 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
~~~
Create dedicated local app user with least permissions using your DB admin, credentials only in local environment. The exact SQL user creation depends on version/security preferences. No real password in Git or chat.

Suggested app environment variables:
- ORG_DB_URL
- ORG_DB_USER
- ORG_DB_PASSWORD
- ORG_SMS_MODE (DISABLED by default)
- ORG_FAKE_TWILIO_BASE_URL
- ORG_FAKE_TWILIO_SERVICE_ID
- ORG_FAKE_TWILIO_DUMMY_AUTH
- ORG_REMINDER_SCAN_MS, ORG_EXPIRY_SCAN_MS

Example terminal env values (placeholders must be replaced LOCALLY; never publish):
~~~powershell
$env:ORG_DB_URL="jdbc:mysql://127.0.0.1:3306/orgresource?serverTimezone=Asia/Manila"
$env:ORG_DB_USER="<LOCAL_DB_USER>"
$env:ORG_DB_PASSWORD="<LOCAL_SECRET>"
$env:ORG_SMS_MODE="DISABLED"
~~~

Application properties template:
~~~properties
server.address=127.0.0.1
server.port=8080
spring.datasource.url=${ORG_DB_URL}
spring.datasource.username=${ORG_DB_USER}
spring.datasource.password=${ORG_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
orgresource.sms.mode=DISABLED
orgresource.reminder.scan-ms=3600000
orgresource.expiry.scan-ms=3600000
~~~
The 'update' schema mode is academic convenience only; do not use it as migration system. Do not switch to destructive drop/create in ordinary demo config. Prove LocalDateTime round trips and actual JDBC driver.

## E. Backend build

Recommended backend/ with pom.xml, Maven Wrapper, src/main/java/app and src/main/resources. Original course library inventory includes Boot 2.1.0.RELEASE, Jersey 2.26, Hibernate 5.3.7, Spring Data JPA 2.1.2, MySQL Connector/J 8.0.13; select a compatible dependency graph only after inspecting original code. Java 8 source/target, javax.* imports, Jersey not Spring MVC.

Once project generated:
~~~powershell
Set-Location backend
.\mvnw.cmd -version
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
~~~
Use 'mvn' if the wrapper was not generated and local Maven is verified. On macOS/Linux use './mvnw'. Runtime target API is http://127.0.0.1:8080/api/departments. Wrong '/api/api' mapping is a common error: ResourceConfig @ApplicationPath("/api"), controllers @Path("/departments").

## F. React frontend after skeleton

Frontend should be Vite + React + TypeScript with pinned package-lock.json. Scaffold when O7 begins; do not claim it exists beforehand. Use 'npm ci' only after lockfile creation.
~~~powershell
Set-Location frontend
npm ci
npm run dev
npm run build
npm run test
~~~
Vite dev target http://127.0.0.1:5173, proxy '/api' -> Java server 127.0.0.1:8080. Never use frontend localStorage as inventory authority. Final static assets bundled into backend only after verifying packaged / and /api routes. Use React HashRouter for simple Java servlet coexistence.

## G. Multi-agent workflow

Codex primary backend, Claude independent reviewer/React track, but one writer per worktree. Use separate git branches/worktrees for simultaneous edits. Review Git diff and tests before merge. Agents may make useful local edits, run commands and prepare export/video if tools support them. They must not invent video, Canvas receipt, test result, repository or submission. Ask user permission for push/merge/submission/destructive actions. Do not claim continuous GitHub monitoring without actually creating a monitoring workflow.

## H. Final setup checkboxes

[ ] Git, author, branch, remote and visibility verified.
[ ] Original assignment/syllabus/proposal accessible to agents.
[ ] Correct course JDK, Maven and dependencies identified.
[ ] Actual MySQL server, InnoDB and dedicated schema work.
[ ] Jersey/Spring/Repository startup proven with real POST/GET.
[ ] Five principal JPA tables and FKs observed.
[ ] npm and React setup reserved for after skeleton gate.
[ ] No credentials in project or logs.
[ ] Eclipse-compatible source archive strategy explicitly verified.


---

# MASTER SECTION 3: 02_IMMEDIATE_SKELETON_VIDEO.md

# 02 — IMMEDIATE SUBMISSION: imported Eclipse Java project + genuine video

COURSE SOURCE: 09_ESP_PROJECT_ASSIGNMENT.pdf states project skeleton due Nov 13, Canvas exported project and recording due Nov 14, 2026; check live Canvas for updates. User specifically wants recorded startup, created tables/test data, and repository/controller overview. Assignment additionally wants URLs returning JSON in Postman. This is immediate P0 work, distinct from final React UI due Dec 5.

## What must be real

1. Java backend starts with no fatal errors.
2. MySQL connection is real.
3. Five entity tables generated from JPA: department, project, resource, resource_requirement, allocation (names subject to actual @Table mapping).
4. Actual foreign keys and unique constraints inspected.
5. Test data genuinely loaded, if enabled; no invented rows.
6. Spring Data repositories initialize; custom query names/JPQL don't prevent application launch.
7. Five Jersey controllers and Spring @Autowired components are reachable.
8. GET/POST return actual appropriate JSON; skeleton stubs may exist but must be labeled.
9. Exported Java project contains source/build/dependencies and has an Eclipse-compatible import route.
10. Video depicts actual terminal, SQL client, source and Postman; no fabricated success.
11. Actual submission must be done by user or explicitly authorized tools. No claim of Canvas receipt without proof.

## Video recording storyboard (suggested 5–7 min, not instructor mandate)

00:00 Show VS Code folder tree, purpose of OrgResource and Java backend.
00:25 Start backend from terminal and show successful startup. Show port and data source initialized; no fatal exception.
01:10 SQL: SELECT VERSION(), SHOW TABLES, SHOW CREATE TABLE for five entities and FK structure.
02:25 SELECT sample rows if present. Explain fictional organization and data scope.
03:10 Show repository source including one custom derived query and JPQL method; show actual result of a route/test invoking it.
04:05 Show Jersey @Path / @GET / @POST controller, @Autowired component delegation, and repository call. Explain service separation.
05:00 Postman POST /api/departments with fictional record, then GET /api/departments, prove matching generated ID in DB. Optionally demonstrate safe delete.
06:05 Show ZIP content/source and import procedure, distinguish verified Maven build from actual Eclipse import. State unfinished final lifecycle/UI honestly.
Do not record secrets, actual phone numbers, notifications, unrelated private browser tabs or student IDs. Show source from same tested Git commit.

## SQL inspection script

~~~sql
SELECT VERSION() AS mysql_server_version;
USE orgresource;
SHOW TABLES;
SHOW CREATE TABLE department;
SHOW CREATE TABLE project;
SHOW CREATE TABLE resource;
SHOW CREATE TABLE resource_requirement;
SHOW CREATE TABLE allocation;
SELECT COUNT(*) FROM department;
SELECT COUNT(*) FROM project;
SELECT COUNT(*) FROM resource;
SELECT COUNT(*) FROM resource_requirement;
SELECT COUNT(*) FROM allocation;
SELECT * FROM department ORDER BY id;
SELECT * FROM project ORDER BY id;
SELECT * FROM resource ORDER BY id;
SELECT * FROM resource_requirement ORDER BY id;
SELECT id, requirement_id, quantity, status FROM allocation ORDER BY id;
~~~
These are candidate names; inspect actual schema and update script rather than presenting failing SQL as proof.

## HTTP proof

POST http://127.0.0.1:8080/api/departments
Content-Type application/json
Body:
~~~json
{"name":"Fictional Logistics Demo"}
~~~
Expected status 201 only when implemented, and actual generated ID. Then GET /api/departments should include matching database row. If controller returns a placeholder, label it as skeleton stub; do not claim persistence.

## Eclipse-compatible source export from VS Code

The Java source archive MUST contain all Java .java source, pom.xml (or tested portable course JAR setup), necessary project config, dependencies resolution and README. If course demands literal Eclipse .project/.classpath/.settings, generate correct portable metadata and test import if feasible; otherwise state NOT_RUN. Maven+m2e import is one route but not proof of literal instructor interpretation. Exclude real credentials, local absolute paths, compiled caches, node_modules and unrelated source archive ZIPs. Build from clean extraction. Record ZIP SHA-256 and tested commit.

## Mandatory skeleton acceptance evidence

S01 source exists; S02 Java compile; S03 startup; S04 DB connection; S05 five tables; S06 fictional test data if any; S07 repository query initializes and runs; S08 controller paths accessible; S09 injected Component called; S10 JSON returned from real HTTP; S11 runtime create/delete; S12 restart persists; S13 exported source ZIP produced; S14 actual Eclipse import PASS or explicitly NOT_RUN; S15 actual video produced or NOT_CREATED; S16 no exposed secrets; S17 correct current Canvas instruction manually checked.

## Adaptable narration

"This is OrgResource, a proposed academic resource-planning application for student organizations. The project is developed in VS Code and its Java source is prepared for Eclipse-compatible import. [Show successful startup.] The application connects to MySQL and defines Department, Project, Resource, ResourceRequirement and Allocation tables. [Show real tables and fictional data.] Spring Data JPA repositories provide persistence, while Jersey controllers forward HTTP requests to injected Spring components. [Show real code and Postman response.] This is the skeleton milestone. Its unfinished business logic and React interface will be implemented and verified in subsequent phases. [Show export artifact.] Thank you."

If evidence differs from narration, revise narration. Neither Codex nor Claude may fabricate a video from screenshots of expected output. If recording capability missing, provide exact shot list and mark VIDEO NOT_CREATED.


---

# MASTER SECTION 4: 03_AUTHORITY_SCOPE_ARCHITECTURE.md

# 03 — Authority, source map, scope, architecture and compatibility

## Primary evidence

COURSE REQUIREMENT: 09_ESP_PROJECT_ASSIGNMENT.pdf governs course project requirements and printed due dates. SYLLABUS FACT: 08_CSCI180_SYLLABUS.pdf governs course context, CL learning outcomes, rubric, Canvas and Certificate of Authorship. Current Canvas announcements might change dates/policies and have not been verified. USER DECISION: VS Code, Codex/Claude, GitHub newonathe, real React UI, local source-export compatibility.

Original proposal: 10_ORGRESOURCE_CONCISE_PROPOSAL.docx (12 pages) with five entities, 25 original HTTP operations, whole-batch reusable/consumable workflows and scheduled reminder. Prior nine-entity and six-entity designs are exploratory history, not binding.

Source corpus: 03_COURSE_REFERENCE_CORPUS.txt searchable extracted code; 04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md source map; 01_KICKOFF_PROMPT.md, 02_MASTER_CONTEXT_DOSSIER.md; 06_VIDEO_TRANSCRIPTS.md audio-only, not video frames; COMPLETE_SOURCE_NONVIDEO_PART1/2.zip original file archives; course labs/slides and installed JAR inventories. Earlier manifest reports 2,611 paths, 852 distinct recorded hashes; full independent ZIP-entry reread and rehash NOT established. Review actual originals before asserting a particular classpath or build succeeds.

Representative original course source:
- Spring Boot 2.1.0, Jersey 2.26, Spring Data JPA 2.1.2, Hibernate 5.3.7, javax.persistence 2.2, MySQL Connector/J 8.0.13, Java 8 compiler, Jersey validator/Java-time support in dependency bundle.
- Separate Retrofit example uses interface with @FormUrlEncoded, fields To/MessagingServiceSid/Body and @Header("Authorization"), Gson converter initially commented out in testing example; FakeTwilio controller receives POST /fake_twilio/send; its Authorization is printed (never send real provider credentials).
- Scheduling example @EnableScheduling/@Scheduled but disables DB auto-config intentionally for scheduling-only teaching example; DO NOT copy its DB exclusions into OrgResource.
- Course JPA sample uses @Component controller with @Autowired component and Spring Data repositories.

## Problem definition (assumption, not validated survey)

One hypothetical student organization maintains shared equipment and consumable supplies. Different internal Departments plan Projects and request quantities. An officer records owned/remaining Resource stock, project Requirements, allocations, issuance, whole-batch usable returns and consumable use. Application computes availability/shortages and can send return reminders in controlled test environment.

No empirical interviews, Ateneo campus adoption, actual borrowing records, centralized university inventory workflow, CFMO integration, measured savings, budget, school endorsement or pilot. Academic demonstration with fictional data. Does not replace existing institutional systems.

## Personas and user stories

- Trusted local organizational officer: can create Departments/Projects/Resources/Requirements; approve requests; record issued/returned/consumed batches; correct quantities with stale-update guard; see commitments and shortages; correct data, safely delete eligible records.
- Borrower: represented by a descriptive label and optional fictional SMS destination, not a self-service account. No login or permissions.
- Instructor/reviewer: imports Java source, inspects five entity tables and wiring, uses Postman, watches real startup/DB/code UI demo and confirms compatibility.
- Coding-agent reviewer: checks actual source/diff and reproducible evidence; never invents tests or merges without permission.

Core user journeys: create resource pool -> create future Project -> add Requirements -> submit PENDING Allocation -> APPROVE within capacity -> ISSUE reusable/CONSUME consumable during active period -> RETURN entire usable batch even if overdue -> update stock guard -> read coverage/shortage -> scheduler expiry/reminders -> restart/persistence.

## Alternatives and decision

- Spreadsheet: low adoption cost but manual concurrent integrity.
- Existing organizational inventory tool: possible and not evaluated directly; no claim unavailable.
- Nine-entity ERP and 68 routes: too large for current deadline.
- Six entities with StockMovement event log: improved stock audit but added entity/migrations/routes/test complexity.
- Five-entity conservative inventory service: selected small complete course-aligned prototype. No authenticated roles, no partial damaged/lost stock, no procurement, supplier integration, payment, locations, event ledger, cloud hosting or production readiness.
- UI is a USER DECISION even though course says bonus optional. Build real React pages once backend skeleton is evidenced.

## Quality requirements

Correct database constraints and transactions, no stock overbooking, no double consumption, predictable 400/404/409/500 errors, Javax Validation, full POST/GET route coverage, one truth for availability, restart-safe DB, reproducible Eclipse-compatible export, no secrets/public personal data, typed frontend API, responsive/accessible UI, observable scheduled outbound HTTP. No invented throughput number/SLAs.

## Architecture

Browser React/TypeScript/Vite -> /api JSON -> Jersey controller (@Path, @GET/@POST, @Autowired) -> Spring component (@Transactional) -> Spring Data JpaRepository/JPQL -> MySQL InnoDB. Two scheduled components: expiry cleanup and due-return reminders. Retrofit/Gson Java gateway sends to local FakeTwilio or authorized Twilio Virtual provider, disabled by default. No second backend or separate DB.

Backend may be Maven-built Java 8 with legacy Spring Boot 2.1.0.RELEASE and javax.ws.rs; choose resolved transitive versions only after checking original course code. If Maven impossible or literally incompatible with expected export, use a portable classpath with original JARs and document alternative. Do not migrate to Spring Boot 3/Jakarta.
Frontend modern Node/Vite toolchain independent of Java8; pin lockfile. Use Vite dev proxy /api -> 127.0.0.1:8080, HashRouter. Final Java serves static UI at root; Jersey exclusively /api. Register endpoints individually because packaged Jersey class scanning unreliable. Test actual JAR for / and /api, not just Vite dev server.

## Time and scope amendment

All dates API YYYY-MM-DDTHH:mm:ss Asia/Manila wall time, active Project interval startAt <= now < endAt. Early approval holds globally regardless of future start; unissued APPROVED hold expires at Project end. Issued physical custody remains until genuine recorded RETURN. Original proposal reserved indefinitely: expiration amendment must be disclosed. Original Resource update had only name/total; amended to expectedTotalQuantity precondition. Original response included phoneNumber; amended to public redacted contactPresent and reminder outcome. Original 25 paths preserved, plus proposed A09 consent revocation.

## Conditional images

Syllabus CLO9 image handling may be expected; assignment does not prescribe exact image routes. Treat as OPEN QUESTION. If mandatory, extend Resource with imageData/imageMimeType and POST/GET /api/resources/{id}/image, enforce content type/signature/size, actual SQL bytes and browser display, without sixth entity. Route count then 28. Do not delay imminent skeleton unless course explicitly requires it.

## Official references

https://docs.spring.io/spring-boot/docs/2.1.0.RELEASE/reference/htmlsingle/
https://code.visualstudio.com/docs/java/java-project
https://vite.dev/guide/
https://react.dev/
https://maven.apache.org/
https://dev.mysql.com/doc/
https://www.twilio.com/docs/messaging
https://docs.github.com/
https://schema.postman.com/json/collection/v2.1.0/docs/index.html


---

# MASTER SECTION 5: 04_DATA_MODEL_AND_INVARIANTS.md

# 04 — Exhaustive domain and relational data contract

## Schema: exactly five JPA entities

Department (1) -> many Project.
Project (1) -> many ResourceRequirement.
Resource (1) -> many ResourceRequirement.
ResourceRequirement (1) -> many Allocation.
A Project may need several Resource types; uniqueness is one Requirement for each (project_id,resource_id) pair. Allocation is a request/commitment toward one Requirement. Do not introduce StockMovement, User, Notification, AuditLog or other JPA entity for baseline.

Use explicit @Table physical names and @Column names for repeatable video/SQL evidence: department, project, resource, resource_requirement, allocation. Actual schema must be inspected, not assumed.

Entity: Department
- id Long generated positive PK NOT NULL, never client writable.
- name String 1–100, trim + normalize display.
- nameKey String server-computed normalized unique value, NOT NULL, indexed unique.
Meaning: a fictional organization department/team that owns Projects.
Creation uniqueness must be enforced by DB, not only a preflight SELECT. Update duplicate check excludes same record.

Entity: Project
- id Long generated PK.
- department @ManyToOne Department, required FK.
- name String 1–100 trimmed, NOT NULL.
- startAt LocalDateTime NOT NULL, future on create or when date changed; local Philippine time.
- endAt LocalDateTime NOT NULL, strictly after startAt.
Meaning: organizational activity interval. On update department is immutable. A name-only edit retains unchanged dates even if historical; changing either date requires a future new start and NO historically approved Allocation under Project. Delete only if no Requirements.

Entity: Resource
- id Long generated PK.
- name String 1–100 trimmed, unique normalized nameKey.
- nameKey String NOT NULL unique index.
- type ResourceType REUSABLE | CONSUMABLE, NOT NULL, immutable after create.
- totalQuantity Integer 0–1,000,000, NOT NULL.
Meaning: fungible whole units. REUSABLE total is usable units owned (including out on loan); CONSUMABLE total is current remaining stock. No mandatory unit column (the original 12-page proposal did not include one). Name must clarify measurement, e.g. "A4 paper sheets".
- availableQuantity derived in output, NOT stored.
- Delete only with totalQuantity=0 AND zero Requirement references (intentional stricter rule than original, which only checked Requirements).
- Resource updates must send expectedTotalQuantity from last read; after Resource lock, reject mismatch and refused lower-than-commitment total. This is optimistic expected-value protection, NOT full idempotency or movement audit.

Entity: ResourceRequirement
- id Long generated PK.
- project required @ManyToOne Project FK.
- resource required @ManyToOne Resource FK.
- quantity Integer 1–1,000,000.
- unique(project_id,resource_id) in DB.
- allocatedQuantity/shortageQuantity derived in JSON, NOT stored.
- Cannot reduce below MAX(current effective coverage, largest live PENDING Allocation quantity).
- Cannot update quantity after Project ended; can delete only if no Allocation records.
- Once a never-approved CANCELLED Allocation is deleted, it no longer constrains demand; historically returned/consumed count remains.

Entity: Allocation
- id Long PK.
- requirement required @ManyToOne ResourceRequirement.
- quantity Integer 1–1,000,000, <= Requirement quantity at create, IMMUTABLE thereafter.
- borrowerName String 1–100 trimmed, NOT NULL; a fictional label, no verified identity.
- phoneNumber String nullable; if non-null must pass E.164-like + plus 8–15 digits, first digit 1–9; require for smsConsent true. With consent false, do not retain a phone number. Use fictional controlled test destination, no actual person.
- smsConsent Boolean NOT NULL default false. The fact a JSON Boolean says true does NOT prove actual human consent; demo only.
- status AllocationStatus NOT NULL one of PENDING APPROVED ISSUED RETURNED CONSUMED CANCELLED, persisted as STRING.
- createdAt LocalDateTime NOT NULL, server set.
- approvedAt LocalDateTime nullable, immutable historical marker after first approval.
- issuedAt LocalDateTime nullable, set once.
- returnedAt LocalDateTime nullable, set once.
- consumedAt LocalDateTime nullable, set once.
- cancelledAt LocalDateTime nullable, set when closed/cancelled.
- cancellationReason Enum nullable: USER_REQUEST or EXPIRED.
- reminderClaimedAt LocalDateTime nullable, durable mark for one automatic SMS attempt.
- reminderOutcome Enum NOT NULL default NONE: NONE, CLAIMED, SUPPRESSED, SIMULATED, PROVIDER_ACCEPTED, REJECTED, UNKNOWN.
- reminderHttpStatus Integer nullable, actual received code.
- providerMessageSid String nullable, only when actual provider returns it; no invented SID.
Derived response:
- contactPresent Boolean = stored valid destination present.
- reminderAttempted Boolean = reminderClaimedAt != null.
- expiredPendingCleanup Boolean when status PENDING/APPROVED and now >= associated Project.endAt.
The public response must never return phoneNumber. Do not persist derived fields unnecessarily.

### Java enums

ResourceType = REUSABLE, CONSUMABLE.
AllocationStatus = PENDING, APPROVED, ISSUED, RETURNED, CONSUMED, CANCELLED.
CancellationReason = USER_REQUEST, EXPIRED.
ReminderOutcome = NONE, CLAIMED, SUPPRESSED, SIMULATED, PROVIDER_ACCEPTED, REJECTED, UNKNOWN.
Use EnumType.STRING, NOT ORDINAL.

### Name normalization

Input "  Logistics   " -> display "Logistics", nameKey "logistics".
Collapse internal multi-spaces consistently; lower-case with fixed Locale.ROOT and trim. Protect DB unique index and handle constraint violation races. Do not use case-insensitive Java check as a substitute for DB uniqueness. Verify collation and Unicode behavior; document chosen comparator.

### Date and time

Public Project strings format YYYY-MM-DDTHH:mm:ss interpreted as Asia/Manila local time. Use an injected java.time.Clock for time-dependent test determinism and ZoneId.of("Asia/Manila"). Do not call local Java clock separately in repeated query phases and inadvertently cross end boundary. Sample a common 'now' per business operation. A Project's active time is [startAt,endAt): inclusive start, exclusive end. Project creation requires future start; name-only update does not require moving existing dates into the future.

### Reservation and stock mathematics

Let T = Resource.totalQuantity.
Let H = sum quantity of Allocation where status APPROVED and Project.endAt > now, regardless of Project startAt.
Let I = sum quantity of ISSUED reusable Allocations, even after Project end.
Reusable: available = T - H - I.
Consumable: available = T - H (physical total deducted on consume).
Never clamp negative availability to zero. Negative is DB integrity failure. For an APPROVED consumable of quantity q, on CONSUME total drops T->T-q and H->H-q so available stays unchanged at that instant, not deducted twice.

For Requirement Q:
Covered = effective APPROVED + ISSUED + RETURNED + CONSUMED amounts.
Shortage = max(0, Q.quantity - Covered).
PENDING and CANCELLED count zero. Effective APPROVED stops counting when Project ends even if stored status remains APPROVED awaiting cleanup. RETURNED/CONSUMED remain historical fulfillment after end.
Invariant: 0 <= Covered <= Q.quantity, 0 <= Available <= T subject to reusable issued custody.
Important semantics: returned reusable equipment is available for another Project but the first Project's Requirement stays covered. Approved future Project reserves globally even before it begins; this is conservative capacity behavior and no double future booking based on hoped return.

### Allocation transitions

None -> create -> PENDING (no hold).
PENDING -> approve -> APPROVED (requires Project not ended, sufficient stock and uncovered demand; timestamp approvedAt).
PENDING -> cancel -> CANCELLED/USER_REQUEST.
PENDING -> expiry cleanup after Project end -> CANCELLED/EXPIRED.
PENDING -> delete allowed (never approved).
APPROVED reusable -> issue -> ISSUED during Project active interval; no second availability deduction; timestamp issuedAt.
ISSUED reusable -> return -> RETURNED at any time, including after end; officer explicitly declares whole batch returned usable; timestamp returnedAt; custody removed; coverage preserved.
APPROVED consumable -> consume -> CONSUMED during active interval; subtract Resource.totalQuantity exactly q once in same transaction; timestamp consumedAt.
APPROVED -> cancel -> CANCELLED/USER_REQUEST before expiry; after end mark reason EXPIRED (no second stock release); preserve approvedAt.
APPROVED expired -> expiry cleanup -> CANCELLED/EXPIRED, effective hold already zero.
CANCELLED never-approved -> delete allowed when approvedAt null.
CANCELLED historically approved -> never physically delete.
RETURNED, CONSUMED and ISSUED cannot cancel/delete through current API.
A09 withdraw consent changes no stock lifecycle status; clears phone and sets smsConsent=false; repeated revocation on existing record harmless.
All prohibited transitions return controlled 409 without change.

### Guarded Resource correction

Request: name,totalQuantity,expectedTotalQuantity. Lock Resource and check actual total==expected, else 409 STALE_RESOURCE_QUANTITY. Compute H/I at same time after lock. For reusable new T >= H+I; consumable new T >= H. Proposed total in 0..1e6. Preserve type. On lost HTTP response, do NOT blindly retry modifying POST, because commit might have succeeded. Re-fetch and reconcile.

### Guarded Requirement reduction

Require 1..1e6 and Project not ended. Lock Project then Resource, obtain accurate covered and MAX single live PENDING quantity. New Q >= max(Covered,maxPending). Pending sum not used since competing pending requests do not reserve demand. A historically CANCELLED/never-approved Allocation does not constrain Q after cancellation.

### Protected Project dates

If either date changes: new start future, new end>new start, no Allocation with nonnull approvedAt under any Requirement of Project. This preserves historical approved context even if previously approved Allocation now CANCELLED. Name-only update may preserve old date, subject to name validation. No department change.

### Deletion constraints

Department: no Projects.
Project: no Requirements.
Resource: totalQuantity==0 and no Requirements.
Requirement: no Allocation records.
Allocation: status PENDING or CANCELLED with approvedAt==null.
The database enforces foreign-key integrity; components return domain conflicts when known, and must handle DB integrity races without inappropriate 500 if recognized.

### Historical limitations and truthful UI descriptions

No partial return/damage, physical verification, stock movement ledger, serialized devices, procurement or multi-org ownership. A RETURN is officer-recorded usable whole-batch receipt. No real authentication or identity verification. A Resource total correction lacks event ledger; Git history is not data audit. Clearly disclose in About/Demo.

### Invariant IDs for tests

INV01 Resource total nonnegative.
INV02 Resource availability never negative.
INV03 Requirement coverage <= demand.
INV04 Allocation quantity immutable.
INV05 ISSUE once.
INV06 RETURN once.
INV07 CONSUME once.
INV08 RETURN only after valid ISSUE.
INV09 RETURN removes custody, not historical coverage.
INV10 CONSUME deducts physical total exactly once.
INV11 CANCELLED never reserves.
INV12 approvedAt remains historical.
INV13 expired unfulfilled approved hold zero.
INV14 overdue ISSUED custody remains unavailable.
INV15 stale total correction refuses overwrite.
INV16 business errors/rollback do not partially commit.
INV17 Project date edit doesn't alter historical approved periods.
INV18 FK references valid.
INV19 restart persistence.
INV20 scheduler one automatic claim per Allocation, not guaranteed handset delivery.


---

# MASTER SECTION 6: 05_API_26_ROUTES_AND_RESPONSES.md

# 05 — Complete HTTP/JSON contract: 25 original routes + 1 proposed additional route

All external URLs begin with http://127.0.0.1:8080/api (actual port configured at runtime). GET for retrieval, POST for submissions/updates/deletes per original course assignment and Word proposal. Components are Spring-managed, injected via @Autowired by Jersey @Component controllers; business rules not written in controllers. Every create returns 201; other successful calls 200; list none returns []; successful delete response {"message":"Record deleted."}; errors have 'code' (new optional additive field) and 'message' (original required field).

Entity response formats

Department:
~~~json
{"id":1,"name":"Logistics"}
~~~
Project (dates PHT local string):
~~~json
{"id":2,"departmentId":1,"name":"Welcome Workshop","startAt":"2026-11-20T09:00:00","endAt":"2026-11-20T12:00:00"}
~~~
Resource:
~~~json
{"id":3,"name":"Extension cords","type":"REUSABLE","totalQuantity":10,"availableQuantity":6}
~~~
Availability: MUST PRESERVE original TWO fields:
~~~json
{"resourceId":3,"availableQuantity":6}
~~~
Requirement:
~~~json
{"id":4,"projectId":2,"resourceId":3,"quantity":6,"allocatedQuantity":4,"shortageQuantity":2}
~~~
Allocation REDACTED revised DTO:
~~~json
{"id":5,"requirementId":4,"quantity":4,"borrowerName":"Fictional representative","contactPresent":false,"smsConsent":false,"status":"PENDING","createdAt":"2026-11-13T11:00:00","approvedAt":null,"issuedAt":null,"returnedAt":null,"consumedAt":null,"cancelledAt":null,"cancellationReason":null,"expiredPendingCleanup":false,"reminderAttempted":false,"reminderOutcome":"NONE"}
~~~
Never return full phoneNumber in GET list or response. 'contactPresent' boolean, 'reminderAttempted' derived from claim; 'expiredPendingCleanup' time-derived. Provider message SID should not be unnecessarily surfaced in public UI.

DepartmentController — 4 routes
D01 POST /api/departments. Input {"name":"Logistics"}. Component DepartmentComponent.create(name). Response 201 Department. 400 invalid name; 409 DUPLICATE_NAME normalized unique name.
D02 GET /api/departments. Input none. DepartmentComponent.list(). Response 200 array (or []). No anonymous list authentication in local prototype.
D03 POST /api/departments/{id}/update. Input {"name":"Operations"}. DepartmentComponent.update(id,name). Response 200 Department. 400 invalid; 404 target absent; 409 duplicate.
D04 POST /api/departments/{id}/delete. No body. DepartmentComponent.delete(id). Response 200 deletion message. 404 absent; 409 DEPENDENCY_EXISTS if Projects reference it.

ProjectController — 4 routes
P01 POST /api/projects. Input {"departmentId":1,"name":"Workshop","startAt":"2026-11-20T09:00:00","endAt":"2026-11-20T12:00:00"}. ProjectComponent.create(departmentId,name,startAt,endAt). Response 201 Project. 400 invalid name/date, start not future or end <= start; 404 Department absent.
P02 GET /api/projects?departmentId=1 (optional filter). ProjectComponent.list(departmentId). Response 200 array; a syntactically valid missing filter ID => []; invalid nonpositive filter => 400.
P03 POST /api/projects/{id}/update. Input {"name":"Workshop updated","startAt":"2026-11-20T09:00:00","endAt":"2026-11-20T12:00:00"}; Department cannot change. ProjectComponent.update(id,name,startAt,endAt). 200 Project; 404 absent; 400 invalid dates; 409 PROJECT_DATES_LOCKED on changed dates after any historical approval. Name-only edit with unchanged past date allowed.
P04 POST /api/projects/{id}/delete. No body. ProjectComponent.delete(id). Response 200 deletion message; 404 missing; 409 DEPENDENCY_EXISTS while Requirements exist.

ResourceController — 5 routes
R01 POST /api/resources. Input {"name":"Extension cords","type":"REUSABLE","totalQuantity":10}. ResourceComponent.create(name,type,totalQuantity). Response 201 Resource with initially equal available. 400 invalid type/quantity/name; 409 DUPLICATE_NAME. NO REQUIRED 'unit' property.
R02 GET /api/resources?type=REUSABLE (optional filter). ResourceComponent.list(type). Response 200 Resource[]; invalid enum => 400; no matches => [].
R03 POST /api/resources/{id}/update. Input {"name":"A4 paper sheets","totalQuantity":120,"expectedTotalQuantity":70}. ResourceComponent.update(id,name,totalQuantity,expectedTotalQuantity). Response 200 Resource; 404 missing; 400 invalid; 409 DUPLICATE_NAME, STALE_RESOURCE_QUANTITY, QUANTITY_BELOW_COMMITMENTS. Type unchanged. Expected field is intentional CHANGE from original proposal; update proposal and Postman accordingly. This expected-value guard is NOT a full revision counter; ABA reuse possible if same total recurs.
R04 POST /api/resources/{id}/delete. No body. ResourceComponent.delete(id). 200 deletion; 404 absent; 409 DEPENDENCY_EXISTS if Requirement reference or RESOURCE_NOT_EMPTY if totalQuantity>0.
R05 GET /api/resources/{id}/availability. ResourceComponent.getAvailability(id). Response exactly {"resourceId":id,"availableQuantity":n}; 404 missing; impossible negative state sanitized 500 INTERNAL_ERROR. No expanded approvedQuantity/issuedQuantity fields.

RequirementController — 4 routes
Q01 POST /api/requirements. Input {"projectId":2,"resourceId":3,"quantity":6}. RequirementComponent.create(projectId,resourceId,quantity). 201 Requirement with covered 0, shortage 6. 404 parent missing; 400 invalid quantity; 409 DUPLICATE_REQUIREMENT, PROJECT_ENDED. DB unique pair enforced.
Q02 GET /api/requirements?projectId=2 (optional). RequirementComponent.list(projectId). 200 array with derived covered, shortage; unknown positive filter => []; invalid filter =>400.
Q03 POST /api/requirements/{id}/update. Input {"quantity":8}. RequirementComponent.update(id,quantity). 200 Requirement; 404 missing; 400 invalid; 409 PROJECT_ENDED or QUANTITY_BELOW_COMMITMENTS if below effective covered or largest live PENDING request.
Q04 POST /api/requirements/{id}/delete. No body. RequirementComponent.delete(id). 200 deletion; 404 missing; 409 DEPENDENCY_EXISTS if any Allocation history.

AllocationController — 8 original + one added = 9 routes
A01 POST /api/allocations. Required input {"requirementId":4,"quantity":4,"borrowerName":"Fictional representative"}; optional "smsConsent":false, optional "phoneNumber" (may only be stored when consent true and valid). AllocationComponent.create(requirementId,quantity,borrowerName,phoneNumber,smsConsent). 201 Allocation with PENDING, stock unaffected. 400 invalid fields or quantity > requirement, missing destination when consent true; 404 Requirement missing; 409 PROJECT_ENDED.
A02 GET /api/allocations?requirementId=4&status=PENDING (both optional; both filters conjunctive). AllocationComponent.list(requirementId,status). 200 redacted Allocation[]; valid no matches []; invalid status 400.
A03 POST /api/allocations/{id}/approve. No body. AllocationComponent.approve(id). 200 APPROVED. 404 missing; 409 if not PENDING, Project ended, insufficient stock, Requirement overcoverage. Early approval before start allowed.
A04 POST /api/allocations/{id}/issue. No body. AllocationComponent.issue(id). 200 ISSUED. 409 if not APPROVED reusable or now outside Project period. Reserve/issued transition DOES NOT double-deduct availability.
A05 POST /api/allocations/{id}/return. No body. AllocationComponent.returnItems(id). 200 RETURNED. 409 if not currently ISSUED REUSABLE. Allows late return after Project ended; operator declares full batch usable, no partial/damaged.
A06 POST /api/allocations/{id}/consume. No body. AllocationComponent.consume(id). 200 CONSUMED. 409 if not APPROVED consumable or outside active period; deduct Resource total atomically once.
A07 POST /api/allocations/{id}/cancel. No body. AllocationComponent.cancel(id). 200 CANCELLED with reason USER_REQUEST when before end; EXPIRED after Project end. 409 for ISSUED/RETURNED/CONSUMED or previously CANCELLED. PENDING cancellation never changes stock. APPROVED cancellation removes effective hold, not physical total.
A08 POST /api/allocations/{id}/delete. No body. AllocationComponent.delete(id). 200 deletion. 409 unless never-approved PENDING/CANCELLED with approvedAt null. Physical deletion protects historical approvals.
A09 POST /api/allocations/{id}/revoke-sms-consent. No body. AllocationComponent.revokeSmsConsent(id). 200 Allocation; clear phoneNumber and smsConsent false; lifecycle/inventory unchanged; repeated existing-record revocation harmless. 404 if missing. This is one OPTIONAL additional privacy operation relative to original 25.

Shared HTTP error taxonomy (proposed addition to original message-only error)
- 400 VALIDATION_FAILED: malformed JSON, missing required field, nonpositive IDs, invalid enum/quantity/date/client-controlled field.
- 400 INVALID_PROJECT_PERIOD: end<=start or invalid new start.
- 404 NOT_FOUND: path target or required parent FK absent.
- 409 DUPLICATE_NAME, DUPLICATE_REQUIREMENT, DEPENDENCY_EXISTS, RESOURCE_NOT_EMPTY.
- 409 INSUFFICIENT_STOCK, REQUIREMENT_ALREADY_COVERED, QUANTITY_BELOW_COMMITMENTS, STALE_RESOURCE_QUANTITY.
- 409 INVALID_ALLOCATION_STATE, RESOURCE_TYPE_MISMATCH, OUTSIDE_PROJECT_PERIOD, PROJECT_ENDED, PROJECT_DATES_LOCKED, HISTORY_PROTECTED.
- 503 RETRY_REQUIRED only for recognized transient contention/deadlock, no automatic non-idempotent POST retry.
- 500 INTERNAL_ERROR sanitized unexpected server defect, including invariant breach; input constraint violations 400 versus server return-value violation 500.
Example:
~~~json
{"code":"INVALID_ALLOCATION_STATE","message":"Only approved reusable allocations can be issued."}
~~~
Delete:
~~~json
{"message":"Record deleted."}
~~~

Creation 201, other success 200, empty list [] and unknown positive filter => 200 []. Unknown required FK => 404. No raw ORM entity JSON. UTC conversion must not shift declared PHT wall times. Reject or safely ignore unknown JSON fields deliberately and test; do not silently permit server fields.

Component method signatures proposed
DepartmentResponse create(String name); List<DepartmentResponse> list(); DepartmentResponse update(Long id,String name); DeleteResponse delete(Long id).
ProjectResponse create(Long departmentId,String name,LocalDateTime startAt,LocalDateTime endAt); List<ProjectResponse> list(Long departmentId); ProjectResponse update(Long id,String name,LocalDateTime startAt,LocalDateTime endAt); DeleteResponse delete(Long id).
ResourceResponse create(String name,ResourceType type,Integer totalQuantity); List<ResourceResponse> list(ResourceType type); ResourceResponse update(Long id,String name,Integer totalQuantity,Integer expectedTotalQuantity); DeleteResponse delete(Long id); ResourceAvailabilityResponse getAvailability(Long id).
RequirementResponse create(Long projectId,Long resourceId,Integer quantity); List<RequirementResponse> list(Long projectId); RequirementResponse update(Long id,Integer quantity); DeleteResponse delete(Long id).
AllocationResponse create(Long requirementId,Integer quantity,String borrowerName,String phoneNumber,Boolean smsConsent); List<AllocationResponse> list(Long requirementId,AllocationStatus status); AllocationResponse approve(Long id), issue(Long id), returnItems(Long id), consume(Long id), cancel(Long id), revokeSmsConsent(Long id); DeleteResponse delete(Long id).

Frontend public TypeScript model must match JSON, not persisted entity graph. Frontend derives valid UI action availability but server always rechecks transactionally. Frontend should not synthesize new GET-by-id endpoints; use list and local lookup. GET lists are not atomic cross-endpoint snapshots; show refresh/retry and avoid false claims of snapshot consistency.


---

# MASTER SECTION 7: 06_PERSISTENCE_TRANSACTIONS_JPQL.md

# 06 — Java components, repositories, JPQL, locking, concurrency, atomicity

## Course-compatible structure

Jersey JAX-RS @Component Controller (@Path, @GET, @POST, @Consumes JSON, @Produces JSON) -> @Autowired Spring Component (validation, business rule, @Transactional) -> JpaRepository (derived and @Query JPQL) -> MySQL InnoDB. Controller does NOT directly update ORM graph. DTO request/response classes avoid exposing phoneNumber or bidirectional JPA relations and prevent clients writing server-computed fields.

Jersey ResourceConfig register each @Component controller individually to work in packaged archives. Example @ApplicationPath("/api") + controller @Path("/departments"), no double prefix. @EnableScheduling on app, but do NOT copy SchedulingApp teaching example's exclusions disabling DataSource/JPA. Spring @Transactional uses proxy semantics; do not rely on self-invocation or @PostConstruct proxied transaction. Use a separate Spring bean for @Transactional expiry-one and reminder-claim methods.

## Repository contracts

DepartmentRepository extends JpaRepository<Department,Long>:
- existsByNameKey(String nameKey).

ProjectRepository:
- findByDepartment_Id(Long departmentId).
- existsByDepartment_Id(Long departmentId).
- Optional<Project> lockById(Long id) @Lock(PESSIMISTIC_WRITE) and explicit JPQL; verify actual behavior.

ResourceRepository:
- existsByNameKey(String nameKey).
- findByType(ResourceType type).
- Optional<Resource> lockById(Long id) @Lock(PESSIMISTIC_WRITE), explicit query.

ResourceRequirementRepository:
- findByProject_Id(Long projectId).
- existsByProject_Id(Long projectId).
- existsByResource_Id(Long resourceId).
- existsByProject_IdAndResource_Id(Long projectId,Long resourceId).
- Appropriate locked query or reload for update/delete, respecting Project then Resource.

AllocationRepository:
- findByRequirement_Id(Long requirementId).
- findByStatus(AllocationStatus status).
- findByRequirement_IdAndStatus(Long requirementId,AllocationStatus status).
- existsByRequirement_Id(Long requirementId).
- sumEffectiveHolds(resourceId,approved,now).
- sumIssuedCustody(resourceId,issued).
- sumCoveredQuantity(requirementId,completedStatuses,approved,now).
- maxPendingQuantity(requirementId,pending).
- countHistoricallyApproved(projectId).
- findExpiredCandidateIds(now,limit).
- findReminderCandidateIds(now,limit).
- explicit lockById of Allocation.
Do NOT redeclare JpaRepository inherited save, findById, findAll, delete unnecessarily.
JPQL references Java property names, not physical SQL columns.
Aggregates SUM(Integer) should be Long; handle null as zero with safe long arithmetic. Empty collection repository returns empty list; ordinary scalar aggregate can return null.

## Candidate JPQL (must compile and run on actual Spring Data/JPA stack)

Effective holds:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.resource.id = :resourceId " +
       "AND a.status = :approved " +
       "AND a.requirement.project.endAt > :now")
Long sumEffectiveHolds(@Param("resourceId") Long resourceId,
                       @Param("approved") AllocationStatus approved,
                       @Param("now") LocalDateTime now);
~~~

Issued custody:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.resource.id = :resourceId " +
       "AND a.status = :issued")
Long sumIssuedCustody(@Param("resourceId") Long resourceId,
                      @Param("issued") AllocationStatus issued);
~~~

Requirement covered:
~~~java
@Query("SELECT SUM(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.id = :requirementId " +
       "AND (a.status IN :completed " +
       "OR (a.status = :approved " +
       "AND a.requirement.project.endAt > :now))")
Long sumCovered(@Param("requirementId") Long requirementId,
                @Param("completed") Collection<AllocationStatus> completed,
                @Param("approved") AllocationStatus approved,
                @Param("now") LocalDateTime now);
~~~
Completed = ISSUED, RETURNED, CONSUMED exactly. Verify 'IN :completed' binding syntax and no empty collection (always 3 values).

Max pending:
~~~java
@Query("SELECT MAX(a.quantity) FROM Allocation a " +
       "WHERE a.requirement.id = :requirementId " +
       "AND a.status = :pending")
Integer maxPendingQuantity(@Param("requirementId") Long requirementId,
                           @Param("pending") AllocationStatus pending);
~~~

Historical approved:
~~~java
@Query("SELECT COUNT(a) FROM Allocation a " +
       "WHERE a.requirement.project.id = :projectId " +
       "AND a.approvedAt IS NOT NULL")
Long countHistoricallyApproved(@Param("projectId") Long projectId);
~~~

Expiry candidate query:
~~~java
@Query("SELECT a.id FROM Allocation a " +
       "WHERE a.status IN :unfulfilled " +
       "AND a.requirement.project.endAt <= :now " +
       "ORDER BY a.id")
List<Long> findExpiredCandidateIds(
  @Param("unfulfilled") Collection<AllocationStatus> unfulfilled,
  @Param("now") LocalDateTime now,
  Pageable pageable);
~~~
unfulfilled = PENDING, APPROVED. Use PageRequest.of(0, boundedNumber) API version compatible with chosen Spring Data. Query candidates then process one ID per small transaction; lock after lookup and recheck.

Reminder candidate selection should JOIN through Requirement/Resource/Project and filter ISSUED, ResourceType.REUSABLE, consent=true, phone not null, reminderClaimedAt null and endAt within [now,now+24h). Query returns IDs only; selection is NOT final eligibility proof. Recheck after locking Allocation and using same clock sample.

Pessimistic locks:
~~~java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Resource r WHERE r.id = :id")
Optional<Resource> lockById(@Param("id") Long id);
~~~
Analogous Project/Allocation. Test Hibernate SQL generated and race results; do not assume all lock methods compile by reading source only.

## Correct lock order and transaction boundaries

Inventory mutating commands:
1. Determine associated Project/Resource identifiers (prelookup can be stale; never trust after locks).
2. Start Spring transactional method (prefer verified READ_COMMITTED with InnoDB).
3. Lock Project row.
4. Lock Resource row.
5. Reload/refresh Requirement, Allocation and related associations, and lock Allocation when changing it.
6. Sample one Clock instant in Asia/Manila.
7. Re-evaluate current state, FK identity, project dates, available H/I and Requirement coverage.
8. Persist all related modifications.
9. Commit; on refusal or exception rollback.

Resource total update locks Resource only and computes obligations under that lock. Reminder-only claim/revoke locks Allocation without then acquiring Project/Resource WRITE locks. This avoids reverse lock-order cycle; inventory commands lock Project/Resource first, then Allocation. Queries that read parents without write locks in SMS are advisory; final pre-dispatch recheck cannot absolutely eliminate send-return race once outbound call starts.
Revalidate entities after waiting for lock. JPA's persistence context can retain stale prelock instances; explicit refresh/re-query/clear as appropriate, verified in tests. Ensure completion statuses/timestamps included in fresh DB aggregate (flush/refresh), and compare immediate DTO to a fresh GET after commit.

Approve:
- Require PENDING and Project.endAt > now; early start allowed.
- require alloc.quantity <= availableResource AND <= Requirement.quantity - covered.
- Save APPROVED and approvedAt once.
- Failures leave PENDING unchanged.

Issue:
- Require APPROVED, REUSABLE, startAt <= now < endAt.
- Save ISSUED/issuedAt, no stock total decrement.

Return:
- Require ISSUED REUSABLE; allow late; mark RETURNED/returnedAt; no stock total adjustment.
- Officer declares whole batch usable, not objective physical proof.

Consume:
- Require APPROVED, CONSUMABLE, active period, underlying Resource.total >= quantity.
- Resource.totalQuantity -= quantity; Allocation.status=CONSUMED; consumedAt=now in SAME transaction.
- After commit availability remains old available as hold disappears and stock drops same q.
- Force test exception after one write and prove both rollback.

Cancel:
- PENDING/APPROVED only; after expiry reason EXPIRED, otherwise USER_REQUEST. Preserve approvedAt. No second physical stock decrease.
Delete:
- Only PENDING or CANCELLED and approvedAt null; actual DB deletion.
Requirement update:
- Project and Resource locks; newQ >= max(covered,largest live pending); refuse Project ended.
Project date update:
- Project lock and check countHistoricallyApproved>0; changed dates require new future start/end; unchanged dates allow name-only.
Project/Resource/Requirement create and delete must respect same lock conventions for races. Department delete vs Project create depends on DB FK and appropriate locking; catch recognized integrity collision and map to 409 when supportable, not generic success.

## Race tests

1. Resource five units, two PENDING Allocation requests qty four for different Requirements: parallel APPROVE, exactly one success, available >=0.
2. Same Requirement qty six, two PENDING qty four, different rows: parallel APPROVE, exactly one success, covered<=6.
3. Concurrent consume and Resource stock correction with expectedTotalQuantity stale: one correct winner, no overwritten stock.
4. Concurrent Requirement update and approval: never coverage > demand.
5. Project date edit and approval: historical approval prevents date change if approval happens first; if date changed first, subsequent approval must use new times.
6. Expiry cleanup vs return/issue: lock and reload; no fraudulent physical return.
7. Reminder result recording vs RETURN must not overwrite state via stale detached entity.
8. Concurrent Resource create with same normalized name: DB unique index wins, no duplicate.
9. Concurrent Requirement create for same pair: DB unique index wins.
10. Lost HTTP response after successful COMSUME: retry state CONSUMED refused; never double deduct. (Correct spelling CONSUME in source code.)

## Error/rollback semantics

Validation exceptions from request => 400; server-generated DTO constraint defect => 500; missing parent/target => 404; business conflict => 409; recognized lock deadlock/timeout => 503 or mapped safe retry; unexpected DB integrity internal defect => 500 sanitized unless known duplicate/reference collision; never print SQL stack in public JSON.
A rejected business operation should not commit partial writes. A missing HTTP response DOES NOT establish DB rollback; client must re-fetch.
No automatic POST retries by frontend or scheduler after uncertain commit. Ensure exception classes are unchecked or @Transactional(rollbackFor=...) for checked domain exceptions as appropriate.

## Test evidence

For each state-changing operation check HTTP code, persisted state, and independent invariant. Sequential Postman can exercise route behavior; only real parallel DB tests prove concurrency. Test Java compiler/JPA initialization/SQL/timeout details with actual environment, not just this document.


---

# MASTER SECTION 8: 07_RETROFIT_SCHEDULING_SECURITY.md

# 07 — Retrofit/Gson, SMS, scheduling, security, privacy, demo-only policy

## Course interface

Printed assignment says "RetroJit", but course examples and syllabus use Retrofit and Gson. Implement actual outbound Retrofit from Java backend; frontend fetch to OrgResource API does not satisfy external-service requirement.

Course FakeTwilioController maps @Path("/fake_twilio"), @POST @Path("/send"), consumes application/x-www-form-urlencoded fields To, MessagingServiceSid, Body; Authorization header required and PRINTED by simulator (never send real auth). Local configured port in original course files 9997, verify actual startup. Simulator returns JSON accepted status typically HTTP 200; original Retrofit tester looked for HTTP 201 for real Twilio API, so do not assume 201 in simulator. Gson converter must be enabled and actual reply parsed. Response key messaging_service_sid needs @SerializedName.

## Modes

DISABLED default: no send, no provider credential requirement; core and expiry work.
FAKE_TWILIO: form-encoded HTTP to local simulator, dummy auth only, outcome SIMULATED when actual HTTP response accepted.
TWILIO_VIRTUAL conditional: actual authorized Twilio Messaging API to user's eligible controlled Virtual Phone destination, provider-accepted result only, NOT handset delivered. Trial/template restrictions are changeable and must be verified against current Twilio primary docs and actual account; never invent provider entitlement.

No real student/borrower phone number or live SMS without explicit safe authorization. A Boolean in demo JSON cannot establish real-world legally valid consent. Prototype uses only fictional/controlled destinations.

## Suggested interface

~~~java
public interface FakeTwilioApi {
 @FormUrlEncoded
 @POST("fake_twilio/send")
 Call<FakeTwilioReply> send(
   @Field("To") String to,
   @Field("MessagingServiceSid") String sid,
   @Field("Body") String body,
   @Header("Authorization") String dummyAuth
 );
}
~~~
Initialize Retrofit with verified baseUrl ending '/' and GsonConverterFactory. DTO fields body,to,status,messaging_service_sid via @SerializedName. Observe actual HTTP status, body and error stream; status acceptance != delivery.

## Return reminder eligibility

Project ends in upcoming 24h: endAt -24h <= now < endAt.
Status ISSUED (reusable only), smsConsent true, valid phoneNumber, reminderClaimedAt null.
Avoid REMINDERS for RETURNED/CANCELLED/CONSUMED or overdue after end. ISSUED after end remains custody but no new upcoming reminder; this is faithful to original proposal and should not silently become overdue SMS.

## Claim/dispatch lifecycle

Scanner @Scheduled query selects bounded IDs.
Transaction claimOne(id): lock Allocation, re-read status/consent and relevant current Project end, check unclaimed, set reminderClaimedAt=now and outcome CLAIMED, COMMIT.
Outside DB tx, last eligibility and consent check; if newly RETURNED/revoked, mark SUPPRESSED. If still eligible, form POST through Retrofit. Rejected definitive response -> REJECTED. Fake accepted -> SIMULATED. Real provider accepts -> PROVIDER_ACCEPTED. Network timeout, crash after claim -> UNKNOWN (don't automatically resend). Result persistence should only update reminder fields while locking Allocation; never save stale Allocation object and overwrite returned status.
Do NOT hold open database transaction while calling remote HTTP. Do NOT guarantee perfectly synchronized revocation/return and external dispatch after it starts; document unavoidable race.
Do NOT automatically retry uncertain send. Stale CLAIMED record can be reconciled to UNKNOWN without another send.

## Expiry job

@Scheduled separately from SMS; actual @EnableScheduling on Spring Boot app; DO NOT copy example's excluded datasource autoconfiguration. At/after Project.endAt, find PENDING or APPROVED, lock Project -> Resource -> Allocation, recheck current status/end, set CANCELLED with reason EXPIRED and cancelledAt. For APPROVED, stock effective hold was already zero at end; cleanup should not release again. For PENDING, no stock effect. For ISSUED overdue, no auto return. Preserve approvedAt historical marker.
Demonstrate actual invocation; a manually called method alone doesn't establish Scheduling requirement.
Configurable interval, ordinary 1h, short local demo profile for observed scan. Inject Clock for deterministic tests; no public time-modification endpoint.

## Security boundary

No authentication/authorization in baseline; one trusted local operator. Server bind 127.0.0.1 by default, no unrestricted public host. No claims of production security. UI About/Demo Guide labels as fictional academic prototype, not official Ateneo service. Do not show fake login/role UI implying real access control.

Data minimization: borrowerName fictional; phone only if consent true; revocation clears destination; API responses never expose raw number; logs never print destination or auth; prevent frontend state echo of persisted contact values.
Secrets via environment, not repo, README, Postman exported environment or CI log. Gitignore .env, test output, dumps, node_modules, target, credentials, user-specific settings. Avoid blanket ignoring required Maven wrapper or Eclipse metadata if needed for reproducible import.
No unsafe CORS wildcard + credentials; same-origin UI with Vite local proxy. Browser cannot override server-controlled IDs/status or computed values; DTOs narrow, server validation and DB constraints still apply.
Handle malformed JSON, invalid enum, invalid LocalDateTime, overflow/out-of-range quantity, duplicate normalized names, unknown FKs, concurrency, invariant failure, SQL errors with sanitized responses.
Do not expose stack traces/SQL in UI; technical logs use non-sensitive IDs. Browser must show errors without falsely claiming success.
Conditional image feature: validate actual PNG/JPEG signature, MIME, size; no filesystem paths or untrusted SVG/scripts, store bytes in Resource if confirmed required.

## Test controls

SEC01 loopback listener; SEC02 credentials excluded; SEC03 redacted Allocation JSON; SEC04 consent=false destination null; SEC05 server-controlled fields protected; SEC06 DB FKs; SEC07 no broad CORS; SEC08 simulator dummy Authorization; SEC09 no SQL leaks; SEC10 stale update refused; SEC11 frontend escapes text; SEC12 no fake success on lost POST; SEC13 demo seed non-destructive; SEC14 no unsolicited provider sends; SEC15 upload safe if added; SEC16 GitHub CI logs sanitized.

SMS01 disabled startup; SMS02 expiry independent; SMS03 eligible claim one; SMS04 second scan none; SMS05 return before claim; SMS06 revocation before claim; SMS07 return between claim and pre-dispatch recheck; SMS08 real simulator accepted JSON; SMS09 error status; SMS10 timeout unknown; SMS11 restart no duplicate; SMS12 window boundaries; SMS13 no physical delivery claim; SMS14 missing destination suppression; SMS15 no real secret in simulator logs.

## Privacy and academic integrity

No real campus inventory, demand measurements, interviews, real user accounts or provider approval asserted. No actual project Java code/DB/SMS tests are claimed by this file. Document outside AI/research assistance truthfully as required by syllabus Certificate of Authorship. Check Canvas for explicit generative-AI policies; cannot infer from syllabus absence.


---

# MASTER SECTION 9: 08_UI_REACT_TYPESCRIPT.md

# 08 — Complete React/TypeScript UI specification (real functional application)

USER DECISION: Actual working UI is mandatory as a final outcome even though course calls UI a bonus. It cannot replace backend Jersey requirements and should not delay imminent skeleton ZIP/video. No fake dashboard/placeholder database presented as complete. React must use real HTTP/JSON and server-authored stock/coverage.

## Framework architecture

Vite + React + TypeScript (strict mode) with pinned npm lockfile. HashRouter to make deployed routes '/#/projects' possible while backend owns '/api/*'. Dev Vite proxy '/api' -> http://127.0.0.1:8080 (preserve prefix). Bundled production assets into Spring Boot static classpath, Jersey @ApplicationPath("/api") and individual controller registration. Verify real packaged JAR serves '/' and '/api/departments', CSS/JS and refresh. If legacy servlet mapping fails, documented two-process local run fallback until corrected, not fake bundled success.

Suggested frontend dirs: src/api (typed client, mutations), src/types (DTO), src/components (layout, cards, table, modal, form), src/features/dashboard,departments,projects,resources,requirements,allocations,reminders, src/styles, src/tests, e2e.

No separate Node backend, Next.js server, Supabase, Firebase, DB or auth provider. No manual send SMS endpoint; reminder is scheduled Java backend.

## Design system

Professional restrained academic operations app:
- Light neutral background; white cards; primary deep blue/indigo; restrained green success, amber warning, red destructive.
- Accessible sans-serif body 14–16px; visible heading hierarchy; 4/8/12/16/24/32px spacing.
- Consistent border radius ~10–14px, subtle borders, little shadow.
- Desktop 240–260px sidebar; mobile collapsible drawer and no page overflow; responsive grids and table horizontal scroll if necessary.
- Persistent app name "OrgResource", contextual "Local academic prototype" notice, accessible nav and keyboard focus.
- Buttons descriptive, no generic unlabeled icons; non-color status text; WCAG-minded contrast, logical labels/aria where appropriate.
- Loading skeletons only when actually loading; empty state with next action; error with Retry; success after real response; no fabricated sample stats.

## Screen architecture and functions

Dashboard:
- Load GET /departments,/projects,/resources,/requirements,/allocations (parallel where useful).
- Count Departments, Resource TYPES, active Projects, upcoming Projects, pending Allocation REQUESTS, outstanding ISSUED BATCHES, overdue ISSUED BATCHES, Requirements with positive shortage, CONSUMED Allocation records. Do NOT sum 10 cords + 100 sheets as one meaningful "110 resources available" metric.
- Show recent Allocation rows, upcoming/overdue lists, actions linking to real workflows.
- Active Project derived startAt<=now<endAt in Asia/Manila; upcoming start>now, ended end<=now.
- Partial failure must show affected metrics as unavailable, not fake zero/false fresh success. Cross-endpoint reads may not form atomic snapshot; label refresh time and provide refresh.

Departments:
- GET list, create POST, rename POST, delete POST only after user confirms.
- Search/filter locally by display name, stable sort.
- Duplicate normalized name 409, dependency 409, missing ID 404, empty state, responsive form and table.
- Do not remove row until server returns success.

Projects:
- GET list filter departmentId, creation form with Department picker, name, PHT start/end.
- HTML datetime-local input sends YYYY-MM-DDTHH:mm:ss *local wall time* directly, not toISOString converting UTC unexpectedly.
- UI Upcoming/Active/Ended derived labels; not stored enum.
- Edit name and dates; name-only update preserves unchanged old dates; date changes may be 409 if historic approval.
- Project detail view composes Requirements/Allocations data using existing list APIs (no invented GET-by-ID).
- Delete only when no Requirements; explain refusal.

Resources:
- GET list/filter type, create Resource name,type,total; display totalQuantity vs availableQuantity and meaning by REUSABLE/CONSUMABLE.
- Do not display unit column unless deriving from name; no required 'unit' field.
- Update name/total using expectedTotalQuantity set from last server observation; if 409 stale, re-fetch current, explain conflict, force user to review (never automatic overwrite).
- Safe delete total=0 and no Requirements, confirm identity, error feedback.

Requirements:
- GET list/project filter, create with Project and Resource picker; unique pair; show required, allocated, shortage. PENDING not counted.
- Update quantity with server enforcing >= coverage and max individual pending; show explanatory 409, allow cancel pending first.
- Delete only no Allocations; no erasing history.

Allocations:
- List/filter Requirement,status; show Project name and Resource through related read-only maps; borrower label, quantity, status, lifecycle times, consent presence and reminder result.
- Create PENDING with borrower label, quantity and optional fictional test contact/consent. No real student numbers.
- On PENDING approve/cancel and delete if never approved.
- On APPROVED REUSABLE issue/cancel; on APPROVED CONSUMABLE consume/cancel; on ISSUED reusable return; RETURNED/CONSUMED read-only; CANCELLED previously approved read-only; CANCELLED never approved deletable.
- Consent revocation action independent of lifecycle (if consent currently true); POST /revoke-sms-consent clears saved phone; don't show full phone in UI.
- Return confirmation explicitly declares entire batch physically received usable. Consume confirmation clearly states full batch deduction and no undo.
- Post state-changing command must await actual result and re-fetch Allocations, Resources, Requirements, Project when relevant. No optimistic guessed balance.
- HTTP timeout or lost result: show uncertain outcome, re-fetch row/list, don't automatically retry non-idempotent POST.
- Show 409 state/race conflicts accurately, no fabricated success.

Reminders / Activity:
- Use Allocation and Project lists, not invented notification-log API. ISSUED batches due soon (end within 24h), overdue issued; show recorded reminderClaimed/Outcome and consent.
- SIMULATED label "Accepted by local simulator", PROVIDER_ACCEPTED "Provider accepted request", UNKNOWN "Outcome uncertain", not "Delivered".
- No manual Send SMS UI unless new authorized backend contract explicitly agreed.

About/Demo:
- Explain hypothetical org, five entities, stock formulas, limitations: whole-batch returns, no damage/loss, no auth, fictional local data, no campus endorsement or live SMS claim.
- Provide documentation/dev version and accessible usage help, not fake Admin account.

## Shared states every screen must handle

INITIAL_LOADING, EMPTY, READY, VALIDATION_ERROR, HTTP_404, HTTP_409, NETWORK_ERROR, SERVER_ERROR, REFRESHING, MUTATING, SUCCESS_CONFIRMED, STALE_DATA. Retry GET safely; no automatic POST retry. Modal focus trap and restore, keyboard tab order, visible focus, accessible labels, screen-reader status; test at 375px and desktop widths. Do not use disabled action as security; server revalidates.

## Typed API contract

Department {id:number,name:string}.
Project {id,departmentId,name,startAt,endAt} date strings PHT.
Resource {id,name,type:"REUSABLE"|"CONSUMABLE",totalQuantity,availableQuantity}.
ResourceRequirement {id,projectId,resourceId,quantity,allocatedQuantity,shortageQuantity}.
Allocation {id,requirementId,quantity,borrowerName,contactPresent,smsConsent,status,createdAt,approvedAt,issuedAt,returnedAt,consumedAt,cancelledAt,cancellationReason,expiredPendingCleanup,reminderAttempted,reminderOutcome} nullable timestamps and enums. Never phoneNumber in read DTO.
Error {code,message}. Availability {resourceId,availableQuantity}. Delete {message:"Record deleted."}.

HTTP client:
- relative /api paths, JSON Accept/Content-Type, AbortSignal support for GET, parse JSON failures separately, throw typed ApiError(status,code,message) for non-2xx, do not use null "success".
- Prevent two simultaneous mutation submissions from same UI form where possible, still backend is authoritative.
- No localStorage as inventory database, no mock intercepts in final browser E2E.
- Handle 200 [] from valid unmatched list filters vs 404 required FK.

## Visual quality acceptance

UI01 dashboard empty DB shows no fabricated stats.
UI02 Department create persists.
UI03 Project dates round-trip PHT.
UI04 Resource create/list and availability.
UI05 Requirement shortage.
UI06 Allocation PENDING stock unchanged.
UI07 Approve reserves.
UI08 ISSUE no second deduction.
UI09 RETURN releases custody preserves coverage.
UI10 CONSUME once.
UI11 Stale expected total 409.
UI12 Unsafe delete refused with informative message.
UI13 Consent revoke no contact leak.
UI14 browser refresh persistence.
UI15 backend restart persistence.
UI16 backend unreachable shows correct offline/error, not success.
UI17 keyboard-only core workflows.
UI18 narrow viewport all controls usable.
UI19 real backend browser E2E not all mocks.
UI20 final bundled assets work from Java JAR.

## Testing

Vitest and React Testing Library (unit/component), Playwright browser E2E after actual API live. Frontend build, lint, unit tests, E2E. Mock fetch is allowed for isolated component tests but must NOT replace real-backend acceptance. Mobile layout and accessibility manual checks plus automated semantic queries. No actual screen or performance measurement claim until tested.


---

# MASTER SECTION 10: 09_TEST_POSTMAN_DEMO_ACCEPTANCE.md

# 09 — Complete acceptance testing, fixtures, Postman and demonstration

## Test philosophy

A planning specification or mock reference model is not a running Java test. Each required code feature must be accepted based on real execution: compiler, actual relational DB, HTTP/Postman, real browser, actual scheduler, external HTTP simulator, and clean export. Every test result PASS / FAIL / BLOCKED / NOT_RUN with command, timestamp, output evidence and commit. A generated Postman JSON file is not evidence that any request executed. Static route coverage can be checked independently from runtime.

## Reproducible fictional fixture

Department "Fictional Logistics" and Project "Welcome Workshop" created with startAt in future and endAt>startAt. TWO Resources: REUSABLE "Extension cords" totalQuantity 10, CONSUMABLE "A4 paper sheets" totalQuantity 100. Requirements: 6 cords and 40 sheets. Allocations: A=4 cords, B=2 cords, C=30 sheets. A and B cannot be issued until Project actually becomes active; do not run setup and fulfillment back-to-back expecting future-start to have passed. Use injected fixed Clock in automated integration tests and real clock in video.

Expected state checkpoints:
- Initial cords available 10, covered 0; paper available100,covered0.
- A PENDING no change. A APPROVED: cords available6,covered4.
- A ISSUED: cords available6,covered4 (no second deduction).
- A RETURNED: cords available10,covered4.
- B APPROVED: cords available8,covered6.
- B ISSUED: cords available8,covered6. Leave B ISSUED through real scheduled FakeTwilio demonstration if eligible.
- B RETURNED: cords available10,covered6.
- C APPROVED qty30 paper: paper available70, covered30, total still100.
- C CONSUMED: paper available70, covered30, total becomes70 (not 40!).
- Paper Resource updated with total=120 expectedTotalQuantity=70: paper available120,covered30.
- Requirement shortage: cords0, paper10.
No callback can imply physical return when only SQL status updated.

## Disposable CRUD fixture

Separate new Department, new Project, zero-stock Resource, Requirement, never-approved Allocation. Create/list/rename each, cancel pending, revoke SMS if testing optional route, delete Allocation -> Requirement -> Resource -> Project -> Department; verify DB rows gone and main workflow unchanged. Resource delete requires total0 and no Requirement reference. This sequence was statically dependency checked, not run in Java.

## Route coverage

Department D01-D04; Project P01-P04; Resource R01-R05; Requirement Q01-Q04; Allocation A01-A09. Original proposal 25 + reviewed A09=26. See 05_API_26_ROUTES_AND_RESPONSES.md for exact path, body and errors. Postman template supplied in ZIP is importable scaffolding only; agents must add generated-ID scripts, negative assertions and run it against actual backend.

Postman environment variable names:
baseUrl; runTag; projectStartAt; projectEndAt;
crudDepartmentId,crudProjectId,crudResourceId,crudRequirementId,crudAllocationId;
workflowDepartmentId,workflowProjectId,cordResourceId,paperResourceId,cordRequirementId,paperRequirementId,allocationAId,allocationBId,paperAllocationId.
Store created IDs from real JSON response, never assume 1. For 'create' expected 201; list expected 200 array; mutators 200. Responses include code/message on failure, deletion message on success. Use unique runTag per run to avoid duplicates; check no residual environment IDs from a failed earlier run; never automatically replay POST after network uncertainty.

Example Postman post-response script for Department create:
~~~javascript
pm.test("Department created", function() {
  pm.response.to.have.status(201);
  const body = pm.response.json();
  pm.expect(body.id).to.be.a("number").above(0);
  pm.expect(body.name).to.be.a("string");
});
if (pm.response.code === 201) {
  const body = pm.response.json();
  if (Number.isInteger(body.id) && body.id>0)
    pm.environment.set("workflowDepartmentId",String(body.id));
}
~~~

Example availability:
~~~javascript
pm.test("Four approved cords reserve exactly four",function(){
 pm.response.to.have.status(200);
 const r=pm.response.json();
 pm.expect(r.availableQuantity).to.eql(6);
 pm.expect(r.resourceId).to.eql(Number(pm.environment.get("cordResourceId")));
});
~~~
Availability JSON has ONLY resourceId and availableQuantity. To independently prove Resource.total and commitments, query Resource GET and DB/Allocation history, not same function twice.

Example refusal:
~~~javascript
pm.test("Repeat CONSUME refused",function(){
 pm.response.to.have.status(409);
 pm.expect(pm.response.json().code).to.eql("INVALID_ALLOCATION_STATE");
});
~~~
Then query DB Resource total to ensure unchanged on repeat. This is not an injected rollback test.

### Time fixture

Project must be created with future start and later end. Postman variables can be generated dynamically in Philippine wall-time, but avoid browser/device timezone assumptions; use Intl DateTimeFormat with Asia/Manila or explicitly offset for demo date only. Use a 3-minute future start and a few hours duration for video after waiting; never pretend immediate fulfillment allowed. Fixed Clock tests use exact instants without wall clock wait.

## Backend test cases

BE01 Fresh startup five tables.
BE02 Department 201 and persisted.
BE03 GET Department row.
BE04 normalized duplicate 409.
BE05 referenced Department delete 409.
BE06 Project invalid interval 400.
BE07 Project foreign key absent 404.
BE08 Project rename while historical dates remain unchanged allowed.
BE09 Project date change after historically approved Allocation 409.
BE10 Resource zero-stock create, available0.
BE11 Approval 4 of available5 reserves4.
BE12 Parallel Resource approvals qty4+4 of total5: exactly one success.
BE13 Parallel Requirement approvals qty4+4 of demand6: exactly one success.
BE14 Pending Allocation doesn't reserve.
BE15 Reusable ISSUE no double deduction.
BE16 Return releases custody once.
BE17 Second return 409/no change.
BE18 Consumable CONSUME decrements total once.
BE19 Second consume 409/no change.
BE20 Reduce Requirement below current coverage 409.
BE21 Requirement10 with pending9, reduce to5 =>409.
BE22 stale Resource update =>409/no overwrite.
BE23 expired APPROVED before cleanup =>effective hold0.
BE24 expired PENDING cleanup => CANCELLED/EXPIRED no stock effect.
BE25 expired APPROVED cleanup=>CANCELLED/EXPIRED no second release.
BE26 overdue ISSUED remains unavailable.
BE27 approved then cancelled deletion =>409.
BE28 never-approved cancelled/pending deletion succeeds.
BE29 positive stock Resource delete =>409.
BE30 zero-stock unreferenced Resource delete succeeds.
BE31 duplicate Project+Resource Requirement =>409.
BE32 forced DB exception during consume rolls back Resource and Allocation.
BE33 empty aggregate quantity0.
BE34 nonexistent required parent FK =>404 no save.
BE35 malformed JSON=>400.
BE36 client-controlled generated ID/status rejected or safely ignored per documented contract, never applied.
BE37 restart preserves all data.
BE38 clean Java source import/build.
BE39 duplicate Resource/Department create concurrently => single row due unique constraint.
BE40 Project update vs approval race preserves historical-date rule.
BE41 Resource correction vs consume race preserves invariant.
BE42 Query results across status and endAt boundary coherent.
BE43 Read-only response across multiqueries stable or documented snapshot.
BE44 Client lost response cannot trigger blind duplicate mutation.

## SMS/scheduler tests

SMS01 Disabled startup with no SMS config.
SMS02 Expiry scheduled independent of SMS.
SMS03 Eligibility exactly [endAt-24h,endAt).
SMS04 One durable claim and no second scanner claim.
SMS05 RETURNED before claim suppress.
SMS06 Revoked consent before claim suppress.
SMS07 Return/revoke observed before dispatch suppress.
SMS08 Actual FakeTwilio accepted HTTP and typed Gson parsed => SIMULATED.
SMS09 Definitive HTTP rejection => REJECTED.
SMS10 Timeout/uncertain => UNKNOWN and no automatic resend.
SMS11 Restart with CLAIMED/UNKNOWN no repeated send.
SMS12 Overdue endAt<=now no upcoming reminder.
SMS13 Real provider success != handset delivery.
SMS14 Missing destination no call.
SMS15 Only simulator dummy Authorization; check no tokens in log.
SMS16 Reminder result persistence never overwrites RETURNED status.
SMS17 Expired PENDING cleanup no stock change.
SMS18 Expired APPROVED cleanup no double stock change.

## UI acceptance tests

UI01 Empty DB shows empty state/no fake stats.
UI02 Department create through real API.
UI03 Project date input PHT roundtrip.
UI04 Resource types and availability.
UI05 Requirement coverage/shortage.
UI06 Pending allocation stock unchanged.
UI07 Approval reserves.
UI08 Issue doesn't double deduct.
UI09 Return restores custody.
UI10 Consume reduces physical once.
UI11 Stale Resource update shows 409 and refresh.
UI12 Unsafe delete explanatory 409.
UI13 Consent revoke clears destination.
UI14 Browser reload shows persistent rows.
UI15 Backend restart data retained.
UI16 Backend outage yields error, no false success.
UI17 Keyboard-only core workflows.
UI18 Mobile/narrow controls accessible.
UI19 Browser E2E uses actual backend, not only mocks.
UI20 Bundled JAR serves UI and API.
UI21 Partial dashboard fetch failure not silently zero.
UI22 Lost POST response does not auto-retry.
UI23 Exact action gating by Allocation status/resource type.
UI24 Responsive table scroll, dialogs and focus restore.

## Acceptance levels and evidence

- STATIC: source reads.
- COMPILE: javac/Maven output, exit code.
- DB: real schema and query.
- HTTP: real Postman/curl against backend.
- CONCURRENCY: parallel requests and final persistent state.
- SCHEDULE: observed @Scheduled invocation.
- EXTERNAL: actual Retrofit HTTP to simulator/provider.
- BROWSER: real React UI and Java backend.
- PACKAGING: real source ZIP, checksums and tested import.
- SUBMISSION: actual Canvas receipt/video link, not performed here.

Use independent assertions, not simply calling the same helper function that could be wrong. If CHECK constraint unsupported by older MySQL, Javax validation + DB FKs/unique still required and document exact enforcement.

## Postman source-generation caveat

The included postman collection is a ROUTE TEMPLATE, not a final full test suite. Every endpoint definition has method, path, placeholder body where needed. Coding agents must amend dynamic IDs, create-flow scripts, negative tests and actual requests after implementation. Report import status separately from execution status.


---

# MASTER SECTION 11: 10_GITHUB_DEVOPS_BACKLOG_RELEASE.md

# 10 — GitHub, agent coordination, CI, backlog, packaging, demonstration and release

## Repository and file layout

Proposed repo: newonathe/OrgResource (not created in this handoff). Prefer private until user approves publication. Verify actual remote before push.

OrgResource/
  AGENTS.md
  CLAUDE.md
  README.md
  .gitignore
  .editorconfig
  .github/workflows/
  backend/pom.xml
  backend/.mvn/, backend/mvnw, backend/mvnw.cmd
  backend/src/main/java/app/SpringBootApp.java
  backend/src/main/java/app/config/
  backend/src/main/java/app/entities/
  backend/src/main/java/app/repositories/
  backend/src/main/java/app/components/
  backend/src/main/java/app/rest/controllers/
  backend/src/main/java/app/rest/errors/
  backend/src/main/java/app/dto/request/
  backend/src/main/java/app/dto/response/
  backend/src/main/java/app/integration/
  backend/src/main/java/app/scheduling/
  backend/src/main/resources/application.properties
  backend/src/main/resources/static/
  backend/src/test/java/
  frontend/package.json, package-lock.json, vite.config.ts, tsconfig.json
  frontend/src/api,types,components,features,styles,tests
  frontend/e2e
  postman/
  docs/ORGRESOURCE_MASTER_HANDOFF.md
  docs/source-audit, decisions, implementation-status, test-plan, proposal-changes
  scripts/
  evidence/skeleton,integration,frontend,release

Only create modules as actual phase needs. Do not generate empty fake app modules or README claiming features.

## Agent responsibilities and worktree isolation

Codex primary: actual course source/environment, Java/JPA/Jersey, DB, transaction engine, time/scheduler/Retrofit, Java tests, export/packaging.
Claude Code primary: independent read-only reviews, React shell and UI/UX in separate worktree, browser tests, user-facing quality, approved targeted fixes.
Roles can swap after explicit task assignment. No two writers on same worktree/file. Source of truth 'docs/ORGRESOURCE_MASTER_HANDOFF.md'; AGENTS and CLAUDE are small references, not competing specs.
If one agent opens PR on feature/backend-skeleton, other agent can review without changing branch, or use distinct branch. No merge to main, no push or destructive git operation without user permission. Check git status/remote before every task and log commit SHA.

## Branches and PR checklists

Branches may be feature/backend-skeleton, feature/crud, feature/allocation-engine, feature/scheduler, feature/ui-shell, feature/ui-workflows, test/full-regression. Use Git worktrees when parallel.
PR includes scope, source evidence, changed files, API/DB contract changes, commands executed, tests PASS/FAIL/NOT_RUN, actual screenshots only if real UI, security review, regressions, reversible rollback, reviewer signoff; no fake checks.

## CI build policy

GitHub Actions: backend JDK 8 + Maven verify, isolated MySQL 8.0 InnoDB service and test profile as needed; frontend Node compatible with Vite, npm ci, lint, unit, build; browser E2E with actual backend when available. Use current official actions versions verified at implementation (2026 docs show actions/checkout@v7, setup-java@v6, setup-node version verify). Never pin deprecated syntax blindly or leak credentials.
Do not claim CI green as proof of tests not configured. Real DB integration tests must use actual MySQL engine, not only in-memory H2. Java compile target class major version 52 for Java 8, verify javap. Frontend output must be copied into backend static classpath and packaged JAR tested. Credentials from GitHub Actions secrets only for actual providers; default SMS DISABLED.
Suggested jobs: java-build, java-db-integration, frontend-build, browser-e2e, release-packaging (later). Avoid Docker deployment, cloud registry, Kubernetes, Redis, tracing, payment API, microservices without requirements.

## Dated backlog (printed source deadlines; current Canvas unverified)

ORG001 Source+environment inspection (Nov13 P0).
ORG002 Java/Maven/Jersey bootstrap (Nov13 P0).
ORG003 Five JPA entities/DB (Nov13 P0).
ORG004 Repositories/query smoke test (Nov13 P0).
ORG005 Five controllers/components JSON basic create-read (Nov13 P0).
ORG006 Exported Eclipse-compatible source ZIP + genuine skeleton video (Nov14 P0).
ORG007 CRUD, validation, safe deletion (Nov15-17 P1).
ORG008 Transactional Allocation and stock math (Nov18-22 P1).
ORG009 Clock/Project expiry scheduling (Nov23-24 P1).
ORG010 Retrofit/Gson/FakeTwilio reminders (Nov24-26 P1).
ORG011 React shell/design system (parallel after API stable P1).
ORG012 Departments/Projects/Resources UI (after CRUD P1).
ORG013 Requirements/Allocations UI (after lifecycle P1).
ORG014 Dashboard/reminder UI (after SMS P1).
ORG015 All-routes Postman, Java regression and concurrency (Nov27-30 P1).
ORG016 Browser E2E, accessibility, responsive checks (Nov27-Dec2 P1).
ORG017 Conditional image feature only if required and confirmed.
ORG018 UI static bundling, Eclipse import and full release export (Dec1-3 P1).
ORG019 Final proposal/source audit, Certificate of Authorship and external help (Dec1-3 P1).
ORG020 Final video, actual evidence, release and academic submission by printed Dec5, subject Canvas confirmation.
Dates are proposed sequence, not actual completed milestones.

## Critical path

Environment -> working Java/Jersey/DB -> five real entity tables/repos -> controllers/JSON -> skeleton export+video.
Final backend: CRUD -> transaction correctness -> time/expiry -> external Retrofit/SMS -> integration tests.
Final frontend: API DTO contract -> React shell -> CRUD screens -> Allocation workflows -> dashboard/reminders -> actual browser tests -> bundled UI.
Agent may parallelize frontend shell after DTOs stable, but testing must be rerun when shared API changed.

## Build and release gates

Gate0 actual source/toolchain and repository state documented.
Gate1 one Jersey->Component->Repository->DB POST/GET observed.
Gate2 five mapped entities and real skeleton export/demo.
Gate3 all original CRUD/validation/deletion correct.
Gate4 concurrent approvals, consume rollback, stock formulas correct.
Gate5 clock/expiration and actual scheduler.
Gate6 outbound Retrofit/Gson HTTP to simulator and honest statuses.
Gate7 React UI integrates real API with correct UX states.
Gate8 comprehensive Postman, DB concurrency, browser E2E.
Gate9 packaged JAR / and /api serve, clean Eclipse import/export, source ZIP and checksums.
Gate10 video/source/assistance/Canvas submission reviewed; actual receipt only if verified.
Every gate can be PASS/FAIL/BLOCKED/NOT_RUN, not "looks good".

## Packaging required

- OrgResource-Eclipse-Project.zip (Java source, pom.xml and any verified portable .project/.classpath/.settings needed, wrapper, no secrets, import guide).
- OrgResource-Full-Source.zip (Java + React + docs + Postman + scripts + lockfile; no generated caches, private lecture archives unless authorized).
- Runnable Java JAR with React production static assets, if bundled build verified.
- All-Routes and Demo Postman collection/environment, non-secret.
- README/setup/run, API/DB/UI/docs, actual tests and logs, latest decision/conflict register, Video link/file, screenshots only when genuine, SHA-256 manifest, exact commit.
- Current course/syllabus final deliverables and Canvas instructions manual verification.
An Eclipse source ZIP is NOT the same as executable JAR. Maven-based Eclipse m2e import can suffice only if accepted; if literal .project export required, generate and actually test portable metadata or disclose limitation.

Package flow:
1. Freeze reviewed Git commit, inspect diff/secret scan.
2. Java JDK8 Maven clean verify + actual MySQL tests.
3. Node npm ci, frontend unit/lint/build.
4. Copy dist assets to Java src/main/resources/static (or configured asset path) and repackage.
5. Run resulting JAR, verify real GET / and GET /api/departments and HashRouter, browser E2E.
6. Build two source ZIPs with exact include/exclude list.
7. Re-extract to temp, check source/config, try clean import, compute SHA-256.
8. Record results, video/Canvas status, limitations. No invented success.

## Contingency cuts

Protect course mandatory five entities/Jersey/JSON/Spring JPA/Javax/Retrofit/Schedule/Postman/Export first. User-selected real UI remains final required scope, but polish/animations/analytics charts can be cut before functional screens. Defer sixth movement ledger, auth/public hosting, procurement, partial damaged returns, automatic anticipated-return booking, third-party campus integration, cloud deploy, complex observability. Conditional image feature only if course interpretation requires; do not pretend waived. Do not substitute fake data or stubs for required final logic.

## Risk register

R1 Legacy JDK/JAR/Maven incompatibility (critical): actual env + course code inspection, portable fallback.
R2 MySQL auth/driver/tables fail: real server checks and schema inspection.
R3 Jersey path /api/api or static root broken: explicit registration/mapping smoke.
R4 JPA derived query typo: startup+query tests.
R5 Approval concurrency stock overbooking: Resource locks + parallel DB tests.
R6 Requirement overcoverage: same lock+JPQL test.
R7 Double consumable deduction: single atomic transaction, repeat refusal and rollback.
R8 Stale stock update: expectedTotalQuantity guarded under lock.
R9 Expiry frees issued custody falsely: status/calc tests.
R10 SMS duplicate/lost response: claim/outcome persisted and no auto retry.
R11 Real Twilio auth in FakeTwilio logs: dummy only.
R12 Frontend fake-success/UTC shift: typed client and PHT tests.
R13 React bundled / served incorrectly: packaged JAR smoke.
R14 Export cannot clean import: early cross-workspace import; reveal limitation.
R15 Video fabricated or unreadable: genuine capture script and checks.
R16 Course Canvas updates unknown: student check.
R17 AI assistance misunderstood: truthful acknowledgment.
R18 ZIP source archive completeness unverified: never claim every original entry reviewed.
R19 Git worktree conflict: one writer per file and reviewed branch merge.
R20 Scope inflation/UI distraction from skeleton deadline: prioritize P0 gates.


---

# MASTER SECTION 12: 11_SOURCE_AUDIT_DECISIONS_RISKS.md

# 11 — Source-coverage and conflict audit; requirements traceability; open decisions

## Source coverage

09_ESP_PROJECT_ASSIGNMENT.pdf: directly read three pages, governing requirements including technologies, group size, >=5 entity types, components @Autowired, runtime create/delete, Postman, proposal description, skeleton exported project/video, final date/video.
08_CSCI180_SYLLABUS.pdf: directly consulted outcomes/rubric/academic integrity/Canvas policy; CLO9 image handling cited but final-project route obligation ambiguous; includes Certificate of Authorship and outside help acknowledgment.
10_ORGRESOURCE_CONCISE_PROPOSAL.docx: 12 pages indexed/extracted; 25 exact route definitions verified unique, 4+4+5+4+8, components, repository methods, five entities, consent and reminder.
03_COURSE_REFERENCE_CORPUS.txt: selected code and dependency inventory inspected. Includes Lab2/Lab3 animal sighting Jersey/Spring JPA, FakeTwilio controller, Retrofit demo, SchedulingApp, dependency JARs. Searchable text is not proof every binary original byte or picture was inspected.
01_KICKOFF_PROMPT.md, 02_MASTER_CONTEXT_DOSSIER.md, 04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md: project history and file map used; earlier extensive plans treated as drafts. Manifest earlier reported 2,611 paths and 852 unique recorded SHA-256 hashes; not independently recalculated here.
06_VIDEO_TRANSCRIPTS.md: timestamped generated AUDIO transcription only. Not actual video screen frames; technical details checked against course slides/code as possible. No request to reupload raw videos.
11_COMPLETE_SOURCE_NONVIDEO_PART1.zip and 12_COMPLETE_SOURCE_NONVIDEO_PART2.zip: provided original complete non-video archives; full original byte inspection unavailable in current agent runtime; agents should inspect exact originals when they have them.
Course slides and references: original Spring/Java/Jersey/JPA/validation/JSON/Retrofit/Twilio/scheduling sources available in project; not every slide or archive entry independently reviewed. Do not assert exhaustive semantic review of all sources.
Public official docs: Spring Boot 2.1.0, VS Code Java, Vite, MySQL, Twilio, React Router, GitHub Actions/Postman schema; date of consultation is Nov13 2026, reevaluate current changeable API/provider behavior when implementing.

## Conflict resolution / ADR registry

ADR01 Exactly five JPA entities Department,Project,Resource,ResourceRequirement,Allocation; no six-entity ledger, no nine-entity ERP.
ADR02 Preserve original 25 route method/path pairs.
ADR03 Propose A09 consent withdrawal as only 26th route.
ADR04 Original Resource create name,type,totalQuantity; NO mandatory unit column.
ADR05 Original availability response two fields only (resourceId,availableQuantity).
ADR06 Resource update add expectedTotalQuantity and 409 stale check as documented amendment.
ADR07 Global conservative holds, approval before future Project start allowed; no future return-based capacity promise.
ADR08 Whole-batch issue/return/consume; no damage/lost/partial statuses.
ADR09 LocalDateTime PHT [startAt,endAt); fixed/injected Clock for test.
ADR10 APPROVED effective hold expires at Project end (intentional behavioral amendment vs original indefinite hold).
ADR11 Expire both PENDING and APPROVED unfulfilled; never auto-return issued.
ADR12 Historical approvedAt retained and protects deletion/date change.
ADR13 Requirement lower bound includes max live PENDING and effective covered (intentional amendment).
ADR14 One durable reminder claim; no automatic retry on uncertain SMS.
ADR15 Reminder only upcoming [endAt-24h,endAt), no overdue extension (matches original).
ADR16 Simulated FakeTwilio HTTP accepted != real provider accepted != handset delivered.
ADR17 Phone number redacted from API and cleared on consent false/revoke (intentional privacy amendment).
ADR18 Javax-era Spring Boot/Jersey/JPA preserved; Maven conditional verified build path; no Spring 3/Jakarta rewrite.
ADR19 VS Code exclusive development, Eclipse-compatible exported Java source for course.
ADR20 Real React/Vite UI mandatory by user decision, but immediate skeleton P0 first.
ADR21 HashRouter initial UI navigation, /api Jersey, root static assets; verify packaged.
ADR22 No auth, one local trusted operator, fictional data, loopback only.
ADR23 Image receive/serve conditional course clarification, not invented requirement/waiver.
ADR24 No public cloud, microservices, extra infrastructure.
ADR25 Single source spec and stage-gated two-agent workflow; no unverified pass/merge.
ADR26 Strict source evidence, full archive and Canvas uncertainty visible.

## 33 course/academic traceability entries

C01 useful Ateneo service/group 1-3 -> hypothetical single-org problem, truthful member list -> Word proposal.
C02 Jersey REST -> JerseyConfig/controller registration -> actual HTTP.
C03 JSON -> DTOs and Jackson -> request/response test.
C04 Spring @Autowired -> controller->component -> source and call test.
C05 JPA/Spring Data -> five entity tables/repositories -> DB tests.
C06 Javax Validation -> constraints/request refusal -> 400/no row.
C07 Retrofit external/Twilio -> Java Retrofit/Gson adapter -> actual simulator HTTP.
C08 Scheduling -> @EnableScheduling + @Scheduled jobs -> observed run.
C09 >=5 justified entities -> domain responsibilities -> 5 real tables.
C10 optional @PostConstruct seeding -> controlled fictional initializer -> restart-safe if used.
C11 web-triggered operations -> 26 route map -> no 404 for declared paths.
C12 GET read POST submit -> method audit and actual calls.
C13 create/read all entities -> runtime create and list per each.
C14 runtime row create/delete -> disposable CRUD reverse dependency.
C15 Postman all calls -> 26-route collection imported and run.
C16 Word proposal prose/group -> actual reviewed docx.
C17 per-operation URL/input/output/errors/component -> API section.
C18 component methods steps -> algorithms/repo section.
C19 entity types/null/ranges/queries -> data dictionary and actual schema.
C20 Oct30 printed proposal date -> actual Canvas receipt if known.
C21 peer reviews other members -> real applicable review, solo interpretation check, no invention.
C22 skeleton DB/tables/data/repos -> SQL evidence.
C23 skeleton controller/components -> Postman.
C24 Nov13/14 printed dates -> Canvas verify.
C25 exported source and skeleton video -> real ZIP/video and import test.
C26 final Dec5 complete logic -> regression and submission.
C27 final recorded group discussion / accessible link -> actual video permissions.
C28 optional UI bonus -> now user-mandatory React and real browser tests.
C29 early presentation alternative -> only if actually accepted.
C30 syllabus authorship and help disclosure -> truthful statement.
C31 Canvas announcements -> manual current course check.
C32 lecture JPQL/Retrofit/JSON -> source-era compatible queries and HTTP evidence.
C33 CLO9 image handling ambiguous -> conditional two-route Resource image feature if confirmed.

## Remaining unknowns (DO NOT GUESS)

- Actual user's OS/install paths, repo preexisting state, GitHub permission/visibility.
- Actual MySQL server/JDK/Maven/Vite versions, current dependency resolutions.
- Current Canvas updates, actual upload receipts, peer review applicability.
- Whether exact Eclipse .project/.classpath vs Maven import is demanded; test/report both where possible.
- Real Twilio Virtual eligibility/trial constraints and approved destinations; FakeTwilio is course-compatible testing route.
- Whether syllabus image CLO9 is mandatory in final assignment.
- Actual original source ZIP full byte coverage; video frame observations not available.
- Actual Java/API/UI/DB code/test evidence: NONE claimed in planning ZIP.
- Actual final proposal amendment approval: NOT established.
- Actual UI implementation, screenshot/video, deployment: NOT established.

## Evidence statuses

SPECIFIED = plan defines it.
IMPLEMENTED = code exists in actual repo.
VERIFIED = observed appropriate running proof.
FAILED = check performed and failed.
BLOCKED = cannot currently run due prerequisite.
NOT_RUN = no execution attempted.
Academic submission needs actual receipt; only user/authorized tools can submit. Source path references from ChatGPT are not local agent files unless copied and mounted explicitly.

## External rationale constraints

Ateneo community need is a prototype hypothesis, not measured demand. Existing orgs may already have inventory systems, and campus facility reservations involve separate official authority; do not claim OrgResource replaces them or has API access. Only fictional seed data. No invented interviews/benefits/costs/policies.

## Acceptance gate before Codex

This planning archive can be used to start Phase O0 if transferred into accessible repo. An agent must NOT assume this archive contains a compiled project, actual tests, or direct Canvas/GitHub authentication. The master is a recommended implementation contract, not a course-approved revised proposal.


---

# MASTER SECTION 13: ORIGINAL_SOURCE_FILES_REQUIRED_FOR_CODEX.md

# Original primary source attachments that coding agents must actually be able to inspect

This handoff ZIP is a COMPLETE PLANNING PACKET, not a redistribution of the user's original uploaded course archives. Those original source bytes are not copied into this archive. To let Codex reproduce the course-compatible implementation, attach/mount original sources when permitted:

- 09_ESP_PROJECT_ASSIGNMENT.pdf
- 08_CSCI180_SYLLABUS.pdf
- 10_ORGRESOURCE_CONCISE_PROPOSAL.docx
- 03_COURSE_REFERENCE_CORPUS.txt
- 04_ORGRESOURCE_PRIOR_WORK_RESEARCH_AND_MANIFEST.md
- 02_MASTER_CONTEXT_DOSSIER.md
- 01_KICKOFF_PROMPT.md
- 06_VIDEO_TRANSCRIPTS.md (timestamped AUDIO only)
- 11_COMPLETE_SOURCE_NONVIDEO_PART1.zip
- 12_COMPLETE_SOURCE_NONVIDEO_PART2.zip
- The original class/JAR example files: Jersey, Spring/JPA, Lab2/Lab3, Retrofit, FakeTwilio, Scheduling, Validation, Hibernate and Image demo if required.
- Relevant slides/PDFs already supplied in ChatGPT Project.

Do not publicly upload third-party course archives or real personal data without permission. A source filename in this packet does not mean the file is inside this ZIP. Each agent must report exact files actually opened. Do not infer video screen content from audio transcripts, or claim all original source archive entries independently reviewed without evidence.


---

# MASTER SECTION 14: PROMPTS/00_FIRST_PROMPT.md

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


---

# MASTER SECTION 15: PROMPTS/O0_TO_O6_BACKEND.md

# Staged backend prompts O0–O6 — use one at a time if not using fast-track

Every phase inherits master contract and must produce actual phase report. Never create a completion claim without running appropriate test. No default permission to push/merge/submit.

## O0 — Source and environment inspection (READ ONLY)

Inspect real repo directory, source map, assignment, syllabus, original concise proposal, available course ZIP examples, JDK, Java compiler, Maven, JDBC/MySQL server, Node, Vite, Git, VS Code settings, ports, .project/.classpath, classpath JARs. Verify Javax/Jersey/Spring Data compatibility and exact 25 routes. Identify existing OrgResource code, branches, secrets without displaying them, source access limitations. Test an environment smoke if safe but do not modify application files. Report valid build/import path, version matrix, potential classes/annotations, and exact O1 implementation plan. Stop on essential blocker.

## O1 — Five-entity working skeleton (P0)

Implement actual Spring Boot application, Jersey Config with individually registered @Component controllers, MySQL datasource and five entities Department, Project, Resource, ResourceRequirement, Allocation, JPA enums string/FK/index/unique, five JpaRepositories and component layer, request/response DTOs, create and list per entity. Ensure @Autowired controller->component forwarding. Validate essential fields, generated IDs, date serialization, and one representative custom query. Optional restart-safe fictional test data in demo profile. Final lifecycle functions can be unfinished stubs if accurately labeled. Do not fake persistence. Run actual Java 8 build, Spring Boot startup, five tables/FKs, repository initialization, live HTTP POST/GET on each entity, restart preserving DB, basic errors. Gate requires real evidence. Stop or progress O2 only after PASS.

## O2 — Immediate exported Java project and genuine video (P0)

Prepare Eclipse-compatible Java source ZIP, Maven/m2e import and portable .project/.classpath/.settings if literal expected. Include dependencies and README, no secrets; run clean extraction/import/build as feasible, record true status. Prepare actual Postman requests, DB SQL inspection, source walkthrough, startup video. If screen recorder not accessible, provide shot list and mark video absent; no fabricated media or Canvas submission. Record actual ZIP checksums/paths and tested commit. Gate ends only with honest export/video/evidence report.

## O3 — Full CRUD and validation

Finish all Department, Project, Resource, Requirement and Allocation create/list/update/delete operations, dynamic computed Resource/Requirement DTOs; normalized unique keys DB-enforced; referential integrity; expectedTotalQuantity Resource update; Department/Project/Resource/Requirement deletion constraints; Project future dates and name-only edits; Requirement >= covered and largest PENDING; redacted Allocation DTO; wrong input vs return-value validation mapping. Preserve original route names and original two-field availability response. Do not implement optional whole UI in this phase. Test positive/negative JSON for each operation, empty array filters, 404 missing targets/parent, 400 malformed data, 409 business conflicts, restart. No fake controller success. Gate real DB/HTTP.

## O4 — Transactional Allocation stock engine

Implement global conservative reservations, effective approved holds/issued custody and Requirement coverage formulas, @Transactional components, pessimistic Project->Resource->Allocation lock order and post-lock refresh, independent availability consistency. Implement PENDING→APPROVED, APPROVED reusable→ISSUED, ISSUED reusable→RETURNED, APPROVED consumable→CONSUMED with atomic Resource decrement, cancel and protected history. Prevent repeated fulfillment. Guard Resource expected quantity updates and Requirement reductions. Real parallel approval/Requirement race tests, stale stock correction vs consume, forced exception rollback, early approval/future Project, expiry boundary arithmetic. Report actual DB states. No StockMovement/partial return/real SMS within transactions. Gate actual concurrency/rollback PASS.

## O5 — Clock/Project expiration and actual Scheduling

Inject Clock/ZoneId Asia/Manila. Implement Project active [start,end), approved future early holds, endAt effective hold expiration, late RETURN allowed, expired PENDING+APPROVED cleanup to CANCELLED/EXPIRED only; no issued auto-return, no physical total manipulation. @EnableScheduling in Spring Boot without disabling DB auto-config. Candidate-ID scanning bounded, each candidate processed in separate transactional component call with proper lock order and state recheck. Fixed-clock tests before/at/after start/end; real @Scheduled invocation, restart idempotency, approvedAt preserved. No HTTP time-control route. Gate clock tests and observed scheduler PASS.

## O6 — Retrofit/Gson, FakeTwilio, SMS consent and one-claim reminders

Implement DISABLED startup, FAKE_TWILIO adapter using form POST To/MessagingServiceSid/Body and dummy Authorization, GsonConverterFactory typed response including @SerializedName("messaging_service_sid"), actual simulator status; optional verified TWILIO_VIRTUAL authorization separate and never fake delivery claim. Reminder window [endAt-24h,endAt), ISSUED reusable, consent, valid fictional destination, no prior claim. Short transaction commits claim; later HTTP outside transaction, then record outcome under Allocation lock without overwriting lifecycle. Handle definitive reject, timeout UNKNOWN, post-claim crash, no auto resend, return/consent suppression. Implement A09 POST revoke-sms-consent, clearing destination, same lifecycle. Actually observe scheduled scan, HTTP and DB outcome; no provider credentials in logs. Gate external/DB/scheduler PASS. Do not claim real handset delivery.


---

# MASTER SECTION 16: PROMPTS/O7_TO_O14_UI_TEST_RELEASE.md

# Staged frontend, integration, conditional image, release prompts O7–O14

All prompts inherit and require actually readable docs/ORGRESOURCE_MASTER_HANDOFF.md and current repo. A phase can begin only when its dependencies are verified. You may work on separate worktrees in parallel after API DTO contract is frozen. Each phase ends with full report: scope, source consulted, actual changes, commands/results, DB/HTTP/UI evidence, failures, security, diff and Git state, gate PASS/FAIL/BLOCKED/NOT_RUN. No unauthorized pushes, merges, Canvas submissions or fabricated screenshots.

## O7 — React/Vite/TypeScript foundation

AGENT: Claude Code or Codex assigned separate frontend worktree.
PRECONDITION: At least stable backend DTO contract and Java basic CRUD.
GOAL: Genuine modern React application skeleton using Vite, TypeScript strict, HashRouter (hash not sent to server), relative /api JSON client, Vite dev proxy preserving /api, responsive layout, sidebar/nav, header, 8 screen routes, error/loading/empty/retry/confirmation components, reusable forms/tables/dialogs, accessible focus and keyboard handling. All screen placeholders must be clearly labeled not connected. Do not fake numbers. Pin npm lockfile and verify node/Vite versions. Do not introduce Node backend, second database, Next.js, authentication, payments or a sixth JPA entity. Frontend does not calculate authoritative stock. Run npm ci after lockfile creation, TypeScript build, lint/unit tests, route navigation, 375px mobile and desktop usability, keyboard focus. Gate real frontend build and shell behavior; no claim backend integration without actual HTTP.

## O8 — Functional Departments, Projects and Resources screens

AGENT: frontend writer; backend CRUD O3 verified.
Implement Department list/search/create/update/delete via original D01-D04, confirm server response and explain 409 duplicate/dependency.
Projects list/filter, create future PHT period, name-only edit preserving dates, guarded date changes, read linked Requirements and Allocations using existing GET lists, safe delete and 400/404/409.
Resources list/filter REUSABLE/CONSUMABLE, create (NO unit field), show server total/available and semantics, update with expectedTotalQuantity precondition, 409 stale refresh and force user review rather than overwriting, delete total0/unreferenced only.
State: loading, empty, field errors, server unavailable, retry, pending mutation, success only after confirmed response, stale warning, destructive confirmation. No invented GET-by-ID routes, no localStorage stock. Run real backend browser/API tests, keyboard/mobile, refresh/persistence. Gate screens actually work against Java.

## O9 — Functional Requirements and Allocations workflows

AGENT: frontend writer; O4 business engine verified.
Requirements create/select Project and Resource; one pair unique; display quantity/allocated/shortage; modify demand with largest-pending rule; safe delete only no history.
Allocations list/filter by Requirement/status, link to related Project and Resource through existing list endpoints; create PENDING with borrower fictional and optional controlled test SMS destination/consent; approve/issue/return/consume/cancel/delete/revoke consent only valid actions. Always server-authoritative despite client-side button states. Confirm whole usable batch on RETURN and whole quantity on CONSUME; avoid pretending partial/damaged handling. Refresh all relevant data after mutating POST. Handle 409 state and 503 contention, 404, 400; after network timeout do NOT auto POST retry, re-fetch and reconcile.
Display lifecycle timestamps, consent boolean, outcomes without phone. Browser E2E: pending no hold, approve reserve, issue no second deduction, return restores, consume once, repeated commands refused, history deletion blocked. No fake success. Gate real UI+DB evidence.

## O10 — Dashboard, reminder activity and responsive finishing

AGENT: frontend writer; backend O6 and O9 dependencies.
Dashboard actual GET data; departments count, Resource TYPES (not sum unlike physical quantities), active/upcoming Projects, PENDING count, ISSUED batches, overdue ISSUED, Requirements shortage count, CONSUMED batch count. Show recent allocations and contextual actions; no fabricated metrics, no inconsistent hidden errors. The dashboard may read multiple endpoints at different instants; provide refresh and don't claim one transaction snapshot.
Reminders Activity: compute due/overdue from issued Project end, show persisted reminderOutcome SIMULATED vs PROVIDER_ACCEPTED vs UNKNOWN, no invented send-history endpoint/manual SMS button. About screen clearly says fictional local academic prototype without auth/public deployment or campus endorsement.
Polish visual hierarchy, accessible focus, mobile drawer, responsive table, positive/warning/error labels; test small screens and keyboard. Gate all UI screens connected and real browser tests.

## O11 — Whole system acceptance and contradiction audit

AGENT: Codex primary QA, Claude independent review read-only.
Do not add features. Validate 5 entity DB schema, unique FKs, exactly 26 intended routes, DTO JSON/TypeScript equality, Java JPA JPQL initialization, Javax validation and return-value error mapping, DB transactions/parallel approval/rollback, stale Resource update, Requirement coverage/shortage, PHT time boundaries, expiry scheduler, Retrofit/Gson FakeTwilio actual request, consent no duplicate, Postman import/real 26 routes plus negative cases, React component/browser E2E with real backend, restart persistence, HashRouter final jar, clean source import, security/credentials, no false evidence. Run tests, log test identifiers, commands, outputs, failures and unrun. Gate no unresolved critical correctness/academic/security defect. Static reference-model checks from planning do NOT count as Java tests.

## O12 — Conditional syllabus image feature (not automatic)

ONLY IF current final-project Canvas/instructor instruction requires receiving and serving images, add nullable Resource.imageData byte[] / imageMimeType String (same five JPA types) and POST /api/resources/{id}/image + GET /api/resources/{id}/image. Test PNG/JPEG magic bytes, MIME, small upload size, genuine database byte round trip, correct Content-Type, 404 no image, invalid/large payload, UI display. Follow actually inspected course multipart/Jersey image examples. Update proposal, Postman and route count 26->28. Do not add cloud object store or sixth entity. Gate real upload/download byte equality. If ambiguity unresolved, record decision as OPEN QUESTION, do not assert instructor waived it.

## O13 — Reproducible bundled app, Eclipse-compatible source export and ZIPs

AGENT: Codex with Claude review.
Freeze and record tested Git commit. Run real Maven JDK8 clean verify, MySQL integration, frontend npm ci/lint/test/build, browser E2E, Postman. Copy built React static assets into packaged Java app; verify executable JAR GET /, /assets, /api/departments, HashRouter refresh/navigation. Generate Java-only source ZIP containing all .java/POM/wrapper/config/example deps, tested m2e Eclipse route and portable .project/.classpath/.settings if course literal, no absolute machine paths. Generate full source ZIP Java+React+docs+Postman+lockfile. SHA-256 checksums, re-extract, secret scan, actual fresh import if feasible; mark import NOT_RUN if not. No unsupported "Eclipse import PASS" claims. Do not include node_modules, target, secrets, private student data, original course archives unless authorized.
Report file paths, sizes, checksums, exact command outputs, failures. Gate reproducible artifacts.

## O14 — Final academic evidence, proposal reconciliation and release

AGENT: Codex primary, Claude review.
Check current assignment/syllabus/Canvas if accessible; preserve original 25 endpoints and explicitly amended 26th, Resource update expectedTotalQuantity, phone redaction, expiry etc. Document actual implementation matches proposal or list deviations. Verify tested final release has course required Jersey JSON Spring JPA Javax Retrofit/Gson Scheduling Postman, 5 entities, real UI, actual restart, DB, concurrency, SMS simulator. Create truthful video storyboard and capture genuine recording only with authorized screen tool. Ensure accessible final recording link, correct instructor permissions (if applicable), Certificate of Authorship and outside AI help acknowledgments; no invented receipt, peer review, instructor approval or pilot. No final release claims without real evidence.
Final output artifact inventory and gate VERIFIED/CONDITIONALLY_READY/BLOCKED, commit SHA, checksums, setup, test matrix, limitations, instructions for actual student Canvas submission. Stop; do not submit or push without user approval.

## General acceptance verdict

PASS/VERIFIED only when suitable observed evidence exists. FAIL if executed and failed. BLOCKED if dependency/tool unavailable. NOT_RUN if never attempted. Report specific tests, no generic "everything tested" claim. Keep test scope precise: static code inspection, compilation, real DB transaction, HTTP, browser E2E, scheduler, external provider, actual import/export and academic submission are distinct evidence classes.


---

# MASTER SECTION 17: PROMPTS/REVIEW_AND_REPAIR.md

# Copy-ready independent review, repair, repo-resume and final challenge prompts

## A — Claude Code independent READ ONLY review of Codex work

Review the current OrgResource implementation against docs/ORGRESOURCE_MASTER_HANDOFF.md, the original current CSCI 180.05 assignment, syllabus, proposal and course examples. Inspect actual Git diff, test output and touched source files. DO NOT WRITE OR COMMIT during this review.

Check:
- Is source Java 8-compatible legacy Spring Boot/Jersey/Javax, not Spring MVC/Jakarta substitution?
- Exactly five entity types? Correct FKs/indexes/unique names/project-resource pair? Validation/nulls/ranges?
- Controllers truly delegate via @Autowired components, no business logic in Jersey?
- Are all original 25 route paths/methods preserved plus proposed A09? Only two-field availability response? No mandatory Resource unit?
- Do project active/expires/timezone rules work? Is historical approvedAt protected?
- Does stock math avoid double hold/consume? Are RETURNED resources available while old Requirement remains fulfilled?
- Does concurrency overbook resource or Requirement? Does stale expectedTotalQuantity guard actually reject? Does rollback work with checked exceptions and lock order?
- Are Project edits, Requirement reductions and deletions race safe?
- Does SMS genuinely use Retrofit+Gson+actual form HTTP, one claim, optional actual provider, fake acceptance not delivery, no real secrets in simulator logs?
- Is @Scheduled genuinely invoked, without disabled DB auto configuration?
- Is React UI real API-backed, responsive, keyboard accessible, with error/loading/stale flows and no fake numbers? Are datetime-local values PHT without UTC shift? Is public /api independent of static route?
- Is Maven/Eclipse-compatible source import actually verified or only documented? Does packaged JAR serve UI and API?
- Are Postman and browser tests run against actual DB or only mocks?
- Is original course-required video/export genuinely present? Any false PASS or fake screenshots? Any credentials/student identifiers in public repo?
For each finding: severity BLOCKER/HIGH/MEDIUM/LOW, exact file/line or repro, violated requirement/invariant, smallest fix, test to prove fix, and whether already demonstrated. End ACCEPT/REJECT/CONDITIONAL with explicit NOT_RUN evidence. Do not introduce unrelated framework rewrite.

## B — Codex targeted repair of accepted findings

Read the independent review report, inspect current branch, reproduce each accepted BLOCKER/HIGH defect, modify smallest set of relevant files, add regression test, execute real check, inspect diff and side effects. Preserve 5 entity schema/route contract unless explicit approved amendment. Do not change unrelated UI design or framework. Report before/after result, test failures and commit; no unauthorized merge/push.

## C — Resume once GitHub newonathe repository exists

Inspect exact verified remote newonathe/OrgResource (or actual name), main, all relevant PRs and feature branches, latest accepted commit, working-tree conflicts, docs master, AGENTS/CLAUDE. Don't trust earlier chat statements as proof current code exists. Gather environment/build/DB/test evidence, route coverage, UI implementation, known blockers, changed files and PR tests. Produce truthful implementation status and next smallest unblocked phase with copy-ready prompt. Do not merge/push/change unrelated user repositories.

## D — Full release adversarial audit (read only)

Treat project as actual course software, not proposal. Test or inspect independently where tools permit:
1. Can all registered Jersey routes execute, not just appear in source?
2. Can all five tables persist through real MySQL and restart?
3. Does Resource lock prevent concurrent stock overbook?
4. Does Requirement lock/coverage prevent concurrent excess demand?
5. Can stale Resource corrections overwrite consume?
6. Can one Allocation be issued/returned/consumed twice?
7. Can cancelled/expired approvals still reserve?
8. Can overdue ISSUED equipment incorrectly become available?
9. Can Project dates change after historical approval?
10. Can physical deletion erase approvals/completed history?
11. Can JPQL SUM/null or flush timing produce wrong totals?
12. Can DB failure partially commit consumable deduction?
13. Can scheduler duplicate SMS claims after restart?
14. Can returned/revoked Allocation still be dispatched once HTTP started (document race)?
15. Can fake SMS accidentally log real credentials?
16. Can public responses expose destination?
17. Can frontend clients set computed IDs/status?
18. Can UI claim success after lost POST response?
19. Does PHT datetime-local preserve wall-time semantics?
20. Does UI survive reload/backend restart?
21. Does UI work keyboard and mobile?
22. Does packaged JAR serve /, /assets and /api?
23. Can Java source import into Eclipse-compatible tool without absolute paths?
24. Does source ZIP contain all build files/lockfile?
25. Does Postman route template map actual 26 paths and dynamic fixture?
26. Are all course technologies evidenced, not just compiled dependencies?
27. Are any unrun tests reported PASS?
28. Is video genuine?
29. Are all proposal amendments visible and scholarly assistance acknowledged?
30. Does source manifest overclaim complete raw ZIP or video review?
Report source, actual commands, PASS/FAIL/BLOCKED/NOT_RUN, exact defect severity/repro, remediation, verdict. No edits absent authorization.


---

# MASTER SECTION 18: AGENTS.md

# OrgResource — Repository rules for Codex and other coding agents

1. Read docs/ORGRESOURCE_MASTER_HANDOFF.md completely, and relevant original course assignment/syllabus/concise proposal and code, before changing interfaces. Source authority: assignment > syllabus/current verified Canvas > user decisions > original course examples > 25-route proposal > reviewed amendments.
2. User develops in VS Code; produce Eclipse-compatible Java source export, actual MySQL backend, real React UI.
3. Exactly five JPA entities: Department, Project, Resource, ResourceRequirement, Allocation. 25 original routes plus proposed A09 consent withdrawal. Do NOT add StockMovement, a unit field, expanded availability JSON, overdue reminder extension, fake Admin auth or second backend.
4. Java backend is course-compatible Java 8 Spring Boot/Jersey, Spring @Autowired components, JPA/Spring Data/Javax, Retrofit/Gson, @Scheduled, MySQL. Modern React/TypeScript/Vite frontend has independent Node toolchain.
5. Stock math and Allocation transitions defined in master. Database transaction is source of truth. No double consume, no overbook, no unsafe deletion, no fake DB effects.
6. Implement actual responsive UI with loading/empty/error/retry and keyboard/mobile states, real server data, no fabricated analytics.
7. Local-only fictional data, SMS disabled by default; no real contacts or provider credentials in logs, commits, screenshots or simulator.
8. Inspect git status/remotes and existing user edits. One writer per file/worktree. No unauthorized push/merge/reset/database drop/Canvas submission.
9. Every phase report actual files, commands, tests PASS/FAIL/BLOCKED/NOT_RUN, schema/HTTP/UI proof, warnings, diff, commit status and next gate. Never claim static code or sample JSON is runtime proof.
10. Stop on essential blocker; otherwise implement the smallest tested slice without repeatedly seeking permission for routine local edits.


---

# MASTER SECTION 19: CLAUDE.md

# OrgResource — Claude Code instructions

The single controlling source is docs/ORGRESOURCE_MASTER_HANDOFF.md.
Also read AGENTS.md and the actual current CSCI 180.05 assignment/syllabus/proposal.
Default Claude role: independent read-only reviewer and/or React/TypeScript/UI implementation in an isolated worktree; Codex default Java/backend/DB writer. Roles may be swapped explicitly.
Never edit Codex's working tree concurrently. Before edits inspect files/diffs/branch. Preserve original five-entity, 25+1 route contracts and Jersey/Javax course stack.
React UI must actually call the Java API, handle all status/errors/empty/loading/keyboard/mobile states, not show fake data. Do not create second backend/auth/DB.
Report executed tests and evidence honestly. No invented code/video/Canvas/GitHub state; no secrets, personal contacts, false campus adoption.
Do not merge/push/reset or perform destructive operations without authorization. If assigned review-only, do not modify files.


---

# MASTER SECTION 20: TEMPLATES/UNVERIFIED_BACKEND_POM_CANDIDATE.xml

~~~text
<?xml version="1.0" encoding="UTF-8"?>
<!-- PLANNING TEMPLATE ONLY. Not compiled, resolved, or executed. Confirm actual source-era JARs and Maven dependency tree first. -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
 <modelVersion>4.0.0</modelVersion>
 <parent><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-parent</artifactId><version>2.1.0.RELEASE</version><relativePath/></parent>
 <groupId>edu.ateneo.csci180</groupId><artifactId>orgresource</artifactId><version>0.1.0-SNAPSHOT</version><packaging>jar</packaging>
 <properties><java.version>1.8</java.version></properties>
 <dependencies>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-jersey</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.glassfish.jersey.media</groupId><artifactId>jersey-media-json-jackson</artifactId></dependency>
  <dependency><groupId>org.glassfish.jersey.ext</groupId><artifactId>jersey-bean-validation</artifactId></dependency>
  <dependency><groupId>mysql</groupId><artifactId>mysql-connector-java</artifactId><scope>runtime</scope></dependency>
  <dependency><groupId>com.squareup.retrofit2</groupId><artifactId>retrofit</artifactId><version>2.1.0</version></dependency>
  <dependency><groupId>com.squareup.retrofit2</groupId><artifactId>converter-gson</artifactId><version>2.1.0</version></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
 </dependencies>
 <build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build>
</project>

~~~


---

# MASTER SECTION 21: TEMPLATES/application.properties.example

~~~text
# TEMPLATE ONLY. Copy into actual backend after confirming JDK/DB/settings.
server.address=127.0.0.1
server.port=8080
spring.datasource.url=${ORG_DB_URL}
spring.datasource.username=${ORG_DB_USER}
spring.datasource.password=${ORG_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
orgresource.sms.mode=DISABLED
orgresource.reminder.scan-ms=3600000
orgresource.expiry.scan-ms=3600000
# Do not use destructive DDL modes for normal demo or store secrets here.

~~~


---

# MASTER SECTION 22: TEMPLATES/frontend_api_types.ts

~~~text
// Proposed public DTO types, not implemented/tested React source.
export type ResourceType = "REUSABLE" | "CONSUMABLE";
export type AllocationStatus = "PENDING" | "APPROVED" | "ISSUED" | "RETURNED" | "CONSUMED" | "CANCELLED";
export type ReminderOutcome = "NONE" | "CLAIMED" | "SUPPRESSED" | "SIMULATED" | "PROVIDER_ACCEPTED" | "REJECTED" | "UNKNOWN";
export type CancellationReason = "USER_REQUEST" | "EXPIRED";
export interface Department { id:number; name:string }
export interface Project { id:number; departmentId:number; name:string; startAt:string; endAt:string }
export interface Resource { id:number; name:string; type:ResourceType; totalQuantity:number; availableQuantity:number }
export interface ResourceAvailability { resourceId:number; availableQuantity:number }
export interface ResourceRequirement { id:number; projectId:number; resourceId:number; quantity:number; allocatedQuantity:number; shortageQuantity:number }
export interface Allocation {
  id:number; requirementId:number; quantity:number; borrowerName:string;
  contactPresent:boolean; smsConsent:boolean; status:AllocationStatus;
  createdAt:string; approvedAt:string|null; issuedAt:string|null;
  returnedAt:string|null; consumedAt:string|null; cancelledAt:string|null;
  cancellationReason:CancellationReason|null; expiredPendingCleanup:boolean;
  reminderAttempted:boolean; reminderOutcome:ReminderOutcome;
}
export interface ApiErrorBody { code:string; message:string }
export interface DeleteResponse { message:"Record deleted." }

~~~


---

# MASTER SECTION 23: TEMPLATES/frontend_api_client.ts

~~~text
// Candidate code only. Test with real Jersey API before relying on it.
export class ApiError extends Error {
  constructor(public status:number, public code:string, message:string) {
    super(message); this.name="ApiError";
  }
}
async function request<T>(path:string, method:"GET"|"POST", body?:unknown, signal?:AbortSignal):Promise<T> {
  const res=await fetch("/api"+path, {
    method, signal,
    headers: body===undefined ? {Accept:"application/json"} : {Accept:"application/json","Content-Type":"application/json"},
    body: body===undefined ? undefined : JSON.stringify(body)
  });
  const text=await res.text(); let parsed:unknown=null;
  if (text.length) {
    try { parsed=JSON.parse(text); }
    catch { throw new ApiError(res.status,"INVALID_SERVER_RESPONSE","Server returned invalid JSON."); }
  }
  if (!res.ok) {
    const err = parsed && typeof parsed==="object" ? parsed as Partial<{code:string,message:string}> : null;
    throw new ApiError(res.status,err?.code??"REQUEST_FAILED",err?.message??"Request failed.");
  }
  return parsed as T;
}
export function apiGet<T>(path:string,signal?:AbortSignal){return request<T>(path,"GET",undefined,signal);}
export function apiPost<T>(path:string,body?:unknown){return request<T>(path,"POST",body);}

~~~


---

# MASTER SECTION 24: TEMPLATES/vite.config.ts

~~~text
// Proposed Vite dev proxy only. Requires actual React/Vite package setup.
import {defineConfig} from "vite";
import react from "@vitejs/plugin-react";
export default defineConfig({
 plugins:[react()],
 server:{
   host:"127.0.0.1",
   port:5173,
   proxy:{"/api":{target:"http://127.0.0.1:8080",changeOrigin:true}}
 }
});

~~~


---

# MASTER SECTION 25: TEMPLATES/PHASE_REPORT.md

# OrgResource Phase Evidence Report (fill with ACTUAL results only)

Phase ID:
Goal:
Agent:
Source files actually inspected:
Inaccessible source files:
Repository URL/account:
Branch/worktree:
Base commit / head commit:
Git status/diff before:
Files created:
Files modified:
Files removed:
DB schema change:
API response/request change:
Frontend change:
Dependencies added/changed:
Security/privacy changes:
Build commands actually executed:
Exit codes and result:
JDK/Maven/Node/DB server versions:
Real DB queries executed and observed rows:
Real HTTP/Postman calls executed and observed status/JSON:
Real scheduler/Retrofit request observed:
Real browser UI observed:
Tests PASSED (names and output):
Tests FAILED (names and output):
Tests BLOCKED (reason):
Tests NOT_RUN:
Clean source import test PASS/FAIL/NOT_RUN:
Export ZIP path and SHA-256:
Video path or VIDEO NOT_CREATED:
Secrets scan:
Course requirement IDs:
Spec deviations, reasons and explicit approval:
Open risks/next action:
Final phase gate VERIFIED/FAILED/BLOCKED/NOT_RUN:
Do not claim an unrun action is done.


---

# MASTER SECTION 26: TEMPLATES/PROPOSAL_AMENDMENTS.md

# Revised-proposal amendment log (not evidence of submission or approval)

Baseline: 10_ORGRESOURCE_CONCISE_PROPOSAL.docx, 12 pages, 5 entities, 25 unique HTTP routes.

1. Keep five entity names and 25 original method/path pairs unchanged.
2. Resource creation does NOT add mandatory unit.
3. Resource availability response remains exactly resourceId + availableQuantity.
4. Resource update adds expectedTotalQuantity and stale-state 409 behavior; align component method and Postman.
5. Resource delete stricter: require totalQuantity=0 in addition to no Requirements.
6. Department/Resource use normalized nameKey unique DB index.
7. ResourceRequirement update lower bound includes largest live PENDING Allocation in addition to current effective coverage.
8. Allocation persistence adds lifecycle timestamps, approvedAt history, cancellation reason, durable SMS claim/outcome and provider response metadata.
9. Allocation output redacts phone; adds contactPresent, expiredPendingCleanup, reminderOutcome; reminderAttempted derived.
10. Allocation delete protects any historical approvedAt, even after cancellation.
11. Approval holds expire at Project end (behavioral change to original proposal's indefinitely reserved stock), expiry cleanup both PENDING and APPROVED.
12. Reminder stays in upcoming 24-hour window only, no overdue extension.
13. SMS modes disabled/fake/conditional virtual; no delivery claim; no actual provider token in simulator.
14. Add proposed A09 consent-withdrawal route only if retained; original 25 remain.
15. Public errors keep original message property and add stable code.
16. Maven build, VS Code workspace and React UI are user implementation decisions; still export Eclipse-compatible source.
17. Optional image upload/download only if final course interpretation requires; update routes to 28 if added.
18. Explain factual evidence limits: fictional use case, no interviews/adoption, user-approved outside assistance acknowledgment.
19. Update controller method descriptions, request/response JSON, errors, components, entity fields/repositories, Postman to one coherent contract. Do not claim proposal submitted/approved unless actual receipt exists.


---

# MASTER SECTION 27: TEMPLATES/DEMO_CHECKLIST.md

# Genuine skeleton and final demonstration checklist

SKELETON:
[ ] Working commit noted.
[ ] VS Code Java source visible.
[ ] Actual backend startup successful, no fatal errors.
[ ] Actual MySQL server connection and version observed.
[ ] Five JPA tables and FKs observed through SQL.
[ ] Fictional seed data verified if used.
[ ] Actual repository methods and query output shown.
[ ] Jersey controller + @Autowired component code shown.
[ ] Postman real create/list JSON demonstrated.
[ ] Eclipse-compatible source ZIP exists; import status honestly disclosed.
[ ] No credentials or personal data in footage.
[ ] Video file actually captured and playable; not simulated.

FINAL:
[ ] Complete running UI against real Java backend.
[ ] Departments/Projects/Resources/Requirements create/read/edit.
[ ] Allocation pending->approved->issued->returned.
[ ] Consumable approved->consumed; total decremented once.
[ ] Stale stock update 409 and UI refresh.
[ ] Real scheduled FakeTwilio request/outcome.
[ ] Invalid operation refused; DB unchanged.
[ ] Browser/backend restart persistence.
[ ] All-route Postman results and tests referenced accurately.
[ ] Built UI from packaged Java root / verified.
[ ] Complete source/evidence/ZIP/checksums.
[ ] Authorship and assistance acknowledgment prepared truthfully.
[ ] Canvas and instructor access manually verified.


---

# MASTER SECTION 28: .gitignore.example

~~~text
# EXAMPLE — review with actual repository before using
.env
.env.*
!.env.example
*.log
*.tmp
node_modules/
frontend/dist/
backend/target/
target/
.vscode/settings.json
.idea/
.DS_Store
Thumbs.db
*.class
*.jar
# Do not blindly ignore required Maven wrappers, source .project/.classpath/.settings,
# frontend package-lock.json or documented portable dependencies.
# Avoid committing SQL dumps with actual people/private data.

~~~


---

# MASTER SECTION 29: postman/README.md

# Postman starter artifacts — not runtime proof

The collection contains 26 original/proposed route definitions with request examples and simple response-status checks. It is explicitly NOT a safe full-order runner: create, edit, destructive delete, and mutually exclusive lifecycle commands need separate fixtures/ordering. In particular, an Allocation cannot both be CONSUMED and RETURNED; a Project created with future start cannot be immediately fulfilled; deleting a Resource with positive stock is refused. Agents must replace this starter with complete all-routes and demo collections, dynamic generated IDs, negative tests and real execution evidence. The front-end UI uses the same 26 routes.


---

# MASTER SECTION 30: postman/OrgResource-UNEXECUTED-26-Route-Template.postman_collection.json

~~~json
{
  "info": {
    "name": "OrgResource 26 Routes — UNEXECUTED API TEMPLATE",
    "description": "PLANNING TEMPLATE ONLY. Importable route definitions, not a tested or safe end-to-end runner. Destructive routes and mutually exclusive lifecycle routes cannot be executed consecutively on one record. Provide correct active Project times, separate disposable fixtures, and run actual DB/HTTP tests before acceptance.",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Departments",
      "item": [
        {
          "name": "D01 POST /api/departments",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Fictional Logistics {{runTag}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"departmentId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "D02 GET /api/departments",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "D03 POST /api/departments/{{departmentId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments/{{departmentId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Fictional Operations {{runTag}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "D04 POST /api/departments/{{departmentId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/departments/{{departmentId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"D04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Projects",
      "item": [
        {
          "name": "P01 POST /api/projects",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"departmentId\": {{departmentId}},\n  \"name\": \"Fictional Workshop {{runTag}}\",\n  \"startAt\": \"{{projectStartAt}}\",\n  \"endAt\": \"{{projectEndAt}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"projectId\",String(b.id));}"
                ]
              }
            },
            {
              "listen": "prerequest",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "function pht(d){return new Intl.DateTimeFormat(\"sv-SE\",{timeZone:\"Asia/Manila\",year:\"numeric\",month:\"2-digit\",day:\"2-digit\",hour:\"2-digit\",minute:\"2-digit\",second:\"2-digit\",hour12:false}).format(d).replace(\" \",\"T\");}",
                  "pm.environment.set(\"projectStartAt\",pht(new Date(Date.now()+3*60000)));",
                  "pm.environment.set(\"projectEndAt\",pht(new Date(Date.now()+4*60*60000)));"
                ]
              }
            }
          ]
        },
        {
          "name": "P02 GET /api/projects?departmentId={{departmentId}}",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects?departmentId={{departmentId}}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "P03 POST /api/projects/{{projectId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects/{{projectId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Workshop Updated {{runTag}}\",\n  \"startAt\": \"{{projectStartAt}}\",\n  \"endAt\": \"{{projectEndAt}}\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "P04 POST /api/projects/{{projectId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/projects/{{projectId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"P04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Resources",
      "item": [
        {
          "name": "R01 POST /api/resources",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Extension cords {{runTag}}\",\n  \"type\": \"REUSABLE\",\n  \"totalQuantity\": 10\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"resourceId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "R02 GET /api/resources?type=REUSABLE",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources?type=REUSABLE"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "R03 POST /api/resources/{{resourceId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources/{{resourceId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Extension cords {{runTag}}\",\n  \"totalQuantity\": 10,\n  \"expectedTotalQuantity\": 10\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "R04 POST /api/resources/{{resourceId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources/{{resourceId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "R05 GET /api/resources/{{resourceId}}/availability",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/resources/{{resourceId}}/availability"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"R05 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Requirements",
      "item": [
        {
          "name": "Q01 POST /api/requirements",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"projectId\": {{projectId}},\n  \"resourceId\": {{resourceId}},\n  \"quantity\": 6\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"requirementId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "Q02 GET /api/requirements?projectId={{projectId}}",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements?projectId={{projectId}}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "Q03 POST /api/requirements/{{requirementId}}/update",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements/{{requirementId}}/update"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"quantity\": 8\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "Q04 POST /api/requirements/{{requirementId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/requirements/{{requirementId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Q04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Allocations",
      "item": [
        {
          "name": "A01 POST /api/allocations",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations"
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"requirementId\": {{requirementId}},\n  \"quantity\": 4,\n  \"borrowerName\": \"Fictional Representative\",\n  \"smsConsent\": false\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A01 expected status 201\", function() { pm.response.to.have.status(201); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });",
                  "if (pm.response.code===201) {const b=pm.response.json();if(Number.isInteger(b.id)&&b.id>0)pm.environment.set(\"allocationId\",String(b.id));}"
                ]
              }
            }
          ]
        },
        {
          "name": "A02 GET /api/allocations?requirementId={{requirementId}}",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations?requirementId={{requirementId}}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A02 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A03 POST /api/allocations/{{allocationId}}/approve",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/approve"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A03 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A04 POST /api/allocations/{{allocationId}}/issue",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/issue"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A04 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A05 POST /api/allocations/{{allocationId}}/return",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/return"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A05 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A06 POST /api/allocations/{{allocationId}}/consume",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/consume"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A06 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A07 POST /api/allocations/{{allocationId}}/cancel",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/cancel"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A07 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A08 POST /api/allocations/{{allocationId}}/delete",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/delete"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A08 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        },
        {
          "name": "A09 POST /api/allocations/{{allocationId}}/revoke-sms-consent",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Accept",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/allocations/{{allocationId}}/revoke-sms-consent"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"A09 expected status 200\", function() { pm.response.to.have.status(200); });",
                  "pm.test(\"JSON response\",function(){ pm.expect(()=>pm.response.json()).not.to.throw(); });"
                ]
              }
            }
          ]
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://127.0.0.1:8080"
    }
  ]
}
~~~


---

# MASTER SECTION 31: postman/OrgResource-Local-Template.postman_environment.json

~~~json
{
  "name": "OrgResource Local \u2014 PLACEHOLDERS / NO SECRETS",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://127.0.0.1:8080",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "runTag",
      "value": "DEMO",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "departmentId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "projectId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "resourceId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "requirementId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "allocationId",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "projectStartAt",
      "value": "",
      "enabled": true,
      "type": "default"
    },
    {
      "key": "projectEndAt",
      "value": "",
      "enabled": true,
      "type": "default"
    }
  ],
  "_postman_variable_scope": "environment"
}
~~~


---

# MASTER SECTION 32: MATRICES/ALL_26_ROUTES.csv

~~~text
route_id,group,http_method,path,success_status,body_example,template_unexecuted
D01,Departments,POST,/api/departments,201,"{
  ""name"": ""Fictional Logistics {{runTag}}""
}",True
D02,Departments,GET,/api/departments,200,,True
D03,Departments,POST,/api/departments/{{departmentId}}/update,200,"{
  ""name"": ""Fictional Operations {{runTag}}""
}",True
D04,Departments,POST,/api/departments/{{departmentId}}/delete,200,,True
P01,Projects,POST,/api/projects,201,"{
  ""departmentId"": {{departmentId}},
  ""name"": ""Fictional Workshop {{runTag}}"",
  ""startAt"": ""{{projectStartAt}}"",
  ""endAt"": ""{{projectEndAt}}""
}",True
P02,Projects,GET,/api/projects?departmentId={{departmentId}},200,,True
P03,Projects,POST,/api/projects/{{projectId}}/update,200,"{
  ""name"": ""Workshop Updated {{runTag}}"",
  ""startAt"": ""{{projectStartAt}}"",
  ""endAt"": ""{{projectEndAt}}""
}",True
P04,Projects,POST,/api/projects/{{projectId}}/delete,200,,True
R01,Resources,POST,/api/resources,201,"{
  ""name"": ""Extension cords {{runTag}}"",
  ""type"": ""REUSABLE"",
  ""totalQuantity"": 10
}",True
R02,Resources,GET,/api/resources?type=REUSABLE,200,,True
R03,Resources,POST,/api/resources/{{resourceId}}/update,200,"{
  ""name"": ""Extension cords {{runTag}}"",
  ""totalQuantity"": 10,
  ""expectedTotalQuantity"": 10
}",True
R04,Resources,POST,/api/resources/{{resourceId}}/delete,200,,True
R05,Resources,GET,/api/resources/{{resourceId}}/availability,200,,True
Q01,Requirements,POST,/api/requirements,201,"{
  ""projectId"": {{projectId}},
  ""resourceId"": {{resourceId}},
  ""quantity"": 6
}",True
Q02,Requirements,GET,/api/requirements?projectId={{projectId}},200,,True
Q03,Requirements,POST,/api/requirements/{{requirementId}}/update,200,"{
  ""quantity"": 8
}",True
Q04,Requirements,POST,/api/requirements/{{requirementId}}/delete,200,,True
A01,Allocations,POST,/api/allocations,201,"{
  ""requirementId"": {{requirementId}},
  ""quantity"": 4,
  ""borrowerName"": ""Fictional Representative"",
  ""smsConsent"": false
}",True
A02,Allocations,GET,/api/allocations?requirementId={{requirementId}},200,,True
A03,Allocations,POST,/api/allocations/{{allocationId}}/approve,200,,True
A04,Allocations,POST,/api/allocations/{{allocationId}}/issue,200,,True
A05,Allocations,POST,/api/allocations/{{allocationId}}/return,200,,True
A06,Allocations,POST,/api/allocations/{{allocationId}}/consume,200,,True
A07,Allocations,POST,/api/allocations/{{allocationId}}/cancel,200,,True
A08,Allocations,POST,/api/allocations/{{allocationId}}/delete,200,,True
A09,Allocations,POST,/api/allocations/{{allocationId}}/revoke-sms-consent,200,,True

~~~


---

# MASTER SECTION 33: MATRICES/ACCEPTANCE_TESTS.csv

~~~text
test_id,category,acceptance_goal,planned_status,observed_command,observed_result
BE01,backend,five JPA schema tables startup,NOT_RUN,,
BE02,backend,Department persisted 201,NOT_RUN,,
BE03,backend,Department GET list,NOT_RUN,,
BE04,backend,normalized duplicate conflict,NOT_RUN,,
BE05,backend,referenced Department delete refused,NOT_RUN,,
BE06,backend,invalid Project date rejected,NOT_RUN,,
BE07,backend,missing department FK rejected,NOT_RUN,,
BE08,backend,name only Project update with past unchanged dates,NOT_RUN,,
BE09,backend,historically approved Project date edit rejected,NOT_RUN,,
BE10,backend,zero stock Resource available zero,NOT_RUN,,
BE11,backend,approved hold reduces available,NOT_RUN,,
BE12,backend,parallel Resource approvals no overbook,NOT_RUN,,
BE13,backend,parallel Requirement approvals no overcover,NOT_RUN,,
BE14,backend,pending request no hold,NOT_RUN,,
BE15,backend,issue no double reserve,NOT_RUN,,
BE16,backend,return restores custody,NOT_RUN,,
BE17,backend,repeat return refused,NOT_RUN,,
BE18,backend,consume decrements once,NOT_RUN,,
BE19,backend,repeat consume refused,NOT_RUN,,
BE20,backend,Requirement reduction below coverage refused,NOT_RUN,,
BE21,backend,Requirement reduction below pending refused,NOT_RUN,,
BE22,backend,stale Resource correction conflict,NOT_RUN,,
BE23,backend,expired approved effective hold zero before cleanup,NOT_RUN,,
BE24,backend,expired pending cleanup,NOT_RUN,,
BE25,backend,expired approved cleanup,NOT_RUN,,
BE26,backend,overdue issued custody stays unavailable,NOT_RUN,,
BE27,backend,approved then cancelled deletion refused,NOT_RUN,,
BE28,backend,never approved deletion succeeds,NOT_RUN,,
BE29,backend,positive stock Resource delete refused,NOT_RUN,,
BE30,backend,zero stock unreferenced delete,NOT_RUN,,
BE31,backend,unique requirement pair conflict,NOT_RUN,,
BE32,backend,forced SQL error consumption rollback,NOT_RUN,,
BE33,backend,empty aggregates zero,NOT_RUN,,
BE34,backend,unknown FK 404 no save,NOT_RUN,,
BE35,backend,malformed JSON 400,NOT_RUN,,
BE36,backend,protected client field rejected/safe ignore,NOT_RUN,,
BE37,backend,restart persisted records,NOT_RUN,,
BE38,backend,clean import,NOT_RUN,,
BE39,backend,parallel duplicate name DB unique,NOT_RUN,,
BE40,backend,Project edit vs approval race,NOT_RUN,,
BE41,backend,Resource correction vs consume race,NOT_RUN,,
BE42,backend,time-boundary read consistency,NOT_RUN,,
BE43,backend,consistent read-only report or documented snapshot,NOT_RUN,,
BE44,backend,lost response no automatic retry,NOT_RUN,,
SMS01,sms,disabled startup,NOT_RUN,,
SMS02,sms,expiry independent,NOT_RUN,,
SMS03,sms,24h window edges,NOT_RUN,,
SMS04,sms,claim only once,NOT_RUN,,
SMS05,sms,return before claim,NOT_RUN,,
SMS06,sms,revocation before claim,NOT_RUN,,
SMS07,sms,return after claim before dispatch,NOT_RUN,,
SMS08,sms,simulator accepted typed reply,NOT_RUN,,
SMS09,sms,definitive reject,NOT_RUN,,
SMS10,sms,timeout UNKNOWN,NOT_RUN,,
SMS11,sms,restart no resend,NOT_RUN,,
SMS12,sms,overdue suppress,NOT_RUN,,
SMS13,sms,provider accepted not delivery,NOT_RUN,,
SMS14,sms,missing destination suppress,NOT_RUN,,
SMS15,sms,dummy auth only,NOT_RUN,,
SMS16,sms,result record not overwrite RETURNED,NOT_RUN,,
SMS17,sms,expired pending no inventory change,NOT_RUN,,
SMS18,sms,expired approved no double release,NOT_RUN,,
UI01,ui,empty DB,NOT_RUN,,
UI02,ui,Department real create,NOT_RUN,,
UI03,ui,PHT datetime roundtrip,NOT_RUN,,
UI04,ui,Resource availability,NOT_RUN,,
UI05,ui,Requirement shortage,NOT_RUN,,
UI06,ui,PENDING stock unchanged,NOT_RUN,,
UI07,ui,APPROVE stock reserve,NOT_RUN,,
UI08,ui,ISSUE no double deduction,NOT_RUN,,
UI09,ui,RETURN stock restore,NOT_RUN,,
UI10,ui,CONSUME stock once,NOT_RUN,,
UI11,ui,stale update 409,NOT_RUN,,
UI12,ui,unsafe delete 409,NOT_RUN,,
UI13,ui,revoke consent,NOT_RUN,,
UI14,ui,browser reload,NOT_RUN,,
UI15,ui,backend restart,NOT_RUN,,
UI16,ui,backend outage error,NOT_RUN,,
UI17,ui,keyboard support,NOT_RUN,,
UI18,ui,mobile viewport,NOT_RUN,,
UI19,ui,real backend browser E2E,NOT_RUN,,
UI20,ui,bundled JAR UI,NOT_RUN,,
UI21,ui,dashboard partial failure,NOT_RUN,,
UI22,ui,lost POST response,NOT_RUN,,
UI23,ui,actions by state/type,NOT_RUN,,
UI24,ui,responsive table/dialog focus,NOT_RUN,,
S01,skeleton,source exists,NOT_RUN,,
S02,skeleton,Java compiles,NOT_RUN,,
S03,skeleton,startup no fatal error,NOT_RUN,,
S04,skeleton,real MySQL connection,NOT_RUN,,
S05,skeleton,five JPA tables and FKs,NOT_RUN,,
S06,skeleton,fictional test rows,NOT_RUN,,
S07,skeleton,repository query runs,NOT_RUN,,
S08,skeleton,controller registration,NOT_RUN,,
S09,skeleton,Autowired delegation,NOT_RUN,,
S10,skeleton,JSON live HTTP,NOT_RUN,,
S11,skeleton,runtime create/delete,NOT_RUN,,
S12,skeleton,restart persists,NOT_RUN,,
S13,skeleton,Eclipse source ZIP,NOT_RUN,,
S14,skeleton,actual Eclipse import status,NOT_RUN,,
S15,skeleton,real video evidence,NOT_RUN,,
S16,skeleton,no secrets,NOT_RUN,,
S17,skeleton,source and checksums,NOT_RUN,,
S18,skeleton,Canvas checked,NOT_RUN,,

~~~

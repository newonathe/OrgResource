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

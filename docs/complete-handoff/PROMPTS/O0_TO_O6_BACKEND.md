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

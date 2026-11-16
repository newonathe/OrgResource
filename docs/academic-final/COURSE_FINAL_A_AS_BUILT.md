# OrgResource Course Final A — As-Built Proposal

**Document type:** technical as-built proposal and implementation reference
**Project:** OrgResource — Student Organization Resource Planning and Allocation System
**Course:** CSCI 180.05
**Prepared from:** current source tree, API contracts, frozen Course A specification, and observed acceptance evidence
**Academic status:** technical evidence only; student and instructor actions remain open where identified below

## 1. Purpose, provenance, and status language

This document reconciles the original concise proposal with the application that was actually implemented. It is a current technical companion, not a replacement for, or proof of submission or approval of, the original proposal. The original proposal at ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx is preserved unchanged.

The original proposal describes five entities and 25 operations. The current academic implementation retains those five entities and 25 operations, separately records A09 as an added consent-revocation operation, and adds two image operations authorized by the Course A specification. Therefore the current HTTP contract is 28 operations: 25 proposal operations + A09 + two image operations. Older nine-entity/68-operation documents are historical and do not describe the current build.

The source proposal lists one group member, but this document does not recertify a group roster or identify a current group. The student must verify the actual submitted proposal and current group membership. No authorship certificate, signature, instructor decision, Canvas receipt, video, peer review, provider delivery, or presentation is asserted here.

Evidence labels used in the release record:

- COURSE REQUIREMENT: wording in the assignment or syllabus.
- VERIFIED IMPLEMENTATION: behavior or source structure directly inspected.
- VERIFIED TEST RESULT: a command was executed and its result recorded.
- USER DECISION: an authorized scope choice.
- PROVISIONAL: interpretation or recommendation awaiting human confirmation.
- NOT_RUN: required check was not executed.
- NOT_VERIFIED: evidence does not establish the claim.
- MANUAL STUDENT ACTION: must be performed by the student.

The final per-command results and artifact identities are in evidence/COURSE-A/A3_TECHNICAL_GATE.md and the external A3 release receipt. Local acceptance is not instructor acceptance.

## 2. Problem and intended use

A student organization needs to record departments, projects, resources, project requirements, and allocation history. OrgResource supports that limited workflow. An officer can create a project requirement for a resource, record allocation requests, approve an allocation when stock and project demand allow it, issue and return reusable items, consume consumable items, and inspect current availability and requirement shortage.

The project is a local academic prototype. It does not claim institutional sponsorship, adoption, or production suitability. It has no real authentication or authorization, multi-tenant isolation, public deployment, or real SMS delivery. Use only fictional demonstration records.

## 3. Scope and architecture

### 3.1 Current stack

- Java 8 source and bytecode target.
- Spring Boot 2.1 application.
- Jersey/JAX-RS javax REST controllers.
- Spring-managed controller-to-component delegation using @Autowired.
- Spring Data JPA and Hibernate persistence.
- MySQL with InnoDB.
- Javax Bean Validation on request DTOs, with explicit request validation in controller code.
- JPQL and derived Spring Data repository methods.
- Retrofit, Gson, and OkHttp for the optional Twilio Messages protocol transport.
- Spring scheduling with an injectable Asia/Manila Clock.
- React, TypeScript, and Vite frontend bundled into the same executable JAR.
- Postman Collection v2.1 reference and Newman execution collections.
- Maven Wrapper for the Java build and npm lockfile for the frontend build.

### 3.2 Runtime boundaries

The executable Java application serves the React single-page interface at / and JSON REST APIs under /api/. The local launcher binds to 127.0.0.1:8080. MySQL remains the authority for persistent project, stock, and allocation state. The frontend does not calculate authoritative inventory.

The eight frontend screens are Overview, Allocations, Resource Library, Projects, Requirements, Departments, Reminder Activity, and About. The interface has no image-upload workflow; Course A image receive/serve behavior is implemented at the Resource HTTP API.

### 3.3 Five core entities and field dictionary

All identifiers are generated Long primary keys. The table names below are the five JPA domain tables. DTOs, enums, repositories, schedulers, and helper classes are not additional JPA entities.

| Entity / table | Persisted fields and Java types | Constraints and meaning |
|---|---|---|
| Department / department | id Long; name String(100); nameKey String(100) | Identity primary key. name and name_key are non-null. name_key is unique and indexed. Display name is normalized before persistence; nameKey supports normalized uniqueness. |
| Project / project | id Long; department Department; name String(100); startAt LocalDateTime; endAt LocalDateTime | Identity primary key. department_id is a non-null FK to department. name, start_at, end_at are non-null. Period requires endAt after startAt; creation and actual date changes require a future start. |
| Resource / resource | id Long; name String(100); nameKey String(100); type ResourceType; totalQuantity Integer; imageBytes byte[]; imageContentType String(16) | Identity primary key. name/name_key are non-null; name_key unique/indexed. type is enum text and immutable after creation. total_quantity is a non-null whole-unit value, API range 0–1,000,000. Optional image bytes use MEDIUMBLOB on this same row; image_content_type is nullable. |
| ResourceRequirement / resource_requirement | id Long; project Project; resource Resource; quantity Integer | Identity primary key. project_id and resource_id are non-null FKs. quantity is a non-null whole-unit value, API range 1–1,000,000. Unique constraint permits one Requirement per Project/Resource pair. |
| Allocation / allocation | id Long; requirement ResourceRequirement; quantity Integer; borrowerName String(100); phoneNumber String(20); smsConsent Boolean; status AllocationStatus; createdAt, approvedAt, issuedAt, returnedAt, consumedAt, cancelledAt LocalDateTime; cancellationReason CancellationReason; reminderClaimedAt LocalDateTime; reminderOutcome ReminderOutcome; reminderHttpStatus Integer; providerMessageSid String(64) | Identity primary key. requirement_id and required core fields are non-null. quantity is a positive whole number. Phone is optional and omitted from public DTOs. Consent defaults false, status PENDING, reminder outcome NONE. Timestamps and outcomes record state/history; a prior approvedAt protects history after cancellation. |

### 3.4 Entity relationship diagram

| Parent | Relationship | Child | Database rule |
|---|---:|---|---|
| Department | 1 to many | Project | project.department_id references department.id; deleting a referenced Department is refused. |
| Project | 1 to many | ResourceRequirement | resource_requirement.project_id references project.id; deleting a referenced Project is refused. |
| Resource | 1 to many | ResourceRequirement | resource_requirement.resource_id references resource.id; deleting a referenced Resource is refused. |
| Project and Resource | one pair to one Requirement | ResourceRequirement | Unique constraint on (project_id, resource_id). |
| ResourceRequirement | 1 to many | Allocation | allocation.requirement_id references resource_requirement.id; Requirements with Allocation history cannot be deleted. |

The only image-storage extension is nullable image content on Resource. It adds no sixth entity and no image table.

### 3.5 Indexes, uniqueness, and persistence safeguards

The source declares indexes for Department/Resource normalized keys, Project department lookup, Requirement project/resource foreign-key lookups, and Allocation requirement, status, and created-at query predicates. Unique constraints protect Department nameKey, Resource nameKey, and the Project/Resource Requirement pair. Foreign keys protect all four relationships. Deletes are guarded in components and by database foreign keys. Multi-row allocation transitions use transactions and explicit row locks; failed transitions do not intentionally leave partial stock changes.

## 4. Inventory and lifecycle rules

All quantities are integer whole units. Availability, covered quantity, and shortage are derived at read time; they are not duplicated as stored totals.

- Reusable available quantity = physical Resource total − effective APPROVED holds − outstanding ISSUED custody.
- Consumable available quantity = physical Resource total − effective APPROVED holds.
- An APPROVED hold is effective only while its Project endAt is later than the single sampled Manila-local now.
- PENDING and CANCELLED allocations do not reserve stock.
- Approval reserves stock once. Issuing reusable stock transfers it to outstanding custody and does not deduct it a second time.
- Returning reusable stock marks the complete batch usable and releases outstanding custody. Partial or damaged returns are not modeled.
- Consuming an approved consumable allocation decreases physical total exactly once in the same transaction that marks it CONSUMED.
- Requirement coverage counts ISSUED, RETURNED, and CONSUMED quantities. APPROVED counts only while the Project is active. PENDING and CANCELLED do not count.
- Requirement shortage = max(0, requirement quantity − effective covered quantity).
- A live pending request is checked as a maximum single pending quantity when a Requirement is edited; pending requests are not summed as stock reservations.
- Resource quantity edits use expectedTotalQuantity to detect a stale displayed total and may not lower stock below current holds and outstanding reusable custody.
- Deletes are protected where dependencies or approved history exist. Permitted deletion returns {"message":"Record deleted."}.
- The application samples the injected Asia/Manila Clock for date-sensitive decisions. Database LocalDateTime values represent local wall time; the API does not claim UTC conversion.

## 5. API contract — all 28 operations

All paths below are relative to http://127.0.0.1:8080. JSON operations use application/json except IMAGE-01 multipart upload and IMAGE-02 binary retrieval. Controllers delegate to the matching Spring component. Create requests return 201. Other successful JSON reads, updates, state transitions, and permitted deletes return 200. Lists return arrays, including [] when empty. JSON errors use {code, message}. Path IDs must be positive where the component validates them.

### 5.1 Departments — D01 through D04

| ID | Method and path | Request | Component delegation | Success and material failures |
|---|---|---|---|---|
| D01 | POST /api/departments | {"name":"Logistics"} | DepartmentComponent.create(name) | 201 DepartmentView {id,name}; 400 invalid/blank/overlong name; 409 DUPLICATE_NAME. |
| D02 | GET /api/departments | No body or query | DepartmentComponent.list() | 200 DepartmentView[]; empty result is []. |
| D03 | POST /api/departments/{id}/update | {"name":"Operations"} | DepartmentComponent.update(id,name) | 200 DepartmentView; 400 invalid ID/name; 404 NOT_FOUND; 409 DUPLICATE_NAME. |
| D04 | POST /api/departments/{id}/delete | No body | DepartmentComponent.delete(id) | 200 {"message":"Record deleted."}; 404 NOT_FOUND; 409 DEPENDENCY_EXISTS if Projects refer to the Department. |

### 5.2 Projects — P01 through P04

Project date strings use LocalDateTime without a zone, for example 2026-12-10T09:00:00.

| ID | Method and path | Request | Component delegation | Success and material failures |
|---|---|---|---|---|
| P01 | POST /api/projects | {"departmentId":1,"name":"Workshop","startAt":"2026-12-10T09:00:00","endAt":"2026-12-10T12:00:00"} | ProjectComponent.create(departmentId,name,startAt,endAt) | 201 ProjectView {id,departmentId,name,startAt,endAt}; 400 invalid name/period or start not future; 404 missing Department. |
| P02 | GET /api/projects?departmentId=1 | Optional positive departmentId | ProjectComponent.list(departmentId) | 200 ProjectView[]; empty result is []. Nonpositive filter is 400. |
| P03 | POST /api/projects/{id}/update | {"name":"Workshop updated","startAt":"2026-12-10T09:00:00","endAt":"2026-12-10T12:00:00"} | ProjectComponent.update(id,name,startAt,endAt) | 200 ProjectView; 400 invalid ID/name/period; 404 NOT_FOUND; 409 PROJECT_DATES_LOCKED if dates change after any approval. Department reference is unchanged. Name-only update may preserve historical dates. |
| P04 | POST /api/projects/{id}/delete | No body | ProjectComponent.delete(id) | 200 deletion message; 400 nonpositive ID; 404 NOT_FOUND; 409 DEPENDENCY_EXISTS if Requirements exist. |

### 5.3 Resources — R01 through R05

Resource type is REUSABLE or CONSUMABLE. There is no unit field.

| ID | Method and path | Request | Component delegation | Success and material failures |
|---|---|---|---|---|
| R01 | POST /api/resources | {"name":"Extension cords","type":"REUSABLE","totalQuantity":10} | ResourceComponent.create(name,type,totalQuantity) | 201 ResourceView {id,name,type,totalQuantity,availableQuantity}; 400 invalid type/name/quantity; 409 DUPLICATE_NAME. |
| R02 | GET /api/resources?type=REUSABLE | Optional ResourceType filter | ResourceComponent.list(type) | 200 ResourceView[] with server-derived availability; empty result is []; invalid enum is 400. |
| R03 | POST /api/resources/{id}/update | {"name":"Extension cords","totalQuantity":12,"expectedTotalQuantity":10} | ResourceComponent.update(id,name,totalQuantity,expectedTotalQuantity) | 200 ResourceView; 400 invalid ID/name/quantity; 404 NOT_FOUND; 409 DUPLICATE_NAME, STALE_RESOURCE_QUANTITY, or QUANTITY_BELOW_COMMITMENTS. Resource type stays immutable. |
| R04 | POST /api/resources/{id}/delete | No body | ResourceComponent.delete(id) | 200 deletion message; 400 nonpositive ID; 404 NOT_FOUND; 409 DEPENDENCY_EXISTS if a Requirement refers to it or RESOURCE_NOT_EMPTY when totalQuantity is positive. |
| R05 | GET /api/resources/{id}/availability | No body | ResourceComponent.getAvailability(id) | 200 exactly {resourceId,availableQuantity}; 400 nonpositive ID; 404 NOT_FOUND. Stored invariant failure is sanitized as 500 INTERNAL_ERROR. |

### 5.4 Requirements — Q01 through Q04

| ID | Method and path | Request | Component delegation | Success and material failures |
|---|---|---|---|---|
| Q01 | POST /api/requirements | {"projectId":2,"resourceId":3,"quantity":6} | RequirementComponent.create(projectId,resourceId,quantity) | 201 RequirementView {id,projectId,resourceId,quantity,allocatedQuantity,shortageQuantity}; 400 invalid IDs/quantity; 404 missing Project or Resource; 409 PROJECT_ENDED or DUPLICATE_REQUIREMENT. |
| Q02 | GET /api/requirements?projectId=2 | Optional positive projectId | RequirementComponent.list(projectId) | 200 RequirementView[] with current coverage and shortage; empty result is []; nonpositive filter is 400. |
| Q03 | POST /api/requirements/{id}/update | {"quantity":8} | RequirementComponent.update(id,quantity) | 200 RequirementView; 400 invalid ID/quantity; 404 NOT_FOUND; 409 PROJECT_ENDED or QUANTITY_BELOW_COMMITMENTS if below effective coverage or the largest live PENDING request. Parent references do not change. |
| Q04 | POST /api/requirements/{id}/delete | No body | RequirementComponent.delete(id) | 200 deletion message; 400 nonpositive ID; 404 NOT_FOUND; 409 DEPENDENCY_EXISTS if Allocation history exists. |

### 5.5 Allocations — A01 through A09

Public AllocationView exposes contactPresent, smsConsent, lifecycle timestamps, cancellation reason, expiredPendingCleanup, reminderAttempted, and reminderOutcome. It never exposes phoneNumber or provider credentials.

| ID | Method and path | Request | Component delegation | Success and material failures |
|---|---|---|---|---|
| A01 | POST /api/allocations | {"requirementId":4,"quantity":4,"borrowerName":"Fictional representative","smsConsent":false}; phoneNumber and smsConsent are optional | AllocationComponent.create(requirementId,quantity,borrowerName,phoneNumber,smsConsent) | 201 PENDING AllocationView; 400 invalid fields, quantity above Requirement, or consent without valid E.164 destination; 404 missing Requirement; 409 PROJECT_ENDED. No stock reservation. Phone is stored only when consent is true. |
| A02 | GET /api/allocations?requirementId=4&status=PENDING | Both filters optional and conjunctive | AllocationComponent.list(requirementId,status) | 200 redacted AllocationView[]; empty result is []; invalid ID/status is 400. |
| A03 | POST /api/allocations/{id}/approve | No body | AllocationComponent.approve(id) | 200 APPROVED AllocationView; 400 invalid ID; 404 NOT_FOUND; 409 invalid state, ended Project, already-approved history, over-coverage, or insufficient stock. Approval creates one effective hold. |
| A04 | POST /api/allocations/{id}/issue | No body | AllocationComponent.issue(id) | 200 ISSUED AllocationView; 400 invalid ID; 404 NOT_FOUND; 409 invalid state, non-reusable Resource, or outside active Project period. Issue does not double-deduct availability. |
| A05 | POST /api/allocations/{id}/return | No body | AllocationComponent.returnItems(id) | 200 RETURNED AllocationView; 400 invalid ID; 404 NOT_FOUND; 409 invalid state or non-reusable Resource. Full-batch return is allowed after Project end. |
| A06 | POST /api/allocations/{id}/consume | No body | AllocationComponent.consume(id) | 200 CONSUMED AllocationView; 400 invalid ID; 404 NOT_FOUND; 409 invalid state, non-consumable Resource, outside active period, or insufficient physical stock. Stock decrement and status change are transactional. |
| A07 | POST /api/allocations/{id}/cancel | No body | AllocationComponent.cancel(id) | 200 CANCELLED AllocationView with USER_REQUEST or EXPIRED reason based on Project end; 400 invalid ID; 404 NOT_FOUND; 409 unless currently PENDING or APPROVED. |
| A08 | POST /api/allocations/{id}/delete | No body | AllocationComponent.delete(id) | 200 deletion message; 400 invalid ID; 404 NOT_FOUND; 409 INVALID_ALLOCATION_STATE or HISTORY_PROTECTED. Only never-approved PENDING/CANCELLED records can be deleted. |
| A09 | POST /api/allocations/{id}/revoke-sms-consent | No body | AllocationComponent.revokeSmsConsent(id) | 200 updated redacted AllocationView; 400 invalid ID; 404 NOT_FOUND. Clears consent and phoneNumber; lifecycle and inventory do not change. This is a separately recorded addition to the original 25 operations. |

### 5.6 Resource image HTTP operations — IMAGE-01 and IMAGE-02

| ID | Method and path | Request | Component delegation | Success and material failures |
|---|---|---|---|---|
| IMAGE-01 | POST /api/resources/{id}/image | multipart/form-data with one file part named file | ResourceController performs bounded multipart read, then ResourceComponent.storeImage(id,bytes,contentType) | 200 JSON {resourceId,contentType,sizeBytes,sha256}; 400 invalid ID/part/media/content; 404 missing Resource; 413 over 2 MiB. Only verified PNG/JPEG are accepted. |
| IMAGE-02 | GET /api/resources/{id}/image | Accept image/png or image/jpeg | ResourceComponent.getImage(id) | 200 raw stored bytes with validated Content-Type, Cache-Control no-store, X-Content-Type-Options nosniff; 400 invalid ID; 404 missing Resource/image. |

### 5.7 Common HTTP behavior

Request DTO constraints use Javax Validation, and controllers call RequestValidation. Controlled failures return JSON with a stable code and safe message. Validation and malformed input use 400; missing targets use 404; known state, uniqueness, dependency, and stock conflicts use 409; oversized image body uses 413; unexpected server/invariant failures are sanitized as 500. Create returns 201. Other successful JSON operations return 200. A permitted delete returns {"message":"Record deleted."}. No route serializes a JPA entity graph.

## 6. Custom repository methods as implemented

In addition to JpaRepository methods, the current source declares 28 custom repository methods. This list is derived from the current interfaces, not the older EERD handoff.

### DepartmentRepository

- boolean existsByNameKey(String nameKey)

### ProjectRepository

- List<Project> findByDepartment_Id(Long departmentId)
- boolean existsByDepartment_Id(Long departmentId)
- Optional<Project> lockById(Long id)

### ResourceRepository

- boolean existsByNameKey(String nameKey)
- boolean existsByNameKeyAndIdNot(String nameKey, Long id)
- List<Resource> findByType(ResourceType type)
- List<Object[]> findListSummaries(ResourceType type)
- Optional<Resource> lockById(Long id)

### ResourceRequirementRepository

- List<ResourceRequirement> findByProject_Id(Long projectId)
- boolean existsByProject_Id(Long projectId)
- boolean existsByResource_Id(Long resourceId)
- boolean existsByProject_IdAndResource_Id(Long projectId, Long resourceId)
- Optional<ResourceRequirement> lockById(Long id)

### AllocationRepository

- List<Allocation> findByRequirement_Id(Long requirementId)
- List<Allocation> findByStatus(AllocationStatus status)
- List<Allocation> findByRequirement_IdAndStatus(Long requirementId, AllocationStatus status)
- boolean existsByRequirement_Id(Long requirementId)
- Optional<Allocation> lockById(Long id)
- Long countHistoricallyApproved(Long projectId)
- Integer maxPendingQuantity(Long requirementId, AllocationStatus pending)
- Long sumCoveredQuantity(Long requirementId, List<AllocationStatus> completedStatuses, AllocationStatus approved, LocalDateTime now)
- Long sumEffectiveHolds(Long resourceId, AllocationStatus approved, LocalDateTime now)
- Long sumIssuedCustody(Long resourceId, AllocationStatus issued)
- List<Object[]> sumResourceCommitments(List<Long> resourceIds, AllocationStatus approved, AllocationStatus issued, LocalDateTime now, Collection<AllocationStatus> countedStatuses)
- List<Long> findExpiredCandidateIds(Collection<AllocationStatus> openStatuses, LocalDateTime now, Pageable pageable)
- List<Long> findReminderCandidateIds(AllocationStatus issued, ResourceType reusable, ReminderOutcome neverAttempted, LocalDateTime now, LocalDateTime within24Hours, Pageable pageable)
- List<Long> findStaleReminderClaimIds(ReminderOutcome claimed, LocalDateTime cutoff, Pageable pageable)

All declarations and annotations are in backend/src/main/java/app/repositories. AllocationRepository aggregate methods use JPQL; bounded scheduler queries use Pageable. The signatures above preserve parameter and return types.

## 7. Component sequences and transaction boundaries

- Department create/update normalize display name and key, enforce normalized uniqueness, and persist through DepartmentRepository. Delete checks Project dependency first.
- Project create validates normalized name and future ordered dates, resolves Department, then persists. Update locks Project; date changes require a future start and are refused after historical approval. Delete locks and checks Requirements.
- Resource list uses a scalar projection and grouped commitment aggregate so it does not load image bytes or repeat allocation scans. Availability derives from physical stock and commitments. Update locks Resource, checks expected total, uniqueness, and commitments before saving. Delete locks, checks Requirement dependency, then zero stock.
- Requirement create locks Project then Resource, checks Project end and unique pair, then saves. Update locks Project, Resource, and Requirement in that order, calculates effective coverage and largest live pending request, and refuses an invalid decrease. Delete checks Allocation history.
- Allocation create resolves parent IDs and locks Project, Resource, then Requirement. State transitions use the common Project → Resource → Requirement → Allocation order. Approval checks Project, requirement coverage, and resource availability before setting APPROVED/approvedAt. Issue changes reusable custody state. Return releases custody. Consume updates Resource total and Allocation status in one transaction. Cancel only accepts PENDING/APPROVED. Delete preserves any record ever approved. Consent revocation clears phone and consent without changing lifecycle.
- Image upload reads no more than the byte bound before storage; the Resource row is locked and receives validated bytes and media type transactionally. Retrieval returns stored bytes.
- External HTTP does not run inside the database transaction that claims or persists a reminder outcome. Claim, network send, and outcome persistence are separate short steps.

## 8. Scheduled work and external-service behavior

### 8.1 Project expiry

ExpiryScheduler uses the injected Manila Clock and a bounded candidate query, default maximum 100 records per pass and default fixed delay of one hour. It rechecks each candidate under transactional locks and changes only still-open PENDING/APPROVED Allocations whose Project has ended to CANCELLED with EXPIRED reason. It does not delete history.

### 8.2 Return reminders

ReminderScheduler defaults to DISABLED. When enabled, it selects a bounded batch of issued reusable Allocations whose Project ends from now through the next 24 hours and that have consent but no prior claim. A durable claim is recorded before transport send. A stale unresolved claim becomes UNKNOWN and is not automatically replayed. Network failures or uncertain success are not automatically retried. Outcomes distinguish NONE, CLAIMED, SIMULATED, PROVIDER_ACCEPTED, REJECTED, and UNKNOWN as defined in source. PROVIDER_ACCEPTED means a provider-shaped response was accepted; it does not mean a phone received a message.

### 8.3 Twilio protocol

The optional transport implements the Twilio Messages API request shape through Retrofit/Gson and OkHttp. It sends an authenticated HTTPS form request only to api.twilio.com; tests may use an explicit loopback-only HTTP base URL. It applies bounded timeouts, disables connection retry and redirects, and does not log credentials or message bodies. Uncertain outcomes become UNKNOWN rather than replaying a non-idempotent send. Course acceptance uses a fictional loopback server; no provider account, charge, real phone number, or real SMS was used. FakeTwilio remains a separate local simulator.

## 9. Resource image contract

Course A image functionality is restricted to PNG and JPEG. The server reads no more than 2,097,152 bytes, checks declared media type against file signatures, decodes dimensions before acceptance, and enforces a 16,777,216-pixel limit. Image bytes and validated media type are stored on Resource in nullable columns; the binary column is MEDIUMBLOB and lazy loading is verified for ordinary list/locked inventory paths by MySQL integration tests. The normal Resource DTO contains no image bytes. IMAGE-02 returns raw stored bytes, exact media type, no-store caching, and nosniff.

Observed local HTTP/MySQL acceptance and exact-byte checks are listed in the A3 technical gate and release receipt. These checks prove the behavior exercised locally, not the course's interpretation of CLO9 or instructor acceptance.

## 10. Frontend and demonstration use

The React interface presents eight screens over the Java REST API. It uses typed models and server responses for availability, coverage, lifecycle state, and reminder outcome. The interface supports light/dark appearance and reduced motion. The image receive/serve API has no image-upload screen.

The student demonstration should use fictional Department, Project, reusable Resource, consumable Resource, Requirement, and Allocation records. Show pending requests without reservation, approval and availability change, reusable issue without a second deduction, full return, consumable approval/consumption, a protected delete or invalid transition, a simulator result only if appropriate, a Postman request, and persistence across Java restart. Do not show real contacts or send real SMS.

A student-operated storyboard, expected observations, safe commands, recovery steps, and limitations are in docs/COURSE_A_FINAL_DEMO_GUIDE.md. The student must verify live Canvas instructions, perform and record the demonstration if required, and confirm access permissions. This document is not a recording.

## 11. Build, package, and technical acceptance

The release workflow uses scripts/package-release.ps1 and builds from one clean committed source SHA. The complete-source ZIP is produced from Git archive at that SHA. The Java/Eclipse ZIP contains Java source/tests, Maven Wrapper files, POM, backend README, and Eclipse settings. The executable JAR includes React static assets.

The A3 release set is generated outside Git and contains the JAR, complete-source ZIP, Java/Eclipse ZIP, as-built proposal DOCX, demo guide, portable first-run guide and launcher, read-only SQL, Postman reference and environment, SHA256SUMS, and JSON manifest with source SHA, toolchain, sizes, and hashes. The external receipt records hashes and check results. Source archives exclude passwords, local credential scripts, databases, build caches, node_modules, and release outputs.

Acceptance requires the Java 8/MySQL Maven gate, frontend typecheck/tests/lint/build, Newman execution, packaged JAR HTTP/API checks, clean extraction and rebuild where accessible, exact image retrieval and restart persistence, and hash/secret review. The technical gate labels each command PASS, FAIL, BLOCKED, NOT_RUN, or NOT_VERIFIED and names evidence limits. No test is represented as passing unless executed on the recorded source.

## 12. Academic and operational items controlled by people

- MANUAL STUDENT ACTION: verify current Canvas due date, rubric, submission location, and whether image work is expected in OrgResource or assessed separately.
- MANUAL STUDENT ACTION: verify group size and members against the submitted proposal. This document does not approve or reconstruct a roster.
- MANUAL STUDENT ACTION: review this as-built companion and decide whether to submit it with the original proposal.
- MANUAL STUDENT ACTION: record the actual demonstration with actual participants if required; verify instructor access to any link.
- MANUAL STUDENT ACTION: complete the authorship certificate personally and accurately acknowledge assistance.
- MANUAL STUDENT ACTION: perform required peer review with actual group members.
- MANUAL STUDENT ACTION: upload correct final artifacts and verify the Canvas receipt.
- INSTRUCTOR/COURSE OPEN ITEM: determine whether CLO9 image work is required within OrgResource and whether the implemented proof meets course expectations.
- PROVIDER OPEN ITEM: no live Twilio account, paid request, carrier delivery, or handset receipt is proven or required by this local technical acceptance.

## 13. Primary source and implementation locators

- Assignment and syllabus copies: ChatGPT-Project-Pack/UPLOAD/09_ESP_PROJECT_ASSIGNMENT.pdf and 08_CSCI180_SYLLABUS.pdf.
- Original concise proposal, preserved unchanged: ChatGPT-Project-Pack/UPLOAD/10_ORGRESOURCE_CONCISE_PROPOSAL.docx.
- Frozen Course A scope: docs/academic-final/07_COURSE_A_EXECUTABLE_SPEC_FREEZE_2026-11-16.md.
- Current API contract: docs/complete-handoff/05_API_26_ROUTES_AND_RESPONSES.md, amended here with current A09 and image operations.
- Entities: backend/src/main/java/app/entities.
- Jersey controllers: backend/src/main/java/app/rest/controllers.
- Public DTOs: backend/src/main/java/app/dto/ApiDtos.java.
- Spring components: backend/src/main/java/app/components.
- Repositories: backend/src/main/java/app/repositories.
- Current Postman inventory: postman/OrgResource-Course-A-28-Routes.postman_collection.json.
- Course A compliance status: docs/compliance/FINAL_COURSE_COMPLIANCE_MATRIX.md.
- Per-operation execution map: evidence/COURSE-A/ROUTE_ACCEPTANCE_MATRIX.md.
- Actual A3 command evidence: evidence/COURSE-A/A3_TECHNICAL_GATE.md.

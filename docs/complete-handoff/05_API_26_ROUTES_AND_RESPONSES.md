# 05 — Complete HTTP/JSON contract: 25 original routes + 1 proposed additional route

**Scope note:** this is the five-entity, 26-operation academic-baseline contract. The assignment requires at least five justified entity types, so five is not a course-imposed maximum or a permanent production-product cap. Any later justified production additions must preserve and separately map these academic routes and behaviors; O14 does not implement those additions.

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

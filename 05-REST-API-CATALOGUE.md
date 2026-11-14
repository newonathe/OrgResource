# Complete REST API Catalogue

## Common wire contract

Base URL http://127.0.0.1:8080; actual port follows course baseline. Routes below include /api exactly once in ResourceConfig/controller paths. All retrieval uses GET; every state change uses POST. Content-Type and Accept are application/json; GET has no request body. In DEMO send X-Demo-User-Id: a positive active seeded user id. In authenticated profile replace with the configured verified principal mechanism; the demo header is forbidden. X-Request-Id may be a valid UUID; otherwise generate one and echo it. No route accepts a client actorId. This is a local demo identity convention, not authentication.

Positive Long path/query IDs; field limits/types in04 apply to all matching DTO fields. Unknown JSON keys and unknown query names fail400, not silently ignored. An omitted optional update field preserves it; explicit null clears only nullable fields. Required update fields are shown in examples and cannot be null. expectedVersion is mandatory for every versioned mutation, including deletion; immutable history delete accepts it only as an ignored compatibility field and always refuses. Read/list responses expose version. Strings are trimmed, enums uppercase exact; no PUT/PATCH/DELETE needed under the assignment convention.

Success is a bare named DTO or Page DTO, not a different wrapper on each route. A201 response has Location of the created row; replay of a create returns200 with replay=true where specified, or the existing notification event. A204 has no JSON/body. Updates/actions200. HTTP422 is not used: field/cross-field failures400, invalid domain state/conflict409. HTTP401/403/404/409/500 are real error statuses, never200 error bodies. Missing referenced id in a body returns404; malformed id400. All public routes can also produce400,401,403,500 as described below. No stacktrace, SQL, credentials or phone appears in an error. A known access-denied object returns403 in this local organization demo; do not use it as a public enumeration endpoint.

The optional page envelope is {content:[NamedView],page:integer,size:integer,totalElements:long,totalPages:integer}. Default0/20,max100,id ascending. Date filter intervals are half-open. from/to use offset instants and test event occurredAt for movements/audit, interval overlap for projects/upcoming. All list queries return an empty page200 for no matches. Resource q filter is case-insensitive substring over code/name with escaped SQL LIKE wildcards, max100 characters; no arbitrary query expressions.

Repository calls listed per operation include possible checks, replay and audit paths. Dependencies below supply additional exact calls; no controller calls a repository. The specification includes every operation even where an entity can also be created indirectly. InventoryTransaction POST is a typed movement dispatcher, NotificationLog POST queues an eligible computed event, AuditEvent POST appends a NOTE. These are genuine runtime creates without allowing clients to fabricate protected history.

## Error response schema

{"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "RESOURCE_OVERALLOCATION", "message": "The requested quantity exceeds capacity for the selected period.", "path": "/api/allocations/1/approve", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {"availableQuantity": 4, "requestedQuantity": 5, "competingAllocationIds": [1]}}

Error details is always an object (empty if not applicable). Validation adds fieldErrors array of {field,message}; over-allocation adds requestedQuantity,availableQuantity,competingAllocationIds; dependency conflict adds dependentTypes; version conflict adds currentVersion. retryable is true only RETRY_REQUIRED or a temporary internal condition explicitly classified. For ordinary500 false is the safe default. All documented codes map to their listed HTTP status.

400 BAD_REQUEST: The request has invalid or unknown fields.

400 VALIDATION_FAILED: One or more fields failed validation.

401 UNAUTHENTICATED: An active acting user is required.

403 FORBIDDEN: The acting user cannot perform this operation.

404 NOT_FOUND: The referenced record does not exist.

409 DUPLICATE_RECORD: A record with this unique reference already exists.

409 DEPENDENCY_EXISTS: Related records prevent deletion; use the documented lifecycle action.

409 IMMUTABLE_HISTORY: Posted history cannot be deleted; use a new correction or reversal.

409 VERSION_CONFLICT: The record changed; retrieve its current version before deciding again.

409 INVALID_STATE: The current state does not permit this action.

409 RESOURCE_OVERALLOCATION: The requested quantity exceeds capacity for the selected period.

409 REQUIREMENT_COVERAGE_EXCEEDED: Approved coverage would exceed the requirement quantity.

409 INSUFFICIENT_PHYSICAL_STOCK: There is insufficient usable stock in storage for this issue.

409 RESOURCE_TYPE_MISMATCH: The resource type does not support this movement.

409 SCHEDULE_LOCKED: A recorded issue or consumption prevents schedule replacement.

409 IDEMPOTENCY_KEY_REUSED: This command key was used with different input.

409 REVERSAL_DEPENDENCY_CONFLICT: Later activity prevents safe reversal of this event.

409 RETRY_REQUIRED: The command rolled back due to concurrent activity; retrieve state and retry safely.

409 NOTIFICATION_NOT_ELIGIBLE: This reminder is not currently due or has no outstanding custody.

409 PROVIDER_OUTCOME_UNKNOWN: The previous send outcome is unknown; reconcile before resending.

500 INTERNAL_ERROR: The operation could not be completed. Refer to the request ID.

## Request DTO catalogue

Every operation with JSON defines a distinct request DTO named Op plus endpoint ID plus Request, for example Op01CRequest and OpA01Request. Its permitted keys are exactly the request sample keys. Query DTOs use Op plus ID plus Query. Per-key types follow04, except expectedVersion Long>=0; reason NotBlank max1000 when required; waiveUnfulfilled Boolean default false; candidateQuantity Long>0; requestKey/commandKey UUID36; requirements list of {requirementId Long,startAt OffsetDateTime,endAt OffsetDateTime}, complete set required on reschedule. Empty {} request actions accept no other fields. Creation requests do not accept ids, states, timestamps, counters or actor references. Department/User/Project/Resource/Requirement create DTOs infer initial state server side as described in06. Null resource/allocation/inspection fields in a movement are explicitly allowed/required by movement type; Resource.quantity and client signed deltas are always rejected.

User create keys include username/displayName/role/departmentId only. User update separates role/department/active administration from self contact/consent in the same endpoint: if ADMIN edits another user, smsConsent may be false to revoke but may not be set true on their behalf. A self update may omit role/department/active or echo unchanged values, but attempting to change them returns403. phoneE164 is normalized from an already explicit E.164 value; no guessed country. consentVersion OP_SMS_V1 is generated by the server only after the self opt-in. A valid regex is not evidence the number exists; the live test recipient must independently be confirmed/allowed in the provider account.

For stock POST, resourceId,type,quantity,reason,commandKey are mandatory; allocationId is required for ISSUE/RETURN/CONSUMPTION and optional only for custody LOSS, forbidden otherwise except REVERSAL which derives it from reversalOfId. inspection is required only RETURN, null otherwise. reversalOfId required only REVERSAL; correctionReferenceId optional for a present corrective observation and must reference same resource. The reverse convenience route derives resource,type,quantity,allocation,inspection from original. All underlying rules are identical to direct POST; no second stock-write path bypasses validation.

## Named response DTO examples

### DepartmentView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "code": "LOG", "name": "Logistics", "description": "Fictional demonstration department", "active": true}

### AppUserView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "username": "demo.custodian", "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": 1, "active": true, "phoneE164": null, "smsConsent": false, "consentAt": null, "consentVersion": null}

### ProjectView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "DRAFT", "statusReason": null}

### ResourceView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "code": "CORD_POOL", "name": "Extension Cords", "description": "Interchangeable fictional cords", "type": "REUSABLE", "category": "Equipment", "unit": "piece", "storageLocation": "Demo cabinet", "controlBasis": "OWNED", "authorizationNote": null, "departmentId": 1, "custodianId": 1, "revision": 1, "active": true, "physicalInStore": 10, "outstanding": 0, "damagedInStore": 0, "usableOwned": 10, "reservedConsumable": 0, "availableConsumable": null}

### ResourceRequirementView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "projectId": 1, "resourceId": 1, "quantity": 6, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "priority": "NORMAL", "notes": "Fictional planning requirement", "status": "ACTIVE", "coveredQuantity": 0, "suppliedQuantity": 0, "outstandingQuantity": 0, "unallocatedQuantity": 6}

### AllocationView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "requirementId": 1, "quantity": 6, "releasedUnissued": 0, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "REQUESTED", "decidedById": null, "decidedAt": null, "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 0, "fulfillment": "NONE", "overdue": false, "replay": false}

### InventoryTransactionView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "resourceId": 1, "allocationId": null, "type": "RECEIPT", "quantity": 10, "inspection": null, "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": 10, "deltaOutstanding": 0, "deltaDamaged": 0, "deltaIssued": 0, "deltaResolved": 0, "deltaGoodReturned": 0, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null, "resourceRevisionAfter": 1, "replay": false, "impactedAllocationIds": []}

### NotificationLogView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "SKIPPED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": null, "attemptCount": 0, "destinationMasked": null, "providerSid": null, "providerStatus": null, "lastErrorCode": "NO_CONSENT", "lastAttemptAt": null, "cancelReason": null, "attempts": []}

### AuditEventView

{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "action": "NOTE", "targetType": "Project", "targetId": 1, "targetSummary": "WELCOME_26", "beforeJson": null, "afterJson": null, "reason": "Fictional planning note", "requestId": "00000000-0000-4000-8000-000000000002"}

### AvailabilityView

{"resourceId": 1, "asOf": "2026-11-10T00:00:00Z", "startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T05:00:00Z", "unit": "piece", "physicalInStore": 10, "outstanding": 0, "usableOwned": 10, "reservedConsumable": null, "rawMinimumFree": 4, "allocatableQuantity": 4, "overdueOutstanding": 0, "returnDependentQuantity": 0, "segments": [{"startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T04:00:00Z", "committed": 6, "rawFree": 4, "allocationIds": [1]}], "warnings": []}

### ShortageView

{"projectId": 1, "asOf": "2026-11-10T00:00:00Z", "requirements": [{"requirementId": 1, "resourceId": 1, "unit": "piece", "demand": 6, "approvedCoverage": 0, "supplied": 0, "outstanding": 0, "unallocated": 6, "freeIfConsideredAlone": 10, "sourcingGapIfConsideredAlone": 0, "atRisk": false}], "warning": "Individual candidate gaps are not additive."}

### DemandView

{"resourceId": 1, "asOf": "2026-11-10T00:00:00Z", "to": "2026-11-10T06:00:00Z", "includeDraft": false, "unit": "piece", "minimumAdditionalUnits": 1, "segments": [{"startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T04:00:00Z", "usableCapacity": 10, "approvedCommitted": 6, "unallocatedDemand": 5, "deficit": 1, "requirementIds": [1, 2], "projectIds": [1, 2], "departmentIds": [1, 2]}], "reasons": ["TIME_CONFLICT"], "warning": "Capacity scenario with fixed dates; not purchasing authorization.", "from": "2026-11-10T01:00:00Z"}

### InventoryReport

{"asOf": "2026-11-10T00:00:00Z", "items": [{"resourceId": 1, "type": "REUSABLE", "unit": "piece", "physicalInStore": 10, "outstanding": 0, "damaged": 0, "usableOwned": 10, "ledgerConsistent": true}]}

### AllocationReport

{"asOf": "2026-11-10T00:00:00Z", "items": [{"allocationId": 1, "projectId": 1, "resourceId": 1, "recipientId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "unissued": 0, "outstanding": 2, "overdue": true, "atRisk": false}]}

### DepartmentReport

{"asOf": "2026-11-10T00:00:00Z", "departmentId": 1, "to": "2026-11-11T01:00:00Z", "items": [{"resourceId": 1, "unit": "piece", "requested": 6, "approvedCoverage": 4, "supplied": 0, "unallocated": 2}], "warning": "Different units are never summed.", "from": "2026-11-10T01:00:00Z"}

### JobRunView

{"asOf": "2026-11-10T00:00:00Z", "created": 1, "skipped": 0, "existing": 0, "errors": 0, "providerMode": "FAKE"}

These are complete shape examples, not populated real database records. ID1 is illustrative. Nulls shown for optional values do not prove every null is permitted in every state. Resource balances are type-dependent: reusable exposes S/O/B/U and availableConsumable=null; consumable exposes physicalInStore=P, outstanding=0,damagedInStore=0,usableOwned=P,reservedConsumable=V,availableConsumable=ATP. Names and relation IDs have the exact meanings in04. AllocationView includes replay Boolean on create replay (false otherwise); InventoryTransactionView replay is always present. User contact values are null for unauthorized viewers rather than omitted. List response content is an array of its exact named view. No generic Map of uncontrolled keys is accepted as business input.

## 01C POST /api/departments

Purpose: Flat accountable unit inside the one organization. Permission: ADMIN.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op01CRequest): {"code": "LOG", "name": "Logistics", "description": "Fictional demonstration department"}

Success: HTTP201; DepartmentView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "LOG", "name": "Logistics", "description": "Fictional demonstration department", "active": true}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/departments", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: DepartmentController.create. Autowired component: DepartmentComponent. Forward exactly to DepartmentComponent.create as defined in06.

Repository methods involved: DepartmentRepository.save, DepartmentRepository.findById, DepartmentRepository.findByCode, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Normalize code/name; validate unique code, ADMIN and nonblank fields. 2. Persist active department and append CREATE audit; no stock or hierarchy exists.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/departments with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 01L GET /api/departments

Purpose: List visible departments with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"active": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op01LRequest): none; GET has no body.

Success: HTTP200; Page<DepartmentView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "code": "LOG", "name": "Logistics", "description": "Fictional demonstration department", "active": true}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/departments", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: DepartmentController.list. Autowired component: DepartmentComponent. Forward exactly to DepartmentComponent.list as defined in06.

Repository methods involved: DepartmentRepository.findAll.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/departments with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 01R GET /api/departments/{id}

Purpose: Retrieve one Department with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op01RRequest): none; GET has no body.

Success: HTTP200; DepartmentView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "LOG", "name": "Logistics", "description": "Fictional demonstration department", "active": true}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/departments/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: DepartmentController.get. Autowired component: DepartmentComponent. Forward exactly to DepartmentComponent.get as defined in06.

Repository methods involved: DepartmentRepository.findById.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/departments/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 01D POST /api/departments/{id}/delete

Purpose: Hard delete only when no user, project or resource references exist. Otherwise409 DEPENDENCY_EXISTS; update active=false only when no active operational dependency. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op01DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

Success: HTTP204; no body.

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/departments/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: DepartmentController.delete. Autowired component: DepartmentComponent. Forward exactly to DepartmentComponent.delete as defined in06.

Repository methods involved: DepartmentRepository.findById, DepartmentRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/departments/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 01U POST /api/departments/{id}/update

Purpose: Versioned update of permitted Department fields. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op01URequest): {"expectedVersion": 0, "name": "Logistics Team", "description": "Fictional team", "active": true}

Success: HTTP200; DepartmentView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "LOG", "name": "Logistics", "description": "Fictional demonstration department", "active": true}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 DUPLICATE_RECORD, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/departments/1/update", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: DepartmentController.update. Autowired component: DepartmentComponent. Forward exactly to DepartmentComponent.update as defined in06.

Repository methods involved: DepartmentRepository.findById, DepartmentRepository.save, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle. 2. Setting active=false requires no active AppUser, active Resource or project outside COMPLETED/CANCELLED referencing this department; otherwise409 DEPENDENCY_EXISTS. 3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced. 4. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval. 5. Apply metadata change and append before/after audit atomically; return updated version.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/departments/1/update with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 02C POST /api/users

Purpose: Member identity and access/contact preferences. Permission: ADMIN.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op02CRequest): {"username": "demo.custodian", "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": "{{departmentId}}"}

Success: HTTP201; AppUserView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "username": "demo.custodian", "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": 1, "active": true, "phoneE164": null, "smsConsent": false, "consentAt": null, "consentVersion": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/users", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: UserController.create. Autowired component: UserComponent. Forward exactly to UserComponent.create as defined in06.

Repository methods involved: AppUserRepository.save, AppUserRepository.findById, AppUserRepository.findByUsername, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Validate ADMIN, unique normalized username and allowed role; check active department unless organization-wide role. 2. Ignore no security fields: reject actor/password/consent timestamps; initial SMS consent false unless a separate self consent update is made after creation. 3. Save user and append redacted CREATE audit without phone.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/users with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 02L GET /api/users

Purpose: List visible users with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"departmentId": "optional exact/range filter", "role": "optional exact/range filter", "active": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op02LRequest): none; GET has no body.

Success: HTTP200; Page<AppUserView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "username": "demo.custodian", "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": 1, "active": true, "phoneE164": null, "smsConsent": false, "consentAt": null, "consentVersion": null}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/users", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: UserController.list. Autowired component: UserComponent. Forward exactly to UserComponent.list as defined in06.

Repository methods involved: AppUserRepository.findAll.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/users with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 02R GET /api/users/{id}

Purpose: Retrieve one AppUser with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op02RRequest): none; GET has no body.

Success: HTTP200; AppUserView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "username": "demo.custodian", "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": 1, "active": true, "phoneE164": null, "smsConsent": false, "consentAt": null, "consentVersion": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/users/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: UserController.get. Autowired component: UserComponent. Forward exactly to UserComponent.get as defined in06.

Repository methods involved: AppUserRepository.findById.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/users/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 02D POST /api/users/{id}/delete

Purpose: Hard delete only if no operational or actor references anywhere. Otherwise deactivate using update; deny deactivation if assigned unresolved custody or sole active custodian, until reassigned. Never delete last active ADMIN. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op02DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

Success: HTTP204; no body.

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/users/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: UserController.delete. Autowired component: UserComponent. Forward exactly to UserComponent.delete as defined in06.

Repository methods involved: AppUserRepository.findById, AppUserRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/users/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 02U POST /api/users/{id}/update

Purpose: Versioned update of permitted AppUser fields. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op02URequest): {"expectedVersion": 0, "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": "{{departmentId}}", "active": true, "phoneE164": null, "smsConsent": false}

Success: HTTP200; AppUserView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "username": "demo.custodian", "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": 1, "active": true, "phoneE164": null, "smsConsent": false, "consentAt": null, "consentVersion": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 DUPLICATE_RECORD, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/users/1/update", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: UserController.update. Autowired component: UserComponent. Forward exactly to UserComponent.update as defined in06.

Repository methods involved: AppUserRepository.findById, AppUserRepository.save, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle. 2. Prevent deleting/deactivating/demoting last active ADMIN. Role/department transfer is refused while user owns a nonterminal project, is a resource custodian or has unresolved allocation custody; reassign resource custody through its own officer command first. 3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced. 4. Only self or ADMIN may change contact; only self can grant consent, notice version OP_SMS_V1 and consentAt are server recorded. Role/department/active edits require ADMIN; never trust a role change from the ordinary self-contact DTO. Revocation clears consent flag/metadata and cancels queued sends at dispatch recheck. 5. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval. 6. Apply metadata change and append before/after audit atomically; return updated version.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/users/1/update with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 03C POST /api/projects

Purpose: Dated departmental activity with internal planning approval. Permission: MEMBER owner or HEAD in own department.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op03CRequest): {"code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": "{{departmentId}}", "ownerId": "{{memberId}}", "startAt": "{{startA}}", "endAt": "{{endA}}"}

Success: HTTP201; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "DRAFT", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/projects", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.create. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.create as defined in06.

Repository methods involved: ProjectRepository.save, ProjectRepository.findById, ProjectRepository.findByCode, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve active department and owner, require owner in department and authorized actor. 2. Validate offset schedule, duration and unique code; force DRAFT, no client state override. 3. Save and audit the planning record.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 03L GET /api/projects

Purpose: List visible projects with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"departmentId": "optional exact/range filter", "ownerId": "optional exact/range filter", "status": "optional exact/range filter", "from": "optional exact/range filter", "to": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op03LRequest): none; GET has no body.

Success: HTTP200; Page<ProjectView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "DRAFT", "statusReason": null}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/projects", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.list. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.list as defined in06.

Repository methods involved: ProjectRepository.findAll.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/projects with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 03R GET /api/projects/{id}

Purpose: Retrieve one Project with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op03RRequest): none; GET has no body.

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "DRAFT", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/projects/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.get. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.get as defined in06.

Repository methods involved: ProjectRepository.findById.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/projects/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 03D POST /api/projects/{id}/delete

Purpose: Hard delete only DRAFT with no requirements/allocations. Historical project uses cancel or completed archival; no cascading delete. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op03DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

Success: HTTP204; no body.

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/projects/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.delete. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.delete as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 03U POST /api/projects/{id}/update

Purpose: Versioned update of permitted Project fields. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op03URequest): {"expectedVersion": 0, "name": "Welcome Workshop Revised", "description": "Fictional revised description"}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "DRAFT", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 DUPLICATE_RECORD, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/projects/1/update", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.update. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.update as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle. 2. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced. 3. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval. 4. Apply metadata change and append before/after audit atomically; return updated version.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/update with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 04C POST /api/resources

Purpose: Pool of interchangeable whole units under organizational control. Permission: assigned CUSTODIAN or OFFICER assigning an active custodian.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op04CRequest): {"code": "CORD_POOL", "name": "Extension Cords", "description": "Interchangeable fictional cords", "type": "REUSABLE", "category": "Equipment", "unit": "piece", "storageLocation": "Demo cabinet", "controlBasis": "OWNED", "authorizationNote": null, "departmentId": "{{departmentId}}", "custodianId": "{{custodianId}}"}

Success: HTTP201; ResourceView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "CORD_POOL", "name": "Extension Cords", "description": "Interchangeable fictional cords", "type": "REUSABLE", "category": "Equipment", "unit": "piece", "storageLocation": "Demo cabinet", "controlBasis": "OWNED", "authorizationNote": null, "departmentId": 1, "custodianId": 1, "revision": 1, "active": true, "physicalInStore": 0, "outstanding": 0, "damagedInStore": 0, "usableOwned": 0, "reservedConsumable": 0, "availableConsumable": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/resources", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ResourceController.create. Autowired component: ResourceComponent. Forward exactly to ResourceComponent.create as defined in06.

Repository methods involved: ResourceRepository.save, ResourceRepository.findById, ResourceRepository.findByCode, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Check resource control basis and authorization note, active department and active CUSTODIAN. 2. Create metadata with zero ledger stock; reject supplied quantity, writable condition totals or stock deltas. 3. Save and audit; actual opening stock requires RECEIPT.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/resources with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 04L GET /api/resources

Purpose: List visible resources with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"departmentId": "optional exact/range filter", "custodianId": "optional exact/range filter", "type": "optional exact/range filter", "active": "optional exact/range filter", "category": "optional exact/range filter", "q": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op04LRequest): none; GET has no body.

Success: HTTP200; Page<ResourceView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "code": "CORD_POOL", "name": "Extension Cords", "description": "Interchangeable fictional cords", "type": "REUSABLE", "category": "Equipment", "unit": "piece", "storageLocation": "Demo cabinet", "controlBasis": "OWNED", "authorizationNote": null, "departmentId": 1, "custodianId": 1, "revision": 1, "active": true, "physicalInStore": 10, "outstanding": 0, "damagedInStore": 0, "usableOwned": 10, "reservedConsumable": 0, "availableConsumable": null}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/resources", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ResourceController.list. Autowired component: ResourceComponent. Forward exactly to ResourceComponent.list as defined in06.

Repository methods involved: ResourceRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/resources with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 04R GET /api/resources/{id}

Purpose: Retrieve one Resource with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op04RRequest): none; GET has no body.

Success: HTTP200; ResourceView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "CORD_POOL", "name": "Extension Cords", "description": "Interchangeable fictional cords", "type": "REUSABLE", "category": "Equipment", "unit": "piece", "storageLocation": "Demo cabinet", "controlBasis": "OWNED", "authorizationNote": null, "departmentId": 1, "custodianId": 1, "revision": 1, "active": true, "physicalInStore": 10, "outstanding": 0, "damagedInStore": 0, "usableOwned": 10, "reservedConsumable": 0, "availableConsumable": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/resources/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ResourceController.get. Autowired component: ResourceComponent. Forward exactly to ResourceComponent.get as defined in06.

Repository methods involved: ResourceRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/resources/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 04D POST /api/resources/{id}/delete

Purpose: Hard delete only with no requirements, allocations or transactions. Archive requires no pending/active unissued commitment or outstanding custody; keep ledger forever in demo. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op04DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

Success: HTTP204; no body.

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/resources/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ResourceController.delete. Autowired component: ResourceComponent. Forward exactly to ResourceComponent.delete as defined in06.

Repository methods involved: ResourceRepository.findById, ResourceRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/resources/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 04U POST /api/resources/{id}/update

Purpose: Versioned update of permitted Resource fields. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op04URequest): {"expectedVersion": 0, "name": "Extension Cords", "description": "Checked pool", "category": "Equipment", "storageLocation": "Demo cabinet B", "departmentId": "{{departmentId}}", "custodianId": "{{custodianId}}", "active": true, "reason": "Cabinet label changed"}

Success: HTTP200; ResourceView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "CORD_POOL", "name": "Extension Cords", "description": "Interchangeable fictional cords", "type": "REUSABLE", "category": "Equipment", "unit": "piece", "storageLocation": "Demo cabinet", "controlBasis": "OWNED", "authorizationNote": null, "departmentId": 1, "custodianId": 1, "revision": 1, "active": true, "physicalInStore": 10, "outstanding": 0, "damagedInStore": 0, "usableOwned": 10, "reservedConsumable": 0, "availableConsumable": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 DUPLICATE_RECORD, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/resources/1/update", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ResourceController.update. Autowired component: ResourceComponent. Forward exactly to ResourceComponent.update as defined in06.

Repository methods involved: ResourceRepository.findById, ResourceRepository.save, AuditEventRepository.save, ResourceRepository.lockById.

Business rules and ordered method behavior: 1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle. 2. Acquire resource lock; custodian reassignment is OFFICER only with reason, and cannot rewrite existing allocation recipients. 3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced. 4. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval. 5. Apply metadata change and append before/after audit atomically; return updated version.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/resources/1/update with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 05C POST /api/requirements

Purpose: Demand for one resource during one project window. Permission: project owner or HEAD in own department.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op05CRequest): {"projectId": "{{projectId}}", "resourceId": "{{resourceId}}", "quantity": 6, "startAt": "{{startA}}", "endAt": "{{endA}}", "priority": "NORMAL", "notes": "Fictional planning requirement"}

Success: HTTP201; ResourceRequirementView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "projectId": 1, "resourceId": 1, "quantity": 6, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "priority": "NORMAL", "notes": "Fictional planning requirement", "status": "ACTIVE", "coveredQuantity": 0, "suppliedQuantity": 0, "outstandingQuantity": 0, "unallocatedQuantity": 6}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/requirements", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: RequirementController.create. Autowired component: RequirementComponent. Forward exactly to RequirementComponent.create as defined in06.

Repository methods involved: ResourceRequirementRepository.save, ResourceRequirementRepository.findById, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, ResourceRequirementRepository.findByResource_IdAndStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Read project/resource IDs, lock resource then re-read project; validate owner/head and DRAFT/SUBMITTED project. 2. Require active resource, one project-resource pair, quantity cap and interval within project. 3. Persist ACTIVE demand; if project was SUBMITTED return it to DRAFT and audit both changes.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/requirements with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 05L GET /api/requirements

Purpose: List visible requirements with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"projectId": "optional exact/range filter", "resourceId": "optional exact/range filter", "status": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op05LRequest): none; GET has no body.

Success: HTTP200; Page<ResourceRequirementView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "projectId": 1, "resourceId": 1, "quantity": 6, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "priority": "NORMAL", "notes": "Fictional planning requirement", "status": "ACTIVE", "coveredQuantity": 0, "suppliedQuantity": 0, "outstandingQuantity": 0, "unallocatedQuantity": 6}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/requirements", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: RequirementController.list. Autowired component: RequirementComponent. Forward exactly to RequirementComponent.list as defined in06.

Repository methods involved: ResourceRequirementRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/requirements with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 05R GET /api/requirements/{id}

Purpose: Retrieve one ResourceRequirement with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op05RRequest): none; GET has no body.

Success: HTTP200; ResourceRequirementView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "projectId": 1, "resourceId": 1, "quantity": 6, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "priority": "NORMAL", "notes": "Fictional planning requirement", "status": "ACTIVE", "coveredQuantity": 0, "suppliedQuantity": 0, "outstandingQuantity": 0, "unallocatedQuantity": 6}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/requirements/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: RequirementController.get. Autowired component: RequirementComponent. Forward exactly to RequirementComponent.get as defined in06.

Repository methods involved: ResourceRequirementRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/requirements/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 05D POST /api/requirements/{id}/delete

Purpose: Hard delete only when no allocation or movement references. Otherwise withdrawal through update with reason, only after all operational obligations are resolved. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op05DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

Success: HTTP204; no body.

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/requirements/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: RequirementController.delete. Autowired component: RequirementComponent. Forward exactly to RequirementComponent.delete as defined in06.

Repository methods involved: ResourceRequirementRepository.findById, ResourceRequirementRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/requirements/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 05U POST /api/requirements/{id}/update

Purpose: Versioned update of permitted ResourceRequirement fields. Permission: ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op05URequest): {"expectedVersion": 0, "quantity": 6, "startAt": "{{startA}}", "endAt": "{{endA}}", "priority": "HIGH", "notes": "Updated need", "status": "ACTIVE", "reason": "Priority clarified"}

Success: HTTP200; ResourceRequirementView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "projectId": 1, "resourceId": 1, "quantity": 6, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "priority": "NORMAL", "notes": "Fictional planning requirement", "status": "ACTIVE", "coveredQuantity": 0, "suppliedQuantity": 0, "outstandingQuantity": 0, "unallocatedQuantity": 6}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 DUPLICATE_RECORD, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/requirements/1/update", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: RequirementController.update. Autowired component: RequirementComponent. Forward exactly to RequirementComponent.update as defined in06.

Repository methods involved: ResourceRequirementRepository.findById, ResourceRequirementRepository.save, AuditEventRepository.save, ResourceRepository.lockById.

Business rules and ordered method behavior: 1. Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle. 2. Distinguish withdrawal from demand edits: quantity/window/priority/notes edits require DRAFT/SUBMITTED project and no approved allocation or movement; WITHDRAWN is allowed after all history is operationally resolved, without deleting it. Cancel REQUESTED rows and release no stock implicitly. projectId/resourceId cannot change after creation. 3. Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced. 4. Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval. 5. Apply metadata change and append before/after audit atomically; return updated version.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/requirements/1/update with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 06C POST /api/allocations

Purpose: One requested quantity and its immutable approved schedule/recipient. Permission: project owner or HEAD in own department; recipient active in that department.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op06CRequest): {"requirementId": "{{requirementId}}", "quantity": 6, "recipientId": "{{memberId}}", "requestKey": "{{requestKey}}"}

Success: HTTP201; AllocationView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "requirementId": 1, "quantity": 6, "releasedUnissued": 0, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "REQUESTED", "decidedById": null, "decidedAt": null, "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 0, "fulfillment": "NONE", "overdue": false, "replay": false}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED, 409 IDEMPOTENCY_KEY_REUSED, 409 RESOURCE_TYPE_MISMATCH, 409 RESOURCE_OVERALLOCATION, 409 REQUIREMENT_COVERAGE_EXCEEDED, 409 INSUFFICIENT_PHYSICAL_STOCK, 409 REVERSAL_DEPENDENCY_CONFLICT. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/allocations", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.request. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.request as defined in06.

Repository methods involved: AllocationRepository.save, AllocationRepository.findById, AllocationRepository.findByRequestKey, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, AllocationRepository.findByRequirement_Project_Id, AllocationRepository.findByRequirement_Id, AllocationRepository.findOverlappingApproved, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Canonicalize requestKey payload; resolve requirement/project/resource and lock resource then requirement. 2. Check identical-key replay first; different hash409. Verify active demand and APPROVED/IN_PROGRESS project, authorized requester and same-department recipient. 3. Require endAt > Clock.instant(); a current interval is allowed, a wholly past commitment is not. 4. Require quantity <= unallocated requirement demand; REQUESTED allocations do not reserve stock or count as approved coverage. 5. Copy requirement interval, fix requester/recipient, set REQUESTED, save and audit; do not autoapprove.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 06L GET /api/allocations

Purpose: List visible allocations with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"projectId": "optional exact/range filter", "resourceId": "optional exact/range filter", "recipientId": "optional exact/range filter", "decisionStatus": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op06LRequest): none; GET has no body.

Success: HTTP200; Page<AllocationView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "requirementId": 1, "quantity": 6, "releasedUnissued": 0, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "REQUESTED", "decidedById": null, "decidedAt": null, "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 0, "fulfillment": "NONE", "overdue": false, "replay": false}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/allocations", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.list. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.list as defined in06.

Repository methods involved: AllocationRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/allocations with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 06R GET /api/allocations/{id}

Purpose: Retrieve one Allocation with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op06RRequest): none; GET has no body.

Success: HTTP200; AllocationView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "requirementId": 1, "quantity": 6, "releasedUnissued": 0, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "REQUESTED", "decidedById": null, "decidedAt": null, "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 0, "fulfillment": "NONE", "overdue": false, "replay": false}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/allocations/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.get. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.get as defined in06.

Repository methods involved: AllocationRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/allocations/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 06D POST /api/allocations/{id}/delete

Purpose: Hard delete only REQUESTED by requester or HEAD/OFFICER, with no movements or notifications; append audit. Approved/rejected/cancelled history retained;409 directs cancel/release. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op06DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

Success: HTTP204; no body.

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/allocations/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.delete. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.delete as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 07C POST /api/inventory-transactions

Purpose: Immutable physical/custody delta and correction event. Permission: assigned CUSTODIAN; OFFICER for recount/reversal.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op07CRequest): {"resourceId": "{{resourceId}}", "allocationId": null, "type": "RECEIPT", "quantity": 10, "inspection": null, "reason": "OPENING_COUNT fictional stock", "commandKey": "{{commandKey}}", "reversalOfId": null, "correctionReferenceId": null}

Success: HTTP201; InventoryTransactionView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "resourceId": 1, "allocationId": null, "type": "RECEIPT", "quantity": 10, "inspection": null, "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": 10, "deltaOutstanding": 0, "deltaDamaged": 0, "deltaIssued": 0, "deltaResolved": 0, "deltaGoodReturned": 0, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null, "resourceRevisionAfter": 1, "replay": false, "impactedAllocationIds": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED, 409 IDEMPOTENCY_KEY_REUSED, 409 RESOURCE_TYPE_MISMATCH, 409 RESOURCE_OVERALLOCATION, 409 REQUIREMENT_COVERAGE_EXCEEDED, 409 INSUFFICIENT_PHYSICAL_STOCK, 409 REVERSAL_DEPENDENCY_CONFLICT. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/inventory-transactions", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: InventoryTransactionController.record. Autowired component: InventoryTransactionComponent. Forward exactly to InventoryTransactionComponent.record as defined in06.

Repository methods involved: InventoryTransactionRepository.save, InventoryTransactionRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, InventoryTransactionRepository.findByResource_IdAndCommandKey, InventoryTransactionRepository.findByReversalOf_Id, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc, ResourceRepository.lockById, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve pool and optional allocation; take resource lock, canonicalize commandKey/hash and return identical replay before effect application. 2. Dispatch the declared type through the exact typed movement rules in02; verify operator and required/forbidden references and inspection. 3. Rebuild ledger balances and allocation counters; check nonnegative physical/custody balances, eligible interval/state and type-specific reservation rules. 4. Derive signed effect vector, increment resource revision, flush and capture resourceRevisionAfter; insert immutable transaction and append audit in the same transaction. 5. Return derived balances and impacted commitment IDs; observed damage/loss may create a reported future planning deficit.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/inventory-transactions with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 07L GET /api/inventory-transactions

Purpose: List visible inventory-transactions with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"resourceId": "optional exact/range filter", "allocationId": "optional exact/range filter", "type": "optional exact/range filter", "from": "optional exact/range filter", "to": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op07LRequest): none; GET has no body.

Success: HTTP200; Page<InventoryTransactionView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "resourceId": 1, "allocationId": null, "type": "RECEIPT", "quantity": 10, "inspection": null, "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": 10, "deltaOutstanding": 0, "deltaDamaged": 0, "deltaIssued": 0, "deltaResolved": 0, "deltaGoodReturned": 0, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null, "resourceRevisionAfter": 1, "replay": false, "impactedAllocationIds": []}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/inventory-transactions", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: InventoryTransactionController.list. Autowired component: InventoryTransactionComponent. Forward exactly to InventoryTransactionComponent.list as defined in06.

Repository methods involved: InventoryTransactionRepository.findAll.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/inventory-transactions with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 07R GET /api/inventory-transactions/{id}

Purpose: Retrieve one InventoryTransaction with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op07RRequest): none; GET has no body.

Success: HTTP200; InventoryTransactionView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "resourceId": 1, "allocationId": null, "type": "RECEIPT", "quantity": 10, "inspection": null, "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": 10, "deltaOutstanding": 0, "deltaDamaged": 0, "deltaIssued": 0, "deltaResolved": 0, "deltaGoodReturned": 0, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null, "resourceRevisionAfter": 1, "replay": false, "impactedAllocationIds": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/inventory-transactions/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: InventoryTransactionController.get. Autowired component: InventoryTransactionComponent. Forward exactly to InventoryTransactionComponent.get as defined in06.

Repository methods involved: InventoryTransactionRepository.findById.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/inventory-transactions/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 07D POST /api/inventory-transactions/{id}/delete

Purpose: Never physically delete a posted movement. Delete endpoint returns409 IMMUTABLE_HISTORY with reversal route. No permission to clear the ledger through REST. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op07DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

No successful deletion is permitted for posted history. Documented response: HTTP409 Error JSON. Example: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "IMMUTABLE_HISTORY", "message": "Posted history cannot be deleted; use a new correction or reversal.", "path": "/api/inventory-transactions/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED, 409 IMMUTABLE_HISTORY. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/inventory-transactions/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: InventoryTransactionController.delete. Autowired component: InventoryTransactionComponent. Forward exactly to InventoryTransactionComponent.delete as defined in06.

Repository methods involved: InventoryTransactionRepository.findById, InventoryTransactionRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/inventory-transactions/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 08C POST /api/notifications

Purpose: Durable return reminder event/outbox and provider outcome. Permission: assigned CUSTODIAN or OFFICER; server recomputes due eligibility.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op08CRequest): {"allocationId": "{{allocationId}}", "kind": "RETURN_DUE"}

Success: HTTP201; NotificationLogView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "SKIPPED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": null, "attemptCount": 0, "destinationMasked": null, "providerSid": null, "providerStatus": null, "lastErrorCode": "NO_CONSENT", "lastAttemptAt": null, "cancelReason": null, "attempts": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED, 409 NOTIFICATION_NOT_ELIGIBLE. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/notifications", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.enqueueManual. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.enqueueManual as defined in06.

Repository methods involved: NotificationLogRepository.save, NotificationLogRepository.findById, NotificationLogRepository.findByEventKey, NotificationLogRepository.findDue, NotificationLogRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve allocation and recipient; require authorized actor, reusable outstanding custody and requested kind currently in reminder window. 2. Compute unique eventKey from kind/allocation/endAt/recipient. Existing key returns existing view200, never a second event. 3. Read current consent/contact; create QUEUED with nextAttemptAt now or SKIPPED with reason, append audit, never call Twilio here.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/notifications with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 08L GET /api/notifications

Purpose: List visible notifications with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"allocationId": "optional exact/range filter", "recipientId": "optional exact/range filter", "status": "optional exact/range filter", "kind": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op08LRequest): none; GET has no body.

Success: HTTP200; Page<NotificationLogView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": null, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "SKIPPED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": null, "attemptCount": 0, "destinationMasked": null, "providerSid": null, "providerStatus": null, "lastErrorCode": "NO_CONSENT", "lastAttemptAt": null, "cancelReason": null, "attempts": []}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/notifications", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.list. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.list as defined in06.

Repository methods involved: NotificationLogRepository.findAll.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/notifications with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 08R GET /api/notifications/{id}

Purpose: Retrieve one NotificationLog with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op08RRequest): none; GET has no body.

Success: HTTP200; NotificationLogView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "SKIPPED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": null, "attemptCount": 0, "destinationMasked": null, "providerSid": null, "providerStatus": null, "lastErrorCode": "NO_CONSENT", "lastAttemptAt": null, "cancelReason": null, "attempts": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/notifications/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.get. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.get as defined in06.

Repository methods involved: NotificationLogRepository.findById.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/notifications/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 08D POST /api/notifications/{id}/delete

Purpose: Hard delete only unattempted QUEUED/SKIPPED/CANCELLED rows with attemptCount0, by OFFICER/ADMIN; record AuditEvent. Otherwise409 IMMUTABLE_HISTORY. Cancellation of queued reminder is explicit and prevents sending. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op08DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

Success: HTTP204; no body.

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/notifications/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.delete. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.delete as defined in06.

Repository methods involved: NotificationLogRepository.findById, NotificationLogRepository.delete, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/notifications/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 09C POST /api/audit-events

Purpose: Append-only actor/action/change record including non-stock decisions. Permission: OFFICER/ADMIN; NOTE only through public create.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op09CRequest): {"targetType": "Project", "targetId": "{{projectId}}", "reason": "Fictional planning note"}

Success: HTTP201; AuditEventView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "action": "NOTE", "targetType": "Project", "targetId": 1, "targetSummary": "WELCOME_26", "beforeJson": null, "afterJson": null, "reason": "Fictional planning note", "requestId": "00000000-0000-4000-8000-000000000002"}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 DUPLICATE_RECORD, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DUPLICATE_RECORD", "message": "A record with this unique reference already exists.", "path": "/api/audit-events", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AuditController.appendNote. Autowired component: AuditComponent. Forward exactly to AuditComponent.appendNote as defined in06.

Repository methods involved: AuditEventRepository.save, AuditEventRepository.findById, AuditEventRepository.findByTargetTypeAndTargetIdOrderByIdAsc.

Business rules and ordered method behavior: 1. Check OFFICER/ADMIN and restrict public action to NOTE. 2. Resolve allowlisted target, generate targetSummary/requestId and reject supplied before/after/actor/time fields. 3. Persist immutable note with reason; do not recursively audit the AuditEvent insertion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/audit-events with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 09L GET /api/audit-events

Purpose: List visible audit-events with bounded filters. Permission: authorized organization read; contact/audit/notification scope rules apply.

Path parameters: none. Query parameters: {"targetType": "optional exact/range filter", "targetId": "optional exact/range filter", "from": "optional exact/range filter", "to": "optional exact/range filter", "page": "integer>=0 default0", "size": "1..100 default20"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op09LRequest): none; GET has no body.

Success: HTTP200; Page<AuditEventView> with complete shape above. Example: {"content": [{"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": null, "action": "NOTE", "targetType": "Project", "targetId": 1, "targetSummary": "WELCOME_26", "beforeJson": null, "afterJson": null, "reason": "Fictional planning note", "requestId": "00000000-0000-4000-8000-000000000002"}], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/audit-events", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AuditController.list. Autowired component: AuditComponent. Forward exactly to AuditComponent.list as defined in06.

Repository methods involved: AuditEventRepository.findAll.

Business rules and ordered method behavior: 1. Validate pagination and whitelist filter names/types; apply visibility restrictions in the query. 2. Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/audit-events with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 09R GET /api/audit-events/{id}

Purpose: Retrieve one AuditEvent with its derived public fields. Permission: same read scope as list.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op09RRequest): none; GET has no body.

Success: HTTP200; AuditEventView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "action": "NOTE", "targetType": "Project", "targetId": 1, "targetSummary": "WELCOME_26", "beforeJson": null, "afterJson": null, "reason": "Fictional planning note", "requestId": "00000000-0000-4000-8000-000000000002"}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/audit-events/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AuditController.get. Autowired component: AuditComponent. Forward exactly to AuditComponent.get as defined in06.

Repository methods involved: AuditEventRepository.findById.

Business rules and ordered method behavior: 1. Validate positive id and active actor; retrieve or404. 2. Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/audit-events/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## 09D POST /api/audit-events/{id}/delete

Purpose: Never physically delete audit history. Delete endpoint returns409 IMMUTABLE_HISTORY. A correction is a new NOTE referencing the original id in its reason, not editing the original. Permission: ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (Op09DRequest): {"expectedVersion": 0, "reason": "Remove unused fictional setup record"}

No successful deletion is permitted for posted history. Documented response: HTTP409 Error JSON. Example: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "IMMUTABLE_HISTORY", "message": "Posted history cannot be deleted; use a new correction or reversal.", "path": "/api/audit-events/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 DEPENDENCY_EXISTS, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RETRY_REQUIRED, 409 IMMUTABLE_HISTORY. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "DEPENDENCY_EXISTS", "message": "Related records prevent deletion; use the documented lifecycle action.", "path": "/api/audit-events/1/delete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AuditController.delete. Autowired component: AuditComponent. Forward exactly to AuditComponent.delete as defined in06.

Repository methods involved: AuditEventRepository.findById, AuditEventRepository.delete.

Business rules and ordered method behavior: 1. Resolve target and authorize; require expectedVersion for versioned rows. 2. Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT. 3. If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative. 4. For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/audit-events/1/delete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## P01 POST /api/projects/{id}/submit

Purpose: Move internal project to SUBMITTED. Permission: owner/HEAD.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpP01Request): {"expectedVersion": 0, "reason": "Fictional internal planning decision"}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "SUBMITTED", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RESOURCE_OVERALLOCATION, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/projects/1/submit", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.submit. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.submit as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state. 2. Require DRAFT, at least one active valid requirement and all schedules within project. 3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/submit with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## P02 POST /api/projects/{id}/approve

Purpose: Move internal project to APPROVED. Permission: OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpP02Request): {"expectedVersion": 0, "reason": "Fictional internal planning decision"}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "APPROVED", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RESOURCE_OVERALLOCATION, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/projects/1/approve", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.approve. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.approve as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state. 2. Require SUBMITTED; internal planning approval does not approve university funding or allocate inventory. 3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/approve with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## P03 POST /api/projects/{id}/return-draft

Purpose: Move internal project to DRAFT. Permission: OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpP03Request): {"expectedVersion": 0, "reason": "Fictional internal planning decision"}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "DRAFT", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RESOURCE_OVERALLOCATION, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/projects/1/return-draft", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.returnDraft. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.returnDraft as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state. 2. Require SUBMITTED, reason, and no approved allocations; draft return is an internal decision. 3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/return-draft with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## P04 POST /api/projects/{id}/start

Purpose: Move internal project to IN_PROGRESS. Permission: owner/HEAD or OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpP04Request): {"expectedVersion": 0, "reason": "Fictional internal planning decision"}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "IN_PROGRESS", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RESOURCE_OVERALLOCATION, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/projects/1/start", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.start. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.start as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state. 2. Require APPROVED and startAt <= now < endAt. 3. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/start with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## P05 POST /api/projects/{id}/complete

Purpose: Move internal project to COMPLETED. Permission: OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpP05Request): {"expectedVersion": 0, "reason": "Fictional internal planning decision", "waiveUnfulfilled": false}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "COMPLETED", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RESOURCE_OVERALLOCATION, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/projects/1/complete", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.complete. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.complete as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state. 2. Require IN_PROGRESS or APPROVED; no REQUESTED allocation, no unissued approved remainder or outstanding custody. 3. For unfulfilled demand require waiveUnfulfilled=true plus a reason; write waived quantities in audit, not stock. 4. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/complete with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## P06 POST /api/projects/{id}/cancel

Purpose: Move internal project to CANCELLED. Permission: owner/HEAD for DRAFT/SUBMITTED; OFFICER for APPROVED/IN_PROGRESS.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpP06Request): {"expectedVersion": 0, "reason": "Fictional internal planning decision"}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "CANCELLED", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 VERSION_CONFLICT, 409 RESOURCE_OVERALLOCATION, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/projects/1/cancel", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.cancel. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.cancel as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state. 2. Require not COMPLETED/CANCELLED and reason; lock complete affected resource set in ascending order. 3. Set REQUESTED allocations CANCELLED; approved with no supply become CANCELLED, partially supplied approvals release their remaining R. Preserve reusable custody and allow return/loss afterward. 4. Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/cancel with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## P07 POST /api/projects/{id}/reschedule

Purpose: Atomically replace full unissued project schedule. Permission: OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpP07Request): {"expectedVersion": 0, "startAt": "{{startA}}", "endAt": "{{endA}}", "requirements": [{"requirementId": "{{requirementId}}", "startAt": "{{startA}}", "endAt": "{{endA}}"}], "reason": "Fictional schedule change"}

Success: HTTP200; ProjectView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "status": "DRAFT", "statusReason": null}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 SCHEDULE_LOCKED, 409 VERSION_CONFLICT, 409 RESOURCE_OVERALLOCATION, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "SCHEDULE_LOCKED", "message": "A recorded issue or consumption prevents schedule replacement.", "path": "/api/projects/1/reschedule", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ProjectController.reschedule. Autowired component: ProjectComponent. Forward exactly to ProjectComponent.reschedule as defined in06.

Repository methods involved: ProjectRepository.findById, ProjectRepository.save, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Project_Id, ResourceRepository.lockById, AuditEventRepository.save, AllocationRepository.save, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Read affected requirements/resources; sort IDs and lock all parents before rows. Re-read complete graph and versions. 2. Require no issue/consumption ever recorded for any allocation of this project (including reversed events); reject incomplete/extra requirement-window IDs. 3. Validate new project/requirement intervals and construct proposed snapshots for every requested/approved allocation. 4. Exclude exactly the old affected commitments, then sweep all proposed replacements together with all unaffected and overdue holds; reject any deficit. 5. Save all schedules and audit old/new windows, bump resource revisions; entire command rolls back on any conflict.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/projects/1/reschedule with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## A01 POST /api/allocations/{id}/approve

Purpose: Commit requested quantity without moving stock. Permission: assigned CUSTODIAN different from requester.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpA01Request): {"expectedVersion": 0, "reason": "Custodian checked capacity"}

Success: HTTP200; AllocationView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "requirementId": 1, "quantity": 6, "releasedUnissued": 0, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "APPROVED", "decidedById": 2, "decidedAt": "2026-11-03T02:00:00Z", "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 6, "fulfillment": "NONE", "overdue": false, "replay": false}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 INVALID_STATE, 409 RESOURCE_OVERALLOCATION, 409 REQUIREMENT_COVERAGE_EXCEEDED, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/allocations/1/approve", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.approve. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.approve as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve pool; acquire resource then requirement/allocation locks and re-read actor, expectedVersion and REQUESTED state. 2. Verify active requirement/resource and approved/in-progress project; requester/recipient references remain active. 3. Require endAt > Clock.instant(); a current interval is allowed, a wholly past commitment is not. 4. Sum existing approved effective quantities for this requirement; reject if adding quantity exceeds demand. 5. Rebuild balances and current pool commitments including overdue outstanding. Sweep reusable interval or compute global consumable ATP. 6. Require the whole requested quantity fits; no silent partial approval. If not, keep REQUESTED and return409 with available/competing IDs. 7. Set APPROVED, decidedBy/At/reason; increment resource revision and audit. No SMS is sent for every approval.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/approve with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## A02 POST /api/allocations/{id}/reject

Purpose: Reject a pending request without deleting history. Permission: assigned CUSTODIAN different from requester.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpA02Request): {"expectedVersion": 0, "reason": "Request needs a revised quantity"}

Success: HTTP200; AllocationView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "requirementId": 1, "quantity": 6, "releasedUnissued": 0, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "REJECTED", "decidedById": 2, "decidedAt": "2026-11-03T02:00:00Z", "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 0, "fulfillment": "NONE", "overdue": false, "replay": false}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/allocations/1/reject", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.reject. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.reject as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Lock resource and re-read request; verify actor/version and REQUESTED state. 2. Require reason; set REJECTED with decidedBy/At and audit. It held no quantity to release.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/reject with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## A03 POST /api/allocations/{id}/cancel

Purpose: Cancel pending or entirely unissued approved allocation. Permission: requester/own HEAD, assigned CUSTODIAN or OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpA03Request): {"expectedVersion": 0, "reason": "Project no longer needs this pending commitment"}

Success: HTTP200; AllocationView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "requirementId": 1, "quantity": 6, "releasedUnissued": 0, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "CANCELLED", "decidedById": null, "decidedAt": null, "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 0, "fulfillment": "NONE", "overdue": false, "replay": false}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/allocations/1/cancel", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.cancel. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.cancel as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Lock pool and re-read version/state. 2. Allow REQUESTED or APPROVED only; approved cancellation requires issued/consumed net0 and no outstanding. 3. Set CANCELLED and reason, releasing effective approved commitment; bump resource revision and audit. Rejected and resolved history is not cancelled.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/cancel with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## A04 POST /api/allocations/{id}/release

Purpose: Release some or all unissued approved remainder. Permission: requester/own HEAD, assigned CUSTODIAN or OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpA04Request): {"expectedVersion": 0, "quantity": 1, "reason": "Unused part no longer required"}

Success: HTTP200; AllocationView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "requirementId": 1, "quantity": 6, "releasedUnissued": 1, "startAt": "2026-11-03T02:00:00Z", "endAt": "2026-11-03T02:00:00Z", "requestedById": 1, "recipientId": 1, "decisionStatus": "APPROVED", "decidedById": null, "decidedAt": null, "decisionReason": null, "requestKey": "00000000-0000-4000-8000-000000000001", "issuedQuantity": 0, "resolvedQuantity": 0, "outstandingQuantity": 0, "unissuedQuantity": 5, "fulfillment": "NONE", "overdue": false, "replay": false}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/allocations/1/release", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.release. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.release as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Lock pool and re-read APPROVED allocation/version and ledger counters. 2. Require positive quantity <= R; never release issued or consumed coverage. 3. Increase releasedUnissued, retain approved historical supply, bump version and audit exact released amount and reason.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/release with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## A05 POST /api/allocations/{id}/issue

Purpose: Record ISSUE against this allocation. Permission: assigned CUSTODIAN.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpA05Request): {"quantity": 1, "reason": "Fictional issue observation", "commandKey": "{{commandKey}}"}

Success: HTTP201; InventoryTransactionView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "resourceId": 1, "allocationId": 1, "type": "ISSUE", "quantity": 1, "inspection": null, "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": -1, "deltaOutstanding": 1, "deltaDamaged": 0, "deltaIssued": 1, "deltaResolved": 0, "deltaGoodReturned": 0, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null, "resourceRevisionAfter": 2, "replay": false, "impactedAllocationIds": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 RESOURCE_TYPE_MISMATCH, 409 INSUFFICIENT_PHYSICAL_STOCK, 409 RESOURCE_OVERALLOCATION, 409 IDEMPOTENCY_KEY_REUSED, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/allocations/1/issue", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.issueResources. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.issueResources as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save, InventoryTransactionRepository.save, InventoryTransactionRepository.findByResource_IdAndCommandKey.

Business rules and ordered method behavior: 1. Resolve allocation/pool; call InventoryTransactionComponent.record with server-filled resource/allocation/type fields, preserving the commandKey. 2. Acquire resource lock and perform identical-key replay check. 3. Require reusable and project APPROVED/IN_PROGRESS; startAt <= now < endAt; q <= unissued R. 4. Require physical S >= q plus other active-now unissued commitments; report conflicts if not. 5. Derive immutable signed movement, recheck balances/counters, save with new resource revision and append audit in one transaction.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/issue with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## A06 POST /api/allocations/{id}/return

Purpose: Record RETURN against this allocation. Permission: assigned CUSTODIAN.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpA06Request): {"quantity": 1, "reason": "Fictional return observation", "commandKey": "{{commandKey}}", "inspection": "USABLE"}

Success: HTTP201; InventoryTransactionView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "resourceId": 1, "allocationId": 1, "type": "RETURN", "quantity": 1, "inspection": "USABLE", "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": 1, "deltaOutstanding": -1, "deltaDamaged": 0, "deltaIssued": 0, "deltaResolved": 1, "deltaGoodReturned": 1, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null, "resourceRevisionAfter": 2, "replay": false, "impactedAllocationIds": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 RESOURCE_TYPE_MISMATCH, 409 INSUFFICIENT_PHYSICAL_STOCK, 409 RESOURCE_OVERALLOCATION, 409 IDEMPOTENCY_KEY_REUSED, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/allocations/1/return", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.returnResources. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.returnResources as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save, InventoryTransactionRepository.save, InventoryTransactionRepository.findByResource_IdAndCommandKey.

Business rules and ordered method behavior: 1. Resolve allocation/pool; call InventoryTransactionComponent.record with server-filled resource/allocation/type fields, preserving the commandKey. 2. Acquire resource lock and perform identical-key replay check. 3. Require reusable, outstanding >= q and inspection USABLE or DAMAGED; allow returns after project cancellation/end. 4. Apply O-to-S for usable or O-to-B for damaged; update resolution counters and include impacted commitments for damaged stock. 5. Derive immutable signed movement, recheck balances/counters, save with new resource revision and append audit in one transaction.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/return with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## A07 POST /api/allocations/{id}/consume

Purpose: Record CONSUMPTION against this allocation. Permission: assigned CUSTODIAN.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpA07Request): {"quantity": 1, "reason": "Fictional consume observation", "commandKey": "{{commandKey}}"}

Success: HTTP201; InventoryTransactionView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "resourceId": 1, "allocationId": 1, "type": "CONSUMPTION", "quantity": 1, "inspection": null, "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": -1, "deltaOutstanding": 0, "deltaDamaged": 0, "deltaIssued": 1, "deltaResolved": 0, "deltaGoodReturned": 0, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null, "resourceRevisionAfter": 2, "replay": false, "impactedAllocationIds": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 RESOURCE_TYPE_MISMATCH, 409 INSUFFICIENT_PHYSICAL_STOCK, 409 RESOURCE_OVERALLOCATION, 409 IDEMPOTENCY_KEY_REUSED, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/allocations/1/consume", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: AllocationController.consumeResources. Autowired component: AllocationComponent. Forward exactly to AllocationComponent.consumeResources as defined in06.

Repository methods involved: AllocationRepository.findById, AllocationRepository.save, AllocationRepository.findByRequirement_Id, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, ResourceRepository.lockById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AuditEventRepository.save, InventoryTransactionRepository.save, InventoryTransactionRepository.findByResource_IdAndCommandKey.

Business rules and ordered method behavior: 1. Resolve allocation/pool; call InventoryTransactionComponent.record with server-filled resource/allocation/type fields, preserving the commandKey. 2. Acquire resource lock and perform identical-key replay check. 3. Require consumable, project APPROVED/IN_PROGRESS, within its requirement interval and q <= unconsumed R. 4. Require physical P >= q plus all other remaining consumable commitments. Reduce P and own reservation equally; no issue-then-consume duplication. 5. Derive immutable signed movement, recheck balances/counters, save with new resource revision and append audit in one transaction.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/allocations/1/consume with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## T01 POST /api/inventory-transactions/{id}/reverse

Purpose: Append constrained reversal of a latest event. Permission: OFFICER.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpT01Request): {"reason": "Correct latest erroneous fictional event", "commandKey": "{{commandKey}}"}

Success: HTTP201; InventoryTransactionView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "resourceId": 1, "allocationId": null, "type": "REVERSAL", "quantity": 10, "inspection": null, "reason": "OPENING_COUNT fictional stock", "occurredAt": "2026-11-03T02:00:00Z", "deltaStored": -10, "deltaOutstanding": 0, "deltaDamaged": 0, "deltaIssued": 0, "deltaResolved": 0, "deltaGoodReturned": 0, "deltaDamagedReturned": 0, "deltaLost": 0, "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": 1, "correctionReferenceId": null, "resourceRevisionAfter": 2, "replay": false, "impactedAllocationIds": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 REVERSAL_DEPENDENCY_CONFLICT, 409 IDEMPOTENCY_KEY_REUSED, 409 RESOURCE_OVERALLOCATION, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "REVERSAL_DEPENDENCY_CONFLICT", "message": "Later activity prevents safe reversal of this event.", "path": "/api/inventory-transactions/1/reverse", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: InventoryTransactionController.reverse. Autowired component: InventoryTransactionComponent. Forward exactly to InventoryTransactionComponent.reverse as defined in06.

Repository methods involved: InventoryTransactionRepository.findById, InventoryTransactionRepository.findByReversalOf_Id, InventoryTransactionRepository.findByResource_IdAndCommandKey, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, InventoryTransactionRepository.save, ResourceRepository.lockById, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Resolve original and lock its resource; verify original is not REVERSAL and no existing reversal. 2. Check commandKey replay; require original.resourceRevisionAfter equals current resource.revision and no later ledger event; otherwise409 dependency conflict. 3. Negate all original signed deltas and allocation counter effects; recheck balances, coverage, lifecycle and commitment capacity. 4. Insert REVERSAL linked to original, increment resource revision/capture it after flush, append correction audit; never modify original.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/inventory-transactions/1/reverse with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## N01 POST /api/notifications/{id}/cancel

Purpose: Cancel a reminder before any send begins. Permission: recipient for own, assigned CUSTODIAN, OFFICER or ADMIN.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpN01Request): {"expectedVersion": 0, "reason": "Reminder no longer needed"}

Success: HTTP200; NotificationLogView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "CANCELLED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": null, "attemptCount": 0, "destinationMasked": null, "providerSid": null, "providerStatus": null, "lastErrorCode": "NO_CONSENT", "lastAttemptAt": null, "cancelReason": "Reminder no longer needed", "attempts": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/notifications/1/cancel", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.cancel. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.cancel as defined in06.

Repository methods involved: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Lock notification and re-read version; only QUEUED/SKIPPED with attemptCount0 can cancel. 2. Set CANCELLED and reason, clear nextAttemptAt, audit; SENDING/accepted/unknown cannot be recalled.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/notifications/1/cancel with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## N02 POST /api/notifications/{id}/dispatch

Purpose: Run one due outbox record through the same dispatcher as scheduler. Permission: OFFICER or ADMIN; restricted live allowlist.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpN02Request): {}

Success: HTTP200; NotificationLogView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "SKIPPED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": null, "attemptCount": 0, "destinationMasked": null, "providerSid": null, "providerStatus": null, "lastErrorCode": "NO_CONSENT", "lastAttemptAt": null, "cancelReason": null, "attempts": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 PROVIDER_OUTCOME_UNKNOWN, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/notifications/1/dispatch", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.dispatch. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.dispatch as defined in06.

Repository methods involved: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save, AppUserRepository.findById, AllocationRepository.findById, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Call claim(id,asOf) in a short transaction; recheck QUEUED,due,consent,recipient and outstanding custody. 2. After committing SENDING and an attempt marker, perform Retrofit send outside any DB transaction. 3. Call finish(id,result) in a new transaction; bounded retry only on known not-accepted outcome, unknown outcomes never auto-resend.

Transaction: SPLIT. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/notifications/1/dispatch with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## N03 POST /api/notifications/{id}/refresh

Purpose: Fetch provider status for a known SID; no new message. Permission: recipient for own, OFFICER or ADMIN.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpN03Request): {}

Success: HTTP200; NotificationLogView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "DELIVERED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": null, "attemptCount": 1, "destinationMasked": null, "providerSid": "SMaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "providerStatus": "delivered", "lastErrorCode": null, "lastAttemptAt": null, "cancelReason": null, "attempts": [{"ordinal": 1, "attemptedAt": "2026-11-03T02:00:00Z", "outcome": "ACCEPTED", "errorCode": null, "providerSid": "SMaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}]}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 INVALID_STATE, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/notifications/1/refresh", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.refresh. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.refresh as defined in06.

Repository methods involved: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save, NotificationLogRepository.findById.

Business rules and ordered method behavior: 1. Read visible log; require LIVE profile and stored providerSid, or FAKE fixture SID in fake mode. 2. Fetch exact SID through TwilioIntegrationComponent.fetchStatus outside DB transaction; require SID matches. 3. Lock log and persist mapped status/error in a fresh transaction; never regress terminal delivery or enqueue a new send.

Transaction: SPLIT. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/notifications/1/refresh with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## N04 POST /api/notifications/{id}/retry

Purpose: Queue a known not-accepted failure within bounded attempts. Permission: OFFICER or ADMIN.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpN04Request): {"expectedVersion": 0, "reason": "Provider definitively rejected without accepting"}

Success: HTTP200; NotificationLogView with complete shape above. Example: {"id": 1, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 1, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1, "allocationId": 1, "recipientId": 1, "kind": "RETURN_DUE", "eventKey": "RETURN_DUE:1:2026-11-10T04:00:00Z:1", "status": "QUEUED", "dueAt": "2026-11-03T02:00:00Z", "nextAttemptAt": "2026-11-03T02:01:00Z", "attemptCount": 1, "destinationMasked": null, "providerSid": null, "providerStatus": null, "lastErrorCode": "20429", "lastAttemptAt": null, "cancelReason": null, "attempts": [{"ordinal": 1, "attemptedAt": "2026-11-03T02:00:00Z", "outcome": "REJECTED_NOT_ACCEPTED", "errorCode": "20429", "providerSid": null}]}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND, 409 VERSION_CONFLICT, 409 INVALID_STATE, 409 PROVIDER_OUTCOME_UNKNOWN, 409 RETRY_REQUIRED. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "VERSION_CONFLICT", "message": "The record changed; retrieve its current version before deciding again.", "path": "/api/notifications/1/retry", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: NotificationController.retry. Autowired component: NotificationComponent. Forward exactly to NotificationComponent.retry as defined in06.

Repository methods involved: NotificationLogRepository.lockById, NotificationLogRepository.save, AuditEventRepository.save.

Business rules and ordered method behavior: 1. Lock log/version; allow FAILED only with providerSid null and a stored allowlisted known-not-accepted error; attempts <3. 2. Revalidate consent, due kind and outstanding custody; set QUEUED,nextAttemptAt to now plus backoff; audit. 3. UNKNOWN, ACCEPTED, DELIVERED and UNDELIVERED cannot be retried as the same event; no blind duplicate SMS.

Transaction: WRITE. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/notifications/1/retry with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## Q01 GET /api/availability

Purpose: Explain present/future capacity for one resource and interval. Permission: organization read.

Path parameters: none. Query parameters: {"resourceId": "required positive Long", "startAt": "required offset instant >=asOf", "endAt": "required offset instant > startAt", "candidateQuantity": "optional positive Long to compute return dependence"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpQ01Request): none; GET has no body.

Success: HTTP200; AvailabilityView with complete shape above. Example: {"resourceId": 1, "asOf": "2026-11-10T00:00:00Z", "startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T05:00:00Z", "unit": "piece", "physicalInStore": 10, "outstanding": 0, "usableOwned": 10, "reservedConsumable": null, "rawMinimumFree": 4, "allocatableQuantity": 4, "overdueOutstanding": 0, "returnDependentQuantity": 0, "segments": [{"startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T04:00:00Z", "committed": 6, "rawFree": 4, "allocationIds": [1]}], "warnings": []}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/availability", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: PlanningController.query. Autowired component: AvailabilityComponent. Forward exactly to AvailabilityComponent.query as defined in06.

Repository methods involved: ResourceRepository.findById, InventoryTransactionRepository.findByResource_IdOrderByIdAsc, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus.

Business rules and ordered method behavior: 1. Validate active resource and present/future <=90-day interval; capture asOf once. 2. Read ledger plus all approved resource allocations in one repeatable-read snapshot, derive balances and commitment intervals. 3. Sweep grouped endpoints or calculate consumable ATP; return raw negative deficit, clamped allocatable capacity, conflicting IDs and return-dependent warning.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/availability with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## Q02 GET /api/shortages/projects/{id}

Purpose: Explain each project requirement coverage and conditional sourcing gap. Permission: project read scope.

Path parameters: id positive Long, target record identity. Query parameters: none.

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpQ02Request): none; GET has no body.

Success: HTTP200; ShortageView with complete shape above. Example: {"projectId": 1, "asOf": "2026-11-10T00:00:00Z", "requirements": [{"requirementId": 1, "resourceId": 1, "unit": "piece", "demand": 6, "approvedCoverage": 0, "supplied": 0, "outstanding": 0, "unallocated": 6, "freeIfConsideredAlone": 10, "sourcingGapIfConsideredAlone": 0, "atRisk": false}], "warning": "Individual candidate gaps are not additive."}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/shortages/projects/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ShortageController.projectSummary. Autowired component: ShortageComponent. Forward exactly to ShortageComponent.projectSummary as defined in06.

Repository methods involved: ProjectRepository.findById, ResourceRequirementRepository.findByProject_IdOrderByIdAsc, AllocationRepository.findByRequirement_Id, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Load project and all active requirements in a consistent read snapshot. 2. For each resource calculate remaining demand and available candidate capacity; do not remove existing approved commitments except when measuring already-covered demand. 3. Return unallocated quantities, physical supply and risk flags separately; never sum isolated candidate gaps.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/shortages/projects/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## Q03 GET /api/shortages/resources/{id}

Purpose: Consolidate competing departments and peak sourcing capacity. Permission: organization read.

Path parameters: id positive Long, target record identity. Query parameters: {"to": "required offset instant max90days", "includeDraft": "boolean default false", "from": "required offset instant >=asOf; actual query key is from"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpQ03Request): none; GET has no body.

Success: HTTP200; DemandView with complete shape above. Example: {"resourceId": 1, "asOf": "2026-11-10T00:00:00Z", "to": "2026-11-10T06:00:00Z", "includeDraft": false, "unit": "piece", "minimumAdditionalUnits": 1, "segments": [{"startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T04:00:00Z", "usableCapacity": 10, "approvedCommitted": 6, "unallocatedDemand": 5, "deficit": 1, "requirementIds": [1, 2], "projectIds": [1, 2], "departmentIds": [1, 2]}], "reasons": ["TIME_CONFLICT"], "warning": "Capacity scenario with fixed dates; not purchasing authorization.", "from": "2026-11-10T01:00:00Z"}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 404 NOT_FOUND. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 404, "error": "Not Found", "code": "NOT_FOUND", "message": "The referenced record does not exist.", "path": "/api/shortages/resources/1", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ShortageController.consolidate. Autowired component: ShortageComponent. Forward exactly to ShortageComponent.consolidate as defined in06.

Repository methods involved: ResourceRepository.findById, ResourceRequirementRepository.findByResource_IdAndStatus, AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Validate bounded horizon and resource; load active requirements and eligible project states. 2. Compute unapproved N per requirement, existing K commitments and ledger capacity in one snapshot. 3. For reusable sweep K plus all N intervals together; for consumable subtract all reserved stock globally and sum selected unallocated demand. 4. Return peak units and affected segments/projects/departments with reason flags; no allocation or procurement side effect.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/shortages/resources/1 with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## R01 GET /api/reports/inventory

Purpose: Current explainable ledger stock by resource. Permission: organization report read; contact data excluded.

Path parameters: none. Query parameters: {"departmentId": "optional positive Long", "page": "default0", "size": "1..100"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpR01Request): none; GET has no body.

Success: HTTP200; InventoryReport with complete shape above. Example: {"asOf": "2026-11-10T00:00:00Z", "items": [{"resourceId": 1, "type": "REUSABLE", "unit": "piece", "physicalInStore": 10, "outstanding": 0, "damaged": 0, "usableOwned": 10, "ledgerConsistent": true}]}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/reports/inventory", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ReportController.inventory. Autowired component: ReportingComponent. Forward exactly to ReportingComponent.inventory as defined in06.

Repository methods involved: ResourceRepository.findAll, InventoryTransactionRepository.findByResource_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Load bounded resource page and ledger rows; reconstruct balances including reversal deltas. 2. Return each pool/unit separately and invariant/reconciliation flags; never repair automatically.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/reports/inventory with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## R02 GET /api/reports/upcoming

Purpose: Approved allocations due in a selected future window. Permission: organization report read; contact data excluded.

Path parameters: none. Query parameters: {"to": "offset instant", "departmentId": "optional Long", "page": "default0", "size": "1..100", "from": "offset instant query key from"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpR02Request): none; GET has no body.

Success: HTTP200; AllocationReport with complete shape above. Example: {"asOf": "2026-11-10T00:00:00Z", "items": [{"allocationId": 1, "projectId": 1, "resourceId": 1, "recipientId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "unissued": 0, "outstanding": 2, "overdue": true, "atRisk": false}]}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/reports/upcoming", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ReportController.upcoming. Autowired component: ReportingComponent. Forward exactly to ReportingComponent.upcoming as defined in06.

Repository methods involved: AllocationRepository.findAll, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Validate present/future max90-day window and permitted department filter. 2. Load approved allocations through Specification, derive unissued/outstanding and current risk; paginate by id.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/reports/upcoming with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## R03 GET /api/reports/overdue

Purpose: Outstanding reusable custody past its expected end. Permission: organization report read; contact data excluded.

Path parameters: none. Query parameters: {"resourceId": "optional Long", "departmentId": "optional Long", "page": "default0", "size": "1..100"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpR03Request): none; GET has no body.

Success: HTTP200; AllocationReport with complete shape above. Example: {"asOf": "2026-11-10T00:00:00Z", "items": [{"allocationId": 1, "projectId": 1, "resourceId": 1, "recipientId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "unissued": 0, "outstanding": 2, "overdue": true, "atRisk": false}]}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/reports/overdue", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ReportController.overdue. Autowired component: ReportingComponent. Forward exactly to ReportingComponent.overdue as defined in06.

Repository methods involved: AllocationRepository.findAll, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Capture now; query approved reusable allocations with endAt <= now through Specification. 2. Derive outstanding from ledger and retain only >0; paginate after filtering so totalElements is accurate.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/reports/overdue with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## R04 GET /api/reports/department-demand

Purpose: Observed planned demand by resource for one department/horizon. Permission: organization report read; contact data excluded.

Path parameters: none. Query parameters: {"departmentId": "required Long", "to": "offset instant", "from": "offset instant query key from"}

Headers: Accept application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpR04Request): none; GET has no body.

Success: HTTP200; DepartmentReport with complete shape above. Example: {"asOf": "2026-11-10T00:00:00Z", "departmentId": 1, "to": "2026-11-11T01:00:00Z", "items": [{"resourceId": 1, "unit": "piece", "requested": 6, "approvedCoverage": 4, "supplied": 0, "unallocated": 2}], "warning": "Different units are never summed.", "from": "2026-11-10T01:00:00Z"}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 400, "error": "Bad Request", "code": "VALIDATION_FAILED", "message": "One or more fields failed validation.", "path": "/api/reports/department-demand", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: ReportController.departmentDemand. Autowired component: ReportingComponent. Forward exactly to ReportingComponent.departmentDemand as defined in06.

Repository methods involved: DepartmentRepository.findById, ResourceRequirementRepository.findAll, AllocationRepository.findByRequirement_Id, InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc.

Business rules and ordered method behavior: 1. Validate department and max90-day horizon. 2. Load eligible requirements/projects, aggregate requested/approved/supplied by resource/unit, with no mixed-unit grand total.

Transaction: READ. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: GET http://127.0.0.1:8080/api/reports/department-demand with the headers/body above and query keys if specified. No calls have been executed against an implementation.

## J01 POST /api/jobs/reminders/run-once

Purpose: Deterministically run reminder scan once without changing clock. Permission: ADMIN/OFFICER, DEMO profile only.

Path parameters: none. Query parameters: none.

Headers: Accept application/json; Content-Type application/json; X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.

Request JSON (OpJ01Request): {}

Success: HTTP200; JobRunView with complete shape above. Example: {"asOf": "2026-11-10T00:00:00Z", "created": 1, "skipped": 0, "existing": 0, "errors": 0, "providerMode": "FAKE"}

Errors: 400 BAD_REQUEST, 400 VALIDATION_FAILED, 401 UNAUTHENTICATED, 403 FORBIDDEN, 500 INTERNAL_ERROR, 409 INVALID_STATE. Common message catalogue and Error schema apply. Example failure: {"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "INVALID_STATE", "message": "The current state does not permit this action.", "path": "/api/jobs/reminders/run-once", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {}}

Controller: SchedulingController.runOnce. Autowired component: SchedulingComponent. Forward exactly to SchedulingComponent.runOnce as defined in06.

Repository methods involved: AllocationRepository.findAll, NotificationLogRepository.findByEventKey, NotificationLogRepository.save.

Business rules and ordered method behavior: 1. Require DEMO profile; capture Clock, invoke NotificationComponent.scanReminders(asOf). 2. Return enqueue/skip/existing/error counts; no implicit dispatch or physical stock change. 3. LIVE authenticated deployment does not register this demonstration route.

Transaction: SPLIT. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: POST http://127.0.0.1:8080/api/jobs/reminders/run-once with the headers/body above and query keys if specified. No calls have been executed against an implementation.

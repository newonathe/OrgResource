# OrgResource Project Proposal

Student Organization Resource Planning and Allocation System

## Group members

1. [Full name and student number]
2. [Full name and student number or remove]
3. [Full name and student number or remove]

## Application description

OrgResource helps one Ateneo student organization coordinate resources across departments. A project records what it needs and when. The resource custodian checks existing stock and commitments before approving an allocation. The system then records reusable items issued and returned, consumables used, and any remaining shortage. SMS reminders help recipients return reusable items on time.

The system manages only organization-owned or authorized resources. It does not handle payments, official procurement or university facility reservations. A requirement states what a project needs; an allocation states what has been committed; a transaction records what physically happened.

## Controller operations

Controllers receive requests and forward them through Autowired to the component named in each table. The controller method has the same name as the forwarded method. GET reads data; POST submits an action. All URLs begin at http://127.0.0.1:8080.

### Inputs and JSON outputs

Requests and responses use JSON. Path id means the positive Long ID of the record. Other IDs identify existing records. Date-times include an offset, for example 2026-11-10T09:00:00+08:00. The acting user is identified by X-Demo-User-Id in the local demonstration. Role and ownership checks are made by the component.

expectedVersion is the version from the latest record read; a different current version prevents overwriting a change. requestKey and commandKey are unique UUID strings for a new action; retrying the same action uses the same key so it is not recorded twice. A reused key with different input is rejected. Examples show format, not a sequence using fixed IDs.

Create returns201 and the created record; update/action/read returns200. An identical allocation or transaction retry returns200 without repeating the change. A successful delete returns204 with no body. Protected history returns409 instead of being deleted.

A single-record output uses its entity fields listed in the final section. Related records appear as IDs, such as projectId instead of a nested Project. payloadHash is not returned. Fields set by the system cannot be supplied as input. Example Department JSON:

{"id": 1, "code": "LOG", "name": "Logistics", "description": "Handles shared resources", "active": true, "createdAt": "2026-11-03T02:00:00Z", "createdById": 1, "version": 0, "updatedAt": "2026-11-03T02:00:00Z", "updatedById": 1}

A list returns {"content":[record],"page":0,"size":20,"totalElements":1,"totalPages":1}. With no matches, content is empty. Record means the JSON for the named entity, not a quoted string.

Resource JSON also includes physicalInStore, outstanding, damagedInStore, usableOwned, reservedConsumable and availableConsumable. Requirement JSON adds coveredQuantity, suppliedQuantity, outstandingQuantity and unallocatedQuantity. Allocation JSON adds issuedQuantity, resolvedQuantity, outstandingQuantity, unissuedQuantity, fulfillment, overdue and replay. Transaction JSON adds replay and impactedAllocationIds. These quantities are calculated from saved records. Phone/consent details are returned only to the user or administrator.

fulfillment is NONE, PARTIAL, OUTSTANDING, RESOLVED, RELEASED or EXPIRED_UNISSUED. It describes progress and cannot be edited. overdue is true when reusable custody remains after its expected return time.

### Request bodies

The body label in each operation table identifies its JSON below. Fields have the types and limits listed under entities; expectedVersion is a nonnegative Long and reason is text up to1000 characters. A reason is required for rejection, cancellation, release, adjustment, correction or waiving unmet demand. {} accepts no fields. Omitted optional update fields stay unchanged; null clears only a nullable field.

Bodies 01C: {"code": "LOG", "name": "Logistics", "description": "Fictional demonstration department"}

Bodies 01D, 02D, 03D, 04D, 05D, 06D, 07D, 08D, 09D, P01, P02, P03, P04, P06, A01, A02, A03, N01, N04: {"expectedVersion": 0, "reason": "Reason for this action"}

Bodies 01U: {"expectedVersion": 0, "name": "Logistics Team", "description": "Fictional team", "active": true}

Bodies 02C: {"username": "demo.custodian", "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": 1}

Bodies 02U: {"expectedVersion": 0, "displayName": "Demo Custodian", "role": "CUSTODIAN", "departmentId": 1, "active": true, "phoneE164": null, "smsConsent": false}

Bodies 03C: {"code": "WELCOME_26", "name": "Welcome Workshop", "description": "Fictional scenario", "departmentId": 1, "ownerId": 3, "startAt": "2026-11-10T09:00:00+08:00", "endAt": "2026-11-10T12:00:00+08:00"}

Bodies 03U: {"expectedVersion": 0, "name": "Welcome Workshop Revised", "description": "Fictional revised description"}

Bodies 04C: {"code": "CORD_POOL", "name": "Extension Cords", "description": "Interchangeable fictional cords", "type": "REUSABLE", "category": "Equipment", "unit": "piece", "storageLocation": "Demo cabinet", "controlBasis": "OWNED", "authorizationNote": null, "departmentId": 1, "custodianId": 2}

Bodies 04U: {"expectedVersion": 0, "name": "Extension Cords", "description": "Checked pool", "category": "Equipment", "storageLocation": "Demo cabinet B", "departmentId": 1, "custodianId": 2, "active": true, "reason": "Cabinet label changed"}

Bodies 05C: {"projectId": 1, "resourceId": 1, "quantity": 6, "startAt": "2026-11-10T09:00:00+08:00", "endAt": "2026-11-10T12:00:00+08:00", "priority": "NORMAL", "notes": "Fictional planning requirement"}

Bodies 05U: {"expectedVersion": 0, "quantity": 6, "startAt": "2026-11-10T09:00:00+08:00", "endAt": "2026-11-10T12:00:00+08:00", "priority": "HIGH", "notes": "Updated need", "status": "ACTIVE", "reason": "Priority clarified"}

Bodies 06C: {"requirementId": 1, "quantity": 6, "recipientId": 3, "requestKey": "00000000-0000-4000-8000-000000000001"}

Bodies 07C: {"resourceId": 1, "allocationId": null, "type": "RECEIPT", "quantity": 10, "inspection": null, "reason": "OPENING_COUNT fictional stock", "commandKey": "00000000-0000-4000-8000-000000000001", "reversalOfId": null, "correctionReferenceId": null}

Bodies 08C: {"allocationId": 1, "kind": "RETURN_DUE"}

Bodies 09C: {"targetType": "Project", "targetId": 1, "reason": "Fictional planning note"}

Bodies P05: {"expectedVersion": 0, "reason": "Fictional internal planning decision", "waiveUnfulfilled": false}

Bodies P07: {"expectedVersion": 0, "startAt": "2026-11-10T09:00:00+08:00", "endAt": "2026-11-10T12:00:00+08:00", "requirements": [{"requirementId": 1, "startAt": "2026-11-10T09:00:00+08:00", "endAt": "2026-11-10T12:00:00+08:00"}], "reason": "Fictional schedule change"}

Bodies A04: {"expectedVersion": 0, "quantity": 1, "reason": "Unused part no longer required"}

Bodies A05, A07: {"quantity": 1, "reason": "Reason for this action", "commandKey": "00000000-0000-4000-8000-000000000001"}

Bodies A06: {"quantity": 1, "reason": "Fictional return observation", "commandKey": "00000000-0000-4000-8000-000000000001", "inspection": "USABLE"}

Bodies T01: {"reason": "Correct latest erroneous fictional event", "commandKey": "00000000-0000-4000-8000-000000000001"}

Bodies N02, N03, J01: {}

For a transaction, allocationId is required for ISSUE, RETURN, CONSUMPTION and custody LOSS. Other storage movements have no allocation. inspection is required only for RETURN. REVERSAL uses reversalOfId to identify the original; correctionReferenceId optionally links a new observation to an earlier transaction in the same resource. Actors, times and signed changes are set by the component. SMS consent can be granted only by that user; an administrator can revoke it.

### Query parameters

Lists accept page (integer, default0), size (1..100, default20) and the optional filters below. IDs are Long; active/includeDraft are Boolean; role/type/status/kind use the allowed values in the entity tables; q/category are text; from/to are date-times. q searches resource code/name, up to100 characters. List from/to filters select matching events or overlapping project dates. Records are ordered by ID.

| List URL | Optional filters |
|---|---|
| /api/departments | active |
| /api/users | departmentId, role, active |
| /api/projects | departmentId, ownerId, status, from, to |
| /api/resources | departmentId, custodianId, type, active, category, q |
| /api/requirements | projectId, resourceId, status |
| /api/allocations | projectId, resourceId, recipientId, decisionStatus |
| /api/inventory-transactions | resourceId, allocationId, type, from, to |
| /api/notifications | allocationId, recipientId, status, kind |
| /api/audit-events | targetType, targetId, from, to |

Query Q01: resourceId, startAt and endAt are required; candidateQuantity is optional. The interval must start now or later and end after startAt.

Query Q03: from and to are required; includeDraft defaults to false. Shows demand for the resource identified by path id.

Query R01: departmentId, page and size are optional.

Query R02: from and to are required; departmentId, page and size are optional.

Query R03: resourceId, departmentId, page and size are optional.

Query R04: departmentId, from and to are required.

Date-range reports and availability queries are limited to90 days. Unless listed in the table, an operation has no query parameters.

### Error messages

Failures return the real HTTP status and the JSON below, never a successful200 response. details identifies invalid fields, the current version or the conflicting quantities/IDs when relevant.

{"timestamp": "2026-11-03T02:00:00Z", "status": 409, "error": "Conflict", "code": "RESOURCE_OVERALLOCATION", "message": "The requested quantity exceeds capacity for the selected period.", "path": "/api/allocations/1/approve", "requestId": "00000000-0000-4000-8000-000000000002", "retryable": false, "details": {"availableQuantity": 4, "requestedQuantity": 5, "competingAllocationIds": [1]}}

| Error and status | Message |
|---|---|
| Invalid input (400) | The request has invalid or unknown fields. |
| Invalid field (400) | One or more fields failed validation. |
| No acting user (401) | An active acting user is required. |
| Not allowed (403) | The acting user cannot perform this operation. |
| Not found (404) | The referenced record does not exist. |
| Duplicate (409) | A record with this unique reference already exists. |
| In use (409) | Related records prevent deletion; use the documented lifecycle action. |
| Protected history (409) | Posted history cannot be deleted; use a new correction or reversal. |
| Changed record (409) | The record changed; retrieve its current version before deciding again. |
| Wrong state (409) | The current state does not permit this action. |
| Not enough available (409) | The requested quantity exceeds capacity for the selected period. |
| Demand already covered (409) | Approved coverage would exceed the requirement quantity. |
| Not enough in storage (409) | There is insufficient usable stock in storage for this issue. |
| Wrong resource type (409) | The resource type does not support this movement. |
| Schedule already used (409) | A recorded issue or consumption prevents schedule replacement. |
| Reused action key (409) | This command key was used with different input. |
| Unsafe correction (409) | Later activity prevents safe reversal of this event. |
| Busy resource (409) | The command rolled back due to concurrent activity; retrieve state and retry safely. |
| Reminder not due (409) | This reminder is not currently due or has no outstanding custody. |
| Unconfirmed SMS (409) | The previous send outcome is unknown; reconcile before resending. |
| Server error (500) | The operation could not be completed. Refer to the request ID. |

Every operation can return Invalid input, Invalid field, No acting user, Not allowed or Server error. A missing target/related record returns Not found. The operation tables list additional applicable errors. Busy resource means nothing was saved and the request can be tried again after retrieving current data.

### Operations

#### DepartmentController forwards to DepartmentComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 01C POST /api/departments Create a record. | body 01C | 201 Department | create; Duplicate |
| 01L GET /api/departments List matching records. | query 01L | 200 Department list | list; common errors only |
| 01R GET /api/departments/{id} Read one record. | id; none | 200 Department | get; common errors only |
| 01D POST /api/departments/{id}/delete Delete an unused record. | id; body 01D | 204; no body | delete; In use, Wrong state, Changed record |
| 01U POST /api/departments/{id}/update Update allowed details. | id; body 01U | 200 Department | update; Changed record, Duplicate, In use, Wrong state |

#### UserController forwards to UserComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 02C POST /api/users Create a record. | body 02C | 201 User | create; Duplicate, Wrong state |
| 02L GET /api/users List matching records. | query 02L | 200 User list | list; common errors only |
| 02R GET /api/users/{id} Read one record. | id; none | 200 User | get; common errors only |
| 02D POST /api/users/{id}/delete Delete an unused record. | id; body 02D | 204; no body | delete; In use, Wrong state, Changed record |
| 02U POST /api/users/{id}/update Update allowed details. | id; body 02U | 200 User | update; Changed record, Duplicate, In use, Wrong state |

#### ProjectController forwards to ProjectComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 03C POST /api/projects Create a record. | body 03C | 201 Project | create; Duplicate, Wrong state |
| 03L GET /api/projects List matching records. | query 03L | 200 Project list | list; common errors only |
| 03R GET /api/projects/{id} Read one record. | id; none | 200 Project | get; common errors only |
| 03D POST /api/projects/{id}/delete Delete an unused record. | id; body 03D | 204; no body | delete; In use, Wrong state, Changed record |
| 03U POST /api/projects/{id}/update Update allowed details. | id; body 03U | 200 Project | update; Changed record, Duplicate, In use, Wrong state |
| P01 POST /api/projects/{id}/submit Move internal project to SUBMITTED. | id; body P01 | 200 Project SUBMITTED | submit; Wrong state, Changed record, Not enough available |
| P02 POST /api/projects/{id}/approve Move internal project to APPROVED. | id; body P02 | 200 Project APPROVED | approve; Wrong state, Changed record, Not enough available |
| P03 POST /api/projects/{id}/return-draft Move internal project to DRAFT. | id; body P03 | 200 Project DRAFT | returnDraft; Wrong state, Changed record, Not enough available |
| P04 POST /api/projects/{id}/start Move internal project to IN_PROGRESS. | id; body P04 | 200 Project IN_PROGRESS | start; Wrong state, Changed record, Not enough available |
| P05 POST /api/projects/{id}/complete Move internal project to COMPLETED. | id; body P05 | 200 Project COMPLETED | complete; Wrong state, Changed record, Not enough available |
| P06 POST /api/projects/{id}/cancel Move internal project to CANCELLED. | id; body P06 | 200 Project CANCELLED | cancel; Wrong state, Changed record, Not enough available |
| P07 POST /api/projects/{id}/reschedule Atomically replace full unissued project schedule. | id; body P07 | 200 Project | reschedule; Schedule already used, Changed record, Not enough available |

#### ResourceController forwards to ResourceComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 04C POST /api/resources Create a record. | body 04C | 201 Resource | create; Duplicate, Wrong state |
| 04L GET /api/resources List matching records. | query 04L | 200 Resource list | list; common errors only |
| 04R GET /api/resources/{id} Read one record. | id; none | 200 Resource | get; common errors only |
| 04D POST /api/resources/{id}/delete Delete an unused record. | id; body 04D | 204; no body | delete; In use, Wrong state, Changed record |
| 04U POST /api/resources/{id}/update Update allowed details. | id; body 04U | 200 Resource | update; Changed record, Duplicate, In use, Wrong state |

#### RequirementController forwards to RequirementComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 05C POST /api/requirements Create a record. | body 05C | 201 Requirement | create; Duplicate, Wrong state |
| 05L GET /api/requirements List matching records. | query 05L | 200 Requirement list | list; common errors only |
| 05R GET /api/requirements/{id} Read one record. | id; none | 200 Requirement | get; common errors only |
| 05D POST /api/requirements/{id}/delete Delete an unused record. | id; body 05D | 204; no body | delete; In use, Wrong state, Changed record |
| 05U POST /api/requirements/{id}/update Update allowed details. | id; body 05U | 200 Requirement | update; Changed record, Duplicate, In use, Wrong state |

#### AllocationController forwards to AllocationComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 06C POST /api/allocations Request an allocation. | body 06C | 201 Allocation | request; Reused action key, Wrong state, Demand already covered |
| 06L GET /api/allocations List matching records. | query 06L | 200 Allocation list | list; common errors only |
| 06R GET /api/allocations/{id} Read one record. | id; none | 200 Allocation | get; common errors only |
| 06D POST /api/allocations/{id}/delete Delete an unused record. | id; body 06D | 204; no body | delete; In use, Wrong state, Changed record |
| A01 POST /api/allocations/{id}/approve Commit requested quantity without moving stock. | id; body A01 | 200 Allocation APPROVED | approve; Changed record, Wrong state, Not enough available, Demand already covered |
| A02 POST /api/allocations/{id}/reject Reject a pending request without deleting history. | id; body A02 | 200 Allocation REJECTED | reject; Changed record, Wrong state |
| A03 POST /api/allocations/{id}/cancel Cancel pending or entirely unissued approved allocation. | id; body A03 | 200 Allocation CANCELLED | cancel; Changed record, Wrong state |
| A04 POST /api/allocations/{id}/release Release some or all unissued approved remainder. | id; body A04 | 200 Allocation | release; Changed record, Wrong state |
| A05 POST /api/allocations/{id}/issue Record ISSUE against this allocation. | id; body A05 | 201 Transaction ISSUE | issueResources; Wrong state, Wrong resource type, Not enough in storage, Not enough available, Reused action key |
| A06 POST /api/allocations/{id}/return Record RETURN against this allocation. | id; body A06 | 201 Transaction RETURN | returnResources; Wrong state, Wrong resource type, Not enough in storage, Not enough available, Reused action key |
| A07 POST /api/allocations/{id}/consume Record CONSUMPTION against this allocation. | id; body A07 | 201 Transaction CONSUMPTION | consumeResources; Wrong state, Wrong resource type, Not enough in storage, Not enough available, Reused action key |

#### InventoryTransactionController forwards to InventoryTransactionComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 07C POST /api/inventory-transactions Record a stock or custody change. | body 07C | 201 Transaction | record; Duplicate, Wrong state, Reused action key, Wrong resource type, Not enough available, Demand already covered, Not enough in storage, Unsafe correction |
| 07L GET /api/inventory-transactions List matching records. | query 07L | 200 Transaction list | list; common errors only |
| 07R GET /api/inventory-transactions/{id} Read one record. | id; none | 200 Transaction | get; common errors only |
| 07D POST /api/inventory-transactions/{id}/delete Refuse deletion of posted history. | id; body 07D | 409 Protected history | delete; In use, Wrong state, Changed record, Protected history |
| T01 POST /api/inventory-transactions/{id}/reverse Append constrained reversal of a latest event. | id; body T01 | 201 Transaction REVERSAL | reverse; Unsafe correction, Reused action key, Not enough available, Wrong state |

#### NotificationController forwards to NotificationComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 08C POST /api/notifications Queue an eligible reminder. | body 08C | 201 Notification | enqueueManual; Reminder not due |
| 08L GET /api/notifications List matching records. | query 08L | 200 Notification list | list; common errors only |
| 08R GET /api/notifications/{id} Read one record. | id; none | 200 Notification | get; common errors only |
| 08D POST /api/notifications/{id}/delete Delete an unused record. | id; body 08D | 204; no body | delete; In use, Wrong state, Changed record |
| N01 POST /api/notifications/{id}/cancel Cancel a reminder before any send begins. | id; body N01 | 200 Notification | cancel; Changed record, Wrong state |
| N02 POST /api/notifications/{id}/dispatch Run one due outbox record through the same dispatcher as scheduler. | id; body N02 | 200 Notification | dispatch; Wrong state, Unconfirmed SMS |
| N03 POST /api/notifications/{id}/refresh Fetch provider status for a known SID; no new message. | id; body N03 | 200 Notification | refresh; Wrong state |
| N04 POST /api/notifications/{id}/retry Queue a known not-accepted failure within bounded attempts. | id; body N04 | 200 Notification | retry; Changed record, Wrong state, Unconfirmed SMS |

#### AuditController forwards to AuditComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| 09C POST /api/audit-events Add an audit note. | body 09C | 201 Audit note | appendNote; common errors only |
| 09L GET /api/audit-events List matching records. | query 09L | 200 Audit note list | list; common errors only |
| 09R GET /api/audit-events/{id} Read one record. | id; none | 200 Audit note | get; common errors only |
| 09D POST /api/audit-events/{id}/delete Refuse deletion of posted history. | id; body 09D | 409 Protected history | delete; In use, Wrong state, Changed record, Protected history |

#### PlanningController forwards to AvailabilityComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| Q01 GET /api/availability Explain present/future capacity for one resource and interval. | query Q01 | 200 Availability | query; common errors only |

#### ShortageController forwards to ShortageComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| Q02 GET /api/shortages/projects/{id} Explain each project requirement coverage and conditional sourcing gap. | id; none | 200 Project shortage | projectSummary; common errors only |
| Q03 GET /api/shortages/resources/{id} Consolidate competing departments and peak sourcing capacity. | id; query Q03 | 200 Combined demand | consolidate; common errors only |

#### ReportController forwards to ReportingComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| R01 GET /api/reports/inventory Current explainable ledger stock by resource. | query R01 | 200 Inventory report | inventory; common errors only |
| R02 GET /api/reports/upcoming Approved allocations due in a selected future window. | query R02 | 200 Allocation report | upcoming; common errors only |
| R03 GET /api/reports/overdue Outstanding reusable custody past its expected end. | query R03 | 200 Allocation report | overdue; common errors only |
| R04 GET /api/reports/department-demand Observed planned demand by resource for one department/horizon. | query R04 | 200 Department report | departmentDemand; common errors only |

#### SchedulingController forwards to SchedulingComponent

| Operation and purpose | Input | JSON output | Forwarded method and errors |
|---|---|---|---|
| J01 POST /api/jobs/reminders/run-once Deterministically run reminder scan once without changing clock. | body J01 | 200 Reminder scan | runOnce; Wrong state |

### Planning and report JSON

The following are the output formats used by the planning/report operations. Quantities in these examples are illustrative.

Availability: {"resourceId": 1, "asOf": "2026-11-10T00:00:00Z", "startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T05:00:00Z", "unit": "piece", "physicalInStore": 10, "outstanding": 0, "usableOwned": 10, "reservedConsumable": null, "rawMinimumFree": 4, "allocatableQuantity": 4, "overdueOutstanding": 0, "returnDependentQuantity": 0, "segments": [{"startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T04:00:00Z", "committed": 6, "rawFree": 4, "allocationIds": [1]}], "warnings": []}

Project shortage: {"projectId": 1, "asOf": "2026-11-10T00:00:00Z", "requirements": [{"requirementId": 1, "resourceId": 1, "unit": "piece", "demand": 6, "approvedCoverage": 0, "supplied": 0, "outstanding": 0, "unallocated": 6, "freeIfConsideredAlone": 10, "sourcingGapIfConsideredAlone": 0, "atRisk": false}], "warning": "Individual candidate gaps are not additive."}

Combined demand: {"resourceId": 1, "asOf": "2026-11-10T00:00:00Z", "to": "2026-11-10T06:00:00Z", "includeDraft": false, "unit": "piece", "minimumAdditionalUnits": 1, "segments": [{"startAt": "2026-11-10T02:00:00Z", "endAt": "2026-11-10T04:00:00Z", "usableCapacity": 10, "approvedCommitted": 6, "unallocatedDemand": 5, "deficit": 1, "requirementIds": [1, 2], "projectIds": [1, 2], "departmentIds": [1, 2]}], "reasons": ["TIME_CONFLICT"], "warning": "Capacity scenario with fixed dates; not purchasing authorization.", "from": "2026-11-10T01:00:00Z"}

Inventory report: {"asOf": "2026-11-10T00:00:00Z", "items": [{"resourceId": 1, "type": "REUSABLE", "unit": "piece", "physicalInStore": 10, "outstanding": 0, "damaged": 0, "usableOwned": 10, "ledgerConsistent": true}]}

Allocation report: {"asOf": "2026-11-10T00:00:00Z", "items": [{"allocationId": 1, "projectId": 1, "resourceId": 1, "recipientId": 1, "startAt": "2026-11-10T01:00:00Z", "endAt": "2026-11-10T04:00:00Z", "unissued": 0, "outstanding": 2, "overdue": true, "atRisk": false}]}

Department report: {"asOf": "2026-11-10T00:00:00Z", "departmentId": 1, "to": "2026-11-11T01:00:00Z", "items": [{"resourceId": 1, "unit": "piece", "requested": 6, "approvedCoverage": 4, "supplied": 0, "unallocated": 2}], "warning": "Different units are never summed.", "from": "2026-11-10T01:00:00Z"}

Reminder scan: {"asOf": "2026-11-10T00:00:00Z", "created": 1, "skipped": 0, "existing": 0, "errors": 0, "providerMode": "FAKE"}

## Components and method steps

All changes check the acting user's role and responsibility. ADMIN manages users/departments; OFFICER handles internal project decisions and recount adjustments; HEAD manages department plans; CUSTODIAN manages assigned resources; MEMBER manages their own draft plans. A requester cannot approve their own allocation.

Common read steps:1check the ID/filters;2retrieve the record(s), returning Not found where applicable;3return the permitted JSON fields. Common change steps:1retrieve/check the record and current version;2validate the supplied fields, related records and method-specific rules below;3save the change and audit together;4return the result. A failed change leaves the earlier data unchanged. The same resource cannot be approved or issued by two changes at the same time.

### DepartmentComponent

Maintains the organization's departments.

| Method | Ordered steps |
|---|---|
| create | 1. Check required name/code and duplicate code. 2. Save the active department. 3. Return it. |
| update | 1. Apply the common change steps. 2. Before deactivation, check that active users/resources/projects do not still depend on it. |
| list, get | Use the common read steps. |
| delete | 1. Check that no user, resource or project uses it. 2. Delete it, or return In use. |

### UserComponent

Maintains members, roles and optional SMS preferences.

| Method | Ordered steps |
|---|---|
| create | 1. Check administrator permission, username, role and department. 2. Save an active user with SMS consent off. |
| update | 1. Check the current version and allowed editor. 2. Restrict role/department changes to administrators; consent can be granted only by the user. 3. Prevent removal of the last administrator or an unresolved responsibility. 4. Save the change. |
| list, get | Use the common read steps; hide contact/consent details from other members. |
| delete | 1. Check that no history or current responsibility uses the user. 2. Delete if unused; otherwise deactivate after responsibilities are reassigned. |

### ResourceComponent

Maintains the resource catalogue and displays its actual balances.

| Method | Ordered steps |
|---|---|
| create | 1. Check ownership/authorization, department, custodian, type and unit. 2. Save the resource with zero stock; a receipt adds opening stock. |
| update | 1. Check permission/version. 2. Keep code/type/unit/control basis fixed once referenced. 3. Allow officer-approved custodian reassignment and safe detail changes. |
| list, get | Use the common read steps and calculate stock from transactions. |
| delete | 1. Check that no requirement/allocation/transaction uses it. 2. Delete only if unused; otherwise archive once commitments/custody are resolved. |

### ProjectComponent

Maintains departmental projects and their planning status.

| Method | Ordered steps |
|---|---|
| create, update | 1. Check owner/department, dates and required fields. 2. Create DRAFT or update allowed details. 3. Save and return the project. |
| list, get | Use the common read steps. |
| submit | 1. Require DRAFT and valid resource requirements. 2. Set SUBMITTED. |
| approve, returnDraft | 1. Require an officer and SUBMITTED. 2. Approve to APPROVED, or return to DRAFT with a reason and no approved supply. |
| start | 1. Require APPROVED and the current project interval. 2. Set IN_PROGRESS. |
| complete | 1. Require all allocations resolved or released. 2. Require a reason for waiving unmet demand. 3. Set COMPLETED. |
| cancel | 1. Check permission and reason. 2. Cancel pending/unissued promises and release unused remainders. 3. Keep outstanding custody for later return/loss resolution. 4. Set CANCELLED. |
| reschedule | 1. Reject if any project allocation has been issued/consumed. 2. Check every replacement requirement interval against other commitments. 3. Save all changed dates together, or none. |
| delete | 1. Require an unused DRAFT with no requirements. 2. Delete it. |

### RequirementComponent

Records what each project needs.

| Method | Ordered steps |
|---|---|
| create | 1. Check owner/head, project state, resource, quantity and dates within the project. 2. Reject duplicate project-resource lines. 3. Save ACTIVE; return a changed submitted plan to DRAFT. |
| update | 1. Allow demand edits only in DRAFT/SUBMITTED before approved supply. 2. Cancel stale pending requests. 3. Allow withdrawal only after obligations are resolved. 4. Save; do not reassign project/resource IDs. |
| list, get | Use the common read steps; add approved coverage, supplied, outstanding and unallocated quantities. |
| delete | 1. Check that no allocation/movement uses the requirement. 2. Delete only if unused. |

### AvailabilityComponent

Checks usable stock against dated commitments.

| Method | Ordered steps |
|---|---|
| query, calculateLocked | 1. Read usable stored and issued quantities and approved remaining commitments. 2. For reusable resources, check simultaneous load throughout the requested interval, including overdue custody. 3. Return the lowest free quantity, conflicts and any dependency on an expected return. 4. For consumables, subtract all unconsumed reservations from physical stock. |
| balanceLocked | 1. Add receipts/increases and subtract consumption, damage/loss and decreases. 2. Account for issue/return changes in custody. 3. Refuse impossible negative balances. |

### AllocationComponent

Handles resource requests and commitments.

| Method | Ordered steps |
|---|---|
| request | 1. Check requester, active requirement/project, recipient and action key. 2. Check remaining demand. 3. Copy the required interval and save REQUESTED. |
| approve | 1. Check the assigned custodian is different from the requester and the request is still pending. 2. Recheck remaining demand and availability for the entire interval. 3. Approve only if the full requested amount fits. 4. Save approver/time/reason. |
| reject | 1. Require a pending request and reason. 2. Save REJECTED. |
| cancel | 1. Allow a pending or wholly unissued approval. 2. Save CANCELLED and release its commitment. |
| release | 1. Check the quantity is still unissued/unconsumed. 2. Increase releasedUnissued by that amount. 3. Preserve earlier supplied history. |
| issueResources, returnResources, consumeResources | 1. Fill in the allocation, resource and movement type. 2. Forward to InventoryTransactionComponent.record, which checks and saves the physical change. |
| list, get | Use the common read steps and calculate supplied/outstanding quantities. |
| delete | 1. Require an unused REQUESTED allocation with no movements/reminders. 2. Delete; keep decided history. |

### InventoryTransactionComponent

Records actual stock and custody changes without overwriting history.

| Method | Ordered steps |
|---|---|
| record | 1. Check the resource, action key, user, type and linked allocation. 2. Recalculate balances and check the quantity. 3. Save one movement and its actor/time/reason. 4. Recalculate balances and identify promises affected by damage/loss. |
| reverse | 1. Require the latest unreversed event, no later dependent change and an officer's reason. 2. Check that undoing it keeps stock/custody/commitments valid. 3. Add a REVERSAL; keep the original. |
| list, get | Use the common read steps. |
| delete | Return Protected history; a correction is a new event. |

### ShortageComponent

Shows remaining demand and the quantity still to source.

| Method | Ordered steps |
|---|---|
| projectSummary | 1. Read project requirements and approved coverage. 2. Calculate unallocated demand and free quantity. 3. Show each line's remaining gap and any at-risk commitment. |
| consolidate | 1. Gather the included departments' requirements and current commitments. 2. For reusable resources, find the largest simultaneous deficit; for consumables, compare combined demand with unreserved stock. 3. Show the periods/projects/quantities behind the result. |

### ReportingComponent

Returns stock, upcoming/overdue custody and departmental demand.

| Method | Ordered steps |
|---|---|
| inventory | 1. Read resource transactions. 2. Calculate current usable, issued and damaged quantities. 3. Return each resource separately. |
| upcoming | 1. Select approved allocations overlapping the requested dates. 2. Return their unissued/outstanding quantities. |
| overdue | 1. Find reusable allocations past endAt. 2. Keep only those with outstanding custody. 3. Return the responsible recipient. |
| departmentDemand | 1. Select department requirements in the period. 2. Group requested, approved and supplied quantities by resource/unit. 3. Return the totals without mixing units. |

### NotificationComponent

Prepares return reminders and records sending results.

| Method | Ordered steps |
|---|---|
| enqueueManual, enqueueEvent | 1. Check outstanding reusable custody, due window and recipient. 2. Reuse an existing event key instead of duplicating it. 3. Save QUEUED if a valid phone and consent exist; otherwise SKIPPED. |
| scanReminders | 1. Find outstanding returns due within 24 hours or already overdue. 2. Select the allocation recipient. 3. Create one RETURN_DUE and one OVERDUE event at most per due date. |
| claim, dispatch, dispatchDue, finish | 1. Select/claim a due queued reminder. 2. Recheck custody and consent; mark SENDING. 3. Send through TwilioIntegrationComponent outside the stock change. 4. Record acceptance, failure or UNKNOWN outcome and the attempt. |
| cancel | 1. Require a queued/skipped reminder that has not been attempted. 2. Set CANCELLED. |
| refresh | 1. Require a saved provider SID. 2. Fetch its current provider status. 3. Update the log without sending again. |
| retry | 1. Require a confirmed unaccepted failure and fewer than 3 attempts. 2. Recheck eligibility/consent. 3. Queue a bounded retry; never blindly retry UNKNOWN. |
| list, get | Use the common read steps and restrict contact details. |
| delete | 1. Require an unattempted queued/skipped/cancelled log. 2. Delete it; otherwise return Protected history. |

### AuditComponent

Preserves who changed a record and why, including actions that do not move stock.

| Method | Ordered steps |
|---|---|
| append | 1. Take the actor, target, changes and reason from the successful component action. 2. Remove private contact/credential values. 3. Save the audit with the main change. |
| appendNote | 1. Check officer/administrator permission and target. 2. Save a NOTE with the reason. |
| list, get | Use the common read steps. |
| delete | Return Protected history; add a correcting note instead. |

### TwilioIntegrationComponent

Sends SMS and reads a known message status.

| Method | Ordered steps |
|---|---|
| sendSms | 1. Prepare To, MessagingServiceSid and Body with server credentials. 2. Submit through Retrofit to Twilio. 3. Return the provider SID/status or a sending error. |
| fetchStatus | 1. Request the stored SID's status. 2. Return it without creating another SMS. |

### SchedulingComponent

Runs reminder checks and delivery automatically.

| Method | Ordered steps |
|---|---|
| scan | 1. Every60 seconds, check upcoming/overdue returns. 2. Call NotificationComponent.scanReminders. |
| dispatch | 1. Every5 seconds, select due reminders. 2. Send them through the notification component. 3. Log each result without changing stock. |
| runOnce | 1. Run the same scan once in the local demonstration. 2. Return counts of created, skipped and existing reminders. |

### PermissionComponent and ActorResolver

Identify the acting user and apply role/record ownership rules.

| Method | Ordered steps |
|---|---|
| resolve, require | 1. Find the active acting user. 2. Check the requested action against their role, department and resource responsibility. 3. Continue or return No acting user/Not allowed. |

### DemoSeedBootstrap and SeedComponent

Optionally provide a repeatable fictional starting state.

| Method | Ordered steps |
|---|---|
| initialize (@PostConstruct) | 1. Check that demonstration initialization is enabled. 2. Call the separate SeedComponent to initialize the administrator and optional scenario. 3. Do not seed live phone consent or send SMS. |
| bootstrapAdmin | 1. Return an existing demonstration administrator. 2. If absent, create one only on an empty demonstration database. 3. Return its generated ID. |
| seedScenario | 1. Create departments/users. 2. Create resources and opening receipts. 3. Add projects, requirements and sample allocations. 4. Skip an already complete matching scenario instead of adding stock again. |

Movement rules: ISSUE moves reusable units from storage into custody; RETURN makes inspected usable units available again and keeps damaged returns unavailable. LOSS removes the relevant stored/issued quantity. CONSUMPTION is for consumables only; it decreases physical stock and the allocation's remaining reservation together. RECEIPT/ADJUSTMENT_INCREASE add usable storage; DAMAGE/LOSS/ADJUSTMENT_DECREASE remove it. Recount adjustments require an officer.

Issue/consumption must occur within the approved allocation dates and cannot exceed its unissued/unconsumed remainder. Returns and custody loss cannot exceed the outstanding quantity. Storage damage/loss/decreases cannot exceed usable stock in storage. An issue also requires actual stock and cannot take units still held by another project.

Reusable booking intervals are [startAt,endAt), so adjacent bookings do not overlap. Late outstanding returns continue to block availability. Consumables reserve current stock across dates. A requirement can have several partial allocations; approved coverage cannot exceed its demand. Damage/loss is recorded even if it makes an earlier promise at risk, but unsafe new approvals are refused.

Reminders require an optional valid phone and user consent. Only a confirmed unaccepted429 failure can retry after 60then300 seconds, at most 3 attempts. An uncertain send remains UNKNOWN to avoid duplicate SMS. A sending failure never changes an approved allocation or stock. The demonstration cap is5 attempts per local day.

## Entities and repositories

All IDs are positive Long values generated by the database. Every entity has id, createdAt (Instant) and createdBy (AppUser; null only for system initialization/jobs). Mutable entities also have version (nonnegative Long), updatedAt (Instant) and updatedBy (AppUser; null only for system actions). id, createdAt, version and updatedAt do not allow null where present. InventoryTransaction and AuditEvent cannot be edited. Instant is a date/time stored in UTC. A field whose type is another entity links to an existing record of that type.

Common validation: required text cannot be blank; text and quantities stay within the listed limits; endAt must follow startAt; requirement dates must lie within the project. Existing related records cannot be removed while in use. The tables give the remaining fields and their allowed values.

### Department

A department responsible for its members, projects and resources.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| code | String | No | 3..20 uppercase A-Z0-9_ unique |
| name | String | No | Required text; at most 100 characters |
| description | String | Yes | at most 1000 characters |
| active | Boolean | No | Initially true |

DepartmentRepository stores and retrieves Department records. Hard delete only when no user, project or resource references exist. Otherwise409 DEPENDENCY_EXISTS; update active=false only when no active operational dependency.

Custom method findByCode(String code): Detect duplicate normalized code.

### AppUser

A member with a role and department, and optional SMS preferences.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| username | String | No | 3..50 lowercase a-z0-9._- unique |
| displayName | String | No | Required text at most 100 characters |
| role | Role | No | ADMIN OFFICER HEAD CUSTODIAN MEMBER |
| department | Department | Yes | Existing record; required except ADMIN/OFFICER |
| active | Boolean | No | Initially true |
| phoneE164 | String | Yes | Plus followed by 8..15digits; at most 16 characters; required when smsConsent is true. |
| smsConsent | Boolean | No | Initially false |
| consentAt | Instant | Yes | Required when consent true; server timestamp |
| consentVersion | String | Yes | at most 30 characters; required when consent true |

AppUserRepository stores and retrieves AppUser records. Hard delete only if no operational or actor references anywhere. Otherwise deactivate using update; deny deactivation if assigned unresolved custody or sole active custodian, until reassigned. Never delete last active ADMIN.

Custom method findByUsername(String username): Resolve known username and detect duplicate.

### Project

An organization activity with an owner, department and schedule.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| code | String | No | 3..30 uppercase A-Z0-9_- unique |
| name | String | No | Required text at most 150 characters |
| description | String | Yes | at most 2000 characters |
| department | Department | No | Existing record; active at create |
| owner | AppUser | No | Existing record; active same department |
| startAt | Instant | No | Strictly less than endAt |
| endAt | Instant | No | After startAt; project duration at most 90 days |
| status | ProjectStatus | No | DRAFT SUBMITTED APPROVED IN_PROGRESS COMPLETED CANCELLED |
| statusReason | String | Yes | at most 1000 characters; required for return/cancel/waiver |

ProjectRepository stores and retrieves Project records. Hard delete only DRAFT with no requirements/allocations. Historical project uses cancel or completed archival; no cascading delete.

Custom method findByCode(String code): Detect duplicate project reference.

### Resource

A reusable or consumable resource managed by the organization.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| code | String | No | 3..30 uppercase A-Z0-9_- unique |
| name | String | No | Required text at most 150 characters |
| description | String | Yes | at most 2000 characters |
| type | ResourceType | No | REUSABLE CONSUMABLE; locked after reference |
| category | String | No | Required text at most 50 characters |
| unit | String | No | Required text at most 20 characters; fixed after reference |
| storageLocation | String | No | Required text at most 200 characters |
| controlBasis | ControlBasis | No | OWNED AUTHORIZED |
| authorizationNote | String | Yes | at most 1000 characters; required for AUTHORIZED |
| department | Department | No | Existing record active |
| custodian | AppUser | No | Existing record active CUSTODIAN |
| revision | Long | No | Nonnegative system counter; increases after a resource action. |
| active | Boolean | No | Initially true |

ResourceRepository stores and retrieves Resource records. Hard delete only with no requirements, allocations or transactions. Archive requires no pending/active unissued commitment or outstanding custody; keep ledger forever in demo.

Custom method findByCode(String code): Detect duplicate pool reference.

Custom method lockById(Long id): Load this record and protect it while its change is checked and saved.

### ResourceRequirement

One resource needed by one project, with its quantity and dates.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| project | Project | No | Existing record; active planning project |
| resource | Resource | No | Existing record active |
| quantity | Long | No | 1..1000000 |
| startAt | Instant | No | Within project; start less than end |
| endAt | Instant | No | Within project |
| priority | Priority | No | LOW NORMAL HIGH |
| notes | String | Yes | at most 2000 characters |
| status | RequirementStatus | No | ACTIVE WITHDRAWN |

ResourceRequirementRepository stores and retrieves ResourceRequirement records. Hard delete only when no allocation or movement references. Otherwise withdrawal through update with reason, only after all operational obligations are resolved.

Custom method findByProject_IdOrderByIdAsc(Long projectId): Load complete project requirement set.

Custom method findByResource_IdAndStatus(Long resourceId, RequirementStatus status): Load active demand for consolidation; project-status filter in component.

### Allocation

A request and the quantity committed toward a requirement.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| requirement | ResourceRequirement | No | Existing record ACTIVE |
| quantity | Long | No | 1..1000000 |
| releasedUnissued | Long | No | Initially0;0..quantity;cannot reduce issued coverage |
| startAt | Instant | No | Snapshot requirement.startAt |
| endAt | Instant | No | Snapshot requirement.endAt |
| requestedBy | AppUser | No | Existing record active at create |
| recipient | AppUser | No | Existing record active same project department |
| decisionStatus | AllocationDecision | No | REQUESTED APPROVED REJECTED CANCELLED |
| decidedBy | AppUser | Yes | Existing record; required on approve/reject |
| decidedAt | Instant | Yes | Required on approve/reject |
| decisionReason | String | Yes | at most 1000 characters; required rejection/cancellation |
| requestKey | String | No | UUID canonical36;global unique |
| payloadHash | String | No | 64-character value calculated from the request to detect changed repeats. |

AllocationRepository stores and retrieves Allocation records. Hard delete only REQUESTED by requester or HEAD/OFFICER, with no movements or notifications; append audit. Approved/rejected/cancelled history retained;409 directs cancel/release.

Custom method findByRequestKey(String requestKey): Find an earlier allocation request with this key.

Custom method findByRequirement_Resource_IdAndDecisionStatus(Long resourceId, AllocationDecision status): Current approved pool commitments; cannot filter only finite overlap because overdue holds extend.

Custom method findByRequirement_Project_Id(Long projectId): Cancel/reschedule/complete entire project safely.

Custom method findByRequirement_Id(Long requirementId): Find allocations used to fulfill this requirement.

Custom method findOverlappingApproved(Long resourceId, Instant start, Instant end, AllocationDecision approved): Find approved bookings overlapping the requested dates. Overdue issued resources are checked separately.

### InventoryTransaction

A saved receipt, issue, return, consumption, damage, loss or correction.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| resource | Resource | No | Existing record |
| allocation | Allocation | Yes | Existing record;required ISSUE RETURN CONSUMPTION and custody LOSS |
| type | TransactionType | No | RECEIPT ISSUE RETURN CONSUMPTION DAMAGE LOSS ADJUSTMENT_INCREASE ADJUSTMENT_DECREASE REVERSAL |
| quantity | Long | No | 1..1000000 |
| inspection | Inspection | Yes | USABLE DAMAGED;required RETURN only |
| reason | String | No | Required text at most 1000 characters |
| occurredAt | Instant | No | Server Clock, no client backdating |
| deltaStored | Long | No | Signed change to usable stock in storage; set by the component. |
| deltaOutstanding | Long | No | Signed change to reusable quantity in custody; 0 for consumables. |
| deltaDamaged | Long | No | Signed change to quarantined stock; 0 for consumables. |
| deltaIssued | Long | No | Signed change to the allocation's issued or consumed quantity. |
| deltaResolved | Long | No | Signed change to its returned or lost quantity. |
| deltaGoodReturned | Long | No | Signed change to usable returns. |
| deltaDamagedReturned | Long | No | Signed change to damaged returns. |
| deltaLost | Long | No | Signed change to custody losses. |
| commandKey | String | No | UUID36 unique together with resource |
| payloadHash | String | No | 64-character value calculated from the request to detect changed repeats. |
| reversalOf | InventoryTransaction | Yes | Existing original transaction in the same resource; at most one reversal. |
| correctionReference | InventoryTransaction | Yes | Earlier transaction in the same resource explained by this new observation. |
| resourceRevisionAfter | Long | No | Positive resource counter immediately after this movement. |

InventoryTransactionRepository stores and retrieves InventoryTransaction records. Never physically delete a posted movement. Delete endpoint returns409 IMMUTABLE_HISTORY with reversal route. No permission to clear the ledger through REST.

Custom method findByResource_IdOrderByIdAsc(Long resourceId): Read the resource history in order to calculate stock.

Custom method findByResource_IdAndCommandKey(Long resourceId, String commandKey): Find an earlier stock action with this resource and key.

Custom method findByReversalOf_Id(Long originalId): Check whether the original transaction has already been reversed.

Custom method findByAllocation_IdOrderByIdAsc(Long allocationId): Read issues, returns and consumption for this allocation.

### NotificationLog

A return reminder and its sending result.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| allocation | Allocation | No | Existing record reusable APPROVED with outstanding custody |
| recipient | AppUser | No | Existing record;allocation recipient |
| kind | NotificationKind | No | RETURN_DUE OVERDUE |
| eventKey | String | No | at most 160 characters unique;calculated by the system |
| status | NotificationStatus | No | QUEUED SENDING ACCEPTED DELIVERED UNDELIVERED FAILED UNKNOWN SKIPPED CANCELLED |
| dueAt | Instant | No | Server derived |
| nextAttemptAt | Instant | Yes | Required QUEUED |
| attemptCount | Integer | No | 0..3 |
| destinationMasked | String | Yes | at most 30 characters |
| providerSid | String | Yes | at most 64 characters unique when present |
| providerStatus | String | Yes | at most 40 characters |
| lastErrorCode | String | Yes | at most 80 characters |
| lastAttemptAt | Instant | Yes | Set when attempt starts |
| cancelReason | String | Yes | at most 1000 characters |
| attempts | List<NotificationAttempt> | No | At most 3 sending attempts; maintained by the component |

NotificationLogRepository stores and retrieves NotificationLog records. Hard delete only unattempted QUEUED/SKIPPED/CANCELLED rows with attemptCount0, by OFFICER/ADMIN; record AuditEvent. Otherwise409 IMMUTABLE_HISTORY. Cancellation of queued reminder is explicit and prevents sending.

Custom method findByEventKey(String eventKey): Check whether this reminder already exists.

Custom method findDue(NotificationStatus status, Instant now, Pageable page): Find queued reminders whose next sending time has arrived.

Custom method lockById(Long id): Load this record and protect it while its change is checked and saved.

### AuditEvent

A record of who changed something, what changed and why.

| Field | Java type | Null allowed | Validation or allowed value |
|---|---|---|---|
| action | String | No | at most 80 characters fixed allowlist or NOTE |
| targetType | String | No | One of nine entity names;NOTE allows Project/Resource/Department |
| targetId | Long | No | Positive;real target at creation |
| targetSummary | String | No | at most 300 characters server generated |
| beforeJson | String | Yes | System JSON of values before the change; at most 16000 characters; excludes private contact/credential values. |
| afterJson | String | Yes | System JSON of values after the change; at most 16000 characters; excludes private contact/credential values. |
| reason | String | Yes | at most 1000 characters;required NOTE and corrective actions |
| requestId | String | No | UUID36 set by the system |

AuditEventRepository stores and retrieves AuditEvent records. Never physically delete audit history. Delete endpoint returns409 IMMUTABLE_HISTORY. A correction is a new NOTE referencing the original id in its reason, not editing the original.

Custom method findByTargetTypeAndTargetIdOrderByIdAsc(String targetType, Long targetId): Show the changes made to one record.

NotificationAttempt belongs to its NotificationLog: ordinal Integer1..3, attemptedAt Instant, outcome String(max40), errorCode String(max80, nullable) and providerSid String(max64, nullable). It is a small list of sending attempts, not a separate entity.

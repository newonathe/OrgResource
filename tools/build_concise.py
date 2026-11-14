"""Create a separate concise submission proposal without changing the full specification."""
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
model=json.loads((ROOT/'contracts/domain-model.json').read_text())
ops=json.loads((ROOT/'contracts/operations.json').read_text())
examples=json.loads((ROOT/'contracts/response-examples.json').read_text())
parts=[]
def add(t):parts.append(t.strip())
def table(headers,rows):
    return '\n'.join(['| '+' | '.join(headers)+' |','|'+'|'.join(['---']*len(headers))+'|']+['| '+' | '.join(str(c).replace('|','/') for c in row)+' |' for row in rows])
def compact(obj):return json.dumps(obj,ensure_ascii=False,separators=(', ',': '))
add('''# OrgResource Project Proposal

Student Organization Resource Planning and Allocation System

## Template Pack Applicability Assessment

We adapted clear module boundaries, validation, traceable stock movements and explicit error rules from the reference pack. Its food production, sales, accounting and technology choices are outside this project.

## Group members

1. [Full name and student number]
2. [Full name and student number or remove]
3. [Full name and student number or remove]

CSCI180.05 Enterprise Systems Programming. Instructor Jeffrey Jongko. Submission date [actual date].

## Application description

OrgResource helps one student organization coordinate resources across departments. Each project states what it needs and when. A custodian checks those requirements against usable stock and existing commitments, approves feasible quantities, and records actual issues, returns or consumption. The system shows competing requests and the remaining sourcing gap before the organization proceeds with its separate official project and financial processes.

The GUIDON's18May2025 report describes departments separately requesting similar materials rather than coordinating sharing. It does not establish how often this occurs or which materials could be shared. MEA already describes digital inventory tracking and physical audits. OrgResource therefore focuses on connecting project requirements, dates and allocations, rather than claiming that a basic inventory list is new or that its benefits are already measured.

The application covers organization-owned or explicitly authorized resources. It does not replace OSA, SAPS, CFMO, university asset management, procurement, payments or accounting. A frontend is optional; the required backend is demonstrated through Postman.

## Main workflow and rules

1. An administrator creates departments and users. A custodian registers resources and records counted opening stock as RECEIPT transactions.
2. A department creates a project and its dated resource requirements. The project owner/head submits the plan; an organization officer gives internal planning approval.
3. The requester asks for an allocation. The assigned resource custodian approves only a feasible quantity. A requirement can receive several partial allocations.
4. The custodian issues reusable units and records inspected returns or loss. Consumables are recorded as consumed when handed over.
5. The organization views available quantities, overlapping commitments and consolidated shortages. Optional SMS reminds consenting recipients about upcoming or overdue reusable returns.

A requirement is demand; an allocation is a promise; a transaction is a physical/custody event. These remain separate records. Resources are interchangeable whole-unit pools; a distinctive device can have its own resource record with quantity1. Fractional units, repairs, serial tracking, future-purchase stock and automatic allocation optimization are outside the initial scope.

Reusable intervals are half-open [startAt,endAt): startA < endB and startB < endA means overlap. A booking ending at12:00 does not overlap one starting at12:00, but the later issue still requires the actual return. Approved unissued quantities reserve their interval. Currently issued quantities remain committed until their expected end; overdue outstanding quantities block all later periods until resolved. Availability based on an expected future return is explicitly conditional.

Reusable usable capacity U equals usable units in storage S plus usable units currently issued O. At any future instant, planned free = U minus remaining commitments active then. The service checks the minimum free across the requested interval using simultaneous load, not the sum of bookings that merely touch the interval. Actual issue also checks S and other commitments due now. Damage/loss can put existing promises at risk; it must be recorded honestly and unsafe new approval/issue is refused.

Consumable physical P equals receipts plus increases minus consumption, damage, loss and decreases. Reserved V is the total approved quantity not yet consumed or released, across all dates. Available-to-promise = P-V. Consuming q reduces P and V together, so it is not deducted twice. Consumables cannot use reusable ISSUE/RETURN operations.

For each requirement, unallocated demand = requested quantity minus approved effective coverage. Coverage retains already supplied quantities. A single-request gap compares that unallocated quantity with free capacity, but those gaps cannot simply be added. Consolidated reusable shortage is the peak of approved commitments plus all included unallocated demand minus U; consumable shortage compares combined unallocated demand against P-V. Show projects, periods and units behind each figure. This is planning information, not a purchase authorization.

Fictional example:10 cords; A needs6 during09:00-12:00, B needs5 during10:00-13:00, C needs4 during12:00-14:00. A6 and B4 can be approved, while B's fifth unit is a shortage during the overlap. C can reuse capacity after A, subject to return. The peak additional capacity is1 cord, not15-10=5. For consumables,100 sheets reserved60 then consumed20 leaves physical80, reserved40 and available40.

## Roles and lifecycle
''')
add(table(['Role','Permission'],[
('ADMIN','Maintain users/departments; assign roles. No stock-conflict override.'),('OFFICER','Internally approve/close/cancel projects, authorize recount adjustments and resource custodian reassignment.'),('HEAD','Manage own department projects/requirements and requests.'),('CUSTODIAN','Manage assigned resources; approve/reject requests; receive, issue, inspect returns and record damage/loss.'),('MEMBER','Manage own department draft projects/requirements and request allocations as owner.')]))
add('''Only the assigned CUSTODIAN, different from the requester, approves an allocation. Object/role checks occur in components. The local classroom demo identifies a seeded active user through X-Demo-User-Id; this is not authentication and the demo stays on127.0.0.1. A real shared deployment needs verified authentication separately.

Project states: DRAFT -> SUBMITTED -> APPROVED -> IN_PROGRESS -> COMPLETED. An officer can return SUBMITTED to DRAFT with a reason. Start requires the actual project interval. Cancellation releases unissued commitments but preserves outstanding custody; completion requires resolved/released allocations and a reason for waived unmet demand. These are internal states, not university approvals.

Allocation decision states: REQUESTED -> APPROVED or REJECTED; a pending or wholly unissued approval can become CANCELLED. After partial supply, release only the unissued remainder. Issued/returned/consumed/outstanding quantities are derived from the ledger rather than contradictory writable states. Approved dates/quantities are not ordinary editable fields. Rescheduling before any issue must revalidate the entire affected set atomically; after an issue it is refused. Ordinary requirement edits occur in DRAFT/SUBMITTED before approved supply; they cancel stale pending requests.

Inventory history and audit decisions are retained. Unused setup/draft rows can be physically deleted at runtime; dependencies return409. Posted movements are corrected by a constrained reversal/new observation, never overwritten. A reversal must be the latest reversible pool event, pass its dependency fence and preserve valid balances. Resource.revision is the internal command counter used for that fence, separate from JPA's version.

## Required technologies

Use the working class project: Jersey REST and JSON, Spring annotations and Autowired controller delegation, JPA/Spring Data with MySQL, Javax Validation, Retrofit/GSON for Twilio, and Spring scheduling. The assignment prints RetroJit; Retrofit/GSON is the interpretation supported by the syllabus and supplied retrofit2 imports, subject to confirmation. Preserve the class javax-era dependency baseline. Controllers bind and return DTOs; components own business rules/transactions; repositories perform persistence.

An allocation approval, movement, release, cancellation or reschedule is transactional. Each takes a PESSIMISTIC_WRITE lock on the existing Resource row before rereading quantities/commitments; multi-resource commands lock ascending IDs. This makes concurrent last-unit approvals serialize. A failed command rolls back stock and audit together. Reads combine balances and commitments in a consistent snapshot. External SMS runs after short queue/claim transactions, outside stock transactions.

## Entities and repositories

The nine entities below all have a distinct purpose; AuditEvent also records approvals/edits that do not move stock. All IDs are generated Long/BIGINT. Common fields are id, createdAt Instant and createdBy AppUser(nullable only for SYSTEM bootstrap/jobs). Mutable entities additionally have version Long, updatedAt Instant and updatedBy AppUser; InventoryTransaction and AuditEvent are immutable. Instants use UTC DATETIME(3); API date-times require offsets. Every required field uses Javax validation plus component reference/state checks.

JPA uses Entity, Id/GeneratedValue(IDENTITY), ManyToOne(LAZY) with explicit nullability, EnumType.STRING and Version on mutable entities. Foreign keys restrict deletion; no cascading removal of history. Each repository extends JpaRepository<Entity,Long> and JpaSpecificationExecutor<Entity>; inherited findById, findAll, save, delete, flush and count are used with the safety rules below. Field/repository lists define the complete persisted model; returned DTOs expose relation IDs and the derived balances/coverage described above.
''')
common={'id','createdAt','createdBy','version','updatedAt','updatedBy'}
dbtypes={'Long':'BIGINT','String':'VARCHAR','Instant':'DATETIME(3)','Boolean':'BOOLEAN','Integer':'INT'}
for name,e in model['entities'].items():
    add('### '+name)
    add(e['purpose']+' '+e['why'])
    rows=[]
    for f in e['fields']:
        if f['name'] in common:continue
        typ=f['java'];db='BIGINT FK' if typ in model['entities'] else dbtypes.get(typ,'VARCHAR enum')
        if typ.startswith('List'):db='owned collection'
        if 'TEXT' in f['constraint']:db='TEXT'
        rows.append((f['name'],typ+' / '+db,'Yes' if f['nullable'] else 'No',f['constraint']))
    add(table(['Field','Java and database type','Null','Constraint'],rows))
    add('Deletion: '+e['delete'])
    for r in model['repositories'][name]:
        add('Repository '+name+'Repository: '+r['returns']+' '+r['name']+'('+r['params']+'). '+r['purpose'])
add('''Database uniqueness covers department/resource/project codes, username, project-resource requirement pair, allocation requestKey, resource-commandKey, reversalOf, notification eventKey and nonnull providerSid. Foreign keys are indexed. Names/strings use their listed limits; quantity is1..1,000,000 whole units, not floating point. Ledger balances/counters cannot be negative. Required intervals lie within their project and end after start. Type/unit/control basis cannot change after references exist. Long strings in audit snapshots are redacted server-generated TEXT, not client input.

Enums below are stored as strings. Phone shape is plus and8..15 digits; live account/recipient eligibility still needs checking. NotificationAttempt is a value-owned collection with ordinal, attemptedAt, outcome, errorCode and providerSid; it is not an additional entity or a separate API.
''')
for n,vs in model['enums'].items():add(n+': '+', '.join(vs)+'.')
add('''The resource overlap query is explicit JPQL: select a from Allocation a where a.requirement.resource.id = :resourceId and a.decisionStatus = :approved and a.startAt < :end and a.endAt > :start. Its four parameters are resourceId, start, end and approved. Safe approval also loads overdue outstanding custody through the complete approved-resource query; finite overlap alone is insufficient. ResourceRepository.lockById and NotificationLogRepository.lockById use explicit id queries with PESSIMISTIC_WRITE.

## JSON and operation conventions

Every URL below is complete relative to http://127.0.0.1:8080. GET retrieves; POST submits. Headers are Accept application/json, Content-Type application/json for POST, and the local demo acting-user header. Path IDs are positive Long. Dates are ISO8601 offset instants; responses use UTC Z. Unknown fields, invalid quantities/intervals/enums or malformed IDs return400. Missing records404, permission failure403, missing/inactive actor401, duplicate/state/version/stock/dependency conflict409, unexpected server failure500. No422 and no200 error bodies are used.

Success201 creates a row with Location; repeated identical allocation/movement keys return200 without repeating effects. A changed payload with the same key returns409 IDEMPOTENCY_KEY_REUSED. Successful updates/actions/reads return200. Safe deletion returns204 with no body; immutable history deletion always returns409. Other updates require expectedVersion. All errors use the following JSON shape; details holds fieldErrors, currentVersion, competing IDs or available quantity where relevant. No SQL/stacktrace/secrets are returned.
''')
add(compact(dict(timestamp='2026-11-03T02:00:00Z',status=409,error='Conflict',code='RESOURCE_OVERALLOCATION',message='The requested quantity exceeds capacity for the selected period.',path='/api/allocations/1/approve',requestId='00000000-0000-4000-8000-000000000002',retryable=False,details=dict(availableQuantity=4,requestedQuantity=5,competingAllocationIds=[1]))))
add('''A named EntityView is JSON containing its common/model fields, replacing related entities with xxxId and omitting payloadHash. Contact values are null for viewers without permission. ResourceView also has physicalInStore, outstanding, damagedInStore, usableOwned, reservedConsumable and availableConsumable. RequirementView adds coveredQuantity, suppliedQuantity, outstandingQuantity and unallocatedQuantity. AllocationView adds issuedQuantity, resolvedQuantity, outstandingQuantity, unissuedQuantity, fulfillment, overdue and replay. InventoryTransactionView adds replay and impactedAllocationIds. No client may submit these computed fields.

Page<EntityView> is {content:[EntityView],page,size,totalElements,totalPages}. Lists default page0,size20,max100,id ascending; empty results return200 with an empty array. Query filters are exact matches except resource q searches code/name; from/to are half-open event/overlap windows. Availability/shortage horizons are at most90days. Success JSON is specified once here and reused by the operation table to avoid repeated identical examples.
''')
add('### Complete request JSON shapes')
add('Each OpIDRequest below permits exactly the shown keys. Types/nullability/limits follow its entity field or common conventions above. expectedVersion is Long>=0; reason is max1000 and required for rejection, cancellation, release, reversal, adjustment and waiver. Keys are UUID strings. A null optional reference is permitted only for its documented transaction type. {} actions accept no additional keys. IDs and dates in these shapes are placeholders populated through earlier Postman calls, not guessed database values.')
groups={}
for o in ops:
    if o['body'] is not None:groups.setdefault(tuple(sorted(o['body'].keys())),[]).append(o)
for key,group in groups.items():
    sample=dict(group[0]['body'])
    if len(group)>1 and 'reason' in sample:sample['reason']='Reason for this action'
    add(', '.join(o['requestDto'] for o in group)+': '+compact(sample))
add('Inventory input rules: allocationId is required for ISSUE/RETURN/CONSUMPTION and custody LOSS, otherwise absent/null except a derived reversal link. inspection is required only for RETURN. reversalOfId is required only for REVERSAL; correctionReferenceId optionally identifies an earlier event in the same pool. Actors/timestamps/delta fields are always server assigned. User consent may be granted only by that user; an administrator can revoke but cannot opt someone else in.')
add('### List filters')
add(table(['List','Allowed filters in addition to page and size'],[(o['path'],', '.join(k for k in o['query'] if k not in ('page','size'))) for o in ops if o['id'].endswith('L')]))
add('Other GET query parameters:')
for o in ops:
    if o['query'] and not o['id'].endswith('L'):add(o['queryDto']+' for '+o['path']+': '+('; '.join(k+' '+v for k,v in o['query'].items()))+'.')
add('### Complete controller operation catalogue')
add('Every controller method below has the same method name as its forwarded component method and uses Autowired to reach that component. Inputs are the named request DTO plus Long id when present and Actor; GET filters use the named OpIDQuery. The component recipes following the catalogue supply ordered processing steps. Every row has the common400/401/403/500 failures; every id/reference lookup can return404. The final column names additional409 cases without repeating the common JSON error format.')
state={'P01':'SUBMITTED','P02':'APPROVED','P03':'DRAFT','P04':'IN_PROGRESS','P05':'COMPLETED','P06':'CANCELLED','A01':'APPROVED','A02':'REJECTED','A03':'CANCELLED','A05':'ISSUE','A06':'RETURN','A07':'CONSUMPTION','T01':'REVERSAL'}
for domain in dict.fromkeys(o['domain'] for o in ops):
    subset=[o for o in ops if o['domain']==domain]
    add('#### '+subset[0]['controller']+' to '+subset[0]['component'])
    rows=[]
    for o in subset:
        inp=o['requestDto'] if o['body'] is not None else o['queryDto'] if o['query'] else 'none'
        if '{id}' in o['path']:inp='id; '+inp
        result='409 immutable history' if o.get('alwaysRefuses') else str(o['success'])+' '+('no body' if o['view']=='NoContent' else o['view'])
        if o['id'] in state:result+='; '+state[o['id']]
        ec=[c for c in o['errors'] if c not in ('BAD_REQUEST','VALIDATION_FAILED','UNAUTHENTICATED','FORBIDDEN','NOT_FOUND','INTERNAL_ERROR')]
        errors=', '.join(ec).replace('RESOURCE_OVERALLOCATION','stock conflict').replace('REQUIREMENT_COVERAGE_EXCEEDED','coverage exceeded').replace('IDEMPOTENCY_KEY_REUSED','changed key payload').replace('REVERSAL_DEPENDENCY_CONFLICT','reversal dependency').replace('INSUFFICIENT_PHYSICAL_STOCK','physical stock').replace('RESOURCE_TYPE_MISMATCH','wrong resource type').replace('NOTIFICATION_NOT_ELIGIBLE','not due').replace('PROVIDER_OUTCOME_UNKNOWN','unknown provider outcome').replace('DEPENDENCY_EXISTS','dependent rows').replace('IMMUTABLE_HISTORY','immutable history').replace('VERSION_CONFLICT','stale version').replace('INVALID_STATE','invalid state').replace('RETRY_REQUIRED','concurrent retry').replace('DUPLICATE_RECORD','duplicate')
        rows.append((o['id']+' '+o['http']+' '+o['path'],inp,result,o['method']+'; '+errors))
    add(table(['ID method and URL','Input','Output','Forwarded method and additional conflict cases'],rows))
add('''The additional error labels in the table correspond to the uppercase domain codes, such as VERSION_CONFLICT, DEPENDENCY_EXISTS, INVALID_STATE and RESOURCE_TYPE_MISMATCH, in the attached Postman examples. Validation and authorization are never bypassed by a convenience route. Direct inventory POST and allocation issue/return/consume call the same typed movement logic. J01 is registered only for the local demo; its manual scan creates no physical stock and sends no message by itself.

AvailabilityView JSON has resourceId, asOf, startAt, endAt, unit, physicalInStore, outstanding, usableOwned, reservedConsumable, rawMinimumFree, allocatableQuantity, overdueOutstanding, returnDependentQuantity, segments[{startAt,endAt,committed,rawFree,allocationIds}],warnings. ShortageView has projectId,asOf,requirements[{requirementId,resourceId,unit,demand,approvedCoverage,supplied,outstanding,unallocated,freeIfConsideredAlone,sourcingGapIfConsideredAlone,atRisk}],warning. DemandView has resourceId,asOf,from,to,includeDraft,unit,minimumAdditionalUnits,segments[{startAt,endAt,usableCapacity,approvedCommitted,unallocatedDemand,deficit,requirementIds,projectIds,departmentIds}],reasons,warning. InventoryReport contains asOf/items of current per-resource balances; AllocationReport contains asOf/items of dated allocation/unissued/outstanding/overdue/risk records; DepartmentReport contains asOf,departmentId,from,to/items of resource-unit demand/coverage/supply. JobRunView contains asOf,created,skipped,existing,errors,providerMode.

## Components and ordered processing

All public writes are component transactions with rollback on failure; reads use consistent read-only transactions. Scheduled/provider work uses short separate database phases. Each controller-invoked method is listed above; these shared recipes describe its ordered steps without repeating the same retrieval/validation boilerplate68times.

DepartmentComponent: create normalizes/validates the unique code, checks ADMIN, saves and audits. update checks version/role and fields, checks active dependencies, then saves/audits. list/get apply filters, retrieve or404, and map DTOs. delete checks ADMIN/version and absence of users/projects/resources, audits and deletes or409.

UserComponent: create checks ADMIN, unique username, role and active department, then saves with no initial SMS consent and audits. update checks version; role/department/active changes are ADMIN only, while phone/consent changes are self/admin with consent grant only by self; prevent loss of the last ADMIN or orphaning active responsibility. Save and audit redacted changes. list/get redact contacts; delete allows only wholly unreferenced users, otherwise deactivate after responsibility is reassigned.

ResourceComponent: create validates control authority, active department and custodian, then saves metadata with zero stock and audits. update locks the pool and checks version; referenced code/type/unit/control basis stay fixed; custodian reassignment is officer-only with reason. list/get compute ledger balances. delete requires no references; archival requires no pending/unissued/outstanding obligation.

ProjectComponent: create checks owner/department, interval and unique code, then saves DRAFT. update permits safe versioned metadata only. submit checks DRAFT and valid requirements; approve checks officer and SUBMITTED; returnDraft needs officer/reason and no approved supply; start checks APPROVED/current interval. Each changes state and audits. complete requires resolved/released commitments and explicit reason for unmet-demand waiver. cancel locks affected pools, cancels pending/unissued promises and releases partial remainders, retains outstanding custody, then audits all changes atomically. reschedule locks all affected pools in ascending order, rejects any prior issue/consumption, validates the complete replacement requirement-window set against all unaffected commitments, then saves every changed window or none. delete is unreferenced DRAFT only. list/get retrieve/match filters.

RequirementComponent: create locks its pool, verifies owner/head and DRAFT/SUBMITTED project, unique project-resource pair, positive quantity and enclosed interval, then saves ACTIVE; a submitted plan returns to DRAFT. update distinguishes edits before approved supply from withdrawal after obligations resolve; cancel stale pending requests, save and audit. IDs cannot be reassigned. list/get add derived coverage quantities. delete requires no allocation/movement reference.

AvailabilityComponent: query loads one consistent ledger/approved-allocation snapshot, derives S/O/B/U or P/V, includes overdue holds, clips commitment endpoints and sweeps simultaneous segments; return minimum free, conflicts and conditional-return warning. calculateLocked uses the same computation inside a caller's locked transaction. balanceLocked reconstructs signed ledger effects and rejects impossible balances. No availability method approves or changes data.

AllocationComponent: request locks resource/requirement, checks actor/project/recipient and requestKey replay, verifies quantity does not exceed unallocated demand, copies dates and saves REQUESTED. approve re-locks/re-reads, checks assigned non-requesting custodian, current state/coverage and full-window capacity; only then save APPROVED with actor/time/reason and audit. reject requires pending state and reason. cancel requires pending or wholly unissued approval; release increases releasedUnissued only within remaining unissued quantity. Neither removes history. issueResources, returnResources and consumeResources construct the typed inventory request and forward to InventoryTransactionComponent.record. list/get derive progress; delete is unreferenced REQUESTED only.

InventoryTransactionComponent: record locks the pool, checks permission/type/references and commandKey replay, rebuilds balances/counters, validates effect and applies exactly one immutable movement with actor/time/reason; increment pool revision and append audit in the same transaction. ISSUE moves S to O; usable RETURN moves O to S; damaged RETURN moves O to quarantined B; custody LOSS removes O. Consumable CONSUMPTION removes physical units and its remaining reservation together. RECEIPT/increase add usable storage; storage damage/loss/decrease remove it, with officer authorization for recount adjustments. Negative physical/custody counters are refused; observed capacity loss reports affected promises. reverse requires the latest unreversed event and matching pool revision, negates its recorded vector, rechecks all invariants and appends a new REVERSAL. list/get return history; delete always409.

ShortageComponent: projectSummary loads requirements/coverage and computes remaining demand and candidate gaps separately; consolidate groups eligible departmental demand with existing commitments and sweeps its simultaneous reusable load, or compares total consumable demand against globally reserved current stock. Return the underlying periods/projects/units. No automatic purchases or allocation priority override.

ReportingComponent: inventory reconstructs per-resource balances; upcoming filters approved intervals; overdue retains reusable outstanding quantity past endAt; departmentDemand groups requirements/coverage/supply by resource/unit. Read one consistent snapshot, paginate accurately and return asOf. Do not add incompatible units or invent utilization/savings.

AuditComponent: append writes redacted before/after, actor, target and reason inside the main command transaction. Public appendNote accepts NOTE only for a real target. list/get retrieve visible history; delete always409. It also records runtime deletions without losing the former target identity. No generic client can fabricate approvals or overwrite audit rows.

PermissionComponent checks the actual active user, role, owner/department and assigned custodian before each command. ActorResolver supplies that actor from the local demo header or a separately verified authenticated principal. Exception mappers return the common error JSON; controllers contain no stock arithmetic or database transaction logic.

## Twilio scheduling and initialization

TwilioIntegrationComponent.sendSms sends the server-configured Retrofit form POST to /2010-04-01/Accounts/{AccountSid}/Messages.json with To, MessagingServiceSid and Body plus server-side Authorization. fetchStatus reads a known SID only. Credentials are environment configuration and are excluded from source/Postman/video. FakeTwilio uses the supplied lab adapter with dummy credentials; its success is not live delivery. Confirm the instructor's final expectation.

NotificationComponent.enqueueManual/enqueueEvent checks outstanding reusable custody, due window, recipient and consent; compute unique kind:allocation:endAt:recipient key and save QUEUED or SKIPPED. scanReminders finds upcoming returns within24hours and overdue returns after endAt, one event of each kind per due date. claim locks a due queue record, rechecks current custody/consent, marks SENDING and commits; dispatch then calls Twilio outside the transaction; finish saves acceptance/failure/UNKNOWN in a new transaction. No repeated reminder after a full return. cancel affects an unattempted queue only; refresh reads a known provider SID; retry is permitted only for a definitively unaccepted failure within the attempt limit.

SchedulingComponent.scan runs every60seconds and dispatch every5seconds using EnableScheduling/Scheduled, one process/runner and injected UTC Clock. A stale SENDING after120seconds becomes UNKNOWN, not resent automatically. Only confirmed429/error20429 can retry after60 then300seconds, max3attempts. A timeout after possible acceptance remains UNKNOWN. Five attempted SMS per local day is the default demonstration cap. Acceptance and delivered status are distinct. SMS failure cannot roll back an allocation or movement.

DemoSeedBootstrap.initialize is DEMO-only PostConstruct. It calls the separately injected transactional SeedComponent.bootstrapAdmin to create/return the one local administrator on a fresh database, and optionally seedScenario to create fictional departments/users/resources/projects/requirements/allocations through the same rules. Stable codes/UUIDs prevent duplicate stock on restart. Full seeds are disabled for the runtime-create Postman demonstration. No phones, consent or live SMS are seeded; ordinary operation can populate every entity through its explicit endpoints.

## Postman demonstration and submission

The separate full API collection defines all68calls. The ordered demonstration creates three departments, users, two resources and opening receipts; captures every returned id/version; creates projects/requirements and internal approvals; demonstrates A6/B5 conflict, B4 partial allocation, C's later booking, consumable reserve/consume arithmetic, issue/inspected returns, reports, notification deduplication and runtime deletion. The negative collection covers invalid input, wrong resource type, unauthorized approval, duplicate key changes and protected history. No guessed ID is required; the initial administrator ID comes from the local bootstrap output. Current intervals make issue demonstration possible without changing the clock.

Verify repository startup/JPQL and field validation; actual MySQL concurrent last-unit approvals must yield exactly one winner. Test partial returns, cancellation with outstanding custody, damage-induced risk, safe reversal, repeated consumption/return keys, stale edits, reminder retry/UNKNOWN outcomes and outage isolation. Unit/documentation checks do not substitute for executed REST/database/provider tests.

Skeleton: demonstrate database connectivity, all entity tables, fictional test data, valid repositories, reachable Autowired controllers/components and correct Postman JSON; submit exported project and video. Final: actual logic, finalized export, recorded group discussion and peer reviews; demonstrate optional UI if built. The assignment lists proposal30October2026, partial target13November with Canvas submission14November, and final5December. Check current Canvas instructions because the proposal date has passed; do not assume an extension.

Before submission, fill member/date placeholders, review the RetroJit/Retrofit interpretation, class dependency baseline, image expectation under all-tools wording, FakeTwilio/live expectation and protected-history deletion interpretation. Students complete their own truthful Certificate of Authorship, acknowledgement of outside assistance, and peer reviews of other members rated1-100with comments. No submission, email, certification or rating has been performed on their behalf.

## References

ESP Project Proposal2026, supplied assignment, pages1-3. CSCI180.05 syllabus2026-27, supplied course outcomes, submission and authorship policies; supplied Jersey/JPA/validation/Retrofit/scheduling examples and Lab4 FakeTwilio. The GUIDON, Following the funds,18May2025: https://theguidon.com/2025/05/following-the-funds-the-gaps-in-the-sanggunians-financial-processes-2/ . Ateneo MEA, MEA Room description: https://www.ateneomea.com/cs . Full source boundaries and technical contracts remain in the separate complete specification; this concise proposal preserves its nine-entity,68-operation design.
''')
text='\n\n'.join(parts)+'\n'
# The separate submission follows Part I of the professor's PDF. The full
# specification above remains unchanged in its own files and Word document.
sections={}
matches=list(re.finditer(r'^## (.+)$',text,re.M))
for j,m in enumerate(matches):sections[m[1]]=text[m.end():matches[j+1].start() if j+1<len(matches) else len(text)].strip()
preface='# OrgResource Project Proposal\n\nStudent Organization Resource Planning and Allocation System\n\n'
app=sections['Application description']
tech=sections['Required technologies'].split('\n\n')[0]
wire=sections['JSON and operation conventions']
full_api=(ROOT/'05-REST-API-CATALOGUE.md').read_text(encoding='utf-8-sig')
error_rows=[(status+' '+code,message) for status,code,message in re.findall(r'^(400|401|403|404|409|500) ([A-Z_]+): (.+)$',full_api,re.M)]
wire=wire.replace('### Complete request JSON shapes','### Error messages\n\n'+table(['Status and code','Message'],error_rows)+'\n\n### Complete request JSON shapes')
component_intro='''For each list/get method:1validate the ID/filters and actor;2retrieve or return404;3check scope and map the documented JSON. For each create/update/action:1resolve the actor and record, including version where required;2validate input/references/state and the method-specific rules below;3persist and audit atomically, returning the documented view. Deletion checks dependencies before removal and never removes posted history. These common ordered steps apply to every method in the controller catalogue; the paragraphs below specify each component's additional steps.

Allocation approval and stock changes lock the parent Resource before rereading stock/commitments. Multi-resource changes lock ascending resource IDs. Failure rolls back the core change and audit together. SMS work runs separately after short queue transactions.'''
components=sections['Components and ordered processing']
components=components.replace('All public writes are component transactions with rollback on failure; reads use consistent read-only transactions. Scheduled/provider work uses short separate database phases. Each controller-invoked method is listed above; these shared recipes describe its ordered steps without repeating the same retrieval/validation boilerplate68times.',component_intro)
calculation='''Availability rules: let S be usable storage, O outstanding reusable custody, B damaged storage and U=S+O. For reusable windows [start,end), overlap is startA<endB and startB<endA. Compute U minus remaining approved commitments on each simultaneous segment and use the minimum; do not sum sequential bookings. Outstanding custody past end becomes an open-ended hold. Future availability relying on an expected return is conditional; actual issue requires returned physical stock. For consumables, P=receipts-consumption-damage-loss+/-adjustments, V=approved unconsumed/unreleased commitments across all dates, and available=P-V. Consumption decreases P and V together. Unallocated demand=requested-approved effective coverage; consolidated shortage tests all included demand together, never sums isolated gaps. Example:10cords, A6[09,12), B5[10,13), C4[12,14) require1additional cord at peak, not5. Damage can make an existing promise at risk; record it and refuse unsafe new promises.'''
components=components.replace('AvailabilityComponent: query',calculation+'\n\nAvailabilityComponent: query')
scope='Roles are fixed: ADMIN maintains accounts/departments; OFFICER decides internal plans/recounts; HEAD manages its department; CUSTODIAN controls assigned resources; MEMBER manages its own draft plans. Only the assigned custodian, different from the requester, approves stock. The seeded demo header identifies an active actor for local testing; it is not authentication and the demo stays on127.0.0.1.'
postman='''Postman definitions accompany every one of the68REST calls, with headers, JSON, responses and errors. The ordered fictional demonstration creates departments/users/resources, records opening stock, captures generated IDs, creates project requirements, shows overlapping allocation rejection and partial approval, records issue/return/consumption, inspects shortages/reminders, and creates/deletes unused rows at runtime. The administrator ID comes from local bootstrap output; later IDs/versions come from prior responses. The same backend remains usable without a UI.

The skeleton will demonstrate database connectivity, generated tables, test data, valid repositories and reachable controllers/components with correct Postman JSON. The final adds completed actual logic and the recorded group discussion; any optional UI is also demonstrated. Each member supplies their own peer review of other members, rated1-100with comments, and the required authorship/help acknowledgement. Group/date placeholders and the RetroJit/Retrofit, class-library, final Twilio, image and safe-history deletion interpretations require review before submission.'''
refs='Sources: supplied ESP Project Proposal2026, pages1-3; CSCI180.05 syllabus; class examples and Lab4 FakeTwilio. Public rationale: The GUIDON18May2025, https://theguidon.com/2025/05/following-the-funds-the-gaps-in-the-sanggunians-financial-processes-2/ ; MEA, https://www.ateneomea.com/cs .'
text=preface+'## Group members\n\n'+sections['Group members']+'\n\n## Template Pack Applicability Assessment\n\n'+sections['Template Pack Applicability Assessment']+'\n\n## Prose description of the application\n\n'+app+'\n\n'+scope+'\n\n'+tech+'\n\n## Controller operations\n\n'+wire+'\n\n## Components and important method steps\n\n'+components+'\n\n'+sections['Twilio scheduling and initialization']+'\n\n## Entities and repositories\n\n'+sections['Entities and repositories']+'\n\n## Postman and project demonstration\n\n'+postman+'\n\n## References\n\n'+refs+'\n'
text=text.replace('RequirementView adds','ResourceRequirementView adds').replace('CSCI180.05','CSCI 180.05').replace('Proposal2026','Proposal 2026').replace('The GUIDON18May2025','The GUIDON 18 May 2025')
text=re.sub(r'\b(the|all|every|of|by|within|after|before)(\d+)',r'\1 \2',text)
text=re.sub(r'(\d)(seconds|hours|times|days|calls|with|cords|sheets)\b',r'\1 \2',text)
(ROOT/'OrgResource-Concise-Submission-Proposal.md').write_text(text,encoding='utf-8')
print(json.dumps(dict(words=len(text.split()),characters=len(text),entities=len(model['entities']),operations=len(ops))))

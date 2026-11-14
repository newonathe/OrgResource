"""Revise only the separate concise proposal to the requested Part I content."""
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
model=json.loads((ROOT/'contracts/domain-model.json').read_text())
ops=json.loads((ROOT/'contracts/operations.json').read_text())
views=json.loads((ROOT/'contracts/response-examples.json').read_text())
out=[]
def add(s):out.append(s.strip())
def table(headers,rows):return '\n'.join(['| '+' | '.join(headers)+' |','|'+'|'.join(['---']*len(headers))+'|']+['| '+' | '.join(str(x).replace('|','/').replace('\n',' ') for x in row)+' |' for row in rows])
def js(x):return json.dumps(x,ensure_ascii=False,separators=(', ',': '))
labels={'DepartmentView':'Department','AppUserView':'User','ProjectView':'Project','ResourceView':'Resource','ResourceRequirementView':'Requirement','AllocationView':'Allocation','InventoryTransactionView':'Transaction','NotificationLogView':'Notification','AuditEventView':'Audit note','AvailabilityView':'Availability','ShortageView':'Project shortage','DemandView':'Combined demand','InventoryReport':'Inventory report','AllocationReport':'Allocation report','DepartmentReport':'Department report','JobRunView':'Reminder scan'}
err_names={'BAD_REQUEST':'Invalid input','VALIDATION_FAILED':'Invalid field','UNAUTHENTICATED':'No acting user','FORBIDDEN':'Not allowed','NOT_FOUND':'Not found','DUPLICATE_RECORD':'Duplicate','DEPENDENCY_EXISTS':'In use','IMMUTABLE_HISTORY':'Protected history','VERSION_CONFLICT':'Changed record','INVALID_STATE':'Wrong state','RESOURCE_OVERALLOCATION':'Not enough available','REQUIREMENT_COVERAGE_EXCEEDED':'Demand already covered','INSUFFICIENT_PHYSICAL_STOCK':'Not enough in storage','RESOURCE_TYPE_MISMATCH':'Wrong resource type','SCHEDULE_LOCKED':'Schedule already used','IDEMPOTENCY_KEY_REUSED':'Reused action key','REVERSAL_DEPENDENCY_CONFLICT':'Unsafe correction','RETRY_REQUIRED':'Busy resource','NOTIFICATION_NOT_ELIGIBLE':'Reminder not due','PROVIDER_OUTCOME_UNKNOWN':'Unconfirmed SMS','INTERNAL_ERROR':'Server error'}
add('''# OrgResource Project Proposal

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
''')
add(js(dict(id=1,code='LOG',name='Logistics',description='Handles shared resources',active=True,createdAt='2026-11-03T02:00:00Z',createdById=1,version=0,updatedAt='2026-11-03T02:00:00Z',updatedById=1)))
add('''A list returns {"content":[record],"page":0,"size":20,"totalElements":1,"totalPages":1}. With no matches, content is empty. Record means the JSON for the named entity, not a quoted string.

Resource JSON also includes physicalInStore, outstanding, damagedInStore, usableOwned, reservedConsumable and availableConsumable. Requirement JSON adds coveredQuantity, suppliedQuantity, outstandingQuantity and unallocatedQuantity. Allocation JSON adds issuedQuantity, resolvedQuantity, outstandingQuantity, unissuedQuantity, fulfillment, overdue and replay. Transaction JSON adds replay and impactedAllocationIds. These quantities are calculated from saved records. Phone/consent details are returned only to the user or administrator.

fulfillment is NONE, PARTIAL, OUTSTANDING, RESOLVED, RELEASED or EXPIRED_UNISSUED. It describes progress and cannot be edited. overdue is true when reusable custody remains after its expected return time.

### Request bodies

The body label in each operation table identifies its JSON below. Fields have the types and limits listed under entities; expectedVersion is a nonnegative Long and reason is text up to1000characters. A reason is required for rejection, cancellation, release, adjustment, correction or waiving unmet demand. {} accepts no fields. Omitted optional update fields stay unchanged; null clears only a nullable field.
''')
def example_body(body):
    def value(k,v):
        if isinstance(v,list):return [{a:value(a,b) for a,b in r.items()} for r in v]
        if not isinstance(v,str) or not v.startswith('{{'):return v
        if k.endswith('Id'):return 2 if k=='custodianId' else 3 if k in ('ownerId','recipientId') else 1
        if k in ('startAt','endAt'):return '2026-11-10T09:00:00+08:00' if k=='startAt' else '2026-11-10T12:00:00+08:00'
        return '00000000-0000-4000-8000-000000000001'
    return {k:value(k,v) for k,v in body.items()}
groups={}
for o in ops:
    if o['body'] is not None:groups.setdefault(tuple(sorted(o['body'])),[]).append(o)
for _,group in groups.items():
    b=example_body(group[0]['body'])
    if len(group)>1 and 'reason'in b:b['reason']='Reason for this action'
    add('Bodies '+', '.join(o['id'] for o in group)+': '+js(b))
add('''For a transaction, allocationId is required for ISSUE, RETURN, CONSUMPTION and custody LOSS. Other storage movements have no allocation. inspection is required only for RETURN. REVERSAL uses reversalOfId to identify the original; correctionReferenceId optionally links a new observation to an earlier transaction in the same resource. Actors, times and signed changes are set by the component. SMS consent can be granted only by that user; an administrator can revoke it.

### Query parameters

Lists accept page (integer, default0), size (1..100, default20) and the optional filters below. IDs are Long; active/includeDraft are Boolean; role/type/status/kind use the allowed values in the entity tables; q/category are text; from/to are date-times. q searches resource code/name, up to100characters. List from/to filters select matching events or overlapping project dates. Records are ordered by ID.
''')
add(table(['List URL','Optional filters'],[(o['path'],', '.join(k for k in o['query'] if k not in ('page','size'))) for o in ops if o['id'].endswith('L')]))
special_queries={
'Q01':'resourceId, startAt and endAt are required; candidateQuantity is optional. The interval must start now or later and end after startAt.',
'Q03':'from and to are required; includeDraft defaults to false. Shows demand for the resource identified by path id.',
'R01':'departmentId, page and size are optional.',
'R02':'from and to are required; departmentId, page and size are optional.',
'R03':'resourceId, departmentId, page and size are optional.',
'R04':'departmentId, from and to are required.'}
for code,desc in special_queries.items():add('Query '+code+': '+desc)
add('Date-range reports and availability queries are limited to90days. Unless listed in the table, an operation has no query parameters.')
add('### Error messages')
add('Failures return the real HTTP status and the JSON below, never a successful200 response. details identifies invalid fields, the current version or the conflicting quantities/IDs when relevant.')
add(js(dict(timestamp='2026-11-03T02:00:00Z',status=409,error='Conflict',code='RESOURCE_OVERALLOCATION',message='The requested quantity exceeds capacity for the selected period.',path='/api/allocations/1/approve',requestId='00000000-0000-4000-8000-000000000002',retryable=False,details=dict(availableQuantity=4,requestedQuantity=5,competingAllocationIds=[1]))))
api=(ROOT/'05-REST-API-CATALOGUE.md').read_text(encoding='utf-8-sig')
errors=re.findall(r'^(400|401|403|404|409|500) ([A-Z_]+): (.+)$',api,re.M)
add(table(['Error and status','Message'],[(err_names[code]+' ('+status+')',msg) for status,code,msg in errors]))
add('Every operation can return Invalid input, Invalid field, No acting user, Not allowed or Server error. A missing target/related record returns Not found. The operation tables list additional applicable errors. Busy resource means nothing was saved and the request can be tried again after retrieving current data.')
add('### Operations')
purpose={'create':'Create a record.','list':'List matching records.','get':'Read one record.','delete':'Delete an unused record.','update':'Update allowed details.','request':'Request an allocation.','record':'Record a stock or custody change.','appendNote':'Add an audit note.','enqueueManual':'Queue an eligible reminder.'}
specific={
'01C':['DUPLICATE_RECORD'],'02C':['DUPLICATE_RECORD','INVALID_STATE'],'03C':['DUPLICATE_RECORD','INVALID_STATE'],'04C':['DUPLICATE_RECORD','INVALID_STATE'],'05C':['DUPLICATE_RECORD','INVALID_STATE'],'06C':['IDEMPOTENCY_KEY_REUSED','INVALID_STATE','REQUIREMENT_COVERAGE_EXCEEDED'],'08C':['NOTIFICATION_NOT_ELIGIBLE'],'09C':[]}
result_state={'P01':'SUBMITTED','P02':'APPROVED','P03':'DRAFT','P04':'IN_PROGRESS','P05':'COMPLETED','P06':'CANCELLED','A01':'APPROVED','A02':'REJECTED','A03':'CANCELLED','A05':'ISSUE','A06':'RETURN','A07':'CONSUMPTION','T01':'REVERSAL'}
def response(o):
    if o.get('alwaysRefuses'):return '409 Protected history'
    if o['view']=='NoContent':return '204; no body'
    if o['view'].startswith('Page<'):r=labels[o['view'][5:-1]]+' list'
    else:r=labels[o['view']]
    return str(o['success'])+' '+r+(' '+result_state[o['id']] if o['id']in result_state else '')
for domain in dict.fromkeys(o['domain']for o in ops):
    subset=[o for o in ops if o['domain']==domain]
    add('#### '+subset[0]['controller']+' forwards to '+subset[0]['component'])
    rows=[]
    for o in subset:
        inp='body '+o['id'] if o['body'] is not None else 'query '+o['id'] if o['query'] else 'none'
        if '{id}'in o['path']:inp='id; '+inp
        codes=specific.get(o['id'],[c for c in o['errors'] if c not in ('BAD_REQUEST','VALIDATION_FAILED','UNAUTHENTICATED','FORBIDDEN','NOT_FOUND','INTERNAL_ERROR','RETRY_REQUIRED')])
        desc=purpose.get(o['method'],o['purpose'])
        if o.get('alwaysRefuses'):desc='Refuse deletion of posted history.'
        rows.append((o['id']+' '+o['http']+' '+o['path']+'\n'+desc,inp,response(o),o['method']+'; '+(', '.join(err_names[c]for c in codes)or'common errors only')))
    add(table(['Operation and purpose','Input','JSON output','Forwarded method and errors'],rows))
add('### Planning and report JSON')
add('The following are the output formats used by the planning/report operations. Quantities in these examples are illustrative.')
for n in ['AvailabilityView','ShortageView','DemandView','InventoryReport','AllocationReport','DepartmentReport','JobRunView']:add(labels[n]+': '+js(views[n]))

add('''## Components and method steps

All changes check the acting user's role and responsibility. ADMIN manages users/departments; OFFICER handles internal project decisions and recount adjustments; HEAD manages department plans; CUSTODIAN manages assigned resources; MEMBER manages their own draft plans. A requester cannot approve their own allocation.

Common read steps:1check the ID/filters;2retrieve the record(s), returning Not found where applicable;3return the permitted JSON fields. Common change steps:1retrieve/check the record and current version;2validate the supplied fields, related records and method-specific rules below;3save the change and audit together;4return the result. A failed change leaves the earlier data unchanged. The same resource cannot be approved or issued by two changes at the same time.
''')
components={
'DepartmentComponent':('Maintains the organization\'s departments.',[
('create','1. Check required name/code and duplicate code. 2. Save the active department. 3. Return it.'),('update','1. Apply the common change steps. 2. Before deactivation, check that active users/resources/projects do not still depend on it.'),('list, get','Use the common read steps.'),('delete','1. Check that no user, resource or project uses it. 2. Delete it, or return In use.')]),
'UserComponent':('Maintains members, roles and optional SMS preferences.',[
('create','1. Check administrator permission, username, role and department. 2. Save an active user with SMS consent off.'),('update','1. Check the current version and allowed editor. 2. Restrict role/department changes to administrators; consent can be granted only by the user. 3. Prevent removal of the last administrator or an unresolved responsibility. 4. Save the change.'),('list, get','Use the common read steps; hide contact/consent details from other members.'),('delete','1. Check that no history or current responsibility uses the user. 2. Delete if unused; otherwise deactivate after responsibilities are reassigned.')]),
'ResourceComponent':('Maintains the resource catalogue and displays its actual balances.',[
('create','1. Check ownership/authorization, department, custodian, type and unit. 2. Save the resource with zero stock; a receipt adds opening stock.'),('update','1. Check permission/version. 2. Keep code/type/unit/control basis fixed once referenced. 3. Allow officer-approved custodian reassignment and safe detail changes.'),('list, get','Use the common read steps and calculate stock from transactions.'),('delete','1. Check that no requirement/allocation/transaction uses it. 2. Delete only if unused; otherwise archive once commitments/custody are resolved.')]),
'ProjectComponent':('Maintains departmental projects and their planning status.',[
('create, update','1. Check owner/department, dates and required fields. 2. Create DRAFT or update allowed details. 3. Save and return the project.'),('list, get','Use the common read steps.'),('submit','1. Require DRAFT and valid resource requirements. 2. Set SUBMITTED.'),('approve, returnDraft','1. Require an officer and SUBMITTED. 2. Approve to APPROVED, or return to DRAFT with a reason and no approved supply.'),('start','1. Require APPROVED and the current project interval. 2. Set IN_PROGRESS.'),('complete','1. Require all allocations resolved or released. 2. Require a reason for waiving unmet demand. 3. Set COMPLETED.'),('cancel','1. Check permission and reason. 2. Cancel pending/unissued promises and release unused remainders. 3. Keep outstanding custody for later return/loss resolution. 4. Set CANCELLED.'),('reschedule','1. Reject if any project allocation has been issued/consumed. 2. Check every replacement requirement interval against other commitments. 3. Save all changed dates together, or none.'),('delete','1. Require an unused DRAFT with no requirements. 2. Delete it.')]),
'RequirementComponent':('Records what each project needs.',[
('create','1. Check owner/head, project state, resource, quantity and dates within the project. 2. Reject duplicate project-resource lines. 3. Save ACTIVE; return a changed submitted plan to DRAFT.'),('update','1. Allow demand edits only in DRAFT/SUBMITTED before approved supply. 2. Cancel stale pending requests. 3. Allow withdrawal only after obligations are resolved. 4. Save; do not reassign project/resource IDs.'),('list, get','Use the common read steps; add approved coverage, supplied, outstanding and unallocated quantities.'),('delete','1. Check that no allocation/movement uses the requirement. 2. Delete only if unused.')]),
'AvailabilityComponent':('Checks usable stock against dated commitments.',[
('query, calculateLocked','1. Read usable stored and issued quantities and approved remaining commitments. 2. For reusable resources, check simultaneous load throughout the requested interval, including overdue custody. 3. Return the lowest free quantity, conflicts and any dependency on an expected return. 4. For consumables, subtract all unconsumed reservations from physical stock.'),('balanceLocked','1. Add receipts/increases and subtract consumption, damage/loss and decreases. 2. Account for issue/return changes in custody. 3. Refuse impossible negative balances.')]),
'AllocationComponent':('Handles resource requests and commitments.',[
('request','1. Check requester, active requirement/project, recipient and action key. 2. Check remaining demand. 3. Copy the required interval and save REQUESTED.'),('approve','1. Check the assigned custodian is different from the requester and the request is still pending. 2. Recheck remaining demand and availability for the entire interval. 3. Approve only if the full requested amount fits. 4. Save approver/time/reason.'),('reject','1. Require a pending request and reason. 2. Save REJECTED.'),('cancel','1. Allow a pending or wholly unissued approval. 2. Save CANCELLED and release its commitment.'),('release','1. Check the quantity is still unissued/unconsumed. 2. Increase releasedUnissued by that amount. 3. Preserve earlier supplied history.'),('issueResources, returnResources, consumeResources','1. Fill in the allocation, resource and movement type. 2. Forward to InventoryTransactionComponent.record, which checks and saves the physical change.'),('list, get','Use the common read steps and calculate supplied/outstanding quantities.'),('delete','1. Require an unused REQUESTED allocation with no movements/reminders. 2. Delete; keep decided history.')]),
'InventoryTransactionComponent':('Records actual stock and custody changes without overwriting history.',[
('record','1. Check the resource, action key, user, type and linked allocation. 2. Recalculate balances and check the quantity. 3. Save one movement and its actor/time/reason. 4. Recalculate balances and identify promises affected by damage/loss.'),('reverse','1. Require the latest unreversed event, no later dependent change and an officer\'s reason. 2. Check that undoing it keeps stock/custody/commitments valid. 3. Add a REVERSAL; keep the original.'),('list, get','Use the common read steps.'),('delete','Return Protected history; a correction is a new event.')]),
'ShortageComponent':('Shows remaining demand and the quantity still to source.',[
('projectSummary','1. Read project requirements and approved coverage. 2. Calculate unallocated demand and free quantity. 3. Show each line\'s remaining gap and any at-risk commitment.'),('consolidate','1. Gather the included departments\' requirements and current commitments. 2. For reusable resources, find the largest simultaneous deficit; for consumables, compare combined demand with unreserved stock. 3. Show the periods/projects/quantities behind the result.')]),
'ReportingComponent':('Returns stock, upcoming/overdue custody and departmental demand.',[
('inventory','1. Read resource transactions. 2. Calculate current usable, issued and damaged quantities. 3. Return each resource separately.'),('upcoming','1. Select approved allocations overlapping the requested dates. 2. Return their unissued/outstanding quantities.'),('overdue','1. Find reusable allocations past endAt. 2. Keep only those with outstanding custody. 3. Return the responsible recipient.'),('departmentDemand','1. Select department requirements in the period. 2. Group requested, approved and supplied quantities by resource/unit. 3. Return the totals without mixing units.')]),
'NotificationComponent':('Prepares return reminders and records sending results.',[
('enqueueManual, enqueueEvent','1. Check outstanding reusable custody, due window and recipient. 2. Reuse an existing event key instead of duplicating it. 3. Save QUEUED if a valid phone and consent exist; otherwise SKIPPED.'),('scanReminders','1. Find outstanding returns due within24hours or already overdue. 2. Select the allocation recipient. 3. Create one RETURN_DUE and one OVERDUE event at most per due date.'),('claim, dispatch, dispatchDue, finish','1. Select/claim a due queued reminder. 2. Recheck custody and consent; mark SENDING. 3. Send through TwilioIntegrationComponent outside the stock change. 4. Record acceptance, failure or UNKNOWN outcome and the attempt.'),('cancel','1. Require a queued/skipped reminder that has not been attempted. 2. Set CANCELLED.'),('refresh','1. Require a saved provider SID. 2. Fetch its current provider status. 3. Update the log without sending again.'),('retry','1. Require a confirmed unaccepted failure and fewer than3attempts. 2. Recheck eligibility/consent. 3. Queue a bounded retry; never blindly retry UNKNOWN.'),('list, get','Use the common read steps and restrict contact details.'),('delete','1. Require an unattempted queued/skipped/cancelled log. 2. Delete it; otherwise return Protected history.')]),
'AuditComponent':('Preserves who changed a record and why, including actions that do not move stock.',[
('append','1. Take the actor, target, changes and reason from the successful component action. 2. Remove private contact/credential values. 3. Save the audit with the main change.'),('appendNote','1. Check officer/administrator permission and target. 2. Save a NOTE with the reason.'),('list, get','Use the common read steps.'),('delete','Return Protected history; add a correcting note instead.')]),
'TwilioIntegrationComponent':('Sends SMS and reads a known message status.',[
('sendSms','1. Prepare To, MessagingServiceSid and Body with server credentials. 2. Submit through Retrofit to Twilio. 3. Return the provider SID/status or a sending error.'),('fetchStatus','1. Request the stored SID\'s status. 2. Return it without creating another SMS.')]),
'SchedulingComponent':('Runs reminder checks and delivery automatically.',[
('scan','1. Every60seconds, check upcoming/overdue returns. 2. Call NotificationComponent.scanReminders.'),('dispatch','1. Every5seconds, select due reminders. 2. Send them through the notification component. 3. Log each result without changing stock.'),('runOnce','1. Run the same scan once in the local demonstration. 2. Return counts of created, skipped and existing reminders.')]),
'PermissionComponent and ActorResolver':('Identify the acting user and apply role/record ownership rules.',[
('resolve, require','1. Find the active acting user. 2. Check the requested action against their role, department and resource responsibility. 3. Continue or return No acting user/Not allowed.')]),
'DemoSeedBootstrap and SeedComponent':('Optionally provide a repeatable fictional starting state.',[
('initialize (@PostConstruct)','1. Check that demonstration initialization is enabled. 2. Call the separate SeedComponent to initialize the administrator and optional scenario. 3. Do not seed live phone consent or send SMS.'),('bootstrapAdmin','1. Return an existing demonstration administrator. 2. If absent, create one only on an empty demonstration database. 3. Return its generated ID.'),('seedScenario','1. Create departments/users. 2. Create resources and opening receipts. 3. Add projects, requirements and sample allocations. 4. Skip an already complete matching scenario instead of adding stock again.')])}
for name,(job,rows)in components.items():
    add('### '+name);add(job);add(table(['Method','Ordered steps'],rows))
add('''Movement rules: ISSUE moves reusable units from storage into custody; RETURN makes inspected usable units available again and keeps damaged returns unavailable. LOSS removes the relevant stored/issued quantity. CONSUMPTION is for consumables only; it decreases physical stock and the allocation's remaining reservation together. RECEIPT/ADJUSTMENT_INCREASE add usable storage; DAMAGE/LOSS/ADJUSTMENT_DECREASE remove it. Recount adjustments require an officer.

Issue/consumption must occur within the approved allocation dates and cannot exceed its unissued/unconsumed remainder. Returns and custody loss cannot exceed the outstanding quantity. Storage damage/loss/decreases cannot exceed usable stock in storage. An issue also requires actual stock and cannot take units still held by another project.

Reusable booking intervals are [startAt,endAt), so adjacent bookings do not overlap. Late outstanding returns continue to block availability. Consumables reserve current stock across dates. A requirement can have several partial allocations; approved coverage cannot exceed its demand. Damage/loss is recorded even if it makes an earlier promise at risk, but unsafe new approvals are refused.

Reminders require an optional valid phone and user consent. Only a confirmed unaccepted429failure can retry after60then300seconds, at most3attempts. An uncertain send remains UNKNOWN to avoid duplicate SMS. A sending failure never changes an approved allocation or stock. The demonstration cap is5attempts per local day.

## Entities and repositories

All IDs are positive Long values generated by the database. Every entity has id, createdAt (Instant) and createdBy (AppUser; null only for system initialization/jobs). Mutable entities also have version (nonnegative Long), updatedAt (Instant) and updatedBy (AppUser; null only for system actions). id, createdAt, version and updatedAt do not allow null where present. InventoryTransaction and AuditEvent cannot be edited. Instant is a date/time stored in UTC. A field whose type is another entity links to an existing record of that type.

Common validation: required text cannot be blank; text and quantities stay within the listed limits; endAt must follow startAt; requirement dates must lie within the project. Existing related records cannot be removed while in use. The tables give the remaining fields and their allowed values.
''')
common={'id','createdAt','createdBy','version','updatedAt','updatedBy'}
field_rules={'revision':'Nonnegative system counter; increases after a resource action.','payloadHash':'64-character value calculated from the request to detect changed repeats.','resourceRevisionAfter':'Positive resource counter immediately after this movement.','deltaStored':'Signed change to usable stock in storage; set by the component.','deltaOutstanding':'Signed change to reusable quantity in custody; 0 for consumables.','deltaDamaged':'Signed change to quarantined stock; 0 for consumables.','deltaIssued':'Signed change to the allocation\'s issued or consumed quantity.','deltaResolved':'Signed change to its returned or lost quantity.','deltaGoodReturned':'Signed change to usable returns.','deltaDamagedReturned':'Signed change to damaged returns.','deltaLost':'Signed change to custody losses.','reversalOf':'Existing original transaction in the same resource; at most one reversal.','correctionReference':'Earlier transaction in the same resource explained by this new observation.','beforeJson':'System JSON of values before the change; at most16000characters; excludes private contact/credential values.','afterJson':'System JSON of values after the change; at most16000characters; excludes private contact/credential values.','phoneE164':'Plus followed by8..15digits; at most16characters; required when smsConsent is true.'}
entity_purpose={'Department':'A department responsible for its members, projects and resources.','AppUser':'A member with a role and department, and optional SMS preferences.','Project':'An organization activity with an owner, department and schedule.','Resource':'A reusable or consumable resource managed by the organization.','ResourceRequirement':'One resource needed by one project, with its quantity and dates.','Allocation':'A request and the quantity committed toward a requirement.','InventoryTransaction':'A saved receipt, issue, return, consumption, damage, loss or correction.','NotificationLog':'A return reminder and its sending result.','AuditEvent':'A record of who changed something, what changed and why.'}
repo_purpose={'lockById':'Load this record and protect it while its change is checked and saved.','findByRequestKey':'Find an earlier allocation request with this key.','findByResource_IdAndCommandKey':'Find an earlier stock action with this resource and key.','findByResource_IdOrderByIdAsc':'Read the resource history in order to calculate stock.','findByAllocation_IdOrderByIdAsc':'Read issues, returns and consumption for this allocation.','findByRequirement_Id':'Find allocations used to fulfill this requirement.','findByReversalOf_Id':'Check whether the original transaction has already been reversed.','findOverlappingApproved':'Find approved bookings overlapping the requested dates. Overdue issued resources are checked separately.','findDue':'Find queued reminders whose next sending time has arrived.','findByEventKey':'Check whether this reminder already exists.','findByTargetTypeAndTargetIdOrderByIdAsc':'Show the changes made to one record.'}
def rule(s):
    s=re.sub(r'max(\d+)-day project duration',r'project duration at most \1 days',s)
    s=s.replace('ElementCollection append only;max3','At most3 sending attempts; maintained by the component')
    s=s.replace('NotBlank;','Required text;').replace('NotBlank','Required text').replace('FK RESTRICT;','Existing record;').replace('FK RESTRICT','Existing record').replace('FK','Existing record')
    s=s.replace('Min1 Max1000000','1..1000000').replace('Default','Initially').replace('Version;','Version;')
    s=re.sub(r'\bmax(\d+)',r'at most \1 characters',s)
    s=re.sub(r'\bMin(\d+)',r'at least \1',s)
    s=s.replace('SHA256 char64 server derived','64-character value calculated by the system').replace('server managed','set by the system').replace('server derived','calculated by the system').replace('server assigned','set by the system')
    return s
for name,e in model['entities'].items():
    add('### '+name)
    add(entity_purpose[name])
    add(table(['Field','Java type','Null allowed','Validation or allowed value'],[(f['name'],f['java'],'Yes'if f['nullable']else'No',field_rules.get(f['name'],rule(f['constraint'])))for f in e['fields']if f['name']not in common]))
    repo=name+'Repository'
    add(repo+' stores and retrieves '+name+' records. '+e['delete'])
    for r in model['repositories'][name]:add('Custom method '+r['name']+'('+r['params']+'): '+repo_purpose.get(r['name'],r['purpose']))
add('NotificationAttempt belongs to its NotificationLog: ordinal Integer1..3, attemptedAt Instant, outcome String(max40), errorCode String(max80, nullable) and providerSid String(max64, nullable). It is a small list of sending attempts, not a separate entity.')
text='\n\n'.join(out)+'\n'
text=text.replace('protected state','protected state')
text=re.sub(r'\b(the|all|every|of|by|within|after|before|than|most|per)(\d+)',r'\1 \2',text)
text=re.sub(r'(\d)(seconds|hours|times|days|calls|with|cords|sheets|attempts|characters|failure)\b',r'\1 \2',text)
text=text.replace('after60then300seconds','after 60 then 300 seconds').replace('under version','under version')
assert not any(s in text for s in ('theguidon','ateneomea','## References','## Sources','Template Pack','syllabus','CLO','submission deadline','Canvas'))
(ROOT/'OrgResource-Concise-Submission-Proposal.md').write_text(text,encoding='utf-8')
print(json.dumps(dict(words=len(text.split()),operations=len(ops),entities=len(model['entities']),componentGroups=len(components))))

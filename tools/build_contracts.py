"""Author specification artifacts only. This is not application implementation."""
from pathlib import Path
import json, re, hashlib

ROOT = Path(__file__).resolve().parents[1]
for directory in ('contracts','postman','evidence','qa'): (ROOT/directory).mkdir(exist_ok=True)
def dump(name,obj):
    (ROOT/name).write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
def write(name,text):
    text=re.sub(r'(\|[^\n]*\|)\n\n(?=\|)',r'\1\n',text)
    (ROOT/name).write_text(text.strip()+'\n',encoding='utf-8')
def field(name,typ,nullable,constraint,description):
    return dict(name=name,java=typ,nullable=nullable,constraint=constraint,description=description)
def fields(text):
    out=[]
    for line in text.strip().splitlines():
        name,typ,null,con,desc=line.split('|',4)
        out.append(field(name,typ,null=='Y',con,desc))
    return out
COMMON=fields('''id|Long|N|Generated IDENTITY; positive; immutable|Surrogate primary key
createdAt|Instant|N|UTC millisecond; server assigned|Creation instant
createdBy|AppUser|Y|FK RESTRICT; null only SYSTEM bootstrap/jobs|Creator actor; null never means an unauthenticated public request''')
MUTABLE=fields('''version|Long|N|Version; nonnegative; server managed|Expected version checked on updates
updatedAt|Instant|N|UTC millisecond; server assigned|Last changed instant
updatedBy|AppUser|Y|FK RESTRICT; null only SYSTEM|Last actor''')
E={}
def entity(name,path,component,purpose,why,body,sample,delete,edit=None):
    E[name]=dict(name=name,path=path,component=component,purpose=purpose,why=why,fields=COMMON+([] if name in ('InventoryTransaction','AuditEvent') else MUTABLE)+fields(body),create=sample,delete=delete,edit=edit)
entity('Department','departments','DepartmentComponent','Flat accountable unit inside the one organization.','Needed to consolidate demand and assign responsibility without tenancy or hierarchy.','''code|String|N|3..20 uppercase A-Z0-9_ unique|Permanent human reference
name|String|N|NotBlank; max100|Display name
description|String|Y|max1000|Optional purpose
active|Boolean|N|Default true|New references require active department''',dict(code='LOG',name='Logistics',description='Fictional demonstration department'),'Hard delete only when no user, project or resource references exist. Otherwise409 DEPENDENCY_EXISTS; update active=false only when no active operational dependency.',dict(name='Logistics Team',description='Fictional team',active=True))
entity('AppUser','users','UserComponent','Member identity and access/contact preferences.','Provides accountable requesters, approvers, recipients and actors.','''username|String|N|3..50 lowercase a-z0-9._- unique|Stable local reference; not a password
displayName|String|N|NotBlank max100|Person label; fictional in demo
role|Role|N|ADMIN OFFICER HEAD CUSTODIAN MEMBER|One fixed role
department|Department|Y|FK RESTRICT; required except ADMIN/OFFICER|Home department
active|Boolean|N|Default true|Inactive cannot act or receive new responsibility
phoneE164|String|Y|Pattern plus followed by8..15 digits; max16|Optional normalized contact; self/admin only
smsConsent|Boolean|N|Default false|Operational SMS opt in
consentAt|Instant|Y|Required when consent true; server timestamp|Most recent affirmative consent
consentVersion|String|Y|max30; required when consent true|Notice version such as OP_SMS_V1''',dict(username='demo.custodian',displayName='Demo Custodian',role='CUSTODIAN',departmentId='{{departmentId}}'),'Hard delete only if no operational or actor references anywhere. Otherwise deactivate using update; deny deactivation if assigned unresolved custody or sole active custodian, until reassigned. Never delete last active ADMIN.',dict(displayName='Demo Custodian',role='CUSTODIAN',departmentId='{{departmentId}}',active=True,phoneE164=None,smsConsent=False))
entity('Project','projects','ProjectComponent','Dated departmental activity with internal planning approval.','Requirements need one accountable owner and bounded schedule.','''code|String|N|3..30 uppercase A-Z0-9_- unique|Stable project reference
name|String|N|NotBlank max150|Project name
description|String|Y|max2000|Scope description
department|Department|N|FK RESTRICT; active at create|Accountable unit
owner|AppUser|N|FK RESTRICT; active same department|Project lead
startAt|Instant|N|Strictly less than endAt|Planning start
endAt|Instant|N|After startAt; max90-day project duration|Planning end
status|ProjectStatus|N|DRAFT SUBMITTED APPROVED IN_PROGRESS COMPLETED CANCELLED|Internal lifecycle
statusReason|String|Y|max1000; required for return/cancel/waiver|Latest decision reason; full changes in audit''',dict(code='WELCOME_26',name='Welcome Workshop',description='Fictional scenario',departmentId='{{departmentId}}',ownerId='{{memberId}}',startAt='{{startA}}',endAt='{{endA}}'),'Hard delete only DRAFT with no requirements/allocations. Historical project uses cancel or completed archival; no cascading delete.',dict(name='Welcome Workshop Revised',description='Fictional revised description'))
entity('Resource','resources','ResourceComponent','Pool of interchangeable whole units under organizational control.','Defines the identity, type, unit and accountable custodian shared by all requirements.','''code|String|N|3..30 uppercase A-Z0-9_- unique|Stable resource reference
name|String|N|NotBlank max150|Resource display name
description|String|Y|max2000|Handling notes
type|ResourceType|N|REUSABLE CONSUMABLE; locked after reference|Allocation arithmetic class
category|String|N|NotBlank max50|Simple organization vocabulary
unit|String|N|NotBlank max20; fixed after reference|Whole base unit such as piece sheet roll
storageLocation|String|N|NotBlank max200|Normal store description; one pool location
controlBasis|ControlBasis|N|OWNED AUTHORIZED|Who may manage the resource
authorizationNote|String|Y|max1000; required for AUTHORIZED|Authority description without claiming university permission
department|Department|N|FK RESTRICT active|Responsible department
custodian|AppUser|N|FK RESTRICT active CUSTODIAN|Designated stock/approval operator
revision|Long|N|Nonnegative server counter;increment under resource lock for every pool command|Dependency fence independent of JPA flush timing;not a stock quantity
active|Boolean|N|Default true|Archive rather than erase history''',dict(code='CORD_POOL',name='Extension Cords',description='Interchangeable fictional cords',type='REUSABLE',category='Equipment',unit='piece',storageLocation='Demo cabinet',controlBasis='OWNED',authorizationNote=None,departmentId='{{departmentId}}',custodianId='{{custodianId}}'),'Hard delete only with no requirements, allocations or transactions. Archive requires no pending/active unissued commitment or outstanding custody; keep ledger forever in demo.',dict(name='Extension Cords',description='Checked pool',category='Equipment',storageLocation='Demo cabinet B',departmentId='{{departmentId}}',custodianId='{{custodianId}}',active=True,reason='Cabinet label changed'))
entity('ResourceRequirement','requirements','RequirementComponent','Demand for one resource during one project window.','Separates desired supply from an approved promise and permits partial allocations.','''project|Project|N|FK RESTRICT; active planning project|Demanding activity
resource|Resource|N|FK RESTRICT active|Requested pool
quantity|Long|N|Min1 Max1000000|Total desired whole base units
startAt|Instant|N|Within project; start less than end|Required beginning
endAt|Instant|N|Within project|Required end even for demand-timing of consumables
priority|Priority|N|LOW NORMAL HIGH|Display/sort priority only
notes|String|Y|max2000|Justification
status|RequirementStatus|N|ACTIVE WITHDRAWN|Demand visibility; not fulfillment status''',dict(projectId='{{projectId}}',resourceId='{{resourceId}}',quantity=6,startAt='{{startA}}',endAt='{{endA}}',priority='NORMAL',notes='Fictional planning requirement'),'Hard delete only when no allocation or movement references. Otherwise withdrawal through update with reason, only after all operational obligations are resolved.',dict(quantity=6,startAt='{{startA}}',endAt='{{endA}}',priority='HIGH',notes='Updated need',status='ACTIVE',reason='Priority clarified'))
entity('Allocation','allocations','AllocationComponent','One requested quantity and its immutable approved schedule/recipient.','Allows a requirement to be satisfied in several separately auditable commitments.','''requirement|ResourceRequirement|N|FK RESTRICT ACTIVE|Links demand to pool/project
quantity|Long|N|Min1 Max1000000|Requested and if approved committed quantity
releasedUnissued|Long|N|Default0;0..quantity;cannot reduce issued coverage|Explicitly released portion
startAt|Instant|N|Snapshot requirement.startAt|Committed start
endAt|Instant|N|Snapshot requirement.endAt|Committed end and reusable due time
requestedBy|AppUser|N|FK RESTRICT active at create|Actual actor requester
recipient|AppUser|N|FK RESTRICT active same project department|Fixed custody holder / consumption recipient
decisionStatus|AllocationDecision|N|REQUESTED APPROVED REJECTED CANCELLED|Approval decision separate from ledger progress
decidedBy|AppUser|Y|FK RESTRICT; required on approve/reject|Designated custodian decision actor
decidedAt|Instant|Y|Required on approve/reject|Decision timestamp
decisionReason|String|Y|max1000; required rejection/cancellation|Decision/release reason; full history in audit
requestKey|String|N|UUID canonical36;global unique|Create replay guard
payloadHash|String|N|SHA256 char64 server derived|Detect key reuse with changed input''',dict(requirementId='{{requirementId}}',quantity=6,recipientId='{{memberId}}',requestKey='{{requestKey}}'),'Hard delete only REQUESTED by requester or HEAD/OFFICER, with no movements or notifications; append audit. Approved/rejected/cancelled history retained;409 directs cancel/release.')
entity('InventoryTransaction','inventory-transactions','InventoryTransactionComponent','Immutable physical/custody delta and correction event.','Only authority for physical stock; original observations must remain traceable.','''resource|Resource|N|FK RESTRICT|Affected pool
allocation|Allocation|Y|FK RESTRICT;required ISSUE RETURN CONSUMPTION and custody LOSS|Operational custody/consumption reference
type|TransactionType|N|RECEIPT ISSUE RETURN CONSUMPTION DAMAGE LOSS ADJUSTMENT_INCREASE ADJUSTMENT_DECREASE REVERSAL|Movement meaning
quantity|Long|N|Min1 Max1000000|Magnitude; not signed client delta
inspection|Inspection|Y|USABLE DAMAGED;required RETURN only|Return condition split across separate transactions
reason|String|N|NotBlank max1000|Physical/correction explanation
occurredAt|Instant|N|Server Clock, no client backdating|Observed recording time
deltaStored|Long|N|Derived signed;for consumables same as physical delta|Usable storage delta
deltaOutstanding|Long|N|Derived signed;zero for consumables|Reusable custody delta
deltaDamaged|Long|N|Derived signed;zero for consumables|Quarantine delta
deltaIssued|Long|N|Derived signed for linked allocation|Net issued/consumed counter delta
deltaResolved|Long|N|Derived signed for linked allocation|Reusable returned/lost resolution delta
deltaGoodReturned|Long|N|Derived signed|Usable return count delta
deltaDamagedReturned|Long|N|Derived signed|Damaged return count delta
deltaLost|Long|N|Derived signed|Custody lost count delta
commandKey|String|N|UUID36 unique together with resource|Replay identity
payloadHash|String|N|SHA256 char64 server derived|Canonical command fingerprint
reversalOf|InventoryTransaction|Y|FK RESTRICT unique;non-reversal original only|Exactly one correction of original
correctionReference|InventoryTransaction|Y|FK RESTRICT;informational only|Present correction after old event cannot be reversed
resourceRevisionAfter|Long|N|Positive;captured after resource revision flush|Dependency fence for latest-event reversal''',dict(resourceId='{{resourceId}}',allocationId=None,type='RECEIPT',quantity=10,inspection=None,reason='OPENING_COUNT fictional stock',commandKey='{{commandKey}}',reversalOfId=None,correctionReferenceId=None),'Never physically delete a posted movement. Delete endpoint returns409 IMMUTABLE_HISTORY with reversal route. No permission to clear the ledger through REST.')
entity('NotificationLog','notifications','NotificationComponent','Durable return reminder event/outbox and provider outcome.','Decouples SMS from stock commits and suppresses repeated reminders.','''allocation|Allocation|N|FK RESTRICT reusable APPROVED with outstanding custody|Reminder target
recipient|AppUser|N|FK RESTRICT;allocation recipient|Operational recipient
kind|NotificationKind|N|RETURN_DUE OVERDUE|Only two V1 event types
eventKey|String|N|max160 unique;server derived|kind:allocationId:endAt:recipientId
status|NotificationStatus|N|QUEUED SENDING ACCEPTED DELIVERED UNDELIVERED FAILED UNKNOWN SKIPPED CANCELLED|Dispatch/provider outcome
dueAt|Instant|N|Server derived|First eligible delivery time
nextAttemptAt|Instant|Y|Required QUEUED|Bounded retry time
attemptCount|Integer|N|0..3|Transport attempts;zero when skipped
destinationMasked|String|Y|max30|No full number snapshot
providerSid|String|Y|max64 unique when present|Accepted external message identity
providerStatus|String|Y|max40|Raw safe provider status
lastErrorCode|String|Y|max80|Sanitized category/code;no credentials
lastAttemptAt|Instant|Y|Set when attempt starts|Recovery and diagnostics
cancelReason|String|Y|max1000|Staleness/consent/manual cancellation reason
attempts|List<NotificationAttempt>|N|ElementCollection append only;max3|Each embedded attempt records instant outcome error SID''',dict(allocationId='{{allocationId}}',kind='RETURN_DUE'),'Hard delete only unattempted QUEUED/SKIPPED/CANCELLED rows with attemptCount0, by OFFICER/ADMIN; record AuditEvent. Otherwise409 IMMUTABLE_HISTORY. Cancellation of queued reminder is explicit and prevents sending.')
entity('AuditEvent','audit-events','AuditComponent','Append-only actor/action/change record including non-stock decisions.','Stock history alone cannot explain approvals, edits, cancellations, or deletions.','''action|String|N|max80 fixed allowlist or NOTE|Server command action
targetType|String|N|One of nine entity names;NOTE allows Project/Resource/Department|Historical typed reference
targetId|Long|N|Positive;real target at creation|May survive a permitted target deletion
targetSummary|String|N|max300 server generated|Historical code/name identity
beforeJson|String|Y|TEXT max16000;server redacted|Prior public business values
afterJson|String|Y|TEXT max16000;server redacted|Resulting public business values
reason|String|Y|max1000;required NOTE and corrective actions|Human explanation
requestId|String|N|UUID36 server assigned|Diagnostic request correlation''',dict(targetType='Project',targetId='{{projectId}}',reason='Fictional planning note'),'Never physically delete audit history. Delete endpoint returns409 IMMUTABLE_HISTORY. A correction is a new NOTE referencing the original id in its reason, not editing the original.')

DB={'Long':'BIGINT','String':'VARCHAR','Instant':'DATETIME(3)','Boolean':'BOOLEAN','Integer':'INT'}
ENUMS=dict(Role=['ADMIN','OFFICER','HEAD','CUSTODIAN','MEMBER'],ResourceType=['REUSABLE','CONSUMABLE'],ControlBasis=['OWNED','AUTHORIZED'],ProjectStatus=['DRAFT','SUBMITTED','APPROVED','IN_PROGRESS','COMPLETED','CANCELLED'],RequirementStatus=['ACTIVE','WITHDRAWN'],Priority=['LOW','NORMAL','HIGH'],AllocationDecision=['REQUESTED','APPROVED','REJECTED','CANCELLED'],TransactionType=['RECEIPT','ISSUE','RETURN','CONSUMPTION','DAMAGE','LOSS','ADJUSTMENT_INCREASE','ADJUSTMENT_DECREASE','REVERSAL'],Inspection=['USABLE','DAMAGED'],NotificationKind=['RETURN_DUE','OVERDUE'],NotificationStatus=['QUEUED','SENDING','ACCEPTED','DELIVERED','UNDELIVERED','FAILED','UNKNOWN','SKIPPED','CANCELLED'])
REPOS={n:[] for n in E}
def repo(n,method,params,ret,query,purpose):REPOS[n].append(dict(name=method,params=params,returns=ret,query=query,purpose=purpose))
repo('Department','findByCode','String code','Optional<Department>','Derived code equality','Detect duplicate normalized code.')
repo('AppUser','findByUsername','String username','Optional<AppUser>','Derived username equality','Resolve known username and detect duplicate.')
repo('Project','findByCode','String code','Optional<Project>','Derived code equality','Detect duplicate project reference.')
repo('Resource','findByCode','String code','Optional<Resource>','Derived code equality','Detect duplicate pool reference.')
repo('Resource','lockById','Long id','Optional<Resource>','JPQL select r from Resource r where r.id = :id; @Lock(PESSIMISTIC_WRITE)','Serialize all commands affecting this pool; write transaction required.')
repo('ResourceRequirement','findByProject_IdOrderByIdAsc','Long projectId','List<ResourceRequirement>','Derived project.id predicate sorted id','Load complete project requirement set.')
repo('ResourceRequirement','findByResource_IdAndStatus','Long resourceId, RequirementStatus status','List<ResourceRequirement>','Derived resource.id and status equality','Load active demand for consolidation; project-status filter in component.')
repo('Allocation','findByRequestKey','String requestKey','Optional<Allocation>','Derived unique requestKey equality','Replay original create or reject changed payload.')
repo('Allocation','findByRequirement_Resource_IdAndDecisionStatus','Long resourceId, AllocationDecision status','List<Allocation>','Derived requirement.resource.id and decisionStatus equality','Current approved pool commitments; cannot filter only finite overlap because overdue holds extend.')
repo('Allocation','findByRequirement_Project_Id','Long projectId','List<Allocation>','Derived requirement.project.id equality','Cancel/reschedule/complete entire project safely.')
repo('Allocation','findByRequirement_Id','Long requirementId','List<Allocation>','Derived requirement.id equality','Coverage and dependent-row checks.')
repo('Allocation','findOverlappingApproved','Long resourceId, Instant start, Instant end','List<Allocation>','JPQL select a from Allocation a where a.requirement.resource.id = :resourceId and a.decisionStatus = :approved and a.startAt < :end and a.endAt > :start; bind AllocationDecision.APPROVED as extra enum parameter approved','Finite scheduled overlap inspection only; merge overdue custody found through approved pool scan, never use alone for safe approval.')
REPOS['Allocation'][-1]['params'] += ', AllocationDecision approved'
repo('InventoryTransaction','findByResource_IdOrderByIdAsc','Long resourceId','List<InventoryTransaction>','Derived resource.id equality sorted id','Reconstruct balances/effects in stable order under resource lock.')
repo('InventoryTransaction','findByResource_IdAndCommandKey','Long resourceId, String commandKey','Optional<InventoryTransaction>','Derived equality on both fields','Stock command replay guard.')
repo('InventoryTransaction','findByReversalOf_Id','Long originalId','Optional<InventoryTransaction>','Derived reversalOf.id equality','Reject second reversal of same original.')
repo('InventoryTransaction','findByAllocation_IdOrderByIdAsc','Long allocationId','List<InventoryTransaction>','Derived allocation.id equality sorted id','Derive issued/resolved counters and custody history.')
repo('NotificationLog','findByEventKey','String eventKey','Optional<NotificationLog>','Derived eventKey equality','Deduplicate reminder generation across restarts/scans.')
repo('NotificationLog','findDue','NotificationStatus status, Instant now, Pageable page','List<NotificationLog>','JPQL select n from NotificationLog n where n.status = :status and n.nextAttemptAt <= :now order by n.id','Fetch bounded QUEUED candidates; lock each before claim.')
repo('NotificationLog','lockById','Long id','Optional<NotificationLog>','JPQL select n from NotificationLog n where n.id = :id; @Lock(PESSIMISTIC_WRITE)','Claim reminder or serialize completion/cancellation.')
repo('AuditEvent','findByTargetTypeAndTargetIdOrderByIdAsc','String targetType, Long targetId','List<AuditEvent>','Derived targetType and targetId equality sorted id','Audit history for one referenced record.')

def dbtype(f):
    if f['java'] in E:return 'BIGINT FK'
    if f['java']=='List<NotificationAttempt>':return 'separate collection table'
    if f['java'] not in DB:return 'VARCHAR(40)'
    if f['java']=='String':
        m=re.search(r'(?:max|\.\.)(\d+)',f['constraint'])
        if 'TEXT' in f['constraint']:return 'TEXT'
        if 'SHA256' in f['constraint']:return 'CHAR(64)'
        if 'UUID' in f['constraint']:return 'CHAR(36)'
        return 'VARCHAR('+ (m.group(1) if m else ('20' if f['name']=='code' else '160')) +')'
    return DB[f['java']]

model=dict(version='1.0',entities=E,enums=ENUMS,repositories=REPOS)
dump('contracts/domain-model.json',model)
out=['# Complete Database Specification','## Shared mapping and constraints',
'Nine entity types are selected. Organization name, categories, storage labels and units are configuration or bounded strings, not artificial entities. Every primary key uses javax.persistence.Entity, Id and GeneratedValue(strategy=IDENTITY). Mutable entities use Version. Timestamps are server assigned UTC Instant values mapped to DATETIME(3). Foreign keys use ManyToOne(fetch=LAZY), JoinColumn with the nullability shown and no cascading delete. Explicitly annotate EnumType.STRING. A mapped superclass may carry common fields but is not a tenth entity.',
'All inverse one-to-many navigation is optional and if implemented uses mappedBy, LAZY, no REMOVE cascade and orphanRemoval=false. No ManyToMany or OneToOne entity relationship is needed except the unique nullable reversalOf foreign key (map ManyToOne plus database unique constraint). Do not serialize these graphs. createdBy and updatedBy point to AppUser and may be null only for the audited SYSTEM bootstrap or scheduler. An ordinary request always has a real resolved actor. AppUser may reference itself as creator during bootstrap, or SYSTEM/null with an AuditEvent.',
'All repositories extend JpaRepository<Entity,Long> and JpaSpecificationExecutor<Entity>. Used inherited methods are findById(Long), findAll(Specification,Pageable), save(Entity), flush(), delete(Entity), existsById(Long) and count(Specification). A component invokes delete only under the entity-specific safe-deletion predicate. Specifications build parameterized equality/range/status predicates against actual mapped field paths, never concatenate user JPQL. Lists use a whitelist of fields described in05. Unmentioned inherited methods are not exposed as generic REST operations.',
'Uniqueness: Department.code, AppUser.username, Project.code, Resource.code, ResourceRequirement(project_id,resource_id), Allocation.requestKey, InventoryTransaction(resource_id,commandKey), InventoryTransaction.reversalOf, NotificationLog.eventKey and nullable NotificationLog.providerSid. Codes are trimmed and uppercased Locale.ROOT; usernames trimmed and lowercased Locale.ROOT; unique DB collation must agree. Resource.code DB width is30, Project.code30, Department.code20, username50. Specification text limits override inferred sample values. Restrict all foreign-key deletion; no cascade can bypass history.',
'Indexes: every FK; Allocation(requirement_id,decision_status,start_at,end_at); ResourceRequirement(resource_id,status); InventoryTransaction(resource_id,id) and (allocation_id,id); NotificationLog(status,next_attempt_at,id); AuditEvent(target_type,target_id,id). Cross-row sums and role/active-reference constraints are component rules under locks, not Bean Validation annotations. Schema tests must confirm unique constraints and actual DB types. No negative quantity or illegal state is made safe merely by JPA annotations.']
for name,e in E.items():
    out += [f'## {name}',f"Purpose: {e['purpose']} Why necessary: {e['why']}",'| Field | Java type | Database type | Nullable | Constraints | Meaning |','|---|---|---|---|---|---|']
    for f in e['fields']:out.append('| '+' | '.join([f['name'],f['java'],dbtype(f),'Yes' if f['nullable'] else 'No',f['constraint'],f['description']])+' |')
    out += ['Validation: NotNull for required reference/wrapper/enum values, NotBlank plus Size for required strings, Size for optional strings, Min/Max for quantity, Pattern for code/phone/UUID shapes. Optional strings normalize blank to null. Enum parsing and unknown JSON properties fail400. Entity validation mirrors mandatory field limits; request DTO and component cross-field checks remain required. Cross-field and lifecycle constraints are specified in02 and its owning component in06.',
    'Relationships: '+('; '.join(f"{f['name']} -> {f['java']} ({'optional' if f['nullable'] else 'required'} FK, LAZY, RESTRICT)" for f in e['fields'] if f['java'] in E) or 'No entity references.') ,
    f"Repository: {name}Repository. Entity type {name}; ID type Long. Standard inherited methods and Specification queries above apply."]
    for r in REPOS[name]:out += [f"Custom method: {r['returns']} {r['name']}({r['params']}). Purpose: {r['purpose']} Query: {r['query']}."]
    out += [f"Lifecycle and deletion: {e['delete']}"]
out += ['## NotificationAttempt embeddable',
'NotificationAttempt is an Embeddable, not an entity. It contains ordinal Integer1..3, attemptedAt Instant nonnull, outcome String max40 nonnull, errorCode String max80 nullable, providerSid String max64 nullable. ElementCollection join table notification_attempt(notification_log_id FK, ordinal, attempted_at, outcome, error_code, provider_sid), unique(log_id,ordinal), ordered ordinal. Log attemptCount must equal collection size. Append within the same short notification-result transaction; no phone, body, secret or raw exception is stored. No independent CRUD endpoint exists because this is a value-owned part of NotificationLog, not a separate entity.',
'## Enum catalogue']
out += [f"{n}: {', '.join(v)}." for n,v in ENUMS.items()]
out += ['## Field and DTO distinctions','Entity properties representing relationships become xxxId Long in request/response DTOs; direct actor fields are never accepted. Primitive booleans and counters use wrapper validation where null can otherwise be silently converted. Responses include derived stock/coverage fields defined in05 but those are not persisted JPA properties. No derived Spring Data query may refer to them. AuditEvent beforeJson/afterJson uses a fixed redacted DTO serialization, not arbitrary entity serialization; no PII/credentials/consent phone values in snapshots. InventoryTransaction signed delta fields are server generated. Their zero values are explicit and never null. NotificationLog full destination is read fresh from recipient before send and is not stored in attempts.',
'## Repository query validation','Compile/start every repository under the pinned Spring Data version. The explicitly documented findOverlappingApproved query has four arguments including approved; bind each with Param. It is useful for reporting but insufficient for overdue safety. All List-derived methods are intentionally bounded by the stated demo size and loaded in batches per pool; paginated public queries use Specifications. If an additional custom query is introduced by implementation it must be added to this catalogue and the contract references before review.']
write('04-DATABASE-SPECIFICATION.md','\n\n'.join(out))

# One operation registry drives API catalogue, method catalogue and Postman coverage.
OPS=[]
ERRORS={
 'BAD_REQUEST':(400,'The request has invalid or unknown fields.'),
 'VALIDATION_FAILED':(400,'One or more fields failed validation.'),
 'UNAUTHENTICATED':(401,'An active acting user is required.'),
 'FORBIDDEN':(403,'The acting user cannot perform this operation.'),
 'NOT_FOUND':(404,'The referenced record does not exist.'),
 'DUPLICATE_RECORD':(409,'A record with this unique reference already exists.'),
 'DEPENDENCY_EXISTS':(409,'Related records prevent deletion; use the documented lifecycle action.'),
 'IMMUTABLE_HISTORY':(409,'Posted history cannot be deleted; use a new correction or reversal.'),
 'VERSION_CONFLICT':(409,'The record changed; retrieve its current version before deciding again.'),
 'INVALID_STATE':(409,'The current state does not permit this action.'),
 'RESOURCE_OVERALLOCATION':(409,'The requested quantity exceeds capacity for the selected period.'),
 'REQUIREMENT_COVERAGE_EXCEEDED':(409,'Approved coverage would exceed the requirement quantity.'),
 'INSUFFICIENT_PHYSICAL_STOCK':(409,'There is insufficient usable stock in storage for this issue.'),
 'RESOURCE_TYPE_MISMATCH':(409,'The resource type does not support this movement.'),
 'SCHEDULE_LOCKED':(409,'A recorded issue or consumption prevents schedule replacement.'),
 'IDEMPOTENCY_KEY_REUSED':(409,'This command key was used with different input.'),
 'REVERSAL_DEPENDENCY_CONFLICT':(409,'Later activity prevents safe reversal of this event.'),
 'RETRY_REQUIRED':(409,'The command rolled back due to concurrent activity; retrieve state and retry safely.'),
 'NOTIFICATION_NOT_ELIGIBLE':(409,'This reminder is not currently due or has no outstanding custody.'),
 'PROVIDER_OUTCOME_UNKNOWN':(409,'The previous send outcome is unknown; reconcile before resending.'),
 'INTERNAL_ERROR':(500,'The operation could not be completed. Refer to the request ID.')}

def op(id,domain,http,path,method,body,view,purpose,permission,steps,repos,errors=(),tx='WRITE',status=200,query=None):
    OPS.append(dict(id=id,domain=domain,http=http,path='/api/'+path,controller=domain+'Controller',controllerMethod=method,component=COMPONENTS.get(domain,domain+'Component'),method=method,body=body,view=view,purpose=purpose,permission=permission,steps=steps,repositories=repos,errors=list(dict.fromkeys(['BAD_REQUEST','VALIDATION_FAILED','UNAUTHENTICATED','FORBIDDEN','INTERNAL_ERROR']+(['NOT_FOUND'] if '{id}' in path else [])+list(errors))),transaction=tx,success=status,query=query or {}))
COMPONENTS={e['name'].replace('AppUser','User').replace('ResourceRequirement','Requirement').replace('InventoryTransaction','InventoryTransaction').replace('NotificationLog','Notification').replace('AuditEvent','Audit'):e['component'] for e in E.values()}
COMPONENTS.update(Planning='AvailabilityComponent',Shortage='ShortageComponent',Report='ReportingComponent',Scheduling='SchedulingComponent')
VIEW={}
def val(f):
    if f['nullable']:return None
    if f['java'] in E or f['java']=='Long':return 1
    if f['java']=='Instant':return '2026-11-03T02:00:00Z'
    if f['java']=='Boolean':return True
    if f['java']=='Integer':return 0
    if f['java'] in ENUMS:return ENUMS[f['java']][0]
    if f['java'].startswith('List'):return []
    return f['name']+' example'
for name,e in E.items():
    v={}
    for f in e['fields']:
        if f['name'] in ('payloadHash',):continue
        key=f['name']+'Id' if f['java'] in E else f['name']
        v[key]=val(f)
    for k,x in e['create'].items():
        if k in v:v[k]=1 if k.endswith('Id') and x is not None else (x if not isinstance(x,str) or not x.startswith('{{') else ('2026-11-10T01:00:00Z' if 'start' in x else '2026-11-10T04:00:00Z' if 'end' in x else '00000000-0000-4000-8000-000000000001'))
    if name=='Project':v['status']='DRAFT'
    if name=='ResourceRequirement':v.update(status='ACTIVE',coveredQuantity=0,suppliedQuantity=0,outstandingQuantity=0,unallocatedQuantity=6)
    if name=='Allocation':v.update(decisionStatus='REQUESTED',releasedUnissued=0,issuedQuantity=0,resolvedQuantity=0,outstandingQuantity=0,unissuedQuantity=0,fulfillment='NONE',overdue=False)
    if name=='Resource':v.update(physicalInStore=10,outstanding=0,damagedInStore=0,usableOwned=10,reservedConsumable=0,availableConsumable=None)
    if name=='AppUser':v.update(smsConsent=False,phoneE164=None,consentAt=None,consentVersion=None)
    if name=='InventoryTransaction':v.update(type='RECEIPT',quantity=10,deltaStored=10,deltaOutstanding=0,deltaDamaged=0,deltaIssued=0,deltaResolved=0,deltaGoodReturned=0,deltaDamagedReturned=0,deltaLost=0,resourceRevisionAfter=1,replay=False,impactedAllocationIds=[])
    if name=='NotificationLog':v.update(status='SKIPPED',kind='RETURN_DUE',eventKey='RETURN_DUE:1:2026-11-10T04:00:00Z:1',attemptCount=0,attempts=[],lastErrorCode='NO_CONSENT')
    if name=='AuditEvent':v.update(action='NOTE',targetType='Project',targetId=1,targetSummary='WELCOME_26',reason='Fictional planning note',requestId='00000000-0000-4000-8000-000000000002')
    VIEW[name+'View']=v

PERM_CREATE=dict(Department='ADMIN',AppUser='ADMIN',Project='MEMBER owner or HEAD in own department',Resource='assigned CUSTODIAN or OFFICER assigning an active custodian',ResourceRequirement='project owner or HEAD in own department',Allocation='project owner or HEAD in own department; recipient active in that department',InventoryTransaction='assigned CUSTODIAN; OFFICER for recount/reversal',NotificationLog='assigned CUSTODIAN or OFFICER; server recomputes due eligibility',AuditEvent='OFFICER/ADMIN; NOTE only through public create')
FILTERS=dict(Department=['active'],AppUser=['departmentId','role','active'],Project=['departmentId','ownerId','status','from','to'],Resource=['departmentId','custodianId','type','active','category','q'],ResourceRequirement=['projectId','resourceId','status'],Allocation=['projectId','resourceId','recipientId','decisionStatus'],InventoryTransaction=['resourceId','allocationId','type','from','to'],NotificationLog=['allocationId','recipientId','status','kind'],AuditEvent=['targetType','targetId','from','to'])
CREATE_STEPS={
'Department':['Normalize code/name; validate unique code, ADMIN and nonblank fields.','Persist active department and append CREATE audit; no stock or hierarchy exists.'],
'AppUser':['Validate ADMIN, unique normalized username and allowed role; check active department unless organization-wide role.','Ignore no security fields: reject actor/password/consent timestamps; initial SMS consent false unless a separate self consent update is made after creation.','Save user and append redacted CREATE audit without phone.'],
'Project':['Resolve active department and owner, require owner in department and authorized actor.','Validate offset schedule, duration and unique code; force DRAFT, no client state override.','Save and audit the planning record.'],
'Resource':['Check resource control basis and authorization note, active department and active CUSTODIAN.','Create metadata with zero ledger stock; reject supplied quantity, writable condition totals or stock deltas.','Save and audit; actual opening stock requires RECEIPT.'],
'ResourceRequirement':['Read project/resource IDs, lock resource then re-read project; validate owner/head and DRAFT/SUBMITTED project.','Require active resource, one project-resource pair, quantity cap and interval within project.','Persist ACTIVE demand; if project was SUBMITTED return it to DRAFT and audit both changes.'],
'Allocation':['Canonicalize requestKey payload; resolve requirement/project/resource and lock resource then requirement.','Check identical-key replay first; different hash409. Verify active demand and APPROVED/IN_PROGRESS project, authorized requester and same-department recipient.','Require quantity <= unallocated requirement demand; REQUESTED allocations do not reserve stock or count as approved coverage.','Copy requirement interval, fix requester/recipient, set REQUESTED, save and audit; do not autoapprove.'],
'InventoryTransaction':['Resolve pool and optional allocation; take resource lock, canonicalize commandKey/hash and return identical replay before effect application.','Dispatch the declared type through the exact typed movement rules in02; verify operator and required/forbidden references and inspection.','Rebuild ledger balances and allocation counters; check nonnegative physical/custody balances, eligible interval/state and type-specific reservation rules.','Derive signed effect vector, increment resource revision, flush and capture resourceRevisionAfter; insert immutable transaction and append audit in the same transaction.','Return derived balances and impacted commitment IDs; observed damage/loss may create a reported future planning deficit.'],
'NotificationLog':['Resolve allocation and recipient; require authorized actor, reusable outstanding custody and requested kind currently in reminder window.','Compute unique eventKey from kind/allocation/endAt/recipient. Existing key returns existing view200, never a second event.','Read current consent/contact; create QUEUED with nextAttemptAt now or SKIPPED with reason, append audit, never call Twilio here.'],
'AuditEvent':['Check OFFICER/ADMIN and restrict public action to NOTE.','Resolve allowlisted target, generate targetSummary/requestId and reject supplied before/after/actor/time fields.','Persist immutable note with reason; do not recursively audit the AuditEvent insertion.']}
for ix,(name,e) in enumerate(E.items(),1):
    domain={'AppUser':'User','ResourceRequirement':'Requirement','NotificationLog':'Notification','AuditEvent':'Audit'}.get(name,name)
    p=e['path']; rep=name+'Repository'; prefix=f'{ix:02d}'
    cm='record' if name=='InventoryTransaction' else 'enqueueManual' if name=='NotificationLog' else 'appendNote' if name=='AuditEvent' else 'request' if name=='Allocation' else 'create'
    errors=['DUPLICATE_RECORD','INVALID_STATE','RETRY_REQUIRED']
    if name in ('InventoryTransaction','Allocation'):errors+=['IDEMPOTENCY_KEY_REUSED','RESOURCE_TYPE_MISMATCH','RESOURCE_OVERALLOCATION','REQUIREMENT_COVERAGE_EXCEEDED','INSUFFICIENT_PHYSICAL_STOCK','REVERSAL_DEPENDENCY_CONFLICT']
    if name=='NotificationLog':errors+=['NOTIFICATION_NOT_ELIGIBLE']
    calls=[rep+'.save',rep+'.findById']+[rep+'.'+r['name'] for r in REPOS[name]]
    if name in ('ResourceRequirement','Allocation','InventoryTransaction'):calls += ['ResourceRepository.lockById','InventoryTransactionRepository.findByResource_IdOrderByIdAsc','AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus']
    if name!='AuditEvent':calls+=['AuditEventRepository.save']
    op(prefix+'C',domain,'POST',p,cm,e['create'],name+'View',e['purpose'],PERM_CREATE[name],CREATE_STEPS[name],calls,errors,status=201)
    op(prefix+'L',domain,'GET',p,'list',None,'Page<'+name+'View>','List visible '+p+' with bounded filters.','authorized organization read; contact/audit/notification scope rules apply',['Validate pagination and whitelist filter names/types; apply visibility restrictions in the query.','Run parameterized Specification query with stable id ascending sort; map within read transaction and redact contacts.'],[rep+'.findAll'],tx='READ',query={**{k:'optional exact/range filter' for k in FILTERS[name]},'page':'integer>=0 default0','size':'1..100 default20'})
    op(prefix+'R',domain,'GET',p+'/{id}','get',None,name+'View','Retrieve one '+name+' with its derived public fields.','same read scope as list',['Validate positive id and active actor; retrieve or404.','Check object visibility, load required relationships/counters, map a detached DTO and redact sensitive fields.'],[rep+'.findById'],tx='READ')
    delerr=['DEPENDENCY_EXISTS','INVALID_STATE','VERSION_CONFLICT','RETRY_REQUIRED']
    if name in ('InventoryTransaction','AuditEvent'):delerr+=['IMMUTABLE_HISTORY']
    op(prefix+'D',domain,'POST',p+'/{id}/delete','delete',dict(expectedVersion=0,reason='Remove unused fictional setup record'), 'NoContent',e['delete'], 'ADMIN for setup; owner/HEAD for unused drafts; OFFICER for notification removal; immutable history never deletable', ['Resolve target and authorize; require expectedVersion for versioned rows.','Apply the exact entity deletion predicate in04; foreign-key references are checked with Specifications and constrained by database RESTRICT.','If allowed append a deletion audit containing original code and id, then physically delete; otherwise409 with allowed alternative.','For InventoryTransaction and AuditEvent always return409 IMMUTABLE_HISTORY without mutating data; no pretend successful deletion.'],[rep+'.findById',rep+'.delete']+(['AuditEventRepository.save'] if name!='AuditEvent' else []),delerr,status=204)
    if e['edit']:
        body={'expectedVersion':0,**e['edit']}
        steps=['Retrieve target, verify object-specific editor, expectedVersion and permitted lifecycle.','Validate only the explicit mutable DTO fields; existing code/type/unit/owner are not implicitly replaced.','Check active relationships and referential/deactivation constraints in02/04; requirement changes cancel pending requests and invalidate submitted planning approval.','Apply metadata change and append before/after audit atomically; return updated version.']
        if name=='Resource':steps.insert(1,'Acquire resource lock; custodian reassignment is OFFICER only with reason, and cannot rewrite existing allocation recipients.')
        if name=='AppUser':steps.insert(2,'Only self or ADMIN may change contact; only self can grant consent, notice version OP_SMS_V1 and consentAt are server recorded. Role/department/active edits require ADMIN; never trust a role change from the ordinary self-contact DTO. Revocation clears consent flag/metadata and cancels queued sends at dispatch recheck.')
        op(prefix+'U',domain,'POST',p+'/{id}/update','update',body,name+'View','Versioned update of permitted '+name+' fields.','ADMIN setup/user role; self contact; owner/HEAD project/requirement; assigned CUSTODIAN resource metadata',steps,[rep+'.findById',rep+'.save','AuditEventRepository.save']+(['ResourceRepository.lockById'] if name in ('Resource','ResourceRequirement') else []),['VERSION_CONFLICT','DUPLICATE_RECORD','DEPENDENCY_EXISTS','INVALID_STATE','RETRY_REQUIRED'])

def action(id,domain,path,method,body,view,purpose,permission,steps,repos,errors=(),tx='WRITE',status=200):
    op(id,domain,'POST',path,method,body,view,purpose,permission,steps,repos,errors,tx,status)
PS=['ProjectRepository.findById','ProjectRepository.save','ResourceRequirementRepository.findByProject_IdOrderByIdAsc','AllocationRepository.findByRequirement_Project_Id','ResourceRepository.lockById','AuditEventRepository.save']
for id,suffix,target,permission,extra in [
('P01','submit','SUBMITTED','owner/HEAD',['Require DRAFT, at least one active valid requirement and all schedules within project.']),
('P02','approve','APPROVED','OFFICER',['Require SUBMITTED; internal planning approval does not approve university funding or allocate inventory.']),
('P03','return-draft','DRAFT','OFFICER',['Require SUBMITTED, reason, and no approved allocations; draft return is an internal decision.']),
('P04','start','IN_PROGRESS','owner/HEAD or OFFICER',['Require APPROVED and startAt <= now < endAt.']),
('P05','complete','COMPLETED','OFFICER',['Require IN_PROGRESS or APPROVED; no REQUESTED allocation, no unissued approved remainder or outstanding custody.','For unfulfilled demand require waiveUnfulfilled=true plus a reason; write waived quantities in audit, not stock.']),
('P06','cancel','CANCELLED','owner/HEAD for DRAFT/SUBMITTED; OFFICER for APPROVED/IN_PROGRESS',['Require not COMPLETED/CANCELLED and reason; lock complete affected resource set in ascending order.','Set REQUESTED allocations CANCELLED; approved with no supply become CANCELLED, partially supplied approvals release their remaining R. Preserve reusable custody and allow return/loss afterward.'])]:
    b=dict(expectedVersion=0,reason='Fictional internal planning decision')
    if suffix=='complete':b['waiveUnfulfilled']=False
    action(id,'Project','projects/{id}/'+suffix,suffix.replace('-',' ').title().replace(' ','')[0].lower()+suffix.replace('-',' ').title().replace(' ','')[1:],b,'ProjectView','Move internal project to '+target+'.',permission,['Resolve actor/project and relevant resource set; acquire locks in prescribed order and re-read version/state.']+extra+['Set target state, save project and related release decisions, increment affected resource revisions, append audits; commit all changes together.'],PS,['INVALID_STATE','VERSION_CONFLICT','RESOURCE_OVERALLOCATION','RETRY_REQUIRED'])
action('P07','Project','projects/{id}/reschedule','reschedule',dict(expectedVersion=0,startAt='{{startA}}',endAt='{{endA}}',requirements=[dict(requirementId='{{requirementId}}',startAt='{{startA}}',endAt='{{endA}}')],reason='Fictional schedule change'),'ProjectView','Atomically replace full unissued project schedule.','OFFICER',['Read affected requirements/resources; sort IDs and lock all parents before rows. Re-read complete graph and versions.','Require no issue/consumption ever recorded (including reversed events); reject incomplete/extra requirement-window IDs.','Validate new project/requirement intervals and construct proposed snapshots for every requested/approved allocation.','Exclude exactly the old affected commitments, then sweep all proposed replacements together with all unaffected and overdue holds; reject any deficit.','Save all schedules and audit old/new windows, bump resource revisions; entire command rolls back on any conflict.'],PS+['AllocationRepository.save','InventoryTransactionRepository.findByResource_IdOrderByIdAsc'],['SCHEDULE_LOCKED','VERSION_CONFLICT','RESOURCE_OVERALLOCATION','RETRY_REQUIRED'])
AS=['AllocationRepository.findById','AllocationRepository.save','AllocationRepository.findByRequirement_Id','AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus','ResourceRepository.lockById','InventoryTransactionRepository.findByResource_IdOrderByIdAsc','AuditEventRepository.save']
action('A01','Allocation','allocations/{id}/approve','approve',dict(expectedVersion=0,reason='Custodian checked capacity'),'AllocationView','Commit requested quantity without moving stock.','assigned CUSTODIAN different from requester',['Resolve pool; acquire resource then requirement/allocation locks and re-read actor, expectedVersion and REQUESTED state.','Verify active requirement/resource and approved/in-progress project; requester/recipient references remain active.','Sum existing approved effective quantities for this requirement; reject if adding quantity exceeds demand.','Rebuild balances and current pool commitments including overdue outstanding. Sweep reusable interval or compute global consumable ATP.','Require the whole requested quantity fits; no silent partial approval. If not, keep REQUESTED and return409 with available/competing IDs.','Set APPROVED, decidedBy/At/reason; increment resource revision and audit. No SMS is sent for every approval.'],AS,['VERSION_CONFLICT','INVALID_STATE','RESOURCE_OVERALLOCATION','REQUIREMENT_COVERAGE_EXCEEDED','RETRY_REQUIRED'])
action('A02','Allocation','allocations/{id}/reject','reject',dict(expectedVersion=0,reason='Request needs a revised quantity'),'AllocationView','Reject a pending request without deleting history.','assigned CUSTODIAN different from requester',['Lock resource and re-read request; verify actor/version and REQUESTED state.','Require reason; set REJECTED with decidedBy/At and audit. It held no quantity to release.'],AS,['VERSION_CONFLICT','INVALID_STATE','RETRY_REQUIRED'])
action('A03','Allocation','allocations/{id}/cancel','cancel',dict(expectedVersion=0,reason='Project no longer needs this pending commitment'),'AllocationView','Cancel pending or entirely unissued approved allocation.','requester/own HEAD, assigned CUSTODIAN or OFFICER',['Lock pool and re-read version/state.','Allow REQUESTED or APPROVED only; approved cancellation requires issued/consumed net0 and no outstanding.','Set CANCELLED and reason, releasing effective approved commitment; bump resource revision and audit. Rejected and resolved history is not cancelled.'],AS,['VERSION_CONFLICT','INVALID_STATE','RETRY_REQUIRED'])
action('A04','Allocation','allocations/{id}/release','release',dict(expectedVersion=0,quantity=1,reason='Unused part no longer required'),'AllocationView','Release some or all unissued approved remainder.','requester/own HEAD, assigned CUSTODIAN or OFFICER',['Lock pool and re-read APPROVED allocation/version and ledger counters.','Require positive quantity <= R; never release issued or consumed coverage.','Increase releasedUnissued, retain approved historical supply, bump version and audit exact released amount and reason.'],AS,['VERSION_CONFLICT','INVALID_STATE','RETRY_REQUIRED'])
for id,suffix,kind,extra,inspection in [
('A05','issue','ISSUE',['Require reusable and project APPROVED/IN_PROGRESS; startAt <= now < endAt; q <= unissued R.','Require physical S >= q plus other active-now unissued commitments; report conflicts if not.'],None),
('A06','return','RETURN',['Require reusable, outstanding >= q and inspection USABLE or DAMAGED; allow returns after project cancellation/end.','Apply O-to-S for usable or O-to-B for damaged; update resolution counters and include impacted commitments for damaged stock.'],'USABLE'),
('A07','consume','CONSUMPTION',['Require consumable, project APPROVED/IN_PROGRESS, within its requirement interval and q <= unconsumed R.','Require physical P >= q plus all other remaining consumable commitments. Reduce P and own reservation equally; no issue-then-consume duplication.'],None)]:
    b=dict(quantity=1,reason='Fictional '+suffix+' observation',commandKey='{{commandKey}}')
    if inspection:b['inspection']=inspection
    action(id,'Allocation','allocations/{id}/'+suffix,suffix+'Resources' if suffix!='return' else 'returnResources',b,'InventoryTransactionView','Record '+kind+' against this allocation.','assigned CUSTODIAN',['Resolve allocation/pool; call InventoryTransactionComponent.record with server-filled resource/allocation/type fields, preserving the commandKey.','Acquire resource lock and perform identical-key replay check.']+extra+['Derive immutable signed movement, recheck balances/counters, save with new resource revision and append audit in one transaction.'],AS+['InventoryTransactionRepository.save','InventoryTransactionRepository.findByResource_IdAndCommandKey'],['INVALID_STATE','RESOURCE_TYPE_MISMATCH','INSUFFICIENT_PHYSICAL_STOCK','RESOURCE_OVERALLOCATION','IDEMPOTENCY_KEY_REUSED','RETRY_REQUIRED'],status=201)
action('T01','InventoryTransaction','inventory-transactions/{id}/reverse','reverse',dict(reason='Correct latest erroneous fictional event',commandKey='{{commandKey}}'),'InventoryTransactionView','Append constrained reversal of a latest event.','OFFICER',['Resolve original and lock its resource; verify original is not REVERSAL and no existing reversal.','Check commandKey replay; require original.resourceRevisionAfter equals current resource.revision and no later ledger event; otherwise409 dependency conflict.','Negate all original signed deltas and allocation counter effects; recheck balances, coverage, lifecycle and commitment capacity.','Insert REVERSAL linked to original, increment resource revision/capture it after flush, append correction audit; never modify original.'],['InventoryTransactionRepository.findById','InventoryTransactionRepository.findByReversalOf_Id','InventoryTransactionRepository.findByResource_IdAndCommandKey','InventoryTransactionRepository.findByResource_IdOrderByIdAsc','InventoryTransactionRepository.save','ResourceRepository.lockById','AuditEventRepository.save'],['REVERSAL_DEPENDENCY_CONFLICT','IDEMPOTENCY_KEY_REUSED','RESOURCE_OVERALLOCATION','INVALID_STATE','RETRY_REQUIRED'],status=201)
NS=['NotificationLogRepository.lockById','NotificationLogRepository.save','AuditEventRepository.save']
action('N01','Notification','notifications/{id}/cancel','cancel',dict(expectedVersion=0,reason='Reminder no longer needed'),'NotificationLogView','Cancel a reminder before any send begins.','recipient for own, assigned CUSTODIAN, OFFICER or ADMIN',['Lock notification and re-read version; only QUEUED/SKIPPED with attemptCount0 can cancel.','Set CANCELLED and reason, clear nextAttemptAt, audit; SENDING/accepted/unknown cannot be recalled.'],NS,['VERSION_CONFLICT','INVALID_STATE','RETRY_REQUIRED'])
action('N02','Notification','notifications/{id}/dispatch','dispatch',{},'NotificationLogView','Run one due outbox record through the same dispatcher as scheduler.','OFFICER or ADMIN; restricted live allowlist',['Call claim(id,asOf) in a short transaction; recheck QUEUED,due,consent,recipient and outstanding custody.','After committing SENDING and an attempt marker, perform Retrofit send outside any DB transaction.','Call finish(id,result) in a new transaction; bounded retry only on known not-accepted outcome, unknown outcomes never auto-resend.'],NS+['AppUserRepository.findById','AllocationRepository.findById','InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc'],['INVALID_STATE','PROVIDER_OUTCOME_UNKNOWN','RETRY_REQUIRED'],tx='SPLIT')
action('N03','Notification','notifications/{id}/refresh','refresh',{},'NotificationLogView','Fetch provider status for a known SID; no new message.','recipient for own, OFFICER or ADMIN',['Read visible log; require LIVE profile and stored providerSid, or FAKE fixture SID in fake mode.','Fetch exact SID through TwilioIntegrationComponent.fetchStatus outside DB transaction; require SID matches.','Lock log and persist mapped status/error in a fresh transaction; never regress terminal delivery or enqueue a new send.'],NS+['NotificationLogRepository.findById'],['INVALID_STATE','RETRY_REQUIRED'],tx='SPLIT')
action('N04','Notification','notifications/{id}/retry','retry',dict(expectedVersion=0,reason='Provider definitively rejected without accepting'),'NotificationLogView','Queue a known not-accepted failure within bounded attempts.','OFFICER or ADMIN',['Lock log/version; allow FAILED only with providerSid null and a stored allowlisted known-not-accepted error; attempts <3.','Revalidate consent, due kind and outstanding custody; set QUEUED,nextAttemptAt to now plus backoff; audit.','UNKNOWN, ACCEPTED, DELIVERED and UNDELIVERED cannot be retried as the same event; no blind duplicate SMS.'],NS,['VERSION_CONFLICT','INVALID_STATE','PROVIDER_OUTCOME_UNKNOWN','RETRY_REQUIRED'])

VIEW['AvailabilityView']=dict(resourceId=1,asOf='2026-11-10T00:00:00Z',startAt='2026-11-10T02:00:00Z',endAt='2026-11-10T05:00:00Z',unit='piece',physicalInStore=10,outstanding=0,usableOwned=10,reservedConsumable=None,rawMinimumFree=4,allocatableQuantity=4,overdueOutstanding=0,returnDependentQuantity=0,segments=[dict(startAt='2026-11-10T02:00:00Z',endAt='2026-11-10T04:00:00Z',committed=6,rawFree=4,allocationIds=[1])],warnings=[])
VIEW['ShortageView']=dict(projectId=1,asOf='2026-11-10T00:00:00Z',requirements=[dict(requirementId=1,resourceId=1,unit='piece',demand=6,approvedCoverage=0,supplied=0,outstanding=0,unallocated=6,freeIfConsideredAlone=10,sourcingGapIfConsideredAlone=0,atRisk=False)],warning='Individual candidate gaps are not additive.')
VIEW['DemandView']=dict(resourceId=1,asOf='2026-11-10T00:00:00Z',from_='2026-11-10T01:00:00Z',to='2026-11-10T06:00:00Z',includeDraft=False,unit='piece',minimumAdditionalUnits=1,segments=[dict(startAt='2026-11-10T02:00:00Z',endAt='2026-11-10T04:00:00Z',usableCapacity=10,approvedCommitted=6,unallocatedDemand=5,deficit=1,requirementIds=[1,2],projectIds=[1,2],departmentIds=[1,2])],reasons=['TIME_CONFLICT'],warning='Capacity scenario with fixed dates; not purchasing authorization.')
VIEW['InventoryReport']=dict(asOf='2026-11-10T00:00:00Z',items=[dict(resourceId=1,type='REUSABLE',unit='piece',physicalInStore=10,outstanding=0,damaged=0,usableOwned=10,ledgerConsistent=True)])
VIEW['AllocationReport']=dict(asOf='2026-11-10T00:00:00Z',items=[dict(allocationId=1,projectId=1,resourceId=1,recipientId=1,startAt='2026-11-10T01:00:00Z',endAt='2026-11-10T04:00:00Z',unissued=0,outstanding=2,overdue=True,atRisk=False)])
VIEW['DepartmentReport']=dict(asOf='2026-11-10T00:00:00Z',departmentId=1,from_='2026-11-10T01:00:00Z',to='2026-11-11T01:00:00Z',items=[dict(resourceId=1,unit='piece',requested=6,approvedCoverage=4,supplied=0,unallocated=2)],warning='Different units are never summed.')
VIEW['JobRunView']=dict(asOf='2026-11-10T00:00:00Z',created=1,skipped=0,existing=0,errors=0,providerMode='FAKE')
for _view in VIEW.values():
    if 'from_' in _view: _view['from'] = _view.pop('from_')
VIEW['AllocationView']['replay']=False
op('Q01','Planning','GET','availability','query',None,'AvailabilityView','Explain present/future capacity for one resource and interval.','organization read',['Validate active resource and present/future <=90-day interval; capture asOf once.','Read ledger plus all approved resource allocations in one repeatable-read snapshot, derive balances and commitment intervals.','Sweep grouped endpoints or calculate consumable ATP; return raw negative deficit, clamped allocatable capacity, conflicting IDs and return-dependent warning.'],['ResourceRepository.findById','InventoryTransactionRepository.findByResource_IdOrderByIdAsc','AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus'],tx='READ',query=dict(resourceId='required positive Long',startAt='required offset instant >=asOf',endAt='required offset instant > startAt',candidateQuantity='optional positive Long to compute return dependence'))
op('Q02','Shortage','GET','shortages/projects/{id}','projectSummary',None,'ShortageView','Explain each project requirement coverage and conditional sourcing gap.','project read scope',['Load project and all active requirements in a consistent read snapshot.','For each resource calculate remaining demand and available candidate capacity; do not remove existing approved commitments except when measuring already-covered demand.','Return unallocated quantities, physical supply and risk flags separately; never sum isolated candidate gaps.'],['ProjectRepository.findById','ResourceRequirementRepository.findByProject_IdOrderByIdAsc','AllocationRepository.findByRequirement_Id','InventoryTransactionRepository.findByResource_IdOrderByIdAsc'],tx='READ')
op('Q03','Shortage','GET','shortages/resources/{id}','consolidate',None,'DemandView','Consolidate competing departments and peak sourcing capacity.','organization read',['Validate bounded horizon and resource; load active requirements and eligible project states.','Compute unapproved N per requirement, existing K commitments and ledger capacity in one snapshot.','For reusable sweep K plus all N intervals together; for consumable subtract all reserved stock globally and sum selected unallocated demand.','Return peak units and affected segments/projects/departments with reason flags; no allocation or procurement side effect.'],['ResourceRepository.findById','ResourceRequirementRepository.findByResource_IdAndStatus','AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus','InventoryTransactionRepository.findByResource_IdOrderByIdAsc'],tx='READ',query=dict(from_='required offset instant >=asOf; actual query key is from',to='required offset instant max90days',includeDraft='boolean default false'))
for id,path,method,view,purpose,steps,calls,query in [
('R01','inventory','inventory','InventoryReport','Current explainable ledger stock by resource.',['Load bounded resource page and ledger rows; reconstruct balances including reversal deltas.','Return each pool/unit separately and invariant/reconciliation flags; never repair automatically.'],['ResourceRepository.findAll','InventoryTransactionRepository.findByResource_IdOrderByIdAsc'],dict(departmentId='optional positive Long',page='default0',size='1..100')),
('R02','upcoming','upcoming','AllocationReport','Approved allocations due in a selected future window.',['Validate present/future max90-day window and permitted department filter.','Load approved allocations through Specification, derive unissued/outstanding and current risk; paginate by id.'],['AllocationRepository.findAll','InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc'],dict(from_='offset instant query key from',to='offset instant',departmentId='optional Long',page='default0',size='1..100')),
('R03','overdue','overdue','AllocationReport','Outstanding reusable custody past its expected end.',['Capture now; query approved reusable allocations with endAt <= now through Specification.','Derive outstanding from ledger and retain only >0; paginate after filtering so totalElements is accurate.'],['AllocationRepository.findAll','InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc'],dict(resourceId='optional Long',departmentId='optional Long',page='default0',size='1..100')),
('R04','department-demand','departmentDemand','DepartmentReport','Observed planned demand by resource for one department/horizon.',['Validate department and max90-day horizon.','Load eligible requirements/projects, aggregate requested/approved/supplied by resource/unit, with no mixed-unit grand total.'],['DepartmentRepository.findById','ResourceRequirementRepository.findAll','AllocationRepository.findByRequirement_Id','InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc'],dict(departmentId='required Long',from_='offset instant query key from',to='offset instant'))]:
    op(id,'Report','GET','reports/'+path,method,None,view,purpose,'organization report read; contact data excluded',steps,calls,tx='READ',query=query)
action('J01','Scheduling','jobs/reminders/run-once','runOnce',{},'JobRunView','Deterministically run reminder scan once without changing clock.','ADMIN/OFFICER, DEMO profile only',['Require DEMO profile; capture Clock, invoke NotificationComponent.scanReminders(asOf).','Return enqueue/skip/existing/error counts; no implicit dispatch or physical stock change.','LIVE authenticated deployment does not register this demonstration route.'],['AllocationRepository.findAll','NotificationLogRepository.findByEventKey','NotificationLogRepository.save'],['INVALID_STATE'],tx='SPLIT')
for o in OPS:
    o['requestDto']='Op'+o['id']+'Request'
    o['queryDto']='Op'+o['id']+'Query'
    if 'from_' in o['query']:o['query']['from']=o['query'].pop('from_')
    # All references are signatures documented in04, plus inherited methods.
    o['repositories']=list(dict.fromkeys(o['repositories']))
    if o['id'] in ('07D','09D'): o['alwaysRefuses']=True
    if o['id'] in ('04L','04R','05L','05R','06L','06R'):
        o['repositories'] += ['InventoryTransactionRepository.findByResource_IdOrderByIdAsc','AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus']
    if o['id'] in ('06C','A01'):o['steps'].insert(2,'Require endAt > Clock.instant(); a current interval is allowed, a wholly past commitment is not.')
    if o['id']=='05U':o['steps'].insert(1,'Distinguish withdrawal from demand edits: quantity/window/priority/notes edits require DRAFT/SUBMITTED project and no approved allocation or movement; WITHDRAWN is allowed after all history is operationally resolved, without deleting it. Cancel REQUESTED rows and release no stock implicitly. projectId/resourceId cannot change after creation.')
    if o['id']=='P07':o['steps']=[s.replace('Require no issue/consumption ever recorded','Require no issue/consumption ever recorded for any allocation of this project') for s in o['steps']]
    if o['id']=='01U':o['steps'].insert(1,'Setting active=false requires no active AppUser, active Resource or project outside COMPLETED/CANCELLED referencing this department; otherwise409 DEPENDENCY_EXISTS.')
    if o['id']=='02U':o['steps'].insert(1,'Prevent deleting/deactivating/demoting last active ADMIN. Role/department transfer is refused while user owns a nonterminal project, is a resource custodian or has unresolved allocation custody; reassign resource custody through its own officer command first.')
dump('contracts/operations.json',OPS)
dump('contracts/response-examples.json',VIEW)

def js(x):return json.dumps(x,ensure_ascii=False,separators=(', ',': '))
ERROR_SAMPLE=dict(timestamp='2026-11-03T02:00:00Z',status=409,error='Conflict',code='RESOURCE_OVERALLOCATION',message=ERRORS['RESOURCE_OVERALLOCATION'][1],path='/api/allocations/1/approve',requestId='00000000-0000-4000-8000-000000000002',retryable=False,details=dict(availableQuantity=4,requestedQuantity=5,competingAllocationIds=[1]))
def view(o):
    if o.get('alwaysRefuses'):return dict(timestamp='2026-11-03T02:00:00Z',status=409,error='Conflict',code='IMMUTABLE_HISTORY',message=ERRORS['IMMUTABLE_HISTORY'][1],path=o['path'].replace('{id}','1'),requestId='00000000-0000-4000-8000-000000000002',retryable=False,details={})
    if o['view']=='NoContent':return None
    if o['view'].startswith('Page<'):
        n=o['view'][5:-1];return dict(content=[VIEW[n]],page=0,size=20,totalElements=1,totalPages=1)
    v=json.loads(json.dumps(VIEW[o['view']]))
    if 'createdById' in v:v['createdById']=1
    if 'updatedById' in v:v['updatedById']=1
    if 'version' in v:v['version']=0 if o['success']==201 else 1
    ps={'P01':'SUBMITTED','P02':'APPROVED','P03':'DRAFT','P04':'IN_PROGRESS','P05':'COMPLETED','P06':'CANCELLED'}
    if o['id'] in ps:v['status']=ps[o['id']]
    if o['id'] in ('A01','A02','A03','A04'):
        v.update(decisionStatus={'A01':'APPROVED','A02':'REJECTED','A03':'CANCELLED','A04':'APPROVED'}[o['id']],unissuedQuantity=6 if o['id']=='A01' else 5 if o['id']=='A04' else 0)
        if o['id'] in ('A01','A02'):v.update(decidedById=2,decidedAt='2026-11-03T02:00:00Z')
        if o['id']=='A04':v['releasedUnissued']=1
    if o['id']=='06C':v['unissuedQuantity']=0
    if o['id']=='04C':v.update(physicalInStore=0,outstanding=0,damagedInStore=0,usableOwned=0)
    if o['id'] in ('A05','A06','A07','T01'):
        v.update(type={'A05':'ISSUE','A06':'RETURN','A07':'CONSUMPTION','T01':'REVERSAL'}[o['id']],quantity=10 if o['id']=='T01' else 1,allocationId=None if o['id']=='T01' else 1,inspection='USABLE' if o['id']=='A06' else None,deltaStored=-10 if o['id']=='T01' else 1 if o['id']=='A06' else -1,deltaOutstanding=1 if o['id']=='A05' else -1 if o['id']=='A06' else 0,deltaIssued=1 if o['id'] in ('A05','A07') else 0,deltaResolved=1 if o['id']=='A06' else 0,deltaGoodReturned=1 if o['id']=='A06' else 0,reversalOfId=1 if o['id']=='T01' else None,resourceRevisionAfter=2)
    if o['id']=='N01':v.update(status='CANCELLED',cancelReason='Reminder no longer needed',nextAttemptAt=None)
    if o['id']=='N03':v.update(status='DELIVERED',providerSid='SM'+'a'*32,providerStatus='delivered',attemptCount=1,attempts=[dict(ordinal=1,attemptedAt='2026-11-03T02:00:00Z',outcome='ACCEPTED',errorCode=None,providerSid='SM'+'a'*32)],lastErrorCode=None)
    if o['id']=='N04':v.update(status='QUEUED',nextAttemptAt='2026-11-03T02:01:00Z',providerSid=None,attemptCount=1,attempts=[dict(ordinal=1,attemptedAt='2026-11-03T02:00:00Z',outcome='REJECTED_NOT_ACCEPTED',errorCode='20429',providerSid=None)],lastErrorCode='20429')
    return v
api=['# Complete REST API Catalogue',
'## Common wire contract',
'Base URL http://127.0.0.1:8080; actual port follows course baseline. Routes below include /api exactly once in ResourceConfig/controller paths. All retrieval uses GET; every state change uses POST. Content-Type and Accept are application/json; GET has no request body. In DEMO send X-Demo-User-Id: a positive active seeded user id. In authenticated profile replace with the configured verified principal mechanism; the demo header is forbidden. X-Request-Id may be a valid UUID; otherwise generate one and echo it. No route accepts a client actorId. This is a local demo identity convention, not authentication.',
'Positive Long path/query IDs; field limits/types in04 apply to all matching DTO fields. Unknown JSON keys and unknown query names fail400, not silently ignored. An omitted optional update field preserves it; explicit null clears only nullable fields. Required update fields are shown in examples and cannot be null. expectedVersion is mandatory for every versioned mutation, including deletion; immutable history delete accepts it only as an ignored compatibility field and always refuses. Read/list responses expose version. Strings are trimmed, enums uppercase exact; no PUT/PATCH/DELETE needed under the assignment convention.',
'Success is a bare named DTO or Page DTO, not a different wrapper on each route. A201 response has Location of the created row; replay of a create returns200 with replay=true where specified, or the existing notification event. A204 has no JSON/body. Updates/actions200. HTTP422 is not used: field/cross-field failures400, invalid domain state/conflict409. HTTP401/403/404/409/500 are real error statuses, never200 error bodies. Missing referenced id in a body returns404; malformed id400. All public routes can also produce400,401,403,500 as described below. No stacktrace, SQL, credentials or phone appears in an error. A known access-denied object returns403 in this local organization demo; do not use it as a public enumeration endpoint.',
'The optional page envelope is {content:[NamedView],page:integer,size:integer,totalElements:long,totalPages:integer}. Default0/20,max100,id ascending. Date filter intervals are half-open. from/to use offset instants and test event occurredAt for movements/audit, interval overlap for projects/upcoming. All list queries return an empty page200 for no matches. Resource q filter is case-insensitive substring over code/name with escaped SQL LIKE wildcards, max100 characters; no arbitrary query expressions.',
'Repository calls listed per operation include possible checks, replay and audit paths. Dependencies below supply additional exact calls; no controller calls a repository. The specification includes every operation even where an entity can also be created indirectly. InventoryTransaction POST is a typed movement dispatcher, NotificationLog POST queues an eligible computed event, AuditEvent POST appends a NOTE. These are genuine runtime creates without allowing clients to fabricate protected history.',
'## Error response schema',js(ERROR_SAMPLE),
'Error details is always an object (empty if not applicable). Validation adds fieldErrors array of {field,message}; over-allocation adds requestedQuantity,availableQuantity,competingAllocationIds; dependency conflict adds dependentTypes; version conflict adds currentVersion. retryable is true only RETRY_REQUIRED or a temporary internal condition explicitly classified. For ordinary500 false is the safe default. All documented codes map to their listed HTTP status.']
for code,(status,msg) in ERRORS.items():api.append(f'{status} {code}: {msg}')
api += ['## Request DTO catalogue',
'Every operation with JSON defines a distinct request DTO named Op plus endpoint ID plus Request, for example Op01CRequest and OpA01Request. Its permitted keys are exactly the request sample keys. Query DTOs use Op plus ID plus Query. Per-key types follow04, except expectedVersion Long>=0; reason NotBlank max1000 when required; waiveUnfulfilled Boolean default false; candidateQuantity Long>0; requestKey/commandKey UUID36; requirements list of {requirementId Long,startAt OffsetDateTime,endAt OffsetDateTime}, complete set required on reschedule. Empty {} request actions accept no other fields. Creation requests do not accept ids, states, timestamps, counters or actor references. Department/User/Project/Resource/Requirement create DTOs infer initial state server side as described in06. Null resource/allocation/inspection fields in a movement are explicitly allowed/required by movement type; Resource.quantity and client signed deltas are always rejected.',
'User create keys include username/displayName/role/departmentId only. User update separates role/department/active administration from self contact/consent in the same endpoint: if ADMIN edits another user, smsConsent may be false to revoke but may not be set true on their behalf. A self update may omit role/department/active or echo unchanged values, but attempting to change them returns403. phoneE164 is normalized from an already explicit E.164 value; no guessed country. consentVersion OP_SMS_V1 is generated by the server only after the self opt-in. A valid regex is not evidence the number exists; the live test recipient must independently be confirmed/allowed in the provider account.',
 'For stock POST, resourceId,type,quantity,reason,commandKey are mandatory; allocationId is required for ISSUE/RETURN/CONSUMPTION and optional only for custody LOSS, forbidden otherwise except REVERSAL which derives it from reversalOfId. inspection is required only RETURN, null otherwise. reversalOfId required only REVERSAL; correctionReferenceId optional for a present corrective observation and must reference same resource. The reverse convenience route derives resource,type,quantity,allocation,inspection from original. All underlying rules are identical to direct POST; no second stock-write path bypasses validation.', '## Named response DTO examples']
for name,sample in VIEW.items():api += [f'### {name}',js(sample)]
api += ['These are complete shape examples, not populated real database records. ID1 is illustrative. Nulls shown for optional values do not prove every null is permitted in every state. Resource balances are type-dependent: reusable exposes S/O/B/U and availableConsumable=null; consumable exposes physicalInStore=P, outstanding=0,damagedInStore=0,usableOwned=P,reservedConsumable=V,availableConsumable=ATP. Names and relation IDs have the exact meanings in04. AllocationView includes replay Boolean on create replay (false otherwise); InventoryTransactionView replay is always present. User contact values are null for unauthorized viewers rather than omitted. List response content is an array of its exact named view. No generic Map of uncontrolled keys is accepted as business input.']
for o in OPS:
    success=view(o)
    # Full named schema is above; retain compact concrete response examples per operation.
    failcode=next((x for x in o['errors'] if x not in ('BAD_REQUEST','VALIDATION_FAILED','UNAUTHENTICATED','FORBIDDEN','INTERNAL_ERROR','NOT_FOUND')), 'NOT_FOUND' if '{id}' in o['path'] else 'VALIDATION_FAILED')
    fs=dict(ERROR_SAMPLE,status=ERRORS[failcode][0],error={400:'Bad Request',401:'Unauthorized',403:'Forbidden',404:'Not Found',409:'Conflict',500:'Internal Server Error'}[ERRORS[failcode][0]],code=failcode,message=ERRORS[failcode][1],path=o['path'].replace('{id}','1'),details={})
    api += [f"## {o['id']} {o['http']} {o['path']}",f"Purpose: {o['purpose']} Permission: {o['permission']}.",
    'Path parameters: '+('id positive Long, target record identity.' if '{id}' in o['path'] else 'none.')+' Query parameters: '+(js(o['query']) if o['query'] else 'none.'),
    'Headers: Accept application/json; '+('Content-Type application/json; ' if o['http']=='POST' else '')+'X-Demo-User-Id in DEMO; verified principal in authenticated profile; optional X-Request-Id UUID.',
    'Request JSON ('+o['requestDto']+'): '+(js(o['body']) if o['body'] is not None else 'none; GET has no body.'),
    ("No successful deletion is permitted for posted history. Documented response: HTTP409 Error JSON. Example: "+js(success) if o.get('alwaysRefuses') else f"Success: HTTP{o['success']}; "+('no body.' if success is None else o['view']+' with complete shape above. Example: '+js(success))),
    'Errors: '+', '.join(str(ERRORS[c][0])+' '+c for c in o['errors'])+'. Common message catalogue and Error schema apply. Example failure: '+js(fs),
    f"Controller: {o['controller']}.{o['controllerMethod']}. Autowired component: {o['component']}. Forward exactly to {o['component']}.{o['method']} as defined in06.",
    'Repository methods involved: '+', '.join(o['repositories'])+'.',
    'Business rules and ordered method behavior: '+' '.join(f'{i+1}. {s}' for i,s in enumerate(o['steps'])),
    f"Transaction: {o['transaction']}. READ means a consistent read-only transaction; WRITE means one rollback-on-error component transaction; SPLIT means short database phases separated from external work, documented in07. Example request: {o['http']} http://127.0.0.1:8080{o['path'].replace('{id}','1')} with the headers/body above and query keys if specified. No calls have been executed against an implementation."]
write('05-REST-API-CATALOGUE.md','\n\n'.join(api))

components=['# Component and Method Specification',
'## Shared public method boundary',
'Method signatures below are specification signatures, not application source code. Actor is a resolved immutable {userId,role,departmentId,system} value supplied by the trusted resolver; clients never construct it. Window is {startAt,endAt}; quantities Long. Each OpEndpointIDRequest is exactly the DTO in05. Id methods take Long id. A query DTO contains exactly the corresponding filter keys with OffsetDateTime parsed to Instant. Page<T> here means the public page DTO, not exposing Spring internals. Every public method validates DTO with javax Validator before business checks, checks actor/active/object permissions, and maps domain exceptions through the standard error catalogue. Nonexistent references404; invalid fields400; disallowed action403; lifecycle and invariant failures409.',
'Dependencies follow the listed repository calls plus PermissionComponent, AuditComponent and Clock. Inventory writes additionally call AvailabilityComponent.calculateLocked. Report and shortage components call the same pure balance/sweep calculations, not controllers. Notification external transport is only TwilioIntegrationComponent. Component methods do not make internal HTTP calls to their own REST API. All Write methods are public proxy-invoked Transactional(rollbackFor=Exception.class); Read are readOnly consistent snapshots. No self-invocation is relied on to start a new transaction. Exceptions abort the whole command before any success DTO is returned.']
for comp in sorted(set(o['component'] for o in OPS)):
    ops=[o for o in OPS if o['component']==comp]
    deps=sorted(set(r.split('.')[0] for o in ops for r in o['repositories']))
    components += [f'## {comp}', 'Responsibility: '+'; '.join(dict.fromkeys(o['purpose'] for o in ops)),'Dependencies: '+', '.join(deps)+', PermissionComponent, Clock, DTO mapper'+(' and AuditComponent.' if comp!='AuditComponent' else '.')]
    for o in ops:
        args=[]
        if '{id}' in o['path']:args.append('Long id')
        if o['body'] is not None:args.append(o['requestDto']+' input')
        if o['query']:args.append(o['queryDto']+' query')
        args.append('Actor actor')
        components += [f"### {o['id']} {o['method']}",f"Signature: {o['view']} {o['method']}({', '.join(args)}). NoContent means void and maps204 only on real deletion.",
        f"Purpose/input/output: {o['purpose']} Input fields and return DTO are exactly those in05 operation{o['id']}. Permission: {o['permission']}.",
        '\n'.join(f'{i+1}. {s}' for i,s in enumerate(o['steps'])),
        'Repository calls: '+', '.join(o['repositories'])+'.',
        'Other components: PermissionComponent.require; AuditComponent.append for state changes except audit NOTE itself; '+('InventoryTransactionComponent.record for convenience movement routes; ' if o['id'] in ('A05','A06','A07') else '')+('AvailabilityComponent.calculateLocked for capacity commands; ' if o['id'] in ('A01','P07','04U','06C','07C','T01') else '')+'DTO mapping happens before transaction closes.',
        f"Transaction: {o['transaction']}. Exceptions: {', '.join(o['errors'])}. Edge cases: absent/invalid references, inactive actor, duplicate input and stale state use the error contract; type-specific and lifecycle edge cases are the ordered rules above and02. No catch-and-continue after a database failure."]
components += ['## Internal component methods',
'PermissionComponent.require(Actor actor, Action action, TargetContext target): void. No transaction starts here; participates in caller. Re-read active AppUser/department/resource role as appropriate, compare owner/department/custodian IDs, enforce non-self approval and state-specific permission, throw FORBIDDEN or UNAUTHENTICATED. Dependencies AppUserRepository.findById and target context already loaded. Action is an internal allowlisted enum of API operation IDs, not client permission expressions. ActorResolver.resolve(headers,principal): Actor runs before controller delegation, validates profile and active user; never uses body actor IDs.',
'AuditComponent.append(String action,String targetType,Long targetId,RedactedSnapshot before,RedactedSnapshot after,String reason,Actor actor,String requestId): AuditEventView. Participates in existing WRITE transaction, never REQUIRES_NEW. Validate action/target allowlist and sizes; generate targetSummary and server time; remove phone/secret/provider credential fields; AuditEventRepository.save; no recursion. Errors roll back the main command. Failed/refused actions go to safe operational logs with requestId, not a false committed AuditEvent. Public appendNote uses09C and its own WRITE transaction.',
'AvailabilityComponent.balanceLocked(Long resourceId): Balance. Require caller already holds Resource lock and write transaction; InventoryTransactionRepository.findByResource_IdOrderByIdAsc; sum exact signed vectors with overflow checks; verify reusable S/O/B nonnegative and consumable O/B zero. Group allocation net counters; validate counter equations. Balance is an immutable value, never an entity. Corrupt data raises INTERNAL_ERROR with safe diagnostics and no automatic repair.',
'AvailabilityComponent.calculateLocked(Long resourceId,Window window,Instant asOf,Set<Long> excludedAllocationIds,List<ProposedCommitment> replacements): AvailabilityView. Caller owns WRITE resource lock; load ResourceRepository.findById, full approved allocations with AllocationRepository.findByRequirement_Resource_IdAndDecisionStatus and ledger via balanceLocked. Validate the internal exclusion list is only affected records owned by the command; clients cannot send exclusions. Build R/O contributions, merge replacements, sweep endpoints ending before starting; return minimum, all conflict segments, physical quantity and dependence. Throws BAD_REQUEST/NOT_FOUND/INTERNAL_ERROR. Does not approve, mutate, or call Twilio. Read query uses the same pure sweep over a consistent snapshot rather than acquiring a write lock.',
'ShortageComponent.remaining(ResourceRequirement requirement,List<Allocation> allocations,LedgerSnapshot ledger): RequirementCoverage. Pure function using persisted statuses/effective quantities and signed counters; no repository calls. Return D,C,N,supplied,outstanding; reject impossible coverage as corruption. Used by projectSummary and consolidate, ensuring per-request and aggregate reports share arithmetic.',
'InventoryTransactionComponent.applyTypedMovement(ValidatedMovement input,LockedResource context,Actor actor): InventoryTransactionView. Internal helper inside record transaction; no extra proxy/transaction assumed. Select exact effect vector in02, never client deltas. LOSS with allocation affects O/resolution, without allocation affects S; DAMAGE storage only; RETURN inspected; adjustments officer only. Check type/quantity/counters and stock before insert, bump parent version, flush, save immutable vector and audit. Convenience issue/return/consume invoke public record on the injected separate component with full typed DTO. reverse invokes the same effect writer after dependency fence and reversal checks. No nested public method annotation is relied on through self-calls.',
'NotificationComponent.scanReminders(Instant asOf): ScanResult. Single-process scan, no long all-rows transaction. Query AllocationRepository.findAll(Specification decisionStatus APPROVED and resource.type REUSABLE) in bounded pages, derive outstanding via InventoryTransactionRepository.findByAllocation_IdOrderByIdAsc. If outstanding>0 and now in [endAt-24h,endAt), enqueue RETURN_DUE; if now>=endAt enqueue OVERDUE. Calls enqueueEvent in a separate transactional bean/proxy per event; catch/report one record failure without stopping remaining events. No stock mutation. Event key suppresses repeats across scans/restarts; recipient fixed allocation.recipient.',
'NotificationComponent.enqueueEvent(Long allocationId,NotificationKind kind,Instant asOf,Actor system): NotificationLogView. WRITE per event. Re-read current allocation/custody, consent/contact and due predicate; findByEventKey then insert unique key. On unique race roll back and re-read in a new READ transaction; do not continue a failed transaction. Save QUEUED or SKIPPED, with no provider call. CREATED/EXISTING/SKIPPED counts are explicit result values. Reversal of last return may make a reminder eligible again but an existing event key prevents a repeat for the same due instant.',
'NotificationComponent.claim(Long notificationId,Instant asOf): Optional<DispatchEnvelope>. Short WRITE. NotificationLogRepository.lockById; require QUEUED and nextAttemptAt<=asOf, attempts<3; re-read allocation and recipient under same transaction, recompute outstanding. If returned/cancelled/no longer due or consent absent set CANCELLED/SKIPPED, append audit and return empty. Otherwise set SENDING, increment attempt count, append STARTED attempt with timestamp, commit and return an in-memory destination/body/SID-free envelope. The full number is never persisted in log. Caller sends only after claim commits. The existing STARTED attempt entry is completed in place exactly once, while subsequent attempts append; operational attempt rows are an exception to append-only outcome completion, not editable by public API.',
'NotificationComponent.finish(Long notificationId,SendResult result): NotificationLogView. New WRITE. Lock log, require same SENDING attempt; map accepted SID/status, known rejection or UNKNOWN exactly as07, complete attempt outcome, clear/set nextAttemptAt, save audit. A process crash after possible send is recovered as UNKNOWN, not automatically QUEUED. If finish itself fails, durable SENDING remains recoverable; do not roll back the already committed allocation or resend inline.',
'NotificationComponent.dispatchDue(): void. Outside DB transaction, fetch up to20 due IDs via NotificationLogRepository.findDue(QUEUED,Clock.instant(),PageRequest), call claim per ID through a separate transactional proxy, then TwilioIntegrationComponent.sendSms, then finish. Single runner; each exception logged by request/event id and remaining IDs continue. Timeout ambiguity becomes UNKNOWN. Scheduling never sends raw resource lists or full user data.',
'TwilioIntegrationComponent.sendSms(String destinationE164,String body): SendResult and fetchStatus(String providerSid): ProviderStatus. No DB transaction or repository. Select FAKE/LIVE fixed configured host only; construct verified Retrofit form request, set secret Authorization header server side, execute with bounded timeouts, parse safe provider result into accepted/rejected/unknown. Do not invent provider idempotency. fetchStatus is read-only external GET. Retry decisions are made by NotificationComponent, not automatically by the HTTP client. Full contract in07.',
'SchedulingComponent.scan(): void fixedDelay60000, and dispatch(): void fixedDelay5000. Each takes a local AtomicBoolean guard, captures Clock, calls the NotificationComponent methods on injected proxies, releases guard in finally, logs counts/errors; no business state update merely because an interval ended. SchedulingComponent.runOnce(OpJ01Request,Actor) is the DEMO route in05, calls same scan logic once and returns counts. A separate maintenance recovery step marks stale SENDING older than120seconds UNKNOWN; no auto-resend. NotificationLogRepository.findAll with status/time Specification and lockById supplies candidates.',
'ReportingComponent snapshot methods inR01..R04 use a shared READ transaction and existing pure balance/coverage methods, apply scope and bounded filters, include asOf and explicit units. Avoid pagination before derived overdue filtering; compute totalElements on the filtered set within demo bounds. No saved report entity or unsupported repository method is invented.',
'DemoSeedBootstrap.initialize(): void, PostConstruct, DEMO profile only. Validate DEMO profile. If demo.bootstrap-admin.enabled call injected SeedComponent.bootstrapAdmin() first; if demo.seed.enabled then call injected SeedComponent.seedScenario(), both through Spring proxies. Print the returned nonsecret bootstrap user ID for the ordered Postman setup. Do not put Transactional on a self-invoked PostConstruct method and expect it to start a transaction.',
'SeedComponent.bootstrapAdmin(): Long. WRITE transaction, DEMO profile only, explicit ORGRESOURCE_BOOTSTRAP_ADMIN_ENABLED=true; default false. AppUserRepository.findByUsername("demo.admin") returns an existing active ADMIN id on repeat. If absent, require AppUserRepository.count(empty Specification)==0; otherwise abort rather than injecting a privileged identity into an existing dataset. Save one active ADMIN with username demo.admin, displayName Demo Administrator, department null, phone null, consent false, createdBy SYSTEM/null; append SEED audit and return generated id. All timestamps/role values are server assigned. This method never resets data or creates anonymous user-registration routes. A shared/live profile cannot enable it. DemoSeedBootstrap invokes it before optional full fixture seed; seedScenario requires this known administrator already exists.',
'SeedComponent.seedScenario(): SeedResult. WRITE transaction using inherited findById/save and code lookups on the nine repositories plus ordinary domain commands through components after dependency bootstrap. Create the SYSTEM/bootstrap administrator and three fictional departments/users first. Use stable codes and fixed UUID command/request keys, receipt opening stock through normal movement rules, then projects/requirements/allocations in08. Seeds use injected Clock-relative future instants. If sentinel project exists verify entire fixture shape and skip; if partial mismatch abort rather than duplicate. Never reset user data. Disable by default outside DEMO. Mock contact absent, consent false, LIVE SMS never seeded. Initial notification SKIPPED sample may be created only after issuing eligible fictional custody under a frozen test clock; no fake ACCEPTED/DELIVERED record.']
write('06-COMPONENT-SPECIFICATION.md','\n\n'.join(components))

# Importable request coverage, not a claim of a running application.
folders={}
for o in OPS:
    id_variable='projectId' if o['id']=='Q02' else 'resourceId' if o['id']=='Q03' else o['domain'][0].lower()+o['domain'][1:]+'Id'
    raw='{{baseUrl}}'+o['path'].replace('{id}','{{'+id_variable+'}}')
    q=[]
    for k,v in o['query'].items():
        val='{{resourceId}}' if k=='resourceId' else '{{departmentId}}' if k=='departmentId' else '{{windowStart}}' if k in ('startAt','from') else '{{windowEnd}}' if k in ('endAt','to') else '20' if k=='size' else '0' if k=='page' else 'false' if k=='includeDraft' else '1'
        q.append(dict(key=k,value=val,disabled=not ('required' in v or k in ('startAt','endAt','from','to'))))
    req=dict(method=o['http'],header=[dict(key='Accept',value='application/json'),dict(key='X-Demo-User-Id',value='{{actingUserId}}')],url=dict(raw=raw,host=['{{baseUrl}}'],path=o['path'].replace('{id}','{{'+id_variable+'}}').lstrip('/').split('/'),query=q),description=f"{o['id']}: {o['purpose']} Permission: {o['permission']}. Expected {409 if o.get('alwaysRefuses') else o['success']}; error codes "+', '.join(o['errors'])+'. See05/06 for complete contract.')
    if q:req['url']['raw'] += '?'+ '&'.join(k['key']+'='+k['value'] for k in q if not k['disabled'])
    if o['body'] is not None:
        body=json.dumps(o['body'],indent=2)
        for m in set(re.findall(r'"(\{\{\w+Id\}\})"',body)):body=body.replace('"'+m+'"',m)
        req['header'].append(dict(key='Content-Type',value='application/json'))
        req['body']=dict(mode='raw',raw=body,options=dict(raw=dict(language='json')))
    accepted=[409] if o.get('alwaysRefuses') else [201,200] if o['id'] in ('06C','07C','08C','A05','A06','A07','T01') else [o['success']]
    test=[f"pm.test('Expected status contract', function () {{ pm.expect(pm.response.code).to.be.oneOf({json.dumps(accepted)}); }});"]
    # Collection coverage requests are examples; separate ordered demo captures all ids.
    _code=409 if o.get('alwaysRefuses') else o['success']
    _ec=next((c for c in o['errors'] if c not in ('BAD_REQUEST','VALIDATION_FAILED','UNAUTHENTICATED','FORBIDDEN','INTERNAL_ERROR')), 'VALIDATION_FAILED')
    _err=dict(ERROR_SAMPLE,status=ERRORS[_ec][0],code=_ec,message=ERRORS[_ec][1],path=o['path'].replace('{id}','1'),details={})
    item=dict(name=o['id']+' '+o['method']+' '+o['path'],request=req,event=[dict(listen='test',script=dict(type='text/javascript',exec=test))],response=[dict(name='Documented result',originalRequest=req,status='Conflict' if _code==409 else 'No Content' if _code==204 else 'Created' if _code==201 else 'OK',code=_code,header=[],body='' if view(o) is None else json.dumps(view(o),indent=2)),dict(name='Example failure',originalRequest=req,status='Conflict' if _err['status']==409 else 'Not Found' if _err['status']==404 else 'Bad Request',code=_err['status'],header=[],body=json.dumps(_err,indent=2))])
    folders.setdefault(o['domain'],[]).append(item)
collection=dict(info=dict(name='OrgResource Complete API Contracts',schema='https://schema.getpostman.com/json/collection/v2.1.0/collection.json',description='All specified routes. Documentation examples only; backend not implemented. Set ids and actor per permission. Use separate ordered demonstration collection first.'),item=[dict(name=k,item=v) for k,v in folders.items()],variable=[dict(key='baseUrl',value='http://127.0.0.1:8080')])
dump('postman/OrgResource-API.postman_collection.json',collection)
env=dict(name='OrgResource Local Fictional Demo',values=[dict(key=k,value=v,enabled=True) for k,v in dict(baseUrl='http://127.0.0.1:8080',actingUserId='',departmentId='',userId='',memberId='',custodianId='',projectId='',resourceId='',requirementId='',allocationId='',inventoryTransactionId='',notificationId='',auditId='',planningId='',shortageId='',reportId='',schedulingId='',windowStart='',windowEnd='',startA='',endA='',commandKey='',requestKey='').items()],_postman_variable_scope='environment')
dump('postman/OrgResource-Local.postman_environment.json',env)
print(json.dumps(dict(entities=len(E),operations=len(OPS),views=len(VIEW),customRepositoryMethods=sum(map(len,REPOS.values())))))






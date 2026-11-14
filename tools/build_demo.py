from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
SCHEMA='https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
items=[]
def req(name,method,path,body=None,actor='bootstrapAdminId',capture=None,expect=200,checks=None):
    tests=[f"pm.test('HTTP {expect}',()=>pm.response.to.have.status({expect}));"]
    if capture:
        tests += ["if(pm.response.code>=200 && pm.response.code<300){ const r=pm.response.json();",f"pm.environment.set('{capture}',r.id); pm.environment.set('{capture}Version',r.version);",'}']
    if checks:tests += checks
    r=dict(method=method,header=[dict(key='Accept',value='application/json'),dict(key='X-Demo-User-Id',value='{{'+actor+'}}')],url='{{baseUrl}}/api/'+path)
    if body is not None:
        raw=json.dumps(body,indent=2)
        import re
        for var in set(re.findall(r'"(\{\{[^{}]+(?:Id|Version)\}\})"',raw)): raw=raw.replace('"'+var+'"',var)
        r['header'].append(dict(key='Content-Type',value='application/json'))
        r['body']=dict(mode='raw',raw=raw,options=dict(raw=dict(language='json')))
    events=[dict(listen='test',script=dict(type='text/javascript',exec=tests))]
    if '{{windowStart}}' in path:events.insert(0,dict(listen='prerequest',script=dict(type='text/javascript',exec=["pm.environment.set('windowStart',new Date(Date.now()+60000).toISOString());"])))
    items.append(dict(name=name,request=r,event=events))

def getversion(entity,var,actor):req('Read current '+var,'GET',entity+'/{{'+var+'}}',actor=actor,capture=var)
def transition(path,var,action,actor,extra=None):
    getversion(path,var,actor)
    req(action+' '+var,'POST',path+'/{{'+var+'}}/'+action,dict(expectedVersion='{{'+var+'Version}}',reason='Fictional demonstration',**(extra or {})),actor,var)
def key(n):return f'00000000-0000-4000-8000-{n:012d}'
for n,label in [('A','Logistics'),('B','Programs'),('C','Communications')]:
    req('Create department '+n,'POST','departments',dict(code='{{runPrefix}}_'+n,name='Demo '+label,description='Fictional department'),'bootstrapAdminId','dep'+n+'Id',201)
for var,role,dept in [('officerId','OFFICER',None),('custodianId','CUSTODIAN','depAId'),('memberAId','MEMBER','depAId'),('memberBId','MEMBER','depBId'),('memberCId','MEMBER','depCId')]:
    req('Create '+var,'POST','users',dict(username='{{runLower}}.'+var.lower(),displayName='Fictional '+var,role=role,departmentId='{{'+dept+'}}' if dept else None),'bootstrapAdminId',var,201)
for var,typ,unit,name in [('cordId','REUSABLE','piece','Extension Cords'),('paperId','CONSUMABLE','sheet','Paper')]:
    req('Create '+var,'POST','resources',dict(code='{{runPrefix}}_'+var.upper(),name='Fictional '+name,description='Synthetic demonstration only',type=typ,category='Demo',unit=unit,storageLocation='Demo cabinet',controlBasis='OWNED',authorizationNote=None,departmentId='{{depAId}}',custodianId='{{custodianId}}'),'custodianId',var,201)
    req('Opening receipt '+var,'POST','inventory-transactions',dict(resourceId='{{'+var+'}}',allocationId=None,type='RECEIPT',quantity=10 if var=='cordId' else 100,inspection=None,reason='OPENING_COUNT fictional',commandKey='{{receipt'+var+'Key}}',reversalOfId=None,correctionReferenceId=None),'custodianId','receipt'+var+'Id',201)
req('Identical cord receipt replay','POST','inventory-transactions',dict(resourceId='{{cordId}}',allocationId=None,type='RECEIPT',quantity=10,inspection=None,reason='OPENING_COUNT fictional',commandKey='{{receiptcordIdKey}}',reversalOfId=None,correctionReferenceId=None),'custodianId',expect=200,checks=["pm.test('Replay true',()=>pm.expect(pm.response.json().replay).to.eql(true));"])
for n,q in [('A',6),('B',5),('C',4)]:
    req('Create project '+n,'POST','projects',dict(code='{{runPrefix}}_P'+n,name='Fictional Project '+n,description='Synthetic resource scenario',departmentId='{{dep'+n+'Id}}',ownerId='{{member'+n+'Id}}',startAt='{{start'+n+'}}',endAt='{{end'+n+'}}'),'member'+n+'Id','project'+n+'Id',201)
    req('Cord requirement '+n,'POST','requirements',dict(projectId='{{project'+n+'Id}}',resourceId='{{cordId}}',quantity=q,startAt='{{start'+n+'}}',endAt='{{end'+n+'}}',priority='NORMAL',notes='Fictional requirement'),'member'+n+'Id','reqCord'+n+'Id',201)
    if n in ('A','B'):
        req('Paper requirement '+n,'POST','requirements',dict(projectId='{{project'+n+'Id}}',resourceId='{{paperId}}',quantity=60 if n=='A' else 50,startAt='{{start'+n+'}}',endAt='{{end'+n+'}}',priority='NORMAL',notes='Fictional paper demand'),'member'+n+'Id','reqPaper'+n+'Id',201)
    transition('projects','project'+n+'Id','submit','member'+n+'Id')
    transition('projects','project'+n+'Id','approve','officerId')

def allocation(var,reqvar,qty,actor,keyvar,expectapprove=200):
    req('Request '+var,'POST','allocations',dict(requirementId='{{'+reqvar+'}}',quantity=qty,recipientId='{{'+actor+'}}',requestKey='{{'+keyvar+'}}'),actor,var,201)
    getversion('allocations',var,'custodianId')
    req('Approve '+var,'POST','allocations/{{'+var+'}}/approve',dict(expectedVersion='{{'+var+'Version}}',reason='Fictional capacity decision'),'custodianId',var if expectapprove==200 else None,expectapprove,checks=["pm.test('Quantity conflict',()=>pm.expect(pm.response.json().code).to.eql('RESOURCE_OVERALLOCATION'));"] if expectapprove==409 else None)
allocation('allocCordAId','reqCordAId',6,'memberAId','allocCordAKey')
allocation('allocCordBFailedId','reqCordBId',5,'memberBId','allocCordBFailedKey',409)
transition('allocations','allocCordBFailedId','cancel','memberBId')
allocation('allocCordBId','reqCordBId',4,'memberBId','allocCordBKey')
allocation('allocCordCId','reqCordCId',4,'memberCId','allocCordCKey')
req('Peak full cord demand shortage one','GET','shortages/resources/{{cordId}}?from={{windowStart}}&to={{endC}}',actor='officerId',checks=["pm.test('One extra cord at peak',()=>pm.expect(pm.response.json().minimumAdditionalUnits).to.eql(1));"])
allocation('allocPaperAId','reqPaperAId',60,'memberAId','allocPaperAKey')
req('Consume twenty sheets','POST','allocations/{{allocPaperAId}}/consume',dict(quantity=20,reason='Fictional handover',commandKey='{{consumePaperKey}}'),'custodianId','consumePaperId',201)
req('Physical80 reserved40 available40','GET','resources/{{paperId}}',actor='officerId',checks=["const r=pm.response.json(); pm.test('No double subtraction',()=>{pm.expect(r.physicalInStore).to.eql(80);pm.expect(r.reservedConsumable).to.eql(40);pm.expect(r.availableConsumable).to.eql(40);});"])
allocation('allocPaperBFailedId','reqPaperBId',50,'memberBId','allocPaperBFailedKey',409)
transition('allocations','allocPaperBFailedId','cancel','memberBId')
allocation('allocPaperBId','reqPaperBId',40,'memberBId','allocPaperBKey')
for n,qty in [('A',6),('B',4)]:req('Issue cords '+n,'POST','allocations/{{allocCord'+n+'Id}}/issue',dict(quantity=qty,reason='Fictional custody handover',commandKey='{{issue'+n+'Key}}'),'custodianId','issue'+n+'Id',201)
req('Queue due reminder without consent','POST','notifications',dict(allocationId='{{allocCordAId}}',kind='RETURN_DUE'),'custodianId','notificationId',201,checks=["pm.test('No consent means skipped',()=>pm.expect(pm.response.json().status).to.eql('SKIPPED'));"])
for n in (1,2):req('Reminder scan '+str(n),'POST','jobs/reminders/run-once',{},'officerId')
for label,var,qty,condition in [('Afirst','allocCordAId',2,'USABLE'),('Arest','allocCordAId',4,'USABLE'),('Bgood','allocCordBId',3,'USABLE'),('Bdamaged','allocCordBId',1,'DAMAGED')]:
    req('Return '+label,'POST','allocations/{{'+var+'}}/return',dict(quantity=qty,reason='Fictional inspection '+label,commandKey='{{return'+label+'Key}}',inspection=condition),'custodianId','return'+label+'Id',201)
req('Inspect final cord condition','GET','resources/{{cordId}}',actor='officerId',checks=["const r=pm.response.json();pm.test('Nine usable one damaged',()=>{pm.expect(r.physicalInStore).to.eql(9);pm.expect(r.outstanding).to.eql(0);pm.expect(r.damagedInStore).to.eql(1);});"])
req('Unused temporary department','POST','departments',dict(code='{{runPrefix}}_TMP',name='Temporary Demo',description='Deletion test'),'bootstrapAdminId','tempDepartmentId',201)
getversion('departments','tempDepartmentId','bootstrapAdminId')
req('Physical runtime deletion','POST','departments/{{tempDepartmentId}}/delete',dict(expectedVersion='{{tempDepartmentIdVersion}}',reason='Unused fictional row'),'bootstrapAdminId',expect=204)
req('Deleted row now missing','GET','departments/{{tempDepartmentId}}',actor='bootstrapAdminId',expect=404)
req('Create auditable note','POST','audit-events',dict(targetType='Project',targetId='{{projectAId}}',reason='Fictional review note'),'officerId','auditId',201)
for path,var,actor in [('departments','depAId','officerId'),('users','memberAId','memberAId'),('projects','projectAId','memberAId'),('resources','cordId','officerId'),('requirements','reqCordAId','memberAId'),('allocations','allocCordAId','memberAId'),('inventory-transactions','issueAId','custodianId'),('notifications','notificationId','officerId'),('audit-events','auditId','officerId')]:
    req('List '+path,'GET',path,actor=actor)
    req('Read '+path+' by ID','GET',path+'/{{'+var+'}}',actor=actor)
for path in ['reports/inventory','reports/overdue','reports/upcoming?from={{windowStart}}&to={{endC}}','reports/department-demand?departmentId={{depAId}}&from={{windowStart}}&to={{endC}}','shortages/projects/{{projectBId}}']:
    req('Read '+path,'GET',path,actor='officerId')

setup=["if(!pm.environment.get('bootstrapAdminId')) throw new Error('Set bootstrapAdminId from isolated DEMO startup output before running.');",
"const stamp=String(Date.now()).slice(-7);pm.environment.set('runPrefix','D'+stamp);pm.environment.set('runLower','d'+stamp);",
"const now=Date.now(); const iso=m=>new Date(now+m*60000).toISOString();",
"for(const [k,m] of Object.entries({startA:-5,endA:60,startB:-4,endB:120,startC:60,endC:180,windowStart:1}))pm.environment.set(k,iso(m));"]
keys=['receiptcordIdKey','receiptpaperIdKey','allocCordAKey','allocCordBFailedKey','allocCordBKey','allocCordCKey','allocPaperAKey','allocPaperBFailedKey','allocPaperBKey','consumePaperKey','issueAKey','issueBKey','returnAfirstKey','returnArestKey','returnBgoodKey','returnBdamagedKey']
setup += ["for(const k of "+json.dumps(keys)+")pm.environment.set(k,pm.variables.replaceIn('{{$guid}}'));"]
# Initialize once per collection run at first request, not on every request.
items[0]['event'].insert(0,dict(listen='prerequest',script=dict(type='text/javascript',exec=setup)))
coll=dict(info=dict(name='OrgResource Ordered Fictional Demonstration',schema=SCHEMA,description='Fresh isolated database with bootstrap ADMIN only. No live sends. Creates and captures dependency IDs; positive and expected-conflict assertions. Execute within one hour of generated windows. Backend not yet implemented.'),item=items)
(ROOT/'postman/OrgResource-Demo.postman_collection.json').write_text(json.dumps(coll,indent=2)+'\n',encoding='utf-8')
demo_count=len(items)
items=[]
req('Member cannot approve own allocation','POST','allocations/{{allocCordAId}}/approve',dict(expectedVersion=0,reason='Unauthorized test'),'memberAId',expect=403)
req('Consumable cannot be returned','POST','allocations/{{allocPaperAId}}/return',dict(quantity=1,inspection='USABLE',reason='Invalid type test',commandKey='{{$guid}}'),'custodianId',expect=409,checks=["pm.test('Type mismatch',()=>pm.expect(pm.response.json().code).to.eql('RESOURCE_TYPE_MISMATCH'));"])
req('Posted movement cannot be deleted','POST','inventory-transactions/{{issueAId}}/delete',dict(expectedVersion=0,reason='Invalid deletion test'),'officerId',expect=409)
req('Audit cannot be deleted','POST','audit-events/{{auditId}}/delete',dict(expectedVersion=0,reason='Invalid deletion test'),'officerId',expect=409)
req('Negative resource quantity invalid','POST','inventory-transactions',dict(resourceId='{{cordId}}',type='RECEIPT',quantity=-1,reason='Invalid test',commandKey='{{$guid}}'),'custodianId',expect=400)
req('Receipt command key changed payload','POST','inventory-transactions',dict(resourceId='{{cordId}}',allocationId=None,type='RECEIPT',quantity=11,inspection=None,reason='OPENING_COUNT fictional',commandKey='{{receiptcordIdKey}}',reversalOfId=None,correctionReferenceId=None),'custodianId',expect=409)
req('Unknown JSON field','POST','departments',dict(code='BAD_FIELD',name='Invalid demo',clientActorId=99),'bootstrapAdminId',expect=400)
req('Missing record','GET','resources/9007199254740000',actor='officerId',expect=404)
negative=dict(info=dict(name='OrgResource Expected Failure Contracts',schema=SCHEMA,description='Run after Ordered Demo in same selected environment. Checks refusal behavior; no real SMS. Backend not implemented.'),item=items)
(ROOT/'postman/OrgResource-Negative.postman_collection.json').write_text(json.dumps(negative,indent=2)+'\n',encoding='utf-8')
# Alias variables for convenient use of the complete route catalogue.
aliases=dict(departmentId='depAId',userId='memberAId',memberId='memberAId',projectId='projectAId',resourceId='cordId',requirementId='reqCordAId',allocationId='allocCordAId',inventoryTransactionId='issueAId',auditId='auditId',actingUserId='officerId',windowEnd='endC')
last=next(e for e in coll['item'][-1]['event'] if e['listen']=='test')['script']['exec']
last += [f"pm.environment.set('{a}',pm.environment.get('{b}'));" for a,b in aliases.items()]
(ROOT/'postman/OrgResource-Demo.postman_collection.json').write_text(json.dumps(coll,indent=2)+'\n',encoding='utf-8')
envpath=ROOT/'postman/OrgResource-Local.postman_environment.json'
env=json.loads(envpath.read_text());env['values'].append(dict(key='bootstrapAdminId',value='',enabled=True));envpath.write_text(json.dumps(env,indent=2)+'\n')
print(json.dumps(dict(demoRequests=demo_count,negativeRequests=len(items))))



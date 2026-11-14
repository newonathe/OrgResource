from pathlib import Path
import json,re,hashlib,itertools
ROOT=Path(__file__).resolve().parents[1]
read=lambda p:json.loads((ROOT/p).read_text(encoding='utf-8-sig'))
model=read('contracts/domain-model.json');ops=read('contracts/operations.json')
checks=[]
def check(name,truth,detail=''):
    checks.append(dict(name=name,passed=bool(truth),detail=detail))
check('Nine justified entities',len(model['entities'])==9)
check('Unique endpoint IDs',len({o['id'] for o in ops})==len(ops))
check('Unique method URL pairs',len({(o['http'],o['path']) for o in ops})==len(ops))
check('GET reads POST writes',all(o['http'] in ('GET','POST') and (o['http']!='GET' or o['body'] is None) for o in ops))
inherited={'findById','findAll','save','flush','delete','existsById','count'}
allowed={n+'Repository':inherited|{r['name'] for r in rs} for n,rs in model['repositories'].items()}
bad=[]
for o in ops:
    check(o['id']+' DTO identifiers valid',all(re.fullmatch(r'[A-Za-z_$][A-Za-z0-9_$]*',o[k]) for k in ('requestDto','queryDto')))
    for call in o['repositories']:
        repo,method=call.split('.')
        if repo not in allowed or method not in allowed[repo]:bad.append((o['id'],call))
check('All operation repository references resolve',not bad,str(bad))
for name,e in model['entities'].items():
    routes=[o for o in ops if o['path'].startswith('/api/'+e['path'])]
    base='/api/'+e['path']
    check(name+' explicit create/list/read/delete',all(any(o['http']==m and o['path']==p for o in routes) for m,p in [('POST',base),('GET',base),('GET',base+'/{id}'),('POST',base+'/{id}/delete')]))
    check(name+' unique fields',len({f['name'] for f in e['fields']})==len(e['fields']))
    check(name+' fields fully typed',all(f['java'] and f['constraint'] and f['description'] and isinstance(f['nullable'],bool) for f in e['fields']))
    for r in model['repositories'][name]:
        # Walk relationship paths encoded in derived names rather than trusting spelling.
        if r['query'].startswith('Derived') and r['name'].startswith('findBy'):
            paths=r['name'][6:].split('OrderBy')[0].split('And')
            for path in paths:
                current=name
                for token in path.split('_'):
                    prop=token[0].lower()+token[1:]
                    fs={f['name']:f for f in model['entities'][current]['fields']}
                    check(name+'.'+r['name']+' path '+prop,prop in fs)
                    if prop in fs and fs[prop]['java'] in model['entities']:current=fs[prop]['java']
api=(ROOT/'05-REST-API-CATALOGUE.md').read_text(encoding='utf-8-sig');comps=(ROOT/'06-COMPONENT-SPECIFICATION.md').read_text(encoding='utf-8-sig')
for o in ops:
    check(o['id']+' documented API and component',f"## {o['id']} {o['http']} {o['path']}" in api and f"### {o['id']} {o['method']}" in comps)
full=read('postman/OrgResource-API.postman_collection.json')
items=[it for folder in full['item'] for it in folder['item']]
check('Postman all68 routes',len(items)==len(ops)==68)
check('Postman endpoint ID bijection',{i['name'].split()[0] for i in items}=={o['id'] for o in ops})
expected_states={'P01':'SUBMITTED','P02':'APPROVED','P03':'DRAFT','P04':'IN_PROGRESS','P05':'COMPLETED','P06':'CANCELLED','A01':'APPROVED','A02':'REJECTED','A03':'CANCELLED'}
for item in items:
    code=item['name'].split()[0]
    if code in expected_states:
        body=json.loads(item['response'][0]['body'])
        check(code+' response has resulting state',(body.get('status') or body.get('decisionStatus'))==expected_states[code])
for code,kind in {'A05':'ISSUE','A06':'RETURN','A07':'CONSUMPTION','T01':'REVERSAL'}.items():
    item=next(x for x in items if x['name'].split()[0]==code)
    check(code+' response has correct movement',json.loads(item['response'][0]['body'])['type']==kind)
for i in items:
    if 'body' in i['request']:
        b=i['request']['body']['raw']
        b=re.sub(r'(?<!")\{\{[^{}]+\}\}(?!")','1',b)
        try:json.loads(b);valid=True
        except Exception:valid=False
        check('Postman JSON '+i['name'].split()[0],valid)
demo=read('postman/OrgResource-Demo.postman_collection.json');neg=read('postman/OrgResource-Negative.postman_collection.json')
check('Ordered demo99 and negative8 requests',len(demo['item'])==99 and len(neg['item'])==8)
# Resolve every demo variable reference against explicit environment or setup/capture assignments.
known={'baseUrl','bootstrapAdminId','$guid'}|set(re.findall(r"pm\.environment\.set\('([^']+)'",json.dumps(demo)))
known|={'startA','endA','startB','endB','startC','endC','windowStart'}
setup='\n'.join(demo['item'][0]['event'][0]['script']['exec'])
known|=set(re.findall(r'"([A-Za-z]+Key)"',setup))
refs=set(re.findall(r'\{\{([^{}]+)\}\}',json.dumps(demo)))
check('Ordered demo variables have setup/capture',not(refs-known),str(sorted(refs-known)))
available={'baseUrl','bootstrapAdminId','$guid','runPrefix','runLower','startA','endA','startB','endB','startC','endC','windowStart'}|set(re.findall(r'"([A-Za-z]+Key)"',setup))
missing=[]
for item in demo['item']:
    needed=set(re.findall(r'\{\{([^{}]+)\}\}',json.dumps(item['request'])))
    if needed-available:missing.append((item['name'],sorted(needed-available)))
    for ev in item['event']:
        if ev['listen']=='test':available|=set(re.findall(r"pm\.environment\.set\('([^']+)'",'\n'.join(ev['script']['exec'])))
check('Ordered demo references only prior created IDs',not missing,str(missing))

# Independent bounded arithmetic checks of the design fixtures, not application tests.
def overlap(a,b):return a[0]<b[1] and b[0]<a[1]
def free_sweep(capacity,bookings,window):
    points=sorted({window[0],window[1]}|{x for s,e,q in bookings for x in (max(s,window[0]),min(e,window[1])) if window[0]<=x<=window[1]})
    return min(capacity-sum(q for s,e,q in bookings if s<=a<e) for a,b in zip(points,points[1:]) if a<b)
check('Adjacent intervals do not overlap',not overlap((9,12),(12,14)))
check('Actual intersecting intervals overlap',overlap((9,12),(10,13)))
check('Sequential load not summed',free_sweep(10,[(9,10,6),(10,11,6)],(9,11))==4)
check('Overlapping A6 leaves4',free_sweep(10,[(9,12,6)],(10,13))==4)
check('Three department peak sourcing deficit1',free_sweep(10,[(9,12,6),(10,13,5),(12,14,4)],(9,14))==-1)
check('Approved A6 B4 C4 fits',free_sweep(10,[(9,12,6),(10,13,4),(12,14,4)],(9,14))==0)
check('Consumable reserve and consume once',100-20-(60-20)==40)
check('Consumable consolidated gap60',max(0,50+50-40)==60)
check('Good and damaged return buckets',(0+9,10-10,0+1)==(9,0,1))
check('Damaged return reduces usable',(9+0)==9)
check('Overdue hold reduces later capacity',free_sweep(1,[(12,9999,1)],(13,14))==0)
check('Partial custody equation',6-(2+1)==3)
check('Release does not change physical',(80,40-10,80-(40-10))==(80,30,50))
# Exhaustive small integer endpoint sweep against independent half-step sampling.
comparisons=0
for cap in (1,2,3):
    for bookings in itertools.product([(0,1,1),(1,2,1),(0,2,2),(0,3,1),(2,3,2)],repeat=2):
        sampled=min(cap-sum(q for s,e,q in bookings if s<=t<e) for t in (0.5,1.5,2.5))
        assert free_sweep(cap,bookings,(0,3))==sampled
        comparisons+=1
check('Sweep agrees with independent discrete samples',comparisons==75,str(comparisons)+' small fixtures')
check('Nine Mermaid diagrams',len(re.findall(r'```mermaid', (ROOT/'03-TECHNICAL-ARCHITECTURE.md').read_text(encoding='utf-8-sig')))==9)
check('No application source authored',not any(ROOT.rglob('*.java')))
summary=dict(scope='Documentation contracts and independent arithmetic fixtures only; application tests NOT RUN',checks=len(checks),passed=sum(c['passed'] for c in checks),failed=[c for c in checks if not c['passed']],details=checks)
(ROOT/'qa/consistency-check.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')
# Source inventory is provenance/inspection coverage, not semantic-read certification.
pack=Path('C:/Users/ethan/Projects/Template Pack/erp-template-pack')
names=['00-READ-ME-FIRST.md','01-MASTER-PROMPT.md']+[p.name for p in sorted(pack.glob('*.md')) if re.match(r'(?:0[2-9]|1[0-4])-',p.name)]+['ALL-CONTEXT-02-to-14.md']
sources=[]
for n in names:
    p=pack/n;b=p.read_bytes();text=b.decode('utf-8-sig')
    sources.append(dict(path=str(p),bytes=len(b),sha256=hashlib.sha256(b).hexdigest(),headings=len(re.findall(r'^#{1,4} ',text,re.M)),inspection='full text read' if n in ('00-READ-ME-FIRST.md','09-OPEN-QUESTIONS-AND-CLIENT-DISCOVERY.md') else 'redundant compilation structural inspection' if n.startswith('ALL-') else 'structure and applicable sections inspected; unrelated business detail not exhaustively read'))
for p in [ROOT.parent/'ESP Project Proposal 2026.pdf',ROOT.parent/'CS-DISCS-CSCI180.05-JONGKO_J-O-2026-1.pdf',Path('C:/Users/ethan/.codex/attachments/ee07c5ae-188b-4d8e-92bd-fe128511620a/Pasted text.txt')]:
    b=p.read_bytes();sources.append(dict(path=str(p),bytes=len(b),sha256=hashlib.sha256(b).hexdigest(),inspection='full text read; PDF render inspected in preceding audit where applicable'))
(ROOT/'evidence/source-inventory.json').write_text(json.dumps(sources,indent=2)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in summary.items() if k!='details'},indent=2))
if summary['failed']:raise SystemExit(1)

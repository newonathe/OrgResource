from pathlib import Path
import json,re,hashlib
from docx import Document
ROOT=Path(__file__).resolve().parents[1]
model=json.loads((ROOT/'contracts/domain-model.json').read_text())
ops=json.loads((ROOT/'contracts/operations.json').read_text())
md=(ROOT/'OrgResource-Concise-Submission-Proposal.md').read_text()
doc=Document(ROOT/'OrgResource-Concise-Submission-Proposal.docx')
plain='\n'.join(p.text for p in doc.paragraphs)+'\n'+'\n'.join(c.text for t in doc.tables for r in t.rows for c in r.cells)
checks=[]
def check(n,ok):checks.append(dict(check=n,passed=bool(ok)))
check('Part I sections only',[s for s in re.findall(r'^## (.+)$',md,re.M)]==['Group members','Application description','Controller operations','Components and method steps','Entities and repositories'])
for o in ops:
    check(o['id']+' URL and operation included',o['path']in plain and re.search(r'\b'+o['id']+r'\b',plain))
    check(o['id']+' forwarded method included',o['component']in plain and o['method']in plain)
    if o['body'] is not None:check(o['id']+' request body label included',bool(re.search(r'^Bodies [^\n]*\b'+o['id']+r'\b[^\n]*: \{',md,re.M)))
for name,e in model['entities'].items():
    section=md.split('### '+name+'\n\n')[-1].split('\n\n### ')[0]
    check(name+' purpose and repository included',name+'Repository'in section)
    for f in e['fields']:
        check(name+'.'+f['name']+' field included',f['name']in section or f['name']in ('id','createdAt','createdBy','version','updatedAt','updatedBy'))
    for method in model['repositories'][name]:check(name+'.'+method['name']+' parameters and purpose included',method['name']+'('+method['params']+'):'in section)
banned=['The GUIDON','ateneomea','## References','## Sources','Template Pack','CLO','syllabus','Canvas','compliance checklist','@Entity','PESSIMISTIC_WRITE','Op01CRequest']
check('No citations or unnecessary source/framework sections',not any(s in plain or s in md for s in banned))
check('No external citation links',not re.search(r'https?://(?!127\.0\.0\.1)',md))
check('No incorrect field units','characters-day'not in md and 'sending attempts' in md)
check('Ordered component steps included','1. 'in plain and '2. 'in plain and '3. 'in plain and '@PostConstruct'in plain)
previous=json.loads((ROOT/'DELIVERY-MANIFEST.json').read_text())
expected=next(x['sha256']for x in previous if x['path']=='OrgResource-Formal-Proposal.docx')
check('Full specification unchanged',hashlib.sha256((ROOT/'OrgResource-Formal-Proposal.docx').read_bytes()).hexdigest()==expected)
result=dict(scope='Part I document coverage and requested removals; not application testing',checks=len(checks),passed=sum(c['passed']for c in checks),failed=[c for c in checks if not c['passed']])
(ROOT/'qa/part1-check.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
if result['failed']:raise SystemExit(1)

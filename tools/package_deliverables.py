from pathlib import Path
import json,hashlib,shutil,zipfile,re
from docx import Document
from pypdf import PdfReader
ROOT=Path(__file__).resolve().parents[1]
sources=ROOT/'sources';sources.mkdir(exist_ok=True)
for src,dest in [(ROOT.parent/'ESP Project Proposal 2026.pdf',sources/'ESP Project Proposal 2026.pdf'),(ROOT.parent/'CS-DISCS-CSCI180.05-JONGKO_J-O-2026-1.pdf',sources/'CS-DISCS-CSCI180.05-JONGKO_J-O-2026-1.pdf'),(Path('C:/Users/ethan/.codex/attachments/ee07c5ae-188b-4d8e-92bd-fe128511620a/Pasted text.txt'),sources/'OrgResource-User-Master-Prompt.txt')]:
    shutil.copy2(src,dest)
    assert hashlib.sha256(src.read_bytes()).digest()==hashlib.sha256(dest.read_bytes()).digest()
ops=json.loads((ROOT/'contracts/operations.json').read_text())
model=json.loads((ROOT/'contracts/domain-model.json').read_text())
checks=json.loads((ROOT/'qa/consistency-check.json').read_text())
rows=[]
for name in ['OrgResource-Formal-Proposal.docx','OrgResource-Concise-Submission-Proposal.docx']:
    p=ROOT/name;d=Document(p)
    text='\n'.join(x.text for x in d.paragraphs)+'\n'+'\n'.join(c.text for t in d.tables for r in t.rows for c in r.cells)
    missing=[o['id'] for o in ops if not re.search(r'\b'+o['id']+r'\b',text)]
    assert not missing,(name,missing)
    assert all(n in text for n in model['entities'])
    title_border=d.styles['Title'].element.xpath('./w:pPr/w:pBdr')
    assert not title_border
    pdf=PdfReader(ROOT/('qa/'+p.stem+'.pdf'))
    rows.append(dict(file=name,bytes=p.stat().st_size,pages=len(pdf.pages),operationsCovered=len(ops),entitiesCovered=len(model['entities']),titleBorder=False))
concise=(ROOT/'OrgResource-Concise-Submission-Proposal.md').read_text()
order=['## Group members','## Application description','## Controller operations','## Components and method steps','## Entities and repositories']
assert [concise.index(s) for s in order]==sorted(concise.index(s) for s in order)
report=dict(documents=rows,assignmentPartISectionOrder=True,documentationChecks=dict(passed=checks['passed'],checks=checks['checks']),applicationImplemented=False,applicationTestsRun=False,liveSmsSent=False,scope='Final artifact structure, coverage, ZIP integrity and documentation/fixture checks only')
(ROOT/'qa/delivery-check.json').write_text(json.dumps(report,indent=2)+'\n')
files=[]
for p in ROOT.iterdir():
    if p.suffix.lower() in ('.md','.docx'):files.append(p)
for folder in ('contracts','postman','sources','tools'):
    files.extend(p for p in (ROOT/folder).rglob('*') if p.is_file() and '__pycache__' not in p.parts)
files.extend(p for p in (ROOT/'qa').glob('*.json'))
files.append(ROOT/'qa/QUALITY-REVIEW.md')
# Include static diagram images needed to rebuild Word, not rendering intermediates.
files.extend((ROOT/'qa').glob('diagram-*.png'))
manifest=[dict(path=p.relative_to(ROOT).as_posix(),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in sorted(set(files))]
(ROOT/'DELIVERY-MANIFEST.json').write_text(json.dumps(manifest,indent=2)+'\n')
files.append(ROOT/'DELIVERY-MANIFEST.json')
target=ROOT/'OrgResource-Specification-Package.zip'
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
    for p in sorted(set(files)):z.write(p,'OrgResource/'+p.relative_to(ROOT).as_posix())
with zipfile.ZipFile(target) as z:
    assert z.testzip() is None
    for m in manifest:
        assert hashlib.sha256(z.read('OrgResource/'+m['path'])).hexdigest()==m['sha256']
print(json.dumps(dict(report=report,zipBytes=target.stat().st_size,zipFiles=len(set(files))),indent=2))

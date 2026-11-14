from pathlib import Path
from docx import Document
import json, re, hashlib

root = Path(__file__).resolve().parents[1]
md = (root/'OrgResource-Concise-Submission-Proposal.md').read_text(encoding='utf-8')
doc = Document(root/'OrgResource-Concise-Submission-Proposal.docx')
baseline = json.loads((root/'qa/readable-revision-baseline.json').read_text())
checks = []
def check(label, value):
    checks.append({'check': label, 'passed': bool(value)})

check('Exactly three requested technical sections', re.findall(r'^## (.+)$', md, re.M) == ['Controller operations', 'Components and important methods', 'Entities and repositories'])
check('Only named member and student ID', 'Ethan Owen Taruc — 236196' in md and '[Full name' not in md and '[Group members]' not in md)
check('Word author', doc.core_properties.author == 'Ethan Owen Taruc')
blocks = re.findall(r'^#### (.+?)\n\n(.*?)(?=^#### |^### |^## |\Z)', md, re.M | re.S)
check('25 operations', len(blocks) == 25)
sections = md.split('## Components and important methods\n', 1)[1].split('## Entities and repositories\n', 1)[0]
for title, body in blocks:
    check(title+' complete operation description', all('**'+label+':**' in body for label in ['URL','Input','Output','Forward to','Errors']))
    match = re.search(r'\*\*Forward to:\*\* (\w+)\.(.+)\.', body)
    check(title+' component and method steps exist', match and '### '+match[1] in sections and '**'+match[2]+'**' in sections)
json_blocks = re.findall(r'```json\n(.*?)\n```', md, re.S)
for i, block in enumerate(json_blocks):
    value = json.loads(block)
    check('JSON '+str(i+1)+' is valid and indented', isinstance(value, dict) and '\n  "' in block)
check('JSON formats for all five entities', len(json_blocks) == 8)
check('Five entity field tables', len(doc.tables) == 5 and all([c.text for c in t.rows[0].cells] == ['Field','Type','NULL','Validation'] for t in doc.tables))
for name in ['Department','Project','Resource','ResourceRequirement','Allocation']:
    check(name+' purpose and repository', '### '+name+'\n' in md and name+'Repository' in md)
check('Shared ID type and null rule', 'id of type Long' in md and 'never NULL' in md)
check('Reminder schedule and initialization included', '@PostConstruct' in md and 'scheduled every hour' in md and 'sendReminder(phoneNumber, message)' in md)
check('Implementation not claimed', 'implementation and testing are still to be done' in md)
check('No citations or research sections', not re.search(r'https?://(?!localhost)|\]\(|^## (References|Sources|Research)', md, re.M))
check('No raw markdown in Word', not any('**' in p.text or '```' in p.text for p in doc.paragraphs))
check('Full document unchanged', hashlib.sha256((root/'OrgResource-Formal-Proposal.docx').read_bytes()).hexdigest() == baseline['fullDocumentSha256'])
result = {'scope':'Current simplified proposal coverage and JSON formatting only; no application execution', 'operations':25,'entities':5,'checks':len(checks),'passed':sum(c['passed'] for c in checks),'failed':[c for c in checks if not c['passed']]}
(root/'qa/readable-part1-check.json').write_text(json.dumps(result,indent=2))
print(json.dumps(result, indent=2))
if result['failed']: raise SystemExit(1)

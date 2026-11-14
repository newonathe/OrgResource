from pathlib import Path
import re,json
from docx import Document
from docx.shared import Inches,Pt,RGBColor
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT,WD_CELL_VERTICAL_ALIGNMENT
ROOT=Path(__file__).resolve().parents[1]
doc=Document();sec=doc.sections[0]
sec.page_width=Inches(8.5);sec.page_height=Inches(11)
sec.top_margin=sec.bottom_margin=Inches(.7)
sec.left_margin=sec.right_margin=Inches(.8)
for name,size in [('Normal',11),('Title',23),('Subtitle',12),('Heading 1',14),('Heading 2',12),('Heading 3',11)]:
    st=doc.styles[name];st.font.name='Calibri';st.font.size=Pt(size);st.font.color.rgb=RGBColor(0,0,0)
    st.paragraph_format.space_after=Pt(5)
    for border in list(st.element.xpath('./w:pPr/w:pBdr')):border.getparent().remove(border)
    if name.startswith('Heading'):st.paragraph_format.space_before=Pt(10);st.paragraph_format.keep_with_next=True
doc.styles['Normal'].paragraph_format.line_spacing=1.02
doc.core_properties.title='OrgResource Concise Submission Proposal'
doc.core_properties.author='Ethan Owen Taruc'
footer=sec.footer.paragraphs[0];footer.alignment=WD_ALIGN_PARAGRAPH.CENTER
footer.add_run('OrgResource concise proposal  |  ')
field=OxmlElement('w:fldSimple');field.set(qn('w:instr'),'PAGE');footer._p.append(field)
for run in footer.runs:run.font.size=Pt(9)
lines=(ROOT/'OrgResource-Concise-Submission-Proposal.md').read_text(encoding='utf-8-sig').splitlines();i=0
def table(block):
    rows=[[x.strip() for x in l.strip('|').split('|')] for l in block]
    rows=[r for r in rows if not all(re.fullmatch(r'[:\- ]+',x or '-') for x in r)]
    if not rows:return
    cols=len(rows[0]);head=rows[0][0]
    widths=[1.25,5.65] if cols==2 and head=='Method' else [1.1,5.8] if cols==2 and head=='Role' else [2.25,4.65] if cols==2 else [1.5,1.75,.5,3.15] if head=='Field' else [2.0,1.0,1.2,2.7] if cols==4 else [6.9/cols]*cols
    tb=doc.add_table(rows=1,cols=cols);tb.autofit=False;tb.alignment=WD_TABLE_ALIGNMENT.CENTER
    for col,w in zip(tb.columns,widths):col.width=Inches(w)
    for r,row in enumerate(rows):
        cells=tb.rows[0].cells if r==0 else tb.add_row().cells
        tr=tb.rows[r]._tr.get_or_add_trPr();tr.append(OxmlElement('w:cantSplit'))
        if r==0:tr.append(OxmlElement('w:tblHeader'))
        for k,(cell,txt) in enumerate(zip(cells,row)):
            cell.width=Inches(widths[k]);cell.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER;cell.text=txt
            pr=cell._tc.get_or_add_tcPr();bd=OxmlElement('w:tcBorders')
            for side in ('top','left','bottom','right'):
                x=OxmlElement('w:'+side);x.set(qn('w:val'),'single');x.set(qn('w:sz'),'4');x.set(qn('w:color'),'D9D9D9');bd.append(x)
            pr.append(bd)
            pad=OxmlElement('w:tcMar')
            for side in ('top','left','bottom','right'):
                x=OxmlElement('w:'+side);x.set(qn('w:w'),'65');x.set(qn('w:type'),'dxa');pad.append(x)
            pr.append(pad)
            if r==0:
                bg=OxmlElement('w:shd');bg.set(qn('w:fill'),'E8EDF3');pr.append(bg)
            for p in cell.paragraphs:
                p.paragraph_format.space_after=Pt(1);p.paragraph_format.line_spacing=1
                if r==0 or (len(rows)<=5 and r<len(rows)-1):p.paragraph_format.keep_with_next=True
                for run in p.runs:run.font.size=Pt(10.5);run.bold=r==0
    doc.add_paragraph().paragraph_format.space_after=Pt(1)
def inline(p,s):
    for j,chunk in enumerate(re.split(r'\*\*(.*?)\*\*',s)):
        run=p.add_run(chunk);run.bold=j%2==1

while i<len(lines):
    s=lines[i].strip()
    if not s:i+=1;continue
    if s.startswith('```'):
        i+=1;block=[]
        while i<len(lines) and not lines[i].strip().startswith('```'):
            block.append(lines[i]);i+=1
        for j,line in enumerate(block):
            para=doc.add_paragraph()
            para.paragraph_format.left_indent=Inches(.18)
            para.paragraph_format.space_after=Pt(0 if j<len(block)-1 else 6)
            para.paragraph_format.line_spacing=1
            para.paragraph_format.keep_with_next=j<len(block)-1
            run=para.add_run(line);run.font.name='Consolas';run.font.size=Pt(10.5)
        i+=1;continue
    if s.startswith('|'):
        block=[]
        while i<len(lines) and lines[i].strip().startswith('|'):block.append(lines[i].strip());i+=1
        table(block);continue
    if s.startswith('#'):
        level=len(s)-len(s.lstrip('#'));title=s[level:].strip()
        if level==1:doc.add_paragraph(title,'Title')
        else:doc.add_heading(re.sub(r'[^\w\s]',' ',title),level=min(3,level-1))
    elif s=='Student Organization Resource Planning and Allocation System':doc.add_paragraph(s,'Subtitle')
    else:
        p=doc.add_paragraph();inline(p,s)
        if s.startswith(('**URL:**','**Input:**','**Output:**','**Forward to:**','**Errors:**')):
            p.paragraph_format.space_after=Pt(2)
            p.paragraph_format.keep_with_next=not s.startswith('**Errors:**')
        elif s.startswith('**') and s.endswith('**'):
            p.paragraph_format.keep_with_next=True
            p.paragraph_format.space_after=Pt(3)
        elif re.match(r'^\d+\. ',s):
            p.paragraph_format.space_after=Pt(2)
            following=next((line.strip() for line in lines[i+1:] if line.strip()),'')
            p.paragraph_format.keep_with_next=bool(re.match(r'^\d+\. ',following))
        if s.startswith('{') or ((s.startswith('Op') or s.startswith('Bodies ')) and ': {' in s):
            for run in p.runs:run.font.name='Consolas';run.font.size=Pt(9.5)
    i+=1
out=ROOT/'OrgResource-Concise-Submission-Proposal.docx';doc.save(out)
print(json.dumps(dict(path=str(out),bytes=out.stat().st_size,tables=len(doc.tables),paragraphs=len(doc.paragraphs))))

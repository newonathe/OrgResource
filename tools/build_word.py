"""Compile the reviewed prose and contracts into the requested Word proposal."""
from pathlib import Path
import re,json
from docx import Document
from docx.shared import Inches,Pt,RGBColor
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.table import WD_TABLE_ALIGNMENT,WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
doc=Document();sec=doc.sections[0]
sec.page_width=Inches(8.5);sec.page_height=Inches(11)
sec.top_margin=Inches(.75);sec.bottom_margin=Inches(.75)
sec.left_margin=Inches(.8);sec.right_margin=Inches(.8)
styles=doc.styles
for s in ('Normal','Title','Subtitle','Heading 1','Heading 2','Heading 3','Heading 4'):
    styles[s].font.name='Calibri';styles[s].font.color.rgb=RGBColor(0,0,0)
    for border in list(styles[s].element.xpath('./w:pPr/w:pBdr')):border.getparent().remove(border)
    for fontel in styles[s].element.xpath('./w:rPr/w:rFonts'):
        for attr in ('asciiTheme','hAnsiTheme','eastAsiaTheme','cstheme'):
            if qn('w:'+attr) in fontel.attrib:del fontel.attrib[qn('w:'+attr)]
styles['Normal'].font.size=Pt(11)
styles['Normal'].paragraph_format.space_after=Pt(6)
styles['Normal'].paragraph_format.line_spacing=1.05
styles['Title'].font.size=Pt(24)
styles['Subtitle'].font.size=Pt(13)
for n,size in [('Heading 1',17),('Heading 2',13),('Heading 3',11.5),('Heading 4',11)]:
    styles[n].font.size=Pt(size);styles[n].paragraph_format.space_before=Pt(12);styles[n].paragraph_format.space_after=Pt(6)
    styles[n].paragraph_format.keep_with_next=True
if 'Contract Code' not in styles:
    from docx.enum.style import WD_STYLE_TYPE
    cs=styles.add_style('Contract Code',WD_STYLE_TYPE.PARAGRAPH)
else:cs=styles['Contract Code']
cs.font.name='Consolas';cs.font.size=Pt(9)
cs.paragraph_format.line_spacing=1;cs.paragraph_format.space_after=Pt(6)
cs.paragraph_format.keep_together=False
doc.core_properties.title='OrgResource Project Proposal and Complete Specification'
doc.core_properties.subject='CSCI 180.05 academic project proposal'
doc.core_properties.author='[Group members]'
doc.core_properties.keywords='OrgResource Jersey Spring JPA resource planning'
doc.add_paragraph('OrgResource Project Proposal',style='Title')
doc.add_paragraph('Student Organization Resource Planning and Allocation System',style='Subtitle')
doc.add_paragraph('CSCI 180.05  |  Specification version 1.0  |  Prepared 3 November 2026')

# Long technical document needs navigable page numbers, not decorative headers.
fp=sec.footer.paragraphs[0];fp.alignment=WD_ALIGN_PARAGRAPH.CENTER
fp.add_run('OrgResource  |  ')
fld=OxmlElement('w:fldSimple');fld.set(qn('w:instr'),'PAGE');fp._p.append(fld)
for r in fp.runs:r.font.size=Pt(9)

def cleanhead(t):
    t=re.sub(r'[`*_]','',t)
    return re.sub(r'[^\w\s]',' ',t).strip()
def paragraph(t,style=None):
    # Preserve exact API strings and ASCII arithmetic, remove Markdown inline delimiters.
    return doc.add_paragraph(t.replace('**','').replace('`',''),style)
def table(lines):
    rows=[[x.strip() for x in l.strip().strip('|').split('|')] for l in lines]
    rows=[r for r in rows if not all(re.fullmatch(r'[:\- ]+',c or '-') for c in r)]
    cols=len(rows[0]);tb=doc.add_table(rows=1,cols=cols);tb.alignment=WD_TABLE_ALIGNMENT.CENTER;tb.autofit=False
    widths={6:[.95,.9,.75,.4,2.0,1.9],4:[.45,2.25,1.75,2.45],3:[.55,3.1,3.25]}.get(cols,[6.9/cols]*cols)
    for c,w in zip(tb.columns,widths):c.width=Inches(w)
    for j,row in enumerate(rows):
        cells=tb.rows[0].cells if j==0 else tb.add_row().cells
        for k,(cell,txt) in enumerate(zip(cells,row)):
            cell.width=Inches(widths[k]);cell.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER
            cell.text=txt.replace('`','').replace('**','')
            tcPr=cell._tc.get_or_add_tcPr()
            shd=OxmlElement('w:shd');shd.set(qn('w:fill'),'D9E2F3' if j==0 else 'FFFFFF');tcPr.append(shd)
            mar=OxmlElement('w:tcMar')
            for edge in ('top','left','bottom','right'):
                el=OxmlElement('w:'+edge);el.set(qn('w:w'),'70');el.set(qn('w:type'),'dxa');mar.append(el)
            tcPr.append(mar)
            borders=OxmlElement('w:tcBorders')
            for edge in ('top','left','bottom','right'):
                el=OxmlElement('w:'+edge);el.set(qn('w:val'),'single');el.set(qn('w:sz'),'4');el.set(qn('w:color'),'D9D9D9');borders.append(el)
            tcPr.append(borders)
            for p in cell.paragraphs:
                p.paragraph_format.space_after=Pt(2);p.paragraph_format.line_spacing=1.02
                for run in p.runs:run.font.size=Pt(9.5);run.bold=j==0
        trPr=tb.rows[j]._tr.get_or_add_trPr()
        trPr.append(OxmlElement('w:cantSplit'))
        if j==0:trPr.append(OxmlElement('w:tblHeader'))
    doc.add_paragraph()

diagram_index=0
ops=json.loads((ROOT/'contracts/operations.json').read_text())
by_id={o['id']:o for o in ops}
def append_md(filename,front=False):
    global diagram_index
    lines=(ROOT/filename).read_text(encoding='utf-8-sig').splitlines();i=0;firstheading=True
    while i<len(lines):
        line=lines[i].strip()
        if not line:i+=1;continue
        if line.startswith('```'):
            lang=line[3:].strip();block=[];i+=1
            while i<len(lines) and not lines[i].strip().startswith('```'):block.append(lines[i]);i+=1
            if lang=='mermaid':
                diagram_index+=1;p=ROOT/f'qa/diagram-{diagram_index}.png'
                with Image.open(p) as im:w,h=im.size
                width=min(6.9,7.7*w/h)
                pic=doc.add_paragraph();pic.alignment=WD_ALIGN_PARAGRAPH.CENTER
                pic.add_run().add_picture(str(p),width=Inches(width))
                # Add plain text alt label for document accessibility.
                drawing=pic._p.xpath('.//wp:docPr')
                if drawing:drawing[0].set('descr',f'Architecture diagram {diagram_index}; editable Mermaid source in {filename}')
            else:
                for b in block:paragraph(b,'Contract Code')
            i+=1;continue
        if line.startswith('|'):
            block=[]
            while i<len(lines) and lines[i].strip().startswith('|'):block.append(lines[i]);i+=1
            table(block);continue
        if line.startswith('#'):
            level=len(line)-len(line.lstrip('#'));title=line[level:].strip()
            if front and title=='OrgResource Project Proposal':
                doc.add_heading('Group and submission details',level=1);i+=1;continue
            # Reserve Heading1 for chapters, not every API entry.
            wl=1 if level==1 and not front else min(4,level if not front else level+1)
            chapter_start=firstheading and not front
            if chapter_start:wl=1
            m=re.match(r'(\w+) (GET|POST) (/api/.*)',title)
            if m and m[1] in by_id:
                o=by_id[m[1]];title=o['id']+' '+o['domain']+' '+re.sub(r'([a-z])([A-Z])',r'\1 \2',o['method'])
                doc.add_heading(cleanhead(title),level=2)
                paragraph(m[2]+' '+m[3],'Contract Code')
            else:
                heading=doc.add_heading(cleanhead(title),level=wl)
                if chapter_start:heading.paragraph_format.page_break_before=True
            firstheading=False;i+=1;continue
        if line.startswith('{') and line.endswith('}'):
            try:
                obj=json.loads(line)
                # JSON remains editable, use compact readable wrapped paragraphs.
                text=json.dumps(obj,ensure_ascii=False,separators=(', ',': '))
            except Exception:text=line
            paragraph(text,'Contract Code');i+=1;continue
        if line.startswith('- '):paragraph(line[2:],'List Bullet')
        elif re.match(r'^\d+\. ',line):paragraph(line)
        else:
            iscode=line.startswith(('Request JSON','Success:','Example failure:','Signature:'))
            paragraph(line,'Contract Code' if iscode else None)
        i+=1

append_md('09-FORMAL-PROPOSAL-DRAFT.md',front=True)
doc.add_page_break();doc.add_heading('Technical appendix contents',level=1)
p=doc.add_paragraph();f=OxmlElement('w:fldSimple');f.set(qn('w:instr'),'TOC \\o "1-1" \\h \\z \\u');p._p.append(f)
paragraph('The following appendices form part of this proposal. They contain the complete controller catalogue, component steps, entity and repository specifications, diagrams, integration rules, test plan and handoff. The companion Markdown and JSON files preserve editable source contracts.')
for file in ['01-SOURCE-AND-REQUIREMENTS.md','02-COMPLETE-PROJECT-SPECIFICATION.md','03-TECHNICAL-ARCHITECTURE.md','04-DATABASE-SPECIFICATION.md','05-REST-API-CATALOGUE.md','06-COMPONENT-SPECIFICATION.md','07-INTEGRATION-AND-SCHEDULING.md','08-POSTMAN-TESTING-AND-DEMONSTRATION.md','10-IMPLEMENTATION-HANDOFF.md']:
    append_md(file)
out=ROOT/'OrgResource-Formal-Proposal.docx'
doc.save(out)
print(json.dumps(dict(path=str(out),bytes=out.stat().st_size,paragraphs=len(doc.paragraphs),tables=len(doc.tables),diagrams=diagram_index)))

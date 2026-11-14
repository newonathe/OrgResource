from pathlib import Path
import json,sys
import pypdfium2 as pdfium
from PIL import Image,ImageOps,ImageDraw
from pypdf import PdfReader
ROOT=Path(__file__).resolve().parents[1]
stem=sys.argv[1] if len(sys.argv)>1 else 'OrgResource-Formal-Proposal'
path=ROOT/('qa/'+stem+'.pdf')
pdf=pdfium.PdfDocument(path)
dest=ROOT/('qa/concise-pages' if 'Concise' in stem else 'qa/pages');dest.mkdir(exist_ok=True)
sizes=[]
for i in range(len(pdf)):
    page=pdf[i]
    img=page.render(scale=1.5).to_pil()
    img.save(dest/f'page-{i+1:03d}.png')
    sizes.append(dict(page=i+1,width=img.width,height=img.height))
    page.close()
pdf.close()
reader=PdfReader(path)
texts=[p.extract_text() or '' for p in reader.pages]
blank=[i+1 for i,(p,t) in enumerate(zip(reader.pages,texts)) if len(t.split())<5 and len(p.images)==0]
report=dict(pages=len(sizes),wordCounts=[len(t.split()) for t in texts],blankPages=blank,pageSizes=sizes,renderMethod='Microsoft Word native PDF export, bundled pypdfium2 rasterization; packaged LibreOffice renderer unavailable on this Windows host')
(ROOT/('qa/concise-layout-check.json' if 'Concise' in stem else 'qa/layout-check.json')).write_text(json.dumps(report,indent=2)+'\n')
# Overview sheets for all-page navigation; full-resolution page PNGs remain available.
for start in range(0,len(sizes),16):
    sheet=Image.new('RGB',(4*330,4*450),'#dddddd');draw=ImageDraw.Draw(sheet)
    for j in range(start,min(start+16,len(sizes))):
        im=Image.open(dest/f'page-{j+1:03d}.png');im.thumbnail((320,420))
        x=(j-start)%4*330+5;y=(j-start)//4*450+25
        sheet.paste(im,(x,y));draw.text((x,y-20),f'Page {j+1}',fill='black')
    prefix='concise-overview' if 'Concise' in stem else 'overview'
    sheet.save(ROOT/f'qa/{prefix}-{start//16+1:02d}.png')
print(json.dumps(dict(pages=len(sizes),blankPages=report['blankPages'],minimumWords=min(report['wordCounts']))))

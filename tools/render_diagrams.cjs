const fs=require('fs');
const path=require('path');
const {chromium}=require('C:/Users/ethan/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const root=path.resolve(__dirname,'..');
(async()=>{
 const source=fs.readFileSync(path.join(root,'03-TECHNICAL-ARCHITECTURE.md'),'utf8');
 const diagrams=[...source.matchAll(/```mermaid\r?\n([\s\S]*?)```/g)].map(m=>m[1]);
 const browser=await chromium.launch({headless:true,args:['--disable-gpu']});
 try{
  const page=await browser.newPage({viewport:{width:1500,height:1500},deviceScaleFactor:1});
  await page.setContent('<html><body style="margin:0;background:white"><div id="diagram" style="padding:24px;display:inline-block;background:white"></div></body></html>');
  await page.addScriptTag({path:path.join(root,'qa','mermaid.min.js')});
  await page.evaluate(()=>mermaid.initialize({startOnLoad:false,theme:'neutral',securityLevel:'strict',fontFamily:'Arial',themeVariables:{fontSize:'22px'},flowchart:{useMaxWidth:false,nodeSpacing:25,rankSpacing:30},sequence:{useMaxWidth:false,wrap:true,actorFontSize:18,messageFontSize:18,noteFontSize:18,actorMargin:25,width:150},er:{useMaxWidth:false,fontSize:20}}));
  const result=[];
  for(let i=0;i<diagrams.length;i++){
   await page.evaluate(async({code,i})=>{document.getElementById('diagram').innerHTML=(await mermaid.render('orgDiagram'+i,code)).svg;},{code:diagrams[i],i});
   const target=page.locator('#diagram');
   await target.screenshot({path:path.join(root,'qa',`diagram-${i+1}.png`)});
   result.push({number:i+1,parsed:true,size:await target.boundingBox()});
  }
  fs.writeFileSync(path.join(root,'qa','diagram-check.json'),JSON.stringify(result,null,2));
  process.stdout.write(JSON.stringify({diagrams:result.length,allParsed:true}));
 }finally{await browser.close();}
})().catch(e=>{process.stderr.write(String(e));process.exitCode=1});

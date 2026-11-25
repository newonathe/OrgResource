// Verification-only in-memory stdout/stderr capture lifecycle. No launch or DB authority.
// Runtime sink inventory, actual logger canaries, process/DB ownership and cleanup remain external gates.
export function makeR031Capture(config) {
  const LIMIT = 8*1024*1024;
  const ids = ["stdout","stderr"];
  const fixed = status => ({status,sinksChecked:0,matches:0});
  const validCanary = v => typeof v==="string" && /^R031-CAPTURE-[0-9a-f]{32}$/.test(v);
  let state="OPEN", exited=false, closed=false, finished=false;
  let streams={};
  const wipe=()=>{for(const s of Object.values(streams)){for(const b of s.chunks)b.fill(0);s.chunks=[];s.size=0;}};
  const reject=()=>{state="REFUSED";wipe();};
  try{
    if (!config || config.expectedSinks?.length!==2 || !ids.every(id=>config.expectedSinks.includes(id))) reject();
    else{
      const markers=ids.flatMap(id=>[config.canaries?.[id]?.start,config.canaries?.[id]?.end]);
      if(!markers.every(validCanary)||new Set(markers).size!==4)reject();
      else for(const id of ids)streams[id]={chunks:[],size:0,ended:false,start:config.canaries[id].start,end:config.canaries[id].end};
    }
  }catch{reject();}
  function data(id,chunk){
    if(state!=="OPEN")return false;
    const s=Object.hasOwn(streams,id)?streams[id]:null;
    if(closed||!s||s.ended||!(chunk instanceof Uint8Array)||chunk.byteLength===0||s.size+chunk.byteLength>LIMIT){reject();return false;}
    s.chunks.push(new Uint8Array(chunk));s.size+=chunk.byteLength;return true;
  }
  function end(id){
    if(state!=="OPEN")return false;
    const s=Object.hasOwn(streams,id)?streams[id]:null;
    if(closed||!s||s.ended){reject();return false;}
    s.ended=true;return true;
  }
  function exit(code,signal){
    if(state!=="OPEN")return false;
    if(exited||closed||code!==0||signal!==null){reject();return false;}
    exited=true;return true;
  }
  function close(code,signal){
    if(state!=="OPEN")return false;
    if(closed||!exited||code!==0||signal!==null||!ids.every(id=>streams[id]?.ended)){reject();return false;}
    closed=true;return true;
  }
  function abort(){if(!finished)reject();}
  function finish(scan,secrets){
    if(finished)return fixed("REFUSED");
    finished=true;
    const buffers=[];
    try{
      if(state!=="OPEN"||!closed||typeof scan!=="function"||!secrets){reject();return fixed("REFUSED");}
      const captures=[];
      for(const id of ids){
        const s=streams[id], bytes=new Uint8Array(s.size);buffers.push(bytes);
        let n=0;for(const b of s.chunks){bytes.set(b,n);n+=b.length;}
        // Check unique ordered ASCII start/end canaries; cross-chunk matches are included.
        const locate=(marker)=>{
          const p=Uint8Array.from(marker,c=>c.charCodeAt(0));
          let first=-1,count=0;
          for(let i=bytes.indexOf(p[0]);i>=0&&i<=bytes.length-p.length;i=bytes.indexOf(p[0],i+1)){
            let j=1;while(j<p.length&&bytes[i+j]===p[j])j++;
            if(j===p.length){if(first===-1)first=i;count++;}
          }
          return {first,count};
        };
        const a=locate(s.start),b=locate(s.end);
        if(a.count!==1||b.count!==1||a.first+s.start.length>b.first){reject();return fixed("REFUSED");}
        captures.push({id,bytes,complete:true,canary:s.start});
      }
      // No caller-supplied capture/sink override; return only strict scanner receipt.
      const r=scan({oldCodes:secrets.oldCodes,newCodes:secrets.newCodes,validPassword:secrets.validPassword,invalidPassword:secrets.invalidPassword,expectedSinks:ids.slice(),captures});
      if(r?.status==="NO_MATCH"&&r.sinksChecked===2&&r.matches===0)return fixed("NO_MATCH").status==="NO_MATCH"?{status:"NO_MATCH",sinksChecked:2,matches:0}:fixed("REFUSED");
      if(r?.status==="MATCH"&&r.sinksChecked===2&&Number.isInteger(r.matches)&&r.matches>=1&&r.matches<=24)return {status:"MATCH",sinksChecked:2,matches:r.matches};
      return fixed("REFUSED");
    }catch{return fixed("REFUSED");}finally{wipe();for(const b of buffers)b.fill(0);state="FINISHED";}
  }
  return {data,end,exit,close,abort,finish};
}

export function bindR031Child(child,config){
  // This adapter deliberately cannot launch processes. Caller owns preflight, process,
  // timeout/termination, actual sink inventory, and database cleanup.
  const cap=makeR031Capture(config);
  try{
    if(!child||!child.stdout||!child.stderr||child.exitCode!==null||child.signalCode!==null)throw Error("REFUSED");
    for(const id of ["stdout","stderr"]){
      const s=child[id];
      if(s.readableEnded||s.destroyed||s.readableFlowing===true||s.listenerCount("data")!==0||s.listenerCount("readable")!==0||s.readableEncoding!==null)throw Error("REFUSED");
    }
    child.once("error",()=>cap.abort());
    child.once("exit",(code,signal)=>cap.exit(code,signal));
    child.once("close",(code,signal)=>cap.close(code,signal));
    for(const id of ["stdout","stderr"]){
      const s=child[id];
      s.once("error",()=>cap.abort());
      s.once("end",()=>cap.end(id));
      s.once("close",()=>{if(!s.readableEnded)cap.abort();});
      s.on("data",chunk=>cap.data(id,chunk));
    }
  }catch{cap.abort();}
  return {finish:cap.finish,abort:cap.abort};
}

export function testR031Capture(make){
  const bytes=s=>Uint8Array.from(s,c=>c.charCodeAt(0));
  const m=i=>"R031-CAPTURE-"+String(i).repeat(32);
  const config=()=>({expectedSinks:["stdout","stderr"],canaries:{stdout:{start:m(1),end:m(2)},stderr:{start:m(3),end:m(4)}}});
  const secrets={oldCodes:[],newCodes:[],validPassword:"fixture",invalidPassword:"fixture2"};
  const clean=(c)=>{c.data("stdout",bytes(m(1)+"alpha"+m(2)));c.data("stderr",bytes(m(3)+"beta"+m(4)));c.end("stdout");c.end("stderr");c.exit(0,null);c.close(0,null);};
  const spy=input=>({status:"NO_MATCH",sinksChecked:input.captures.length,matches:0});
  const cases=[
    ["clean",clean,"NO_MATCH"],
    ["split-chunks",c=>{let b=bytes(m(1)+"abc"+m(2));c.data("stdout",b.subarray(0,20));c.data("stdout",b.subarray(20));c.data("stderr",bytes(m(3)+m(4)));c.exit(0,null);c.end("stderr");c.end("stdout");c.close(0,null);},"NO_MATCH"],
    ["match",clean,"MATCH",()=>({status:"MATCH",sinksChecked:2,matches:1})],
    ["bad-scanner-receipt",clean,"REFUSED",()=>({status:"NO_MATCH",sinksChecked:1,matches:0})],
    ["scanner-throws",clean,"REFUSED",()=>{throw Error("synthetic") }],
    ["missing-start",c=>{c.data("stdout",bytes(m(2)));c.data("stderr",bytes(m(3)+m(4)));c.end("stdout");c.end("stderr");c.exit(0,null);c.close(0,null);},"REFUSED"],
    ["missing-end",c=>{c.data("stdout",bytes(m(1)));c.data("stderr",bytes(m(3)+m(4)));c.end("stdout");c.end("stderr");c.exit(0,null);c.close(0,null);},"REFUSED"],
    ["reversed",c=>{c.data("stdout",bytes(m(2)+m(1)));c.data("stderr",bytes(m(3)+m(4)));c.end("stdout");c.end("stderr");c.exit(0,null);c.close(0,null);},"REFUSED"],
    ["duplicated-canary",c=>{c.data("stdout",bytes(m(1)+m(1)+m(2)));c.data("stderr",bytes(m(3)+m(4)));c.end("stdout");c.end("stderr");c.exit(0,null);c.close(0,null);},"REFUSED"],
    ["late-data",c=>{clean(c);c.data("stdout",bytes("late"));},"REFUSED"],
    ["data-after-eof",c=>{c.end("stdout");c.data("stdout",bytes("late"));clean(c);},"REFUSED"],
    ["duplicate-eof",c=>{c.end("stdout");c.end("stdout");clean(c);},"REFUSED"],
    ["missing-eof",c=>{c.data("stdout",bytes(m(1)+m(2)));c.data("stderr",bytes(m(3)+m(4)));c.end("stdout");c.exit(0,null);c.close(0,null);},"REFUSED"],
    ["no-exit",c=>{c.end("stdout");c.end("stderr");c.close(0,null);},"REFUSED"],
    ["nonzero",c=>{c.exit(1,null);clean(c);},"REFUSED"],
    ["signal",c=>{c.exit(0,"SIGTERM");clean(c);},"REFUSED"],
    ["duplicate-exit",c=>{c.exit(0,null);c.exit(0,null);clean(c);},"REFUSED"],
    ["duplicate-close",c=>{clean(c);c.close(0,null);},"REFUSED"],
    ["abort",c=>{c.abort();clean(c);},"REFUSED"],
    ["no-close",c=>{c.end("stdout");c.end("stderr");c.exit(0,null);},"REFUSED"],
    ["wrong-sink",c=>{c.data("other",bytes("bad"));clean(c);},"REFUSED"],
    ["oversize",c=>{c.data("stdout",new Uint8Array(8*1024*1024));c.data("stdout",bytes("x"));clean(c);},"REFUSED"],
    ["non-bytes",c=>{c.data("stdout","bad");clean(c);},"REFUSED"],
    ["extra-sink",clean,"REFUSED",spy,x=>x.expectedSinks.push("file")],
    ["duplicate-markers",clean,"REFUSED",spy,x=>x.canaries.stderr.start=x.canaries.stdout.start],
    ["bad-marker",clean,"REFUSED",spy,x=>x.canaries.stdout.start="bad"],
    ["no-config",clean,"REFUSED",spy,x=>null],
    ["double-finish",c=>{clean(c);c.finish(spy,secrets);},"REFUSED"],
    ["copied-buffers",c=>{const b=bytes(m(1)+m(2));c.data("stdout",b);b.fill(0);c.data("stderr",bytes(m(3)+m(4)));c.end("stdout");c.end("stderr");c.exit(0,null);c.close(0,null);},"NO_MATCH"]
  ];
  const failures=[];
  for(const [name,events,expected,scanner=spy,modify] of cases){
    const x=config(),v=modify?.(x),c=make(v===null?null:x);
    events(c);const r=c.finish(scanner,secrets);
    if(r.status!==expected||Object.keys(r).sort().join(",")!=="matches,sinksChecked,status")failures.push(name);
  }
  return {total:cases.length,passed:cases.length-failures.length,failures};
}

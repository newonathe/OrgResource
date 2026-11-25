// Verification-only R031 in-memory scanner. No runtime capture or authorization.
// Complete streams and sink canaries must be established independently by the caller.
export function scanR031(input) {
  const refuse = () => ({status:"REFUSED", sinksChecked:0, matches:0});
  try {
    if (!input || typeof input !== "object") return refuse();
    const {oldCodes,newCodes,validPassword,invalidPassword,expectedSinks,captures}=input;
    const isCode = v => typeof v==="string" && /^[A-Za-z0-9_-]{43}$/.test(v);
    const isPassword = v => typeof v==="string" && /^[\x20-\x7e]{12,256}$/.test(v);
    if (!Array.isArray(oldCodes)||oldCodes.length!==5||!oldCodes.every(isCode)||
        !Array.isArray(newCodes)||newCodes.length!==5||!newCodes.every(isCode)||
        !isPassword(validPassword)||!isPassword(invalidPassword)) return refuse();
    const values=[...oldCodes,...newCodes,validPassword,invalidPassword];
    if (new Set(values).size!==12) return refuse();
    if (!Array.isArray(expectedSinks)||expectedSinks.length<2||expectedSinks.length>16||
        !expectedSinks.includes("stdout")||!expectedSinks.includes("stderr")||
        !expectedSinks.every(s=>typeof s==="string"&&/^[a-zA-Z0-9_.-]{1,64}$/.test(s))||
        new Set(expectedSinks).size!==expectedSinks.length||
        !Array.isArray(captures)||captures.length!==expectedSinks.length) return refuse();
    const ascii = v => Uint8Array.from(v, c=>c.charCodeAt(0));
    const wide = (v,le) => Uint8Array.from([...v].flatMap(c=>le?[c.charCodeAt(0),0]:[0,c.charCodeAt(0)]));
    const has = (bytes, pattern) => {
      for(let i=bytes.indexOf(pattern[0]); i>=0 && i<=bytes.length-pattern.length; i=bytes.indexOf(pattern[0],i+1)){
        let j=1; while(j<pattern.length && bytes[i+j]===pattern[j]) j++;
        if(j===pattern.length) return true;
      }
      return false;
    };
    const base64 = s => {
      const alphabet="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
      let out="";
      for(let i=0;i<s.length;i+=3){
        const n=(s.charCodeAt(i)<<16)|((s.charCodeAt(i+1)||0)<<8)|(s.charCodeAt(i+2)||0);
        out+=alphabet[(n>>18)&63]+alphabet[(n>>12)&63]+(i+1<s.length?alphabet[(n>>6)&63]:"=")+(i+2<s.length?alphabet[n&63]:"=");
      }
      return out;
    };
    const forms = v => {
      const uri=encodeURIComponent(v);
      const form=uri.replace(/%20/g,"+");
      const lower=s=>s.replace(/%[0-9A-F]{2}/g,m=>m.toLowerCase());
      const b64=base64(v);
      const url64=b64.replace(/\+/g,"-").replace(/\//g,"_").replace(/=+$/,"");
      return [...new Set([v,JSON.stringify(v).slice(1,-1),uri,lower(uri),form,lower(form),b64,b64.replace(/=+$/,""),url64])];
    };
    const patterns=values.map(v=>forms(v).flatMap(s=>[ascii(s),wide(s,true),wide(s,false)]));
    const seen=new Set(), canaries=new Set();
    let checked=0,matches=0;
    for(const c of captures){
      if(!c||!expectedSinks.includes(c.id)||seen.has(c.id)||c.complete!==true||
         !(c.bytes instanceof Uint8Array)||c.bytes.length===0||c.bytes.length>8*1024*1024||
         typeof c.canary!=="string"||!/^R031-CAPTURE-[0-9a-f]{32}$/.test(c.canary)||
         canaries.has(c.canary)||values.some(v=>c.canary.includes(v)||v.includes(c.canary))) return refuse();
      if (![ascii(c.canary),wide(c.canary,true),wide(c.canary,false)].some(p=>has(c.bytes,p))) return refuse();
      seen.add(c.id); canaries.add(c.canary); checked++;
      for(const variants of patterns) if(variants.some(p=>has(c.bytes,p))) matches++;
    }
    return {status:matches===0?"NO_MATCH":"MATCH",sinksChecked:checked,matches};
  } catch {return refuse();}
}

export function selfTest(scan) {
  const bytes=s=>Uint8Array.from(s,c=>c.charCodeAt(0));
  const canary1="R031-CAPTURE-"+"1".repeat(32),canary2="R031-CAPTURE-"+"2".repeat(32);
  const fresh=()=>({oldCodes:"ABCDE".split("").map(c=>c.repeat(43)),newCodes:"FGHIJ".split("").map(c=>c.repeat(43)),validPassword:'Fictional "pass"+word',invalidPassword:"Fictional-wrong-password",expectedSinks:["stdout","stderr"],captures:[{id:"stdout",bytes:bytes(canary1),complete:true,canary:canary1},{id:"stderr",bytes:bytes(canary2),complete:true,canary:canary2}]});
  const cases=[
    ["clean",()=>{}, "NO_MATCH"],
    ["old-code",i=>i.captures[0].bytes=bytes(canary1+i.oldCodes[0]),"MATCH"],
    ["new-code",i=>i.captures[1].bytes=bytes(canary2+i.newCodes[4]),"MATCH"],
    ["password",i=>i.captures[0].bytes=bytes(canary1+i.validPassword),"MATCH"],
    ["wrong-password",i=>i.captures[1].bytes=bytes(canary2+i.invalidPassword),"MATCH"],
    ["url",i=>i.captures[0].bytes=bytes(canary1+encodeURIComponent(i.validPassword)),"MATCH"],
    ["json",i=>i.captures[0].bytes=bytes(canary1+JSON.stringify(i.validPassword)),"MATCH"],
    ["base64",i=>i.captures[1].bytes=bytes(canary2+"RmljdGlvbmFsLXdyb25nLXBhc3N3b3Jk"),"MATCH"],
    ["utf16le",i=>i.captures[0].bytes=Uint8Array.from([...canary1+i.oldCodes[0]].flatMap(c=>[c.charCodeAt(0),0])),"MATCH"],
    ["utf16be",i=>i.captures[1].bytes=Uint8Array.from([...canary2+i.newCodes[0]].flatMap(c=>[0,c.charCodeAt(0)])),"MATCH"],
    ["missing-sink",i=>i.captures.pop(),"REFUSED"],
    ["duplicate-sink",i=>i.captures[1].id="stdout","REFUSED"],
    ["unexpected-sink",i=>i.captures[1].id="unknown","REFUSED"],
    ["missing-canary",i=>i.captures[0].bytes=bytes("normal log"),"REFUSED"],
    ["duplicate-canary",i=>{i.captures[1].canary=canary1;i.captures[1].bytes=bytes(canary1);},"REFUSED"],
    ["incomplete",i=>i.captures[1].complete=false,"REFUSED"],
    ["empty",i=>i.captures[1].bytes=new Uint8Array(),"REFUSED"],
    ["oversize",i=>i.captures[1].bytes=new Uint8Array(8*1024*1024+1),"REFUSED"],
    ["missing-code",i=>i.oldCodes.pop(),"REFUSED"],
    ["duplicate-code",i=>i.newCodes[0]=i.oldCodes[0],"REFUSED"],
    ["non-ascii-password",i=>i.invalidPassword="fictional-ü-password","REFUSED"],
    ["invalid-code",i=>i.oldCodes[0]="short","REFUSED"],
    ["missing-stdout",i=>i.expectedSinks=["file","stderr"],"REFUSED"],
    ["null",()=>null,"REFUSED"]
  ];
  const results=cases.map(([name,mutate,expected])=>{const i=fresh();const v=mutate(i);const r=scan(v===null?null:i);return {name,pass:r.status===expected&&Object.keys(r).sort().join(",")==="matches,sinksChecked,status"}});
  return {total:results.length,passed:results.filter(r=>r.pass).length,failures:results.filter(r=>!r.pass).map(r=>r.name)};
}

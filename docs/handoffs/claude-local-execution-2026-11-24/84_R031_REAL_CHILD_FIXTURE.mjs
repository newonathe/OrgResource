// Public fictional process-only control. No app, database, private input or arbitrary command.
import {spawn} from "node:child_process";
import {randomBytes} from "node:crypto";
import {bindR031Child} from "./82_R031_CAPTURE_LIFECYCLE.mjs";

const cases = [
 ["clean","NO_MATCH"],["stdout-leak","MATCH"],["stderr-leak","MATCH"],
 ["split","MATCH"],["missing-start","REFUSED"],["missing-end","REFUSED"],
 ["reverse","REFUSED"],["duplicate","REFUSED"],["nonzero","REFUSED"],
 ["oversize","REFUSED"],["timeout","REFUSED"],["abort","REFUSED"],
 ["preconsumed","REFUSED"],["invalid-secrets","REFUSED"]
];
const fixture = {
 oldCodes:"ABCDE".split("").map(c=>c.repeat(43)),
 newCodes:"FGHIJ".split("").map(c=>c.repeat(43)),
 validPassword:"Fictional-valid-password",invalidPassword:"Fictional-invalid-password"
};
function childMain(mode, marks, fake) {
 const write = (s,bytes)=>new Promise((resolve,reject)=>s.write(bytes,e=>e?reject(e):resolve()));
 async function main(){
  const o=process.stdout,e=process.stderr;
  if(mode==="reverse"){await write(o,marks.stdout.end);await write(o,marks.stdout.start);}
  else {
   if(mode!=="missing-start")await write(o,marks.stdout.start);
   if(mode==="stdout-leak")await write(o,fake.oldCodes[0]);
   if(mode==="split"){await write(o,fake.newCodes[0].slice(0,17));await write(o,fake.newCodes[0].slice(17));}
   if(mode==="duplicate")await write(o,marks.stdout.start);
   if(mode==="oversize")for(let i=0;i<129;i++)await write(o,Buffer.alloc(65536,65));
   if(mode!=="missing-end")await write(o,marks.stdout.end);
  }
  await write(e,marks.stderr.start);
  if(mode==="stderr-leak")await write(e,fake.validPassword);
  await write(e,marks.stderr.end);
  if(mode==="timeout"||mode==="abort")setInterval(()=>{},1000);
  if(mode==="nonzero")process.exitCode=7;
 }
 main().catch(()=>{process.exitCode=9;});
}
async function runCase(mode,expected) {
 const marker=()=>"R031-CAPTURE-"+randomBytes(16).toString("hex");
 const marks={stdout:{start:marker(),end:marker()},stderr:{start:marker(),end:marker()}};
 const config={expectedSinks:["stdout","stderr"],canaries:marks};
 const program="("+childMain.toString()+")("+[mode,marks,fixture].map(x=>JSON.stringify(x)).join(",")+");";
 const child=spawn(process.execPath,["--eval",program],{shell:false,env:{},stdio:["ignore","pipe","pipe"],windowsHide:true});
 if(mode==="preconsumed")child.stdout.on("data",()=>{});
 const capture=bindR031Child(child,config);
 // Consume only in the deliberate preconsumed refusal case, to avoid pipe deadlock.
 if(mode==="preconsumed")child.stderr.resume();
 let timedOut=false, aborted=false, spawnError=false, exitSeen=false;
 let timer,hard,abortTimer;
 const actual=await new Promise(resolve=>{
  const settle=(r)=>{clearTimeout(timer);clearTimeout(hard);clearTimeout(abortTimer);resolve(r);};
  child.once("error",()=>{spawnError=true;capture.abort();});
  child.once("exit",()=>{exitSeen=true;});
  child.once("close",(code,signal)=>{
   const receipt=capture.finish(mode==="invalid-secrets"?{}:fixture);
   const normal=code===0&&signal===null&&exitSeen;
   const controlled=exitSeen&&signal==="SIGKILL"&&code===null;
   const lifecycle=mode==="timeout"?timedOut&&controlled:
     mode==="abort"?aborted&&!timedOut&&controlled:
     mode==="nonzero"?exitSeen&&code===7&&signal===null:
     normal&&!timedOut&&!aborted;
   settle({pass:!spawnError&&lifecycle&&receipt.status===expected,
     closed:true,status:receipt.status});
  });
  const terminate=()=>{
   capture.abort();
   if(child.exitCode===null&&child.signalCode===null)child.kill("SIGKILL");
  };
  timer=setTimeout(()=>{timedOut=true;terminate();},mode==="timeout"?1200:8000);
  hard=setTimeout(()=>{capture.abort();settle({pass:false,closed:false,status:"REFUSED"});},12000);
  if(mode==="abort")abortTimer=setTimeout(()=>{aborted=true;terminate();},300);
 });
 return actual;
}
async function main(){
 if(process.platform!=="linux"||Number(process.versions.node.split(".")[0])!==24)throw Error("REFUSED_ENVIRONMENT");
 let passed=0;
 for(const [name,expected] of cases){
  const actual=await runCase(name,expected);
  // Only fixed fixture labels, status and booleans; never raw captured buffers or errors.
  console.log("R031_REAL_CHILD_CASE:"+name+":"+(actual.pass?"PASS":"FAIL")+":closed="+actual.closed);
  if(actual.pass)passed++;
  if(!actual.closed){process.exitCode=1;return;}
 }
 console.log("R031_REAL_CHILD_TOTAL:"+passed+"/"+cases.length);
 if(passed!==cases.length)process.exitCode=1;
}
main().catch(()=>{console.log("R031_REAL_CHILD_HARNESS_REFUSED");process.exitCode=1;});

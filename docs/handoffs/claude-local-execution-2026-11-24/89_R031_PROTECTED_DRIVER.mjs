// Protected fictional-only verification driver; no arbitrary commands, DBs or paths.
import {spawn,spawnSync} from "node:child_process";
import {randomBytes} from "node:crypto";
import {mkdtempSync,readFileSync,lstatSync,unlinkSync,rmSync,chmodSync,readdirSync,existsSync,realpathSync} from "node:fs";
import {resolve,join} from "node:path";
import {tmpdir} from "node:os";
import {withR031Database} from "./86_R031_OWNED_MYSQL_GUARD.mjs";
import {bindR031Child} from "./82_R031_CAPTURE_LIFECYCLE.mjs";
const check=x=>{if(!x)throw Error("R031_DRIVER_REFUSED");};
const stages=new Set(["MIGRATE","MIGRATED","STARTUP","SINKS","DB","BOOTSTRAP","HTTP","DENIAL","ROTATION","CUSTODY","DRAIN","COMPLETE"]);
const mark=()=>"R031-CAPTURE-"+randomBytes(16).toString("hex");
const readPhase=dir=>{try{const x=readFileSync(join(dir,"phase"),"utf8");return stages.has(x)?x:"UNKNOWN";}catch{return "UNKNOWN";}};
async function run(){
 check(process.argv.length===2&&process.platform==="linux"&&process.versions.node.startsWith("24."));
 const root=realpathSync(resolve("r031-main"));
 const head=spawnSync("git",["-C",root,"rev-parse","HEAD"],{encoding:"utf8",stdio:["ignore","pipe","pipe"]});
 check(head.status===0&&head.stdout.trim()==="96741bbd295a2d1e7b4bbf6a75cb558a793b09bd");
 const dirty=spawnSync("git",["-C",root,"diff","--quiet","HEAD","--"],{stdio:"ignore"});check(dirty.status===0);
 const cp=resolve("r031-classes")+":"+join(root,"extended/backend/target/classes")+":"+readFileSync(resolve("r031-classpath.txt"),"utf8").trim();
 const java=realpathSync(join(process.env.JAVA_HOME,"bin/java"));
 const dir=mkdtempSync(join(tmpdir(),"r031-owned-"));chmodSync(dir,0o700);
 let phase="UNKNOWN",receipt=null;
 try{
  await withR031Database(async db=>{
   const marks={stdout:{start:mark(),end:mark()},stderr:{start:mark(),end:mark()}};
   const env={PATH:"/usr/bin:/bin",HOME:dir,LANG:"C.UTF-8",EXT_DB_URL:db.url,EXT_DB_USER:db.runtime,EXT_DB_PASSWORD:db.runtimePassword,
    EXT_DB_MIGRATION_URL:db.url,EXT_DB_MIGRATION_USER:db.migrator,EXT_DB_MIGRATION_PASSWORD:db.migratorPassword,
    R031_SCHEMA:db.schema,R031_UUID:db.uuid,R031_VERIFIER_USER:db.verifier,R031_VERIFIER_PASSWORD:db.verifierPassword,
    SPRING_PROFILES_ACTIVE:"c1-test",EXT_SMS_ENABLED:"false",EXT_REMINDER_MODE:"DISABLED",EXT_CONTACT_ENCRYPTION_KEY:randomBytes(32).toString("base64"),
    R031_STDOUT_START:marks.stdout.start,R031_STDOUT_END:marks.stdout.end,R031_STDERR_START:marks.stderr.start,R031_STDERR_END:marks.stderr.end};
   const args=["-XX:-HeapDumpOnOutOfMemoryError","-XX:ErrorFile="+join(dir,"jvm-error.log"),"-cp",cp,"verification.R031ProtectedFork"];
   db.verify();
   const m=spawnSync(java,[...args,"migrate"],{cwd:dir,env,shell:false,timeout:120000,maxBuffer:8*1024*1024,stdio:["ignore","pipe","pipe"]});
   phase=readPhase(dir);check(m.status===0&&!m.error&&phase==="MIGRATED");
   m.stdout?.fill(0);m.stderr?.fill(0);
   db.narrowRuntime();console.log("R031_PROTECTED_MIGRATION_OK:host_jdbc=true flyway_v1_v7=true narrowed=true");
   const child=spawn(java,[...args,"rotate"],{cwd:dir,env,shell:false,stdio:["ignore","pipe","pipe"]});
   const capture=bindR031Child(child,{expectedSinks:["stdout","stderr"],canaries:marks});
   const closed=await new Promise(resolve=>{
    let timeout=false,errored=false;
    const timer=setTimeout(()=>{timeout=true;capture.abort();if(child.exitCode===null&&child.signalCode===null)child.kill("SIGKILL");},180000);
    child.once("error",()=>{errored=true;capture.abort();});
    child.once("close",(code,signal)=>{clearTimeout(timer);resolve(!timeout&&!errored&&code===0&&signal===null);});
   });
   phase=readPhase(dir);check(closed&&phase==="COMPLETE");
   const file=join(dir,"fixture.json"),st=lstatSync(file);
   check(st.isFile()&&!st.isSymbolicLink()&&st.uid===process.getuid()&&(st.mode&0o777)===0o600&&st.nlink===1&&st.size>0&&st.size<16384);
   const bytes=readFileSync(file);let fixture;
   try{fixture=JSON.parse(bytes.toString("utf8"));receipt=capture.finish(fixture);}finally{bytes.fill(0);fixture=null;unlinkSync(file);}
   check(receipt.status==="NO_MATCH"&&receipt.sinksChecked===2&&receipt.matches===0);
   // Unexpected files, including JVM crash dumps or app file logs, refuse the bounded scope.
   check(readdirSync(dir).every(x=>x==="phase"));db.verify();
  });
  console.log("R031_PROTECTED_ROTATION_OK:http_denial=true http_success=true db_hashes=true audit_version_session=true no_store=true stdout_stderr_no_match=true cleanup=true");
 }catch{console.log("R031_PROTECTED_REFUSED:phase="+phase+":scan="+(receipt?.status??"NOT_RUN"));process.exitCode=1;}
 finally{rmSync(dir,{recursive:true,force:true});check(!existsSync(dir));}
}
run().catch(()=>{console.log("R031_PROTECTED_DRIVER_REFUSED");process.exitCode=1;});

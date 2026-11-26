// Dedicated fictional-only hosted verification; no inherited external DB credentials.
import {spawnSync} from "node:child_process";
import {randomBytes} from "node:crypto";
import {mkdtempSync,readFileSync,rmSync,chmodSync,readdirSync,existsSync,realpathSync} from "node:fs";
import {resolve,join} from "node:path";
import {tmpdir} from "node:os";
import {withR031Database} from "./86_R031_OWNED_MYSQL_GUARD.mjs";
const check=x=>{if(!x)throw Error("R038_DRIVER_REFUSED");};
const stages=new Set(["MIGRATE","MIGRATED","STARTUP","POOL","BASELINE","LOCK","DIRECT_TIMEOUT","HTTP_LOCK_503","HTTP_LOCK_500","RECOVERY","COMPLETE_503","COMPLETE_500"]);
function phase(dir){try{const x=readFileSync(join(dir,"phase"),"utf8");return stages.has(x)?x:"UNKNOWN";}catch{return "UNKNOWN";}}
async function run(){
 check(process.argv.length===2&&process.platform==="linux"&&process.versions.node.startsWith("24."));
 const root=realpathSync(resolve("r031-main"));
 const head=spawnSync("git",["-C",root,"rev-parse","HEAD"],{encoding:"utf8",stdio:["ignore","pipe","pipe"]});
 check(head.status===0&&head.stdout.trim()==="96741bbd295a2d1e7b4bbf6a75cb558a793b09bd");
 check(spawnSync("git",["-C",root,"diff","--quiet","HEAD","--"],{stdio:"ignore"}).status===0);
 const cp=resolve("r031-classes")+":"+join(root,"extended/backend/target/classes")+":"+readFileSync(resolve("r031-classpath.txt"),"utf8").trim();
 const java=realpathSync(join(process.env.JAVA_HOME,"bin/java"));
 const dir=mkdtempSync(join(tmpdir(),"r038-owned-"));chmodSync(dir,0o700);
 let last="UNKNOWN";
 try{
  await withR031Database(async db=>{
   const env={PATH:"/usr/bin:/bin",HOME:dir,LANG:"C.UTF-8",EXT_DB_URL:db.url,EXT_DB_USER:db.runtime,EXT_DB_PASSWORD:db.runtimePassword,
    EXT_DB_MIGRATION_URL:db.url,EXT_DB_MIGRATION_USER:db.migrator,EXT_DB_MIGRATION_PASSWORD:db.migratorPassword,
    R031_SCHEMA:db.schema,R031_UUID:db.uuid,R031_VERIFIER_USER:db.verifier,R031_VERIFIER_PASSWORD:db.verifierPassword,
    SPRING_PROFILES_ACTIVE:"c1-test",EXT_SMS_ENABLED:"false",EXT_REMINDER_MODE:"DISABLED",EXT_CONTACT_ENCRYPTION_KEY:randomBytes(32).toString("base64")};
   const args=["-XX:-HeapDumpOnOutOfMemoryError","-XX:ErrorFile="+join(dir,"jvm-error.log"),"-cp",cp];
   const execute=(classes,timeout)=>{
    const r=spawnSync(java,[...args,...classes],{cwd:dir,env,shell:false,timeout,killSignal:"SIGKILL",maxBuffer:8*1024*1024,stdio:["ignore","pipe","pipe"]});
    last=phase(dir);r.stdout?.fill(0);r.stderr?.fill(0);check(r.status===0&&!r.error&&r.signal===null);
   };
   db.verify();execute(["verification.R031ProtectedFork","migrate"],120000);check(last==="MIGRATED");
   db.narrowRuntime();
   execute(["verification.R038OwnedLockFork"],180000);
   check(last==="COMPLETE_503"||last==="COMPLETE_500");
   check(readdirSync(dir).every(x=>x==="phase"));db.verify();
  });
 }catch{console.log("R038_OWNED_LOCK_REFUSED:phase="+last);process.exitCode=1;}
 finally{rmSync(dir,{recursive:true,force:true});check(!existsSync(dir));}
 if(!process.exitCode)console.log("R038_OWNED_LOCK_OK:mysql=8.0.46 jdbc_error=1205 sqlstate=HY000 status="+last.slice(-3)+" no_store=true exact_all_table_rows=true restored=true cleanup=true");
}
run().catch(()=>{console.log("R038_DRIVER_REFUSED");process.exitCode=1;});

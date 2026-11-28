// Dedicated GitHub-hosted fictional MySQL guard, not a general DB connector.
// Never accept externally supplied database URLs, container IDs, credentials or commands.
import {spawnSync} from "node:child_process";
import {randomBytes} from "node:crypto";
import {setTimeout as sleep} from "node:timers/promises";
import {pathToFileURL} from "node:url";
const IMAGE="mysql:8.0.46";
const fail=()=>{throw Error("R031_GUARD_REFUSED");};
const check=x=>{if(!x)fail();};
const hex=n=>randomBytes(n).toString("hex");
const idPattern=/^[0-9a-f]{64}$/;
const uuidPattern=/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const inheritedDenied=/^(EXT_|MYSQL_|SPRING_|JAVA_TOOL_OPTIONS$|JDK_JAVA_OPTIONS$|_JAVA_OPTIONS$|DOCKER_HOST$|DOCKER_CONTEXT$|DOCKER_TLS_VERIFY$|DOCKER_CERT_PATH$)/;
function envGuard(){
 check(process.platform==="linux"&&process.versions.node.split(".")[0]==="24");
 check(process.env.GITHUB_ACTIONS==="true"&&process.env.RUNNER_ENVIRONMENT==="github-hosted");
 check(process.env.GITHUB_REPOSITORY==="newonathe/OrgResource");
 check(process.env.GITHUB_REF==="refs/heads/docs/claude-local-execution-handoff-2026-11-24");
 check(!Object.keys(process.env).some(k=>inheritedDenied.test(k)));
}
function docker(args,extra={},input="",timeout=30000){
 const r=spawnSync("docker",["--context","default",...args],{shell:false,encoding:"utf8",input,
  env:{PATH:process.env.PATH,HOME:process.env.HOME,...extra},
  maxBuffer:1024*1024,timeout,stdio:["pipe","pipe","pipe"]});
 return {ok:r.status===0&&!r.error,code:r.status,out:(r.stdout||"").trim(),err:(r.stderr||"").trim()};
}
function must(args,extra,input,timeout){const r=docker(args,extra,input,timeout);check(r.ok);return r.out;}
function inspect(id){check(idPattern.test(id));const v=JSON.parse(must(["inspect",id]));check(v.length===1&&v[0].Id===id);return v[0];}
function verifyOwnership(v,id,name,nonce,imageId){
 check(v.Id===id&&v.Name==="/"+name&&v.Config.Image===IMAGE&&v.Image===imageId);
 check(v.Config.Labels?.["orgresource.r031.owner"]===nonce);
 check(v.HostConfig.Privileged===false&&v.HostConfig.NetworkMode!=="host");
 check((v.Mounts||[]).every(m=>m.Type==="volume"&&m.Destination==="/var/lib/mysql"));
}
function boundPort(v){
 const ports=v.NetworkSettings.Ports;
 check(ports&&Object.keys(ports).filter(k=>ports[k]!==null).join(",")==="3306/tcp");
 const binding=ports["3306/tcp"];check(binding?.length===1&&binding[0].HostIp==="127.0.0.1");
 const port=Number(binding[0].HostPort);check(Number.isInteger(port)&&port>=1024&&port<=65535);return port;
}
export async function withR031Database(action){
 envGuard();check(typeof action==="function");
 const nonce=hex(12),name="orgresource-r031-"+nonce,schema="orgresource_c1_r031_"+nonce;
 const root=hex(32),migratorPassword=hex(24),runtimePassword=hex(24),verifierPassword=hex(24);
 const migrator="r031_m_"+nonce.slice(0,12),runtime="r031_r_"+nonce.slice(0,12),verifier="r031_v_"+nonce.slice(0,12);
 let id=null,imageId=null,created=false,cleanup=false,completed=false,volumeName=null;
 function sql(user,password,statement,{database=schema,expectedDenied=false,rootSocket=false}={}){
  check(id&&idPattern.test(id));
  const args=["exec","-i","-e","MYSQL_PWD",id,"mysql","--batch","--raw","--skip-column-names","--connect-timeout=3",
   ...(rootSocket?["--protocol=socket"]:["--protocol=tcp","-h","127.0.0.1"]),"-u",user];
  if(database)args.push(database);
  const r=docker(args,{MYSQL_PWD:password},statement+"\n");
  if(expectedDenied){check(!r.ok&&r.code===1&&/^ERROR 1142 \(42000\) at line 1:/m.test(r.err));return null;}
  check(r.ok);return r.out;
 }
 try{
  // Pull only the same MySQL release as the accepted Extended CI workflow.
  const ctx=JSON.parse(must(["context","inspect","default"]));
  check(ctx.length===1&&ctx[0].Name==="default"&&ctx[0].Endpoints?.docker?.Host==="unix:///var/run/docker.sock");
  must(["pull",IMAGE],{},"",180000);
  imageId=must(["image","inspect",IMAGE,"--format","{{.Id}}"]);check(/^sha256:[0-9a-f]{64}$/.test(imageId));
  const c=docker(["create","--name",name,"--label","orgresource.r031.owner="+nonce,
   "--publish","127.0.0.1::3306","--env","MYSQL_ROOT_PASSWORD",
   "--env","MYSQL_DATABASE="+schema,IMAGE],{MYSQL_ROOT_PASSWORD:root},"",60000);
  // Resolve only exact unique name if creation returned incomplete output; validate label before removal.
  if(c.ok&&idPattern.test(c.out)){id=c.out;created=true;}
  else {const found=docker(["inspect","--format","{{.Id}}",name]);if(found.ok&&idPattern.test(found.out)){id=found.out;created=true;}fail();}
  verifyOwnership(inspect(id),id,name,nonce,imageId);
  must(["start",id]);
  let ready=false;
  for(let i=0;i<90;i++){
   try{if(sql("root",root,"SELECT 1;",{database:null,rootSocket:true})==="1"){
    // TCP readiness distinguishes final server from entrypoint temporary socket server.
    if(sql("root",root,"SELECT 1;",{database:null})==="1"){ready=true;break;}
   }}catch{}
   await sleep(1000);
  }
  check(ready);
  const v=inspect(id);verifyOwnership(v,id,name,nonce,imageId);check(v.State.Running===true);
  check(v.Mounts.length===1&&v.Mounts[0].Type==="volume"&&v.Mounts[0].Destination==="/var/lib/mysql");
  volumeName=v.Mounts[0].Name;check(idPattern.test(volumeName));
  const port=boundPort(v);
  const version=sql("root",root,"SELECT VERSION();",{database:null,rootSocket:true});check(/^8\.0\.46(?:[-+].*)?$/.test(version));
  const uuid=sql("root",root,"SELECT @@server_uuid;",{database:null,rootSocket:true});check(uuidPattern.test(uuid));
  const initial=sql("root",root,"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='"+schema+"';",{database:null,rootSocket:true});check(initial==="0");
  sql("root",root,
   "CREATE USER '"+migrator+"'@'%' IDENTIFIED BY '"+migratorPassword+"';\n"+
   "CREATE USER '"+runtime+"'@'%' IDENTIFIED BY '"+runtimePassword+"';\n"+
   "CREATE USER '"+verifier+"'@'%' IDENTIFIED BY '"+verifierPassword+"';\n"+
   "GRANT ALL PRIVILEGES ON \x60"+schema+"\x60.* TO '"+migrator+"'@'%';\n"+
   "GRANT SELECT,INSERT,UPDATE,DELETE ON \x60"+schema+"\x60.* TO '"+runtime+"'@'%';\n"+
   "GRANT SELECT ON \x60"+schema+"\x60.* TO '"+verifier+"'@'%';",
   {database:null,rootSocket:true});
  for(const [user,pass] of [[migrator,migratorPassword],[runtime,runtimePassword],[verifier,verifierPassword]]){
   check(sql(user,pass,"SELECT DATABASE(),@@server_uuid,CURRENT_USER();")===schema+"\t"+uuid+"\t"+user+"@%");
  }
  // Real privilege control on a disposable table. Drop it before any application launch.
  sql(migrator,migratorPassword,"CREATE TABLE r031_guard_probe(id INT PRIMARY KEY);");
  sql(runtime,runtimePassword,"INSERT INTO r031_guard_probe VALUES (1);");
  check(sql(verifier,verifierPassword,"SELECT COUNT(*) FROM r031_guard_probe;")==="1");
  sql(runtime,runtimePassword,"CREATE TABLE r031_guard_forbidden(id INT);",{expectedDenied:true});
  sql(verifier,verifierPassword,"DELETE FROM r031_guard_probe;",{expectedDenied:true});
  // Use an existing protected system table, so a syntax/missing-table error is not the intended denial.
  sql(migrator,migratorPassword,"SELECT COUNT(*) FROM mysql.user;",{expectedDenied:true});
  check(sql(verifier,verifierPassword,"SELECT COUNT(*) FROM r031_guard_probe;")==="1");
  sql(migrator,migratorPassword,"DROP TABLE r031_guard_probe;");
  check(sql("root",root,"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='"+schema+"';",{database:null,rootSocket:true})==="0");
  verifyOwnership(inspect(id),id,name,nonce,imageId);check(boundPort(inspect(id))===port);
  const url="jdbc:mysql://127.0.0.1:"+port+"/"+schema+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
  // Credentials only in process memory. Callback is trusted checked-in verification code, not user input.
  const verify=()=>{const x=inspect(id);verifyOwnership(x,id,name,nonce,imageId);check(x.State.Running&&boundPort(x)===port);
   check(sql(verifier,verifierPassword,"SELECT DATABASE(),@@server_uuid,CURRENT_USER();")===schema+"\t"+uuid+"\t"+verifier+"@%");};
  // Call only after the dedicated Flyway process has successfully migrated the owned schema.
  let narrowed=false;
  const narrowRuntime=()=>{
   verify();check(!narrowed);
   const rows=sql("root",root,"SELECT table_name FROM information_schema.tables WHERE table_schema='"+schema+"' AND engine='InnoDB' ORDER BY table_name;",{database:null,rootSocket:true}).split("\n");
   check(rows.length>=10&&new Set(rows).size===rows.length&&rows.every(x=>/^[a-zA-Z0-9_]+$/.test(x))&&["stock_movement","audit_event","flyway_schema_history"].every(x=>rows.includes(x)));
   check(sql(verifier,verifierPassword,"SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version IN ('1','2','3','4','5','6','7');")==="7");
   check(sql(verifier,verifierPassword,"SELECT COUNT(*) FROM organization;")==="0"&&sql(verifier,verifierPassword,"SELECT COUNT(*) FROM user_account;")==="0");
   const account="'"+runtime+"'@'%'";
   const q=x=>String.fromCharCode(96)+x+String.fromCharCode(96);
   sql("root",root,"REVOKE ALL PRIVILEGES, GRANT OPTION FROM "+account+";",{database:null,rootSocket:true});
   for(const table of rows){
    const grant=["stock_movement","audit_event"].includes(table)?"SELECT,INSERT":table==="flyway_schema_history"?"SELECT":"SELECT,INSERT,UPDATE,DELETE";
    sql("root",root,"GRANT "+grant+" ON "+q(schema)+"."+q(table)+" TO "+account+";",{database:null,rootSocket:true});
   }
   for(const [table,col] of [["stock_movement","kind"],["audit_event","action"]]){
    sql(runtime,runtimePassword,"UPDATE "+q(table)+" SET "+col+"="+col+" WHERE 1=0;",{expectedDenied:true});
    sql(runtime,runtimePassword,"DELETE FROM "+q(table)+" WHERE 1=0;",{expectedDenied:true});
   }
   sql(runtime,runtimePassword,"DELETE FROM flyway_schema_history WHERE 1=0;",{expectedDenied:true});
   check(sql(verifier,verifierPassword,"SELECT COUNT(*) FROM stock_movement;")==="0"&&sql(verifier,verifierPassword,"SELECT COUNT(*) FROM audit_event;")==="0");
   narrowed=true;verify();
  };
  await action({url,schema,uuid,port,runtime,runtimePassword,migrator,migratorPassword,verifier,verifierPassword,verify,narrowRuntime});
  verify();completed=true;
 }finally{
  if(created&&id){
   try{verifyOwnership(inspect(id),id,name,nonce,imageId);must(["rm","-f","-v",id]);
    const list=must(["ps","-a","--no-trunc","--filter","label=orgresource.r031.owner="+nonce,"--format","{{.ID}}"]);
    check(list==="");
    if(volumeName){const volumes=must(["volume","ls","--format","{{.Name}}"]).split("\n");check(!volumes.includes(volumeName));}
    cleanup=true;
   }catch{cleanup=false;}
  }
  check(cleanup&&completed);
 }
}
async function main(){
 check(process.argv.length===2);
 await withR031Database(async db=>{db.verify();check(db.url.startsWith("jdbc:mysql://127.0.0.1:"));});
 console.log("R031_MYSQL_GUARD_OK:version=8.0.46 identity=true empty=true role_denials=1142 loopback=true local_docker_context=true volume_removed=true cleanup=true app_not_run=true");
}
if(process.argv[1]&&import.meta.url===pathToFileURL(process.argv[1]).href)
 main().catch(()=>{console.log("R031_MYSQL_GUARD_REFUSED");process.exitCode=1;});

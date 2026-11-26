package verification;

import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import javax.sql.DataSource;
import com.google.gson.*;
import org.springframework.boot.SpringApplication;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.context.ConfigurableApplicationContext;
import edu.orgresource.extended.C1Application;
import edu.orgresource.extended.identity.C1BootstrapService;
import edu.orgresource.extended.service.DemoProvenance;

// Fictional owned database only; no application changes or raw output.
public final class R038OwnedLockFork {
    static void require(boolean ok){if(!ok)throw new IllegalStateException("R038_REFUSED");}
    static String env(String k){String v=System.getenv(k);require(v!=null&&!v.isBlank());return v;}
    static void phase(String v)throws Exception{Files.writeString(Path.of("phase"),v);}
    static List<List<String>> rows(Connection c,String query,Object... params)throws Exception{
        try(var s=c.prepareStatement(query)){
            s.setQueryTimeout(12);
            for(int i=0;i<params.length;i++)s.setObject(i+1,params[i]);
            try(var r=s.executeQuery()){
                var all=new ArrayList<List<String>>();
                while(r.next()){
                    var row=new ArrayList<String>();
                    for(int i=1;i<=r.getMetaData().getColumnCount();i++){
                        byte[] v=r.getBytes(i);
                        row.add(v==null?null:Base64.getEncoder().encodeToString(v));
                    }
                    all.add(row);
                }
                return all;
            }
        }
    }
    static String decode(String v){return new String(Base64.getDecoder().decode(v),java.nio.charset.StandardCharsets.UTF_8);}
    static String one(Connection c,String query,Object... params)throws Exception{
        var r=rows(c,query,params);
        require(r.size()==1&&r.getFirst().size()==1&&r.getFirst().getFirst()!=null);
        return decode(r.getFirst().getFirst());
    }
    static void identity(Connection c,String user)throws Exception{
        require(one(c,"SELECT DATABASE()").equals(env("R031_SCHEMA")));
        require(one(c,"SELECT @@server_uuid").equals(env("R031_UUID")));
        require(one(c,"SELECT CURRENT_USER()").equals(user+"@%"));
        require(one(c,"SELECT VERSION()").equals("8.0.46"));
    }
    static Connection connect(String user,String password)throws Exception{
        Connection c=DriverManager.getConnection(env("EXT_DB_URL"),env(user),env(password));
        try{identity(c,env(user));return c;}catch(Exception e){c.close();throw e;}
    }
    static String quoted(String v){require(v.matches("[A-Za-z0-9_]+"));return Character.toString(96)+v+Character.toString(96);}
    static Map<String,List<List<String>>> snapshot(Connection c)throws Exception{
        var tables=rows(c,"SELECT table_name FROM information_schema.tables WHERE table_schema=DATABASE() AND table_type='BASE TABLE' ORDER BY table_name");
        Map<String,List<List<String>>> out=new TreeMap<>();
        for(var row:tables){
            String table=decode(row.getFirst());
            var keys=rows(c,"SELECT column_name FROM information_schema.key_column_usage WHERE table_schema=DATABASE() AND table_name=? AND constraint_name='PRIMARY' ORDER BY ordinal_position",table);
            var order=new ArrayList<String>();
            for(var key:keys)order.add(quoted(decode(key.getFirst())));
            require(!order.isEmpty());
            out.put(table.toLowerCase(Locale.ROOT),rows(c,"SELECT * FROM "+quoted(table)+" ORDER BY "+String.join(",",order)));
        }
        require(out.keySet().equals(Set.of("organization","user_account","local_credential","account_recovery_code","membership","invitation","ownership_transfer","audit_event","department","project","resource","resource_requirement","allocation","spring_session","spring_session_attributes","stock_movement","idempotency_key","flyway_schema_history")));
        return out;
    }
    static HttpResponse<String> get(HttpClient http,int port)throws Exception{
        var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v2/demo/metadata"))
                .timeout(Duration.ofSeconds(15)).header("Accept","application/json").GET().build();
        var response=http.send(request,HttpResponse.BodyHandlers.ofString());
        require(response.body().length()<=8192&&response.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        require(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        return response;
    }
    static JsonObject original(HttpResponse<String> response){
        require(response.statusCode()==200);
        var body=JsonParser.parseString(response.body()).getAsJsonObject();
        require(body.keySet().equals(Set.of("data","requestId")));
        var data=body.getAsJsonObject("data");
        require(!data.get("synthetic").getAsBoolean());
        require(Set.of("synthetic","datasetId","demonstrationAsOf","notice").containsAll(data.keySet()));
        for(String key:List.of("datasetId","demonstrationAsOf","notice"))require(!data.has(key)||data.get(key).isJsonNull());
        return data;
    }
    static void fault(HttpResponse<String> r,int expected,String code,String message){
        require(r.statusCode()==expected);
        var body=JsonParser.parseString(r.body()).getAsJsonObject();
        require(body.keySet().equals(Set.of("code","message","requestId")));
        require(body.get("code").getAsString().equals(code)&&body.get("message").getAsString().equals(message));
        require(!body.get("requestId").getAsString().isBlank());
    }
    static void run()throws Exception{
        require(Runtime.version().feature()==21);
        require(env("EXT_DB_URL").matches("jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/orgresource_c1_r031_[0-9a-f]{24}\\?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
        require(env("EXT_DB_URL").equals(env("EXT_DB_MIGRATION_URL")));
        phase("STARTUP");
        try(var v=connect("R031_VERIFIER_USER","R031_VERIFIER_PASSWORD")){
            require(one(v,"SELECT COUNT(*) FROM organization").equals("0"));
            require(one(v,"SELECT COUNT(*) FROM flyway_schema_history WHERE success=1").equals("7"));
            try(ConfigurableApplicationContext ctx=new SpringApplication(C1Application.class).run(
                    "--server.port=0","--spring.datasource.hikari.connection-init-sql=SET @@session.time_zone = '+00:00', @@session.lock_wait_timeout = 2")){
                require(!ctx.getEnvironment().getProperty("c1.sms.enabled",Boolean.class,true));
                require("DISABLED".equals(ctx.getEnvironment().getProperty("c1.reminder.mode")));
                var ds=ctx.getBean(DataSource.class);
                phase("POOL");
                require(ctx.getEnvironment().getProperty("spring.datasource.hikari.maximum-pool-size",Integer.class,0)==6);
                var held=new ArrayList<Connection>();
                try{
                    for(int i=0;i<6;i++){
                        var c=ds.getConnection();held.add(c);identity(c,env("EXT_DB_USER"));
                        require(one(c,"SELECT @@session.lock_wait_timeout").equals("2"));
                        require(one(c,"SELECT @@session.time_zone").equals("+00:00"));
                    }
                }finally{for(var c:held)c.close();}
                phase("BASELINE");
                String password="R038-fictional-"+HexFormat.of().formatHex(new SecureRandom().generateSeed(24));
                var boot=ctx.getBean(C1BootstrapService.class).bootstrap("r038-fictional","Fictional R038","r038-owner","Fictional Owner",password);
                require(boot.recoveryCodes().size()==5&&one(v,"SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NULL").equals("1"));
                require(one(v,"SELECT COUNT(*) FROM user_account").equals("1"));
                int port=ctx.getEnvironment().getProperty("local.server.port",Integer.class,0);
                require(port>1024&&port<=65535);
                var http=HttpClient.newBuilder().proxy(ProxySelector.of(null)).followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(5)).build();
                var baseline=original(get(http,port));
                var before=snapshot(v);
                require(before.get("organization").size()==1&&before.get("membership").size()==1&&before.get("account_recovery_code").size()==5);
                require(before.get("spring_session").isEmpty()&&before.get("spring_session_attributes").isEmpty());
                phase("LOCK");
                boolean translated=false;
                try(var lock=connect("EXT_DB_MIGRATION_USER","EXT_DB_MIGRATION_PASSWORD");var s=lock.createStatement()){
                    s.setQueryTimeout(12);
                    s.execute("LOCK TABLES organization WRITE");
                    try{
                        phase("DIRECT_TIMEOUT");
                        boolean saw=false;
                        try{ctx.getBean(DemoProvenance.class).publicMetadata();}
                        catch(RuntimeException ex){
                            for(Throwable t=ex;t!=null;t=t.getCause()){
                                if(t instanceof SQLException sql && sql.getErrorCode()==1205&&"HY000".equals(sql.getSQLState()))saw=true;
                                if(t instanceof PessimisticLockingFailureException)translated=true;
                            }
                        }
                        require(saw);
                        phase(translated?"HTTP_LOCK_503":"HTTP_LOCK_500");
                        if(translated)fault(get(http,port),503,"TRANSIENT_TRANSACTION_CONFLICT",
                                "The transaction could not acquire its required locks. Retry with the same operation identity.");
                        else fault(get(http,port),500,"INTERNAL_ERROR","The request could not be completed.");
                    }finally{s.execute("UNLOCK TABLES");}
                }
                phase("RECOVERY");
                require(baseline.equals(original(get(http,port))));
                require(before.equals(snapshot(v)));
                require(baseline.equals(original(get(http,port))));
                require(before.equals(snapshot(v)));
                phase(translated?"COMPLETE_503":"COMPLETE_500");
            }
        }
    }
    public static void main(String[] args){
        int result=1;
        try{require(args.length==0);run();result=0;}catch(Throwable failure){/* only fixed phase emitted by parent */}
        System.exit(result);
    }
}

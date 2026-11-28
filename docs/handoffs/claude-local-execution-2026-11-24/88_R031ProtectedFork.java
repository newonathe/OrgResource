package verification;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.ConsoleAppender;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.google.gson.*;
import edu.orgresource.extended.C1Application;
import edu.orgresource.extended.identity.C1BootstrapService;
import edu.orgresource.extended.identity.C1Secrets;

// Verification-only, compiled outside product tree. No JUnit/Surefire report or raw stdout handoff.
public final class R031ProtectedFork {
    private static final Gson JSON = new Gson();
    private static final Path STAGE = Path.of("phase");
    private static void require(boolean condition) { if (!condition) throw new IllegalStateException("R031_REFUSED"); }
    private static String env(String key) { String v=System.getenv(key); require(v!=null&&!v.isBlank());return v; }
    private static void stage(String name) throws IOException { Files.writeString(STAGE,name); }
    private static String secret() { return "R031-fixture-"+HexFormat.of().formatHex(new SecureRandom().generateSeed(24)); }
    private static Connection connect(String user,String password) throws Exception {
        var c=DriverManager.getConnection(env("EXT_DB_URL"),env(user),env(password));
        require(rows(c,"SELECT DATABASE(),@@server_uuid,CURRENT_USER(),VERSION()").equals(List.of(List.of(
            env("R031_SCHEMA"),env("R031_UUID"),env(user)+"@%","8.0.46"))));
        return c;
    }
    private static List<List<String>> rows(Connection c,String sql,Object... params) throws SQLException {
        try(var s=c.prepareStatement(sql)){
            for(int i=0;i<params.length;i++)s.setObject(i+1,params[i]);
            try(var r=s.executeQuery()){
                List<List<String>> out=new ArrayList<>();
                while(r.next()){List<String> row=new ArrayList<>();for(int i=1;i<=r.getMetaData().getColumnCount();i++)row.add(r.getString(i));out.add(row);}
                return out;
            }
        }
    }
    private static String one(Connection c,String sql,Object... params)throws SQLException {
        var r=rows(c,sql,params);require(r.size()==1&&r.get(0).size()==1);return r.get(0).get(0);
    }
    private static Flyway flyway(){
        return Flyway.configure().dataSource(env("EXT_DB_MIGRATION_URL"),env("EXT_DB_MIGRATION_USER"),env("EXT_DB_MIGRATION_PASSWORD")).load();
    }
    private static void history(Connection c)throws SQLException {
        var h=rows(c,"SELECT version,success,checksum IS NOT NULL FROM flyway_schema_history ORDER BY installed_rank");
        require(h.size()==7);
        for(int i=0;i<7;i++)require(h.get(i).equals(List.of(Integer.toString(i+1),"1","1")));
    }
    private static void deny(Connection c,String sql)throws SQLException {
        try(var s=c.createStatement()){s.executeUpdate(sql);throw new IllegalStateException("R031_GRANT_ACCEPTED");}
        catch(SQLException e){require(e.getErrorCode()==1142&&"42000".equals(e.getSQLState()));}
    }
    private static void sinks(ConfigurableApplicationContext ctx){
        var e=ctx.getEnvironment();
        require(!e.getProperty("server.tomcat.accesslog.enabled",Boolean.class,false));
        require(e.getProperty("logging.file.name")==null&&e.getProperty("logging.file.path")==null&&e.getProperty("logging.config")==null);
        require(LoggerFactory.getILoggerFactory() instanceof LoggerContext);
        LoggerContext lc=(LoggerContext)LoggerFactory.getILoggerFactory();
        int count=0;
        for(Logger l:lc.getLoggerList()){
            var it=l.iteratorForAppenders();
            while(it.hasNext()){
                var app=it.next();require(l.getName().equals(org.slf4j.Logger.ROOT_LOGGER_NAME)&&app instanceof ConsoleAppender<?> ca&&ca.isStarted()&&"System.out".equals(ca.getTarget()));count++;
            }
        }
        require(count==1);
        var lm=java.util.logging.LogManager.getLogManager();
        var names=lm.getLoggerNames();
        while(names.hasMoreElements()){
            var logger=lm.getLogger(names.nextElement());if(logger==null)continue;
            for(var handler:logger.getHandlers())require(handler.getClass().getName().equals("org.slf4j.bridge.SLF4JBridgeHandler"));
        }
    }
    private static final class Client {
        final String base; final CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        final HttpClient http=HttpClient.newBuilder().cookieHandler(cookies).proxy(ProxySelector.of(null)).followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(5)).build();
        String csrf;
        Client(int port){require(port>1024&&port<=65535);base="http://127.0.0.1:"+port;}
        JsonObject req(String path,Map<String,String> body,int status)throws Exception{
            require(path.startsWith("/api/v2/"));
            var builder=HttpRequest.newBuilder(URI.create(base+path)).timeout(Duration.ofSeconds(15)).header("Accept","application/json");
            if(body!=null)builder.header("Content-Type","application/json").header("Origin",base).header("X-CSRF-TOKEN",csrf).POST(HttpRequest.BodyPublishers.ofString(JSON.toJson(body)));
            else builder.GET();
            var r=http.send(builder.build(),HttpResponse.BodyHandlers.ofString());
            require(r.statusCode()==status&&r.body().length()<=65536);
            require(r.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
            return JsonParser.parseString(r.body()).getAsJsonObject();
        }
        JsonObject data(String path,Map<String,String> body)throws Exception{return req(path,body,200).getAsJsonObject("data");}
        void refresh()throws Exception{csrf=data("/api/v2/auth/csrf",null).get("csrfToken").getAsString();}
        String cookie(){var list=cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("ORGSESSION")).toList();require(list.size()==1);return list.get(0).getValue();}
    }
    private static final String SNAP="SELECT id,HEX(code_hash),HEX(batch_id),created_at,expires_at,used_at,revoked_at FROM account_recovery_code WHERE user_id=? ORDER BY id";
    private static final String AUDIT="SELECT COUNT(*) FROM audit_event WHERE actor_user_id=? AND action='RECOVERY_CODES_ROTATED'";
    private static final String VERSION="SELECT credential_version FROM user_account WHERE id=?";
    private static void rotation()throws Exception{
        stage("STARTUP");
        try(var c=connect("R031_VERIFIER_USER","R031_VERIFIER_PASSWORD")){
            history(c);require(one(c,"SELECT COUNT(*) FROM organization").equals("0")&&one(c,"SELECT COUNT(*) FROM user_account").equals("0"));
        }
        // Flyway already migrated and runtime grants narrowed by parent before this launch.
        var app=new SpringApplication(C1Application.class);
        try(ConfigurableApplicationContext ctx=app.run("--server.port=0")){
            stage("SINKS");sinks(ctx);
            var logger=LoggerFactory.getLogger("verification.R031ProtectedFork");
            logger.info(env("R031_STDOUT_START"));System.err.println(env("R031_STDERR_START"));
            stage("DB");
            require(!ctx.getEnvironment().getProperty("c1.sms.enabled",Boolean.class,true));
            require("DISABLED".equals(ctx.getEnvironment().getProperty("c1.reminder.mode")));
            try(var c=ctx.getBean(DataSource.class).getConnection()){
                require(rows(c,"SELECT DATABASE(),@@server_uuid,CURRENT_USER()").equals(List.of(List.of(env("R031_SCHEMA"),env("R031_UUID"),env("EXT_DB_USER")+"@%"))));
                deny(c,"UPDATE stock_movement SET kind=kind WHERE 1=0");deny(c,"DELETE FROM stock_movement WHERE 1=0");
                deny(c,"UPDATE audit_event SET action=action WHERE 1=0");deny(c,"DELETE FROM audit_event WHERE 1=0");
                deny(c,"DELETE FROM flyway_schema_history WHERE 1=0");
            }
            flyway().validate();
            try(var c=connect("R031_VERIFIER_USER","R031_VERIFIER_PASSWORD")){
                history(c);
                stage("BOOTSTRAP");
                String valid=secret(),invalid=secret(),username="r031-owner";
                var boot=ctx.getBean(C1BootstrapService.class).bootstrap("r031-fixture","Fictional R031",username,"Fictional Owner",valid);
                long id=boot.accountId();var old=boot.recoveryCodes();
                require(old.size()==5&&new HashSet<>(old).size()==5);
                String org=one(c,"SELECT BIN_TO_UUID(public_id,0) FROM organization WHERE id=?",boot.organizationId());
                String account=one(c,"SELECT BIN_TO_UUID(public_id,0) FROM user_account WHERE id=?",id);
                stage("HTTP");
                Client cl=new Client(ctx.getEnvironment().getProperty("local.server.port",Integer.class,0));cl.refresh();
                var login=cl.data("/api/v2/auth/login",Map.of("username",username,"password",valid));
                require(login.getAsJsonObject("session").get("activeOrgId").isJsonNull());
                require(login.getAsJsonObject("session").get("accountId").getAsString().equals(account));
                cl.csrf=login.get("csrfToken").getAsString();
                require(cl.data("/api/v2/session/active-org",Map.of("organizationId",org)).get("activeOrgId").getAsString().equals(org));cl.refresh();
                String cookie=cl.cookie();
                var session=cl.data("/api/v2/session",null);
                require(session.get("accountId").getAsString().equals(account)&&session.get("activeOrgId").getAsString().equals(org));
                var before=rows(c,SNAP,id);require(before.size()==5);
                long audit=Long.parseLong(one(c,AUDIT,id));String version=one(c,VERSION,id);
                String credential=one(c,"SELECT password_hash FROM local_credential WHERE user_id=?",id);
                for(String v:old)require(one(c,"SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND code_hash=? AND used_at IS NULL AND revoked_at IS NULL",id,C1Secrets.sha256(v)).equals("1"));
                String endpoint="/api/v2/account/recovery-codes/rotate";
                stage("DENIAL");
                var denied=cl.req(endpoint,Map.of("currentPassword",invalid),401);
                require(denied.keySet().equals(Set.of("code","message","requestId"))&&denied.get("code").getAsString().equals("CURRENT_PASSWORD_INVALID"));
                require(!JSON.toJson(denied).contains(invalid)&&!JSON.toJson(denied).contains(valid)&&old.stream().noneMatch(v->JSON.toJson(denied).contains(v)));
                require(before.equals(rows(c,SNAP,id))&&one(c,AUDIT,id).equals(Long.toString(audit))&&one(c,VERSION,id).equals(version));
                require(one(c,"SELECT password_hash FROM local_credential WHERE user_id=?",id).equals(credential));
                require(cl.data("/api/v2/session",null).equals(session)&&cl.cookie().equals(cookie));
                stage("ROTATION");
                var rotated=cl.data(endpoint,Map.of("currentPassword",valid));require(rotated.keySet().equals(Set.of("codes")));
                List<String> fresh=new ArrayList<>();for(var v:rotated.getAsJsonArray("codes"))fresh.add(v.getAsString());
                require(fresh.size()==5&&new HashSet<>(fresh).size()==5&&Collections.disjoint(old,fresh));
                for(String v:fresh)require(one(c,"SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND code_hash=? AND used_at IS NULL AND revoked_at IS NULL",id,C1Secrets.sha256(v)).equals("1"));
                for(String v:old)require(one(c,"SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND code_hash=? AND revoked_at IS NOT NULL",id,C1Secrets.sha256(v)).equals("1"));
                require(one(c,"SELECT COUNT(*) FROM account_recovery_code WHERE user_id=?",id).equals("10"));
                require(one(c,"SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND revoked_at IS NOT NULL",id).equals("5"));
                require(one(c,AUDIT,id).equals(Long.toString(audit+1))&&one(c,VERSION,id).equals(version));
                require(one(c,"SELECT password_hash FROM local_credential WHERE user_id=?",id).equals(credential));
                require(cl.data("/api/v2/session",null).equals(session)&&cl.cookie().equals(cookie));
                String accountView=JSON.toJson(cl.data("/api/v2/account",null));
                require(!accountView.contains(valid)&&!accountView.contains(invalid)&&!accountView.contains(credential)&&old.stream().noneMatch(accountView::contains)&&fresh.stream().noneMatch(accountView::contains));
                stage("CUSTODY");
                Path transfer=Path.of("fixture.json");
                Files.createFile(transfer,PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
                Files.writeString(transfer,JSON.toJson(Map.of("oldCodes",old,"newCodes",fresh,"validPassword",valid,"invalidPassword",invalid)),StandardOpenOption.WRITE);
            }
            stage("DRAIN");sinks(ctx);
            logger.info(env("R031_STDOUT_END"));System.err.println(env("R031_STDERR_END"));
        }
        stage("COMPLETE");
    }
    public static void main(String[] args){
        int code=1;
        try{
            require(Runtime.version().feature()==21&&args.length==1);
            require(env("EXT_DB_URL").matches("jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/orgresource_c1_r031_[0-9a-f]{24}\\?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
            require(env("EXT_DB_URL").equals(env("EXT_DB_MIGRATION_URL")));
            if(args[0].equals("migrate")){
                stage("MIGRATE");try(var c=connect("R031_VERIFIER_USER","R031_VERIFIER_PASSWORD")){require(one(c,"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()").equals("0"));}
                try(var c=connect("EXT_DB_MIGRATION_USER","EXT_DB_MIGRATION_PASSWORD")){flyway().migrate();flyway().validate();history(c);}
                stage("MIGRATED");
            }else{require(args[0].equals("rotate"));rotation();}
            code=0;
        }catch(Throwable failure){/* Deliberately suppress unchecked exceptions and values. Parent reports only fixed stage. */}
        System.exit(code);
    }
}

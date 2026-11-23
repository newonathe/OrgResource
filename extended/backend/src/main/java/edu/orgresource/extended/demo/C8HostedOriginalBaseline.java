package edu.orgresource.extended.demo;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Operator-only, read-only database safeguard for original OrgResource data.
 * CAPTURE creates a mode-0600 local fingerprint manifest (never changes DB).
 * VERIFY checks all pre-existing V7 database rows under CAPTURE-time
 * per-table primary-key watermarks; new additive rows have higher IDs.
 * VERIFY checks the same ORIGINAL account, credential and organization rows
 * after fictional additive import. It does not replace an actual DB backup.
 */
public final class C8HostedOriginalBaseline {
    public static final String ARGUMENT = "--c8-hosted-original-baseline";
    private static final String VERSION = "orgresource-c8-original-baseline-v2-all-original-rows";
    // V7 persistent records. The non-id credential PK is user_id; CAPTURE
    // binds it to the pre-import user_account.id ceiling, including accounts
    // that did not previously have a local credential.
    private static final String[][] TABLES = {
            {"organization","id"}, {"user_account","id"}, {"local_credential","user_id"},
            {"account_recovery_code","id"}, {"membership","id"}, {"invitation","id"},
            {"ownership_transfer","id"}, {"audit_event","id"}, {"department","id"},
            {"project","id"}, {"resource","id"}, {"resource_requirement","id"},
            {"allocation","id"}, {"stock_movement","id"}, {"idempotency_key","id"}
    };

    private C8HostedOriginalBaseline() { }

    public static void run(String[] args, Map<String,String> env) throws Exception {
        if (args.length != 1 || !ARGUMENT.equals(args[0]))
            throw new IllegalArgumentException("Only the exact original-baseline command is supported.");
        String mode = required(env, "EXT_C8_BASELINE_MODE");
        if (!("CAPTURE".equals(mode) || "VERIFY".equals(mode)))
            throw new IllegalArgumentException("Mode must be CAPTURE or VERIFY.");
        String schema = required(env, "EXT_C8_HOSTED_EXPECTED_SCHEMA");
        String fingerprint = required(env, "EXT_C8_HOSTED_EXPECTED_FINGERPRINT");
        if (!schema.matches("orgresource_[a-z0-9_]{1,48}") || !fingerprint.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Explicit OrgResource schema and SHA-256 fingerprint required.");
        UUID org = UUID.fromString(required(env, "EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID"));
        UUID owner = UUID.fromString(required(env, "EXT_C8_HOSTED_OWNER_PUBLIC_ID"));
        String url = required(env, "EXT_DB_URL");
        if (!url.startsWith("jdbc:mysql://") || url.toLowerCase(java.util.Locale.ROOT).matches(".*(?:password|user)=.*"))
            throw new IllegalArgumentException("Use a MySQL JDBC URL without embedded credentials.");
        String keyHex = required(env, "EXT_C8_BASELINE_HMAC_HEX");
        if (!keyHex.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Use a private random 32-byte HMAC key (64 lowercase hex).");
        byte[] key = HexFormat.of().parseHex(keyHex);
        Path path = Path.of(required(env, "EXT_C8_BASELINE_FILE")).toAbsolutePath();
        try {
            try (Connection db = DriverManager.getConnection(url, required(env, "EXT_DB_USER"),
                    required(env, "EXT_DB_PASSWORD"))) {
                db.setReadOnly(true);
                db.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                db.setAutoCommit(false);
                String actualSchema = singleText(db, "SELECT DATABASE()");
                if (!schema.equals(actualSchema) || !singleText(db,"SELECT VERSION()").startsWith("8.0."))
                    throw new IllegalStateException("Baseline requires the confirmed MySQL 8.0 OrgResource schema.");
                String actualFingerprint = sha256(schema + "\u0000" + singleText(db,"SELECT @@server_uuid"));
                if (!fingerprint.equals(actualFingerprint))
                    throw new IllegalStateException("Original-data baseline connected to a different server or schema.");
                if (singleLong(db,"SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1") != 7)
                    throw new IllegalStateException("Only the verified Flyway V7 schema is supported.");
                long originalOrg = id(db, "SELECT id FROM organization WHERE public_id=UUID_TO_BIN(?,0) AND status='ACTIVE'",
                        org);
                long ownerAccount = id(db,"SELECT id FROM user_account WHERE public_id=UUID_TO_BIN(?,0) AND status='ACTIVE'",
                        owner);
                if (pairCount(db,originalOrg,ownerAccount) != 1)
                    throw new IllegalStateException("The original credentialed owner/organization pair differs.");
                Properties saved = null;
                if ("VERIFY".equals(mode)) {
                    if (!Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS)
                            || !PosixFilePermissions.toString(Files.getPosixFilePermissions(path)).equals("rw-------"))
                        throw new IllegalStateException("Baseline evidence file absent or not private mode 0600.");
                    saved = new Properties();
                    try(InputStream in=Files.newInputStream(path)) { saved.load(in); }
                    if (!VERSION.equals(saved.getProperty("version")))
                        throw new IllegalStateException("Original baseline version mismatch; STOP.");
                }
                Properties state = new Properties();
                state.setProperty("version", VERSION);
                state.setProperty("schema",schema);
                state.setProperty("fingerprint", fingerprint);
                state.setProperty("originalOrg", org.toString());
                state.setProperty("originalOwner", owner.toString());
                state.setProperty("tableCount",Integer.toString(TABLES.length));
                long originalUserCeiling = "CAPTURE".equals(mode)
                        ? maxKey(db,"user_account","id") : readCeiling(saved,"user_account.maxid");
                for (String[] item : TABLES) {
                    String table=item[0], pk=item[1];
                    long max = "CAPTURE".equals(mode)
                            ? ("local_credential".equals(table) ? originalUserCeiling : maxKey(db,table,pk))
                            : readCeiling(saved,table+".maxid");
                    TableDigest result = digestTable(db,table,pk,max,key);
                    state.setProperty(table+".maxid",Long.toString(max));
                    state.setProperty(table+".count",Integer.toString(result.rows));
                    state.setProperty(table+".hmac",result.hex);
                }
                db.rollback();
                if ("CAPTURE".equals(mode)) {
                    // A new evidence file only; refuse overwrites and existing symlinks.
                    Files.createFile(path,PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rw-------")));
                    try (OutputStream out=Files.newOutputStream(path)) {
                        state.store(out,"PRIVATE C8 original-state verification; contains no row values or credentials");
                    }
                    System.out.println("C8_ORIGINAL_BASELINE_CAPTURED: private mode-0600 manifest, "
                            + TABLES.length+" original-data tables. No database writes.");
                } else {
                    if (!saved.equals(state))
                        throw new IllegalStateException("C8_ORIGINAL_BASELINE_MISMATCH: Pre-existing V7 rows changed or were removed; STOP.");
                    System.out.println("C8_ORIGINAL_BASELINE_READ_ONLY_OK: all "+TABLES.length
                            +" pre-existing V7 tables match. This is not backup/restore evidence.");
                }
            }
        } finally {
            Arrays.fill(key,(byte)0);
        }
    }

    private static TableDigest digestTable(Connection db,String table,String pk,long ceiling,byte[] key) throws Exception {
        // Identifiers are hard-coded V7 table/PK literals, not operator input.
        Mac mac=Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key,"HmacSHA256"));
        update(mac,table.getBytes(StandardCharsets.UTF_8));
        int rows=0;
        try (PreparedStatement q=db.prepareStatement("SELECT * FROM "+table+" WHERE "+pk+"<=? ORDER BY "+pk)) {
            q.setLong(1,ceiling);
            try(ResultSet rs=q.executeQuery()) {
                ResultSetMetaData md=rs.getMetaData();
                while(rs.next()) {
                    rows=Math.addExact(rows,1);
                    mac.update((byte)0x51);
                    for(int col=1;col<=md.getColumnCount();col++) update(mac,rs.getBytes(col));
                }
            }
        }
        mac.update(ByteBuffer.allocate(4).putInt(rows).array());
        return new TableDigest(rows,HexFormat.of().formatHex(mac.doFinal()));
    }

    private static long maxKey(Connection db,String table,String pk) throws Exception {
        try(PreparedStatement q=db.prepareStatement("SELECT COALESCE(MAX("+pk+"),0) FROM "+table);
            ResultSet rs=q.executeQuery()) {
            if(!rs.next() || rs.getLong(1)<0) throw new IllegalStateException("Invalid original-data watermark.");
            return rs.getLong(1);
        }
    }

    private static long readCeiling(Properties state,String name) {
        String raw=state.getProperty(name);
        if(raw==null || !raw.matches("(?:0|[1-9][0-9]{0,18})"))
            throw new IllegalStateException("Invalid or missing original-data watermark.");
        try { return Long.parseLong(raw); }
        catch(NumberFormatException overflow) { throw new IllegalStateException("Original-data watermark overflow."); }
    }

    private static void update(Mac mac, byte[] bytes) {
        if (bytes==null) {
            mac.update(ByteBuffer.allocate(4).putInt(-1).array());
        } else {
            mac.update(ByteBuffer.allocate(4).putInt(bytes.length).array());
            mac.update(bytes);
        }
    }
    private static String singleText(Connection db,String sql) throws Exception {
        try(PreparedStatement q=db.prepareStatement(sql);ResultSet rs=q.executeQuery()) {
            if(!rs.next()) throw new IllegalStateException("Expected target identity missing.");
            return rs.getString(1);
        }
    }
    private static long singleLong(Connection db,String sql) throws Exception {
        try(PreparedStatement q=db.prepareStatement(sql);ResultSet rs=q.executeQuery()) {
            if(!rs.next()||rs.getObject(1)==null) throw new IllegalStateException("Expected migration missing.");
            return rs.getLong(1);
        }
    }
    private static long id(Connection db,String sql,UUID uuid) throws Exception {
        try(PreparedStatement q=db.prepareStatement(sql)) {
            q.setString(1,uuid.toString());
            try(ResultSet rs=q.executeQuery()) {
                if(!rs.next()) throw new IllegalStateException("Expected original identity missing.");
                long key=rs.getLong(1);
                if(rs.next()) throw new IllegalStateException("Ambiguous original identity.");
                return key;
            }
        }
    }
    private static long pairCount(Connection db,long org,long owner) throws Exception {
        try(PreparedStatement q=db.prepareStatement("""
                SELECT COUNT(*) FROM membership m JOIN local_credential c ON c.user_id=m.user_id
                WHERE m.org_id=? AND m.user_id=? AND m.role='OWNER' AND m.status='ACTIVE'
                """)) {
            q.setLong(1,org);
            q.setLong(2,owner);
            try(ResultSet rs=q.executeQuery()) { rs.next();return rs.getLong(1); }
        }
    }
    private static String sha256(String text) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }
    private static String required(Map<String,String> env,String key) {
        String value=env.get(key);
        if(value==null||value.isBlank()) throw new IllegalArgumentException("Missing original-baseline setting: "+key);
        return value;
    }
    private record TableDigest(int rows,String hex) { }
}

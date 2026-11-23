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
 * VERIFY checks the same ORIGINAL account, credential and organization rows
 * after fictional additive import. It does not replace an actual DB backup.
 */
public final class C8HostedOriginalBaseline {
    public static final String ARGUMENT = "--c8-hosted-original-baseline";
    private static final String VERSION = "orgresource-c8-original-baseline-v1";
    private static final String[][] TABLES = {
            {"organization", "id", "org"},
            {"user_account", "id", "owner"},
            {"local_credential", "user_id", "owner"},
            {"account_recovery_code", "user_id", "owner"},
            {"membership", "org_id", "org"},
            {"invitation", "org_id", "org"},
            {"ownership_transfer", "org_id", "org"},
            {"audit_event", "org_id", "org"},
            {"department", "organization_id", "org"},
            {"project", "organization_id", "org"},
            {"resource", "organization_id", "org"},
            {"resource_requirement", "organization_id", "org"},
            {"allocation", "organization_id", "org"},
            {"stock_movement", "organization_id", "org"},
            {"idempotency_key", "organization_id", "org"}
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
                Properties state = new Properties();
                state.setProperty("version", VERSION);
                state.setProperty("schema",schema);
                state.setProperty("fingerprint", fingerprint);
                state.setProperty("originalOrg", org.toString());
                state.setProperty("originalOwner", owner.toString());
                state.setProperty("tableCount",Integer.toString(TABLES.length));
                for (String[] item : TABLES) {
                    String table = item[0], column = item[1];
                    long selectedId = "owner".equals(item[2]) ? ownerAccount : originalOrg;
                    TableDigest digested = digestTable(db,table,column,selectedId,key);
                    state.setProperty(table+".count",Integer.toString(digested.rows));
                    state.setProperty(table+".hmac",digested.hex);
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
                    if (!Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS)
                            || !PosixFilePermissions.toString(Files.getPosixFilePermissions(path)).equals("rw-------"))
                        throw new IllegalStateException("Baseline evidence file is absent or not private mode 0600.");
                    Properties saved = new Properties();
                    try (InputStream in=Files.newInputStream(path)) { saved.load(in); }
                    if (!saved.equals(state))
                        throw new IllegalStateException("C8_ORIGINAL_BASELINE_MISMATCH: Original owner, credentials or organization data changed; STOP.");
                    System.out.println("C8_ORIGINAL_BASELINE_READ_ONLY_OK: all "+TABLES.length
                            +" original-state table fingerprints match. This is not backup/restore evidence.");
                }
            }
        } finally {
            Arrays.fill(key,(byte)0);
        }
    }

    private static TableDigest digestTable(Connection db,String table,String column,long id,byte[] key) throws Exception {
        // The identifiers are compiled-in fixed table and column strings; never user-controlled SQL.
        Mac mac=Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key,"HmacSHA256"));
        update(mac,table.getBytes(StandardCharsets.UTF_8));
        int rows=0;
        String sql="SELECT * FROM "+table+" WHERE "+column+"=? ORDER BY "
                + (table.equals("local_credential") ? "user_id" : "id");
        try (PreparedStatement q=db.prepareStatement(sql)) {
            q.setLong(1,id);
            try (ResultSet rs=q.executeQuery()) {
                ResultSetMetaData md=rs.getMetaData();
                while(rs.next()) {
                    rows=Math.addExact(rows,1);
                    mac.update((byte)0x51); // unambiguous row boundary
                    for(int col=1;col<=md.getColumnCount();col++) {
                        // getBytes preserves binary UUIDs, encrypted phone and hashed token
                        // values without exposing them as text or logs.
                        update(mac,rs.getBytes(col));
                    }
                }
            }
        }
        mac.update(ByteBuffer.allocate(4).putInt(rows).array());
        return new TableDigest(rows,HexFormat.of().formatHex(mac.doFinal()));
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

package edu.orgresource.extended.demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * No Spring application, Flyway, JPA, migrations, DML or hosted execution trigger.
 * A private operator must execute this read-only command using the exact approved
 * local plan and an appropriately scoped target-account connection.
 */
public final class C8HostedDemoPreflight {
    public static final String ARGUMENT = "--c8-hosted-demo-preflight";
    public static final String APPROVED_FULL_PLAN_SHA = "3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c";
    public static final String APPROVED_SOURCE_SHA = "57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2";
    private static final int MAX_BYTES = 32 * 1024 * 1024;
    private C8HostedDemoPreflight() { }

    public static void run(String[] args, Map<String, String> environment) throws Exception {
        if (args.length != 1 || !ARGUMENT.equals(args[0]))
            throw new IllegalArgumentException("Only the dedicated read-only hosted preflight flag is permitted.");
        String schema = required(environment, "EXT_C8_HOSTED_EXPECTED_SCHEMA");
        if (!schema.matches("orgresource_[a-z0-9_]{1,48}"))
            throw new IllegalArgumentException("The expected schema must be an explicit OrgResource schema.");
        UUID owner = UUID.fromString(required(environment, "EXT_C8_HOSTED_OWNER_PUBLIC_ID"));
        UUID originalOrg = UUID.fromString(required(environment, "EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID"));
        String url = required(environment, "EXT_DB_URL");
        if (!url.startsWith("jdbc:mysql://") || url.toLowerCase(java.util.Locale.ROOT).matches(".*(?:password|user)=.*"))
            throw new IllegalArgumentException("Use a MySQL JDBC URL without embedded user or password fields.");
        requireApprovedPlan(Path.of(required(environment, "EXT_C8_DEMO_PLAN")));

        // This is deliberately a separate JDBC read-only connection and never starts Spring.
        try (Connection db = DriverManager.getConnection(url, required(environment, "EXT_DB_USER"),
                required(environment, "EXT_DB_PASSWORD"))) {
            db.setReadOnly(true);
            String catalog = singleString(db, "SELECT DATABASE()");
            if (!schema.equals(catalog))
                throw new IllegalStateException("The connection did not select the explicitly expected schema.");
            String serverUuid = singleString(db, "SELECT @@server_uuid");
            String version = singleString(db, "SELECT VERSION()");
            if (!version.startsWith("8.0."))
                throw new IllegalStateException("The current Hosted C8 fixture was verified against MySQL 8.0 only.");
            long flyway = singleLong(db, "SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1");
            if (flyway != 7) throw new IllegalStateException("The existing schema must have exactly Flyway V7 applied.");
            long demo = singleLong(db, "SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL");
            if (demo != 0) throw new IllegalStateException("The target has an existing or partial demo cohort: refuse new import.");
            long original = queryCount(db, """
                    SELECT COUNT(*) FROM organization o JOIN membership m ON m.org_id=o.id
                    JOIN user_account u ON u.id=m.user_id JOIN local_credential c ON c.user_id=u.id
                    WHERE o.public_id=UUID_TO_BIN(?,0) AND u.public_id=UUID_TO_BIN(?,0)
                    AND o.status='ACTIVE' AND u.status='ACTIVE'
                    AND m.status='ACTIVE' AND m.role='OWNER'
                    """, originalOrg.toString(), owner.toString());
            if (original != 1) throw new IllegalStateException("The expected original owner/organization pair is unavailable.");
            long organizations = singleLong(db, "SELECT COUNT(*) FROM organization");
            if (organizations == 0) throw new IllegalStateException("This command is for an already populated database.");
            String fingerprint = sha256(schema + "\u0000" + serverUuid);
            System.out.println("HOSTED_DEMO_PREFLIGHT_READ_ONLY_OK");
            System.out.println("target_fingerprint_sha256=" + fingerprint);
            System.out.println("original_organization_count=" + organizations);
            System.out.println("flyway_version=7");
            System.out.println("approved_full_plan=96_workspaces_21060_requests");
            System.out.println("database_writes=0");
            System.out.println("LIMIT: Backup and isolated restore, live job access, runtime size, additive importer and independent review are NOT verified.");
        }
    }

    public static void requireApprovedPlan(Path planFile) throws Exception {
        long size = Files.size(planFile);
        if (size <= 0 || size > MAX_BYTES) throw new IllegalArgumentException("The plan size is outside the approved bound.");
        byte[] raw = Files.readAllBytes(planFile);
        if (!APPROVED_FULL_PLAN_SHA.equals(sha256(raw)))
            throw new IllegalArgumentException("The plan does not match the exact private full-plan SHA-256.");
        ObjectMapper parser = new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        JsonNode plan = parser.readTree(raw);
        if (!"orgresource-c8-plan-v1".equals(plan.path("generator").asText())
                || !"LOCAL_ONLY_DRY_RUN".equals(plan.path("scope").asText())
                || !plan.path("synthetic").asBoolean(false)
                || !APPROVED_SOURCE_SHA.equals(plan.path("source_sha256").asText())
                || !"2026-11-19".equals(plan.path("window").path("as_of").asText())
                || !plan.path("organizations").isArray() || plan.path("organizations").size() != 96)
            throw new IllegalArgumentException("The exact approved plan has inconsistent provenance.");
        String[] fields = {"departments", "personas", "projects", "resources", "requirements", "allocations", "events"};
        int[] totals = {248, 462, 713, 1312, 2139, 21060, 67370};
        int[] counted = new int[fields.length];
        HashSet<String> refs = new HashSet<>();
        HashSet<String> ids = new HashSet<>();
        for (JsonNode organization : plan.path("organizations")) {
            String ref = organization.path("reference_key").asText();
            String label = organization.path("directory_label").asText();
            String orgId = organization.path("id").asText();
            if (!refs.add(ref) || ref.isBlank() || label.isBlank() || label.length() > 120
                    || !ids.add(orgId) || !isUuid(orgId))
                throw new IllegalArgumentException("Approved plan has a repeated or malformed organization.");
            for (int i = 0; i < fields.length; i++) {
                JsonNode records = organization.path(fields[i]);
                if (!records.isArray()) throw new IllegalArgumentException("Missing synthetic records: " + fields[i]);
                counted[i] = Math.addExact(counted[i], records.size());
                for (JsonNode record : records) {
                    String id = record.path("id").asText();
                    if (!isUuid(id) || !ids.add(id))
                        throw new IllegalArgumentException("Repeated or malformed synthetic record ID.");
                }
            }
        }
        for (int i = 0; i < fields.length; i++) {
            if (counted[i] != totals[i] || plan.path("counts").path(fields[i]).asInt(-1) != totals[i])
                throw new IllegalArgumentException("The approved plan record totals differ from the expected scope.");
        }
    }

    private static boolean isUuid(String value) {
        try { UUID.fromString(value); return true; } catch (IllegalArgumentException invalid) { return false; }
    }

    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Required preflight variable is missing: " + name);
        return value;
    }

    private static String singleString(Connection db, String sql) throws Exception {
        try (PreparedStatement query = db.prepareStatement(sql); var result = query.executeQuery()) {
            if (!result.next()) throw new IllegalStateException("Read-only target validation returned no result.");
            return result.getString(1);
        }
    }

    private static long singleLong(Connection db, String sql) throws Exception {
        try (PreparedStatement query = db.prepareStatement(sql); var result = query.executeQuery()) {
            if (!result.next()) throw new IllegalStateException("Read-only target validation returned no count.");
            long value = result.getLong(1);
            if (result.wasNull()) throw new IllegalStateException("Target migration or count is missing.");
            return value;
        }
    }

    private static long queryCount(Connection db, String sql, String org, String owner) throws Exception {
        try (PreparedStatement query = db.prepareStatement(sql)) {
            query.setString(1, org); query.setString(2, owner);
            try (var result = query.executeQuery()) {
                if (!result.next()) throw new IllegalStateException("Missing original owner query result.");
                return result.getLong(1);
            }
        }
    }

    private static String sha256(String value) throws Exception {
        return sha256(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}

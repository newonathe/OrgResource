package edu.orgresource.extended.demo;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * SELECT-only independent reconciliation of the actual persisted demonstration.
 * The public hosted command accepts ONLY the exact SHA-approved 96-org plan.
 * The package-private verification method is also called by disposable MySQL CI.
 */
public final class C8HostedDemoPostflight {
    public static final String ARGUMENT = "--c8-hosted-demo-postflight";
    private C8HostedDemoPostflight() { }

    public static void run(String[] args, Map<String, String> env) throws Exception {
        if (args.length != 1 || !ARGUMENT.equals(args[0]))
            throw new IllegalArgumentException("Use only the read-only hosted postflight command.");
        String expectedFingerprint = required(env, "EXT_C8_HOSTED_EXPECTED_FINGERPRINT");
        if (!expectedFingerprint.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("The approved target fingerprint must be a SHA-256 digest.");
        String schema = required(env, "EXT_C8_HOSTED_EXPECTED_SCHEMA");
        if (!schema.matches("orgresource_[a-z0-9_]{1,48}"))
            throw new IllegalArgumentException("The expected schema must be explicit and constrained.");
        String url = required(env, "EXT_DB_URL");
        if (!url.startsWith("jdbc:mysql://") || url.toLowerCase(java.util.Locale.ROOT).matches(".*(?:password|user)=.*"))
            throw new IllegalArgumentException("Use a MySQL URL without embedded credentials.");
        String path = required(env, "EXT_C8_DEMO_PLAN");
        C8HostedDemoPreflight.requireApprovedPlan(Path.of(path));
        JsonNode plan = C8DemoCommand.readPlan(Path.of(path), C8HostedDemoPreflight.APPROVED_FULL_PLAN_SHA);
        UUID original = UUID.fromString(required(env, "EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID"));
        UUID owner = UUID.fromString(required(env, "EXT_C8_HOSTED_OWNER_PUBLIC_ID"));
        try (Connection db = DriverManager.getConnection(url, required(env, "EXT_DB_USER"),
                required(env, "EXT_DB_PASSWORD"))) {
            db.setReadOnly(true);
            db.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            db.setAutoCommit(false);
            String selected = oneText(db, "SELECT DATABASE()");
            if (!schema.equals(selected)) throw new IllegalStateException("The postflight connected to the wrong schema.");
            String version = oneText(db, "SELECT VERSION()");
            if (!version.startsWith("8.0.")) throw new IllegalStateException("The postflight requires the verified MySQL 8.0 schema.");
            String target = sha256(schema + "\u0000" + oneText(db, "SELECT @@server_uuid"));
            if (!expectedFingerprint.equals(target))
                throw new IllegalStateException("The postflight target differs from the expected preflight database.");
            Summary summary = verify(db, plan, C8HostedDemoPreflight.APPROVED_FULL_PLAN_SHA, original, owner);
            db.rollback();
            System.out.printf("HOSTED_DEMO_POSTFLIGHT_READ_ONLY_OK organizations=%d allocations=%d events=%d; independent browser/backup/owner-baseline acceptance still required.%n",
                    summary.organizations(), summary.allocations(), summary.events());
        }
    }

    /**
     * Independent SQL/JDBC comparator, also exercised after each disposable CI
     * additive replay. Its input never authorizes writes to any database.
     */
    static Summary verify(Connection db, JsonNode plan, String planSha, UUID originalId, UUID ownerId) throws Exception {
        if (plan == null || !plan.path("synthetic").asBoolean(false) || !plan.path("organizations").isArray()
                || !planSha.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("A hash-identified synthetic plan is required.");
        JsonNode workspaces = plan.path("organizations");
        String source = plan.path("source_sha256").asText();
        String dataset = plan.path("dataset_id").asText();
        String asOf = plan.path("window").path("as_of").asText();
        if (workspaces.isEmpty() || source.isBlank() || dataset.isBlank() || asOf.isBlank())
            throw new IllegalArgumentException("The plan is incomplete.");
        if (oneLong(db, "SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1") != 7)
            throw new IllegalStateException("The schema migration version changed.");
        if (oneLong(db, "SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL") != workspaces.size())
            throw new IllegalStateException("A partial or unexpected demo cohort was found.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM organization
                WHERE demo_plan_sha=? AND demo_dataset_id=? AND demo_source_sha=?
                  AND demo_as_of=? AND status='ACTIVE'
                """, planSha, dataset, source, asOf) != workspaces.size())
            throw new IllegalStateException("The demo provenance and expected cohort differ.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM organization o
                JOIN membership m ON m.org_id=o.id AND m.role='OWNER' AND m.status='ACTIVE'
                JOIN user_account u ON u.id=m.user_id AND u.status='ACTIVE'
                JOIN local_credential c ON c.user_id=u.id
                WHERE o.public_id=UUID_TO_BIN(?,0) AND u.public_id=UUID_TO_BIN(?,0)
                  AND o.status='ACTIVE' AND o.demo_dataset_id IS NULL
                """, originalId.toString(), ownerId.toString()) != 1)
            throw new IllegalStateException("The original credentialed owner and original active organization are missing.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM membership m
                JOIN organization o ON o.id=m.org_id
                JOIN user_account u ON u.id=m.user_id
                WHERE o.demo_plan_sha=? AND m.role='OWNER' AND m.status='ACTIVE'
                  AND u.public_id=UUID_TO_BIN(?,0) AND u.status='ACTIVE'
                """, planSha, ownerId.toString()) != workspaces.size())
            throw new IllegalStateException("The original owner cannot access every demonstration workspace.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM membership m
                JOIN organization o ON o.id=m.org_id
                WHERE o.demo_plan_sha=? AND m.role='OWNER' AND m.status='ACTIVE'
                """, planSha) != workspaces.size())
            throw new IllegalStateException("Each demo workspace must have exactly one active OWNER.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM user_account u JOIN local_credential c ON c.user_id=u.id
                WHERE u.username_key='c8-demo-historical-actor'
                """) != 0)
            throw new IllegalStateException("Historical synthetic actor acquired login credentials.");

        Map<String, JsonNode> expectedOrgs = new HashMap<>();
        for (JsonNode item : workspaces) {
            String key = item.path("reference_key").asText();
            if (key.isBlank() || expectedOrgs.putIfAbsent(key, item) != null)
                throw new IllegalArgumentException("Duplicate or missing planned organization reference.");
        }
        Set<String> observed = new HashSet<>();
        try (PreparedStatement statement = db.prepareStatement("""
                SELECT BIN_TO_UUID(public_id,0),demo_reference_key,display_name,demo_dataset_id,
                       demo_source_sha, demo_plan_sha, DATE_FORMAT(demo_as_of,'%Y-%m-%d'),status
                FROM organization WHERE demo_plan_sha=?
                """)) {
            statement.setString(1, planSha);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    String key = rows.getString(2);
                    JsonNode item = expectedOrgs.get(key);
                    if (item == null || !observed.add(key) || !item.path("id").asText().equals(rows.getString(1))
                            || !item.path("directory_label").asText().equals(rows.getString(3))
                            || !dataset.equals(rows.getString(4)) || !source.equals(rows.getString(5))
                            || !planSha.equals(rows.getString(6)) || !asOf.equals(rows.getString(7))
                            || !"ACTIVE".equals(rows.getString(8)))
                        throw new IllegalStateException("Persisted organization identity or exact public label differs from approved plan.");
                }
            }
        }
        if (observed.size() != workspaces.size())
            throw new IllegalStateException("An approved directory entry is missing.");

        String[] entities = {"department","project","resource","resource_requirement","allocation","membership","audit_event"};
        String[] fields = {"departments","projects","resources","requirements","allocations","personas","events"};
        for (int i=0; i<entities.length; i++) {
            String table = entities[i];
            String link = table.equals("membership") || table.equals("audit_event") ? "org_id" : "organization_id";
            String filter = table.equals("audit_event") ? " AND t.action LIKE 'C8_DEMO_%'" : "";
            Map<String, Long> counts = grouped(db, """
                    SELECT o.demo_reference_key,COUNT(*) FROM """ + table + """
                     t JOIN organization o ON o.id=t.""" + link + """
                     WHERE o.demo_plan_sha=?""" + filter + """
                     GROUP BY o.demo_reference_key
                    """, planSha);
            for (JsonNode org : workspaces) {
                String key = org.path("reference_key").asText();
                int planned = org.path(fields[i]).size() + (table.equals("membership") ? 1 : 0);
                if (counts.getOrDefault(key, 0L) != planned)
                    throw new IllegalStateException("Mismatch in persisted " + table + " count for demo workspace.");
            }
            if (counts.size() != workspaces.size())
                throw new IllegalStateException("An unexpected or missing demonstration group exists in " + table + ".");
        }

        comparePublicIds(db, workspaces, planSha, "project", "projects", false, ownerId);
        comparePublicIds(db, workspaces, planSha, "resource", "resources", false, ownerId);
        comparePublicIds(db, workspaces, planSha, "resource_requirement", "requirements", false, ownerId);
        comparePublicIds(db, workspaces, planSha, "allocation", "allocations", false, ownerId);
        comparePublicIds(db, workspaces, planSha, "membership", "personas", true, ownerId);

        Map<String, Long> expectedStates = new HashMap<>();
        plan.path("final_states").properties().forEach(entry -> expectedStates.put(entry.getKey(), entry.getValue().longValue()));
        Map<String, Long> actualStates = grouped(db, """
                SELECT a.status,COUNT(*) FROM allocation a JOIN organization o ON o.id=a.organization_id
                WHERE o.demo_plan_sha=? GROUP BY a.status
                """, planSha);
        if (!expectedStates.equals(actualStates))
            throw new IllegalStateException("The six final allocation statuses differ from the approved simulation.");

        Map<String, Long> expectedActions = new HashMap<>();
        Set<String> expectedMonths = new HashSet<>();
        long allocations = 0, events = 0;
        for (JsonNode org : workspaces) {
            allocations += org.path("allocations").size();
            events += org.path("events").size();
            for (JsonNode action : org.path("events")) {
                expectedActions.merge("C8_DEMO_" + action.path("action").asText(), 1L, Long::sum);
                expectedMonths.add(action.path("occurred_at").asText().substring(0,7));
            }
        }
        Map<String, Long> actualActions = grouped(db, """
                SELECT a.action,COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id
                WHERE o.demo_plan_sha=? AND a.action LIKE 'C8_DEMO_%' GROUP BY a.action
                """, planSha);
        if (!actualActions.equals(expectedActions))
            throw new IllegalStateException("The simulated historical action distribution differs from the approved plan.");
        Set<String> actualMonths = new HashSet<>();
        try (PreparedStatement q=db.prepareStatement("""
                SELECT DISTINCT DATE_FORMAT(a.created_at,'%Y-%m') FROM audit_event a
                JOIN organization o ON o.id=a.org_id
                WHERE o.demo_plan_sha=? AND a.action LIKE 'C8_DEMO_%'
                """)) {
            q.setString(1, planSha);
            try (ResultSet rows=q.executeQuery()) {
                while (rows.next()) actualMonths.add(rows.getString(1));
            }
        }
        if (!expectedMonths.equals(actualMonths))
            throw new IllegalStateException("The imported simulated calendar coverage differs from the plan.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id
                JOIN user_account u ON u.id=a.actor_user_id
                WHERE o.demo_plan_sha=? AND a.action='C8_DEMO_REQUEST'
                  AND u.username_key='c8-demo-historical-actor'
                """,planSha) != allocations)
            throw new IllegalStateException("Simulated REQUEST actions are not attributed to the synthetic operator.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id
                JOIN user_account u ON u.id=a.actor_user_id
                WHERE o.demo_plan_sha=? AND a.action LIKE 'C8_DEMO_%'
                  AND u.public_id=UUID_TO_BIN(?,0)
                """,planSha,ownerId.toString()) != 0)
            throw new IllegalStateException("The real original owner was attributed simulated history.");
        if (oneLong(db, """
                SELECT COUNT(*) FROM allocation a JOIN organization o ON o.id=a.organization_id
                WHERE o.demo_plan_sha=? AND (a.sms_consent OR a.phone_ciphertext IS NOT NULL)
                """,planSha) != 0)
            throw new IllegalStateException("The fictional dataset includes SMS consent or contact ciphertext.");

        if (oneLong(db, """
                SELECT COUNT(*) FROM resource r
                JOIN organization o ON o.id=r.organization_id AND o.demo_plan_sha=?
                LEFT JOIN (SELECT organization_id,resource_id,SUM(delta_total) total,SUM(delta_issued) issued
                    FROM stock_movement GROUP BY organization_id,resource_id) m
                    ON m.organization_id=r.organization_id AND m.resource_id=r.id
                LEFT JOIN (SELECT a.organization_id,a.resource_id,
                    SUM(CASE WHEN a.status='ISSUED' THEN a.quantity ELSE 0 END) issued,
                    SUM(CASE WHEN a.status='APPROVED' AND p.end_at>'2026-11-19 00:00:00'
                        THEN a.quantity ELSE 0 END) holds
                    FROM allocation a JOIN resource_requirement q ON q.id=a.requirement_id
                        AND q.organization_id=a.organization_id
                    JOIN project p ON p.id=q.project_id AND p.organization_id=a.organization_id
                    GROUP BY a.organization_id,a.resource_id) a
                    ON a.organization_id=r.organization_id AND a.resource_id=r.id
                WHERE r.total_quantity<>COALESCE(m.total,0)
                   OR COALESCE(m.issued,0)<>COALESCE(a.issued,0)
                   OR COALESCE(a.issued,0)+COALESCE(a.holds,0)>r.total_quantity
                """,planSha) != 0)
            throw new IllegalStateException("Imported inventory totals, holds, or issues fail physical conservation.");

        if (oneLong(db, """
                SELECT COUNT(*) FROM resource_requirement q
                JOIN organization o ON o.id=q.organization_id AND o.demo_plan_sha=?
                JOIN (SELECT a.organization_id,a.requirement_id,SUM(a.quantity) covered
                    FROM allocation a
                    JOIN resource_requirement rr ON rr.id=a.requirement_id AND rr.organization_id=a.organization_id
                    JOIN project p ON p.id=rr.project_id AND p.organization_id=a.organization_id
                    WHERE a.status IN ('ISSUED','RETURNED','CONSUMED')
                       OR (a.status='APPROVED' AND p.end_at>'2026-11-19 00:00:00')
                    GROUP BY a.organization_id,a.requirement_id) c
                    ON c.organization_id=q.organization_id AND c.requirement_id=q.id
                WHERE c.covered>q.quantity
                """,planSha) != 0)
            throw new IllegalStateException("Imported requirement coverage exceeds the approved quantity.");
        return new Summary(workspaces.size(), allocations, events);
    }

    private static void comparePublicIds(Connection db, JsonNode workspaces, String sha,
            String table, String field, boolean ignoreOriginalOwnerMembership, UUID owner) throws Exception {
        Set<String> expected = new HashSet<>();
        for (JsonNode org : workspaces) for (JsonNode row : org.path(field))
            if (!expected.add(row.path("id").asText()))
                throw new IllegalArgumentException("Repeated synthetic identifier in " + field + ".");
        String link = table.equals("membership") ? "org_id" : "organization_id";
        String ownerFilter = ignoreOriginalOwnerMembership
                ? " JOIN user_account u ON u.id=t.user_id AND u.public_id<>UUID_TO_BIN(?,0)" : "";
        String sql = "SELECT BIN_TO_UUID(t.public_id,0) FROM " + table +
                " t JOIN organization o ON o.id=t." + link + ownerFilter + " WHERE o.demo_plan_sha=?";
        try (PreparedStatement q=db.prepareStatement(sql)) {
            int index=1;
            if (ignoreOriginalOwnerMembership) q.setString(index++, owner.toString());
            q.setString(index,sha);
            try (ResultSet rows=q.executeQuery()) {
                while (rows.next()) if (!expected.remove(rows.getString(1)))
                    throw new IllegalStateException("An unexpected or duplicated synthetic " + field + " identifier was persisted.");
            }
        }
        if (!expected.isEmpty())
            throw new IllegalStateException("One or more approved " + field + " IDs were not persisted.");
    }

    private static Map<String, Long> grouped(Connection db, String sql, String sha) throws Exception {
        Map<String, Long> result = new HashMap<>();
        try (PreparedStatement q=db.prepareStatement(sql)) {
            q.setString(1,sha);
            try (ResultSet rows=q.executeQuery()) {
                while (rows.next()) {
                    String key=rows.getString(1);
                    if (key==null || result.putIfAbsent(key,rows.getLong(2))!=null)
                        throw new IllegalStateException("Unexpected grouped result from independent postflight.");
                }
            }
        }
        return result;
    }

    private static long oneLong(Connection db, String sql, String... args) throws Exception {
        try (PreparedStatement q=db.prepareStatement(sql)) {
            for (int i=0;i<args.length;i++) q.setString(i+1,args[i]);
            try (ResultSet rows=q.executeQuery()) {
                if (!rows.next()) throw new IllegalStateException("Postflight SQL did not return a count.");
                long value=rows.getLong(1);
                if (rows.wasNull()) throw new IllegalStateException("Postflight SQL count was null.");
                return value;
            }
        }
    }

    private static String oneText(Connection db, String sql) throws Exception {
        try (PreparedStatement q=db.prepareStatement(sql); ResultSet rows=q.executeQuery()) {
            if (!rows.next()) throw new IllegalStateException("Postflight SQL did not return a value.");
            return rows.getString(1);
        }
    }

    private static String required(Map<String,String> env,String key) {
        String s=env.get(key);
        if (s==null || s.isBlank()) throw new IllegalArgumentException("Postflight missing required setting: "+key);
        return s;
    }
    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    public record Summary(int organizations,long allocations,long events) { }
}

package edu.orgresource.extended.demo;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
        C8FlywayHistory.requireExactV7(db);
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
            // Table names and link columns come only from the fixed literals above.
            // Explicit spaces avoid text-block concatenation swallowing SQL boundaries.
            String groupedSql = "SELECT o.demo_reference_key,COUNT(*) FROM " + table
                    + " t JOIN organization o ON o.id=t." + link
                    + " WHERE o.demo_plan_sha=?" + filter + " GROUP BY o.demo_reference_key";
            Map<String, Long> counts = grouped(db, groupedSql, planSha);
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
        compareExactAllocations(db, workspaces, planSha);

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
        compareExactAuditHistory(db, workspaces, planSha);
        compareExpectedMovementKinds(db, workspaces, planSha);
        C8MovementHistory.verify(db, workspaces, planSha);
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



    /**
     * Independent per-allocation contract, including borrower/requirement/
     * resource links and exact final state derived from ordered plan events.
     * Mere global counts cannot detect two swapped allocation outcomes.
     */
    private static void compareExactAllocations(Connection db, JsonNode workspaces, String planSha) throws Exception {
        Map<String, AllocationSignature> expected = new HashMap<>();
        for (JsonNode org : workspaces) {
            String orgReference = org.path("reference_key").asText();
            Map<String,String> finalStates = new HashMap<>();
            for (JsonNode event : org.path("events")) {
                String allocation = event.path("allocation_id").asText();
                String action = event.path("action").asText();
                String state = switch (action) {
                    case "REQUEST" -> "PENDING";
                    case "APPROVE" -> "APPROVED";
                    case "ISSUE" -> "ISSUED";
                    case "RETURN" -> "RETURNED";
                    case "CONSUME" -> "CONSUMED";
                    case "CANCEL", "EXPIRE" -> "CANCELLED";
                    default -> throw new IllegalArgumentException("Unexpected synthetic allocation action.");
                };
                finalStates.put(allocation, state);
            }
            for (JsonNode allocation : org.path("allocations")) {
                String id = allocation.path("id").asText();
                String state = finalStates.remove(id);
                if (state == null) throw new IllegalArgumentException("A planned Allocation is missing its state.");
                AllocationSignature record = new AllocationSignature(orgReference, id,
                        allocation.path("requirement_id").asText(), allocation.path("resource_id").asText(),
                        allocation.path("borrower_id").asText(), allocation.path("quantity").asInt(-1), state);
                if (expected.putIfAbsent(orgReference + "\u0000" + id, record) != null)
                    throw new IllegalArgumentException("A planned Allocation ID is duplicated.");
            }
            if (!finalStates.isEmpty())
                throw new IllegalArgumentException("The plan has actions for a missing Allocation.");
        }
        try (PreparedStatement query = db.prepareStatement("""
                SELECT o.demo_reference_key,BIN_TO_UUID(a.public_id,0),
                       BIN_TO_UUID(q.public_id,0),BIN_TO_UUID(r.public_id,0),
                       BIN_TO_UUID(m.public_id,0),a.quantity,a.status
                FROM allocation a
                JOIN organization o ON o.id=a.organization_id
                JOIN resource_requirement q ON q.id=a.requirement_id
                    AND q.organization_id=a.organization_id
                JOIN resource r ON r.id=a.resource_id
                    AND r.organization_id=a.organization_id
                JOIN membership m ON m.id=a.borrower_membership_id
                    AND m.org_id=a.organization_id
                WHERE o.demo_plan_sha=?
                """)) {
            query.setString(1, planSha);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    String reference = rows.getString(1), id = rows.getString(2);
                    AllocationSignature planned = expected.remove(reference + "\u0000" + id);
                    if (planned == null || !planned.requirementId().equals(rows.getString(3))
                            || !planned.resourceId().equals(rows.getString(4))
                            || !planned.borrowerMembershipId().equals(rows.getString(5))
                            || planned.quantity() != rows.getInt(6)
                            || !planned.finalState().equals(rows.getString(7)))
                        throw new IllegalStateException("Persisted allocation fields or final state differ from the approved plan.");
                }
            }
        }
        if (!expected.isEmpty())
            throw new IllegalStateException("Some approved allocations are missing or lack correct linked membership IDs.");
    }

    private record AllocationSignature(String reference, String allocationId, String requirementId,
                                       String resourceId, String borrowerMembershipId, int quantity,
                                       String finalState) { }

    /**
     * Verify every historical event against the exact approved plan. The replay
     * does not store the plan's operation UUID on the audit row, so the stable
     * comparison key is tenant + action + allocation target + UTC second +
     * synthetic actor. Multiplicity detects duplicates; no actual names leak.
     */
    private static void compareExactAuditHistory(Connection db, JsonNode workspaces, String planSha) throws Exception {
        var utcFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        var manila = ZoneId.of("Asia/Manila");
        Map<AuditSignature, Integer> pending = new HashMap<>();
        int plannedEvents = 0;
        for (JsonNode org : workspaces) {
            String reference = org.path("reference_key").asText();
            Map<String, String> actors = new HashMap<>();
            for (JsonNode persona : org.path("personas")) {
                String id = persona.path("id").asText();
                String username = "OWNER".equals(persona.path("role").asText())
                        ? "c8-demo-historical-actor" : "demo-" + id;
                if (actors.putIfAbsent(id, username) != null)
                    throw new IllegalArgumentException("Repeated synthetic actor identifier.");
            }
            for (JsonNode event : org.path("events")) {
                String actor = actors.get(event.path("actor_id").asText());
                if (actor == null) throw new IllegalArgumentException("A simulated historical actor is missing.");
                String timestamp = LocalDateTime.parse(event.path("occurred_at").asText())
                        .atZone(manila).withZoneSameInstant(ZoneOffset.UTC).format(utcFormat);
                AuditSignature expected = new AuditSignature(reference,
                        "C8_DEMO_" + event.path("action").asText(),
                        event.path("allocation_id").asText(), timestamp, actor);
                pending.merge(expected, 1, Integer::sum);
                plannedEvents = Math.addExact(plannedEvents, 1);
            }
        }
        int counted = 0;
        try (PreparedStatement query = db.prepareStatement("""
                SELECT o.demo_reference_key,a.action,a.target_id,
                       DATE_FORMAT(a.created_at,'%Y-%m-%dT%H:%i:%s.%f'),
                       u.username_key,a.target_type,a.outcome
                FROM audit_event a
                JOIN organization o ON o.id=a.org_id
                LEFT JOIN user_account u ON u.id=a.actor_user_id
                WHERE o.demo_plan_sha=? AND a.action LIKE 'C8_DEMO_%'
                """)) {
            query.setString(1, planSha);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    if (!"allocation".equals(rows.getString(6)) || !"SUCCESS".equals(rows.getString(7)))
                        throw new IllegalStateException("The simulated history has an invalid audit target or outcome.");
                    AuditSignature observed = new AuditSignature(rows.getString(1), rows.getString(2),
                            rows.getString(3), rows.getString(4), rows.getString(5));
                    Integer remaining = pending.get(observed);
                    if (remaining == null || remaining <= 0)
                        throw new IllegalStateException("A historical audit event differs from the approved plan.");
                    if (remaining == 1) pending.remove(observed);
                    else pending.put(observed, remaining - 1);
                    counted = Math.addExact(counted, 1);
                }
            }
        }
        if (counted != plannedEvents || !pending.isEmpty())
            throw new IllegalStateException("One or more approved simulated historical events are missing.");
    }

    private record AuditSignature(String reference, String action, String allocationId,
                                  String utcTime, String actorUsername) { }

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

    /**
     * Independent C8 simulation constraint: resource setup produces INITIAL,
     * and only ISSUE, RETURN and CONSUME historical events create movements.
     * Reject extras even when compensating deltas preserve the net stock total.
     * Ordinary inventory behavior is unchanged; only C8 plan postflight uses
     * this source-derived count and kind oracle.
     */
    private static void compareExpectedMovementKinds(Connection db, JsonNode workspaces, String planSha)
            throws Exception {
        Map<String, Long> expected = new HashMap<>();
        for (JsonNode org : workspaces) {
            String ref = org.path("reference_key").asText();
            if (org.path("resources").size() > 0)
                expected.put(ref + "\u0000INITIAL", (long) org.path("resources").size());
            for (JsonNode event : org.path("events")) {
                String action = event.path("action").asText();
                if ("ISSUE".equals(action) || "RETURN".equals(action) || "CONSUME".equals(action))
                    expected.merge(ref + "\u0000" + action, 1L, Long::sum);
            }
        }
        Map<String, Long> actual = new HashMap<>();
        try (PreparedStatement query = db.prepareStatement("""
                SELECT o.demo_reference_key,m.kind,COUNT(*)
                FROM stock_movement m JOIN organization o ON o.id=m.organization_id
                WHERE o.demo_plan_sha=?
                GROUP BY o.demo_reference_key,m.kind
                """)) {
            query.setString(1, planSha);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    String kind = rows.getString(2);
                    String key = rows.getString(1) + "\u0000" + kind;
                    if (rows.getString(1) == null || kind == null
                            || actual.putIfAbsent(key, rows.getLong(3)) != null)
                        throw new IllegalStateException("C8_STOCK_MOVEMENT_HISTORY_MISMATCH: Invalid movement group.");
                }
            }
        }
        if (!expected.equals(actual))
            throw new IllegalStateException(
                    "C8_STOCK_MOVEMENT_HISTORY_MISMATCH: Movement kinds/counts differ from the approved simulation.");
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

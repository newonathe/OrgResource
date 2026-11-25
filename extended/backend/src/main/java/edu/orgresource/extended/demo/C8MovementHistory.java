package edu.orgresource.extended.demo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * C8-only, SELECT-only comparator of synthetic movement rows against the
 * approved replay plan. Runtime inventory policies and V1-V7 are unchanged.
 * Movement public_id is random by design and cannot be plan-derived; the
 * stable operation UUID, actor, links, values, timestamp and canonical request
 * fingerprint must nevertheless match exactly.
 */
final class C8MovementHistory {
    private static final String ERROR = "C8_STOCK_MOVEMENT_HISTORY_MISMATCH";
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private static final String OPENING_UTC = asUtc("2025-12-01T08:00:00");

    private C8MovementHistory() { }

    static void verify(Connection db, JsonNode workspaces, String planSha) throws Exception {
        Map<String, PlannedMovement> expected = new HashMap<>();
        for (JsonNode org : workspaces) {
            String ref = org.path("reference_key").asText();
            String openingActor = null;
            for (JsonNode persona : org.path("personas")) {
                if ("OWNER".equals(persona.path("role").asText())) {
                    if (openingActor != null)
                        throw new IllegalArgumentException("Multiple synthetic owners in one workspace.");
                    openingActor = persona.path("id").asText();
                }
            }
            if (openingActor == null)
                throw new IllegalArgumentException("No synthetic owner in approved workspace.");

            for (JsonNode resource : org.path("resources")) {
                String resourceId = resource.path("id").asText();
                int quantity = resource.path("initial_quantity").asInt(-1);
                if (quantity < 1)
                    throw new IllegalArgumentException("Opening resource quantity must be positive.");
                String operation = UUID.nameUUIDFromBytes(
                        ("c8-opening:" + resourceId).getBytes(StandardCharsets.UTF_8)).toString();
                add(expected, new PlannedMovement(ref, operation, "INITIAL", resourceId,
                        null, openingActor, quantity, quantity, 0, OPENING_UTC, null));
            }

            Map<String, JsonNode> allocations = new HashMap<>();
            for (JsonNode allocation : org.path("allocations")) {
                String allocationId = allocation.path("id").asText();
                if (allocations.putIfAbsent(allocationId, allocation) != null)
                    throw new IllegalArgumentException("Duplicate synthetic allocation.");
            }
            for (JsonNode event : org.path("events")) {
                String action = event.path("action").asText();
                if (!"ISSUE".equals(action) && !"RETURN".equals(action) && !"CONSUME".equals(action))
                    continue;
                String allocationId = event.path("allocation_id").asText();
                JsonNode allocation = allocations.get(allocationId);
                if (allocation == null)
                    throw new IllegalArgumentException("Movement refers to an absent allocation.");
                int quantity = allocation.path("quantity").asInt(-1);
                if (quantity < 1)
                    throw new IllegalArgumentException("Invalid synthetic movement quantity.");
                String operation = event.path("id").asText();
                long total = "CONSUME".equals(action) ? -quantity : 0;
                long issued = "ISSUE".equals(action) ? quantity : "RETURN".equals(action) ? -quantity : 0;
                String fingerprint = sha256("c8-demo|" + planSha + "|" + action + "|" + allocationId + "|" + operation);
                add(expected, new PlannedMovement(ref, operation, action,
                        allocation.path("resource_id").asText(), allocationId,
                        event.path("actor_id").asText(), quantity, total, issued,
                        asUtc(event.path("occurred_at").asText()), fingerprint));
            }
        }

        try (PreparedStatement query = db.prepareStatement("""
                SELECT o.demo_reference_key,BIN_TO_UUID(s.operation_public_id,0),s.kind,
                       BIN_TO_UUID(r.public_id,0),BIN_TO_UUID(a.public_id,0),
                       BIN_TO_UUID(m.public_id,0),s.quantity,s.delta_total,s.delta_issued,
                       DATE_FORMAT(s.occurred_at,'%Y-%m-%dT%H:%i:%s.%f'),
                       LOWER(HEX(s.request_fingerprint)),o.id,r.id
                FROM stock_movement s
                JOIN organization o ON o.id=s.organization_id
                JOIN resource r ON r.id=s.resource_id AND r.organization_id=s.organization_id
                LEFT JOIN allocation a ON a.id=s.allocation_id AND a.organization_id=s.organization_id
                JOIN membership m ON m.id=s.actor_membership_id AND m.org_id=s.organization_id
                WHERE o.demo_plan_sha=?
                """)) {
            query.setString(1, planSha);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    String ref = rows.getString(1), operation = rows.getString(2);
                    PlannedMovement planned = expected.remove(ref + "\u0000" + operation);
                    if (planned == null)
                        throw new IllegalStateException(ERROR + ": Unexpected movement operation.");
                    // Opening request fingerprints depend on the actual database
                    // surrogate IDs; these are verified through the joined,
                    // plan-matched Resource and Organization rows.
                    String fingerprint = planned.fingerprint() != null ? planned.fingerprint()
                            : sha256("POST:/api/resources|" + rows.getLong(12) + "|"
                                    + rows.getLong(13) + "|" + planned.quantity());
                    if (!planned.kind().equals(rows.getString(3))
                            || !planned.resourceId().equals(rows.getString(4))
                            || !Objects.equals(planned.allocationId(), rows.getString(5))
                            || !planned.actorId().equals(rows.getString(6))
                            || planned.quantity() != rows.getInt(7)
                            || planned.deltaTotal() != rows.getLong(8)
                            || planned.deltaIssued() != rows.getLong(9)
                            || !planned.utcTime().equals(rows.getString(10))
                            || !fingerprint.equals(rows.getString(11)))
                        throw new IllegalStateException(ERROR + ": Persisted movement fields differ from replay.");
                }
            }
        }
        if (!expected.isEmpty())
            throw new IllegalStateException(ERROR + ": One or more approved movements are missing.");
    }

    private static void add(Map<String, PlannedMovement> expected, PlannedMovement movement) {
        if (expected.putIfAbsent(movement.orgRef() + "\u0000" + movement.operationId(), movement) != null)
            throw new IllegalArgumentException("Repeated movement operation in the approved plan.");
    }

    private static String asUtc(String manilaLocal) {
        return LocalDateTime.parse(manilaLocal).atZone(MANILA)
                .withZoneSameInstant(ZoneOffset.UTC).format(FORMAT);
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private record PlannedMovement(String orgRef, String operationId, String kind, String resourceId,
                                   String allocationId, String actorId, int quantity,
                                   long deltaTotal, long deltaIssued, String utcTime, String fingerprint) { }
}

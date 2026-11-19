package edu.orgresource.extended.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.identity.Membership;
import edu.orgresource.extended.security.C1OrganizationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class ReportingComponent {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAX_EXPORT_ROWS = 1_000;
    private static final int MAX_EXPORT_BYTES = 4 * 1024 * 1024;
    private static final Comparator<Event> NEWEST = Comparator.comparing(Event::at)
            .thenComparing(Event::source).thenComparingLong(Event::id).reversed();

    public enum Format { CSV, TENANT_JSON }
    public enum Dataset { RESOURCES, MOVEMENTS, AUDIT }
    private enum Source {
        AUDIT("audit_event", "org_id", "created_at"),
        MOVEMENT("stock_movement", "organization_id", "occurred_at");

        final String table;
        final String scope;
        final String time;
        Source(String table, String scope, String time) {
            this.table = table; this.scope = scope; this.time = time;
        }
    }

    public record EventView(String id, String source, String action, String targetType, String outcome,
                            String requestId, String resourceId, Integer quantity, Integer deltaTotal,
                            Integer deltaIssued, Instant occurredAt) { }
    public record EventPage(List<EventView> items, String nextCursor) { }
    public record ResourceRow(String id, String name, String type, int totalQuantity) { }
    public record ExportDocument(int schemaVersion, UUID organizationId, Dataset dataset, Instant exportedAt, List<?> rows) { }
    public record ExportFile(Format format, Dataset dataset, Instant exportedAt, byte[] bytes) { }
    private record Position(Source source, long id, LocalDateTime at) { }
    private record Event(Source source, long id, LocalDateTime at, EventView view) { }

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final Clock clock;
    private final C1OrganizationContext organization;

    public ReportingComponent(JdbcTemplate jdbc, ObjectMapper json, Clock clock, C1OrganizationContext organization) {
        this.jdbc = jdbc; this.json = json; this.clock = clock; this.organization = organization;
    }

    public EventPage events(UUID organizationId, int limit, String cursor, boolean auditOnly) {
        long org = requireScope(organizationId);
        if (limit < 1 || limit > 100)
            throw new C1ApiException(400, "INVALID_PAGE_SIZE", "Page size must be between 1 and 100.");
        Position after = position(org, cursor, auditOnly);
        List<Event> rows = new ArrayList<>(readEvents(org, Source.AUDIT, after, limit + 1));
        if (!auditOnly) rows.addAll(readEvents(org, Source.MOVEMENT, after, limit + 1));
        rows.sort(NEWEST);
        int count = Math.min(rows.size(), limit);
        Event last = count == 0 ? null : rows.get(count - 1);
        String next = rows.size() > limit ? cursor(last) : null;
        return new EventPage(rows.subList(0, count).stream().map(Event::view).toList(), next);
    }

    public ExportFile export(UUID organizationId, InputStream input) {
        long org = requireScope(organizationId);
        if (input == null) throw invalidExport();
        JsonNode body;
        try {
            byte[] bounded = input.readNBytes(1025);
            if (bounded.length > 1024) throw invalidExport();
            body = json.reader().with(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(bounded);
        } catch (IOException invalid) {
            throw invalidExport();
        }
        if (body == null || !body.isObject() || body.size() != 2 ||
                !body.path("format").isTextual() || !body.path("dataset").isTextual())
            throw invalidExport();
        Format format;
        Dataset dataset;
        try {
            format = Format.valueOf(body.path("format").textValue());
            dataset = Dataset.valueOf(body.path("dataset").textValue());
        } catch (IllegalArgumentException invalid) {
            throw new C1ApiException(400, "INVALID_EXPORT_REQUEST", "Choose a supported export format and dataset.");
        }
        Instant at = clock.instant();
        List<?> rows = switch (dataset) {
            case RESOURCES -> jdbc.query("""
                    SELECT BIN_TO_UUID(public_id,0), name, type, total_quantity
                    FROM resource WHERE organization_id=? ORDER BY id LIMIT ?
                    """, (rs, row) -> new ResourceRow(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)),
                    org, MAX_EXPORT_ROWS + 1);
            case AUDIT -> readEvents(org, Source.AUDIT, null, MAX_EXPORT_ROWS + 1).stream().map(Event::view).toList();
            case MOVEMENTS -> readEvents(org, Source.MOVEMENT, null, MAX_EXPORT_ROWS + 1).stream().map(Event::view).toList();
        };
        if (rows.size() > MAX_EXPORT_ROWS) throw exportTooLarge();
        byte[] bytes;
        try {
            bytes = format == Format.CSV ? csv(dataset, at, rows)
                    : json.writeValueAsBytes(new ExportDocument(SCHEMA_VERSION, organizationId, dataset, at, rows));
        } catch (JsonProcessingException invalid) {
            throw new IllegalStateException("Export serialization failed.", invalid);
        }
        requireExportByteLimit(bytes.length);
        return new ExportFile(format, dataset, at, bytes);
    }

    static void requireExportByteLimit(int bytes) {
        if (bytes > MAX_EXPORT_BYTES) throw exportTooLarge();
    }

    private long requireScope(UUID pathOrganization) {
        Membership membership = organization.requirePathOrganization(pathOrganization, false);
        if (!organization.currentOrganizationId().equals(membership.organizationId))
            throw new C1ApiException(404, "NOT_FOUND", "The requested organization was not found.");
        if (membership.role != Membership.Role.OWNER && membership.role != Membership.Role.ADMIN)
            throw new C1ApiException(403, "FORBIDDEN", "The current membership cannot perform this action.");
        return membership.organizationId;
    }

    private Position position(long org, String cursor, boolean auditOnly) {
        if (cursor == null) return null;
        try {
            if (cursor.length() > 64 || !cursor.matches("[A-Za-z0-9_-]+")) throw invalidCursor();
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.US_ASCII);
            if (!encoded(decoded).equals(cursor)) throw invalidCursor();
            String[] fields = decoded.split(":", -1);
            if (fields.length != 2 || !fields[1].matches("[1-9][0-9]{0,18}")) throw invalidCursor();
            Source source = Source.valueOf(fields[0]);
            if (auditOnly && source != Source.AUDIT) throw invalidCursor();
            long id = Long.parseLong(fields[1]);
            Position match = jdbc.query("SELECT " + source.time + " FROM " + source.table
                            + " WHERE " + source.scope + "=? AND id=?",
                    rs -> rs.next() ? new Position(source, id, rs.getObject(1, LocalDateTime.class)) : null, org, id);
            if (match == null) throw invalidCursor();
            return match;
        } catch (IllegalArgumentException invalid) {
            throw invalidCursor();
        }
    }

    private List<Event> readEvents(long org, Source source, Position after, int limit) {
        String select = source == Source.AUDIT
                ? "m.action,m.target_type,m.outcome,m.request_id"
                : "m.kind,BIN_TO_UUID(r.public_id,0),m.quantity,m.delta_total,m.delta_issued";
        String join = source == Source.AUDIT ? "" : " JOIN resource r ON r.id=m.resource_id AND r.organization_id=m.organization_id";
        String time = "m." + source.time;
        String sql = "SELECT m.id," + time + "," + select + " FROM " + source.table + " m" + join
                + " WHERE m." + source.scope + "=?";
        List<Object> arguments = new ArrayList<>();
        arguments.add(org);
        if (after != null) {
            if (source == after.source()) {
                sql += " AND (" + time + "<? OR (" + time + "=? AND m.id<?))";
                arguments.add(after.at()); arguments.add(after.at()); arguments.add(after.id());
            } else {
                sql += " AND " + time + (source.compareTo(after.source()) < 0 ? "<=?" : "<?");
                arguments.add(after.at());
            }
        }
        arguments.add(limit);
        return jdbc.query(sql + " ORDER BY " + time + " DESC,m.id DESC LIMIT ?",
                (rs, row) -> event(source, rs), arguments.toArray());
    }

    private static Event event(Source source, ResultSet rs) throws SQLException {
        long id = rs.getLong(1);
        LocalDateTime at = rs.getObject(2, LocalDateTime.class);
        String eventId = encoded(source.name() + ":" + id);
        EventView view = source == Source.AUDIT
                ? new EventView(eventId, source.name(), rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getString(6), null, null, null, null, at.toInstant(ZoneOffset.UTC))
                : new EventView(eventId, source.name(), rs.getString(3), "resource", null, null,
                        rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), at.toInstant(ZoneOffset.UTC));
        return new Event(source, id, at, view);
    }

    private static byte[] csv(Dataset dataset, Instant at, List<?> rows) {
        String fields = switch (dataset) {
            case RESOURCES -> "id,name,type,totalQuantity";
            case AUDIT -> "id,source,action,targetType,outcome,requestId,occurredAt";
            case MOVEMENTS -> "id,source,action,resourceId,quantity,deltaTotal,deltaIssued,occurredAt";
        };
        StringBuilder text = new StringBuilder("schemaVersion,exportedAt,").append(fields).append("\r\n");
        for (Object row : rows) {
            text.append(SCHEMA_VERSION);
            cell(text, at.toString());
            if (row instanceof ResourceRow resource) {
                cell(text, resource.id()); cell(text, resource.name()); cell(text, resource.type());
                cell(text, resource.totalQuantity());
            } else {
                EventView event = (EventView) row;
                cell(text, event.id()); cell(text, event.source()); cell(text, event.action());
                if (dataset == Dataset.AUDIT) {
                    cell(text, event.targetType()); cell(text, event.outcome()); cell(text, event.requestId());
                } else {
                    cell(text, event.resourceId()); cell(text, event.quantity());
                    cell(text, event.deltaTotal()); cell(text, event.deltaIssued());
                }
                cell(text, event.occurredAt().toString());
            }
            text.append("\r\n");
        }
        return text.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void cell(StringBuilder csv, Object value) {
        csv.append(',');
        if (value == null) return;
        if (value instanceof Number number) {
            csv.append(number);
            return;
        }
        String text = value.toString();
        csv.append('"');
        int offset = 0;
        boolean controlPrefix = false;
        while (offset < text.length()) {
            int cp = text.codePointAt(offset);
            boolean control = Character.isISOControl(cp) || Character.getType(cp) == Character.FORMAT;
            if (!control && !Character.isWhitespace(cp) && !Character.isSpaceChar(cp)) break;
            controlPrefix |= control;
            offset += Character.charCount(cp);
        }
        if (controlPrefix || offset < text.length() && "=+-@".indexOf(text.charAt(offset)) >= 0) csv.append('\'');
        csv.append(text.replace("\"", "\"\"")).append('"');
    }

    private static String cursor(Event event) { return event.view().id(); }
    private static String encoded(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.US_ASCII));
    }
    private static C1ApiException invalidCursor() {
        return new C1ApiException(400, "INVALID_CURSOR", "The event cursor is invalid for this feed.");
    }
    private static C1ApiException exportTooLarge() {
        return new C1ApiException(413, "EXPORT_TOO_LARGE", "The export exceeds 1,000 rows or 4 MiB. No file was generated.");
    }
    private static C1ApiException invalidExport() {
        return new C1ApiException(400, "INVALID_EXPORT_REQUEST", "Provide exactly one format and one dataset in a JSON body of at most 1 KiB.");
    }
}

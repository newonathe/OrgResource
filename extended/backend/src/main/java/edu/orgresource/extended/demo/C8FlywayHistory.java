package edu.orgresource.extended.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Read-only C8 V7 release guard. The old MAX(successful version) check accepts
 * incomplete or dirty history; exact metadata and Flyway's packaged-script
 * validation are both necessary. This class never calls migrate or repair.
 */
public final class C8FlywayHistory {
    private static final List<String> EXPECTED_SCRIPTS = List.of(
            "V1__extended_c1_baseline.sql",
            "V2__spring_session_jdbc_mysql.sql",
            "V3__atomic_inventory_ledger.sql",
            "V4__c3_contact_images_and_reminders.sql",
            "V5__c4_reminder_dispatch_boundary.sql",
            "V6__c8_project_leads_and_linked_borrowers.sql",
            "V7__c8_local_demo_provenance.sql");

    private C8FlywayHistory() { }

    public static void requireExactV7(JdbcTemplate jdbc) {
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            requireExactV7(connection);
            return null;
        });
    }

    public static void requireExactV7(Connection db) {
        try {
            int count = 0;
            try (PreparedStatement query = db.prepareStatement("""
                    SELECT version,type,script,checksum,success
                    FROM flyway_schema_history ORDER BY installed_rank
                    """);
                 ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    if (count >= EXPECTED_SCRIPTS.size()
                            || !Integer.toString(count + 1).equals(rows.getString(1))
                            || !"SQL".equals(rows.getString(2))
                            || !EXPECTED_SCRIPTS.get(count).equals(rows.getString(3))
                            || rows.getObject(4) == null || !rows.getBoolean(5))
                        throw new IllegalStateException("Unexpected version, script, checksum, or failed migration.");
                    count++;
                }
            }
            if (count != EXPECTED_SCRIPTS.size())
                throw new IllegalStateException("Missing V1–V7 migration record.");

            // Flyway checks stored CRC32 values against the exact packaged SQL
            // migrations (and their names, types and resolved order). A
            // suppress-close wrapper keeps the caller's SELECT-only connection.
            var result = Flyway.configure()
                    .dataSource(new SingleConnectionDataSource(db, true))
                    .locations("classpath:db/migration")
                    .createSchemas(false)
                    .validateWithResult();
            if (!result.validationSuccessful)
                throw new IllegalStateException("Flyway packaged migration validation failed.");
        } catch (Exception failure) {
            throw new IllegalStateException("C8_FLYWAY_HISTORY_INVALID: The exact applied V1–V7 migration history is not valid.", failure);
        }
    }
}

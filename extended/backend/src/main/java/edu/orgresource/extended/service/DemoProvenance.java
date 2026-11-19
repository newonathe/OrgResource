package edu.orgresource.extended.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.C1ApiException;

@Component
public class DemoProvenance {
    public static final String NOTICE = "DEMO ENVIRONMENT — Any real organization labels come from public listings. All operations, dates, accounts, inventories and transaction history are simulated. No affiliation, adoption or endorsement is implied.";
    private final JdbcTemplate jdbc;

    public DemoProvenance(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Metadata publicMetadata() {
        return jdbc.query("""
                SELECT COUNT(*) AS organizations, COUNT(demo_dataset_id) AS tagged,
                    MIN(demo_dataset_id) AS dataset_id, MIN(demo_as_of) AS as_of_date,
                    COUNT(DISTINCT demo_dataset_id)=1 AND COUNT(DISTINCT demo_source_sha)=1
                        AND COUNT(DISTINCT demo_plan_sha)=1 AND COUNT(DISTINCT demo_as_of)=1 AS single_cohort,
                    EXISTS (SELECT 1 FROM user_account u JOIN local_credential c ON c.user_id=u.id
                        WHERE u.username_key='c8-demo-operator' AND u.status='ACTIVE') AS operator_ready
                FROM organization
                """, rows -> {
            if (!rows.next()) throw unavailable();
            long tagged = rows.getLong("tagged");
            if (tagged == 0) return new Metadata(false, null, null, null);
            String dataset = rows.getString("dataset_id");
            if (!dataset.matches("orgresource-c8-plan-v1-[0-9a-f]{12}-[0-9a-f]{12}(-small|-intermediate)?"))
                throw unavailable();
            int expected = dataset.endsWith("-small") ? 3 : dataset.endsWith("-intermediate") ? 8 : 96;
            if (tagged != expected || tagged != rows.getLong("organizations")
                    || !rows.getBoolean("single_cohort") || !rows.getBoolean("operator_ready")) throw unavailable();
            return new Metadata(true, dataset, rows.getString("as_of_date"), NOTICE);
        });
    }

    private static C1ApiException unavailable() {
        return new C1ApiException(503, "DEMO_CLASSIFICATION_UNAVAILABLE", "The workspace classification is unavailable.");
    }

    Metadata organizationMetadata(long organizationId) {
        return jdbc.query("SELECT demo_dataset_id,demo_as_of FROM organization WHERE id=? AND demo_dataset_id IS NOT NULL",
                rows -> rows.next() ? new Metadata(true, rows.getString(1), rows.getString(2), NOTICE) : null,
                organizationId);
    }

    public record Metadata(boolean synthetic, String datasetId, String demonstrationAsOf, String notice) { }
}

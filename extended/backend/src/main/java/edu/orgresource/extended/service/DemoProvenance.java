package edu.orgresource.extended.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DemoProvenance {
    public static final String NOTICE = "DEMO ENVIRONMENT — Any real organization labels come from public listings. All operations, dates, accounts, inventories and transaction history are simulated. No affiliation, adoption or endorsement is implied.";
    private final JdbcTemplate jdbc;

    public DemoProvenance(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Metadata publicMetadata() {
        return jdbc.query("""
                SELECT demo_dataset_id,demo_as_of FROM organization o WHERE demo_dataset_id IS NOT NULL
                AND (SELECT COUNT(*) FROM organization cohort WHERE cohort.demo_dataset_id=o.demo_dataset_id
                    AND cohort.demo_plan_sha=o.demo_plan_sha)=CASE
                        WHEN o.demo_dataset_id LIKE '%-small' THEN 3
                        WHEN o.demo_dataset_id LIKE '%-intermediate' THEN 8
                        ELSE 96 END
                AND EXISTS (SELECT 1 FROM user_account u JOIN local_credential c ON c.user_id=u.id
                    WHERE u.username_key='c8-demo-operator' AND u.status='ACTIVE')
                ORDER BY id LIMIT 1
                """, rows -> rows.next() ? new Metadata(true, rows.getString(1), rows.getString(2), NOTICE)
                        : new Metadata(false, null, null, null));
    }

    Metadata organizationMetadata(long organizationId) {
        return jdbc.query("SELECT demo_dataset_id,demo_as_of FROM organization WHERE id=? AND demo_dataset_id IS NOT NULL",
                rows -> rows.next() ? new Metadata(true, rows.getString(1), rows.getString(2), NOTICE) : null,
                organizationId);
    }

    public record Metadata(boolean synthetic, String datasetId, String demonstrationAsOf, String notice) { }
}

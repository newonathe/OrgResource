package edu.orgresource.extended.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.C1ApiException;

@Component
public class DemoProvenance {
    public static final String NOTICE = "DEMO ENVIRONMENT — Any real organization labels come from public listings. All operations, dates, accounts, inventories and transaction history are simulated. No affiliation, adoption or endorsement is implied.";
    public static final String MIXED_NOTICE = "DEMO ENVIRONMENT — Workspaces identified as demo organizations contain simulated accounts, projects, inventory and activity. Existing untagged workspaces retain their own data. Public directory labels do not imply participation, adoption or endorsement.";
    private static final String APPROVED_FULL_SOURCE_SHA = "57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2";
    private static final String APPROVED_FULL_PLAN_SHA = "3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c";
    private final JdbcTemplate jdbc;

    public DemoProvenance(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Metadata publicMetadata() {
        return jdbc.query("""
                SELECT COUNT(*) AS organizations, COUNT(demo_dataset_id) AS tagged,
                    MIN(demo_dataset_id) AS dataset_id, MIN(demo_as_of) AS as_of_date,
                    MIN(demo_source_sha) AS source_sha, MIN(demo_plan_sha) AS plan_sha,
                    (COUNT(DISTINCT demo_dataset_id)=1 AND COUNT(DISTINCT demo_reference_key)=COUNT(demo_dataset_id)
                        AND COUNT(DISTINCT demo_source_sha)=1 AND COUNT(DISTINCT demo_plan_sha)=1
                        AND COUNT(DISTINCT demo_as_of)=1 AND COUNT(demo_reference_key)=COUNT(demo_dataset_id)
                        AND COUNT(demo_source_sha)=COUNT(demo_dataset_id)
                        AND COUNT(demo_plan_sha)=COUNT(demo_dataset_id)
                        AND COUNT(demo_as_of)=COUNT(demo_dataset_id)) AS single_cohort,
                    EXISTS (
                        SELECT 1 FROM membership m
                        JOIN user_account u ON u.id=m.user_id AND u.status='ACTIVE'
                        JOIN local_credential c ON c.user_id=u.id
                        JOIN organization o ON o.id=m.org_id AND o.demo_dataset_id IS NOT NULL
                        WHERE m.role='OWNER' AND m.status='ACTIVE'
                        GROUP BY m.user_id
                        HAVING COUNT(DISTINCT o.id)=(SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL)
                    ) AS owner_ready
                FROM organization
                """, rows -> {
            if (!rows.next()) throw unavailable();
            return classify(rows.getLong("organizations"), rows.getLong("tagged"),
                    rows.getString("dataset_id"), rows.getString("as_of_date"),
                    rows.getBoolean("single_cohort"), rows.getBoolean("owner_ready"),
                    rows.getString("source_sha"), rows.getString("plan_sha"));
        });
    }

    /** Pure, fail-closed classifier; live MySQL integration must also prove the SQL owner query. */
    static Metadata classify(long organizations, long tagged, String dataset, String asOf, boolean singleCohort,
                             boolean ownerReady, String sourceSha, String planSha) {
        if (tagged == 0) return new Metadata(false, null, null, null);
        if (dataset == null || !dataset.matches("orgresource-c8-plan-v1-[0-9a-f]{12}-[0-9a-f]{12}(-small|-intermediate)?"))
            throw unavailable();
        int expected = dataset.endsWith("-small") ? 3 : dataset.endsWith("-intermediate") ? 8 : 96;
        boolean standalone = tagged == organizations;
        boolean approvedMixedFull = expected == 96 && organizations > tagged
                && APPROVED_FULL_SOURCE_SHA.equals(sourceSha) && APPROVED_FULL_PLAN_SHA.equals(planSha);
        if (tagged != expected || !singleCohort || !ownerReady || (!standalone && !approvedMixedFull)
                || asOf == null || !asOf.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw unavailable();
        return new Metadata(true, dataset, asOf, approvedMixedFull ? MIXED_NOTICE : NOTICE);
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

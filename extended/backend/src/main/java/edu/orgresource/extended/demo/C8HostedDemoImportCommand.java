package edu.orgresource.extended.demo;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import com.fasterxml.jackson.databind.JsonNode;

import edu.orgresource.extended.service.C8DemoImporter;
import edu.orgresource.extended.service.DemoProvenance;

/**
 * Explicitly opted-in non-web command for an existing Northflank MySQL schema.
 * NEVER run from the web startup command, CI, migrations, or a scheduler.
 * The operator must independently verify/restore an off-host backup BEFORE use.
 */
public final class C8HostedDemoImportCommand {
    public static final String ARGUMENT = "--c8-hosted-demo-import";
    private C8HostedDemoImportCommand() { }

    public static void run(SpringApplication app, String[] args, Map<String, String> env) throws Exception {
        if (args.length != 1 || !ARGUMENT.equals(args[0]))
            throw new IllegalArgumentException("The manual hosted importer accepts only its exact command.");
        String fingerprint = required(env, "EXT_C8_HOSTED_EXPECTED_FINGERPRINT");
        if (!fingerprint.matches("[0-9a-f]{64}")
                || !("IMPORT 96 INTO " + fingerprint).equals(env.get("EXT_C8_HOSTED_CONFIRM")))
            throw new IllegalArgumentException("The manual import requires the exact target fingerprint confirmation.");
        String backup = required(env, "EXT_C8_HOSTED_BACKUP_ID");
        String restore = required(env, "EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256");
        if (!backup.matches("[A-Za-z0-9_.:-]{12,128}") || !restore.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("A backup ID and independent restore evidence digest are required.");
        if (!"I VERIFIED ISOLATED RESTORE".equals(env.get("EXT_C8_HOSTED_RESTORE_ATTESTATION")))
            throw new IllegalArgumentException("Only the operator can attest to a successful independent restore.");
        if (!"false".equalsIgnoreCase(env.get("EXT_SMS_ENABLED"))
                || !"DISABLED".equals(env.get("EXT_REMINDER_MODE")))
            throw new IllegalArgumentException("SMS and reminder processing must explicitly be disabled.");
        String profiles = env.getOrDefault("SPRING_PROFILES_ACTIVE", "");
        if (!profiles.isBlank()) throw new IllegalArgumentException("Do not inherit an application or test profile in the hosted import job.");
        requireExistingBaselineVerification(env);

        // Performs exact private plan digest + count checks, and SELECT-only checks
        // against MySQL identity, migration, existing owner and zero demo rows.
        C8HostedDemoPreflight.Inspection inspected = C8HostedDemoPreflight.inspect(env);
        if (!fingerprint.equals(inspected.fingerprint()))
            throw new IllegalArgumentException("The expected fingerprint is different from the connected target.");
        // A successful CLI CAPTURE here would be meaningless: the original
        // HMAC manifest must already exist from before any importer writes.
        // Refuse absent, altered, wrong-target or V1 capture before Spring starts.
        C8HostedOriginalBaseline.run(new String[]{C8HostedOriginalBaseline.ARGUMENT}, env);

        String url = required(env, "EXT_DB_URL");
        String user = required(env, "EXT_DB_USER");
        String password = required(env, "EXT_DB_PASSWORD");
        String schema = required(env, "EXT_C8_HOSTED_EXPECTED_SCHEMA");
        UUID owner = UUID.fromString(required(env, "EXT_C8_HOSTED_OWNER_PUBLIC_ID"));
        UUID original = UUID.fromString(required(env, "EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID"));
        JsonNode plan = C8DemoCommand.readPlan(Path.of(required(env, "EXT_C8_DEMO_PLAN")),
                C8HostedDemoPreflight.APPROVED_FULL_PLAN_SHA);
        if (plan.path("organizations").size() != 96 || !"LOCAL_ONLY_DRY_RUN".equals(plan.path("scope").asText()))
            throw new IllegalArgumentException("The manual command only accepts the approved full profile.");

        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles(C8DemoCommand.PROFILE);
        app.addInitializers(context -> context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("confirmedHostedDemoImportTarget", Map.of(
                        "spring.datasource.url", url,
                        "spring.datasource.username", user,
                        "spring.datasource.password", password,
                        "spring.flyway.enabled", "false",
                        "c1.sms.enabled", "false",
                        "c1.reminder.mode", "DISABLED"))));
        Exception executionFailure = null;
        try (ConfigurableApplicationContext context = app.run()) {
            context.getBean(C8DemoImporter.class).importApprovedHostedPlan(plan,
                    C8HostedDemoPreflight.APPROVED_FULL_PLAN_SHA, original, owner);
            DemoProvenance.Metadata result = context.getBean(DemoProvenance.class).publicMetadata();
            if (!result.synthetic() || !result.datasetId().equals(plan.path("dataset_id").asText())
                    || !DemoProvenance.MIXED_NOTICE.equals(result.notice()))
                throw new IllegalStateException("The completed mixed cohort failed public classification.");
        } catch (Exception incomplete) {
            executionFailure = incomplete;
            System.err.println("HOSTED_IMPORT_INCOMPLETE: Keep the website in maintenance mode. " +
                    "Investigate privately and restore the independently verified backup to a separate validated target before retry.");
            throw incomplete;
        } finally {
            // Runs whether the replay completed or stopped partway through.
            // Never permit a success message without original-data integrity.
            try {
                C8HostedOriginalBaseline.run(new String[]{C8HostedOriginalBaseline.ARGUMENT}, env);
            } catch (Exception mismatch) {
                System.err.println("HOSTED_ORIGINAL_DATA_NOT_VERIFIED: Stop, keep maintenance active, " +
                        "and investigate privately. Never retry against a partial target.");
                if (executionFailure != null) executionFailure.addSuppressed(mismatch);
                else throw mismatch;
            }
        }
        System.out.println("HOSTED_IMPORT_COMPLETE: 96 explicitly simulated organizations, verified against the exact plan, " +
                "the original owner and all final state counts. Follow with independent SQL and browser acceptance.");
    }

    /**
     * Reject any missing capture or CAPTURE-mode execution before opening a
     * JDBC connection. The deeper V2 file/HMAC/target comparison is read-only.
     * This is not evidence that the operator actually restored a backup.
     */
    static void requireExistingBaselineVerification(Map<String, String> env) {
        if (!"VERIFY".equals(required(env, "EXT_C8_BASELINE_MODE")))
            throw new IllegalArgumentException("Hosted import requires VERIFY of a pre-existing original-data baseline, never CAPTURE.");
        String filename = required(env, "EXT_C8_BASELINE_FILE");
        if (!Path.of(filename).isAbsolute())
            throw new IllegalArgumentException("A private absolute path to the pre-import original-data baseline is required.");
        String secret = required(env, "EXT_C8_BASELINE_HMAC_HEX");
        if (!secret.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("A private 32-byte original-data HMAC key is required.");
    }

    private static String required(Map<String, String> env, String key) {
        String value = env.get(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Required manual import setting is missing: " + key);
        return value;
    }
}

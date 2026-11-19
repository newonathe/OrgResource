package edu.orgresource.extended.demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.orgresource.extended.identity.C1BootstrapService;
import edu.orgresource.extended.service.C8DemoImporter;

public final class C8DemoCommand {
    public static final String ARGUMENT = "--c8-local-demo-import";
    public static final String PROFILE = "c8-offline-demo-import";
    public static final LocalDate AS_OF = LocalDate.of(2026, 11, 19);
    private static final int MAX_PLAN_BYTES = 32 * 1024 * 1024;

    private C8DemoCommand() { }

    public static void run(SpringApplication application, String[] arguments, Map<String, String> env) throws Exception {
        if (arguments.length != 1 || !ARGUMENT.equals(arguments[0]))
            throw new IllegalArgumentException("The offline import accepts only its dedicated command flag.");
        String schema = required(env, "EXT_C8_DEMO_TARGET");
        if (!schema.matches("orgresource_c8_demo_[a-z0-9_]{6,40}")
                || !("IMPORT " + schema).equals(env.get("EXT_C8_DEMO_CONFIRM_TARGET")))
            throw new IllegalArgumentException("An exact confirmation for a fresh local C8 demo schema is required.");
        String port = required(env, "EXT_C8_DEMO_PORT");
        if (!port.matches("[0-9]{1,5}") || Integer.parseInt(port) < 1024 || Integer.parseInt(port) > 65535)
            throw new IllegalArgumentException("Supply the dedicated numeric loopback MySQL port.");
        String jdbcUrl = "jdbc:mysql://127.0.0.1:" + port + "/" + schema
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = required(env, "EXT_DB_USER"), password = required(env, "EXT_DB_PASSWORD");
        String migrator = required(env, "EXT_DB_MIGRATION_USER"), migrationPassword = required(env, "EXT_DB_MIGRATION_PASSWORD");
        if (user.equals(migrator) || password.equals(migrationPassword))
            throw new IllegalArgumentException("Use distinct local runtime and migration credentials.");
        String ownerPassword = required(env, "EXT_C8_DEMO_OWNER_PASSWORD");
        C1BootstrapService.validatePassword(ownerPassword);
        String digest = required(env, "EXT_C8_DEMO_PLAN_SHA256");
        JsonNode plan = readPlan(Path.of(required(env, "EXT_C8_DEMO_PLAN")), digest);

        // Check the exact empty target before Spring can run Flyway or any seed work.
        requireEmptyTarget(jdbcUrl, schema, migrator, migrationPassword);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setAdditionalProfiles(PROFILE);
        application.addInitializers(context -> context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("verifiedLocalDemoTarget", Map.of(
                        "spring.datasource.url", jdbcUrl, "spring.datasource.username", user,
                        "spring.datasource.password", password, "spring.flyway.url", jdbcUrl,
                        "spring.flyway.user", migrator, "spring.flyway.password", migrationPassword,
                        "c1.sms.enabled", "false", "c1.reminder.mode", "DISABLED"))));
        try (ConfigurableApplicationContext context = application.run()) {
            context.getBean(C8DemoImporter.class).importPlan(plan, digest, ownerPassword);
        }
    }

    private static String required(Map<String, String> env, String key) {
        String value = env.get(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Required demo setting is missing: " + key);
        return value;
    }

    private static void requireEmptyTarget(String url, String schema, String user, String password) throws SQLException {
        try (var connection = DriverManager.getConnection(url, user, password);
             var query = connection.prepareStatement("""
                     SELECT DATABASE(),
                        (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()),
                        (SELECT COUNT(*) FROM information_schema.schema_privileges WHERE table_schema=DATABASE()
                            AND privilege_type='SELECT' AND grantee=CONCAT(
                                QUOTE(SUBSTRING_INDEX(CURRENT_USER(),'@',1)), '@', QUOTE(SUBSTRING_INDEX(CURRENT_USER(),'@',-1))))
                     """)) {
            try (var result = query.executeQuery()) {
                if (!result.next() || !schema.equals(result.getString(1)) || result.getInt(2) != 0 || result.getInt(3) == 0)
                    throw new IllegalArgumentException("The confirmed demo schema already contains tables or does not match.");
            }
        }
    }

    private static JsonNode readPlan(Path path, String digest) throws Exception {
        if (!digest.matches("[0-9a-f]{64}") || Files.size(path) > MAX_PLAN_BYTES)
            throw new IllegalArgumentException("The approved plan digest or size is invalid.");
        byte[] raw;
        try (var file = Files.newInputStream(path)) {
            raw = file.readNBytes(MAX_PLAN_BYTES + 1);
        }
        if (raw.length > MAX_PLAN_BYTES || !digest.equals(java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw))))
            throw new IllegalArgumentException("The supplied demo plan differs from the approved SHA-256.");
        var reader = new ObjectMapper().reader().with(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        JsonNode plan = reader.readTree(raw);
        if (!"orgresource-c8-plan-v1".equals(plan.path("generator").asText())
                || !"LOCAL_ONLY_DRY_RUN".equals(plan.path("scope").asText()) || !plan.path("synthetic").asBoolean()
                || !AS_OF.toString().equals(plan.path("window").path("as_of").asText())
                || !plan.path("dataset_id").asText().matches("orgresource-c8-plan-v1-[0-9a-f]{12}-[0-9a-f]{12}")
                || !plan.path("source_sha256").asText().matches("[0-9a-f]{64}")
                || !plan.path("organizations").isArray() || plan.path("organizations").size() != 96)
            throw new IllegalArgumentException("The file is not the approved local C8 full plan.");
        var ids = new HashSet<UUID>();
        var keys = new HashSet<String>();
        int requests = 0;
        for (JsonNode organization : plan.path("organizations")) {
            String key = organization.path("reference_key").asText(), label = organization.path("directory_label").asText();
            if (!key.matches("[a-z0-9-]{3,128}") || !keys.add(key)
                    || !ids.add(UUID.fromString(organization.path("id").asText()))
                    || label.isBlank() || label.codePointCount(0, label.length()) > 120
                    || label.codePoints().anyMatch(Character::isISOControl))
                throw new IllegalArgumentException("The demo plan contains a duplicate or invalid organization reference.");
            requests = Math.addExact(requests, organization.path("allocations").size());
        }
        if (requests != 21060) throw new IllegalArgumentException("The plan does not contain the approved workload floor.");
        return plan;
    }
}

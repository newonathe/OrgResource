package edu.orgresource.extended.demo;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import edu.orgresource.extended.service.C8DemoImporter;

/** A dedicated loopback-only test of adding fictional workspaces to an existing V7 database. */
public final class C8AdditiveProofCommand {
    public static final String ARGUMENT = "--c8-additive-demo-isolated-proof";
    private C8AdditiveProofCommand() { }

    public static void run(SpringApplication app, String[] args, Map<String,String> env) throws Exception {
        if (args.length != 1 || !ARGUMENT.equals(args[0]))
            throw new IllegalArgumentException("The additive acceptance tool only accepts its one dedicated flag.");
        String schema = required(env, "EXT_C8_PROOF_SCHEMA");
        if (!schema.matches("orgresource_c8_additive_ci_[a-z0-9_]{4,35}")
                || !("TEST ADDITIVE " + schema).equals(env.get("EXT_C8_PROOF_CONFIRM")))
            throw new IllegalArgumentException("Additive acceptance is restricted to a confirmed disposable CI schema.");
        String port = required(env, "EXT_C8_PROOF_PORT");
        if (!port.matches("[0-9]{4,5}") || Integer.parseInt(port) > 65535)
            throw new IllegalArgumentException("Only an explicitly selected loopback MySQL test port is supported.");
        String url = "jdbc:mysql://127.0.0.1:" + port + "/" + schema
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String digest = required(env, "EXT_C8_DEMO_PLAN_SHA256");
        JsonNode plan = C8DemoCommand.readPlan(Path.of(required(env, "EXT_C8_DEMO_PLAN")), digest);
        String scope = plan.path("scope").asText();
        int count = plan.path("organizations").size();
        if (!(("LOCAL_ONLY_SMALL_TEST".equals(scope) && count == 3)
                || ("LOCAL_ONLY_INTERMEDIATE_TEST".equals(scope) && count == 8)))
            throw new IllegalArgumentException("This acceptance-only command permits only three or eight fictional test workspaces.");
        String injected = env.getOrDefault("EXT_C8_PROOF_FAIL_AFTER", "0");
        if (!("0".equals(injected) || ("1".equals(injected) && count == 3)))
            throw new IllegalArgumentException("Fault injection is only allowed after one small fictional workspace.");
        UUID originalOrg = UUID.fromString(required(env, "EXT_C8_PROOF_ORIGINAL_ORG_ID"));
        UUID owner = UUID.fromString(required(env, "EXT_C8_PROOF_OWNER_ACCOUNT_ID"));
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles(C8DemoCommand.PROFILE);
        app.addInitializers(context -> context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("isolatedAdditiveProof", Map.of(
                    "spring.datasource.url", url,
                    "spring.datasource.username", required(env, "EXT_DB_USER"),
                    "spring.datasource.password", required(env, "EXT_DB_PASSWORD"),
                    "spring.flyway.enabled", "false",
                    "c1.sms.enabled", "false", "c1.reminder.mode", "DISABLED"))));
        try (ConfigurableApplicationContext context = app.run()) {
            context.getBean(C8DemoImporter.class).importAdditionalPlanForIsolatedProof(
                    plan, digest, originalOrg, owner, Integer.parseInt(injected));
            try (var db = DriverManager.getConnection(url, required(env, "EXT_DB_USER"),
                    required(env, "EXT_DB_PASSWORD"))) {
                db.setReadOnly(true);
                db.setTransactionIsolation(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
                db.setAutoCommit(false);
                C8HostedDemoPostflight.Summary verified =
                        C8HostedDemoPostflight.verify(db, plan, digest, originalOrg, owner);
                db.rollback();
                System.out.printf("ISOLATED_DEMO_POSTFLIGHT_READ_ONLY_OK: %d orgs, %d allocations, %d events%n",
                        verified.organizations(), verified.allocations(), verified.events());
            }
        }
    }

    private static String required(Map<String,String> env, String name) {
        String value = env.get(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Isolated proof setting missing: " + name);
        return value;
    }
}

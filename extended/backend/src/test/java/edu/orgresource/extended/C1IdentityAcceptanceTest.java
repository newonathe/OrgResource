package edu.orgresource.extended;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.identity.*;
import edu.orgresource.extended.domain.Department;
import edu.orgresource.extended.domain.Project;
import edu.orgresource.extended.domain.Resource;
import edu.orgresource.extended.domain.ResourceRequirement;
import edu.orgresource.extended.domain.Allocation;
import edu.orgresource.extended.repository.AllocationRepository;
import edu.orgresource.extended.repository.ResourceRepository;
import edu.orgresource.extended.repository.ResourceRequirementRepository;
import edu.orgresource.extended.security.C1IdentityNormalization;
import edu.orgresource.extended.security.ExtendedUserPrincipal;
import edu.orgresource.extended.service.C1Service;
import edu.orgresource.extended.service.InventoryService;
import edu.orgresource.extended.service.ResourceImageSupport;
import edu.orgresource.extended.service.C3ReminderDispatcher;
import edu.orgresource.extended.service.C3ReminderService;
import edu.orgresource.extended.integration.C3ReminderTransport;
import edu.orgresource.extended.integration.ReminderDispatch;
import edu.orgresource.extended.integration.ReminderSendResult;
import edu.orgresource.extended.domain.ReminderMode;
import edu.orgresource.extended.scheduler.C1ReminderScheduler;
import jakarta.validation.Validator;
import javax.sql.DataSource;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.bean.override.convention.TestBean;

import javax.validation.ValidatorFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=c1-test", "c1.sms.enabled=false", "c1.governance-expiry-delay-ms=86400000"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class C1IdentityAcceptanceTest {
    private static final Gson GSON = new Gson();
    private static final String COOKIE = "ORGSESSION";
    private static final String OWNER_SLUG = "c1-seed-workspace";
    private static final AtomicReference<Instant> SCOPED_TEST_BUSINESS_TIME = new AtomicReference<>();

    @LocalServerPort int port;
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired OrganizationRepository organizations;
    @Autowired UserAccountRepository accounts;
    @Autowired LocalCredentialRepository credentials;
    @Autowired MembershipRepository memberships;
    @Autowired RecoveryCodeRepository recoveryCodes;
    @Autowired InvitationRepository invitations;
    @Autowired OwnershipTransferRepository transfers;
    @Autowired C1BootstrapService bootstrap;
    @Autowired C1GovernanceService governance;
    @Autowired C1GovernanceExpiryWorker expiryWorker;
    @Autowired ResourceRepository resourceRepository;
    @Autowired ResourceRequirementRepository requirementRepository;
    @Autowired AllocationRepository allocationRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @SuppressWarnings("rawtypes")
    @Autowired org.springframework.session.SessionRepository springSessions;
    @Autowired InventoryService inventory;
    @Autowired edu.orgresource.extended.security.ContactCipher contactCipher;
    @Autowired C3ReminderDispatcher reminderDispatcher;
    @Autowired C3ReminderService reminderService;
    @Autowired ScheduledAnnotationBeanPostProcessor scheduledTasks;
    @Autowired Clock clock;
    @TestBean(name = "businessClock", methodName = "scopedBusinessClock")
    Clock businessClock;
    @org.springframework.beans.factory.annotation.Value("${c1.sms.enabled:false}") boolean smsEnabled;
    @Autowired javax.validation.Validator javaxValidator;
    @Autowired ValidatorFactory javaxValidatorFactory;
    @Autowired Validator jakartaValidator;
    @Autowired jakarta.validation.ValidatorFactory jakartaValidatorFactory;
    @Autowired org.springframework.security.web.SecurityFilterChain securityFilterChain;
    @Autowired org.glassfish.jersey.server.ResourceConfig c8JerseyResourceConfig;

    private TransactionTemplate tx;
    private List<String> bootstrapCodes = List.of();

    private static Clock scopedBusinessClock() {
        return testBusinessClock(ZoneId.of("Asia/Manila"));
    }

    private static Clock testBusinessClock(ZoneId zone) {
        return new Clock() {
            @Override public ZoneId getZone() { return zone; }
            @Override public Clock withZone(ZoneId changed) { return testBusinessClock(changed); }
            @Override public Instant instant() {
                Instant simulation = SCOPED_TEST_BUSINESS_TIME.get();
                return simulation == null ? Instant.now() : simulation;
            }
        };
    }

    @BeforeAll
    void requireFreshLoopbackMysqlSchema() throws Exception {
        tx = new TransactionTemplate(transactionManager);
        String url = System.getenv("EXT_DB_URL");
        assertNotNull(url, "EXT_DB_URL must point to the disposable C1 acceptance schema.");
        assertTrue(url.matches("^jdbc:mysql://(?:127\\.0\\.0\\.1|localhost):[0-9]+/orgresource_c[12]_[A-Za-z0-9_]+(?:\\?.*)?$"),
                "Extended C integration tests require a dedicated loopback orgresource_c1_* or orgresource_c2_* schema.");
        configureRuntimeAppendOnlyGrants();
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.getCatalog().startsWith("orgresource_c1_"));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM organization", Integer.class),
                    "ID01 must begin from an empty organization table.");
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM user_account", Integer.class),
                    "ID01 must begin from an empty account table.");
        }
        assertNotNull(System.getenv("EXT_BOOTSTRAP_PASSWORD"), "The isolated test runner supplies an ephemeral bootstrap secret.");
    }

    private void configureRuntimeAppendOnlyGrants() throws Exception {
        String migrationUrl = System.getenv("EXT_DB_MIGRATION_URL");
        String migrationUser = System.getenv("EXT_DB_MIGRATION_USER");
        String migrationPassword = System.getenv("EXT_DB_MIGRATION_PASSWORD");
        assertNotNull(migrationUrl, "C2 acceptance requires a separate elevated migration connection.");
        assertNotNull(migrationUser, "C2 acceptance requires a separate migration identity.");
        String runtimeIdentity = jdbc.queryForObject("SELECT CURRENT_USER()", String.class);
        int at = runtimeIdentity.lastIndexOf('@');
        assertTrue(at > 0 && at < runtimeIdentity.length() - 1, "MySQL must identify the runtime grant principal.");
        String account = "'" + runtimeIdentity.substring(0, at).replace("'", "''") + "'@'"
                + runtimeIdentity.substring(at + 1).replace("'", "''") + "'";
        try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
             var statement = migration.createStatement()) {
            String schema = migration.getCatalog();
            assertTrue(schema.matches("orgresource_c[12]_[A-Za-z0-9_]+"));
            statement.execute("REVOKE ALL PRIVILEGES, GRANT OPTION FROM " + account);
            List<String> tableNames = new ArrayList<>();
            try (var tables = statement.executeQuery("SELECT table_name FROM information_schema.tables "
                    + "WHERE table_schema='" + schema + "' AND engine='InnoDB'")) {
                while (tables.next()) tableNames.add(tables.getString(1));
            }
            for (String table : tableNames) {
                assertTrue(table.matches("(?i)[a-z0-9_]+"));
                String normalizedTable = table.toLowerCase(java.util.Locale.ROOT);
                String privileges = normalizedTable.equals("stock_movement") || normalizedTable.equals("audit_event")
                        ? "SELECT, INSERT"
                        : normalizedTable.equals("flyway_schema_history") ? "SELECT" : "SELECT, INSERT, UPDATE, DELETE";
                statement.execute("GRANT " + privileges + " ON `" + schema + "`.`" + table + "` TO " + account);
            }
            statement.execute("FLUSH PRIVILEGES");
        }
        dataSource.unwrap(com.zaxxer.hikari.HikariDataSource.class)
                .getHikariPoolMXBean().softEvictConnections();
        for (String table : List.of("stock_movement", "audit_event")) {
            assertThrows(java.sql.SQLException.class, () -> {
                try (Connection runtime = dataSource.getConnection(); var statement = runtime.createStatement()) {
                    statement.executeUpdate("UPDATE `" + table + "` SET "
                            + (table.equals("stock_movement") ? "kind=kind" : "action=action") + " WHERE 1=0");
                }
            }, "Runtime role must not update append-only " + table + ".");
            assertThrows(java.sql.SQLException.class, () -> {
                try (Connection runtime = dataSource.getConnection(); var statement = runtime.createStatement()) {
                    statement.executeUpdate("DELETE FROM `" + table + "` WHERE 1=0");
                }
            }, "Runtime role must not delete append-only " + table + ".");
        }
    }

    @Test @Order(1)
    void ID01_offlineBootstrapTwoRacersCreateExactlyOneOwnerAndNeverPublicOrganizationCreation() throws Exception {
        String password = System.getenv("EXT_BOOTSTRAP_PASSWORD");
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var one = pool.submit(() -> { start.await(); return bootstrap.bootstrap(OWNER_SLUG, "Fictional C1 Workspace", "c1-owner", "C1 Owner", password); });
            var two = pool.submit(() -> { start.await(); return bootstrap.bootstrap(OWNER_SLUG, "Fictional C1 Workspace", "c1-owner", "C1 Owner", password); });
            start.countDown();
            int successes = 0, failures = 0;
            for (var future : List.of(one, two)) {
                try { bootstrapCodes = future.get(45, TimeUnit.SECONDS).recoveryCodes(); successes++; }
                catch (java.util.concurrent.ExecutionException expected) { failures++; }
            }
            assertEquals(1, successes);
            assertEquals(1, failures);
        } finally { pool.shutdownNow(); }
        assertEquals(1, organizations.count());
        assertEquals(1, accounts.count());
        Organization org = organizations.findBySlug(OWNER_SLUG).orElseThrow();
        UserAccount owner = accounts.findByUsernameKey("c1-owner").orElseThrow();
        assertEquals(1, memberships.countEffectiveActiveOwners(org.id));
        assertEquals(5, bootstrapCodes.size());
        assertThrows(IllegalStateException.class, () -> bootstrap.bootstrap(OWNER_SLUG, "Fictional C1 Workspace",
                "c1-owner", "C1 Owner", password));
        assertEquals(1, organizations.count());
        assertEquals(1, accounts.count());
        assertTrue(passwordEncoder.matches(password, credentials.findByUserId(owner.id).orElseThrow().passwordHash));
        assertFalse(credentials.findByUserId(owner.id).orElseThrow().passwordHash.equals(password));
        for (String raw : bootstrapCodes)
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM account_recovery_code WHERE code_hash=?", Integer.class,
                    (Object) C1Secrets.sha256(raw)));
        assertEquals(401, get(newClient(), "/api/departments").statusCode());
        assertEquals(403, post(newClient(), "/api/v2/orgs", "{}", null, origin()).statusCode());
    }

    @Test @Order(2)
    void ID02_literalJavaxAndJakartaValidationCoexistWithJerseyAutowiredJpaMysqlHttp() throws Exception {
        assertTrue(javaxValidatorFactory.getClass().getName().contains("bval"));
        assertTrue(javaxValidator.getClass().getName().contains("bval"));
        assertNotNull(jakartaValidatorFactory.unwrap(org.hibernate.validator.HibernateValidatorFactory.class));
        assertTrue(java.util.Arrays.stream(ApiDtos.CreateDepartment.class.getField("name").getAnnotations())
                .anyMatch(a -> a.annotationType().getName().equals("javax.validation.constraints.NotBlank")));
        assertFalse(javaxValidator.validate(new ApiDtos.CreateDepartment(" ")).isEmpty());
        assertFalse(jakartaValidator.validate(new JakartaProbe(" ")).isEmpty());

        Fixture owner = fixtureFor("c1-owner");
        Session session = login(owner.username, owner.password);
        String name = "ID02 persisted " + UUID.randomUUID();
        HttpResponse<String> created = post(session, "/api/departments", GSON.toJson(new NameRequest(name)));
        assertEquals(201, created.statusCode(), created.body());
        long id = json(created).get("id").getAsLong();
        try (Connection fresh = dataSource.getConnection();
             PreparedStatement query = fresh.prepareStatement("SELECT name,organization_id FROM department WHERE id=?")) {
            query.setLong(1, id);
            try (ResultSet row = query.executeQuery()) {
                assertTrue(row.next()); assertEquals(name, row.getString(1)); assertEquals(owner.orgDbId, row.getLong(2));
            }
        }
        assertTrue(get(session.client, "/api/departments").body().contains(name));
    }

    @Test @Order(3)
    void ID03_mysqlSchemaHasAllIdentityDomainAndOfficialSessionTablesAndRejectsCrossTenantForeignKeys() {
        Set<String> required = Set.of("organization", "user_account", "local_credential", "account_recovery_code",
                "membership", "invitation", "ownership_transfer", "audit_event", "department", "project",
                "resource", "resource_requirement", "allocation", "stock_movement", "idempotency_key",
                "spring_session", "spring_session_attributes");
        Set<String> actual = jdbc.queryForList("SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema=DATABASE()", String.class)
                .stream().collect(java.util.stream.Collectors.toSet());
        assertTrue(actual.containsAll(required), actual.toString());
        assertEquals(17, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND LOWER(table_name) IN ("
                + "'organization','user_account','local_credential','account_recovery_code','membership','invitation','ownership_transfer','audit_event',"
                + "'department','project','resource','resource_requirement','allocation','stock_movement','idempotency_key','spring_session','spring_session_attributes')", Integer.class));
        assertEquals(17, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND engine='InnoDB' AND LOWER(table_name) IN ("
                + "'organization','user_account','local_credential','account_recovery_code','membership','invitation','ownership_transfer','audit_event',"
                + "'department','project','resource','resource_requirement','allocation','stock_movement','idempotency_key','spring_session','spring_session_attributes')", Integer.class));
        assertEquals(12, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.referential_constraints WHERE constraint_schema=DATABASE() AND constraint_name LIKE 'fk_%_scope'", Integer.class));
        assertTrue(actualSchemaColumns("ownership_transfer").contains("pending_org_id"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() "
                + "AND table_name='ownership_transfer' AND index_name='uk_transfer_one_pending_per_org' AND non_unique=0", Integer.class));
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='organization' AND column_name='created_at' AND datetime_precision=6", Integer.class) == 1);

        Fixture a = newTenant("ID03-A", Membership.Role.OWNER);
        Fixture b = newTenant("ID03-B", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(a.orgDbId, Membership.Role.ADMIN);
        OwnershipTransfer firstPending = new OwnershipTransfer(a.orgDbId, a.membershipDbId, admin.membershipDbId,
                now(), now().plusHours(1));
        tx.executeWithoutResult(status -> transfers.saveAndFlush(firstPending));
        assertThrows(DataIntegrityViolationException.class, () -> tx.executeWithoutResult(status ->
                transfers.saveAndFlush(new OwnershipTransfer(a.orgDbId, a.membershipDbId, admin.membershipDbId,
                        now(), now().plusHours(1)))));
        Department saved = tx.execute(s -> departmentRepository.saveAndFlush(new Department(
                organizations.findByPublicId(a.orgPublicId).orElseThrow().id, "Cross tenant", "cross tenant")));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO project(public_id,organization_id,department_id,name,start_at,end_at) VALUES(UUID_TO_BIN(UUID(),0),?,?,?,?,?)",
                b.orgDbId, saved.id, "cross-tenant project", "2040-01-01 00:00:00", "2040-01-02 00:00:00"));
        RuntimeException invalidRole = assertThrows(RuntimeException.class,
                () -> jdbc.update("UPDATE membership SET role='ROOT' WHERE id=?", a.membershipDbId));
        assertNotNull(findCause(invalidRole, java.sql.SQLException.class));
        assertEquals(7, jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1", Integer.class));
    }

    @Test @Order(4)
    void ID04_anonymousWrongRoleForeignTenantAndFrozenOrganizationBoundaries() throws Exception {
        Fixture owner = newTenant("ID04-owner", Membership.Role.OWNER);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Fixture foreign = newTenant("ID04-foreign", Membership.Role.OWNER);
        Session user = login(member.username, member.password);
        assertEquals(401, get(newClient(), "/api/departments").statusCode());
        assertEquals(403, get(user.client, "/api/v2/orgs/" + owner.orgPublicId + "/members").statusCode());
        assertEquals(404, get(user.client, "/api/v2/orgs/" + foreign.orgPublicId + "/members").statusCode());
        assertEquals(403, post(user, "/api/departments", GSON.toJson(new NameRequest("ID04 denied"))).statusCode());

        Session ownerSession = login(owner.username, owner.password);
        jdbc.update("UPDATE organization SET status='FROZEN' WHERE id=?", owner.orgDbId);
        HttpResponse<String> frozenWrite = post(ownerSession, "/api/departments", GSON.toJson(new NameRequest("ID04 frozen")));
        assertEquals(409, frozenWrite.statusCode()); assertEquals("ORG_FROZEN", json(frozenWrite).get("code").getAsString());
        assertEquals(200, get(ownerSession.client, "/api/departments").statusCode());
        HttpResponse<String> switchFrozen = post(ownerSession, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(owner.orgPublicId)));
        assertEquals(404, switchFrozen.statusCode());
        assertEquals(404, post(user, "/api/v2/session/active-org", GSON.toJson(new OrgRequest(foreign.orgPublicId))).statusCode());
    }

    @Test @Order(5)
    void ID05_preAuthCsrfLoginRotationSameOriginLogoutAndCookieProfiles() throws Exception {
        Fixture owner = newTenant("ID05-owner", Membership.Role.OWNER);
        HttpClient anonymous = newClient();
        HttpResponse<String> csrf = get(anonymous, "/api/v2/auth/csrf");
        assertEquals(200, csrf.statusCode());
        assertTrue(csrf.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        String oldToken = json(csrf).getAsJsonObject("data").get("csrfToken").getAsString();
        String preLoginId = cookie(anonymous);
        assertNotNull(preLoginId);
        assertTrue(csrf.headers().firstValue("Set-Cookie").orElse("").contains("HttpOnly"));
        assertFalse(csrf.headers().firstValue("Set-Cookie").orElse("").toLowerCase().contains("secure"));
        assertEquals(403, post(anonymous, "/api/v2/auth/login", loginJson(owner), null, origin()).statusCode());
        assertEquals(403, post(anonymous, "/api/v2/auth/login", loginJson(owner), oldToken, "https://attacker.invalid").statusCode());

        Session session = login(anonymous, owner.username, owner.password, oldToken, preLoginId);
        assertNotEquals(preLoginId, session.sessionId);
        assertNotEquals(oldToken, session.csrf);
        HttpResponse<String> oldCsrf = post(session.client, "/api/departments", GSON.toJson(new NameRequest("old csrf")), oldToken, origin());
        assertEquals(403, oldCsrf.statusCode());
        assertEquals(403, post(session.client, "/api/departments", GSON.toJson(new NameRequest("missing csrf")), null, origin()).statusCode());
        assertEquals(403, post(session.client, "/api/departments", GSON.toJson(new NameRequest("wrong origin")), session.csrf, "http://evil.invalid").statusCode());
        HttpResponse<String> logout = post(session, "/api/v2/auth/logout", "");
        assertEquals(204, logout.statusCode(), logout.body());
        assertEquals(401, get(session.client, "/api/v2/session").statusCode());
        String prod = new String(getClass().getClassLoader().getResourceAsStream("application.properties").readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(prod.contains("server.servlet.session.cookie.name=__Host-ORGSESSION"));
        assertTrue(prod.contains("server.servlet.session.cookie.secure=true"));
    }

    @Test @Order(6)
    void ID06_membershipRoleIsReadFromMysqlAndActiveOrganizationCannotBeForged() throws Exception {
        Fixture one = newTenant("ID06-one", Membership.Role.OWNER);
        Fixture two = newTenant("ID06-two", Membership.Role.OWNER);
        Fixture user = fixtureInOrg(one.orgDbId, Membership.Role.CUSTODIAN);
        addMembership(two.orgDbId, user.userDbId, Membership.Role.MEMBER);
        Session session = login(user.username, user.password);
        JsonObject orgs = json(get(session.client, "/api/v2/orgs")).getAsJsonObject();
        assertEquals(2, orgs.getAsJsonArray("data").size());
        assertEquals(200, post(session, "/api/v2/session/active-org", GSON.toJson(new OrgRequest(one.orgPublicId))).statusCode());
        refreshCsrf(session);
        HttpRequest request = HttpRequest.newBuilder(URI.create(base() + "/api/resources"))
                .header("Origin", origin()).header("X-CSRF-TOKEN", session.csrf)
                .header("X-Organization-ID", two.orgPublicId.toString())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(new ResourceRequest("ID06 resource", "REUSABLE", 2)))).build();
        HttpResponse<String> created = session.client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, created.statusCode(), created.body());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE organization_id=? AND name='ID06 resource'", Integer.class, one.orgDbId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE organization_id=? AND name='ID06 resource'", Integer.class, two.orgDbId));

        Session owner = login(one.username, one.password);
        HttpResponse<String> changed = post(owner, "/api/v2/orgs/" + one.orgPublicId + "/members/" + user.membershipPublicId + "/role",
                GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"");
        assertEquals(200, changed.statusCode(), changed.body());
        assertEquals(403, post(session, "/api/resources", GSON.toJson(new ResourceRequest("ID06 now denied", "REUSABLE", 1))).statusCode());
        HttpResponse<String> suspended = post(owner, "/api/v2/orgs/" + one.orgPublicId + "/members/"
                + user.membershipPublicId + "/suspend", "", "\"v1\"");
        assertEquals(200, suspended.statusCode(), suspended.body());
        JsonObject liveSession = json(get(session.client, "/api/v2/session")).getAsJsonObject("data");
        assertTrue(liveSession.getAsJsonArray("memberships").asList().stream()
                .noneMatch(row -> row.getAsJsonObject().get("organizationId").getAsString().equals(one.orgPublicId.toString())));
        assertEquals(404, post(session, "/api/v2/session/active-org", GSON.toJson(new OrgRequest(UUID.randomUUID()))).statusCode());
    }

    @Test @Order(7)
    void ID07_passwordHashGenericLoginFailuresAndBoundedThrottle() throws Exception {
        Fixture user = newTenant("ID07-user", Membership.Role.OWNER);
        String hash = credentials.findByUserId(user.userDbId).orElseThrow().passwordHash;
        assertTrue(hash.matches("^\\$2[aby]\\$11\\$.*"));
        assertTrue(passwordEncoder.matches(user.password, hash));
        assertFalse(hash.contains(user.password));
        assertThrows(IllegalArgumentException.class, () -> C1BootstrapService.validatePassword("short"));

        HttpClient client = newClient();
        String token = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        HttpResponse<String> wrongPassword = post(client, "/api/v2/auth/login", "{\"username\":\"" + user.username + "\",\"password\":\"bad\"}", token, origin());
        HttpResponse<String> wrongUser = post(newClientWithCsrf(client, token), "/api/v2/auth/login",
                "{\"username\":\"missing-" + UUID.randomUUID() + "\",\"password\":\"bad\"}", token, origin());
        assertEquals(401, wrongPassword.statusCode()); assertEquals(401, wrongUser.statusCode());
        assertEquals(json(wrongPassword).get("code"), json(wrongUser).get("code"));
        assertEquals(json(wrongPassword).get("message"), json(wrongUser).get("message"));

        HttpClient throttled = newClient();
        String throttleToken = json(get(throttled, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        String name = "missing-" + UUID.randomUUID();
        int status = 0;
        for (int i = 0; i < 6; i++) status = post(throttled, "/api/v2/auth/login",
                "{\"username\":\"" + name + "\",\"password\":\"bad\"}", throttleToken, origin()).statusCode();
        assertEquals(429, status);
    }

    @Test @Order(8)
    void ID08_recoveryCodeIsHashedSingleUseAndRevokesExistingSessions() throws Exception {
        Fixture user = newTenant("ID08-recovery", Membership.Role.OWNER);
        Session oldSession = login(user.username, user.password);
        String rawCode = user.recoveryCodes.get(0);
        Long unusedCodeId = jdbc.queryForObject("SELECT id FROM account_recovery_code WHERE user_id=? AND code_hash=?",
                Long.class, user.userDbId, C1Secrets.sha256(user.recoveryCodes.get(1)));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM account_recovery_code WHERE code_hash=?", Integer.class,
                (Object) C1Secrets.sha256(rawCode)));
        String newPassword = "Recovered-C1-Password-" + UUID.randomUUID();
        long credentialVersionBefore = accounts.findById(user.userDbId).orElseThrow().credentialVersion;
        HttpClient recovery = newClient();
        String csrf = json(get(recovery, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        HttpResponse<String> recovered = post(recovery, "/api/v2/auth/recover", GSON.toJson(new RecoverRequest(user.username, rawCode, newPassword)), csrf, origin());
        assertEquals(200, recovered.statusCode());
        assertEquals(credentialVersionBefore + 1, accounts.findById(user.userDbId).orElseThrow().credentialVersion);
        assertTrue(json(recovered).getAsJsonObject("data").get("message").getAsString().startsWith("If the account"));
        assertEquals(401, get(oldSession.client, "/api/v2/session").statusCode());
        var codeRow = tx.execute(status -> recoveryCodes.lockByUserAndHash(user.userDbId, C1Secrets.sha256(rawCode)).orElseThrow());
        assertNotNull(codeRow.usedAt);
        HttpClient replay = newClient();
        String replayCsrf = json(get(replay, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        assertEquals(200, post(replay, "/api/v2/auth/recover", GSON.toJson(new RecoverRequest(user.username, rawCode, "Another-C1-Password-" + UUID.randomUUID())), replayCsrf, origin()).statusCode());
        assertTrue(passwordEncoder.matches(newPassword, credentials.findByUserId(user.userDbId).orElseThrow().passwordHash));
        Session current = login(user.username, newPassword);
        Session peerSession = login(user.username, newPassword);
        long versionBeforeChange = accounts.findById(user.userDbId).orElseThrow().credentialVersion;
        String changedPassword = "Changed-C1-Password-" + UUID.randomUUID();
        assertEquals(401, post(current, "/api/v2/account/password",
                GSON.toJson(new PasswordChangeRequest("incorrect current password", changedPassword))).statusCode());
        assertEquals(422, post(current, "/api/v2/account/password",
                GSON.toJson(new PasswordChangeRequest(newPassword, newPassword))).statusCode());
        String priorSessionId = current.sessionId;
        HttpResponse<String> passwordChanged = post(current, "/api/v2/account/password",
                GSON.toJson(new PasswordChangeRequest(newPassword, changedPassword)));
        assertEquals(200, passwordChanged.statusCode(), passwordChanged.body());
        assertNotEquals(priorSessionId, cookie(current.client));
        assertEquals(401, get(peerSession.client, "/api/v2/session").statusCode());
        assertEquals(versionBeforeChange + 1, accounts.findById(user.userDbId).orElseThrow().credentialVersion);
        assertTrue(passwordEncoder.matches(changedPassword, credentials.findByUserId(user.userDbId).orElseThrow().passwordHash));
        refreshCsrf(current);
        assertEquals(200, get(current.client, "/api/v2/session").statusCode());
        assertEquals(401, loginStatus(user.username, newPassword));
        assertEquals(200, loginStatus(user.username, changedPassword));
        HttpResponse<String> rotated = post(current, "/api/v2/account/recovery-codes/rotate",
                GSON.toJson(new CurrentPasswordRequest(changedPassword)));
        assertEquals(200, rotated.statusCode());
        assertEquals(5, json(rotated).getAsJsonObject("data").getAsJsonArray("codes").size());
        assertNotNull(tx.execute(status -> recoveryCodes.findById(unusedCodeId).orElseThrow().revokedAt));

        Fixture raced = newTenant("ID08-raced", Membership.Role.OWNER);
        String racedCode = raced.recoveryCodes.get(0);
        long racedVersion = accounts.findById(raced.userDbId).orElseThrow().credentialVersion;
        String passwordA = "Concurrent-Recovery-A-" + UUID.randomUUID();
        String passwordB = "Concurrent-Recovery-B-" + UUID.randomUUID();
        HttpClient first = newClient(), second = newClient();
        String csrfA = json(get(first, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        String csrfB = json(get(second, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        CountDownLatch raceStart = new CountDownLatch(1);
        var racePool = Executors.newFixedThreadPool(2);
        try {
            var recoverA = racePool.submit(() -> { raceStart.await(); return post(first, "/api/v2/auth/recover",
                    GSON.toJson(new RecoverRequest(raced.username, racedCode, passwordA)), csrfA, origin()); });
            var recoverB = racePool.submit(() -> { raceStart.await(); return post(second, "/api/v2/auth/recover",
                    GSON.toJson(new RecoverRequest(raced.username, racedCode, passwordB)), csrfB, origin()); });
            raceStart.countDown();
            HttpResponse<String> resultA = recoverA.get(30, TimeUnit.SECONDS);
            HttpResponse<String> resultB = recoverB.get(30, TimeUnit.SECONDS);
            assertEquals(200, resultA.statusCode());
            assertEquals(200, resultB.statusCode());
        } finally { racePool.shutdownNow(); }
        UserAccount racedAccount = accounts.findById(raced.userDbId).orElseThrow();
        String racedHash = credentials.findByUserId(raced.userDbId).orElseThrow().passwordHash;
        assertEquals(racedVersion + 1, racedAccount.credentialVersion, "The same recovery code may update the credential only once.");
        assertTrue(passwordEncoder.matches(passwordA, racedHash) ^ passwordEncoder.matches(passwordB, racedHash));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND used_at IS NOT NULL",
                Integer.class, raced.userDbId));
    }

    @Test @Order(9)
    void ID09_invitationTokenIs256BitHashedExpiresAndRedeemsNewAndExistingAccountsOnce() throws Exception {
        Fixture owner = newTenant("ID09-owner", Membership.Role.OWNER);
        Fixture existing = newTenant("ID09-existing", Membership.Role.OWNER);
        Session ownerSession = login(owner.username, owner.password);
        HttpResponse<String> inviteResponse = post(ownerSession, "/api/v2/orgs/" + owner.orgPublicId + "/invitations", GSON.toJson(new RoleRequest("MEMBER")));
        assertEquals(201, inviteResponse.statusCode(), inviteResponse.body());
        assertTrue(inviteResponse.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        assertEquals("\"v0\"", inviteResponse.headers().firstValue("ETag").orElse(""));
        String token = json(inviteResponse).getAsJsonObject("data").get("bearerToken").getAsString();
        var row = invitations.findByTokenHash(C1Secrets.sha256(token)).orElseThrow();
        assertEquals(24L, java.time.Duration.between(row.createdAt, row.expiresAt).toHours());
        assertTrue(token.length() >= 40);

        HttpClient anonymous = newClient();
        String csrf = json(get(anonymous, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        String newName = "invitee-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String newPass = "New-Invited-Account-" + UUID.randomUUID();
        HttpResponse<String> redeemed = post(anonymous, "/api/v2/invitations/redeem",
                GSON.toJson(new RedeemRequest(token, newName, newPass, "Fictional Invitee")), csrf, origin());
        assertEquals(201, redeemed.statusCode(), redeemed.body());
        assertEquals(5, json(redeemed).getAsJsonObject("data").getAsJsonArray("recoveryCodes").size());
        assertTrue(json(redeemed).getAsJsonObject("data").getAsJsonObject("session")
                .get("activeOrgId").isJsonNull(), "New-account establishment must not choose the invited organization.");
        assertEquals(200, get(anonymous, "/api/v2/session").statusCode());
        HttpResponse<String> unselectedLegacyRequest = get(anonymous, "/api/departments");
        assertEquals(409, unselectedLegacyRequest.statusCode());
        assertEquals("ORG_CONTEXT_REQUIRED", json(unselectedLegacyRequest).get("code").getAsString());
        HttpResponse<String> selectInvitedOrganization = post(anonymous, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(owner.orgPublicId)), json(redeemed).getAsJsonObject("data")
                        .get("csrfToken").getAsString(), origin());
        assertEquals(200, selectInvitedOrganization.statusCode(), selectInvitedOrganization.body());
        assertEquals(owner.orgPublicId.toString(), json(get(anonymous, "/api/v2/session"))
                .getAsJsonObject("data").get("activeOrgId").getAsString());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM invitation WHERE token_hash=? AND status='ACCEPTED'", Integer.class,
                (Object) C1Secrets.sha256(token)));
        assertFalse(actualSchemaColumns("invitation").contains("intended_user_id"));
        HttpClient replayClient = newClient();
        String replayToken = json(get(replayClient, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        assertEquals(404, post(replayClient, "/api/v2/invitations/redeem", GSON.toJson(new TokenRequest(token)), replayToken, origin()).statusCode());

        HttpResponse<String> inviteExisting = post(ownerSession, "/api/v2/orgs/" + owner.orgPublicId + "/invitations", GSON.toJson(new RoleRequest("CUSTODIAN")));
        String existingToken = json(inviteExisting).getAsJsonObject("data").get("bearerToken").getAsString();
        Session existingSession = login(existing.username, existing.password);
        HttpResponse<String> joined = post(existingSession, "/api/v2/invitations/redeem", GSON.toJson(new TokenRequest(existingToken)));
        assertEquals(200, joined.statusCode(), joined.body());
        assertEquals(1, memberships.countByOrganizationAndRoleAndStatus(owner.orgDbId, Membership.Role.CUSTODIAN, Membership.Status.ACTIVE));
        assertEquals(404, post(existingSession, "/api/v2/invitations/redeem", GSON.toJson(new TokenRequest(existingToken))).statusCode());

        HttpResponse<String> concurrentInvite = post(ownerSession, "/api/v2/orgs/" + owner.orgPublicId
                + "/invitations", GSON.toJson(new RoleRequest("LEAD")));
        assertEquals(201, concurrentInvite.statusCode(), concurrentInvite.body());
        String concurrentToken = json(concurrentInvite).getAsJsonObject("data").get("bearerToken").getAsString();
        String concurrentUser = "race-invite-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String concurrentPassword = "Invite-Race-Password-" + UUID.randomUUID();
        HttpClient redeemA = newClient(), redeemB = newClient();
        String redeemCsrfA = json(get(redeemA, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        String redeemCsrfB = json(get(redeemB, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        RedeemRequest racedRedeem = new RedeemRequest(concurrentToken, concurrentUser, concurrentPassword, "Raced Invitee");
        CountDownLatch redeemStart = new CountDownLatch(1);
        var redeemPool = Executors.newFixedThreadPool(2);
        HttpResponse<String> redeemResponseA;
        HttpResponse<String> redeemResponseB;
        try {
            var futureA = redeemPool.submit(() -> { redeemStart.await(); return post(redeemA, "/api/v2/invitations/redeem",
                    GSON.toJson(racedRedeem), redeemCsrfA, origin()); });
            var futureB = redeemPool.submit(() -> { redeemStart.await(); return post(redeemB, "/api/v2/invitations/redeem",
                    GSON.toJson(racedRedeem), redeemCsrfB, origin()); });
            redeemStart.countDown();
            redeemResponseA = futureA.get(30, TimeUnit.SECONDS);
            redeemResponseB = futureB.get(30, TimeUnit.SECONDS);
        } finally { redeemPool.shutdownNow(); }
        assertTrue((redeemResponseA.statusCode() == 201 && redeemResponseB.statusCode() == 404)
                || (redeemResponseB.statusCode() == 201 && redeemResponseA.statusCode() == 404),
                "Concurrent redemption statuses=" + redeemResponseA.statusCode() + "," + redeemResponseB.statusCode()
        );
        UserAccount racedInvitee = accounts.findByUsernameKey(concurrentUser).orElseThrow();
        assertEquals(1, memberships.countByOrganizationAndRoleAndStatus(owner.orgDbId,
                Membership.Role.LEAD, Membership.Status.ACTIVE));
        assertEquals(5, jdbc.queryForObject("SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND used_at IS NULL AND revoked_at IS NULL",
                Integer.class, racedInvitee.id));

        HttpResponse<String> revokeInvite = post(ownerSession, "/api/v2/orgs/" + owner.orgPublicId
                + "/invitations", GSON.toJson(new RoleRequest("MEMBER")));
        assertEquals(201, revokeInvite.statusCode(), revokeInvite.body());
        JsonObject revokeView = json(revokeInvite).getAsJsonObject("data");
        String revokedToken = revokeView.get("bearerToken").getAsString();
        String revokedId = revokeView.get("id").getAsString();
        assertEquals(404, post(existingSession, "/api/v2/orgs/" + existing.orgPublicId
                + "/invitations/" + revokedId + "/revoke", "", "\"v0\"").statusCode());
        HttpResponse<String> revoked = post(ownerSession, "/api/v2/orgs/" + owner.orgPublicId
                + "/invitations/" + revokedId + "/revoke", "", "\"v0\"");
        assertEquals(200, revoked.statusCode(), revoked.body());
        assertEquals("REVOKED", json(revoked).getAsJsonObject("data").get("status").getAsString());
        HttpClient revokedRedeem = newClient();
        String revokedCsrf = json(get(revokedRedeem, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        assertEquals(404, post(revokedRedeem, "/api/v2/invitations/redeem",
                GSON.toJson(new TokenRequest(revokedToken)), revokedCsrf, origin()).statusCode());

        HttpResponse<String> expiring = post(ownerSession, "/api/v2/orgs/" + owner.orgPublicId + "/invitations", GSON.toJson(new RoleRequest("LEAD")));
        UUID expiredId = UUID.fromString(json(expiring).getAsJsonObject("data").get("id").getAsString());
        Invitation expiresSoon = invitations.findByOrganizationIdAndPublicId(owner.orgDbId, expiredId).orElseThrow();
        LocalDateTime pastExpiry = now().minusMinutes(1);
        jdbc.update("UPDATE invitation SET created_at=?,expires_at=? WHERE id=?",
                java.sql.Timestamp.valueOf(pastExpiry.minusMinutes(1)),
                java.sql.Timestamp.valueOf(pastExpiry), expiresSoon.id);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM invitation WHERE id=? AND expires_at<=?",
                Integer.class, expiresSoon.id, java.sql.Timestamp.valueOf(now())));
        expiryWorker.expireBoundedBatch();
        assertEquals(Invitation.Status.EXPIRED, invitations.findById(invitations.findByTokenHash(C1Secrets.sha256(json(expiring).getAsJsonObject("data").get("bearerToken").getAsString())).orElseThrow().id).orElseThrow().status);
    }

    @Test @Order(10)
    void ID10_ownerAdminGrantBoundariesNoSelfPromotionAndStrongVersionPreconditions() throws Exception {
        Fixture owner = newTenant("ID10-owner", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Session ownerSession = login(owner.username, owner.password);
        Session adminSession = login(admin.username, admin.password);
        Session memberSession = login(member.username, member.password);
        String prefix = "/api/v2/orgs/" + owner.orgPublicId;
        assertEquals(403, post(ownerSession, prefix + "/invitations", GSON.toJson(new RoleRequest("OWNER"))).statusCode());
        assertEquals(403, post(adminSession, prefix + "/invitations", GSON.toJson(new RoleRequest("ADMIN"))).statusCode());
        assertEquals(403, post(memberSession, prefix + "/invitations", GSON.toJson(new RoleRequest("MEMBER"))).statusCode());
        assertEquals(403, get(memberSession.client, prefix + "/members").statusCode());
        String memberPath = prefix + "/members/" + member.membershipPublicId + "/role";
        assertEquals(428, post(ownerSession, memberPath, GSON.toJson(new RoleRequest("LEAD"))).statusCode());
        assertEquals(412, post(ownerSession, memberPath, GSON.toJson(new RoleRequest("LEAD")), "\"v8\"").statusCode());
        assertEquals(403, post(ownerSession, memberPath, GSON.toJson(new RoleRequest("OWNER")), "\"v0\"").statusCode());
        assertEquals(403, post(adminSession, prefix + "/members/" + owner.membershipPublicId + "/role",
                GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"").statusCode());
        assertEquals(403, post(adminSession, memberPath, GSON.toJson(new RoleRequest("ADMIN")), "\"v0\"").statusCode());
        HttpResponse<String> adminChange = post(adminSession, memberPath, GSON.toJson(new RoleRequest("LEAD")), "\"v0\"");
        assertEquals(200, adminChange.statusCode(), adminChange.body());
        assertEquals(403, post(adminSession, memberPath, GSON.toJson(new RoleRequest("ADMIN")), "\"v1\"").statusCode());
        HttpResponse<String> changed = post(ownerSession, memberPath, GSON.toJson(new RoleRequest("MEMBER")), "\"v1\"");
        assertEquals(200, changed.statusCode(), changed.body());
        assertEquals(0, memberships.countByOrganizationAndRoleAndStatus(owner.orgDbId, Membership.Role.LEAD, Membership.Status.ACTIVE));
        assertEquals(1, memberships.countByOrganizationAndRoleAndStatus(owner.orgDbId, Membership.Role.MEMBER, Membership.Status.ACTIVE));
    }

    @Test @Order(11)
    void ID11_transferLocksOrgAndMembersEnforcesOnePendingAndAtomicallySwapsOwners() throws Exception {
        Fixture owner = newTenant("ID11-owner", Membership.Role.OWNER);
        Fixture adminA = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Fixture adminB = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Session ownerSession = login(owner.username, owner.password);
        String prefix = "/api/v2/orgs/" + owner.orgPublicId;
        CountDownLatch transferStart = new CountDownLatch(1);
        var transferPool = Executors.newFixedThreadPool(2);
        HttpResponse<String> transferA;
        HttpResponse<String> transferB;
        try {
            var startA = transferPool.submit(() -> { transferStart.await(); return post(ownerSession, prefix + "/transfer-ownership",
                    GSON.toJson(new TransferRequest(adminA.membershipPublicId, owner.password))); });
            var startB = transferPool.submit(() -> { transferStart.await(); return post(ownerSession, prefix + "/transfer-ownership",
                    GSON.toJson(new TransferRequest(adminB.membershipPublicId, owner.password))); });
            transferStart.countDown();
            transferA = startA.get(30, TimeUnit.SECONDS);
            transferB = startB.get(30, TimeUnit.SECONDS);
        } finally { transferPool.shutdownNow(); }
        assertTrue((transferA.statusCode() == 201 && transferB.statusCode() == 409)
                || (transferB.statusCode() == 201 && transferA.statusCode() == 409));
        boolean adminAWon = transferA.statusCode() == 201;
        HttpResponse<String> pending = adminAWon ? transferA : transferB;
        Fixture recipientFixture = adminAWon ? adminA : adminB;
        Fixture otherAdminFixture = adminAWon ? adminB : adminA;
        String transferId = json(pending).getAsJsonObject("data").get("id").getAsString();
        String etag = pending.headers().firstValue("ETag").orElseThrow();
        Session wrongRecipient = login(otherAdminFixture.username, otherAdminFixture.password);
        assertEquals(404, post(wrongRecipient, prefix + "/accept-transfer", GSON.toJson(new TransferIdRequest(transferId)), etag).statusCode());
        Session recipient = login(recipientFixture.username, recipientFixture.password);
        HttpResponse<String> accepted = post(recipient, prefix + "/accept-transfer", GSON.toJson(new TransferIdRequest(transferId)), etag);
        assertEquals(200, accepted.statusCode(), accepted.body());
        assertEquals(Membership.Role.ADMIN, memberships.findById(owner.membershipDbId).orElseThrow().role);
        assertEquals(Membership.Role.OWNER, memberships.findById(recipientFixture.membershipDbId).orElseThrow().role);
        assertEquals(1, memberships.countEffectiveActiveOwners(owner.orgDbId));
        assertEquals(OwnershipTransfer.Status.ACCEPTED, transfers.findByOrganizationIdAndPublicId(owner.orgDbId,
                UUID.fromString(transferId)).orElseThrow().status);
        assertEquals(404, post(recipient, prefix + "/accept-transfer", GSON.toJson(new TransferIdRequest(transferId)),
                accepted.headers().firstValue("ETag").orElseThrow()).statusCode());

        HttpResponse<String> revocable = post(recipient, prefix + "/transfer-ownership",
                GSON.toJson(new TransferRequest(otherAdminFixture.membershipPublicId, recipientFixture.password)));
        assertEquals(201, revocable.statusCode(), revocable.body());
        JsonObject revocableData = json(revocable).getAsJsonObject("data");
        String revocableId = revocableData.get("id").getAsString();
        HttpResponse<String> revoked = post(recipient, prefix + "/transfers/" + revocableId + "/revoke", "",
                revocable.headers().firstValue("ETag").orElseThrow());
        assertEquals(200, revoked.statusCode(), revoked.body());
        assertEquals(OwnershipTransfer.Status.REVOKED, transfers.findByOrganizationIdAndPublicId(owner.orgDbId,
                UUID.fromString(revocableId)).orElseThrow().status);

        HttpResponse<String> expiring = post(recipient, prefix + "/transfer-ownership",
                GSON.toJson(new TransferRequest(otherAdminFixture.membershipPublicId, recipientFixture.password)));
        assertEquals(201, expiring.statusCode(), expiring.body());
        String expiringId = json(expiring).getAsJsonObject("data").get("id").getAsString();
        OwnershipTransfer expiringRow = transfers.findByOrganizationIdAndPublicId(owner.orgDbId, UUID.fromString(expiringId)).orElseThrow();
        LocalDateTime expiredAt = now().minusMinutes(1);
        jdbc.update("UPDATE ownership_transfer SET created_at=?,expires_at=? WHERE id=?",
                java.sql.Timestamp.valueOf(expiredAt.minusMinutes(1)), java.sql.Timestamp.valueOf(expiredAt), expiringRow.id);
        expiryWorker.expireBoundedBatch();
        assertEquals(OwnershipTransfer.Status.EXPIRED, transfers.findById(expiringRow.id).orElseThrow().status);
        long expiredVersion = transfers.findById(expiringRow.id).orElseThrow().version;
        assertEquals(409, post(wrongRecipient, prefix + "/accept-transfer", GSON.toJson(new TransferIdRequest(expiringId)),
                "\"v" + expiredVersion + "\"").statusCode());
    }

    @Test @Order(12)
    void ID12_lastOwnerDisableAndConcurrentTransferAcceptCannotLeaveOrganizationOwnerless() throws Exception {
        Fixture owner = newTenant("ID12-owner", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Fixture otherOrg = newTenant("ID12-other", Membership.Role.OWNER);
        addMembership(otherOrg.orgDbId, owner.userDbId, Membership.Role.MEMBER);
        Session ownerSession = login(owner.username, owner.password);
        String prefix = "/api/v2/orgs/" + owner.orgPublicId;
        assertEquals(409, post(ownerSession, "/api/v2/account/disable", GSON.toJson(new CurrentPasswordRequest(owner.password))).statusCode());
        HttpResponse<String> pending = post(ownerSession, prefix + "/transfer-ownership",
                GSON.toJson(new TransferRequest(admin.membershipPublicId, owner.password)));
        assertEquals(201, pending.statusCode(), pending.body());
        Session adminSession = login(admin.username, admin.password);
        String ifMatch = pending.headers().firstValue("ETag").orElseThrow();
        String transferId = json(pending).getAsJsonObject("data").get("id").getAsString();
        var ready = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var accept = pool.submit(() -> { ready.await(); return post(adminSession, prefix + "/accept-transfer",
                    GSON.toJson(new TransferIdRequest(transferId)), ifMatch).statusCode(); });
            var disable = pool.submit(() -> { ready.await(); return post(ownerSession, "/api/v2/account/disable",
                    GSON.toJson(new CurrentPasswordRequest(owner.password))).statusCode(); });
            ready.countDown();
            assertEquals(200, accept.get(30, TimeUnit.SECONDS));
            int disableStatus = disable.get(30, TimeUnit.SECONDS);
            assertTrue(disableStatus == 204 || disableStatus == 409, "disable status=" + disableStatus);
        } finally { pool.shutdownNow(); }
        assertEquals(1, memberships.countEffectiveActiveOwners(owner.orgDbId));
        assertEquals(1, memberships.countEffectiveActiveOwners(otherOrg.orgDbId));
        assertEquals(1, memberships.findByOrganizationIdAndUserId(otherOrg.orgDbId, owner.userDbId).stream()
                .filter(m -> m.status == Membership.Status.ACTIVE).count(), "Global disable preserves the other organization membership record.");
        if (accounts.findById(owner.userDbId).orElseThrow().status == UserAccount.Status.DISABLED)
            assertEquals(401, loginStatus(owner.username, owner.password));
    }

    @Test @Order(13)
    void ID13_jdbcSessionPersistenceAndIdleAndAbsoluteExpiryAreEnforced() throws Exception {
        Fixture user = newTenant("ID13-session", Membership.Role.OWNER);
        Session session = login(user.username, user.password);
        assertEquals(200, get(session.client, "/api/v2/session").statusCode());
        SessionRow stored = jdbc.queryForObject("SELECT PRIMARY_ID,SESSION_ID FROM SPRING_SESSION WHERE PRINCIPAL_NAME=? ORDER BY CREATION_TIME DESC LIMIT 1",
                (rs, row) -> new SessionRow(rs.getString(1), rs.getString(2)), user.username);
        assertNotNull(stored);
        assertEquals(session.sessionId, stored.sessionId);
        String primary = stored.primaryId;
        assertNotNull(primary, "The authenticated security context must be persisted in Spring Session JDBC.");
        byte[] serialized;
        try (Connection connection = dataSource.getConnection(); PreparedStatement query = connection.prepareStatement(
                "SELECT ATTRIBUTE_BYTES FROM SPRING_SESSION_ATTRIBUTES WHERE SESSION_PRIMARY_ID=? AND ATTRIBUTE_NAME=?")) {
            query.setString(1, primary); query.setString(2, "org.orgresource.sessionCreatedAt");
            try (ResultSet row = query.executeQuery()) { assertTrue(row.next()); serialized = row.getBytes(1); }
        }
        long created;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialized))) { created = (Long) input.readObject(); }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) { output.writeObject(created - TimeUnit.HOURS.toMillis(13)); }
        jdbc.update("UPDATE SPRING_SESSION_ATTRIBUTES SET ATTRIBUTE_BYTES=? WHERE SESSION_PRIMARY_ID=? AND ATTRIBUTE_NAME=?",
                bytes.toByteArray(), primary, "org.orgresource.sessionCreatedAt");
        assertEquals(401, get(session.client, "/api/v2/session").statusCode());

        Session idle = login(user.username, user.password);
        SessionRow idleStored = jdbc.queryForObject("SELECT PRIMARY_ID,SESSION_ID FROM SPRING_SESSION WHERE PRINCIPAL_NAME=? ORDER BY CREATION_TIME DESC LIMIT 1",
                (rs, row) -> new SessionRow(rs.getString(1), rs.getString(2)), user.username);
        assertNotNull(idleStored);
        assertEquals(idle.sessionId, idleStored.sessionId);
        jdbc.update("UPDATE SPRING_SESSION SET LAST_ACCESS_TIME=?,EXPIRY_TIME=0 WHERE SESSION_ID=?",
                java.time.Instant.now().minus(2, java.time.temporal.ChronoUnit.HOURS).toEpochMilli(), idleStored.sessionId);
        assertEquals(401, get(idle.client, "/api/v2/session").statusCode());
    }

    @Test @Order(14)
    void ID14_allProtectedLegacyAndV2RoutesDenyAnonymousAndNoC0FallbackUserExists() throws Exception {
        assertTrue(getClass().getClassLoader().getResource("application.properties") != null);
        String[] protectedGets = {"/api/departments", "/api/projects", "/api/resources", "/api/requirements", "/api/allocations",
                "/api/v2/session", "/api/v2/orgs", "/api/v2/account", "/api/v2/orgs/" + UUID.randomUUID() + "/members"};
        for (String path : protectedGets) assertEquals(401, get(newClient(), path).statusCode(), path);
        assertEquals(200, get(newClient(), "/api/v2/auth/csrf").statusCode());
        assertTrue(getClass().getResource("/edu/orgresource/extended/C1IdentityAcceptanceTest.class") != null);
        assertFalse(getClass().getResource("/edu/orgresource/c0/C0UserPrincipal.class") != null);
        assertFalse(organizations.findBySlug("c1-editor").isPresent());
        assertFalse(accounts.findByUsernameKey("c1-editor").isPresent());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM user_account WHERE username_key IN ('c1-editor','c1-viewer')", Integer.class));
        assertNotNull(securityFilterChain);
    }

    @Test @Order(15)
    void ID15_cleanFlywayBootstrapAndEffectiveDependencyVersionsArePresent() throws Exception {
        assertEquals("1", jdbc.queryForObject("SELECT version FROM flyway_schema_history WHERE success=1 ORDER BY installed_rank LIMIT 1", String.class));
        assertEquals(7, jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1", Integer.class));
        String[] tables = jdbc.queryForList("SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema=DATABASE()", String.class).toArray(String[]::new);
        assertTrue(List.of(tables).contains("spring_session"));
        assertTrue(get(newClient(), "/").body().contains("OrgResource"));
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='ownership_transfer' AND column_name='pending_org_id'", Integer.class) == 1);
    }

    @Test @Order(16)
    void ID16_testConfigurationUsesNoLiveSmsAndDoesNotExposeSensitiveAccountData() throws Exception {
        Fixture user = newTenant("ID16-private", Membership.Role.OWNER);
        Session session = login(user.username, user.password);
        HttpResponse<String> account = get(session.client, "/api/v2/account");
        assertEquals(200, account.statusCode());
        String body = account.body();
        assertFalse(body.contains(user.password));
        assertFalse(body.contains(user.recoveryCodes.get(0)));
        assertFalse(body.contains(credentials.findByUserId(user.userDbId).orElseThrow().passwordHash));
        assertFalse(smsEnabled);
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE target_id LIKE '%password%' OR target_id LIKE '%recovery%'", Integer.class));
    }

    private Fixture newTenant(String prefix, Membership.Role role) {
        return tx.execute(status -> createTenant(prefix, role));
    }

    private Fixture createTenant(String prefix, Membership.Role role) {
        LocalDateTime now = now();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String username = C1IdentityNormalization.username(prefix + "-" + suffix);
        String password = "C1-fixture-" + UUID.randomUUID() + "-strong";
        Organization org = organizations.saveAndFlush(new Organization("org-" + suffix, "Fictional " + prefix, now));
        UserAccount account = accounts.saveAndFlush(new UserAccount(username, "Fictional " + prefix, now));
        credentials.saveAndFlush(new LocalCredential(account.id, passwordEncoder.encode(password), now));
        Membership member = memberships.saveAndFlush(new Membership(org.id, account.id, role, now));
        List<String> codes = C1Secrets.recoveryCodes(5);
        UUID batch = C1Secrets.uuid();
        for (String code : codes) recoveryCodes.save(new AccountRecoveryCode(account.id, C1Secrets.sha256(code), batch, now, now.plusDays(365)));
        return new Fixture(org.id, org.publicId, account.id, username, password, member.id, member.publicId, codes);
    }

    private Fixture fixtureFor(String username) {
        UserAccount account = accounts.findByUsernameKey(username).orElseThrow();
        Membership member = memberships.findByOrganizationIdAndUserId(organizations.findBySlug(OWNER_SLUG).orElseThrow().id, account.id).orElseThrow();
        Organization org = organizations.findById(member.organizationId).orElseThrow();
        return new Fixture(org.id, org.publicId, account.id, username, System.getenv("EXT_BOOTSTRAP_PASSWORD"), member.id,
                member.publicId, bootstrapCodes);
    }

    private Fixture fixtureInOrg(long orgId, Membership.Role role) {
        return tx.execute(status -> {
            LocalDateTime now = now();
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            String username = "user-" + suffix;
            String password = "C1-fixture-" + UUID.randomUUID() + "-strong";
            Organization org = organizations.findById(orgId).orElseThrow();
            UserAccount account = accounts.saveAndFlush(new UserAccount(username, "Fictional " + role, now));
            credentials.saveAndFlush(new LocalCredential(account.id, passwordEncoder.encode(password), now));
            Membership member = memberships.saveAndFlush(new Membership(org.id, account.id, role, now));
            return new Fixture(org.id, org.publicId, account.id, username, password, member.id, member.publicId, List.of());
        });
    }

    private void addMembership(long orgId, long userId, Membership.Role role) {
        tx.executeWithoutResult(status -> memberships.saveAndFlush(new Membership(orgId, userId, role, now())));
    }

    @Autowired edu.orgresource.extended.repository.DepartmentRepository departmentRepository;
    @Autowired edu.orgresource.extended.repository.ProjectRepository projectRepository;

    @Test @Order(17)
    void ID17_utcMysqlSessionAndJpaLocalDateTimeRoundTripForYear2040() throws Exception {
        assertEquals("UTC", TimeZone.getDefault().getID());
        assertEquals(java.time.ZoneId.of("Asia/Manila"), clock.getZone());
        Fixture tenant = newTenant("ID17-utc", Membership.Role.OWNER);
        Department department = tx.execute(status -> departmentRepository.saveAndFlush(
                new Department(tenant.orgDbId, "UTC round-trip", "utc round-trip")));
        LocalDateTime startAt = LocalDateTime.of(2040, 1, 1, 0, 30, 15, 123456000);
        LocalDateTime endAt = LocalDateTime.of(2040, 1, 1, 2, 45, 30, 654321000);
        Project saved = tx.execute(status -> projectRepository.saveAndFlush(new Project(
                tenant.orgDbId, department, "UTC year 2040", startAt, endAt)));
        Long projectId = saved.id;

        assertEquals("+00:00", jdbc.queryForObject("SELECT @@session.time_zone", String.class));
        Project reloaded = projectRepository.findById(projectId).orElseThrow();
        assertEquals(startAt, reloaded.startAt);
        assertEquals(endAt, reloaded.endAt);

        try (Connection connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.execute("SET time_zone = '+08:00'");
            }
            try (var statement = connection.prepareStatement("SELECT start_at,end_at FROM project WHERE id=?")) {
                statement.setLong(1, projectId);
                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("2040-01-01 00:30:15.123456", result.getString("start_at"));
                    assertEquals("2040-01-01 02:45:30.654321", result.getString("end_at"));
                }
            } finally {
                try (var statement = connection.createStatement()) {
                    statement.execute("SET time_zone = '+00:00'");
                }
            }
        }
    }

    @Test @Order(18)
    void C1FIX01_loginRequiresExplicitOrganizationAndNeverSelectsFirstOrRevokedMembership() throws Exception {
        Fixture firstOrg = newTenant("C1FIX01-first", Membership.Role.OWNER);
        Fixture secondOrg = newTenant("C1FIX01-second", Membership.Role.OWNER);
        Fixture user = fixtureInOrg(firstOrg.orgDbId, Membership.Role.CUSTODIAN);
        addMembership(secondOrg.orgDbId, user.userDbId, Membership.Role.MEMBER);

        HttpClient client = newClient();
        String preLoginCsrf = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data")
                .get("csrfToken").getAsString();
        HttpResponse<String> login = post(client, "/api/v2/auth/login",
                GSON.toJson(new LoginRequest(user.username, user.password)), preLoginCsrf, origin());
        assertEquals(200, login.statusCode(), login.body());
        assertTrue(json(login).getAsJsonObject("data").getAsJsonObject("session")
                .get("activeOrgId").isJsonNull(), "A multi-organization login must have no implicit tenant.");
        HttpResponse<String> unselected = get(client, "/api/departments");
        assertEquals(409, unselected.statusCode(), unselected.body());
        assertEquals("ORG_CONTEXT_REQUIRED", json(unselected).get("code").getAsString());

        String csrf = json(login).getAsJsonObject("data").get("csrfToken").getAsString();
        HttpResponse<String> firstSelected = post(client, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(firstOrg.orgPublicId)), csrf, origin());
        assertEquals(200, firstSelected.statusCode(), firstSelected.body());
        String firstCsrf = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data")
                .get("csrfToken").getAsString();
        Session selected = new Session(client, firstCsrf, cookie(client), null, firstOrg.orgPublicId.toString());
        assertEquals(201, post(selected, "/api/resources",
                GSON.toJson(new ResourceRequest("C1FIX01 first only", "REUSABLE", 2))).statusCode());

        HttpResponse<String> secondSelected = post(selected, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(secondOrg.orgPublicId)));
        assertEquals(200, secondSelected.statusCode(), secondSelected.body());
        refreshCsrf(selected);
        var secondResources = JsonParser.parseString(get(client, "/api/resources").body()).getAsJsonArray();
        assertTrue(secondResources.asList().stream()
                .noneMatch(row -> row.getAsJsonObject().get("name").getAsString().equals("C1FIX01 first only")));
        HttpResponse<String> switchBack = post(selected, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(firstOrg.orgPublicId)));
        assertEquals(200, switchBack.statusCode(), switchBack.body());
        refreshCsrf(selected);
        var firstResources = JsonParser.parseString(get(client, "/api/resources").body()).getAsJsonArray();
        assertTrue(firstResources.asList().stream()
                .anyMatch(row -> row.getAsJsonObject().get("name").getAsString().equals("C1FIX01 first only")));

        Session firstOwner = login(firstOrg.username, firstOrg.password);
        HttpResponse<String> revoke = post(firstOwner, "/api/v2/orgs/" + firstOrg.orgPublicId + "/members/"
                + user.membershipPublicId + "/suspend", "", "\"v0\"");
        assertEquals(200, revoke.statusCode(), revoke.body());
        HttpResponse<String> revokedSelection = post(selected, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(firstOrg.orgPublicId)));
        assertEquals(404, revokedSelection.statusCode(), revokedSelection.body());

        assertEquals(204, post(selected, "/api/v2/auth/logout", "").statusCode());
        String reloginCsrf = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data")
                .get("csrfToken").getAsString();
        HttpResponse<String> relogin = post(client, "/api/v2/auth/login",
                GSON.toJson(new LoginRequest(user.username, user.password)), reloginCsrf, origin());
        assertEquals(200, relogin.statusCode(), relogin.body());
        assertTrue(json(relogin).getAsJsonObject("data").getAsJsonObject("session")
                .get("activeOrgId").isJsonNull(), "Relogin must not infer the user's remaining organization.");
        HttpResponse<String> reloginLegacy = get(client, "/api/departments");
        assertEquals(409, reloginLegacy.statusCode(), reloginLegacy.body());
        assertEquals("ORG_CONTEXT_REQUIRED", json(reloginLegacy).get("code").getAsString());
    }

    @Test @Order(19)
    void C2_PRIVILEGE01_runtimeCanAppendAuditEventsButCannotUpdateOrDeleteThem() {
        int before = jdbc.queryForObject("SELECT COUNT(*) FROM audit_event", Integer.class);
        int inserted = jdbc.update("INSERT INTO audit_event(org_id,actor_user_id,action,target_type,target_id,outcome,request_id,created_at) "
                + "VALUES(NULL,NULL,'C2_TEST_APPEND','acceptance','fictional','SUCCESS',?,?)",
                UUID.randomUUID().toString(), Timestamp.valueOf(now()));
        assertEquals(1, inserted);
        assertEquals(before + 1, jdbc.queryForObject("SELECT COUNT(*) FROM audit_event", Integer.class));
        assertThrows(org.springframework.dao.DataAccessException.class,
                () -> jdbc.update("UPDATE audit_event SET action=action WHERE action='C2_TEST_APPEND'"));
        assertThrows(org.springframework.dao.DataAccessException.class,
                () -> jdbc.update("DELETE FROM audit_event WHERE action='C2_TEST_APPEND'"));
    }

    @Test @Order(20)
    void C2_LEDGER01_reusableStockHasAppendOnlyMovementAndLateReturnIsReplaySafe() throws Exception {
        Fixture owner = newTenant("C2-ledger-owner", Membership.Role.OWNER);
        Fixture custodian = fixtureInOrg(owner.orgDbId, Membership.Role.CUSTODIAN);
        Session ownerSession = login(owner.username, owner.password);
        Session custodianSession = login(custodian.username, custodian.password);

        HttpResponse<String> departmentResponse = post(ownerSession, "/api/departments",
                GSON.toJson(new NameRequest("C2 Reusable Dept")));
        assertEquals(201, departmentResponse.statusCode(), departmentResponse.body());
        long departmentId = json(departmentResponse).get("id").getAsLong();
        LocalDateTime phtNow = LocalDateTime.now(clock).withNano(0);
        HttpResponse<String> projectResponse = post(ownerSession, "/api/projects", GSON.toJson(new ProjectRequest(
                departmentId, "C2 Reusable Project", phtNow.plusMinutes(5).toString(), phtNow.plusDays(1).toString())));
        assertEquals(201, projectResponse.statusCode(), projectResponse.body());
        long projectId = json(projectResponse).get("id").getAsLong();

        HttpResponse<String> resourceResponse = post(custodianSession, "/api/resources",
                GSON.toJson(new ResourceRequest("C2 Reusable Chairs", "REUSABLE", 10)));
        assertEquals(201, resourceResponse.statusCode(), resourceResponse.body());
        long resourceId = json(resourceResponse).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        UUID resourcePublicId = resource.publicId;
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=? AND kind='INITIAL'",
                Integer.class, owner.orgDbId, resourceId));
        assertEquals(10L, jdbc.queryForObject("SELECT SUM(delta_total) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));

        HttpResponse<String> requirementResponse = post(ownerSession, "/api/requirements",
                GSON.toJson(new RequirementRequest(projectId, resourceId, 6)));
        assertEquals(201, requirementResponse.statusCode(), requirementResponse.body());
        long requirementId = json(requirementResponse).get("id").getAsLong();
        HttpResponse<String> allocationResponse = post(ownerSession, "/api/allocations",
                GSON.toJson(new AllocationRequest(requirementId, 4, "Fictional Borrower", null, false)));
        assertEquals(201, allocationResponse.statusCode(), allocationResponse.body());
        long allocationId = json(allocationResponse).get("id").getAsLong();
        Allocation allocation = allocationRepository.findByIdAndOrganizationId(allocationId, owner.orgDbId).orElseThrow();
        assertEquals(allocation.publicId.toString(), jdbc.queryForObject(
                "SELECT BIN_TO_UUID(public_id,0) FROM allocation WHERE id=? AND organization_id=?", String.class,
                allocationId, owner.orgDbId));

        String root = "/api/v2/orgs/" + owner.orgPublicId;
        HttpResponse<String> resourceState = get(ownerSession.client, root + "/resources/" + resourcePublicId);
        assertEquals(200, resourceState.statusCode(), resourceState.body());
        assertEquals(10, json(resourceState).getAsJsonObject("data").get("availableQuantity").getAsInt());
        assertEquals(201, post(custodianSession, "/api/resources", GSON.toJson(
                new ResourceRequest("C5 Resource Cursor Fixture", "REUSABLE", 0))).statusCode());
        JsonObject firstResource = json(get(ownerSession.client, root + "/resources?limit=1")).getAsJsonObject("data");
        String resourceCursor = firstResource.get("nextCursor").getAsString();
        assertEquals(resourcePublicId.toString(), resourceCursor);
        JsonObject nextResource = json(get(ownerSession.client, root + "/resources?limit=1&after=" + resourceCursor))
                .getAsJsonObject("data");
        assertEquals("C5 Resource Cursor Fixture", nextResource.getAsJsonArray("items").get(0).getAsJsonObject().get("name").getAsString());
        assertTrue(nextResource.get("nextCursor").isJsonNull());
        JsonObject firstMember = json(get(ownerSession.client, root + "/members?limit=1")).getAsJsonObject("data");
        String memberCursor = firstMember.get("nextCursor").getAsString();
        JsonObject nextMember = json(get(ownerSession.client, root + "/members?limit=1&cursor=" + memberCursor))
                .getAsJsonObject("data");
        assertEquals(1, nextMember.getAsJsonArray("items").size());
        assertNotEquals(firstMember.getAsJsonArray("items").get(0).getAsJsonObject().get("id"),
                nextMember.getAsJsonArray("items").get(0).getAsJsonObject().get("id"));
        HttpResponse<String> resourcePage = get(ownerSession.client, root + "/resources?limit=2");
        assertEquals(200, resourcePage.statusCode(), resourcePage.body());
        assertTrue(json(resourcePage).getAsJsonObject("data").getAsJsonArray("items").asList().stream()
                .anyMatch(item -> resourcePublicId.toString().equals(item.getAsJsonObject().get("id").getAsString())));
        HttpResponse<String> allocationState = get(ownerSession.client, root + "/allocations/" + allocation.publicId);
        assertEquals(200, allocationState.statusCode(), allocationState.body());
        HttpResponse<String> allocationPage = get(ownerSession.client, root + "/allocations?limit=2");
        assertEquals(200, allocationPage.statusCode(), allocationPage.body());
        assertTrue(json(allocationPage).getAsJsonObject("data").getAsJsonArray("items").asList().stream()
                .anyMatch(item -> allocation.publicId.toString().equals(item.getAsJsonObject().get("id").getAsString())));

        UUID approveOperation = UUID.randomUUID();
        String approveKey = C1Secrets.bearerToken();
        HttpResponse<String> approved = modernPost(ownerSession, root + "/allocations/" + allocation.publicId + "/approve",
                GSON.toJson(new OperationRequest(approveOperation)), allocationState.headers().firstValue("ETag").orElseThrow(), approveKey);
        assertEquals(200, approved.statusCode(), approved.body());
        assertEquals("APPROVED", json(approved).getAsJsonObject("data").get("allocationStatus").getAsString());
        assertEquals(6, json(get(ownerSession.client, root + "/resources/" + resourcePublicId))
                .getAsJsonObject("data").get("availableQuantity").getAsInt());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, resourceId), "Approval reserves stock without a movement.");

        for (Membership.Role role : List.of(Membership.Role.ADMIN, Membership.Role.LEAD, Membership.Role.MEMBER)) {
            Fixture deniedFixture = fixtureInOrg(owner.orgDbId, role);
            Session deniedSession = login(deniedFixture.username, deniedFixture.password);
            HttpResponse<String> denied = modernPost(deniedSession, root + "/resources/" + resourcePublicId + "/receive",
                    GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)), "\"v0\"", C1Secrets.bearerToken());
            assertEquals(403, denied.statusCode(), role + " must not write inventory: " + denied.body());
        }

        jdbc.update("UPDATE project SET start_at=? WHERE id=?", Timestamp.valueOf(phtNow.minusMinutes(1)), projectId);
        HttpResponse<String> approvedState = get(ownerSession.client, root + "/allocations/" + allocation.publicId);
        UUID issueOperation = UUID.randomUUID();
        String issueKey = C1Secrets.bearerToken();
        String issuePath = root + "/allocations/" + allocation.publicId + "/issue";
        String issueBody = GSON.toJson(new OperationRequest(issueOperation));
        HttpResponse<String> issued = modernPost(custodianSession, issuePath, issueBody,
                approvedState.headers().firstValue("ETag").orElseThrow(), issueKey);
        assertEquals(200, issued.statusCode(), issued.body());
        assertEquals(6, json(get(ownerSession.client, root + "/resources/" + resourcePublicId))
                .getAsJsonObject("data").get("availableQuantity").getAsInt());
        HttpResponse<String> issueReplay = modernPost(custodianSession, issuePath, issueBody, "\"v0\"", issueKey);
        assertEquals(200, issueReplay.statusCode(), issueReplay.body());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND allocation_id=? AND kind='ISSUE'",
                Integer.class, owner.orgDbId, allocationId));

        UUID foreignOrg = UUID.randomUUID();
        HttpResponse<String> foreign = modernPost(custodianSession,
                "/api/v2/orgs/" + foreignOrg + "/resources/" + resourcePublicId + "/receive",
                GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 2)), "\"v0\"", C1Secrets.bearerToken());
        assertEquals(404, foreign.statusCode());

        jdbc.update("UPDATE project SET end_at=? WHERE id=?", Timestamp.valueOf(phtNow.minusSeconds(30)), projectId);
        HttpResponse<String> issuedState = get(ownerSession.client, root + "/allocations/" + allocation.publicId);
        UUID returnOperation = UUID.randomUUID();
        HttpResponse<String> returned = modernPost(custodianSession,
                root + "/allocations/" + allocation.publicId + "/return",
                GSON.toJson(new OperationRequest(returnOperation)), issuedState.headers().firstValue("ETag").orElseThrow(),
                C1Secrets.bearerToken());
        assertEquals(200, returned.statusCode(), returned.body());
        assertEquals(10, json(get(ownerSession.client, root + "/resources/" + resourcePublicId))
                .getAsJsonObject("data").get("availableQuantity").getAsInt());
        assertEquals(10L, jdbc.queryForObject("SELECT SUM(delta_total) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(0L, jdbc.queryForObject("SELECT SUM(delta_issued) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(4L, jdbc.queryForObject("SELECT SUM(quantity) FROM allocation WHERE organization_id=? AND resource_id=? AND status='RETURNED'",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(10, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity.intValue());
        UUID duplicateIssueOperation = UUID.randomUUID();
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO stock_movement(public_id,organization_id,resource_id,"
                + "allocation_id,actor_membership_id,operation_public_id,request_fingerprint,kind,quantity,delta_total,delta_issued,occurred_at) "
                + "VALUES(UUID_TO_BIN(?,0),?,?,?,?,UUID_TO_BIN(?,0),?,'ISSUE',?,0,?,?)", UUID.randomUUID().toString(),
                owner.orgDbId, resourceId, allocationId, custodian.membershipDbId, duplicateIssueOperation.toString(),
                new byte[32], 4, 4, Timestamp.valueOf(now())));
        HttpResponse<String> receipt = get(ownerSession.client, root + "/operations/" + issueOperation);
        assertEquals(200, receipt.statusCode(), receipt.body());
        assertEquals(issueOperation.toString(), json(receipt).getAsJsonObject("data").get("operationId").getAsString());
        HttpResponse<String> movementPage1 = get(ownerSession.client,
                root + "/resources/" + resourcePublicId + "/movements?limit=2");
        assertEquals(200, movementPage1.statusCode(), movementPage1.body());
        assertEquals(2, json(movementPage1).getAsJsonObject("data").getAsJsonArray("items").size());
        UUID cursor = UUID.fromString(json(movementPage1).getAsJsonObject("data").get("nextCursor").getAsString());
        HttpResponse<String> movementPage2 = get(ownerSession.client,
                root + "/resources/" + resourcePublicId + "/movements?limit=2&after=" + cursor);
        assertEquals(200, movementPage2.statusCode(), movementPage2.body());
        assertEquals(1, json(movementPage2).getAsJsonObject("data").getAsJsonArray("items").size());
    }

    @Test @Order(21)
    void C2_LEDGER02_consumableReceiveAdjustReplayAndStaleVersionAreAtomic() throws Exception {
        Fixture owner = newTenant("C2-consume-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        HttpResponse<String> departmentResponse = post(session, "/api/departments", GSON.toJson(new NameRequest("C2 Consumable Dept")));
        long departmentId = json(departmentResponse).get("id").getAsLong();
        LocalDateTime now = LocalDateTime.now(clock).withNano(0);
        HttpResponse<String> projectResponse = post(session, "/api/projects", GSON.toJson(new ProjectRequest(
                departmentId, "C2 Consumable Project", now.plusMinutes(5).toString(), now.plusDays(1).toString())));
        long projectId = json(projectResponse).get("id").getAsLong();
        HttpResponse<String> resourceResponse = post(session, "/api/resources",
                GSON.toJson(new ResourceRequest("C2 Consumable Stock", "CONSUMABLE", 100)));
        long resourceId = json(resourceResponse).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        String resourcePath = root + "/resources/" + resource.publicId;

        HttpResponse<String> initialState = get(session.client, resourcePath);
        UUID receiveOperation = UUID.randomUUID();
        String receiveKey = C1Secrets.bearerToken();
        String receiveBody = GSON.toJson(new ReceiveRequest(receiveOperation, 5));
        HttpResponse<String> received = modernPost(session, resourcePath + "/receive", receiveBody,
                initialState.headers().firstValue("ETag").orElseThrow(), receiveKey);
        assertEquals(201, received.statusCode(), received.body());
        assertEquals(105, json(received).getAsJsonObject("data").get("totalQuantity").getAsInt());
        assertEquals(105, json(get(session.client, resourcePath)).getAsJsonObject("data").get("availableQuantity").getAsInt());
        HttpResponse<String> replay = modernPost(session, resourcePath + "/receive", receiveBody, "\"v0\"", receiveKey);
        assertEquals(201, replay.statusCode(), replay.body());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=? AND kind='RECEIPT'",
                Integer.class, owner.orgDbId, resourceId));
        HttpResponse<String> changedSameKey = modernPost(session, resourcePath + "/receive",
                GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 6)), "\"v0\"", receiveKey);
        assertEquals(409, changedSameKey.statusCode(), changedSameKey.body());
        HttpResponse<String> reusedOperation = modernPost(session, resourcePath + "/receive",
                GSON.toJson(new ReceiveRequest(receiveOperation, 6)), "\"v0\"", C1Secrets.bearerToken());
        assertEquals(409, reusedOperation.statusCode(), reusedOperation.body());
        assertEquals("OPERATION_ID_REUSED", json(reusedOperation).get("code").getAsString());
        LocalDateTime expiredAt = now().minusDays(1);
        jdbc.update("UPDATE idempotency_key SET created_at=?,expires_at=? WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Timestamp.valueOf(expiredAt.minusDays(1)), Timestamp.valueOf(expiredAt), owner.orgDbId, receiveOperation.toString());
        HttpResponse<String> expiredReplay = modernPost(session, resourcePath + "/receive", receiveBody, "\"v0\"", receiveKey);
        assertEquals(200, expiredReplay.statusCode(), expiredReplay.body());
        assertEquals("RECEIPT", json(expiredReplay).getAsJsonObject("data").get("kind").getAsString());
        HttpResponse<String> expiredReceipt = get(session.client, root + "/operations/" + receiveOperation);
        assertEquals(200, expiredReceipt.statusCode(), expiredReceipt.body());
        assertEquals(resource.publicId.toString(), json(expiredReceipt).getAsJsonObject("data").get("resourceId").getAsString());
        jdbc.update("DELETE FROM idempotency_key WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                owner.orgDbId, receiveOperation.toString());
        HttpResponse<String> prunedReplay = modernPost(session, resourcePath + "/receive", receiveBody,
                "\"v0\"", C1Secrets.bearerToken());
        assertEquals(200, prunedReplay.statusCode(), prunedReplay.body());
        assertEquals(105, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=? AND kind='RECEIPT'",
                Integer.class, owner.orgDbId, resourceId));
        HttpResponse<String> staleNewKey = modernPost(session, resourcePath + "/receive",
                GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)), "\"v0\"", C1Secrets.bearerToken());
        assertEquals(412, staleNewKey.statusCode(), staleNewKey.body());

        HttpResponse<String> currentState = get(session.client, resourcePath);
        UUID adjustId = UUID.randomUUID();
        String adjustKey = C1Secrets.bearerToken();
        HttpResponse<String> adjusted = modernPost(session, resourcePath + "/adjust",
                GSON.toJson(new AdjustRequest(adjustId, 102)), currentState.headers().firstValue("ETag").orElseThrow(), adjustKey);
        assertEquals(200, adjusted.statusCode(), adjusted.body());
        HttpResponse<String> afterAdjust = get(session.client, resourcePath);
        assertEquals(102, json(afterAdjust).getAsJsonObject("data").get("totalQuantity").getAsInt());
        HttpResponse<String> noOp = modernPost(session, resourcePath + "/adjust",
                GSON.toJson(new AdjustRequest(UUID.randomUUID(), 102)), afterAdjust.headers().firstValue("ETag").orElseThrow(), C1Secrets.bearerToken());
        assertEquals(422, noOp.statusCode(), noOp.body());
        assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, resourceId));

        HttpResponse<String> requirement = post(session, "/api/requirements",
                GSON.toJson(new RequirementRequest(projectId, resourceId, 40)));
        long requirementId = json(requirement).get("id").getAsLong();
        HttpResponse<String> allocationResponse = post(session, "/api/allocations",
                GSON.toJson(new AllocationRequest(requirementId, 30, "Fictional Consumable User", null, false)));
        assertEquals(201, allocationResponse.statusCode(), allocationResponse.body());
        Allocation allocation = allocationRepository.findByIdAndOrganizationId(
                json(allocationResponse).get("id").getAsLong(), owner.orgDbId).orElseThrow();
        HttpResponse<String> pending = get(session.client, root + "/allocations/" + allocation.publicId);
        HttpResponse<String> approved = modernPost(session, root + "/allocations/" + allocation.publicId + "/approve",
                GSON.toJson(new OperationRequest(UUID.randomUUID())), pending.headers().firstValue("ETag").orElseThrow(), C1Secrets.bearerToken());
        assertEquals(200, approved.statusCode(), approved.body());
        assertEquals(72, json(get(session.client, resourcePath)).getAsJsonObject("data").get("availableQuantity").getAsInt());
        HttpResponse<String> committedState = get(session.client, resourcePath);
        HttpResponse<String> belowCommitments = modernPost(session, resourcePath + "/adjust",
                GSON.toJson(new AdjustRequest(UUID.randomUUID(), 20)), committedState.headers().firstValue("ETag").orElseThrow(),
                C1Secrets.bearerToken());
        assertEquals(409, belowCommitments.statusCode(), belowCommitments.body());
        assertEquals(102, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity);
        assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, resourceId), "A rejected adjustment changes neither physical quantity nor the ledger.");
        jdbc.update("UPDATE project SET start_at=? WHERE id=?", Timestamp.valueOf(now.minusMinutes(1)), projectId);
        HttpResponse<String> approvedState = get(session.client, root + "/allocations/" + allocation.publicId);
        UUID consumeOperation = UUID.randomUUID();
        String consumeKey = C1Secrets.bearerToken();
        String consumeBody = GSON.toJson(new OperationRequest(consumeOperation));
        String consumePath = root + "/allocations/" + allocation.publicId + "/consume";
        HttpResponse<String> consumed = modernPost(session, consumePath, consumeBody,
                approvedState.headers().firstValue("ETag").orElseThrow(), consumeKey);
        assertEquals(200, consumed.statusCode(), consumed.body());
        assertEquals(72, json(get(session.client, resourcePath)).getAsJsonObject("data").get("availableQuantity").getAsInt());
        HttpResponse<String> consumeReplay = modernPost(session, consumePath, consumeBody, "\"v0\"", consumeKey);
        assertEquals(200, consumeReplay.statusCode(), consumeReplay.body());
        assertEquals(72, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity);
        assertEquals(72L, jdbc.queryForObject("SELECT SUM(delta_total) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(0L, jdbc.queryForObject("SELECT SUM(delta_issued) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(30L, jdbc.queryForObject("SELECT SUM(quantity) FROM allocation WHERE organization_id=? AND resource_id=? AND status='CONSUMED'",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=? AND kind='CONSUME'",
                Integer.class, owner.orgDbId, resourceId));
    }

    @Test @Order(22)
    void C2_TINV03_expiredApprovedHoldIsReleasedButIssuedCustodyRemainsAtEndBoundary() throws Exception {
        Fixture owner = newTenant("C2-boundary-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long departmentId = json(post(session, "/api/departments", GSON.toJson(new NameRequest("C2 Boundary Dept"))))
                .get("id").getAsLong();
        LocalDateTime sampledNow = LocalDateTime.now(clock).withNano(0);
        long activeProjectId = json(post(session, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C2 Active Boundary Project", sampledNow.plusMinutes(5).toString(), sampledNow.plusDays(1).toString()))))
                .get("id").getAsLong();
        long expiringProjectId = json(post(session, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C2 Expiring Boundary Project", sampledNow.plusMinutes(5).toString(), sampledNow.plusDays(1).toString()))))
                .get("id").getAsLong();
        long resourceId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest(
                "C2 Boundary Chairs", "REUSABLE", 10)))).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        long activeRequirement = json(post(session, "/api/requirements", GSON.toJson(new RequirementRequest(
                activeProjectId, resourceId, 10)))).get("id").getAsLong();
        long expiringRequirement = json(post(session, "/api/requirements", GSON.toJson(new RequirementRequest(
                expiringProjectId, resourceId, 4)))).get("id").getAsLong();
        long holdId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(
                activeRequirement, 3, "Fictional hold", null, false)))).get("id").getAsLong();
        long issuedId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(
                activeRequirement, 2, "Fictional issued", null, false)))).get("id").getAsLong();
        long expiringId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(
                expiringRequirement, 4, "Fictional expired", null, false)))).get("id").getAsLong();
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        for (long id : List.of(holdId, issuedId, expiringId)) {
            Allocation allocation = allocationRepository.findByIdAndOrganizationId(id, owner.orgDbId).orElseThrow();
            HttpResponse<String> state = get(session.client, root + "/allocations/" + allocation.publicId);
            HttpResponse<String> approved = modernPost(session, root + "/allocations/" + allocation.publicId + "/approve",
                    GSON.toJson(new OperationRequest(UUID.randomUUID())), state.headers().firstValue("ETag").orElseThrow(),
                    C1Secrets.bearerToken());
            assertEquals(200, approved.statusCode(), approved.body());
        }
        jdbc.update("UPDATE project SET start_at=? WHERE id=?", Timestamp.valueOf(sampledNow.minusMinutes(1)), activeProjectId);
        Allocation issued = allocationRepository.findByIdAndOrganizationId(issuedId, owner.orgDbId).orElseThrow();
        HttpResponse<String> issuedState = get(session.client, root + "/allocations/" + issued.publicId);
        HttpResponse<String> issue = modernPost(session, root + "/allocations/" + issued.publicId + "/issue",
                GSON.toJson(new OperationRequest(UUID.randomUUID())), issuedState.headers().firstValue("ETag").orElseThrow(),
                C1Secrets.bearerToken());
        assertEquals(200, issue.statusCode(), issue.body());
        LocalDateTime projectEnd = LocalDateTime.now(clock).withNano(0);
        jdbc.update("UPDATE project SET start_at=?,end_at=? WHERE id=?",
                Timestamp.valueOf(projectEnd.minusMinutes(5)), Timestamp.valueOf(projectEnd), expiringProjectId);

        assertEquals(5, inventory.availableQuantity(resource, projectEnd),
                "At endAt, the q4 approval no longer holds stock while q3 and issued custody q2 remain committed.");
        assertEquals(5, json(get(session.client, root + "/resources/" + resource.publicId))
                .getAsJsonObject("data").get("availableQuantity").getAsInt());
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, resourceId), "Approval and expiry do not create movements; only INITIAL and ISSUE do.");
        assertEquals(10L, jdbc.queryForObject("SELECT SUM(delta_total) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(2L, jdbc.queryForObject("SELECT SUM(delta_issued) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
    }

    @Test @Order(23)
    void C2_CONCURRENCY01_twoApprovalsAndTwoIssuesCannotDoubleCommitStock() throws Exception {
        Fixture owner = newTenant("C2-race-owner", Membership.Role.OWNER);
        Fixture custodian = fixtureInOrg(owner.orgDbId, Membership.Role.CUSTODIAN);
        Session ownerSession = login(owner.username, owner.password);
        Session custodianSession = login(custodian.username, custodian.password);
        long departmentId = json(post(ownerSession, "/api/departments", GSON.toJson(new NameRequest("C2 Race Dept"))))
                .get("id").getAsLong();
        LocalDateTime projectStart = LocalDateTime.now(clock).withNano(0).plusMinutes(5);
        long projectId = json(post(ownerSession, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C2 Race Project", projectStart.toString(), projectStart.plusDays(1).toString())))).get("id").getAsLong();
        long resourceId = json(post(ownerSession, "/api/resources", GSON.toJson(new ResourceRequest(
                "C2 Race Chairs", "REUSABLE", 5)))).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        long requirementId = json(post(ownerSession, "/api/requirements", GSON.toJson(new RequirementRequest(
                projectId, resourceId, 8)))).get("id").getAsLong();
        long firstId = json(post(ownerSession, "/api/allocations", GSON.toJson(new AllocationRequest(
                requirementId, 4, "Fictional race one", null, false)))).get("id").getAsLong();
        long secondId = json(post(ownerSession, "/api/allocations", GSON.toJson(new AllocationRequest(
                requirementId, 4, "Fictional race two", null, false)))).get("id").getAsLong();
        Allocation first = allocationRepository.findByIdAndOrganizationId(firstId, owner.orgDbId).orElseThrow();
        Allocation second = allocationRepository.findByIdAndOrganizationId(secondId, owner.orgDbId).orElseThrow();
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        String firstPath = root + "/allocations/" + first.publicId + "/approve";
        String secondPath = root + "/allocations/" + second.publicId + "/approve";
        String firstEtag = get(ownerSession.client, root + "/allocations/" + first.publicId).headers().firstValue("ETag").orElseThrow();
        String secondEtag = get(ownerSession.client, root + "/allocations/" + second.publicId).headers().firstValue("ETag").orElseThrow();
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        HttpResponse<String> firstApproval;
        HttpResponse<String> secondApproval;
        try {
            var one = pool.submit(() -> { start.await(); return modernPost(ownerSession, firstPath,
                    GSON.toJson(new OperationRequest(UUID.randomUUID())), firstEtag, C1Secrets.bearerToken()); });
            var two = pool.submit(() -> { start.await(); return modernPost(custodianSession, secondPath,
                    GSON.toJson(new OperationRequest(UUID.randomUUID())), secondEtag, C1Secrets.bearerToken()); });
            start.countDown();
            firstApproval = one.get(30, TimeUnit.SECONDS);
            secondApproval = two.get(30, TimeUnit.SECONDS);
        } finally { pool.shutdownNow(); }
        assertEquals(1, List.of(firstApproval, secondApproval).stream().filter(r -> r.statusCode() == 200).count());
        assertEquals(1, List.of(firstApproval, secondApproval).stream().filter(r -> r.statusCode() == 409).count());
        assertEquals(1, json(get(ownerSession.client, root + "/resources/" + resource.publicId))
                .getAsJsonObject("data").get("availableQuantity").getAsInt());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE organization_id=? AND resource_id=? AND status='APPROVED'",
                Integer.class, owner.orgDbId, resourceId));

        Allocation loser = firstApproval.statusCode() == 409 ? first : second;
        HttpResponse<String> loserState = get(ownerSession.client, root + "/allocations/" + loser.publicId);
        HttpResponse<String> cancelled = modernPost(ownerSession, root + "/allocations/" + loser.publicId + "/cancel",
                GSON.toJson(new OperationRequest(UUID.randomUUID())), loserState.headers().firstValue("ETag").orElseThrow(),
                C1Secrets.bearerToken());
        assertEquals(200, cancelled.statusCode(), cancelled.body());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, resourceId), "Cancelling the losing pending request releases no movement because it reserved none.");

        Allocation winner = firstApproval.statusCode() == 200 ? first : second;
        jdbc.update("UPDATE project SET start_at=? WHERE id=?", Timestamp.valueOf(projectStart.minusMinutes(6)), projectId);
        String issuePath = root + "/allocations/" + winner.publicId + "/issue";
        String issuedEtag = get(ownerSession.client, root + "/allocations/" + winner.publicId).headers().firstValue("ETag").orElseThrow();
        CountDownLatch issueStart = new CountDownLatch(1);
        var issuePool = Executors.newFixedThreadPool(2);
        HttpResponse<String> issueOne;
        HttpResponse<String> issueTwo;
        try {
            var one = issuePool.submit(() -> { issueStart.await(); return modernPost(ownerSession, issuePath,
                    GSON.toJson(new OperationRequest(UUID.randomUUID())), issuedEtag, C1Secrets.bearerToken()); });
            var two = issuePool.submit(() -> { issueStart.await(); return modernPost(custodianSession, issuePath,
                    GSON.toJson(new OperationRequest(UUID.randomUUID())), issuedEtag, C1Secrets.bearerToken()); });
            issueStart.countDown();
            issueOne = one.get(30, TimeUnit.SECONDS);
            issueTwo = two.get(30, TimeUnit.SECONDS);
        } finally { issuePool.shutdownNow(); }
        assertEquals(1, List.of(issueOne, issueTwo).stream().filter(r -> r.statusCode() == 200).count());
        assertEquals(1, List.of(issueOne, issueTwo).stream().filter(r -> r.statusCode() == 409 || r.statusCode() == 412).count());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND allocation_id=? AND kind='ISSUE'",
                Integer.class, owner.orgDbId, winner.id));
        assertEquals(5L, jdbc.queryForObject("SELECT SUM(delta_total) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
        assertEquals(4L, jdbc.queryForObject("SELECT SUM(delta_issued) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Long.class, owner.orgDbId, resourceId));
    }

    @Test @Order(24)
    void C2_IDEMP02_failureBetweenQuantityAndMovementRollsBackEverything() throws Exception {
        Fixture owner = newTenant("C2-rollback-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long resourceId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest(
                "C2 Rollback Stock", "CONSUMABLE", 5)))).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        String path = root + "/resources/" + resource.publicId;
        HttpResponse<String> initial = get(session.client, path);
        UUID operationId = UUID.randomUUID();
        String key = C1Secrets.bearerToken();
        String body = GSON.toJson(new ReceiveRequest(operationId, 2));
        String trigger = "c2_fail_movement_" + UUID.randomUUID().toString().replace("-", "");
        String migrationUrl = System.getenv("EXT_DB_MIGRATION_URL");
        String migrationUser = System.getenv("EXT_DB_MIGRATION_USER");
        String migrationPassword = System.getenv("EXT_DB_MIGRATION_PASSWORD");
        assertTrue(trigger.matches("c2_fail_movement_[a-f0-9]{32}"));
        boolean created = false;
        try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
             var statement = migration.createStatement()) {
            statement.execute("CREATE TRIGGER `" + trigger + "` BEFORE INSERT ON stock_movement FOR EACH ROW "
                    + "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='C2_FORCED_MOVEMENT_FAILURE'");
            created = true;
            HttpResponse<String> failed = modernPost(session, path + "/receive", body,
                    initial.headers().firstValue("ETag").orElseThrow(), key);
            assertEquals(500, failed.statusCode(), failed.body());
            assertEquals(5, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity);
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                    Integer.class, owner.orgDbId, resourceId));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                    Integer.class, owner.orgDbId, operationId.toString()), "Failed transaction must not leave a CLAIMED or successful receipt.");
        } finally {
            if (created) try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
                    var statement = migration.createStatement()) { statement.execute("DROP TRIGGER IF EXISTS `" + trigger + "`"); }
        }
    }

    @Test @Order(25)
    void C2_SCHEMA02_tenantForeignKeysAndResourceHistoryDeletionAreEnforcedByMysql() throws Exception {
        Fixture tenantA = newTenant("C2-schema-a", Membership.Role.OWNER);
        Fixture tenantB = newTenant("C2-schema-b", Membership.Role.OWNER);
        Session sessionA = login(tenantA.username, tenantA.password);
        Session sessionB = login(tenantB.username, tenantB.password);
        long sharedNameA = json(post(sessionA, "/api/resources", GSON.toJson(new ResourceRequest(
                "Same fictional name", "REUSABLE", 0)))).get("id").getAsLong();
        long sharedNameB = json(post(sessionB, "/api/resources", GSON.toJson(new ResourceRequest(
                "Same fictional name", "REUSABLE", 1)))).get("id").getAsLong();
        assertNotEquals(sharedNameA, sharedNameB);
        UUID crossTenantOperation = UUID.randomUUID();
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO stock_movement(public_id,organization_id,resource_id,"
                + "allocation_id,actor_membership_id,operation_public_id,request_fingerprint,kind,quantity,delta_total,delta_issued,occurred_at) "
                + "VALUES(UUID_TO_BIN(?,0),?,?,NULL,?,UUID_TO_BIN(?,0),?,'RECEIPT',1,1,0,?)", UUID.randomUUID().toString(),
                tenantA.orgDbId, sharedNameB, tenantA.membershipDbId, crossTenantOperation.toString(), new byte[32], Timestamp.valueOf(now())));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, tenantA.orgDbId, crossTenantOperation.toString()));

        long foreignDepartment = json(post(sessionB, "/api/departments", GSON.toJson(new NameRequest("C2 Foreign Dept"))))
                .get("id").getAsLong();
        LocalDateTime foreignStart = LocalDateTime.now(clock).withNano(0).plusMinutes(5);
        long foreignProject = json(post(sessionB, "/api/projects", GSON.toJson(new ProjectRequest(foreignDepartment,
                "C2 Foreign Project", foreignStart.toString(), foreignStart.plusDays(1).toString())))).get("id").getAsLong();
        long foreignRequirement = json(post(sessionB, "/api/requirements", GSON.toJson(new RequirementRequest(
                foreignProject, sharedNameB, 1)))).get("id").getAsLong();
        long foreignAllocationId = json(post(sessionB, "/api/allocations", GSON.toJson(new AllocationRequest(
                foreignRequirement, 1, "Fictional foreign allocation", null, false)))).get("id").getAsLong();
        HttpResponse<String> foreignNumericAction = post(sessionA, "/api/allocations/" + foreignAllocationId + "/approve", "");
        assertEquals(404, foreignNumericAction.statusCode(), foreignNumericAction.body());
        Resource foreignResource = resourceRepository.findByIdAndOrganizationId(sharedNameB, tenantB.orgDbId).orElseThrow();
        HttpResponse<String> foreignUuidAction = modernPost(sessionA,
                "/api/v2/orgs/" + tenantB.orgPublicId + "/resources/" + foreignResource.publicId + "/receive",
                GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)), "\"v0\"", C1Secrets.bearerToken());
        assertEquals(404, foreignUuidAction.statusCode(), foreignUuidAction.body());
        assertEquals("PENDING", allocationRepository.findByIdAndOrganizationId(foreignAllocationId, tenantB.orgDbId)
                .orElseThrow().status.name());

        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, tenantA.orgDbId, sharedNameA), "A virgin zero-stock Resource creates no artificial INITIAL movement.");
        assertEquals(1, jdbc.update("DELETE FROM resource WHERE id=? AND organization_id=?", sharedNameA, tenantA.orgDbId),
                "A virgin zero-stock Resource with no dependencies is physically deletable.");

        Resource historic = resourceRepository.findByIdAndOrganizationId(sharedNameB, tenantB.orgDbId).orElseThrow();
        HttpResponse<String> state = get(sessionB.client, "/api/v2/orgs/" + tenantB.orgPublicId + "/resources/" + historic.publicId);
        HttpResponse<String> zeroed = modernPost(sessionB, "/api/v2/orgs/" + tenantB.orgPublicId
                + "/resources/" + historic.publicId + "/adjust", GSON.toJson(new AdjustRequest(UUID.randomUUID(), 0)),
                state.headers().firstValue("ETag").orElseThrow(), C1Secrets.bearerToken());
        assertEquals(200, zeroed.statusCode(), zeroed.body());
        assertEquals(0, resourceRepository.findByIdAndOrganizationId(sharedNameB, tenantB.orgDbId).orElseThrow().totalQuantity);
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("DELETE FROM resource WHERE id=? AND organization_id=?", sharedNameB, tenantB.orgDbId),
                "Immutable movement history must prevent deleting even a zero-balance Resource.");

        String migrationUrl = System.getenv("EXT_DB_MIGRATION_URL");
        String migrationUser = System.getenv("EXT_DB_MIGRATION_USER");
        String migrationPassword = System.getenv("EXT_DB_MIGRATION_PASSWORD");
        try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
             var statement = migration.createStatement()) {
            statement.executeUpdate("UPDATE resource SET total_quantity=2 WHERE organization_id=" + tenantB.orgDbId
                    + " AND id=" + sharedNameB);
        }
        HttpResponse<String> corruptBalance = get(sessionB.client, "/api/v2/orgs/" + tenantB.orgPublicId
                + "/resources/" + historic.publicId);
        assertEquals(500, corruptBalance.statusCode(), corruptBalance.body());
        assertEquals("INVENTORY_INTEGRITY_FAILURE", json(corruptBalance).get("code").getAsString());
        try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
             var statement = migration.createStatement()) {
            statement.executeUpdate("UPDATE resource SET total_quantity=0 WHERE organization_id=" + tenantB.orgDbId
                    + " AND id=" + sharedNameB);
        }
    }

    @Test @Order(26)
    void C2_AUTH02_suspendedCustodianCannotReplayPriorReceipt() throws Exception {
        Fixture owner = newTenant("C2-suspend-owner", Membership.Role.OWNER);
        Fixture custodian = fixtureInOrg(owner.orgDbId, Membership.Role.CUSTODIAN);
        Session ownerSession = login(owner.username, owner.password);
        Session custodianSession = login(custodian.username, custodian.password);
        long resourceId = json(post(ownerSession, "/api/resources", GSON.toJson(new ResourceRequest(
                "C2 Suspended Custodian Stock", "CONSUMABLE", 3)))).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        String path = root + "/resources/" + resource.publicId;
        HttpResponse<String> state = get(custodianSession.client, path);
        UUID operationId = UUID.randomUUID();
        String key = C1Secrets.bearerToken();
        String body = GSON.toJson(new ReceiveRequest(operationId, 1));
        HttpResponse<String> committed = modernPost(custodianSession, path + "/receive", body,
                state.headers().firstValue("ETag").orElseThrow(), key);
        assertEquals(201, committed.statusCode(), committed.body());
        jdbc.update("UPDATE membership SET status='SUSPENDED',version=version+1,updated_at=? WHERE id=? AND org_id=?",
                Timestamp.valueOf(now()), custodian.membershipDbId, owner.orgDbId);
        HttpResponse<String> replay = modernPost(custodianSession, path + "/receive", body, "\"v0\"", key);
        assertEquals(404, replay.statusCode(), replay.body());
        assertEquals(4, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=? AND kind='RECEIPT'",
                Integer.class, owner.orgDbId, resourceId));
        HttpResponse<String> ownerReceipt = get(ownerSession.client, root + "/operations/" + operationId);
        assertEquals(200, ownerReceipt.statusCode(), ownerReceipt.body());
    }

    @Test @Order(27)
    void C2_SCHEMA03_v3RefusesLegacyNonzeroStockWithoutInventedHistory() throws Exception {
        String schema = "orgresource_c2_legacy_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        assertTrue(schema.matches("orgresource_c2_legacy_[a-f0-9]{16}"));
        String migrationUrl = System.getenv("EXT_DB_MIGRATION_URL");
        String migrationUser = System.getenv("EXT_DB_MIGRATION_USER");
        String migrationPassword = System.getenv("EXT_DB_MIGRATION_PASSWORD");
        String currentSchema;
        try (Connection connection = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword)) {
            currentSchema = connection.getCatalog();
        }
        String legacyUrl = migrationUrl.replace("/" + currentSchema, "/" + schema);
        boolean created = false;
        try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
             var statement = migration.createStatement()) {
            statement.execute("CREATE DATABASE `" + schema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            created = true;
            org.flywaydb.core.Flyway.configure().dataSource(legacyUrl, migrationUser, migrationPassword)
                    .target(org.flywaydb.core.api.MigrationVersion.fromVersion("2")).load().migrate();
            try (Connection legacy = DriverManager.getConnection(legacyUrl, migrationUser, migrationPassword);
                 var seed = legacy.createStatement()) {
                seed.executeUpdate("INSERT INTO organization(public_id,slug,display_name,status,created_at,updated_at) "
                        + "VALUES(UUID_TO_BIN(UUID(),0),'legacy-stock','Fictional legacy stock','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))");
                seed.executeUpdate("INSERT INTO resource(organization_id,name,name_key,type,total_quantity) "
                        + "SELECT id,'Legacy Resource','legacy resource','REUSABLE',5 FROM organization WHERE slug='legacy-stock'");
            }
            RuntimeException failed = assertThrows(RuntimeException.class, () -> org.flywaydb.core.Flyway.configure()
                    .dataSource(legacyUrl, migrationUser, migrationPassword).load().migrate());
            StringBuilder detail = new StringBuilder();
            for (Throwable cause = failed; cause != null; cause = cause.getCause()) detail.append(cause).append('\n');
            assertTrue(detail.toString().contains("LEGACY_STOCK_HISTORY_UNRECONCILED"), detail.toString());
            try (Connection legacy = DriverManager.getConnection(legacyUrl, migrationUser, migrationPassword);
                 var query = legacy.createStatement()) {
                try (ResultSet row = query.executeQuery("SELECT total_quantity FROM resource WHERE name='Legacy Resource'")) {
                    assertTrue(row.next()); assertEquals(5, row.getInt(1));
                }
                try (ResultSet column = query.executeQuery("SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema=DATABASE() AND table_name='resource' AND column_name='public_id'")) {
                    assertTrue(column.next()); assertEquals(0, column.getInt(1), "The preflight blocker must run before V3 schema changes.");
                }
            }
        } finally {
            if (created) try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
                    var statement = migration.createStatement()) { statement.execute("DROP DATABASE `" + schema + "`"); }
        }
    }

    @Test @Order(44)
    void C5_legacyZeroStockUpgradesToV5WithoutInventedLedgerRows() throws Exception {
        String schema = "orgresource_c2_upgrade_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String url = System.getenv("EXT_DB_MIGRATION_URL");
        String user = System.getenv("EXT_DB_MIGRATION_USER");
        String password = System.getenv("EXT_DB_MIGRATION_PASSWORD");
        try (Connection connection = DriverManager.getConnection(url, user, password);
             var statement = connection.createStatement()) {
            String legacyUrl = url.replace("/" + connection.getCatalog(), "/" + schema);
            statement.execute("CREATE DATABASE `" + schema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            try {
                org.flywaydb.core.Flyway.configure().dataSource(legacyUrl, user, password)
                        .target(org.flywaydb.core.api.MigrationVersion.fromVersion("2")).load().migrate();
                try (Connection legacy = DriverManager.getConnection(legacyUrl, user, password); var seed = legacy.createStatement()) {
                    seed.executeUpdate("INSERT INTO organization(public_id,slug,display_name,status,created_at,updated_at) "
                            + "VALUES(UUID_TO_BIN(UUID(),0),'legacy-safe','Fictional safe legacy','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))");
                    seed.executeUpdate("INSERT INTO resource(id,organization_id,name,name_key,type,total_quantity) "
                            + "SELECT 77,id,'Legacy Empty Resource','legacy empty resource','REUSABLE',0 FROM organization");
                }
                var flyway = org.flywaydb.core.Flyway.configure().dataSource(legacyUrl, user, password)
                        .target(org.flywaydb.core.api.MigrationVersion.fromVersion("5")).load();
                assertEquals(3, flyway.migrate().migrationsExecuted);
                flyway.validate();
                assertEquals("5", flyway.info().current().getVersion().getVersion());
                try (Connection upgraded = DriverManager.getConnection(legacyUrl, user, password);
                     var query = upgraded.createStatement();
                     ResultSet rows = query.executeQuery("SELECT id, BIN_TO_UUID(public_id,0), total_quantity, "
                             + "(SELECT COUNT(*) FROM stock_movement) FROM resource")) {
                    assertTrue(rows.next());
                    assertEquals(77, rows.getLong(1));
                    assertNotNull(UUID.fromString(rows.getString(2)));
                    assertEquals(0, rows.getInt(3));
                    assertEquals(0, rows.getInt(4));
                    assertFalse(rows.next());
                }
                try (Connection upgraded = DriverManager.getConnection(legacyUrl, user, password);
                     var seed = upgraded.createStatement()) {
                    seed.executeUpdate("INSERT INTO department(id,organization_id,name,name_key) "
                            + "SELECT 77,id,'Legacy department','legacy department' FROM organization");
                    seed.executeUpdate("INSERT INTO project(id,organization_id,department_id,name,start_at,end_at) "
                            + "SELECT 77,id,77,'Legacy project','2040-01-01','2040-01-02' FROM organization");
                    seed.executeUpdate("INSERT INTO resource_requirement(id,organization_id,project_id,resource_id,quantity) "
                            + "SELECT 77,id,77,77,3 FROM organization");
                    seed.executeUpdate("INSERT INTO allocation(id,public_id,organization_id,requirement_id,resource_id,quantity,"
                            + "borrower_name,status,created_at) SELECT 77,UUID_TO_BIN(UUID(),0),id,77,77,1,"
                            + "'Legacy fictional label','PENDING','2039-01-01' FROM organization");
                }
                var current = org.flywaydb.core.Flyway.configure().dataSource(legacyUrl, user, password)
                        .target(org.flywaydb.core.api.MigrationVersion.fromVersion("6")).load();
                assertEquals(1, current.migrate().migrationsExecuted);
                current.validate();
                try (Connection upgraded = DriverManager.getConnection(legacyUrl, user, password);
                     var query = upgraded.createStatement();
                     ResultSet rows = query.executeQuery("SELECT BIN_TO_UUID(p.public_id,0), BIN_TO_UUID(q.public_id,0), "
                             + "p.lead_membership_id, a.borrower_membership_id, a.requested_by_membership_id, a.borrower_name "
                             + "FROM project p JOIN resource_requirement q ON q.project_id=p.id JOIN allocation a ON a.requirement_id=q.id WHERE p.id=77")) {
                    assertTrue(rows.next());
                    assertNotNull(UUID.fromString(rows.getString(1)));
                    assertNotNull(UUID.fromString(rows.getString(2)));
                    assertNull(rows.getObject(3));
                    assertNull(rows.getObject(4));
                    assertNull(rows.getObject(5));
                    assertEquals("Legacy fictional label", rows.getString(6));
                }
                var provenance = org.flywaydb.core.Flyway.configure().dataSource(legacyUrl, user, password).load();
                assertEquals(1, provenance.migrate().migrationsExecuted);
                provenance.validate();
                try (Connection upgraded = DriverManager.getConnection(legacyUrl, user, password);
                     var query = upgraded.createStatement(); ResultSet rows = query.executeQuery(
                             "SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NULL AND demo_as_of IS NULL")) {
                    assertTrue(rows.next());
                    assertEquals(1, rows.getInt(1));
                }
            } finally {
                statement.execute("DROP DATABASE `" + schema + "`");
            }
        }
    }

    @Test @Order(28)
    void C3_INPUT01_emptyInventoryBodyReturnsControlledValidationError() throws Exception {
        Fixture owner = newTenant("C3-empty-body-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        HttpRequest request = HttpRequest.newBuilder(URI.create(base() + "/api/v2/orgs/" + owner.orgPublicId
                        + "/resources/" + UUID.randomUUID() + "/receive"))
                .header("Origin", origin())
                .header("X-CSRF-TOKEN", session.csrf)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = session.client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), response.body());
        assertEquals("VALIDATION_FAILED", json(response).get("code").getAsString());
    }

    @Test @Order(35)
    void C3_INPUT02_malformedJsonAndUuidAreControlledClientErrors() throws Exception {
        Fixture owner = newTenant("C3-malformed-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        String path = "/api/v2/orgs/" + owner.orgPublicId + "/resources/" + UUID.randomUUID() + "/receive";
        HttpRequest malformedJson = HttpRequest.newBuilder(URI.create(base() + path))
                .header("Origin", origin())
                .header("X-CSRF-TOKEN", session.csrf)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("If-Match", "\"v0\"")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"operationId\":"))
                .build();
        HttpResponse<String> malformed = session.client.send(malformedJson, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, malformed.statusCode(), malformed.body());
        assertEquals("application/json", malformed.headers().firstValue("Content-Type").orElse("").split(";")[0]);
        assertEquals("INVALID_REQUEST_BODY", json(malformed).get("code").getAsString());

        HttpResponse<String> invalidUuid = get(session.client, "/api/v2/orgs/" + owner.orgPublicId + "/resources/not-a-uuid");
        assertEquals(400, invalidUuid.statusCode(), invalidUuid.body());
        assertEquals("INVALID_PATH_PARAMETER", json(invalidUuid).get("code").getAsString());
    }

    @Test @Order(29)
    void C3_PARITY01_legacyListsFiltersAndProtectedCrudAreTenantScoped() throws Exception {
        Fixture owner = newTenant("C3-parity-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long departmentId = json(post(session, "/api/departments", GSON.toJson(new NameRequest("C3 Parity Department")))).get("id").getAsLong();
        long duplicateDepartmentId = json(post(session, "/api/departments", GSON.toJson(new NameRequest("C3 Parity Other Department")))).get("id").getAsLong();
        HttpResponse<String> departmentRename = post(session, "/api/departments/" + duplicateDepartmentId + "/update",
                GSON.toJson(new NameRequest("C3 Parity Department")));
        assertEquals(409, departmentRename.statusCode(), departmentRename.body());
        assertEquals(200, post(session, "/api/departments/" + duplicateDepartmentId + "/update",
                GSON.toJson(new NameRequest("C3 Parity Renamed"))).statusCode());

        LocalDateTime now = LocalDateTime.now(clock).withNano(0);
        long projectId = json(post(session, "/api/projects", GSON.toJson(new ProjectRequest(departmentId, "C3 Parity Project",
                now.plusDays(2).toString(), now.plusDays(4).toString())))).get("id").getAsLong();
        long reusableId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest("C3 Parity Reusable", "REUSABLE", 10)))).get("id").getAsLong();
        long consumableId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest("C3 Parity Consumable", "CONSUMABLE", 0)))).get("id").getAsLong();
        long requirementId = json(post(session, "/api/requirements", GSON.toJson(new RequirementRequest(projectId, reusableId, 20)))).get("id").getAsLong();
        long allocationId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(requirementId, 8,
                "Fictional Borrower", null, false)))).get("id").getAsLong();

        Fixture otherOwner = newTenant("C3-parity-other", Membership.Role.OWNER);
        Session otherSession = login(otherOwner.username, otherOwner.password);
        long otherDepartment = json(post(otherSession, "/api/departments", GSON.toJson(new NameRequest("Other tenant department")))).get("id").getAsLong();

        var allProjects = JsonParser.parseString(get(session.client, "/api/projects").body()).getAsJsonArray();
        assertTrue(allProjects.asList().stream().anyMatch(row -> row.getAsJsonObject().get("id").getAsLong() == projectId));
        var tenantDepartments = JsonParser.parseString(get(session.client, "/api/departments").body()).getAsJsonArray();
        assertFalse(tenantDepartments.asList().stream().anyMatch(row -> row.getAsJsonObject().get("id").getAsLong() == otherDepartment));
        var filteredResources = JsonParser.parseString(get(session.client, "/api/resources?type=REUSABLE").body()).getAsJsonArray();
        assertTrue(filteredResources.asList().stream().anyMatch(row -> row.getAsJsonObject().get("id").getAsLong() == reusableId));
        assertFalse(filteredResources.asList().stream().anyMatch(row -> row.getAsJsonObject().get("id").getAsLong() == consumableId));
        var allRequirements = JsonParser.parseString(get(session.client, "/api/requirements").body()).getAsJsonArray();
        assertTrue(allRequirements.asList().stream().anyMatch(row -> row.getAsJsonObject().get("id").getAsLong() == requirementId));
        var filteredAllocations = JsonParser.parseString(get(session.client,
                "/api/allocations?requirementId=" + requirementId + "&status=PENDING").body()).getAsJsonArray();
        assertEquals(1, filteredAllocations.size());
        assertEquals("PENDING", filteredAllocations.get(0).getAsJsonObject().get("status").getAsString());
        assertEquals(0, JsonParser.parseString(get(session.client,
                "/api/allocations?requirementId=" + requirementId + "&status=ISSUED").body()).getAsJsonArray().size());

        HttpResponse<String> projectRenamed = post(session, "/api/projects/" + projectId + "/update",
                GSON.toJson(new ProjectUpdateRequest("C3 Parity Renamed Project", now.plusDays(2).toString(), now.plusDays(4).toString())));
        assertEquals(200, projectRenamed.statusCode(), projectRenamed.body());
        assertEquals(departmentId, json(projectRenamed).get("departmentId").getAsLong());
        HttpResponse<String> projectBlocked = post(session, "/api/projects/" + projectId + "/delete", null);
        assertEquals(409, projectBlocked.statusCode(), projectBlocked.body());

        HttpResponse<String> updatedResource = post(session, "/api/resources/" + reusableId + "/update",
                GSON.toJson(new ResourceUpdateRequest("C3 Parity Reusable Updated", 12, 10)));
        assertEquals(200, updatedResource.statusCode(), updatedResource.body());
        assertEquals(12, json(updatedResource).get("totalQuantity").getAsInt());
        assertEquals(12, json(updatedResource).get("availableQuantity").getAsInt());
        HttpResponse<String> availability = get(session.client, "/api/resources/" + reusableId + "/availability");
        assertEquals(200, availability.statusCode(), availability.body());
        assertEquals(12, json(availability).get("availableQuantity").getAsInt());
        assertEquals(409, post(session, "/api/resources/" + reusableId + "/update",
                GSON.toJson(new ResourceUpdateRequest("C3 Parity Stale", 11, 10))).statusCode());
        assertEquals(409, post(session, "/api/resources/" + reusableId + "/delete", null).statusCode());

        assertEquals(200, post(session, "/api/requirements/" + requirementId + "/update",
                GSON.toJson(new RequirementUpdateRequest(25))).statusCode());
        assertEquals(409, post(session, "/api/requirements/" + requirementId + "/update",
                GSON.toJson(new RequirementUpdateRequest(7))).statusCode());
        assertEquals(409, post(session, "/api/requirements/" + requirementId + "/delete", null).statusCode());
        assertEquals(200, post(session, "/api/allocations/" + allocationId + "/delete", null).statusCode());
        assertEquals(200, post(session, "/api/requirements/" + requirementId + "/delete", null).statusCode());
        assertEquals(200, post(session, "/api/projects/" + projectId + "/delete", null).statusCode());
        assertEquals(200, post(session, "/api/departments/" + departmentId + "/delete", null).statusCode());
        assertEquals(200, post(session, "/api/departments/" + duplicateDepartmentId + "/delete", null).statusCode());
        assertEquals(200, post(session, "/api/resources/" + consumableId + "/delete", null).statusCode());
    }

    @Test @Order(30)
    void C3_PRIVACY01_contactIsEncryptedAndConsentRevocationErasesIt() throws Exception {
        Fixture owner = newTenant("C3-contact-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long departmentId = json(post(session, "/api/departments", GSON.toJson(new NameRequest("C3 Contact Department")))).get("id").getAsLong();
        LocalDateTime now = LocalDateTime.now(clock).withNano(0);
        long projectId = json(post(session, "/api/projects", GSON.toJson(new ProjectRequest(departmentId, "C3 Contact Project",
                now.plusDays(1).toString(), now.plusDays(3).toString())))).get("id").getAsLong();
        long resourceId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest("C3 Contact Resource", "REUSABLE", 5)))).get("id").getAsLong();
        long requirementId = json(post(session, "/api/requirements", GSON.toJson(new RequirementRequest(projectId, resourceId, 5)))).get("id").getAsLong();
        String fictionalPhone = "+15555550123";
        HttpResponse<String> created = post(session, "/api/allocations", GSON.toJson(new AllocationRequest(requirementId, 1,
                "Fictional Contact", fictionalPhone, true)));
        assertEquals(201, created.statusCode(), created.body());
        long allocationId = json(created).get("id").getAsLong();
        assertTrue(json(created).get("contactPresent").getAsBoolean());
        assertFalse(json(created).has("phoneNumber"));
        var stored = jdbc.queryForMap("SELECT BIN_TO_UUID(public_id,0) public_id,phone_ciphertext,phone_nonce,phone_key_id,sms_consent FROM allocation WHERE id=? AND organization_id=?",
                allocationId, owner.orgDbId);
        byte[] ciphertext = (byte[]) stored.get("phone_ciphertext");
        byte[] nonce = (byte[]) stored.get("phone_nonce");
        assertNotNull(ciphertext);
        assertNotNull(nonce);
        assertEquals("test-only-v1", stored.get("phone_key_id"));
        assertFalse(java.util.Arrays.equals(fictionalPhone.getBytes(StandardCharsets.UTF_8), ciphertext));
        assertEquals(fictionalPhone, contactCipher.decrypt(owner.orgDbId,
                UUID.fromString((String) stored.get("public_id")), ciphertext, nonce, (String) stored.get("phone_key_id")));
        int movementsBefore = jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=?", Integer.class, owner.orgDbId);
        HttpResponse<String> revoked = post(session, "/api/allocations/" + allocationId + "/revoke-sms-consent", null);
        assertEquals(200, revoked.statusCode(), revoked.body());
        assertFalse(json(revoked).get("contactPresent").getAsBoolean());
        assertFalse(json(revoked).get("smsConsent").getAsBoolean());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE id=? AND sms_consent=FALSE "
                + "AND phone_ciphertext IS NULL AND phone_nonce IS NULL AND phone_key_id IS NULL", Integer.class, allocationId));
        assertEquals(movementsBefore, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=?", Integer.class, owner.orgDbId));
    }

    @Test @Order(31)
    void C3_IMAGE01_boundedPngIsPersistedAndServedOnlyInsideItsTenant() throws Exception {
        Fixture owner = newTenant("C3-image-owner", Membership.Role.OWNER);
        Fixture other = newTenant("C3-image-other", Membership.Role.OWNER);
        Session ownerSession = login(owner.username, owner.password);
        Session otherSession = login(other.username, other.password);
        long resourceId = json(post(ownerSession, "/api/resources", GSON.toJson(new ResourceRequest("C3 Image Resource", "CONSUMABLE", 0)))).get("id").getAsLong();
        HttpResponse<String> missing = get(ownerSession.client, "/api/resources/" + resourceId + "/image");
        assertEquals(404, missing.statusCode());
        byte[] png = tinyPng();
        HttpResponse<String> uploaded = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image", "image/png", png);
        assertEquals(200, uploaded.statusCode(), uploaded.body());
        assertEquals("image/png", json(uploaded).get("contentType").getAsString());
        assertEquals(png.length, json(uploaded).get("sizeBytes").getAsInt());
        HttpResponse<byte[]> served = ownerSession.client.send(HttpRequest.newBuilder(URI.create(base() + "/api/resources/" + resourceId + "/image"))
                .GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, served.statusCode());
        assertArrayEquals(png, served.body());
        assertEquals("image/png", served.headers().firstValue("Content-Type").orElse("").split(";")[0]);
        assertEquals("nosniff", served.headers().firstValue("X-Content-Type-Options").orElse(""));
        assertTrue(served.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        assertArrayEquals(png, jdbc.queryForObject("SELECT image_bytes FROM resource WHERE id=? AND organization_id=?",
                byte[].class, resourceId, owner.orgDbId));
        assertEquals(404, get(otherSession.client, "/api/resources/" + resourceId + "/image").statusCode());
        HttpResponse<String> wrongType = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image", "image/jpeg", png);
        assertEquals(400, wrongType.statusCode(), wrongType.body());
        byte[] oversized = new byte[ResourceImageSupport.MAX_BYTES + 1];
        HttpResponse<String> tooLarge = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image", "image/png", oversized);
        assertEquals(413, tooLarge.statusCode(), tooLarge.body());
        HttpResponse<String> svg = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".getBytes(StandardCharsets.UTF_8));
        assertEquals(400, svg.statusCode(), svg.body());
        assertEquals("INVALID_IMAGE", json(svg).get("code").getAsString());

        HttpResponse<String> noCsrf = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image",
                "image/png", png, false);
        assertEquals(403, noCsrf.statusCode(), "Image write must enforce CSRF; response=" + noCsrf.body());
        HttpResponse<String> malformed = malformedMultipartPost(ownerSession, "/api/resources/" + resourceId + "/image");
        assertTrue(malformed.statusCode() == 400 || malformed.statusCode() == 415,
                "Malformed multipart input must be rejected as a controlled client error: " + malformed.body());

        byte[] corrupt = png.clone();
        corrupt[41] ^= 0x01;
        HttpResponse<String> corrupted = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image", "image/png", corrupt);
        assertEquals(400, corrupted.statusCode(), corrupted.body());
        byte[] oversizedDimensions = pngWithOversizedDimensions(png, 5000, 5000);
        HttpResponse<String> dimensions = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image",
                "image/png", oversizedDimensions);
        assertEquals(400, dimensions.statusCode(), dimensions.body());

        byte[] jpeg = noisyJpeg();
        assertTrue(jpeg.length > 70 * 1024 && jpeg.length <= ResourceImageSupport.MAX_BYTES,
                "The valid JPEG fixture must exercise a nontrivial image larger than 70 KiB.");
        HttpResponse<String> jpegUpload = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image", "image/jpeg", jpeg);
        assertEquals(200, jpegUpload.statusCode(), jpegUpload.body());
        HttpResponse<byte[]> jpegServed = ownerSession.client.send(HttpRequest.newBuilder(URI.create(
                base() + "/api/resources/" + resourceId + "/image")).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, jpegServed.statusCode());
        assertEquals("image/jpeg", jpegServed.headers().firstValue("Content-Type").orElse("").split(";")[0]);
        assertArrayEquals(jpeg, jpegServed.body(), "JPEG bytes must be stored and served without transformation.");

        byte[] maxJpeg = padJpegToSize(jpeg, ResourceImageSupport.MAX_BYTES);
        assertEquals(ResourceImageSupport.MAX_BYTES, maxJpeg.length);
        HttpResponse<String> maximum = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image", "image/jpeg", maxJpeg);
        assertEquals(200, maximum.statusCode(), maximum.body());
        HttpResponse<String> overMaximum = multipartPost(ownerSession, "/api/resources/" + resourceId + "/image",
                "image/jpeg", Arrays.copyOf(maxJpeg, maxJpeg.length + 1));
        assertEquals(413, overMaximum.statusCode(), overMaximum.body());
    }

    @Test @Order(32)
    void C3_LIFECYCLE01_scheduledExpiryAndLoopbackReminderProtocolsAreOneShot() throws Exception {
        Fixture owner = newTenant("C3-lifecycle-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long departmentId = json(post(session, "/api/departments", GSON.toJson(new NameRequest("C3 Lifecycle Department")))).get("id").getAsLong();
        LocalDateTime now = LocalDateTime.now(clock).withNano(0);
        long reminderProjectId = json(post(session, "/api/projects", GSON.toJson(new ProjectRequest(departmentId, "C3 Reminder Project",
                now.plusHours(1).toString(), now.plusHours(4).toString())))).get("id").getAsLong();
        long reusableId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest("C3 Reminder Reusable", "REUSABLE", 2)))).get("id").getAsLong();
        long reminderRequirementId = json(post(session, "/api/requirements", GSON.toJson(new RequirementRequest(reminderProjectId, reusableId, 2)))).get("id").getAsLong();
        long fakeAllocationId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(reminderRequirementId, 1,
                "Fictional Reminder One", "+15555550111", true)))).get("id").getAsLong();
        long twilioAllocationId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(reminderRequirementId, 1,
                "Fictional Reminder Two", "+15555550112", true)))).get("id").getAsLong();
        jdbc.update("UPDATE project SET start_at=?,end_at=? WHERE id=? AND organization_id=?",
                Timestamp.valueOf(now.minusHours(2)), Timestamp.valueOf(now.plusHours(3)), reminderProjectId, owner.orgDbId);
        for (long allocationId : List.of(fakeAllocationId, twilioAllocationId)) {
            assertEquals(200, post(session, "/api/allocations/" + allocationId + "/approve", null).statusCode());
            assertEquals(200, post(session, "/api/allocations/" + allocationId + "/issue", null).statusCode());
        }

        String accountSid = "AC00000000000000000000000000000000";
        String authToken = "c3-local-protocol-only-token";
        String providerSid = "SM00000000000000000000000000000000";
        String expectedAuthorization = "Basic " + Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        AtomicInteger fakeCalls = new AtomicInteger();
        AtomicReference<String> fakeRequest = new AtomicReference<>();
        AtomicReference<String> twilioAuthorization = new AtomicReference<>();
        AtomicReference<String> twilioContentType = new AtomicReference<>();
        AtomicReference<String> twilioRequest = new AtomicReference<>();
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/fake/messages", exchange -> {
            fakeCalls.incrementAndGet();
            fakeRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = "{\"id\":\"fake-local-1\",\"status\":\"accepted\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/2010-04-01/Accounts/" + accountSid + "/Messages.json", exchange -> {
            twilioAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            twilioContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            twilioRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = GSON.toJson(java.util.Map.of("sid", providerSid, "status", "queued")).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String loopbackBase = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            C3ReminderTransport fakeTransport = new C3ReminderTransport(loopbackBase, accountSid, authToken, "+15555550123");
            C3ReminderTransport twilioTransport = new C3ReminderTransport(loopbackBase, accountSid, authToken, "+15555550123");
            reminderDispatcher.dispatchOne(fakeAllocationId, now, ReminderMode.FAKE_TWILIO, fakeTransport);
            reminderDispatcher.dispatchOne(twilioAllocationId, now, ReminderMode.TWILIO_VIRTUAL, twilioTransport);
            assertEquals(1, fakeCalls.get());
            assertTrue(fakeRequest.get().contains("+15555550111"));
            assertEquals(expectedAuthorization, twilioAuthorization.get());
            assertTrue(twilioContentType.get().startsWith("application/x-www-form-urlencoded"));
            assertTrue(twilioRequest.get().contains("To=%2B15555550112"));
            assertTrue(twilioRequest.get().contains("From=%2B15555550123"));
            String smsBody = Arrays.stream(twilioRequest.get().split("&"))
                    .filter(field -> field.startsWith("Body="))
                    .map(field -> java.net.URLDecoder.decode(field.substring(5), StandardCharsets.UTF_8))
                    .findFirst().orElseThrow();
            assertTrue(smsBody.startsWith("OrgResource: 1 × C3 Reminder Reusable due "));
            assertTrue(smsBody.endsWith(" PHT. Coordinate return with your organization."));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE id=? AND reminder_outcome='SIMULATED' AND reminder_http_status=200",
                    Integer.class, fakeAllocationId));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE id=? AND reminder_outcome='PROVIDER_ACCEPTED' "
                    + "AND reminder_http_status=201 AND provider_message_sid=?", Integer.class, twilioAllocationId, providerSid));
            reminderDispatcher.dispatchOne(fakeAllocationId, now, ReminderMode.FAKE_TWILIO, fakeTransport);
            assertEquals(1, fakeCalls.get(), "A committed reminder outcome must never be sent a second time.");
        } finally {
            server.stop(0);
        }

        long expiryProjectId = json(post(session, "/api/projects", GSON.toJson(new ProjectRequest(departmentId, "C3 Expiry Project",
                now.plusDays(1).toString(), now.plusDays(2).toString())))).get("id").getAsLong();
        long expiryResourceId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest("C3 Expiry Resource", "REUSABLE", 5)))).get("id").getAsLong();
        long expiryRequirementId = json(post(session, "/api/requirements", GSON.toJson(new RequirementRequest(expiryProjectId, expiryResourceId, 2)))).get("id").getAsLong();
        long pendingId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(expiryRequirementId, 1,
                "Fictional Expiry Pending", null, false)))).get("id").getAsLong();
        long approvedId = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(expiryRequirementId, 1,
                "Fictional Expiry Approved", null, false)))).get("id").getAsLong();
        jdbc.update("UPDATE project SET start_at=?,end_at=? WHERE id=? AND organization_id=?",
                Timestamp.valueOf(now.minusHours(1)), Timestamp.valueOf(now.plusHours(1)), expiryProjectId, owner.orgDbId);
        assertEquals(200, post(session, "/api/allocations/" + approvedId + "/approve", null).statusCode());
        int movementCount = jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, expiryResourceId);
        jdbc.update("UPDATE project SET end_at=? WHERE id=? AND organization_id=?",
                Timestamp.valueOf(now.minusSeconds(1)), expiryProjectId, owner.orgDbId);
        invokeRegisteredLifecycleSchedule();
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE organization_id=? AND id IN (?,?) "
                + "AND status='CANCELLED' AND cancellation_reason='EXPIRED'", Integer.class,
                owner.orgDbId, pendingId, approvedId));
        assertEquals(movementCount, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, expiryResourceId), "Expiry releases computed holds without inserting stock movements.");
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE id IN (?,?) AND reminder_outcome='NONE'",
                Integer.class, pendingId, approvedId));
    }

    @Test @Order(33)
    void C3_SECURITY02_everyLegacyOperationIsTenantScopedAndMemberWritesAreForbidden() throws Exception {
        Fixture tenantA = newTenant("C3-sec-a", Membership.Role.OWNER);
        Session sessionA = login(tenantA.username, tenantA.password);
        Fixture tenantB = newTenant("C3-sec-b", Membership.Role.OWNER);
        Session ownerB = login(tenantB.username, tenantB.password);
        LocalDateTime start = LocalDateTime.now(clock).withNano(0).plusDays(2);

        long departmentA = json(post(sessionA, "/api/departments", GSON.toJson(new NameRequest("C3 Security A Department")))).get("id").getAsLong();
        long projectA = json(post(sessionA, "/api/projects", GSON.toJson(new ProjectRequest(departmentA, "C3 Security A Project",
                start.toString(), start.plusDays(2).toString())))).get("id").getAsLong();
        long resourceA = json(post(sessionA, "/api/resources", GSON.toJson(new ResourceRequest("C3 Security A Resource", "REUSABLE", 10)))).get("id").getAsLong();
        long requirementA = json(post(sessionA, "/api/requirements", GSON.toJson(new RequirementRequest(projectA, resourceA, 8)))).get("id").getAsLong();
        long allocationA = json(post(sessionA, "/api/allocations", GSON.toJson(new AllocationRequest(requirementA, 1,
                "Fictional Security A Borrower", null, false)))).get("id").getAsLong();
        assertEquals(200, multipartPost(sessionA, "/api/resources/" + resourceA + "/image", "image/png", tinyPng()).statusCode());

        long departmentB = json(post(ownerB, "/api/departments", GSON.toJson(new NameRequest("C3 Security B Department")))).get("id").getAsLong();
        long projectB = json(post(ownerB, "/api/projects", GSON.toJson(new ProjectRequest(departmentB, "C3 Security B Project",
                start.toString(), start.plusDays(2).toString())))).get("id").getAsLong();
        long resourceB = json(post(ownerB, "/api/resources", GSON.toJson(new ResourceRequest("C3 Security B Resource", "REUSABLE", 10)))).get("id").getAsLong();
        long requirementB = json(post(ownerB, "/api/requirements", GSON.toJson(new RequirementRequest(projectB, resourceB, 8)))).get("id").getAsLong();
        long allocationB = json(post(ownerB, "/api/allocations", GSON.toJson(new AllocationRequest(requirementB, 1,
                "Fictional Security B Borrower", null, false)))).get("id").getAsLong();

        HttpResponse<String> createDepartmentB = post(ownerB, "/api/departments", GSON.toJson(new NameRequest("C3 Security B Create")));
        assertEquals(201, createDepartmentB.statusCode(), createDepartmentB.body());
        long createdDepartmentB = json(createDepartmentB).get("id").getAsLong();
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM department WHERE id=? AND organization_id=?",
                Integer.class, createdDepartmentB, tenantB.orgDbId), "D01 creation must persist only in the active tenant.");
        HttpResponse<String> createResourceB = post(ownerB, "/api/resources", GSON.toJson(new ResourceRequest("C3 Security B Create", "CONSUMABLE", 0)));
        assertEquals(201, createResourceB.statusCode(), createResourceB.body());
        long createdResourceB = json(createResourceB).get("id").getAsLong();
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE id=? AND organization_id=?",
                Integer.class, createdResourceB, tenantB.orgDbId), "R01 creation must persist only in the active tenant.");

        assertLegacyListOmits(get(ownerB.client, "/api/departments"), departmentA);
        assertLegacyListOmits(get(ownerB.client, "/api/projects"), projectA);
        assertLegacyListOmits(get(ownerB.client, "/api/resources?type=REUSABLE"), resourceA);
        assertLegacyListOmits(get(ownerB.client, "/api/requirements"), requirementA);
        assertLegacyListOmits(get(ownerB.client, "/api/allocations"), allocationA);

        List<String> foreignNames = List.of("D03", "D04", "P01", "P03", "P04", "R03", "R04", "R05",
                "Q01", "Q03", "Q04", "A01", "A03", "A04", "A05", "A06", "A07", "A08", "A09", "IMAGE-01", "IMAGE-02");
        List<HttpResponse<String>> foreignResponses = List.of(
                post(ownerB, "/api/departments/" + departmentA + "/update", GSON.toJson(new NameRequest("cross tenant"))),
                post(ownerB, "/api/departments/" + departmentA + "/delete", null),
                post(ownerB, "/api/projects", GSON.toJson(new ProjectRequest(departmentA, "C3 Foreign Project", start.toString(), start.plusDays(2).toString()))),
                post(ownerB, "/api/projects/" + projectA + "/update", GSON.toJson(new ProjectUpdateRequest("C3 Foreign Rename", start.toString(), start.plusDays(2).toString()))),
                post(ownerB, "/api/projects/" + projectA + "/delete", null),
                post(ownerB, "/api/resources/" + resourceA + "/update", GSON.toJson(new ResourceUpdateRequest("C3 Foreign Rename", 11, 10))),
                post(ownerB, "/api/resources/" + resourceA + "/delete", null),
                get(ownerB.client, "/api/resources/" + resourceA + "/availability"),
                post(ownerB, "/api/requirements", GSON.toJson(new RequirementRequest(projectA, resourceA, 1))),
                post(ownerB, "/api/requirements/" + requirementA + "/update", GSON.toJson(new RequirementUpdateRequest(2))),
                post(ownerB, "/api/requirements/" + requirementA + "/delete", null),
                post(ownerB, "/api/allocations", GSON.toJson(new AllocationRequest(requirementA, 1, "C3 Foreign Borrower", null, false))),
                post(ownerB, "/api/allocations/" + allocationA + "/approve", null),
                post(ownerB, "/api/allocations/" + allocationA + "/issue", null),
                post(ownerB, "/api/allocations/" + allocationA + "/return", null),
                post(ownerB, "/api/allocations/" + allocationA + "/consume", null),
                post(ownerB, "/api/allocations/" + allocationA + "/cancel", null),
                post(ownerB, "/api/allocations/" + allocationA + "/delete", null),
                post(ownerB, "/api/allocations/" + allocationA + "/revoke-sms-consent", null),
                multipartPost(ownerB, "/api/resources/" + resourceA + "/image", "image/png", tinyPng()),
                get(ownerB.client, "/api/resources/" + resourceA + "/image"));
        for (int i = 0; i < foreignResponses.size(); i++)
            assertEquals(404, foreignResponses.get(i).statusCode(), foreignNames.get(i) + " must conceal a foreign tenant record: " + foreignResponses.get(i).body());

        Fixture memberFixture = fixtureInOrg(tenantB.orgDbId, Membership.Role.MEMBER);
        Session member = login(memberFixture.username, memberFixture.password);
        List<String> deniedNames = List.of("D01", "D03", "D04", "P01", "P03", "P04", "R01", "R03", "R04", "IMAGE-01",
                "Q01", "Q03", "Q04", "A01", "A03", "A04", "A05", "A06", "A07", "A08", "A09");
        List<HttpResponse<String>> deniedResponses = List.of(
                post(member, "/api/departments", GSON.toJson(new NameRequest("C3 Member Denied Department"))),
                post(member, "/api/departments/" + departmentB + "/update", GSON.toJson(new NameRequest("C3 Member Rename"))),
                post(member, "/api/departments/" + departmentB + "/delete", null),
                post(member, "/api/projects", GSON.toJson(new ProjectRequest(departmentB, "C3 Member Denied Project", start.toString(), start.plusDays(2).toString()))),
                post(member, "/api/projects/" + projectB + "/update", GSON.toJson(new ProjectUpdateRequest("C3 Member Rename", start.toString(), start.plusDays(2).toString()))),
                post(member, "/api/projects/" + projectB + "/delete", null),
                post(member, "/api/resources", GSON.toJson(new ResourceRequest("C3 Member Denied Resource", "CONSUMABLE", 0))),
                post(member, "/api/resources/" + resourceB + "/update", GSON.toJson(new ResourceUpdateRequest("C3 Member Rename", 11, 10))),
                post(member, "/api/resources/" + resourceB + "/delete", null),
                multipartPost(member, "/api/resources/" + resourceB + "/image", "image/png", tinyPng()),
                post(member, "/api/requirements", GSON.toJson(new RequirementRequest(projectB, resourceB, 1))),
                post(member, "/api/requirements/" + requirementB + "/update", GSON.toJson(new RequirementUpdateRequest(2))),
                post(member, "/api/requirements/" + requirementB + "/delete", null),
                post(member, "/api/allocations", GSON.toJson(new AllocationRequest(requirementB, 1, "C3 Member Denied", null, false))),
                post(member, "/api/allocations/" + allocationB + "/approve", null),
                post(member, "/api/allocations/" + allocationB + "/issue", null),
                post(member, "/api/allocations/" + allocationB + "/return", null),
                post(member, "/api/allocations/" + allocationB + "/consume", null),
                post(member, "/api/allocations/" + allocationB + "/cancel", null),
                post(member, "/api/allocations/" + allocationB + "/delete", null),
                post(member, "/api/allocations/" + allocationB + "/revoke-sms-consent", null));
        assertEquals(deniedNames.size(), deniedResponses.size());
        for (int i = 0; i < deniedResponses.size(); i++)
            assertEquals(403, deniedResponses.get(i).statusCode(), deniedNames.get(i) + " must reject MEMBER writes: " + deniedResponses.get(i).body());
    }

    @Test @Order(34)
    void C3_RECEIPT01_boundedPrunerDeletesExpiredHttpReceiptButKeepsPhysicalMovementReplayable() throws Exception {
        Fixture owner = newTenant("C3-receipt-prune", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long resourceId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest("C3 Receipt Prune Stock", "CONSUMABLE", 0))))
                .get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        String resourcePath = "/api/v2/orgs/" + owner.orgPublicId + "/resources/" + resource.publicId;
        HttpResponse<String> state = get(session.client, resourcePath);
        assertEquals(200, state.statusCode(), state.body());
        UUID operation = UUID.randomUUID();
        String key = C1Secrets.bearerToken();
        String body = GSON.toJson(new ReceiveRequest(operation, 2));
        HttpResponse<String> receive = modernPost(session, resourcePath + "/receive", body,
                state.headers().firstValue("ETag").orElseThrow(), key);
        assertEquals(201, receive.statusCode(), receive.body());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, owner.orgDbId, operation.toString()));

        LocalDateTime utcNow = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        jdbc.update("UPDATE idempotency_key SET created_at=?,expires_at=? WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Timestamp.valueOf(utcNow.minusHours(25)), Timestamp.valueOf(utcNow.minusHours(1)), owner.orgDbId, operation.toString());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, owner.orgDbId, operation.toString()));

        invokeRegisteredLifecycleSchedule();
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, owner.orgDbId, operation.toString()), "Cleanup removes only expired completed HTTP receipts.");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, owner.orgDbId, operation.toString()), "Permanent stock movement must remain after HTTP receipt pruning.");

        HttpResponse<String> replay = modernPost(session, resourcePath + "/receive", body, "\"v0\"", key);
        assertEquals(200, replay.statusCode(), replay.body());
        assertEquals("RECEIPT", json(replay).getAsJsonObject("data").get("kind").getAsString());
        assertEquals(2, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, owner.orgDbId, operation.toString()));
    }

    @Test @Order(36)
    void C3_SESSION01_organizationSwitchRotatesSessionAndCsrfWithoutExtendingLifetimeOrReauth() throws Exception {
        Fixture home = newTenant("C3-session-home", Membership.Role.OWNER);
        Fixture second = newTenant("C3-session-second", Membership.Role.OWNER);
        Fixture foreign = newTenant("C3-session-foreign", Membership.Role.OWNER);
        addMembership(second.orgDbId, home.userDbId, Membership.Role.MEMBER);

        Session session = login(home.username, home.password);
        String originalSessionId = cookie(session.client);
        String originalCsrf = session.csrf;
        var stored = springSessions.findById(originalSessionId);
        assertNotNull(stored, "The authenticated session must be stored in MySQL.");
        long agedTimestamp = clock.millis() - java.time.Duration.ofHours(12).toMillis() + 60_000;
        stored.setAttribute(edu.orgresource.extended.security.C1AccountSessionFilter.SESSION_CREATED_AT, agedTimestamp);
        stored.setAttribute(edu.orgresource.extended.security.C1AccountSessionFilter.LAST_REAUTH_AT, agedTimestamp);
        springSessions.save(stored);

        HttpResponse<String> switched = post(session, "/api/v2/session/active-org", GSON.toJson(new OrgRequest(second.orgPublicId)));
        assertEquals(200, switched.statusCode(), switched.body());
        assertEquals(second.orgPublicId.toString(), json(switched).getAsJsonObject("data").get("activeOrgId").getAsString());
        String rotatedSessionId = cookie(session.client);
        assertNotEquals(originalSessionId, rotatedSessionId, "Organization selection must rotate the session identifier.");
        assertNull(springSessions.findById(originalSessionId), "The pre-rotation session identifier must no longer authenticate.");
        var afterSwitch = springSessions.findById(rotatedSessionId);
        assertNotNull(afterSwitch, "The rotated session must be persisted in MySQL.");
        assertEquals(agedTimestamp, ((Number) afterSwitch.getAttribute(edu.orgresource.extended.security.C1AccountSessionFilter.SESSION_CREATED_AT)).longValue());
        assertEquals(agedTimestamp, ((Number) afterSwitch.getAttribute(edu.orgresource.extended.security.C1AccountSessionFilter.LAST_REAUTH_AT)).longValue(),
                "Organization switching must not count as password reauthentication.");

        String refreshedCsrf = json(get(session.client, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        assertNotEquals(originalCsrf, refreshedCsrf, "Session rotation must also rotate CSRF.");
        HttpResponse<String> staleCsrf = post(session.client, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(home.orgPublicId)), originalCsrf, origin());
        assertEquals(403, staleCsrf.statusCode(), "The pre-switch CSRF token must no longer authorize writes.");

        session.csrf = refreshedCsrf;
        assertEquals(200, get(session.client, "/api/departments").statusCode(), "An active member can read the selected tenant.");
        assertEquals(403, post(session, "/api/departments", GSON.toJson(new NameRequest("C3 member write denied"))).statusCode(),
                "The selected tenant's MEMBER role must remain read-only.");
        HttpResponse<String> foreignSwitch = post(session, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(foreign.orgPublicId)));
        assertEquals(404, foreignSwitch.statusCode(), "A valid session cannot select an organization without membership.");

        var expiring = springSessions.findById(rotatedSessionId);
        assertNotNull(expiring);
        expiring.setAttribute(edu.orgresource.extended.security.C1AccountSessionFilter.SESSION_CREATED_AT,
                clock.millis() - java.time.Duration.ofHours(12).toMillis());
        springSessions.save(expiring);
        HttpResponse<String> expired = get(session.client, "/api/v2/session");
        assertEquals(401, expired.statusCode(), "Switching organizations must not renew the 12-hour absolute lifetime.");
        assertNull(springSessions.findById(rotatedSessionId), "An expired session must be removed from the persistent repository.");
    }

    @Test @Order(37)
    void C3_MIGRATION01_v4RefusesLegacyPlaintextContactWithoutChangingItsSchemaOrRows() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String schema = "orgresource_c3_contact_" + suffix;
        assertTrue(schema.matches("orgresource_c3_contact_[a-f0-9]{16}"));
        String migrationUrl = System.getenv("EXT_DB_MIGRATION_URL");
        String migrationUser = System.getenv("EXT_DB_MIGRATION_USER");
        String migrationPassword = System.getenv("EXT_DB_MIGRATION_PASSWORD");
        String currentSchema;
        try (Connection connection = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword)) {
            currentSchema = connection.getCatalog();
        }
        String legacyUrl = migrationUrl.replace("/" + currentSchema, "/" + schema);
        boolean created = false;
        try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
             var statement = migration.createStatement()) {
            statement.execute("CREATE DATABASE `" + schema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            created = true;
            org.flywaydb.core.Flyway.configure().dataSource(legacyUrl, migrationUser, migrationPassword)
                    .target(org.flywaydb.core.api.MigrationVersion.fromVersion("3")).load().migrate();
            long organizationId;
            long departmentId;
            long resourceId;
            long projectId;
            long requirementId;
            try (Connection legacy = DriverManager.getConnection(legacyUrl, migrationUser, migrationPassword);
                 var seed = legacy.createStatement()) {
                seed.executeUpdate("INSERT INTO organization(public_id,slug,display_name,status,created_at,updated_at) "
                        + "VALUES(UUID_TO_BIN(UUID(),0),'" + "legacy-" + suffix + "','Fictional legacy contact','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))");
                organizationId = scalarLong(seed, "SELECT id FROM organization WHERE slug='legacy-" + suffix + "'");
                seed.executeUpdate("INSERT INTO department(organization_id,name,name_key) VALUES(" + organizationId + ",'Legacy Department','legacy department')");
                departmentId = scalarLong(seed, "SELECT id FROM department WHERE organization_id=" + organizationId);
                seed.executeUpdate("INSERT INTO project(organization_id,department_id,name,start_at,end_at) VALUES(" + organizationId
                        + "," + departmentId + ",'Legacy Project','2040-01-01 00:00:00.000000','2040-01-01 01:00:00.000000')");
                projectId = scalarLong(seed, "SELECT id FROM project WHERE organization_id=" + organizationId);
                seed.executeUpdate("INSERT INTO resource(public_id,organization_id,name,name_key,type,total_quantity) VALUES(UUID_TO_BIN(UUID(),0),"
                        + organizationId + ",'Legacy Resource','legacy resource','REUSABLE',0)");
                resourceId = scalarLong(seed, "SELECT id FROM resource WHERE organization_id=" + organizationId);
                seed.executeUpdate("INSERT INTO resource_requirement(organization_id,project_id,resource_id,quantity) VALUES("
                        + organizationId + "," + projectId + "," + resourceId + ",1)");
                requirementId = scalarLong(seed, "SELECT id FROM resource_requirement WHERE organization_id=" + organizationId);
                seed.executeUpdate("INSERT INTO allocation(public_id,organization_id,requirement_id,resource_id,quantity,borrower_name,phone_number,sms_consent,status,created_at) "
                        + "VALUES(UUID_TO_BIN(UUID(),0)," + organizationId + "," + requirementId + "," + resourceId
                        + ",1,'Fictional Legacy Borrower','+15555550199',TRUE,'PENDING',UTC_TIMESTAMP(6))");
            }
            RuntimeException failed = assertThrows(RuntimeException.class, () -> org.flywaydb.core.Flyway.configure()
                    .dataSource(legacyUrl, migrationUser, migrationPassword).load().migrate());
            StringBuilder detail = new StringBuilder();
            for (Throwable cause = failed; cause != null; cause = cause.getCause()) detail.append(cause).append('\n');
            assertTrue(detail.toString().contains("LEGACY_CONTACT_DATA_REQUIRES_EXPLICIT_SECURE_MIGRATION"), detail.toString());
            try (Connection legacy = DriverManager.getConnection(legacyUrl, migrationUser, migrationPassword);
                 var query = legacy.createStatement()) {
                assertEquals(1, scalarLong(query, "SELECT COUNT(*) FROM allocation WHERE phone_number='+15555550199' AND sms_consent=TRUE"));
                assertEquals(0, scalarLong(query, "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() "
                        + "AND table_name='allocation' AND column_name='phone_ciphertext'"));
                assertEquals(1, scalarLong(query, "SELECT COUNT(*) FROM flyway_schema_history WHERE version='3' AND success=1"));
                assertEquals(0, scalarLong(query, "SELECT COUNT(*) FROM flyway_schema_history WHERE version='4' AND success=1"));
            }
        } finally {
            if (created) try (Connection migration = DriverManager.getConnection(migrationUrl, migrationUser, migrationPassword);
                    var statement = migration.createStatement()) { statement.execute("DROP DATABASE `" + schema + "`"); }
        }
    }

    @Test @Order(38)
    void C3_INPUT03_missingAndInvalidInventoryPreconditionsAreControlledAndAtomic() throws Exception {
        Fixture owner = newTenant("C3-header-owner", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long resourceId = json(post(session, "/api/resources",
                GSON.toJson(new ResourceRequest("C3 Header Resource", "CONSUMABLE", 5)))).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        String path = "/api/v2/orgs/" + owner.orgPublicId + "/resources/" + resource.publicId + "/receive";

        HttpResponse<String> missingKey = modernPost(session, path,
                GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)), "\"v0\"", null);
        assertEquals(400, missingKey.statusCode(), missingKey.body());
        assertEquals("IDEMPOTENCY_KEY_REQUIRED", json(missingKey).get("code").getAsString());

        HttpResponse<String> missingVersion = modernPost(session, path,
                GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)), null, C1Secrets.bearerToken());
        assertEquals(428, missingVersion.statusCode(), missingVersion.body());
        assertEquals("PRECONDITION_REQUIRED", json(missingVersion).get("code").getAsString());

        HttpResponse<String> malformedVersion = modernPost(session, path,
                GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)), "W/\"v0\"", C1Secrets.bearerToken());
        assertEquals(400, malformedVersion.statusCode(), malformedVersion.body());
        assertEquals("INVALID_ETAG", json(malformedVersion).get("code").getAsString());
        assertEquals(5, resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow().totalQuantity);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND resource_id=?",
                Integer.class, owner.orgDbId, resourceId), "Failed requests must not append a movement or change physical stock.");
    }

    @Test @Order(39)
    void C4_A1_zeroStockResourceWithImageCannotBeDeletedButEmptyUnpicturedResourceCan() throws Exception {
        Fixture owner = newTenant("C4-image-delete", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long picturedId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest(
                "C4 Pictured Empty Resource", "CONSUMABLE", 0)))).get("id").getAsLong();
        byte[] png = tinyPng();
        assertEquals(200, multipartPost(session, "/api/resources/" + picturedId + "/image", "image/png", png).statusCode());

        HttpResponse<String> refused = post(session, "/api/resources/" + picturedId + "/delete", null);
        assertEquals(409, refused.statusCode(), refused.body());
        assertEquals("RESOURCE_IMAGE_EXISTS", json(refused).get("code").getAsString());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE id=? AND organization_id=? AND total_quantity=0",
                Integer.class, picturedId, owner.orgDbId));
        HttpResponse<byte[]> retained = session.client.send(HttpRequest.newBuilder(URI.create(
                base() + "/api/resources/" + picturedId + "/image")).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, retained.statusCode());
        assertArrayEquals(png, retained.body(), "Refused deletion must preserve the exact image bytes.");

        Resource pictured = resourceRepository.findByIdAndOrganizationId(picturedId, owner.orgDbId).orElseThrow();
        String statePath = "/api/v2/orgs/" + owner.orgPublicId + "/resources/" + pictured.publicId;
        String removePath = statePath + "/image/delete";
        String etag = get(session.client, statePath).headers().firstValue("ETag").orElseThrow();
        Fixture memberFixture = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Session member = login(memberFixture.username, memberFixture.password);
        Fixture foreign = newTenant("C5-image-foreign", Membership.Role.OWNER);
        assertEquals(403, post(member, removePath, null, etag).statusCode());
        assertEquals(404, post(login(foreign.username, foreign.password), removePath, null, etag).statusCode());
        assertEquals(403, post(session, removePath, null, "", origin()).statusCode());
        assertEquals(412, post(session, removePath, null, "\"v999999\"").statusCode());
        assertEquals(428, post(session, removePath, null).statusCode());
        assertEquals(200, get(session.client, "/api/resources/" + picturedId + "/image").statusCode());
        HttpResponse<String> removed = post(session, removePath, null, etag);
        assertEquals(200, removed.statusCode(), removed.body());
        String currentEtag = removed.headers().firstValue("ETag").orElseThrow();
        assertNotEquals(etag, currentEtag);
        assertEquals(0, json(removed).getAsJsonObject("data").get("totalQuantity").getAsInt());
        assertEquals(404, get(session.client, "/api/resources/" + picturedId + "/image").statusCode());
        assertEquals(404, post(session, removePath, null, currentEtag).statusCode());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE resource_id=?", Integer.class, picturedId));
        assertEquals(200, post(session, "/api/resources/" + picturedId + "/delete", null).statusCode());

        long emptyId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest(
                "C4 Deletable Empty Resource", "CONSUMABLE", 0)))).get("id").getAsLong();
        HttpResponse<String> deleted = post(session, "/api/resources/" + emptyId + "/delete", null);
        assertEquals(200, deleted.statusCode(), deleted.body());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE id=? AND organization_id=?",
                Integer.class, emptyId, owner.orgDbId));
    }

    @Test @Order(40)
    void C4_A2_operationReceiptsRequireCurrentStockRoleEvenForOriginalActor() throws Exception {
        Fixture owner = newTenant("C4-receipt-owner", Membership.Role.OWNER);
        Fixture actor = fixtureInOrg(owner.orgDbId, Membership.Role.CUSTODIAN);
        Fixture custodian = fixtureInOrg(owner.orgDbId, Membership.Role.CUSTODIAN);
        Fixture foreign = newTenant("C4-receipt-foreign", Membership.Role.OWNER);
        Session ownerSession = login(owner.username, owner.password);
        Session actorSession = login(actor.username, actor.password);
        Session custodianSession = login(custodian.username, custodian.password);
        Session foreignSession = login(foreign.username, foreign.password);

        long resourceId = json(post(ownerSession, "/api/resources", GSON.toJson(new ResourceRequest(
                "C4 Receipt Resource", "CONSUMABLE", 0)))).get("id").getAsLong();
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        String statePath = root + "/resources/" + resource.publicId;
        HttpResponse<String> state = get(actorSession.client, statePath);
        UUID operationId = UUID.randomUUID();
        String body = GSON.toJson(new ReceiveRequest(operationId, 1));
        HttpResponse<String> committed = modernPost(actorSession, statePath + "/receive", body,
                state.headers().firstValue("ETag").orElseThrow(), C1Secrets.bearerToken());
        assertEquals(201, committed.statusCode(), committed.body());

        String receiptPath = root + "/operations/" + operationId;
        assertEquals(200, get(custodianSession.client, receiptPath).statusCode(),
                "A separate active CUSTODIAN may inspect organization stock activity.");
        assertEquals(404, get(foreignSession.client, receiptPath).statusCode());
        assertEquals(401, get(newClient(), receiptPath).statusCode());

        jdbc.update("UPDATE membership SET role='MEMBER',version=version+1,updated_at=? WHERE id=? AND org_id=?",
                Timestamp.valueOf(now()), actor.membershipDbId, owner.orgDbId);
        HttpResponse<String> demotedReceipt = get(actorSession.client, receiptPath);
        assertEquals(403, demotedReceipt.statusCode(), demotedReceipt.body());
        assertEquals("FORBIDDEN", json(demotedReceipt).get("code").getAsString());

        LocalDateTime expired = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).minusHours(1);
        jdbc.update("UPDATE idempotency_key SET created_at=?,expires_at=? WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Timestamp.valueOf(expired.minusHours(24)), Timestamp.valueOf(expired), owner.orgDbId, operationId.toString());
        invokeRegisteredLifecycleSchedule();
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, owner.orgDbId, operationId.toString()));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=? AND operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, owner.orgDbId, operationId.toString()), "Pruning the HTTP receipt must retain permanent movement history.");
        assertEquals(403, get(actorSession.client, receiptPath).statusCode(),
                "Receipt ownership must not restore a role after the temporary HTTP receipt is pruned.");
        assertEquals(200, get(custodianSession.client, receiptPath).statusCode(),
                "An active authorized CUSTODIAN may recover the permanent receipt after pruning.");

        jdbc.update("UPDATE membership SET status='SUSPENDED',version=version+1,updated_at=? WHERE id=? AND org_id=?",
                Timestamp.valueOf(now()), actor.membershipDbId, owner.orgDbId);
        assertEquals(404, get(actorSession.client, receiptPath).statusCode(),
                "A suspended actor must not read permanent operation history.");
    }

    @Test @Order(41)
    void C4_A5_memberAndUnassignedLeadCannotBrowseBorrowerAllocationActivity() throws Exception {
        Fixture owner = newTenant("C4-allocation-reads", Membership.Role.OWNER);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Fixture lead = fixtureInOrg(owner.orgDbId, Membership.Role.LEAD);
        Session ownerSession = login(owner.username, owner.password);
        Session memberSession = login(member.username, member.password);
        Session leadSession = login(lead.username, lead.password);
        long departmentId = json(post(ownerSession, "/api/departments", GSON.toJson(new NameRequest("C4 Allocation Department"))))
                .get("id").getAsLong();
        LocalDateTime start = LocalDateTime.now(clock).withNano(0).plusDays(2);
        long projectId = json(post(ownerSession, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C4 Allocation Project", start.toString(), start.plusDays(2).toString())))).get("id").getAsLong();
        var resourceCreated = json(post(ownerSession, "/api/resources", GSON.toJson(new ResourceRequest(
                "C4 Allocation Resource", "REUSABLE", 4))));
        long resourceId = resourceCreated.get("id").getAsLong();
        Resource persistedResource = resourceRepository.findByIdAndOrganizationId(resourceId, owner.orgDbId).orElseThrow();
        assertEquals(persistedResource.publicId.toString(), resourceCreated.get("publicId").getAsString(),
                "The compatibility DTO must expose the stable UUID needed by the versioned stock API.");
        long requirementId = json(post(ownerSession, "/api/requirements", GSON.toJson(new RequirementRequest(projectId, resourceId, 1))))
                .get("id").getAsLong();
        var allocationCreated = json(post(ownerSession, "/api/allocations", GSON.toJson(new AllocationRequest(requirementId, 1,
                "Fictional Private Borrower", null, false))));
        long allocationId = allocationCreated.get("id").getAsLong();
        Allocation persistedAllocation = allocationRepository.findByIdAndOrganizationId(allocationId, owner.orgDbId).orElseThrow();
        assertEquals(persistedAllocation.publicId.toString(), allocationCreated.get("publicId").getAsString(),
                "The compatibility DTO must expose the stable UUID needed by versioned lifecycle actions.");
        String v2ResourceList = get(ownerSession.client, "/api/v2/orgs/" + owner.orgPublicId + "/resources?limit=50").body();
        assertTrue(v2ResourceList.contains(persistedResource.publicId.toString()), "The resource UUID must address the v2 inventory state.");
        String v2AllocationList = get(ownerSession.client, "/api/v2/orgs/" + owner.orgPublicId + "/allocations?limit=50").body();
        assertTrue(v2AllocationList.contains(persistedAllocation.publicId.toString()), "The allocation UUID must address versioned lifecycle actions.");

        assertEquals(200, get(ownerSession.client, "/api/allocations").statusCode());
        assertEquals(403, get(memberSession.client, "/api/allocations").statusCode(),
                "MEMBER has no account-linked borrower identity to limit the result to their own records.");
        assertEquals(403, get(leadSession.client, "/api/allocations").statusCode(),
                "No project-assignment relationship exists to safely scope a LEAD allocation view.");
        String v2Allocations = "/api/v2/orgs/" + owner.orgPublicId + "/allocations?limit=50";
        assertEquals(403, get(memberSession.client, v2Allocations).statusCode());
        assertEquals(403, get(leadSession.client, v2Allocations).statusCode());
    }

    @Test @Order(43)
    void C4_A4_loopbackTransportsNeverFollowRedirectsAndSimulatorRejectionsStayRejected() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new edu.orgresource.extended.integration.LoopbackRetrofitFakeTwilioClient(
                URI.create("http://example.invalid/")), "Non-loopback hosts must fail before DNS resolution or network access.");
        assertThrows(IllegalArgumentException.class, () -> new edu.orgresource.extended.integration.LoopbackRetrofitTwilioClient(
                URI.create("http://localhost/"), "AC00000000000000000000000000000000", "fictional-token"),
                "Loopback hostnames are rejected so DNS cannot rebind a trusted transport target.");
        AtomicInteger redirectedRequests = new AtomicInteger();
        AtomicReference<String> redirectedAuthorization = new AtomicReference<>();
        AtomicReference<String> redirectedBody = new AtomicReference<>();
        com.sun.net.httpserver.HttpServer target = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        target.createContext("/sink", exchange -> {
            redirectedRequests.incrementAndGet();
            redirectedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            redirectedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        target.start();

        AtomicReference<String> fakeStatus = new AtomicReference<>("redirect");
        String twilioAccount = "AC00000000000000000000000000000000";
        com.sun.net.httpserver.HttpServer source = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        source.createContext("/fake/messages", exchange -> {
            exchange.getRequestBody().readAllBytes();
            if ("redirect".equals(fakeStatus.get())) {
                exchange.getResponseHeaders().set("Location", "http://127.0.0.1:" + target.getAddress().getPort() + "/sink");
                exchange.sendResponseHeaders(307, -1);
            } else {
                byte[] response = GSON.toJson(java.util.Map.of(
                        "id", "fake-rejected", "status", fakeStatus.get())).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
            exchange.close();
        });
        source.createContext("/2010-04-01/Accounts/" + twilioAccount + "/Messages.json", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().set("Location", "http://127.0.0.1:" + target.getAddress().getPort() + "/sink");
            exchange.sendResponseHeaders(307, -1);
            exchange.close();
        });
        source.start();
        try {
            String base = "http://127.0.0.1:" + source.getAddress().getPort() + "/";
            ReminderDispatch payload = new ReminderDispatch("+15555550181", "Fictional local reminder.");
            C3ReminderTransport transport = new C3ReminderTransport(base, twilioAccount,
                    "C4-local-protocol-token", "+15555550123");
            ReminderSendResult fakeRedirect = transport.send(ReminderMode.FAKE_TWILIO, payload);
            assertEquals(edu.orgresource.extended.domain.ReminderOutcome.UNKNOWN, fakeRedirect.outcome());
            ReminderSendResult twilioRedirect = transport.send(ReminderMode.TWILIO_VIRTUAL, payload);
            assertEquals(edu.orgresource.extended.domain.ReminderOutcome.UNKNOWN, twilioRedirect.outcome());
            assertEquals(0, redirectedRequests.get(), "Neither local protocol client may forward a sensitive body across a redirect.");
            assertNull(redirectedAuthorization.get());
            assertNull(redirectedBody.get());

            fakeStatus.set("rejected");
            ReminderSendResult rejected = transport.send(ReminderMode.FAKE_TWILIO, payload);
            assertEquals(edu.orgresource.extended.domain.ReminderOutcome.REJECTED, rejected.outcome(),
                    "An explicit FakeTwilio rejection is not a simulated acceptance.");
        } finally {
            source.stop(0);
            target.stop(0);
        }
    }

    @Test @Order(42)
    void C4_A3_consentHasTransactionalSendBoundaryAndDistinctNoSendOutcomes() throws Exception {
        Fixture owner = newTenant("C4-reminder-race", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        long departmentId = json(post(session, "/api/departments", GSON.toJson(new NameRequest("C4 Reminder Department"))))
                .get("id").getAsLong();
        LocalDateTime now = LocalDateTime.now(clock).withNano(0);
        long projectId = json(post(session, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C4 Reminder Project", now.plusDays(1).toString(), now.plusDays(3).toString())))).get("id").getAsLong();
        long resourceId = json(post(session, "/api/resources", GSON.toJson(new ResourceRequest(
                "C4 Reminder Reusable", "REUSABLE", 8)))).get("id").getAsLong();
        long requirementId = json(post(session, "/api/requirements", GSON.toJson(new RequirementRequest(projectId, resourceId, 4))))
                .get("id").getAsLong();
        long beforeGate = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(requirementId, 1,
                "Fictional Race Borrower A", "+15555550161", true)))).get("id").getAsLong();
        long afterGate = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(requirementId, 1,
                "Fictional Race Borrower B", "+15555550162", true)))).get("id").getAsLong();
        long unavailableKey = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(requirementId, 1,
                "Fictional Race Borrower C", "+15555550163", true)))).get("id").getAsLong();
        long invalidContact = json(post(session, "/api/allocations", GSON.toJson(new AllocationRequest(requirementId, 1,
                "Fictional Race Borrower D", "+15555550164", true)))).get("id").getAsLong();
        jdbc.update("UPDATE project SET start_at=?,end_at=? WHERE id=? AND organization_id=?",
                Timestamp.valueOf(now.minusHours(1)), Timestamp.valueOf(now.plusHours(4)), projectId, owner.orgDbId);
        for (long id : List.of(beforeGate, afterGate, unavailableKey, invalidContact)) {
            assertEquals(200, post(session, "/api/allocations/" + id + "/approve", null).statusCode());
            assertEquals(200, post(session, "/api/allocations/" + id + "/issue", null).statusCode());
        }

        String beforeGatePath = "/api/allocations/" + beforeGate + "/revoke-sms-consent";
        assertTrue(reminderService.claimOne(beforeGate, now));
        assertNotNull(reminderService.prepareForDispatch(beforeGate, now));
        assertEquals(200, post(session, beforeGatePath, null).statusCode());
        assertFalse(reminderService.authorizeDispatch(beforeGate, now),
                "Revocation committed after preparation but before the send gate must suppress transport.");
        assertEquals("SUPPRESSED", jdbc.queryForObject("SELECT reminder_outcome FROM allocation WHERE id=?", String.class, beforeGate));

        assertTrue(reminderService.claimOne(afterGate, now));
        ReminderDispatch dispatch = reminderService.prepareForDispatch(afterGate, now);
        assertNotNull(dispatch);
        assertTrue(reminderService.authorizeDispatch(afterGate, now), "The final consent gate must be committed before network I/O.");
        AtomicReference<Integer> revocationStatus = new AtomicReference<>();
        AtomicReference<Exception> revocationFailure = new AtomicReference<>();
        com.sun.net.httpserver.HttpServer local = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        local.createContext("/fake/messages", exchange -> {
            try {
                HttpResponse<String> revoked = post(session, "/api/allocations/" + afterGate + "/revoke-sms-consent", null);
                revocationStatus.set(revoked.statusCode());
                byte[] response = "{\"id\":\"fake-race-1\",\"status\":\"accepted\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            } catch (Exception failure) { revocationFailure.set(failure); }
            finally { exchange.close(); }
        });
        local.start();
        try {
            String baseUrl = "http://127.0.0.1:" + local.getAddress().getPort() + "/";
            C3ReminderTransport localTransport = new C3ReminderTransport(baseUrl, "", "", "+15555550123");
            ReminderSendResult result = localTransport.send(ReminderMode.FAKE_TWILIO, dispatch);
            assertNull(revocationFailure.get(), String.valueOf(revocationFailure.get()));
            assertEquals(200, revocationStatus.get(), "Consent revocation during local HTTP proves no database lock spans transport.");
            assertEquals(edu.orgresource.extended.domain.ReminderOutcome.SIMULATED, result.outcome(),
                    "The durable send gate precedes revocation; the already-authorized local request cannot be recalled.");
            assertTrue(reminderService.recordOutcome(afterGate, result));
            assertEquals("SIMULATED", jdbc.queryForObject("SELECT reminder_outcome FROM allocation WHERE id=?", String.class, afterGate));
            assertEquals(false, jdbc.queryForObject("SELECT sms_consent FROM allocation WHERE id=?", Boolean.class, afterGate));
        } finally { local.stop(0); }

        edu.orgresource.extended.security.ContactCipher unavailable =
                new edu.orgresource.extended.security.ContactCipher("", "local-v1");
        C3ReminderService noKeyService = new C3ReminderService(allocationRepository, unavailable);
        tx.executeWithoutResult(status -> {
            assertTrue(noKeyService.claimOne(unavailableKey, now));
            assertNull(noKeyService.prepareForDispatch(unavailableKey, now));
        });
        assertEquals("KEY_UNAVAILABLE", jdbc.queryForObject("SELECT reminder_outcome FROM allocation WHERE id=?", String.class, unavailableKey));
        assertNull(jdbc.queryForObject("SELECT reminder_claimed_at FROM allocation WHERE id=?", Timestamp.class, unavailableKey));
        String unrelatedFixtureKey = java.util.Base64.getEncoder().encodeToString(C1Secrets.sha256("isolated C7 test key"));
        C3ReminderService differentKeyService = new C3ReminderService(allocationRepository,
                new edu.orgresource.extended.security.ContactCipher(unrelatedFixtureKey, "c7-other-key"));
        tx.executeWithoutResult(status -> {
            assertTrue(differentKeyService.claimOne(unavailableKey, now));
            assertNull(differentKeyService.prepareForDispatch(unavailableKey, now));
        });
        assertEquals("KEY_UNAVAILABLE", jdbc.queryForObject("SELECT reminder_outcome FROM allocation WHERE id=?", String.class, unavailableKey));
        assertNull(jdbc.queryForObject("SELECT reminder_claimed_at FROM allocation WHERE id=?", Timestamp.class, unavailableKey));
        var reminderPage = org.springframework.data.domain.PageRequest.of(0, 1000);
        assertTrue(allocationRepository.findReminderCandidates(now, now.plusHours(24), contactCipher.availableKeyId(), reminderPage)
                .stream().anyMatch(row -> row.getAllocationId() == unavailableKey));
        assertFalse(allocationRepository.findReminderCandidates(now, now.plusHours(24), "c7-other-key", reminderPage)
                .stream().anyMatch(row -> row.getAllocationId() == unavailableKey));
        String alternateCase = contactCipher.availableKeyId().toUpperCase(java.util.Locale.ROOT);
        assertFalse(allocationRepository.findReminderCandidates(now, now.plusHours(24), alternateCase, reminderPage)
                .stream().anyMatch(row -> row.getAllocationId() == unavailableKey));
        assertFalse(allocationRepository.findReminderCandidates(now, now.plusHours(24), null, reminderPage)
                .stream().anyMatch(row -> row.getAllocationId() == unavailableKey));
        assertTrue(reminderService.claimOne(unavailableKey, now), "A temporary key outage may be retried after key configuration is restored.");
        assertNotNull(reminderService.prepareForDispatch(unavailableKey, now));
        assertTrue(reminderService.authorizeDispatch(unavailableKey, now));
        assertTrue(reminderService.recordOutcome(unavailableKey, ReminderSendResult.unknown(null)));
        assertFalse(reminderService.claimOne(unavailableKey, now), "An uncertain transport outcome must never be automatically retried.");
        assertEquals("UNKNOWN", jdbc.queryForObject("SELECT reminder_outcome FROM allocation WHERE id=?", String.class, unavailableKey));

        Allocation badContact = allocationRepository.findByIdAndOrganizationId(invalidContact, owner.orgDbId).orElseThrow();
        var invalidEnvelope = contactCipher.encrypt(owner.orgDbId, badContact.publicId, "not-an-e164-contact");
        jdbc.update("UPDATE allocation SET phone_ciphertext=?,phone_nonce=?,phone_key_id=? WHERE id=? AND organization_id=?",
                invalidEnvelope.ciphertext(), invalidEnvelope.nonce(), invalidEnvelope.keyId(), invalidContact, owner.orgDbId);
        assertTrue(reminderService.claimOne(invalidContact, now));
        assertNull(reminderService.prepareForDispatch(invalidContact, now));
        assertEquals("INVALID_CONTACT", jdbc.queryForObject("SELECT reminder_outcome FROM allocation WHERE id=?", String.class, invalidContact));

        assertTrue(reminderService.claimOne(afterGate, now) == false,
                "A completed send must never be automatically retried after revocation.");
        HttpResponse<String> activity = get(session.client, "/api/allocations?status=ISSUED");
        assertEquals(200, activity.statusCode(), activity.body());
        JsonObject sent = JsonParser.parseString(activity.body()).getAsJsonArray().asList().stream()
                .map(row -> row.getAsJsonObject()).filter(row -> row.get("id").getAsLong() == afterGate)
                .findFirst().orElseThrow();
        assertEquals("SIMULATED", sent.get("reminderOutcome").getAsString());
        assertTrue(sent.get("reminderAttempted").getAsBoolean());
        assertFalse(sent.get("expiredPendingCleanup").getAsBoolean());
        assertFalse(sent.get("smsConsent").getAsBoolean());
        assertFalse(sent.has("phoneNumber"));
    }

    private static long scalarLong(java.sql.Statement statement, String sql) throws Exception {
        try (ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next(), "Expected a scalar query row: " + sql);
            return result.getLong(1);
        }
    }

    private void assertLegacyListOmits(HttpResponse<String> response, long foreignId) {
        assertEquals(200, response.statusCode(), response.body());
        assertTrue(JsonParser.parseString(response.body()).isJsonArray(), "Legacy list response must remain an unwrapped array.");
        assertFalse(JsonParser.parseString(response.body()).getAsJsonArray().asList().stream()
                .anyMatch(row -> row.getAsJsonObject().get("id").getAsLong() == foreignId),
                "Active tenant list must not reveal a foreign record ID.");
    }

    private static byte[] tinyPng() throws Exception {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xff123456);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(javax.imageio.ImageIO.write(image, "png", output));
        image.flush();
        return output.toByteArray();
    }

    private static byte[] noisyJpeg() throws Exception {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(512, 512, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.util.Random random = new java.util.Random(0xC4C4L);
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) image.setRGB(x, y, random.nextInt(0x1000000));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(javax.imageio.ImageIO.write(image, "jpeg", output));
        image.flush();
        return output.toByteArray();
    }

    private static byte[] padJpegToSize(byte[] jpeg, int targetSize) throws Exception {
        int extra = targetSize - jpeg.length;
        if (extra < 4 || jpeg.length < 4 || (jpeg[0] & 0xff) != 0xff || (jpeg[1] & 0xff) != 0xd8)
            throw new IllegalArgumentException("JPEG fixture cannot be padded to the requested size.");
        List<Integer> segments = new ArrayList<>();
        while (extra > 65537) { segments.add(65537); extra -= 65537; }
        if (extra > 0 && extra < 4) {
            int previous = segments.remove(segments.size() - 1);
            int finalSize = 4;
            segments.add(previous - (finalSize - extra));
            extra = finalSize;
        }
        if (extra > 0) segments.add(extra);
        ByteArrayOutputStream padded = new ByteArrayOutputStream(targetSize);
        padded.write(jpeg, 0, 2);
        for (int segmentSize : segments) {
            padded.write(0xff); padded.write(0xfe);
            int encodedLength = segmentSize - 2;
            padded.write((encodedLength >>> 8) & 0xff); padded.write(encodedLength & 0xff);
            padded.write(new byte[segmentSize - 4]);
        }
        padded.write(jpeg, 2, jpeg.length - 2);
        return padded.toByteArray();
    }

    private static byte[] pngWithOversizedDimensions(byte[] original, int width, int height) {
        byte[] changed = original.clone();
        writeInt32(changed, 16, width);
        writeInt32(changed, 20, height);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(changed, 12, 17);
        writeInt32(changed, 29, (int) crc.getValue());
        return changed;
    }

    private static void writeInt32(byte[] target, int offset, int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + 1] = (byte) (value >>> 16);
        target[offset + 2] = (byte) (value >>> 8);
        target[offset + 3] = (byte) value;
    }

    private HttpResponse<String> multipartPost(Session session, String path, String contentType, byte[] file) throws Exception {
        return multipartPost(session, path, contentType, file, true);
    }

    private HttpResponse<String> multipartPost(Session session, String path, String contentType,
            byte[] file, boolean includeCsrf) throws Exception {
        String boundary = "OrgResourceC3Boundary";
        ByteArrayOutputStream body = new ByteArrayOutputStream(file.length + 256);
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"fixture.bin\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(file);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(base() + path))
                .header("Origin", origin()).header("Content-Type", "multipart/form-data; boundary=" + boundary);
        if (includeCsrf) builder.header("X-CSRF-TOKEN", session.csrf);
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build();
        return session.client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> malformedMultipartPost(Session session, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base() + path))
                .header("Origin", origin()).header("X-CSRF-TOKEN", session.csrf)
                .header("Content-Type", "multipart/form-data; boundary=broken-boundary")
                .POST(HttpRequest.BodyPublishers.ofString("not a valid multipart body", StandardCharsets.UTF_8)).build();
        return session.client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void invokeRegisteredLifecycleSchedule() {
        ScheduledTask lifecycleTask = scheduledTasks.getScheduledTasks().stream()
                .filter(task -> task.getTask().getRunnable().toString()
                        .contains(C1ReminderScheduler.class.getName() + ".scheduledReminderTick"))
                .findFirst().orElse(null);
        assertNotNull(lifecycleTask, "Spring must register the lifecycle @Scheduled method. Registered: "
                + scheduledTasks.getScheduledTasks().stream().map(task -> task.getTask().getRunnable().toString()).toList());
        lifecycleTask.getTask().getRunnable().run();
    }

    @Test @Order(45)
    void C6aReportingEnforcesRolesSelectionCsrfAndTenantScope() throws Exception {
        Fixture owner = newTenant("C6-report-owner", Membership.Role.OWNER);
        Fixture foreign = newTenant("C6-report-other", Membership.Role.OWNER);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        String exportBody = "{\"format\":\"TENANT_JSON\",\"dataset\":\"RESOURCES\"}";
        assertEquals(401, get(newClient(), root + "/audit").statusCode());
        HttpClient unselected = newClient();
        String preToken = json(get(unselected, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        assertEquals(200, post(unselected, "/api/v2/auth/login", GSON.toJson(new LoginRequest(owner.username, owner.password)),
                preToken, origin()).statusCode());
        assertEquals(409, get(unselected, root + "/activity").statusCode());

        Session foreignSession = login(foreign.username, foreign.password);
        assertEquals(404, get(foreignSession.client, root + "/activity").statusCode());
        assertEquals(404, post(foreignSession, root + "/exports", exportBody).statusCode());
        Session ownerSession = login(owner.username, owner.password);
        assertEquals(403, post(ownerSession, root + "/exports", exportBody, "", origin()).statusCode());
        addMembership(foreign.orgDbId, owner.userDbId, Membership.Role.MEMBER);
        String unselectedWorkspace = "/api/v2/orgs/" + foreign.orgPublicId;
        assertEquals(404, get(ownerSession.client, unselectedWorkspace + "/audit").statusCode());
        assertEquals(404, get(ownerSession.client, unselectedWorkspace + "/activity").statusCode());
        assertEquals(404, post(ownerSession, unselectedWorkspace + "/exports", exportBody).statusCode());
        for (Membership.Role role : List.of(Membership.Role.MEMBER, Membership.Role.LEAD, Membership.Role.CUSTODIAN, Membership.Role.ADMIN)) {
            Fixture actor = fixtureInOrg(owner.orgDbId, role);
            Session session = login(actor.username, actor.password);
            int expected = role == Membership.Role.ADMIN ? 200 : 403;
            assertEquals(expected, get(session.client, root + "/audit").statusCode());
            assertEquals(expected, get(session.client, root + "/activity").statusCode());
            assertEquals(expected, post(session, root + "/exports", exportBody).statusCode());
            if (role == Membership.Role.ADMIN) {
                HttpResponse<String> suspended = post(ownerSession, root + "/members/" + actor.membershipPublicId + "/suspend", "{}", "\"v0\"");
                assertEquals(200, suspended.statusCode(), suspended.body());
                assertEquals(404, get(session.client, root + "/audit").statusCode());
                assertEquals(404, post(session, root + "/exports", exportBody).statusCode());
            }
        }
        assertEquals(200, get(ownerSession.client, root + "/audit").statusCode());
        assertEquals(200, post(ownerSession, root + "/exports", exportBody).statusCode());
        assertEquals(1, jdbc.update("UPDATE SPRING_SESSION SET LAST_ACCESS_TIME=0, EXPIRY_TIME=0 WHERE SESSION_ID=?",
                ownerSession.sessionId));
        assertEquals(401, get(ownerSession.client, root + "/activity").statusCode());
    }

    @Test @Order(46)
    void C6aReportingPaginatesTiesAndRejectsOtherTenantCursors() throws Exception {
        Fixture owner = newTenant("C6-cursor-owner", Membership.Role.OWNER);
        Fixture foreign = newTenant("C6-cursor-other", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        Session foreignSession = login(foreign.username, foreign.password);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        LocalDateTime tie = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).withNano(123456000).plusSeconds(30);
        List<Object[]> fixtures = new ArrayList<>();
        for (int row = 0; row < 105; row++) {
            fixtures.add(new Object[]{owner.orgDbId, owner.userDbId, UUID.randomUUID().toString(), tie});
        }
        String insert = """
                INSERT INTO audit_event(org_id,actor_user_id,action,target_type,target_id,outcome,request_id,created_at)
                VALUES(?,?,'C6_FICTIONAL_EVENT','membership','PRIVATE-TARGET-FIXTURE','SUCCESS',?,?)
                """;
        tx.executeWithoutResult(transaction -> jdbc.batchUpdate(insert, fixtures));
        assertEquals(201, post(session, "/api/resources", GSON.toJson(new ResourceRequest("C6 Movement Resource", "REUSABLE", 2))).statusCode());
        String filter = root + "/audit?limit=100";
        HttpResponse<String> firstResponse = get(session.client, filter);
        assertEquals(200, firstResponse.statusCode(), firstResponse.body());
        assertFalse(firstResponse.body().contains("PRIVATE-TARGET-FIXTURE"));
        var first = json(firstResponse).getAsJsonObject("data");
        assertEquals(100, first.getAsJsonArray("items").size());
        assertEquals(tie.toInstant(ZoneOffset.UTC), java.time.Instant.parse(first.getAsJsonArray("items").get(0)
                .getAsJsonObject().get("occurredAt").getAsString()), "UTC timestamp format and microsecond precision");
        String cursor = first.get("nextCursor").getAsString();
        jdbc.update(insert, owner.orgDbId, owner.userDbId, UUID.randomUUID().toString(), tie.plusSeconds(1));
        var second = json(get(session.client, filter + "&cursor=" + cursor)).getAsJsonObject("data");
        Set<String> seen = new java.util.HashSet<>();
        first.getAsJsonArray("items").forEach(row -> assertTrue(seen.add(row.getAsJsonObject().get("id").getAsString())));
        second.getAsJsonArray("items").forEach(row -> assertTrue(seen.add(row.getAsJsonObject().get("id").getAsString())));
        assertTrue(second.get("nextCursor").isJsonNull());
        assertEquals(105, seen.size());
        assertEquals(400, get(session.client, root + "/audit?limit=-1").statusCode());
        assertEquals(400, get(session.client, root + "/activity?limit=101").statusCode());
        assertEquals(400, get(session.client, root + "/activity?cursor=%21bad").statusCode());
        assertEquals(400, get(foreignSession.client, "/api/v2/orgs/" + foreign.orgPublicId + "/activity?cursor=" + cursor).statusCode());
        var mixed = json(get(session.client, root + "/activity?cursor=" + cursor)).getAsJsonObject("data").getAsJsonArray("items");
        assertTrue(mixed.asList().stream().anyMatch(row -> "MOVEMENT".equals(row.getAsJsonObject().get("source").getAsString())));
        String runtime = jdbc.queryForObject("SELECT CURRENT_USER()", String.class);
        assertNotNull(runtime);
        assertTrue(runtime.matches("[a-zA-Z0-9_]+@[a-zA-Z0-9_.%:-]+"));
        String[] principal = runtime.split("@", 2);
        String account = "'" + principal[0] + "'@'" + principal[1] + "'";
        try (Connection migration = DriverManager.getConnection(System.getenv("EXT_DB_MIGRATION_URL"),
                System.getenv("EXT_DB_MIGRATION_USER"), System.getenv("EXT_DB_MIGRATION_PASSWORD"));
             var statement = migration.createStatement()) {
            String schema = migration.getCatalog();
            assertTrue(schema.matches("orgresource_c1_[a-zA-Z0-9_]+"));
            String table = "`" + schema + "`.`audit_event`";
            statement.execute("REVOKE SELECT ON " + table + " FROM " + account);
            try {
                HttpResponse<String> failed = get(session.client, root + "/activity");
                assertEquals(500, failed.statusCode(), failed.body());
                assertEquals("INTERNAL_ERROR", json(failed).get("code").getAsString());
                assertFalse(json(failed).has("data"), "The working movement query must not produce partial success.");
            } finally {
                statement.execute("GRANT SELECT ON " + table + " TO " + account);
            }
        }
    }

    @Test @Order(47)
    void C6aExportsChecksDatasetBytesLimitsAndRedaction() throws Exception {
        Fixture owner = newTenant("C6-export-owner", Membership.Role.OWNER);
        Fixture foreign = newTenant("C6-export-other", Membership.Role.OWNER);
        Session session = login(owner.username, owner.password);
        Session other = login(foreign.username, foreign.password);
        String path = "/api/v2/orgs/" + owner.orgPublicId + "/exports";
        assertEquals(201, post(other, "/api/resources", GSON.toJson(new ResourceRequest("Foreign C6 Resource", "REUSABLE", 3))).statusCode());
        assertEquals(400, post(session, path, "{\"format\":\"PDF\",\"dataset\":\"RESOURCES\"}").statusCode());
        assertEquals(400, post(session, path, "{\"format\":\"CSV\",\"dataset\":\"ALLOCATIONS\"}").statusCode());
        assertEquals(400, post(session, path, "{\"format\":\"CSV\",\"dataset\":\"AUDIT\",\"limit\":1}").statusCode());
        assertEquals(400, post(session, path, "{\"format\":\"CSV\",\"format\":\"TENANT_JSON\",\"dataset\":\"AUDIT\"}").statusCode());
        assertEquals(400, post(session, path, "{\"format\":\"CSV\",\"dataset\":\"AUDIT\"}" + " ".repeat(1024)).statusCode());
        String resources = "{\"format\":\"TENANT_JSON\",\"dataset\":\"RESOURCES\"}";
        assertTrue(json(post(session, path, resources)).getAsJsonArray("rows").isEmpty());
        String csv = "{\"format\":\"CSV\",\"dataset\":\"RESOURCES\"}";
        for (String name : List.of("  =1+1", "\t+Test", "\r\n-Example", "@Example", "É, \"fictional\"\r\nline")) {
            jdbc.update("""
                    INSERT INTO resource(public_id,organization_id,name,name_key,type,total_quantity)
                    VALUES(UUID_TO_BIN(UUID(),0),?,?,?,'REUSABLE',0)
                    """, owner.orgDbId, name, UUID.randomUUID().toString());
        }
        HttpResponse<String> file = post(session, path, csv);
        assertEquals(200, file.statusCode(), file.body());
        assertTrue(file.headers().firstValue("Content-Type").orElseThrow().startsWith("text/csv"));
        assertTrue(file.headers().firstValue("Content-Disposition").orElseThrow().matches("attachment; filename=\"orgresource-resources-[0-9TZ]+\\.csv\""));
        assertEquals("no-store", file.headers().firstValue("Cache-Control").orElseThrow());
        assertEquals("nosniff", file.headers().firstValue("X-Content-Type-Options").orElseThrow());
        assertTrue(file.body().startsWith("schemaVersion,exportedAt,id,name,type,totalQuantity\r\n"));
        assertTrue(file.body().contains(",\"'  =1+1\",\"REUSABLE\",0\r\n"));
        assertTrue(file.body().contains(",\"'\t+Test\","));
        assertTrue(file.body().contains(",\"'\r\n-Example\","));
        assertTrue(file.body().contains(",\"'@Example\","));
        assertTrue(file.body().contains(",\"É, \"\"fictional\"\"\r\nline\","));
        JsonObject jsonFile = json(post(session, path, resources));
        assertEquals(1, jsonFile.get("schemaVersion").getAsInt());
        assertEquals(owner.orgPublicId.toString(), jsonFile.get("organizationId").getAsString());
        assertEquals(5, jsonFile.getAsJsonArray("rows").size());
        assertFalse(jsonFile.toString().contains("Foreign C6 Resource"));
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        HttpResponse<String> invitation = post(session, root + "/invitations", GSON.toJson(new RoleRequest("MEMBER")));
        assertEquals(201, invitation.statusCode());
        String token = json(invitation).getAsJsonObject("data").get("bearerToken").getAsString();
        String privateAuditTarget = json(invitation).getAsJsonObject("data").get("id").getAsString();
        assertEquals(201, post(session, "/api/resources", GSON.toJson(new ResourceRequest("C6 persisted movement", "REUSABLE", 2))).statusCode());
        for (String dataset : List.of("AUDIT", "MOVEMENTS")) {
            HttpResponse<String> safe = post(session, path, GSON.toJson(java.util.Map.of("format", "TENANT_JSON", "dataset", dataset)));
            assertEquals(200, safe.statusCode());
            assertFalse(json(safe).getAsJsonArray("rows").isEmpty());
            assertFalse(safe.body().contains("actorUserId") || safe.body().contains("targetId")
                    || safe.body().contains("requestFingerprint") || safe.body().contains("phoneCiphertext")
                    || safe.body().contains(token) || safe.body().contains(privateAuditTarget));
        }
        var writer = Executors.newSingleThreadExecutor();
        CountDownLatch uncommitted = new CountDownLatch(1), allowCommit = new CountDownLatch(1);
        try {
            var update = writer.submit(() -> tx.executeWithoutResult(transaction -> {
                jdbc.update("UPDATE resource SET name='C6 committed name' WHERE organization_id=? AND name='C6 persisted movement'",
                        owner.orgDbId);
                uncommitted.countDown();
                try {
                    if (!allowCommit.await(20, TimeUnit.SECONDS)) throw new IllegalStateException("Export snapshot test timed out.");
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                }
            }));
            assertTrue(uncommitted.await(20, TimeUnit.SECONDS));
            HttpResponse<String> whileOpen = post(session, path, resources);
            assertEquals(200, whileOpen.statusCode());
            assertTrue(whileOpen.body().contains("C6 persisted movement"));
            assertFalse(whileOpen.body().contains("C6 committed name"));
            allowCommit.countDown();
            update.get(20, TimeUnit.SECONDS);
            assertTrue(post(session, path, resources).body().contains("C6 committed name"));
        } finally {
            allowCommit.countDown();
            writer.shutdownNow();
        }
        jdbc.update("""
                INSERT INTO resource(public_id,organization_id,name,name_key,type,total_quantity)
                WITH RECURSIVE digit(n) AS (SELECT 0 UNION ALL SELECT n+1 FROM digit WHERE n<9)
                SELECT UUID_TO_BIN(UUID(),0),?,
                       CONCAT('C6 export row ',a.n,b.n,c.n),CONCAT('c6 export row ',a.n,b.n,c.n),'REUSABLE',0
                FROM digit a CROSS JOIN digit b CROSS JOIN digit c
                """, owner.orgDbId);
        HttpResponse<String> refused = post(session, path, csv);
        assertEquals(413, refused.statusCode());
        assertEquals("EXPORT_TOO_LARGE", json(refused).get("code").getAsString());
        assertTrue(refused.headers().firstValue("Content-Disposition").isEmpty());
    }

    private Session login(String username, String password) throws Exception {
        HttpClient client = newClient();
        String preToken = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        return login(client, username, password, preToken, cookie(client));
    }

    private Session login(HttpClient client, String username, String password, String preToken, String before) throws Exception {
        HttpResponse<String> response = post(client, "/api/v2/auth/login", GSON.toJson(new LoginRequest(username, password)), preToken, origin());
        assertEquals(200, response.statusCode(), response.body());
        JsonObject data = json(response).getAsJsonObject("data");
        assertTrue(data.getAsJsonObject("session").get("activeOrgId").isJsonNull(),
                "Login must not select an organization implicitly.");
        var orgs = json(get(client, "/api/v2/orgs")).getAsJsonArray("data");
        assertTrue(orgs.size() > 0, "This test helper requires an explicit organization choice.");
        String organizationId = orgs.get(0).getAsJsonObject().get("id").getAsString();
        HttpResponse<String> selected = post(client, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(UUID.fromString(organizationId))), data.get("csrfToken").getAsString(), origin());
        assertEquals(200, selected.statusCode(), selected.body());
        String currentCsrf = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data")
                .get("csrfToken").getAsString();
        return new Session(client, currentCsrf, cookie(client), before, organizationId);
    }

    private int loginStatus(String username, String password) throws Exception {
        HttpClient client = newClient();
        String token = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        return post(client, "/api/v2/auth/login", GSON.toJson(new LoginRequest(username, password)), token, origin()).statusCode();
    }

    private HttpClient newClient() {
        CookieManager manager = new CookieManager(); manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        return HttpClient.newBuilder().cookieHandler(manager).followRedirects(HttpClient.Redirect.NEVER).build();
    }

    private HttpClient newClientWithCsrf(HttpClient client, String token) { return client; }

    private void refreshCsrf(Session session) throws Exception {
        session.csrf = json(get(session.client, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
    }

    private String cookie(HttpClient client) {
        if (!(client.cookieHandler().orElse(null) instanceof CookieManager manager)) return null;
        return manager.getCookieStore().getCookies().stream().filter(c -> COOKIE.equals(c.getName())).map(java.net.HttpCookie::getValue).findFirst().orElse(null);
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base() + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(Session session, String path, String body) throws Exception {
        return post(session.client, path, body, session.csrf, origin());
    }

    private HttpResponse<String> post(Session session, String path, String body, String ifMatch) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path)).header("Origin", origin())
                .header("X-CSRF-TOKEN", session.csrf);
        if (ifMatch != null) request.header("If-Match", ifMatch);
        if (body == null || body.isBlank()) request.POST(HttpRequest.BodyPublishers.noBody());
        else request.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        return session.client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(Session session, String path, String body, String ifMatch, boolean unused) throws Exception {
        return post(session, path, body, ifMatch);
    }

    private HttpResponse<String> modernPost(Session session, String path, String body,
            String ifMatch, String idempotencyKey) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path))
                .header("Origin", origin()).header("X-CSRF-TOKEN", session.csrf)
                .header("Content-Type", "application/json");
        if (idempotencyKey != null) request.header("Idempotency-Key", idempotencyKey);
        if (ifMatch != null) request.header("If-Match", ifMatch);
        request.POST(HttpRequest.BodyPublishers.ofString(body));
        return session.client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(Session session, String path, String body, String csrf, String origin) throws Exception {
        return post(session.client, path, body, csrf, origin);
    }

    private HttpResponse<String> post(HttpClient client, String path, String body, String csrf, String origin) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path));
        request.header("Origin", origin);
        if (csrf != null) request.header("X-CSRF-TOKEN", csrf);
        if (body == null || body.isBlank()) request.POST(HttpRequest.BodyPublishers.noBody());
        else request.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonObject json(HttpResponse<String> response) { return JsonParser.parseString(response.body()).getAsJsonObject(); }
    private String base() { return "http://127.0.0.1:" + port; }
    private String origin() { return base(); }
    @Test @Order(48)
    void C7RecoveryWithoutCredentialLeavesTheCodeUnused() throws Exception {
        Fixture owner = newTenant("C7-incomplete-credential", Membership.Role.OWNER);
        String code = owner.recoveryCodes.get(0);
        long version = accounts.findById(owner.userDbId).orElseThrow().credentialVersion;
        assertEquals(1, jdbc.update("DELETE FROM local_credential WHERE user_id=?", owner.userDbId));
        HttpClient client = newClient();
        String csrf = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        HttpResponse<String> response = post(client, "/api/v2/auth/recover",
                GSON.toJson(new RecoverRequest(owner.username, code, "Fictional-C7-recovery-" + UUID.randomUUID())), csrf, origin());
        assertEquals(200, response.statusCode());
        assertEquals("If the account and recovery code are valid, password recovery has completed.",
                json(response).getAsJsonObject("data").get("message").getAsString());
        assertEquals(version, accounts.findById(owner.userDbId).orElseThrow().credentialVersion);
        assertNull(tx.execute(status -> recoveryCodes.lockByUserAndHash(owner.userDbId, C1Secrets.sha256(code)).orElseThrow().usedAt));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE actor_user_id=? AND action='ACCOUNT_RECOVERED'",
                Integer.class, owner.userDbId));
    }

    @Test @Order(49)
    void C7InventoryKeepsForeignOrganizationsPrivateAndFreezesAuthorizedWrites() throws Exception {
        Fixture owner = newTenant("C7-frozen-inventory", Membership.Role.OWNER);
        Fixture unrelated = newTenant("C7-inventory-scope", Membership.Role.OWNER);
        Session ownerSession = login(owner.username, owner.password);
        Session unrelatedSession = login(unrelated.username, unrelated.password);
        JsonObject resource = json(post(ownerSession, "/api/resources", GSON.toJson(new ResourceRequest(
                "C7 frozen resource", "REUSABLE", 0))));
        assertEquals(1, jdbc.update("UPDATE organization SET status='FROZEN' WHERE id=?", owner.orgDbId));
        String path = "/api/v2/orgs/" + owner.orgPublicId + "/resources/" + resource.get("publicId").getAsString() + "/receive";
        UUID operationId = UUID.randomUUID();
        String body = GSON.toJson(new ReceiveRequest(operationId, 1));
        HttpResponse<String> unavailable = modernPost(unrelatedSession, path, body, "\"v0\"", C1Secrets.bearerToken());
        assertEquals(404, unavailable.statusCode());
        assertEquals("NOT_FOUND", json(unavailable).get("code").getAsString());
        HttpResponse<String> frozen = modernPost(ownerSession, path, body, "\"v0\"", C1Secrets.bearerToken());
        assertEquals(409, frozen.statusCode());
        assertEquals("ORG_FROZEN", json(frozen).get("code").getAsString());
        assertEquals(0, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=?", Integer.class, resource.get("id").getAsLong()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, operationId.toString()));
    }

    @Test @Order(50)
    void C7GovernanceChecksPermissionBeforeTargetVersion() throws Exception {
        Fixture owner = newTenant("C7-governance-authorization", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Session ownerSession = login(owner.username, owner.password);
        Session memberSession = login(member.username, member.password);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        String target = root + "/members/" + admin.membershipPublicId;
        String grant = GSON.toJson(new RoleRequest("MEMBER"));
        assertEquals(403, post(memberSession, target + "/role", grant).statusCode());
        assertEquals(403, post(memberSession, target + "/suspend", null, "\"v9\"").statusCode());
        assertEquals(428, post(ownerSession, target + "/role", grant).statusCode());
        assertEquals(412, post(ownerSession, target + "/suspend", null, "\"v9\"").statusCode());
        HttpResponse<String> started = post(ownerSession, root + "/transfer-ownership",
                GSON.toJson(new TransferRequest(admin.membershipPublicId, owner.password)));
        assertEquals(201, started.statusCode());
        String transfer = json(started).getAsJsonObject("data").get("id").getAsString();
        assertEquals(403, post(memberSession, root + "/transfers/" + transfer + "/revoke", null).statusCode());
        assertEquals(200, post(ownerSession, root + "/transfers/" + transfer + "/revoke", null, "\"v0\"").statusCode());
        Membership unchanged = memberships.findById(admin.membershipDbId).orElseThrow();
        assertEquals(Membership.Role.ADMIN, unchanged.role);
        assertEquals(Membership.Status.ACTIVE, unchanged.status);
        assertEquals(0L, unchanged.version);

        Session adminSession = login(admin.username, admin.password);
        HttpResponse<String> secondTransfer = post(ownerSession, root + "/transfer-ownership",
                GSON.toJson(new TransferRequest(admin.membershipPublicId, owner.password)));
        assertEquals(201, secondTransfer.statusCode());
        String secondTransferId = json(secondTransfer).getAsJsonObject("data").get("id").getAsString();
        String acceptance = GSON.toJson(new TransferIdRequest(secondTransferId));
        assertEquals(428, post(adminSession, root + "/accept-transfer", acceptance).statusCode());
        assertEquals(200, post(ownerSession, target + "/role", grant, "\"v0\"").statusCode());
        assertEquals(404, post(adminSession, root + "/accept-transfer", acceptance).statusCode());
        assertEquals(Membership.Role.OWNER, memberships.findById(owner.membershipDbId).orElseThrow().role);
    }

    @Test @Order(51)
    void C8ScopeUsesPathMembershipForStockAndSelectedWorkspaceForReportingAndLegacy() throws Exception {
        Fixture home = newTenant("C8-scope-home", Membership.Role.OWNER);
        Fixture target = newTenant("C8-scope-target", Membership.Role.OWNER);
        Fixture outsider = newTenant("C8-scope-outsider", Membership.Role.OWNER);
        addMembership(target.orgDbId, home.userDbId, Membership.Role.CUSTODIAN);
        Fixture member = fixtureInOrg(target.orgDbId, Membership.Role.MEMBER);
        Fixture lead = fixtureInOrg(target.orgDbId, Membership.Role.LEAD);
        Session selectedHome = login(home.username, home.password);
        assertEquals(home.orgPublicId.toString(), selectedHome.activeOrgId);
        Session targetOwner = login(target.username, target.password);
        assertEquals(target.orgPublicId.toString(), targetOwner.activeOrgId);
        HttpResponse<String> created = post(targetOwner, "/api/resources",
                GSON.toJson(new ResourceRequest("Fictional C8 target stock", "REUSABLE", 0)));
        assertEquals(201, created.statusCode());
        JsonObject row = json(created);
        long resourceId = row.get("id").getAsLong();
        String root = "/api/v2/orgs/" + target.orgPublicId;
        String item = root + "/resources/" + row.get("publicId").getAsString();
        long[] homeBefore = stockFactCounts(home.orgDbId);
        long[] targetBefore = stockFactCounts(target.orgDbId);

        HttpResponse<String> read = get(selectedHome.client, item);
        assertEquals(200, read.statusCode());
        assertEquals(200, get(selectedHome.client, item + "/movements").statusCode());
        assertEquals(404, get(selectedHome.client, root + "/activity").statusCode());
        assertEquals(403, get(selectedHome.client, root + "/members").statusCode());
        assertFalse(get(selectedHome.client, "/api/resources").body().contains("Fictional C8 target stock"));
        assertEquals(404, get(selectedHome.client, "/api/resources/" + resourceId + "/availability").statusCode());
        assertEquals(401, get(newClient(), item).statusCode());
        assertEquals(404, get(login(outsider.username, outsider.password).client, item).statusCode());
        for (Fixture lowRole : List.of(member, lead)) {
            Session actor = login(lowRole.username, lowRole.password);
            assertEquals(403, get(actor.client, item).statusCode());
            assertEquals(403, get(actor.client, item + "/movements").statusCode());
            assertEquals(403, modernPost(actor, item + "/receive", GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 2)),
                    read.headers().firstValue("ETag").orElseThrow(), C1Secrets.bearerToken()).statusCode());
        }
        assertArrayEquals(homeBefore, stockFactCounts(home.orgDbId));
        assertArrayEquals(targetBefore, stockFactCounts(target.orgDbId));
        assertEquals(0, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=?", Integer.class, resourceId));

        UUID unselectedOperation = UUID.randomUUID();
        assertEquals(201, modernPost(selectedHome, item + "/receive", GSON.toJson(new ReceiveRequest(unselectedOperation, 2)),
                read.headers().firstValue("ETag").orElseThrow(), C1Secrets.bearerToken()).statusCode());
        assertEquals(200, get(selectedHome.client, root + "/operations/" + unselectedOperation).statusCode());
        long[] targetAfterUnselected = stockFactCounts(target.orgDbId);
        assertEquals(targetBefore[0] + 1, targetAfterUnselected[0]);
        assertEquals(targetBefore[1] + 1, targetAfterUnselected[1]);
        assertEquals(2, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=?", Integer.class, resourceId));
        assertArrayEquals(homeBefore, stockFactCounts(home.orgDbId));

        assertEquals(200, post(selectedHome, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(target.orgPublicId))).statusCode());
        refreshCsrf(selectedHome);
        assertTrue(get(selectedHome.client, "/api/resources").body().contains("Fictional C8 target stock"));
        assertEquals(403, get(selectedHome.client, root + "/activity").statusCode());
        assertEquals(200, get(targetOwner.client, root + "/activity").statusCode());
        String etag = get(selectedHome.client, item).headers().firstValue("ETag").orElseThrow();
        assertEquals(201, modernPost(selectedHome, item + "/receive", GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 3)),
                etag, C1Secrets.bearerToken()).statusCode());
        assertEquals(5, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=?", Integer.class, resourceId));
        long[] afterSwitch = stockFactCounts(target.orgDbId);
        assertEquals(targetAfterUnselected[0] + 1, afterSwitch[0]);
        assertEquals(targetAfterUnselected[1] + 1, afterSwitch[1]);

        Membership current = memberships.findByOrganizationIdAndUserId(target.orgDbId, home.userDbId).orElseThrow();
        String membershipPath = root + "/members/" + current.publicId;
        assertEquals(200, post(targetOwner, membershipPath + "/role", GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"").statusCode());
        long[] afterRoleChange = stockFactCounts(target.orgDbId);
        assertEquals(403, get(selectedHome.client, item).statusCode());
        assertEquals(403, get(selectedHome.client, item + "/movements").statusCode());
        assertEquals(403, get(selectedHome.client, root + "/operations/" + unselectedOperation).statusCode());
        assertEquals(403, modernPost(selectedHome, item + "/receive", GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)),
                etag, C1Secrets.bearerToken()).statusCode());
        assertArrayEquals(afterRoleChange, stockFactCounts(target.orgDbId));

        assertEquals(200, post(targetOwner, membershipPath + "/suspend", null, "\"v1\"").statusCode());
        long[] afterSuspension = stockFactCounts(target.orgDbId);
        assertEquals(404, get(selectedHome.client, item).statusCode());
        assertEquals(404, get(selectedHome.client, item + "/movements").statusCode());
        assertEquals(404, get(selectedHome.client, root + "/operations/" + unselectedOperation).statusCode());
        assertEquals(404, modernPost(selectedHome, item + "/receive", GSON.toJson(new ReceiveRequest(UUID.randomUUID(), 1)),
                etag, C1Secrets.bearerToken()).statusCode());
        assertEquals(404, get(selectedHome.client, root + "/activity").statusCode());
        assertArrayEquals(afterSuspension, stockFactCounts(target.orgDbId));
        assertEquals(5, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=?", Integer.class, resourceId));
        assertArrayEquals(homeBefore, stockFactCounts(home.orgDbId));
    }

    private long[] stockFactCounts(long organizationId) {
        return new long[]{
            jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id=?", Long.class, organizationId),
            jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE organization_id=?", Long.class, organizationId),
            jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE org_id=?", Long.class, organizationId)
        };
    }

    @Test @Order(52)
    void C8WorkspacePagesReturnEveryResourceAndAllocationWithScopedContinuation() throws Exception {
        Fixture owner = newTenant("C8-pages-owner", Membership.Role.OWNER);
        Fixture foreign = newTenant("C8-pages-foreign", Membership.Role.OWNER);
        Fixture custodian = fixtureInOrg(owner.orgDbId, Membership.Role.CUSTODIAN);
        Session actor = login(owner.username, owner.password);
        Session foreignActor = login(foreign.username, foreign.password);
        String root = "/api/v2/workspace";
        String stock = root + "/resources?limit=100";
        String requests = root + "/allocations?status=PENDING&limit=100";

        long departmentId = json(post(actor, "/api/departments", GSON.toJson(new NameRequest("C8 fixture team")))).get("id").getAsLong();
        long projectId = json(post(actor, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C8 fictional project", "2040-12-10T10:00:00", "2040-12-10T11:00:00")))).get("id").getAsLong();
        long resourceId = json(post(actor, "/api/resources", GSON.toJson(new ResourceRequest(
                "C8 flagship resource", "REUSABLE", 40)))).get("id").getAsLong();
        long requirementId = json(post(actor, "/api/requirements", GSON.toJson(new RequirementRequest(
                projectId, resourceId, 20)))).get("id").getAsLong();
        long approvedId = json(post(actor, "/api/allocations", GSON.toJson(new AllocationRequest(
                requirementId, 3, "Fictional fixture operator", null, false)))).get("id").getAsLong();
        assertEquals(200, post(actor, "/api/allocations/" + approvedId + "/approve", null).statusCode());

        assertEquals(departmentId, workspacePage(actor, root + "/departments").getAsJsonArray("items").get(0).getAsJsonObject().get("id").getAsLong());
        assertEquals(projectId, workspacePage(actor, root + "/projects?departmentId=" + departmentId)
                .getAsJsonArray("items").get(0).getAsJsonObject().get("id").getAsLong());
        JsonObject requirement = workspacePage(actor, root + "/requirements?projectId=" + projectId)
                .getAsJsonArray("items").get(0).getAsJsonObject();
        assertEquals(3, requirement.get("allocatedQuantity").getAsInt());
        assertEquals(17, requirement.get("shortageQuantity").getAsInt());
        assertEquals("C8 fictional project", requirement.get("projectName").getAsString());
        assertEquals("C8 flagship resource", requirement.get("resourceName").getAsString());
        assertEquals("REUSABLE", requirement.get("resourceType").getAsString());
        assertEquals("2040-12-10T11:00:00", requirement.get("projectEndAt").getAsString());
        assertEquals("C8 fixture team", workspacePage(actor, root + "/projects")
                .getAsJsonArray("items").get(0).getAsJsonObject().get("departmentName").getAsString());
        assertEquals(404, get(foreignActor.client, root + "/projects?departmentId=" + departmentId).statusCode());
        assertEquals(1, workspacePage(actor, root + "/projects?period=UPCOMING").getAsJsonArray("items").size());
        assertTrue(workspacePage(actor, root + "/projects?period=ACTIVE").getAsJsonArray("items").isEmpty());
        assertEquals(400, get(actor.client, root + "/projects?period=unknown").statusCode());
        assertEquals(401, get(newClient(), stock).statusCode());

        insertWorkspaceResources(owner.orgDbId, 1, 100);
        JsonObject firstPage = workspacePage(actor, stock);
        assertEquals(100, firstPage.getAsJsonArray("items").size());
        assertEquals(37, firstPage.getAsJsonArray("items").get(0).getAsJsonObject().get("availableQuantity").getAsInt());
        assertEquals(1, workspacePage(actor, stock + "&search=C8+FLAGSHIP").getAsJsonArray("items").size());
        String firstCursor = firstPage.get("nextCursor").getAsString();
        JsonObject terminal = workspacePage(actor, stock + "&cursor=" + firstCursor);
        assertEquals(1, terminal.getAsJsonArray("items").size());
        assertTrue(terminal.get("nextCursor").isJsonNull());
        assertEquals(404, get(foreignActor.client, stock + "&cursor=" + firstCursor).statusCode());
        assertEquals(404, get(actor.client, stock + "&type=CONSUMABLE&cursor=" + firstCursor).statusCode());
        assertEquals(404, get(actor.client, root + "/departments?cursor=" + firstCursor).statusCode());
        assertEquals(400, get(actor.client, stock + "&cursor=invalid").statusCode());
        assertEquals(400, get(actor.client, root + "/resources?limit=101").statusCode());

        // Commit a new batch between requests and continue from the same original keyset boundary.
        var concurrentInsert = java.util.concurrent.CompletableFuture.runAsync(
                () -> insertWorkspaceResources(owner.orgDbId, 101, 1000));
        assertEquals(firstPage, workspacePage(actor, stock));
        concurrentInsert.get(20, TimeUnit.SECONDS);
        List<Long> seenResources = new java.util.ArrayList<>();
        firstPage.getAsJsonArray("items").forEach(item -> seenResources.add(item.getAsJsonObject().get("id").getAsLong()));
        readWorkspacePages(actor, stock, firstCursor, seenResources);
        assertEquals(jdbc.queryForList("SELECT id FROM resource WHERE organization_id=? ORDER BY id", Long.class, owner.orgDbId), seenResources);
        assertEquals(1001, seenResources.size());

        var pendingFixtures = java.util.stream.IntStream.range(0, 1001)
                .mapToObj(index -> new Object[]{owner.orgDbId, requirementId, resourceId, "Fictional borrower " + index, java.sql.Timestamp.valueOf(now())})
                .toList();
        jdbc.batchUpdate("INSERT INTO allocation(public_id,organization_id,requirement_id,resource_id,quantity,borrower_name,status,created_at) "
                + "VALUES(UUID_TO_BIN(UUID(),0),?,?,?,1,?,'PENDING',?)", pendingFixtures);

        List<Long> seenAllocations = new java.util.ArrayList<>();
        readWorkspacePages(actor, requests, null, seenAllocations);
        assertEquals(jdbc.queryForList("SELECT id FROM allocation WHERE organization_id=? AND status='PENDING' ORDER BY id",
                Long.class, owner.orgDbId), seenAllocations);
        assertEquals(1001, seenAllocations.size());
        assertEquals(100, workspacePage(actor, requests + "&projectId=" + projectId).getAsJsonArray("items").size());
        assertEquals(404, get(foreignActor.client, requests + "&projectId=" + projectId).statusCode());
        assertEquals(404, get(actor.client, requests + "&projectId=" + projectId + "&cursor="
                + workspacePage(actor, requests).get("nextCursor").getAsString()).statusCode());
        JsonObject overview = workspacePage(actor, root + "/overview");
        assertEquals(1, overview.get("departmentCount").getAsLong());
        assertEquals(0, overview.get("projectActiveCount").getAsLong());
        assertEquals(2, overview.get("resourceTypeCount").getAsLong());
        assertEquals(1, overview.get("requirementShortageCount").getAsLong());
        assertEquals(5, overview.getAsJsonArray("resourcePreview").size());
        assertEquals(1, overview.getAsJsonArray("calendarPreview").size());
        JsonObject allocationSummary = overview.getAsJsonObject("allocations");
        assertEquals(1001, allocationSummary.get("pending").getAsLong());
        assertEquals(0, allocationSummary.get("issued").getAsLong());
        assertEquals(0, allocationSummary.get("overdue").getAsLong());
        assertEquals(0, allocationSummary.get("consumed").getAsLong());
        assertEquals(5, allocationSummary.getAsJsonArray("recent").size());
        assertTrue(allocationSummary.getAsJsonArray("recent").asList().stream()
                .allMatch(item -> item.getAsJsonObject().get("resourceName").getAsString().equals("C8 flagship resource")));
        assertEquals(0, workspacePage(foreignActor, root + "/overview").getAsJsonObject("allocations").get("pending").getAsLong());

        var resourcePlan = jdbc.queryForMap(
                "EXPLAIN SELECT id FROM resource WHERE organization_id=? AND id>? ORDER BY id LIMIT 101", owner.orgDbId, resourceId);
        var allocationPlan = jdbc.queryForMap(
                "EXPLAIN SELECT id FROM allocation WHERE organization_id=? AND id>? AND status='PENDING' ORDER BY id LIMIT 101",
                owner.orgDbId, approvedId);
        assertNotNull(resourcePlan.get("type"));
        assertNotNull(allocationPlan.get("type"));
        System.out.printf("C8 MySQL keyset EXPLAIN: resource (access=%s index=%s est=%s); allocation (access=%s index=%s est=%s)%n",
                resourcePlan.get("type"), resourcePlan.get("key"), resourcePlan.get("rows"),
                allocationPlan.get("type"), allocationPlan.get("key"), allocationPlan.get("rows"));
        assertEquals(37, workspacePage(actor, stock + "&search=FLAGSHIP").getAsJsonArray("items")
                .get(0).getAsJsonObject().get("availableQuantity").getAsInt());
        assertEquals(40, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=?", Integer.class, resourceId));

        Session otherRole = login(custodian.username, custodian.password);
        String allocationCursor = workspacePage(otherRole, requests).get("nextCursor").getAsString();
        assertEquals(404, get(actor.client, root + "/allocations?cursor=" + allocationCursor).statusCode());
        String memberPath = "/api/v2/orgs/" + owner.orgPublicId + "/members/" + custodian.membershipPublicId;
        assertEquals(200, post(actor, memberPath + "/role", GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"").statusCode());
        assertEquals(403, get(otherRole.client, requests + "&cursor=" + allocationCursor).statusCode());
        JsonObject memberOverview = workspacePage(otherRole, root + "/overview");
        assertTrue(memberOverview.get("allocations").isJsonNull(), "No borrower metrics or rows are returned to a MEMBER");
        assertEquals(1, memberOverview.get("requirementShortageCount").getAsLong());

        JsonObject pending = workspacePage(actor, requests)
                .getAsJsonArray("items").get(0).getAsJsonObject();
        assertEquals(requirement, pending.getAsJsonObject("requirement"));
        assertFalse(pending.has("phoneNumber"));
        var relatedResources = jdbc.queryForList("SELECT id FROM resource WHERE organization_id=? AND id<>? ORDER BY id LIMIT 100",
                Long.class, owner.orgDbId, resourceId);
        jdbc.batchUpdate("INSERT INTO resource_requirement(public_id,organization_id,project_id,resource_id,quantity,version) VALUES(UUID_TO_BIN(UUID(),0),?,?,?,2,0)",
                relatedResources.stream().map(id -> new Object[]{owner.orgDbId, projectId, id}).toList());
        String relatedPath = root + "/requirements?projectId=" + projectId + "&limit=50";
        var relatedIds = new java.util.ArrayList<Long>();
        readWorkspacePages(actor, relatedPath, null, relatedIds);
        assertEquals(jdbc.queryForList("SELECT id FROM resource_requirement WHERE organization_id=? AND project_id=? ORDER BY id",
                Long.class, owner.orgDbId, projectId), relatedIds);
        assertEquals(101, relatedIds.size());
        assertEquals(404, get(foreignActor.client, relatedPath).statusCode());
        String nextRequirementPage = workspacePage(actor, relatedPath).get("nextCursor").getAsString();
        assertEquals(404, get(actor.client, root + "/requirements?limit=50&cursor=" + nextRequirementPage).statusCode());
    }

    @Test @Order(53)
    void C8ProjectLeadAssignmentIsVersionedScopedAndClearedOnLossOfRole() throws Exception {
        Fixture owner = newTenant("C8-assignment", Membership.Role.OWNER);
        Fixture lead = fixtureInOrg(owner.orgDbId, Membership.Role.LEAD);
        Fixture admin = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Fixture foreign = newTenant("C8-assignment-foreign", Membership.Role.OWNER);
        Session ownerSession = login(owner.username, owner.password);
        Session leadSession = login(lead.username, lead.password);
        Session adminSession = login(admin.username, admin.password);
        Session foreignSession = login(foreign.username, foreign.password);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        long departmentId = json(post(ownerSession, "/api/departments",
                GSON.toJson(new NameRequest("C8 assignment department")))).get("id").getAsLong();
        long projectId = json(post(ownerSession, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C8 authorized Project", "2040-10-20T09:00:00", "2040-10-20T12:00:00")))).get("id").getAsLong();
        long otherProjectId = json(post(ownerSession, "/api/projects", GSON.toJson(new ProjectRequest(departmentId,
                "C8 other Project", "2040-10-20T09:00:00", "2040-10-20T12:00:00")))).get("id").getAsLong();
        String project = root + "/projects/" + projectRepository.findById(projectId).orElseThrow().publicId;
        String otherProject = root + "/projects/" + projectRepository.findById(otherProjectId).orElseThrow().publicId;
        String assignment = project + "/lead-assignment";
        String operationId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        String body = leadAssignment(operationId, lead.membershipPublicId);

        assertEquals(401, get(newClient(), project).statusCode());
        assertEquals(404, get(foreignSession.client, project).statusCode());
        assertEquals(404, get(leadSession.client, project).statusCode());
        assertEquals(403, modernPost(leadSession, assignment, body, "\"v0\"", key).statusCode());
        assertEquals(428, modernPost(ownerSession, assignment, body, null, key).statusCode());
        assertEquals(412, modernPost(ownerSession, assignment, body, "\"v8\"", key).statusCode());
        assertEquals(409, modernPost(ownerSession, assignment,
                leadAssignment(operationId, member.membershipPublicId), "\"v0\"", key).statusCode());
        assertEquals(404, modernPost(ownerSession, assignment,
                leadAssignment(operationId, foreign.membershipPublicId), "\"v0\"", key).statusCode());
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE project SET lead_membership_id=? WHERE id=?", foreign.membershipDbId, projectId));
        assertNull(jdbc.queryForObject("SELECT lead_membership_id FROM project WHERE id=?", Long.class, projectId));

        var saved = modernPost(adminSession, assignment, body, "\"v0\"", key);
        assertEquals(200, saved.statusCode(), saved.body());
        assertEquals("\"v1\"", saved.headers().firstValue("ETag").orElseThrow());
        assertEquals(lead.membershipPublicId.toString(), json(saved).getAsJsonObject("data").get("leadMembershipId").getAsString());
        assertEquals(json(saved).get("data"), json(modernPost(adminSession, assignment, body, "\"v0\"", key)).get("data"));
        assertEquals(409, modernPost(adminSession, assignment, leadAssignment(operationId, null), "\"v1\"", key).statusCode());
        String receipt = root + "/operations/" + operationId;
        assertEquals(200, get(adminSession.client, receipt).statusCode());
        assertEquals(404, get(ownerSession.client, receipt).statusCode());
        assertEquals(403, get(leadSession.client, receipt).statusCode());
        assertEquals(200, get(leadSession.client, project).statusCode());
        assertEquals(404, get(leadSession.client, otherProject).statusCode());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE org_id=? AND action='PROJECT_LEAD_CHANGED'",
                Integer.class, owner.orgDbId));

        String leadRole = root + "/members/" + lead.membershipPublicId + "/role";
        assertEquals(200, post(ownerSession, leadRole, GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"").statusCode());
        assertEquals(403, get(leadSession.client, project).statusCode());
        assertEquals(200, post(ownerSession, leadRole, GSON.toJson(new RoleRequest("LEAD")), "\"v1\"").statusCode());
        assertEquals(404, get(leadSession.client, project).statusCode());
        var current = get(ownerSession.client, project);
        assertEquals("\"v2\"", current.headers().firstValue("ETag").orElseThrow());
        assertTrue(json(current).getAsJsonObject("data").get("leadMembershipId").isJsonNull());
        assertEquals(200, modernPost(ownerSession, assignment, leadAssignment(UUID.randomUUID().toString(),
                lead.membershipPublicId), "\"v2\"", UUID.randomUUID().toString()).statusCode());
        assertEquals(200, modernPost(ownerSession, assignment, leadAssignment(UUID.randomUUID().toString(), null),
                "\"v3\"", UUID.randomUUID().toString()).statusCode());
        assertEquals(404, get(leadSession.client, project).statusCode());

        String adminRole = root + "/members/" + admin.membershipPublicId + "/role";
        assertEquals(200, post(ownerSession, adminRole, GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"").statusCode());
        assertEquals(403, get(adminSession.client, receipt).statusCode());
        assertEquals(403, modernPost(adminSession, assignment, body, "\"v0\"", key).statusCode());
    }

    @Test @Order(54)
    void C8BorrowerAccessBindsIdentityAndHidesDirectoryAndContacts() throws Exception {
        Fixture owner = newTenant("C8 borrowers", Membership.Role.OWNER);
        Fixture assigned = fixtureInOrg(owner.orgDbId, Membership.Role.LEAD);
        Fixture borrower = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Fixture sameName = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Fixture away = newTenant("C8 borrowers elsewhere", Membership.Role.OWNER);
        Session officer = login(owner.username, owner.password);
        Session lead = login(assigned.username, assigned.password);
        Session self = login(borrower.username, borrower.password);
        Session other = login(sameName.username, sameName.password);
        Session outsider = login(away.username, away.password);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        long department = json(post(officer, "/api/departments", GSON.toJson(new NameRequest("C8 fictional borrower team"))))
                .get("id").getAsLong();
        long project = json(post(officer, "/api/projects", GSON.toJson(new ProjectRequest(department,
                "Fictional light workshop", "2040-11-01T10:00:00", "2040-11-01T13:00:00")))).get("id").getAsLong();
        long otherProject = json(post(officer, "/api/projects", GSON.toJson(new ProjectRequest(department,
                "Fictional sound workshop", "2040-11-01T10:00:00", "2040-11-01T13:00:00")))).get("id").getAsLong();
        UUID projectKey = projectRepository.findById(project).orElseThrow().publicId;
        UUID otherKey = projectRepository.findById(otherProject).orElseThrow().publicId;
        String assignment = root + "/projects/" + projectKey + "/lead-assignment";
        assertEquals(200, modernPost(officer, assignment, leadAssignment(UUID.randomUUID().toString(), assigned.membershipPublicId),
                "\"v0\"", UUID.randomUUID().toString()).statusCode());
        String directory = root + "/borrowers?projectId=" + projectKey;
        String directoryOther = root + "/borrowers?projectId=" + otherKey;
        var first = get(lead.client, directory + "&limit=1");
        assertEquals(200, first.statusCode(), first.body());
        String next = json(first).getAsJsonObject("data").get("nextCursor").getAsString();
        assertEquals(404, get(officer.client, directoryOther + "&limit=1&cursor=" + next).statusCode());
        assertEquals(404, get(lead.client, directoryOther).statusCode());
        assertEquals(403, get(self.client, directory).statusCode());
        assertEquals(404, get(outsider.client, directory).statusCode());
        var duplicates = json(get(lead.client, directory + "&search=fictional+member")).getAsJsonObject("data").getAsJsonArray("items");
        assertEquals(2, duplicates.size());
        for (var entry : duplicates) {
            assertEquals(Set.of("membershipId", "displayName"), entry.getAsJsonObject().keySet());
            assertEquals("Fictional MEMBER", entry.getAsJsonObject().get("displayName").getAsString());
        }

        long resource = json(post(officer, "/api/resources", GSON.toJson(new ResourceRequest(
                "Fictional lights", "REUSABLE", 8)))).get("id").getAsLong();
        long requirement = json(post(officer, "/api/requirements", GSON.toJson(new RequirementRequest(
                project, resource, 4)))).get("id").getAsLong();
        UUID requirementKey = UUID.fromString(jdbc.queryForObject("SELECT BIN_TO_UUID(public_id,0) FROM resource_requirement WHERE id=?",
                String.class, requirement));
        String operationId = UUID.randomUUID().toString(), idemKey = UUID.randomUUID().toString();
        JsonObject input = new JsonObject();
        input.addProperty("operationId", operationId);
        input.addProperty("requirementId", requirementKey.toString());
        input.addProperty("quantity", 2);
        assertEquals(400, modernPost(lead, root + "/allocations", input.toString(), null, idemKey).statusCode());
        input.addProperty("borrowerMembershipId", away.membershipPublicId.toString());
        assertEquals(404, modernPost(lead, root + "/allocations", input.toString(), null, idemKey).statusCode());
        input.addProperty("borrowerMembershipId", borrower.membershipPublicId.toString());
        input.addProperty("borrowerNameSnapshot", "Injected label");
        assertEquals(400, modernPost(lead, root + "/allocations", input.toString(), null, idemKey).statusCode());
        input.remove("borrowerNameSnapshot");
        var created = modernPost(lead, root + "/allocations", input.toString(), null, idemKey);
        assertEquals(201, created.statusCode(), created.body());
        var linked = json(created).getAsJsonObject("data");
        UUID allocationKey = UUID.fromString(linked.get("id").getAsString());
        long allocationId = jdbc.queryForObject("SELECT id FROM allocation WHERE public_id=UUID_TO_BIN(?,0)", Long.class, allocationKey.toString());
        assertEquals(borrower.membershipDbId, jdbc.queryForObject("SELECT borrower_membership_id FROM allocation WHERE id=?", Long.class, allocationId));
        assertEquals(assigned.membershipDbId, jdbc.queryForObject("SELECT requested_by_membership_id FROM allocation WHERE id=?", Long.class, allocationId));
        assertEquals("Fictional MEMBER", linked.get("borrowerNameSnapshot").getAsString());
        assertEquals(linked, json(modernPost(lead, root + "/allocations", input.toString(), null, idemKey)).get("data"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE requirement_id=?", Integer.class, requirement));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE resource_id=?", Integer.class, resource));

        String borrowedRoute = root + "/borrowed-allocations";
        String borrowedItem = borrowedRoute + "/" + allocationKey;
        assertEquals(1, json(get(self.client, borrowedRoute)).getAsJsonObject("data").getAsJsonArray("items").size());
        assertTrue(json(get(other.client, borrowedRoute)).getAsJsonObject("data").getAsJsonArray("items").isEmpty());
        assertEquals(404, get(other.client, borrowedItem).statusCode());
        assertEquals(404, get(outsider.client, borrowedItem).statusCode());
        var view = get(self.client, borrowedItem);
        assertEquals(200, view.statusCode());
        assertTrue(view.body().contains("Fictional light workshop"));
        assertFalse(view.body().matches("(?is).*(phone|borrowerName|actor|provider|ciphertext).*"));
        assertEquals("\"v0\"", view.headers().firstValue("ETag").orElseThrow());

        assertFalse(json(view).getAsJsonObject("data").get("smsConsent").getAsBoolean());
        String revoke = borrowedItem + "/revoke-sms-consent";
        String noOpBody = GSON.toJson(new OperationRequest(UUID.randomUUID()));
        String noOpKey = UUID.randomUUID().toString();
        var noOp = modernPost(self, revoke, noOpBody, "\"v0\"", noOpKey);
        assertEquals(200, noOp.statusCode(), noOp.body());
        assertFalse(json(noOp).getAsJsonObject("data").get("smsConsent").getAsBoolean());
        assertEquals("\"v0\"", noOp.headers().firstValue("ETag").orElseThrow());
        assertEquals(json(noOp).get("data"), json(modernPost(self, revoke, noOpBody, "\"v0\"", noOpKey)).get("data"));

        var syntheticContact = contactCipher.encrypt(owner.orgDbId, allocationKey, "+15555550193");
        jdbc.update("UPDATE allocation SET sms_consent=TRUE,phone_ciphertext=?,phone_nonce=?,phone_key_id=?,reminder_outcome='CLAIMED',"
                        + "reminder_claimed_at=? WHERE id=?", syntheticContact.ciphertext(), syntheticContact.nonce(),
                syntheticContact.keyId(), java.sql.Timestamp.valueOf(now()), allocationId);
        var active = json(get(self.client, borrowedItem)).getAsJsonObject("data");
        assertTrue(active.get("smsConsent").getAsBoolean());
        assertFalse(active.toString().matches("(?is).*(phone|borrowerName|actor|provider|ciphertext).*"));
        String revokeOp = UUID.randomUUID().toString(), revokeKey = UUID.randomUUID().toString();
        String revokeBody = "{\"operationId\":\"" + revokeOp + "\"}";
        assertEquals(404, modernPost(other, revoke, revokeBody, "\"v0\"", revokeKey).statusCode());
        assertEquals(412, modernPost(self, revoke, revokeBody, "\"v8\"", revokeKey).statusCode());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE id=? AND version=0 AND sms_consent=TRUE "
                + "AND phone_ciphertext IS NOT NULL AND reminder_outcome='CLAIMED'", Integer.class, allocationId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, revokeOp));
        var revoked = modernPost(self, revoke, revokeBody, "\"v0\"", revokeKey);
        assertEquals(200, revoked.statusCode(), revoked.body());
        assertFalse(json(revoked).getAsJsonObject("data").get("smsConsent").getAsBoolean());
        assertEquals(200, modernPost(self, revoke, revokeBody, "\"v0\"", revokeKey).statusCode());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE id=? AND sms_consent=FALSE AND phone_ciphertext IS NULL "
                + "AND phone_nonce IS NULL AND phone_key_id IS NULL AND reminder_outcome='SUPPRESSED' AND status='PENDING'",
                Integer.class, allocationId));
        assertEquals(200, get(self.client, root + "/operations/" + revokeOp).statusCode());
        assertEquals(403, get(other.client, root + "/operations/" + revokeOp).statusCode());
        String currentVersion = revoked.headers().firstValue("ETag").orElseThrow();
        var redundant = modernPost(self, revoke, GSON.toJson(new OperationRequest(UUID.randomUUID())),
                currentVersion, UUID.randomUUID().toString());
        assertEquals(200, redundant.statusCode());
        assertEquals(currentVersion, redundant.headers().firstValue("ETag").orElseThrow());
        assertEquals(json(revoked).get("data"), json(redundant).get("data"));

        // Reinstall an active fictional fixture state before suspending the borrower.
        jdbc.update("UPDATE allocation SET sms_consent=TRUE,phone_ciphertext=?,phone_nonce=?,phone_key_id=? WHERE id=?",
                syntheticContact.ciphertext(), syntheticContact.nonce(), syntheticContact.keyId(), allocationId);
        assertEquals(200, post(officer, root + "/members/" + borrower.membershipPublicId + "/suspend", "{}", "\"v0\"").statusCode());
        assertEquals(404, get(self.client, borrowedItem).statusCode());
        assertEquals(404, get(self.client, root + "/operations/" + revokeOp).statusCode());
        UUID suspendedOperation = UUID.randomUUID();
        assertEquals(404, modernPost(self, revoke, GSON.toJson(new OperationRequest(suspendedOperation)),
                currentVersion, UUID.randomUUID().toString()).statusCode());
        assertEquals(404, modernPost(self, revoke, revokeBody, "\"v0\"", revokeKey).statusCode());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE id=? AND sms_consent=TRUE "
                + "AND phone_ciphertext IS NOT NULL AND status='PENDING'", Integer.class, allocationId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN(?,0)",
                Integer.class, suspendedOperation.toString()));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE resource_id=?", Integer.class, resource));
        assertEquals(borrower.membershipDbId, jdbc.queryForObject("SELECT borrower_membership_id FROM allocation WHERE id=?", Long.class, allocationId));
    }

    @Test @Order(55)
    void C8AssignedLeadUsesOnlyTheirProjectAndOwnPendingAllocationActions() throws Exception {
        Fixture owner = newTenant("C8 work scope", Membership.Role.OWNER);
        Fixture lead = fixtureInOrg(owner.orgDbId, Membership.Role.LEAD);
        Fixture borrower = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Session officer = login(owner.username, owner.password);
        Session scoped = login(lead.username, lead.password);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        long department = json(post(officer, "/api/departments", GSON.toJson(new NameRequest("Fictional scope department"))))
                .get("id").getAsLong();
        String from = "2040-11-01T10:00:00", until = "2040-11-01T13:00:00";
        long one = json(post(officer, "/api/projects",
                GSON.toJson(new ProjectRequest(department, "Fictional assigned Project", from, until)))).get("id").getAsLong();
        long two = json(post(officer, "/api/projects",
                GSON.toJson(new ProjectRequest(department, "Fictional other Project", from, until)))).get("id").getAsLong();
        UUID project = projectRepository.findById(one).orElseThrow().publicId;
        UUID otherProject = projectRepository.findById(two).orElseThrow().publicId;
        String path = root + "/projects/" + project;
        assertEquals(200, modernPost(officer, path + "/lead-assignment",
                leadAssignment(UUID.randomUUID().toString(), lead.membershipPublicId), "\"v0\"", UUID.randomUUID().toString()).statusCode());
        var list = workspacePage(scoped, root + "/projects?limit=1");
        assertEquals(1, list.getAsJsonArray("items").size());
        assertTrue(list.get("nextCursor").isJsonNull());
        assertEquals(project.toString(), list.getAsJsonArray("items").get(0).getAsJsonObject().get("id").getAsString());
        String update = GSON.toJson(java.util.Map.of("name", "Fictional scoped rename", "startAt", from, "endAt", until));
        assertEquals(404, post(scoped, root + "/projects/" + otherProject + "/update", update, "\"v0\"").statusCode());
        assertEquals(428, post(scoped, path + "/update", update).statusCode());
        var projectWrite = post(scoped, path + "/update", update, "\"v1\"");
        assertEquals(200, projectWrite.statusCode(), projectWrite.body());
        assertEquals("\"v2\"", projectWrite.headers().firstValue("ETag").orElseThrow());

        long resource = json(post(officer, "/api/resources", GSON.toJson(
                new ResourceRequest("Fictional shared Resource", "REUSABLE", 5)))).get("id").getAsLong();
        UUID resourceKey = resourceRepository.findById(resource).orElseThrow().publicId;
        String requirementPath = root + "/requirements";
        String reqBody = GSON.toJson(java.util.Map.of("projectId", project, "resourceId", resourceKey, "quantity", 5));
        assertEquals(404, post(scoped, requirementPath, reqBody.replace(project.toString(), otherProject.toString())).statusCode());
        var requirement = post(scoped, requirementPath, reqBody);
        assertEquals(201, requirement.statusCode(), requirement.body());
        String req = json(requirement).getAsJsonObject("data").get("id").getAsString();
        assertEquals(1, workspacePage(scoped, requirementPath).getAsJsonArray("items").size());
        assertEquals(404, get(scoped.client, requirementPath + "?projectId=" + otherProject).statusCode());
        assertEquals(200, get(scoped.client, requirementPath + "/" + req).statusCode());
        assertEquals(412, post(scoped, requirementPath + "/" + req + "/update", "{\"quantity\":4}", "\"v9\"").statusCode());
        assertEquals(200, post(scoped, requirementPath + "/" + req + "/update", "{\"quantity\":4}", "\"v0\"").statusCode());
        assertEquals(409, post(scoped, path + "/delete", null, "\"v2\"").statusCode());

        String request = GSON.toJson(java.util.Map.of("operationId", UUID.randomUUID(), "requirementId", req,
                "quantity", 2, "borrowerMembershipId", borrower.membershipPublicId));
        var ownCreated = modernPost(scoped, root + "/allocations", request, null, UUID.randomUUID().toString());
        assertEquals(201, ownCreated.statusCode(), ownCreated.body());
        var officerCreated = modernPost(officer, root + "/allocations", GSON.toJson(java.util.Map.of(
                "operationId", UUID.randomUUID(), "requirementId", req, "quantity", 1, "borrowerMembershipId", borrower.membershipPublicId)),
                null, UUID.randomUUID().toString());
        assertEquals(201, officerCreated.statusCode(), officerCreated.body());
        String own = json(ownCreated).getAsJsonObject("data").get("id").getAsString();
        String other = json(officerCreated).getAsJsonObject("data").get("id").getAsString();
        String ownPath = root + "/allocations/" + own, otherPath = root + "/allocations/" + other;
        var projectPage = workspacePage(scoped, path + "/allocations?limit=1");
        assertTrue(projectPage.getAsJsonArray("items").get(0).getAsJsonObject().get("canCancel").getAsBoolean());
        String continuation = projectPage.get("nextCursor").getAsString();
        var last = workspacePage(scoped, path + "/allocations?limit=1&cursor=" + continuation);
        assertEquals(other, last.getAsJsonArray("items").get(0).getAsJsonObject().get("id").getAsString());
        assertFalse(last.getAsJsonArray("items").get(0).getAsJsonObject().get("canCancel").getAsBoolean());
        assertTrue(last.get("nextCursor").isJsonNull());
        assertEquals(404, get(scoped.client, root + "/projects/" + otherProject + "/allocations?cursor=" + continuation).statusCode());
        assertEquals(200, get(scoped.client, ownPath).statusCode());
        String cancelOperation = UUID.randomUUID().toString();
        String cancelBody = "{\"operationId\":\"" + cancelOperation + "\"}", cancelKey = UUID.randomUUID().toString();
        assertEquals(403, modernPost(scoped, ownPath + "/approve", cancelBody, "\"v0\"", cancelKey).statusCode());
        assertEquals(404, modernPost(scoped, otherPath + "/cancel", cancelBody, "\"v0\"", cancelKey).statusCode());
        assertEquals(404, post(scoped, otherPath + "/delete", null, "\"v0\"").statusCode());
        assertEquals(200, modernPost(scoped, ownPath + "/cancel", cancelBody, "\"v0\"", cancelKey).statusCode());
        assertEquals(200, get(scoped.client, root + "/operations/" + cancelOperation).statusCode());
        assertEquals(200, post(scoped, ownPath + "/delete", null, "\"v1\"").statusCode());
        assertEquals(404, get(scoped.client, ownPath).statusCode());
        assertEquals(409, post(scoped, requirementPath + "/" + req + "/delete", null, "\"v1\"").statusCode());
        assertEquals(200, post(officer, otherPath + "/delete", null, "\"v0\"").statusCode());
        assertEquals(200, post(scoped, requirementPath + "/" + req + "/delete", null, "\"v1\"").statusCode());
        assertEquals(200, post(scoped, path + "/delete", null, "\"v2\"").statusCode());
        assertEquals(404, get(scoped.client, path).statusCode());
        assertEquals(5, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=?", Integer.class, resource));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE resource_id=?", Integer.class, resource));
    }

    @Test @Order(56)
    void C8HistoricalBusinessTimeDoesNotBackdateSessionsOrReceipts() throws Exception {
        Fixture owner = newTenant("C8 clock isolation", Membership.Role.OWNER);
        Instant civil = Instant.parse("2026-01-15T01:00:00Z");
        Instant beforeWall = clock.instant();
        SCOPED_TEST_BUSINESS_TIME.set(civil);
        try {
            Session actor = login(owner.username, owner.password);
            var principal = get(actor.client, "/api/v2/session");
            assertEquals(200, principal.statusCode(), principal.body());
            assertTrue(principal.body().contains(owner.orgPublicId.toString()));
            var storedSession = springSessions.findById(actor.sessionId);
            assertNotNull(storedSession);
            Long authenticatedAt = storedSession.getAttribute(
                    edu.orgresource.extended.security.C1AccountSessionFilter.SESSION_CREATED_AT);
            assertNotNull(authenticatedAt);
            assertTrue(authenticatedAt >= beforeWall.toEpochMilli() && authenticatedAt <= clock.millis());
            long department = json(post(actor, "/api/departments", GSON.toJson(new NameRequest("Fictional historical team"))))
                    .get("id").getAsLong();
            var projectResponse = post(actor, "/api/projects", GSON.toJson(new ProjectRequest(department,
                    "Fictional January workshop", "2026-01-15T10:00:00", "2026-01-15T13:00:00")));
            assertEquals(201, projectResponse.statusCode(), projectResponse.body());
            long project = json(projectResponse).get("id").getAsLong();
            long resource = json(post(actor, "/api/resources", GSON.toJson(new ResourceRequest(
                    "Fictional historical projector", "REUSABLE", 4)))).get("id").getAsLong();
            assertEquals(LocalDateTime.ofInstant(civil, ZoneOffset.UTC), jdbc.queryForObject(
                    "SELECT occurred_at FROM stock_movement WHERE resource_id=? AND kind='INITIAL'", LocalDateTime.class, resource));
            long requirement = json(post(actor, "/api/requirements", GSON.toJson(new RequirementRequest(project, resource, 2))))
                    .get("id").getAsLong();
            long allocation = json(post(actor, "/api/allocations", GSON.toJson(new AllocationRequest(
                    requirement, 2, "Fictional historical borrower", null, false)))).get("id").getAsLong();
            UUID allocationKey = allocationRepository.findById(allocation).orElseThrow().publicId;
            String allocationPath = "/api/v2/orgs/" + owner.orgPublicId + "/allocations/" + allocationKey;
            assertEquals(200, modernPost(actor, allocationPath + "/approve", GSON.toJson(new OperationRequest(UUID.randomUUID())),
                    "\"v0\"", UUID.randomUUID().toString()).statusCode());
            Instant issueAt = civil.plusSeconds(3600);
            SCOPED_TEST_BUSINESS_TIME.set(issueAt);
            UUID issueId = UUID.randomUUID();
            var issued = modernPost(actor, allocationPath + "/issue", GSON.toJson(new OperationRequest(issueId)),
                    "\"v1\"", UUID.randomUUID().toString());
            assertEquals(200, issued.statusCode(), issued.body());
            assertEquals(LocalDateTime.ofInstant(issueAt, ZoneOffset.UTC), jdbc.queryForObject(
                    "SELECT occurred_at FROM stock_movement WHERE allocation_id=? AND kind='ISSUE'", LocalDateTime.class, allocation));
            assertEquals(LocalDateTime.parse("2026-01-15T10:00:00"), jdbc.queryForObject(
                    "SELECT issued_at FROM allocation WHERE id=?", LocalDateTime.class, allocation));
            LocalDateTime recordedAt = jdbc.queryForObject(
                    "SELECT created_at FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN(?,0)", LocalDateTime.class, issueId.toString());
            assertFalse(recordedAt.isBefore(LocalDateTime.ofInstant(beforeWall.minusSeconds(1), ZoneOffset.UTC)));
            assertFalse(recordedAt.isAfter(now().plusSeconds(1)));
            assertEquals(2, jdbc.queryForObject("SELECT SUM(delta_issued) FROM stock_movement WHERE allocation_id=?", Integer.class, allocation));
            assertEquals(200, get(actor.client, "/api/v2/session").statusCode());
        } finally {
            SCOPED_TEST_BUSINESS_TIME.set(null);
        }
    }

    @Test @Order(57)
    void C8LeadMutationAndDemotionSerializeOnTheCurrentOrganization() throws Exception {
        Fixture owner = newTenant("C8 assignment race", Membership.Role.OWNER);
        Fixture lead = fixtureInOrg(owner.orgDbId, Membership.Role.LEAD);
        Session officer = login(owner.username, owner.password), actor = login(lead.username, lead.password);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        long department = json(post(officer, "/api/departments", GSON.toJson(new NameRequest("Fictional race team"))))
                .get("id").getAsLong();
        String from = "2040-11-01T10:00:00", until = "2040-11-01T13:00:00";
        long project = json(post(officer, "/api/projects", GSON.toJson(new ProjectRequest(department,
                "Fictional race original", from, until)))).get("id").getAsLong();
        String path = root + "/projects/" + projectRepository.findById(project).orElseThrow().publicId;
        assertEquals(200, modernPost(officer, path + "/lead-assignment",
                leadAssignment(UUID.randomUUID().toString(), lead.membershipPublicId), "\"v0\"", UUID.randomUUID().toString()).statusCode());
        String target = root + "/members/" + lead.membershipPublicId + "/role";
        String change = GSON.toJson(new ProjectUpdateRequest("Fictional race changed", from, until));
        var workers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            var edited = workers.submit(() -> {
                assertTrue(start.await(15, TimeUnit.SECONDS));
                return post(actor, path + "/update", change, "\"v1\"").statusCode();
            });
            var demoted = workers.submit(() -> {
                assertTrue(start.await(15, TimeUnit.SECONDS));
                return post(officer, target, GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"").statusCode();
            });
            start.countDown();
            assertEquals(200, demoted.get(15, TimeUnit.SECONDS));
            int editedStatus = edited.get(15, TimeUnit.SECONDS);
            assertTrue(Set.of(200, 403).contains(editedStatus), "The edit must finish before demotion or be refused.");
            Project persisted = projectRepository.findById(project).orElseThrow();
            assertNull(persisted.leadMembershipId);
            assertEquals(editedStatus == 200 ? "Fictional race changed" : "Fictional race original", persisted.name);
            assertEquals(editedStatus == 200 ? 3 : 2, persisted.version);
            assertEquals(403, post(actor, path + "/update", change, "\"v" + persisted.version + "\"").statusCode());
        } finally {
            workers.shutdownNow();
        }
    }

    @Test @Order(58)
    void C8DemoDisclosureIsServerBackedAndAppearsInBothExportFormats() throws Exception {
        Fixture ordinary = newTenant("C8 ordinary reporting", Membership.Role.OWNER);
        Fixture demo = newTenant("C8 fictional demo reporting", Membership.Role.OWNER);
        Session ordinaryActor = login(ordinary.username, ordinary.password), demoActor = login(demo.username, demo.password);
        String metadata = "/api/v2/demo/metadata";
        var before = get(newClient(), metadata);
        assertEquals(200, before.statusCode(), before.body());
        var ordinaryClassification = json(before).getAsJsonObject("data");
        assertEquals(Set.of("synthetic", "datasetId", "demonstrationAsOf", "notice"), ordinaryClassification.keySet());
        assertFalse(ordinaryClassification.get("synthetic").getAsBoolean());
        for (String field : List.of("datasetId", "demonstrationAsOf", "notice")) assertTrue(ordinaryClassification.get(field).isJsonNull());
        String label = "orgresource-c8-plan-v1-012345678901-abcdefabcdef-small";
        try {
            jdbc.update("UPDATE organization SET demo_dataset_id=?,demo_reference_key='fictional-demo-entry',"
                    + "demo_source_sha=REPEAT('a',64),demo_plan_sha=REPEAT('b',64),demo_as_of='2026-11-19' WHERE id=?",
                    label, demo.orgDbId);
            var partial = get(newClient(), metadata);
            assertEquals(503, partial.statusCode(), "Partially tagged activity must never be classified as ordinary.");
            assertTrue(partial.body().contains("DEMO_CLASSIFICATION_UNAVAILABLE"));
            assertFalse(partial.body().contains(label));
            tx.executeWithoutResult(status -> {
                var operator = accounts.saveAndFlush(new UserAccount("c8-demo-operator", "Fictional local demo operator", now()));
                credentials.saveAndFlush(new LocalCredential(operator.id, passwordEncoder.encode(UUID.randomUUID().toString()), now()));
            });
            assertEquals(503, get(newClient(), metadata).statusCode(),
                    "An operator credential alone cannot advertise an incomplete demo cohort.");
            for (int sequenced = 2; sequenced <= 3; ++sequenced) {
                var extra = newTenant("C8 fictional mixed cohort " + sequenced, Membership.Role.OWNER);
                jdbc.update("UPDATE organization SET demo_dataset_id=?,demo_reference_key=?,"
                                + "demo_source_sha=REPEAT('a',64),demo_plan_sha=REPEAT('b',64),demo_as_of='2026-11-19' WHERE id=?",
                        label, "fictional-mixed-entry-" + sequenced, extra.orgDbId);
            }
            var mixed = get(newClient(), metadata);
            assertEquals(503, mixed.statusCode(), "A complete small cohort mixed with ordinary workspaces must fail closed.");
            assertFalse(mixed.body().contains(label));
            String demoExports = "/api/v2/orgs/" + demo.orgPublicId + "/exports";
            var emptyCsv = post(demoActor, demoExports, "{\"format\":\"CSV\",\"dataset\":\"MOVEMENTS\"}");
            assertEquals(200, emptyCsv.statusCode(), emptyCsv.body());
            assertEquals(1, emptyCsv.body().lines().count());
            assertTrue(emptyCsv.body().contains("synthetic,demoDataset,demonstrationAsOf,demoNotice"));
            String resource = GSON.toJson(new ResourceRequest("Fictional disclosure resource", "REUSABLE", 1));
            assertEquals(201, post(demoActor, "/api/resources", resource).statusCode());
            assertEquals(201, post(ordinaryActor, "/api/resources", resource).statusCode());
            String jsonInput = "{\"format\":\"TENANT_JSON\",\"dataset\":\"RESOURCES\"}";
            var labelled = post(demoActor, demoExports, jsonInput);
            assertEquals(200, labelled.statusCode(), labelled.body());
            var disclosure = json(labelled).getAsJsonObject("demoProvenance");
            assertTrue(disclosure.get("synthetic").getAsBoolean());
            assertEquals(label, disclosure.get("datasetId").getAsString());
            assertTrue(disclosure.get("notice").getAsString().contains("operations, dates, accounts, inventories and transaction history are simulated"));
            var csv = post(demoActor, demoExports, "{\"format\":\"CSV\",\"dataset\":\"RESOURCES\"}");
            assertEquals(200, csv.statusCode());
            assertTrue(csv.body().contains("synthetic,demoDataset,demonstrationAsOf,demoNotice"));
            assertTrue(csv.body().contains(label));
            var ordinaryExport = post(ordinaryActor, "/api/v2/orgs/" + ordinary.orgPublicId + "/exports", jsonInput);
            assertEquals(200, ordinaryExport.statusCode());
            assertFalse(json(ordinaryExport).has("demoProvenance"));
            assertEquals(404, post(ordinaryActor, demoExports, jsonInput).statusCode());
        } finally {
            jdbc.update("UPDATE organization SET demo_dataset_id=NULL,demo_reference_key=NULL,demo_source_sha=NULL,"
                    + "demo_plan_sha=NULL,demo_as_of=NULL WHERE demo_dataset_id=?", label);
            jdbc.update("DELETE c FROM local_credential c JOIN user_account u ON u.id=c.user_id WHERE u.username_key='c8-demo-operator'");
            jdbc.update("DELETE FROM user_account WHERE username_key='c8-demo-operator'");
        }
        var recovered = get(newClient(), metadata);
        assertEquals(200, recovered.statusCode());
        assertFalse(json(recovered).getAsJsonObject("data").get("synthetic").getAsBoolean());
    }

    private String leadAssignment(String operationId, UUID leadMembershipId) {
        JsonObject request = new JsonObject();
        request.addProperty("operationId", operationId);
        request.add("leadMembershipId", leadMembershipId == null
                ? com.google.gson.JsonNull.INSTANCE : new com.google.gson.JsonPrimitive(leadMembershipId.toString()));
        return request.toString();
    }

    private void insertWorkspaceResources(long org, int first, int last) {
        var rows = java.util.stream.IntStream.rangeClosed(first, last)
                .mapToObj(index -> new Object[]{org, "Fictional C8 item " + index, "fictional c8 item " + index}).toList();
        jdbc.batchUpdate("INSERT INTO resource(public_id,organization_id,name,name_key,type,total_quantity) "
                + "VALUES(UUID_TO_BIN(UUID(),0),?,?,?,'CONSUMABLE',0)", rows);
    }

    private JsonObject workspacePage(Session actor, String path) throws Exception {
        HttpResponse<String> response = get(actor.client, path);
        assertEquals(200, response.statusCode(), response.body());
        return json(response).getAsJsonObject("data");
    }

    private void readWorkspacePages(Session actor, String path, String cursor, List<Long> items) throws Exception {
        for (int page = 0; page < 20; page++) {
            JsonObject result = workspacePage(actor, cursor == null ? path : path + "&cursor=" + cursor);
            var rows = result.getAsJsonArray("items");
            assertTrue(rows.size() <= 100);
            rows.forEach(item -> items.add(item.getAsJsonObject().get("id").getAsLong()));
            if (result.get("nextCursor").isJsonNull()) return;
            cursor = result.get("nextCursor").getAsString();
        }
        fail("Workspace paging did not reach an explicit end.");
    }

    private LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
    private Set<String> actualSchemaColumns(String table) {
        return Set.copyOf(jdbc.queryForList("SELECT LOWER(column_name) FROM information_schema.columns WHERE table_schema=DATABASE() AND LOWER(table_name)=?", String.class, table));
    }
    private static String loginJson(Fixture f) { return GSON.toJson(new LoginRequest(f.username, f.password)); }

    private static Throwable findCause(Throwable failure, Class<? extends Throwable> type) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) if (type.isInstance(cause)) return cause;
        return null;
    }


    @Test @Order(59)
    void C8AnonymousGateCoversEveryProtectedRegisteredJerseyOperationWithoutBusinessWrites() throws Exception {
        // An isolated HTTP authentication-boundary check. The separately reviewed
        // role/tenant/object/field policies are NOT implied by anonymous 401 results.
        HttpClient anonymous = newClient();
        HttpResponse<String> csrfResponse = get(anonymous, "/api/v2/auth/csrf");
        assertEquals(200, csrfResponse.statusCode());
        String csrf = json(csrfResponse).getAsJsonObject("data").get("csrfToken").getAsString();
        assertFalse(csrf.isBlank());

        Set<String> publicRoutes = Set.of(
                "GET /api/v2/auth/csrf",
                "GET /api/v2/demo/metadata",
                "POST /api/v2/auth/login",
                "POST /api/v2/auth/recover",
                "POST /api/v2/invitations/redeem");
        Set<String> actualRoutes = new java.util.HashSet<>();
        int[] before = new int[] {
            jdbc.queryForObject("SELECT COUNT(*) FROM organization", Integer.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM resource", Integer.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM allocation", Integer.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement", Integer.class)
        };
        int protectedCount = 0;
        for (Class<?> resource : c8JerseyResourceConfig.getClasses()) {
            jakarta.ws.rs.Path root = resource.getAnnotation(jakarta.ws.rs.Path.class);
            if (root == null || !resource.getPackageName().equals("edu.orgresource.extended.rest")) continue;
            for (java.lang.reflect.Method method : resource.getDeclaredMethods()) {
                String verb = null;
                for (java.lang.annotation.Annotation annotation : method.getAnnotations()) {
                    jakarta.ws.rs.HttpMethod http = annotation.annotationType().getAnnotation(jakarta.ws.rs.HttpMethod.class);
                    if (http == null) continue;
                    assertNull(verb, "Unexpected multiple HTTP verbs on " + method);
                    verb = http.value();
                }
                if (verb == null) continue;
                jakarta.ws.rs.Path suffix = method.getAnnotation(jakarta.ws.rs.Path.class);
                String route = "/api" + root.value() + (suffix == null ? "" : suffix.value());
                String signature = verb + " " + route;
                assertTrue(actualRoutes.add(signature), "Duplicate registered operation: " + signature);
                if (publicRoutes.contains(signature)) continue;

                // Exercise actual authenticated filter chain before Jersey argument parsing.
                // Legacy numeric paths use 1; modern opaque IDs use well-formed UUIDs.
                String path = route.replace("{id}", "1")
                        .replaceAll("\\{[^}]+\\}", "00000000-0000-4000-8000-000000000001");
                HttpResponse<String> response;
                if ("GET".equals(verb)) {
                    response = get(anonymous, path);
                } else {
                    assertEquals("POST", verb, "New HTTP verb requires explicit test support: " + signature);
                    HttpRequest request = HttpRequest.newBuilder(URI.create(base() + path))
                            .header("Origin", origin())
                            .header("X-CSRF-TOKEN", csrf)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{}")).build();
                    response = anonymous.send(request, HttpResponse.BodyHandlers.ofString());
                }
                assertEquals(401, response.statusCode(), "Anonymous route " + signature + " returned " + response.body());
                assertEquals("AUTH_REQUIRED", json(response).get("code").getAsString(),
                        "Expected a non-disclosing authenticated-filter denial on " + signature);
                assertFalse(response.body().contains("passwordHash"),
                        "Anonymous refusal must not leak account secrets: " + signature);
                protectedCount++;
            }
        }
        assertEquals(93, actualRoutes.size(), "Audit exactly the registered C8 route surface");
        assertTrue(actualRoutes.containsAll(publicRoutes), "Public route skip set must match actual handlers");
        assertEquals(88, protectedCount, "Every non-public registered endpoint must be denied to anonymous users");
        assertArrayEquals(before, new int[] {
            jdbc.queryForObject("SELECT COUNT(*) FROM organization", Integer.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM resource", Integer.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM allocation", Integer.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement", Integer.class)
        }, "Anonymous route probes must leave business tables unchanged");
    }


    @Test @Order(60)
    void C8GovernanceDenialsLeaveExistingRecordsAndAuditStateUnchanged() throws Exception {
        // Every refusal below targets an existing fictional membership/invitation with a
        // correct anti-CSRF token. Responses alone would not prove transaction safety.
        Fixture owner = newTenant("C8-governance-denial", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Fixture stranger = newTenant("C8-governance-stranger", Membership.Role.OWNER);
        Session ownerSession = login(owner.username, owner.password);
        Session adminSession = login(admin.username, admin.password);
        Session memberSession = login(member.username, member.password);
        Session outsiderSession = login(stranger.username, stranger.password);
        String root = "/api/v2/orgs/" + owner.orgPublicId;
        HttpResponse<String> invitation = post(ownerSession, root + "/invitations",
                GSON.toJson(new RoleRequest("MEMBER")));
        assertEquals(201, invitation.statusCode(), invitation.body());
        UUID invitationId = UUID.fromString(json(invitation).getAsJsonObject("data").get("id").getAsString());

        long initialInvitations = jdbc.queryForObject("SELECT COUNT(*) FROM invitation WHERE org_id=?",
                Long.class, owner.orgDbId);
        long initialTransfers = jdbc.queryForObject("SELECT COUNT(*) FROM ownership_transfer WHERE org_id=?",
                Long.class, owner.orgDbId);
        long initialAudits = jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE org_id=?",
                Long.class, owner.orgDbId);
        Membership membership = memberships.findById(member.membershipDbId).orElseThrow();
        long memberVersion = membership.version;
        var invitationBefore = invitations.findByOrganizationIdAndPublicId(owner.orgDbId, invitationId).orElseThrow();
        long invitationVersion = invitationBefore.version;
        assertEquals(Invitation.Status.INVITED, invitationBefore.status);

        assertEquals(403, post(memberSession, root + "/invitations",
                GSON.toJson(new RoleRequest("MEMBER"))).statusCode(),
                "Non-managers cannot grant invitations");
        assertEquals(404, post(outsiderSession, root + "/invitations/" + invitationId + "/revoke",
                "", "\"v0\"").statusCode(), "A foreign organization cannot revoke an existing invitation");
        assertEquals(403, post(memberSession, root + "/invitations/" + invitationId + "/revoke",
                "", "\"v0\"").statusCode(), "Members cannot revoke active invitations");
        String memberRolePath = root + "/members/" + member.membershipPublicId + "/role";
        assertEquals(403, post(adminSession, memberRolePath,
                GSON.toJson(new RoleRequest("ADMIN")), "\"v0\"").statusCode(),
                "An administrator cannot elevate a member to administrator");
        assertEquals(404, post(outsiderSession, memberRolePath,
                GSON.toJson(new RoleRequest("CUSTODIAN")), "\"v0\"").statusCode(),
                "Foreign members have no role administration rights");
        assertEquals(412, post(ownerSession, memberRolePath,
                GSON.toJson(new RoleRequest("LEAD")), "\"v999\"").statusCode(),
                "A stale If-Match must not modify a valid target");
        assertEquals(403, post(memberSession, root + "/members/" + owner.membershipPublicId + "/suspend",
                "", "\"v0\"").statusCode(),
                "A member cannot suspend the owner");
        assertEquals(403, post(memberSession, root + "/transfer-ownership",
                GSON.toJson(new TransferRequest(admin.membershipPublicId, member.password))).statusCode(),
                "Only an owner may initiate an ownership transfer");

        Membership memberAfter = memberships.findById(member.membershipDbId).orElseThrow();
        assertEquals(Membership.Role.MEMBER, memberAfter.role);
        assertEquals(Membership.Status.ACTIVE, memberAfter.status);
        assertEquals(memberVersion, memberAfter.version);
        Invitation invitationAfter = invitations.findByOrganizationIdAndPublicId(owner.orgDbId, invitationId).orElseThrow();
        assertEquals(Invitation.Status.INVITED, invitationAfter.status);
        assertEquals(invitationVersion, invitationAfter.version);
        assertEquals(initialInvitations, jdbc.queryForObject(
                "SELECT COUNT(*) FROM invitation WHERE org_id=?", Long.class, owner.orgDbId));
        assertEquals(initialTransfers, jdbc.queryForObject(
                "SELECT COUNT(*) FROM ownership_transfer WHERE org_id=?", Long.class, owner.orgDbId));
        assertEquals(initialAudits, jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE org_id=?", Long.class, owner.orgDbId));
        assertEquals(1, memberships.countEffectiveActiveOwners(owner.orgDbId));
    }


    @Test @Order(61)
    void C8LegacyWorkspaceBaselineReadsAndV2DepartmentRoleBoundaryHaveSqlEvidence() throws Exception {
        // This tests selected-workspace baseline reads separately from privileged
        // legacy writes and from the assigned-Project /api/v2/orgs/{orgId} routes.
        // Every denial below uses an actual same-org target and a valid session/CSRF.
        Fixture owner = newTenant("C8-last29-owner", Membership.Role.OWNER);
        Fixture outsider = newTenant("C8-last29-foreign", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(owner.orgDbId, Membership.Role.ADMIN);
        Fixture custodian = fixtureInOrg(owner.orgDbId, Membership.Role.CUSTODIAN);
        Fixture lead = fixtureInOrg(owner.orgDbId, Membership.Role.LEAD);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Session ownerSession = login(owner.username, owner.password);
        Session outsiderSession = login(outsider.username, outsider.password);
        Session adminSession = login(admin.username, admin.password);
        Session custodianSession = login(custodian.username, custodian.password);
        Session leadSession = login(lead.username, lead.password);
        Session memberSession = login(member.username, member.password);

        HttpResponse<String> departmentCreated = post(ownerSession, "/api/v2/departments",
                GSON.toJson(new NameRequest("C8 last29 home Department")));
        assertEquals(201, departmentCreated.statusCode(), departmentCreated.body());
        long departmentId = json(departmentCreated).get("id").getAsLong();
        long projectId = json(post(ownerSession, "/api/projects",
                GSON.toJson(new ProjectRequest(departmentId, "C8 last29 home Project",
                        "2040-12-10T10:00:00", "2040-12-10T11:00:00")))).get("id").getAsLong();
        long resourceId = json(post(ownerSession, "/api/resources",
                GSON.toJson(new ResourceRequest("C8 last29 home Resource", "REUSABLE", 9)))).get("id").getAsLong();
        long requirementId = json(post(ownerSession, "/api/requirements",
                GSON.toJson(new RequirementRequest(projectId, resourceId, 4)))).get("id").getAsLong();
        long allocationId = json(post(ownerSession, "/api/allocations",
                GSON.toJson(new AllocationRequest(requirementId, 1, "Fictional C8 last29 borrower", null, false))))
                        .get("id").getAsLong();
        long outsiderDepartmentId = json(post(outsiderSession, "/api/departments",
                GSON.toJson(new NameRequest("C8 last29 foreign Department")))).get("id").getAsLong();
        long outsiderResourceId = json(post(outsiderSession, "/api/resources",
                GSON.toJson(new ResourceRequest("C8 last29 foreign Resource", "REUSABLE", 0))))
                        .get("id").getAsLong();

        // Only authenticated sessions may use this legacy CSRF alias. The
        // intentionally public pre-auth token has a separate /auth/csrf route.
        assertEquals(401, get(newClient(), "/api/v2/csrf").statusCode());
        HttpResponse<String> selectedCsrf = get(ownerSession.client, "/api/v2/csrf");
        assertEquals(200, selectedCsrf.statusCode(), selectedCsrf.body());
        assertFalse(json(selectedCsrf).get("token").getAsString().isBlank());

        // OWNER, ADMIN, CUSTODIAN, LEAD and MEMBER may read the selected-org
        // baseline directory. These are not assigned-Project detail grants.
        String[][] expectedPages = {
                {"departments", Long.toString(departmentId)},
                {"projects", Long.toString(projectId)},
                {"resources", Long.toString(resourceId)},
                {"requirements", Long.toString(requirementId)}
        };
        for (Session actor : List.of(ownerSession, adminSession, custodianSession, leadSession, memberSession)) {
            HttpResponse<String> legacyDepartment = get(actor.client, "/api/v2/departments/" + departmentId);
            assertEquals(200, legacyDepartment.statusCode(), legacyDepartment.body());
            assertEquals(departmentId, json(legacyDepartment).get("id").getAsLong());
            for (String[] page : expectedPages) {
                JsonObject view = workspacePage(actor, "/api/v2/workspace/" + page[0] + "?limit=50");
                long expectedId = Long.parseLong(page[1]);
                assertTrue(view.getAsJsonArray("items").asList().stream()
                                .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == expectedId),
                        "Selected-org " + page[0] + " must contain the approved fixture for this active role");
                assertFalse(view.getAsJsonArray("items").asList().stream().anyMatch(item ->
                        item.getAsJsonObject().toString().contains("C8 last29 foreign")),
                        "Selected-org " + page[0] + " must not leak a foreign workspace label");
            }
        }
        assertEquals(404, get(outsiderSession.client, "/api/v2/departments/" + departmentId).statusCode());
        assertEquals(404, get(ownerSession.client, "/api/v2/departments/" + outsiderDepartmentId).statusCode());
        JsonObject foreignResources = workspacePage(outsiderSession, "/api/v2/workspace/resources?limit=50");
        assertTrue(foreignResources.getAsJsonArray("items").asList().stream()
                .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == outsiderResourceId));
        assertFalse(foreignResources.getAsJsonArray("items").asList().stream()
                .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == resourceId));

        // Global Allocation browsing is privileged even while the baseline
        // Project/Resource/Requirement summaries remain visible to active members.
        assertEquals(403, get(memberSession.client, "/api/v2/workspace/allocations?limit=50").statusCode());
        assertEquals(403, get(leadSession.client, "/api/v2/workspace/allocations?limit=50").statusCode());
        for (Session officer : List.of(ownerSession, adminSession, custodianSession)) {
            JsonObject allocationsPage = workspacePage(officer, "/api/v2/workspace/allocations?limit=50");
            assertTrue(allocationsPage.getAsJsonArray("items").asList().stream()
                    .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == allocationId));
        }
        for (Session lowRole : List.of(memberSession, leadSession)) {
            JsonObject overview = workspacePage(lowRole, "/api/v2/workspace/overview");
            assertTrue(overview.get("allocations").isJsonNull(),
                    "MEMBER/LEAD overview must not disclose general borrower activity");
            assertFalse(overview.toString().contains("Fictional C8 last29 borrower"));
        }

        // Snapshot current tenant data AFTER login and authorized setup, then
        // prove lower roles do not mutate a real Department, Project, Requirement
        // or append-only stock/audit history.
        long[] before = new long[] {
            jdbc.queryForObject("SELECT COUNT(*) FROM department WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM project WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM resource_requirement WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE organization_id=?", Long.class, owner.orgDbId)
        };
        long[] stockFacts = stockFactCounts(owner.orgDbId);
        for (Session lowRole : List.of(memberSession, leadSession, custodianSession)) {
            assertEquals(403, post(lowRole, "/api/v2/departments",
                    GSON.toJson(new NameRequest("C8 forbidden Department"))).statusCode());
            assertEquals(403, post(lowRole, "/api/departments/" + departmentId + "/update",
                    GSON.toJson(new NameRequest("C8 forbidden Rename"))).statusCode());
            assertEquals(403, post(lowRole, "/api/projects/" + projectId + "/delete", null).statusCode());
            assertEquals(403, post(lowRole, "/api/requirements/" + requirementId + "/delete", null).statusCode());
        }
        assertEquals(403, post(adminSession, "/api/resources",
                GSON.toJson(new ResourceRequest("C8 forbidden Admin Stock", "CONSUMABLE", 0))).statusCode());
        assertEquals(403, post(memberSession, "/api/resources/" + resourceId + "/update",
                GSON.toJson(new ResourceUpdateRequest("C8 forbidden Resource Rename", 10, 9))).statusCode());

        assertArrayEquals(before, new long[] {
            jdbc.queryForObject("SELECT COUNT(*) FROM department WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM project WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM resource_requirement WHERE organization_id=?", Long.class, owner.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE organization_id=?", Long.class, owner.orgDbId)
        }, "Denied legacy and V2 mutations must not insert or remove tenant business rows");
        assertArrayEquals(stockFacts, stockFactCounts(owner.orgDbId),
                "Denied mutations must not create movements, receipts or audit events");
        assertEquals("C8 last29 home Department", jdbc.queryForObject(
                "SELECT name FROM department WHERE id=? AND organization_id=?", String.class, departmentId, owner.orgDbId));
        assertEquals("C8 last29 home Project", jdbc.queryForObject(
                "SELECT name FROM project WHERE id=? AND organization_id=?", String.class, projectId, owner.orgDbId));
        assertEquals(9, jdbc.queryForObject("SELECT total_quantity FROM resource WHERE id=? AND organization_id=?",
                Integer.class, resourceId, owner.orgDbId));

        // Distinguish two independent grants with real authorized writes.
        assertEquals(201, post(adminSession, "/api/v2/departments",
                GSON.toJson(new NameRequest("C8 allowed Admin Department"))).statusCode(),
                "ADMIN is a Department manager");
        assertEquals(201, post(custodianSession, "/api/resources",
                GSON.toJson(new ResourceRequest("C8 allowed Custodian Resource", "CONSUMABLE", 0))).statusCode(),
                "CUSTODIAN may create stock, while ADMIN may not");
    }


    @Test @Order(62)
    void C8SelectedWorkspaceSwitchDemotionAndSuspensionRecheckMembershipPerRequest() throws Exception {
        // Reuse ONE authenticated browser session across two real fictional
        // organizations. An old tenant cursor and an old privileged role must
        // never survive a selected-organization switch or a role change.
        Fixture home = newTenant("C8-switch-home", Membership.Role.OWNER);
        Fixture away = newTenant("C8-switch-away", Membership.Role.OWNER);
        Fixture actor = fixtureInOrg(home.orgDbId, Membership.Role.CUSTODIAN);
        addMembership(away.orgDbId, actor.userDbId, Membership.Role.MEMBER);
        Session homeOwner = login(home.username, home.password);
        Session awayOwner = login(away.username, away.password);

        long homeDepartment = json(post(homeOwner, "/api/departments",
                GSON.toJson(new NameRequest("Fictional switch home Department")))).get("id").getAsLong();
        long homeProject = json(post(homeOwner, "/api/projects", GSON.toJson(new ProjectRequest(
                homeDepartment, "Fictional switch home Project", "2040-12-11T10:00:00",
                "2040-12-11T14:00:00")))).get("id").getAsLong();
        long homeResource = json(post(homeOwner, "/api/resources", GSON.toJson(new ResourceRequest(
                "Fictional switch home Resource A", "REUSABLE", 4)))).get("id").getAsLong();
        json(post(homeOwner, "/api/resources", GSON.toJson(new ResourceRequest(
                "Fictional switch home Resource B", "CONSUMABLE", 0))));
        long homeRequirement = json(post(homeOwner, "/api/requirements", GSON.toJson(
                new RequirementRequest(homeProject, homeResource, 2)))).get("id").getAsLong();
        long homeAllocation = json(post(homeOwner, "/api/allocations", GSON.toJson(
                new AllocationRequest(homeRequirement, 1, "Fictional switch borrower", null, false))))
                .get("id").getAsLong();
        long awayResource = json(post(awayOwner, "/api/resources", GSON.toJson(new ResourceRequest(
                "Fictional switch away Resource", "REUSABLE", 0)))).get("id").getAsLong();

        Session live = login(actor.username, actor.password);
        assertEquals(200, post(live, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(home.orgPublicId))).statusCode());
        refreshCsrf(live);
        String pagePath = "/api/v2/workspace/resources?limit=1";
        JsonObject first = workspacePage(live, pagePath);
        assertEquals(1, first.getAsJsonArray("items").size());
        assertFalse(first.get("nextCursor").isJsonNull(), "Two resources require a continuation cursor");
        String homeCursor = first.get("nextCursor").getAsString();
        JsonObject homeAllocations = workspacePage(live, "/api/v2/workspace/allocations?limit=10");
        assertTrue(homeAllocations.getAsJsonArray("items").asList().stream()
                .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == homeAllocation));
        assertFalse(workspacePage(live, "/api/v2/workspace/overview").get("allocations").isJsonNull());

        // Same browser, different selected org: the home cursor and original
        // home resource must not produce any away-organization data.
        assertEquals(200, post(live, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(away.orgPublicId))).statusCode());
        refreshCsrf(live);
        assertEquals(404, get(live.client, pagePath + "&cursor=" + homeCursor).statusCode(),
                "Cursor must be bound to the organization that issued it");
        JsonObject awayPage = workspacePage(live, "/api/v2/workspace/resources?limit=10");
        assertTrue(awayPage.getAsJsonArray("items").asList().stream()
                .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == awayResource));
        assertFalse(awayPage.getAsJsonArray("items").asList().stream()
                .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == homeResource));
        assertEquals(404, get(live.client, "/api/v2/departments/" + homeDepartment).statusCode());
        assertEquals(403, get(live.client, "/api/v2/workspace/allocations?limit=10").statusCode());
        assertTrue(workspacePage(live, "/api/v2/workspace/overview").get("allocations").isJsonNull());

        // Switching back restores legitimate home reads, never remembered
        // CUSTODIAN capabilities after the OWNER changes the DB membership.
        assertEquals(200, post(live, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(home.orgPublicId))).statusCode());
        refreshCsrf(live);
        assertEquals(200, get(live.client, pagePath + "&cursor=" + homeCursor).statusCode());
        assertEquals(200, get(live.client, "/api/v2/workspace/allocations?limit=10").statusCode());
        long[] beforeStockFacts = stockFactCounts(home.orgDbId);
        int resourceCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM resource WHERE organization_id=?", Integer.class, home.orgDbId);
        int allocationCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM allocation WHERE organization_id=?", Integer.class, home.orgDbId);
        String memberPath = "/api/v2/orgs/" + home.orgPublicId + "/members/" + actor.membershipPublicId;
        HttpResponse<String> demotion = post(homeOwner, memberPath + "/role",
                GSON.toJson(new RoleRequest("MEMBER")), "\"v0\"");
        assertEquals(200, demotion.statusCode(), demotion.body());

        assertEquals(403, get(live.client, "/api/v2/workspace/allocations?limit=10").statusCode(),
                "Same-session membership role must be reloaded from MySQL");
        assertTrue(workspacePage(live, "/api/v2/workspace/overview").get("allocations").isJsonNull());
        assertEquals(200, get(live.client, "/api/v2/workspace/resources?limit=10").statusCode(),
                "Demotion to MEMBER preserves approved baseline browsing");
        assertEquals(403, post(live, "/api/resources", GSON.toJson(
                new ResourceRequest("Fictional forbidden stale Custodian", "CONSUMABLE", 0))).statusCode());
        assertEquals(resourceCount, jdbc.queryForObject(
                "SELECT COUNT(*) FROM resource WHERE organization_id=?", Integer.class, home.orgDbId));
        assertEquals(allocationCount, jdbc.queryForObject(
                "SELECT COUNT(*) FROM allocation WHERE organization_id=?", Integer.class, home.orgDbId));
        long[] afterDemotion = stockFactCounts(home.orgDbId);
        assertEquals(beforeStockFacts[0], afterDemotion[0], "No phantom inventory movement");
        assertEquals(beforeStockFacts[1], afterDemotion[1], "No denied-write idempotency receipt");

        HttpResponse<String> suspension = post(homeOwner, memberPath + "/suspend", null, "\"v1\"");
        assertEquals(200, suspension.statusCode(), suspension.body());
        for (String protectedRead : List.of(
                "/api/departments",
                "/api/resources",
                "/api/v2/departments/" + homeDepartment,
                "/api/v2/workspace/overview",
                "/api/v2/workspace/departments?limit=10",
                "/api/v2/workspace/projects?limit=10",
                "/api/v2/workspace/resources?limit=10",
                "/api/v2/workspace/requirements?limit=10",
                "/api/v2/workspace/allocations?limit=10")) {
            assertEquals(404, get(live.client, protectedRead).statusCode(),
                    "Suspended selected-org membership must lose access immediately: " + protectedRead);
        }
        assertEquals(resourceCount, jdbc.queryForObject(
                "SELECT COUNT(*) FROM resource WHERE organization_id=?", Integer.class, home.orgDbId));
        assertEquals(allocationCount, jdbc.queryForObject(
                "SELECT COUNT(*) FROM allocation WHERE organization_id=?", Integer.class, home.orgDbId));
        long[] afterSuspension = stockFactCounts(home.orgDbId);
        assertEquals(beforeStockFacts[0], afterSuspension[0]);
        assertEquals(beforeStockFacts[1], afterSuspension[1]);
        assertEquals(beforeStockFacts[2] + 2, afterSuspension[2],
                "Only the authorized role-change and suspension events add governance audit rows");

        // Account remains valid in its other ACTIVE organization; suspension
        // is scoped to the specific membership, not an accidental global ban.
        assertEquals(200, post(live, "/api/v2/session/active-org",
                GSON.toJson(new OrgRequest(away.orgPublicId))).statusCode());
        refreshCsrf(live);
        assertTrue(workspacePage(live, "/api/v2/workspace/resources?limit=10")
                .getAsJsonArray("items").asList().stream()
                .anyMatch(item -> item.getAsJsonObject().get("id").getAsLong() == awayResource));
        assertEquals(403, get(live.client, "/api/v2/workspace/allocations?limit=10").statusCode());
    }


    @Test @Order(63)
    void C8EveryRegisteredPostEnforcesSameOriginAndCsrfBeforeExecutingAnyBusinessHandler() throws Exception {
        // This is a single shared middleware factor across every registered POST.
        // It does NOT establish per-operation authorized behavior, object scope,
        // or role/field policy; those remain separate acceptance obligations.
        Fixture owner = newTenant("C8-post-middleware", Membership.Role.OWNER);
        Session authenticated = login(owner.username, owner.password);
        assertFalse(authenticated.csrf.isBlank());

        // No mutations may occur from requests that fail before Jersey dispatch.
        // Session last-access bookkeeping is intentionally outside this snapshot.
        String[] tables = {
                "organization", "user_account", "membership", "invitation",
                "ownership_transfer", "department", "project", "resource",
                "resource_requirement", "allocation", "stock_movement",
                "idempotency_key", "audit_event", "account_recovery_code"
        };
        long[] before = new long[tables.length];
        for (int i = 0; i < tables.length; i++) {
            assertTrue(tables[i].matches("[a-z_]+"));
            before[i] = jdbc.queryForObject("SELECT COUNT(*) FROM " + tables[i], Long.class);
        }

        Set<String> registered = new java.util.HashSet<>();
        int protectedPosts = 0;
        for (Class<?> resource : c8JerseyResourceConfig.getClasses()) {
            jakarta.ws.rs.Path root = resource.getAnnotation(jakarta.ws.rs.Path.class);
            if (root == null || !resource.getPackageName().equals("edu.orgresource.extended.rest")) continue;
            for (java.lang.reflect.Method method : resource.getDeclaredMethods()) {
                String verb = null;
                for (java.lang.annotation.Annotation annotation : method.getAnnotations()) {
                    jakarta.ws.rs.HttpMethod http = annotation.annotationType().getAnnotation(jakarta.ws.rs.HttpMethod.class);
                    if (http == null) continue;
                    assertNull(verb, "A handler must declare only one HTTP verb: " + method);
                    verb = http.value();
                }
                if (verb == null) continue;
                jakarta.ws.rs.Path suffix = method.getAnnotation(jakarta.ws.rs.Path.class);
                String route = "/api" + root.value() + (suffix == null ? "" : suffix.value());
                String signature = verb + " " + route;
                assertTrue(registered.add(signature), "Duplicate Jersey registration: " + signature);
                if (!"POST".equals(verb)) continue;

                // Real syntactically well-formed path values reach the same
                // Spring filters regardless of whether the object exists.
                String path = route.replace("{id}", "1")
                        .replaceAll("\\{[^}]+\\}", "00000000-0000-4000-8000-000000000001");
                URI uri = URI.create(base() + path);
                String body = "{}";

                // No Origin, valid CSRF: the same-origin filter must block.
                HttpRequest missingOrigin = HttpRequest.newBuilder(uri)
                        .header("X-CSRF-TOKEN", authenticated.csrf)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> missingOriginResponse = authenticated.client.send(
                        missingOrigin, HttpResponse.BodyHandlers.ofString());
                assertEquals(403, missingOriginResponse.statusCode(),
                        "Missing Origin bypassed " + signature + ": " + missingOriginResponse.body());
                assertEquals("ORIGIN_INVALID", json(missingOriginResponse).get("code").getAsString(),
                        "Missing Origin must fail in the early filter for " + signature);

                // Hostile Origin, valid CSRF: accepted cookies alone are insufficient.
                HttpRequest hostileOrigin = HttpRequest.newBuilder(uri)
                        .header("Origin", "https://untrusted.example.invalid")
                        .header("X-CSRF-TOKEN", authenticated.csrf)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> hostileOriginResponse = authenticated.client.send(
                        hostileOrigin, HttpResponse.BodyHandlers.ofString());
                assertEquals(403, hostileOriginResponse.statusCode(),
                        "Cross-origin POST bypassed " + signature + ": " + hostileOriginResponse.body());
                assertEquals("ORIGIN_INVALID", json(hostileOriginResponse).get("code").getAsString());

                // Valid Origin, missing CSRF: security must block before any
                // request body validation or business-object lookup.
                HttpRequest missingCsrf = HttpRequest.newBuilder(uri)
                        .header("Origin", origin())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> missingCsrfResponse = authenticated.client.send(
                        missingCsrf, HttpResponse.BodyHandlers.ofString());
                assertEquals(403, missingCsrfResponse.statusCode(),
                        "Missing CSRF bypassed " + signature + ": " + missingCsrfResponse.body());
                assertEquals("FORBIDDEN", json(missingCsrfResponse).get("code").getAsString(),
                        "CSRF error must be controlled for " + signature);
                for (HttpResponse<String> denied : List.of(
                        missingOriginResponse, hostileOriginResponse, missingCsrfResponse)) {
                    assertTrue(denied.headers().firstValue("Cache-Control").orElse("").contains("no-store"),
                            "Denied POST must be non-cacheable: " + signature);
                    assertFalse(denied.headers().firstValue("X-Request-ID").orElse("").isBlank(),
                            "Every denial must include its trace request ID: " + signature);
                    assertFalse(denied.body().contains(owner.password),
                            "Denial must not disclose fixture credential: " + signature);
                }
                protectedPosts++;
            }
        }

        // A drifted HTTP operation inventory is a review blocker; the static
        // checked-in policy inventory independently expects these 93 signatures.
        assertEquals(93, registered.size(), "Test all source-registered Jersey routes");
        assertEquals(55, protectedPosts, "Expected all 55 declared POST operations, including public login/recovery/redeem");
        long[] after = new long[tables.length];
        for (int i = 0; i < tables.length; i++) {
            after[i] = jdbc.queryForObject("SELECT COUNT(*) FROM " + tables[i], Long.class);
        }
        assertArrayEquals(before, after,
                "All 165 blocked POST requests must leave domain, stock, identity and audit tables unchanged");
    }


    @Test @Order(64)
    void C8ProjectWorkRealForeignObjectsAndReadOnlyRolesCannotChangeSqlState() throws Exception {
        // Routed ProjectWork authorization evidence: use real, existing objects
        // in two unrelated fictional organizations, not random missing UUIDs.
        Fixture home = newTenant("C8-work-denied-home", Membership.Role.OWNER);
        Fixture away = newTenant("C8-work-denied-away", Membership.Role.OWNER);
        Fixture custodian = fixtureInOrg(home.orgDbId, Membership.Role.CUSTODIAN);
        Fixture member = fixtureInOrg(home.orgDbId, Membership.Role.MEMBER);
        Fixture assigned = fixtureInOrg(home.orgDbId, Membership.Role.LEAD);
        Fixture unassigned = fixtureInOrg(home.orgDbId, Membership.Role.LEAD);
        Session homeOwner = login(home.username, home.password);
        Session awayOwner = login(away.username, away.password);
        Session observer = login(custodian.username, custodian.password);
        Session basic = login(member.username, member.password);
        Session lead = login(assigned.username, assigned.password);
        Session otherLead = login(unassigned.username, unassigned.password);
        String homeRoot = "/api/v2/orgs/" + home.orgPublicId;
        String awayRoot = "/api/v2/orgs/" + away.orgPublicId;
        String start = "2040-11-13T08:00:00", end = "2040-11-13T12:00:00";
        long homeDepartment = json(post(homeOwner, "/api/departments",
                GSON.toJson(new NameRequest("Fictional ProjectWork home")))).get("id").getAsLong();
        long awayDepartment = json(post(awayOwner, "/api/departments",
                GSON.toJson(new NameRequest("Fictional ProjectWork away")))).get("id").getAsLong();
        long homeProjectDb = json(post(homeOwner, "/api/projects",
                GSON.toJson(new ProjectRequest(homeDepartment, "Fictional work home", start, end))))
                .get("id").getAsLong();
        long awayProjectDb = json(post(awayOwner, "/api/projects",
                GSON.toJson(new ProjectRequest(awayDepartment, "Fictional work away", start, end))))
                .get("id").getAsLong();
        UUID homeProject = projectRepository.findById(homeProjectDb).orElseThrow().publicId;
        UUID awayProject = projectRepository.findById(awayProjectDb).orElseThrow().publicId;
        long homeResourceDb = json(post(homeOwner, "/api/resources",
                GSON.toJson(new ResourceRequest("Fictional work resource home", "REUSABLE", 10))))
                .get("id").getAsLong();
        long awayResourceDb = json(post(awayOwner, "/api/resources",
                GSON.toJson(new ResourceRequest("Fictional work resource away", "REUSABLE", 10))))
                .get("id").getAsLong();
        UUID homeResource = resourceRepository.findById(homeResourceDb).orElseThrow().publicId;
        UUID awayResource = resourceRepository.findById(awayResourceDb).orElseThrow().publicId;
        String projectFields = GSON.toJson(new ProjectUpdateRequest("Fictional unauthorized rename", start, end));
        String homeProjectPath = homeRoot + "/projects/" + homeProject;
        String awayProjectPathFromHome = homeRoot + "/projects/" + awayProject;
        String homeRequirements = homeRoot + "/requirements";
        String homeCreate = GSON.toJson(java.util.Map.of(
                "projectId", homeProject, "resourceId", homeResource, "quantity", 6));
        String awayCreate = GSON.toJson(java.util.Map.of(
                "projectId", awayProject, "resourceId", awayResource, "quantity", 6));
        var homeCreated = post(homeOwner, homeRequirements, homeCreate);
        var awayCreated = post(awayOwner, awayRoot + "/requirements", awayCreate);
        assertEquals(201, homeCreated.statusCode(), homeCreated.body());
        assertEquals(201, awayCreated.statusCode(), awayCreated.body());
        UUID homeRequirement = UUID.fromString(json(homeCreated).getAsJsonObject("data").get("id").getAsString());
        UUID awayRequirement = UUID.fromString(json(awayCreated).getAsJsonObject("data").get("id").getAsString());
        String homeItem = homeRequirements + "/" + homeRequirement;
        String awayItemFromHome = homeRequirements + "/" + awayRequirement;

        assertEquals(200, modernPost(homeOwner, homeProjectPath + "/lead-assignment",
                leadAssignment(UUID.randomUUID().toString(), assigned.membershipPublicId),
                "\"v0\"", UUID.randomUUID().toString()).statusCode());
        // Current, authorized reads prove the populated targets exist.
        assertEquals(200, get(homeOwner.client, homeRoot + "/projects").statusCode());
        assertEquals(200, get(observer.client, homeRoot + "/projects").statusCode());
        assertEquals(200, get(observer.client, homeRequirements).statusCode());
        assertEquals(200, get(observer.client, homeItem).statusCode());
        assertEquals(200, get(lead.client, homeItem).statusCode());
        assertEquals("\"v0\"", get(lead.client, homeItem).headers().firstValue("ETag").orElseThrow());
        assertEquals(200, get(awayOwner.client, awayRoot + "/requirements/" + awayRequirement).statusCode());

        // MEMBER has no ProjectWork read grant; unassigned LEAD sees only an
        // empty page, never another lead's existing Project or Requirement.
        assertEquals(403, get(basic.client, homeRoot + "/projects").statusCode());
        assertEquals(403, get(basic.client, homeRequirements).statusCode());
        assertEquals(403, get(basic.client, homeItem).statusCode());
        assertTrue(json(get(otherLead.client, homeRoot + "/projects"))
                .getAsJsonObject("data").getAsJsonArray("items").isEmpty());
        assertTrue(json(get(otherLead.client, homeRequirements))
                .getAsJsonObject("data").getAsJsonArray("items").isEmpty());
        assertEquals(404, get(otherLead.client, homeItem).statusCode());
        assertEquals(404, get(awayOwner.client, homeRoot + "/projects").statusCode());
        assertEquals(404, get(awayOwner.client, homeRequirements).statusCode());
        assertEquals(404, get(awayOwner.client, homeItem).statusCode());
        assertEquals(404, get(homeOwner.client, awayItemFromHome).statusCode());

        // Snapshot *actual* pre-existing Project and Requirement values, not
        // just aggregate counts. Include both tenants and non-business effects.
        String beforeProjects = jdbc.queryForList(
                "SELECT id, name, version, lead_membership_id FROM project WHERE organization_id IN (?,?) ORDER BY id",
                home.orgDbId, away.orgDbId).toString();
        String beforeRequirements = jdbc.queryForList(
                "SELECT id, organization_id, project_id, resource_id, quantity, version "
                + "FROM resource_requirement WHERE organization_id IN (?,?) ORDER BY id",
                home.orgDbId, away.orgDbId).toString();
        long[] beforeEffects = new long[] {
            jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE organization_id IN (?,?)",
                    Long.class, home.orgDbId, away.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id IN (?,?)",
                    Long.class, home.orgDbId, away.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key", Long.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE org_id IN (?,?)",
                    Long.class, home.orgDbId, away.orgDbId)
        };

        // CUSTODIAN can read but never update/delete a Project or Requirement.
        assertEquals(403, post(observer, homeProjectPath + "/update", projectFields, "\"v1\"").statusCode());
        assertEquals(403, post(observer, homeProjectPath + "/delete", null, "\"v1\"").statusCode());
        assertEquals(403, post(observer, homeRequirements, homeCreate).statusCode());
        assertEquals(403, post(observer, homeItem + "/update", "{\"quantity\":5}", "\"v0\"").statusCode());
        assertEquals(403, post(observer, homeItem + "/delete", null, "\"v0\"").statusCode());

        // An unassigned LEAD does not receive a write grant over existing work.
        assertEquals(404, post(otherLead, homeProjectPath + "/update", projectFields, "\"v1\"").statusCode());
        assertEquals(404, post(otherLead, homeProjectPath + "/delete", null, "\"v1\"").statusCode());
        assertEquals(404, post(otherLead, homeRequirements, homeCreate).statusCode());
        assertEquals(404, post(otherLead, homeItem + "/update", "{\"quantity\":5}", "\"v0\"").statusCode());
        assertEquals(404, post(otherLead, homeItem + "/delete", null, "\"v0\"").statusCode());

        // A legitimate OWNER still cannot use an existing foreign Project,
        // Resource, or Requirement by substituting its UUID under their own org.
        assertEquals(404, post(homeOwner, awayProjectPathFromHome + "/update", projectFields, "\"v0\"").statusCode());
        assertEquals(404, post(homeOwner, awayProjectPathFromHome + "/delete", null, "\"v0\"").statusCode());
        assertEquals(404, post(homeOwner, homeRequirements, GSON.toJson(java.util.Map.of(
                "projectId", homeProject, "resourceId", awayResource, "quantity", 6))).statusCode());
        assertEquals(404, post(homeOwner, homeRequirements, GSON.toJson(java.util.Map.of(
                "projectId", awayProject, "resourceId", homeResource, "quantity", 6))).statusCode());
        assertEquals(404, post(homeOwner, awayItemFromHome + "/update", "{\"quantity\":5}", "\"v0\"").statusCode());
        assertEquals(404, post(homeOwner, awayItemFromHome + "/delete", null, "\"v0\"").statusCode());

        // Version guards reject stale/missing preconditions before changing SQL.
        assertEquals(428, post(homeOwner, homeProjectPath + "/update", projectFields).statusCode());
        assertEquals(412, post(homeOwner, homeProjectPath + "/update", projectFields, "\"v99\"").statusCode());
        assertEquals(428, post(homeOwner, homeItem + "/update", "{\"quantity\":5}").statusCode());
        assertEquals(412, post(homeOwner, homeItem + "/update", "{\"quantity\":5}", "\"v99\"").statusCode());
        assertEquals(428, post(homeOwner, homeItem + "/delete", null).statusCode());
        assertEquals(412, post(homeOwner, homeItem + "/delete", null, "\"v99\"").statusCode());

        assertEquals(beforeProjects, jdbc.queryForList(
                "SELECT id, name, version, lead_membership_id FROM project WHERE organization_id IN (?,?) ORDER BY id",
                home.orgDbId, away.orgDbId).toString());
        assertEquals(beforeRequirements, jdbc.queryForList(
                "SELECT id, organization_id, project_id, resource_id, quantity, version "
                + "FROM resource_requirement WHERE organization_id IN (?,?) ORDER BY id",
                home.orgDbId, away.orgDbId).toString());
        assertArrayEquals(beforeEffects, new long[] {
            jdbc.queryForObject("SELECT COUNT(*) FROM allocation WHERE organization_id IN (?,?)",
                    Long.class, home.orgDbId, away.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM stock_movement WHERE organization_id IN (?,?)",
                    Long.class, home.orgDbId, away.orgDbId),
            jdbc.queryForObject("SELECT COUNT(*) FROM idempotency_key", Long.class),
            jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE org_id IN (?,?)",
                    Long.class, home.orgDbId, away.orgDbId)
        }, "All refused ProjectWork writes must leave allocations, movements, receipts and audit untouched");
    }


    @Test @Order(65)
    void C8LeadOptionsFiltersActualMembershipsAndBindsDirectoryCursorsWithoutSqlWrites() throws Exception {
        // A previously uncovered handler: /projects/{projectId}/lead-options.
        // Lead assignment tests do not prove this separate directory endpoint.
        Fixture home = newTenant("C8-lead-options", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(home.orgDbId, Membership.Role.ADMIN);
        Fixture custodian = fixtureInOrg(home.orgDbId, Membership.Role.CUSTODIAN);
        Fixture leadOne = fixtureInOrg(home.orgDbId, Membership.Role.LEAD);
        Fixture leadTwo = fixtureInOrg(home.orgDbId, Membership.Role.LEAD);
        Fixture suspendedLead = fixtureInOrg(home.orgDbId, Membership.Role.LEAD);
        Fixture member = fixtureInOrg(home.orgDbId, Membership.Role.MEMBER);
        Fixture foreign = newTenant("C8-lead-options-foreign", Membership.Role.OWNER);
        Session owner = login(home.username, home.password);
        Session officer = login(admin.username, admin.password);
        Session stock = login(custodian.username, custodian.password);
        Session lead = login(leadOne.username, leadOne.password);
        Session ordinary = login(member.username, member.password);
        Session outsider = login(foreign.username, foreign.password);

        long department = json(post(owner, "/api/departments",
                GSON.toJson(new NameRequest("Fictional directory department")))).get("id").getAsLong();
        String from = "2040-12-01T10:00:00", until = "2040-12-01T12:00:00";
        long projectDb = json(post(owner, "/api/projects", GSON.toJson(
                new ProjectRequest(department, "Fictional candidate project", from, until))))
                .get("id").getAsLong();
        long anotherProjectDb = json(post(owner, "/api/projects", GSON.toJson(
                new ProjectRequest(department, "Fictional different project", from, until))))
                .get("id").getAsLong();
        long foreignDepartment = json(post(outsider, "/api/departments",
                GSON.toJson(new NameRequest("Fictional foreign directory department")))).get("id").getAsLong();
        long foreignProjectDb = json(post(outsider, "/api/projects", GSON.toJson(
                new ProjectRequest(foreignDepartment, "Fictional foreign project", from, until))))
                .get("id").getAsLong();
        UUID project = projectRepository.findById(projectDb).orElseThrow().publicId;
        UUID anotherProject = projectRepository.findById(anotherProjectDb).orElseThrow().publicId;
        UUID foreignProject = projectRepository.findById(foreignProjectDb).orElseThrow().publicId;
        String root = "/api/v2/orgs/" + home.orgPublicId;
        String directory = root + "/projects/" + project + "/lead-options";
        String otherDirectory = root + "/projects/" + anotherProject + "/lead-options";

        // This authorized governance mutation precedes the read-only SQL
        // snapshot; a suspended LEAD is ineligible for option discovery.
        assertEquals(200, post(owner, root + "/members/" + suspendedLead.membershipPublicId
                + "/suspend", "{}", "\"v0\"").statusCode());
        String membersBefore = jdbc.queryForList(
                "SELECT id, role, status, version FROM membership WHERE org_id=? ORDER BY id",
                home.orgDbId).toString();
        String projectsBefore = jdbc.queryForList(
                "SELECT id, name, version, lead_membership_id FROM project WHERE organization_id=? ORDER BY id",
                home.orgDbId).toString();
        long[] homeBefore = stockFactCounts(home.orgDbId), foreignBefore = stockFactCounts(foreign.orgDbId);

        var response = get(owner.client, directory);
        assertEquals(200, response.statusCode(), response.body());
        var items = json(response).getAsJsonObject("data").getAsJsonArray("items");
        assertEquals(2, items.size(), "Only two currently active LEAD memberships are eligible.");
        var found = new java.util.HashSet<String>();
        for (var item : items) {
            var option = item.getAsJsonObject();
            assertEquals(Set.of("membershipId", "displayName"), option.keySet(),
                    "Lead options must disclose no account, contact, role or provider fields.");
            found.add(option.get("membershipId").getAsString());
        }
        assertEquals(Set.of(leadOne.membershipPublicId.toString(), leadTwo.membershipPublicId.toString()), found);
        assertFalse(response.body().contains(suspendedLead.membershipPublicId.toString()));
        assertFalse(response.body().contains(foreign.membershipPublicId.toString()));
        assertFalse(response.body().matches("(?is).*(email|phone|ciphertext|provider|accountId).*"));

        assertEquals(200, get(officer.client, directory).statusCode(), "ADMIN is an authorized reader.");
        assertEquals(403, get(stock.client, directory).statusCode(), "CUSTODIAN cannot view lead options.");
        assertEquals(403, get(lead.client, directory).statusCode(), "LEAD cannot view lead options.");
        assertEquals(403, get(ordinary.client, directory).statusCode(), "MEMBER cannot view lead options.");
        assertEquals(404, get(outsider.client, directory).statusCode(), "No foreign organization membership.");
        assertEquals(404, get(owner.client, root + "/projects/" + foreignProject + "/lead-options").statusCode(),
                "An actual foreign Project UUID cannot be resolved under home path.");

        var first = json(get(owner.client, directory + "?limit=1")).getAsJsonObject("data");
        assertEquals(1, first.getAsJsonArray("items").size());
        String cursor = first.get("nextCursor").getAsString();
        var second = json(get(owner.client, directory + "?limit=1&cursor=" + cursor))
                .getAsJsonObject("data");
        assertEquals(1, second.getAsJsonArray("items").size());
        assertTrue(second.get("nextCursor").isJsonNull());
        assertNotEquals(first.getAsJsonArray("items").get(0).getAsJsonObject().get("membershipId"),
                second.getAsJsonArray("items").get(0).getAsJsonObject().get("membershipId"));
        assertEquals(404, get(owner.client, otherDirectory + "?limit=1&cursor=" + cursor).statusCode(),
                "Cursor must be bound to the specific Project even in the same organization.");
        assertEquals(404, get(owner.client, directory + "?limit=1&search=unrelated&cursor=" + cursor).statusCode(),
                "Cursor must be bound to the precise directory search.");
        assertEquals(400, get(owner.client, directory + "?limit=101").statusCode());
        assertEquals(400, get(owner.client, directory + "?cursor=%21invalid").statusCode());
        assertTrue(json(get(owner.client, directory + "?search=no-matching-fixture-name"))
                .getAsJsonObject("data").getAsJsonArray("items").isEmpty());

        assertEquals(membersBefore, jdbc.queryForList(
                "SELECT id, role, status, version FROM membership WHERE org_id=? ORDER BY id",
                home.orgDbId).toString());
        assertEquals(projectsBefore, jdbc.queryForList(
                "SELECT id, name, version, lead_membership_id FROM project WHERE organization_id=? ORDER BY id",
                home.orgDbId).toString());
        assertArrayEquals(homeBefore, stockFactCounts(home.orgDbId));
        assertArrayEquals(foreignBefore, stockFactCounts(foreign.orgDbId));
    }

    @Test @Order(66)
    void C8IdentityAccountDisableWrongPasswordAndSuccessfulNonOwnerAreAtomic() throws Exception {
        // A deterministic positive and negative for R029. The pre-existing
        // concurrent ownership-transfer case #12 allows a legitimate 409 and
        // therefore does not guarantee a 204 account-disable receipt.
        Fixture owner = newTenant("C8-disable-owner", Membership.Role.OWNER);
        Fixture member = fixtureInOrg(owner.orgDbId, Membership.Role.MEMBER);
        Session session = login(member.username, member.password);
        long ownerCount = memberships.countEffectiveActiveOwners(owner.orgDbId);
        long version = accounts.findById(member.userDbId).orElseThrow().credentialVersion;
        String originalHash = credentials.findByUserId(member.userDbId).orElseThrow().passwordHash;
        long audits = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE actor_user_id=? AND action='ACCOUNT_DISABLED'",
                Long.class, member.userDbId);
        assertEquals(0L, audits, "A fresh fictional account starts without disable audits.");

        HttpResponse<String> denied = post(session, "/api/v2/account/disable",
                GSON.toJson(new CurrentPasswordRequest("deliberately-wrong-password")));
        assertEquals(401, denied.statusCode(), denied.body());
        assertEquals("CURRENT_PASSWORD_INVALID", json(denied).get("code").getAsString());
        assertEquals(UserAccount.Status.ACTIVE, accounts.findById(member.userDbId).orElseThrow().status);
        assertEquals(version, accounts.findById(member.userDbId).orElseThrow().credentialVersion);
        assertEquals(originalHash, credentials.findByUserId(member.userDbId).orElseThrow().passwordHash);
        assertEquals(audits, jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE actor_user_id=? AND action='ACCOUNT_DISABLED'",
                Long.class, member.userDbId));
        assertEquals(ownerCount, memberships.countEffectiveActiveOwners(owner.orgDbId));
        assertEquals(200, get(session.client, "/api/v2/session").statusCode(),
                "Wrong-password denial must not revoke the valid session.");

        HttpResponse<String> completed = post(session, "/api/v2/account/disable",
                GSON.toJson(new CurrentPasswordRequest(member.password)));
        assertEquals(204, completed.statusCode(), completed.body());
        assertTrue(completed.body().isBlank(), "Account disable must not return private account data.");
        assertEquals(UserAccount.Status.DISABLED, accounts.findById(member.userDbId).orElseThrow().status);
        assertEquals(version + 1, accounts.findById(member.userDbId).orElseThrow().credentialVersion);
        assertEquals(originalHash, credentials.findByUserId(member.userDbId).orElseThrow().passwordHash,
                "Disabling the account must not silently rewrite its credential.");
        assertEquals(audits + 1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE actor_user_id=? AND action='ACCOUNT_DISABLED'",
                Long.class, member.userDbId));
        assertEquals(ownerCount, memberships.countEffectiveActiveOwners(owner.orgDbId));
        assertEquals(401, get(session.client, "/api/v2/session").statusCode());
        assertEquals(401, loginStatus(member.username, member.password));
    }

    @Test @Order(67)
    void C8R031RecoveryRotationDenialPreservesCodesAndAuthorizedRotationIsNoStore() throws Exception {
        Fixture account = newTenant("C8-R031", Membership.Role.OWNER);
        Session session = login(account.username, account.password);
        String endpoint = "/api/v2/account/recovery-codes/rotate";
        String snapshotSql = "SELECT id, HEX(code_hash) AS digest, HEX(batch_id) AS batch, created_at, expires_at, used_at, revoked_at "
                + "FROM account_recovery_code WHERE user_id=? ORDER BY id";
        var before = jdbc.queryForList(snapshotSql, account.userDbId);
        assertEquals(5, before.size());
        String auditSql = "SELECT COUNT(*) FROM audit_event WHERE actor_user_id=? AND action='RECOVERY_CODES_ROTATED'";
        long audits = jdbc.queryForObject(auditSql, Long.class, account.userDbId);
        long version = accounts.findById(account.userDbId).orElseThrow().credentialVersion;
        HttpResponse<String> denied = post(session, endpoint,
                GSON.toJson(new CurrentPasswordRequest("deliberately-wrong-password")));
        assertEquals(401, denied.statusCode());
        assertEquals("CURRENT_PASSWORD_INVALID", json(denied).get("code").getAsString());
        assertFalse(json(denied).has("data"));
        assertTrue(before.equals(jdbc.queryForList(snapshotSql, account.userDbId)), "Denied rotation changed recovery rows.");
        assertEquals(audits, jdbc.queryForObject(auditSql, Long.class, account.userDbId));
        assertEquals(version, accounts.findById(account.userDbId).orElseThrow().credentialVersion);
        assertEquals(200, get(session.client, "/api/v2/session").statusCode());
        HttpResponse<String> rotated = post(session, endpoint,
                GSON.toJson(new CurrentPasswordRequest(account.password)));
        assertEquals(200, rotated.statusCode());
        assertTrue(rotated.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        var data = json(rotated).getAsJsonObject("data");
        assertEquals(Set.of("codes"), data.keySet());
        var codes = data.getAsJsonArray("codes");
        assertEquals(5, codes.size());
        Set<String> unique = new java.util.HashSet<>();
        for (var item : codes) {
            String raw = item.getAsString();
            assertTrue(unique.add(raw), "Duplicate recovery code.");
            assertFalse(account.recoveryCodes.contains(raw), "Reused previous recovery code.");
            assertEquals(1L, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND code_hash=? AND used_at IS NULL AND revoked_at IS NULL",
                    Long.class, account.userDbId, C1Secrets.sha256(raw)));
        }
        assertEquals(10L, jdbc.queryForObject("SELECT COUNT(*) FROM account_recovery_code WHERE user_id=?", Long.class, account.userDbId));
        assertEquals(5L, jdbc.queryForObject("SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND revoked_at IS NOT NULL", Long.class, account.userDbId));
        for (String old : account.recoveryCodes)
            assertEquals(1L, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM account_recovery_code WHERE user_id=? AND code_hash=? AND revoked_at IS NOT NULL",
                    Long.class, account.userDbId, C1Secrets.sha256(old)));
        assertEquals(audits + 1, jdbc.queryForObject(auditSql, Long.class, account.userDbId));
        assertEquals(version, accounts.findById(account.userDbId).orElseThrow().credentialVersion);
        assertEquals(200, get(session.client, "/api/v2/session").statusCode());
        // Never print raw recovery values or hashes in assertions or receipts.
    }

    @Test @Order(68)
    void C8R082MemberDirectoryAdminForeignCursorFieldAndReadOnlyBoundaries() throws Exception {
        Fixture home = newTenant("C8-R082-home", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(home.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(home.orgDbId, Membership.Role.MEMBER);
        Fixture foreign = newTenant("C8-R082-foreign", Membership.Role.OWNER);
        Session ownerSession = login(home.username, home.password);
        Session adminSession = login(admin.username, admin.password);
        Session memberSession = login(member.username, member.password);
        Session foreignSession = login(foreign.username, foreign.password);
        String endpoint = "/api/v2/orgs/" + home.orgPublicId + "/members";
        String rows = "SELECT id, org_id, user_id, role, status, version FROM membership WHERE org_id IN (?,?) ORDER BY id";
        var before = jdbc.queryForList(rows, home.orgDbId, foreign.orgDbId);
        String auditSql = "SELECT COUNT(*) FROM audit_event WHERE org_id IN (?,?)";
        long audits = jdbc.queryForObject(auditSql, Long.class, home.orgDbId, foreign.orgDbId);
        HttpResponse<String> page = get(adminSession.client, endpoint + "?limit=100");
        assertEquals(200, page.statusCode());
        assertTrue(page.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        var data = json(page).getAsJsonObject("data");
        assertEquals(Set.of("items", "nextCursor"), data.keySet());
        assertEquals(3, data.getAsJsonArray("items").size());
        assertTrue(data.get("nextCursor").isJsonNull());
        Set<String> ids = new java.util.HashSet<>();
        for (var item : data.getAsJsonArray("items")) {
            var row = item.getAsJsonObject();
            assertEquals(Set.of("id", "displayName", "role", "status", "version"), row.keySet());
            ids.add(row.get("id").getAsString());
        }
        assertEquals(Set.of(home.membershipPublicId.toString(), admin.membershipPublicId.toString(),
                member.membershipPublicId.toString()), ids);
        var ownerPage = json(get(ownerSession.client, endpoint + "?limit=100")).getAsJsonObject("data");
        assertEquals(data, ownerPage);
        var first = json(get(adminSession.client, endpoint + "?limit=1")).getAsJsonObject("data");
        assertEquals(1, first.getAsJsonArray("items").size());
        assertFalse(first.get("nextCursor").isJsonNull());
        assertEquals(first, json(get(adminSession.client, endpoint + "?limit=0")).getAsJsonObject("data"));
        assertEquals(data, json(get(adminSession.client, endpoint + "?limit=101")).getAsJsonObject("data"));
        assertEquals(404, get(adminSession.client, endpoint + "?cursor=" + foreign.membershipPublicId).statusCode());
        assertEquals(403, get(memberSession.client, endpoint).statusCode());
        assertEquals(404, get(foreignSession.client, endpoint).statusCode());
        assertTrue(before.equals(jdbc.queryForList(rows, home.orgDbId, foreign.orgDbId)), "Directory reads changed memberships.");
        assertEquals(audits, jdbc.queryForObject(auditSql, Long.class, home.orgDbId, foreign.orgDbId));
        // Small-page clamp compatibility is covered, but >100-row truncation
        // and complete cross-table read-only proof are separate evidence gaps.
    }

    @Test @Order(69)
    void C8R078InvitationRevocationChecksVersionRoleExpiryAndExactRows() throws Exception {
        Fixture home = newTenant("C8-R078-home", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(home.orgDbId, Membership.Role.ADMIN);
        Session owner = login(home.username, home.password);
        Session officer = login(admin.username, admin.password);
        String root = "/api/v2/orgs/" + home.orgPublicId;
        HttpResponse<String> created = post(owner, root + "/invitations", GSON.toJson(new RoleRequest("MEMBER")));
        assertEquals(201, created.statusCode());
        var view = json(created).getAsJsonObject("data");
        UUID id = UUID.fromString(view.get("id").getAsString());
        String tag = created.headers().firstValue("ETag").orElseThrow();
        String revoke = root + "/invitations/" + id + "/revoke";
        String sql = "SELECT id,org_id,role,status,HEX(token_hash) AS digest,version,created_at,updated_at,expires_at,accepted_by_user_id FROM invitation WHERE org_id=? ORDER BY id";
        String audit = "SELECT COUNT(*) FROM audit_event WHERE org_id=?";
        var before = jdbc.queryForList(sql, home.orgDbId);
        long auditBefore = jdbc.queryForObject(audit, Long.class, home.orgDbId);
        LocalDateTime storedExpiry = invitations.findByOrganizationIdAndPublicId(home.orgDbId, id).orElseThrow().expiresAt;
        assertEquals(428, post(owner, revoke, "").statusCode());
        assertEquals(400, post(owner, revoke, "", "W/\"v0\"").statusCode());
        assertEquals(412, post(owner, revoke, "", "\"v999\"").statusCode());
        assertTrue(before.equals(jdbc.queryForList(sql, home.orgDbId)), "Denied revocation changed invitation rows.");
        assertEquals(auditBefore, jdbc.queryForObject(audit, Long.class, home.orgDbId));
        HttpResponse<String> revoked = post(officer, revoke, "", tag);
        assertEquals(200, revoked.statusCode());
        var result = json(revoked).getAsJsonObject("data");
        assertEquals(id.toString(), result.get("id").getAsString());
        assertEquals(home.orgPublicId.toString(), result.get("organizationId").getAsString());
        assertEquals("MEMBER", result.get("role").getAsString());
        assertEquals("REVOKED", result.get("status").getAsString());
        assertTrue(!result.has("bearerToken") || result.get("bearerToken").isJsonNull());
        // Creation returns the managed entity's nanosecond timestamp; MySQL
        // DATETIME(6) stores microseconds. Compare persisted expiration exactly
        // and bound the creation/storage discrepancy to sub-microsecond rounding.
        LocalDateTime createdExpiry = LocalDateTime.parse(view.get("expiresAt").getAsString());
        assertTrue(java.time.Duration.between(createdExpiry, storedExpiry).abs().toNanos() < 1000,
                "Creation expiration differs from stored expiration by at least one microsecond.");
        assertEquals(storedExpiry, LocalDateTime.parse(result.get("expiresAt").getAsString()));
        assertEquals(storedExpiry, invitations.findByOrganizationIdAndPublicId(home.orgDbId, id).orElseThrow().expiresAt);
        assertEquals(view.get("version").getAsLong() + 1, result.get("version").getAsLong());
        String next = revoked.headers().firstValue("ETag").orElseThrow();
        assertEquals("\"v" + result.get("version").getAsLong() + "\"", next);
        assertEquals(Invitation.Status.REVOKED, invitations.findByOrganizationIdAndPublicId(home.orgDbId, id).orElseThrow().status);
        assertEquals(auditBefore + 1, jdbc.queryForObject(audit, Long.class, home.orgDbId));
        before = jdbc.queryForList(sql, home.orgDbId);
        auditBefore = jdbc.queryForObject(audit, Long.class, home.orgDbId);
        assertEquals(409, post(owner, revoke, "", next).statusCode());
        assertTrue(before.equals(jdbc.queryForList(sql, home.orgDbId)));
        assertEquals(auditBefore, jdbc.queryForObject(audit, Long.class, home.orgDbId));
        HttpResponse<String> adminInvite = post(owner, root + "/invitations", GSON.toJson(new RoleRequest("ADMIN")));
        assertEquals(201, adminInvite.statusCode());
        String adminRevoke = root + "/invitations/" + json(adminInvite).getAsJsonObject("data").get("id").getAsString() + "/revoke";
        before = jdbc.queryForList(sql, home.orgDbId);
        auditBefore = jdbc.queryForObject(audit, Long.class, home.orgDbId);
        assertEquals(403, post(officer, adminRevoke, "", adminInvite.headers().firstValue("ETag").orElseThrow()).statusCode());
        assertTrue(before.equals(jdbc.queryForList(sql, home.orgDbId)));
        assertEquals(auditBefore, jdbc.queryForObject(audit, Long.class, home.orgDbId));
    }

    @Test @Order(70)
    void C8R041ExpiredInvitationCannotRevokeOrRedeemBeforeWorkerRuns() throws Exception {
        Fixture home = newTenant("C8-R041-home", Membership.Role.OWNER);
        Fixture outsider = newTenant("C8-R041-outsider", Membership.Role.OWNER);
        Session owner = login(home.username, home.password);
        Session foreign = login(outsider.username, outsider.password);
        String root = "/api/v2/orgs/" + home.orgPublicId;
        HttpResponse<String> issued = post(owner, root + "/invitations", GSON.toJson(new RoleRequest("LEAD")));
        assertEquals(201, issued.statusCode());
        var view = json(issued).getAsJsonObject("data");
        UUID id = UUID.fromString(view.get("id").getAsString());
        String token = view.get("bearerToken").getAsString();
        Invitation row = invitations.findByOrganizationIdAndPublicId(home.orgDbId, id).orElseThrow();
        // Existing acceptance fixture technique, confined to this fresh fictional invitation.
        LocalDateTime past = now().minusMinutes(1);
        assertEquals(1, jdbc.update("UPDATE invitation SET created_at=?,expires_at=? WHERE id=?",
                Timestamp.valueOf(past.minusMinutes(1)), Timestamp.valueOf(past), row.id));
        String sql = "SELECT id,status,version,created_at,updated_at,expires_at,accepted_by_user_id,HEX(token_hash) AS digest FROM invitation WHERE id=?";
        var before = jdbc.queryForList(sql, row.id);
        long members = memberships.count();
        long users = accounts.count();
        long codes = recoveryCodes.count();
        long audits = jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE org_id=?", Long.class, home.orgDbId);
        assertEquals(Invitation.Status.INVITED, invitations.findById(row.id).orElseThrow().status);
        assertEquals(409, post(owner, root + "/invitations/" + id + "/revoke", "",
                issued.headers().firstValue("ETag").orElseThrow()).statusCode());
        HttpResponse<String> denied = post(foreign, "/api/v2/invitations/redeem", GSON.toJson(new TokenRequest(token)));
        assertEquals(404, denied.statusCode());
        assertEquals("INVITE_INVALID", json(denied).get("code").getAsString());
        assertFalse(json(denied).has("data"));
        HttpClient anonymous = newClient();
        String csrf = json(get(anonymous, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        String username = "expired-" + UUID.randomUUID();
        HttpResponse<String> newAccount = post(anonymous, "/api/v2/invitations/redeem",
                GSON.toJson(new RedeemRequest(token, username, "Fictional-Password-" + UUID.randomUUID(), "Expired invitee")), csrf, origin());
        assertEquals(404, newAccount.statusCode());
        assertEquals("INVITE_INVALID", json(newAccount).get("code").getAsString());
        assertEquals(401, get(anonymous, "/api/v2/session").statusCode());
        assertEquals(200, get(foreign.client, "/api/v2/session").statusCode());
        assertTrue(accounts.findByUsernameKey(username).isEmpty());
        assertEquals(members, memberships.count());
        assertEquals(users, accounts.count());
        assertEquals(codes, recoveryCodes.count());
        assertEquals(audits, jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE org_id=?", Long.class, home.orgDbId));
        assertTrue(before.equals(jdbc.queryForList(sql, row.id)), "Expired invitation denial changed the row.");
        // No raw invitation token in output or assertion messages.
    }

    @Test @Order(71)
    void C8R083R084TransferStartAndRevokePreconditionsPreserveOwnership() throws Exception {
        Fixture home = newTenant("C8-transfer-home", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(home.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(home.orgDbId, Membership.Role.MEMBER);
        Fixture other = newTenant("C8-transfer-other", Membership.Role.OWNER);
        Fixture otherAdmin = fixtureInOrg(other.orgDbId, Membership.Role.ADMIN);
        Session owner = login(home.username, home.password);
        Session foreign = login(other.username, other.password);
        String root = "/api/v2/orgs/" + home.orgPublicId;
        String start = root + "/transfer-ownership";
        String rows = "SELECT id,org_id,from_membership_id,to_membership_id,status,version,created_at,expires_at,accepted_at FROM ownership_transfer WHERE org_id IN (?,?) ORDER BY id";
        String roles = "SELECT id,org_id,user_id,role,status,version FROM membership WHERE org_id IN (?,?) ORDER BY id";
        String audit = "SELECT COUNT(*) FROM audit_event WHERE org_id IN (?,?)";
        var before = jdbc.queryForList(rows, home.orgDbId, other.orgDbId);
        var rolesBefore = jdbc.queryForList(roles, home.orgDbId, other.orgDbId);
        long auditBefore = jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId);
        assertEquals(401, post(owner, start, GSON.toJson(new TransferRequest(admin.membershipPublicId, "wrong-password"))).statusCode());
        assertEquals(409, post(owner, start, GSON.toJson(new TransferRequest(member.membershipPublicId, home.password))).statusCode());
        assertEquals(404, post(owner, start, GSON.toJson(new TransferRequest(otherAdmin.membershipPublicId, home.password))).statusCode());
        assertTrue(before.equals(jdbc.queryForList(rows, home.orgDbId, other.orgDbId)));
        assertTrue(rolesBefore.equals(jdbc.queryForList(roles, home.orgDbId, other.orgDbId)));
        assertEquals(auditBefore, jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId));
        HttpResponse<String> issued = post(owner, start, GSON.toJson(new TransferRequest(admin.membershipPublicId, home.password)));
        assertEquals(201, issued.statusCode());
        var view = json(issued).getAsJsonObject("data");
        assertEquals("PENDING", view.get("status").getAsString());
        assertEquals(home.orgPublicId.toString(), view.get("organizationId").getAsString());
        assertEquals(home.membershipPublicId.toString(), view.get("fromMembershipId").getAsString());
        assertEquals(admin.membershipPublicId.toString(), view.get("toMembershipId").getAsString());
        assertTrue(!view.has("acceptedAt") || view.get("acceptedAt").isJsonNull());
        String tag = issued.headers().firstValue("ETag").orElseThrow();
        assertEquals("\"v" + view.get("version").getAsLong() + "\"", tag);
        assertEquals(auditBefore + 1, jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId));
        assertTrue(rolesBefore.equals(jdbc.queryForList(roles, home.orgDbId, other.orgDbId)));
        String id = view.get("id").getAsString();
        String revoke = root + "/transfers/" + id + "/revoke";
        before = jdbc.queryForList(rows, home.orgDbId, other.orgDbId);
        auditBefore = jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId);
        assertEquals(428, post(owner, revoke, "").statusCode());
        assertEquals(400, post(owner, revoke, "", "W/\"v0\"").statusCode());
        assertEquals(412, post(owner, revoke, "", "\"v999\"").statusCode());
        assertEquals(404, post(foreign, "/api/v2/orgs/" + other.orgPublicId + "/transfers/" + id + "/revoke", "", tag).statusCode());
        assertTrue(before.equals(jdbc.queryForList(rows, home.orgDbId, other.orgDbId)));
        assertEquals(auditBefore, jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId));
        HttpResponse<String> revoked = post(owner, revoke, "", tag);
        assertEquals(200, revoked.statusCode());
        var result = json(revoked).getAsJsonObject("data");
        assertEquals("REVOKED", result.get("status").getAsString());
        assertEquals(id, result.get("id").getAsString());
        assertEquals(view.get("version").getAsLong() + 1, result.get("version").getAsLong());
        String current = revoked.headers().firstValue("ETag").orElseThrow();
        assertEquals("\"v" + result.get("version").getAsLong() + "\"", current);
        assertEquals(OwnershipTransfer.Status.REVOKED, transfers.findByOrganizationIdAndPublicId(home.orgDbId, UUID.fromString(id)).orElseThrow().status);
        assertEquals(auditBefore + 1, jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId));
        before = jdbc.queryForList(rows, home.orgDbId, other.orgDbId);
        auditBefore = jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId);
        assertEquals(409, post(owner, revoke, "", current).statusCode());
        assertTrue(before.equals(jdbc.queryForList(rows, home.orgDbId, other.orgDbId)));
        assertEquals(auditBefore, jdbc.queryForObject(audit, Long.class, home.orgDbId, other.orgDbId));
        assertTrue(rolesBefore.equals(jdbc.queryForList(roles, home.orgDbId, other.orgDbId)));
        assertEquals(1, memberships.countEffectiveActiveOwners(home.orgDbId));
        assertEquals(1, memberships.countEffectiveActiveOwners(other.orgDbId));
    }

    @Test @Order(72)
    void C8R089R091DepartmentAndProjectCursorsBindToWorkspaceAndQuery() throws Exception {
        Fixture home = newTenant("C8-pages-directed-home", Membership.Role.OWNER);
        Fixture other = newTenant("C8-pages-directed-other", Membership.Role.OWNER);
        Fixture member = fixtureInOrg(home.orgDbId, Membership.Role.MEMBER);
        Session owner = login(home.username, home.password);
        Session outsider = login(other.username, other.password);
        Session reader = login(member.username, member.password);
        List<Long> departmentIds = new ArrayList<>();
        for (String name : List.of("Directed Alpha", "Directed Beta", "Separate Gamma")) {
            HttpResponse<String> created = post(owner, "/api/departments", GSON.toJson(new NameRequest(name)));
            assertEquals(201, created.statusCode());
            departmentIds.add(json(created).get("id").getAsLong());
        }
        List<Long> projectIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            HttpResponse<String> created = post(owner, "/api/projects", GSON.toJson(new ProjectRequest(
                    departmentIds.get(i == 2 ? 1 : 0), "Directed Project " + i,
                    "2040-12-10T10:00:00", "2040-12-10T11:00:00")));
            assertEquals(201, created.statusCode());
            projectIds.add(json(created).get("id").getAsLong());
        }
        HttpResponse<String> foreignDepartment = post(outsider, "/api/departments", GSON.toJson(new NameRequest("Directed Foreign")));
        assertEquals(201, foreignDepartment.statusCode());
        long foreignId = json(foreignDepartment).get("id").getAsLong();
        assertEquals(201, post(outsider, "/api/projects", GSON.toJson(new ProjectRequest(foreignId,
                "Directed Foreign Project", "2040-12-10T10:00:00", "2040-12-10T11:00:00"))).statusCode());

        String deptSql = "SELECT id,organization_id,name,name_key,version FROM department WHERE organization_id IN (?,?) ORDER BY id";
        String projectSql = "SELECT id,organization_id,department_id,name,HEX(public_id) AS public_id,version,start_at,end_at,lead_membership_id FROM project WHERE organization_id IN (?,?) ORDER BY id";
        var deptBefore = jdbc.queryForList(deptSql, home.orgDbId, other.orgDbId);
        var projectBefore = jdbc.queryForList(projectSql, home.orgDbId, other.orgDbId);
        long[] homeBefore = stockFactCounts(home.orgDbId), otherBefore = stockFactCounts(other.orgDbId);
        String departments = "/api/v2/workspace/departments";
        String projects = "/api/v2/workspace/projects";

        var first = workspacePage(reader, departments + "?search=directed&limit=1");
        assertEquals(Set.of("items", "nextCursor"), first.keySet());
        assertEquals(1, first.getAsJsonArray("items").size());
        var firstDept = first.getAsJsonArray("items").get(0).getAsJsonObject();
        assertEquals(Set.of("id", "name"), firstDept.keySet());
        assertEquals(departmentIds.get(0).longValue(), firstDept.get("id").getAsLong());
        String dc = first.get("nextCursor").getAsString();
        var second = workspacePage(reader, departments + "?search=DIRECTED&limit=1&cursor=" + dc);
        assertEquals(1, second.getAsJsonArray("items").size());
        assertEquals(departmentIds.get(1).longValue(), second.getAsJsonArray("items").get(0).getAsJsonObject().get("id").getAsLong());
        assertTrue(second.get("nextCursor").isJsonNull());
        assertEquals(404, get(reader.client, departments + "?search=separate&cursor=" + dc).statusCode());
        assertEquals(404, get(outsider.client, departments + "?search=directed&cursor=" + dc).statusCode());
        assertEquals(400, get(reader.client, departments + "?limit=0").statusCode());
        assertEquals(400, get(reader.client, departments + "?limit=101").statusCode());
        var allDepartments = workspacePage(reader, departments + "?limit=100");
        assertEquals(departmentIds, allDepartments.getAsJsonArray("items").asList().stream()
                .map(x -> x.getAsJsonObject().get("id").getAsLong()).toList());
        assertTrue(allDepartments.get("nextCursor").isJsonNull());

        String projectQuery = projects + "?departmentId=" + departmentIds.get(0) + "&period=UPCOMING&search=directed&limit=1";
        var firstProjectPage = workspacePage(reader, projectQuery);
        assertEquals(Set.of("items", "nextCursor"), firstProjectPage.keySet());
        assertEquals(1, firstProjectPage.getAsJsonArray("items").size());
        var firstProject = firstProjectPage.getAsJsonArray("items").get(0).getAsJsonObject();
        assertEquals(Set.of("id", "departmentId", "name", "startAt", "endAt", "departmentName"), firstProject.keySet());
        assertEquals(projectIds.get(0).longValue(), firstProject.get("id").getAsLong());
        assertEquals("Directed Alpha", firstProject.get("departmentName").getAsString());
        String pc = firstProjectPage.get("nextCursor").getAsString();
        var secondProjectPage = workspacePage(reader, projectQuery + "&cursor=" + pc);
        assertEquals(1, secondProjectPage.getAsJsonArray("items").size());
        assertEquals(projectIds.get(1).longValue(), secondProjectPage.getAsJsonArray("items").get(0).getAsJsonObject().get("id").getAsLong());
        assertTrue(secondProjectPage.get("nextCursor").isJsonNull());
        assertEquals(404, get(reader.client, projects + "?departmentId=" + departmentIds.get(1) + "&period=UPCOMING&search=directed&cursor=" + pc).statusCode());
        assertEquals(404, get(reader.client, projects + "?departmentId=" + departmentIds.get(0) + "&period=ACTIVE&search=directed&cursor=" + pc).statusCode());
        assertEquals(404, get(reader.client, projects + "?departmentId=" + departmentIds.get(0) + "&period=UPCOMING&search=other&cursor=" + pc).statusCode());
        assertEquals(404, get(outsider.client, projects + "?departmentId=" + foreignId + "&period=UPCOMING&search=directed&cursor=" + pc).statusCode());
        assertEquals(404, get(reader.client, departments + "?cursor=" + pc).statusCode());
        assertEquals(400, get(reader.client, projects + "?limit=101").statusCode());
        var allProjects = workspacePage(reader, projects + "?limit=100");
        assertEquals(projectIds, allProjects.getAsJsonArray("items").asList().stream()
                .map(x -> x.getAsJsonObject().get("id").getAsLong()).toList());
        assertTrue(allProjects.get("nextCursor").isJsonNull());
        assertTrue(deptBefore.equals(jdbc.queryForList(deptSql, home.orgDbId, other.orgDbId)));
        assertTrue(projectBefore.equals(jdbc.queryForList(projectSql, home.orgDbId, other.orgDbId)));
        assertArrayEquals(homeBefore, stockFactCounts(home.orgDbId));
        assertArrayEquals(otherBefore, stockFactCounts(other.orgDbId));
    }

    @Test @Order(73)
    void C8R017AdminProjectCreationValidatesTimeAndPreservesRowsOnRefusal() throws Exception {
        Fixture home = newTenant("C8-project-create-home", Membership.Role.OWNER);
        Fixture other = newTenant("C8-project-create-other", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(home.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(home.orgDbId, Membership.Role.MEMBER);
        Session owner = login(home.username, home.password);
        Session outsider = login(other.username, other.password);
        Session officer = login(admin.username, admin.password);
        Session reader = login(member.username, member.password);
        HttpResponse<String> department = post(owner, "/api/departments", GSON.toJson(new NameRequest("Project fixture department")));
        assertEquals(201, department.statusCode());
        long departmentId = json(department).get("id").getAsLong();
        HttpResponse<String> foreign = post(outsider, "/api/departments", GSON.toJson(new NameRequest("Foreign fixture department")));
        assertEquals(201, foreign.statusCode());
        long foreignId = json(foreign).get("id").getAsLong();
        String sql = "SELECT id,organization_id,department_id,name,HEX(public_id) AS public_id,version,start_at,end_at,lead_membership_id FROM project WHERE organization_id IN (?,?) ORDER BY id";
        var before = jdbc.queryForList(sql, home.orgDbId, other.orgDbId);
        long[] homeFacts = stockFactCounts(home.orgDbId), otherFacts = stockFactCounts(other.orgDbId);
        String start = "2040-12-10T10:00:00", end = "2040-12-10T11:00:00";
        for (ProjectRequest invalid : List.of(
                new ProjectRequest(departmentId, "Malformed start", "not-a-date", end),
                new ProjectRequest(departmentId, "Malformed end", start, "not-a-date"),
                new ProjectRequest(departmentId, "Equal time", start, start),
                new ProjectRequest(departmentId, "Reverse time", end, start),
                new ProjectRequest(departmentId, "Past start", "2000-01-01T10:00:00", end))) {
            HttpResponse<String> rejected = post(officer, "/api/projects", GSON.toJson(invalid));
            assertEquals(400, rejected.statusCode());
            assertEquals("VALIDATION_FAILED", json(rejected).get("code").getAsString());
            assertEquals(before, jdbc.queryForList(sql, home.orgDbId, other.orgDbId));
            assertArrayEquals(homeFacts, stockFactCounts(home.orgDbId));
            assertArrayEquals(otherFacts, stockFactCounts(other.orgDbId));
        }
        assertEquals(404, post(officer, "/api/projects", GSON.toJson(new ProjectRequest(foreignId, "Foreign", start, end))).statusCode());
        assertEquals(403, post(reader, "/api/projects", GSON.toJson(new ProjectRequest(departmentId, "Denied member", start, end))).statusCode());
        assertEquals(before, jdbc.queryForList(sql, home.orgDbId, other.orgDbId));
        assertArrayEquals(homeFacts, stockFactCounts(home.orgDbId));
        assertArrayEquals(otherFacts, stockFactCounts(other.orgDbId));
        HttpResponse<String> created = post(officer, "/api/projects",
                GSON.toJson(new ProjectRequest(departmentId, "  Fictional  admin project  ", start, end)));
        assertEquals(201, created.statusCode());
        var view = json(created);
        assertEquals(Set.of("id", "departmentId", "name", "startAt", "endAt"), view.keySet());
        assertEquals(departmentId, view.get("departmentId").getAsLong());
        assertEquals("Fictional admin project", view.get("name").getAsString());
        assertEquals(start, view.get("startAt").getAsString());
        assertEquals(end, view.get("endAt").getAsString());
        long id = view.get("id").getAsLong();
        var rows = jdbc.queryForList(sql, home.orgDbId, other.orgDbId);
        assertEquals(before.size() + 1, rows.size());
        assertEquals(before, rows.stream().filter(row -> ((Number) row.get("id")).longValue() != id).toList());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM project WHERE id=? AND organization_id=? AND department_id=? AND name=? AND start_at=? AND end_at=? AND version=0 AND lead_membership_id IS NULL",
                Integer.class, id, home.orgDbId, departmentId, "Fictional admin project",
                Timestamp.valueOf(LocalDateTime.parse(start)), Timestamp.valueOf(LocalDateTime.parse(end))));
        // Legacy createProject does not append a governance or stock event.
        assertArrayEquals(homeFacts, stockFactCounts(home.orgDbId));
        assertArrayEquals(otherFacts, stockFactCounts(other.orgDbId));
    }

    @Test @Order(74)
    void C8R040V2DepartmentCreationNormalizesNamesAndRejectsScopedConflicts() throws Exception {
        Fixture home = newTenant("C8-department-create-home", Membership.Role.OWNER);
        Fixture other = newTenant("C8-department-create-other", Membership.Role.OWNER);
        Fixture admin = fixtureInOrg(home.orgDbId, Membership.Role.ADMIN);
        Fixture member = fixtureInOrg(home.orgDbId, Membership.Role.MEMBER);
        Session officer = login(admin.username, admin.password);
        Session outsider = login(other.username, other.password);
        Session reader = login(member.username, member.password);
        String path = "/api/v2/departments";
        String sql = "SELECT id,organization_id,name,name_key,version FROM department WHERE organization_id IN (?,?) ORDER BY id";
        HttpResponse<String> foreign = post(outsider, path, GSON.toJson(new NameRequest("Fictional shared name")));
        assertEquals(201, foreign.statusCode());
        var before = jdbc.queryForList(sql, home.orgDbId, other.orgDbId);
        long[] homeFacts = stockFactCounts(home.orgDbId), otherFacts = stockFactCounts(other.orgDbId);
        HttpResponse<String> created = post(officer, path, GSON.toJson(new NameRequest("  Fictional   shared name  ")));
        assertEquals(201, created.statusCode());
        var view = json(created);
        assertEquals(Set.of("id", "name"), view.keySet());
        assertEquals("Fictional shared name", view.get("name").getAsString());
        long id = view.get("id").getAsLong();
        var rows = jdbc.queryForList(sql, home.orgDbId, other.orgDbId);
        assertEquals(before.size() + 1, rows.size());
        assertEquals(before, rows.stream().filter(row -> ((Number) row.get("id")).longValue() != id).toList());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM department WHERE id=? AND organization_id=? AND name=? AND name_key=? AND version=0",
                Integer.class, id, home.orgDbId, "Fictional shared name", "fictional shared name"));
        assertArrayEquals(homeFacts, stockFactCounts(home.orgDbId));
        assertArrayEquals(otherFacts, stockFactCounts(other.orgDbId));
        HttpResponse<String> duplicate = post(officer, path, GSON.toJson(new NameRequest("FICTIONAL SHARED NAME")));
        assertEquals(409, duplicate.statusCode());
        assertEquals("DATA_CONFLICT", json(duplicate).get("code").getAsString());
        assertEquals(rows, jdbc.queryForList(sql, home.orgDbId, other.orgDbId));
        for (String invalid : List.of("", "   ", "x".repeat(101))) {
            HttpResponse<String> rejected = post(officer, path, GSON.toJson(new NameRequest(invalid)));
            assertEquals(400, rejected.statusCode());
            assertEquals("VALIDATION_FAILED", json(rejected).get("code").getAsString());
            assertEquals(rows, jdbc.queryForList(sql, home.orgDbId, other.orgDbId));
        }
        assertEquals(403, post(reader, path, GSON.toJson(new NameRequest("Denied member"))).statusCode());
        assertEquals(rows, jdbc.queryForList(sql, home.orgDbId, other.orgDbId));
        assertArrayEquals(homeFacts, stockFactCounts(home.orgDbId));
        assertArrayEquals(otherFacts, stockFactCounts(other.orgDbId));
    }

    private record Fixture(long orgDbId, UUID orgPublicId, long userDbId, String username, String password,
                          long membershipDbId, UUID membershipPublicId, List<String> recoveryCodes) { }
    private static final class Session {
        final HttpClient client; String csrf; final String sessionId; final String oldSessionId; final String activeOrgId;
        Session(HttpClient client, String csrf, String sessionId, String oldSessionId, String activeOrgId) {
            this.client = client; this.csrf = csrf; this.sessionId = sessionId; this.oldSessionId = oldSessionId; this.activeOrgId = activeOrgId;
        }
    }
    private record LoginRequest(String username, String password) { }
    private record NameRequest(String name) { }
    private record OrgRequest(UUID organizationId) { }
    private record RoleRequest(String role) { }
    private record ResourceRequest(String name, String type, int totalQuantity) { }
    private record ProjectRequest(long departmentId, String name, String startAt, String endAt) { }
    private record ProjectUpdateRequest(String name, String startAt, String endAt) { }
    private record ResourceUpdateRequest(String name, int totalQuantity, int expectedTotalQuantity) { }
    private record RequirementRequest(long projectId, long resourceId, int quantity) { }
    private record RequirementUpdateRequest(int quantity) { }
    private record AllocationRequest(long requirementId, int quantity, String borrowerName, String phoneNumber, Boolean smsConsent) { }
    private record OperationRequest(UUID operationId) { }
    private record ReceiveRequest(UUID operationId, int quantity) { }
    private record AdjustRequest(UUID operationId, int newTotalQuantity) { }
    private record TransferRequest(UUID toMembershipId, String currentPassword) { }
    private record TransferIdRequest(String transferId) { }
    private record CurrentPasswordRequest(String currentPassword) { }
    private record PasswordChangeRequest(String currentPassword, String newPassword) { }
    private record RecoverRequest(String username, String oneTimeCode, String newPassword) { }
    private record RedeemRequest(String bearerToken, String username, String password, String displayName) { }
    private record TokenRequest(String bearerToken) { }
    private record SessionRow(String primaryId, String sessionId) { }
    private static final class JakartaProbe { @jakarta.validation.constraints.NotBlank final String name; JakartaProbe(String name) { this.name = name; } }
}

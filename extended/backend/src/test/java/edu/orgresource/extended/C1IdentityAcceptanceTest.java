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
import java.time.LocalDateTime;
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

import javax.validation.ValidatorFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=c1-test", "c1.sms.enabled=false", "c1.governance-expiry-delay-ms=86400000"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class C1IdentityAcceptanceTest {
    private static final Gson GSON = new Gson();
    private static final String COOKIE = "ORGSESSION";
    private static final String OWNER_SLUG = "c1-seed-workspace";

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
    @org.springframework.beans.factory.annotation.Value("${c1.sms.enabled:false}") boolean smsEnabled;
    @Autowired javax.validation.Validator javaxValidator;
    @Autowired ValidatorFactory javaxValidatorFactory;
    @Autowired Validator jakartaValidator;
    @Autowired jakarta.validation.ValidatorFactory jakartaValidatorFactory;
    @Autowired org.springframework.security.web.SecurityFilterChain securityFilterChain;

    private TransactionTemplate tx;
    private List<String> bootstrapCodes = List.of();

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
        assertEquals(9, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.referential_constraints WHERE constraint_schema=DATABASE() AND constraint_name LIKE 'fk_%_scope'", Integer.class));
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
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO project(organization_id,department_id,name,start_at,end_at) VALUES(?,?,?,?,?)",
                b.orgDbId, saved.id, "cross-tenant project", "2040-01-01 00:00:00", "2040-01-02 00:00:00"));
        RuntimeException invalidRole = assertThrows(RuntimeException.class,
                () -> jdbc.update("UPDATE membership SET role='ROOT' WHERE id=?", a.membershipDbId));
        assertNotNull(findCause(invalidRole, java.sql.SQLException.class));
        assertEquals(5, jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1", Integer.class));
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
        assertEquals(5, jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1", Integer.class));
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
                var flyway = org.flywaydb.core.Flyway.configure().dataSource(legacyUrl, user, password).load();
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
    private LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC); }
    private Set<String> actualSchemaColumns(String table) {
        return Set.copyOf(jdbc.queryForList("SELECT LOWER(column_name) FROM information_schema.columns WHERE table_schema=DATABASE() AND LOWER(table_name)=?", String.class, table));
    }
    private static String loginJson(Fixture f) { return GSON.toJson(new LoginRequest(f.username, f.password)); }

    private static Throwable findCause(Throwable failure, Class<? extends Throwable> type) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) if (type.isInstance(cause)) return cause;
        return null;
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

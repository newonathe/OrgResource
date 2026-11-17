package edu.orgresource.extended;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.identity.*;
import edu.orgresource.extended.domain.Department;
import edu.orgresource.extended.domain.Project;
import edu.orgresource.extended.security.C1IdentityNormalization;
import edu.orgresource.extended.security.ExtendedUserPrincipal;
import edu.orgresource.extended.service.C1Service;
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
    @Autowired PasswordEncoder passwordEncoder;
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
        assertTrue(url.matches("^jdbc:mysql://(?:127\\.0\\.0\\.1|localhost):[0-9]+/orgresource_c1_[A-Za-z0-9_]+(?:\\?.*)?$"),
                "C1 integration tests require a dedicated loopback orgresource_c1_* schema.");
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.getCatalog().startsWith("orgresource_c1_"));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM organization", Integer.class),
                    "ID01 must begin from an empty organization table.");
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM user_account", Integer.class),
                    "ID01 must begin from an empty account table.");
        }
        assertNotNull(System.getenv("EXT_BOOTSTRAP_PASSWORD"), "The isolated test runner supplies an ephemeral bootstrap secret.");
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
                "resource", "resource_requirement", "allocation", "spring_session", "spring_session_attributes");
        Set<String> actual = jdbc.queryForList("SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema=DATABASE()", String.class)
                .stream().collect(java.util.stream.Collectors.toSet());
        assertTrue(actual.containsAll(required), actual.toString());
        assertEquals(15, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND LOWER(table_name) IN ("
                + "'organization','user_account','local_credential','account_recovery_code','membership','invitation','ownership_transfer','audit_event',"
                + "'department','project','resource','resource_requirement','allocation','spring_session','spring_session_attributes')", Integer.class));
        assertEquals(15, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND engine='InnoDB' AND LOWER(table_name) IN ("
                + "'organization','user_account','local_credential','account_recovery_code','membership','invitation','ownership_transfer','audit_event',"
                + "'department','project','resource','resource_requirement','allocation','spring_session','spring_session_attributes')", Integer.class));
        assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.referential_constraints WHERE constraint_schema=DATABASE() AND constraint_name LIKE 'fk_%_scope'", Integer.class));
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
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1", Integer.class));
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
        assertEquals(200, get(anonymous, "/api/v2/session").statusCode());
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
        SessionRow stored = jdbc.queryForObject("SELECT PRIMARY_ID,SESSION_ID FROM spring_session WHERE PRINCIPAL_NAME=? ORDER BY CREATION_TIME DESC LIMIT 1",
                (rs, row) -> new SessionRow(rs.getString(1), rs.getString(2)), user.username);
        assertNotNull(stored);
        assertEquals(session.sessionId, stored.sessionId);
        String primary = stored.primaryId;
        assertNotNull(primary, "The authenticated security context must be persisted in Spring Session JDBC.");
        byte[] serialized;
        try (Connection connection = dataSource.getConnection(); PreparedStatement query = connection.prepareStatement(
                "SELECT ATTRIBUTE_BYTES FROM spring_session_attributes WHERE SESSION_PRIMARY_ID=? AND ATTRIBUTE_NAME=?")) {
            query.setString(1, primary); query.setString(2, "org.orgresource.sessionCreatedAt");
            try (ResultSet row = query.executeQuery()) { assertTrue(row.next()); serialized = row.getBytes(1); }
        }
        long created;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialized))) { created = (Long) input.readObject(); }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) { output.writeObject(created - TimeUnit.HOURS.toMillis(13)); }
        jdbc.update("UPDATE spring_session_attributes SET ATTRIBUTE_BYTES=? WHERE SESSION_PRIMARY_ID=? AND ATTRIBUTE_NAME=?",
                bytes.toByteArray(), primary, "org.orgresource.sessionCreatedAt");
        assertEquals(401, get(session.client, "/api/v2/session").statusCode());

        Session idle = login(user.username, user.password);
        SessionRow idleStored = jdbc.queryForObject("SELECT PRIMARY_ID,SESSION_ID FROM spring_session WHERE PRINCIPAL_NAME=? ORDER BY CREATION_TIME DESC LIMIT 1",
                (rs, row) -> new SessionRow(rs.getString(1), rs.getString(2)), user.username);
        assertNotNull(idleStored);
        assertEquals(idle.sessionId, idleStored.sessionId);
        jdbc.update("UPDATE spring_session SET LAST_ACCESS_TIME=?,EXPIRY_TIME=0 WHERE SESSION_ID=?",
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
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1", Integer.class));
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

    private Session login(String username, String password) throws Exception {
        HttpClient client = newClient();
        String preToken = json(get(client, "/api/v2/auth/csrf")).getAsJsonObject("data").get("csrfToken").getAsString();
        return login(client, username, password, preToken, cookie(client));
    }

    private Session login(HttpClient client, String username, String password, String preToken, String before) throws Exception {
        HttpResponse<String> response = post(client, "/api/v2/auth/login", GSON.toJson(new LoginRequest(username, password)), preToken, origin());
        assertEquals(200, response.statusCode(), response.body());
        JsonObject data = json(response).getAsJsonObject("data");
        return new Session(client, data.get("csrfToken").getAsString(), cookie(client), before,
                data.getAsJsonObject("session").get("activeOrgId").getAsString());
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

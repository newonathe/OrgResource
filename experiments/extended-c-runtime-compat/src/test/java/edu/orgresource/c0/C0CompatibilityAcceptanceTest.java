package edu.orgresource.c0;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import edu.orgresource.c0.api.ApiDtos;
import edu.orgresource.c0.api.C0ApiException;
import edu.orgresource.c0.api.C0ValidationException;
import edu.orgresource.c0.domain.Allocation;
import edu.orgresource.c0.domain.AllocationStatus;
import edu.orgresource.c0.domain.Department;
import edu.orgresource.c0.domain.Project;
import edu.orgresource.c0.domain.Resource;
import edu.orgresource.c0.domain.ResourceRequirement;
import edu.orgresource.c0.domain.ResourceType;
import edu.orgresource.c0.integration.FakeTwilioMessage;
import edu.orgresource.c0.integration.LoopbackRetrofitFakeTwilioClient;
import edu.orgresource.c0.integration.LoopbackRetrofitTwilioClient;
import edu.orgresource.c0.integration.TwilioMessageResponse;
import edu.orgresource.c0.repository.AllocationRepository;
import edu.orgresource.c0.repository.DepartmentRepository;
import edu.orgresource.c0.repository.ProjectRepository;
import edu.orgresource.c0.repository.ResourceRepository;
import edu.orgresource.c0.repository.ResourceRequirementRepository;
import edu.orgresource.c0.scheduler.C0ReminderScheduler;
import edu.orgresource.c0.security.C0UserPrincipal;
import edu.orgresource.c0.service.C0Service;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import okhttp3.Credentials;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.awaitility.Awaitility;
import org.glassfish.jersey.server.ResourceConfig;
import org.hibernate.Version;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=c0-test", "c0.scheduler.fixed-delay-ms=100", "c0.sms.enabled=false"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class C0CompatibilityAcceptanceTest {
    private static final DateTimeFormatter API_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final ZoneId PHT = ZoneId.of("Asia/Manila");
    private static final Gson GSON = new Gson();
    private static final Pattern ASSET = Pattern.compile("(?:src|href)=\"([^\"]*/assets/[^\"]+\\.(?:js|css))\"");

    @LocalServerPort int port;
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired DepartmentRepository departments;
    @Autowired ProjectRepository projects;
    @Autowired ResourceRepository resources;
    @Autowired ResourceRequirementRepository requirements;
    @Autowired AllocationRepository allocations;
    @Autowired C0Service service;
    @Autowired Clock philippineClock;
    @Autowired C0ReminderScheduler reminderScheduler;
    @org.springframework.beans.factory.annotation.Value("${c0.sms.enabled:false}") boolean smsEnabled;
    @Autowired javax.validation.Validator javaxValidator;
    @Autowired ValidatorFactory javaxValidatorFactory;
    @Autowired Validator jakartaValidator;
    @Autowired jakarta.validation.ValidatorFactory jakartaValidatorFactory;
    @Autowired SecurityFilterChain securityFilterChain;
    @Autowired ResourceConfig jerseyResourceConfig;

    private TransactionTemplate transactions;

    @BeforeAll
    void requireDedicatedLoopbackSchema() throws SQLException {
        transactions = new TransactionTemplate(transactionManager);
        String jdbcUrl = System.getenv("ORG_C0_DB_URL");
        assertNotNull(jdbcUrl, "ORG_C0_DB_URL must point to a dedicated C0 schema.");
        assertTrue(jdbcUrl.matches("^jdbc:mysql://(?:127\\.0\\.0\\.1|localhost):[0-9]+/orgresource_c0_[A-Za-z0-9_]+(?:\\?.*)?$"),
                "C0 acceptance must fail closed unless it uses a loopback orgresource_c0_* schema.");
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.getCatalog().startsWith("orgresource_c0_"));
        }
    }

    @AfterEach
    void removeOnlyC0TestFixtures() {
        jdbc.update("DELETE FROM allocation WHERE organization_id IN (101, 202)");
        jdbc.update("DELETE FROM resource_requirement WHERE organization_id IN (101, 202)");
        jdbc.update("DELETE FROM project WHERE organization_id IN (101, 202)");
        jdbc.update("DELETE FROM resource WHERE organization_id IN (101, 202)");
        jdbc.update("DELETE FROM department WHERE organization_id IN (101, 202)");
        for (String table : List.of("allocation", "resource_requirement", "project", "resource", "department")) {
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + table
                    + " WHERE organization_id IN (101, 202)", Integer.class),
                    "C0 fixture cleanup must leave table " + table + " empty for both test tenants.");
        }
    }

    @Test
    void uc01_jerseyHttpJsonAndSameProcessReactStaticSmoke() throws Exception {
        HttpClient anonymous = HttpClient.newHttpClient();
        HttpResponse<String> denied = send(anonymous, "GET", "/api/departments", null, null, null);
        assertEquals(401, denied.statusCode());
        assertEquals("UNAUTHENTICATED", json(denied).get("code").getAsString());
        assertTrue(denied.headers().firstValue("Content-Type").orElse("").contains("application/json"));

        HttpResponse<String> root = send(anonymous, "GET", "/", null, null, null);
        assertEquals(200, root.statusCode());
        assertTrue(root.body().contains("OrgResource"));
        Matcher matcher = ASSET.matcher(root.body());
        int assets = 0;
        while (matcher.find()) {
            HttpResponse<String> asset = send(anonymous, "GET", matcher.group(1), null, null, null);
            assertEquals(200, asset.statusCode(), matcher.group(1));
            assertTrue(asset.body().length() > 100);
            assets++;
        }
        assertTrue(assets >= 2, "The packaged React page must reference and serve JavaScript and CSS assets.");
        assertNotNull(jerseyResourceConfig);

        Session editor = login("c0-editor", requiredEnv("ORG_C0_EDITOR_PASSWORD"));
        HttpResponse<String> initialList = send(editor.client, "GET", "/api/departments", null, null, null);
        assertEquals(200, initialList.statusCode());
        assertTrue(initialList.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertEquals(0, JsonParser.parseString(initialList.body()).getAsJsonArray().size());
        JsonObject create = new JsonObject();
        create.addProperty("name", "UC01 Jersey " + UUID.randomUUID());
        HttpResponse<String> posted = post(editor, "/api/departments", create);
        assertEquals(201, posted.statusCode(), posted.body());
        assertTrue(send(editor.client, "GET", "/api/departments", null, null, null).body()
                .contains(json(posted).get("name").getAsString()));
    }

    @Test
    void uc02_autowiredJerseyComponentJpaCommitFreshConnectionAndConflict() throws Exception {
        Session editor = login("c0-editor", requiredEnv("ORG_C0_EDITOR_PASSWORD"));
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String name = "C0 duplicate " + suffix;
        JsonObject request = new JsonObject();
        request.addProperty("name", "  C0   duplicate " + suffix + "  ");
        HttpResponse<String> created = post(editor, "/api/departments", request);
        assertEquals(201, created.statusCode());
        JsonObject body = json(created);
        assertTrue(body.get("id").getAsLong() > 0);
        assertEquals(name, body.get("name").getAsString());
        long id = body.get("id").getAsLong();

        try (Connection fresh = DriverManager.getConnection(System.getenv("ORG_C0_DB_URL"),
                System.getenv("ORG_C0_DB_USER"), System.getenv("ORG_C0_DB_PASSWORD"));
             PreparedStatement query = fresh.prepareStatement("SELECT name FROM department WHERE id = ?")) {
            query.setLong(1, id);
            try (ResultSet rows = query.executeQuery()) {
                assertTrue(rows.next(), "The controller/component/JPA transaction must be committed to MySQL.");
                assertEquals(name, rows.getString(1));
                assertFalse(rows.next());
            }
        }

        JsonObject duplicate = new JsonObject();
        duplicate.addProperty("name", name.toUpperCase(java.util.Locale.ROOT));
        HttpResponse<String> conflict = post(editor, "/api/departments", duplicate);
        assertEquals(409, conflict.statusCode());
        assertEquals("DEPENDENCY_OR_DUPLICATE", json(conflict).get("code").getAsString());
    }

    @Test
    void uc03_literalJavaxBvalValidInvalidDtoAndControlledHttp400CoexistsWithJakarta() throws Exception {
        assertEquals(ApacheValidationProvider.class, javaxValidatorFactory.getClass()
                .getMethod("unwrap", Class.class).invoke(javaxValidatorFactory, javax.validation.ValidatorFactory.class)
                .getClass().getInterfaces().length == -1 ? null : ApacheValidationProvider.class);
        assertTrue(javaxValidator.getClass().getName().startsWith("org.apache.bval."), javaxValidator.getClass().getName());
        assertTrue(javaxValidatorFactory.getClass().getName().startsWith("org.apache.bval."),
                javaxValidatorFactory.getClass().getName());
        assertTrue(java.util.Arrays.stream(ApiDtos.CreateDepartment.class.getField("name").getDeclaredAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getName().equals("javax.validation.constraints.NotBlank")));

        ApiDtos.CreateDepartment invalid = new ApiDtos.CreateDepartment("   ");
        assertTrue(javaxValidator.validate(invalid).stream().anyMatch(v ->
                v.getConstraintDescriptor().getAnnotation().annotationType().getName()
                        .startsWith("javax.validation.constraints.")));
        assertTrue(javaxValidator.validate(new ApiDtos.CreateDepartment("Valid department")).isEmpty());

        ApiDtos.CreateResource invalidQuantity = new ApiDtos.CreateResource();
        invalidQuantity.name = "Valid resource";
        invalidQuantity.type = ResourceType.REUSABLE;
        invalidQuantity.totalQuantity = -1;
        assertTrue(javaxValidator.validate(invalidQuantity).stream().anyMatch(v ->
                v.getConstraintDescriptor().getAnnotation().annotationType().getName()
                        .equals("javax.validation.constraints.Min")));

        Session editor = login("c0-editor", requiredEnv("ORG_C0_EDITOR_PASSWORD"));
        JsonObject request = new JsonObject();
        request.addProperty("name", "   ");
        HttpResponse<String> rejected = post(editor, "/api/departments", request);
        assertEquals(400, rejected.statusCode());
        assertEquals("VALIDATION_FAILED", json(rejected).get("code").getAsString());

        JsonObject invalidResource = new JsonObject();
        invalidResource.addProperty("name", "invalid quantity " + UUID.randomUUID());
        invalidResource.addProperty("type", "REUSABLE");
        invalidResource.addProperty("totalQuantity", -1);
        HttpResponse<String> badQuantity = post(editor, "/api/resources", invalidResource);
        assertEquals(400, badQuantity.statusCode());
        assertEquals("VALIDATION_FAILED", json(badQuantity).get("code").getAsString());

        JakartaProbe jakartaInvalid = new JakartaProbe(" ");
        Set<ConstraintViolation<JakartaProbe>> jakartaViolations = jakartaValidator.validate(jakartaInvalid);
        assertFalse(jakartaViolations.isEmpty());
        assertTrue(jakartaValidatorFactory.getValidator().getClass().getName().startsWith("org.hibernate.validator."),
                jakartaValidatorFactory.getValidator().getClass().getName());
    }

    @Test
    void uc04_javaxEntityManualPreSaveVersusActualJpaAndSqlConstraintBoundary() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Resource invalid = new Resource(101L, "C0 invalid " + suffix, "c0 invalid " + suffix,
                ResourceType.REUSABLE, -1);
        assertFalse(javaxValidator.validate(invalid).isEmpty());

        assertThrows(C0ValidationException.class, () -> service.persistResourceWithManualJavaxValidation(invalid));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE name_key = ?",
                Integer.class, invalid.nameKey));

        RuntimeException jpaFailure = assertThrows(RuntimeException.class, () -> transactions.execute(status -> {
            resources.saveAndFlush(invalid); // Deliberately bypass the explicit Javax pre-save validator.
            return null;
        }));
        SQLException sqlFailure = findCause(jpaFailure, SQLException.class);
        assertNotNull(sqlFailure, "The actual MySQL constraint, not Jakarta/Javax JPA callback validation, must reject this row.");
        assertTrue(sqlFailure.getSQLState() == null || sqlFailure.getSQLState().startsWith("23")
                        || sqlFailure.getMessage().contains("ck_resource_total_nonnegative"),
                "Expected the named MySQL nonnegative-quantity check constraint.");
        assertNull(findCause(jpaFailure, javax.validation.ConstraintViolationException.class),
                "This direct JPA save must not be misreported as automatic Javax ORM validation.");
        assertNull(findCause(jpaFailure, jakarta.validation.ConstraintViolationException.class),
                "This direct JPA save must be distinguished from Jakarta ORM validation.");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM resource WHERE name_key = ?",
                Integer.class, invalid.nameKey), "The failed flush must leave no row committed.");
    }

    @Test
    void uc05SpringSecurityRolesCsrfSessionFixationAndJsonDenials() throws Exception {
        HttpClient anonymous = HttpClient.newHttpClient();
        HttpResponse<String> anon = send(anonymous, "GET", "/api/departments", null, null, null);
        assertEquals(401, anon.statusCode());
        assertEquals("UNAUTHENTICATED", json(anon).get("code").getAsString());

        Session viewer = login("c0-viewer", requiredEnv("ORG_C0_VIEWER_PASSWORD"));
        assertNotEquals(viewer.sessionBeforeLogin, viewer.sessionAfterLogin,
                "Spring Security session fixation protection must rotate the session identifier.");
        JsonObject body = new JsonObject();
        body.addProperty("name", "viewer denied " + UUID.randomUUID());
        HttpResponse<String> roleDenied = post(viewer, "/api/departments", body);
        assertEquals(403, roleDenied.statusCode());
        assertEquals("FORBIDDEN", json(roleDenied).get("code").getAsString());

        HttpResponse<String> csrfDenied = send(viewer.client, "POST", "/api/departments",
                GSON.toJson(body), "application/json", null);
        assertEquals(403, csrfDenied.statusCode());
        assertEquals("FORBIDDEN", json(csrfDenied).get("code").getAsString());

        Session editor = login("c0-editor", requiredEnv("ORG_C0_EDITOR_PASSWORD"));
        JsonObject accepted = new JsonObject();
        accepted.addProperty("name", "security accepted " + UUID.randomUUID());
        HttpResponse<String> editorMissingCsrf = send(editor.client, "POST", "/api/departments",
                GSON.toJson(accepted), "application/json", null);
        assertEquals(403, editorMissingCsrf.statusCode());
        assertEquals("FORBIDDEN", json(editorMissingCsrf).get("code").getAsString());
        HttpResponse<String> ok = post(editor, "/api/departments", accepted);
        assertEquals(201, ok.statusCode());
    }

    @Test
    void uc06MysqlSchemaForeignKeysRollbackAndJpaQueriesInitialize() throws Exception {
        List<String> tables = jdbc.queryForList("SHOW TABLES", String.class).stream()
                .filter(name -> Set.of("department", "project", "resource", "resource_requirement", "allocation")
                        .contains(name.toLowerCase(java.util.Locale.ROOT))
                        || name.toLowerCase(java.util.Locale.ROOT).startsWith("flyway_schema_history"))
                .toList();
        assertEquals(6, tables.size(), tables.toString());
        assertEquals(5, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND engine = 'InnoDB' AND table_name IN "
                + "('department','project','resource','resource_requirement','allocation')", Integer.class));
        assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.referential_constraints "
                + "WHERE constraint_schema = DATABASE() AND constraint_name LIKE 'fk_%_scope'", Integer.class));
        assertEquals("1", jdbc.queryForObject("SELECT version FROM flyway_schema_history WHERE success = 1",
                String.class));

        SQLException fkFailure = assertThrows(SQLException.class, () -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement insert = connection.prepareStatement("INSERT INTO project "
                         + "(organization_id, department_id, name, start_at, end_at) VALUES (101, 999999, 'bad', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY))")) {
                insert.executeUpdate();
            }
        });
        assertTrue(fkFailure.getSQLState() != null && fkFailure.getSQLState().startsWith("23"));

        long marker = System.currentTimeMillis();
        transactions.execute(status -> {
            jdbc.update("INSERT INTO department (organization_id,name,name_key) VALUES (101, ?, ?)",
                    "rollback " + marker, "rollback " + marker);
            status.setRollbackOnly();
            return null;
        });
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM department WHERE name_key = ?",
                Integer.class, "rollback " + marker));
        assertNotNull(departments.findByOrganizationIdOrderById(101L));
        assertNotNull(projects.findByOrganizationIdAndDepartmentIdOrderById(101L, -1L));
        assertNotNull(resources.findByOrganizationIdOrderById(101L));
        assertNotNull(requirements.findByOrganizationIdAndProjectIdOrderById(101L, -1L));
        assertNotNull(allocations.findByOrganizationIdAndRequirementIdOrderById(101L, -1L));
        assertEquals(0L, requirements.sumCoveredQuantity(-1L, 101L, LocalDateTime.now(philippineClock)));
        assertEquals(0L, resources.sumEffectiveHoldsAndIssuedCustody(-1L, 101L,
                LocalDateTime.now(philippineClock), ResourceType.REUSABLE));
    }

    @Test
    void uc08OriginalFiveFamilyHttpOperationsAndPrivateContactNeverLeavesDto() throws Exception {
        Session editor = login("c0-editor", requiredEnv("ORG_C0_EDITOR_PASSWORD"));
        JsonObject departmentBody = new JsonObject();
        departmentBody.addProperty("name", "UC08 Dept " + UUID.randomUUID());
        HttpResponse<String> departmentResponse = post(editor, "/api/departments", departmentBody);
        assertEquals(201, departmentResponse.statusCode());
        JsonObject department = json(departmentResponse);

        LocalDateTime now = LocalDateTime.now(philippineClock);
        JsonObject projectBody = new JsonObject();
        projectBody.addProperty("departmentId", department.get("id").getAsLong());
        projectBody.addProperty("name", "UC08 project");
        projectBody.addProperty("startAt", now.plusMinutes(1).format(API_DATE));
        projectBody.addProperty("endAt", now.plusHours(2).format(API_DATE));
        HttpResponse<String> projectResponse = post(editor, "/api/projects", projectBody);
        assertEquals(201, projectResponse.statusCode());
        JsonObject project = json(projectResponse);
        assertEquals(department.get("id").getAsLong(), project.get("departmentId").getAsLong());

        JsonObject resourceBody = new JsonObject();
        resourceBody.addProperty("name", "UC08 paper " + UUID.randomUUID());
        resourceBody.addProperty("type", "CONSUMABLE");
        resourceBody.addProperty("totalQuantity", 12);
        HttpResponse<String> resourceResponse = post(editor, "/api/resources", resourceBody);
        assertEquals(201, resourceResponse.statusCode());
        JsonObject resource = json(resourceResponse);
        assertEquals(12, resource.get("availableQuantity").getAsInt());

        JsonObject requirementBody = new JsonObject();
        requirementBody.addProperty("projectId", project.get("id").getAsLong());
        requirementBody.addProperty("resourceId", resource.get("id").getAsLong());
        requirementBody.addProperty("quantity", 5);
        HttpResponse<String> requirementResponse = post(editor, "/api/requirements", requirementBody);
        assertEquals(201, requirementResponse.statusCode());
        JsonObject requirement = json(requirementResponse);
        assertEquals(0, requirement.get("allocatedQuantity").getAsInt());
        assertEquals(5, requirement.get("shortageQuantity").getAsInt());

        JsonObject allocationBody = new JsonObject();
        allocationBody.addProperty("requirementId", requirement.get("id").getAsLong());
        allocationBody.addProperty("quantity", 2);
        allocationBody.addProperty("borrowerName", "Fictional borrower");
        allocationBody.addProperty("phoneNumber", "+12025550100");
        allocationBody.addProperty("smsConsent", true);
        HttpResponse<String> allocationResponse = post(editor, "/api/allocations", allocationBody);
        assertEquals(201, allocationResponse.statusCode(), allocationResponse.body());
        JsonObject allocation = json(allocationResponse);
        assertEquals("PENDING", allocation.get("status").getAsString());
        assertTrue(allocation.get("contactPresent").getAsBoolean());
        assertTrue(allocation.get("smsConsent").getAsBoolean());
        assertFalse(allocation.has("phoneNumber"), "The contact number must never be returned in the API DTO.");

        assertTrue(allocations.findById(allocation.get("id").getAsLong()).orElseThrow().phoneNumber
                .equals("+12025550100"), "The fictional contact must persist in MySQL while staying private in JSON.");
    }

    @Test
    void uc09TransactionalInventoryBoundariesCleanupAndConcurrentApprovals() throws Exception {
        LocalDateTime now = LocalDateTime.now(philippineClock);
        Fixture active = seedFixture(101L, ResourceType.REUSABLE, 10, 20,
                now.minusHours(1), now.plusHours(1));
        seedAllocation(active.requirementId, 101L, 3, AllocationStatus.APPROVED, now.minusMinutes(2));
        seedAllocation(active.requirementId, 101L, 4, AllocationStatus.PENDING, null);

        Fixture ended = seedRequirementForResource(active.resourceId, 101L, 10,
                now.minusDays(2), now.truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        seedAllocation(ended.requirementId, 101L, 4, AllocationStatus.APPROVED, now.minusDays(1));
        seedAllocation(ended.requirementId, 101L, 2, AllocationStatus.ISSUED, now.minusDays(1));
        long expiredPending = seedAllocation(ended.requirementId, 101L, 2, AllocationStatus.PENDING, null);

        assertEquals(5, service.availableQuantity(active.resourceId, 101L, now),
                "Only the current approval and reusable issued custody reserve physical stock.");
        assertEquals(3L, requirements.sumCoveredQuantity(active.requirementId, 101L, now),
                "Active approved coverage counts; pending and expired approvals do not.");
        Project endedProject = projects.findById(ended.projectId).orElseThrow();
        assertEquals(2L, requirements.sumCoveredQuantity(ended.requirementId, 101L, endedProject.endAt),
                "At the exact project end, the approved 4 is excluded while issued 2 stays covered.");
        assertEquals(5, service.availableQuantity(active.resourceId, 101L, now),
                "T10 - active approved 3 - expired-project issued custody 2; expired approved 4 and pending do not hold.");
        assertEquals(1, service.cleanupExpiredPending(101L, now));
        assertEquals(AllocationStatus.CANCELLED, allocations.findById(expiredPending).orElseThrow().status);
        assertEquals(5, service.availableQuantity(active.resourceId, 101L, now),
                "Expired approval and pending cleanup must not release or consume reusable issued custody.");

        long toIssue = seedAllocation(active.requirementId, 101L, 1, AllocationStatus.PENDING, null);
        service.approveAllocation(toIssue, 101L);
        assertEquals(4, service.availableQuantity(active.resourceId, 101L, now));
        service.issueAllocation(toIssue, 101L);
        assertEquals(4, service.availableQuantity(active.resourceId, 101L, now),
                "Issuing an approved reusable Allocation transfers the hold without a second deduction.");

        Fixture consumable = seedFixture(101L, ResourceType.CONSUMABLE, 5, 5,
                now.minusMinutes(1), now.plusHours(1));
        long toConsume = seedAllocation(consumable.requirementId, 101L, 2, AllocationStatus.PENDING, null);
        service.approveAllocation(toConsume, 101L);
        assertEquals(3, service.availableQuantity(consumable.resourceId, 101L, now));
        service.consumeAllocation(toConsume, 101L);
        assertEquals(3, resources.findById(consumable.resourceId).orElseThrow().totalQuantity);
        assertEquals(3, service.availableQuantity(consumable.resourceId, 101L, now),
                "Consuming decrements physical stock once and removes the temporary approval hold.");
        assertEquals(AllocationStatus.CONSUMED, allocations.findById(toConsume).orElseThrow().status);

        Fixture future = seedFixture(101L, ResourceType.REUSABLE, 10, 10,
                now.plusHours(1), now.plusHours(2));
        long futureApproval = seedAllocation(future.requirementId, 101L, 2, AllocationStatus.PENDING, null);
        service.approveAllocation(futureApproval, 101L);
        assertEquals(8, service.availableQuantity(future.resourceId, 101L, now));
        C0ApiException futureIssue = assertThrows(C0ApiException.class,
                () -> service.issueAllocation(futureApproval, 101L));
        assertEquals("PROJECT_NOT_ACTIVE", futureIssue.getCode());
        assertEquals(AllocationStatus.APPROVED, allocations.findById(futureApproval).orElseThrow().status);

        Fixture concurrent = seedFixture(101L, ResourceType.REUSABLE, 5, 8,
                now.minusMinutes(1), now.plusHours(1));
        long first = seedAllocation(concurrent.requirementId, 101L, 4, AllocationStatus.PENDING, null);
        long second = seedAllocation(concurrent.requirementId, 101L, 4, AllocationStatus.PENDING, null);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var one = pool.submit(() -> attemptApproval(first, ready, start));
            var two = pool.submit(() -> attemptApproval(second, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            List<Boolean> results = List.of(one.get(15, TimeUnit.SECONDS), two.get(15, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter(Boolean::booleanValue).count(), results.toString());
            assertEquals(1, results.stream().filter(value -> !value).count(), results.toString());
            assertEquals(4L, allocations.findByOrganizationIdAndRequirementIdOrderById(101L,
                    concurrent.requirementId).stream().filter(a -> a.status == AllocationStatus.APPROVED)
                    .mapToLong(a -> a.quantity).sum());
            assertEquals(1, service.availableQuantity(concurrent.resourceId, 101L, now));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void uc10OldAndScopedVersionedRoutesShareRowsAndIgnoreForgedOrganizationHeader() throws Exception {
        Session editor = login("c0-editor", requiredEnv("ORG_C0_EDITOR_PASSWORD"));
        JsonObject request = new JsonObject();
        request.addProperty("name", "UC10 shared " + UUID.randomUUID());
        HttpResponse<String> created = post(editor, "/api/departments", request);
        assertEquals(201, created.statusCode());
        long id = json(created).get("id").getAsLong();
        HttpResponse<String> sharedRead = send(editor.client, "GET", "/api/v2/departments/" + id,
                null, null, null);
        assertEquals(200, sharedRead.statusCode());
        assertEquals(id, json(sharedRead).get("id").getAsLong());

        Fixture other = seedFixture(202L, ResourceType.CONSUMABLE, 1, 1,
                LocalDateTime.now(philippineClock).minusMinutes(1), LocalDateTime.now(philippineClock).plusHours(1));
        HttpResponse<String> forged = send(editor.client, "GET", "/api/v2/departments/" + other.departmentId,
                null, null, null, Map.of("X-Organization-Id", "202"));
        assertEquals(404, forged.statusCode());
        assertEquals("NOT_FOUND", json(forged).get("code").getAsString());
    }

    @Test
    void uc11ResolvedRuntimeHasBothValidationNamespacesAndNoLegacyPersistenceApi() throws Exception {
        assertEquals("4.1.2", SpringBootVersion.getVersion());
        assertTrue(org.springframework.core.SpringVersion.getVersion().startsWith("7.0."));
        assertTrue(Version.getVersionString().startsWith("7."));
        assertEquals("jakarta.validation.Validator", Validator.class.getName());
        assertEquals("javax.validation.Validator", javax.validation.Validator.class.getName());
        assertTrue(javaxValidatorFactory.getClass().getName().startsWith("org.apache.bval."));
        assertTrue(jakartaValidatorFactory.getValidator().getClass().getName().startsWith("org.hibernate.validator."),
                jakartaValidatorFactory.getValidator().getClass().getName());
        System.out.println("C0_VALIDATION_PROVIDERS javax=" + javaxValidatorFactory.getClass().getName()
                + "/" + javaxValidator.getClass().getName() + " jakarta="
                + jakartaValidatorFactory.getClass().getName() + "/"
                + jakartaValidatorFactory.getValidator().getClass().getName());
        assertThrows(ClassNotFoundException.class, () -> Class.forName("javax.persistence.Entity"));
        assertTrue(LoopbackRetrofitTwilioClient.class.getName().contains("LoopbackRetrofitTwilioClient"));
        assertTrue(reminderScheduler.disabledTickCount() >= 0);
        Awaitility.await().atMost(java.time.Duration.ofSeconds(3))
                .until(() -> reminderScheduler.disabledTickCount() > 0);
    }

    @Test
    void uc07RetrofitGsonFakeAndTwilioProtocolAreLoopbackOnlyNoRetryAndSchedulerIsDisabled() throws Exception {
        AtomicInteger fakeRequests = new AtomicInteger();
        AtomicInteger providerRequests = new AtomicInteger();
        AtomicReference<String> providerAuthorization = new AtomicReference<>();
        AtomicReference<String> providerContentType = new AtomicReference<>();
        AtomicReference<String> providerBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
        server.createContext("/fake/messages", exchange -> {
            fakeRequests.incrementAndGet();
            assertEquals("POST", exchange.getRequestMethod());
            assertTrue(exchange.getRequestHeaders().getFirst("Content-Type").contains("application/json"));
            FakeTwilioMessage received = GSON.fromJson(new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8), FakeTwilioMessage.class);
            received.id = "SM-FAKE-C0";
            received.status = "simulated";
            writeJson(exchange, 200, GSON.toJson(received));
        });
        server.createContext("/2010-04-01/Accounts/AC-C0/Messages.json", exchange -> {
            int current = providerRequests.incrementAndGet();
            providerAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            providerContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            providerBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (current == 1) writeJson(exchange, 201, "{\"sid\":\"SM-LOCAL-C0\",\"status\":\"accepted\"}");
            else writeJson(exchange, 503, "{\"message\":\"local controlled failure\"}");
        });
        server.start();
        try {
            URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
            FakeTwilioMessage simulated = new LoopbackRetrofitFakeTwilioClient(base)
                    .send(new FakeTwilioMessage("+12025550100", "+12025550101", "C0 fictional test"));
            assertEquals("SM-FAKE-C0", simulated.id);
            assertEquals("simulated", simulated.status);
            assertEquals(1, fakeRequests.get());

            String token = "LOCAL-ONLY-TOKEN";
            LoopbackRetrofitTwilioClient protocol = new LoopbackRetrofitTwilioClient(base, "AC-C0", token);
            TwilioMessageResponse accepted = protocol.send("+12025550100", "+12025550101", "C0 protocol fixture");
            assertEquals("SM-LOCAL-C0", accepted.sid);
            assertEquals("accepted", accepted.status);
            assertEquals(Credentials.basic("AC-C0", token), providerAuthorization.get());
            assertTrue(providerContentType.get().contains("application/x-www-form-urlencoded"));
            Map<String, String> form = parseForm(providerBody.get());
            assertEquals("+12025550100", form.get("To"));
            assertEquals("+12025550101", form.get("From"));
            assertEquals("C0 protocol fixture", form.get("Body"));

            assertThrows(java.io.IOException.class,
                    () -> protocol.send("+12025550100", "+12025550101", "expected local 503"));
            assertEquals(2, providerRequests.get(), "One failing Retrofit call must not be retried.");
            assertThrows(IllegalArgumentException.class, () ->
                    new LoopbackRetrofitTwilioClient(URI.create("https://example.invalid/"), "AC-C0", token));
            assertThrows(IllegalArgumentException.class, () ->
                    new LoopbackRetrofitFakeTwilioClient(URI.create("http://example.invalid/")));
            assertFalse(smsEnabled, "The C0 application context must keep live SMS disabled.");
            Awaitility.await().atMost(java.time.Duration.ofSeconds(3))
                    .until(() -> reminderScheduler.disabledTickCount() > 0);
        } finally {
            server.stop(0);
        }
    }

    private Session login(String username, String password) throws Exception {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> form = send(client, "GET", "/login", null, null, null);
        assertEquals(200, form.statusCode());
        Matcher csrf = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(form.body());
        assertTrue(csrf.find(), "Spring Security login form should include a CSRF token.");
        String before = sessionId(cookies);
        String body = "username=" + enc(username) + "&password=" + enc(password) + "&_csrf=" + enc(csrf.group(1));
        HttpResponse<String> submitted = send(client, "POST", "/login", body,
                "application/x-www-form-urlencoded", null);
        assertTrue(submitted.statusCode() == 302 || submitted.statusCode() == 303, submitted.body());
        String after = sessionId(cookies);
        HttpResponse<String> csrfResponse = send(client, "GET", "/api/v2/csrf", null, null, null);
        assertEquals(200, csrfResponse.statusCode());
        String token = json(csrfResponse).get("token").getAsString();
        assertFalse(token.isBlank());
        return new Session(client, token, before, after);
    }

    private HttpResponse<String> post(Session session, String path, JsonObject body) throws Exception {
        return send(session.client, "POST", path, GSON.toJson(body), "application/json", session.csrf);
    }

    private HttpResponse<String> send(HttpClient client, String method, String path, String body,
                                       String contentType, String csrf) throws Exception {
        return send(client, method, path, body, contentType, csrf, Map.of());
    }

    private HttpResponse<String> send(HttpClient client, String method, String path, String body,
                                      String contentType, String csrf, Map<String, String> headers) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (contentType != null) request.header("Content-Type", contentType);
        if (csrf != null) request.header("X-CSRF-TOKEN", csrf);
        headers.forEach(request::header);
        if (body == null) request.method(method, HttpRequest.BodyPublishers.noBody());
        else request.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static JsonObject json(HttpResponse<String> response) {
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        assertNotNull(value, name + " must be present in the test process environment.");
        assertFalse(value.isBlank());
        return value;
    }

    private static String sessionId(CookieManager cookies) {
        return cookies.getCookieStore().getCookies().stream().filter(cookie -> cookie.getName().equals("JSESSIONID"))
                .map(HttpCookie::getValue).findFirst().orElse(null);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> values = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            values.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(parts.length == 2 ? parts[1] : "", StandardCharsets.UTF_8));
        }
        return values;
    }

    private static void writeJson(HttpExchange exchange, int status, String body) throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }

    private static <T extends Throwable> T findCause(Throwable failure, Class<T> type) {
        Throwable cursor = failure;
        while (cursor != null) {
            if (type.isInstance(cursor)) return type.cast(cursor);
            cursor = cursor.getCause();
        }
        return null;
    }

    private Fixture seedFixture(Long organizationId, ResourceType type, int total, int requirementQuantity,
                                LocalDateTime startAt, LocalDateTime endAt) {
        return transactions.execute(status -> {
            String suffix = UUID.randomUUID().toString().replace("-", "");
            Department department = departments.saveAndFlush(new Department(organizationId,
                    "C0 fixture " + suffix, "c0 fixture " + suffix));
            Project project = projects.saveAndFlush(new Project(organizationId, department,
                    "C0 project " + suffix, startAt, endAt));
            String resourceName = "C0 resource " + suffix;
            Resource resource = resources.saveAndFlush(new Resource(organizationId, resourceName,
                    resourceName.toLowerCase(java.util.Locale.ROOT), type, total));
            ResourceRequirement requirement = requirements.saveAndFlush(new ResourceRequirement(organizationId,
                    project, resource, requirementQuantity));
            return new Fixture(department.id, project.id, resource.id, requirement.id);
        });
    }

    private Fixture seedRequirementForResource(long resourceId, Long organizationId, int requirementQuantity,
                                               LocalDateTime startAt, LocalDateTime endAt) {
        return transactions.execute(status -> {
            String suffix = UUID.randomUUID().toString().replace("-", "");
            Department department = departments.saveAndFlush(new Department(organizationId,
                    "C0 boundary " + suffix, "c0 boundary " + suffix));
            Project project = projects.saveAndFlush(new Project(organizationId, department,
                    "C0 ended " + suffix, startAt, endAt));
            Resource resource = resources.findByIdAndOrganizationId(resourceId, organizationId).orElseThrow();
            ResourceRequirement requirement = requirements.saveAndFlush(new ResourceRequirement(organizationId,
                    project, resource, requirementQuantity));
            return new Fixture(department.id, project.id, resource.id, requirement.id);
        });
    }

    private long seedAllocation(Long requirementId, Long organizationId, int quantity,
                                AllocationStatus status, LocalDateTime approvedAt) {
        return transactions.execute(transaction -> {
            ResourceRequirement requirement = requirements.findByIdAndOrganizationId(requirementId, organizationId)
                    .orElseThrow();
            Allocation allocation = new Allocation(organizationId, requirement, quantity,
                    "Fictional C0 borrower", null, false, LocalDateTime.now(philippineClock));
            allocation.status = status;
            allocation.approvedAt = approvedAt;
            if (status == AllocationStatus.ISSUED) allocation.issuedAt = LocalDateTime.now(philippineClock);
            if (status == AllocationStatus.CANCELLED) allocation.cancelledAt = LocalDateTime.now(philippineClock);
            return allocations.saveAndFlush(allocation).id;
        });
    }

    private boolean attemptApproval(long allocationId, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        assertTrue(start.await(5, TimeUnit.SECONDS));
        try {
            service.approveAllocation(allocationId, 101L);
            return true;
        } catch (C0ApiException expectedConflict) {
            return false;
        }
    }

    private record Fixture(long departmentId, long projectId, long resourceId, long requirementId) { }

    private record Session(HttpClient client, String csrf, String sessionBeforeLogin, String sessionAfterLogin) { }

    private static final class JakartaProbe {
        @jakarta.validation.constraints.NotBlank final String name;
        private JakartaProbe(String name) { this.name = name; }
    }
}

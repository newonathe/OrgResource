package edu.orgresource.extended.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import edu.orgresource.extended.api.ApiDtos;
import edu.orgresource.extended.demo.C8DemoCommand;
import edu.orgresource.extended.demo.OfflineDemoClock;
import edu.orgresource.extended.domain.Allocation;
import edu.orgresource.extended.domain.Department;
import edu.orgresource.extended.domain.Project;
import edu.orgresource.extended.domain.Resource;
import edu.orgresource.extended.domain.ResourceType;
import edu.orgresource.extended.identity.C1AuditService;
import edu.orgresource.extended.identity.LocalCredential;
import edu.orgresource.extended.identity.LocalCredentialRepository;
import edu.orgresource.extended.identity.Membership;
import edu.orgresource.extended.identity.MembershipRepository;
import edu.orgresource.extended.identity.Organization;
import edu.orgresource.extended.identity.OrganizationRepository;
import edu.orgresource.extended.identity.UserAccount;
import edu.orgresource.extended.identity.UserAccountRepository;
import edu.orgresource.extended.repository.DepartmentRepository;
import edu.orgresource.extended.repository.ProjectRepository;
import edu.orgresource.extended.repository.ResourceRequirementRepository;
import edu.orgresource.extended.security.ExtendedUserPrincipal;
import jakarta.persistence.EntityManager;

@Component
@Profile(C8DemoCommand.PROFILE)
@ConditionalOnNotWebApplication
public class C8DemoImporter {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private static final LocalDateTime OPENING = LocalDateTime.parse("2025-12-01T08:00:00");
    public static final String OPERATOR = "c8-demo-operator";

    private final OrganizationRepository organizations;
    private final UserAccountRepository accounts;
    private final MembershipRepository memberships;
    private final LocalCredentialRepository credentials;
    private final DepartmentRepository departments;
    private final ProjectRepository projects;
    private final ResourceRequirementRepository requirements;
    private final InventoryService inventory;
    private final C1AuditService audit;
    private final OfflineDemoClock businessClock;
    private final Clock wallClock;
    private final PasswordEncoder passwords;
    private final JdbcTemplate jdbc;
    private final EntityManager entities;
    private final TransactionTemplate transaction;

    public C8DemoImporter(OrganizationRepository organizations, UserAccountRepository accounts, MembershipRepository memberships,
            LocalCredentialRepository credentials, DepartmentRepository departments, ProjectRepository projects,
            ResourceRequirementRepository requirements, InventoryService inventory, C1AuditService audit,
            OfflineDemoClock businessClock, Clock wallClock, PasswordEncoder passwords, JdbcTemplate jdbc,
            EntityManager entities, PlatformTransactionManager manager) {
        this.organizations = organizations; this.accounts = accounts; this.memberships = memberships; this.credentials = credentials;
        this.departments = departments; this.projects = projects; this.requirements = requirements;
        this.inventory = inventory; this.audit = audit; this.businessClock = businessClock; this.wallClock = wallClock;
        this.passwords = passwords; this.jdbc = jdbc; this.entities = entities;
        this.transaction = new TransactionTemplate(manager);
        this.transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public void importPlan(JsonNode plan, String planSha, String ownerPassword) {
        if (organizations.count() != 0 || accounts.count() != 0
                || jdbc.queryForObject("SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1", Integer.class) != 7)
            throw new IllegalStateException("Apply this plan to a fresh demo schema at the expected migration.");
        LocalDateTime wall = LocalDateTime.ofInstant(wallClock.instant(), ZoneOffset.UTC);
        UserAccount operator = transaction.execute(status -> accounts.saveAndFlush(
                new UserAccount(OPERATOR, "Local demo operator", wall)));
        JsonNode workspaces = plan.path("organizations");
        int expectedRequests = 0;
        int imported = 0;
        for (JsonNode slice : workspaces) {
            expectedRequests += slice.path("allocations").size();
            transaction.executeWithoutResult(status -> importOrganization(slice, plan, planSha, operator, wall));
            imported++;
            if (imported % 12 == 0) System.out.println("Reconciled local demo workspaces: " + imported + " / " + workspaces.size());
        }
        int totalRequests = expectedRequests;
        transaction.executeWithoutResult(status -> {
            assertCount(workspaces.size(), "SELECT COUNT(*) FROM organization WHERE demo_plan_sha=?", planSha);
            assertCount(totalRequests, "SELECT COUNT(*) FROM allocation");
            assertCount(0, "SELECT COUNT(*) FROM allocation WHERE sms_consent OR phone_ciphertext IS NOT NULL");
            for (var state : plan.path("final_states").properties())
                assertCount(state.getValue().intValue(), "SELECT COUNT(*) FROM allocation WHERE status=?", state.getKey());
            credentials.saveAndFlush(new LocalCredential(operator.id, passwords.encode(ownerPassword), wall));
        });
        System.out.printf("C8 local demo import reconciled. %d workspaces and %d fictional requests.%n", workspaces.size(), totalRequests);
    }

    /**
     * The approved full plan is accepted only after the isolated target, independent
     * restoration, authenticated owner, and exact source were checked by the
     * non-web manual command. No web endpoint exposes this method.
     * A failed partial run needs a backup restore before retry.
     */
    public void importApprovedHostedPlan(JsonNode plan, String planSha,
            UUID originalOrganizationId, UUID existingOwnerId) {
        if (!edu.orgresource.extended.demo.C8HostedDemoPreflight.APPROVED_FULL_PLAN_SHA.equals(planSha)
                || !"LOCAL_ONLY_DRY_RUN".equals(plan.path("scope").asText())
                || plan.path("organizations").size() != 96
                || !edu.orgresource.extended.demo.C8HostedDemoPreflight.APPROVED_SOURCE_SHA
                    .equals(plan.path("source_sha256").asText()))
            throw new IllegalArgumentException("The hosted importer requires the exact approved 96-workspace source.");
        importAdditionalPlanForIsolatedProof(plan, planSha, originalOrganizationId, existingOwnerId);
    }

    /**
     * Acceptance-only additive replay into an already populated database.
     * This method has NO live hosted command entry point. A separate private,
     * backup-verified, manually authorized hosted importer is still required.
     */
    public void importAdditionalPlanForIsolatedProof(JsonNode plan, String planSha,
            UUID originalOrganizationId, UUID existingOwnerId) {
        if (organizations.count() < 1 || accounts.count() < 1
                || jdbc.queryForObject("SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1", Integer.class) != 7)
            throw new IllegalStateException("The additive proof requires an existing V7 organization schema.");
        if (plan == null || !plan.path("synthetic").asBoolean(false) || !plan.path("organizations").isArray())
            throw new IllegalArgumentException("A verified synthetic plan is required.");
        UserAccount owner = accounts.findByPublicId(existingOwnerId).orElseThrow(
                () -> new IllegalStateException("The original owner account is missing."));
        Organization original = organizations.findByPublicId(originalOrganizationId).orElseThrow(
                () -> new IllegalStateException("The original organization is missing."));
        if (owner.status != UserAccount.Status.ACTIVE || original.status != Organization.Status.ACTIVE
                || credentials.findByUserId(owner.id).isEmpty())
            throw new IllegalStateException("The original owner is not currently active and credentialed.");
        Membership prior = memberships.findByOrganizationIdAndUserId(original.id, owner.id).orElseThrow(
                () -> new IllegalStateException("The original owner membership is missing."));
        if (prior.status != Membership.Status.ACTIVE || prior.role != Membership.Role.OWNER)
            throw new IllegalStateException("The original active OWNER role must be preserved.");
        assertCount(0, "SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL");
        long originalMembers = jdbc.queryForObject(
                "SELECT COUNT(*) FROM membership WHERE org_id=?", Long.class, original.id);
        long originalResources = jdbc.queryForObject(
                "SELECT COUNT(*) FROM resource WHERE organization_id=?", Long.class, original.id);
        long originalAllocations = jdbc.queryForObject(
                "SELECT COUNT(*) FROM allocation WHERE organization_id=?", Long.class, original.id);
        long originalMovements = jdbc.queryForObject(
                "SELECT COUNT(*) FROM stock_movement WHERE organization_id=?", Long.class, original.id);
        long originalAudits = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE org_id=?", Long.class, original.id);
        // The real account receives current access; its identity must not be
        // used as an actor for fictional historical stock or audit operations.
        if (accounts.findByUsernameKey("c8-demo-historical-actor").isPresent())
            throw new IllegalStateException("The isolated historical persona identity already exists.");
        var keys = new HashSet<UUID>();
        for (JsonNode slice : array(plan, "organizations")) {
            UUID key = id(slice, "id");
            if (!keys.add(key) || organizations.findByPublicId(key).isPresent())
                throw new IllegalStateException("The synthetic cohort conflicts with an existing organization.");
        }
        LocalDateTime wall = LocalDateTime.ofInstant(wallClock.instant(), ZoneOffset.UTC);
        UserAccount historicalActor = transaction.execute(status -> accounts.saveAndFlush(
                new UserAccount("c8-demo-historical-actor", "Simulated historical actor", wall)));
        int requests = 0, actions = 0;
        for (JsonNode slice : array(plan, "organizations")) {
            requests = Math.addExact(requests, array(slice, "allocations").size());
            actions = Math.addExact(actions, array(slice, "events").size());
            transaction.executeWithoutResult(status -> {
                // The simulation is performed as a synthetic OWNER. Current
                // owner access is granted only after history is fully replayed.
                importOrganization(slice, plan, planSha, historicalActor, wall);
                Organization imported = organizations.findByPublicId(id(slice, "id")).orElseThrow();
                Membership syntheticOwner = memberships.findByOrganizationIdAndUserId(imported.id, historicalActor.id)
                        .orElseThrow(() -> new IllegalStateException("The simulated actor membership was lost."));
                if (syntheticOwner.role != Membership.Role.OWNER)
                    throw new IllegalStateException("The simulated actor was not an OWNER during replay.");
                syntheticOwner.role = Membership.Role.MEMBER;
                memberships.saveAndFlush(syntheticOwner);
                memberships.saveAndFlush(new Membership(imported.id, owner.id, Membership.Role.OWNER, wall));
                assertCount(1, """
                        SELECT COUNT(*) FROM membership WHERE org_id=? AND role='OWNER' AND status='ACTIVE'
                        """, imported.id);
            });
        }
        long expectedRequests = requests, expectedActions = actions;
        assertCount(keys.size(), "SELECT COUNT(*) FROM organization WHERE demo_plan_sha=?", planSha);
        assertCount(expectedRequests, """
                SELECT COUNT(*) FROM allocation a JOIN organization o ON o.id=a.organization_id
                WHERE o.demo_plan_sha=?
                """, planSha);
        assertCount(expectedActions, """
                SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id
                WHERE o.demo_plan_sha=? AND a.action LIKE 'C8_DEMO_%'
                """, planSha);
        assertCount(keys.size(), """
                SELECT COUNT(*) FROM membership m JOIN organization o ON o.id=m.org_id
                WHERE o.demo_plan_sha=? AND m.user_id=? AND m.role='OWNER' AND m.status='ACTIVE'
                """, planSha, owner.id);
        assertCount(keys.size(), """
                SELECT COUNT(*) FROM membership m JOIN organization o ON o.id=m.org_id
                WHERE o.demo_plan_sha=? AND m.user_id=? AND m.role='MEMBER' AND m.status='ACTIVE'
                """, planSha, historicalActor.id);
        assertCount(expectedRequests, """
                SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id
                WHERE o.demo_plan_sha=? AND a.action='C8_DEMO_REQUEST' AND a.actor_user_id=?
                """, planSha, historicalActor.id);
        assertCount(0, """
                SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id
                WHERE o.demo_plan_sha=? AND a.action LIKE 'C8_DEMO_%' AND a.actor_user_id=?
                """, planSha, owner.id);
        assertCount(0, """
                SELECT COUNT(*) FROM allocation a JOIN organization o ON o.id=a.organization_id
                WHERE o.demo_plan_sha=? AND (a.sms_consent OR a.phone_ciphertext IS NOT NULL)
                """, planSha);
        assertCount(originalMembers, "SELECT COUNT(*) FROM membership WHERE org_id=?", original.id);
        assertCount(originalResources, "SELECT COUNT(*) FROM resource WHERE organization_id=?", original.id);
        assertCount(originalAllocations, "SELECT COUNT(*) FROM allocation WHERE organization_id=?", original.id);
        assertCount(originalMovements, "SELECT COUNT(*) FROM stock_movement WHERE organization_id=?", original.id);
        assertCount(originalAudits, "SELECT COUNT(*) FROM audit_event WHERE org_id=?", original.id);
        System.out.printf("Isolated additive C8 proof: %d fictional workspaces, %d requests, %d actions, original organization preserved by counts.%n",
                keys.size(), requests, actions);
    }

    private void importOrganization(JsonNode slice, JsonNode plan, String planSha, UserAccount operator, LocalDateTime wall) {
        UUID organizationKey = id(slice, "id");
        Organization org = new Organization("demo-" + organizationKey, text(slice, "directory_label"), wall);
        org.publicId = organizationKey;
        org.demoDatasetId = text(plan, "dataset_id");
        org.demoReferenceKey = text(slice, "reference_key");
        org.demoSourceSha = text(plan, "source_sha256");
        org.demoPlanSha = planSha;
        org.demoAsOf = C8DemoCommand.AS_OF;
        organizations.saveAndFlush(org);

        Map<UUID, Persona> people = new HashMap<>();
        UUID operatorPersona = null;
        for (JsonNode item : array(slice, "personas")) {
            UUID key = id(item, "id");
            Membership.Role role = Membership.Role.valueOf(text(item, "role"));
            if (role != Membership.Role.OWNER && role != Membership.Role.CUSTODIAN
                    && role != Membership.Role.LEAD && role != Membership.Role.MEMBER)
                throw new IllegalArgumentException("Unexpected synthetic persona role.");
            UserAccount account;
            if (role == Membership.Role.OWNER) {
                if (operatorPersona != null) throw new IllegalArgumentException("One owner is expected in each demo workspace.");
                operatorPersona = key;
                account = operator;
            } else {
                String name = text(item, "display_name");
                if (!name.matches("Simulated persona [0-9]{2}")) throw new IllegalArgumentException("Use a synthetic persona label.");
                account = accounts.saveAndFlush(new UserAccount("demo-" + key, name, wall));
            }
            Membership member = new Membership(org.id, account.id, role, wall);
            member.publicId = key;
            memberships.saveAndFlush(member);
            if (people.put(key, new Persona(account, member.publicId)) != null) throw new IllegalArgumentException("Repeated persona.");
        }
        Persona owner = required(people, operatorPersona);

        businessClock.sampleAt(OPENING);
        authenticate(owner.account);
        Map<UUID, Department> teams = new HashMap<>();
        Map<UUID, Resource> stock = new HashMap<>();
        Map<UUID, Project> projectsByKey = new HashMap<>();
        try {
            for (JsonNode item : array(slice, "departments")) {
                String name = simulatedName(item);
                teams.put(id(item, "id"), departments.saveAndFlush(new Department(org.id, name, name.toLowerCase(java.util.Locale.ROOT))));
            }
            var stockActor = inventory.lockPathOrganization(org.publicId, true);
            for (JsonNode item : array(slice, "resources")) {
                UUID key = id(item, "id");
                ApiDtos.CreateResource request = new ApiDtos.CreateResource();
                request.name = simulatedName(item);
                request.type = ResourceType.valueOf(text(item, "kind"));
                request.totalQuantity = integer(item, "initial_quantity");
                stock.put(key, inventory.initializeResource(stockActor, request, key,
                        UUID.nameUUIDFromBytes(("c8-opening:" + key).getBytes(StandardCharsets.UTF_8)), OPENING));
            }
            for (JsonNode item : array(slice, "projects")) {
                UUID key = id(item, "id");
                LocalDateTime created = dateTime(item, "record_at"), start = dateTime(item, "start_at"), end = dateTime(item, "end_at");
                if (created.isBefore(OPENING) || !start.isAfter(created) || !end.isAfter(start))
                    throw new IllegalArgumentException("Invalid demo Project timeline.");
                Project project = new Project(org.id, required(teams, id(item, "department_id")), simulatedName(item), start, end);
                project.publicId = key;
                projectsByKey.put(key, projects.saveAndFlush(project));
            }
            for (JsonNode item : array(slice, "requirements")) {
                var project = required(projectsByKey, id(item, "project_id"));
                var resource = required(stock, id(item, "resource_id"));
                var saved = new edu.orgresource.extended.domain.ResourceRequirement(org.id, project, resource, integer(item, "quantity"));
                saved.publicId = id(item, "id");
                requirements.saveAndFlush(saved);
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
        replay(org, slice, planSha, people);
        assertCount(array(slice, "departments").size(), "SELECT COUNT(*) FROM department WHERE organization_id=?", org.id);
        assertCount(array(slice, "projects").size(), "SELECT COUNT(*) FROM project WHERE organization_id=?", org.id);
        assertCount(array(slice, "resources").size(), "SELECT COUNT(*) FROM resource WHERE organization_id=?", org.id);
        assertCount(array(slice, "requirements").size(), "SELECT COUNT(*) FROM resource_requirement WHERE organization_id=?", org.id);
        assertCount(array(slice, "allocations").size(), "SELECT COUNT(*) FROM allocation WHERE organization_id=?", org.id);
        assertCount(array(slice, "events").size(), "SELECT COUNT(*) FROM audit_event WHERE org_id=? AND action LIKE 'C8_DEMO_%'", org.id);
        reconcileStock(org.id);
    }

    private void replay(Organization org, JsonNode slice, String planSha, Map<UUID, Persona> people) {
        Map<UUID, JsonNode> batches = new HashMap<>();
        for (JsonNode item : array(slice, "allocations"))
            if (batches.put(id(item, "id"), item) != null) throw new IllegalArgumentException("Repeated Allocation ID.");
        var operations = new HashSet<UUID>();
        LocalDateTime prior = OPENING;
        for (JsonNode item : array(slice, "events")) {
            UUID operation = id(item, "id"), allocationKey = id(item, "allocation_id");
            JsonNode batch = required(batches, allocationKey);
            Persona actor = required(people, id(item, "actor_id"));
            LocalDateTime at = dateTime(item, "occurred_at");
            if (at.isBefore(prior) || !at.toLocalDate().isBefore(C8DemoCommand.AS_OF) || !operations.add(operation))
                throw new IllegalArgumentException("The demo event order, range or operation identity is invalid.");
            prior = at;
            businessClock.sampleAt(at);
            authenticate(actor.account);
            try {
                var identity = inventory.lockPathOrganization(org.publicId, true);
                String action = text(item, "action");
                Allocation allocation;
                if ("REQUEST".equals(action)) {
                    var observed = requirements.findByPublicIdAndOrganizationId(id(batch, "requirement_id"), org.id).orElseThrow();
                    var project = projects.lockById(observed.projectId, org.id).orElseThrow();
                    allocation = inventory.persistLinkedRequest(identity, observed, project,
                            required(people, id(batch, "borrower_id")).membershipId, integer(batch, "quantity"), allocationKey);
                } else allocation = inventory.applyHistoricalDemoAction(identity, allocationKey, operation, action, planSha);
                audit.appendAt(org.id, actor.account.id, "C8_DEMO_" + action, "allocation", allocation.publicId.toString(),
                        LocalDateTime.ofInstant(at.atZone(MANILA).toInstant(), ZoneOffset.UTC));
                if (operations.size() % 128 == 0) { entities.flush(); entities.clear(); }
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        entities.flush();
    }

    private void reconcileStock(long org) {
        assertCount(0, """
                SELECT COUNT(*) FROM resource r
                LEFT JOIN (SELECT resource_id,SUM(delta_total) AS total,SUM(delta_issued) AS issued
                    FROM stock_movement WHERE organization_id=? GROUP BY resource_id) m ON m.resource_id=r.id
                LEFT JOIN (SELECT resource_id,SUM(CASE WHEN status='ISSUED' THEN quantity ELSE 0 END) AS issued
                    FROM allocation WHERE organization_id=? GROUP BY resource_id) a ON a.resource_id=r.id
                WHERE r.organization_id=? AND (r.total_quantity<>COALESCE(m.total,0)
                    OR COALESCE(m.issued,0)<>COALESCE(a.issued,0) OR COALESCE(a.issued,0)>r.total_quantity)
                """, org, org, org);
    }

    private void assertCount(long expected, String sql, Object... args) {
        Long actual = jdbc.queryForObject(sql, Long.class, args);
        if (actual == null || expected != actual)
            throw new IllegalStateException("The persisted demo did not match its approved record or stock invariants.");
    }

    private static void authenticate(UserAccount account) {
        var principal = new ExtendedUserPrincipal(account.id, account.credentialVersion, account.usernameKey, null);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
    }

    private static <T> T required(Map<UUID, T> values, UUID key) {
        T item = values.get(key);
        if (item == null) throw new IllegalArgumentException("A synthetic reference is missing from this workspace.");
        return item;
    }

    private static JsonNode array(JsonNode object, String field) {
        JsonNode node = object.path(field);
        if (!node.isArray()) throw new IllegalArgumentException("Expected a synthetic array: " + field);
        return node;
    }

    private static String text(JsonNode object, String field) {
        JsonNode node = object.path(field);
        if (!node.isTextual() || node.textValue().isBlank()) throw new IllegalArgumentException("Missing synthetic text: " + field);
        return node.textValue();
    }

    private static UUID id(JsonNode object, String field) { return UUID.fromString(text(object, field)); }
    private static LocalDateTime dateTime(JsonNode object, String field) { return LocalDateTime.parse(text(object, field)); }

    private static int integer(JsonNode object, String field) {
        JsonNode node = object.path(field);
        if (!node.isIntegralNumber() || !node.canConvertToInt() || node.intValue() < 1 || node.intValue() > 1_000_000)
            throw new IllegalArgumentException("Invalid synthetic quantity: " + field);
        return node.intValue();
    }

    private static String simulatedName(JsonNode object) {
        String name = text(object, "name");
        if (!name.matches("Demo [A-Za-z ]{1,60} [0-9]{2,3}")) throw new IllegalArgumentException("A record requires an original demo label.");
        return name;
    }

    private record Persona(UserAccount account, UUID membershipId) { }
}

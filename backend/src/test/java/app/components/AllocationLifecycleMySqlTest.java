package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.SpringBootApp;
import app.dto.ApiDtos.AllocationView;
import app.dto.ApiDtos.ProjectView;
import app.dto.ApiDtos.ResourceAvailabilityView;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.CancellationReason;
import app.entities.Department;
import app.entities.Project;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.entities.ResourceType;
import app.repositories.AllocationRepository;
import app.repositories.DepartmentRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "orgresource.expiry.scheduler-enabled=true",
        "orgresource.expiry.fixed-delay-ms=100",
        "orgresource.expiry.batch-size=100"
})
public class AllocationLifecycleMySqlTest {
    @Autowired private AllocationComponent allocationComponent;
    @Autowired private ResourceComponent resourceComponent;
    @Autowired private RequirementComponent requirementComponent;
    @Autowired private Clock philippineClock;
    @Autowired private DepartmentRepository departments;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private AllocationRepository allocations;
    @Autowired private PlatformTransactionManager transactionManager;
    @PersistenceContext private EntityManager entityManager;

    private final FixtureIds fixtureIds = new FixtureIds();

    @After
    public void removeOnlyThisTestRunFixtures() {
        for (Long id : fixtureIds.allocations) if (allocations.existsById(id)) allocations.deleteById(id);
        for (Long id : fixtureIds.requirements) requirements.deleteById(id);
        for (Long id : fixtureIds.projects) projects.deleteById(id);
        for (Long id : fixtureIds.resources) resources.deleteById(id);
        for (Long id : fixtureIds.departments) departments.deleteById(id);
    }

    @Test
    public void realMySqlLifecycleCoversReserveIssueReturnConsumeCancelDeleteAndConsent() throws Exception {
        Fixture reusable = fixture(ResourceType.REUSABLE, 10, 6, true);
        Long reusableId = createAllocation(reusable, 4, false);
        assertEquals(Integer.valueOf(4), allocations.findById(reusableId).get().quantity);
        AllocationView approved = allocationComponent.approve(reusableId);
        assertEquals(AllocationStatus.APPROVED, approved.status);
        assertEquals(Integer.valueOf(6), availability(reusable.resource.id).availableQuantity);
        assertEquals(4, allocationComponent.issue(reusableId).status == AllocationStatus.ISSUED
                ? allocations.findById(reusableId).get().quantity : -1);
        assertEquals(Integer.valueOf(6), availability(reusable.resource.id).availableQuantity);
        AllocationView returned = allocationComponent.returnItems(reusableId);
        assertEquals(AllocationStatus.RETURNED, returned.status);
        assertEquals(Integer.valueOf(10), availability(reusable.resource.id).availableQuantity);
        assertEquals(Integer.valueOf(4), requirementComponent.list(reusable.project.id).get(0).allocatedQuantity);

        Fixture consumable = fixture(ResourceType.CONSUMABLE, 100, 40, true);
        Long consumableId = createAllocation(consumable, 30, false);
        allocationComponent.approve(consumableId);
        assertEquals(Integer.valueOf(70), availability(consumable.resource.id).availableQuantity);
        AllocationView consumed = allocationComponent.consume(consumableId);
        assertEquals(AllocationStatus.CONSUMED, consumed.status);
        assertEquals(Integer.valueOf(70), resources.findById(consumable.resource.id).get().getTotalQuantity());
        assertEquals(Integer.valueOf(70), availability(consumable.resource.id).availableQuantity);
        assertEquals(Integer.valueOf(30), requirementComponent.list(consumable.project.id).get(0).allocatedQuantity);
        expectApiError("INVALID_ALLOCATION_STATE", () -> allocationComponent.consume(consumableId));
        assertEquals(Integer.valueOf(70), resources.findById(consumable.resource.id).get().getTotalQuantity());

        Fixture pending = fixture(ResourceType.REUSABLE, 5, 5, true);
        Long pendingId = createAllocation(pending, 2, false);
        AllocationView cancelled = allocationComponent.cancel(pendingId);
        assertEquals(AllocationStatus.CANCELLED, cancelled.status);
        assertEquals("USER_REQUEST", cancelled.cancellationReason);
        allocationComponent.delete(pendingId);
        assertFalse(allocations.existsById(pendingId));

        Long previouslyApprovedId = createAllocation(reusable, 2, false);
        AllocationView historicallyApproved = allocationComponent.approve(previouslyApprovedId);
        LocalDateTime approvedAt = historicallyApproved.approvedAt;
        AllocationView cancelledApproval = allocationComponent.cancel(previouslyApprovedId);
        assertEquals(AllocationStatus.CANCELLED, cancelledApproval.status);
        assertDatabaseTimestampEqual(approvedAt, cancelledApproval.approvedAt);
        expectApiError("HISTORY_PROTECTED", () -> allocationComponent.delete(previouslyApprovedId));

        Long consentId = createAllocation(reusable, 1, true);
        Allocation beforeRevocation = allocations.findById(consentId).get();
        LocalDateTime createdAt = beforeRevocation.createdAt;
        AllocationView revoked = allocationComponent.revokeSmsConsent(consentId);
        AllocationView revokedAgain = allocationComponent.revokeSmsConsent(consentId);
        assertFalse(revoked.contactPresent);
        assertEquals(Boolean.FALSE, revoked.smsConsent);
        assertEquals(AllocationStatus.PENDING, revokedAgain.status);
        assertDatabaseTimestampEqual(createdAt, revokedAgain.createdAt);
        assertFalse(new ObjectMapper().writeValueAsString(revokedAgain).contains("+15550001111"));
    }

    @Test
    public void concurrentApprovalSerializesStockDemandResourceEditsProjectDatesAndRequirementChanges() throws Exception {
        Resource shared = resource(ResourceType.REUSABLE, 5);
        Fixture first = requirement(shared, 4, true);
        Fixture second = requirement(shared, 4, true);
        Long firstId = createAllocation(first, 4, false);
        Long secondId = createAllocation(second, 4, false);
        List<String> stockRace = race(() -> allocationComponent.approve(firstId),
                () -> allocationComponent.approve(secondId));
        assertEquals(1, count(stockRace, "OK"));
        assertEquals(1, count(stockRace, "INSUFFICIENT_STOCK"));
        assertTrue(availability(shared.id).availableQuantity >= 0);
        assertEquals(Integer.valueOf(5), resources.findById(shared.id).get().getTotalQuantity());

        Fixture demand = fixture(ResourceType.REUSABLE, 10, 6, true);
        Long demandA = createAllocation(demand, 4, false);
        Long demandB = createAllocation(demand, 4, false);
        List<String> demandRace = race(() -> allocationComponent.approve(demandA),
                () -> allocationComponent.approve(demandB));
        assertEquals(1, count(demandRace, "OK"));
        assertEquals(1, count(demandRace, "REQUIREMENT_ALREADY_COVERED"));
        int covered = requirementComponent.list(demand.project.id).get(0).allocatedQuantity;
        assertTrue(covered <= requirements.findById(demand.requirement.id).get().quantity);

        Fixture resourceRace = fixture(ResourceType.REUSABLE, 5, 5, true);
        Long resourceAllocation = createAllocation(resourceRace, 4, false);
        List<String> correctionRace = race(
                () -> resourceComponent.update(resourceRace.resource.id, resourceRace.resource.name,
                        0, 5),
                () -> allocationComponent.approve(resourceAllocation));
        assertEquals(1, count(correctionRace, "OK"));
        Resource corrected = resources.findById(resourceRace.resource.id).get();
        Allocation correctedAllocation = allocations.findById(resourceAllocation).get();
        long commitments = allocations.sumIssuedCustody(corrected.id, AllocationStatus.ISSUED) == null
                ? 0L : allocations.sumIssuedCustody(corrected.id, AllocationStatus.ISSUED);
        Long holds = allocations.sumEffectiveHolds(corrected.id, AllocationStatus.APPROVED,
                LocalDateTime.now(philippineClock));
        commitments += holds == null ? 0L : holds;
        assertTrue(corrected.getTotalQuantity().longValue() >= commitments);
        assertTrue(correctedAllocation.status == AllocationStatus.PENDING
                || correctedAllocation.status == AllocationStatus.APPROVED);

        Fixture requirementRace = fixture(ResourceType.REUSABLE, 10, 6, true);
        Long requirementAllocation = createAllocation(requirementRace, 4, false);
        List<String> reductionRace = race(
                () -> requirementComponent.update(requirementRace.requirement.id, 3),
                () -> allocationComponent.approve(requirementAllocation));
        assertEquals(1, count(reductionRace, "OK"));
        int finalDemand = requirements.findById(requirementRace.requirement.id).get().quantity;
        int finalCoverage = requirementComponent.list(requirementRace.project.id).get(0).allocatedQuantity;
        assertTrue(finalCoverage <= finalDemand);

        Fixture projectRace = fixture(ResourceType.REUSABLE, 10, 5, true);
        Long projectAllocation = createAllocation(projectRace, 3, false);
        LocalDateTime shiftedStart = LocalDateTime.now(philippineClock).plusHours(1);
        List<String> dateRace = race(
                () -> projectsComponentUpdate(projectRace, shiftedStart, shiftedStart.plusHours(1)),
                () -> allocationComponent.approve(projectAllocation));
        assertEquals("OK", dateRace.get(0).equals("OK") ? dateRace.get(0) : dateRace.get(1));
        assertEquals(AllocationStatus.APPROVED, allocations.findById(projectAllocation).get().status);
        assertNotNull(allocations.findById(projectAllocation).get().approvedAt);
        if (projects.findById(projectRace.project.id).get().startAt.equals(shiftedStart)) {
            assertTrue(projects.findById(projectRace.project.id).get().startAt.isAfter(LocalDateTime.now(philippineClock)));
        }
    }

    @Test
    public void consumableFailureAfterResourceFlushRollsBackBothRows() {
        Fixture fixture = fixture(ResourceType.CONSUMABLE, 50, 20, true);
        Long allocationId = createAllocation(fixture, 10, false);
        allocationComponent.approve(allocationId);
        AllocationRepository failAllocationWrite = (AllocationRepository) Proxy.newProxyInstance(
                AllocationRepository.class.getClassLoader(), new Class<?>[] { AllocationRepository.class },
                (proxy, method, arguments) -> {
                    if ("saveAndFlush".equals(method.getName()) && arguments[0] instanceof Allocation) {
                        throw new IllegalStateException("forced failure after Resource flush");
                    }
                    try {
                        return method.invoke(allocations, arguments);
                    } catch (InvocationTargetException error) {
                        throw error.getCause();
                    }
                });
        AllocationComponent failingComponent = new AllocationComponent();
        ReflectionTestUtils.setField(failingComponent, "allocations", failAllocationWrite);
        ReflectionTestUtils.setField(failingComponent, "requirements", requirements);
        ReflectionTestUtils.setField(failingComponent, "projects", projects);
        ReflectionTestUtils.setField(failingComponent, "resources", resources);
        ReflectionTestUtils.setField(failingComponent, "philippineClock", philippineClock);
        ReflectionTestUtils.setField(failingComponent, "entityManager", entityManager);
        try {
            new TransactionTemplate(transactionManager).execute(transaction -> {
                failingComponent.consume(allocationId);
                return null;
            });
            throw new AssertionError("Expected injected allocation flush failure.");
        } catch (IllegalStateException expected) {
            assertEquals("forced failure after Resource flush", expected.getMessage());
        }

        assertEquals(Integer.valueOf(50), resources.findById(fixture.resource.id).get().getTotalQuantity());
        Allocation unchanged = allocations.findById(allocationId).get();
        assertEquals(AllocationStatus.APPROVED, unchanged.status);
        assertEquals(null, unchanged.consumedAt);
        assertEquals(Integer.valueOf(40), availability(fixture.resource.id).availableQuantity);
    }

    @Test
    public void scheduledSpringScanExpiresPendingAndApprovedButNeverAutoReturnsIssued() throws Exception {
        Fixture pending = fixture(ResourceType.REUSABLE, 10, 10, true);
        Long pendingId = createAllocation(pending, 2, false);

        Fixture approved = fixture(ResourceType.CONSUMABLE, 10, 10, true);
        Long approvedId = createAllocation(approved, 3, false);
        LocalDateTime approvedAt = allocationComponent.approve(approvedId).approvedAt;

        Fixture issued = fixture(ResourceType.REUSABLE, 10, 10, false);
        Allocation issuedRow = new Allocation(issued.requirement, 2, "O4 overdue issued fixture",
                null, false, LocalDateTime.now(philippineClock).minusDays(2));
        issuedRow.status = AllocationStatus.ISSUED;
        issuedRow.issuedAt = LocalDateTime.now(philippineClock).minusDays(1);
        issuedRow.approvedAt = issuedRow.createdAt.minusHours(1);
        issuedRow = allocations.saveAndFlush(issuedRow);
        fixtureIds.allocations.add(issuedRow.id);

        LocalDateTime pastEnd = LocalDateTime.now(philippineClock).minusSeconds(1);
        pending.project.endAt = pastEnd;
        projects.saveAndFlush(pending.project);
        approved.project.endAt = pastEnd;
        projects.saveAndFlush(approved.project);
        issued.project.endAt = pastEnd;
        projects.saveAndFlush(issued.project);

        awaitCancelled(pendingId);
        awaitCancelled(approvedId);
        Allocation expiredApproval = allocations.findById(approvedId).get();
        assertDatabaseTimestampEqual(approvedAt, expiredApproval.approvedAt);
        assertEquals(CancellationReason.EXPIRED, expiredApproval.cancellationReason);
        assertEquals(AllocationStatus.CANCELLED, allocations.findById(pendingId).get().status);
        assertEquals(AllocationStatus.ISSUED, allocations.findById(issuedRow.id).get().status);
        assertEquals(Integer.valueOf(0), requirementComponent.list(approved.project.id).get(0).allocatedQuantity);
        assertEquals(Integer.valueOf(8), availability(issued.resource.id).availableQuantity);
        LocalDateTime cancelledAt = expiredApproval.cancelledAt;
        assertFalse(allocationComponent.expireIfEligible(approvedId));
        assertDatabaseTimestampEqual(cancelledAt, allocations.findById(approvedId).get().cancelledAt);
    }

    @Test
    public void expiryRacesWithManualCancelAndConsumeWithoutChangingStockTwice() throws Exception {
        Fixture pending = fixture(ResourceType.REUSABLE, 6, 6, true);
        Long pendingId = createAllocation(pending, 2, false);
        pending.project.endAt = LocalDateTime.now(philippineClock).minusSeconds(1);
        projects.saveAndFlush(pending.project);
        List<String> cancelRace = race(() -> allocationComponent.cancel(pendingId),
                () -> allocationComponent.expireIfEligible(pendingId));
        assertTrue(cancelRace.contains("OK"));
        Allocation cancelled = allocations.findById(pendingId).get();
        assertEquals(AllocationStatus.CANCELLED, cancelled.status);
        assertEquals(CancellationReason.EXPIRED, cancelled.cancellationReason);
        assertEquals(Integer.valueOf(6), availability(pending.resource.id).availableQuantity);

        Fixture consumable = fixture(ResourceType.CONSUMABLE, 12, 8, true);
        Long consumableId = createAllocation(consumable, 4, false);
        allocationComponent.approve(consumableId);
        consumable.project.endAt = LocalDateTime.now(philippineClock).minusSeconds(1);
        projects.saveAndFlush(consumable.project);
        List<String> consumeRace = race(() -> allocationComponent.expireIfEligible(consumableId),
                () -> allocationComponent.consume(consumableId));
        assertTrue(consumeRace.contains("OK"));
        Allocation afterRace = allocations.findById(consumableId).get();
        assertEquals(AllocationStatus.CANCELLED, afterRace.status);
        assertEquals(CancellationReason.EXPIRED, afterRace.cancellationReason);
        assertEquals(Integer.valueOf(12), resources.findById(consumable.resource.id).get().getTotalQuantity());
    }

    private ProjectView projectsComponentUpdate(Fixture fixture, LocalDateTime startAt, LocalDateTime endAt) {
        return projectsComponent.update(fixture.project.id, fixture.project.name, startAt, endAt);
    }

    @Autowired private ProjectComponent projectsComponent;

    private Fixture fixture(ResourceType type, int total, int demand, boolean active) {
        return requirement(resource(type, total), demand, active);
    }

    private Resource resource(ResourceType type, int total) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String name = "O4 Resource " + suffix;
        Resource saved = resources.saveAndFlush(new Resource(name, name.toLowerCase(java.util.Locale.ROOT), type, total));
        fixtureIds.resources.add(saved.id);
        return saved;
    }

    private Fixture requirement(Resource resource, int demand, boolean active) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String departmentName = "O4 Department " + suffix;
        Department department = departments.saveAndFlush(new Department(
                departmentName, departmentName.toLowerCase(java.util.Locale.ROOT)));
        fixtureIds.departments.add(department.id);
        LocalDateTime now = LocalDateTime.now(philippineClock);
        LocalDateTime start = active ? now.minusHours(1) : now.minusDays(3);
        LocalDateTime end = active ? now.plusDays(2) : now.minusDays(1);
        Project project = projects.saveAndFlush(new Project(department, "O4 Project " + suffix, start, end));
        fixtureIds.projects.add(project.id);
        ResourceRequirement requirement = requirements.saveAndFlush(new ResourceRequirement(project, resource, demand));
        fixtureIds.requirements.add(requirement.id);
        return new Fixture(project, resource, requirement);
    }

    private Long createAllocation(Fixture fixture, int quantity, boolean consent) {
        AllocationView result = allocationComponent.create(fixture.requirement.id, quantity,
                "O4 fictional representative", consent ? "+15550001111" : null, consent);
        fixtureIds.allocations.add(result.id);
        return result.id;
    }

    private ResourceAvailabilityView availability(Long resourceId) {
        return resourceComponent.getAvailability(resourceId);
    }

    private void awaitCancelled(Long allocationId) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (System.nanoTime() < deadline) {
            Allocation allocation = allocations.findById(allocationId).orElse(null);
            if (allocation != null && allocation.status == AllocationStatus.CANCELLED
                    && allocation.cancellationReason == CancellationReason.EXPIRED) return;
            Thread.sleep(20L);
        }
        throw new AssertionError("The Spring @Scheduled scan did not persist EXPIRED cancellation for Allocation " + allocationId);
    }

    private List<String> race(Callable<?> firstAction, Callable<?> secondAction) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<String> first = executor.submit(raceCall(firstAction, ready, start));
            Future<String> second = executor.submit(raceCall(secondAction, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            return Arrays.asList(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<String> raceCall(Callable<?> action, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Concurrency gate timed out.");
            try {
                action.call();
                return "OK";
            } catch (ApiException conflict) {
                return conflict.code;
            }
        };
    }

    private int count(List<String> outcomes, String expected) {
        int found = 0;
        for (String outcome : outcomes) if (expected.equals(outcome)) found++;
        return found;
    }

    private void expectApiError(String code, Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected API error " + code);
        } catch (ApiException error) {
            assertEquals(409, error.status);
            assertEquals(code, error.code);
        }
    }

    private void assertDatabaseTimestampEqual(LocalDateTime expected, LocalDateTime actual) {
        assertNotNull(actual);
        assertTrue("Timestamp changed beyond MySQL DATETIME fractional precision.",
                Duration.between(expected, actual).abs().toNanos() <= 1000L);
    }

    private static final class Fixture {
        private final Project project;
        private final Resource resource;
        private final ResourceRequirement requirement;

        private Fixture(Project project, Resource resource, ResourceRequirement requirement) {
            this.project = project;
            this.resource = resource;
            this.requirement = requirement;
        }
    }

    private static final class FixtureIds {
        private final List<Long> departments = new ArrayList<>();
        private final List<Long> projects = new ArrayList<>();
        private final List<Long> resources = new ArrayList<>();
        private final List<Long> requirements = new ArrayList<>();
        private final List<Long> allocations = new ArrayList<>();
    }
}

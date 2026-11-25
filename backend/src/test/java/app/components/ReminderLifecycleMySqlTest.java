package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import app.SpringBootApp;
import app.dto.ApiDtos.AllocationView;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.Department;
import app.entities.Project;
import app.entities.ReminderOutcome;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.entities.ResourceType;
import app.reminders.ReminderSendResult;
import app.reminders.ReminderTransport;
import app.repositories.AllocationRepository;
import app.repositories.DepartmentRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = "orgresource.reminders.mode=FAKE_TWILIO")
public class ReminderLifecycleMySqlTest {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 12, 0);

    @Autowired private ReminderComponent reminders;
    @Autowired private ReminderDispatcher dispatcher;
    @Autowired private AllocationComponent allocationComponent;
    @Autowired private DepartmentRepository departments;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private AllocationRepository allocations;
    @MockBean private Clock philippineClock;
    @MockBean private ReminderTransport transport;

    private final List<Long> departmentIds = new ArrayList<Long>();
    private final List<Long> projectIds = new ArrayList<Long>();
    private final List<Long> resourceIds = new ArrayList<Long>();
    private final List<Long> requirementIds = new ArrayList<Long>();
    private final List<Long> allocationIds = new ArrayList<Long>();

    @Before
    public void useFixedManilaClock() {
        when(philippineClock.instant()).thenReturn(NOW.atZone(MANILA).toInstant());
        when(philippineClock.getZone()).thenReturn(MANILA);
        when(transport.send(any())).thenReturn(ReminderSendResult.simulated(200, null));
    }

    @After
    public void removeOnlyThisTestFixtures() {
        for (Long id : allocationIds) if (allocations.existsById(id)) allocations.deleteById(id);
        for (Long id : requirementIds) if (requirements.existsById(id)) requirements.deleteById(id);
        for (Long id : projectIds) if (projects.existsById(id)) projects.deleteById(id);
        for (Long id : resourceIds) if (resources.existsById(id)) resources.deleteById(id);
        for (Long id : departmentIds) if (departments.existsById(id)) departments.deleteById(id);
    }

    @Test
    public void dispatchClaimsOncePersistsOnlyResultAndKeepsContactOutOfPublicDto() throws Exception {
        Fixture fixture = fixture(NOW.plusHours(2), "+15550001111", true);

        dispatcher.dispatchOne(fixture.allocation.id);
        dispatcher.dispatchOne(fixture.allocation.id);

        Allocation saved = allocations.findById(fixture.allocation.id).get();
        assertEquals(AllocationStatus.ISSUED, saved.status);
        assertEquals(ReminderOutcome.SIMULATED, saved.reminderOutcome);
        assertEquals(Integer.valueOf(200), saved.reminderHttpStatus);
        assertNotNull(saved.reminderClaimedAt);
        assertEquals("+15550001111", saved.phoneNumber);
        verify(transport, times(1)).send(any());

        AllocationView publicView = allocationComponent.list(null, null).stream()
                .filter(view -> view.id.equals(fixture.allocation.id)).findFirst().get();
        assertEquals(Boolean.TRUE, publicView.contactPresent);
        assertEquals("SIMULATED", publicView.reminderOutcome);
        try {
            AllocationView.class.getField("phoneNumber");
            throw new AssertionError("Public AllocationView must not contain a phone number field.");
        } catch (NoSuchFieldException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void providerAcceptanceIsPersistedAsAcceptanceAndNeverAsDelivery() {
        Fixture fixture = fixture(NOW.plusHours(2), "+15550001116", true);
        String fictionalSid = "SM00000000000000000000000000000000";
        when(transport.send(any())).thenReturn(ReminderSendResult.providerAccepted(201, fictionalSid));

        dispatcher.dispatchOne(fixture.allocation.id);

        Allocation saved = allocations.findById(fixture.allocation.id).get();
        assertEquals(ReminderOutcome.PROVIDER_ACCEPTED, saved.reminderOutcome);
        assertEquals(Integer.valueOf(201), saved.reminderHttpStatus);
        assertEquals(fictionalSid, saved.providerMessageSid);
        assertNotNull(saved.reminderClaimedAt);
        assertEquals("PROVIDER_ACCEPTED", allocationComponent.list(null, null).stream()
                .filter(view -> view.id.equals(fixture.allocation.id)).findFirst().get().reminderOutcome);
    }

    @Test
    public void finalCheckSuppressesRevokedConsentAndStaleClaimNeverResends() {
        Fixture revoked = fixture(NOW.plusHours(2), "+15550001112", true);
        assertTrue(reminders.claimOne(revoked.allocation.id));
        allocationComponent.revokeSmsConsent(revoked.allocation.id);
        assertNull(reminders.prepareForDispatch(revoked.allocation.id));
        assertEquals(ReminderOutcome.SUPPRESSED,
                allocations.findById(revoked.allocation.id).get().reminderOutcome);

        Fixture stale = fixture(NOW.plusHours(3), "+15550001113", true);
        assertTrue(reminders.claimOne(stale.allocation.id));
        assertTrue(reminders.markStaleClaimUnknown(stale.allocation.id, NOW.plusSeconds(1)));
        dispatcher.dispatchOne(stale.allocation.id);
        assertEquals(ReminderOutcome.UNKNOWN,
                allocations.findById(stale.allocation.id).get().reminderOutcome);
        verify(transport, never()).send(any());
    }

    @Test
    public void claimUsesExclusiveTwentyFourHourUpperBoundaryAndSerializesConcurrentWorkers() throws Exception {
        Fixture atUpperBoundary = fixture(NOW.plusHours(24), "+15550001114", true);
        assertFalse(reminders.claimOne(atUpperBoundary.allocation.id));
        assertEquals(ReminderOutcome.NONE,
                allocations.findById(atUpperBoundary.allocation.id).get().reminderOutcome);

        Fixture concurrent = fixture(NOW, "+15550001115", true);
        final CountDownLatch ready = new CountDownLatch(2);
        final CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> claimAtSameTime(concurrent.allocation.id, ready, start));
            Future<Boolean> second = executor.submit(() -> claimAtSameTime(concurrent.allocation.id, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            boolean firstClaim = first.get(20, TimeUnit.SECONDS);
            boolean secondClaim = second.get(20, TimeUnit.SECONDS);
            assertTrue(firstClaim ^ secondClaim);
            assertEquals(ReminderOutcome.CLAIMED,
                    allocations.findById(concurrent.allocation.id).get().reminderOutcome);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void invalidDestinationIsSuppressedWithoutDurableClaim() {
        Fixture invalid = fixture(NOW.plusHours(1), "not-e164", true);
        assertFalse(reminders.claimOne(invalid.allocation.id));
        Allocation saved = allocations.findById(invalid.allocation.id).get();
        assertEquals(ReminderOutcome.SUPPRESSED, saved.reminderOutcome);
        assertNull(saved.reminderClaimedAt);
        verify(transport, never()).send(any());
    }

    private boolean claimAtSameTime(Long allocationId, CountDownLatch ready, CountDownLatch start)
            throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Concurrent claim gate timed out.");
        return reminders.claimOne(allocationId);
    }

    private Fixture fixture(LocalDateTime endAt, String phoneNumber, boolean consent) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String departmentName = "O6 Reminder department " + suffix;
        Department department = departments.saveAndFlush(new Department(
                departmentName, departmentName.toLowerCase(java.util.Locale.ROOT)));
        departmentIds.add(department.id);
        Resource resource = resources.saveAndFlush(new Resource("O6 Reminder resource " + suffix,
                "o6 reminder resource " + suffix, ResourceType.REUSABLE, 20));
        resourceIds.add(resource.id);
        Project project = projects.saveAndFlush(new Project(department, "O6 Reminder project " + suffix,
                NOW.minusHours(1), endAt));
        projectIds.add(project.id);
        ResourceRequirement requirement = requirements.saveAndFlush(new ResourceRequirement(project, resource, 5));
        requirementIds.add(requirement.id);
        Allocation allocation = new Allocation(requirement, 2, "Fictional reminder recipient",
                phoneNumber, consent, NOW.minusHours(2));
        allocation.status = AllocationStatus.ISSUED;
        allocation.approvedAt = NOW.minusHours(2);
        allocation.issuedAt = NOW.minusHours(1);
        Allocation saved = allocations.saveAndFlush(allocation);
        allocationIds.add(saved.id);
        return new Fixture(saved);
    }

    private static final class Fixture {
        private final Allocation allocation;
        private Fixture(Allocation allocation) { this.allocation = allocation; }
    }
}

package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import app.dto.ApiDtos.AllocationView;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.Department;
import app.entities.Project;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.entities.ResourceType;
import app.repositories.AllocationRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRepository;
import app.repositories.ResourceRequirementRepository;

public class AllocationComponentTest {
    private AllocationComponent component;
    private AllocationRepository allocations;
    private ProjectRepository projects;
    private ResourceRepository resources;
    private ResourceRequirementRepository requirements;
    private Project project;
    private Resource resource;
    private ResourceRequirement requirement;
    private Allocation allocation;
    private LocalDateTime now;

    @Before
    public void setUp() {
        component = new AllocationComponent();
        allocations = org.mockito.Mockito.mock(AllocationRepository.class);
        projects = org.mockito.Mockito.mock(ProjectRepository.class);
        resources = org.mockito.Mockito.mock(ResourceRepository.class);
        requirements = org.mockito.Mockito.mock(ResourceRequirementRepository.class);
        ReflectionTestUtils.setField(component, "allocations", allocations);
        ReflectionTestUtils.setField(component, "projects", projects);
        ReflectionTestUtils.setField(component, "resources", resources);
        ReflectionTestUtils.setField(component, "requirements", requirements);
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T01:00:00Z"), ZoneId.of("Asia/Manila"));
        ReflectionTestUtils.setField(component, "philippineClock", clock);
        now = LocalDateTime.now(clock);

        Department department = new Department("Test department", "test department");
        department.id = 1L;
        project = new Project(department, "Test project", now, now.plusHours(2));
        project.id = 2L;
        resource = new Resource("Test reusable", "test reusable", ResourceType.REUSABLE, 5);
        resource.id = 3L;
        requirement = new ResourceRequirement(project, resource, 5);
        requirement.id = 4L;
        allocation = new Allocation(requirement, 2, "Fictional borrower", null, false, now);
        allocation.id = 5L;
        allocation.status = AllocationStatus.APPROVED;
        allocation.approvedAt = now.minusSeconds(1);
        when(allocations.findById(5L)).thenReturn(Optional.of(allocation));
        when(requirements.findById(4L)).thenReturn(Optional.of(requirement));
        when(projects.lockById(2L)).thenReturn(Optional.of(project));
        when(resources.lockById(3L)).thenReturn(Optional.of(resource));
        when(requirements.lockById(4L)).thenReturn(Optional.of(requirement));
        when(allocations.lockById(5L)).thenReturn(Optional.of(allocation));
        when(allocations.saveAndFlush(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void approvalIsAllowedBeforeProjectStartAndUsesTheProjectFirstLockOrder() {
        project.startAt = now.plusMinutes(1);
        project.endAt = now.plusHours(1);
        allocation.status = AllocationStatus.PENDING;
        allocation.approvedAt = null;
        when(allocations.sumCoveredQuantity(eq(4L), anyList(), eq(AllocationStatus.APPROVED), eq(now)))
                .thenReturn(null);
        when(allocations.sumEffectiveHolds(3L, AllocationStatus.APPROVED, now)).thenReturn(null);
        when(allocations.sumIssuedCustody(3L, AllocationStatus.ISSUED)).thenReturn(null);

        AllocationView result = component.approve(5L);

        assertEquals(AllocationStatus.APPROVED, result.status);
        assertEquals(now, result.approvedAt);
        InOrder order = inOrder(projects, resources, requirements, allocations);
        order.verify(projects).lockById(2L);
        order.verify(resources).lockById(3L);
        order.verify(requirements).lockById(4L);
        order.verify(allocations).lockById(5L);
        verify(allocations).saveAndFlush(allocation);
    }

    @Test
    public void issueAllowsInclusiveStartBoundary() {
        project.startAt = now;
        project.endAt = now.plusHours(1);

        AllocationView result = component.issue(5L);

        assertEquals(AllocationStatus.ISSUED, result.status);
        assertEquals(now, result.issuedAt);
    }

    @Test
    public void issueRefusesExclusiveEndBoundaryWithoutMutation() {
        project.startAt = now.minusHours(1);
        project.endAt = now;

        expectApiError("OUTSIDE_PROJECT_PERIOD", () -> component.issue(5L));

        assertEquals(AllocationStatus.APPROVED, allocation.status);
        assertNotNull(allocation.approvedAt);
    }

    private void expectApiError(String code, Runnable action) {
        try {
            action.run();
            fail("Expected API error " + code);
        } catch (ApiException error) {
            assertEquals(409, error.status);
            assertEquals(code, error.code);
        }
    }
}

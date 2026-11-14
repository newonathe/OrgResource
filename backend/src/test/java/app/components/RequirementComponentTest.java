package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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

import app.dto.ApiDtos.MessageView;
import app.dto.ApiDtos.RequirementView;
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

public class RequirementComponentTest {
    private RequirementComponent component;
    private ResourceRequirementRepository requirements;
    private ProjectRepository projects;
    private ResourceRepository resources;
    private AllocationRepository allocations;
    private Project project;
    private Resource resource;
    private ResourceRequirement existing;
    private LocalDateTime now;

    @Before
    public void setUp() {
        component = new RequirementComponent();
        requirements = org.mockito.Mockito.mock(ResourceRequirementRepository.class);
        projects = org.mockito.Mockito.mock(ProjectRepository.class);
        resources = org.mockito.Mockito.mock(ResourceRepository.class);
        allocations = org.mockito.Mockito.mock(AllocationRepository.class);
        ReflectionTestUtils.setField(component, "requirements", requirements);
        ReflectionTestUtils.setField(component, "projects", projects);
        ReflectionTestUtils.setField(component, "resources", resources);
        ReflectionTestUtils.setField(component, "allocations", allocations);
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"), ZoneId.of("Asia/Manila"));
        ReflectionTestUtils.setField(component, "philippineClock", clock);
        now = LocalDateTime.now(clock);

        Department department = new Department("Test department", "test department");
        department.id = 2L;
        project = new Project(department, "Test project", now.plusDays(1), now.plusDays(3));
        project.id = 7L;
        resource = new Resource("Test paper", "test paper", ResourceType.CONSUMABLE, 100);
        resource.id = 11L;
        existing = new ResourceRequirement(project, resource, 10);
        existing.id = 31L;
    }

    @Test
    public void createLocksParentsInProjectThenResourceOrderAndReturnsZeroCoverage() {
        when(projects.lockById(7L)).thenReturn(Optional.of(project));
        when(resources.lockById(11L)).thenReturn(Optional.of(resource));
        when(requirements.existsByProject_IdAndResource_Id(7L, 11L)).thenReturn(false);
        when(requirements.save(org.mockito.ArgumentMatchers.any(ResourceRequirement.class))).thenAnswer(invocation -> {
            ResourceRequirement saved = (ResourceRequirement) invocation.getArguments()[0];
            saved.id = 32L;
            return saved;
        });
        when(allocations.sumCoveredQuantity(eq(32L), anyList(), eq(AllocationStatus.APPROVED), eq(now)))
                .thenReturn(null);

        RequirementView result = component.create(7L, 11L, 6);

        assertEquals(Integer.valueOf(6), result.quantity);
        assertEquals(Integer.valueOf(0), result.allocatedQuantity);
        assertEquals(Integer.valueOf(6), result.shortageQuantity);
        InOrder order = inOrder(projects, resources);
        order.verify(projects).lockById(7L);
        order.verify(resources).lockById(11L);
    }

    @Test
    public void updateUsesTheMaximumOfCoverageAndLargestPendingRequest() {
        prepareForUpdate();
        when(allocations.sumCoveredQuantity(eq(31L), anyList(), eq(AllocationStatus.APPROVED), eq(now)))
                .thenReturn(3L);
        when(allocations.maxPendingQuantity(31L, AllocationStatus.PENDING)).thenReturn(5);
        when(requirements.saveAndFlush(existing)).thenReturn(existing);

        RequirementView result = component.update(31L, 5);

        assertEquals(Integer.valueOf(5), existing.quantity);
        assertEquals(Integer.valueOf(3), result.allocatedQuantity);
        assertEquals(Integer.valueOf(2), result.shortageQuantity);
        verify(requirements).saveAndFlush(existing);
    }

    @Test
    public void updateRefusesQuantityBelowEffectiveCoverageWithoutMutation() {
        prepareForUpdate();
        when(allocations.sumCoveredQuantity(eq(31L), anyList(), eq(AllocationStatus.APPROVED), eq(now)))
                .thenReturn(8L);
        when(allocations.maxPendingQuantity(31L, AllocationStatus.PENDING)).thenReturn(5);

        expectApiError(409, "QUANTITY_BELOW_COMMITMENTS", () -> component.update(31L, 7));

        assertEquals(Integer.valueOf(10), existing.quantity);
        verify(requirements, never()).saveAndFlush(existing);
    }

    @Test
    public void updateRefusesQuantityBelowLargestSinglePendingRequestNotPendingSum() {
        prepareForUpdate();
        when(allocations.sumCoveredQuantity(eq(31L), anyList(), eq(AllocationStatus.APPROVED), eq(now)))
                .thenReturn(3L);
        when(allocations.maxPendingQuantity(31L, AllocationStatus.PENDING)).thenReturn(9);

        expectApiError(409, "QUANTITY_BELOW_COMMITMENTS", () -> component.update(31L, 8));

        assertEquals(Integer.valueOf(10), existing.quantity);
        verify(requirements, never()).saveAndFlush(existing);
    }

    @Test
    public void updateRefusesEndedProjectBeforeQueryingCoverage() {
        project.endAt = now;
        prepareForUpdate();

        expectApiError(409, "PROJECT_ENDED", () -> component.update(31L, 12));

        assertEquals(Integer.valueOf(10), existing.quantity);
        verify(allocations, never()).sumCoveredQuantity(eq(31L), anyList(),
                eq(AllocationStatus.APPROVED), eq(now));
    }

    @Test
    public void updateValidatesQuantityAndMissingIds() {
        expectApiError(400, "VALIDATION_FAILED", () -> component.update(31L, 0));
        when(requirements.findById(404L)).thenReturn(Optional.<ResourceRequirement>empty());
        expectApiError(404, "NOT_FOUND", () -> component.update(404L, 5));
    }

    @Test
    public void deleteRefusesAllocationHistoryWithoutChangingRequirement() {
        prepareForUpdate();
        when(allocations.existsByRequirement_Id(31L)).thenReturn(true);

        expectApiError(409, "DEPENDENCY_EXISTS", () -> component.delete(31L));

        assertEquals(Integer.valueOf(10), existing.quantity);
        verify(requirements, never()).delete(existing);
    }

    @Test
    public void deletePhysicallyRemovesUnreferencedRequirement() {
        prepareForUpdate();
        when(allocations.existsByRequirement_Id(31L)).thenReturn(false);

        MessageView result = component.delete(31L);

        assertEquals("Record deleted.", result.message);
        verify(requirements).delete(existing);
        verify(requirements).flush();
    }

    private void prepareForUpdate() {
        when(requirements.findById(31L)).thenReturn(Optional.of(existing));
        when(projects.lockById(7L)).thenReturn(Optional.of(project));
        when(resources.lockById(11L)).thenReturn(Optional.of(resource));
        when(requirements.lockById(31L)).thenReturn(Optional.of(existing));
    }

    private void expectApiError(int status, String code, Runnable action) {
        try {
            action.run();
            fail("Expected API error " + code);
        } catch (ApiException error) {
            assertEquals(status, error.status);
            assertEquals(code, error.code);
        }
    }
}

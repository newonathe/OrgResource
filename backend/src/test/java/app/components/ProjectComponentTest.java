package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import app.dto.ApiDtos.MessageView;
import app.dto.ApiDtos.ProjectView;
import app.entities.Department;
import app.entities.Project;
import app.repositories.AllocationRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRequirementRepository;

public class ProjectComponentTest {
    private ProjectComponent component;
    private ProjectRepository projects;
    private ResourceRequirementRepository requirements;
    private AllocationRepository allocations;
    private Project existing;
    private LocalDateTime now;

    @Before
    public void setUp() {
        component = new ProjectComponent();
        projects = org.mockito.Mockito.mock(ProjectRepository.class);
        requirements = org.mockito.Mockito.mock(ResourceRequirementRepository.class);
        allocations = org.mockito.Mockito.mock(AllocationRepository.class);
        ReflectionTestUtils.setField(component, "projects", projects);
        ReflectionTestUtils.setField(component, "departments", org.mockito.Mockito.mock(
                app.repositories.DepartmentRepository.class));
        ReflectionTestUtils.setField(component, "requirements", requirements);
        ReflectionTestUtils.setField(component, "allocations", allocations);
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"), ZoneId.of("Asia/Manila"));
        ReflectionTestUtils.setField(component, "philippineClock", clock);
        now = LocalDateTime.now(clock);

        Department department = new Department("Operations", "operations");
        department.id = 5L;
        existing = new Project(department, "Old project", now.minusDays(3), now.minusDays(2));
        existing.id = 17L;
    }

    @Test
    public void nameOnlyUpdateAllowsHistoricalDatesAndKeepsDepartment() {
        when(projects.lockById(17L)).thenReturn(Optional.of(existing));
        when(projects.saveAndFlush(existing)).thenReturn(existing);

        ProjectView result = component.update(17L, "  Renamed   project ", existing.startAt, existing.endAt);

        assertEquals("Renamed project", result.name);
        assertEquals(Long.valueOf(5L), result.departmentId);
        assertEquals(now.minusDays(3), result.startAt);
        assertEquals(now.minusDays(2), result.endAt);
        verify(allocations, never()).countHistoricallyApproved(17L);
        verify(projects).saveAndFlush(existing);
    }

    @Test
    public void dateUpdateRequiresAValidFuturePeriod() {
        when(projects.lockById(17L)).thenReturn(Optional.of(existing));
        when(allocations.countHistoricallyApproved(17L)).thenReturn(0L);
        when(projects.saveAndFlush(existing)).thenReturn(existing);
        LocalDateTime start = now.plusDays(3);
        LocalDateTime end = start.plusHours(3);

        ProjectView result = component.update(17L, "Updated project", start, end);

        assertEquals(start, result.startAt);
        assertEquals(end, result.endAt);
        verify(allocations).countHistoricallyApproved(17L);
        verify(projects).saveAndFlush(existing);
    }

    @Test
    public void invalidPeriodDoesNotChangeTheProject() {
        when(projects.lockById(17L)).thenReturn(Optional.of(existing));
        LocalDateTime oldStart = existing.startAt;
        LocalDateTime oldEnd = existing.endAt;

        expectApiError(400, "INVALID_PROJECT_PERIOD", () ->
                component.update(17L, "Changed", now.plusDays(2), now.plusDays(2)));

        assertEquals("Old project", existing.name);
        assertEquals(oldStart, existing.startAt);
        assertEquals(oldEnd, existing.endAt);
        verify(projects, never()).saveAndFlush(existing);
    }

    @Test
    public void changedDatesRejectAStartInThePastWithoutMutation() {
        when(projects.lockById(17L)).thenReturn(Optional.of(existing));
        LocalDateTime oldStart = existing.startAt;
        LocalDateTime oldEnd = existing.endAt;

        expectApiError(400, "INVALID_PROJECT_PERIOD", () ->
                component.update(17L, "Changed", now.minusSeconds(1), now.plusDays(1)));

        assertEquals("Old project", existing.name);
        assertEquals(oldStart, existing.startAt);
        assertEquals(oldEnd, existing.endAt);
        verify(allocations, never()).countHistoricallyApproved(17L);
        verify(projects, never()).saveAndFlush(existing);
    }

    @Test
    public void changedDatesAreLockedAfterAnyHistoricalApproval() {
        when(projects.lockById(17L)).thenReturn(Optional.of(existing));
        when(allocations.countHistoricallyApproved(17L)).thenReturn(1L);
        LocalDateTime oldStart = existing.startAt;
        LocalDateTime oldEnd = existing.endAt;

        expectApiError(409, "PROJECT_DATES_LOCKED", () ->
                component.update(17L, "Changed", now.plusDays(2), now.plusDays(2).plusHours(1)));

        assertEquals("Old project", existing.name);
        assertEquals(oldStart, existing.startAt);
        assertEquals(oldEnd, existing.endAt);
        verify(projects, never()).saveAndFlush(existing);
    }

    @Test
    public void missingAndNonpositiveIdsAreControlled() {
        when(projects.lockById(404L)).thenReturn(Optional.<Project>empty());
        expectApiError(404, "NOT_FOUND", () ->
                component.update(404L, "Missing", now.plusDays(1), now.plusDays(1).plusHours(1)));
        expectApiError(400, "VALIDATION_FAILED", () ->
                component.update(0L, "Invalid", now.plusDays(1), now.plusDays(1).plusHours(1)));
        verifyZeroInteractions(allocations);
    }

    @Test
    public void deleteRefusesReferencedProjectWithoutChangingIt() {
        when(projects.lockById(17L)).thenReturn(Optional.of(existing));
        when(requirements.existsByProject_Id(17L)).thenReturn(true);

        expectApiError(409, "DEPENDENCY_EXISTS", () -> component.delete(17L));

        verify(projects, never()).delete(existing);
        verify(projects, never()).flush();
    }

    @Test
    public void deletePhysicallyRemovesUnreferencedProject() {
        when(projects.lockById(17L)).thenReturn(Optional.of(existing));
        when(requirements.existsByProject_Id(17L)).thenReturn(false);

        MessageView result = component.delete(17L);

        assertEquals("Record deleted.", result.message);
        verify(projects).delete(existing);
        verify(projects).flush();
    }

    @Test
    public void deleteReportsMissingProject() {
        when(projects.lockById(404L)).thenReturn(Optional.<Project>empty());
        expectApiError(404, "NOT_FOUND", () -> component.delete(404L));
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

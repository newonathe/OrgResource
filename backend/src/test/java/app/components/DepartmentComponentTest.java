package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import app.dto.ApiDtos.DepartmentView;
import app.entities.Department;
import app.repositories.DepartmentRepository;
import app.repositories.ProjectRepository;

public class DepartmentComponentTest {
    private DepartmentComponent component;
    private DepartmentRepository departments;
    private Department existing;

    @Before
    public void setUp() {
        component = new DepartmentComponent();
        departments = org.mockito.Mockito.mock(DepartmentRepository.class);
        ReflectionTestUtils.setField(component, "departments", departments);
        ReflectionTestUtils.setField(component, "projects", org.mockito.Mockito.mock(ProjectRepository.class));
        existing = new Department("Old Name", "old name");
        existing.id = 17L;
    }

    @Test
    public void updateNormalizesDisplayName() {
        when(departments.findById(17L)).thenReturn(Optional.of(existing));
        when(departments.saveAndFlush(existing)).thenReturn(existing);

        DepartmentView result = component.update(17L, "  NEW    Name  ");

        assertEquals(Long.valueOf(17L), result.id);
        assertEquals("NEW Name", result.name);
        assertEquals("new name", existing.nameKey);
        verify(departments).saveAndFlush(existing);
    }

    @Test
    public void updateAllowsCaseOnlyChangeWithoutConflictingWithItself() {
        when(departments.findById(17L)).thenReturn(Optional.of(existing));
        when(departments.saveAndFlush(existing)).thenReturn(existing);

        DepartmentView result = component.update(17L, "OLD NAME");

        assertEquals("OLD NAME", result.name);
        assertEquals("old name", existing.nameKey);
        verify(departments, never()).existsByNameKey("old name");
    }

    @Test
    public void updateRejectsNormalizedDuplicateName() {
        when(departments.findById(17L)).thenReturn(Optional.of(existing));
        when(departments.existsByNameKey("operations")).thenReturn(true);

        try {
            component.update(17L, " Operations ");
            fail("Expected normalized duplicate to be rejected");
        } catch (ApiException error) {
            assertEquals(409, error.status);
            assertEquals("DUPLICATE_NAME", error.code);
        }
    }

    @Test
    public void updateRejectsNonPositiveIdBeforeRepositoryAccess() {
        try {
            component.update(0L, "Operations");
            fail("Expected nonpositive id to be rejected");
        } catch (ApiException error) {
            assertEquals(400, error.status);
            assertEquals("VALIDATION_FAILED", error.code);
        }
        verifyZeroInteractions(departments);
    }

    @Test
    public void updateReportsMissingDepartment() {
        when(departments.findById(404L)).thenReturn(Optional.<Department>empty());

        try {
            component.update(404L, "Operations");
            fail("Expected missing Department to be rejected");
        } catch (ApiException error) {
            assertEquals(404, error.status);
            assertEquals("NOT_FOUND", error.code);
        }
    }
}

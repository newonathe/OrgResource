package app.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.Department;
import app.entities.Project;
import app.entities.Resource;
import app.entities.ResourceRequirement;
import app.entities.ResourceType;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AllocationRepositoryMySqlTest {
    private static final List<AllocationStatus> COMPLETED = Arrays.asList(
            AllocationStatus.ISSUED, AllocationStatus.RETURNED, AllocationStatus.CONSUMED);

    @Autowired private DepartmentRepository departments;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private AllocationRepository allocations;
    @PersistenceContext private EntityManager entityManager;

    @Test
    public void coverageUsesRealMySqlAndTreatsProjectEndAsExclusive() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime projectEnd = LocalDateTime.of(2026, 11, 20, 12, 0);
        Department department = departments.saveAndFlush(new Department("O3 " + suffix, "o3 " + suffix));
        Project project = projects.saveAndFlush(new Project(department, "Coverage " + suffix,
                projectEnd.minusDays(2), projectEnd));
        Resource resource = resources.saveAndFlush(new Resource("Paper " + suffix,
                "paper " + suffix, ResourceType.CONSUMABLE, 100));
        ResourceRequirement requirement = requirements.saveAndFlush(
                new ResourceRequirement(project, resource, 100));

        saveAllocation(requirement, 3, AllocationStatus.APPROVED, projectEnd.minusDays(1));
        saveAllocation(requirement, 4, AllocationStatus.ISSUED, null);
        saveAllocation(requirement, 5, AllocationStatus.RETURNED, null);
        saveAllocation(requirement, 6, AllocationStatus.CONSUMED, null);
        saveAllocation(requirement, 7, AllocationStatus.PENDING, null);
        saveAllocation(requirement, 8, AllocationStatus.CANCELLED, projectEnd.minusDays(2));
        allocations.flush();

        assertEquals(Long.valueOf(18L), allocations.sumCoveredQuantity(requirement.id, COMPLETED,
                AllocationStatus.APPROVED, projectEnd.minusSeconds(1)));
        assertEquals(Long.valueOf(15L), allocations.sumCoveredQuantity(requirement.id, COMPLETED,
                AllocationStatus.APPROVED, projectEnd));
        assertEquals(Long.valueOf(15L), allocations.sumCoveredQuantity(requirement.id, COMPLETED,
                AllocationStatus.APPROVED, projectEnd.plusSeconds(1)));
        assertNull(allocations.sumCoveredQuantity(Long.MAX_VALUE, COMPLETED,
                AllocationStatus.APPROVED, projectEnd.minusNanos(1)));
        assertEquals(Integer.valueOf(7), allocations.maxPendingQuantity(requirement.id, AllocationStatus.PENDING));
        assertEquals(Long.valueOf(2L), allocations.countHistoricallyApproved(project.id));

        assertNotNull(projects.lockById(project.id).orElse(null));
        assertNotNull(resources.lockById(resource.id).orElse(null));
        assertNotNull(requirements.lockById(requirement.id).orElse(null));

        entityManager.flush();
        entityManager.clear();
        ResourceRequirement reloadedRequirement = requirements.findById(requirement.id).get();
        Long reloadedProjectId = reloadedRequirement.project.getId();
        Project lockedProject = projects.lockById(reloadedProjectId).get();
        assertNotNull("The locked Project proxy must initialize endAt through its getter.",
                lockedProject.getEndAt());
    }

    private void saveAllocation(ResourceRequirement requirement, int quantity,
                                AllocationStatus status, LocalDateTime approvedAt) {
        Allocation allocation = new Allocation(requirement, quantity, "Fictional O3 test", null,
                false, LocalDateTime.of(2026, 11, 19, 9, 0));
        allocation.status = status;
        allocation.approvedAt = approvedAt;
        allocations.save(allocation);
    }
}

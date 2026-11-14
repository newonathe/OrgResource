package app.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import app.SpringBootApp;
import app.dto.ApiDtos.ResourceView;
import app.entities.Allocation;
import app.entities.AllocationStatus;
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
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class ResourceListAggregationMySqlTest {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 12, 0);

    @Autowired private ResourceComponent component;
    @Autowired private DepartmentRepository departments;
    @Autowired private ProjectRepository projects;
    @Autowired private ResourceRepository resources;
    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private AllocationRepository allocations;
    @MockBean private Clock philippineClock;
    @PersistenceContext private EntityManager entityManager;

    private final List<Long> departmentIds = new ArrayList<>();
    private final List<Long> projectIds = new ArrayList<>();
    private final List<Long> resourceIds = new ArrayList<>();
    private final List<Long> requirementIds = new ArrayList<>();
    private final List<Long> allocationIds = new ArrayList<>();

    @Before
    public void fixedPhilippineClock() {
        reset(philippineClock);
        when(philippineClock.instant()).thenReturn(NOW.atZone(MANILA).toInstant());
        when(philippineClock.getZone()).thenReturn(MANILA);
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
    public void oneClockSampleAndOneGroupedQueryProduceExactAvailabilityAtProjectEndBoundary() {
        Resource reusable = resource(ResourceType.REUSABLE, 10);
        Resource consumable = resource(ResourceType.CONSUMABLE, 20);
        Resource untouched = resource(ResourceType.REUSABLE, 7);

        Fixture activeReusable = fixture(reusable, NOW.plusHours(4));
        saveAllocation(activeReusable.requirement, 3, AllocationStatus.APPROVED);
        saveAllocation(activeReusable.requirement, 2, AllocationStatus.ISSUED);

        Fixture endingNow = fixture(reusable, NOW);
        saveAllocation(endingNow.requirement, 4, AllocationStatus.APPROVED);

        Fixture activeConsumable = fixture(consumable, NOW.plusHours(2));
        saveAllocation(activeConsumable.requirement, 5, AllocationStatus.APPROVED);

        reset(philippineClock);
        when(philippineClock.instant()).thenReturn(NOW.atZone(MANILA).toInstant());
        when(philippineClock.getZone()).thenReturn(MANILA);
        List<ResourceView> views = component.list(null);

        Map<Long, ResourceView> byId = new HashMap<>();
        for (ResourceView view : views) byId.put(view.id, view);
        assertNotNull(byId.get(reusable.id));
        assertEquals(Integer.valueOf(5), byId.get(reusable.id).availableQuantity);
        assertEquals(Integer.valueOf(15), byId.get(consumable.id).availableQuantity);
        assertEquals(Integer.valueOf(7), byId.get(untouched.id).availableQuantity);
        verify(philippineClock, times(1)).instant();
    }

    private Resource resource(ResourceType type, int total) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String name = "O6 Resource list " + suffix;
        Resource saved = resources.saveAndFlush(new Resource(name, name.toLowerCase(java.util.Locale.ROOT), type, total));
        resourceIds.add(saved.id);
        return saved;
    }

    private Fixture fixture(Resource resource, LocalDateTime endAt) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String departmentName = "O6 Resource department " + suffix;
        Department department = departments.saveAndFlush(new Department(
                departmentName, departmentName.toLowerCase(java.util.Locale.ROOT)));
        departmentIds.add(department.id);
        Project project = projects.saveAndFlush(new Project(department, "O6 Resource project " + suffix,
                NOW.minusHours(2), endAt));
        projectIds.add(project.id);
        ResourceRequirement requirement = requirements.saveAndFlush(
                new ResourceRequirement(project, resource, 20));
        requirementIds.add(requirement.id);
        return new Fixture(requirement);
    }

    private void saveAllocation(ResourceRequirement requirement, int quantity, AllocationStatus status) {
        Allocation allocation = new Allocation(requirement, quantity, "Fictional test representative", null,
                false, NOW.minusHours(1));
        allocation.status = status;
        if (status != AllocationStatus.PENDING) allocation.approvedAt = NOW.minusMinutes(30);
        if (status == AllocationStatus.ISSUED) allocation.issuedAt = NOW.minusMinutes(10);
        allocations.saveAndFlush(allocation);
        allocationIds.add(allocation.id);
    }

    private static final class Fixture {
        private final ResourceRequirement requirement;
        private Fixture(ResourceRequirement requirement) { this.requirement = requirement; }
    }
}

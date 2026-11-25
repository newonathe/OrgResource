package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import app.dto.ApiDtos.ProjectView;
import app.dto.ApiDtos.MessageView;
import app.entities.Department;
import app.entities.Project;
import app.repositories.AllocationRepository;
import app.repositories.DepartmentRepository;
import app.repositories.ProjectRepository;
import app.repositories.ResourceRequirementRepository;

@Component
public class ProjectComponent {
    @Autowired private ProjectRepository projects;
    @Autowired private DepartmentRepository departments;
    @Autowired private ResourceRequirementRepository requirements;
    @Autowired private AllocationRepository allocations;
    @Autowired private Clock philippineClock;

    @Transactional
    public ProjectView create(Long departmentId, String rawName, LocalDateTime startAt, LocalDateTime endAt) {
        String name = NameNormalizer.display(rawName, "name");
        validatePeriod(startAt, endAt);
        if (!startAt.isAfter(LocalDateTime.now(philippineClock))) {
            throw new ApiException(400, "INVALID_PROJECT_PERIOD", "Project startAt must be in the future.");
        }
        Department department = departments.findById(departmentId)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Department not found."));
        Project saved = projects.save(new Project(department, name, startAt, endAt));
        return new ProjectView(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectView> list(Long departmentId) {
        if (departmentId != null && departmentId <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "departmentId must be positive.");
        }
        List<Project> rows = departmentId == null ? projects.findAll() : projects.findByDepartment_Id(departmentId);
        return rows.stream().map(ProjectView::new).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ProjectView update(Long id, String rawName, LocalDateTime startAt, LocalDateTime endAt) {
        requirePositiveId(id);
        Project project = projects.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        String name = NameNormalizer.display(rawName, "name");
        validatePeriod(startAt, endAt);

        boolean datesChanged = !project.startAt.equals(startAt) || !project.endAt.equals(endAt);
        if (datesChanged) {
            LocalDateTime now = LocalDateTime.now(philippineClock);
            if (!startAt.isAfter(now)) {
                throw new ApiException(400, "INVALID_PROJECT_PERIOD", "Project startAt must be in the future when dates change.");
            }
            if (allocations.countHistoricallyApproved(id) > 0L) {
                throw new ApiException(409, "PROJECT_DATES_LOCKED",
                        "Project dates cannot change after an Allocation has been approved.");
            }
        }

        project.name = name;
        project.startAt = startAt;
        project.endAt = endAt;
        return new ProjectView(projects.saveAndFlush(project));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MessageView delete(Long id) {
        requirePositiveId(id);
        Project project = projects.lockById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Project not found."));
        if (requirements.existsByProject_Id(id)) {
            throw new ApiException(409, "DEPENDENCY_EXISTS", "Project still has ResourceRequirements.");
        }
        projects.delete(project);
        projects.flush();
        return new MessageView("Record deleted.");
    }

    private void validatePeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new ApiException(400, "INVALID_PROJECT_PERIOD", "endAt must be later than startAt.");
        }
    }

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException(400, "VALIDATION_FAILED", "Project ID must be positive.");
        }
    }
}

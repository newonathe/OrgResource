package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import app.dto.ApiDtos.ProjectView;
import app.entities.Department;
import app.entities.Project;
import app.repositories.DepartmentRepository;
import app.repositories.ProjectRepository;

@Component
public class ProjectComponent {
    @Autowired private ProjectRepository projects;
    @Autowired private DepartmentRepository departments;
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

    private void validatePeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new ApiException(400, "INVALID_PROJECT_PERIOD", "endAt must be later than startAt.");
        }
    }
}

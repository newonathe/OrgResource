package app.components;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import app.dto.ApiDtos.DepartmentView;
import app.dto.ApiDtos.MessageView;
import app.entities.Department;
import app.repositories.DepartmentRepository;
import app.repositories.ProjectRepository;

@Component
public class DepartmentComponent {
    @Autowired private DepartmentRepository departments;
    @Autowired private ProjectRepository projects;

    @Transactional
    public DepartmentView create(String rawName) {
        String name = NameNormalizer.display(rawName, "name");
        String key = NameNormalizer.key(name);
        if (departments.existsByNameKey(key)) {
            throw new ApiException(409, "DUPLICATE_NAME", "A department with this name already exists.");
        }
        Department saved = departments.save(new Department(name, key));
        return new DepartmentView(saved.id, saved.name);
    }

    @Transactional(readOnly = true)
    public List<DepartmentView> list() {
        return departments.findAll().stream()
                .map(row -> new DepartmentView(row.id, row.name)).collect(Collectors.toList());
    }

    @Transactional
    public MessageView delete(Long id) {
        Department department = departments.findById(id)
                .orElseThrow(() -> new ApiException(404, "NOT_FOUND", "Department not found."));
        if (projects.existsByDepartment_Id(id)) {
            throw new ApiException(409, "DEPENDENCY_EXISTS", "Department still has Projects.");
        }
        departments.delete(department);
        return new MessageView("Record deleted.");
    }
}

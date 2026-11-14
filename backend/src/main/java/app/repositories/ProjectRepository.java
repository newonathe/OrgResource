package app.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.entities.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDepartment_Id(Long departmentId);
    boolean existsByDepartment_Id(Long departmentId);
}

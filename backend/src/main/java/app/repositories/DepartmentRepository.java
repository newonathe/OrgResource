package app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.entities.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByNameKey(String nameKey);
}

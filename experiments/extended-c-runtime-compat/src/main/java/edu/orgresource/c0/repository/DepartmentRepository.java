package edu.orgresource.c0.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.orgresource.c0.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByOrganizationIdOrderById(Long organizationId);
    Optional<Department> findByIdAndOrganizationId(Long id, Long organizationId);
    boolean existsByOrganizationIdAndNameKey(Long organizationId, String nameKey);
}

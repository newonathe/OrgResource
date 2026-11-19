package edu.orgresource.extended.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.orgresource.extended.domain.Project;
import jakarta.persistence.LockModeType;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @EntityGraph(attributePaths = {"department"})
    List<Project> findByOrganizationIdOrderById(Long organizationId);
    @EntityGraph(attributePaths = {"department"})
    List<Project> findByOrganizationIdAndDepartmentIdOrderById(Long organizationId, Long departmentId);
    Optional<Project> findByIdAndOrganizationId(Long id, Long organizationId);

    @EntityGraph(attributePaths = {"department"})
    @Query("select p from Project p where p.organizationId=:org and p.id>:after "
            + "and (:department is null or p.department.id=:department) "
            + "and (:search is null or locate(:search, lower(concat(p.name, ' ', p.department.name)))>0) order by p.id")
    List<Project> findWorkspacePage(@Param("org") long org, @Param("after") long after,
            @Param("department") Long department, @Param("search") String search, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.id = :id and p.organizationId = :organizationId")
    Optional<Project> lockById(@Param("id") Long id, @Param("organizationId") Long organizationId);

    boolean existsByOrganizationIdAndDepartmentId(Long organizationId, Long departmentId);
    boolean existsByDepartmentIdAndOrganizationId(Long departmentId, Long organizationId);
}

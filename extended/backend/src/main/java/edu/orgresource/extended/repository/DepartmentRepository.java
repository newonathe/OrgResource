package edu.orgresource.extended.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import edu.orgresource.extended.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByOrganizationIdOrderById(Long organizationId);
    Optional<Department> findByIdAndOrganizationId(Long id, Long organizationId);
    boolean existsByOrganizationIdAndNameKey(Long organizationId, String nameKey);
    boolean existsByOrganizationIdAndNameKeyAndIdNot(Long organizationId, String nameKey, Long id);

    @Query("select d from Department d where d.organizationId=:org and d.id>:after "
            + "and (:search is null or locate(:search, d.nameKey)>0) order by d.id")
    List<Department> findWorkspacePage(@Param("org") long org, @Param("after") long after,
            @Param("search") String search, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Department d where d.id=:id and d.organizationId=:organizationId")
    Optional<Department> lockById(@Param("id") Long id, @Param("organizationId") Long organizationId);
}

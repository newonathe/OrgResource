package edu.orgresource.c0.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.orgresource.c0.domain.Allocation;
import edu.orgresource.c0.domain.AllocationStatus;
import jakarta.persistence.LockModeType;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    @EntityGraph(attributePaths = {"requirement"})
    List<Allocation> findByOrganizationIdAndRequirementIdOrderById(Long organizationId, Long requirementId);
    Optional<Allocation> findByIdAndOrganizationId(Long id, Long organizationId);

    interface OperationSnapshot {
        Long getProjectId();
        Long getResourceId();
        Long getRequirementId();
        Integer getRequirementQuantity();
    }

    @Query("select p.id as projectId, r.id as resourceId, q.id as requirementId, q.quantity as requirementQuantity "
            + "from Allocation a join a.requirement q join q.project p join q.resource r "
            + "where a.id = :id and a.organizationId = :organizationId")
    Optional<OperationSnapshot> findOperationSnapshot(@Param("id") Long id,
                                                       @Param("organizationId") Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Allocation a where a.id = :id and a.organizationId = :organizationId")
    Optional<Allocation> lockById(@Param("id") Long id, @Param("organizationId") Long organizationId);

    @Query("select a from Allocation a join a.requirement q join q.project p "
            + "where a.organizationId = :organizationId and a.status = :pending and p.endAt <= :sampledNow")
    List<Allocation> findExpiredPending(@Param("organizationId") Long organizationId,
                                        @Param("pending") AllocationStatus pending,
                                        @Param("sampledNow") LocalDateTime sampledNow);
}

package edu.orgresource.extended.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.orgresource.extended.domain.Allocation;
import edu.orgresource.extended.domain.AllocationStatus;
import jakarta.persistence.LockModeType;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    interface ExpiryCandidate { Long getOrganizationId(); Long getAllocationId(); }
    interface ReminderCandidate { Long getAllocationId(); }
    @EntityGraph(attributePaths = {"requirement.project"})
    List<Allocation> findByOrganizationIdAndRequirementIdOrderById(Long organizationId, Long requirementId);
    @EntityGraph(attributePaths = {"requirement.project"})
    List<Allocation> findByOrganizationIdOrderById(Long organizationId);
    @EntityGraph(attributePaths = {"requirement.project"})
    List<Allocation> findByOrganizationIdAndStatusOrderById(Long organizationId, AllocationStatus status);
    @EntityGraph(attributePaths = {"requirement.project"})
    List<Allocation> findByOrganizationIdAndRequirementIdAndStatusOrderById(Long organizationId,
            Long requirementId, AllocationStatus status);
    boolean existsByOrganizationIdAndRequirementId(Long organizationId, Long requirementId);

    @EntityGraph(attributePaths = {"requirement", "requirement.project"})
    @Query("select a from Allocation a where a.organizationId=:org and a.id>:after "
            + "and (:requirement is null or a.requirement.id=:requirement) "
            + "and (:status is null or a.status=:status) order by a.id")
    List<Allocation> findWorkspacePage(@Param("org") long org, @Param("after") long after,
            @Param("requirement") Long requirement, @Param("status") AllocationStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Allocation a where a.id=:id")
    Optional<Allocation> lockById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"requirement", "requirement.project", "requirement.resource"})
    Optional<Allocation> findDetailedByIdAndOrganizationId(@Param("id") Long id,
                                                            @Param("organizationId") Long organizationId);

    @Query(value = "SELECT a.organization_id AS organizationId,a.id AS allocationId "
            + "FROM allocation a JOIN resource_requirement q ON q.id=a.requirement_id AND q.organization_id=a.organization_id "
            + "JOIN project p ON p.id=q.project_id AND p.organization_id=q.organization_id "
            + "WHERE a.status IN ('PENDING','APPROVED') AND p.end_at<=:sampledNow "
            + "ORDER BY a.organization_id,a.id", nativeQuery = true)
    List<ExpiryCandidate> findExpiredCandidates(@Param("sampledNow") LocalDateTime sampledNow, Pageable pageable);

    @Query(value = "SELECT a.id AS allocationId FROM allocation a "
            + "JOIN resource_requirement q ON q.id=a.requirement_id AND q.organization_id=a.organization_id "
            + "JOIN project p ON p.id=q.project_id AND p.organization_id=q.organization_id "
            + "JOIN resource r ON r.id=q.resource_id AND r.organization_id=q.organization_id "
            + "WHERE a.status='ISSUED' AND r.type='REUSABLE' AND a.sms_consent=TRUE "
            + "AND a.phone_ciphertext IS NOT NULL "
            + "AND (a.reminder_outcome='NONE' OR (a.reminder_outcome='KEY_UNAVAILABLE' "
            + "AND CAST(a.phone_key_id AS BINARY)=CAST(:availableKeyId AS BINARY))) "
            + "AND p.end_at>=:sampledNow AND p.end_at<:dueBefore "
            + "ORDER BY p.end_at,a.id", nativeQuery = true)
    List<ReminderCandidate> findReminderCandidates(@Param("sampledNow") LocalDateTime sampledNow,
                                                   @Param("dueBefore") LocalDateTime dueBefore,
                                                   @Param("availableKeyId") String availableKeyId,
                                                   Pageable pageable);

    @Query(value = "SELECT id AS allocationId FROM allocation WHERE reminder_outcome IN ('CLAIMED','DISPATCHING') "
            + "AND reminder_claimed_at<:cutoff ORDER BY reminder_claimed_at,id", nativeQuery = true)
    List<ReminderCandidate> findStaleReminderClaims(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    @Query("select count(a) from Allocation a join a.requirement q where a.organizationId=:organizationId "
            + "and q.projectId=:projectId and a.approvedAt is not null")
    long countHistoricallyApprovedByProject(@Param("organizationId") Long organizationId,
                                            @Param("projectId") Long projectId);
    Optional<Allocation> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<Allocation> findByPublicIdAndOrganizationId(java.util.UUID publicId, Long organizationId);

    interface OperationSnapshot {
        Long getAllocationId();
        Long getProjectId();
        Long getResourceId();
        Long getRequirementId();
        Integer getRequirementQuantity();
    }

    @Query("select a.id as allocationId, p.id as projectId, r.id as resourceId, q.id as requirementId, q.quantity as requirementQuantity "
            + "from Allocation a join a.requirement q join q.project p join q.resource r "
            + "where a.id = :id and a.organizationId = :organizationId")
    Optional<OperationSnapshot> findOperationSnapshot(@Param("id") Long id,
                                                       @Param("organizationId") Long organizationId);

    @Query("select a.id as allocationId, p.id as projectId, r.id as resourceId, q.id as requirementId, q.quantity as requirementQuantity "
            + "from Allocation a join a.requirement q join q.project p join q.resource r "
            + "where a.publicId = :publicId and a.organizationId = :organizationId")
    Optional<OperationSnapshot> findOperationSnapshotByPublicId(@Param("publicId") java.util.UUID publicId,
                                                                  @Param("organizationId") Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Allocation a where a.id = :id and a.organizationId = :organizationId")
    Optional<Allocation> lockById(@Param("id") Long id, @Param("organizationId") Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Allocation a where a.publicId = :publicId and a.organizationId = :organizationId")
    Optional<Allocation> lockByPublicIdAndOrganizationId(@Param("publicId") java.util.UUID publicId,
                                                          @Param("organizationId") Long organizationId);

    String INVENTORY_BALANCE_PROJECTION = "SELECT r.id AS resource_id, COALESCE(l.ledger_total,0) AS ledger_total, "
            + "COALESCE(l.ledger_issued,0) AS ledger_issued, COALESCE(c.holds,0) AS holds, "
            + "COALESCE(c.issued,0) AS issued FROM resource r ";
    String INVENTORY_LEDGER_AGGREGATE = "LEFT JOIN (SELECT organization_id,resource_id,SUM(delta_total) AS ledger_total,"
            + "SUM(delta_issued) AS ledger_issued FROM stock_movement WHERE organization_id=:organizationId ";
    String INVENTORY_LEDGER_JOIN = "GROUP BY organization_id,resource_id) l "
            + "ON l.organization_id=r.organization_id AND l.resource_id=r.id ";
    String INVENTORY_CUSTODY_AGGREGATE = "LEFT JOIN (SELECT q.organization_id,q.resource_id,"
            + "SUM(CASE WHEN a.status='APPROVED' AND p.end_at>:sampledNow THEN a.quantity ELSE 0 END) AS holds,"
            + "SUM(CASE WHEN a.status='ISSUED' THEN a.quantity ELSE 0 END) AS issued "
            + "FROM allocation a JOIN resource_requirement q ON q.id=a.requirement_id AND q.organization_id=a.organization_id "
            + "JOIN project p ON p.id=q.project_id AND p.organization_id=q.organization_id "
            + "WHERE a.organization_id=:organizationId ";
    String INVENTORY_CUSTODY_JOIN = "GROUP BY q.organization_id,q.resource_id) c "
            + "ON c.organization_id=r.organization_id AND c.resource_id=r.id ";

    @Query(value = INVENTORY_BALANCE_PROJECTION + INVENTORY_LEDGER_AGGREGATE + INVENTORY_LEDGER_JOIN
            + INVENTORY_CUSTODY_AGGREGATE + INVENTORY_CUSTODY_JOIN
            + "WHERE r.organization_id=:organizationId ORDER BY r.id", nativeQuery = true)
    List<Object[]> findInventoryListBalances(@Param("organizationId") Long organizationId,
                                             @Param("sampledNow") LocalDateTime sampledNow);

    @Query(value = INVENTORY_BALANCE_PROJECTION + INVENTORY_LEDGER_AGGREGATE + "AND resource_id IN (:ids) "
            + INVENTORY_LEDGER_JOIN + INVENTORY_CUSTODY_AGGREGATE + "AND q.resource_id IN (:ids) "
            + INVENTORY_CUSTODY_JOIN + "WHERE r.organization_id=:organizationId AND r.id IN (:ids)", nativeQuery = true)
    List<Object[]> findInventoryPageBalances(@Param("organizationId") long organizationId,
            @Param("ids") List<Long> ids, @Param("sampledNow") LocalDateTime sampledNow);

    @Query("select a from Allocation a join a.requirement q join q.project p "
            + "where a.organizationId = :organizationId and a.status = :pending and p.endAt <= :sampledNow")
    List<Allocation> findExpiredPending(@Param("organizationId") Long organizationId,
                                        @Param("pending") AllocationStatus pending,
                                        @Param("sampledNow") LocalDateTime sampledNow);
}

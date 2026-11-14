package app.repositories;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.ReminderOutcome;
import app.entities.ResourceType;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByRequirement_Id(Long requirementId);
    List<Allocation> findByStatus(AllocationStatus status);
    List<Allocation> findByRequirement_IdAndStatus(Long requirementId, AllocationStatus status);
    boolean existsByRequirement_Id(Long requirementId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Allocation a WHERE a.id = :id")
    Optional<Allocation> lockById(@Param("id") Long id);

    @Query("SELECT COUNT(a) FROM Allocation a "
            + "WHERE a.requirement.project.id = :projectId AND a.approvedAt IS NOT NULL")
    Long countHistoricallyApproved(@Param("projectId") Long projectId);

    @Query("SELECT MAX(a.quantity) FROM Allocation a "
            + "WHERE a.requirement.id = :requirementId AND a.status = :pending")
    Integer maxPendingQuantity(@Param("requirementId") Long requirementId,
                               @Param("pending") AllocationStatus pending);

    @Query("SELECT SUM(a.quantity) FROM Allocation a "
            + "WHERE a.requirement.id = :requirementId "
            + "AND (a.status IN :completedStatuses "
            + "OR (a.status = :approved AND a.requirement.project.endAt > :now))")
    Long sumCoveredQuantity(@Param("requirementId") Long requirementId,
                            @Param("completedStatuses") List<AllocationStatus> completedStatuses,
                            @Param("approved") AllocationStatus approved,
                            @Param("now") LocalDateTime now);

    @Query("SELECT SUM(a.quantity) FROM Allocation a "
            + "WHERE a.requirement.resource.id = :resourceId "
            + "AND a.status = :approved AND a.requirement.project.endAt > :now")
    Long sumEffectiveHolds(@Param("resourceId") Long resourceId,
                           @Param("approved") AllocationStatus approved,
                           @Param("now") LocalDateTime now);

    @Query("SELECT SUM(a.quantity) FROM Allocation a "
            + "WHERE a.requirement.resource.id = :resourceId AND a.status = :issued")
    Long sumIssuedCustody(@Param("resourceId") Long resourceId,
                          @Param("issued") AllocationStatus issued);

    @Query("SELECT a.requirement.resource.id, "
            + "SUM(CASE WHEN a.status = :approved AND a.requirement.project.endAt > :now "
            + "THEN a.quantity ELSE 0 END), "
            + "SUM(CASE WHEN a.status = :issued THEN a.quantity ELSE 0 END) "
            + "FROM Allocation a WHERE a.requirement.resource.id IN :resourceIds "
            + "AND a.status IN :countedStatuses GROUP BY a.requirement.resource.id")
    List<Object[]> sumResourceCommitments(@Param("resourceIds") List<Long> resourceIds,
                                          @Param("approved") AllocationStatus approved,
                                          @Param("issued") AllocationStatus issued,
                                          @Param("now") LocalDateTime now,
                                          @Param("countedStatuses") Collection<AllocationStatus> countedStatuses);

    @Query("SELECT a.id FROM Allocation a "
            + "WHERE a.status IN :openStatuses "
            + "AND a.requirement.project.endAt <= :now "
            + "ORDER BY a.id")
    List<Long> findExpiredCandidateIds(@Param("openStatuses") Collection<AllocationStatus> openStatuses,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

    @Transactional(readOnly = true)
    @Query("SELECT a.id FROM Allocation a "
            + "WHERE a.status = :issued AND a.requirement.resource.type = :reusable "
            + "AND a.smsConsent = true AND a.reminderClaimedAt IS NULL "
            + "AND a.reminderOutcome = :neverAttempted "
            + "AND a.requirement.project.endAt >= :now "
            + "AND a.requirement.project.endAt < :within24Hours "
            + "ORDER BY a.requirement.project.endAt, a.id")
    List<Long> findReminderCandidateIds(@Param("issued") AllocationStatus issued,
                                        @Param("reusable") ResourceType reusable,
                                        @Param("neverAttempted") ReminderOutcome neverAttempted,
                                        @Param("now") LocalDateTime now,
                                        @Param("within24Hours") LocalDateTime within24Hours,
                                        Pageable pageable);

    @Transactional(readOnly = true)
    @Query("SELECT a.id FROM Allocation a "
            + "WHERE a.reminderOutcome = :claimed AND a.reminderClaimedAt < :cutoff "
            + "ORDER BY a.reminderClaimedAt, a.id")
    List<Long> findStaleReminderClaimIds(@Param("claimed") ReminderOutcome claimed,
                                         @Param("cutoff") LocalDateTime cutoff,
                                         Pageable pageable);
}

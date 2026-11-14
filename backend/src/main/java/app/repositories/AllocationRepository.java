package app.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import app.entities.Allocation;
import app.entities.AllocationStatus;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByRequirement_Id(Long requirementId);
    List<Allocation> findByStatus(AllocationStatus status);
    List<Allocation> findByRequirement_IdAndStatus(Long requirementId, AllocationStatus status);

    @Query("SELECT SUM(a.quantity) FROM Allocation a "
            + "WHERE a.requirement.id = :requirementId AND a.status IN :statuses")
    Long sumCoveredQuantity(@Param("requirementId") Long requirementId,
                            @Param("statuses") List<AllocationStatus> statuses);

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
}

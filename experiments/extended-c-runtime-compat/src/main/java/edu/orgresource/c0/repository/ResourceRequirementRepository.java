package edu.orgresource.c0.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.orgresource.c0.domain.ResourceRequirement;
import jakarta.persistence.LockModeType;

public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {
    @EntityGraph(attributePaths = {"project", "resource"})
    List<ResourceRequirement> findByOrganizationIdAndProjectIdOrderById(Long organizationId, Long projectId);
    Optional<ResourceRequirement> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("select q from ResourceRequirement q join fetch q.project p join fetch q.resource r "
            + "where q.id = :id and q.organizationId = :organizationId")
    Optional<ResourceRequirement> findDetailedByIdAndOrganizationId(@Param("id") Long id,
                                                                     @Param("organizationId") Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from ResourceRequirement q where q.id = :id and q.organizationId = :organizationId")
    Optional<ResourceRequirement> lockById(@Param("id") Long id, @Param("organizationId") Long organizationId);

    @Query("select coalesce(sum(case when a.status in "
            + "(edu.orgresource.c0.domain.AllocationStatus.ISSUED, edu.orgresource.c0.domain.AllocationStatus.RETURNED, "
            + "edu.orgresource.c0.domain.AllocationStatus.CONSUMED) then a.quantity "
            + "when a.status = edu.orgresource.c0.domain.AllocationStatus.APPROVED and p.endAt > :sampledNow "
            + "then a.quantity else 0 end), 0) from Allocation a join a.requirement q join q.project p "
            + "where q.id = :requirementId and a.organizationId = :organizationId")
    Long sumCoveredQuantity(@Param("requirementId") Long requirementId,
                            @Param("organizationId") Long organizationId,
                            @Param("sampledNow") java.time.LocalDateTime sampledNow);
}

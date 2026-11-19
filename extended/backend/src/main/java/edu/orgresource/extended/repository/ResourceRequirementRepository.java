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

import edu.orgresource.extended.domain.ResourceRequirement;
import jakarta.persistence.LockModeType;

public interface ResourceRequirementRepository extends JpaRepository<ResourceRequirement, Long> {
    @EntityGraph(attributePaths = {"project", "resource"})
    List<ResourceRequirement> findByOrganizationIdOrderById(Long organizationId);
    @EntityGraph(attributePaths = {"project", "resource"})
    List<ResourceRequirement> findByOrganizationIdAndProjectIdOrderById(Long organizationId, Long projectId);
    Optional<ResourceRequirement> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("select q from ResourceRequirement q join fetch q.project p join fetch q.resource r "
            + "where q.id = :id and q.organizationId = :organizationId")
    Optional<ResourceRequirement> findDetailedByIdAndOrganizationId(@Param("id") Long id,
                                                                     @Param("organizationId") Long organizationId);

    @EntityGraph(attributePaths = {"project", "resource"})
    @Query("select q from ResourceRequirement q where q.organizationId=:org and q.id>:after "
            + "and (:project is null or q.project.id=:project) "
            + "and (:search is null or locate(:search, lower(concat(q.project.name, ' ', q.resource.name)))>0) order by q.id")
    List<ResourceRequirement> findWorkspacePage(@Param("org") long org, @Param("after") long after,
            @Param("project") Long project, @Param("search") String search, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from ResourceRequirement q where q.id = :id and q.organizationId = :organizationId")
    Optional<ResourceRequirement> lockById(@Param("id") Long id, @Param("organizationId") Long organizationId);

    boolean existsByOrganizationIdAndProjectId(Long organizationId, Long projectId);
    boolean existsByOrganizationIdAndResourceId(Long organizationId, Long resourceId);
    boolean existsByOrganizationIdAndProjectIdAndResourceId(Long organizationId, Long projectId, Long resourceId);

    @Query("select max(a.quantity) from Allocation a join a.requirement q join q.project p "
            + "where q.id=:requirementId and a.organizationId=:organizationId "
            + "and q.organizationId=:organizationId and p.organizationId=:organizationId "
            + "and a.status=:status and p.endAt>:sampledNow")
    Integer maxPendingQuantity(@Param("requirementId") Long requirementId,
                               @Param("organizationId") Long organizationId,
                               @Param("status") edu.orgresource.extended.domain.AllocationStatus status,
                               @Param("sampledNow") java.time.LocalDateTime sampledNow);

    String COVERAGE_TOTAL = "sum(case when a.status in "
            + "(edu.orgresource.extended.domain.AllocationStatus.ISSUED, edu.orgresource.extended.domain.AllocationStatus.RETURNED, "
            + "edu.orgresource.extended.domain.AllocationStatus.CONSUMED) then a.quantity "
            + "when a.status = edu.orgresource.extended.domain.AllocationStatus.APPROVED and p.endAt > :sampledNow "
            + "then a.quantity else 0 end)";

    @Query("select coalesce(" + COVERAGE_TOTAL + ", 0) from Allocation a join a.requirement q join q.project p "
            + "where q.id = :requirementId and a.organizationId = :organizationId")
    Long sumCoveredQuantity(@Param("requirementId") Long requirementId,
                            @Param("organizationId") Long organizationId,
                            @Param("sampledNow") java.time.LocalDateTime sampledNow);

    @Query("select q.id, " + COVERAGE_TOTAL + " from Allocation a join a.requirement q join q.project p "
            + "where q.id in :ids and a.organizationId=:organizationId group by q.id")
    List<Object[]> sumPageCoverage(@Param("ids") List<Long> ids, @Param("organizationId") long organizationId,
            @Param("sampledNow") LocalDateTime sampledNow);
}

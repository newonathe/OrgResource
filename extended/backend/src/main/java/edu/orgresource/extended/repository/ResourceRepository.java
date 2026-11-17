package edu.orgresource.extended.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.orgresource.extended.domain.Resource;
import edu.orgresource.extended.domain.ResourceType;
import jakarta.persistence.LockModeType;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByOrganizationIdOrderById(Long organizationId);
    List<Resource> findByOrganizationIdAndTypeOrderById(Long organizationId, ResourceType type);
    Optional<Resource> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<Resource> findByPublicIdAndOrganizationId(java.util.UUID publicId, Long organizationId);
    boolean existsByOrganizationIdAndNameKey(Long organizationId, String nameKey);
    boolean existsByOrganizationIdAndNameKeyAndIdNot(Long organizationId, String nameKey, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resource r where r.id = :id and r.organizationId = :organizationId")
    Optional<Resource> lockById(@Param("id") Long id, @Param("organizationId") Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resource r where r.publicId = :publicId and r.organizationId = :organizationId")
    Optional<Resource> lockByPublicIdAndOrganizationId(@Param("publicId") java.util.UUID publicId,
                                                        @Param("organizationId") Long organizationId);

    @Query("select coalesce(sum(case when a.status = edu.orgresource.extended.domain.AllocationStatus.APPROVED "
            + "and p.endAt > :sampledNow then a.quantity "
            + "when a.status = edu.orgresource.extended.domain.AllocationStatus.ISSUED "
            + "and r.type = :reusable then a.quantity else 0 end), 0) "
            + "from Allocation a join a.requirement q join q.project p join q.resource r "
            + "where q.resource.id = :resourceId and a.organizationId = :organizationId")
    Long sumEffectiveHoldsAndIssuedCustody(@Param("resourceId") Long resourceId,
                                          @Param("organizationId") Long organizationId,
                                          @Param("sampledNow") java.time.LocalDateTime sampledNow,
                                          @Param("reusable") ResourceType reusable);
}

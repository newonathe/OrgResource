package edu.orgresource.extended.identity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface OwnershipTransferRepository extends JpaRepository<OwnershipTransfer, Long> {
    Optional<OwnershipTransfer> findByOrganizationIdAndPublicId(Long organizationId, java.util.UUID publicId);
    boolean existsByOrganizationIdAndStatus(Long organizationId, OwnershipTransfer.Status status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from OwnershipTransfer t where t.organizationId = :org and t.status = 'PENDING' order by t.id")
    List<OwnershipTransfer> lockPendingByOrganization(@Param("org") Long orgId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from OwnershipTransfer t where t.organizationId = :org and t.publicId = :id")
    Optional<OwnershipTransfer> lockByOrgAndPublicId(@Param("org") Long orgId, @Param("id") java.util.UUID id);
    List<OwnershipTransfer> findTop100ByStatusAndExpiresAtLessThanEqualOrderByOrganizationIdAscIdAsc(
            OwnershipTransfer.Status status, java.time.LocalDateTime expiresAt);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from OwnershipTransfer t where t.id = :id")
    Optional<OwnershipTransfer> lockById(@Param("id") Long id);
}

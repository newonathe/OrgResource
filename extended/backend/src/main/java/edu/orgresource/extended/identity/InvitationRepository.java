package edu.orgresource.extended.identity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    interface LockTarget {
        Long getId();
        Long getOrganizationId();
    }
    java.util.Optional<Invitation> findByOrganizationIdAndPublicId(Long organizationId, java.util.UUID publicId);
    java.util.Optional<Invitation> findByTokenHash(byte[] hash);
    @Query("select i.id as id, i.organizationId as organizationId from Invitation i where i.tokenHash = :hash")
    Optional<LockTarget> findLockTargetByTokenHash(@Param("hash") byte[] hash);
    java.util.List<Invitation> findTop100ByStatusAndExpiresAtLessThanEqualOrderByOrganizationIdAscIdAsc(
            Invitation.Status status, java.time.LocalDateTime expiresAt);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invitation i where i.id = :id")
    Optional<Invitation> lockById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invitation i where i.tokenHash = :hash")
    Optional<Invitation> lockByTokenHash(@Param("hash") byte[] hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invitation i where i.organizationId = :org and i.publicId = :id")
    Optional<Invitation> lockByOrgAndPublicId(@Param("org") Long orgId, @Param("id") java.util.UUID id);
}

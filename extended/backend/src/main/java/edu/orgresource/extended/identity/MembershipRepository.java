package edu.orgresource.extended.identity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    interface BorrowerRow {
        long getId();
        java.util.UUID getMembershipId();
        String getDisplayName();
    }

    @Query("select m.id as id,m.publicId as membershipId,u.displayName as displayName "
            + "from Membership m join UserAccount u on u.id=m.userId "
            + "where m.organizationId=:org and m.status=:membershipStatus and u.status=:accountStatus and m.id>:after "
            + "and (:role is null or m.role=:role) and (:search is null or locate(:search,lower(u.displayName))>0) order by m.id")
    List<BorrowerRow> findBorrowerOptions(@Param("org") long organizationId, @Param("after") long after,
            @Param("search") String search, @Param("membershipStatus") Membership.Status membershipStatus,
            @Param("accountStatus") UserAccount.Status accountStatus, @Param("role") Membership.Role role, Pageable limit);

    Optional<Membership> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    Optional<Membership> findByOrganizationIdAndPublicId(Long organizationId, java.util.UUID publicId);
    interface AccountOrganizationRow {
        java.util.UUID getMembershipId();
        java.util.UUID getOrganizationId();
        String getSlug();
        String getDisplayName();
        Organization.Status getOrganizationStatus();
        Membership.Role getRole();
        Membership.Status getMembershipStatus();
        long getVersion();
    }

    @Query("select m.publicId as membershipId,o.publicId as organizationId,o.slug as slug,o.displayName as displayName, "
            + "o.status as organizationStatus,m.role as role,m.status as membershipStatus,m.version as version "
            + "from Membership m join Organization o on o.id=m.organizationId "
            + "where m.userId=:userId and m.status=:status order by o.id")
    List<AccountOrganizationRow> findAccountOrganizations(@Param("userId") long userId,
                                                            @Param("status") Membership.Status status);
    List<Membership> findByOrganizationIdAndStatusOrderById(Long organizationId, Membership.Status status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Membership m where m.organizationId = :org and m.id in :ids order by m.id")
    List<Membership> lockByOrganizationAndIds(@Param("org") Long orgId, @Param("ids") List<Long> ids);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Membership m where m.organizationId = :org and m.userId = :user")
    Optional<Membership> lockByOrganizationAndUser(@Param("org") Long orgId, @Param("user") Long userId);
    @Query("select count(m) from Membership m where m.organizationId = :org and m.role = :role and m.status = :status")
    long countByOrganizationAndRoleAndStatus(@Param("org") Long orgId, @Param("role") Membership.Role role,
                                             @Param("status") Membership.Status status);
    @Query("select m.organizationId from Membership m where m.userId = :user and m.status = :status order by m.organizationId")
    List<Long> organizationIdsByUserAndStatus(@Param("user") Long userId, @Param("status") Membership.Status status);

    @Query(value = "select count(*) from membership m join user_account u on u.id=m.user_id "
            + "where m.org_id=:org and m.role='OWNER' and m.status='ACTIVE' and u.status='ACTIVE'", nativeQuery = true)
    long countEffectiveActiveOwners(@Param("org") Long organizationId);

    List<Membership> findByOrganizationIdAndIdGreaterThanOrderById(Long organizationId, long afterId,
                                                                     org.springframework.data.domain.Pageable page);
}

package edu.orgresource.extended.identity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "invitation")
public class Invitation {
    public enum Status { INVITED, ACCEPTED, REVOKED, EXPIRED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    public UUID publicId;
    @Column(name = "org_id", nullable = false)
    public Long organizationId;
    @Column(name = "invited_by_membership_id", nullable = false)
    public Long invitedByMembershipId;
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false, length = 16)
    public Membership.Role role;
    @Column(name = "token_hash", nullable = false, unique = true, columnDefinition = "BINARY(32)")
    public byte[] tokenHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    public Status status;
    @Column(name = "accepted_by_user_id")
    public Long acceptedByUserId;
    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;
    @Version @Column(nullable = false)
    public long version;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    protected Invitation() { }
    public Invitation(Long organizationId, Long inviterId, Membership.Role role, byte[] tokenHash,
                      LocalDateTime now, LocalDateTime expiresAt) {
        this.publicId = UUID.randomUUID(); this.organizationId = organizationId;
        this.invitedByMembershipId = inviterId; this.role = role; this.tokenHash = tokenHash;
        this.status = Status.INVITED; this.createdAt = now; this.updatedAt = now; this.expiresAt = expiresAt;
    }
}

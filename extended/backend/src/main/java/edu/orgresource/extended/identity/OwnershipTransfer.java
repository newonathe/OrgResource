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
@Table(name = "ownership_transfer")
public class OwnershipTransfer {
    public enum Status { PENDING, ACCEPTED, REVOKED, EXPIRED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    public UUID publicId;
    @Column(name = "org_id", nullable = false)
    public Long organizationId;
    @Column(name = "from_membership_id", nullable = false)
    public Long fromMembershipId;
    @Column(name = "to_membership_id", nullable = false)
    public Long toMembershipId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    public Status status;
    @Version @Column(nullable = false)
    public long version;
    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @Column(name = "accepted_at")
    public LocalDateTime acceptedAt;

    protected OwnershipTransfer() { }
    public OwnershipTransfer(Long organizationId, Long from, Long to,
                             LocalDateTime now, LocalDateTime expiresAt) {
        this.publicId = UUID.randomUUID(); this.organizationId = organizationId;
        this.fromMembershipId = from; this.toMembershipId = to; this.status = Status.PENDING;
        this.createdAt = now; this.expiresAt = expiresAt;
    }
}

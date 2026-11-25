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
@Table(name = "membership")
public class Membership {
    public enum Role { OWNER, ADMIN, CUSTODIAN, LEAD, MEMBER }
    public enum Status { ACTIVE, SUSPENDED, LEFT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    public UUID publicId;
    @Column(name = "org_id", nullable = false)
    public Long organizationId;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    public Role role;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    public Status status;
    @Version @Column(nullable = false)
    public long version;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    protected Membership() { }
    public Membership(Long organizationId, Long userId, Role role, LocalDateTime now) {
        this.publicId = UUID.randomUUID(); this.organizationId = organizationId; this.userId = userId;
        this.role = role; this.status = Status.ACTIVE; this.createdAt = now; this.updatedAt = now;
    }
}

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
@Table(name = "user_account")
public class UserAccount {
    public enum Status { ACTIVE, DISABLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    public UUID publicId;
    @Column(name = "username_key", nullable = false, unique = true, length = 64)
    public String usernameKey;
    @Column(name = "display_name", nullable = false, length = 120)
    public String displayName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    public Status status = Status.ACTIVE;
    @Column(name = "credential_version", nullable = false)
    public long credentialVersion;
    @Version @Column(nullable = false)
    public long version;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    protected UserAccount() { }
    public UserAccount(String usernameKey, String displayName, LocalDateTime now) {
        this.publicId = UUID.randomUUID(); this.usernameKey = usernameKey; this.displayName = displayName;
        this.status = Status.ACTIVE; this.credentialVersion = 0; this.createdAt = now; this.updatedAt = now;
    }
}

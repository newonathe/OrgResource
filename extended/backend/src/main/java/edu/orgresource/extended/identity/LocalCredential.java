package edu.orgresource.extended.identity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "local_credential")
public class LocalCredential {
    @Id @Column(name = "user_id")
    public Long userId;
    @Column(name = "password_hash", nullable = false, length = 255)
    public String passwordHash;
    @Column(name = "changed_at", nullable = false)
    public LocalDateTime changedAt;

    protected LocalCredential() { }
    public LocalCredential(Long userId, String passwordHash, LocalDateTime now) {
        this.userId = userId; this.passwordHash = passwordHash; this.changedAt = now;
    }
}

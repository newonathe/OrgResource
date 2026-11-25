package edu.orgresource.extended.identity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_recovery_code")
public class AccountRecoveryCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "code_hash", nullable = false, unique = true, columnDefinition = "BINARY(32)")
    public byte[] codeHash;
    @Column(name = "batch_id", nullable = false, columnDefinition = "BINARY(16)")
    public UUID batchId;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;
    @Column(name = "used_at")
    public LocalDateTime usedAt;
    @Column(name = "revoked_at")
    public LocalDateTime revokedAt;

    protected AccountRecoveryCode() { }
    public AccountRecoveryCode(Long userId, byte[] codeHash, UUID batchId,
                               LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.userId = userId; this.codeHash = codeHash; this.batchId = batchId;
        this.createdAt = createdAt; this.expiresAt = expiresAt;
    }
}

package edu.orgresource.extended.identity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_event")
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "org_id")
    public Long organizationId;
    @Column(name = "actor_user_id")
    public Long actorUserId;
    @Column(nullable = false, length = 64)
    public String action;
    @Column(name = "target_type", nullable = false, length = 40)
    public String targetType;
    @Column(name = "target_id", length = 64)
    public String targetId;
    @Column(nullable = false, length = 16)
    public String outcome;
    @Column(name = "request_id", nullable = false, length = 36)
    public String requestId;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    protected AuditEvent() { }
    public AuditEvent(Long org, Long actor, String action, String targetType, String targetId,
                      String outcome, String requestId, LocalDateTime now) {
        this.organizationId = org; this.actorUserId = actor; this.action = action;
        this.targetType = targetType; this.targetId = targetId; this.outcome = outcome;
        this.requestId = requestId; this.createdAt = now;
    }
}

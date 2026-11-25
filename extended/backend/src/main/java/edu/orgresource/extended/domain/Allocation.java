package edu.orgresource.extended.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "allocation")
public class Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    public UUID publicId = UUID.randomUUID();

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @Column(name = "requirement_id", nullable = false)
    public Long requirementId;

    @Column(name = "resource_id", nullable = false)
    public Long resourceId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "requirement_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id",
                    insertable = false, updatable = false, nullable = false)
    })
    public ResourceRequirement requirement;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    @NotNull
    @Column(name = "borrower_name", nullable = false, length = 120, updatable = false)
    public String borrowerName;

    @Column(name = "borrower_membership_id", updatable = false)
    public Long borrowerMembershipId;

    @Column(name = "requested_by_membership_id", updatable = false)
    public Long requestedByMembershipId;

    @Column(name = "phone_ciphertext", columnDefinition = "VARBINARY(512)")
    public byte[] phoneCiphertext;

    @Column(name = "phone_nonce", columnDefinition = "BINARY(12)")
    public byte[] phoneNonce;

    @Column(name = "phone_key_id", length = 64)
    public String phoneKeyId;

    @NotNull
    @Column(name = "sms_consent", nullable = false)
    public Boolean smsConsent;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public AllocationStatus status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "issued_at")
    public LocalDateTime issuedAt;

    @Column(name = "returned_at")
    public LocalDateTime returnedAt;

    @Column(name = "consumed_at")
    public LocalDateTime consumedAt;

    @Column(name = "cancelled_at")
    public LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 24)
    public String cancellationReason;

    @Column(name = "reminder_claimed_at")
    public LocalDateTime reminderClaimedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_outcome", nullable = false, length = 24)
    public ReminderOutcome reminderOutcome = ReminderOutcome.NONE;

    @Column(name = "reminder_http_status")
    public Integer reminderHttpStatus;

    @Column(name = "provider_message_sid", length = 64)
    public String providerMessageSid;

    @Version @Column(nullable = false)
    public long version;

    protected Allocation() { }

    public Allocation(Long organizationId, ResourceRequirement requirement, Integer quantity,
                      String borrowerName, Boolean smsConsent, LocalDateTime createdAt) {
        this.organizationId = organizationId;
        this.requirement = requirement;
        this.requirementId = requirement.id;
        this.resourceId = requirement.resourceId;
        this.quantity = quantity;
        this.borrowerName = borrowerName;
        this.smsConsent = smsConsent;
        this.status = AllocationStatus.PENDING;
        this.createdAt = createdAt;
    }
}

package app.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "allocation", indexes = {
        @Index(name = "ix_allocation_requirement_id", columnList = "requirement_id"),
        @Index(name = "ix_allocation_status", columnList = "status"),
        @Index(name = "ix_allocation_created_at", columnList = "created_at")
})
public class Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requirement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_allocation_requirement"))
    public ResourceRequirement requirement;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    @NotNull
    @Column(name = "borrower_name", nullable = false, length = 100)
    public String borrowerName;

    @Column(name = "phone_number", length = 20)
    public String phoneNumber;

    @NotNull
    @Column(name = "sms_consent", nullable = false)
    public Boolean smsConsent = Boolean.FALSE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public AllocationStatus status = AllocationStatus.PENDING;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", length = 20)
    public CancellationReason cancellationReason;

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

    protected Allocation() { }

    public Long getId() { return id; }
    public ResourceRequirement getRequirement() { return requirement; }

    public Allocation(ResourceRequirement requirement, Integer quantity, String borrowerName,
                      String phoneNumber, boolean smsConsent, LocalDateTime createdAt) {
        this.requirement = requirement;
        this.quantity = quantity;
        this.borrowerName = borrowerName;
        this.phoneNumber = phoneNumber;
        this.smsConsent = smsConsent;
        this.status = AllocationStatus.PENDING;
        this.createdAt = createdAt;
        this.reminderOutcome = ReminderOutcome.NONE;
    }
}

package edu.orgresource.c0.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "allocation")
public class Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requirement_id", nullable = false)
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

    @Column(name = "cancelled_at")
    public LocalDateTime cancelledAt;

    protected Allocation() { }

    public Allocation(Long organizationId, ResourceRequirement requirement, Integer quantity,
                      String borrowerName, String phoneNumber, Boolean smsConsent, LocalDateTime createdAt) {
        this.organizationId = organizationId;
        this.requirement = requirement;
        this.quantity = quantity;
        this.borrowerName = borrowerName;
        this.phoneNumber = phoneNumber;
        this.smsConsent = smsConsent;
        this.status = AllocationStatus.PENDING;
        this.createdAt = createdAt;
    }
}

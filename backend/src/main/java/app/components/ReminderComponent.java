package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import app.entities.Allocation;
import app.entities.AllocationStatus;
import app.entities.ReminderOutcome;
import app.entities.ResourceType;
import app.reminders.ReminderDispatch;
import app.reminders.ReminderSendResult;
import app.repositories.AllocationRepository;

/** Durable, one-time reminder claim and narrow result persistence. */
@Component
public class ReminderComponent {
    private static final DateTimeFormatter MESSAGE_DATE =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.ENGLISH);

    @Autowired private AllocationRepository allocations;
    @Autowired private Clock philippineClock;
    @PersistenceContext private EntityManager entityManager;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean claimOne(Long allocationId) {
        Allocation allocation = lock(allocationId);
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (allocation == null || allocation.reminderClaimedAt != null
                || allocation.reminderOutcome != ReminderOutcome.NONE
                || !hasReminderWindow(allocation, now)) {
            return false;
        }
        if (!Boolean.TRUE.equals(allocation.smsConsent)) return false;
        if (!validE164(allocation.phoneNumber)) {
            allocation.reminderOutcome = ReminderOutcome.SUPPRESSED;
            allocation.reminderHttpStatus = null;
            allocation.providerMessageSid = null;
            allocations.saveAndFlush(allocation);
            return false;
        }

        allocation.reminderClaimedAt = now;
        allocation.reminderOutcome = ReminderOutcome.CLAIMED;
        allocation.reminderHttpStatus = null;
        allocation.providerMessageSid = null;
        allocations.saveAndFlush(allocation);
        return true;
    }

    /** Rechecks current eligibility under a short lock, then commits before the caller starts HTTP. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReminderDispatch prepareForDispatch(Long allocationId) {
        Allocation allocation = lock(allocationId);
        if (allocation == null) return null;
        LocalDateTime now = LocalDateTime.now(philippineClock);
        if (allocation.reminderOutcome != ReminderOutcome.CLAIMED
                || allocation.reminderClaimedAt == null
                || !isEligible(allocation, now)) {
            if (allocation.reminderOutcome == ReminderOutcome.CLAIMED
                    && allocation.reminderClaimedAt != null) {
                allocation.reminderOutcome = ReminderOutcome.SUPPRESSED;
                allocations.saveAndFlush(allocation);
            }
            return null;
        }

        LocalDateTime endAt = allocation.getRequirement().getProject().getEndAt();
        String message = "OrgResource reminder: your reusable allocation is due by "
                + MESSAGE_DATE.format(endAt) + ". Please coordinate its return with your organization.";
        return new ReminderDispatch(allocation.id, allocation.phoneNumber, message);
    }

    /** Updates reminder fields on a freshly locked entity without saving a stale detached Allocation. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean recordOutcome(Long allocationId, ReminderSendResult result) {
        Allocation allocation = lock(allocationId);
        if (allocation == null || allocation.reminderClaimedAt == null
                || allocation.reminderOutcome != ReminderOutcome.CLAIMED) {
            return false;
        }
        ReminderOutcome outcome = result == null ? ReminderOutcome.UNKNOWN : result.getOutcome();
        if (outcome != ReminderOutcome.SIMULATED && outcome != ReminderOutcome.PROVIDER_ACCEPTED
                && outcome != ReminderOutcome.REJECTED && outcome != ReminderOutcome.UNKNOWN) {
            outcome = ReminderOutcome.UNKNOWN;
        }
        allocation.reminderOutcome = outcome;
        allocation.reminderHttpStatus = result == null ? null : result.getHttpStatus();
        String sid = result == null ? null : result.getProviderMessageSid();
        allocation.providerMessageSid = sid != null && sid.length() <= 64 ? sid : null;
        allocations.saveAndFlush(allocation);
        return true;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean markStaleClaimUnknown(Long allocationId, LocalDateTime cutoff) {
        Allocation allocation = lock(allocationId);
        if (allocation == null || allocation.reminderOutcome != ReminderOutcome.CLAIMED
                || allocation.reminderClaimedAt == null || !allocation.reminderClaimedAt.isBefore(cutoff)) {
            return false;
        }
        allocation.reminderOutcome = ReminderOutcome.UNKNOWN;
        allocation.reminderHttpStatus = null;
        allocation.providerMessageSid = null;
        allocations.saveAndFlush(allocation);
        return true;
    }

    private Allocation lock(Long allocationId) {
        if (allocationId == null || allocationId <= 0) return null;
        Allocation allocation = allocations.lockById(allocationId).orElse(null);
        if (allocation != null && entityManager != null) entityManager.refresh(allocation);
        return allocation;
    }

    private boolean hasReminderWindow(Allocation allocation, LocalDateTime now) {
        if (allocation.status != AllocationStatus.ISSUED
                || allocation.getRequirement().getResource().getType() != ResourceType.REUSABLE) return false;
        LocalDateTime endAt = allocation.getRequirement().getProject().getEndAt();
        return !endAt.isBefore(now) && endAt.isBefore(now.plusHours(24));
    }

    private boolean isEligible(Allocation allocation, LocalDateTime now) {
        return hasReminderWindow(allocation, now)
                && Boolean.TRUE.equals(allocation.smsConsent)
                && validE164(allocation.phoneNumber);
    }

    private boolean validE164(String destination) {
        return destination != null && destination.matches("^\\+[1-9][0-9]{7,14}$");
    }
}

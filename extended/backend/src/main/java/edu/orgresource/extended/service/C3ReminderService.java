package edu.orgresource.extended.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import edu.orgresource.extended.api.C1ApiException;
import edu.orgresource.extended.domain.Allocation;
import edu.orgresource.extended.domain.AllocationStatus;
import edu.orgresource.extended.domain.ReminderOutcome;
import edu.orgresource.extended.domain.ResourceType;
import edu.orgresource.extended.repository.AllocationRepository;
import edu.orgresource.extended.security.ContactCipher;
import edu.orgresource.extended.integration.ReminderDispatch;
import edu.orgresource.extended.integration.ReminderSendResult;

@Service
public class C3ReminderService {
    private static final DateTimeFormatter MESSAGE_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.ENGLISH);
    private final AllocationRepository allocations;
    private final ContactCipher contactCipher;

    public C3ReminderService(AllocationRepository allocations, ContactCipher contactCipher) {
        this.allocations = allocations;
        this.contactCipher = contactCipher;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean claimOne(long allocationId, LocalDateTime sampledNow) {
        Allocation allocation = loadLocked(allocationId);
        boolean retryableKeyFailure = allocation != null && allocation.reminderOutcome == ReminderOutcome.KEY_UNAVAILABLE;
        if (allocation == null || (allocation.reminderClaimedAt != null && !retryableKeyFailure)
                || (allocation.reminderOutcome != ReminderOutcome.NONE && !retryableKeyFailure)
                || !hasReminderWindow(allocation, sampledNow)) return false;
        if (!Boolean.TRUE.equals(allocation.smsConsent) || allocation.phoneCiphertext == null
                || allocation.phoneNonce == null || allocation.phoneKeyId == null) {
            allocation.reminderOutcome = ReminderOutcome.SUPPRESSED;
            allocations.saveAndFlush(allocation);
            return false;
        }
        allocation.reminderClaimedAt = sampledNow;
        allocation.reminderOutcome = ReminderOutcome.CLAIMED;
        allocation.reminderHttpStatus = null;
        allocation.providerMessageSid = null;
        allocations.saveAndFlush(allocation);
        return true;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReminderDispatch prepareForDispatch(long allocationId, LocalDateTime sampledNow) {
        Allocation allocation = loadLocked(allocationId);
        if (allocation == null || allocation.reminderOutcome != ReminderOutcome.CLAIMED
                || allocation.reminderClaimedAt == null || !hasReminderWindow(allocation, sampledNow)
                || !Boolean.TRUE.equals(allocation.smsConsent)) {
            suppressClaim(allocation);
            return null;
        }
        try {
            String destination = contactCipher.decrypt(allocation.organizationId, allocation.publicId,
                    allocation.phoneCiphertext, allocation.phoneNonce, allocation.phoneKeyId);
            if (!destination.matches("^\\+[1-9][0-9]{7,14}$")) {
                invalidContactClaim(allocation);
                return null;
            }
            LocalDateTime dueAt = allocation.requirement.project.endAt;
            String body = "OrgResource reminder: your reusable allocation is due by " + MESSAGE_DATE.format(dueAt)
                    + ". Please coordinate its return with your organization.";
            return new ReminderDispatch(destination, body);
        } catch (C1ApiException unavailableOrCorrupt) {
            if ("CONTACT_ENCRYPTION_UNAVAILABLE".equals(unavailableOrCorrupt.getCode()))
                deferForKeyAvailability(allocation);
            else if ("CONTACT_INTEGRITY_FAILURE".equals(unavailableOrCorrupt.getCode()))
                invalidContactClaim(allocation);
            else
                suppressClaim(allocation);
            return null;
        }
    }

    /** Commits the consent check immediately before transport; no database lock spans network I/O. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean authorizeDispatch(long allocationId, LocalDateTime sampledNow) {
        Allocation allocation = loadLocked(allocationId);
        if (allocation == null || allocation.reminderOutcome != ReminderOutcome.CLAIMED
                || allocation.reminderClaimedAt == null || !hasReminderWindow(allocation, sampledNow)
                || !Boolean.TRUE.equals(allocation.smsConsent) || allocation.phoneCiphertext == null
                || allocation.phoneNonce == null || allocation.phoneKeyId == null) {
            suppressClaim(allocation);
            return false;
        }
        allocation.reminderOutcome = ReminderOutcome.DISPATCHING;
        allocations.saveAndFlush(allocation);
        return true;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean recordOutcome(long allocationId, ReminderSendResult result) {
        Allocation allocation = loadLocked(allocationId);
        if (allocation == null || allocation.reminderClaimedAt == null
                || allocation.reminderOutcome != ReminderOutcome.DISPATCHING) return false;
        ReminderOutcome outcome = result == null ? ReminderOutcome.UNKNOWN : result.outcome();
        if (outcome != ReminderOutcome.SIMULATED && outcome != ReminderOutcome.PROVIDER_ACCEPTED
                && outcome != ReminderOutcome.REJECTED && outcome != ReminderOutcome.UNKNOWN) outcome = ReminderOutcome.UNKNOWN;
        allocation.reminderOutcome = outcome;
        allocation.reminderHttpStatus = result == null ? null : result.httpStatus();
        String sid = result == null ? null : result.providerMessageSid();
        allocation.providerMessageSid = sid != null && sid.length() <= 64 ? sid : null;
        allocations.saveAndFlush(allocation);
        return true;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean markStaleClaimUnknown(long allocationId, LocalDateTime cutoff) {
        Allocation allocation = loadLocked(allocationId);
        if (allocation == null || (allocation.reminderOutcome != ReminderOutcome.CLAIMED
                && allocation.reminderOutcome != ReminderOutcome.DISPATCHING)
                || allocation.reminderClaimedAt == null || !allocation.reminderClaimedAt.isBefore(cutoff)) return false;
        allocation.reminderOutcome = ReminderOutcome.UNKNOWN;
        allocation.reminderHttpStatus = null;
        allocation.providerMessageSid = null;
        allocations.saveAndFlush(allocation);
        return true;
    }

    private Allocation loadLocked(long allocationId) {
        return allocations.lockById(allocationId).orElse(null);
    }

    private static boolean hasReminderWindow(Allocation allocation, LocalDateTime now) {
        if (allocation.status != AllocationStatus.ISSUED || allocation.requirement.resource.type != ResourceType.REUSABLE) return false;
        LocalDateTime endAt = allocation.requirement.project.endAt;
        return !endAt.isBefore(now) && endAt.isBefore(now.plusHours(24));
    }

    private void suppressClaim(Allocation allocation) {
        if (allocation != null && allocation.reminderOutcome == ReminderOutcome.CLAIMED) {
            allocation.reminderOutcome = ReminderOutcome.SUPPRESSED;
            allocation.reminderHttpStatus = null;
            allocation.providerMessageSid = null;
            allocations.saveAndFlush(allocation);
        }
    }

    private void deferForKeyAvailability(Allocation allocation) {
        if (allocation != null && allocation.reminderOutcome == ReminderOutcome.CLAIMED) {
            allocation.reminderOutcome = ReminderOutcome.KEY_UNAVAILABLE;
            allocation.reminderClaimedAt = null;
            allocation.reminderHttpStatus = null;
            allocation.providerMessageSid = null;
            allocations.saveAndFlush(allocation);
        }
    }

    private void invalidContactClaim(Allocation allocation) {
        if (allocation != null && allocation.reminderOutcome == ReminderOutcome.CLAIMED) {
            allocation.reminderOutcome = ReminderOutcome.INVALID_CONTACT;
            allocation.reminderHttpStatus = null;
            allocation.providerMessageSid = null;
            allocations.saveAndFlush(allocation);
        }
    }
}

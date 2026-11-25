package edu.orgresource.extended.scheduler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.domain.ReminderMode;
import edu.orgresource.extended.repository.AllocationRepository;
import edu.orgresource.extended.security.ContactCipher;
import edu.orgresource.extended.service.C3ReminderDispatcher;
import edu.orgresource.extended.service.C3ReminderService;
import edu.orgresource.extended.service.InventoryService;

/** Bounded PHT expiry and reminder worker; external HTTP is performed after claim transactions commit. */
@Component
public class C1ReminderScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(C1ReminderScheduler.class);
    private final AllocationRepository allocations;
    private final InventoryService inventory;
    private final C3ReminderService reminders;
    private final C3ReminderDispatcher dispatcher;
    private final ContactCipher contactCipher;
    private final JdbcTemplate jdbc;
    private final Clock philippineClock;
    private final Clock businessClock;
    private final int batchSize;
    private final ReminderMode reminderMode;

    public C1ReminderScheduler(AllocationRepository allocations, InventoryService inventory,
            C3ReminderService reminders, C3ReminderDispatcher dispatcher, ContactCipher contactCipher,
            JdbcTemplate jdbc, Clock philippineClock, @Qualifier("businessClock") Clock businessClock,
            @Value("${c1.scheduler.batch-size:100}") int batchSize,
            @Value("${c1.reminder.mode:DISABLED}") String mode,
            @Value("${c1.sms.enabled:false}") boolean legacySmsEnabled) {
        if (legacySmsEnabled) throw new IllegalStateException("Legacy SMS enablement is disabled; use a loopback-only reminder mode.");
        this.allocations = allocations;
        this.inventory = inventory;
        this.reminders = reminders;
        this.dispatcher = dispatcher;
        this.contactCipher = contactCipher;
        this.jdbc = jdbc;
        this.philippineClock = philippineClock;
        this.businessClock = businessClock;
        this.batchSize = batchSize;
        try { this.reminderMode = ReminderMode.valueOf(mode); }
        catch (RuntimeException invalid) { throw new IllegalStateException("c1.reminder.mode must be DISABLED, FAKE_TWILIO, or TWILIO_VIRTUAL."); }
    }

    @Scheduled(fixedDelayString = "${c1.scheduler.fixed-delay-ms:60000}")
    public void scheduledReminderTick() {
        processOnce();
    }

    public WorkResult processOnce() {
        if (batchSize < 1 || batchSize > 1000) {
            LOG.error("Lifecycle tick skipped because c1.scheduler.batch-size must be between 1 and 1000.");
            return new WorkResult(0, 0, 0);
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        LocalDateTime projectNow = LocalDateTime.now(businessClock);
        PageRequest page = PageRequest.of(0, batchSize);
        int expiredCount = 0;
        for (AllocationRepository.ExpiryCandidate candidate : allocations.findExpiredCandidates(projectNow, page)) {
            try {
                if (inventory.expireIfEligible(candidate.getOrganizationId(), candidate.getAllocationId(), projectNow)) expiredCount++;
            } catch (RuntimeException failure) {
                LOG.warn("Expiry processing failed for Allocation {} ({}).", candidate.getAllocationId(), failure.getClass().getSimpleName());
            }
        }

        int staleClaims = 0;
        LocalDateTime staleCutoff = now.minusMinutes(15);
        for (AllocationRepository.ReminderCandidate candidate : allocations.findStaleReminderClaims(staleCutoff, page)) {
            try {
                if (reminders.markStaleClaimUnknown(candidate.getAllocationId(), staleCutoff)) staleClaims++;
            } catch (RuntimeException failure) {
                LOG.warn("Stale reminder claim recovery failed for Allocation {} ({}).", candidate.getAllocationId(), failure.getClass().getSimpleName());
            }
        }

        try {
            LocalDateTime utcNow = LocalDateTime.ofInstant(philippineClock.instant(), ZoneOffset.UTC);
            int pruned = jdbc.update("DELETE FROM idempotency_key WHERE state='COMPLETED' AND expires_at<=? "
                            + "ORDER BY expires_at,id LIMIT ?", Timestamp.valueOf(utcNow), batchSize);
            if (pruned > 0) LOG.info("Pruned {} expired HTTP idempotency receipts; permanent stock movements were retained.", pruned);
        } catch (RuntimeException failure) {
            LOG.warn("Expired HTTP receipt cleanup failed ({}).", failure.getClass().getSimpleName());
        }

        int remindersProcessed = 0;
        if (reminderMode != ReminderMode.DISABLED) {
            for (AllocationRepository.ReminderCandidate candidate : allocations.findReminderCandidates(
                    now, now.plusHours(24), contactCipher.availableKeyId(), page)) {
                try {
                    dispatcher.dispatchOne(candidate.getAllocationId(), now, reminderMode);
                    remindersProcessed++;
                } catch (RuntimeException failure) {
                    LOG.warn("Reminder processing failed for Allocation {} ({}).", candidate.getAllocationId(), failure.getClass().getSimpleName());
                }
            }
        }
        return new WorkResult(expiredCount, remindersProcessed, staleClaims);
    }

    public record WorkResult(int expiredAllocations, int remindersProcessed, int staleClaimsMarkedUnknown) { }
}

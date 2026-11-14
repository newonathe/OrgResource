package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import app.entities.AllocationStatus;
import app.entities.ReminderOutcome;
import app.entities.ResourceType;
import app.reminders.ReminderMode;
import app.repositories.AllocationRepository;

/** Upcoming-return reminders are scheduled independently of O5 expiry cancellation. */
@Component
@ConditionalOnProperty(name = "orgresource.reminders.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class ReminderScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(ReminderScheduler.class);

    @Autowired private AllocationRepository allocations;
    @Autowired private ReminderDispatcher dispatcher;
    @Autowired private ReminderComponent reminders;
    @Autowired private ReminderMode mode;
    @Autowired private Clock philippineClock;
    @Value("${orgresource.reminders.batch-size:100}") private int batchSize;
    @Value("${orgresource.reminders.claim-stale-after-seconds:300}") private long staleAfterSeconds;

    @Scheduled(fixedDelayString = "${orgresource.reminders.fixed-delay-ms:3600000}")
    public void scanUpcomingReturns() {
        if (mode == ReminderMode.DISABLED) return;
        if (batchSize < 1) {
            LOG.error("Reminder scan skipped because orgresource.reminders.batch-size must be positive.");
            return;
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        List<Long> candidates = allocations.findReminderCandidateIds(
                AllocationStatus.ISSUED, ResourceType.REUSABLE, ReminderOutcome.NONE,
                now, now.plusHours(24), PageRequest.of(0, batchSize));
        for (Long id : candidates) {
            try {
                dispatcher.dispatchOne(id);
            } catch (RuntimeException failure) {
                LOG.warn("Reminder processing failed for Allocation {} ({}).",
                        id, failure.getClass().getSimpleName());
            }
        }
    }

    @Scheduled(fixedDelayString = "${orgresource.reminders.recovery-fixed-delay-ms:60000}")
    public void reconcileStaleClaims() {
        if (batchSize < 1 || staleAfterSeconds < 1) return;
        LocalDateTime cutoff = LocalDateTime.now(philippineClock).minusSeconds(staleAfterSeconds);
        List<Long> stale = allocations.findStaleReminderClaimIds(
                ReminderOutcome.CLAIMED, cutoff, PageRequest.of(0, batchSize));
        for (Long id : stale) {
            try {
                if (reminders.markStaleClaimUnknown(id, cutoff)) {
                    LOG.warn("Stale reminder claim marked UNKNOWN for Allocation {}.", id);
                }
            } catch (RuntimeException failure) {
                LOG.warn("Stale reminder reconciliation failed for Allocation {} ({}).",
                        id, failure.getClass().getSimpleName());
            }
        }
    }
}

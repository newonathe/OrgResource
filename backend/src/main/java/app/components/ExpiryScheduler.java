package app.components;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import app.entities.AllocationStatus;
import app.repositories.AllocationRepository;

@Component
public class ExpiryScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(ExpiryScheduler.class);
    private static final List<AllocationStatus> OPEN_STATUSES = Arrays.asList(
            AllocationStatus.PENDING, AllocationStatus.APPROVED);

    @Autowired private AllocationRepository allocations;
    @Autowired private AllocationComponent allocationComponent;
    @Autowired private Clock philippineClock;
    @Value("${orgresource.expiry.batch-size:100}") private int batchSize;

    @Scheduled(fixedDelayString = "${orgresource.expiry.fixed-delay-ms:3600000}")
    public void scanExpiredAllocations() {
        if (batchSize < 1) {
            LOG.error("Expiry scan skipped because orgresource.expiry.batch-size must be positive.");
            return;
        }
        LocalDateTime now = LocalDateTime.now(philippineClock);
        List<Long> candidates = allocations.findExpiredCandidateIds(
                OPEN_STATUSES, now, PageRequest.of(0, batchSize));
        for (Long id : candidates) {
            try {
                if (allocationComponent.expireIfEligible(id)) {
                    LOG.info("Expired open Allocation {}.", id);
                }
            } catch (RuntimeException failure) {
                LOG.warn("Expiry processing failed for Allocation {} ({}).",
                        id, failure.getClass().getSimpleName());
            }
        }
    }
}

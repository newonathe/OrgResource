package edu.orgresource.extended.scheduler;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class C1ReminderScheduler {
    private final AtomicLong disabledTicks = new AtomicLong();

    public C1ReminderScheduler(@Value("${c1.sms.enabled:false}") boolean smsEnabled) {
        if (smsEnabled) {
            throw new IllegalStateException("This isolated C1 proof never enables outbound SMS.");
        }
    }

    @Scheduled(fixedDelayString = "${c1.scheduler.fixed-delay-ms:60000}")
    public void scheduledReminderTick() {
        disabledTicks.incrementAndGet();
    }

    public long disabledTickCount() {
        return disabledTicks.get();
    }
}

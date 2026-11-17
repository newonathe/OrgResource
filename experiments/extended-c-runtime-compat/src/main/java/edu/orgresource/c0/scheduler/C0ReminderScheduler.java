package edu.orgresource.c0.scheduler;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class C0ReminderScheduler {
    private final AtomicLong disabledTicks = new AtomicLong();

    public C0ReminderScheduler(@Value("${c0.sms.enabled:false}") boolean smsEnabled) {
        if (smsEnabled) {
            throw new IllegalStateException("This isolated C0 proof never enables outbound SMS.");
        }
    }

    @Scheduled(fixedDelayString = "${c0.scheduler.fixed-delay-ms:60000}")
    public void scheduledReminderTick() {
        disabledTicks.incrementAndGet();
    }

    public long disabledTickCount() {
        return disabledTicks.get();
    }
}

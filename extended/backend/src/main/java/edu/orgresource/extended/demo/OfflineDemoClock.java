package edu.orgresource.extended.demo;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile(C8DemoCommand.PROFILE)
@ConditionalOnNotWebApplication
public class OfflineDemoClock {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private volatile Instant now = C8DemoCommand.AS_OF.atStartOfDay(MANILA).toInstant();

    public void sampleAt(LocalDateTime businessTime) {
        now = businessTime.atZone(MANILA).toInstant();
    }

    @Bean
    Clock businessClock() {
        return forZone(MANILA);
    }

    private Clock forZone(ZoneId zone) {
        return new Clock() {
            @Override public ZoneId getZone() { return zone; }
            @Override public Clock withZone(ZoneId changed) { return forZone(changed); }
            @Override public Instant instant() { return now; }
        };
    }
}

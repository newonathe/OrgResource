package edu.orgresource.extended.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import edu.orgresource.extended.demo.C8DemoCommand;

@Configuration
public class C1ClockConfiguration {
    @Bean @Primary
    Clock applicationClock() {
        return Clock.system(ZoneId.of("Asia/Manila"));
    }

    @Bean @Profile("!" + C8DemoCommand.PROFILE)
    Clock businessClock(Clock applicationClock) {
        return applicationClock;
    }
}

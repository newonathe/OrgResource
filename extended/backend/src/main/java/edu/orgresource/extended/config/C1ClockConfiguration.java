package edu.orgresource.extended.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class C1ClockConfiguration {
    @Bean
    Clock applicationClock() {
        return Clock.system(ZoneId.of("Asia/Manila"));
    }
}

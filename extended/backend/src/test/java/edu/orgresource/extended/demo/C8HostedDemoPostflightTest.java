package edu.orgresource.extended.demo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class C8HostedDemoPostflightTest {
    @Test void rejectsMissingArgumentsWithoutDatabaseAccess() {
        assertThrows(IllegalArgumentException.class,
            () -> C8HostedDemoPostflight.run(new String[0], Map.of()));
    }
    @Test void rejectsMissingFingerprintWithoutDatabaseAccess() {
        assertThrows(IllegalArgumentException.class,
            () -> C8HostedDemoPostflight.run(new String[]{C8HostedDemoPostflight.ARGUMENT}, Map.of()));
    }
    @Test void rejectsMalformedTargetBeforeReadingPlanOrDatabase() {
        Map<String,String> env = new HashMap<>();
        env.put("EXT_C8_HOSTED_EXPECTED_FINGERPRINT", "ab".repeat(32));
        env.put("EXT_C8_HOSTED_EXPECTED_SCHEMA", "non-orgresource-schema");
        assertThrows(IllegalArgumentException.class,
            () -> C8HostedDemoPostflight.run(new String[]{C8HostedDemoPostflight.ARGUMENT}, env));
    }
}

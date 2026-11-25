package edu.orgresource.extended.demo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class C8HostedOriginalBaselineTest {
    @Test void requiresExactCommand() {
        assertThrows(IllegalArgumentException.class,
            () -> C8HostedOriginalBaseline.run(new String[]{"--bootstrap"},Map.of()));
    }
    @Test void refusesUnspecifiedMode() {
        assertThrows(IllegalArgumentException.class,
            () -> C8HostedOriginalBaseline.run(new String[]{C8HostedOriginalBaseline.ARGUMENT},Map.of()));
    }
    @Test void refusesUnpinnedSchema() {
        Map<String,String> env=new HashMap<>();
        env.put("EXT_C8_BASELINE_MODE","CAPTURE");
        env.put("EXT_C8_HOSTED_EXPECTED_SCHEMA","other_database");
        env.put("EXT_C8_HOSTED_EXPECTED_FINGERPRINT","a".repeat(64));
        assertThrows(IllegalArgumentException.class,
            () -> C8HostedOriginalBaseline.run(new String[]{C8HostedOriginalBaseline.ARGUMENT},env));
    }
}

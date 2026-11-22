package edu.orgresource.extended.demo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.util.Map;

class C8HostedDemoPreflightTest {
    @Test void neverStartsDatabaseWithoutAnApprovedFullPlan() throws Exception {
        var testPlan = Files.createTempFile("c8-private-plan-unapproved-", ".json");
        try {
            Files.writeString(testPlan, "{\"generator\":\"orgresource-c8-plan-v1\",\"synthetic\":true}");
            assertThrows(IllegalArgumentException.class, () -> C8HostedDemoPreflight.requireApprovedPlan(testPlan));
        } finally { Files.deleteIfExists(testPlan); }
    }

    @Test void missingTargetIdentityFailsBeforeDatabaseConnection() {
        assertThrows(IllegalArgumentException.class, () -> C8HostedDemoPreflight.run(
                new String[]{C8HostedDemoPreflight.ARGUMENT}, Map.of()));
    }

    @Test void refusesAnyOtherCommandShapeWithoutDoingWork() {
        assertThrows(IllegalArgumentException.class, () -> C8HostedDemoPreflight.run(
                new String[]{"--c8-hosted-demo-import"}, Map.of()));
    }
}

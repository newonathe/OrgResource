package edu.orgresource.extended.demo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class C8HostedDemoImportCommandTest {
    @Test void refusesAbsentApprovalBeforeAnyMySqlConnection() {
        assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, Map.of()));
    }

    @Test void refusesNoBackupEvenWhenFingerprintAndConfirmationAreFilled() {
        String fingerprint = "a".repeat(64);
        Map<String, String> settings = new HashMap<>();
        settings.put("EXT_C8_HOSTED_EXPECTED_FINGERPRINT", fingerprint);
        settings.put("EXT_C8_HOSTED_CONFIRM", "IMPORT 96 INTO " + fingerprint);
        assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, settings));
    }

    @Test void refusesAnUnverifiedRestoreAttestationBeforeReadingSecrets() {
        String fingerprint = "a".repeat(64);
        Map<String, String> settings = new HashMap<>();
        settings.put("EXT_C8_HOSTED_EXPECTED_FINGERPRINT", fingerprint);
        settings.put("EXT_C8_HOSTED_CONFIRM", "IMPORT 96 INTO " + fingerprint);
        settings.put("EXT_C8_HOSTED_BACKUP_ID", "confirmed-backup-id-01");
        settings.put("EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256", "b".repeat(64));
        settings.put("EXT_C8_HOSTED_RESTORE_ATTESTATION", "UNVERIFIED");
        assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, settings));
    }

    @Test void refusesAnInheritedTestProfileBeforeConnecting() {
        String fingerprint = "a".repeat(64);
        Map<String, String> settings = new HashMap<>();
        settings.put("EXT_C8_HOSTED_EXPECTED_FINGERPRINT", fingerprint);
        settings.put("EXT_C8_HOSTED_CONFIRM", "IMPORT 96 INTO " + fingerprint);
        settings.put("EXT_C8_HOSTED_BACKUP_ID", "confirmed-backup-id-01");
        settings.put("EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256", "b".repeat(64));
        settings.put("EXT_C8_HOSTED_RESTORE_ATTESTATION", "I VERIFIED ISOLATED RESTORE");
        settings.put("EXT_SMS_ENABLED", "false");
        settings.put("EXT_REMINDER_MODE", "DISABLED");
        settings.put("SPRING_PROFILES_ACTIVE", "c1-test");
        assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, settings));
    }

    private static Map<String,String> authorizedPreflightSettings() {
        Map<String,String> settings = new HashMap<>();
        String fingerprint = "a".repeat(64);
        settings.put("EXT_C8_HOSTED_EXPECTED_FINGERPRINT", fingerprint);
        settings.put("EXT_C8_HOSTED_CONFIRM", "IMPORT 96 INTO " + fingerprint);
        settings.put("EXT_C8_HOSTED_BACKUP_ID", "confirmed-backup-id-01");
        settings.put("EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256", "b".repeat(64));
        settings.put("EXT_C8_HOSTED_RESTORE_ATTESTATION", "I VERIFIED ISOLATED RESTORE");
        settings.put("EXT_SMS_ENABLED", "false");
        settings.put("EXT_REMINDER_MODE", "DISABLED");
        return settings;
    }

    @Test void refusesMissingOriginalBaselineBeforeMySqlConnection() {
        Map<String,String> settings = authorizedPreflightSettings();
        var failure = assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, settings));
        org.junit.jupiter.api.Assertions.assertTrue(failure.getMessage().contains("EXT_C8_BASELINE_MODE"));
    }

    @Test void refusesCaptureInsteadOfVerifyBeforeMySqlConnection() {
        Map<String,String> settings = authorizedPreflightSettings();
        settings.put("EXT_C8_BASELINE_MODE", "CAPTURE");
        var failure = assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, settings));
        org.junit.jupiter.api.Assertions.assertTrue(failure.getMessage().contains("never CAPTURE"));
    }

    @Test void refusesRelativeOriginalEvidencePathBeforeMySqlConnection() {
        Map<String,String> settings = authorizedPreflightSettings();
        settings.put("EXT_C8_BASELINE_MODE", "VERIFY");
        settings.put("EXT_C8_BASELINE_FILE", "private-baseline.properties");
        var failure = assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, settings));
        org.junit.jupiter.api.Assertions.assertTrue(failure.getMessage().contains("absolute path"));
    }

    @Test void refusesInvalidOriginalEvidenceHmacKeyBeforeMySqlConnection(@TempDir Path tempDir) {
        Map<String,String> settings = authorizedPreflightSettings();
        settings.put("EXT_C8_BASELINE_MODE", "VERIFY");
        settings.put("EXT_C8_BASELINE_FILE", tempDir.toAbsolutePath().resolve("private-baseline.properties").toString());
        settings.put("EXT_C8_BASELINE_HMAC_HEX", "invalid");
        var failure = assertThrows(IllegalArgumentException.class,
                () -> C8HostedDemoImportCommand.run(null,
                        new String[]{C8HostedDemoImportCommand.ARGUMENT}, settings));
        org.junit.jupiter.api.Assertions.assertTrue(failure.getMessage().contains("32-byte"));
    }
}

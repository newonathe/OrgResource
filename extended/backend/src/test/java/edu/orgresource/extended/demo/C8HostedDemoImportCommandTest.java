package edu.orgresource.extended.demo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

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
}

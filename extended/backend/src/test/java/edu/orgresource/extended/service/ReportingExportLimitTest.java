package edu.orgresource.extended.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.orgresource.extended.api.C1ApiException;
import org.junit.jupiter.api.Test;

class ReportingExportLimitTest {
    @Test
    void exportAcceptsExactlyFourMiBAndRejectsTheNextByte() {
        int limit = 4 * 1024 * 1024;
        assertDoesNotThrow(() -> ReportingComponent.requireExportByteLimit(limit - 1));
        assertDoesNotThrow(() -> ReportingComponent.requireExportByteLimit(limit));
        C1ApiException error = assertThrows(C1ApiException.class,
                () -> ReportingComponent.requireExportByteLimit(limit + 1));
        assertEquals(413, error.getStatus());
        assertEquals("EXPORT_TOO_LARGE", error.getCode());
    }
}

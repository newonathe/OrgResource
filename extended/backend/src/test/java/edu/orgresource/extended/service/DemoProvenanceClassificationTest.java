package edu.orgresource.extended.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.orgresource.extended.api.C1ApiException;

class DemoProvenanceClassificationTest {
    private static final String FULL = "orgresource-c8-plan-v1-012345678901-abcdefabcdef";
    private static final String SMALL = FULL + "-small";
    private static final String SOURCE = "57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2";
    private static final String PLAN = "3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c";

    @Test void emptyDatabaseOrOrdinaryWorkspacesRemainUnclassified() {
        var result = DemoProvenance.classify(1, 0, null, null, false, false, null, null);
        assertFalse(result.synthetic());
    }

    @Test void standaloneFullApprovedCohortRetainsTheExistingLabel() {
        var result = DemoProvenance.classify(96, 96, FULL, "2026-11-19", true, true, SOURCE, PLAN);
        assertTrue(result.synthetic());
        assertEquals(DemoProvenance.NOTICE, result.notice());
    }

    @Test void approvedFullCohortCanCoexistWithOriginalAccountAndWorkspace() {
        var result = DemoProvenance.classify(97, 96, FULL, "2026-11-19", true, true, SOURCE, PLAN);
        assertTrue(result.synthetic());
        assertEquals(DemoProvenance.MIXED_NOTICE, result.notice());
        assertTrue(result.notice().contains("Existing untagged workspaces"));
    }

    @Test void mixedCohortFailsClosedWithoutExactApprovedSourceAndPlanHashes() {
        assertThrows(C1ApiException.class, () -> DemoProvenance.classify(97, 96, FULL, "2026-11-19", true, true, "other", PLAN));
        assertThrows(C1ApiException.class, () -> DemoProvenance.classify(97, 96, FULL, "2026-11-19", true, true, SOURCE, "other"));
    }

    @Test void incompleteOrUnreachableCohortsStillFailClosed() {
        assertThrows(C1ApiException.class, () -> DemoProvenance.classify(97, 95, FULL, "2026-11-19", true, true, SOURCE, PLAN));
        assertThrows(C1ApiException.class, () -> DemoProvenance.classify(97, 96, FULL, "2026-11-19", false, true, SOURCE, PLAN));
        assertThrows(C1ApiException.class, () -> DemoProvenance.classify(97, 96, FULL, "2026-11-19", true, false, SOURCE, PLAN));
        assertThrows(C1ApiException.class, () -> DemoProvenance.classify(4, 3, SMALL, "2026-11-19", true, true, SOURCE, PLAN));
    }

    @Test void localSmallCohortRequiresOwnershipAndCompleteProvenance() {
        var result = DemoProvenance.classify(3, 3, SMALL, "2026-11-19", true, true, "source", "plan");
        assertTrue(result.synthetic());
        assertEquals(DemoProvenance.NOTICE, result.notice());
    }
}

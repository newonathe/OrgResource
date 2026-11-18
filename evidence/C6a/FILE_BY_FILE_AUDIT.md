# C6a source audit

C5 baseline is `9755ef63bd4c02a7042cb48823bf759e609357cd`. C6a starts from policy `29efe86298431db16a780ae2e582b2276a699530`. The source coverage CSV records exact Git objects, review depth and specific sections. Historical phase reports supply leads and prior evidence.

## C5 patch (19 paths)

| Path | Observed flow, challenge and disposition |
|---|---|
| `docs/EXTENDED_C_RELEASE.md` | C5 manually described startup and protected login. Runtime proof required separate credentials and a real bootstrap. C6a adds staged publishing and specific schema grants. FIX. |
| `evidence/C5/PHASE_REPORT.md` | Reconciled build SHA and evidence-only branch SHA. The 44 Surefire observations and stale 43 runner banner differ. C6a fixes the runner and preserves historical evidence. KEEP. |
| `extended/backend/src/main/java/edu/orgresource/extended/rest/InventoryResource.java` | Image removal forwards path UUIDs and If-Match through Jersey and returns the updated version ETag. KEEP. |
| `extended/backend/src/main/java/edu/orgresource/extended/service/InventoryService.java` | Image removal locks current organization/member/resource, checks ETag, clears media bytes and validates availability without inserting movement. Receipt replay matches fingerprint before the current ETag. KEEP. |
| `extended/backend/src/main/java/edu/orgresource/extended/service/C3ReminderService.java` | Reminder uses related Resource name, Allocation quantity and Project due time. Name controls are stripped and the length is bounded. Existing claim and consent states still govern dispatch. KEEP. |
| `extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java` | Existing preservation, Retrofit body, migration and image checks remain. New reporting cases target actual inputs, permissions, bytes, ties and current data. EXTEND. |
| `extended/frontend/README.md` | Vite loopback target and standalone acceptance are separate from packaged delivery. KEEP. |
| `extended/frontend/e2e/orgresource.spec.ts` | C5 uses restricted and admin browser sessions, structured receipt outcomes and image hashes. Added actual activity and downloads. EXTEND. |
| `extended/frontend/playwright.config.ts` | Supplied packaged origin must be numeric loopback; disabled reuse for standalone development. KEEP. |
| `extended/frontend/src/api/extended.ts` | Receipt shapes discriminate movement and transient operation. Versioned image removal sends If-Match. C6a adds reporting methods. EXTEND. |
| `extended/frontend/src/auth/AuthScreens.tsx` | Legacy invite parameters are scrubbed by replacement; manual entry holds the secret in memory. Existing query already traversed navigation before sanitization. Bounded member list discloses continuation. KEEP. |
| `extended/frontend/src/pages/AllocationsPage.tsx` | Safe retry keeps the original action, operation identity and ETag. Receipt 404 is uncertain. Tenant switch abort and remount are material dependencies. KEEP. |
| `extended/frontend/src/pages/ProjectsPage.tsx` | OWNER/ADMIN/CUSTODIAN can fetch Allocation history. MEMBER and LEAD read permitted Project and Requirement data without calling the Allocation list. KEEP. |
| `extended/frontend/src/pages/ResourcesPage.tsx` | Receive and adjust capture target, values, operation/key and ETag. Image replacement/removal refreshes details. First 30 movements identified as partial when more exist. KEEP. |
| `extended/frontend/vite.config.ts` | Proxy preserves external host and targets a numeric loopback port. Same-origin filter still makes the authorization decision. KEEP. |
| `scripts/extended-c-backup.ps1` | Existing AES-GCM checks ciphertext before any import. C6a catches missing login path and metadata-limited dumps, publishes via file move, and labels partial imports. FIX. |
| `scripts/package-extended-c.ps1` | C5 created the final directory early. C6a publishes only after clean rebuild and verification, hashes marker metadata, and refuses unsafe tracked paths and Git symlink modes. FIX. |
| `scripts/verify-extended-c-recovery.ps1` | Second data directory/port compares all tables, FK and persisted rows, authenticates image/cipher, and replays movements. Uses fixture root. Supplement with independent restricted-login drill. KEEP. |
| `scripts/verify-extended-c1.ps1` | Starts fresh MySQL then the Java and React tests, Newman and browser checks. Removed stale test count and added reporting collection. FIX. |

## High-risk dependencies and new paths

- C1Application bootstraps in nonweb mode. C6a must verify fresh Flyway and one-time owner initialization with scoped credentials independently of the test-class fixtures.
- C1OrganizationContext, C1AccountSessionFilter, C1SameOriginFilter, C1SecurityConfiguration and C1RequestIdFilter govern scope, age, account status, CSRF/origin and safe request IDs.
- C1AuditService appends recorded SUCCESS facts in the caller's transaction. AuditEvent can contain internal actor and target identifiers. ReportingComponent uses explicit projections and both scope checks. SQL templates vary only by internal enum, with bound values.
- ReportingResource delegates using Spring injection and sets content types, no-store, nosniff and attachment metadata. No new migration or audit ledger was added.
- ReportingPages, App, AcademicShell and api/client reuse organization remount and request-abort behavior. The server separately checks staff permissions. API errors and successful files use distinct branches.
- The operator verifier uses two isolated MySQL processes, new schema-scoped users, the real bootstrap and a protected login path. It keeps one-time codes in memory for the test.
- Flyway V1–V5, both Course A source trees and the original Course A collection remain frozen. The v2 acceptance collection and CI wiring are additive.

The [source register](SOURCE_COVERAGE_REGISTER.csv) records review boundaries. Runtime results belong to the phase report.

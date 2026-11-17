# C2 Phase Report — Atomic Inventory and Ledger

**Status:** C2 implementation and acceptance gates passed; C3 not started.
**Report date:** 2026-11-18 (Asia/Manila)
**Repository:** `newonathe/OrgResource`
**Branch:** `feat/extended-c-c2-ledger-atomicity-2026-11-17`
**C1 parent:** `7c349a6a818f2aafd21598e76d3eb8b9faa9a8ef`
**Implementation SHA tested:** `aa2f30f7239316f8be11968fff4a41e665e710de`
**Draft PR:** https://github.com/newonathe/OrgResource/pull/15 (base: C1 feature branch)

This report records observed outcomes. It does not claim a merge, release, deployment, real SMS, Canvas action, or work in C3.

## Scope completed

- Closed the C1 organization-context mismatch: login and invitation redemption no longer silently select the first membership. Protected legacy operations require explicit active-organization selection. The acceptance test covers login with multiple memberships, revoked/foreign selections, explicit switching, logout/relogin, and the legacy missing-context response.
- Added Flyway V3 for public resource/allocation IDs, allocation-to-resource identity, movement history and idempotency receipts. V1 and V2 were left intact.
- V3 checks for legacy nonzero stock or physical allocation history before DDL. It aborts with `LEGACY_STOCK_HISTORY_UNRECONCILED` instead of inventing opening-balance movements. A dedicated MySQL test verifies the refusal and preserves the legacy quantity.
- Added tenant-scoped, append-only `stock_movement` records for initial stock, receipt, adjustment, issue, return and consumption. Database checks constrain quantity, movement kinds and deltas; unique keys and foreign keys enforce operation, resource, allocation and actor scope.
- Added unique idempotency receipts keyed by organization, actor, route and hashed request key; request-body fingerprints prevent key reuse with a different payload. The receipt stores the completed HTTP response and expiry. Replay, expired-key refusal, receipt lookup, restart persistence and unchanged movement count are tested.
- Added bounded organization-scoped inventory discovery and movement/receipt reads, versioned resource/allocation state, receive/adjust operations and allocation lifecycle endpoints under `/api/v2/orgs/{organizationId}`. Existing allocation lifecycle calls delegate to the ledger-backed service. Mutations require current membership authorization, `If-Match` and a random `Idempotency-Key`.
- Inventory updates remain transactional: reusable issued custody is tracked separately from physical totals, approved holds reserve once, return releases custody, and consumption reduces physical quantity once. The service rejects mismatched ledger balances and returns a sanitized integrity error instead of presenting fabricated availability.
- Restricted runtime SQL rights for stock movement and audit history to append operations, with verification that update/delete are rejected. Migration and isolated fixture work use a separate elevated connection.
- Added a Java 21 / isolated MySQL 8 GitHub Actions job. Existing Java 8 course and React jobs remain active.

## Implementation paths

- `extended/backend/src/main/resources/db/migration/V3__atomic_inventory_ledger.sql` — guarded V3 schema migration and MySQL constraints.
- `extended/backend/src/main/java/edu/orgresource/extended/service/InventoryService.java` — transactional inventory, reconciliation, authorization, receipts and replay behavior.
- `extended/backend/src/main/java/edu/orgresource/extended/rest/InventoryResource.java` and `AllocationResource.java` — versioned inventory and lifecycle HTTP routes.
- `extended/backend/src/main/java/edu/orgresource/extended/api/InventoryDtos.java` — request and response contracts.
- `extended/backend/src/main/java/edu/orgresource/extended/domain/Resource.java` and `Allocation.java`, plus their repositories — persisted IDs, versions and movement-linked state.
- `extended/backend/src/main/java/edu/orgresource/extended/identity/C1SessionService.java` and `security/C1OrganizationContext.java` — explicit organization context correction.
- `extended/backend/src/test/java/edu/orgresource/extended/C1IdentityAcceptanceTest.java` — C1 and C2 MySQL-backed acceptance suite.
- `scripts/verify-extended-c1.ps1` — isolated local MySQL, packaged JAR and restart acceptance runner.
- `.github/workflows/verify.yml` and `AGENTS.md` — independent Java 21 gate and Extended C safety rules.

The Extended C implementation continues to reuse the five Course A domain entities; movement and receipt tables are persistence ledgers, not additional JPA domain entities.

## Verification evidence

### Local Windows acceptance

- `.\scripts\verify-extended-c1.ps1` — **PASS** before the final test-only MySQL identifier-case correction. This run included frontend install, typecheck, 15 frontend tests, lint, production build, MySQL-backed Maven verify, packaged JAR/UI/API checks, and receipt/session/data persistence through restart.
- `.\scripts\verify-extended-c1.ps1 -SkipFrontend` — **PASS** after the final test-harness correction. Java 21.0.3, MySQL 8.0.46; Maven reported 27 tests, 0 failures, 0 errors, 0 skipped. The script also packaged and started the JAR, checked UI/API behavior, replayed a persisted receipt after restart, and confirmed no duplicate movement. The frontend source was unchanged by the test-only correction.
- The latest local Surefire XML at `extended/backend/target/surefire-reports/TEST-edu.orgresource.extended.C1IdentityAcceptanceTest.xml` records 27/27 passing test methods. `target/` output is local build evidence and is not committed.
- The isolated acceptance runner stopped its temporary MySQL instance after verification; it did not reset a user-owned schema.

### GitHub Actions on the tested implementation SHA

Workflow run **44149452957**, head `aa2f30f7239316f8be11968fff4a41e665e710de`:

- `Extended C Java 21 and isolated MySQL acceptance` — **PASS**, 2m34s; MySQL 8 service, Java 21 Maven clean verify.
- `Java 8 and isolated MySQL tests` — **PASS**, 2m52s; course backend, packaged Java 8 JAR smoke test and artifact upload.
- `React typecheck, tests, lint, and build` — **PASS**, 1m57s.

The initial CI attempt failed because the Maven wrapper lacked an executable bit in checkout; invoking it via `bash` fixed that. The next run exposed case-sensitive Linux MySQL handling of V2's uppercase `SPRING_SESSION` identifiers. The grant setup now preserves the metadata identifier's case and normalizes only privilege selection; raw session-table queries use the schema's actual uppercase names. The final run above passed all three jobs. No assertion was weakened.

### Acceptance test roster

All 27 methods passed on local MySQL and the final Java 21 CI run:

`ID01_offlineBootstrapTwoRacersCreateExactlyOneOwnerAndNeverPublicOrganizationCreation`; `ID02_literalJavaxAndJakartaValidationCoexistWithJerseyAutowiredJpaMysqlHttp`; `ID03_mysqlSchemaHasAllIdentityDomainAndOfficialSessionTablesAndRejectsCrossTenantForeignKeys`; `ID04_anonymousWrongRoleForeignTenantAndFrozenOrganizationBoundaries`; `ID05_preAuthCsrfLoginRotationSameOriginLogoutAndCookieProfiles`; `ID06_membershipRoleIsReadFromMysqlAndActiveOrganizationCannotBeForged`; `ID07_passwordHashGenericLoginFailuresAndBoundedThrottle`; `ID08_recoveryCodeIsHashedSingleUseAndRevokesExistingSessions`; `ID09_invitationTokenIs256BitHashedExpiresAndRedeemsNewAndExistingAccountsOnce`; `ID10_ownerAdminGrantBoundariesNoSelfPromotionAndStrongVersionPreconditions`; `ID11_transferLocksOrgAndMembersEnforcesOnePendingAndAtomicallySwapsOwners`; `ID12_lastOwnerDisableAndConcurrentTransferAcceptCannotLeaveOrganizationOwnerless`; `ID13_jdbcSessionPersistenceAndIdleAndAbsoluteExpiryAreEnforced`; `ID14_allProtectedLegacyAndV2RoutesDenyAnonymousAndNoC0FallbackUserExists`; `ID15_cleanFlywayBootstrapAndEffectiveDependencyVersionsArePresent`; `ID16_testConfigurationUsesNoLiveSmsAndDoesNotExposeSensitiveAccountData`; `ID17_utcMysqlSessionAndJpaLocalDateTimeRoundTripForYear2040`; `C1FIX01_loginRequiresExplicitOrganizationAndNeverSelectsFirstOrRevokedMembership`; `C2_PRIVILEGE01_runtimeCanAppendAuditEventsButCannotUpdateOrDeleteThem`; `C2_LEDGER01_reusableStockHasAppendOnlyMovementAndLateReturnIsReplaySafe`; `C2_LEDGER02_consumableReceiveAdjustReplayAndStaleVersionAreAtomic`; `C2_TINV03_expiredApprovedHoldIsReleasedButIssuedCustodyRemainsAtEndBoundary`; `C2_CONCURRENCY01_twoApprovalsAndTwoIssuesCannotDoubleCommitStock`; `C2_IDEMP02_failureBetweenQuantityAndMovementRollsBackEverything`; `C2_SCHEMA02_tenantForeignKeysAndResourceHistoryDeletionAreEnforcedByMysql`; `C2_AUTH02_suspendedCustodianCannotReplayPriorReceipt`; `C2_SCHEMA03_v3RefusesLegacyNonzeroStockWithoutInventedHistory`.

## Protected Course A and Git

- The Course Final A reference remains `7f8c0746da18fd54b47f430ac1435ac36e605d67`.
- `git diff --exit-code 7f8c0746da18fd54b47f430ac1435ac36e605d67 HEAD -- backend frontend docs/academic-final release submission` returned success before this report was added. No Course A source or artifact path was changed.
- The C2 branch descends from C1 SHA `7c349a6a818f2aafd21598e76d3eb8b9faa9a8ef`, not `main` or the documentation branch.
- PR #15 is open and draft against `feat/extended-c-c1-identity-tenancy-2026-11-17`; it is not merged.
- The local branch was clean and matched `origin/feat/extended-c-c2-ledger-atomicity-2026-11-17` at the tested implementation SHA before adding this report.

## Remaining risks and limits

- Upgrading a populated legacy database with nonzero stock or issued/returned/consumed history intentionally stops at V3. A separately designed and approved historical reconciliation is needed before such a database can be migrated; the migration does not synthesize history.
- `npm ci` reported 12 high-severity advisories in the existing frontend dependency tree. The C2 phase did not perform broad or potentially breaking toolchain upgrades.
- GitHub emitted maintenance notices for Node 20 action runtimes and `actions/setup-java@v4`; these did not fail the final run and are outside this C2 correction.
- The failing earlier CI runs were superseded by the final all-green workflow run above; their causes and corrective changes are recorded here.

**C2 verdict:** implementation, local MySQL acceptance, Java 21 isolated MySQL CI, Java 8 regression, and frontend gates pass on the implementation SHA. PR #15 remains for review. Stop here; do not begin C3 without separate authorization.

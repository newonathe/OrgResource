# Extended C5 phase report

November 19, 2026, Asia/Manila

**C5 VERIFIED — LOCAL RELEASE CANDIDATE AND INDEPENDENT RESTORE GATES PASS; READY FOR C6 REVIEW**

## Source and scope

- C5 starts at C4 `3c0d677e37fb4df883d8eeb075ba14a773c90c8f`, [draft PR #17](https://github.com/newonathe/OrgResource/pull/17). Parent CI completed successfully in [run 44327604951](https://github.com/newonathe/OrgResource/actions/runs/44327604951). This is prior evidence.
- The tested and packaged source is `14576b57fff44c6f0112fd37d8c237ca0122efb4`. GitHub reported that exact head on `feat/extended-c-c5-release-recovery-2026-11-18`, based on C4, and all three jobs passed. This report was updated after the package was sealed. The later report commit adds evidence only. [Draft PR #18](https://github.com/newonathe/OrgResource/pull/18) [CI run for packaged source](https://github.com/newonathe/OrgResource/actions/runs/44342137298)
- Protected Course A `7f8c0746da18fd54b47f430ac1435ac36e605d67` is an ancestor. The protected `backend/` Git tree is `f2b522f88214a9cfba908cac6b0bcda70171875c` and `frontend/` is `052e5dbc5a0e84823c7dda06ccee83f27b1818c9`. Both match this branch. Flyway V1–V5 are unchanged.
- Authority is the submitted assignment and syllabus, the original proposal, lesson transcripts V01–V10 and V12, and the approved specifications CP34–36 and CP41. CP44–45 keep the two editions distinct. CP49 is an audit list for future product decisions.

## Source changes and correction gate

| Requirement | Source and evidence |
|---|---|
| A1 project details | `ProjectsPage.tsx` fetches required details for all authorized roles. Only OWNER, ADMIN, and CUSTODIAN request allocation history. History failure has its own retry. Chromium exercised MEMBER and LEAD with real requirement rows and zero allocation calls, and ADMIN with its history pane. |
| A2 invite credentials | `AuthScreens.tsx` removes legacy query/hash token parameters using replacement, requires manual entry, and holds one-time values in memory. Connected Chromium verifies empty token input, URL cleanup, no web-storage bearer value, and recovery codes shown once. MySQL test ID09 covers existing-account and new-account redemption, expiry, replay, and concurrent redemption. |
| A3 reminder composition | `C3ReminderService.java` adds Resource name, Allocation quantity, Manila due time and return guidance. The actual Retrofit loopback form captured by `C3_LIFECYCLE01` contains the message. The proposed content is in the original proposal p. 10. Frozen Course A's due-time-focused message differs and is preserved. |
| A4 image retirement | `InventoryResource.java`, `InventoryService.java`, `extended.ts`, and `ResourcesPage.tsx` add a versioned OWNER/CUSTODIAN operation at `POST /api/v2/orgs/{orgId}/resources/{resourceId}/image/delete`. It clears bytes and media type in one transaction. MySQL/HTTP assertions cover ETag, CSRF, role, tenant, missing image, and eligible R04 after removal. Chromium verifies original uploaded bytes, removal, 404, and retained movement rows. |
| A5 development mode | `playwright.config.ts` uses port 5173 for its Vite server. `vite.config.ts` keeps the browser origin through the proxy and accepts the verifier's numeric backend port. The verifier starts actual Vite and runs the authenticated Allocation navigation test. |
| A6 stable replay and bounded history | `ResourcesPage.tsx` and `AllocationsPage.tsx` retain the original target, operation identity and ETag. Receipt display distinguishes permanent movements from expiring transitions. A 404 leaves completion uncertain. Chromium covers lost response after commit and a receipt 404 before delayed upstream completion, followed by same-identity replay and a single movement. Member and movement lists mark partial results. The browser library still uses the legacy complete resource list. MySQL cases exercise movement, Resource, and member cursors with more rows than the configured limit. |
| B/C local delivery | `package-extended-c.ps1` exports the selected tracked paths and builds from clean extraction. `verify-extended-c1.ps1` and `verify-extended-c-recovery.ps1` run the source and independent target instances, checksums, restore, permissions and browser checks. `extended-c-backup.ps1` handles bounded raw MySQL bytes and authenticated AES-GCM. `docs/EXTENDED_C_RELEASE.md` and `extended/frontend/README.md` document the operator path. |

## Academic runtime cross-check

The assignment p. 1 and syllabus pp. 1–4 preserve the Java project, Spring Boot/Jersey controller and component pattern, GET/POST operations, JSON, Javax validation, JPA/JPQL/MySQL and Retrofit/Gson integration with scheduling. Course A retains Java 8 and Jersey 2. Extended C uses the separately approved C0 Java 21/Jersey 4 edition and executes literal Javax validation alongside its Jakarta runtime. SQL migrations, credential/session controls and ledger records are Extended support tables. The five academic business families remain Department, Project, Resource, ResourceRequirement, and Allocation. Course lesson sources include Jersey V01–02, validation V03, persistence V05–07, Retrofit V08, and HTTP image server V10.

| Course IDs | Extended C observed runtime check | Frozen Course A comparison |
|---|---|---|
| D01–D04 | Authenticated Newman create, read, update, delete | Same route family in A3. |
| P01–P04 | Create, department-filtered read, update, delete | Same route family and local business time. |
| R01–R05 | Create, filtered read, update and stale refusal, eligible delete and nonempty refusal, availability | Same course operations. Extended computes availability with its durable ledger and has an extra image-removal operation. |
| Q01–Q04 | Create, filtered read, update, eligible delete and dependency refusal | Same course operations and coverage invariant. |
| A01–A08 | Create, status and requirement filters, approve, issue, return, consume, cancel, eligible delete and history refusal | Same course operations. Extended adds role checks, stock movements, and versioned modern actions. |
| A09 | Revoke consent and clear stored contact through the approved amendment | Implemented in A3. Extended adds tenant-bound encrypted contact storage. |
| IMAGE-01 and IMAGE-02 | Multipart PNG upload and exact persisted GET, plus JPEG/PNG MySQL acceptance and restart checks | Implemented in A3 on the Resource. No new academic business entity. |

The collection ran 48 requests and 90 assertions with zero failures for the 28 original/amended operations. The route-by-route contract is in the [exercised Postman collection](../../postman/OrgResource-Extended-C3-28-Operations.postman_collection.json). Instructor interpretation of the syllabus p. 4 CLO9/CLO10 mapping and the current Canvas checklist remain student actions. See the [Course A technical evidence](../COURSE-A/A3_TECHNICAL_GATE.md).

## Operational review

- Default bind is `127.0.0.1`. Outside `c1-test` the session is a secure, HTTP-only, SameSite cookie. Cross-origin writes are rejected. Browser writes need CSRF. Session checks enforce 30-minute inactivity, 12-hour absolute age, and current account/membership status. Tenant selection rotates credentials without counting as password reauthentication.
- Account throttling and password or recovery-code rotation are covered by the integration suite. Recovery codes and invitation bearers are hashed. Project LEAD visibility is limited by the current role policy. Assignment-specific reads await an approved ownership model.
- The runtime account has SELECT/INSERT only on immutable movement and audit tables. Flyway uses a distinct migration identity. Background jobs are bounded; interrupted `DISPATCHING` attempts become UNKNOWN and are not replayed automatically. SIMULATED and PROVIDER_ACCEPTED remain distinct from delivery.
- Images are bounded, validated PNG/JPEG, database-managed and served without cache. Stored contact ciphertext is tenant/allocation-bound. The archive key and the contact key have distinct purposes. Bootstrapping displays recovery codes once on the private operator console. Do not retain that console output as general run evidence.
- Fresh `npm audit --json` returned 12 high development records rooted in one Picomatch advisory. `npm audit --omit=dev --json` returned zero findings. Affected records are picomatch, fdir, tinyglobby, vite, vitest, @vitejs/plugin-react, typescript-eslint, and its parser, plugin, utils, type-utils and typescript-estree packages. The registry advertised 4.0.7 as latest, still in the vulnerable range. The non-forced audit fix dry run retained the 12 records. npm suggested a semver-major Vitest downgrade, which was not applied. Separate owner review and compatible remediation remain public release gates. [Picomatch GHSA-fc5q-39xc-vjpf](https://github.com/advisories/GHSA-fc5q-39xc-vjpf)

## Remaining P14 decisions

- Complete modern tenant-scoped CRUD is a deferred Extended product requirement. Identity/governance, Department v2, core stock actions, history and image removal exist. Some UI CRUD uses authorized academic API operations. Borrower-account linkage and LEAD project assignments require explicit identity and ownership contracts. Free-text borrower labels cannot prove accounts, and current LEAD reads are broader than an assignment-scoped design.
- Activity/audit browsing, exports, and traversal of bounded history remain deferred product affordances. Physical image retirement is implemented; a separate archival policy has yet to be approved.
- Hosting and a genuine independent off-host recovery are later owner decisions. Live Twilio and the current academic rubric require separate authorization or student confirmation. No public deployment, live SMS, instructor contact, Canvas submission, or C6 work is represented here.

## Executed verification

The final local release command was `pwsh -NoProfile -File .\scripts\package-extended-c.ps1 -Destination <new external release directory>`. It ran `verify-extended-c1.ps1 -VerifyRecovery` from a clean extraction of the Git archive. The executable JAR shipped in the release is copied directly from that tested extracted build.

| Command or test gate | Observed result |
|---|---|
| `npm ci`, `npm run typecheck`, `npm test`, `npm run lint`, `npm run build` | PASS. 24 frontend unit tests across three files. |
| `backend\mvnw.cmd -B -f extended/backend/pom.xml clean verify` | PASS. Java 21.0.3 with MySQL 8.0.46, 44 tests, zero failures/errors/skips. The runner's legacy summary banner still says 43; the Surefire aggregate in the saved log is 44. Includes tenant boundaries, grants, transaction rollback and concurrent inventory/replay. |
| `run-extended-c3-newman.ps1` against the packaged app | PASS. 48 requests and 90 assertions over all 28 academic operations. |
| `npm run e2e` with packaged origin | PASS. 11 Chromium journeys, including bounded role access, safe invitation handling, image changes, reduced motion and responsive screenshot capture. The desktop-light Projects and mobile-dark Resources screenshots were spot-checked. |
| `npm run e2e -- --grep 'Allocation status filters'` | PASS at standalone Vite 5173 against the real Java/MySQL API. |
| Fresh migration, safe upgrade and legacy refusals | PASS. Clean V1–V5 and the supported zero-stock V2–V5 upgrade preserved numeric identity and created no fictitious movement. The existing V3 nonzero historical stock and V4 legacy plaintext-contact refusals are intentional. Their preflight checks left legacy rows intact. |
| Protected Course A and Extended on GitHub CI | PASS at the packaged source. Java 8/MySQL executed 61 tests with zero failures/errors/skips and the bundled JAR smoke test. The frozen frontend job passed. Extended CI repeated 44 Java tests, Newman and 11 browser journeys. [GitHub run](https://github.com/newonathe/OrgResource/actions/runs/44342137298) |

Earlier checks found a TypeScript fixture error, an unsupported `mysqldump` option and an incorrect 201 expectation for durable replay. The corrected fixture, supported dump argument set and HTTP 200 permanent-receipt assertion passed the final extraction gate. Stock total, available stock, receipt identity and no-duplication checks remained required.

## Backup and restored runtime

The verified source and target each ran on their own fresh MySQL 8.0.46 process, port and data directory on this workstation. Restore authenticated the encrypted archive before sending SQL. A flipped ciphertext byte, a wrong backup key and an absent backup key were each refused with the target still empty. The verified fixture archive was 4,372,434 bytes, SHA-256 `93562ce55c090b8560919bed3439a344b5b935e5b10f5c227caad662ebb60775`. Its ephemeral test key is outside the release.

- The restore checked all 18 tables, the foreign-key definitions and data checksums for non-session tables. Flyway validated existing V1–V5 before the JAR served traffic. Runtime grants were recreated with append-only movement and audit rights.
- The verified lost-session policy omits session data from the snapshot. The pre-backup cookie received 401. Fresh sign-in succeeded. Active MEMBER and suspended membership checks remained enforced, including foreign-workspace denial.
- The restored stock fixture retained total and availability of 12. Replay of a pruned durable operation returned its original movement identity and left the one-unit fixture and movement count unchanged. The PNG SHA-256 matched after restore.
- Restored reminder outcomes and contact envelopes matched the source. A stored simulated reminder contact authenticated with the original contact key and was refused with an unrelated key. The MySQL integration suite separately covers the unavailable-key outcome. Recovery ran with external reminders disabled.
- A fresh authenticated Chromium session opened the restored 2040 Project and verified the packaged assets and five API lists. The drill stopped both MySQL processes and Java on exit. **Off-host recovery — NOT RUN.**

## Release artifacts

The local package is `deliverables/OrgResource-Extended-C5-2026-11-19-14576b5` in the course workspace. The exact-commit source ZIP carried 195 entries, including directories. Python `zipfile.testzip()` returned no CRC failures for source ZIP or JAR. JAR inspection confirmed React and all five migrations. A second `Get-FileHash -Algorithm SHA256` check matched all manifest entries.

| File | Bytes | SHA-256 |
|---|---:|---|
| `OrgResource-Extended-C5-Source.zip` | 331,828 | `d382e1b9783230d82391c81aa3142734cc1c81a199f878824aef2c95112db9df` |
| `OrgResource-Extended-C5-Java21.jar` | 72,298,848 | `3471c98821f22ed28577a8fd29a96015b6982bf5a1a63b5b8ce2cdbf7914faaa` |
| `VERIFICATION.log` | 35,220 | `214bd638b6bca251ca0851db2a8bacf4cb5503e2cc82d3f42e37c4d434d2767e` |
| `RELEASE-MANIFEST.json` | See manifest file | `140fadeb0de56d65e321776b658aa3b2154ec2e8e09e75135666bce0f12eeb09` |

The package uses Java 21.0.3, MySQL 8.0.46, Node 24.14.1, npm 11.11.0 and PowerShell 7.6.9 on Windows. Use this exact folder for local release review. Review the release guide, this evidence and draft PR before authorizing C6. The student should separately verify current Canvas instructions, course wording, authorship disclosure and delivery obligations using the frozen Course A materials.

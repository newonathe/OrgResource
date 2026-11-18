# Extended C5 phase report

## Source and scope

- C5 starts at C4 `3c0d677e37fb4df883d8eeb075ba14a773c90c8f`, [draft PR #17](https://github.com/newonathe/OrgResource/pull/17). Parent CI completed successfully in [run 44327604951](https://github.com/newonathe/OrgResource/actions/runs/44327604951). This is prior evidence.
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
- Fresh `npm audit --json` returned 12 high development records rooted in [Picomatch GHSA-fc5q-39xc-vjpf](https://github.com/advisories/GHSA-fc5q-39xc-vjpf). `npm audit --omit=dev --json` returned zero findings. Affected records are picomatch, fdir, tinyglobby, vite, vitest, @vitejs/plugin-react, typescript-eslint, and its parser, plugin, utils, type-utils and typescript-estree packages. No forced upgrades. Build tooling remains local; compatible remediation or owner review is required before public release.

## Remaining P14 decisions

- Complete modern tenant-scoped CRUD is a deferred Extended product requirement. Identity/governance, Department v2, core stock actions, history and image removal exist. Some UI CRUD uses authorized academic API operations. Borrower-account linkage and LEAD project assignments require explicit identity and ownership contracts. Free-text borrower labels cannot prove accounts, and current LEAD reads are broader than an assignment-scoped design.
- Activity/audit browsing, exports, and traversal of bounded history remain deferred product affordances. Physical image retirement is implemented; a separate archival policy has yet to be approved.
- Hosting and a genuine independent off-host recovery are later owner decisions. Live Twilio and the current academic rubric require separate authorization or student confirmation. No public deployment, live SMS, instructor contact, Canvas submission, or C6 work is represented here.

## Run status

The initial C5-A run passed frontend typecheck, lint, build and 24 units, 43 Java 21/MySQL tests, Newman 48/90, 10 packaged Chromium journeys and one standalone Vite journey. The later recovery-enabled run passed 11 packaged Chromium journeys, plus the standalone Vite test. It stopped at an unsupported `mysqldump` option before altering a restore database. The option was removed, and a standalone encrypted backup against that isolated source succeeded. The final clean-extraction run, CI, restore result and release hashes will be recorded against their actual source and evidence revisions.

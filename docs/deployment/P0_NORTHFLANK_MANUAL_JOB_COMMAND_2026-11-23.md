# P0 manual Northflank demo import — operator-only release checklist

**Status (2026-11-23 PHT): prepared commands, NO Northflank execution.**
**User priority:** fill the **existing** Northflank OrgResource instance with the **exact previously approved** 96 directory labels and simulated data, while retaining the old owner and old organization. **Code branch:** `feat/extended-c-c8-domain-workflows-2026-11-19` / draft PR #25, **DO NOT merge or release without owner approval and live proof**. **P0 issue:** [#26](https://github.com/newonathe/OrgResource/issues/26). Frozen Course A untouched.

## P0-SEC: Northflank October 30, 2026 secrets incident — verify rotation before hosting actions

**VERIFIED EXTERNAL FACT (source published November 17, 2026; checked November 23):** [Northflank's incident report](https://northflank.com/blog/incident-report-october-30-security-incident) says a previously unknown API flaw led to remote code execution and exfiltration of **a subset of stored customer secrets on October 30**. Its investigation estimated 1.5% of customer teams and 1.8% of stored secret values exfiltrated. Northflank reports **no evidence of customer workload-data or customer personal-data exfiltration** and says the exploit was fixed October 30. It explicitly advised **all customers to rotate every stored secret**, beginning with cloud, API and database credentials; it revoked Northflank API tokens and expanded addon rotation tooling and audit logs. The [Northflank public incident status](https://status.northflank.com/history/1) marks it resolved November 17. **No claim that the specific OrgResource tenant was affected or unaffected**.

**GO/NO-GO — owner-operated security review (NOT VERIFIED in this ChatGPT session):**

- [ ] Inspect the OrgResource Northflank project/team's private security notices, all stored secrets and inherited/global secret groups, linked addons, audit logs and current environment settings. Review logins, DB privileges, activity/billing and historical access since October 30 without dumping secrets or private logs into public GitHub/ChatGPT.
- [ ] Rotate the actual Northflank-managed MySQL addon/DB runtime and migrator credentials **using platform-supported rotation**, update every dependent service/job connection, and verify successful login with the **new** credentials. Arrange a controlled change window and backup before rotation to avoid accidentally taking the existing site offline.
- [ ] Revoke/replace any deployment, CLI, source-control, external-service, email/SMS, storage and other tokens previously held as Northflank secrets, as applicable; prefer shortest-lived scoped tokens. Check for inappropriate use and configure spending/usage restrictions where relevant. Never replace a source-control token unless it actually exists in the project.
- [ ] Confirm jobs and the existing website still work after rotation, no real SMS is enabled, and audit events and backup/restore capabilities are accessible under new permissions. Record **redacted** timestamps, responsible operator and confirmation, not actual credentials.
- [ ] Do not create new high-privilege import-job secrets or upload the approved private plan into the Northflank environment until the owner has completed this review and confirmed the security posture.

This incident review is independent from code CI, read-only database postflight, backup/restore, full 96-plan rehearsals, academic compliance and the separate public-label-rights decision. **BLOCK LIVE IMPORT pending completion.**

## P0-00: Full-plan file exceeds Northflank's documented secret-file limit

**VERIFIED EXTERNAL FACT (2026-04-27 Northflank changelog; recheck account limits at execution):** Northflank's published limit for **one secret file is 3,500 KiB** and its published API request-body limit is 10 MB ([first-party changelog](https://northflank.com/changelog/march-and-april-2026)). The exact approved plan is **19,265,850 bytes** (about 18.4 MiB). It **cannot** be submitted unchanged as one Northflank secret file or API-embedded `runtimeFiles` payload under these published limits. Never include the plan in a public GitHub commit, container image, build argument, GitHub Actions artifact, plaintext environment variable or log.

**VERIFIED EXTERNAL FACT:** Northflank documents private file transfer into **running services or jobs** using the CLI/JS client: [Copy files](https://northflank.com/docs/v1/api/copy-files) and [Transfer data to/from containers](https://northflank.com/docs/v1/application/run/transfer-data-to-and-from-containers). The CLI's documented job equivalent is `northflank upload job file`; inspect `northflank upload job file --help` for the actual version/target flags instead of assuming service flags work for jobs. This does not prove the user's Northflank account, job shell or ephemeral storage has the necessary access.

**DESIGN RECOMMENDATION — safe staged transfer:**

1. In an approved **isolated** operator job, start a non-mutating standby process. Do not configure its startup command to import automatically. Check runtime access and file permissions, quota and costs.
2. From the trusted operator's private computer, use authenticated Northflank CLI job-file transfer (or an equivalently private, expiring encrypted object download) to place the exact approved plan on the **temporary job filesystem**, outside source control or shared web containers. Ensure restrictive access (`umask 077`, owner-only readable), no public HTTP upload endpoint.
3. In the job, compare full file size **19,265,850 bytes** and SHA-256 **`3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`**. Check the local approved roster SHA on the operator's computer. Only after private file verification and independently restored DB clone, set `EXT_C8_DEMO_PLAN` to the verified file's absolute path and run the **read-only preflight**.
4. Finish the independent restore/security/capacity/operator approval gates **before** manually running the write command in that same controlled environment. Remove the temporary plan and revoke temporary credentials afterward. Avoid persisting the job's unencrypted file on an enduring shared volume.
5. If the selected Northflank job cannot be held safely between upload and manual execution, **STOP** and design a different non-mutating, privately secured staging mechanism. Do not use the secret-file workaround or start an unguarded auto-import job.

No staged transfer, job control, backup, restore, or live import has been performed in this chat.

## 1. Before proceeding: hard gates

- [ ] **Private source actually present:** original roster SHA-256 `57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2`; full canonical plan SHA-256 `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`; 19,265,850 historical plan bytes. SHA check in a private operator job. No guesses, public reconstitution, real personal data or live contacts.
- [ ] **Source/CI:** all three CI jobs green on the **exact full importer+UI code SHA**; reviewed Java 21 packaged JAR and React bundle; full plan import load-tested on a disposable MySQL 8 clone with compatible RAM/CPU, stock and audit postconditions; independent reviewer signs off.
- [ ] **Actual host:** Northflank running image digest/code SHA, current owner, original organization, MySQL **8.0** version, schema and migration V7, runtime vs migration SQL privileges, private connectivity and TLS checked. Actual limits and any cost documented. No paid upgrade without owner approval.
- [ ] **Safe restore:** Northflank completed timestamped native backup ID, off-host encrypted dump and backup key in owner control, **independent isolated restore** of original schema, login and data checksum proof. Recording a backup job without testing restore is insufficient. Do not restore over a populated running addon.
- [ ] **Compatibility before insert:** deployed web app MUST support mixed *original plus 96 tagged demo* classification; an older running image would return 503 after import. Verify compatible build in a clone, then deploy only under an authorized controlled release. Rollback plans must include a matching application+database pair. **Never import while the old web version is still running.**
- [ ] **Maintenance gate:** live website enters a controlled maintenance window or otherwise denies requests while 96 per-workspace transactions run. The importer has **no automatic resume**; a partial failure means stop and recover from a verified backup/clone under a reviewed plan. No second attempt against partial database.
- [ ] **No live SMS:** `EXT_SMS_ENABLED=false`, `EXT_REMINDER_MODE=DISABLED` and no `SPRING_PROFILES_ACTIVE` in the one-shot job.

## 2. Prepare a sealed manual job (the owner/operator, never public CI)

Northflank supports [manual jobs](https://northflank.com/docs/v1/application/run/run-an-image-once-or-on-a-schedule), [private job-file transfers](https://northflank.com/docs/v1/api/copy-files), small [secrets/variables](https://northflank.com/docs/v1/application/secure/inject-secrets), and [native DB backup or isolated fork](https://northflank.com/docs/v1/application/databases-and-persistence/backup-restore-and-import-data). **The full 19.3 MB approved plan exceeds Northflank's documented single-secret-file limit; use an approved private file-transfer workflow, not a secret-file payload.** These are platform capabilities; none proves this account has a verified backup, storage quota or access.

Create an owner-authorized manual **standby job with no automatic import trigger**, using an audited pinned image. Arrange the private upload of the approved JSON using the supported Northflank job-file transfer and verify it at an **absolute ephemeral path** (for example, `/tmp/orgresource-private/full-plan.json`; example only, not an existing path). The separate trusted operator then manually invokes read-only preflight and—only after all gates—manual import inside the controlled job. Do **not** copy the web service bootstrap arguments, expose an import HTTP endpoint, or commit/log the plan or credentials. Avoid putting the 19.3 MB JSON in Northflank's small secret file or API request payload.

Required operator-supplied environment variables (values remain private and real):
```text
EXT_C8_DEMO_PLAN                         [absolute TEMPORARY operator-transferred file path to SHA-verified full plan]
EXT_C8_HOSTED_EXPECTED_SCHEMA            [existing OrgResource MySQL schema]
EXT_C8_HOSTED_OWNER_PUBLIC_ID            [existing active OWNER account UUID]
EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID     [existing original organization UUID]
EXT_DB_URL                               [MySQL JDBC URL; no embedded username/password]
EXT_DB_USER                              [existing schema-scoped operational identity]
EXT_DB_PASSWORD                          [secret, never in scripts or repository]
EXT_SMS_ENABLED                          false
EXT_REMINDER_MODE                        DISABLED
# Do not set SPRING_PROFILES_ACTIVE
```

Check the **real image's** packaged JAR path, Java 21 and private MySQL connectivity. Example below uses `$JAR` symbolically; it is **not** the confirmed Northflank path.

**Read-only first**, in a trusted operator session:
```sh
test "$(wc -c < "$EXT_C8_DEMO_PLAN" | tr -d ' ')" = "19265850"
test "$(sha256sum "$EXT_C8_DEMO_PLAN" | cut -d ' ' -f1)" = "3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c"
java -jar "$JAR" --c8-hosted-demo-preflight
```
A genuine passing run prints `HOSTED_DEMO_PREFLIGHT_READ_ONLY_OK`, the 64-hex-character `target_fingerprint_sha256`, existing organization count and `database_writes=0`. Keep the fingerprint private but record its digest alongside the approved backup and restore receipt. **It makes no changes.**

For **manual write only**, after independent restore and validation, additionally supply:
```text
EXT_C8_HOSTED_EXPECTED_FINGERPRINT      [exact read-only preflight target_fingerprint_sha256]
EXT_C8_HOSTED_CONFIRM                   IMPORT 96 INTO [exact fingerprint]  (replace bracketed text)
EXT_C8_HOSTED_BACKUP_ID                 [actual finished native-backup identifier]
EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256   [SHA-256 digest of reviewed independent restore evidence]
EXT_C8_HOSTED_RESTORE_ATTESTATION       I VERIFIED ISOLATED RESTORE
```
Then, **only during the confirmed maintenance window and with owner authorization**, invoke:
```sh
java -jar "$JAR" --c8-hosted-demo-import
```

**Critical distinction:** The backup ID, restore-evidence digest and phrase above are *operator attestations*. The Java process validates their presence/format, the exact approved plan, connected database fingerprint, existing OWNER, V7, 8.0 and zero existing demo cohort. It **cannot independently verify Northflank's backups or actual restore**: that has to be done and reviewed outside the app. These settings are never permission to skip that work. A failed or partial import must trigger a STOP and reviewed backup recovery, **not an automatic rerun**.

## 3. Independent read-only postflight before declaring the demo complete

**SOURCE IMPLEMENTED, CURRENT CI PENDING as of this update.** After the manual importer reports success, preserve the exact private plan in the trusted temporary job until the independent verifier has run. Execute the following **separate read-only command** in an authenticated operator session, ideally with a SELECT-only MySQL principal, against the exact same schema and server fingerprint validated before import:

```sh
# EXT_C8_DEMO_PLAN, EXT_DB_URL, EXT_DB_USER, EXT_DB_PASSWORD,
# EXT_C8_HOSTED_EXPECTED_SCHEMA, EXT_C8_HOSTED_EXPECTED_FINGERPRINT,
# EXT_C8_HOSTED_OWNER_PUBLIC_ID and EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID
# must all refer to the approved plan and the same target.
java -jar "$JAR" --c8-hosted-demo-postflight
```

This direct JDBC command starts **no Spring web process, scheduler, Flyway or data-writing service** and enforces the full plan SHA `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`. It independently compares all 96 organization IDs, exact directory labels/reference keys and provenance; counts for departments/projects/resources/requirements/allocations/memberships/audits per workspace; public IDs for Projects, Resources, Requirements, Allocations and simulated persona memberships; all final states, action distributions and simulated event months; original active credentialed OWNER, exactly one active OWNER per imported org, simulated audit actor; absence of synthetic SMS contact/consent; physical inventory and Requirement coverage. It outputs sanitized totals only, e.g. `HOSTED_DEMO_POSTFLIGHT_READ_ONLY_OK` upon passing. A failed result keeps the live demo in **NOT ACCEPTED / maintenance**, never blindly rerun the importer.

**Limits:** Postflight does not independently prove that original pre-existing record values were unchanged, that a Northflank native/off-host backup can be restored, that live browser sessions and tenant boundaries work, or that the exact directory labels can lawfully be publicly displayed. Compare separately captured *pre-import* original account/org/domain row hashes, real login, privacy rights, backup/restore receipt and browsers. CI's small/intermediate postflight fixture is **not** a live 96-plan or full-scale performance test.

## 4. Independent post-write checks

The hosted webpage is only considered populated after independent read-only postflight **and** all of these are observed against the **actual deployed instance**, with dated/redacted evidence and source SHA:

1. Original owner still signs in and can access the **unchanged original** organization. Compare original owner, credentials, membership, original per-table counts/data hashes against the **pre-import baseline**, not only the total row counts.
2. Exactly 96 additional tagged organization IDs and exactly the approved 96 directory labels (72 COA-M + 24 LIONS); original untagged organizations remain. Searchable picker should find all, select another workspace and return without cross-tenant records.
3. Imported planned counts: 248 departments, 713 projects, 1,312 resources, 2,139 requirements, 462 synthetic planned personas, 21,060 allocations and 67,370 simulated business events. Additional *owner access* membership rows and the synthetic historical actor account are implementation details, **not** additional planned personas. Validate six final allocation statuses and every month from December 2025 to November 2026 in source history.
4. SQL cross-check no SMS consent/phone ciphertext, inventory movements, total/issued/hold quantities, requirement coverage, immutable audit history and tenant isolation. Check expected 96 OWNER memberships for existing real account and **zero** `C8_DEMO_%` historical audit events attributed to that account.
5. Browser/API checks at the actual Northflank URL: backend status, synthetic notice, original workspace, 96 demo workspaces, main resource and allocation screens, search, exports, motion preference in Account Settings, responsive mobile, 401/403 denial for unauthorized requests, success for permitted owner actions. Record failed cases honestly.
6. Verify recovery options remain available, inspect resource cost and logs without printing secrets, and remove sealed plan from the manual job environment when done.

Do not equate CI success, a read-only preflight, a private restored clone or a code commit with `LIVE_IMPORTED`. No merge, hosting mutation, spending or real SMS has been performed by this ChatGPT session.

**Independent references consulted 2026-11-23:** [Northflank secret-file limit (changelog 2026-04-27)](https://northflank.com/changelog/march-and-april-2026), [Northflank authenticated job file copy](https://northflank.com/docs/v1/api/copy-files), [Northflank manual jobs](https://northflank.com/docs/v1/application/run/run-an-image-once-or-on-a-schedule), [Northflank database backups and restores](https://northflank.com/docs/v1/application/databases-and-persistence/backup-restore-and-import-data), [MySQL version options](https://northflank.com/docs/v1/application/databases-and-persistence/deploy-a-database).

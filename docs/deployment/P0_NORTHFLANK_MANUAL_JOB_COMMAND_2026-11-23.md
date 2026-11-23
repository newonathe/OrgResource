# P0 manual Northflank demo import — operator-only release checklist

**Status (2026-11-23 PHT): prepared commands, NO Northflank execution.**
**User priority:** fill the **existing** Northflank OrgResource instance with the **exact previously approved** 96 directory labels and simulated data, while retaining the old owner and old organization. **Code branch:** `feat/extended-c-c8-domain-workflows-2026-11-19` / draft PR #25, **DO NOT merge or release without owner approval and live proof**. **P0 issue:** [#26](https://github.com/newonathe/OrgResource/issues/26). Frozen Course A untouched.

## 1. Before proceeding: hard gates

- [ ] **Private source actually present:** original roster SHA-256 `57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2`; full canonical plan SHA-256 `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`; 19,265,850 historical plan bytes. SHA check in a private operator job. No guesses, public reconstitution, real personal data or live contacts.
- [ ] **Source/CI:** all three CI jobs green on the **exact full importer+UI code SHA**; reviewed Java 21 packaged JAR and React bundle; full plan import load-tested on a disposable MySQL 8 clone with compatible RAM/CPU, stock and audit postconditions; independent reviewer signs off.
- [ ] **Actual host:** Northflank running image digest/code SHA, current owner, original organization, MySQL **8.0** version, schema and migration V7, runtime vs migration SQL privileges, private connectivity and TLS checked. Actual limits and any cost documented. No paid upgrade without owner approval.
- [ ] **Safe restore:** Northflank completed timestamped native backup ID, off-host encrypted dump and backup key in owner control, **independent isolated restore** of original schema, login and data checksum proof. Recording a backup job without testing restore is insufficient. Do not restore over a populated running addon.
- [ ] **Compatibility before insert:** deployed web app MUST support mixed *original plus 96 tagged demo* classification; an older running image would return 503 after import. Verify compatible build in a clone, then deploy only under an authorized controlled release. Rollback plans must include a matching application+database pair. **Never import while the old web version is still running.**
- [ ] **Maintenance gate:** live website enters a controlled maintenance window or otherwise denies requests while 96 per-workspace transactions run. The importer has **no automatic resume**; a partial failure means stop and recover from a verified backup/clone under a reviewed plan. No second attempt against partial database.
- [ ] **No live SMS:** `EXT_SMS_ENABLED=false`, `EXT_REMINDER_MODE=DISABLED` and no `SPRING_PROFILES_ACTIVE` in the one-shot job.

## 2. Prepare a sealed manual job (the owner/operator, never public CI)

Northflank supports [manual jobs](https://northflank.com/docs/v1/application/run/run-an-image-once-or-on-a-schedule), [runtime encrypted secret files and variables](https://northflank.com/docs/v1/application/secure/inject-secrets), and [native DB backup or isolated fork](https://northflank.com/docs/v1/application/databases-and-persistence/backup-restore-and-import-data). These are platform capabilities; they are **not** evidence that this user's instance has a verified backup.

Create a one-shot manual job using an **audited image built from the selected PR SHA**, with the **same private schema/MySQL** as the intended existing deployment only after gates. Do **not** copy the web service's bootstrap arguments; never expose an import HTTP endpoint. Mount the private approved full-plan JSON at an absolute secret-file path, e.g. `/run/private/orgresource-c8-full-plan.json`; the example name is not an existing Northflank path. Never commit or log the plan or credentials.

Required operator-supplied environment variables (values remain private and real):
```text
EXT_C8_DEMO_PLAN                         [absolute secret file path to SHA-verified full plan]
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
sha256sum "$EXT_C8_DEMO_PLAN"  # compare in memory with exact 3b0f54da... digest
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

## 3. Independent post-write checks

The hosted webpage is only considered populated after all of these are observed against the **actual deployed instance**, with dated/redacted evidence and source SHA:

1. Original owner still signs in and can access the **unchanged original** organization. Compare original owner, credentials, membership, original per-table counts/data hashes against the **pre-import baseline**, not only the total row counts.
2. Exactly 96 additional tagged organization IDs and exactly the approved 96 directory labels (72 COA-M + 24 LIONS); original untagged organizations remain. Searchable picker should find all, select another workspace and return without cross-tenant records.
3. Imported planned counts: 248 departments, 713 projects, 1,312 resources, 2,139 requirements, 462 synthetic planned personas, 21,060 allocations and 67,370 simulated business events. Additional *owner access* membership rows and the synthetic historical actor account are implementation details, **not** additional planned personas. Validate six final allocation statuses and every month from December 2025 to November 2026 in source history.
4. SQL cross-check no SMS consent/phone ciphertext, inventory movements, total/issued/hold quantities, requirement coverage, immutable audit history and tenant isolation. Check expected 96 OWNER memberships for existing real account and **zero** `C8_DEMO_%` historical audit events attributed to that account.
5. Browser/API checks at the actual Northflank URL: backend status, synthetic notice, original workspace, 96 demo workspaces, main resource and allocation screens, search, exports, motion preference in Account Settings, responsive mobile, 401/403 denial for unauthorized requests, success for permitted owner actions. Record failed cases honestly.
6. Verify recovery options remain available, inspect resource cost and logs without printing secrets, and remove sealed plan from the manual job environment when done.

Do not equate CI success, a read-only preflight, a private restored clone or a code commit with `LIVE_IMPORTED`. No merge, hosting mutation, spending or real SMS has been performed by this ChatGPT session.

**Independent references consulted 2026-11-23:** [Northflank manual jobs](https://northflank.com/docs/v1/application/run/run-an-image-once-or-on-a-schedule), [Northflank database backups and restores](https://northflank.com/docs/v1/application/databases-and-persistence/backup-restore-and-import-data), [MySQL version options](https://northflank.com/docs/v1/application/databases-and-persistence/deploy-a-database).

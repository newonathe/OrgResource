# Extended C5 local release and recovery

Extended C runs on Java 21, Jersey, Spring Boot, and MySQL 8.0.46. Its bundled React UI and JSON API share the same loopback origin. Course Final A remains the Java 8 submission at source revision `7f8c0746da18fd54b47f430ac1435ac36e605d67`. Use the Course A artifacts for the academic submission.

## Verify and package

Use JDK 21, Node 24, MySQL 8.0.46, PowerShell 7.4 or newer, Git, and command-line Playwright Chromium. Set `MYSQL_HOME` to the MySQL installation folder if it differs from the Windows default. If Chromium is missing, run `npm ci` and `npx playwright install chromium` in `extended/frontend`. The runner allocates free loopback ports and creates fresh MySQL directories with fictional fixtures.

```powershell
pwsh -NoProfile -File .\scripts\verify-extended-c1.ps1 -VerifyRecovery
pwsh -NoProfile -File .\scripts\package-extended-c.ps1 -Destination 'C:\local-releases\OrgResource-C5'
```

The packaging command requires a clean commit and a new destination outside the checkout. It checks the protected Course A Git trees, archives tracked Extended C sources, checks extraction against that file list, and builds and runs the acceptance and restore gates from the extraction. The tested executable, source ZIP, run log, release manifest, and SHA-256 list are in the destination on success. The manifest identifies the commit. Byte-for-byte deterministic JAR output across independent builds is unverified.

## First run on a fresh local schema

For a fully scripted fictional demonstration and real tests, use the verifier above. For a persistent local workspace, use a newly created empty `orgresource_extended_local` schema with UTF-8 `utf8mb4` and `utf8mb4_0900_ai_ci`. Confirm the MySQL host and schema before granting permissions. Provision separate migration and runtime accounts at loopback with passwords from a protected password manager.

Give the migration identity schema-scoped DDL and DML privileges needed by Flyway V1–V5, including routine creation and removal for the V3 preflight. The bootstrap pass can use that elevated identity as its runtime identity for this new schema. After Flyway creates the tables, assign the ordinary runtime identity `SELECT, INSERT` on `stock_movement` and `audit_event`, `SELECT` on `flyway_schema_history`, and `SELECT, INSERT, UPDATE, DELETE` on the remaining application tables. Grant only on the Extended schema tables. Test first with the guarded runner, which exercises the append-only grants.

Supply the following settings through a private process environment using the chosen local accounts and ports.

- `EXT_DB_URL`, `EXT_DB_USER`, and `EXT_DB_PASSWORD` configure the runtime connection. A local URL has the form `jdbc:mysql://127.0.0.1:3306/orgresource_extended_local?serverTimezone=UTC`.
- `EXT_DB_MIGRATION_URL`, `EXT_DB_MIGRATION_USER`, and `EXT_DB_MIGRATION_PASSWORD` configure Flyway's separate connection.
- `EXT_BOOTSTRAP_SLUG`, `EXT_BOOTSTRAP_ORG_NAME`, `EXT_BOOTSTRAP_USERNAME`, `EXT_BOOTSTRAP_DISPLAY_NAME`, and `EXT_BOOTSTRAP_PASSWORD` configure the first fictional owner one time. Run `java -jar OrgResource-Extended-C5-Java21.jar --bootstrap` in a private terminal. Protect the generated one-time recovery codes and clear the bootstrap password from the environment afterward.
- For this loopback HTTP demo only, set `SPRING_PROFILES_ACTIVE=c1-test`. Leave `EXT_SMS_ENABLED=false` and `EXT_REMINDER_MODE=DISABLED`. Optional encrypted fictional contact collection uses a protected, independent 32-byte Base64 `EXT_CONTACT_ENCRYPTION_KEY` with its key ID. Keep that key with the operator's protected key records.

Run `java -Duser.timezone=Asia/Manila -jar OrgResource-Extended-C5-Java21.jar`. Open `http://127.0.0.1:8085/#/login`, sign in, and choose the organization. `GET /api/departments` returns JSON 401 before authentication and a JSON list after sign-in and workspace selection. The Postman collection and local environment under `postman/` use ephemeral fictional credentials. Stop Java with Ctrl+C and restart using the same settings. MySQL retains the records.

If startup reports Flyway validation or an unexpected existing schema, stop and inspect the selected schema and migration account. For HTTP 401, sign in again. For a POST rejected on origin or CSRF, use the same numeric loopback origin and a fresh session CSRF token. For local development at `127.0.0.1:5173`, use Vite and set `EXT_VITE_BACKEND_PORT` to the Java port if it differs from 8085.

## Encrypted backup and recovery

Keep the Java app stopped or otherwise quiesce writes and DDL before taking a recovery snapshot. Set up a protected local MySQL login path using `mysql_config_editor` and a dedicated schema-scoped read identity. Give it the metadata visibility required by your dump. The restore login path uses an elevated identity restricted to the new target schema. Keep the separate random 32-byte Base64 `EXT_BACKUP_KEY` in the backup operator's protected environment. Store access to that key separately from the encrypted archive. Retain the separate contact key and its key ID for as long as the snapshot contains encrypted contacts.

```powershell
pwsh -NoProfile -File .\scripts\extended-c-backup.ps1 -Mode Backup -Port 3306 -Schema orgresource_extended_local -LoginPath orgresource_backup -Archive C:\private-backups\workspace.aesgcm
pwsh -NoProfile -File .\scripts\extended-c-backup.ps1 -Mode Restore -Port 3307 -Schema orgresource_extended_restore -LoginPath orgresource_restore -Archive C:\private-backups\workspace.aesgcm
```

The local tool is limited to 64 MiB of SQL, MySQL on numeric loopback, and Extended schemas. It takes an InnoDB single-transaction dump, includes Flyway history and binary columns, and saves session table definitions with empty data. Decryption and authentication complete in memory before any SQL import. A restore requires zero tables in a newly created schema and can leave a partial schema if MySQL fails during import. Keep that failed target isolated and retry on a fresh empty target after diagnosis. Never run the import over the current workspace or Course A. Recreate database users and grants separately. Sign in again after restoring. Account hashes, contact envelopes, and durable operation identities survive the restore.

Record the archive SHA-256 and size in protected operator records. Retain the prior independently verified backup until the replacement has passed a restore drill. Maintain a second protected storage copy and key access for the authorized recoverer. Review retention alongside the organization's data policy. Use a new key for subsequent snapshots when rotating backup keys, and keep the older key available until all backups using it expire or are replaced. Keep reminders disabled in recovery drills and on restored data until contact settings and dispatch state have been reviewed.

An upgrade failure can leave MySQL DDL partly applied. Retain the pre-upgrade JAR and selected snapshot and restore them into a separate empty target. Confirm Flyway history, logins, tenant permissions, inventory balances, image bytes, and durable replay before switching a local launch configuration. Never fabricate stock history or migrate legacy plaintext contacts by default.

## Release boundaries

Default bindings are loopback and secure cookies require HTTPS outside the local test profile. There is no verified production reverse proxy, hosting, operator access policy, off-host backup drill, or live provider path in this release. The Picomatch development advisory remains a public-release blocker pending a compatible reviewed remediation or owner decision. See the [C5 evidence](../evidence/C5/PHASE_REPORT.md) for actual results.

# P0: Populate the existing Northflank OrgResource with the exact 96-workspace demo

**Priority:** P0 (user-directed, 2026-11-23 PHT). **Status:** PREPARATION IN PROGRESS; **LIVE_IMPORT_NOT_RUN**. **Issue:** [#26](https://github.com/newonathe/OrgResource/issues/26). **Draft PR:** [#25](https://github.com/newonathe/OrgResource/pull/25). **Do not mistake successful GitHub CI for live database execution.**

## Target and immutable inputs

The user wants the **existing deployed Northflank application**, with the existing owner, original organization and records preserved, populated with the *specific previously approved* directory names and fictional internal histories. The approved historical full plan is **96 workspaces (72 COA-M / 24 LIONS), 248 Departments, 713 Projects, 1,312 Resources, 2,139 Requirements, 462 simulated personas, 21,060 requests, 67,370 action records**. All student activity and transaction history must be prominently synthetic. Public labels are not partnerships or institutional adoption.

- Private reference manifest SHA-256: `57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2`.
- Private plan SHA-256: `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`.
- Plan `dataset_id` and organization labels/tiers are in the **private approved input**, not to be guessed, scraped into a different file, or publicly committed. Restricted local source/plan were **not accessible to this execution session**. Run the existing `scripts/preflight-c8-demo-plan.py --profile full` on the owner's private host against that exact plan first.
- Separate frozen Course Final A commit: `7f8c0746da18fd54b47f430ac1435ac36e605d67`. No alteration.

## Work already committed to PR #25

1. `6c7fb2653dd7ccb6fe299ec1b96105193ef4928f` — bounded `DemoProvenance` support for a full 96-workspace cohort **alongside** untagged existing workspaces, without permitting incomplete cohorts or mismatched SHA source/plan. Requires an active credentialed OWNER who is a member of **every demo organization**. Includes pure-classifier tests. This is code prepared for an eventual additive importer, not verified live behavior.
2. `aa757ef9f319260bbddbeeea0cab8a7e05208c59` — separate **read-only** `--c8-hosted-demo-preflight` command, run before Spring starts. Checks exact full plan SHA-256, provenance/counts/unique IDs, explicit selected database, MySQL **8.0**, Flyway V7, existing active OWNER credential and expected original organization, no existing tagged demo, and prints only an opaque target fingerprint and counts. Never writes, migrates, sends messages or imports. Includes negative tests.
3. As of this report, CI for these commits is **PENDING / CHECK LIVE**; full positive runtime/SQL checks and independent review remain **NOT_RUN**. Check exact runs before promotion.

## First safe private operator preflight (no DB writes)

Use a **separate manual Northflank job or trusted private operator process**, not the public web process. Keep the exact source file in a private short-lived secret/volume, not GitHub, CI artifact or this conversation. Provide environment variable values privately using the Northflank secret facility. Confirm actual internal MySQL TLS and identity requirements; never expose a database publicly for convenience.

```sh
# Environment variable NAMES required; values must come from the owner/operator,
# never from an example, chat or public repository:
# EXT_C8_HOSTED_EXPECTED_SCHEMA      actual schema, beginning orgresource_
# EXT_C8_HOSTED_OWNER_PUBLIC_ID      current real owner account's opaque UUID
# EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID current original organization's opaque UUID
# EXT_C8_DEMO_PLAN                    absolute path to exact private approved plan
# EXT_DB_URL                          existing MySQL JDBC URL (no embedded creds)
# EXT_DB_USER                         schema-scoped read-only preflight principal
# EXT_DB_PASSWORD                     private database password
java -jar extended-backend-0.1.0-SNAPSHOT.jar --c8-hosted-demo-preflight
```

Expected stdout on a **verified run** includes `HOSTED_DEMO_PREFLIGHT_READ_ONLY_OK`, `database_writes=0`, Flyway V7, original organization count, opaque target fingerprint and fixture count. This tool deliberately **cannot** load the demo, and a passing preflight is not backup/restore evidence. Run from a pinned audited code SHA/packaged JAR, not an unverified main branch. Never paste source/plan or secret URLs into public build logs.

Before a preflight is considered authoritative, check **actual CLI successful execution on a private cloned/staging database**, and the final GitHub tests for that same SHA. When the URL or MySQL version differs, STOP: the code's host checks are conservative and currently accept only MySQL 8.0.

## Live mutation gates — all mandatory

- [ ] **Source:** exact approved private manifest and plan present; independent SHA verification and aliases/tier/96 unique keys reconciled.
- [ ] **Identity:** confirmed Northflank service image, branch commit SHA, original owner + org IDs, MySQL V7/8.0, runtime permissions, cost/quota, no unexpected tagged cohort. No secrets in reports.
- [ ] **Backup:** completed native MySQL backup with timestamp/ID **and** independent encrypted off-host backup; restore on a *separate isolated copy*, then verify original account, roles, organization, resources, stock ledger, exports and owner login. Never overwrite populated target with a test restore.
- [ ] **Importer:** implement, review and test a **separate additive non-web importer**, NEVER reuse the current `--c8-local-demo-import` command. Check exact plan and target fingerprint, preserve existing rows, bind the current owner as OWNER on each demo workspace, use existing inventory domain transitions for historical replay, no contact/SMS, unique source identifiers, conflict-free slugs, bounded per-workspace transactions, stable replay/retry refusal or verified resume, and a clear partial failure/maintenance recovery plan.
- [ ] **Fixture tests:** test 3-organization small, 8-organization intermediate and full 96 private plan in isolated MySQL 8 clones; verify sums/hold/custody/receipt/restart/rollback and original-row hashes. Measure memory/disk/CPU and backup sizes, not assume a 512 MiB/0.2-vCPU staging service is sufficient.
- [ ] **Review:** genuinely independent security/data integrity review with exact source, SQL, test logs and each unproven claim labeled. C8 93-route acceptance still separate and remains BLOCKED.
- [ ] **Execution:** only then run an owner-authorized Northflank **manual job** with a verified private source, managed maintenance window and a confirmed rollback reference. No silent startup import, scheduled CI import, spend or unapproved public release.
- [ ] **Hosted browser:** verify 96 approved labels, selected workspace switching, 248/713/1312/2139/462/21060/67370 counts, status/time distributions, owner existing data intact, all synthetic badges and CSV/JSON provenance, access controls and mobile/desktop. Record exact SHA, importer receipts, resource usage and any anomalies.

## Genuine external platform facts and hazards

Northflank supports [manual jobs](https://northflank.com/docs/v1/application/run/run-an-image-once-or-on-a-schedule), [MySQL backups and isolated fork/restore](https://northflank.com/docs/v1/application/databases-and-persistence/backup-restore-and-import-data) and version choices such as [MySQL 8.0.46](https://northflank.com/docs/v1/application/databases-and-persistence/deploy-databases-on-northflank/deploy-mysql-on-northflank) (docs checked 2026-11-23). **Restoring/importing backups to an existing addon can replace existing databases**, so test restore must use a separate instance. Verify actual pricing and resources in the owner's account. GitHub access alone does not grant access to Northflank or its database.

## Exact next work

1. Reconcile and fix any CI errors at `6c7fb265` and `aa757ef9`; do not suppress or alter a safety test to make green.
2. Implement dedicated additive importer and an isolated real MySQL test that begins with an original owner and original organization, adds 3 fictional workspaces, confirms both pre/post and idempotency behavior, then extends to 8 and private 96. Preserve local fresh-only import unchanged.
3. In parallel, the owner/private operator must securely make the approved manifest + plan and a verified off-host restore available **in the actual authorized runtime**. Their absence is a hard stop for live population.
4. When all gates pass, use a manual job rather than a web route, record proof and only then claim the live site populated.

**Verdict:** `LIVE_IMPORT_NOT_RUN`. Current repository improvements are preparation; the production importer, verified private source and Northflank operator proof remain to be completed. Keep this task P0 before optional route-oracle expansion, while separately protecting Course Final A's printed December 5 deadline.

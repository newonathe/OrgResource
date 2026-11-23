# P0 Northflank 96-organization demo — source and test checkpoint, 2026-11-23

## Provenance and status

**USER DECISION / P0:** The existing deployed Northflank OrgResource must show the previously owner-approved 96 public directory organization names, containing fully fictional internal histories; original account and old organization remain unchanged. **LIVE_IMPORT_NOT_RUN.** Work is on draft [PR #25](https://github.com/newonathe/OrgResource/pull/25) and [issue #26](https://github.com/newonathe/OrgResource/issues/26). This is not C8 acceptance, independent review, merge, deployment, academic submission or hosted backup evidence.

**VERIFIED CI baseline:** `c259759cbee3902d730bcc3c7218c98877d6a3d9` — all 3 jobs SUCCESS in [run 45074711543](https://github.com/newonathe/OrgResource/actions/runs/45074711543), including Java 8/MySQL, frontend and Extended C Java 21/MySQL, Newman/Playwright. That SHA tested 3- and 8-workspace fictional isolated additive replay and the Account Settings motion toggle.

**Source commits during this continuation:**

| Change | Source / evidence | Status |
|---|---|---|
| `98a4c9765418937296d1acdf4c3ce248ccbebcc9` | `C8HostedDemoImportCommand.java`, `C8HostedDemoPreflight.java`, `C8DemoImporter.java`, `C1Application.java`, `C8HostedDemoImportCommandTest.java`; [CI 45096884882](https://github.com/newonathe/OrgResource/actions/runs/45096884882) | **SOURCE_CHANGED; exact CI still IN_PROGRESS when recorded** |
| `a967fffff15ba9ba8d81e18e72b709b4c929a223` | `AuthScreens.tsx`, `styles.css`, `App.test.tsx`; searchable 97-workspace picker, [CI 45096965248](https://github.com/newonathe/OrgResource/actions/runs/45096965248) | **SOURCE_CHANGED; exact CI still IN_PROGRESS** |
| `d5961b795857c311708ba45992a6a5e892a5a4eb` | `docs/deployment/P0_NORTHFLANK_MANUAL_JOB_COMMAND_2026-11-23.md`; operator manual job/rollback/maintenance, [CI 45097294067](https://github.com/newonathe/OrgResource/actions/runs/45097294067) | Documentation only, **CI still IN_PROGRESS** |
| `35f96466cf04c7239f6e04e72bb1ccfd79525d40` | `scripts/verify-c8-additive-isolated.sh` tests original fictional domain/owner hashes, all six status counts, demo audit action totals, hold/issued/stock/requirement conservation, zero demo phone/SMS fields and repeat import refusal; [CI 45097447089](https://github.com/newonathe/OrgResource/actions/runs/45097447089) | **SOURCE_CHANGED; exact test execution PENDING** |

## Technical current design, grounded to actual code

**SOURCE VERIFIED:** `C8HostedDemoPreflight.inspect` is SELECT-only and refuses wrong target schema, MySQL version other than 8.0, non-V7 migration state, missing active original credentialed OWNER, existing tagged demo cohort or any full plan different from immutable SHA `3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c`. Expected private source manifest SHA `57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2`.

**SOURCE VERIFIED:** `--c8-hosted-demo-import` is a separate non-web command and requires exact plan plus target fingerprint, explicit backup ID, restore evidence digest and an operator's restore attestation; SMS and reminders disabled and test profile refused. It does not start automatically in a web service, CI or scheduled task. Source method `importApprovedHostedPlan` refuses anything other than exact SHA+96-full cohort before delegating to the same transaction-scoped domain replay used in isolated CI proofs. A new real owner gets present-day OWNER access; synthetic historical accounts carry fictional action attribution; all 96 workspaces must reconcile and public mixed metadata must validate at end.

**LIMITS / independent critique:**
1. **Critical:** The backup ID, receipt SHA and text attestation are strings provided by the operator, **not cryptographic confirmation of an independently restored MySQL backup**. Never run live based on them alone. External signed/reviewed restore proof is still absent.
2. **Critical:** The full private approved roster/plan bytes have not been recovered in this conversation or public GitHub. Exact SHA makes an unauthorized substitute fail; no full 96 live or isolated-run proof exists.
3. **Critical:** No authenticated Northflank console/DB access exists here. Current image SHA, target version, resource limits, service/DB backups, private runtime secret file availability and running service compatibility are all **NOT_VERIFIED**.
4. **Critical:** The importer is transaction-atomic *per workspace*, not for all 96. Any later failure can leave a partial tagged cohort, which intentionally produces a 503 classification and requires maintenance and a separately validated recovery plan. **No resumable import exists.**
5. **High:** Deploy the compatible web version **before** bulk import. A stale web image rejects a new 96+original mixed database. Roll back app+database together; do not claim service compatibility from GitHub CI.
6. **High:** No measured end-to-end 96-workspace MySQL memory/CPU/disk/backup sizes on the real selected Northflank resource, no independent full scale test. Test in a detached clone with same capacity.
7. **High:** C8 route-level 93-operation positive/negative+SQL suite and independent security review remain separately **BLOCKED**. Do not claim course-compliant final deployment or approval.
8. **Medium:** The account has ~97 memberships; searchable picker improves first login. Test real logged-in switching of each role and live host mobile/browser responsiveness separately; no screenshot observation here.

## Next tasks, in order

1. Fetch exact HEAD from GitHub. Inspect final run status/logs at `98a4c976`, `a967ffff`, `35f96466`; fix only observable errors; rerun CI and capture exact green SHA. Never conceal a failing run.
2. Have an **independent reviewer** inspect actual changed source, authorized inputs, MySQL original-row integrity, P0 tests, partial-failure recovery, contact minimization and full dataset rights. Reviewer work **NOT_RUN**.
3. Secure exact approved private roster and plan on an authorized encrypted operator machine, verify SHA, and verify whether the source list is appropriate for **public hosting** as well as local demo use. No private uploads to public repository.
4. Check real Northflank service/container version, MySQL 8.0/V7, original owner/org, separate runtime privileges, quotas, actual cost, web compatibility, and prevent live SMS.
5. Backup existing MySQL with Northflank native backup, encrypted off-host copy; independently restore to an **isolated separate DB** and test login/data hashes. Validate full 96 import there before making a live change. Compare resource usage to available plan.
6. Under owner-approved maintenance/change control, use the manual job and source SHA-only command `--c8-hosted-demo-import`, then independently inspect all 96 labels, counts, stock, statuses, history, tenant boundaries, owner data, synthetic notice and search.
7. Mark `LIVE_IMPORTED` only if real hosted MySQL+UI and backup evidence are fully documented; otherwise record exact blocker and keep P0 open.

**External facts — checked November 23, 2026:** Northflank [manual jobs](https://northflank.com/docs/v1/application/run/run-an-image-once-or-on-a-schedule), [backups and destructive restore warnings](https://northflank.com/docs/v1/application/databases-and-persistence/backup-restore-and-import-data), [runtime secret files](https://northflank.com/docs/v1/application/secure/inject-secrets), [version offerings](https://northflank.com/docs/v1/application/databases-and-persistence/deploy-a-database). Actual user subscription/service state unverified.

**Next copy-ready implementation/ops handoff:** [docs/handoffs/2026-11-23_CODEX_NEXT_P0_HOSTED_DEMO.md](../../docs/handoffs/2026-11-23_CODEX_NEXT_P0_HOSTED_DEMO.md).

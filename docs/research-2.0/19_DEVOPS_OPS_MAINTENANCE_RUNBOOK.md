# GitHub, CI/CD, service operations, backup and incident runbook
**Status:** PROVISIONAL RECOMMENDATION — no V2 pipeline or host created. **Date:** 2026-11-15. Sources G01–G11, E06–E18, E26.

## Repository / branch governance
- Private `newonathe/OrgResource`, `main` O1 baseline, current stacked PRs #2–#6 unmerged; research draft #7 based on O8/O10. **Do not merge** by automation. Do not make V2 code branch from stale `main` without reconciling accepted PR stack.
- Before each implementation phase: audit current origin HEAD, all PR bases/merges, local `git status --short --branch`, remotes, dirty/untracked changes, last phase evidence, outstanding tests, `AGENTS.md`, source docs and current issue; one writer/worktree per file.
- Create dedicated feature branch based on *approved* latest accepted code, not on docs-only research; commit code/tests/migrations/Postman/evidence coherently and push **without force**, verify remote HEAD and open draft PR. Do not commit `node_modules`, `target`, personal data, MySQL dumps, real API keys, .env, screenshots with PII, secrets, huge source archives or user unrelated files.
- Separate course V1 tagged release only after O14 and student review from future `v2.*` tags. No retroactive rewriting of submission artifacts.
- PR template requires source evidence IDs, schema/API changes, migration from previous version, security/privacy effect, free-tier effect, actual passed/failed/not-run commands, rollback method, code owner review and branching provenance.

## Proposed CI gates (implementation pending)
1. Check source formatting/lint, documented dependency checks, secret detection and no real-data fixtures; pinned Maven/Node lock.
2. Compile/test supported Java candidate in temporary test DB, with real JSON+Jersey endpoint tests if chosen.
3. Build versioned V2 React assets with TypeScript strict, lint/unit tests; minimal real browser E2E in dedicated separate job when budget allows.
4. Run DB migration up/down **recovery rehearsal**, composite FK tenant isolation, scenario-based property/race tests. Don't label CI generic Java green as Postman or Playwright passing unless jobs run them.
5. Generate release SBOM, effective dependency versions and API contract diff; fail release on critical unresolved vulnerabilities and tenant-bypass regressions.
6. Size/quota check workflows; target within GitHub private repo included actions minutes and artifacts retention. E26 warns advanced security may not be free for private repo; use compatible local OSS scanner if paid feature required.
7. Upload only sanitized test artifacts, bounded retention and no credentials or public screenshots of private data.

## Deployment pipeline when explicitly approved
Build immutable artifact from commit SHA, sign/checksum; rehearsal staging schema (all fictional) with separate secrets; migrate under a maintenance lock and snapshot backup; health check; deploy app/API and versioned asset names; verify TLS+auth+guest isolation; smoke every critical API; monitor error/quota and rollback on failure. Migration rollback is reversible only if data transform supports it; else restore backups and freeze writes; never assume generic down migration safe.

## Routine operating cadence (proposed; requires actual available operator)
| Cadence | Task | Proof |
|---|---|---|
| Daily | Check availability/quota error/unsent in-app reminders; verify backup job success | Redacted metrics + backup SHA |
| Weekly | Check free-tier usage at 50/75/90%, DB growth and failed jobs, review invalid auth/tenant access trends, test sampled export readability | Bounded report, no real emails in logs |
| Monthly | Restore backup to fresh isolated test environment; verify row counts, stock/ledger checksums, membership role bounds, return application | Dated restore report + actual measured RPO/RTO |
| Monthly | Dependency vulnerability assessment and patch decision; archive/log retention review | CVE waiver with expiry or upgrade PR |
| Quarterly | Provider TOS/quotas/eligibility/reclaim reevaluation, DR incident simulation, accessibility review, open source license check | Decision/risks update and user approval for costly changes |
| On officer transition | Verified ownership acceptance, role revoke/recover, documented org export | Non-orphan OWNER and confirmed handoff |

## Incident response playbooks
**Security suspected:** disable affected session/API and if necessary public ingress; preserve logs/evidence, classify impact, rotate keys, identify tenant scope, notify legally applicable parties using current NPC E02–E04 criteria; no unsupported promise of always reporting every incident under 72h. Do not expose data in Git issues.
**Inventory invariant mismatch:** set affected resource/tenant write gate read-only, export event trace, compare snapshot vs SUM(delta), investigate idempotency and concurrency; correction must append movement with reason+review, not mutate old row.
**Provider quota reached:** return safe `503 QUOTA_UNAVAILABLE` with user-readable message, never auto-upgrade; wait until documented reset or move to fully verified alternative. No false successful write response.
**Primary VM loss:** restore from separately held encrypted backup and manifest, attest RPO (time of last successful backup) and RTO (actual recovery duration), replay migrations and outbox safely, rotate leaked secrets. If no alternative free VM is available, keep service offline and provide secure export—not invented high availability.
**Decommission/retirement:** give org admins reasonable notice and export if any real users; lock new mutations, revoke sessions, purge DB/backup according policy, record non-sensitive evidence of deletion; no official institution retention assumption.

## Operator secrets and cost boundaries
Least-privileged DB user, no public DB port, config via protected env/host key system; HTTPS only; scoped service tokens never in committed scripts/logs. No account creation/deployment in research. Prefer built-in logs or free-tier tool only if current quotas verified. SMS/email disabled by default; course FakeTwilio is sandbox-only. Operator must have an incident contact and access to recovery key separate from backups before any real people enroll.

## New release train and immutable academic-artifact protection — 2026-11-15
**User-approved order:** fresh GitHub PR/code audit → true O14 early academic baseline source+executable SHA → independent modern supported V2 feature-branch development → actual secure hosted technical QA, quota and off-host recovery rehearsal → **P-ACCEPT** → fresh **A-FINAL** original course binary/Postman/video/Canvas/authorship → **student manual submission**. Student-controlled Canvas only. Post-graduation org enrollment is a separate later adoption action, not technical acceptance.

At O14 baseline, record exact Java8 original dependencies, five entities, all Jersey/Javax/autowired/JPA/Retrofit/Gson/scheduler/Postman code, source archive/JAR hashes and repeatable build. Do not silently rewrite the class artifact while modernizing supported V2. Actual V2 work must have its own reviewed PR, BOM, migrations and clean verified runtime/DB tests. No auto-merge, auto-release, expenses or paid services.

For later hosted trial (requires explicit user authorization): stage fictional tenants only, check secure external HTTPS, no real messages, free account/quotas and hard cost stop; run BOLA/race/CSRF/idempotency, access/contrast/route parity, in-service scheduler, synthetic load, encrypted **off-host full backup restored on a clean alternative**, and rollback/incident drill. Keep a `P-ACCEPT` matrix with per-test SHA/command/timestamp/evidence/PASS|FAIL|BLOCKED|NOT_RUN/owner. If any hard P gate fails, no production-ready claim and **do not slip academic final gate ahead of it** under current user instruction. Print Dec5 is high risk, never an automatic waiver.

## CP11 runbook prerequisites, NOT executed (2026-11-15)
Before any hosted P-ACCEPT, choose **A self-managed Oracle Linux9 ARM MySQL8.4.13** or **B MySQL.Free managed private VCN with dynamically upgraded MySQL9.7 default**, without changing actual course MySQL technology. Track separate app/db/admin secrets, operator+Java service user, no inbound 3306, update policy and DBA/managed maintenance windows. On B, provider backup retention only **1 day** and no manual backup/PITR; on A, VM/block backup same failure domain. Both require authenticated-encrypted off-account daily logical export, retention/key manifest, checksum and actual clean restore; [MySQL dump --single-transaction](https://dev.mysql.com/doc/refman/8.4/en/mysqldump.html) handles consistent InnoDB but must prevent concurrent DDL during dump; JPA schema tables, session tables and audit events included. Record selected DB actual version/collation, migration rollback, ledger offsets and cross-org FK checks. [Let's Encrypt IP](https://letsencrypt.org/2026/03/11/shorter-certs-certbot) Certbot>=5.4 manual proxy cert installation, renewal every3 days/deploy reload and expiry alert. [Backblaze B2](https://www.backblaze.com/docs/cloud-storage-data-caps-and-alerts) caps Classes A–C only, Class D unbounded, no charge-proof yet. Full H-01..12 [38](38_ZERO_COST_HOST_MYSQL_FREE_TLS_AND_EXTERNAL_DR_CP11.md). **No operator automation, account, cert, invoice, backup or clean restore run in this research pass.**

## CP12 exact maintenance/backup version capture (2026-11-15)
Managed MySQL.Free is **unpinnable**: log actual server version, `sql_mode`, collation, `require_secure_transport`, selected Connector/J/Hibernate versions and timestamp of every provider maintenance; general default changed to 9.7.4 Oct20 but MySQL.Free highest-version rule may differ. User must authorize account before any live inspection. Self-managed repo MySQL8.4 LTS track offers version control but OS/patch operations are operator responsibilities. Schedule app/DB migrations separately from dump; using [Oracle MySQL Shell dumpInstance](https://docs.oracle.com/en-us/iaas/mysql-database/doc/migrating-mysql-heatwave-db-system-mysql-instance.html), export user-created schemas/users/routines/events plus Spring Session JDBC tables and metadata, **encrypt client-side off-OCI**. Backup automatic Free one day and final-on-deletion max seven days are SAME-TENANCY only. Restore **both** same-tenancy supported Free snapshot and independent export into a clean compatible **MySQL 8.4 or free-assigned version**, verify data+FK+ledger+role invariants; cross-major newer→older only conditionally supported and requires documented exercise. [39](39_MYSQL_84_VS_FREE_97_VERSION_CLIENT_AND_RESTORE_COMPATIBILITY.md). No run performed.

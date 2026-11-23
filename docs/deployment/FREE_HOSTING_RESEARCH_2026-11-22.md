# OrgResource: free whole-stack hosting decision (research dated 2026-11-22)

**Status: research and operator instructions only; no account, instance, database, deployment, or new code has been created.** This document intentionally contains no private logs, student identifiers, passwords, API keys, generated real data, backups, or restricted local proof. Repository currently public; retain privacy/rights release veto.

## Actual application and source boundary

- Repository: https://github.com/newonathe/OrgResource ; draft C8 PR: https://github.com/newonathe/OrgResource/pull/25
- Source inspected before this documentation: PR head `de41d0822cbfdf6877f22ac68f8bfd0a4b7e49e6`. Application code last changed at `0b8da10d76d746b315d9601e411acbb2ca48882b`; following commits largely evidence, agent guidance and CI setup. Re-read live refs before acting.
- Last verified GitHub Actions all-green run on that head: https://github.com/newonathe/OrgResource/actions/runs/44729375170 (Course A Java 8, Extended Java 21/MySQL/Newman/Playwright, React).
- Extended: Java 21, Spring Boot 4.1.2, Jersey, session-backed auth/CSRF, JPA/Hibernate, Flyway, MySQL. React/Vite is bundled into the Spring Boot JAR; **one HTTP application process**, no separate frontend host. See `extended/backend/pom.xml`, `extended/backend/src/main/resources/application.properties`, `extended/frontend/README.md`.
- Build without source edits: `npm --prefix extended/frontend ci && npm --prefix extended/frontend run build && bash ./backend/mvnw -B -f extended/backend/pom.xml -DskipTests clean package` from repository root with Node 24 and Java 21; verify output `extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar` and bundled `BOOT-INF/classes/static/`. The normal CI uses `clean verify`, so skip-tests packaging is **not** an acceptance substitute.
- Runtime default: `server.address=127.0.0.1`, port 8085; `EXT_DB_URL/USER/PASSWORD` and separate `EXT_DB_MIGRATION_URL/USER/PASSWORD`. Default secure `__Host-ORGSESSION` cookie requires HTTPS. The `c1-test` profile disables secure cookies and must NOT be used on an externally accessible deployment.
- `C1SameOriginFilter` compares browser Origin to request scheme/host/port on state-changing API calls. A reverse proxy must supply trustworthy forwarded request metadata and `SERVER_FORWARD_HEADERS_STRATEGY=framework` (configuration-only proposal) must be validated by a real HTTPS login + POST/CSRF test.
- All development/CI MySQL processes are disposable. They are **not a persistent hosted database**.

## Provider comparison and current recommendation

Selection requirements: truly free ongoing operation, always-on or near-always-on Java 21 with enough RAM, persistent **MySQL** (not just Postgres), HTTPS, same-origin bundled React, no compulsory code modification while Codex is unavailable. No free service guarantees perfect or endless uptime.

| Provider | Ongoing free tier | Fit | Principal blocker |
| --- | --- | --- | --- |
| **Oracle OCI Always Free** | Ampere A1 2 OCPUs + 12 GB RAM equivalent (1,500 OCPU and 9,000 GB-hours/month); one MySQL HeatWave `MySQL.Free` DB with 50 GiB and 1-day managed backup | **Best resource capacity for full Java+MySQL stack; recommendation for manually operated staging** | A1 capacity/account registration; inactive VM reclamation; managed DB defaults to newest MySQL version, incompatible until tested; no SLA, high-availability or long managed backup |
| **Northflank Developer Sandbox** | 2 always-on app services, 1 database addon, 2 jobs, free sandbox; provider explicitly supports MySQL 8.0.46 in addon version list | **Best low-ops alternative for a small demo**; Git-linked builds, automatic HTTPS | Sandbox officially **not for production**; much tighter compute/memory; app build for this two-stage frontend+Java monorepo needs testing; payment method requirement confirmed by current billing docs (older blogs conflict) |
| Google Cloud Always Free Compute | 1 e2-micro in limited US regions, small disk/egress | Possible simple experiments | 1 GB-class VM and no free managed MySQL; running Java+MySQL together is resource constrained |
| Azure for Students | US$100 credit over 12 months and selected limited free services | Useful **temporary** academic alternative | Expiring credit / student eligibility, not whole-stack forever-free |
| Render Free | Free Java web service | Temporary UI/demo | Free services spin down; no lasting free managed MySQL |
| Railway Free | Small recurring free allowance | Trial/demo | 24/7 Java + persistent MySQL typically exceeds allowance; actual price and sleep must be checked in UI |
| Pure static hosting/serverless | Often truly free at small scale | Bundled React alone | Cannot run existing stateful Java service and MySQL unchanged |

**Recommendation:** Oracle OCI Always Free VM plus Always Free managed MySQL *only after an isolated compatibility preflight*. Northflank Sandbox is a genuine second option and is easier to operate but is not production-supported and cannot be assumed to fit the app within free size. If managed Oracle MySQL fails source/flyway compatibility, STOP, consider a tested 8.0-compatible database path; do not assume managed Oracle MySQL can be pinned to 8.0.46.

**Specific 2026 Oracle DB hazard:** MySQL HeatWave announced MySQL **9.7** as the default for newly created DB systems (September 29, 2026); Always Free maintenance upgrades to the highest version. OrgResource verification is on MySQL **8.0.46**. The ability to select a supported 8.4 track or migrate successfully is not proven for this exact tenancy. Never import existing/private data until the actual free DB version, Flyway V1–V7 behavior, indexes, procedure/trigger privileges, and JDBC/TLS are verified on disposable fictional data.

**Costs/reliability caveats:** OCI Always Free may require card verification. Select HOME region carefully; region is not changeable. Choose only resources labeled 'Always Free' and avoid paid upgrades. A1 shape may be out of stock. OCI may reclaim inactive Always Free VMs by its documented seven-day metric rules; avoid artificial keep-alive load. MySQL.Free is standalone, no HA, limited managed backups (one day; no PITR, no manual managed backup), may auto-upgrade. Do not promise uninterrupted service, response times, capacity, or zero charge without dashboard confirmation. Configure free-tier usage warnings/quotas where supported and maintain an independently verified off-instance recovery copy if making a data-bearing service.

## Primary sources inspected 2026-11-22

- Oracle official Always Free allowances, capacity and reclamation: https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm
- Oracle signup and irrevocable home region: https://docs.oracle.com/en-us/iaas/Content/GSG/Tasks/signingup_topic-Sign_Up_for_Free_Oracle_Cloud_Promotion.htm
- Oracle MySQL.Free console creation, private subnet, one-day backups: https://docs.oracle.com/en-us/iaas/mysql-database/doc/creating-always-free-db-system.html
- Oracle managed MySQL version automatic upgrades: https://docs.oracle.com/en-us/iaas/mysql-database/doc/mysql-server-upgrades.html
- Oracle default MySQL version 9.7 announcement: https://docs.oracle.com/en-us/iaas/releasenotes/mysql-database/heatwave-97x-default-track.htm
- Oracle backups restrictions: https://docs.oracle.com/en-us/iaas/mysql-database/doc/overview-backups.html
- Oracle database private networking: https://docs.oracle.com/en-us/iaas/mysql-database/doc/overview-networking.html
- Northflank official Sandbox pricing: https://northflank.com/pricing
- Northflank current payment requirement, no production use on Sandbox: https://northflank.com/docs/v1/application/billing/pricing-on-northflank
- Northflank MySQL-supported versions: https://northflank.com/docs/v1/application/databases-and-persistence/deploy-a-database
- Google Cloud free tier: https://docs.cloud.google.com/free/docs/free-cloud-features
- Azure for Students: https://learn.microsoft.com/en-us/azure/education-hub/about-azure-for-students
- Caddy HTTPS/proxy: https://caddyserver.com/docs/automatic-https and https://caddyserver.com/docs/quick-starts/reverse-proxy
- Free DNS hostname possible via DuckDNS: https://www.duckdns.org/spec.jsp

## Unverified, no-code pre-deployment gates

1. User can create the chosen free account; free compute and free MySQL both available in chosen home region without payment.
2. Exact MySQL server version and Flyway schema/bootstrap compatibility proven using a NEW fictional schema. Managed-user DDL/CREATE ROUTINE/TRIGGER permissions verified; no bypass of runtime read-only audit/ledger grants.
3. Bundled JAR reproducibly builds from pinned C8 SHA on ARM64 or can be transferred as a verified artifact, without changing source.
4. HTTPS proxy forwards correct host/scheme; secure-cookie login, CSRF/Origin-protected POST, account recovery, role revocation, stock replay, backups and MySQL access pass.
5. Staging is access-restricted and uses fictional data only. The public repository's unresolved rights/privacy concern still blocks unrestricted public release.
6. Real uptime/performance, backup/restore, remote recovery, and $0 spend evidenced from the actual configured free account. No SLA.

**Current decision**: research recommendation only. No provisioning, checkout, build, release, data transfer or deployment is represented as completed.

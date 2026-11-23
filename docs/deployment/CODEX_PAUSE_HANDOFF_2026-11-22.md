# OrgResource deployment handoff while Codex quota is exhausted — 2026-11-22

This is a **public-safe context summary**, not a full copy of private chat or sensitive acceptance receipts. The original local C8-IR evidence, 96-workspace fictional data, encryption keys, and account setup details stay on the authorized operator's machine.

## Current decision and status

- User's immediate goal: **zero-dollar, persistent, always-on-style OrgResource staging** using the existing code, then a credible path to production. User currently cannot edit code through Codex. Wants manual dashboard/SSH steps and continuity on GitHub.
- Deployment recommendation: OCI Always Free A1 VM + Always Free MySQL.Free if resource availability and MySQL major-version/Flyway compatibility checks pass; Northflank free Developer Sandbox as easier but capacity-constrained, expressly non-production alternative.
- GitHub draft PR: https://github.com/newonathe/OrgResource/pull/25 ; prior checked HEAD `de41d0822cbfdf6877f22ac68f8bfd0a4b7e49e6` (freshly check new head).
- Code branch: `feat/extended-c-c8-domain-workflows-2026-11-19`, parent C7. Application last changed `0b8da10d76d746b315d9601e411acbb2ca48882b`. Frozen Course A commit `7f8c0746da18fd54b47f430ac1435ac36e605d67`; protected source trees must remain unchanged.
- Last observed full GitHub run all green at prior head: https://github.com/newonathe/OrgResource/actions/runs/44729375170 . Previous transient CI download failures were subsequently repaired/retried; verify against each new SHA.
- C8 has a revised **bounded** authorization acceptance contract `evidence/C8/BOUNDED_AUTHORIZATION_CONTRACT_V1.md`; it has not yet received formal technical acceptance, as described in `evidence/C8/C8_FINAL_STATUS.md`. Historical 22,320-role-state Cartesian cases are research/old proof ledger, not an automatic production blocker under revised scope. Do not relabel unrun cases passed.
- IR4 local reports 199 executed passes, 48 reviewed structural exclusions and 22,073 unrun under the old ledger. Full-row historical+fresh restore reportedly matched 118,867 eligible rows; local-only proof not independently inspected in this handoff. No public exposure of private raw receipts.
- C9 remains unstarted; public release rights/privacy, hosting and institutional representation remain separate gates. Course Final A December 5, 2026 deadline in assignment, subject to Canvas announcements. Do not fabricate academic submission, authorship, video viewing or provider approval.

## No-code operational facts discovered from actual source

- `extended/backend/pom.xml`: Java 21, Spring Boot 4.1.2; copies `extended/frontend/dist` into JAR under `static`.
- `extended/backend/src/main/resources/application.properties`: defaults `server.address=127.0.0.1`, `server.port=8085`, secure cookie default, JDBC/Flyway env variables, disabled SMS.
- `extended/backend/src/main/resources/application-c1-test.properties`: disables secure cookie; **not** for Internet-accessible deployment.
- `extended/backend/src/main/java/edu/orgresource/extended/security/C1SameOriginFilter.java`: explicit browser Origin vs request scheme/host/port enforcement; HTTPS proxy forwarding must be tested.
- `extended/backend/src/main/java/edu/orgresource/extended/C1Application.java`: one-time `--bootstrap` is nonweb and emits recovery codes once; default starts web app; offline demo import is opt-in.
- `docs/EXTENDED_C_RELEASE.md`: existing local-first Windows/PowerShell release instructions. The backup script requires numeric loopback MySQL; cloud-hosted MySQL backup needs separate validation.
- `.github/workflows/verify.yml`: builds `extended/frontend`, then `extended/backend`, tests MySQL/Newman/browser. Course A artifact upload is separate; **do not assume an Extended downloadable JAR artifact exists**.
- Build/deploy guide: [manual Oracle/no-code runbook](NO_CODE_STAGING_SETUP_2026-11-22.md). Free hosting evidence: [provider comparison](FREE_HOSTING_RESEARCH_2026-11-22.md).

## Safe next actions and stop conditions

1. User confirms actual free hosting provider account and home region capacity; do not choose paid alternatives without authorization.
2. Provision **private** managed MySQL and Always Free ARM64 VM via dashboard, leaving 3306 and 8085 private. Validate exact MySQL version and Flyway V1–V7 in an empty fictional database.
3. Build pinned JAR from existing repo using Node24 and Java21 (no source changes), keep app on loopback, add Caddy HTTPS reverse proxy and front-door staging auth.
4. Configure private runtime/migration DB credentials, secure cookie (avoid `c1-test`), forwarded scheme, and SMS disabled. Run first-run bootstrap privately and capture recovery codes without logs.
5. Perform actual HTTPS same-origin POST, stock, cross-tenant and session smoke; measure RAM/disk/DB connections, persistence after restart and cost. Arrange independently verified encrypted off-instance backup before any data-bearing production claims.
6. If managed MySQL 9.7 conflicts, if release cannot be built on ARM64, or if HTTPS CSRF cannot work via configuration, record precise blocker, **do not disable security or change source**. Resume when Codex available.
7. For public release: separate owner authorization after the outstanding privacy/rights and C8 technical gates are resolved. No merges, paid subscriptions, external live SMS, real students, or official school endorsement.

## Minimal next Codex prompt, when quota returns

> Audit the latest OrgResource C8 draft PR and these dated docs: `docs/deployment/FREE_HOSTING_RESEARCH_2026-11-22.md`, `docs/deployment/NO_CODE_STAGING_SETUP_2026-11-22.md`, `docs/deployment/CODEX_PAUSE_HANDOFF_2026-11-22.md`. Reconcile live SHA with prior `de41d082` and the last application SHA `0b8da10`. Prioritize a small reproducible OCI Always Free ARM64 + managed MySQL **staging** gate, with private DB, TLS-secure cookies, forwarded scheme/Origin+CSRF, owner bootstrap, least-privilege Flyway/runtime accounts, privacy-safe fictional dataset and checked backup/restore. Inspect the actual managed MySQL version first; never assume 8.0.46 is available or automatically compatible with 9.7. Do not claim deployment until real HTTPS POST, session persistence, stock replay, role/tenant tests and cost/uptime are observed. Do not rewrite Course A, bypass C8 authorization, begin C9, merge, pay, deploy publicly or expose private artifacts without explicit additional authority. Return minimal tested changes if configuration cannot solve an actual blocker.

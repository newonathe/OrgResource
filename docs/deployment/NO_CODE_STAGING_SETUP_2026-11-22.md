# OrgResource: no-Codex, no-source-edit cloud staging setup (2026-11-22)

**Operator runbook — NOT executed.** Intended for a user with GitHub access but no remaining Codex quota. This prepares a restricted staging instance, not an unrestricted production launch. Use fictional data, no live SMS, no real organization/student records, no real provider keys. No merge/deploy/public release is authorized by merely following this document; confirm rights/privacy clearance before widening access.

**Start with** [hosting research](FREE_HOSTING_RESEARCH_2026-11-22.md). Oracle OCI is the preferred resource-fit option, **conditional** on usable free resources and MySQL compatibility. If those preconditions fail, do not pay or force a migration.

## Stage 0 — no account needed: verify repo

1. Open https://github.com/newonathe/OrgResource/pull/25 . Record its latest **head SHA**, not `main` or the PR's merge pseudo-commit.
2. Confirm all three current GitHub Actions jobs pass at that exact SHA. Previous reference: https://github.com/newonathe/OrgResource/actions/runs/44729375170 on `de41d0822cbfdf6877f22ac68f8bfd0a4b7e49e6`; subsequent doc commits require fresh CI.
3. Required app files: `extended/backend/pom.xml`, `extended/backend/src/main/resources/application.properties`, `extended/frontend/README.md`, `docs/EXTENDED_C_RELEASE.md`. **This is the Extended Java 21 app**, not the separate frozen Course A Java 8 JAR.
4. Check that raw files/academic proprietary materials and public-repository content are permitted for *this* destination. No real data or live SMS.

## Stage 1 — OCI registration and topology (console only)

1. Go to https://www.oracle.com/cloud/free/ and use the regular Free Tier/Always Free option. Card verification may be required. Do **not** upgrade to Pay As You Go. Read the warning about selecting an immutable home region; Singapore may reduce latency from the Philippines, but do not claim capacity until the Console confirms. OCI availability and shapes depend on actual home region.
2. Create one VCN with a **public subnet** for the VM and a **private subnet** for MySQL, e.g. `10.0.0.0/24` and `10.0.1.0/24`. Use Oracle's VCN wizard and check routing. The MySQL endpoint must never have public internet ingress.
3. Compute > Instances > Create: choose Ubuntu 24.04 ARM64 (or another supported ARM64 Java 21 image), `VM.Standard.A1.Flex`, within **2 OCPUs and 12 GB RAM**, marked Always Free, public subnet and a public IPv4. Save/download the SSH **private** key locally; only upload the **public** key to OCI. No SSH key belongs in GitHub. If A1 is out of host capacity, stop and inspect available free capacity; never silently choose a paid VM.
4. Network rules: inbound 80/443 for HTTPS validation; SSH 22 only from **your IP** if feasible. Deny public 3306 and 8085; the application binds to loopback 127.0.0.1. MySQL's private-subnet security group allows TCP 3306 only from the VM's private address/security group. Also check host firewalls.
5. Database > MySQL HeatWave > DB Systems > Create > **Always Free** / `MySQL.Free`; choose the **same VCN**, **private subnet**, 50 GiB, and a strong *new* admin password in a local password manager. One-day automatic backups are fixed. This service is private, not Internet-accessible.
6. Inspect the **actual MySQL version** offered. New managed DB systems may default to 9.7; existing OrgResource verification is 8.0.46. This is a mandatory compatibility gate. Check if the Console *actually* allows 8.4 LTS selection; do not claim it does without verifying. Managed Always Free auto-upgrades.
7. Add free-tier usage/budget alerts where available. Confirm each resource's 'Always Free' label and estimated recurring cost $0 before creation. Do not enable paid load balancer, IP, persistent volumes or paid monitoring accidentally.

Official: https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm ;
https://docs.oracle.com/en-us/iaas/mysql-database/doc/creating-always-free-db-system.html ;
https://docs.oracle.com/en-us/iaas/mysql-database/doc/overview-networking.html

## Stage 2 — connect and install prebuilt dependencies (VM, no repo edits)

On Windows PowerShell, assuming a saved private SSH key:
```powershell
ssh -i "$HOME\.ssh\orgresource_oci" ubuntu@YOUR_VM_PUBLIC_IP
```
The default Linux login username depends on the image (Ubuntu is usually `ubuntu`). Restrict private-key permissions.

On an Ubuntu 24.04 ARM64 VM:
```bash
sudo apt update
sudo apt install -y git curl ca-certificates openjdk-21-jdk-headless caddy
java -version
uname -m
```
Install **Node.js 24** using its official supported Linux ARM64 install instructions and verify `node --version` starts with v24; distribution default Node may be too old. Do not replace project lockfiles. Install a modern MySQL client/SQL shell able to authenticate to the **actual** HeatWave DB version. Do not install and expose a second database just to avoid the private managed DB.

Create working directory and pin audited code:
```bash
git clone --branch feat/extended-c-c8-domain-workflows-2026-11-19 --single-branch https://github.com/newonathe/OrgResource.git
cd OrgResource
git rev-parse HEAD
# Only if the previously audited commit is still the approved target:
git checkout de41d0822cbfdf6877f22ac68f8bfd0a4b7e49e6
```
**Important:** The commit above is a dated snapshot reference, not automatically the newest approved commit. Reconcile your current PR HEAD and use the chosen exact SHA instead. Do not `git reset`, force-push, or edit source.

Build the existing React assets, then Java:
```bash
npm --prefix extended/frontend ci
npm --prefix extended/frontend run typecheck
npm --prefix extended/frontend run build
bash ./backend/mvnw -B -f extended/backend/pom.xml -DskipTests clean package
ls -lh extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar
jar tf extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar | grep -E 'BOOT-INF/classes/static/(index.html|assets/)' | head
sha256sum extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar
```
Build succeeded + a bundled index/assets entry is a **packaging gate**; not a MySQL/runtime/security pass. `-DskipTests` here is for operator packaging only; GitHub exact-head and full MySQL tests remain mandatory. The backend Maven POM already copies `extended/frontend/dist` to JAR static resources; no Dockerfile is required for this VM method.

If ARM Java/Node build fails, **STOP** and record the exact error; don't change Gradle/Maven/Java dependencies or switch to paid infrastructure merely to mask it.

## Stage 3 — disposable MySQL compatibility check *before* any import

From VM, use a compatible MySQL CLI to reach the **private IP** of MySQL.Free. Use its interactive password prompt, TLS and a private connection. Do not put passwords in shell history.

```sql
SELECT VERSION(), @@version_comment, @@sql_mode;
CREATE DATABASE orgresource_extended_local CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

Provision two schema-scoped DB identities in MySQL (names/examples only):
- `orgresource_migrator`: the narrowly necessary create/alter/drop/index/REFERENCES, routine/trigger and read/write permissions for Flyway V1–V7.
- `orgresource_runtime`: normal application DML, with `stock_movement` and `audit_event` **append-only** (`SELECT, INSERT` only), and only `SELECT` on `flyway_schema_history`.

The exact grants must be reviewed against migrations and actual MySQL.Free administrator capabilities. Test `SHOW GRANTS` for both accounts (redact names/secrets in public proof). Never give the normal runtime root/global privileges. **Do not point at any existing Course A, user-owned or shared schema.**

If migrations require privileges unavailable to the managed service or reject MySQL 9.x/8.4 behavior, STOP. The fallback is an independently tested supported MySQL server in a secured VM or a Northflank 8.0.46 free addon **only if quotas fit**. Do not silently downgrade/bypass Flyway.

## Stage 4 — runtime configuration without editing application code

Keep Java listening only to VM loopback. For a first **restricted staging** run, create `/etc/orgresource/extended.env` outside the checkout with root ownership and mode 0600; never commit or paste real values. Representative variable NAMES:
```ini
EXT_DB_URL=jdbc:mysql://PRIVATE_MYSQL_IP:3306/orgresource_extended_local?serverTimezone=UTC&sslMode=REQUIRED
EXT_DB_USER=orgresource_runtime
EXT_DB_PASSWORD=SET_PRIVATELY
EXT_DB_MIGRATION_URL=jdbc:mysql://PRIVATE_MYSQL_IP:3306/orgresource_extended_local?serverTimezone=UTC&sslMode=REQUIRED
EXT_DB_MIGRATION_USER=orgresource_migrator
EXT_DB_MIGRATION_PASSWORD=SET_PRIVATELY
EXT_SERVER_PORT=8085
EXT_SMS_ENABLED=false
EXT_REMINDER_MODE=DISABLED
SERVER_FORWARD_HEADERS_STRATEGY=framework
```
`sslMode=REQUIRED` encrypts traffic but does not validate host identity; where the managed-service CA and endpoint name are available, prefer `VERIFY_IDENTITY` with the verified trust chain. This remains an operator security check.

**DO NOT set `SPRING_PROFILES_ACTIVE=c1-test` for Internet-accessible HTTPS.** That profile deliberately configures an insecure cookie. Default profile must keep `__Host-ORGSESSION`, Secure, HttpOnly, SameSite Lax. Forwarded headers must be trusted only from your own same-host proxy, not from arbitrary Internet requests.

Place the built JAR under `/opt/orgresource/orgresource.jar` with a dedicated unprivileged `orgresource` system account. Set immutable JAR checksum and read-only file permissions for the runtime identity.

For persistent startup, a minimal `/etc/systemd/system/orgresource.service` can contain:
```ini
[Unit]
Description=OrgResource Extended Java 21 staging
After=network-online.target
Wants=network-online.target
[Service]
Type=simple
User=orgresource
Group=orgresource
WorkingDirectory=/opt/orgresource
EnvironmentFile=/etc/orgresource/extended.env
ExecStart=/usr/bin/java -Xms256m -Xmx1536m -jar /opt/orgresource/orgresource.jar
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
PrivateTmp=true
[Install]
WantedBy=multi-user.target
```
This is a **configuration example, not a verified deployed unit**. Confirm Java path, database TLS and system-user file privileges. Follow with `sudo systemctl daemon-reload`, `sudo systemctl enable --now orgresource`, `sudo systemctl status orgresource` and `sudo journalctl -u orgresource -n 80 --no-pager` (redact before sharing). Do not paste credential-bearing logs.

**One-time bootstrap:** Existing app supports `java -jar <JAR> --bootstrap` and reads `EXT_BOOTSTRAP_SLUG/ORG_NAME/USERNAME/DISPLAY_NAME/PASSWORD` from the process environment. It exits without web listener and prints one-use recovery codes. Perform **once** from a private terminal with disposable, strong fictional owner credentials (password at least 15 characters); store recovery codes privately and remove bootstrap password from persistent env afterward. Do not feed bootstrap secrets to GitHub Actions, systemd journal, cloud logs or screenshots.

Check local service only:
```bash
curl -i http://127.0.0.1:8085/api/v2/demo/metadata
sudo ss -ltnp | grep 8085
```
Expected: private loopback listener; metadata reports ordinary/synthetic classification based on actual DB state, not an assumed 200. Startup may fail if schema/Migrations/session table privileges differ — treat this as a blocking error.

## Stage 5 — protected HTTPS ingress without code changes

1. Reserve an authorized free DNS hostname (e.g., https://www.duckdns.org/; availability not guaranteed) and point its A record to VM public IP. A custom paid domain is optional.
2. Permit public TCP 80/443 on OCI VCN and VM firewall; **never** public TCP 8085 or 3306.
3. Keep Java bound to loopback and use Caddy as local reverse proxy. The proxy needs to pass accurate hostname and HTTPS scheme to the app to satisfy `C1SameOriginFilter` on POSTs.
4. To avoid an unrestricted public service while privacy/source rights remain open, configure Caddy `basic_auth` with a **unique bcrypt hash** produced using `caddy hash-password`. Do NOT put a plaintext password or hash in GitHub. Example (replace placeholders in private `/etc/caddy/Caddyfile`):
```caddyfile
DEMO_HOSTNAME {
    basic_auth {
        operator BCRYPT_HASH_GENERATED_LOCALLY
    }
    reverse_proxy 127.0.0.1:8085
}
```
Caddy version must support `basic_auth` (older packages use `basicauth`); validate with `sudo caddy validate --config /etc/caddy/Caddyfile` and `sudo systemctl reload caddy`. Automatic HTTPS needs working DNS and public 80/443. Never expose app login screens before access controls are confirmed.
5. **Mandatory first HTTPS browser test:** load the correct hostname, pass staging auth, sign into OrgResource, choose a fictional organization, submit a normal CSRF-protected POST, switch tenant, reload and log out. If POST gives `403 ORIGIN_INVALID`, inspect forwarded headers and `SERVER_FORWARD_HEADERS_STRATEGY=framework`, **do not disable origin or CSRF filters**. If secure session cookie is missing/blocked, inspect HTTPS and profile rather than turn Secure off.

Official Caddy docs: https://caddyserver.com/docs/automatic-https ; https://caddyserver.com/docs/caddyfile/directives/basic_auth ; https://caddyserver.com/docs/quick-starts/reverse-proxy .

## Stage 6 — non-optional staging smoke and maintenance

Confirm, with fictional accounts:
- Correct exact-source JAR, V1–V7 schema, URL and no application error on load.
- Login/session persistence, secure cookies, CSRF-protected POST, account recovery without leaking codes.
- OWNER/ADMIN/CUSTODIAN/MEMBER/assigned and unassigned LEAD expected role distinctions.
- Selected workspace v2 vs path-tenant v2 semantics and foreign-object 403/404.
- A meaningful stock receive/adjust with ETag, replay and row-level MySQL reconciliation.
- Borrower sees only own linked Allocation and can revoke own consent; no contact/provider exposure.
- App restarts via systemd; data remains in **persistent** managed DB; no one-time bootstrap rerun.
- Restore plan, external encrypted backup and controlled restore drill before real data (the existing `scripts/extended-c-backup.ps1` is expressly loopback/MySQL-client based and is **not automatically compatible with a remote managed DB**; one-day provider backup alone is not sufficient for durable production).
- CPU, RAM, DB connection count/storage, error logs, certificate renewal, DNS and actual free-tier billing checked. No uptime SLA.
- Public release authorization, academic materials rights, privacy review, actual end-user data permissions remain **BLOCKED** until independently cleared.

**If no compatible free managed DB or tested backup path exists, do not declare the system production-ready.** A restricted staging demo can still be a meaningful milestone with its limitations stated.

## Northflank alternative — shorter dashboard path, narrower resources

Official: https://northflank.com/pricing and https://northflank.com/docs/v1/application/billing/pricing-on-northflank

1. Sign up, verify current Developer Sandbox availability and payment-method requirement; do not assume an older no-card blog remains accurate.
2. Create a free project; add **one free MySQL addon** and explicitly select a verified MySQL **8.0.46** version if available under Sandbox.
3. Connect GitHub, select the **C8 feature branch**, not `main`. Use one app service; build frontend `extended/frontend` before packaging Java `extended/backend` by documented build settings. No Dockerfile presently exists; if Northflank cannot orchestrate this monorepo without a source change, STOP until Codex quota returns.
4. Provide private `EXT_DB_*`, `EXT_DB_MIGRATION_*`, `EXT_SERVER_PORT`, `SERVER_ADDRESS=0.0.0.0`, `SERVER_FORWARD_HEADERS_STRATEGY=framework`, reminder-disabled settings; use the Northflank internal MySQL address, not an Internet-exposed database.
5. Use platform-provided HTTPS URL, test secure cookie, Origin/CSRF and all essential workflows. Confirm allocated sandbox RAM/CPU and 24/7 memory stability (Spring Boot may be too large), build limits, addon persistence and actual zero-dollar billing.
6. Northflank itself says Sandbox **should not be used for production applications**. Treat it as private demo only.

## No-Codex STOP rules

Do not edit Java/React/SQL/migrations, turn off authentication, CSRF, origin or secure cookies, copy real user data, send live SMS, force-merge draft PRs, upgrade to a paid tier, or claim public production readiness. Keep a private log of each step actually run and its result. If one requirement cannot be met without code changes, stop at the last working verified staging state and record the exact blocking check.

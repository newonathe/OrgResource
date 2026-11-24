# 02 — Current local task scope and acceptance runbook

**Current authority (Report49, 2026-11-25 PHT):** R09 technical gate VERIFIED_BOUNDED; exact reviewed source `2c626cb251fc3e0a5e3304f9ea5b03f89f55e024`, draft PR56, full CI45494597345 and dedicated CI45494597313. Owner explicitly initiates local execution. Private/independent/host/student gates remain open. Historical text is preserved in the previous handoff commit `cef3b1d566d5d480156e114a1947089518022f0f`; current 09/11/12/45 supersede it. No native execution claimed.

**Execution candidate; not run by ChatGPT on the owner's machine.** Existing scripts are the source of truth; inspect full script before executing. Use PowerShell 7.4+ on Windows, Java 21, installed MySQL 8.0 client/server, Node 24, npm and Python (including pywinpty for protected-login operator drill). Check actual versions against .github/workflows/verify.yml and script requirements. Do not assume installed software, elevate privileges, change a service or install paid dependencies. Report missing prerequisite; continue independent tasks. Never call the Browser/Chrome plugin. CLI Playwright only.

## L00 — checkout, guard, environment and private inputs (deterministic)
Use the checked exact-source launch procedure in 11, not historical PR46 or a comparison requiring the older C5 script to remain byte-identical (authorized additive transport changes already occurred). Verify current graph/lock and pinned source blobs, frozen Course A and exact V1–V7 as in 11. After owner starts the clean detached worktree, inspect actual tool versions without dumping remote URLs or environment values:
```powershell
$PSVersionTable.PSVersion
java -version
node -v
npm -v
python --version
git --version
```
Check each native exit code separately. Use JAVA_HOME actually pointing to Java21 and MYSQL_HOME to local MySQL 8 binaries (including mysqld.exe/mysql.exe/mysqladmin.exe/mysqldump.exe/mysql_config_editor.exe); inspect versions using --version, do not connect to existing service. C6a requires Python import winpty; check module availability. Check free disk and memory without scanning private folders; use user-approved local private workspace outside repo, no OneDrive/cloud sync or public directory, owner-restricted ACL. Preserve existing untracked files. If no valid private path, skip private-dependent tasks only. Set SMS false/reminder DISABLED in child test process; never inherit hosted connection variables. Do not print environment. The task scripts create unique fresh disposable instances; never substitute an existing service, MySQL port 3306 or user-owned schema.

Private plan L00b: owner supplies absolute path to exact fictional-events-v1.local.json outside repo. Check actual byte length 19265850 and SHA256 3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c via Get-Item and Get-FileHash; case-normalize. Source SHA 57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2, flags synthetic=true scope LOCAL_ONLY_DRY_RUN. Counts 96/248/713/1312/2139/462/21060/67370 (org/dept/project/resource/requirement/persona/allocation/event). Do not regenerate, download lookalike or change newline. Existing Java preflight enforces approved bytes and aggregate IDs. Hash check is mandatory; no raw data pasted. If absent/mismatched, mark L04/L06 BLOCKED, continue other tasks.

## Primary order — package-first (superseding older sequence)

After L00, execute L03 once. Source scripts/package-extended-c.ps1 lines 61–96 actually extracts the committed source, runs the L01 verifier with -VerifyRecovery, then L02 operator drill on the extracted JAR. If both nested receipts pass, L01/L02 are covered by L03; do not rerun them in advance. See 11_NATIVE_EXECUTION_RECIPE.md for fresh worktree, log redirection, exact package/hash checks and verified JAR reuse for L04. L05 independent. If packaging fails, isolate first failure and use L01/L02 below only for missing required evidence.

## L01 — actual Windows C1/C5 recovery (fallback or nested receipt)
From exact clean tested source, run once:
```powershell
pwsh -NoProfile -File .\scripts\verify-extended-c1.ps1 -VerifyRecovery
```
Read full verifier + dot-sourced recovery script first. No -SkipFrontend. This includes local native build, real MySQL, original API/browser tests, C5 encrypted archive, session exclusion, wrong key/tamper, receipt replay, suspended/foreign role, contact key correctness, packaged app/browser after restore. Capture actual markers and exit, tested SHA, time and private output folder. A failed command is TEST_FAILED; no editing protected files. Existing historical C5 evidence can be cited as prior, not current proof. This run is Windows disposable-mode regression, not protected-login drill or off-host. If it fails, only continue independent tasks with validated JAR; otherwise L02/L04 depend on clean correct packaged JAR.

## L02 — protected-login operator drill (fallback or nested receipt; no external database)
```powershell
$jar = (Resolve-Path .\extended\backend\target\extended-backend-0.1.0-SNAPSHOT.jar).Path
pwsh -NoProfile -File .\scripts\verify-extended-c6a-operator.ps1 -JarPath $jar -Python python.exe
```
Verify actual script requirements, including Python pywinpty and correct client version. This creates its own isolated MySQL servers, private login paths and ephemeral secrets; never use actual protected login paths. Require actual positive protected-login restore, wrong/insufficient grants, genuinely partial import/refusal, new empty target recovery, original source intact, failed SQL/outage while app alive, recovery login/browser, over-limit source dump refused without archive, cleanup and no secret leakage. Prior C6a receipt is not current run. Record archive bytes/duration/exit privately. It does not prove 64 MiB full96 capacity or off-host transfer.

## L03 — primary combined clean local package, recovery and operator drill
Use a new nonexistent output directory under approved private parent, outside source checkout and with no reparse points, as required by package script. Read it in full; use existing filename convention and record legacy C6a naming as packaging scope risk, do not rename binaries or promise complete C8/C9 source coverage if script allowlist omits evidence. 
```powershell
pwsh -NoProfile -File .\scripts\package-extended-c.ps1 -Destination $NewPrivateReleaseDir -Python python.exe
```
Check exit, source SHA, JAR/source archive SHA256, manifests, actual zip entries against allowlist, extraction/build/first-run receipts if present. No upload ZIP/JAR automatically. Distinguish exact source identity from reproducible binary byte equality. Report missing modern packaging paths to ChatGPT; no broad rewrite. This package is not approved release.

## L04 — exact private 96 standalone local proof (existing script)
Require L00b and a verified JAR from L03 (or separately verified exact source build). L03 builds under extracted source, so copy exact verified JAR to fresh worktree's ignored target path only after manifest/hash and all V1–V7 archive checks; no overwrite, see 11. Use:
```powershell
$hash = '3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c'
pwsh -NoProfile -File .\scripts\verify-c8-demo-import.ps1 -Plan $PrivatePlanAbsolutePath -Sha256 $hash
```
Script uses native Windows MySQL and guarded fresh orgresource_c8_demo_<12hex> datadir under temp, independent restore, exact plan hash, all count/state/stock/requirement checks, refusal of repeated import, actual C5 AES-GCM with DisposableDemoAcceptance, 64 MiB raw size cap, SQL fingerprints. Keep proof folder private; it contains stopped disposable DB and encrypted archive. Record counts/states, movement count, raw dump bytes/time, archive bytes/time, import wall clock and cleanup. Do NOT call counts/four-table CRC equality full exact row equality; record limitation. The script's -ReuseProofFolder is for the same guarded proof, not an arbitrary folder or production clone. Only use it after verifying same source/plan and intentionally running restart proof. No full approved mixed metadata or hosted proof from this standalone run.

## L05 — private historical IR4 reconciliation and operator handback
If owner makes the restricted ledger available locally, deterministic parse and tally by original case ID; compare to historical 22,320 rows (199 executed passes, 48 reviewed exclusions, 22,073 unrun at IR4 snapshot). Never rewrite original labels/results. Join by stable route_id to 05_ROUTE_GAP_MAP.csv and bounded v1 contract; identify actual receipt/fixture evidence, missing cases, exact tested SHA, status/role/target/SQL/field match. Output only route-level counts/gap categories and public-safe IDs/links; private case evidence stays local. Do not rerun 22,320 cases by default. If private ledger absent, return BLOCKED. Independent reviewer signs separately; Claude execution is not sign-off.

## L06 — actual private full mixed rehearsal
Remote R06B engineering is VERIFIED_BOUNDED; final PR56 includes the reviewed runner and actual public controls. Local positive NOT_RUN/BLOCKED until L00B, L03, owner-approved POSIX environment and direct owner-terminal consent. Follow ONLY 11 and report27 current exact-source command contract. Native Windows is incompatible with POSIX baseline utility; do not bypass mode checks. Do not duplicate prep, copy private material without owner permission, or automate operator confirmation. L07 separately authorized off-host; L08 sanitized return for all tasks.

## L08 — return even if tasks blocked
Follow 07/08. No raw logs/plan/SQL/keys/archives/private case IDs or paths in public output. Keep test-owned cleanup separate from retained restricted proof; stop if cleanup unsafe. No host, merge, deployment, student action or later phase.
# 02 — Local-only sequential execution runbook (Windows)

**CURRENT DISPATCH AUTHORITY (2026-11-25 PHT, Report48):** R09 technical remote preparation is VERIFIED_BOUNDED; graph 12 and lock 45 ready=true at PR56 `2c626cb251fc3e0a5e3304f9ea5b03f89f55e024`, exact full CI45494597345 and dedicated45494597313 passed with actual logs. Owner explicitly initiates local queue and supplies authorized prerequisites; no automatic local action. Historical checkpoints below are retained as history, not instructions. Use 09/11, report27 L06 single guarded owner-terminal command; G01/G03/private/host/student/release gates remain open. No C8/C9 formal acceptance or deployment implied.

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

After L00, execute L03 once. Source scripts/package-extended-c.ps1 lines 73–96 actually extracts the committed source, runs the L01 verifier with -VerifyRecovery, then L02 operator drill on the extracted JAR. If both nested receipts pass, L01/L02 are covered by L03; do not rerun them in advance. See 11_NATIVE_EXECUTION_RECIPE.md for fresh worktree, log redirection, exact package/hash checks and verified JAR reuse for L04. L05 independent. If packaging fails, isolate first failure and use L01/L02 below only for missing required evidence.

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

## L06 — full-96 approved mixed additive rehearsal: CHAT-PREPARED HARNESS REQUIRED
This is a distinct test, not L04 or #26 live import. **Superseding compatibility gate:** 13_MIXED_REHEARSAL_COMPATIBILITY_GATE.md. Existing original-data baseline is POSIX-only; native Windows path is not accepted. ChatGPT must prepare and review the Linux/POSIX local runner and compatible actual C5 transport before execution. Do not improvise or fabricate operator attestation. Do not use production variables, pretend a CI-only env, relax C5 schema guards, or manually target a pre-existing service. ChatGPT must supply a reviewed guarded script before Claude runs it. Exact design and required oracles are in 03_E2E_OWNERSHIP.md. Must use exact private file, separate disposable prepopulated fictional original fixture (owner, second original org, related records and immutable history), independently restored fresh target before import; actual C5 protected login, exact target fingerprint and V2 HMAC CAPTURE/VERIFY, independent SELECT-only postflight and valid mixed metadata 200. If no script/authorization, mark BLOCKED and continue other tasks. No invented restore attestation or repeated partial target writes. Actual Northflank clone and off-host proof remain operator-gated, separate.

## L08 — local-only evidence after execution (L07 off-host remains operator-only)
Provide private receipt file to owner, sanitized aggregate summary here. Check actual child processes stopped; remove only test-owned temporary secrets as script authorizes, retain protected local proof as needed. Record failed cleanup separately. Never upload raw plan, local JAR/source ZIP, MySQL dump, screenshots revealing sensitive data, secrets, archive/key or private IR4 data. Do not send SMS. No actual deployment or merge. Return all unresolved issues to ChatGPT.

**Launch gate:** R09 in 12_EXECUTION_GRAPH.json must be explicitly ready from ChatGPT; all older hardcoded baseline commands are examples for their pinned revision, not authority to run stale code. L08 reports every task including blocked inputs/owner actions.

R06A now has a test-first source candidate on #48; R06B still requires the actual executable local runner and full-96 post-import recovery support. Do not run scripts/verify-c9-local-original-transport.sh on the owner's machine: it is a public synthetic CI test, not L06. See 13 and 12. Native package-first L03/L04 unchanged.

## Superseding continuation checkpoint (2026-11-24 PHT)

R01 PR46 b62ff3c6 exact CI #45425950575 and R06A PR48 c9133376 exact CI #45427270775 are VERIFIED BOUNDED with actual logs inspected. R06B1 PR49 406ce2af CI #45430758956 pending actual receipts. The canonical cross-chat protocol is 14_CONTINUE_PRE_CLAUDE.md (GitHub issue #50), dependency state is 12_EXECUTION_GRAPH.json and historical receipts are 15_CONTINUATION_LEDGER.jsonl. Older dated statuses are historical. R09 remains false; no Claude launch, private plan import, operator attestation, host, merge or deployment. Independent review open.

PR49 `scripts/verify-c9-local-mixed-preparation.sh` has `--fixture-only PRIVATE_ROOT SOURCE_SHA JAR JAR_SHA256` and `--approved-plan PRIVATE_ROOT SOURCE_SHA JAR JAR_SHA256 PRIVATE_PLAN`. Both are ephemeral preparation-only; approved mode does exact private hash and existing Java read-only preflight, not import. The test CI wrapper never uses private plan; exact-size plan refused in public CI. Final L06 must perform same actual recovery/preflight in same guarded lifetime before explicit operator confirmation, then import/postflight and full recovery/UI. Do not run standalone prep again if integrated runner covers it. Not a launch command; wait for R06B2/B3 and R09.

## Superseding checkpoint — 2026-11-25 PHT

R06B1 PR49 406ce2af exact CI #45430758956 / Extended job 138575101070 VERIFIED BOUNDED: 7 refusal cases, actual fresh bootstrap, C5 archive 33,858 bytes, independent exact row restore, read-only V2 baseline and cleanup, 3/3 jobs, 91 Maven/19 Playwright. R06B2 PR51 17bd5bf0 exact CI #45434204255 in progress, source-only candidate; no actual private import or operator attestation. Current graph 12, continuation 14 and ledger 15 supersede older status text. R09 false; no local launch, hosted operation, merge/deploy or independent self-signoff.

R06B2 extends `scripts/verify-c9-local-mixed-preparation.sh` with `--approved-import PRIVATE_ROOT SOURCE_SHA JAR JAR_SHA256 PRIVATE_PLAN`. This is **interim source only, not a released local command**. The final R06B3 runner must integrate full post-import actual C5 recovery/browser in the same lifetime. Never duplicate original bootstrap/archive/restore or run this intermediate import in advance. A direct owner terminal (not agent auto-input) is mandatory; only the owner may review actual local restoration receipt and type the exact fingerprint/digest-bound confirmation. Public CI refuses import before plan access. Exact private plan stays local; source/JAR/archive/plan/baseline/rows rechecked after approval. Actual existing importer once, independent SELECT-only V2 and postflight, repeat preflight refusal, no retry on partial state; restricted receipt/logs only in owner private scope. Terminal presence is not proof of human identity; no generated confirmation or unattended automation is authorized. No actual full private runtime or independent review yet.

## Superseding checkpoint — R06B3a, 2026-11-25 PHT

R06B2 PR51 17bd5bf0 exact CI 45434204255 / Extended job 138587448867 VERIFIED BOUNDED: actual confirmation matching and CI refusal, 8 precheck refusals, actual restricted MySQL grants, original encrypted C5/V2/cleanup, 91 Maven/19 Playwright and all 3 jobs. Actual owner consent/private full-plan positive NOT_RUN. R06B3a PR52 d3e8a8550e00e3bb9423d84f51f8141783e71fd1 exact CI 45437263264 pending actual receipts, adds separate LocalMixedAcceptance C5 mode, exact approved plan private, third independently initialized server, exact row equality, independent restored postflight, size/timing and cleanup. Public CI refuses full mixed backup/restore and tests third EMPTY server; 10 prechecks expected. Actual full cohort positive remains NOT_RUN. R06B3b valid recovered normal browser/API remains open. Graph 12 (33 nodes), 14, 15 and project-wide registry supersede older status. No R09 release, no Claude launch, no host/private input upload/merge/deploy, independent review open.

Final L06 must use one guarded lifetime (three test-owned servers, first original source, restored-and-imported source, third restored mixed target). Exact approved input and owner confirmation remain required. C5 separate LocalMixedAcceptance only after actual independent postflight, distinct restore->mixed schema/container pair, approved cohort and original pair/owner, empty sessions, exact archive digest and owner mode0600, unchanged 64MiB cap. No spoofing GitHub Actions, no synthetic lookalike full cohort, no elevated runtime/host endpoint. Never reuse original V2 fingerprint on third server or recapture post-import; verified originals carried by exact rows and independent restored SQL postflight. Do not run interim --approved-import separately: it currently stops before normal browser. R06B3b must extend this same lifetime. On size/timeout/failure stop and preserve sanitized status; no partial-target retry or raised cap. No local platform or full positive proof yet.

## Superseding L06 command contract (source-reviewed; still blocked)

See [report27](27_R06B3B_AGGREGATE_RECEIPT_AND_L06_COMMAND_CONTRACT.md). The integrated same-lifetime command is now source-implemented; final exact-head CI and R09 authorization remain mandatory. Do not run interim preparation separately, automate confirmation, or use a private positive in CI. Final implementation SHA must be pinned at R09; L06 remains NOT_RUN/BLOCKED. Windows L03/L04 unaffected.

## Remote gate checkpoint — R06B VERIFIED_BOUNDED

Report28 records actual final PR53 CI45475369786. Integrated L06 runner remote engineering complete within public scope; still BLOCKED pending final R09 SHA, local prerequisites and direct owner consent. Report27 command contract remains authoritative; no actual private positive or independent acceptance.

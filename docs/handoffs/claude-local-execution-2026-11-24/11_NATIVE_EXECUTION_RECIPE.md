# 11 — Native execution recipe (minimal model turns, package-first)

**CURRENT DISPATCH AUTHORITY (2026-11-25 PHT, Report48):** R09 technical remote preparation is VERIFIED_BOUNDED; graph 12 and lock 45 ready=true at PR56 `2c626cb251fc3e0a5e3304f9ea5b03f89f55e024`, exact full CI45494597345 and dedicated45494597313 passed with actual logs. Owner explicitly initiates local queue and supplies authorized prerequisites; no automatic local action. Historical checkpoints below are retained as history, not instructions. Use 09/11, report27 L06 single guarded owner-terminal command; G01/G03/private/host/student/release gates remain open. No C8/C9 formal acceptance or deployment implied.

**Current mandatory gate (supersedes historical examples below):** Read `45_LOCAL_DISPATCH_SOURCE_LOCK.json` and `12_EXECUTION_GRAPH.json` from the live handoff branch. Both must be ready and the lock must provide a non-null approved exact source SHA authorized by the owner. Candidate SHA is not execution authority. Verify the pinned source blobs and seven migrations; no historical checkout, no native task while R09=false. L06 uses report27 single owner-terminal command. Native Windows L03/L04 require owner-approved exclusive disposable environment with no unrelated MySQL servers/listeners or competing launches; if custody/identity cannot be established, BLOCKED. L04 encrypted restore proves four-table fingerprints and allocation count, not full all-row equality. Separate R04 temporary packaging tools reference remains outside Extended source. No independent approval implied.

This is a command runbook for a **fresh, clean, detached source worktree at the reviewed implementation SHA**, and the separately fetched handoff. It is not permission automation or a new tested script. Native commands use existing repository guard logic; no fake CI env, global config changes, elevated privileges, or broad script/PowerShell wildcard permissions. The owner explicitly supplies a private, locally stored, restricted directory (not inside a Git tree, shared/cloud-sync/UNC path or reparse-point tree) and optional exact private plan/IR4 path. Do not search unrelated personal files. Keep raw logs private; terminal output may be retained by Claude. Do not import or attach private logs to Claude memory. Verify native exits and actual receipt markers locally; no copy/paste of sensitive output.

## One small launch from the owner's repository

Owner verifies live PR56 and actual full/dedicated CI before launch. Use exact checked handoff SHA and source lock; never use historical PR46 or a latest-branch assumption. Keep existing worktree untouched and private root separate. The following source-reviewed PowerShell recipe is not a claim of native execution; inspect errors and stop before any task if check fails. Do not paste owner paths or git credential diagnostics into public receipts.

```powershell
# Run only after owner explicitly initiates the local queue; from the trusted existing repo.
# $NewSourceWorktree is an owner-selected absolute, nonexistent path outside private output.
function Invoke-CheckedGit {
    $result = & git @args
    if ($LASTEXITCODE -ne 0) { throw 'Git command failed; inspect safe diagnostics locally.' }
    $result
}
$origin = (Invoke-CheckedGit config --get remote.origin.url) -join ''
if ($origin -cnotin @('https://github.com/newonathe/OrgResource.git','https://github.com/newonathe/OrgResource','git@github.com:newonathe/OrgResource.git','git@github.com:newonathe/OrgResource')) { throw 'Unexpected origin.' }
$packet = 'docs/handoffs/claude-local-execution-2026-11-24'
Invoke-CheckedGit fetch origin refs/heads/docs/claude-local-execution-handoff-2026-11-24
$HandoffSha = (Invoke-CheckedGit rev-parse FETCH_HEAD) -join ''
$Gate = ((Invoke-CheckedGit show "${HandoffSha}:$packet/12_EXECUTION_GRAPH.json") -join "`n") | ConvertFrom-Json
$Dispatch = ((Invoke-CheckedGit show "${HandoffSha}:$packet/45_LOCAL_DISPATCH_SOURCE_LOCK.json") -join "`n") | ConvertFrom-Json
$FinalSourceSha = $Dispatch.approved_source_sha
if ($Gate.ready_to_launch -ne $true -or $Dispatch.ready_to_launch -ne $true -or $FinalSourceSha -cnotmatch '^[a-f0-9]{40}$' -or $Gate.verified_source.sha -cne $FinalSourceSha -or $Dispatch.candidate_source_sha -cne $FinalSourceSha -or $Dispatch.repository -cne 'newonathe/OrgResource' -or ($Gate.tasks | Where-Object id -eq 'R09').state -cne 'VERIFIED_BOUNDED') { throw 'R09 technical gate or source lock mismatch.' }
Invoke-CheckedGit fetch origin refs/heads/test/r09-integrated-evidence-candidate-2026-11-25
if (((Invoke-CheckedGit rev-parse FETCH_HEAD) -join '') -cne $FinalSourceSha) { throw 'Live implementation ref changed; return to ChatGPT.' }
if (((Invoke-CheckedGit rev-parse "${FinalSourceSha}^{tree}") -join '') -cne $Dispatch.approved_source_tree) { throw 'Source tree mismatch.' }
foreach ($entry in @($Dispatch.source_files) + @($Dispatch.migrations)) {
    $actual = (Invoke-CheckedGit ls-tree $FinalSourceSha -- $entry.path) -join ''
    $expected = '{0} blob {1}' -f $entry.mode, $entry.blob
    $expected += "`t" + $entry.path
    if ($actual -cne $expected) { throw 'Pinned source blob/mode mismatch.' }
}
if ($Dispatch.source_files.Count -ne 16 -or $Dispatch.migrations.Count -ne 7) { throw 'Source lock inventory mismatch.' }
Invoke-CheckedGit diff --exit-code 7f8c0746da18fd54b47f430ac1435ac36e605d67 $FinalSourceSha -- backend frontend
Invoke-CheckedGit diff --exit-code 45bcdfc7285598de3fe98684448aa65b5dd47e12 $FinalSourceSha -- extended/backend/src/main/resources/db/migration
# Review actual exact CI runs (full 45494597345, dedicated 45494597313) and live PR56 before proceeding.
if ([string]::IsNullOrWhiteSpace($NewSourceWorktree) -or ![IO.Path]::IsPathFullyQualified($NewSourceWorktree) -or (Test-Path -LiteralPath $NewSourceWorktree)) { throw 'Owner must choose a fresh absolute source path.' }
Invoke-CheckedGit worktree add --detach $NewSourceWorktree $FinalSourceSha
if (((Invoke-CheckedGit -C $NewSourceWorktree rev-parse HEAD) -join '') -cne $FinalSourceSha) { throw 'Detached checkout mismatch.' }
if (@(Invoke-CheckedGit -C $NewSourceWorktree status --porcelain --untracked-files=all).Count -ne 0) { throw 'Fresh checkout is not clean.' }
```

Run Claude Code interactively in the new source worktree with the short launch prompt (09). Read AGENTS/QA mandatory policy; no duplicated plan-mode design. In the handoff docs worktree, use selective deterministic context retrieval; do not paste whole manifest or CSV into agent context. The exact refs are in 04. For example:
```powershell
$packet = 'docs/handoffs/claude-local-execution-2026-11-24'
# In the handoff worktree (or resolve this directory explicitly):
$index = Get-Content -Raw (Join-Path $packet '04_CONTEXT_INDEX.json') | ConvertFrom-Json
$index.context | ForEach-Object { [pscustomobject]@{ Stage=$_.stage; Ref=$_.ref; Files=$_.files.Count } }
# Verify one selected file's actual blob at the recorded ref; read only if needed.
$group = $index.context | Where-Object stage -eq 'mandatory routing source'
$entry = $group.files | Where-Object path -eq 'docs/execution/12_TOKEN_BUDGETED_SUBAGENT_ROUTING.md'
git rev-parse "$($group.ref):$($entry.path)"
git show "$($group.ref):$($entry.path)"
Import-Csv (Join-Path $packet '05_ROUTE_GAP_MAP.csv') | Where-Object route_id -eq 'R038' |
  Select-Object route_id,method,path,remaining_oracle_gap_from_source
```

Check each native exit. Do not load the 778-blob inventory or 93 route rows as conversational context; use CSV for exact targeted lookups. If pinned reference missing, fetch corresponding public branch/PR and verify the SHA before reading. No merges.

## Run package once (L03 covers L01 + L02)

Complete L00, verify clean source, real toolchain, no inherited live connection configuration, owner-approved private output folder, new release directory absent. Use child process / dedicated terminal with environment scrubbed of hosted DB/provider/production settings (check variable names without printing values; keep only needed trusted toolchain and OS paths). No credential values in commands or prompts. Set SMS false and reminders DISABLED explicitly. Set TEMP and TMP to an approved private local directory for that child environment; existing scripts create guarded test-only DB dirs beneath it. Do not claim process isolation against a hostile local OS; if owner cannot establish private directory/access scope, mark BLOCKED.

```powershell
$env:EXT_SMS_ENABLED = 'false'
$env:EXT_REMINDER_MODE = 'DISABLED'
# $NewPrivateReleaseDir: nonexistent child of owner-approved local private parent.
# $PrivatePackageLog: fresh file outside repository, inside same private scope.
pwsh -NoProfile -File .\scripts\package-extended-c.ps1 -Destination $NewPrivateReleaseDir -Python python.exe *> $PrivatePackageLog
$PackageExit = $LASTEXITCODE
"PACKAGE_EXIT=$PackageExit"
```

If zero, require actual nested receipts from local VERIFICATION.log: full C1/C5 connected recovery and C6a protected-login operator drill; actual release READY.json, RELEASE-MANIFEST.json, SHA256SUMS.txt and JAR/source ZIP matching all recorded digests. Verify log completion by inspecting exact script terminal markers and intermediate requirements. Keep raw output local, no dumping into agent. Credit L01/L02 as COVERED_BY_L03 only with their own verified nested receipts. If nonzero, identify first failed phase from private log; do not execute entire duplicate suite automatically. Only independently missing test may be run in a fresh safe scope using L01/L02 commands in 02. Retain failed/staging evidence privately; don't use a partial release's JAR without separate verified provenance.

## Use exact verified JAR for L04 (no rebuild)

Package script names JAR OrgResource-Extended-C6a-Java21.jar. Source archive includes extended but not all C8/C9 support evidence. Verify exact JAR source SHA from manifest, file SHA256, expected static assets and **all seven migration files** against current reviewed committed blobs; package's existing loop checks only V1–V5. Example read-only Python verification: use zipfile to identify BOOT-INF/classes/db/migration/V1__... through V7__...; enumerate tracked migration paths via git ls-tree, get exact blob bytes via subprocess.check_output(['git','cat-file','blob', '<sha>:<path>']); require each unique archive entry bytes equal tracked blob, no extra migration version and exact count seven. Run on verified source worktree, never dump bytes. If mismatch, return BLOCKED. Record package's limited historical allowlist (not a full Extended C8/C9 release).

Only in the newly created detached worktree, ensure target path extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar does not already exist. Create ignored target directory if needed, copy the verified JAR there without overwriting, compare SHA256 before/after, recheck source tracked tree clean. L04's existing script resolves that exact path. No rebuilt JAR or arbitrary previous target artifact. Then if private approved file hash and size match:
```powershell
pwsh -NoProfile -File .\scripts\verify-c8-demo-import.ps1 -Plan $PrivatePlanAbsolutePath -Sha256 '3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c' *> $PrivateDemoLog
$DemoExit = $LASTEXITCODE
"DEMO_EXIT=$DemoExit"
```

L05 private IR4 only if provided; local deterministic join/tallies, no blind rerun. L06 full approved mixed rehearsal remains blocked until ChatGPT's reviewed POSIX-compatible actual-C5 guarded runner and operator restore attestation (13). Do not run Windows-only baseline, spoof GitHub Actions, or change permissions/code to bypass the baseline. Off-host proof separate operator gate. Return summary 08. Continue independent work if input missing; do not try to fix remote code here. No guarantee of permission-free, quota-free or error-free execution.

## Usage and resume
Before first task, use installed CLI's available usage/context indicators if supported; no new telemetry packages. During long shell execution, no repeated LLM polling or log reads. Let native process complete, record exit and safe receipts. Save a tiny local checkpoint outside repo after each completed/failed/blocked task: task ID, exact SHA, evidence status, elapsed seconds, next task; no raw credentials or plan. If context compaction is needed, preserve these, protected boundaries, known blockers and current task; use actual installed CLI support. On limit/interruption, resume from checkpoint and inspect prior verified outputs, never blindly rerun. Do not change account/model or enable extra paid usage without owner action.

R06A candidate PR48 adds local original-only Linux transport plus a GitHub Actions test. This is not a local execution recipe for L06. Do not run CI-only script or bypass its guard. The final R06B local runner must be pinned in 02/04/12 after verified acceptance; do not improvise.

## Superseding continuation checkpoint (2026-11-24 PHT)

R01 PR46 b62ff3c6 exact CI #45425950575 and R06A PR48 c9133376 exact CI #45427270775 are VERIFIED BOUNDED with actual logs inspected. R06B1 PR49 406ce2af CI #45430758956 pending actual receipts. The canonical cross-chat protocol is 14_CONTINUE_PRE_CLAUDE.md (GitHub issue #50), dependency state is 12_EXECUTION_GRAPH.json and historical receipts are 15_CONTINUATION_LEDGER.jsonl. Older dated statuses are historical. R09 remains false; no Claude launch, private plan import, operator attestation, host, merge or deployment. Independent review open.

R06A POSIX proof is actual GitHub Linux, not owner WSL. PR49 preparation-only runner is not final L06. Final integrated runner must reuse the verified JAR and perform same-lifetime preparation, operator approval, actual importer/postflight and full mixed C5/UI. No separate duplicate prep or Windows-only HMAC bypass. Await R09.

## Superseding checkpoint — 2026-11-25 PHT

R06B1 PR49 406ce2af exact CI #45430758956 / Extended job 138575101070 VERIFIED BOUNDED: 7 refusal cases, actual fresh bootstrap, C5 archive 33,858 bytes, independent exact row restore, read-only V2 baseline and cleanup, 3/3 jobs, 91 Maven/19 Playwright. R06B2 PR51 17bd5bf0 exact CI #45434204255 in progress, source-only candidate; no actual private import or operator attestation. Current graph 12, continuation 14 and ledger 15 supersede older status text. R09 false; no local launch, hosted operation, merge/deploy or independent self-signoff.

R06B2 extends `scripts/verify-c9-local-mixed-preparation.sh` with `--approved-import PRIVATE_ROOT SOURCE_SHA JAR JAR_SHA256 PRIVATE_PLAN`. This is **interim source only, not a released local command**. The final R06B3 runner must integrate full post-import actual C5 recovery/browser in the same lifetime. Never duplicate original bootstrap/archive/restore or run this intermediate import in advance. A direct owner terminal (not agent auto-input) is mandatory; only the owner may review actual local restoration receipt and type the exact fingerprint/digest-bound confirmation. Public CI refuses import before plan access. Exact private plan stays local; source/JAR/archive/plan/baseline/rows rechecked after approval. Actual existing importer once, independent SELECT-only V2 and postflight, repeat preflight refusal, no retry on partial state; restricted receipt/logs only in owner private scope. Terminal presence is not proof of human identity; no generated confirmation or unattended automation is authorized. No actual full private runtime or independent review yet.

Windows L03 package-first unchanged. POSIX L06 uses the exact verified JAR and final reviewed integrated runner, with the mandatory owner-terminal checkpoint. Claude prepares prerequisites, owner executes confirmation, Claude resumes sanitized result collection. Do not promise unattended one-shot approval.

## Superseding checkpoint — R06B3a, 2026-11-25 PHT

R06B2 PR51 17bd5bf0 exact CI 45434204255 / Extended job 138587448867 VERIFIED BOUNDED: actual confirmation matching and CI refusal, 8 precheck refusals, actual restricted MySQL grants, original encrypted C5/V2/cleanup, 91 Maven/19 Playwright and all 3 jobs. Actual owner consent/private full-plan positive NOT_RUN. R06B3a PR52 d3e8a8550e00e3bb9423d84f51f8141783e71fd1 exact CI 45437263264 pending actual receipts, adds separate LocalMixedAcceptance C5 mode, exact approved plan private, third independently initialized server, exact row equality, independent restored postflight, size/timing and cleanup. Public CI refuses full mixed backup/restore and tests third EMPTY server; 10 prechecks expected. Actual full cohort positive remains NOT_RUN. R06B3b valid recovered normal browser/API remains open. Graph 12 (33 nodes), 14, 15 and project-wide registry supersede older status. No R09 release, no Claude launch, no host/private input upload/merge/deploy, independent review open.

Final L06 must use one guarded lifetime (three test-owned servers, first original source, restored-and-imported source, third restored mixed target). Exact approved input and owner confirmation remain required. C5 separate LocalMixedAcceptance only after actual independent postflight, distinct restore->mixed schema/container pair, approved cohort and original pair/owner, empty sessions, exact archive digest and owner mode0600, unchanged 64MiB cap. No spoofing GitHub Actions, no synthetic lookalike full cohort, no elevated runtime/host endpoint. Never reuse original V2 fingerprint on third server or recapture post-import; verified originals carried by exact rows and independent restored SQL postflight. Do not run interim --approved-import separately: it currently stops before normal browser. R06B3b must extend this same lifetime. On size/timeout/failure stop and preserve sanitized status; no partial-target retry or raised cap. No local platform or full positive proof yet.

## Superseding L06 command contract (source-reviewed; still blocked)

See [report27](27_R06B3B_AGGREGATE_RECEIPT_AND_L06_COMMAND_CONTRACT.md). The integrated same-lifetime command is now source-implemented; final exact-head CI and R09 authorization remain mandatory. Do not run interim preparation separately, automate confirmation, or use a private positive in CI. Final implementation SHA must be pinned at R09; L06 remains NOT_RUN/BLOCKED. Windows L03/L04 unaffected.

## Remote gate checkpoint — R06B VERIFIED_BOUNDED

Report28 records actual final PR53 CI45475369786. Integrated L06 runner remote engineering complete within public scope; still BLOCKED pending final R09 SHA, local prerequisites and direct owner consent. Report27 command contract remains authoritative; no actual private positive or independent acceptance.
) { throw 'R09 not authorized; no checkout or local execution.' }
$FinalSourceSha = $Dispatch.approved_source_sha
# Owner authorization, actual full CI and exact source/blob validation are mandatory before this command.
git worktree add --detach $NewSourceWorktree $FinalSourceSha
```

Run Claude Code interactively in the new source worktree with the short launch prompt (09). Read AGENTS/QA mandatory policy; no duplicated plan-mode design. In the handoff docs worktree, use selective deterministic context retrieval; do not paste whole manifest or CSV into agent context. The exact refs are in 04. For example:
```powershell
$packet = 'docs/handoffs/claude-local-execution-2026-11-24'
# In the handoff worktree (or resolve this directory explicitly):
$index = Get-Content -Raw (Join-Path $packet '04_CONTEXT_INDEX.json') | ConvertFrom-Json
$index.context | ForEach-Object { [pscustomobject]@{ Stage=$_.stage; Ref=$_.ref; Files=$_.files.Count } }
# Verify one selected file's actual blob at the recorded ref; read only if needed.
$group = $index.context | Where-Object stage -eq 'mandatory routing source'
$entry = $group.files | Where-Object path -eq 'docs/execution/12_TOKEN_BUDGETED_SUBAGENT_ROUTING.md'
git rev-parse "$($group.ref):$($entry.path)"
git show "$($group.ref):$($entry.path)"
Import-Csv (Join-Path $packet '05_ROUTE_GAP_MAP.csv') | Where-Object route_id -eq 'R038' |
  Select-Object route_id,method,path,remaining_oracle_gap_from_source
```

Check each native exit. Do not load the 778-blob inventory or 93 route rows as conversational context; use CSV for exact targeted lookups. If pinned reference missing, fetch corresponding public branch/PR and verify the SHA before reading. No merges.

## Run package once (L03 covers L01 + L02)

Complete L00, verify clean source, real toolchain, no inherited live connection configuration, owner-approved private output folder, new release directory absent. Use child process / dedicated terminal with environment scrubbed of hosted DB/provider/production settings (check variable names without printing values; keep only needed trusted toolchain and OS paths). No credential values in commands or prompts. Set SMS false and reminders DISABLED explicitly. Set TEMP and TMP to an approved private local directory for that child environment; existing scripts create guarded test-only DB dirs beneath it. Do not claim process isolation against a hostile local OS; if owner cannot establish private directory/access scope, mark BLOCKED.

```powershell
$env:EXT_SMS_ENABLED = 'false'
$env:EXT_REMINDER_MODE = 'DISABLED'
# $NewPrivateReleaseDir: nonexistent child of owner-approved local private parent.
# $PrivatePackageLog: fresh file outside repository, inside same private scope.
pwsh -NoProfile -File .\scripts\package-extended-c.ps1 -Destination $NewPrivateReleaseDir -Python python.exe *> $PrivatePackageLog
$PackageExit = $LASTEXITCODE
"PACKAGE_EXIT=$PackageExit"
```

If zero, require actual nested receipts from local VERIFICATION.log: full C1/C5 connected recovery and C6a protected-login operator drill; actual release READY.json, RELEASE-MANIFEST.json, SHA256SUMS.txt and JAR/source ZIP matching all recorded digests. Verify log completion by inspecting exact script terminal markers and intermediate requirements. Keep raw output local, no dumping into agent. Credit L01/L02 as COVERED_BY_L03 only with their own verified nested receipts. If nonzero, identify first failed phase from private log; do not execute entire duplicate suite automatically. Only independently missing test may be run in a fresh safe scope using L01/L02 commands in 02. Retain failed/staging evidence privately; don't use a partial release's JAR without separate verified provenance.

## Use exact verified JAR for L04 (no rebuild)

Package script names JAR OrgResource-Extended-C6a-Java21.jar. Source archive includes extended but not all C8/C9 support evidence. Verify exact JAR source SHA from manifest, file SHA256, expected static assets and **all seven migration files** against current reviewed committed blobs; package's existing loop checks only V1–V5. Example read-only Python verification: use zipfile to identify BOOT-INF/classes/db/migration/V1__... through V7__...; enumerate tracked migration paths via git ls-tree, get exact blob bytes via subprocess.check_output(['git','cat-file','blob', '<sha>:<path>']); require each unique archive entry bytes equal tracked blob, no extra migration version and exact count seven. Run on verified source worktree, never dump bytes. If mismatch, return BLOCKED. Record package's limited historical allowlist (not a full Extended C8/C9 release).

Only in the newly created detached worktree, ensure target path extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar does not already exist. Create ignored target directory if needed, copy the verified JAR there without overwriting, compare SHA256 before/after, recheck source tracked tree clean. L04's existing script resolves that exact path. No rebuilt JAR or arbitrary previous target artifact. Then if private approved file hash and size match:
```powershell
pwsh -NoProfile -File .\scripts\verify-c8-demo-import.ps1 -Plan $PrivatePlanAbsolutePath -Sha256 '3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c' *> $PrivateDemoLog
$DemoExit = $LASTEXITCODE
"DEMO_EXIT=$DemoExit"
```

L05 private IR4 only if provided; local deterministic join/tallies, no blind rerun. L06 full approved mixed rehearsal remains blocked until ChatGPT's reviewed POSIX-compatible actual-C5 guarded runner and operator restore attestation (13). Do not run Windows-only baseline, spoof GitHub Actions, or change permissions/code to bypass the baseline. Off-host proof separate operator gate. Return summary 08. Continue independent work if input missing; do not try to fix remote code here. No guarantee of permission-free, quota-free or error-free execution.

## Usage and resume
Before first task, use installed CLI's available usage/context indicators if supported; no new telemetry packages. During long shell execution, no repeated LLM polling or log reads. Let native process complete, record exit and safe receipts. Save a tiny local checkpoint outside repo after each completed/failed/blocked task: task ID, exact SHA, evidence status, elapsed seconds, next task; no raw credentials or plan. If context compaction is needed, preserve these, protected boundaries, known blockers and current task; use actual installed CLI support. On limit/interruption, resume from checkpoint and inspect prior verified outputs, never blindly rerun. Do not change account/model or enable extra paid usage without owner action.

R06A candidate PR48 adds local original-only Linux transport plus a GitHub Actions test. This is not a local execution recipe for L06. Do not run CI-only script or bypass its guard. The final R06B local runner must be pinned in 02/04/12 after verified acceptance; do not improvise.

## Superseding continuation checkpoint (2026-11-24 PHT)

R01 PR46 b62ff3c6 exact CI #45425950575 and R06A PR48 c9133376 exact CI #45427270775 are VERIFIED BOUNDED with actual logs inspected. R06B1 PR49 406ce2af CI #45430758956 pending actual receipts. The canonical cross-chat protocol is 14_CONTINUE_PRE_CLAUDE.md (GitHub issue #50), dependency state is 12_EXECUTION_GRAPH.json and historical receipts are 15_CONTINUATION_LEDGER.jsonl. Older dated statuses are historical. R09 remains false; no Claude launch, private plan import, operator attestation, host, merge or deployment. Independent review open.

R06A POSIX proof is actual GitHub Linux, not owner WSL. PR49 preparation-only runner is not final L06. Final integrated runner must reuse the verified JAR and perform same-lifetime preparation, operator approval, actual importer/postflight and full mixed C5/UI. No separate duplicate prep or Windows-only HMAC bypass. Await R09.

## Superseding checkpoint — 2026-11-25 PHT

R06B1 PR49 406ce2af exact CI #45430758956 / Extended job 138575101070 VERIFIED BOUNDED: 7 refusal cases, actual fresh bootstrap, C5 archive 33,858 bytes, independent exact row restore, read-only V2 baseline and cleanup, 3/3 jobs, 91 Maven/19 Playwright. R06B2 PR51 17bd5bf0 exact CI #45434204255 in progress, source-only candidate; no actual private import or operator attestation. Current graph 12, continuation 14 and ledger 15 supersede older status text. R09 false; no local launch, hosted operation, merge/deploy or independent self-signoff.

R06B2 extends `scripts/verify-c9-local-mixed-preparation.sh` with `--approved-import PRIVATE_ROOT SOURCE_SHA JAR JAR_SHA256 PRIVATE_PLAN`. This is **interim source only, not a released local command**. The final R06B3 runner must integrate full post-import actual C5 recovery/browser in the same lifetime. Never duplicate original bootstrap/archive/restore or run this intermediate import in advance. A direct owner terminal (not agent auto-input) is mandatory; only the owner may review actual local restoration receipt and type the exact fingerprint/digest-bound confirmation. Public CI refuses import before plan access. Exact private plan stays local; source/JAR/archive/plan/baseline/rows rechecked after approval. Actual existing importer once, independent SELECT-only V2 and postflight, repeat preflight refusal, no retry on partial state; restricted receipt/logs only in owner private scope. Terminal presence is not proof of human identity; no generated confirmation or unattended automation is authorized. No actual full private runtime or independent review yet.

Windows L03 package-first unchanged. POSIX L06 uses the exact verified JAR and final reviewed integrated runner, with the mandatory owner-terminal checkpoint. Claude prepares prerequisites, owner executes confirmation, Claude resumes sanitized result collection. Do not promise unattended one-shot approval.

## Superseding checkpoint — R06B3a, 2026-11-25 PHT

R06B2 PR51 17bd5bf0 exact CI 45434204255 / Extended job 138587448867 VERIFIED BOUNDED: actual confirmation matching and CI refusal, 8 precheck refusals, actual restricted MySQL grants, original encrypted C5/V2/cleanup, 91 Maven/19 Playwright and all 3 jobs. Actual owner consent/private full-plan positive NOT_RUN. R06B3a PR52 d3e8a8550e00e3bb9423d84f51f8141783e71fd1 exact CI 45437263264 pending actual receipts, adds separate LocalMixedAcceptance C5 mode, exact approved plan private, third independently initialized server, exact row equality, independent restored postflight, size/timing and cleanup. Public CI refuses full mixed backup/restore and tests third EMPTY server; 10 prechecks expected. Actual full cohort positive remains NOT_RUN. R06B3b valid recovered normal browser/API remains open. Graph 12 (33 nodes), 14, 15 and project-wide registry supersede older status. No R09 release, no Claude launch, no host/private input upload/merge/deploy, independent review open.

Final L06 must use one guarded lifetime (three test-owned servers, first original source, restored-and-imported source, third restored mixed target). Exact approved input and owner confirmation remain required. C5 separate LocalMixedAcceptance only after actual independent postflight, distinct restore->mixed schema/container pair, approved cohort and original pair/owner, empty sessions, exact archive digest and owner mode0600, unchanged 64MiB cap. No spoofing GitHub Actions, no synthetic lookalike full cohort, no elevated runtime/host endpoint. Never reuse original V2 fingerprint on third server or recapture post-import; verified originals carried by exact rows and independent restored SQL postflight. Do not run interim --approved-import separately: it currently stops before normal browser. R06B3b must extend this same lifetime. On size/timeout/failure stop and preserve sanitized status; no partial-target retry or raised cap. No local platform or full positive proof yet.

## Superseding L06 command contract (source-reviewed; still blocked)

See [report27](27_R06B3B_AGGREGATE_RECEIPT_AND_L06_COMMAND_CONTRACT.md). The integrated same-lifetime command is now source-implemented; final exact-head CI and R09 authorization remain mandatory. Do not run interim preparation separately, automate confirmation, or use a private positive in CI. Final implementation SHA must be pinned at R09; L06 remains NOT_RUN/BLOCKED. Windows L03/L04 unaffected.

## Remote gate checkpoint — R06B VERIFIED_BOUNDED

Report28 records actual final PR53 CI45475369786. Integrated L06 runner remote engineering complete within public scope; still BLOCKED pending final R09 SHA, local prerequisites and direct owner consent. Report27 command contract remains authoritative; no actual private positive or independent acceptance.

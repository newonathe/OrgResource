# 11 — Native execution recipe (minimal model turns, package-first)

This is a command runbook for a **fresh, clean, detached source worktree at the reviewed implementation SHA**, and the separately fetched handoff. It is not permission automation or a new tested script. Native commands use existing repository guard logic; no fake CI env, global config changes, elevated privileges, or broad script/PowerShell wildcard permissions. The owner explicitly supplies a private, locally stored, restricted directory (not inside a Git tree, shared/cloud-sync/UNC path or reparse-point tree) and optional exact private plan/IR4 path. Do not search unrelated personal files. Keep raw logs private; terminal output may be retained by Claude. Do not import or attach private logs to Claude memory. Verify native exits and actual receipt markers locally; no copy/paste of sensitive output.

## One small launch from the owner's repository

First check R09 in 12_EXECUTION_GRAPH.json. **Do not execute while ready_to_launch=false unless owner explicitly authorizes a named independent local slice.** Fetch the handoff branch and final reviewed implementation ref. The literal worktree SHA below is a historical verified example; use the exact newly approved source SHA, not latest branch by assumption. Verify origin is newonathe/OrgResource, working tree status, actual PR #46 current HEAD and CI, then create a new worktree at approved source SHA (the exact initial implementation tested by CI #45419347291 is ec8600f5fdb58b7ea8bdafa42b109b1c41c4f306). Handoff docs are available via git show or a separate read-only checkout. Do not change the user's existing branch or worktree. Keep new worktree outside private evidence dir; stop if checkout path exists. Owner-selected actual paths, never placeholders executed as literals.

```powershell
git fetch origin
git fetch origin refs/heads/docs/claude-local-execution-handoff-2026-11-24
git show FETCH_HEAD:docs/handoffs/claude-local-execution-2026-11-24/09_LAUNCH_PROMPT.md
# If using this reviewed baseline, create a fresh unique worktree at a new path:
git worktree add --detach $NewSourceWorktree 'ec8600f5fdb58b7ea8bdafa42b109b1c41c4f306'
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

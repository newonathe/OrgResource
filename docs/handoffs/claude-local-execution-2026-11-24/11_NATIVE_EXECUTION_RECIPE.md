# 11 — Current native execution recipe (one authoritative sequence)

**Current authority (Report49, 2026-11-25 PHT):** R09 technical gate VERIFIED_BOUNDED; exact reviewed source `2c626cb251fc3e0a5e3304f9ea5b03f89f55e024`, draft PR56, full CI45494597345 and dedicated CI45494597313. Owner explicitly initiates local execution. Private/independent/host/student gates remain open. Historical text is preserved in the previous handoff commit `cef3b1d566d5d480156e114a1947089518022f0f`; current 09/11/12/45 supersede it. No native execution claimed.

## L00: explicit owner launch and exact source checkout

This is source-reviewed PowerShell, NOT_RUN on the owner machine. Run only from a trusted existing repo in an owner-approved exclusive disposable Windows environment (prefer an isolated VM; no unrelated MySQL services/listeners or competing launches). Verify actual local toolchain and private custody first; if isolation or process ownership cannot be established, BLOCKED. Do not change global Git/PowerShell/OS settings or stop unrelated services. Owner picks a fresh absolute worktree path outside private output, existing worktrees and cloud-sync/reparse-point trees. Verify live PR56 and exact actual successful [full CI](https://github.com/newonathe/OrgResource/actions/runs/45494597345) and [dedicated CI](https://github.com/newonathe/OrgResource/actions/runs/45494597313). Read live handoff by pinned commit, not by merging it. Do not print credential-bearing remote URLs or private path values to model context.

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
foreach ($entry in (@($Dispatch.source_files) + @($Dispatch.migrations) + @($Dispatch.support_files))) {
    $actual = (Invoke-CheckedGit ls-tree $FinalSourceSha -- $entry.path) -join ''
    $expected = '{0} blob {1}' -f $entry.mode, $entry.blob
    $expected += "`t" + $entry.path
    if ($actual -cne $expected) { throw 'Pinned source blob/mode mismatch.' }
}
if ($Dispatch.source_files.Count -ne 16 -or $Dispatch.migrations.Count -ne 7 -or $Dispatch.support_files.Count -ne 17) { throw 'Source lock inventory mismatch.' }
Invoke-CheckedGit diff --exit-code 7f8c0746da18fd54b47f430ac1435ac36e605d67 $FinalSourceSha -- backend frontend
Invoke-CheckedGit diff --exit-code 45bcdfc7285598de3fe98684448aa65b5dd47e12 $FinalSourceSha -- extended/backend/src/main/resources/db/migration
# Owner must already have verified current PR56 and both completed CI runs before proceeding.
if ([string]::IsNullOrWhiteSpace($NewSourceWorktree) -or ![IO.Path]::IsPathFullyQualified($NewSourceWorktree) -or (Test-Path -LiteralPath $NewSourceWorktree)) { throw 'Owner must choose a fresh absolute source path.' }
Invoke-CheckedGit worktree add --detach $NewSourceWorktree $FinalSourceSha
if (((Invoke-CheckedGit -C $NewSourceWorktree rev-parse HEAD) -join '') -cne $FinalSourceSha) { throw 'Detached checkout mismatch.' }
if (@(Invoke-CheckedGit -C $NewSourceWorktree status --porcelain --untracked-files=all).Count -ne 0) { throw 'Fresh checkout is not clean.' }
```

After the checked checkout, enter the new worktree explicitly with Set-Location -LiteralPath $NewSourceWorktree. Confirm HEAD and clean status again. Fetch/read the handoff files using git show at $HandoffSha; source checkout does not itself contain the docs-only packet or its CLAUDE.md. Follow 09 explicitly; never launch from the handoff docs checkout by mistake. Native shell parsing/execution remains unverified. Use 02 for version/private prerequisites and task scope.

## L03: package once, credit L01/L02 only from actual receipts

Owner supplies nonexistent $NewPrivateReleaseDir and $PrivatePackageLog in a private local scope outside any repo, plus private existing $PrivateTempDir. Check all ancestors, ACLs, free space, no cloud/UNC/reparse point or unrelated data. Validate Java21, real MySQL8 binaries, Node24/npm, Python with pywinpty, tar, Git, CLI Playwright prerequisites and required dependency cache/network authority. No broad installs, global changes, paid actions or new permissions. Use a dedicated child environment scrubbed of hosted DB/provider configuration, no environment dump, explicit SMS disabled. TEMP/TMP must point to approved private temp. Before launch, inspect actual package script and transitive verifier safety, stop if any DB or app listener identity cannot be established. Keep raw logs private. Do not stream into agent conversation.

```powershell
$env:EXT_SMS_ENABLED = 'false'
$env:EXT_REMINDER_MODE = 'DISABLED'
$env:TEMP = $PrivateTempDir
$env:TMP = $PrivateTempDir
pwsh -NoProfile -File .\scripts\package-extended-c.ps1 -Destination $NewPrivateReleaseDir -Python python.exe *> $PrivatePackageLog
$PackageExit = $LASTEXITCODE
"PACKAGE_EXIT=$PackageExit"
```

Require actual exit0 plus READY.json, RELEASE-MANIFEST.json, SHA256SUMS.txt, exact final source SHA, source ZIP/JAR/log digests and source inventory. Check actual nested VERIFICATION.log completion markers: 'Extended C PASS: React, packaged and Vite Chromium, Maven, academic Newman, inventory and governance, durable replay, and image checks passed.', 'C5 recovery PASS: independent MySQL processes', 'Operator first run passed:', 'Oversized MySQL output was refused before an archive was published.', 'Protected-login restore passed.' Inspect test counts, errors, exits and cleanup, not just substrings; do not print raw logs or private credentials. Source package retains historical C6a name and allowlist; it is a local release candidate, not complete current C8/C9 distribution. Actual current package source includes all extended tracked code plus selected legacy scripts/evidence; does not include the complete mixed harness. Record missing paths; no final release clearance. Use 02 fallback L01/L02 only for independently missing evidence after first failure review; no automatic full repetition.

## L04: reuse exact verified JAR, no rebuild

Package output OrgResource-Extended-C6a-Java21.jar must match manifest and checksum files. Compare all seven migration SQL bytes inside JAR against git cat-file blob at exact approved SHA (not potentially CRLF-converted working tree). Require one unique entry per migration, no extra SQL/version and packaged index.html plus JS/CSS assets. Verify manifest sourceSha, protectedCourseSha and tree values, READY checksumsSha256, SHA256SUMS.txt and all actual file hashes; no claim binary equality with Linux CI merely because same source. Use deterministic Python zipfile/subprocess checks locally. On mismatch mark BLOCKED. Copy only this exact verified JAR to fresh checkout ignored extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar, never overwrite, compare before/after SHA256 and tracked clean state. Actual private approved plan must be 19,265,850 bytes, SHA256 3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c; owner supplies absolute path outside repo, no symlinks/reparse/cloud.

```powershell
pwsh -NoProfile -File .\scripts\verify-c8-demo-import.ps1 -Plan $PrivatePlanAbsolutePath -Sha256 '3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c' *> $PrivateDemoLog
$DemoExit = $LASTEXITCODE
"DEMO_EXIT=$DemoExit"
```

Require actual count/state/ledger reconciliation and encrypted restore, repeat-import refusal, measured dump/archive cap and clean termination. Four-table count/sum/XOR CRC32 fingerprints and allocation count are bounded; NOT exact all-row proof. Retained stopped disposable datadirs and archive stay private. No reused target or shared service; no automatic retry, no raised size cap.

## L05: independent eligible private IR4 work

Owner supplies the restricted ledger only; deterministic join/tally and receipt reconciliation with 05 and v1 contract. Do not re-run 22,320 historical cases wholesale or rewrite old labels. If absent, BLOCKED. No independent signoff or private case IDs in public return.

## L06: distinct owner-terminal POSIX mixed rehearsal

Read report27's command and acceptance contract, with current graph/lock exact source; do not use historical candidate source. Native Linux or explicitly approved WSL, nonroot, canonical private root mode0700 and exact private plan mode0600 outside checkout; distinct source/private roots. Existing local Docker Unix socket, preinstalled mysql:8.0.46, Java21, pwsh>=7.4, Python PIDFD, openssl, sudo -n nsenter/setpriv/ip/timeout, node/curl, exact frontend lockfile dependencies and existing Chromium cache; no auto-pull, installs or permission broadening. Same L03 verified JAR and digest copied only with owner permission. Verify actual toolchain, source, path, file mode and hashes, sufficient space, no untrusted untracked source; no hosted config or shell tracing. No fake CI env. If owner unavailable or any prerequisite missing, BLOCKED, continue independent L05/L08.

Owner alone uses direct terminal, all three TTYs, from exact clean POSIX source checkout, with already verified variable values:
```bash
bash scripts/verify-c9-local-mixed-preparation.sh --approved-import "$PRIVATE_ROOT" "$FINAL_SOURCE_SHA" "$VERIFIED_JAR" "$VERIFIED_JAR_SHA256" "$PRIVATE_PLAN"
```

Do not pipe/redirect or automate challenge. Owner reviews actual restore receipt/fingerprint/digest and types exact confirmation within 300s or declines. No preliminary duplicate prep/import. Require R06B2_LOCAL_MIXED_IMPORT_OK, R06B3A_FULL_MIXED_RECOVERY_OK, R06B3B_PRIVATE_MIXED_RUNTIME_OK, R06B1_CLEANUP_OK and actual exit0/status.txt phase=cleanup; full report27 oracles, 98 orgs (96 tagged+2 originals), 21,060 allocations/67,370 events, exact preserved rows and independent recovered session-only runtime/browser. Partial markers never success. Failure, refusal, timeout or cleanup uncertainty: preserve restricted receipt, no partial-import retry or forced kill, no new grants/cap changes. L07 off-host is separately authorized owner/operator only.

## L08: resume and handback

Checkpoint each task using 07/08; preserve exact SHA, exit, elapsed, safe markers, blockers, private receipt locator OWNER_LOCAL_ONLY. No raw plan/SQL/logs/keys/contact/private IDs, artifacts, screenshots or private paths in public or model output. Use actual installed Claude model/effort/usage indicators where available, no extra telemetry or permission bypass. No duplicate successful native tasks, no blind rerun after interruption. If source defect emerges, report first actual failure to ChatGPT. No local production source modifications, host/deploy/merge/V8/C10/C11/live SMS or fabricated approval. Return even if private or operator tasks remain blocked.
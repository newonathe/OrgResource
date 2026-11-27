# 146 - Fictional native dispatch preparation (not execution)
2026-11-28 PHT. Intake 36c6778cb3d4558840b900be7eb3a0fb53c032f8; exact live harness 768418c9b503e280fa363668d5362fcce3cb6dba, tree d9b062c971795a08fb3e9a7e040fe3cf88eef52c; main 96741bbd295a2d1e7b4bbf6a75cb558a793b09bd, PR58 4545fdd81a09772f1ac667af9a0f0c034b9e31ee and PR59 0b07f45c0eb6482b9dcdd8f38f0d8cdb6bdf6427. Root/00/issue50, Reports102/120/134/136/142/145 and Matrix66 reopened; Report124 governs via current pointers. Inline GPT-6.2, self-review not independent. Reuse Report145 compile evidence; no new CI workflow, source change, native invocation, or repeated pure tests. Goal: bind the first eligible fictional native batch to exact source, inputs and acceptance/refusal markers without guessing the owner environment or weakening quarantine.

## Exact dispatch artifact and scope
[Machine-readable dispatch manifest](146_FICTIONAL_NATIVE_DISPATCH_MANIFEST.json), blob 6ed2c8e4846cd145bdd5898d022cd1c2e14e9fba, 12781 ASCII bytes. Deterministic JS check against the exact nontruncated Git tree: six full fixture sources, ten distinct C# source dependencies (16 unique paths), seven distinct root labels; exact Add-Type lists and all fixture/dependency blob identities matched; fixture Windows/ConfirmFictional guards, parameter signatures, Python requirement and all fixed/dynamic success-marker literals cross-checked. This is source validation, not PowerShell execution. No new production runner. Matrix66's 32 original rows remain NOT_RUN; partial fixture coverage cannot close a row or replace independent acceptance.

## Mandatory intake before any native execution
1. Owner manually selects/authorizes a dedicated nonadmin Windows session and seven new empty, disjoint fixed local NTFS roots. All seven outside the source checkout, other worktrees, real user profiles, synced/shared/temp storage, existing recovery/retention locations and production data; separate approved root for retained local logs. No inferred path. Non-elevated current identity, not SYSTEM, no administrator/elevation or policy change. PowerShell 7.4+ actual version; 64-bit required by later ConPTY, not authorized here. Check exclusive operator custody and no concurrent launcher. Do not create broad ACL or repair an existing directory; if root not approved or ACL not protected and owner-only full control, stop. Existing C9DirectoryIdentity.cs and C9ControlLock.cs (full reviewed) enforce real handle identities, fixed NTFS, no reparse/offline/recall, owner-only protected ACL, exclusive lock and no deletion. Root emptiness and outside-private-location approval are additional operator preconditions, not claimed by the C# code. No Windows host available to this chat.
2. Dedicated clean worktree of exact harness commit, separate from main, PR58/59 and handoff. Verify git HEAD, tree, status and all manifest pins before invocation. Example read-only checks below, not executed on owner machine. Use trusted Git/Pwsh; verify SHA256 of approved absolute python.exe and installed dependencies separately before first Python case, no trust from computing current hash alone. No auto-install/unknown interpreter fallback. Existing probe pins executable and directories, not complete DLL/runtime provenance.
3. Select private local transcript/receipt path outside source and fixture roots, no secrets or raw logs uploaded. Review exact fixture bodies (all six read in this turn) and material dependencies before running. Prior reports pin transitive C# review, not renewed full audit of every file. Fresh pwsh process per fixture to avoid type caching, serial execution with separate roots. Stop on nonzero, missing/extra success marker, unexpected effects or unknown process/identity; preserve evidence, no recursive cleanup, global process kill, force unlock or rerun in used root.

Read-only Git checks from the dedicated worktree:
```powershell
git rev-parse HEAD
git rev-parse 'HEAD^{tree}'
git status --porcelain --untracked-files=all
# All results must match the manifest and status must be empty.
# $manifest is loaded from the trusted separately pinned handoff file.
foreach ($entry in $manifest.fixtures) {
    foreach ($item in @($entry) + @($entry.source_dependencies)) {
        $committed = git rev-parse "HEAD:$($item.path)"
        if ($LASTEXITCODE -ne 0 -or $committed -cne $item.blob) { throw 'FIXTURE_PIN' }
        $actual = git hash-object --path=$($item.path) $item.path
        if ($LASTEXITCODE -ne 0 -or $actual -cne $item.blob) { throw 'FIXTURE_WORKTREE_PIN' }
    }
}
```
The command is a source-bound recipe, not an actual owner-machine preflight receipt. Bind manifest and checkout from trusted refs, not a copied untrusted directory.

## First six fixture commands (only after prerequisites)
Variables $controlRoot, $loginRoot, $retentionSourceRoot, $retentionStageRoot, $ownedJobRoot, $outerRoot, $transportRoot, $python, $pythonSha are owner-confirmed values; no defaults. Execute one command at a time, record actual exit and all expected marker lines from manifest, inspect retained fixture artifacts; never merely echo marker. Each command starts a new process.
```powershell
pwsh -NoProfile -File .\scripts\windows-l06\test-control-lock.ps1 -FixtureRoot $controlRoot -ConfirmFictional
pwsh -NoProfile -File .\scripts\windows-l06\test-login-custody.ps1 -FixtureRoot $loginRoot -ConfirmFictional
pwsh -NoProfile -File .\scripts\windows-l06\test-retention-stage.ps1 -SourceFixtureRoot $retentionSourceRoot -RetentionFixtureRoot $retentionStageRoot -ConfirmFictional
pwsh -NoProfile -File .\scripts\windows-l06\test-owned-job-probe.ps1 -FixtureRoot $ownedJobRoot -Python $python -PythonSha256 $pythonSha -ConfirmFictional
pwsh -NoProfile -File .\scripts\windows-l06\test-outer-watchdog.ps1 -FixtureRoot $outerRoot -Python $python -PythonSha256 $pythonSha -ConfirmFictional
pwsh -NoProfile -File .\scripts\windows-l06\test-owned-journal-transport.ps1 -FixtureRoot $transportRoot -Python $python -PythonSha256 $pythonSha -ConfirmFictional
```
Stop dependent tests if control-lock or owned-job fails; login and retention remain disjoint after control accepted. The owned-job sentinel intentionally uses a held Process object with 120-second fictional sleep; interruption before finally is not proven safe. Observe actual sentinel preservation and owned termination, no broad name/PID kill. Outer surrogate retains uncertainty even on success; transport retains marker and never publishes journal. Login uses harmless invalid metadata bytes; retention header-only random bytes are not genuine encryption/restore. Do not invoke legacy journal parent/worker/producer, valid ConPTY, real editor, SQL, private runner or recovery. No accepted marker, no deletion or settlement. Actual role/session/console host attribution, API fault/parent interruption, process identity, journal integration, revoke/cleanup, real H05 and H01-A restore/retention remain. Resolve actual failures on harness-only branch using fixed oracle, new approved root, exact ref and independent review. No relaxation to make CI pass.

## Receipt and regression reconciliation
Handoff Verify46069481376/job140742710627 terminal success actual exact checkoutad0616b at21:19:26 UTC; 61 Maven zero failures/errors/skips at21:19:56.530; Newman29+30+19+27+37+9+9=160 assertions zero failures; one browser at21:21:14.552. Prior Report145 repeat receipts retained.
Other two jobs at observation: [[{"id":140740862802,"status":"in_progress","conclusion":null}],[{"id":140742710573,"status":"in_progress","conclusion":null}]].

Record each future native test: exact candidate/manifest/fixture blobs, dependency and tool pins, private root locator and verified identity/ACL, actual invocation and timestamps, exit/marker lines, actual side effects/absence, retained paths and failure/cleanup disposition; public-safe summary only. Human independent acceptance and real owner authorization separate.

## Next
Consume only outstanding terminal actual regression receipts. Remote preparation of this first six-fixture batch is complete, native effectful work is blocked on owner-provided approved disposable nonadmin Windows environment and trusted tools; don't generate more speculative wrappers or rerun unchanged compile/packaging. Use existing Report120/121/124 and Matrix66 for remaining authorized work; advance a materially distinct eligible defect or academic delivery task if available. No valid ConPTY until independent outer/console-host acceptance contract. Private L06 HOLD/no WSL, full native package/operator, H03/H06 settlement, P0/V7 freeze, L07 off-host, 93 independent/13 privacy/student gates unchanged. No merge/deploy/release.

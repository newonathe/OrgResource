# Negative matrix for the native Windows harness. Fictional inputs only; no approved plan byte is read.
#Requires -Version 7.4
param([Parameter(Mandatory)][string] $TestRoot, [Parameter(Mandatory)][string] $Jar, [Parameter(Mandatory)][string] $JarSha256, [Parameter(Mandatory)][string] $Python,
    [Parameter(Mandatory)][string] $NodeModules, [Parameter(Mandatory)][string] $ChromiumDir, [string] $OtherMySqlHome, [string] $OtherJavaHome, [switch] $SkipInterruption)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$me = [Security.Principal.WindowsIdentity]::GetCurrent()
$approvedPlanSha = '3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c'
function Fail([string] $Code) { throw [InvalidOperationException]::new($Code) }
function Get-Sha([string] $Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
. (Join-Path $PSScriptRoot 'windows_common.ps1')
. (Join-Path $PSScriptRoot 'operator_gate.ps1')
$runner = Join-Path $PSScriptRoot 'verify-c9-local-mixed-windows.ps1'; $pwsh = "$env:ProgramFiles\PowerShell\7\pwsh.exe"
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path; $sha = 'fbfe961f861478c1727efa1ebf5df209f81f2c5d'
$packages = Join-Path $env:LOCALAPPDATA 'Packages'
$script:passed = 0
function Get-Residue { return (@(Get-ChildItem -LiteralPath $packages -Directory -Filter 'orgresource.c9.*' -ErrorAction SilentlyContinue).Count, @(& "$env:SystemRoot\System32\subst.exe").Count, @(Get-Process mysqld -ErrorAction SilentlyContinue).Count, @(Get-ChildItem -LiteralPath $TestRoot -Directory -Recurse -Filter 'orgresource-c9w-*').Count) -join ',' }
function New-PrivateDir([string] $Name) { $p = Join-Path $TestRoot $Name; New-Item -ItemType Directory -Path $p | Out-Null; Set-PrivateAcl $p @(); return $p }
function Invoke-Runner([hashtable] $Override, [hashtable] $Environment = @{}) {
    $a = [ordered]@{ Mode = 'FixtureOnly'; Root = $good; SourceSha = $sha; Jar = $Jar; JarSha256 = $JarSha256; Python = $Python; NodeModules = $NodeModules; ChromiumDir = $ChromiumDir }
    foreach ($k in $Override.Keys) { $a[$k] = $Override[$k] }
    $info = [Diagnostics.ProcessStartInfo]::new($pwsh); $info.RedirectStandardOutput = $true; $info.RedirectStandardError = $true; $info.RedirectStandardInput = $true; $info.UseShellExecute = $false
    foreach ($x in '-NoProfile', '-NonInteractive', '-File', $runner) { $info.ArgumentList.Add($x) }
    foreach ($k in $a.Keys) { $info.ArgumentList.Add("-$k"); $info.ArgumentList.Add($a[$k]) }
    foreach ($k in @($info.Environment.Keys)) { if ($k -match '^(CLAUDE|ANTHROPIC|GITHUB_|CODEX|AI_AGENT)' -or $k -ceq 'CI') { $null = $info.Environment.Remove($k) } }
    foreach ($k in $Environment.Keys) { $info.Environment[$k] = $Environment[$k] }
    $p = [Diagnostics.Process]::Start($info); $p.StandardInput.Close(); $o = $p.StandardOutput.ReadToEndAsync(); $e = $p.StandardError.ReadToEndAsync()
    if (-not $p.WaitForExit(180000)) { $p.Kill($true); throw 'C9W_TEST_TIMEOUT' }
    return [pscustomobject]@{ Code = $p.ExitCode; Text = $o.Result + $e.Result }
}
function Assert-Refused([string] $Label, [string] $Expected, [hashtable] $Override, [hashtable] $Environment = @{}) {
    $before = Get-Residue
    $r = Invoke-Runner $Override $Environment
    if ($r.Code -eq 0) { throw "C9W_UNEXPECTED_SUCCESS:$Label" }
    if (($r.Text -split '\r?\n') -cnotcontains $Expected) { [Console]::Error.WriteLine($r.Text); throw "C9W_WRONG_REFUSAL:$Label" }
    if ((Get-Residue) -cne $before -or $r.Text.Contains('R06B1_NETWORK_ISOLATION_OK') -or $r.Text.Contains('R06B1_PLAN_READ_BEGIN_WIN') -or $r.Text.Contains('R06B1_PRIVATE_COPY_BEGIN_WIN')) { throw "C9W_REFUSAL_LEFT_STATE:$Label" }
    $script:passed++; "R06B1_PRECHECK_REFUSAL_OK_WIN:$Label"
}
New-Item -ItemType Directory -Path $TestRoot | Out-Null; Set-PrivateAcl $TestRoot @()
$good = New-PrivateDir 'good'
try {
    # Custody
    $open = Join-Path $TestRoot 'open'; New-Item -ItemType Directory -Path $open | Out-Null
    Assert-Refused 'root-inherited-acl' 'R06B1_PRIVATE_ROOT_REQUIRED' @{ Root = $open }
    $wide = New-PrivateDir 'wide'; & "$env:SystemRoot\System32\icacls.exe" $wide /grant '*S-1-1-0:R' /Q | Out-Null
    Assert-Refused 'root-extra-ace' 'R06B1_PRIVATE_ROOT_REQUIRED' @{ Root = $wide }
    $junction = Join-Path $TestRoot 'junction'; New-Item -ItemType Junction -Path $junction -Target $good | Out-Null
    Assert-Refused 'root-reparse' 'R06B1_PRIVATE_ROOT_REQUIRED' @{ Root = $junction }
    Assert-Refused 'root-relative' 'R06B1_PRIVATE_ROOT_REQUIRED' @{ Root = 'good' }
    Assert-Refused 'root-unc' 'R06B1_PRIVATE_ROOT_REQUIRED' @{ Root = "\\localhost\C$\$($good.Substring(3))" }
    Assert-Refused 'root-dotdot' 'R06B1_PRIVATE_ROOT_REQUIRED' @{ Root = "$good\..\good" }
    $inRepo = Join-Path $repo 'extended\backend\target\c9w-test-root'; New-Item -ItemType Directory -Path $inRepo -Force | Out-Null; Set-PrivateAcl $inRepo @()
    try { Assert-Refused 'root-in-source' 'R06B1_PRIVATE_ROOT_REQUIRED' @{ Root = $inRepo } } finally { Remove-Item -LiteralPath $inRepo }
    # Source, JAR
    Assert-Refused 'wrong-source' 'R06B1_SOURCE_SHA_MISMATCH' @{ SourceSha = '0' * 40 }
    $dirty = Join-Path $repo 'c9w-untracked.tmp'; [IO.File]::WriteAllText($dirty, 'x')
    try { Assert-Refused 'dirty-source' 'R06B1_DIRTY_TRACKED_SOURCE' @{} } finally { Remove-Item -LiteralPath $dirty }
    Assert-Refused 'jar-missing' 'R06B1_JAR_REQUIRED' @{ Jar = "$good\none.jar" }
    Assert-Refused 'jar-digest-format' 'R06B1_JAR_REQUIRED' @{ JarSha256 = $JarSha256.ToUpperInvariant() }
    Assert-Refused 'jar-wrong-digest' 'R06B1_JAR_HASH_MISMATCH' @{ JarSha256 = '0' * 64 }
    foreach ($case in 'changed-migration', 'extra-migration', 'missing-assets') {
        $bad = Join-Path $TestRoot "$case.jar"; Copy-Item -LiteralPath $Jar -Destination $bad
        $zip = [IO.Compression.ZipFile]::Open($bad, 'Update')
        try {
            if ($case -ceq 'missing-assets') { $zip.GetEntry('BOOT-INF/classes/static/index.html').Delete() }
            elseif ($case -ceq 'extra-migration') { $w = [IO.StreamWriter]::new($zip.CreateEntry('BOOT-INF/classes/db/migration/V8__extra.sql').Open()); $w.Write('SELECT 1;'); $w.Dispose() }
            else { $e = @($zip.Entries | Where-Object FullName -Like 'BOOT-INF/classes/db/migration/V7__*')[0]; $name = $e.FullName; $e.Delete(); $w = [IO.StreamWriter]::new($zip.CreateEntry($name).Open()); $w.Write('SELECT 1;'); $w.Dispose() }
        } finally { $zip.Dispose() }
        Assert-Refused "jar-$case" 'R06B1_JAR_MIGRATION_OR_ASSET_MISMATCH' @{ Jar = $bad; JarSha256 = (Get-FileHash -LiteralPath $bad).Hash.ToLowerInvariant() }
        Remove-Item -LiteralPath $bad
    }
    # Tools, listeners, versions
    Assert-Refused 'tool-missing' 'R06B1_TOOL_MISSING' @{ NodeModules = "$good\none" }
    Assert-Refused 'browser-missing' 'R06B1_TOOL_MISSING' @{ ChromiumDir = $good }
    if ($OtherMySqlHome) { Assert-Refused 'mysql-version' 'R06B1_MYSQL_VERSION' @{} @{ MYSQL_HOME = $OtherMySqlHome } } else { 'R06B1_PRECHECK_NOT_RUN:mysql-version' }
    if ($OtherJavaHome) { Assert-Refused 'java-version' 'R06B1_JAVA21_REQUIRED' @{} @{ JAVA_HOME = $OtherJavaHome } } else { 'R06B1_PRECHECK_NOT_RUN:java-version' }
    $l = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 8085); $l.Start()
    try { Assert-Refused 'unexpected-listener' 'R06B1_UNEXPECTED_LISTENER' @{} } finally { $l.Stop() }
    # Modes and owner gate
    Assert-Refused 'fixture-with-plan' 'R06B1_ARGUMENTS' @{ Plan = "$good\plan.json" }
    Assert-Refused 'import-noninteractive' 'R06B2_OWNER_TERMINAL_REQUIRED' @{ Mode = 'ApprovedImport'; Plan = "$good\plan.json" }
    Assert-Refused 'import-in-ci' 'R06B2_OPERATOR_CI_REFUSED' @{ Mode = 'ApprovedImport'; Plan = "$good\plan.json" } @{ GITHUB_ACTIONS = 'true' }
    Assert-Refused 'import-in-agent' 'R06B2_AGENT_SESSION_REFUSED' @{ Mode = 'ApprovedImport'; Plan = "$good\plan.json" } @{ CLAUDECODE = '1' }
    # Exact matching is pure; interactive refusal runs on a real console with fictional digests.
    $fp = '1' * 64; $dg = '2' * 64; $expected = Get-C9Challenge $fp $dg
    if (-not (Test-C9Response $fp $dg $expected)) { throw 'C9W_GATE_MATCH' }
    foreach ($reply in '', "$expected ", 'yes', (Get-C9Challenge $dg $fp), $expected.ToLowerInvariant()) { if (Test-C9Response $fp $dg $reply) { throw 'C9W_GATE_MATCH' } }
    if (Test-C9Response 'malformed' $dg $expected) { throw 'C9W_GATE_MATCH' }
    'R06B2_CONFIRMATION_CONTRACT_OK_WIN:exact_match=true negative_matches_refused=true'
    & $Python -B (Join-Path $PSScriptRoot 'test_gate.py') (Join-Path $PSScriptRoot 'operator_gate.ps1') $pwsh
    if ($LASTEXITCODE -ne 0) { throw 'C9W_GATE_CONSOLE' }
    # Acceptance and plan custody are unit-tested as functions with fictional bytes; the owner guard is never worked around.
    function Assert-Code([string] $Label, [string] $Expected, [scriptblock] $Action) {
        $code = 'ACCEPTED'; try { & $Action } catch { $code = $_.Exception.GetBaseException().Message }
        if ($code -cne $Expected) { throw "C9W_CUSTODY:$Label $code" }; $script:passed++; "R06B1_CUSTODY_REFUSAL_OK_WIN:$Label"
    }
    $head = (& git -C $repo rev-parse HEAD) -join ''; $acc = "$good\WINDOWS_HARNESS_ACCEPTED.txt"
    Assert-Code 'acceptance-missing' 'R06B2_HARNESS_NOT_ACCEPTED' { Assert-HarnessAccepted $good $head }
    foreach ($text in "ACCEPTED $('0' * 40)", "accepted $head", $head, '') { [IO.File]::WriteAllText($acc, $text); Assert-Code 'acceptance-wrong' 'R06B2_HARNESS_NOT_ACCEPTED' { Assert-HarnessAccepted $good $head } }
    [IO.File]::WriteAllText($acc, "ACCEPTED $head"); Assert-HarnessAccepted $good $head
    & "$env:SystemRoot\System32\icacls.exe" $acc /grant '*S-1-1-0:R' /Q | Out-Null
    Assert-Code 'acceptance-wide-acl' 'R06B2_HARNESS_NOT_ACCEPTED' { Assert-HarnessAccepted $good $head }
    Remove-Item -LiteralPath $acc
    Assert-Code 'plan-absent' 'R06B1_PRIVATE_PLAN_REQUIRED' { Assert-ApprovedPlan "$good\none.json" $repo }
    [IO.File]::WriteAllText("$good\small.json", '{}'); Assert-Code 'plan-size' 'R06B1_APPROVED_PLAN_MISMATCH' { Assert-ApprovedPlan "$good\small.json" $repo }
    $fs = [IO.File]::Create("$good\same.json"); $fs.SetLength(19265850); $fs.Dispose()
    Assert-Code 'plan-same-size-wrong-bytes' 'R06B1_APPROVED_PLAN_MISMATCH' { Assert-ApprovedPlan "$good\same.json" $repo }
    Assert-Code 'plan-inside-source' 'R06B1_PRIVATE_PLAN_REQUIRED' { Assert-ApprovedPlan "$good\same.json" $good }
    Assert-Code 'plan-through-reparse' 'R06B1_PRIVATE_PLAN_REQUIRED' { Assert-ApprovedPlan "$junction\same.json" $repo }
    & "$env:SystemRoot\System32\icacls.exe" "$good\same.json" /grant '*S-1-1-0:R' /Q | Out-Null
    Assert-Code 'plan-wide-acl' 'R06B1_PRIVATE_PLAN_REQUIRED' { Assert-ApprovedPlan "$good\same.json" $repo }
    Remove-Item -LiteralPath "$good\small.json", "$good\same.json"
    # Network and loopback failure: the evaluator must refuse each altered result.
    $sound = @{ self = 'CONNECTED'; ownListener = 'CONNECTED'; external4 = 'EACCES'; external6 = 'ENETUNREACH'; hostLoopback = 'ETIMEDOUT'; lan = 'TIMEOUT'; 'role mixed' = 'ETIMEDOUT'; 'udp lan' = 'NO_REPLY'; dnsDirect = 'ECONNREFUSED'; dnsSystem = 'ENOTFOUND'; 'path x' = 'EPERM'; 'write x' = 'EPERM' }
    $sound['udp loopback'] = 'NO_REPLY'
    $probeSpec = @{ tcpTargets = @{ ownListener = @{}; hostLoopback = @{}; lan = @{}; 'role mixed' = @{} }; udpTargets = @(@{ name = 'lan' }, @{ name = 'loopback' }); paths = @('x'); writePaths = @('x') }
    Assert-ProbeResult $sound $probeSpec
    foreach ($case in @(@('self', 'TIMEOUT', 'R06B1_LOOPBACK_UNAVAILABLE'), @('ownListener', 'ECONNREFUSED', 'R06B1_LOOPBACK_UNAVAILABLE'), @('external4', 'CONNECTED', 'R06B1_EXTERNAL_REACHABLE'), @('external4', 'TIMEOUT', 'R06B1_EXTERNAL_REACHABLE'),
            @('external6', 'CONNECTED', 'R06B1_EXTERNAL_REACHABLE'), @('hostLoopback', 'CONNECTED', 'R06B1_EXTERNAL_REACHABLE'), @('lan', 'CONNECTED', 'R06B1_EXTERNAL_REACHABLE'), @('role mixed', 'CONNECTED', 'R06B1_EXTERNAL_REACHABLE'),
            @('udp lan', 'REPLY', 'R06B1_EXTERNAL_REACHABLE'), @('dnsDirect', 'ENOTFOUND', 'R06B1_EXTERNAL_REACHABLE'), @('dnsSystem', 'RESOLVED', 'R06B1_EXTERNAL_REACHABLE'), @('path x', 'READ', 'R06B1_CROSS_ROLE_FILES'), @('write x', 'WRITTEN', 'R06B1_RUNTIME_WRITABLE'))) {
        $bad = $sound.Clone(); $bad[$case[0]] = $case[1]; Assert-Code "probe-$($case[0])-$($case[1])" $case[2] { Assert-ProbeResult $bad $probeSpec }
    }
    # A mode spelled in another case must not fall through to the import stage.
    foreach ($m in 'approvedimport', 'fixtureonly', 'Other') {
        $before = Get-Residue; $r = Invoke-Runner @{ Mode = $m }
        if ($r.Code -eq 0 -or $r.Text.Contains('R06B1_PREFLIGHT_OK') -or $r.Text.Contains('R06B1_FAILURE_PHASE') -or (Get-Residue) -cne $before) { throw "C9W_MODE:$m" }
        $script:passed++; "R06B1_MODE_REFUSAL_OK_WIN:$m"
    }
    if (-not $SkipInterruption) {
        # Interruption: end only the runner; the kernel must end every contained child and nothing else.
        $sentinel = Start-Process "$env:SystemRoot\System32\PING.EXE" -ArgumentList '-n', '900', '127.0.0.1' -WindowStyle Hidden -PassThru
        try {
            $info = [Diagnostics.ProcessStartInfo]::new($pwsh); $info.UseShellExecute = $false; $info.RedirectStandardOutput = $true; $info.RedirectStandardError = $true
            foreach ($x in '-NoProfile', '-NonInteractive', '-File', $runner, '-Mode', 'FixtureOnly', '-Root', $good, '-SourceSha', $sha, '-Jar', $Jar, '-JarSha256', $JarSha256, '-Python', $Python, '-NodeModules', $NodeModules, '-ChromiumDir', $ChromiumDir) { $info.ArgumentList.Add($x) }
            $p = [Diagnostics.Process]::Start($info); $null = $p.StandardOutput.ReadToEndAsync(); $null = $p.StandardError.ReadToEndAsync()
            for ($i = 0; $i -lt 600 -and @(Get-Process mysqld -ErrorAction SilentlyContinue).Count -lt 2 -and -not $p.HasExited; $i++) { Start-Sleep -Milliseconds 500 }
            if (@(Get-Process mysqld -ErrorAction SilentlyContinue).Count -lt 2) { throw 'C9W_INTERRUPT_SETUP' }
            $live = @(Get-ChildItem -LiteralPath $good -Directory -Filter 'orgresource-c9w-*')[0]
            $o = & $pwsh -NoProfile -File (Join-Path $PSScriptRoot 'recover-c9-windows.ps1') -Root $good -Token $live.Name.Substring(16) 2>&1
            if ($LASTEXITCODE -eq 0 -or "$o" -cnotmatch 'C9W_RECOVER_REFUSED_LIVE' -or @(& "$env:SystemRoot\System32\subst.exe").Count -ne 1) { throw "C9W_RECOVER_LIVE $o" }
            $p.Kill() # the runner only, not its tree
            $null = $p.WaitForExit(10000)
            for ($i = 0; $i -lt 40 -and @(Get-Process mysqld, java -ErrorAction SilentlyContinue).Count; $i++) { Start-Sleep -Milliseconds 250 }
            if (@(Get-Process mysqld, java -ErrorAction SilentlyContinue).Count -or $sentinel.HasExited) { throw 'C9W_INTERRUPT_CONTAINMENT' }
            $left = @(Get-ChildItem -LiteralPath $good -Directory -Filter 'orgresource-c9w-*')
            if ($left.Count -ne 1) { throw 'C9W_INTERRUPT_STATE' }
            $out = & $pwsh -NoProfile -File (Join-Path $PSScriptRoot 'recover-c9-windows.ps1') -Root $good -Token $left[0].Name.Substring(16)
            if ($LASTEXITCODE -ne 0 -or "$out" -cnotmatch 'C9W_RECOVER_OK:mappings=1 profiles=3 ') { throw "C9W_RECOVER $out" }
            if (-not (Test-Path -LiteralPath $left[0].FullName) -or @(& "$env:SystemRoot\System32\subst.exe").Count) { throw 'C9W_RECOVER_STATE' }
            Remove-Item -LiteralPath $left[0].FullName -Recurse -Force
            'R06B1_INTERRUPTION_OK_WIN:children_ended_by_kernel=true sentinel_alive=true recovery_refused_while_live=true mapping_and_profiles_recovered=true work_kept_for_inspection=true'
        } finally { if (-not $sentinel.HasExited) { $sentinel.Kill() }; $sentinel.Dispose() }
    }
    "R06B1_WINDOWS_NEGATIVE_MATRIX_OK:precheck_refusals=$($script:passed)"
} finally {
    $junction = Join-Path $TestRoot 'junction'; if (Test-Path -LiteralPath $junction) { [IO.Directory]::Delete($junction) }
    if (@(Get-ChildItem -LiteralPath $TestRoot -Directory -Recurse -Filter 'orgresource-c9w-*' -ErrorAction SilentlyContinue).Count -eq 0) { Remove-Item -LiteralPath $TestRoot -Recurse -Force }
}

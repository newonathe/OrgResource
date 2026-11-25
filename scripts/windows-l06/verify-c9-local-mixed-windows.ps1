# Native Windows, verification-only port of verify-c9-local-mixed-preparation.sh (Report54). It never changes the application,
# the Linux harness or Windows security settings. Containment is a zero-capability AppContainer per server role plus a
# kill-on-close Job Object. ApprovedImport is owner-terminal only and needs a separate harness acceptance record.
#Requires -Version 7.4
[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('FixtureOnly', 'ApprovedImport', IgnoreCase = $false)][string] $Mode,
    [Parameter(Mandatory)][string] $Root,
    [Parameter(Mandatory)][string] $SourceSha,
    [Parameter(Mandatory)][string] $Jar,
    [Parameter(Mandatory)][string] $JarSha256,
    [string] $Plan,
    [string] $Python = 'python.exe',
    [Parameter(Mandatory)][string] $NodeModules,
    [Parameter(Mandatory)][string] $ChromiumDir
)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$approvedPlanSha = '3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c'
$approvedSourceSha = '57f1f0c680041c39dcf7644a1acca5fcc75dc7d525262547842389da7540b9a2'
$approvedDataset = 'orgresource-c8-plan-v1-57f1f0c68004-20deeda9e2af'
$maxArchive = 64MB + 26 + 12 + 16
$script:phase = 'intake'; $script:seq = 0
$exitCode = 1; $lock = $null
$roles = [ordered]@{}; $work = $null; $drive = $null; $receiptDir = $null
function Fail([string] $Code) { throw [InvalidOperationException]::new($Code) }
function New-Secret([int] $Bytes = 24) { [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes($Bytes)).ToLowerInvariant() }
function Get-Sha([string] $Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Write-Marker([string] $Text) { [Console]::Out.WriteLine($Text) }
$me = [Security.Principal.WindowsIdentity]::GetCurrent()
. (Join-Path $PSScriptRoot 'operator_gate.ps1')
. (Join-Path $PSScriptRoot 'windows_common.ps1')

try {
    # ---- intake: every refusal precedes containers, servers and private bytes ----
    if ($Mode -ceq 'ApprovedImport') { Assert-C9OwnerTerminal } elseif ($Plan) { Fail 'R06B1_ARGUMENTS' }
    if (-not $IsWindows) { Fail 'R06B1_WINDOWS_REQUIRED' }
    if ([Security.Principal.WindowsPrincipal]::new($me).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator) -or $me.IsSystem) { Fail 'R06B1_NONADMIN_REQUIRED' }
    $repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
    if (-not (Test-PlainPath $Root 'Container') -or -not (Test-OwnerOnly $Root) -or -not (Get-Acl -LiteralPath $Root).AreAccessRulesProtected) { Fail 'R06B1_PRIVATE_ROOT_REQUIRED' }
    $Root = $Root.TrimEnd('\')
    if ("$Root\".StartsWith("$repo\", [StringComparison]::OrdinalIgnoreCase) -or "$repo\".StartsWith("$Root\", [StringComparison]::OrdinalIgnoreCase)) { Fail 'R06B1_PRIVATE_ROOT_REQUIRED' }
    if ($SourceSha -cne 'fbfe961f861478c1727efa1ebf5df209f81f2c5d') { Fail 'R06B1_SOURCE_SHA_MISMATCH' }
    $head = (& git -C $repo rev-parse HEAD) -join ''
    # The harness commit may only ADD files below scripts/windows-l06 to the approved source.
    $delta = @(& git -C $repo diff --name-status --no-renames $SourceSha HEAD)
    if ($LASTEXITCODE -ne 0 -or @($delta | Where-Object { $_ -cnotmatch '^A\tscripts/windows-l06/' }).Count) { Fail 'R06B1_SOURCE_SHA_MISMATCH' }
    $dirty = @(& git -C $repo status --porcelain --untracked-files=all)
    if ($LASTEXITCODE -ne 0 -or $dirty.Count) { Fail 'R06B1_DIRTY_TRACKED_SOURCE' }
    if ($JarSha256 -cnotmatch '^[a-f0-9]{64}$' -or -not (Test-PlainPath $Jar 'Leaf')) { Fail 'R06B1_JAR_REQUIRED' }
    if ((Get-Sha $Jar) -cne $JarSha256) { Fail 'R06B1_JAR_HASH_MISMATCH' }
    & $Python -I (Join-Path $PSScriptRoot 'check_jar.py') $repo $SourceSha $Jar
    if ($LASTEXITCODE -ne 0) { Fail 'R06B1_JAR_MIGRATION_OR_ASSET_MISMATCH' }
    if ($Mode -ceq 'ApprovedImport') {
        # Acceptance first: no private byte is read for a harness commit nobody accepted.
        Assert-HarnessAccepted $Root $head
        Assert-ApprovedPlan $Plan $repo
    }
    $mysqlHome = if ($env:MYSQL_HOME) { $env:MYSQL_HOME } else { Join-Path $env:ProgramFiles 'MySQL\MySQL Server 8.0' }
    $javaBin = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { Join-Path $env:ProgramFiles 'Java\missing' }
    foreach ($tool in "$mysqlHome\bin\mysqld.exe", "$mysqlHome\bin\mysql.exe", "$mysqlHome\bin\mysqldump.exe", "$mysqlHome\bin\mysqladmin.exe", "$mysqlHome\bin\mysql_config_editor.exe", $javaBin,
        "$env:ProgramFiles\nodejs\node.exe", "$env:ProgramFiles\PowerShell\7\pwsh.exe", "$env:SystemRoot\System32\curl.exe", "$NodeModules\playwright-core\package.json", "$NodeModules\@playwright\test\package.json") {
        if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) { Fail 'R06B1_TOOL_MISSING' }
    }
    if (((& "$mysqlHome\bin\mysqld.exe" --no-defaults --version) -join '') -cnotmatch ' 8\.0\.46 ') { Fail 'R06B1_MYSQL_VERSION' }
    if (((& $javaBin -version 2>&1) -join "`n") -cnotmatch 'version "21[."]') { Fail 'R06B1_JAVA21_REQUIRED' }
    $chromiumExe = @(Get-ChildItem -LiteralPath $ChromiumDir -Recurse -Filter 'chrome-headless-shell.exe' -ErrorAction SilentlyContinue)
    if ($chromiumExe.Count -ne 1) { Fail 'R06B1_TOOL_MISSING' }
    # Browser tooling must be the lockfile version, not whatever happens to be installed.
    & $Python -I (Join-Path $PSScriptRoot 'check_tools.py') $repo $SourceSha $NodeModules $ChromiumDir
    if ($LASTEXITCODE -ne 0) { Fail 'R06B1_TOOL_VERSION' }
    & $Python -I -c 'from winpty import PtyProcess' 2>$null
    if ($LASTEXITCODE -ne 0) { Fail 'R06B1_TOOL_MISSING' }
    if (@(Get-Service -Name 'MySQL*' | Where-Object Status -ne 'Stopped').Count -or @(Get-Process mysqld -ErrorAction SilentlyContinue).Count) { Fail 'R06B1_UNEXPECTED_LISTENER' }
    foreach ($p0 in 3306, 33060, 8085) { if ((Get-Listeners $p0).Count) { Fail 'R06B1_UNEXPECTED_LISTENER' } }
    if ([IO.DriveInfo]::new($Root.Substring(0, 1)).AvailableFreeSpace -lt 6GB) { Fail 'R06B1_DISK_SPACE' }
    Write-Marker "R06B1_PREFLIGHT_OK_WIN:source_hash=true jar_hash=true seven_migration_bytes=true packaged_assets=true harness_head=$head delta_files=$($delta.Count)"
    Write-Marker "R06B1_TOOLS_WIN:chromium_sha256=$(Get-Sha $chromiumExe[0].FullName) playwright_core_sha256=$(Get-Sha (Join-Path $NodeModules 'playwright-core\package.json')) winpty=$(& $Python -I -c 'import importlib.metadata as m; print(m.version("pywinpty"))')"

    $script:phase = 'containment'
    . (Join-Path $PSScriptRoot 'stage_prepare.ps1')
    . (Join-Path $PSScriptRoot 'stage_original.ps1')
    if ($Mode -ceq 'FixtureOnly') { . (Join-Path $PSScriptRoot 'stage_fixture.ps1') }
    elseif ($Mode -ceq 'ApprovedImport') { . (Join-Path $PSScriptRoot 'stage_approved_import.ps1') }
    else { Fail 'R06B1_ARGUMENTS' }

    $script:phase = 'cleanup'
    if (-not (Invoke-Cleanup)) { Fail 'R06B1_CLEANUP_FAILED' }
    $work = $null; $drive = $null
    if ($receiptDir) { [IO.File]::WriteAllText("$receiptDir\status.txt", "exit=0`nphase=cleanup`n") }
    Write-Marker 'R06B1_CLEANUP_OK'
    if ($receiptDir) { Write-Marker 'R06B2_PRIVATE_RECEIPT_RETAINED:owner_local_only' }
    $exitCode = 0
} catch {
    [Console]::Error.WriteLine("R06B1_FAILURE_PHASE:$($script:phase)")
    $base = $_.Exception.GetBaseException()
    # Static codes only; arbitrary exception text can carry paths or values.
    if ($base.Message -cmatch '^(R06B[0-9A-Z]+_[A-Z0-9_]+|C9W_[A-Z0-9_]+)') { [Console]::Error.WriteLine($Matches[1]) }
    else { [Console]::Error.WriteLine("UNEXPECTED_$($base.GetType().Name) line=$($_.InvocationInfo.ScriptLineNumber) file=$(Split-Path -Leaf "$($_.InvocationInfo.ScriptName)")") }
    $exitCode = 1
} finally {
    if ($work) {
        if ($env:C9W_KEEP_FIXTURE_LOGS -ceq 'true' -and $Mode -ceq 'FixtureOnly' -and $exitCode -ne 0 -and (Test-Path "$work\dumps")) {
            # Fictional fixture diagnostics only; never reachable in ApprovedImport.
            $keep = Join-Path $Root "fixture-logs-$token"; New-Item -ItemType Directory -Force $keep | Out-Null
            Get-ChildItem "$work\dumps" -File | Where-Object { $_.Extension -in '.log', '.txt', '.err' } | Copy-Item -Destination $keep
        }
        if (Invoke-Cleanup) { [Console]::Error.WriteLine('R06B1_CLEANUP_AFTER_FAILURE_OK') } else { [Console]::Error.WriteLine('R06B1_CLEANUP_FAILED'); $exitCode = 1 }
    }
    if ($receiptDir -and $exitCode -ne 0) { [IO.File]::WriteAllText("$receiptDir\status.txt", "exit=$exitCode`nphase=$($script:phase)`n") }
}
exit $exitCode

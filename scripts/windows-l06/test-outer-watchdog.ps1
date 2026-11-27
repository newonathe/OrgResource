#Requires -Version 7.4
# Fictional process-tree surrogate only. NOT a ConPTY/editor/SQL fixture.
# Approved nonadmin owner-only disposable NTFS root and trusted Python required.
param(
    [Parameter(Mandatory)][string] $FixtureRoot,
    [Parameter(Mandatory)][string] $Python,
    [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{64}$')][string] $PythonSha256,
    [switch] $ConfirmFictional
)
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ControlLock.cs','C9JournalStorage.cs','C9ProfileStorageObservation.cs','C9Contain.cs','C9OwnedJobProbe.cs','C9FictionalConPty.cs','C9PtyHelper.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $ok,[string] $code) { if (-not $ok) { throw $code } }
function Refuses([scriptblock] $action,[string] $code) {
    $caught=$false
    try { $null=& $action }
    catch { if (-not $_.Exception.GetBaseException().Message.Contains($code,[StringComparison]::Ordinal)) { throw }; $caught=$true }
    Require $caught "C9W_TEST_EXPECTED_$code"
}
function Hash([string] $p) { (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash.ToLowerInvariant() }
foreach ($case in @(
    @{Mode='zero';TimedOut=$false;Exit=0;Total=1;Active=0;RootExited=$true},
    @{Mode='nonzero';TimedOut=$false;Exit=23;Total=1;Active=0;RootExited=$true},
    @{Mode='child-zero';TimedOut=$false;Exit=0;Total=2;Active=0;RootExited=$true},
    @{Mode='root-hang';TimedOut=$true;Exit=139;Total=1;Active=1;RootExited=$false},
    @{Mode='descendant-hang';TimedOut=$true;Exit=0;Total=2;Active=1;RootExited=$true},
    @{Mode='helper-tree-hang';TimedOut=$true;Exit=139;Total=2;Active=2;RootExited=$false}
)) {
    $token=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
    $held=[C9ControlLock]::Create($FixtureRoot,$token)
    $marker=Join-Path $held.ControlPath 'ownership.worker-uncertain.tmp'
    try {
        Refuses { [C9OwnedJobProbe]::RunFictionalOuterWatchdog($held,$Python,('0'*64),$case.Mode,10000) } 'C9W_WATCHDOG_TOOL'
        Require (-not (Test-Path -LiteralPath $marker)) 'C9W_TEST_PREFLIGHT_MARKER'
        $watch=[Diagnostics.Stopwatch]::StartNew()
        $r=[C9OwnedJobProbe]::RunFictionalOuterWatchdog($held,$Python,$PythonSha256,$case.Mode,10000)
        Require ($r.Mode -ceq $case.Mode -and $r.TimedOut -eq $case.TimedOut -and
            $r.RootExitCode -eq $case.Exit -and $r.RootExitedBeforeStop -eq $case.RootExited -and
            $r.TotalProcesses -eq $case.Total -and $r.ActiveBeforeStop -eq $case.Active -and
            $r.ActiveAfterStop -eq 0 -and $watch.ElapsedMilliseconds -lt 30000) 'C9W_TEST_OUTER_OBSERVATION'
        Require ([IO.File]::ReadAllText($marker) -ceq ('C9W_JOURNAL_WORKER_UNCERTAIN'+[char]10)) 'C9W_TEST_OUTER_MARKER'
        $digest=Hash $marker
        Refuses { [C9OwnedJobProbe]::RunFictionalOuterWatchdog($held,$Python,$PythonSha256,'zero',10000) } 'C9W_JOURNAL_WORKER_QUARANTINED'
        Refuses { [C9OwnedJobProbe]::RunFictional($held,$Python,$PythonSha256,'zero',10000) } 'C9W_JOURNAL_WORKER_QUARANTINED'
        Require ((Hash $marker) -ceq $digest) 'C9W_TEST_OUTER_RETRY_CHANGED'
    } finally { $held.Dispose() }
    $held=[C9ControlLock]::OpenExisting($FixtureRoot,$token)
    try {
        Refuses { [C9OwnedJobProbe]::RunFictionalOuterWatchdog($held,$Python,$PythonSha256,'zero',10000) } 'C9W_JOURNAL_STAGING_AMBIGUOUS'
        Refuses { [C9OwnedJobProbe]::RunFictional($held,$Python,$PythonSha256,'zero',10000) } 'C9W_JOURNAL_STAGING_AMBIGUOUS'
        Require ((Hash $marker) -ceq $digest) 'C9W_TEST_OUTER_REOPEN_CHANGED'
    } finally { $held.Dispose() }
    Write-Output ('C9W_OUTER_SURROGATE_CASE_OK:'+$case.Mode)
}
Write-Output 'C9W_OUTER_SURROGATE_FIXTURE_OK:fictional_only=true actual_conpty_not_run=true intent_retained=true'

#Requires -Version 7.4
# H06 native fictional job probe. No actual credential, SQL, profiles, mapping or editor.
param(
    [Parameter(Mandatory)][string] $FixtureRoot,
    [Parameter(Mandatory)][string] $Python,
    [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{64}$')][string] $PythonSha256,
    [switch] $ConfirmFictional
)
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ControlLock.cs','C9JournalStorage.cs','C9ProfileStorageObservation.cs','C9Contain.cs','C9OwnedJobProbe.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $ok,[string] $code) { if (-not $ok) { throw $code } }
function Refuses([scriptblock] $action,[string] $code) {
    $caught=$false
    try { $null=& $action }
    catch { if (-not $_.Exception.GetBaseException().Message.Contains($code,[StringComparison]::Ordinal)) { throw }; $caught=$true }
    Require $caught "C9W_TEST_EXPECTED_$code"
}
$token=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
$held=[C9ControlLock]::Create($FixtureRoot,$token)
try {
    Refuses { [C9OwnedJobProbe]::RunFictional($held,$Python,('0'*64),'zero',8000) } 'C9W_WATCHDOG_TOOL'
    Refuses { [C9OwnedJobProbe]::RunFictional($held,$Python,$PythonSha256,'invalid',8000) } 'C9W_WATCHDOG_MODE'
    Refuses { [C9OwnedJobProbe]::RunFictional($held,$Python,$PythonSha256,'zero',0) } 'C9W_WATCHDOG_ARGUMENT'
    # A second independently owned process outside the probe job remains alive.
    # No PID/name-based discovery or kill. Use a held Process object only.
    Require ([IO.Path]::IsPathFullyQualified($Python) -and [IO.Path]::GetFullPath($Python) -ceq $Python -and [IO.Path]::GetFileName($Python) -ieq 'python.exe') 'C9W_TEST_PYTHON_PATH'
    Require (([int][IO.File]::GetAttributes($Python) -band 0x441410) -eq 0) 'C9W_TEST_PYTHON_ATTRIBUTES'
    $dir=[C9DirectoryIdentity]::Open([IO.Path]::GetDirectoryName($Python))
    $pin=$null
    try {
    $pin=[IO.FileStream]::new($Python,[IO.FileMode]::Open,[IO.FileAccess]::Read,[IO.FileShare]::Read)
    Require ([Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($pin)).ToLowerInvariant() -ceq $PythonSha256) 'C9W_TEST_PYTHON_HASH'
    $psi=[Diagnostics.ProcessStartInfo]::new($Python)
    $psi.UseShellExecute=$false; $psi.CreateNoWindow=$true
    $psi.WorkingDirectory=$held.ControlPath
    $psi.Environment.Clear()
    $psi.Environment['SystemRoot']=$env:SystemRoot; $psi.Environment['SystemDrive']=$env:SystemDrive
    foreach ($arg in '-I','-S','-B','-c','import time; time.sleep(120)') { $psi.ArgumentList.Add($arg) }
    $sentinel=[Diagnostics.Process]::Start($psi)
    Require ($null -ne $sentinel) 'C9W_TEST_SENTINEL_START'
    try {
        $null=$sentinel.Handle
        Require (-not $sentinel.HasExited) 'C9W_TEST_SENTINEL_PREMATURE_EXIT'
        foreach ($mode in 'zero','nonzero','child-zero','root-hang','descendant-hang') {
            $before=[Diagnostics.Stopwatch]::StartNew()
            $r=[C9OwnedJobProbe]::RunFictional($held,$Python,$PythonSha256,$mode,8000)
            Require ($r.Mode -ceq $mode -and $r.ActiveAfterStop -eq 0 -and $before.ElapsedMilliseconds -lt 30000) 'C9W_TEST_WATCHDOG_BOUND'
            switch ($mode) {
                'zero' { Require (-not $r.TimedOut -and $r.RootExitedBeforeStop -and $r.RootExitCode -eq 0 -and $r.TotalProcesses -eq 1) 'C9W_TEST_ZERO' }
                'nonzero' { Require (-not $r.TimedOut -and $r.RootExitedBeforeStop -and $r.RootExitCode -eq 23 -and $r.TotalProcesses -eq 1) 'C9W_TEST_NONZERO' }
                'child-zero' { Require (-not $r.TimedOut -and $r.RootExitedBeforeStop -and $r.RootExitCode -eq 0 -and $r.TotalProcesses -eq 2) 'C9W_TEST_CHILD_ZERO' }
                'root-hang' { Require ($r.TimedOut -and -not $r.RootExitedBeforeStop -and $r.RootExitCode -eq 139 -and $r.ActiveBeforeStop -eq 1 -and $r.TotalProcesses -eq 1) 'C9W_TEST_ROOT_HANG' }
                'descendant-hang' { Require ($r.TimedOut -and $r.RootExitedBeforeStop -and $r.RootExitCode -eq 0 -and $r.ActiveBeforeStop -eq 1 -and $r.TotalProcesses -eq 2) 'C9W_TEST_DESCENDANT_HANG' }
            }
            Require (-not $sentinel.HasExited) 'C9W_TEST_UNRELATED_SENTINEL_STOPPED'
        }
    } finally {
        try {
            if (-not $sentinel.HasExited) { $sentinel.Kill(); Require ($sentinel.WaitForExit(10000)) 'C9W_TEST_SENTINEL_STOP_UNKNOWN' }
        } finally { $sentinel.Dispose() }
    }
    } finally { if ($null -ne $pin) { $pin.Dispose() }; $dir.Dispose() }
} finally { $held.Dispose() }
Refuses { [C9OwnedJobProbe]::RunFictional($held,$Python,$PythonSha256,'zero',8000) } 'C9W_CONTROL_LOCK_NOT_HELD'
[Console]::WriteLine('C9W_H06_OWNED_JOB_FIXTURE_OK:root_exit=true descendant_accounting=true timeout=true unrelated_sentinel=true credentials=false native_faults_not_injected=true')

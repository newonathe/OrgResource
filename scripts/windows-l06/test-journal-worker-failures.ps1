#Requires -Version 7.4
# Fictional journal transport failures only; no real credentials, data or recovery.
param(
    [Parameter(Mandatory)][string] $FixtureRoot,
    [Parameter(Mandatory)][string] $Python,
    [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{64}$')][string] $PythonSha256,
    [switch] $ConfirmFictional
)
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ControlLock.cs','C9JournalStorage.cs','C9JournalParent.cs','C9DosDeviceObservation.cs','C9LogonContext.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $ok,[string] $code) { if (-not $ok) { throw $code } }
function Refuses([scriptblock] $action,[string] $code) {
    $caught=$false
    try { $null=& $action }
    catch {
        if (-not $_.Exception.GetBaseException().Message.Contains($code,[StringComparison]::Ordinal)) { throw }
        $caught=$true
    }
    Require $caught "C9W_TEST_EXPECTED_$code"
}
function Hash([string] $p) { (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash.ToLowerInvariant() }
function Write-New([string] $p,[string] $s) {
    $bytes=[Text.Encoding]::ASCII.GetBytes($s)
    $f=[IO.FileStream]::new($p,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write,[IO.FileShare]::None)
    try { $f.Write($bytes,0,$bytes.Length); $f.Flush($true) } finally { $f.Dispose() }
}
function New-Parent($held,[string] $bridge) {
    $dir=[IO.Path]::GetDirectoryName($bridge)
    [C9JournalParent]::new($held,$Python,$PythonSha256,$bridge,(Hash $bridge),
        (Hash (Join-Path $dir 'ownership_journal.py')),(Hash (Join-Path $dir 'ownership_journal_v2.py')))
}
$cases=@(
    @{name='completed_nonzero'; script='import sys; sys.stdin.buffer.read(); raise SystemExit(23)'; expected='C9W_JOURNAL_CHILD'; quarantine=$false},
    @{name='stdout_overflow'; script='import sys,time; sys.stdin.buffer.read(); sys.stdout.buffer.write(b"x"*131072); sys.stdout.buffer.flush(); time.sleep(120)'; expected='C9W_JOURNAL_WORKER_QUARANTINED'; quarantine=$true},
    @{name='stderr_overflow'; script='import sys,time; sys.stdin.buffer.read(); sys.stderr.buffer.write(b"x"*4096); sys.stderr.buffer.flush(); time.sleep(120)'; expected='C9W_JOURNAL_WORKER_QUARANTINED'; quarantine=$true},
    @{name='hang'; script='import time; time.sleep(120)'; expected='C9W_JOURNAL_WORKER_QUARANTINED'; quarantine=$true}
)
foreach ($case in $cases) {
    $token=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
    $held=[C9ControlLock]::Create($FixtureRoot,$token)
    $bridge=Join-Path $held.ControlPath 'ownership_bridge_v2.py'
    $marker=Join-Path $held.ControlPath 'ownership.worker-uncertain.tmp'
    $journal=Join-Path $held.ControlPath 'ownership.json'
    try {
        Write-New $bridge $case.script
        Write-New (Join-Path $held.ControlPath 'ownership_journal.py') '# fictional unused sibling'
        Write-New (Join-Path $held.ControlPath 'ownership_journal_v2.py') '# fictional unused sibling'
        $before=Hash $bridge
        $p=New-Parent $held $bridge
        $watch=[Diagnostics.Stopwatch]::StartNew()
        Refuses { $p.Invoke('init') } $case.expected
        Require (-not (Test-Path -LiteralPath $journal)) 'C9W_TEST_WORKER_PUBLISHED'
        Require ((Hash $bridge) -ceq $before) 'C9W_TEST_WORKER_SOURCE_CHANGED'
        Require ($watch.ElapsedMilliseconds -lt 35000) 'C9W_TEST_WORKER_BOUND'
        if ($case.name -like '*overflow') {
            Require ($watch.ElapsedMilliseconds -lt 15000) 'C9W_TEST_WORKER_NOT_FAIL_FAST'
        }
        if ($case.quarantine) {
            Require ([IO.File]::ReadAllText($marker) -ceq ('C9W_JOURNAL_WORKER_UNCERTAIN'+[char]10)) 'C9W_TEST_WORKER_MARKER'
            $hash=Hash $marker
            $again=New-Parent $held $bridge
            Refuses { $again.Invoke('init') } 'C9W_JOURNAL_WORKER_QUARANTINED'
            Require ((Hash $marker) -ceq $hash -and -not (Test-Path -LiteralPath $journal)) 'C9W_TEST_WORKER_RETRY_WROTE'
        } else {
            Require (-not (Test-Path -LiteralPath $marker)) 'C9W_TEST_COMPLETED_MARKER'
        }
    } finally { $held.Dispose() }
    if ($case.quarantine) {
        $held=[C9ControlLock]::OpenExisting($FixtureRoot,$token)
        try {
            $again=New-Parent $held $bridge
            Refuses { $again.Invoke('init') } 'C9W_JOURNAL_STAGING_AMBIGUOUS'
            Require ((Hash $marker) -ceq $hash -and -not (Test-Path -LiteralPath $journal)) 'C9W_TEST_WORKER_REOPEN_WROTE'
        } finally { $held.Dispose() }
    }
    [Console]::WriteLine('C9W_JOURNAL_WORKER_CASE_OK:'+ $case.name)
}
[Console]::WriteLine('C9W_JOURNAL_WORKER_FIXTURE_OK:fictional_only=true no_publication=true retained_quarantine=true full_tree_not_proven=true')

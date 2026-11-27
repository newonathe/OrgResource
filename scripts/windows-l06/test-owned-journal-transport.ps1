#Requires -Version 7.4
# One-shot fictional transport only. Requires a preapproved nonadmin owner-only NTFS root.
# No journal publication, credential, profile, SQL, mapping or recovery.
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
    catch {
        if (-not $_.Exception.GetBaseException().Message.Contains($code,[StringComparison]::Ordinal)) { throw }
        $caught=$true
    }
    Require $caught "C9W_TEST_EXPECTED_$code"
}
function Hash([string] $p) { (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash.ToLowerInvariant() }
foreach ($case in @(
    @{Mode='echo';Exit=0;Failure='';Timeout=8000},
    @{Mode='nonzero';Exit=23;Failure='';Timeout=8000},
    @{Mode='child-denied';Exit=0;Failure='';Timeout=8000},
    @{Mode='overflow';Exit=-1;Failure='C9W_WORKER_PIPE_OVERFLOW';Timeout=8000},
    @{Mode='hang';Exit=-1;Failure='C9W_WORKER_TIMEOUT';Timeout=3000}
)) {
    $token=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
    $held=[C9ControlLock]::Create($FixtureRoot,$token)
    $marker=Join-Path $held.ControlPath 'ownership.worker-uncertain.tmp'
    $journal=Join-Path $held.ControlPath 'ownership.json'
    try {
        Refuses { [C9OwnedJobProbe]::RunFictionalJournalTransport($held,$Python,('0'*64),$case.Mode,$case.Timeout) } 'C9W_WORKER_TOOL'
        Require (-not (Test-Path -LiteralPath $marker)) 'C9W_TEST_PREFLIGHT_MARKER'
        $watch=[Diagnostics.Stopwatch]::StartNew()
        if ($case.Failure) {
            Refuses { [C9OwnedJobProbe]::RunFictionalJournalTransport($held,$Python,$PythonSha256,$case.Mode,$case.Timeout) } $case.Failure
        } else {
            $exit=[C9OwnedJobProbe]::RunFictionalJournalTransport($held,$Python,$PythonSha256,$case.Mode,$case.Timeout)
            Require ($exit -eq $case.Exit) 'C9W_TEST_WORKER_EXIT'
        }
        Require ($watch.ElapsedMilliseconds -lt 35000) 'C9W_TEST_WORKER_BOUND'
        Require (-not (Test-Path -LiteralPath $journal)) 'C9W_TEST_UNEXPECTED_PUBLICATION'
        Require ([IO.File]::ReadAllText($marker) -ceq ('C9W_JOURNAL_WORKER_UNCERTAIN'+[char]10)) 'C9W_TEST_RETAINED_INTENT'
        $digest=Hash $marker
        Refuses { [C9OwnedJobProbe]::RunFictionalJournalTransport($held,$Python,$PythonSha256,'echo',8000) } 'C9W_JOURNAL_WORKER_QUARANTINED'
        Require ((Hash $marker) -ceq $digest) 'C9W_TEST_RETRY_CHANGED_INTENT'
    } finally { $held.Dispose() }
    $held=[C9ControlLock]::OpenExisting($FixtureRoot,$token)
    try {
        Refuses { [C9OwnedJobProbe]::RunFictionalJournalTransport($held,$Python,$PythonSha256,'echo',8000) } 'C9W_JOURNAL_STAGING_AMBIGUOUS'
        Require ((Hash $marker) -ceq $digest -and -not (Test-Path -LiteralPath $journal)) 'C9W_TEST_REOPEN_CHANGED_INTENT'
    } finally { $held.Dispose() }
    Write-Output ('C9W_OWNED_TRANSPORT_CASE_OK:'+$case.Mode)
}
Write-Output 'C9W_OWNED_TRANSPORT_FIXTURE_OK:fictional_only=true intent_retained=true native_acceptance_separate=true'

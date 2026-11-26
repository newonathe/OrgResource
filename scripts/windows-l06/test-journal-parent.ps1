#Requires -Version 7.4
# Native fictional-only journal parent integration; no database or OS profile effects.
param(
    [Parameter(Mandatory)][string] $FixtureRoot,
    [Parameter(Mandatory)][string] $Python,
    [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{64}$')][string] $PythonSha256,
    [switch] $ConfirmFictional
)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ControlLock.cs','C9JournalStorage.cs','C9JournalParent.cs','C9DosDeviceObservation.cs','C9LogonContext.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $Condition, [string] $Code) { if (-not $Condition) { throw $Code } }
function Hash([string] $Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Refuses([scriptblock] $Action, [string] $Expected) {
    $caught = $false
    try { $null = & $Action }
    catch { if ($_.Exception.ToString() -notmatch [regex]::Escape($Expected)) { throw }; $caught = $true }
    Require $caught "C9W_TEST_EXPECTED_REFUSAL_$Expected"
}
$token = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
$bridge = Join-Path $PSScriptRoot 'ownership_bridge.py'
$journal = Join-Path $PSScriptRoot 'ownership_journal.py'
$held = [C9ControlLock]::Create($FixtureRoot, $token)
try {
    $parent = [C9JournalParent]::new($held, $Python, $PythonSha256, $bridge, (Hash $bridge), (Hash $journal))
    $path = Join-Path $held.ControlPath 'ownership.json'
    $bad = [C9JournalParent]::new($held, $Python, ('0'*64), $bridge, (Hash $bridge), (Hash $journal))
    Refuses { $bad.Invoke('init') } 'C9W_JOURNAL_TOOL'
    Require (-not (Test-Path -LiteralPath $path)) 'C9W_TEST_TOOL_REFUSAL_WROTE'
    $first = $parent.Invoke('init') | ConvertFrom-Json
    Require ((Hash $path) -ceq $first.sha256) 'C9W_TEST_INIT_HASH'
    Refuses { $parent.Invoke('init') } 'C9W_JOURNAL_STATE'
    $intent = @{ expectedSha256=$first.sha256; resource='work'; state='intent'; fields=@{} } | ConvertTo-Json -Compress
    $next = $parent.Invoke('transition', $intent) | ConvertFrom-Json
    Require ((Hash $path) -ceq $next.sha256 -and $next.sha256 -cne $first.sha256) 'C9W_TEST_INTENT_HASH'
    Refuses { $parent.Invoke('transition', $intent) } 'C9W_JOURNAL_STALE'
    $read = $parent.Invoke('read') | ConvertFrom-Json
    Require ($read.sha256 -ceq $next.sha256 -and $read.journal.work.state -ceq 'intent') 'C9W_TEST_READ'
    $tmp = Join-Path $held.ControlPath 'ownership.fictional.tmp'
    [IO.File]::WriteAllText($tmp, 'fictional interrupted staging')
    Refuses { $parent.Invoke('read') } 'C9W_JOURNAL_STAGING_AMBIGUOUS'
    Require ((Hash $path) -ceq $next.sha256 -and (Test-Path -LiteralPath $tmp)) 'C9W_TEST_STAGING_CHANGED'
} finally { $held.Dispose() }
Refuses { $parent.Invoke('read') } 'C9W_CONTROL_LOCK_NOT_HELD'
[Console]::WriteLine('C9W_JOURNAL_PARENT_FIXTURE_OK:tool_refusal=true exact_readback=true stale_refused=true staging_preserved=true disposed_refused=true')

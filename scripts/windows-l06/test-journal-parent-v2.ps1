#Requires -Version 7.4
# Fictional native parent integration only; no profiles, work creation, mappings or database.
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
function Require([bool] $Condition,[string] $Code) { if (-not $Condition) { throw $Code } }
function Hash([string] $Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Refuses([scriptblock] $Action,[string] $Expected) {
    $caught = $false
    try { $null = & $Action }
    catch { if ($_.Exception.ToString() -notmatch [regex]::Escape($Expected)) { throw }; $caught = $true }
    Require $caught "C9W_TEST_EXPECTED_REFUSAL_$Expected"
}
function Token { [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant() }
$bridge = Join-Path $PSScriptRoot 'ownership_bridge_v2.py'
$journal = Join-Path $PSScriptRoot 'ownership_journal.py'
$v2 = Join-Path $PSScriptRoot 'ownership_journal_v2.py'
$oldBridge = Join-Path $PSScriptRoot 'ownership_bridge.py'
$held = [C9ControlLock]::Create($FixtureRoot,(Token))
try {
    $path = Join-Path $held.ControlPath 'ownership.json'
    $bad = [C9JournalParent]::new($held,$Python,$PythonSha256,$bridge,(Hash $bridge),(Hash $journal),('0'*64))
    Refuses { $bad.Invoke('init') } 'C9W_JOURNAL_TOOL'
    Require (-not (Test-Path -LiteralPath $path)) 'C9W_TEST_V2_TOOL_WROTE'
    $parent = [C9JournalParent]::new($held,$Python,$PythonSha256,$bridge,(Hash $bridge),(Hash $journal),(Hash $v2))
    $first = $parent.Invoke('init') | ConvertFrom-Json
    $read = $parent.Invoke('read') | ConvertFrom-Json
    $ctx = [C9LogonContext]::Read()
    Require ($first.sha256 -ceq (Hash $path) -and $read.sha256 -ceq $first.sha256) 'C9W_TEST_V2_HASH'
    Require ($read.journal.schema -ceq 'orgresource.windows-l06-ownership.v2') 'C9W_TEST_V2_SCHEMA'
    Require ($read.journal.context.userSid -ceq $ctx.UserSid -and $read.journal.context.authenticationId -ceq $ctx.AuthenticationId -and $read.journal.context.sessionId -eq $ctx.SessionId -and $read.journal.context.logonTime -ceq $ctx.LogonTime -and $null -eq $read.journal.context.bootId) 'C9W_TEST_V2_CONTEXT'
    Refuses { $parent.Invoke('init') } 'C9W_JOURNAL_STATE'
    Refuses { $parent.Invoke('read','{"context":{}}') } 'C9W_JOURNAL_JSON'
    $intent = @{expectedSha256=$first.sha256;resource='work';state='intent';fields=@{}} | ConvertTo-Json -Compress
    $next = $parent.Invoke('transition',$intent) | ConvertFrom-Json
    Require ($next.sha256 -ceq (Hash $path) -and $next.sha256 -cne $first.sha256) 'C9W_TEST_V2_INTENT'
    Refuses { $parent.Invoke('transition',$intent) } 'C9W_JOURNAL_STALE'
    $mapping = @{expectedSha256=$next.sha256;resource='mapping';state='intent';fields=@{drive='Q:'}} | ConvertTo-Json -Compress
    Refuses { $parent.Invoke('transition',$mapping) } 'C9W_JOURNAL_EFFECT_REFUSED'
    $remove = @{expectedSha256=$next.sha256;resource='work';state='removing';fields=@{}} | ConvertTo-Json -Compress
    Refuses { $parent.Invoke('transition',$remove) } 'C9W_JOURNAL_EFFECT_REFUSED'
    $plan = @{expectedSha256=$next.sha256;observed=@{}} | ConvertTo-Json -Compress
    Refuses { $parent.Invoke('plan',$plan) } 'C9W_JOURNAL_EFFECT_REFUSED'
    $legacy = [C9JournalParent]::new($held,$Python,$PythonSha256,$oldBridge,(Hash $oldBridge),(Hash $journal))
    Refuses { $legacy.Invoke('read') } 'C9W_JOURNAL_CHILD'
    Require ((Hash $path) -ceq $next.sha256) 'C9W_TEST_V2_REFUSAL_CHANGED'
} finally { $held.Dispose() }
Refuses { $parent.Invoke('read') } 'C9W_CONTROL_LOCK_NOT_HELD'

# An actual legacy snapshot must remain byte-identical on v2 refusal.
$held = [C9ControlLock]::Create($FixtureRoot,(Token))
try {
    $legacy = [C9JournalParent]::new($held,$Python,$PythonSha256,$oldBridge,(Hash $oldBridge),(Hash $journal))
    $receipt = $legacy.Invoke('init') | ConvertFrom-Json
    $path = Join-Path $held.ControlPath 'ownership.json'
    $parent = [C9JournalParent]::new($held,$Python,$PythonSha256,$bridge,(Hash $bridge),(Hash $journal),(Hash $v2))
    Refuses { $parent.Invoke('init') } 'C9W_JOURNAL_STATE'
    # Strict shape rejects legacy record before the child can run.
    Refuses { $parent.Invoke('read') } 'C9W_JOURNAL_JSON'
    Require ((Hash $path) -ceq $receipt.sha256) 'C9W_TEST_LEGACY_CHANGED'
} finally { $held.Dispose() }
[Console]::WriteLine('C9W_JOURNAL_V2_PARENT_FIXTURE_OK:actual_context=true pinned_v2=true exact_readback=true stale_refused=true effects_refused=true legacy_preserved=true')

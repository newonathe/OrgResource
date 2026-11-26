#Requires -Version 7.4
# Fictional-only, isolated owner-controlled NTFS root. Retains all created control artifacts.
param([Parameter(Mandatory)][string] $FixtureRoot, [switch] $ConfirmFictional)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @((Join-Path $PSScriptRoot 'C9DirectoryIdentity.cs'), (Join-Path $PSScriptRoot 'C9ControlLock.cs'))
function Require([bool] $Condition, [string] $Code) { if (-not $Condition) { throw $Code } }
function Refuses([scriptblock] $Action, [string] $Expected) {
    $caught = $false
    try { $unexpected = & $Action; if ($unexpected -is [IDisposable]) { $unexpected.Dispose() } }
    catch { if ($_.Exception.ToString() -notmatch [regex]::Escape($Expected)) { throw }; $caught = $true }
    Require $caught "C9W_TEST_EXPECTED_REFUSAL_$Expected"
}
$token = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
$missing = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
$control = Join-Path $FixtureRoot "orgresource-c9w-control-$token"
$absent = Join-Path $FixtureRoot "orgresource-c9w-control-$missing"
Require (-not (Test-Path -LiteralPath $control)) 'C9W_TEST_COLLISION'
Require (-not (Test-Path -LiteralPath $absent)) 'C9W_TEST_COLLISION'
Refuses { [C9ControlLock]::OpenExisting($FixtureRoot, $missing) } 'C9W_DIRECTORY_OPEN'
Require (-not (Test-Path -LiteralPath $absent)) 'C9W_TEST_RECOVERY_CREATED_CONTROL'
Refuses { [C9ControlLock]::Create($FixtureRoot, '../invalid') } 'C9W_CONTROL_ARGUMENT'
$a = [C9ControlLock]::Create($FixtureRoot, $token)
try {
    $a.AssertHeld()
    $rootId = $a.RootId; $controlId = $a.ControlId
    Require ($rootId -cne $controlId -and $rootId -cmatch '^[a-f0-9]{16}:[a-f0-9]{32}$' -and $controlId -cmatch '^[a-f0-9]{16}:[a-f0-9]{32}$') 'C9W_TEST_ID'
    Refuses { [C9ControlLock]::Create($FixtureRoot, $token) } 'C9W_CONTROL_CREATE'
    Refuses { [C9ControlLock]::OpenExisting($FixtureRoot, $token) } 'C9W_CONTROL_LOCK_REFUSED'
} finally { $a.Dispose() }
Refuses { $a.AssertHeld() } 'C9W_CONTROL_LOCK_NOT_HELD'
$b = [C9ControlLock]::OpenExisting($FixtureRoot, $token)
try {
    Require ($b.RootId -ceq $rootId -and $b.ControlId -ceq $controlId) 'C9W_TEST_ID_CHANGED'
    Refuses { [C9ControlLock]::OpenExisting($FixtureRoot, $token) } 'C9W_CONTROL_LOCK_REFUSED'
} finally { $b.Dispose() }
Require ((Test-Path -LiteralPath (Join-Path $control 'ownership.lock') -PathType Leaf) -and (Get-Item -LiteralPath (Join-Path $control 'ownership.lock')).Length -eq 0) 'C9W_TEST_LOCK_NOT_RETAINED'
[Console]::WriteLine('C9W_CONTROL_LOCK_FIXTURE_OK:exclusive_create=true competing_open_refused=true stable_identity=true missing_refused=true artifacts_retained=true')

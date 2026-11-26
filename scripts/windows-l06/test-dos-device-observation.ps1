#Requires -Version 7.4
# Read-only native observation. Never maps or unmaps a drive, emits no target or SID.
param([switch] $ConfirmFictional)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path (Join-Path $PSScriptRoot 'C9DosDeviceObservation.cs')
function Require([bool] $Condition, [string] $Code) { if (-not $Condition) { throw $Code } }
function Refuses([scriptblock] $Action, [string] $Expected) {
    $caught = $false
    try { $null = & $Action }
    catch { if ($_.Exception.ToString() -notmatch [regex]::Escape($Expected)) { throw }; $caught = $true }
    Require $caught "C9W_TEST_EXPECTED_REFUSAL_$Expected"
}
foreach ($invalid in '', 'q:', 'Q:\', 'Q', '\\?\Q:', 'Q:extra') {
    Refuses { [C9DosDeviceObservation]::Read($invalid) } 'C9W_DOS_DRIVE'
}
$systemDrive = [IO.Path]::GetPathRoot([Environment]::SystemDirectory).Substring(0,2).ToUpperInvariant()
$a = [C9DosDeviceObservation]::Read($systemDrive)
$b = [C9DosDeviceObservation]::Read($systemDrive)
Require ($a.Status -ceq 'OBSERVED' -and $a.Targets.Count -ge 1 -and $a.NativeError -eq 0) 'C9W_DOS_SYSTEM_POSITIVE'
Require ($a.AuthenticationId -cmatch '^[a-f0-9]{16}$' -and $a.UserSid -cmatch '^S-1-') 'C9W_DOS_CONTEXT_SHAPE'
Require ($a.SameObservation($b)) 'C9W_DOS_OBSERVATION_CHANGED'
$clone = $a.Targets
$clone[0] = 'fictional mutation'
Require ($a.SameObservation($b)) 'C9W_DOS_TARGETS_MUTABLE'
foreach ($drive in 'Q:', 'R:', 'U:', 'V:', 'W:') {
    $seen = [C9DosDeviceObservation]::Read($drive)
    Require ($seen.UserSid -ceq $a.UserSid -and $seen.AuthenticationId -ceq $a.AuthenticationId) 'C9W_DOS_CONTEXT_CHANGED'
    if ($seen.Status -ceq 'OBSERVED') {
        Require ($seen.NativeError -eq 0 -and $seen.Targets.Count -ge 1) 'C9W_DOS_OBSERVED_SHAPE'
    } elseif ($seen.Status -ceq 'UNRESOLVED') {
        Require ($seen.NativeError -ne 0 -and $seen.Targets.Count -eq 0) 'C9W_DOS_UNKNOWN_SHAPE'
    } else { throw 'C9W_DOS_UNKNOWN_STATUS' }
}
[Console]::WriteLine('C9W_DOS_OBSERVER_FIXTURE_OK:system_positive=true context_bound=true targets_immutable=true errors_not_absence=true read_only=true')

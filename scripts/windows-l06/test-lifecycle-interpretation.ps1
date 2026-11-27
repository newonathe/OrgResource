#Requires -Version 7.4
# Pure interpretation fixture. Compiles exact native source, never starts a process,
# opens a real token, creates a job/profile, or establishes an H06 native receipt.
param([switch] $ConfirmFictional)
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
if (-not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ProfileStorageObservation.cs','C9Contain.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $ok,[string] $code) { if (-not $ok) { throw $code } }
function Refuses([scriptblock] $action,[string] $code) {
    $caught=$false
    try { $null=& $action }
    catch { if (-not $_.Exception.GetBaseException().Message.Contains($code,[StringComparison]::Ordinal)) { throw }; $caught=$true }
    Require $caught "EXPECTED_$code"
}
Require ([C9Lifecycle]::WaitResult(0,5)) 'WAIT_SIGNALED'
Require (-not [C9Lifecycle]::WaitResult(258,5)) 'WAIT_TIMEOUT'
Refuses { [C9Lifecycle]::WaitResult([uint32]::MaxValue,6) } 'C9W_PROCESS_WAIT_FAILED'
Refuses { [C9Lifecycle]::WaitResult(128,0) } 'C9W_PROCESS_WAIT_UNKNOWN'
[C9Lifecycle]::RequireInitialResume(1)
foreach ($n in @([uint32]0,[uint32]2,[uint32]::MaxValue)) {
    Refuses { [C9Lifecycle]::RequireInitialResume($n) } 'C9W_THREAD_RESUME_STATE'
}
[C9Lifecycle]::RequireTokenSize($false,122,64,8)
Refuses { [C9Lifecycle]::RequireTokenSize($true,122,64,8) } 'C9W_TOKEN_SIZE'
Refuses { [C9Lifecycle]::RequireTokenSize($false,5,64,8) } 'C9W_TOKEN_SIZE'
Refuses { [C9Lifecycle]::RequireTokenSize($false,122,4,8) } 'C9W_TOKEN_SIZE'
Refuses { [C9Lifecycle]::RequireTokenSize($false,122,1048577,8) } 'C9W_TOKEN_SIZE'
[C9Lifecycle]::RequireTokenLength(64,64,8)
Refuses { [C9Lifecycle]::RequireTokenLength(65,64,8) } 'C9W_TOKEN_LENGTH'
Refuses { [C9Lifecycle]::RequireTokenLength(7,64,8) } 'C9W_TOKEN_LENGTH'
[Console]::WriteLine('C9W_H06_LIFECYCLE_INTERPRETATION_OK:fictional_only=true native_faults_not_run=true')

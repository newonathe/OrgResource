#Requires -Version 7.4
# Fictional caller-spy only; no database, process or credential file operation.
param([switch] $ConfirmFictional)
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
if (-not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
. (Join-Path $PSScriptRoot 'windows_common.ps1')
$script:effects=0
function Fail([string] $Code) { throw [InvalidOperationException]::new($Code) }
function Invoke-Root { $script:effects++; throw 'UNEXPECTED_SQL' }
function New-Secret { $script:effects++; throw 'UNEXPECTED_SECRET' }
function Invoke-Contained { $script:effects++; throw 'UNEXPECTED_PROCESS' }
function Remove-Item { $script:effects++; throw 'UNEXPECTED_DELETE' }
foreach ($role in 'source','restore','mixed') {
    foreach ($kind in 'backup','restore') {
        foreach ($action in @({ Grant-Transport $role $kind },{ Revoke-Transport $role $kind })) {
            $caught=$false
            try { & $action }
            catch {
                if ($_.Exception.GetBaseException().Message -cne 'C9W_PROTECTED_LOGIN_REFUSED_H06_INCOMPLETE') { throw }
                $caught=$true
            }
            if (-not $caught -or $script:effects -ne 0) { throw 'C9W_TEST_TRANSPORT_EFFECT' }
        }
    }
}
[Console]::WriteLine('C9W_H06_TRANSPORT_QUARANTINE_OK:fictional_spy=true no_effects=true native_not_run=true')

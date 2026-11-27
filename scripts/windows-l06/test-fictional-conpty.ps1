#Requires -Version 7.4
# Fictional ConPTY source fixture: do not run before independent outer watchdog acceptance.
# No real editor, login file, credential, SQL, role or user profile.
param(
    [Parameter(Mandatory)][string] $FixtureRoot,
    [Parameter(Mandatory)][string] $Python,
    [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{64}$')][string] $PythonSha256,
    [switch] $ConfirmFictional
)
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
# The native launcher is not authorized by this staged fixture alone.
throw 'C9W_PTY_FIXTURE_OUTER_WATCHDOG_REQUIRED'

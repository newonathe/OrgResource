# H03 quarantine: legacy name-inference recovery is unsafe. Preserve the work directory and
# all native resources for owner reconciliation. Do not acquire a lock in an unverified
# directory, enumerate profiles, remove mappings, terminate processes or delete files here.
# The predecessor remains available as historical source, never as an execution recipe.
#Requires -Version 7.4
param([Parameter(Mandatory)][string] $Root, [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{12}$')][string] $Token)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
[Console]::Error.WriteLine('C9W_RECOVER_REFUSED_H03_INCOMPLETE')
exit 1

# After an interrupted run (power loss, closed terminal): the kernel has already ended every contained child, because the
# only Job Object handles died with the runner. This removes only that run's drive mapping and container profiles and keeps
# the work directory for owner inspection. It ends no process and deletes no file.
#Requires -Version 7.4
param([Parameter(Mandatory)][string] $Root, [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{12}$')][string] $Token)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
$me = [Security.Principal.WindowsIdentity]::GetCurrent()
. (Join-Path $PSScriptRoot 'windows_common.ps1')
$work = Join-Path $Root "orgresource-c9w-$Token"
if (-not (Test-PlainPath $Root 'Container') -or -not (Test-OwnerOnly $Root) -or -not (Test-PlainPath $work 'Container')) { [Console]::Error.WriteLine('C9W_RECOVER_REFUSED_PATH'); exit 1 }
$names = @(Get-ChildItem -Path "$work\*\Packages" -Directory -ErrorAction SilentlyContinue | ForEach-Object { Get-ChildItem -LiteralPath $_.FullName -Directory } | Where-Object Name -CMatch '^orgresource\.c9\.[a-f0-9]{12}$' | ForEach-Object Name)
Add-Type -Namespace C9 -Name Profile -MemberDefinition '[DllImport("userenv", CharSet=CharSet.Unicode)] public static extern int DeleteAppContainerProfile(string n);'
# The runner holds run.lock exclusively for its whole life; the operating system releases it when the runner dies.
try { $held = [IO.File]::Open((Join-Path $work 'run.lock'), 'Open', 'ReadWrite', 'None') } catch { [Console]::Error.WriteLine('C9W_RECOVER_REFUSED_LIVE'); exit 1 }
$mapped = @(& "$env:SystemRoot\System32\subst.exe") | Where-Object { $_.EndsWith("=> $work", [StringComparison]::OrdinalIgnoreCase) }
foreach ($line in $mapped) { & "$env:SystemRoot\System32\subst.exe" $line.Substring(0, 2) /D | Out-Null; if ($LASTEXITCODE -ne 0) { [Console]::Error.WriteLine('C9W_RECOVER_MAPPING'); exit 1 } }
foreach ($name in $names) { if ([C9.Profile]::DeleteAppContainerProfile($name) -ne 0) { [Console]::Error.WriteLine('C9W_RECOVER_PROFILE'); exit 1 } }
$held.Dispose()
"C9W_RECOVER_OK:mappings=$(@($mapped).Count) profiles=$($names.Count) work_directory_kept=true"

#Requires -Version 7.4
# Fictional-only, isolated owner-controlled NTFS root. Retains all created control artifacts.
param([Parameter(Mandatory)][string] $FixtureRoot, [switch] $ConfirmFictional)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @((Join-Path $PSScriptRoot 'C9DirectoryIdentity.cs'), (Join-Path $PSScriptRoot 'C9ControlLock.cs'))
function Require([bool] $Condition, [string] $Code) { if (-not $Condition) { throw $Code } }
# Request DELETE access without performing rename, deletion, or disposition.
# Positive control after releasing all directory handles distinguishes actual
# sharing refusal from lack of permission. Never probe a caller-owned path.
Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
using Microsoft.Win32.SafeHandles;
public static class C9DirectoryShareProbe {
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern SafeFileHandle CreateFileW(string path,uint access,uint share,IntPtr security,uint disposition,uint flags,IntPtr template);
    public static int DeleteAccessOpen(string path) {
        using var handle=CreateFileW(path,0x10000,7,IntPtr.Zero,3,0x02200000,IntPtr.Zero);
        return handle.IsInvalid ? Marshal.GetLastWin32Error() : 0;
    }
}
'@

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
    # Only generated paths under the newly created control directory.
    $probeParent=Join-Path $control 'fictional-share-parent'
    $probeChild=Join-Path $probeParent 'child'
    New-Item -ItemType Directory -Path $probeParent | Out-Null
    New-Item -ItemType Directory -Path $probeChild | Out-Null
    Require ([C9DirectoryShareProbe]::DeleteAccessOpen($probeParent) -eq 0 -and [C9DirectoryShareProbe]::DeleteAccessOpen($probeChild) -eq 0) 'C9W_TEST_SHARE_POSITIVE_BEFORE'
    $pinned=[C9DirectoryIdentity]::Open($probeChild)
    try {
        Require ([C9DirectoryShareProbe]::DeleteAccessOpen($probeParent) -eq 32) 'C9W_TEST_ANCESTOR_DELETE_SHARE'
        Require ([C9DirectoryShareProbe]::DeleteAccessOpen($probeChild) -eq 32) 'C9W_TEST_FINAL_DELETE_SHARE'
    } finally { $pinned.Dispose() }
    Require ([C9DirectoryShareProbe]::DeleteAccessOpen($probeParent) -eq 0 -and [C9DirectoryShareProbe]::DeleteAccessOpen($probeChild) -eq 0) 'C9W_TEST_SHARE_POSITIVE_AFTER'
    Require ((Test-Path -LiteralPath $probeParent -PathType Container) -and (Test-Path -LiteralPath $probeChild -PathType Container)) 'C9W_TEST_SHARE_PATH_CHANGED'
} finally { $a.Dispose() }
Refuses { $a.AssertHeld() } 'C9W_CONTROL_LOCK_NOT_HELD'
$b = [C9ControlLock]::OpenExisting($FixtureRoot, $token)
try {
    Require ($b.RootId -ceq $rootId -and $b.ControlId -ceq $controlId) 'C9W_TEST_ID_CHANGED'
    Refuses { [C9ControlLock]::OpenExisting($FixtureRoot, $token) } 'C9W_CONTROL_LOCK_REFUSED'
} finally { $b.Dispose() }
Require ((Test-Path -LiteralPath (Join-Path $control 'ownership.lock') -PathType Leaf) -and (Get-Item -LiteralPath (Join-Path $control 'ownership.lock')).Length -eq 0) 'C9W_TEST_LOCK_NOT_RETAINED'
[Console]::WriteLine('C9W_CONTROL_LOCK_FIXTURE_OK:exclusive_create=true competing_open_refused=true stable_identity=true missing_refused=true delete_access_sharing=true artifacts_retained=true')

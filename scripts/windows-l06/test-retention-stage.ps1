#Requires -Version 7.4
# Fictional header+random bytes, never a genuine private archive. No cleanup or commit marker.
param(
    [Parameter(Mandatory)][string] $SourceFixtureRoot,
    [Parameter(Mandatory)][string] $RetentionFixtureRoot,
    [switch] $ConfirmFictional
)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ControlLock.cs','C9JournalStorage.cs','C9RetentionStage.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $Condition,[string] $Code) { if (-not $Condition) { throw $Code } }
function Refuses([scriptblock] $Action,[string] $Expected) {
    $caught = $false
    try { $null = & $Action }
    catch { if ($_.Exception.ToString() -notmatch [regex]::Escape($Expected)) { throw }; $caught = $true }
    Require $caught "C9W_TEST_EXPECTED_REFUSAL_$Expected"
}
function Token { [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant() }
function Hash([string] $Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function IsBelow([string] $Path,[string] $Parent) {
    $Path -ieq $Parent -or $Path.StartsWith($Parent+'\',[StringComparison]::OrdinalIgnoreCase)
}
Require (-not (IsBelow $SourceFixtureRoot $RetentionFixtureRoot) -and -not (IsBelow $RetentionFixtureRoot $SourceFixtureRoot)) 'C9W_TEST_ROOTS_OVERLAP'
# Both existing roots must pass native owner-only, fixed NTFS and no-reparse controls.
$sourceLock = [C9ControlLock]::Create($SourceFixtureRoot,(Token))
$held = $null
try {
    $held = [C9ControlLock]::Create($RetentionFixtureRoot,(Token))
    $src = $sourceLock.ControlPath; $dirId = $sourceLock.ControlId
    $path = Join-Path $src 'third.aesgcm'
    $bytes = [byte[]]([Text.Encoding]::ASCII.GetBytes('ORGRESOURCE-C5-AES256GCM-1') + [Security.Cryptography.RandomNumberGenerator]::GetBytes(131101))
    # CreateNew inside the already protected source directory; explicit protected file ACL.
    $file = [IO.FileStream]::new($path,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write,[IO.FileShare]::None)
    try { $file.Write($bytes,0,$bytes.Length); $file.Flush($true) } finally { $file.Dispose() }
    $sid = [Security.Principal.WindowsIdentity]::GetCurrent().User
    $acl = [Security.AccessControl.FileSecurity]::new()
    $acl.SetOwner($sid)
    $acl.SetAccessRuleProtection($true,$false)
    $acl.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new($sid,[Security.AccessControl.FileSystemRights]::FullControl,[Security.AccessControl.AccessControlType]::Allow))
    Set-Acl -LiteralPath $path -AclObject $acl
    $sha = Hash $path; $size = [long]$bytes.Length
    $id = $held.ObserveFictionalArchive($src,$dirId,$sha,$size)
    Require ($id -cmatch '^[a-f0-9]{16}:[a-f0-9]{32}$') 'C9W_TEST_SOURCE_ID'
    $stage = Join-Path $held.ControlPath 'retention.stage.aesgcm'
    Refuses { $held.StageFictionalArchive($src,$dirId,$id,('0'*64),$size) } 'C9W_RETENTION_HASH'
    Refuses { $held.StageFictionalArchive($src,$dirId,$id,$sha,54) } 'C9W_RETENTION_EXPECTED'
    Refuses { $held.StageFictionalArchive($src,$dirId,('0'*16+':'+('0'*32)),$sha,$size) } 'C9W_RETENTION_SOURCE_ID'
    Refuses { $held.StageFictionalArchive($src,$held.ControlId,$id,$sha,$size) } 'C9W_RETENTION_SOURCE_DIRECTORY'
    Refuses { $held.ObserveFictionalArchive($held.ControlPath,$held.ControlId,$sha,$size) } 'C9W_RETENTION_SOURCE_DIRECTORY'
    Require (-not (Test-Path -LiteralPath $stage)) 'C9W_TEST_REFUSAL_WROTE'
    $result = $held.StageFictionalArchive($src,$dirId,$id,$sha,$size)
    Require ($result.SourceId -ceq $id -and $result.StageId -cne $id -and $result.Sha256 -ceq $sha -and $result.Bytes -eq $size) 'C9W_TEST_STAGE_RECEIPT'
    Require ((Hash $stage) -ceq $sha -and (Get-Item -LiteralPath $stage).Length -eq $size -and (Hash $path) -ceq $sha) 'C9W_TEST_STAGE_BYTES'
    $outAcl = Get-Acl -LiteralPath $stage
    Require ($outAcl.AreAccessRulesProtected -and @($outAcl.GetAccessRules($true,$true,[Security.Principal.SecurityIdentifier]) | Where-Object { $_.IdentityReference -ne $sid -or $_.IsInherited }).Count -eq 0) 'C9W_TEST_STAGE_ACL'
    Refuses { $held.StageFictionalArchive($src,$dirId,$id,$sha,$size) } 'C9W_RETENTION_STAGE_CREATE'
    Require ((Hash $stage) -ceq $sha) 'C9W_TEST_OVERWRITE'
    # Source extra hard link must refuse, even if hash and byte count still match.
    $link = Join-Path $src 'fictional-extra-link'
    New-Item -ItemType HardLink -Path $link -Target $path | Out-Null
    Refuses { $held.ObserveFictionalArchive($src,$dirId,$sha,$size) } 'C9W_RETENTION_FILE_SHAPE'
    Require ((Hash $stage) -ceq $sha -and (Hash $link) -ceq $sha) 'C9W_TEST_LINK_CHANGED'
    Require (-not (Test-Path -LiteralPath (Join-Path $held.ControlPath 'retention.committed.json'))) 'C9W_TEST_COMMIT_EXISTS'
} finally {
    if ($null -ne $held) { $held.Dispose() }
    $sourceLock.Dispose()
}
Refuses { $held.ObserveFictionalArchive($src,$dirId,$sha,$size) } 'C9W_CONTROL_LOCK_NOT_HELD'
[Console]::WriteLine('C9W_RETENTION_STAGE_FIXTURE_OK:header_only_fixture=true exact_bytes=true exclusive_stage=true source_identity=true link_refused=true no_commit=true evidence_retained=true')

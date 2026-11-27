#Requires -Version 7.4
# Native fictional metadata fixture only. No editor/DB/actual credential or role access.
# Retains all generated fixture paths for inspection; use approved disposable owner-only NTFS root.
param([Parameter(Mandatory)][string] $FixtureRoot,[switch] $ConfirmFictional)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ControlLock.cs','C9JournalStorage.cs','C9LoginCustody.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $ok,[string] $code) { if (-not $ok) { throw $code } }
function Refuses([scriptblock] $action,[string] $code) {
    $caught = $false
    try { $null = & $action }
    catch { if (-not $_.Exception.GetBaseException().Message.Contains($code,[StringComparison]::Ordinal)) { throw }; $caught=$true }
    Require $caught "C9W_TEST_EXPECTED_$code"
}
function Write-Fictional([string] $path,[switch] $Wide) {
    $data=[Text.Encoding]::ASCII.GetBytes('FICTIONAL_METADATA_ONLY_NOT_A_LOGIN')
    $stream=[IO.FileStream]::new($path,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write,[IO.FileShare]::None)
    try { $stream.Write($data,0,$data.Length); $stream.Flush($true) } finally { $stream.Dispose() }
    $acl=[Security.AccessControl.FileSecurity]::new()
    $acl.SetOwner($sid); $acl.SetAccessRuleProtection($true,$false)
    $acl.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new($sid,'FullControl','Allow'))
    if ($Wide) { $acl.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new([Security.Principal.SecurityIdentifier]::new('S-1-1-0'),'Read','Allow')) }
    Set-Acl -LiteralPath $path -AclObject $acl
}
$identity=[Security.Principal.WindowsIdentity]::GetCurrent()
$sid=$identity.User
$token=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
$held=[C9ControlLock]::Create($FixtureRoot,$token)
$slots=[Collections.Generic.List[IDisposable]]::new()
try {
    Refuses { $held.BeginFictionalLoginSlot('source','restore') } 'C9W_LOGIN_SLOT_SCOPE'
    Refuses { $held.BeginFictionalLoginSlot('mixed','backup') } 'C9W_LOGIN_SLOT_SCOPE'
    Refuses { $held.BeginFictionalLoginSlot('SOURCE','backup') } 'C9W_LOGIN_SLOT_SCOPE'
    $a=$held.BeginFictionalLoginSlot('source','backup'); $slots.Add($a)
    $path=$a.FilePath; $id=$a.DirectoryId
    Require ($id -cne $held.ControlId -and $id -cne $held.RootId) 'C9W_TEST_SLOT_ID'
    $a.AssertVacant()
    Write-Fictional $path
    $hash=(Get-FileHash -LiteralPath $path).Hash
    Refuses { $held.BeginFictionalLoginSlot('source','backup') } 'C9W_LOGIN_SLOT_CREATE'
    Require ((Get-FileHash -LiteralPath $path).Hash -ceq $hash) 'C9W_TEST_SLOT_OVERWRITE'
    $fileId=$a.ObserveFictionalFile()
    Require ($fileId -cmatch '^[a-f0-9]{16}:[a-f0-9]{32}$' -and $fileId -cne $id) 'C9W_TEST_FILE_ID'
    $a.AssertObserved()
    # Exclusive metadata handle: no ordinary concurrent read/write open.
    $blocked=$false
    try { $other=[IO.File]::Open($path,'Open','ReadWrite','None'); $other.Dispose() }
    catch [IO.IOException] {
        if (($_.Exception.HResult -band 0xffff) -ne 32) { throw }
        $blocked=$true
    }
    Require $blocked 'C9W_TEST_FILE_NOT_EXCLUSIVE'
    $a.Dispose()
    Require ((Get-FileHash -LiteralPath $path).Hash -ceq $hash) 'C9W_TEST_FILE_CHANGED'
    $b=$held.BeginFictionalLoginSlot('restore','restore'); $slots.Add($b)
    $wide=$b.FilePath; Write-Fictional $wide -Wide
    $wideHash=(Get-FileHash -LiteralPath $wide).Hash
    Refuses { $b.ObserveFictionalFile() } 'C9W_CONTROL_ACL'
    Refuses { $b.AssertVacant() } 'C9W_LOGIN_SLOT_REFUSED'
    Require ((Get-FileHash -LiteralPath $wide).Hash -ceq $wideHash) 'C9W_TEST_WIDE_CHANGED'
    $c=$held.BeginFictionalLoginSlot('restore','backup'); $slots.Add($c)
    $existing=$c.FilePath; Write-Fictional $existing
    $existingHash=(Get-FileHash -LiteralPath $existing).Hash
    Refuses { $c.AssertVacant() } 'C9W_LOGIN_FILE_EXISTS'
    Require ((Get-FileHash -LiteralPath $existing).Hash -ceq $existingHash) 'C9W_TEST_EXISTING_CHANGED'
    $d=$held.BeginFictionalLoginSlot('mixed','restore'); $slots.Add($d)
    $linked=$d.FilePath; Write-Fictional $linked
    $extra=Join-Path (Split-Path $linked) 'fictional-extra-link'
    New-Item -ItemType HardLink -Path $extra -Target $linked | Out-Null
    Refuses { $d.ObserveFictionalFile() } 'C9W_LOGIN_FILE_SHAPE'
    Require ((Get-FileHash -LiteralPath $linked).Hash -ceq (Get-FileHash -LiteralPath $extra).Hash) 'C9W_TEST_LINK_CHANGED'
    Require ((Get-FileHash -LiteralPath $path).Hash -ceq $hash) 'C9W_TEST_SENTINEL_CHANGED'
} finally {
    foreach ($slot in $slots) { $slot.Dispose() }
    $held.Dispose()
    $identity.Dispose()
}
Refuses { $held.BeginFictionalLoginSlot('source','backup') } 'C9W_CONTROL_LOCK_NOT_HELD'
[Console]::WriteLine('C9W_H06_LOGIN_SLOT_FIXTURE_OK:metadata_only=true no_editor=true no_database=true no_cleanup=true')

# Separate tokens: cover every allowed pair without reusing the metadata fixture's slots.
foreach ($pair in @('source/backup','restore/backup','restore/restore','mixed/restore')) {
    $role,$kind=$pair.Split('/')
    $newToken=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
    $control=[C9ControlLock]::Create($FixtureRoot,$newToken)
    $slot=$null
    try {
        $slot=$control.BeginFictionalLoginSlot($role,$kind)
        $file=$slot.FilePath
        $dirId=$slot.DirectoryId
        $created=$slot.CreateFixedFictionalFile()
        Require ($created -cmatch '^[a-f0-9]{16}:[a-f0-9]{32}$' -and $created -cne $dirId) 'C9W_TEST_FIXED_FILE_ID'
        $slot.AssertObserved()
        $blocked=$false
        try { $other=[IO.File]::Open($file,'Open','ReadWrite','None'); $other.Dispose() }
        catch [IO.IOException] {
            if (($_.Exception.HResult -band 0xffff) -ne 32) { throw }
            $blocked=$true
        }
        Require $blocked 'C9W_TEST_FIXED_FILE_NOT_EXCLUSIVE'
        # A repeated creation must refuse, poison this instance, and retain bytes.
        Refuses { $slot.CreateFixedFictionalFile() } 'C9W_LOGIN_SLOT_ALREADY_OBSERVED'
        Refuses { $slot.AssertObserved() } 'C9W_LOGIN_SLOT_REFUSED'
        $slot.Dispose()
        Require ([IO.File]::ReadAllText($file) -ceq ("FICTIONAL_C9W_CUSTODY_PROBE_NOT_A_LOGIN"+[char]10)) 'C9W_TEST_FIXED_BYTES'
        $before=(Get-FileHash -LiteralPath $file).Hash
        Refuses { $control.BeginFictionalLoginSlot($role,$kind) } 'C9W_LOGIN_SLOT_CREATE'
        Require ((Get-FileHash -LiteralPath $file).Hash -ceq $before) 'C9W_TEST_FIXED_RETRY_CHANGED'
    } finally {
        if ($null -ne $slot) { $slot.Dispose() }
        $control.Dispose()
    }
    $reopened=[C9ControlLock]::OpenExisting($FixtureRoot,$newToken)
    try {
        Refuses { $reopened.BeginFictionalLoginSlot($role,$kind) } 'C9W_LOGIN_SLOT_CREATE'
        Require ((Get-FileHash -LiteralPath $file).Hash -ceq $before) 'C9W_TEST_FIXED_REOPEN_CHANGED'
    } finally { $reopened.Dispose() }
    [Console]::WriteLine('C9W_H06_FIXED_SLOT_CASE_OK:'+$pair)
}

# A pre-existing fixture file must survive an attempted create unchanged.
$collisionToken=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant()
$control=[C9ControlLock]::Create($FixtureRoot,$collisionToken)
$slot=$null
try {
    $slot=$control.BeginFictionalLoginSlot('source','backup')
    $file=$slot.FilePath
    # Deliberately fixed, harmless existing payload; not a real credential.
    [IO.File]::WriteAllText($file,'FICTIONAL_EXISTING_SENTINEL')
    $before=(Get-FileHash -LiteralPath $file).Hash
    Refuses { $slot.CreateFixedFictionalFile() } 'C9W_LOGIN_FILE_EXISTS'
    Refuses { $slot.AssertObserved() } 'C9W_LOGIN_SLOT_REFUSED'
    Require ((Get-FileHash -LiteralPath $file).Hash -ceq $before) 'C9W_TEST_FIXED_COLLISION_CHANGED'
} finally {
    if ($null -ne $slot) { $slot.Dispose() }
    $control.Dispose()
}
[Console]::WriteLine('C9W_H06_FIXED_SLOT_FIXTURE_OK:four_pairs=true collision_refused=true retained=true no_editor=true no_database=true')

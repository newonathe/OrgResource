#Requires -Version 7.4
# Fictional-only, nonadmin, isolated owner-only NTFS root. Retain created work/control evidence.
param(
    [Parameter(Mandatory)][string] $FixtureRoot,
    [Parameter(Mandatory)][string] $Python,
    [Parameter(Mandatory)][ValidatePattern('^[a-f0-9]{64}$')][string] $PythonSha256,
    [switch] $ConfirmFictional
)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not $IsWindows -or -not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
Add-Type -Path @('C9DirectoryIdentity.cs','C9ControlLock.cs','C9JournalStorage.cs','C9JournalParent.cs','C9DosDeviceObservation.cs','C9LogonContext.cs','C9WorkCreation.cs','C9ProfileStorageObservation.cs','C9Contain.cs','C9OwnershipProducer.cs' | ForEach-Object { Join-Path $PSScriptRoot $_ })
function Require([bool] $Condition, [string] $Code) { if (-not $Condition) { throw $Code } }
function Hash([string] $Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Refuses([scriptblock] $Action, [string] $Expected) {
    $caught = $false
    try { $null = & $Action }
    catch { if ($_.Exception.ToString() -notmatch [regex]::Escape($Expected)) { throw }; $caught = $true }
    Require $caught "C9W_TEST_EXPECTED_REFUSAL_$Expected"
}
$bridge = Join-Path $PSScriptRoot 'ownership_bridge_v2.py'
$journal = Join-Path $PSScriptRoot 'ownership_journal.py'
$v2 = Join-Path $PSScriptRoot 'ownership_journal_v2.py'
function New-Producer($Held) { [C9OwnershipProducer]::new($Held, $Python, $PythonSha256, $bridge, (Hash $bridge), (Hash $journal), (Hash $v2)) }
function Read-Journal($Held) { Get-Content -LiteralPath (Join-Path $Held.ControlPath 'ownership.json') -Raw | ConvertFrom-Json }
function Token { [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(6)).ToLowerInvariant() }

# Collision: durable intent, no adoption/deletion, existing sentinel unchanged, poisoned retry.
$collision = Token
$held = [C9ControlLock]::Create($FixtureRoot, $collision)
try {
    $p = New-Producer $held
    try {
        $path = $p.WorkPath
        Require (-not (Test-Path -LiteralPath $path)) 'C9W_TEST_COLLISION'
        New-Item -ItemType Directory -Path $path | Out-Null
        $sentinel = Join-Path $path 'fictional-sentinel.txt'
        [IO.File]::WriteAllText($sentinel, 'untouched')
        Refuses { $p.CreateWork() } 'C9W_WORK_CREATE'
        Require ((Read-Journal $held).ownership.work.state -ceq 'intent') 'C9W_TEST_INTENT_MISSING'
        Require ([IO.File]::ReadAllText($sentinel) -ceq 'untouched') 'C9W_TEST_COLLISION_CHANGED'
        Refuses { $p.CreateWork() } 'C9W_PRODUCER_REFUSED'
    } finally { $p.Dispose() }
} finally { $held.Dispose() }

# Positive isolated creation; exact identity, profile create/remove journal and legacy refusal.
$held = [C9ControlLock]::Create($FixtureRoot, (Token))
try {
    $p = New-Producer $held
    try {
        Refuses { $p.CreateProfile('source') } 'C9W_PROFILE_ROLE'
        $path = $p.CreateWork()
        $record = Read-Journal $held
        Require ($record.schema -ceq 'orgresource.windows-l06-ownership.v2' -and $null -eq $record.context.bootId) 'C9W_TEST_V2_SCHEMA'
        Require ($record.ownership.work.state -ceq 'created' -and $record.ownership.work.id -ceq $p.WorkId) 'C9W_TEST_WORK_ID'
        $id = [C9DirectoryIdentity]::Open($path)
        try { Require ($id.Id -ceq $record.ownership.work.id) 'C9W_TEST_WORK_OBSERVED_ID' } finally { $id.Dispose() }
        Refuses { $p.CreateWork() } 'C9W_WORK_ALREADY_CREATED'
        Refuses { [C9Container]::new("orgresource.c9.$(Token)") } 'C9W_PROFILE_JOURNAL_REQUIRED'
        $profile = $p.CreateProfile('jobtest')
        $record = Read-Journal $held
        Require ($record.ownership.profiles.jobtest.state -ceq 'created' -and $record.ownership.profiles.jobtest.name -ceq $profile.Name -and $record.ownership.profiles.jobtest.sid -ceq $profile.SidString) 'C9W_TEST_PROFILE_ID'
        Require ($profile.ActiveProcesses -eq 0) 'C9W_TEST_PROFILE_NOT_EMPTY'
        $profile.AssertProfileStoragePresent()
        $packages = Join-Path ([Environment]::GetFolderPath([Environment+SpecialFolder]::LocalApplicationData)) 'Packages'
        $package = Join-Path $packages $profile.Name
        $storage = [C9DirectoryIdentity]::Open($package)
        try {
            Require ($storage.Id -cmatch '^[a-f0-9]{16}:[a-f0-9]{32}$') 'C9W_TEST_PROFILE_STORAGE_ID'
            Require ($storage.Id -ceq $record.profileStorage.jobtest.packageId) 'C9W_TEST_V2_PACKAGE_ID'
        } finally { $storage.Dispose() }
        $parentId = [C9DirectoryIdentity]::Open($packages)
        $appId = [C9DirectoryIdentity]::Open((Join-Path $package 'AC'))
        try {
            Require ($parentId.Id -ceq $record.profileStorage.jobtest.packagesId -and $appId.Id -ceq $record.profileStorage.jobtest.appDataId -and $packages -ceq $record.profileStorage.jobtest.packagesPath) 'C9W_TEST_V2_STORAGE_ID'
        } finally { $appId.Dispose(); $parentId.Dispose() }
        Refuses { $profile.AssertProfileStorageAbsent() } 'C9W_PROFILE_STORAGE_STATE'
        $p.RemoveProfile('jobtest')
        $profile.AssertProfileStorageAbsent()
        Require (-not (Test-Path -LiteralPath $package)) 'C9W_TEST_PROFILE_STORAGE_REMAINS'
        Refuses { $profile.AssertProfileStoragePresent() } 'C9W_PROFILE_STORAGE_STATE'
        $removed = Read-Journal $held
        Require ($removed.ownership.profiles.jobtest.state -ceq 'removed') 'C9W_TEST_PROFILE_REMOVED'
        Require ($removed.removalEvidence.jobtest.kind -ceq 'api_success_and_package_absent' -and $removed.removalEvidence.jobtest.hresult -eq 0 -and $removed.removalEvidence.jobtest.packagesId -ceq $record.profileStorage.jobtest.packagesId) 'C9W_TEST_V2_REMOVAL_EVIDENCE'
        Refuses { $p.RemoveProfile('jobtest') } 'C9W_PROFILE_ROLE'
    } finally { $p.Dispose() }
    Require (Test-Path -LiteralPath $path -PathType Container) 'C9W_TEST_WORK_REMOVED'
} finally { $held.Dispose() }
[Console]::WriteLine('C9W_OWNERSHIP_PRODUCER_FIXTURE_OK:collision_preserved=true intent_before_create=true exact_identity=true profile_journal=true profile_storage_absence=true durable_v2_storage_evidence=true legacy_refused=true evidence_retained=true')

#Requires -Version 7.4
# Pure H05 scope/inventory/dump/refusal contract checks with fictional stubs.
# No database, process, filesystem access outside fresh temporary fixture files.
param([switch] $ConfirmFictional)
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
if (-not $ConfirmFictional) { throw 'C9W_TEST_FIXTURE_REQUIRED' }
. (Join-Path $PSScriptRoot 'scoped_additive.ps1')
$Mode='FixtureOnly'; $token='0123456789ab'; $drive='Q:'
$roles=@{
    source=@{Uuid='11111111-1111-1111-1111-111111111111';Port=24001;Schema="orgresource_c9_local_{0}_source" -f $token}
    restore=@{Uuid='22222222-2222-2222-2222-222222222222';Port=24002;Schema="orgresource_c9_local_{0}_restore" -f $token}
    mixed=@{Uuid='33333333-3333-3333-3333-333333333333';Port=24003;Schema="orgresource_c9_local_{0}_mixed" -f $token}
}
$schema="orgresource_c8_additive_ci_$token"; $uuid=$roles.mixed.Uuid; $port=$roles.mixed.Port
$script:sqlCalls=0; $script:roleCalls=0
$script:inventory=@('account_recovery_code','allocation','audit_event','department','flyway_schema_history','idempotency_key','invitation','local_credential','membership','organization','ownership_transfer','project','resource','resource_requirement','SPRING_SESSION','SPRING_SESSION_ATTRIBUTES','stock_movement','user_account')
$script:primary=@($script:inventory | ForEach-Object { $_.ToLowerInvariant() })
$script:identity=@('8.0.46',$uuid,"$port","$drive/mixed/data/") -join [char]9
$script:actualSchema=$schema
function Fail([string] $Code) { throw [InvalidOperationException]::new($Code) }
function Assert-Role([string] $Role) { if ($Role -cne 'mixed') { throw 'UNEXPECTED_ROLE' }; $script:roleCalls++ }
function Invoke-Root([string] $Role,[string] $Sql,[string] $Db='') {
    if ($Role -cne 'mixed') { throw 'UNEXPECTED_ROLE' }
    $script:sqlCalls++
    if ($Sql -ceq 'SELECT VERSION(),@@server_uuid,@@port,@@datadir;') { return $script:identity }
    if ($Db -cne $schema) { throw 'UNEXPECTED_SCHEMA' }
    if ($Sql -ceq 'SELECT DATABASE(),@@server_uuid,@@port') { return @($script:actualSchema,$uuid,"$port") -join [char]9 }
    if ($Sql.StartsWith('SELECT table_name,table_type,engine ')) {
        return (@($script:inventory | ForEach-Object { @($_,'BASE TABLE','InnoDB') -join [char]9 }) -join [char]10)
    }
    if ($Sql.StartsWith('SELECT DISTINCT LOWER(table_name)')) { return $script:primary -join [char]10 }
    throw 'UNEXPECTED_SQL'
}
function Require([bool] $ok,[string] $Code) { if (-not $ok) { throw $Code } }
function Refuses([scriptblock] $action,[string] $code) {
    $caught=$false
    try { $null=& $action } catch { if ($_.Exception.GetBaseException().Message -cne $code) { throw }; $caught=$true }
    Require $caught "EXPECTED_$code"
}
function Test-Log([string] $Path,[string] $Text) { return ([IO.File]::ReadAllText($Path).Contains($Text,[StringComparison]::Ordinal)) }
$null=Assert-C9AdditiveScope $schema $uuid $port
Require ($script:sqlCalls -eq 1 -and $script:roleCalls -eq 1) 'SCOPE_POSITIVE'
$before=$script:sqlCalls; $rBefore=$script:roleCalls
Refuses { Assert-C9AdditiveScope 'other' $uuid $port } 'R06B2_ADDITIVE_SCOPE'
Refuses { Assert-C9AdditiveScope $schema $roles.source.Uuid $port } 'R06B2_ADDITIVE_SCOPE'
Refuses { Assert-C9AdditiveScope $schema $uuid $roles.restore.Port } 'R06B2_ADDITIVE_SCOPE'
$Mode='ApprovedImport'
Refuses { Assert-C9AdditiveScope $schema $uuid $port } 'R06B2_ADDITIVE_SCOPE'
$Mode='FixtureOnly'
Require ($script:sqlCalls -eq $before -and $script:roleCalls -eq $rBefore) 'GUARD_PERFORMED_SQL'
$old=$script:identity
$script:identity=@('8.0.46',$uuid,"$port","$drive/restore/data/") -join [char]9
Refuses { Assert-C9AdditiveScope $schema $uuid $port } 'R06B2_ADDITIVE_SCOPE'
$script:identity=$old
$actual=@(Get-C9AdditiveTables $schema $uuid $port)
Require (($actual -join ',') -ceq ($script:inventory -join ',')) 'EXACT_INVENTORY'
$saved=$script:inventory
$script:inventory=@($saved[0..16])
Refuses { Get-C9AdditiveTables $schema $uuid $port } 'R06B2_ADDITIVE_INVENTORY'
$script:inventory=@($saved)+@('unexpected_table')
Refuses { Get-C9AdditiveTables $schema $uuid $port } 'R06B2_ADDITIVE_INVENTORY'
$script:inventory=$saved
$pSaved=$script:primary; $script:primary=@($pSaved[0..16])
Refuses { Get-C9AdditiveTables $schema $uuid $port } 'R06B2_ADDITIVE_INVENTORY'
$script:primary=$pSaved
$script:actualSchema='wrong_schema'
Refuses { Get-C9AdditiveTables $schema $uuid $port } 'R06B2_ADDITIVE_SCOPE'
$script:actualSchema=$schema
$tmp=Join-Path ([IO.Path]::GetTempPath()) ('c9-h05-fictional-'+[Guid]::NewGuid().ToString('N'))
# No recursive cleanup or preexisting file overwrite.
$stream=[IO.File]::Open($tmp,[IO.FileMode]::CreateNew,[IO.FileAccess]::ReadWrite,[IO.FileShare]::None)
$stream.Dispose()
try {
    $q=[char]96; $insert="INSERT INTO $q"+"organization$q VALUES (1);"
    [IO.File]::WriteAllLines($tmp,@($insert))
    Assert-C9AdditiveDumpRows $tmp @('organization') @{organization=1}
    Refuses { Assert-C9AdditiveDumpRows $tmp @('organization') @{organization=2} } 'R06B2_ADDITIVE_DUMP_ROW_COUNT'
    Refuses { Assert-C9AdditiveDumpRows $tmp @('allocation') @{allocation=0} } 'R06B1_DOMAIN_DUMP_SCOPE'
    [IO.File]::WriteAllLines($tmp,@($insert,$insert))
    Refuses { Assert-C9AdditiveDumpRows $tmp @('organization') @{organization=1} } 'R06B2_ADDITIVE_DUMP_ROW_COUNT'
    [IO.File]::WriteAllText($tmp,'SET SQL_MODE=0;')
    Refuses { Assert-C9AdditiveDumpRows $tmp @('organization') @{organization=1} } 'R06B2_ADDITIVE_DUMP_ROW_COUNT'
    $message='The persisted demo did not match its approved record or stock invariants.'
    [IO.File]::WriteAllText($tmp,$message)
    Refuses { Assert-C9AdditiveRepeatRefusal 1 $tmp } 'R06B2_ADDITIVE_REPEAT'
    [IO.File]::WriteAllLines($tmp,@($message,'at edu.orgresource.extended.service.C8DemoImporter.importAdditionalPlanForIsolatedProof(C8DemoImporter.java:163)'))
    Assert-C9AdditiveRepeatRefusal 1 $tmp
    Refuses { Assert-C9AdditiveRepeatRefusal 0 $tmp } 'R06B2_ADDITIVE_REPEAT'
    Refuses { Assert-C9AdditiveRepeatRefusal 124 $tmp } 'R06B2_ADDITIVE_REPEAT'
} finally {
    # Remove only this generated ordinary fictional fixture file.
    [IO.File]::Delete($tmp)
}
[Console]::WriteLine('C9W_H05_PURE_FIXTURE_OK:scope_spy=true inventory=true row_completeness=true precise_refusal=true mysql_not_run=true')

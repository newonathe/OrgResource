# H05: only the generated fictional additive schema on the captured mixed server.
# Requires actual owned-server/containment prerequisites; not a standalone runner.
function Assert-C9AdditiveScope([string] $Schema, [string] $ExpectedUuid, [int] $ExpectedPort) {
    if ($Mode -cne 'FixtureOnly' -or $token -cnotmatch '\A[a-f0-9]{12}\z' -or
        $Schema -cne "orgresource_c8_additive_ci_$token" -or
        $ExpectedUuid -cnotmatch '\A[a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12}\z' -or
        $ExpectedPort -lt 1000 -or $ExpectedPort -gt 65535 -or
        $roles.mixed.Uuid -cne $ExpectedUuid -or $roles.mixed.Port -ne $ExpectedPort -or
        $ExpectedUuid -cin @($roles.source.Uuid,$roles.restore.Uuid) -or
        $ExpectedPort -in @($roles.source.Port,$roles.restore.Port) -or
        $Schema -cin @($roles.source.Schema,$roles.restore.Schema,$roles.mixed.Schema)) {
        Fail 'R06B2_ADDITIVE_SCOPE'
    }
    Assert-Role 'mixed'
    $actual = (Invoke-Root 'mixed' "SELECT VERSION(),@@server_uuid,@@port,@@datadir;") -split '\t'
    if ($actual.Count -ne 4 -or $actual[0] -cne '8.0.46' -or
        $actual[1] -cne $ExpectedUuid -or $actual[2] -cne "$ExpectedPort" -or
        (($actual[3] -replace '[\\/]+','/').TrimEnd('/') -ine "$drive/mixed/data")) {
        Fail 'R06B2_ADDITIVE_SCOPE'
    }
}
function Get-C9AdditiveTables([string] $Schema,[string] $Uuid,[int] $Port) {
    Assert-C9AdditiveScope $Schema $Uuid $Port
    $required = @('account_recovery_code','allocation','audit_event','department','flyway_schema_history','idempotency_key','invitation','local_credential','membership','organization','ownership_transfer','project','resource','resource_requirement','spring_session','spring_session_attributes','stock_movement','user_account')
    $identity = Invoke-Root 'mixed' 'SELECT DATABASE(),@@server_uuid,@@port' $Schema
    if ($identity -cne (@($Schema,$Uuid,"$Port") -join [char]9)) { Fail 'R06B2_ADDITIVE_SCOPE' }
    $rows = (Invoke-Root 'mixed' "SELECT table_name,table_type,engine FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY LOWER(table_name),BINARY table_name" $Schema) -split '\n'
    if ($rows.Count -ne $required.Count) { Fail 'R06B2_ADDITIVE_INVENTORY' }
    $names = @()
    for ($i=0; $i -lt $required.Count; $i++) {
        $parts = $rows[$i] -split '\t'
        if ($parts.Count -ne 3 -or $parts[0] -cnotmatch '\A[A-Za-z_]+\z' -or
            $parts[0].ToLowerInvariant() -cne $required[$i] -or $parts[1] -cne 'BASE TABLE' -or $parts[2] -cne 'InnoDB') {
            Fail 'R06B2_ADDITIVE_INVENTORY'
        }
        $names += $parts[0]
    }
    $primary = (Invoke-Root 'mixed' "SELECT DISTINCT LOWER(table_name) FROM information_schema.statistics WHERE table_schema=DATABASE() AND index_name='PRIMARY' ORDER BY LOWER(table_name)" $Schema) -split '\n'
    if (($primary -join ',') -cne ($required -join ',')) { Fail 'R06B2_ADDITIVE_INVENTORY' }
    return $names
}
function Assert-C9AdditiveQuiescent {
    # No importer/JVM or other contained client may survive between snapshot operations.
    Assert-Role 'mixed'
    if ($roles.mixed.Container.ActiveProcesses -ne 1) { Fail 'R06B2_ADDITIVE_NOT_QUIESCENT' }
}
function Get-C9AdditiveRowCounts([string] $Schema,[string] $Uuid,[int] $Port,[string[]] $Tables) {
    Assert-C9AdditiveScope $Schema $Uuid $Port
    Assert-C9AdditiveQuiescent
    $actual = @(Get-C9AdditiveTables $Schema $Uuid $Port)
    if (($Tables -join ',') -cne ($actual -join ',')) { Fail 'R06B2_ADDITIVE_INVENTORY' }
    $counts = [ordered]@{}
    foreach ($table in $actual) {
        $n = Invoke-Root 'mixed' ('SELECT COUNT(*) FROM ' + [char]96 + $table + [char]96) $Schema
        if ($n -cnotmatch '\A(?:0|[1-9][0-9]{0,9})\z') { Fail 'R06B2_ADDITIVE_INVENTORY' }
        $counts[$table] = [long]$n
    }
    Assert-C9AdditiveQuiescent
    return $counts
}
function Assert-C9AdditiveDumpRows([string] $File,[string[]] $Selected,[System.Collections.IDictionary] $Counts) {
    # --skip-extended-insert yields exactly one INSERT per row; every selected
    # table must match the independently observed count, even when the dump exits 0.
    $seen = @{}
    foreach ($name in $Selected) { $seen[$name] = [long]0 }
    foreach ($line in [IO.File]::ReadLines($File)) {
        if ($line.StartsWith('INSERT INTO ',[StringComparison]::Ordinal)) {
            if ($line -cnotmatch '^INSERT INTO \x60([A-Za-z_]+)\x60 ' -or $Selected -cnotcontains $Matches[1]) {
                Fail 'R06B1_DOMAIN_DUMP_SCOPE'
            }
            $seen[$Matches[1]]++
        }
    }
    foreach ($name in $Selected) {
        if ($seen[$name] -ne $Counts[$name]) { Fail 'R06B2_ADDITIVE_DUMP_ROW_COUNT' }
    }
}
function Assert-C9AdditiveGrants([string] $Schema,[string] $Uuid,[int] $Port) {
    Assert-C9AdditiveScope $Schema $Uuid $Port
    $tables = @(Get-C9AdditiveTables $Schema $Uuid $Port)
    # Explicit per-table SELECT, not a schema wildcard grant. SHOW GRANTS must
    # enumerate exactly the intended effective grants; no role, grant option or extra scope.
    foreach ($user in 'c9_add_importer','c9_add_ro') {
        $q = [string][char]96
        $account = $q + $user + $q + '@' + $q + '%' + $q
        $expected = @("GRANT USAGE ON *.* TO $account")
        foreach ($table in $tables) {
            $rights = 'SELECT'
            if ($user -ceq 'c9_add_importer') {
                if ($table -cin @('organization','user_account','membership','department','project','resource','resource_requirement','allocation')) { $rights += ', INSERT, UPDATE' }
                elseif ($table -cin @('stock_movement','audit_event')) { $rights += ', INSERT' }
            }
            $expected += "GRANT $rights ON $q$Schema$q.$q$table$q TO $account"
        }
        $actual = (Invoke-Root 'mixed' "SHOW GRANTS FOR '$user'@'%'") -split '\n'
        if ($actual.Count -ne $expected.Count -or @(Compare-Object -ReferenceObject $expected -DifferenceObject $actual -CaseSensitive).Count) {
            Fail 'R06B2_ADDITIVE_GRANTS'
        }
    }
}
function Assert-C9AdditiveGrantBoundary([string] $Schema,[string] $Uuid,[int] $Port,
                                          [string] $ImporterPassword,[string] $ReadPassword) {
    Assert-C9AdditiveGrants $Schema $Uuid $Port
    # Probe the actual accounts without mutating a row even if a grant is unexpectedly broad.
    foreach ($account in @(@{User='c9_add_importer';Password=$ImporterPassword},@{User='c9_add_ro';Password=$ReadPassword})) {
        $positive = Invoke-Sql 'mixed' $account.User $account.Password 'SELECT COUNT(*) FROM organization' $Schema
        if ($positive.Code -ne 0 -or $positive.Out -cnotmatch '\A[0-9]+\z') { Fail 'R06B2_ADDITIVE_GRANTS' }
        $foreignSchema = "orgresource_c9_local_{0}_mixed" -f $token
        if ($foreignSchema -cne $roles.mixed.Schema) { Fail 'R06B2_ADDITIVE_SCOPE' }
        $denied = Invoke-Sql 'mixed' $account.User $account.Password "SELECT COUNT(*) FROM $foreignSchema.organization" $Schema
        if ($denied.Code -eq 0 -or $denied.Code -eq 124 -or $denied.Err -cnotmatch 'ERROR 1142 .*command denied') { Fail 'R06B2_ADDITIVE_GRANTS' }
    }
    foreach ($table in 'stock_movement','audit_event') {
        $denied = Invoke-Sql 'mixed' 'c9_add_importer' $ImporterPassword "UPDATE $table SET id=id WHERE 1=0" $Schema
        if ($denied.Code -eq 0 -or $denied.Code -eq 124 -or $denied.Err -cnotmatch 'ERROR 1142 .*command denied') { Fail 'R06B2_ADDITIVE_GRANTS' }
    }
    $denied = Invoke-Sql 'mixed' 'c9_add_ro' $ReadPassword 'UPDATE organization SET display_name=display_name WHERE 1=0' $Schema
    if ($denied.Code -eq 0 -or $denied.Code -eq 124 -or $denied.Err -cnotmatch 'ERROR 1142 .*command denied') { Fail 'R06B2_ADDITIVE_GRANTS' }
}
function Save-C9AdditiveSnapshot([string] $Schema,[string] $Uuid,[int] $Port) {
    $tables = @(Get-C9AdditiveTables $Schema $Uuid $Port)
    $countsBefore = Get-C9AdditiveRowCounts $Schema $Uuid $Port $tables
    $domain = @($tables | Where-Object { $_ -inotmatch '\Aspring_session(?:_attributes)?\z' })
    $sessions = @($tables | Where-Object { $_ -imatch '\Aspring_session(?:_attributes)?\z' })
    if ($domain.Count -ne 16 -or $sessions.Count -ne 2) { Fail 'R06B2_ADDITIVE_INVENTORY' }
    $base = @('--no-defaults','--protocol=tcp','--host=127.0.0.1',"--port=$Port",'--user=root',
        '--skip-comments','--skip-dump-date','--hex-blob','--skip-lock-tables','--skip-add-drop-table',
        '--no-tablespaces','--set-gtid-purged=OFF','--skip-column-statistics','--single-transaction','--quick',
        '--no-create-info','--order-by-primary','--skip-add-locks','--skip-disable-keys','--skip-triggers','--skip-extended-insert')
    $paths = @{}
    foreach ($kind in 'domain','sessions') {
        Assert-C9AdditiveScope $Schema $Uuid $Port
        Assert-C9AdditiveQuiescent
        if (-not (Test-PlainPath "$work\dumps" 'Container') -or -not (Test-OwnerOnly "$work\dumps")) { Fail 'R06B2_ADDITIVE_DUMP_CUSTODY' }
        $file = New-Scratch "additive-$kind.sql"
        $err = New-Scratch "additive-$kind.err"
        # Reserve both names exclusively. The contained launcher reopens the files for stdout/stderr.
        # Native same-user path/handle substitution and real custody remain H03/H06 gates.
        foreach ($path in $file,$err) {
            $stream = [IO.File]::Open($path,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write,[IO.FileShare]::None)
            $stream.Dispose()
            if (-not (Test-PlainPath $path 'Leaf') -or -not (Test-OwnerOnly $path)) { Fail 'R06B2_ADDITIVE_DUMP_CUSTODY' }
        }
        $selected = if ($kind -ceq 'domain') { $domain } else { $sessions }
        if ((Invoke-Contained 'mixed' "$mysqlHome\bin\mysqldump.exe" ($base + @($Schema) + $selected) @{ MYSQL_PWD=$roles.mixed.Password } '' $file $err 600) -ne 0) {
            Fail 'R06B1_ROW_DUMP_FAILED'
        }
        if (-not (Test-PlainPath $file 'Leaf') -or -not (Test-OwnerOnly $file) -or
            (Get-Item -LiteralPath $file).Length -eq 0) { Fail 'R06B1_ROW_DUMP_FAILED' }
        Assert-C9AdditiveQuiescent
        Assert-C9AdditiveDumpRows $file $selected $countsBefore
        $paths[$kind] = $file
    }
    if ((@(Get-C9AdditiveTables $Schema $Uuid $Port) -join ',') -cne ($tables -join ',')) { Fail 'R06B2_ADDITIVE_INVENTORY' }
    $countsAfter = Get-C9AdditiveRowCounts $Schema $Uuid $Port $tables
    foreach ($name in $tables) {
        if ($countsBefore[$name] -ne $countsAfter[$name]) { Fail 'R06B2_ADDITIVE_NOT_QUIESCENT' }
    }
    foreach ($name in $sessions) {
        if ($countsBefore[$name] -ne 0 -or $countsAfter[$name] -ne 0) { Fail 'R06B2_ADDITIVE_SESSIONS' }
    }
    return [pscustomobject]@{ Domain=$paths.domain; Sessions=$paths.sessions; SessionCounts='0,0' }
}
function Assert-C9AdditiveSnapshotEqual([string] $Code,$Before,$After) {
    if ($Before.SessionCounts -cne $After.SessionCounts) { Fail $Code }
    Assert-Same $Code $Before.Domain $After.Domain
    Assert-Same $Code $Before.Sessions $After.Sessions
}
function Assert-C9AdditiveRepeatRefusal([int] $Code,[string] $Log) {
    # Exact pinned C8DemoImporter.java guard: assertCount(0, tagged cohort), line 163.
    # Generic invariant exception alone is insufficient; fail closed on missing callsite.
    if ($Code -eq 0 -or $Code -eq 124 -or
        -not (Test-Log $Log 'The persisted demo did not match its approved record or stock invariants.') -or
        -not (Test-Log $Log 'C8DemoImporter.importAdditionalPlanForIsolatedProof(C8DemoImporter.java:163)')) {
        Fail 'R06B2_ADDITIVE_REPEAT'
    }
}

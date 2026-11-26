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
function Save-C9AdditiveSnapshot([string] $Schema,[string] $Uuid,[int] $Port) {
    $tables = @(Get-C9AdditiveTables $Schema $Uuid $Port)
    $domain = @($tables | Where-Object { $_ -inotmatch '\Aspring_session(?:_attributes)?\z' })
    $sessions = @($tables | Where-Object { $_ -imatch '\Aspring_session(?:_attributes)?\z' })
    if ($domain.Count -ne 16 -or $sessions.Count -ne 2) { Fail 'R06B2_ADDITIVE_INVENTORY' }
    $base = @('--no-defaults','--protocol=tcp','--host=127.0.0.1',"--port=$Port",'--user=root',
        '--skip-comments','--skip-dump-date','--hex-blob','--skip-lock-tables','--skip-add-drop-table',
        '--no-tablespaces','--set-gtid-purged=OFF','--skip-column-statistics','--single-transaction','--quick',
        '--no-create-info','--order-by-primary','--skip-add-locks','--skip-disable-keys','--skip-triggers')
    $paths = @{}
    foreach ($kind in 'domain','sessions') {
        Assert-C9AdditiveScope $Schema $Uuid $Port
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
        # The actual table list is explicit; also reject unexpected INSERT targets in the dump.
        # No raw lines are emitted.
        foreach ($match in @(Select-String -LiteralPath $file -CaseSensitive -Pattern '^INSERT INTO ')) {
            if ($match.Line -cnotmatch '^INSERT INTO \x60([A-Za-z_]+)\x60 ' -or $selected -cnotcontains $Matches[1]) {
                Fail 'R06B1_DOMAIN_DUMP_SCOPE'
            }
        }
        $paths[$kind] = $file
    }
    if ((@(Get-C9AdditiveTables $Schema $Uuid $Port) -join ',') -cne ($tables -join ',')) { Fail 'R06B2_ADDITIVE_INVENTORY' }
    $counts = Invoke-Root 'mixed' 'SELECT (SELECT COUNT(*) FROM SPRING_SESSION),(SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES)' $Schema
    if ($counts -cne (@('0','0') -join [char]9)) { Fail 'R06B2_ADDITIVE_SESSIONS' }
    return [pscustomobject]@{ Domain=$paths.domain; Sessions=$paths.sessions; SessionCounts=$counts }
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

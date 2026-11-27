# Test only the real release-inventory block; never run the full packager here.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$packager = Join-Path $PSScriptRoot 'package-extended-c.ps1'
$tokens = $null
$errors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile($packager, [ref]$tokens, [ref]$errors)
if ($errors.Count) { throw 'Release packager has PowerShell parser errors.' }
$statements = @($ast.EndBlock.Statements)
$starts = @()
$ends = @()
for ($i = 0; $i -lt $statements.Count; $i++) {
    $statement = $statements[$i]
    if ($statement -isnot [System.Management.Automation.Language.AssignmentStatementAst] -or
        $statement.Left -isnot [System.Management.Automation.Language.VariableExpressionAst]) { continue }
    if ($statement.Left.VariablePath.UserPath -ceq 'migrationRoot') { $starts += $i }
    if ($statement.Left.VariablePath.UserPath -ceq 'artifacts') { $ends += $i }
}
if ($starts.Count -ne 1 -or $ends.Count -ne 1 -or $starts[0] -ge $ends[0]) {
    throw 'Cannot locate the exact release inventory guard.'
}
$guardText = (@($statements[$starts[0]..($ends[0] - 1)] | ForEach-Object { $_.Extent.Text }) -join "`n")
$guard = [scriptblock]::Create($guardText)
$names = @(
    'V1__extended_c1_baseline.sql',
    'V2__spring_session_jdbc_mysql.sql',
    'V3__atomic_inventory_ledger.sql',
    'V4__c3_contact_images_and_reminders.sql',
    'V5__c4_reminder_dispatch_boundary.sql',
    'V6__c8_project_leads_and_linked_borrowers.sql',
    'V7__c8_local_demo_provenance.sql'
)
$sourcePrefix = 'extended/backend/src/main/resources/db/migration/'
$jarPrefix = 'BOOT-INF/classes/db/migration/'
$sourceOk = @($names | ForEach-Object { $sourcePrefix + $_ })
$jarOk = @($names | ForEach-Object { $jarPrefix + $_ })
$sourceRefusal = 'Source migration inventory differs from the approved V1–V7 release lane.'
$jarRefusal = 'Executable JAR migration inventory differs from the approved V1–V7 release lane.'
$script:casesPassed = 0
function Assert-Case {
    param([string] $Name, [string[]] $Source, [string[]] $Jar, [string] $Expected = '')
    $entries = @($Source)
    $jarContents = @($Jar)
    $actual = ''
    try { & $guard } catch { $actual = $_.Exception.Message }
    if ($actual -cne $Expected) {
        throw "Unexpected guard disposition: $Name (expected '$Expected', actual '$actual')"
    }
    $script:casesPassed++
    Write-Output "PASS: $Name"
}

Assert-Case -Name 'exact inventories' -Source $sourceOk -Jar $jarOk
Assert-Case -Name 'order independence and unrelated assets' -Source @($sourceOk[6..0] + 'extended/frontend/package.json') -Jar @($jarOk[6..0] + $jarPrefix + 'BOOT-INF/classes/static/index.html')
foreach ($side in @('source', 'jar')) {
    $prefix = if ($side -ceq 'source') { $sourcePrefix } else { $jarPrefix }
    $correct = if ($side -ceq 'source') { $sourceOk } else { $jarOk }
    $variants = @(
        @{ Name = 'missing V6'; Files = @($correct | Where-Object { $_ -cne ($prefix + $names[5]) }) },
        @{ Name = 'missing V7'; Files = @($correct[0..5]) },
        @{ Name = 'extra V8'; Files = @($correct + ($prefix + 'V8__unexpected.sql')) },
        @{ Name = 'duplicate V7'; Files = @($correct + $correct[6]) },
        @{ Name = 'same-count duplicate replacing V7'; Files = @($correct[0..5] + $correct[5]) },
        @{ Name = 'renamed V7'; Files = @($correct[0..5] + ($prefix + 'V7__wrong.sql')) },
        @{ Name = 'wrong-case V7 filename'; Files = @($correct[0..5] + ($prefix + $names[6].ToLowerInvariant())) },
        @{ Name = 'empty'; Files = @() }
    )
    foreach ($variant in $variants) {
        if ($side -ceq 'source') {
            Assert-Case -Name "$side $($variant.Name)" -Source $variant.Files -Jar $jarOk -Expected $sourceRefusal
        } else {
            Assert-Case -Name "$side $($variant.Name)" -Source $sourceOk -Jar $variant.Files -Expected $jarRefusal
        }
    }
}

Push-Location $root
try {
    $tracked = @(git ls-tree -r --name-only HEAD -- extended/backend/src/main/resources/db/migration)
    if ($LASTEXITCODE -ne 0) { throw 'Cannot enumerate exact-head migration tree.' }
} finally { Pop-Location }
Assert-Case -Name 'actual tracked migration inventory' -Source $tracked -Jar $jarOk

# Reproduce the packager's tar listing operation on harmless ZIP-format JAR fixtures.
$tmp = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-ReleaseInventory-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $tmp
try {
    foreach ($variant in @(
        @{ Name = 'actual archive exact'; Files = $jarOk; Expected = '' },
        @{ Name = 'actual archive missing V7'; Files = @($jarOk[0..5]); Expected = $jarRefusal }
    )) {
        $file = Join-Path $tmp ($variant.Name.Replace(' ', '-') + '.jar')
        $archive = [IO.Compression.ZipFile]::Open($file, [IO.Compression.ZipArchiveMode]::Create)
        try {
            foreach ($name in $variant.Files) { $null = $archive.CreateEntry($name) }
        } finally { $archive.Dispose() }
        $inventory = @(tar -tf $file)
        if ($LASTEXITCODE -ne 0) { throw 'Synthetic JAR inventory listing failed.' }
        Assert-Case -Name $variant.Name -Source $tracked -Jar $inventory -Expected $variant.Expected
    }
} finally {
    Remove-Item -LiteralPath $tmp -Recurse -Force
}
if ($script:casesPassed -ne 21) { throw "Expected 21 executed cases, got $script:casesPassed" }
Write-Output 'Release inventory guard: 21/21 passed (PowerShell and synthetic JAR listings).'

# Execute the real byte guards without running the package or accessing a DB.
foreach ($functionName in @('Assert-ReleaseCommand', 'Assert-FrozenMigrationSource', 'Get-FrozenMigrationDigests', 'Assert-PackagedMigrations')) {
    $matches = @($ast.FindAll({
        param($node)
        $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -ceq $functionName
    }, $false))
    if ($matches.Count -ne 1) { throw "Missing or duplicate production function: $functionName" }
    . ([scriptblock]::Create($matches[0].Extent.Text))
}
$pinStatements = @($statements | Where-Object {
    $_ -is [System.Management.Automation.Language.AssignmentStatementAst] -and
    $_.Left -is [System.Management.Automation.Language.VariableExpressionAst] -and
    $_.Left.VariablePath.UserPath -ceq 'migrationPins'
})
if ($pinStatements.Count -ne 1) { throw 'Cannot locate frozen migration pins.' }
. ([scriptblock]::Create($pinStatements[0].Extent.Text))
if ($migrationPins.Count -ne $names.Count -or @(Compare-Object $names @($migrationPins.Keys) -CaseSensitive).Count) {
    throw 'Frozen pins do not match the independent expected names.'
}
$sourceTmp = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-ReleaseBytes-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $sourceTmp
Push-Location $root
try {
    $migrationDir = $sourcePrefix.TrimEnd('/')
    Assert-FrozenMigrationSource -Revision HEAD -MigrationDir $migrationDir -Pins $migrationPins
    Write-Output 'PASS: exact tracked migration blob pins'
    $badPins = [ordered]@{}
    foreach ($name in $migrationPins.Keys) { $badPins[$name] = $migrationPins[$name] }
    $badPins[$names[6]] = '0000000000000000000000000000000000000000'
    $failure = ''
    try { Assert-FrozenMigrationSource -Revision HEAD -MigrationDir $migrationDir -Pins $badPins } catch { $failure = $_.Exception.Message }
    if ($failure -cne "Frozen migration changed: $($names[6])") { throw 'Wrong refusal for altered source pin.' }
    Write-Output 'PASS: wrong tracked migration pin refused'
    $archivePath = Join-Path $sourceTmp 'source.zip'
    git archive --format=zip "--output=$archivePath" HEAD -- $migrationDir
    Assert-ReleaseCommand 'Migration-only Git archive'
    $archiveRoot = Join-Path $sourceTmp 'extracted'
    $null = New-Item -ItemType Directory -Path $archiveRoot
    tar -xf $archivePath -C $archiveRoot
    Assert-ReleaseCommand 'Migration-only Git archive extraction'
    $actualDigests = Get-FrozenMigrationDigests -SourceRoot $archiveRoot -MigrationDir $migrationDir -Pins $migrationPins
    if ($actualDigests.Count -ne 7) { throw 'Incomplete extracted source digest inventory.' }
    $jarPath = Join-Path $sourceTmp 'exact.jar'
    $zip = [IO.Compression.ZipFile]::Open($jarPath, [IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($name in $names) {
            $null = [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, (Join-Path $archiveRoot "$migrationDir/$name"), ($jarPrefix + $name))
        }
    } finally { $zip.Dispose() }
    Assert-PackagedMigrations -JarPath $jarPath -Expected $actualDigests
    Write-Output 'PASS: exact tracked archive bytes through JAR digest guard'
    [IO.File]::AppendAllText((Join-Path $archiveRoot "$migrationDir/$($names[6])"), '-- fictional byte alteration')
    $failure = ''
    try { $null = Get-FrozenMigrationDigests -SourceRoot $archiveRoot -MigrationDir $migrationDir -Pins $migrationPins } catch { $failure = $_.Exception.Message }
    if ($failure -cne "Extracted migration bytes differ: $($names[6])") { throw 'Wrong refusal for modified extracted bytes.' }
    Write-Output 'PASS: altered extracted migration refused'
} finally {
    Pop-Location
    Remove-Item -LiteralPath $sourceTmp -Recurse -Force
}

$root = Join-Path ([IO.Path]::GetTempPath()) ('orgresource-package-migration-test-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $root
try {
    $expected = [ordered]@{}
    $rows = @(1..7 | ForEach-Object {
        $name = "V$($_)__fictional.sql"
        $bytes = [Text.Encoding]::UTF8.GetBytes("SELECT $_;")
        $expected[$name] = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes))
        [pscustomobject]@{ Name = $name; Bytes = $bytes }
    })
    $prefix = 'BOOT-INF/classes/db/migration/'
    function Test-Archive([string] $Case, [object[]] $Rows, [string] $ExpectedError = '') {
        $path = Join-Path $root "$Case.jar"
        $zip = [IO.Compression.ZipFile]::Open($path, [IO.Compression.ZipArchiveMode]::Create)
        try {
            $null = $zip.CreateEntry($prefix)
            foreach ($row in $Rows) {
                $entry = $zip.CreateEntry($prefix + $row.Name)
                $stream = $entry.Open()
                try { $stream.Write($row.Bytes, 0, $row.Bytes.Length) } finally { $stream.Dispose() }
            }
        } finally { $zip.Dispose() }
        $failure = $null
        try { Assert-PackagedMigrations -JarPath $path -Expected $expected } catch { $failure = $_.Exception.Message }
        if ($ExpectedError) {
            if ($null -eq $failure -or -not $failure.StartsWith($ExpectedError, [StringComparison]::Ordinal)) {
                throw "Wrong refusal for $Case."
            }
        } elseif ($null -ne $failure) { throw "Unexpected refusal for $Case." }
        Write-Output "PASS $Case"
    }
    Test-Archive 'exact-seven' $rows
    Test-Archive 'missing-v6' @($rows | Where-Object Name -cne 'V6__fictional.sql') 'Executable JAR migration inventory count differs.'
    Test-Archive 'missing-v7' @($rows | Where-Object Name -cne 'V7__fictional.sql') 'Executable JAR migration inventory count differs.'
    Test-Archive 'extra-v8' ($rows + [pscustomobject]@{Name='V8__fictional.sql';Bytes=$rows[0].Bytes}) 'Executable JAR migration inventory count differs.'
    Test-Archive 'duplicate-v6-replaces-v7' ($rows[0..5] + $rows[5]) 'Executable JAR migration missing or duplicated:'
    Test-Archive 'wrong-case' ($rows[0..5] + [pscustomobject]@{Name='v7__fictional.sql';Bytes=$rows[6].Bytes}) 'Executable JAR migration missing or duplicated:'
    Test-Archive 'same-name-changed-bytes' ($rows[0..5] + [pscustomobject]@{Name=$rows[6].Name;Bytes=[Text.Encoding]::UTF8.GetBytes('SELECT 0;')}) 'Executable JAR migration bytes differ:'
    Test-Archive 'wrong-description' ($rows[0..5] + [pscustomobject]@{Name='V7__other.sql';Bytes=$rows[6].Bytes}) 'Executable JAR migration missing or duplicated:'
    Write-Output '8 fictional packaged migration assertions passed.'
} finally {
    Remove-Item -LiteralPath $root -Recurse -Force
}

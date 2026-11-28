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
foreach ($functionName in @('Assert-ReleaseCommand', 'New-ReleaseSourceArchive', 'Assert-FrozenMigrationSource', 'Get-FrozenMigrationDigests', 'Assert-PackagedMigrations')) {
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
    New-ReleaseSourceArchive -ArchivePath $archivePath -Revision HEAD -Paths @($migrationDir)
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

# Full-source inventory/byte guard: execute production AST functions only.
foreach ($functionName in @('Get-ReleaseSourcePins','Expand-VerifiedReleaseSource')) {
    $definitions = @($ast.FindAll({
        param($node)
        $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -ceq $functionName
    }, $false))
    if ($definitions.Count -ne 1) { throw "Missing production function: $functionName" }
    . ([scriptblock]::Create($definitions[0].Extent.Text))
}
$assignment = @($ast.FindAll({
    param($node)
    $node -is [System.Management.Automation.Language.AssignmentStatementAst] -and
    $node.Left -is [System.Management.Automation.Language.VariableExpressionAst] -and
    $node.Left.VariablePath.UserPath -ceq 'included'
}, $true))
if ($assignment.Count -ne 1) { throw 'Cannot locate exact source allowlist.' }
. ([scriptblock]::Create($assignment[0].Extent.Text))
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$testRoot = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-SourcePins-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $testRoot
$script:sourceCases=0
function Assert-SourceCase([string] $Name,[scriptblock] $Action,[string] $Expected='') {
    $failure=''
    try { & $Action | Out-Null } catch { $failure=$_.Exception.Message }
    if ($failure -cne $Expected) { throw "Unexpected source guard disposition: $Name (expected '$Expected', actual '$failure')" }
    $script:sourceCases++
    Write-Output "PASS source guard: $Name"
}
Push-Location $repo
try {
    $rows = @(git ls-tree -r HEAD -- @included)
    Assert-ReleaseCommand 'Actual source inventory fixture'
    $pins = Get-ReleaseSourcePins $rows
    $full = Join-Path $testRoot 'actual.zip'
    New-ReleaseSourceArchive -ArchivePath $full -Revision HEAD -Paths $included
    $dest = Join-Path $testRoot 'actual'
    $null = New-Item -ItemType Directory -Path $dest
    Assert-SourceCase 'actual full tracked allowlist' { Expand-VerifiedReleaseSource $full $dest $pins }
    if (@(Get-ChildItem -LiteralPath $dest -File -Recurse -Force).Count -ne $pins.Count) { throw 'Wrong actual extracted count' }
    Write-Output "SOURCE_ALLOWLIST_EXACT:files=$($pins.Count)"

    $tiny = Join-Path $testRoot 'fixture.txt'
    [IO.File]::WriteAllBytes($tiny,[byte[]](65,10))
    $hash = git hash-object --no-filters -- $tiny
    Assert-ReleaseCommand 'Fictional source blob'
    $row = "100644 blob $hash`t"
    $tinyPins = Get-ReleaseSourcePins @(($row+'a/x.txt'),($row+'a/y.txt'))
    Assert-SourceCase 'case-aliased Git paths' { Get-ReleaseSourcePins @(($row+'A/x.txt'),($row+'a/y.txt')) } 'Case-aliased tracked release-source path.'
    Assert-SourceCase 'device path' { Get-ReleaseSourcePins @(($row+'a/CON.txt')) } 'Unsupported tracked release-source path.'
    Assert-SourceCase 'traversal Git path' { Get-ReleaseSourcePins @(($row+'a/../x.txt')) } 'Unsupported tracked release-source path.'
    Assert-SourceCase 'file directory collision' { Get-ReleaseSourcePins @(($row+'a'),($row+'a/x.txt')) } 'Conflicting tracked release-source path.'
    Assert-SourceCase 'unsupported Git mode' { Get-ReleaseSourcePins @("120000 blob $hash`ta/x.txt") } 'Unsupported tracked release-source entry.'
    function Test-SourceZip([string] $Case,[string[]] $Names,[string] $Expected='', [switch] $Alter,[switch] $Link) {
        $file = Join-Path $testRoot "$Case.zip"
        $out = Join-Path $testRoot $Case
        $null = New-Item -ItemType Directory -Path $out
        $zip = [IO.Compression.ZipFile]::Open($file,[IO.Compression.ZipArchiveMode]::Create)
        try {
            foreach ($name in $Names) {
                $entry = $zip.CreateEntry($name)
                if ($Link -and $name -ceq 'a/x.txt') { $entry.ExternalAttributes = -1610612736 }
                if ($name.EndsWith('/')) { continue }
                $stream = $entry.Open()
                try {
                    $bytes = if ($Alter -and $name -ceq 'a/x.txt') { [byte[]](66,10) } else { [byte[]](65,10) }
                    $stream.Write($bytes,0,$bytes.Length)
                } finally { $stream.Dispose() }
            }
        } finally { $zip.Dispose() }
        Assert-SourceCase $Case { Expand-VerifiedReleaseSource $file $out $tinyPins } $Expected
    }
    Test-SourceZip 'exact' @('a/','a/x.txt','a/y.txt')
    Test-SourceZip 'missing' @('a/x.txt') 'Release ZIP file inventory differs.'
    Test-SourceZip 'duplicate' @('a/x.txt','a/x.txt','a/y.txt') 'Duplicate or case-aliased release ZIP entry.'
    Test-SourceZip 'case-alias' @('a/x.txt','a/X.txt','a/y.txt') 'Duplicate or case-aliased release ZIP entry.'
    Test-SourceZip 'wrong-case' @('A/x.txt','a/y.txt') 'Unexpected release ZIP file.'
    Test-SourceZip 'extra' @('a/x.txt','a/y.txt','private.txt') 'Unexpected release ZIP file.'
    Test-SourceZip 'traversal' @('a/x.txt','a/y.txt','../outside.txt') 'Unexpected release ZIP file.'
    Test-SourceZip 'extra-directory' @('a/x.txt','a/y.txt','extra/') 'Unexpected release ZIP directory.'
    Test-SourceZip 'changed-bytes' @('a/x.txt','a/y.txt') 'Extracted release source bytes differ: a/x.txt' -Alter
    Test-SourceZip 'link-type' @('a/x.txt','a/y.txt') 'Unsupported release ZIP entry type.' -Link
    if (Test-Path -LiteralPath (Join-Path $testRoot 'outside.txt')) { throw 'Unexpected traversal output' }
    if ($script:sourceCases -ne 16) { throw "Wrong source case count: $script:sourceCases" }
    Write-Output 'Full source archive guard: 16/16 passed.'
} finally {
    Pop-Location
    Remove-Item -LiteralPath $testRoot -Recurse -Force
}

# Actual read-only package checker with fictional payloads; never launch Java.
$checker = Join-Path $PSScriptRoot 'verify-extended-package-integrity.ps1'
$testRoot = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-PackageCheck-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $testRoot
$script:packageCases=0
$source = 'a' * 40
$payloads = @('OrgResource-Extended-C6a-Source.zip','OrgResource-Extended-C6a-Java21.jar','VERIFICATION.log')
function Build-CheckFixture([string] $Name) {
    $dir = Join-Path $testRoot $Name
    $null = New-Item -ItemType Directory -Path $dir
    $records = @(foreach ($file in $payloads) {
        $path = Join-Path $dir $file
        [IO.File]::WriteAllText($path,'fictional payload')
        [ordered]@{name=$file;bytes=[IO.FileInfo]::new($path).Length;sha256=(Get-FileHash -LiteralPath $path).Hash.ToLowerInvariant()}
    })
    $manifest=[ordered]@{status='local-release-candidate';sourceSha=$source;protectedCourseSha='7f8c0746da18fd54b47f430ac1435ac36e605d67';courseTrees=@{backend=('b'*40);frontend=('c'*40)};extractedSourceRoot='fictional';environment=@{};artifacts=$records}
    $manifest | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $dir 'RELEASE-MANIFEST.json') -Encoding utf8
    return $dir
}
function Seal-CheckFixture([string] $Dir,[switch] $Duplicate) {
    $rows = @(foreach ($file in @($payloads + 'RELEASE-MANIFEST.json')) {
        "$((Get-FileHash -LiteralPath (Join-Path $Dir $file)).Hash.ToLowerInvariant())  $file"
    })
    if ($Duplicate) { $rows[1]=$rows[0] }
    $sum=Join-Path $Dir 'SHA256SUMS.txt'
    $rows | Set-Content -LiteralPath $sum -Encoding utf8
    $pin=(Get-FileHash -LiteralPath $sum).Hash.ToLowerInvariant()
    [ordered]@{sourceSha=$source;checksumsSha256=$pin} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $Dir 'READY.json') -Encoding utf8
    return $pin
}
function Check-FictionalPackage([string] $Name,[string] $Expected='', [scriptblock] $BeforeSeal, [scriptblock] $AfterSeal,[switch] $Duplicate,[switch] $WrongSource,[switch] $WrongPin) {
    $dir=Build-CheckFixture $Name
    if ($BeforeSeal) { & $BeforeSeal $dir }
    $pin=Seal-CheckFixture $dir -Duplicate:$Duplicate
    if ($AfterSeal) { & $AfterSeal $dir }
    $actual=''
    try {
        $output=@(& $checker -PackageRoot $dir -ExpectedSourceSha $(if($WrongSource){'d'*40}else{$source}) -ExpectedChecksumsSha256 $(if($WrongPin){'e'*64}else{$pin}))
        if ($output.Count -ne 1 -or $output[0] -cne 'EXTENDED_PACKAGE_INTEGRITY_OK:files=6 payloads=3 source_and_checksum_pinned=true') { throw 'WRONG_SUCCESS_MARKER' }
    } catch { $actual=$_.Exception.Message }
    if ($actual -cne $Expected) { throw "Package checker case $Name expected '$Expected' got '$actual'" }
    $script:packageCases++
    Write-Output "PASS package checker: $Name"
}
try {
    Check-FictionalPackage 'exact'
    Check-FictionalPackage 'wrong-source' 'PACKAGE_READY_IDENTITY' -WrongSource
    Check-FictionalPackage 'wrong-anchor' 'PACKAGE_READY_IDENTITY' -WrongPin
    Check-FictionalPackage 'missing-ready' 'PACKAGE_INVENTORY' -AfterSeal { param($d) Remove-Item -LiteralPath (Join-Path $d 'READY.json') }
    Check-FictionalPackage 'extra-file' 'PACKAGE_INVENTORY' -AfterSeal { param($d) [IO.File]::WriteAllText((Join-Path $d 'extra.txt'),'x') }
    Check-FictionalPackage 'payload-mutation' 'PACKAGE_ARTIFACT_HASH' -AfterSeal { param($d) [IO.File]::AppendAllText((Join-Path $d $payloads[0]),'x') }
    Check-FictionalPackage 'list-mutation' 'PACKAGE_CHECKSUM_LIST_HASH' -AfterSeal { param($d) [IO.File]::AppendAllText((Join-Path $d 'SHA256SUMS.txt'),'x') }
    Check-FictionalPackage 'duplicate-list' 'PACKAGE_CHECKSUM_LIST_SHAPE' -Duplicate
    Check-FictionalPackage 'duplicate-ready-key' 'PACKAGE_JSON_SHAPE' -AfterSeal { param($d) $p=Join-Path $d 'READY.json';$raw=[IO.File]::ReadAllText($p);[IO.File]::WriteAllText($p,$raw.Insert(1,'"sourceSha":"fictional",')) }
    Check-FictionalPackage 'manifest-size-lie' 'PACKAGE_MANIFEST_ARTIFACTS' -BeforeSeal { param($d) $p=Join-Path $d 'RELEASE-MANIFEST.json';$m=Get-Content -LiteralPath $p -Raw | ConvertFrom-Json -AsHashtable;$m.artifacts[0].bytes++;$m | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $p -Encoding utf8 }
    Check-FictionalPackage 'manifest-source-lie' 'PACKAGE_MANIFEST_IDENTITY' -BeforeSeal { param($d) $p=Join-Path $d 'RELEASE-MANIFEST.json';$m=Get-Content -LiteralPath $p -Raw | ConvertFrom-Json -AsHashtable;$m.sourceSha='f'*40;$m | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $p -Encoding utf8 }
    Check-FictionalPackage 'directory-instead-of-payload' 'PACKAGE_FILE_TYPE' -AfterSeal { param($d) $p=Join-Path $d $payloads[0];Remove-Item -LiteralPath $p;$null=New-Item -ItemType Directory -Path $p }
    if ($script:packageCases -ne 12) { throw 'Wrong package checker case count' }
    Write-Output 'Read-only package integrity checker: 12/12 passed.'
} finally { Remove-Item -LiteralPath $testRoot -Recurse -Force }

# Exercise the actual isolated-client environment initializer, not the DB runner.
$c1Path = Join-Path $PSScriptRoot 'verify-extended-c1.ps1'
$tokens = $null; $errors = $null
$c1Ast = [System.Management.Automation.Language.Parser]::ParseFile($c1Path,[ref]$tokens,[ref]$errors)
if ($errors.Count) { throw 'C1 verifier parser errors.' }
$functions = @($c1Ast.FindAll({param($node) $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -ceq 'Initialize-C1MysqlClientIsolation'},$true))
if ($functions.Count -ne 1) { throw 'C1 isolation function missing or ambiguous.' }
. ([scriptblock]::Create($functions[0].Extent.Text))
$caseRoot = Join-Path ([IO.Path]::GetTempPath()) ('OrgResource-ClientIsolation-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $caseRoot
$previousLogin = [Environment]::GetEnvironmentVariable('MYSQL_TEST_LOGIN_FILE','Process')
$previousPassword = [Environment]::GetEnvironmentVariable('MYSQL_PWD','Process')
try {
    $fictionalLogin = Join-Path $caseRoot 'unrelated-login.cnf'
    [IO.File]::WriteAllText($fictionalLogin,'fictional sentinel')
    $env:MYSQL_TEST_LOGIN_FILE = $fictionalLogin
    $env:MYSQL_PWD = 'fictional-sentinel'
    $data = Join-Path $caseRoot 'disposable'
    $null = New-Item -ItemType Directory -Path $data
    Initialize-C1MysqlClientIsolation $data
    $expected = Join-Path $data '.orgresource-no-login.cnf'
    if ($env:MYSQL_TEST_LOGIN_FILE -cne $expected -or $null -ne [Environment]::GetEnvironmentVariable('MYSQL_PWD','Process') -or
        (Test-Path -LiteralPath $expected) -or [IO.File]::ReadAllText($fictionalLogin) -cne 'fictional sentinel') {
        throw 'C1 isolated login environment differs.'
    }
    'PASS C1 isolation: alternate absent path, password removed, unrelated file unchanged'
    [IO.File]::WriteAllText($expected,'fictional existing login')
    $env:MYSQL_TEST_LOGIN_FILE = $fictionalLogin
    $env:MYSQL_PWD = 'fictional-sentinel'
    $failure = ''
    try { Initialize-C1MysqlClientIsolation $data } catch { $failure = $_.Exception.Message }
    if ($failure -cne 'C1_MYSQL_ISOLATION_LOGIN_EXISTS' -or $env:MYSQL_TEST_LOGIN_FILE -cne $fictionalLogin -or
        $env:MYSQL_PWD -cne 'fictional-sentinel' -or [IO.File]::ReadAllText($expected) -cne 'fictional existing login') {
        throw 'Existing login path refusal changed state.'
    }
    'PASS C1 isolation: existing alternate login refused without mutation'
    foreach ($bad in @('relative', (Join-Path $caseRoot 'missing'))) {
        $failure = ''
        try { Initialize-C1MysqlClientIsolation $bad } catch { $failure = $_.Exception.Message }
        if ($failure -cne 'C1_MYSQL_ISOLATION_DIRECTORY' -or $env:MYSQL_TEST_LOGIN_FILE -cne $fictionalLogin -or
            $env:MYSQL_PWD -cne 'fictional-sentinel') { throw 'Invalid directory refusal changed state.' }
    }
    'PASS C1 isolation: relative and missing directories refused'
    $source = [IO.File]::ReadAllText($c1Path)
    foreach ($literal in @(
        '$startInfo.Arguments = "--no-defaults --protocol=tcp --host=127.0.0.1 --port=$databasePort --user=root --skip-password --batch --raw"',
        'Wait-C1DatabaseReady -Process $dbProcess -Port $sourceDatabasePort',
        '& $mysqladmin --no-defaults --protocol=tcp --host=127.0.0.1 "--port=$Port" --user=root --skip-password shutdown',
        'Initialize-C1MysqlClientIsolation $dataDirectory',
        "'SPRING_PROFILES_ACTIVE', 'MYSQL_TEST_LOGIN_FILE', 'MYSQL_PWD'"
    )) {
        if (-not $source.Contains($literal,[StringComparison]::Ordinal)) { throw 'C1 native client source binding differs.' }
    }
    'PASS C1 isolation: bootstrap/readiness/shutdown argv and environment binding'
    'C1_MYSQL_CLIENT_ISOLATION_GUARD_OK:4 fixture cases, 5 source bindings, no MySQL launched'
} finally {
    [Environment]::SetEnvironmentVariable('MYSQL_TEST_LOGIN_FILE',$previousLogin,'Process')
    [Environment]::SetEnvironmentVariable('MYSQL_PWD',$previousPassword,'Process')
    Remove-Item -LiteralPath $caseRoot -Recurse -Force
}

# Execute the actual parent/recovery finally blocks with fictional spies only.
# The network/process stop implementation is intentionally not invoked.
$recoveryPath = Join-Path $PSScriptRoot 'verify-extended-c-recovery.ps1'
function Get-OuterCleanup([string] $Path) {
    $parseTokens = $null; $parseErrors = $null
    $parsed = [System.Management.Automation.Language.Parser]::ParseFile($Path,[ref]$parseTokens,[ref]$parseErrors)
    if ($parseErrors.Count) { throw 'C1 cleanup script parser errors.' }
    $outer = @($parsed.EndBlock.Statements | Where-Object {
        $_ -is [System.Management.Automation.Language.TryStatementAst] -and $_.Finally
    })
    if ($outer.Count -ne 1) { throw 'Cannot locate exact C1 outer cleanup.' }
    $block = $outer[0].Finally.Extent.Text
    [scriptblock]::Create($block.Substring(1,$block.Length-2))
}
$parentCleanup = Get-OuterCleanup $c1Path
$recoveryCleanup = Get-OuterCleanup $recoveryPath
function Assert-C1CleanupCase([string] $Lane,[string] $Fault) {
    $sourceDatabasePort = 14321; $sourcePort = $sourceDatabasePort
    $recoveryPort = 14322; $databasePort = $recoveryPort
    $dbProcess = 'fictional-source'; $target = 'fictional-target'
    $environmentNames = @('ORGRESOURCE_FICTIONAL_CLEANUP_SENTINEL')
    $envNames = $environmentNames
    $savedEnvironment = @{ ORGRESOURCE_FICTIONAL_CLEANUP_SENTINEL = 'prior' }
    $savedRecoveryEnvironment = $savedEnvironment
    $contactKey = [byte[]]@(1,2); $backupKey = [byte[]]@(3,4)
    $oldSession = $null; $restored = $null; $otherActor = $null; $disabledMember = $null
    $spy = [System.Collections.Generic.List[string]]::new()
    function Stop-C1App {
        $spy.Add('app')
        if ($Fault -ceq 'app') { throw 'fictional app fault' }
    }
    function Stop-C1Database($Process,[int]$Port) {
        $spy.Add("${Process}:$Port")
        if ($Fault -ceq 'db') { throw 'fictional db fault' }
    }
    $prior = [Environment]::GetEnvironmentVariable($environmentNames[0],'Process')
    try {
        [Environment]::SetEnvironmentVariable($environmentNames[0],'temporary','Process')
        $failure = ''
        try {
            if ($Lane -ceq 'parent') { . $parentCleanup } else { . $recoveryCleanup }
        } catch { $failure = $_.Exception.Message }
        $expectedFailure = if ($Fault -ceq 'none') { '' } else { "fictional $Fault fault" }
        $expectedStop = if ($Lane -ceq 'parent') { 'fictional-source:14321' } else { 'fictional-target:14322' }
        if ($failure -cne $expectedFailure -or ($spy -join ',') -cne "app,$expectedStop" -or
            [Environment]::GetEnvironmentVariable($environmentNames[0],'Process') -cne 'prior') {
            throw "C1 cleanup disposition differs: $Lane/$Fault"
        }
        if ($Lane -ceq 'recovery' -and ($databasePort -ne $sourcePort -or
            @($contactKey | Where-Object { $_ -ne 0 }).Count -or @($backupKey | Where-Object { $_ -ne 0 }).Count)) {
            throw 'C1 recovery restoration or key clearing differs.'
        }
        "PASS C1 cleanup: $Lane/$Fault"
    } finally { [Environment]::SetEnvironmentVariable($environmentNames[0],$prior,'Process') }
}
foreach ($lane in @('parent','recovery')) {
    foreach ($fault in @('none','app','db')) { Assert-C1CleanupCase $lane $fault }
}
'C1_CLEANUP_CONTROL_FLOW_OK:6 fictional cases, actual finally blocks, no MySQL or process termination'

# Real Windows TCP listener attribution, plus fictional snapshots for refusals.
# No MySQL executable, external endpoint or termination is invoked.
foreach ($name in @('Test-C1OwnedListener','Assert-C1OwnedListener')) {
    $defs = @($c1Ast.FindAll({
        param($node)
        $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -ceq $name
    },$false))
    if ($defs.Count -ne 1) { throw 'Cannot locate exact listener helper.' }
    . ([scriptblock]::Create($defs[0].Extent.Text))
}
$ownProcess = [Diagnostics.Process]::GetCurrentProcess()
$fixtureListener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback,0)
$fixtureListener.Start()
try {
    $fixturePort = $fixtureListener.LocalEndpoint.Port
    if (-not (Test-C1OwnedListener -Process $ownProcess -Port $fixturePort)) { throw 'Real listener not attributed.' }
    'PASS C1 listener: actual numeric loopback listener owned by current process'
} finally { $fixtureListener.Stop(); $ownProcess.Dispose() }
function Assert-C1ListenerSnapshot([string] $Case,[object[]] $Rows,[string] $Expected) {
    $self = [Diagnostics.Process]::GetCurrentProcess()
    function Get-NetTCPConnection { param($State,$ErrorAction) $Rows }
    try {
        $failure = ''; $result = $null
        try { $result = Test-C1OwnedListener -Process $self -Port 14321 } catch { $failure = $_.Exception.Message }
        if ($Expected -ceq 'absent') {
            if ($failure -or $result -ne $false) { throw 'Absent listener oracle differs.' }
        } elseif ($failure -cne $Expected) { throw "Listener refusal differs: $Case" }
        "PASS C1 listener: $Case"
    } finally { $self.Dispose() }
}
Assert-C1ListenerSnapshot 'absent' @() 'absent'
$right = [pscustomobject]@{LocalPort=14321;LocalAddress='127.0.0.1';OwningProcess=$PID}
Assert-C1ListenerSnapshot 'foreign owner' @([pscustomobject]@{LocalPort=14321;LocalAddress='127.0.0.1';OwningProcess=($PID+1)}) 'C1_LISTENER_NOT_OWNED'
Assert-C1ListenerSnapshot 'wildcard' @([pscustomobject]@{LocalPort=14321;LocalAddress='0.0.0.0';OwningProcess=$PID}) 'C1_LISTENER_NOT_OWNED'
Assert-C1ListenerSnapshot 'ambiguous' @($right,$right) 'C1_LISTENER_NOT_OWNED'
'C1_LISTENER_GUARD_OK:1 real loopback listener, 4 fictional snapshots, no MySQL launched'

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

# Pure fictional ZIP oracle; no application, database, network or release action.
$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot 'package-extended-c.ps1'
$tokens = $null
$errors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile($source, [ref]$tokens, [ref]$errors)
if ($errors.Count) { throw 'Packaging script failed to parse.' }
$functions = @($ast.FindAll({
    param($node)
    $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and
        $node.Name -ceq 'Assert-PackagedMigrations'
}, $false))
if ($functions.Count -ne 1) { throw 'Expected exactly one packaging migration guard.' }
. ([scriptblock]::Create($functions[0].Extent.Text))

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

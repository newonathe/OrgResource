# Read-only local package integrity check. Use this script from the trusted
# source checkout, not from an unverified archive. No Java, DB or extraction.
# The expected source SHA and checksum-list SHA256 must be obtained separately
# from the trusted producer; self-consistency alone does not authenticate.
param(
    [Parameter(Mandatory)][string] $PackageRoot,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{40}$')][string] $ExpectedSourceSha,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{64}$')][string] $ExpectedChecksumsSha256
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 3
if ($ExpectedSourceSha -cnotmatch '^[0-9a-f]{40}$' -or
    $ExpectedChecksumsSha256 -cnotmatch '^[0-9a-f]{64}$') { throw 'PACKAGE_EXPECTED_IDENTITY' }

function Require-Keys([System.Text.Json.JsonElement] $Object,[string[]] $Names) {
    if ($Object.ValueKind -ne [System.Text.Json.JsonValueKind]::Object) { throw 'PACKAGE_JSON_SHAPE' }
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($property in $Object.EnumerateObject()) {
        if ($property.Name -cnotin $Names -or -not $seen.Add($property.Name)) { throw 'PACKAGE_JSON_SHAPE' }
    }
    if ($seen.Count -ne $Names.Count) { throw 'PACKAGE_JSON_SHAPE' }
}
function Read-PackageJson([string] $Path) {
    if ([IO.FileInfo]::new($Path).Length -gt 65536) { throw 'PACKAGE_METADATA_SIZE' }
    try { return [System.Text.Json.JsonDocument]::Parse([IO.File]::ReadAllText($Path)) }
    catch { throw 'PACKAGE_JSON_INVALID' }
}
function FileDigest([string] $Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}
$directory = Get-Item -LiteralPath $PackageRoot -Force
if (-not $directory.PSIsContainer -or $directory.PSProvider.Name -cne 'FileSystem') { throw 'PACKAGE_ROOT' }
for ($ancestor = $directory; $null -ne $ancestor; $ancestor = $ancestor.Parent) {
    if ($ancestor.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'PACKAGE_ROOT_REPARSE' }
}
$root = $directory.FullName
$payloads = @('OrgResource-Extended-C6a-Source.zip','OrgResource-Extended-C6a-Java21.jar','VERIFICATION.log')
$hashNames = @($payloads + 'RELEASE-MANIFEST.json')
$expectedNames = @($hashNames + 'SHA256SUMS.txt' + 'READY.json')
$items = @(Get-ChildItem -LiteralPath $root -Force)
if ($items.Count -ne $expectedNames.Count -or
    @(Compare-Object $expectedNames @($items.Name) -CaseSensitive).Count) { throw 'PACKAGE_INVENTORY' }
foreach ($item in $items) {
    if ($item.PSIsContainer -or ($item.Attributes -band [IO.FileAttributes]::ReparsePoint)) { throw 'PACKAGE_FILE_TYPE' }
}
$readyPath = Join-Path $root 'READY.json'
$sumPath = Join-Path $root 'SHA256SUMS.txt'
$manifestPath = Join-Path $root 'RELEASE-MANIFEST.json'
$ready = Read-PackageJson $readyPath
try {
    $value = $ready.RootElement
    Require-Keys $value @('sourceSha','checksumsSha256')
    if ($value.GetProperty('sourceSha').GetString() -cne $ExpectedSourceSha -or
        $value.GetProperty('checksumsSha256').GetString() -cne $ExpectedChecksumsSha256) { throw 'PACKAGE_READY_IDENTITY' }
} finally { $ready.Dispose() }
if ([IO.FileInfo]::new($sumPath).Length -gt 4096) { throw 'PACKAGE_METADATA_SIZE' }
if ((FileDigest $sumPath) -cne $ExpectedChecksumsSha256) { throw 'PACKAGE_CHECKSUM_LIST_HASH' }
$lines = @([IO.File]::ReadAllLines($sumPath))
if ($lines.Count -ne $hashNames.Count) { throw 'PACKAGE_CHECKSUM_LIST_SHAPE' }
$hashes = [Collections.Generic.Dictionary[string,string]]::new([StringComparer]::Ordinal)
foreach ($line in $lines) {
    if ($line -cnotmatch '^([0-9a-f]{64})  ([A-Za-z0-9._-]+)$') { throw 'PACKAGE_CHECKSUM_LIST_SHAPE' }
    $digest = $Matches[1]; $name = $Matches[2]
    if ($name -cnotin $hashNames -or -not $hashes.TryAdd($name,$digest)) { throw 'PACKAGE_CHECKSUM_LIST_SHAPE' }
}
foreach ($name in $hashNames) {
    if ((FileDigest (Join-Path $root $name)) -cne $hashes[$name]) { throw 'PACKAGE_ARTIFACT_HASH' }
}
$manifest = Read-PackageJson $manifestPath
try {
    $value = $manifest.RootElement
    Require-Keys $value @('status','sourceSha','protectedCourseSha','courseTrees','extractedSourceRoot','environment','artifacts')
    if ($value.GetProperty('status').GetString() -cne 'local-release-candidate' -or
        $value.GetProperty('sourceSha').GetString() -cne $ExpectedSourceSha -or
        $value.GetProperty('protectedCourseSha').GetString() -cne '7f8c0746da18fd54b47f430ac1435ac36e605d67') {
        throw 'PACKAGE_MANIFEST_IDENTITY'
    }
    $trees = $value.GetProperty('courseTrees')
    Require-Keys $trees @('backend','frontend')
    foreach ($tree in @('backend','frontend')) {
        if ($trees.GetProperty($tree).GetString() -cnotmatch '^[0-9a-f]{40}$') { throw 'PACKAGE_MANIFEST_IDENTITY' }
    }
    $artifacts = $value.GetProperty('artifacts')
    if ($artifacts.ValueKind -ne [System.Text.Json.JsonValueKind]::Array -or
        $artifacts.GetArrayLength() -ne $payloads.Count) { throw 'PACKAGE_MANIFEST_ARTIFACTS' }
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($artifact in $artifacts.EnumerateArray()) {
        Require-Keys $artifact @('name','bytes','sha256')
        $name = $artifact.GetProperty('name').GetString()
        if ($name -cnotin $payloads -or -not $seen.Add($name)) { throw 'PACKAGE_MANIFEST_ARTIFACTS' }
        $size = $artifact.GetProperty('bytes').GetInt64()
        if ($size -le 0 -or $size -ne [IO.FileInfo]::new((Join-Path $root $name)).Length -or
            $artifact.GetProperty('sha256').GetString() -cne $hashes[$name]) { throw 'PACKAGE_MANIFEST_ARTIFACTS' }
    }
} finally { $manifest.Dispose() }

# Exclusive custody is a precondition; this does not protect against a writer
# racing the check or authenticate a source/digest obtained from the package.
'EXTENDED_PACKAGE_INTEGRITY_OK:files=6 payloads=3 source_and_checksum_pinned=true'

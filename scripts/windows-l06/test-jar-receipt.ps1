#Requires -Version 7.4
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
function Fail([string] $Code) { throw [InvalidOperationException]::new($Code) }
. (Join-Path $PSScriptRoot 'jar_receipt.ps1')
$source = 'a' * 40; $digest = 'b' * 64; $size = 123L
$valid = '{"schema":"orgresource.windows-l03-receipt.v1","sourceSha":"' + $source + '","buildReceiptId":"fictional-1","jarSha256":"' + $digest + '","jarBytes":123}'
$count = 0
function Case([string] $Name, [string] $Json, [string] $Want, [string] $Anchor = '', [string] $FileDigest = $digest, [long] $FileSize = $size) {
    $bytes = [Text.Encoding]::UTF8.GetBytes($Json)
    if (-not $Anchor) { $Anchor = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant() }
    $actual = 'OK'
    try { Assert-L03ReceiptBytes $bytes $Anchor $source $FileDigest $FileSize }
    catch { $actual = $_.Exception.GetBaseException().Message }
    if ($actual -cne $Want) { throw "CASE_FAILED:$Name expected=$Want actual=$actual" }
    $script:count++
    Write-Output "H02_CASE_OK:$Name"
}
$m = 'R06B1_JAR_RECEIPT_MISMATCH'; $r = 'R06B1_JAR_RECEIPT_REQUIRED'
Case 'valid-fictional' $valid 'OK'
Case 'wrong-anchor' $valid $m ('c' * 64)
Case 'invalid-anchor' $valid $r 'wrong'
Case 'wrong-source' ($valid.Replace($source, ('c' * 40))) $m
Case 'wrong-file-digest' $valid $m '' ('d' * 64)
Case 'wrong-file-length' $valid $m '' $digest 124
Case 'missing-field' ($valid.Replace(',"jarBytes":123','')) $m
Case 'extra-field' ($valid.Replace(',"jarBytes":123',',"jarBytes":123,"extra":true')) $m
Case 'duplicate-field' ($valid.Replace(',"jarBytes":123',',"jarBytes":123,"jarBytes":123')) $m
Case 'case-duplicate' ($valid.Replace(',"jarBytes":123',',"jarBytes":123,"JarBytes":123')) $m
Case 'wrong-case-key' ($valid.Replace('"jarBytes"','"JarBytes"')) $m
Case 'uppercase-digest' ($valid.Replace($digest,$digest.ToUpperInvariant())) $m
Case 'wrong-id-type' ($valid.Replace('"fictional-1"','1')) $m
Case 'invalid-id' ($valid.Replace('fictional-1','../bad')) $m
Case 'wrong-size-type' ($valid.Replace(':123',':"123"')) $m
Case 'fractional-size' ($valid.Replace(':123',':123.0')) $m
Case 'exponent-size' ($valid.Replace(':123',':123e0')) $m
Case 'overflow-size' ($valid.Replace(':123',':9999999999999999999')) $m
Case 'negative-size' ($valid.Replace(':123',':-123')) $m
Case 'malformed' '{' $m
Case 'trailing-root' ($valid+'{}') $m
Case 'array' ('['+$valid+']') $m
Case 'empty' '' $r
Case 'oversized' ('x' * 4097) $r
if ($count -ne 24) { throw 'H02_CASE_COUNT' }
Write-Output "H02_PURE_OK:$count"

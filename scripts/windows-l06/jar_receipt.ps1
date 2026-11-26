# H02: no native side effects; the owner-controlled acceptance marker is separate from the receipt.
# The receipt is bounded, strict UTF-8 JSON with exactly five fields.
function Assert-L03ReceiptBytes([byte[]] $Bytes, [string] $ExpectedReceiptSha, [string] $Source, [string] $JarDigest, [long] $JarLength) {
    if (-not $Bytes -or $Bytes.Length -gt 4096 -or $ExpectedReceiptSha -cnotmatch '^[a-f0-9]{64}$') { Fail 'R06B1_JAR_RECEIPT_REQUIRED' }
    $actual = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($Bytes)).ToLowerInvariant()
    if ($actual -cne $ExpectedReceiptSha) { Fail 'R06B1_JAR_RECEIPT_MISMATCH' }
    $doc = $null
    try {
        $json = [Text.UTF8Encoding]::new($false, $true).GetString($Bytes)
        $doc = [System.Text.Json.JsonDocument]::Parse($json)
        if ($doc.RootElement.ValueKind -ne [System.Text.Json.JsonValueKind]::Object) { throw 'shape' }
        $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
        $fields = [Collections.Generic.Dictionary[string,System.Text.Json.JsonElement]]::new([StringComparer]::Ordinal)
        foreach ($prop in $doc.RootElement.EnumerateObject()) {
            if (-not $seen.Add($prop.Name) -or $prop.Name -cnotin @('schema','sourceSha','buildReceiptId','jarSha256','jarBytes')) { throw 'shape' }
            $fields.Add($prop.Name, $prop.Value)
        }
        if ($fields.Count -ne 5) { throw 'shape' }
        foreach ($key in 'schema','sourceSha','buildReceiptId','jarSha256') {
            if ($fields[$key].ValueKind -ne [System.Text.Json.JsonValueKind]::String) { throw 'shape' }
        }
        if ($fields['schema'].GetString() -cne 'orgresource.windows-l03-receipt.v1' -or
            $fields['buildReceiptId'].GetString() -cnotmatch '^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$' -or
            $fields['sourceSha'].GetString() -cnotmatch '^[a-f0-9]{40}$' -or
            $fields['jarSha256'].GetString() -cnotmatch '^[a-f0-9]{64}$' -or
            $fields['jarBytes'].ValueKind -ne [System.Text.Json.JsonValueKind]::Number -or
            $fields['jarBytes'].GetRawText() -cnotmatch '^[1-9][0-9]{0,18}$') { throw 'shape' }
        if ($fields['sourceSha'].GetString() -cne $Source -or
            $fields['jarSha256'].GetString() -cne $JarDigest -or
            $fields['jarBytes'].GetInt64() -ne $JarLength) { throw 'binding' }
    } catch { Fail 'R06B1_JAR_RECEIPT_MISMATCH' }
    finally { if ($null -ne $doc) { $doc.Dispose() } }
}
function Assert-L03Accepted([string] $RootPath, [string] $Source, [string] $JarDigest, [long] $JarLength) {
    $anchor = Join-Path $RootPath 'WINDOWS_L03_RECEIPT_ACCEPTED.txt'
    $receipt = Join-Path $RootPath 'WINDOWS_L03_RECEIPT.json'
    foreach ($path in $anchor, $receipt) {
        if (-not (Test-PlainPath $path 'Leaf') -or -not (Test-OwnerOnly $path) -or (Get-Item -LiteralPath $path).Length -gt 4096) { Fail 'R06B1_JAR_RECEIPT_REQUIRED' }
    }
    # One bounded read per file, with write sharing denied. No caller-supplied anchor.
    $anchorBytes = Read-L03BoundedFile $anchor
    try { $marker = [Text.UTF8Encoding]::new($false, $true).GetString($anchorBytes).TrimEnd([char]13, [char]10) }
    catch { Fail 'R06B1_JAR_RECEIPT_MISMATCH' }
    if ($marker -cnotmatch '^ACCEPTED ([a-f0-9]{40}) ([a-f0-9]{64})$' -or $Matches[1] -cne $Source) { Fail 'R06B1_JAR_RECEIPT_MISMATCH' }
    $expected = $Matches[2]
    Assert-L03ReceiptBytes (Read-L03BoundedFile $receipt) $expected $Source $JarDigest $JarLength
}
function Read-L03BoundedFile([string] $Path) {
    $stream = [IO.File]::Open($Path, 'Open', 'Read', 'Read')
    try {
        if ($stream.Length -lt 1 -or $stream.Length -gt 4096) { Fail 'R06B1_JAR_RECEIPT_REQUIRED' }
        $bytes = [byte[]]::new([int]$stream.Length)
        $stream.ReadExactly($bytes)
        return ,$bytes
    } finally { $stream.Dispose() }
}

# Bounded tool identity and scoped runtime copy checks. No claim of a full trusted dependency chain.
function Get-C9ToolIdentity([System.Collections.IDictionary] $Tools) {
    $records = foreach ($name in $Tools.Keys | Sort-Object -CaseSensitive) {
        $path = $Tools[$name]
        if (-not (Test-PlainPath $path 'Leaf')) { Fail 'R06B1_TOOL_MISSING' }
        $length = (Get-Item -LiteralPath $path).Length
        "$name $length $(Get-Sha $path)"
    }
    $bytes = [Text.Encoding]::UTF8.GetBytes(($records -join "`n"))
    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
}
function Copy-C9VerifiedTree([string] $Label, [string] $Mode, [string] $From, [string] $To) {
    $receipt = @(& $Python -I -B (Join-Path $PSScriptRoot 'runtime_copy.py') $Mode $From $To 2>$null)
    if ($LASTEXITCODE -ne 0 -or $receipt.Count -ne 1 -or $receipt[0] -cnotmatch '^entries=[1-9][0-9]* sha256=[a-f0-9]{64}$') { Fail 'R06B1_RUNTIME_COPY' }
    Write-Marker "R06B1_RUNTIME_COPY_OK_WIN:$Label $($receipt[0])"
}

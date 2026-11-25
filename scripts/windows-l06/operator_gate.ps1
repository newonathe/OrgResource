# Windows port of c9-local-operator-gate.sh. No switch, environment token or redirected input can answer it.
function Get-C9Challenge([string] $Fingerprint, [string] $Digest) {
    if ($Fingerprint -cnotmatch '^[0-9a-f]{64}$' -or $Digest -cnotmatch '^[0-9a-f]{64}$') { throw 'R06B2_OPERATOR_ARGUMENTS' }
    return "I VERIFIED ISOLATED RESTORE $Fingerprint $Digest"
}
function Test-C9Response([string] $Fingerprint, [string] $Digest, [string] $Reply) {
    try { $expected = Get-C9Challenge $Fingerprint $Digest } catch { return $false }
    return [string]::Equals($Reply, $expected, [StringComparison]::Ordinal)
}
function Assert-C9OwnerTerminal {
    if ($env:GITHUB_ACTIONS -ceq 'true') { throw 'R06B2_OPERATOR_CI_REFUSED' }
    foreach ($name in 'CLAUDECODE', 'CLAUDE_CODE_ENTRYPOINT', 'CLAUDE_CODE_SESSION_ID', 'AI_AGENT', 'CODEX_SANDBOX', 'CI') {
        if ([Environment]::GetEnvironmentVariable($name)) { throw 'R06B2_AGENT_SESSION_REFUSED' }
    }
    if ([Console]::IsInputRedirected -or [Console]::IsOutputRedirected -or [Console]::IsErrorRedirected) { throw 'R06B2_OWNER_TERMINAL_REQUIRED' }
}
function Read-C9Reply([int] $Seconds) {
    while ([Console]::KeyAvailable) { $null = [Console]::ReadKey($true) } # typed-ahead input never counts
    $deadline = [DateTime]::UtcNow.AddSeconds($Seconds); $reply = [Text.StringBuilder]::new()
    while ([DateTime]::UtcNow -lt $deadline) {
        if (-not [Console]::KeyAvailable) { Start-Sleep -Milliseconds 25; continue }
        $key = [Console]::ReadKey($true)
        if ($key.Key -eq 'Enter') { [Console]::Error.WriteLine(); return $reply.ToString() }
        if ($key.Key -eq 'Backspace') { if ($reply.Length) { $null = $reply.Remove($reply.Length - 1, 1); [Console]::Error.Write("`b `b") } }
        elseif ($key.KeyChar -ge ' ' -and $reply.Length -lt 400) { $null = $reply.Append($key.KeyChar); [Console]::Error.Write($key.KeyChar) }
    }
    [Console]::Error.WriteLine("`nR06B2_OPERATOR_EXPIRED")
    return $null
}
function Confirm-C9Operator([string] $Fingerprint, [string] $Digest, [int] $Seconds = 300) {
    if ($Seconds -lt 1 -or $Seconds -gt 300) { throw 'R06B2_OPERATOR_ARGUMENTS' }
    Assert-C9OwnerTerminal
    $challenge = Get-C9Challenge $Fingerprint $Digest
    [Console]::Error.WriteLine("`nOWNER ONLY: review the restoration receipt above. To authorize this disposable import, enter the exact line below within $Seconds seconds. Do not automate this response. Decline or close the terminal to stop.")
    [Console]::Error.Write("$challenge`n> ")
    $reply = Read-C9Reply $Seconds
    if ($null -eq $reply) { return $false }
    return (Test-C9Response $Fingerprint $Digest $reply)
}

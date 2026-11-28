# Proposed test-first H04 regression driver. NOT an accepted harness or native receipt.
# Run with PowerShell 7.4+, no DB/network/AppContainer, only trusted reviewed common.ps1.
# Pass expected SHA256 obtained from the exact reviewed checkout; do not execute an untrusted file.
# Baseline 72de0c10 should fail the missing/extra/unknown-result assertions.
# A corrected candidate must pass, then also pass the existing native matrix unchanged.
#Requires -Version 7.4
param(
    [Parameter(Mandatory)][string] $Common,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-fA-F]{64}$')][string] $ExpectedSha256
)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $Common -PathType Leaf) -or
    (Get-FileHash -LiteralPath $Common -Algorithm SHA256).Hash -ine $ExpectedSha256) {
    throw 'H04_SOURCE_IDENTITY'
}
$tokens = $null; $errors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile($Common, [ref]$tokens, [ref]$errors)
if (@($errors).Count) { throw 'H04_SOURCE_PARSE' }
$defs = @($ast.FindAll({ param($n)
    $n -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $n.Name -ceq 'Assert-ProbeResult'
}, $true))
if ($defs.Count -ne 1) { throw 'H04_FUNCTION_IDENTITY' }
function Fail([string] $Code) { throw [InvalidOperationException]::new($Code) }
# Evaluate only the pinned function definition, not the complete common script.
. ([scriptblock]::Create($defs[0].Extent.Text))
$hasSpec = (Get-Command Assert-ProbeResult -CommandType Function).Parameters.ContainsKey('Spec')
function Evaluate([hashtable] $Seen, [hashtable] $Spec) {
    if ($hasSpec) { Assert-ProbeResult -Seen $Seen -Spec $Spec }
    else { Assert-ProbeResult $Seen }
}
function Copy-Seen([hashtable] $From) {
    $out = [hashtable]::new([StringComparer]::Ordinal)
    foreach ($k in $From.Keys) { $out.Add($k, $From[$k]) }
    return $out
}
$script:cases = 0; $script:failed = 0
function Check([string] $Id, [hashtable] $Seen, [hashtable] $Spec, [string] $Expected = '') {
    $script:cases++
    $actual = ''
    try { Evaluate $Seen $Spec } catch { $actual = $_.Exception.Message }
    if ($actual -cne $Expected) {
        $script:failed++
        # Do not print arbitrary exception text. Report only fixed case ID and disposition.
        $kind = if (-not $actual) { 'UNEXPECTED_ACCEPT' } else { 'UNEXPECTED_REFUSAL' }
        Write-Output "H04_CASE_FAILED:$Id $kind"
    } else { Write-Output "H04_CASE_OK:$Id" }
}
$spec = @{
    tcpTargets = @{ ownListener = @{}; hostLoopback = @{}; 'role mixed' = @{}; lan = @{} }
    udpTargets = @(@{ name = 'loopback' }, @{ name = 'lan' }, @{ name = 'gateway' })
    paths = @('fixture-other', 'fixture-dumps'); writePaths = @('fixture-runtime')
    dnsServer = 'fixture-resolver'
}
# Addresses/ports are deliberately absent: this test exercises result keys and enum semantics only.
$sound = Copy-Seen @{
    self = 'CONNECTED'; ownListener = 'CONNECTED'; external4 = 'EACCES'; external6 = 'ENETUNREACH'
    hostLoopback = 'ETIMEDOUT'; lan = 'TIMEOUT'; 'role mixed' = 'ETIMEDOUT'
    'udp loopback' = 'NO_REPLY'; 'udp lan' = 'NO_REPLY'; 'udp gateway' = 'NO_REPLY'
    dnsDirect = 'ECONNREFUSED'; dnsSystem = 'ENOTFOUND'
    'path fixture-other' = 'EPERM'; 'path fixture-dumps' = 'EACCES'; 'write fixture-runtime' = 'EPERM'
}
$shape = 'R06B1_NETWORK_PROBE_SHAPE'
Check 'complete' $sound $spec
# Host-dependent optional probes must be required exactly when in this invocation spec.
$minimalSpec = @{
    tcpTargets = @{ ownListener = @{}; hostLoopback = @{}; 'role mixed' = @{} }
    udpTargets = @(@{ name = 'loopback' }); paths = $spec.paths; writePaths = $spec.writePaths; dnsServer = ''
}
$minimal = Copy-Seen $sound
foreach ($k in 'lan', 'udp lan', 'udp gateway') { $minimal.Remove($k) }
Check 'no-lan-no-gateway' $minimal $minimalSpec
foreach ($k in 'hostLoopback', 'lan', 'role mixed', 'udp loopback', 'udp lan', 'udp gateway',
               'path fixture-other', 'path fixture-dumps', 'write fixture-runtime') {
    $bad = Copy-Seen $sound; $bad.Remove($k)
    Check ('missing-' + $k.Replace(' ', '-')) $bad $spec $shape
}
$bad = Copy-Seen $sound; $bad.Add('unknown-result', 'EACCES')
Check 'extra-key' $bad $spec $shape
$bad = Copy-Seen $sound; $bad.Remove('role mixed'); $bad.Add('Role mixed', 'ETIMEDOUT')
Check 'wrong-case-key' $bad $spec $shape
$bad = Copy-Seen $sound; $bad.Add('Role mixed', 'ETIMEDOUT')
Check 'case-duplicate-key' $bad $spec $shape
foreach ($k in 'role mixed', 'udp loopback', 'dnsDirect', 'dnsSystem') {
    $bad = Copy-Seen $sound; $bad[$k] = 'NOT_A_RECOGNIZED_RESULT'
    Check ('unknown-enum-' + $k.Replace(' ', '-')) $bad $spec $shape
}
$bad = Copy-Seen $sound; $bad['role mixed'] = $null
Check 'null-result' $bad $spec $shape
$bad = Copy-Seen $sound; $bad['udp loopback'] = 42
Check 'nonstring-result' $bad $spec $shape
# Preserve the established refusal codes, not just a generic error.
foreach ($case in @(
    @('self','TIMEOUT','R06B1_LOOPBACK_UNAVAILABLE'),
    @('ownListener','ECONNREFUSED','R06B1_LOOPBACK_UNAVAILABLE'),
    @('external4','CONNECTED','R06B1_EXTERNAL_REACHABLE'),
    @('external6','CONNECTED','R06B1_EXTERNAL_REACHABLE'),
    @('hostLoopback','CONNECTED','R06B1_EXTERNAL_REACHABLE'),
    @('role mixed','CONNECTED','R06B1_EXTERNAL_REACHABLE'),
    @('udp loopback','REPLY','R06B1_EXTERNAL_REACHABLE'),
    @('dnsDirect','ENOTFOUND','R06B1_EXTERNAL_REACHABLE'),
    @('dnsSystem','RESOLVED','R06B1_EXTERNAL_REACHABLE'),
    @('path fixture-other','READ','R06B1_CROSS_ROLE_FILES'),
    @('write fixture-runtime','WRITTEN','R06B1_RUNTIME_WRITABLE')
)) {
    $bad = Copy-Seen $sound; $bad[$case[0]] = $case[1]
    Check ('unsafe-' + $case[0].Replace(' ', '-')) $bad $spec $case[2]
}
# Exact duplicate JSON keys are lost by some parsers: test raw JSON separately in Invoke-Probe.
# This dictionary test is NOT a parser, network, filesystem, listener ownership or native isolation receipt.
if ($script:cases -ne 31) { throw "H04_CASE_INVENTORY:$script:cases" }
Write-Output "H04_CASE_SUMMARY:cases=$script:cases failed=$script:failed spec_parameter=$hasSpec"
if ($script:failed) { exit 1 }

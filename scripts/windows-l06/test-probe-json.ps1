#Requires -Version 7.4
# Pure fictional result parser check; no native containment or private input.
param([Parameter(Mandatory)][string] $Common,[Parameter(Mandatory)][ValidatePattern('^[0-9a-fA-F]{64}$')][string] $ExpectedSha256)
Set-StrictMode -Version 3
$ErrorActionPreference = 'Stop'
if ((Get-FileHash -LiteralPath $Common -Algorithm SHA256).Hash -ine $ExpectedSha256) { throw 'H04_SOURCE_IDENTITY' }
$tokens = $null; $errors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile($Common,[ref]$tokens,[ref]$errors)
if (@($errors).Count) { throw 'H04_SOURCE_PARSE' }
$defs = @($ast.FindAll({param($n) $n -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $n.Name -ceq 'Convert-ProbeJson'},$true))
if ($defs.Count -ne 1) { throw 'H04_PARSER_IDENTITY' }
function Fail([string] $Code) { throw [InvalidOperationException]::new($Code) }
. ([scriptblock]::Create($defs[0].Extent.Text))
$ok = Convert-ProbeJson '{"self":"CONNECTED","external4":"EACCES"}'
if ($ok.Count -ne 2 -or $ok.self -cne 'CONNECTED' -or $ok.external4 -cne 'EACCES') { throw 'H04_PARSER_POSITIVE' }
$bad = @('{"self":"CONNECTED","self":"EACCES"}','{"self":"CONNECTED","Self":"EACCES"}','{"self":null}','{"self":4}','{"self":{}}','[]','{"self":','{"self":"CONNECTED"} trailing')
foreach ($json in $bad) {
    $actual = ''
    try { $null = Convert-ProbeJson $json } catch { $actual = $_.Exception.GetBaseException().Message }
    if ($actual -cne 'R06B1_NETWORK_PROBE_SHAPE') { throw 'H04_PARSER_UNEXPECTED_RESULT' }
}
'H04_PARSER_SUMMARY:cases=9 failed=0'

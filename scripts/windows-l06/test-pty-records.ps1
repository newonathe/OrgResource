#Requires -Version 7.4
# Pure record tests only; no C9ControlLock instance, native call or file mutation.
Set-StrictMode -Version 3
$ErrorActionPreference='Stop'
function Check([bool] $ok,[string] $label) { if(-not $ok) { throw "C9W_PTY_RECORD_TEST:$label" } }
function Reject([scriptblock] $body,[string] $label) {
    $caught=$false
    try { $null=& $body } catch {
        if ($_.Exception.GetBaseException().Message -cne 'C9W_PTY_RECORD') { throw }
        $caught=$true
    }
    Check $caught $label
}
$r='a'*16+':'+('1'*32); $p='a'*16+':'+('2'*32); $c='a'*16+':'+('3'*32)
$t='1'*12; $u='2'*12; $pkg='b'*64; $argv='c'*64
$parent=[C9PtyRecords]::Parent($r,$p,$t,$u,'normal',$pkg,$argv)
$child=[C9PtyRecords]::Child($parent,$c)
$result=[C9PtyRecords]::ChildResult($parent,$child,$true)
$outer=[C9PtyRecords]::Outer($parent,$false,$true,0,0,2,0)
Check (([C9PtyRecords]::ReadParent($parent))[3] -ceq $u) 'recoverable child token'
Check ([C9PtyRecords]::ReadChild($parent,$child) -ceq $c) 'child identity'
Check ([C9PtyRecords]::ReadChildResult($parent,$child,$result)) 'positive child result'
[C9PtyRecords]::VerifyOuter($parent,$outer)
Check ([Text.Encoding]::ASCII.GetString([C9PtyRecords]::CrossCheck($parent,$child,$result,$outer)).StartsWith("C9W_PTY_CROSSCHECK_OBSERVED_V1`n")) 'positive crosscheck'
$refused=[C9PtyRecords]::ChildResult($parent,$child,$false)
$failed=[C9PtyRecords]::Outer($parent,$false,$true,74,0,1,0)
Check (-not [C9PtyRecords]::ReadChildResult($parent,$child,$refused)) 'explicit refusal'
Check (([C9PtyRecords]::CrossCheck($parent,$child,$refused,$failed)).Length -gt 0) 'refusal observation'
$timed=[C9PtyRecords]::Outer($parent,$true,$false,139,2,2,0)
Check (([C9PtyRecords]::CrossCheck($parent,$child,$result,$timed)).Length -gt 0) 'timeout after result is observation only'
Reject { [C9PtyRecords]::Parent($r,$p,$t,$t,'normal',$pkg,$argv) } 'same token'
Reject { [C9PtyRecords]::Parent($r,$r,$t,$u,'normal',$pkg,$argv) } 'same parent identity'
Reject { [C9PtyRecords]::Parent($r,('d'*16+':'+('2'*32)),$t,$u,'normal',$pkg,$argv) } 'different volume'
Reject { [C9PtyRecords]::Parent($r,$p,$t,$u,'wrong',$pkg,$argv) } 'unknown mode'
Reject { [C9PtyRecords]::Parent($r,$p,$t,$u,'normal',('B'*64),$argv) } 'uppercase digest'
Reject { [C9PtyRecords]::Child($parent,$p) } 'parent reused as child'
Reject { [C9PtyRecords]::Child($parent,$r) } 'root reused as child'
$other=[C9PtyRecords]::Parent($r,$p,$t,('3'*12),'normal',$pkg,$argv)
Reject { [C9PtyRecords]::ReadChild($other,$child) } 'foreign parent'
$otherChild=[C9PtyRecords]::Child($parent,('a'*16+':'+('4'*32)))
Reject { [C9PtyRecords]::ReadChildResult($parent,$otherChild,$result) } 'foreign child result'
$negative=[C9PtyRecords]::Parent($r,$p,$t,$u,'hang',$pkg,$argv)
$negativeChild=[C9PtyRecords]::Child($negative,$c)
Reject { [C9PtyRecords]::ChildResult($negative,$negativeChild,$true) } 'impossible success mode'
Reject { [C9PtyRecords]::Outer($parent,$false,$false,0,0,2,0) } 'root not exited'
Reject { [C9PtyRecords]::Outer($parent,$true,$false,139,2,2,1) } 'active remainder'
Reject { [C9PtyRecords]::Outer($parent,$true,$false,139,3,2,0) } 'invalid accounting'
Reject { [C9PtyRecords]::CrossCheck($parent,$child,$refused,$outer) } 'exit zero contradicts refusal'
Reject { [C9PtyRecords]::CrossCheck($parent,$child,$result,$failed) } 'nonzero contradicts success'
Reject { [C9PtyRecords]::VerifyOuter($other,$outer) } 'foreign outer'
$bad=[Text.Encoding]::ASCII.GetBytes(([Text.Encoding]::ASCII.GetString($parent)).Replace([char]10,[char]13))
Reject { [C9PtyRecords]::ReadParent($bad) } 'noncanonical newline'
Reject { [C9PtyRecords]::ReadParent([byte[]]($parent+[byte]10)) } 'trailing field'
Reject { [C9PtyRecords]::ReadParent([byte[]]($parent[0..($parent.Length-2)])) } 'truncation'
Reject { [C9PtyRecords]::ReadParent([byte[]](@(255)+$parent)) } 'nonascii'
$outerLines=[Text.Encoding]::ASCII.GetString($outer).Split([char]10)
$outerLines[4]='+0'
$badOuter=[Text.Encoding]::ASCII.GetBytes(($outerLines -join [char]10))
Reject { [C9PtyRecords]::VerifyOuter($parent,$badOuter) } 'noncanonical number'
$one=[C9PtyRecords]::Outer($parent,$false,$true,0,0,1,0)
Reject { [C9PtyRecords]::CrossCheck($parent,$child,$result,$one) } 'missing client accounting'
Reject { [C9PtyRecords]::Outer($parent,$true,$false,139,2,9,0) } 'outer job limit'
'C9W_H06_PTY_RECORDS_OK:positive=8 negative=23 native_effects_not_run=true'

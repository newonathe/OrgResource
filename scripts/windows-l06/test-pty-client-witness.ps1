# Pure data-only witness frame checks. No native methods or fixture launch.
$ErrorActionPreference = 'Stop'
$pos=0; $neg=0
function Refuse([scriptblock]$action) {
    $refused=$false
    try { & $action | Out-Null } catch {
        if($_.Exception.ToString() -notmatch 'C9W_PTY_WITNESS_FRAME') { throw }
        $refused=$true
    }
    if(-not $refused) { throw 'Expected witness frame refusal' }
    $script:neg++
}
[uint32]$pidOut=0
$b=[C9OwnedJobProbe]::EncodePtyClientWitness(4660,42)
if($b.Length -ne 16 -or ([Convert]::ToHexString($b)) -cne '433957312A0000003412000000000000') { throw 'Wire bytes' }
$pos++
$h=[C9OwnedJobProbe]::DecodePtyClientWitness($b,41,[ref]$pidOut)
if($h -ne 4660 -or $pidOut -ne 42) { throw 'Round trip' }
$pos++
$max=[C9OwnedJobProbe]::EncodePtyClientWitness([long]::MaxValue,[uint32]::MaxValue)
if([C9OwnedJobProbe]::DecodePtyClientWitness($max,1,[ref]$pidOut) -ne [long]::MaxValue -or $pidOut -ne [uint32]::MaxValue) { throw 'Upper bounds' }
$pos++
Refuse { [C9OwnedJobProbe]::EncodePtyClientWitness(0,42) }
Refuse { [C9OwnedJobProbe]::EncodePtyClientWitness(-1,42) }
Refuse { [C9OwnedJobProbe]::EncodePtyClientWitness(1,0) }
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness($null,41,[ref]$pidOut) }
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness([byte[]]::new(15),41,[ref]$pidOut) }
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness([byte[]]::new(17),41,[ref]$pidOut) }
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness($b,0,[ref]$pidOut) }
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness($b,42,[ref]$pidOut) }
$wrong=[byte[]]$b.Clone(); $wrong[0]=0
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness($wrong,41,[ref]$pidOut) }
$wrong=[byte[]]$b.Clone(); [Array]::Clear($wrong,4,4)
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness($wrong,41,[ref]$pidOut) }
$wrong=[byte[]]$b.Clone(); [Array]::Clear($wrong,8,8)
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness($wrong,41,[ref]$pidOut) }
$wrong=[byte[]]$b.Clone(); $wrong[15]=128
Refuse { [C9OwnedJobProbe]::DecodePtyClientWitness($wrong,41,[ref]$pidOut) }
if($pos -ne 3 -or $neg -ne 12) { throw 'Wrong case counts' }
"C9W_H06_PTY_CLIENT_WIRE_OK:positive=$pos negative=$neg native_effects_not_run=true"

# Runs inside the container. A child that asks to leave the Job Object must be refused; an ordinary grandchild must stay contained.
Add-Type -Namespace B -Name K -MemberDefinition @'
[StructLayout(LayoutKind.Sequential)] public struct SI { public int cb; public IntPtr a,b,c; public int d,e,f,g,h,i,j,k; public short l,m; public IntPtr n,o,p,q; }
[StructLayout(LayoutKind.Sequential)] public struct PI { public IntPtr h,t; public int p,q; }
[DllImport("kernel32",SetLastError=true,CharSet=CharSet.Unicode)] public static extern bool CreateProcessW(string a,System.Text.StringBuilder c,IntPtr x,IntPtr y,bool i,uint f,IntPtr e,string d,ref SI s,out PI p);
public static string Run(string exe, string cmd, uint flags) { var s = new SI(); s.cb = Marshal.SizeOf(s); PI p; bool ok = CreateProcessW(exe, new System.Text.StringBuilder(cmd), IntPtr.Zero, IntPtr.Zero, false, flags | 0x08000000u, IntPtr.Zero, null, ref s, out p); return ok + "/" + (ok ? 0 : Marshal.GetLastWin32Error()); }
'@
$exe = Join-Path (Join-Path $env:ProgramFiles 'PowerShell') (Join-Path '7' 'pwsh.exe')
$line = '"{0}" -NoProfile -NonInteractive -Command "Start-Sleep 120"' -f $exe
'BREAKAWAY=' + [B.K]::Run($exe, $line, 0x01000000)
'ORDINARY=' + [B.K]::Run($exe, $line, 0)
Start-Sleep 120

using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using Microsoft.Win32.SafeHandles;

// Verification-only Windows containment: zero-capability AppContainer + kill-on-close Job Object.
// Every child is created suspended, checked by HANDLE (never by PID), then resumed.
public sealed class C9Process : IDisposable {
    internal IntPtr H; public int Pid; public string Role;
    public bool HasExited { get { return N.WaitForSingleObject(H, 0) == 0; } }
    public bool Wait(int ms) { return N.WaitForSingleObject(H, ms < 0 ? 0xFFFFFFFF : (uint)ms) == 0; }
    public int ExitCode { get { uint c; if (!N.GetExitCodeProcess(H, out c)) throw new Win32Exception(); return unchecked((int)c); } }
    public void Terminate() { if (!HasExited) { N.TerminateProcess(H, 137); Wait(10000); } }
    public void Dispose() { if (H != IntPtr.Zero) { N.CloseHandle(H); H = IntPtr.Zero; } }
}
public sealed class C9Container : IDisposable {
    public readonly string Name, SidString; IntPtr sid, job; bool profile;
    // Legacy entrypoint is deliberately refused while journal integration is incomplete.
    public C9Container(string name) { throw new InvalidOperationException("C9W_PROFILE_JOURNAL_REQUIRED"); }
    internal C9Container(string name, Action<string> confirmCreated) {
        if (confirmCreated == null) throw new InvalidOperationException("C9W_PROFILE_JOURNAL_REQUIRED");
        if (!System.Text.RegularExpressions.Regex.IsMatch(name, "^orgresource\\.c9\\.[a-f0-9]{12}$")) throw new ArgumentException("C9W_CONTAINER_NAME");
        Name = name;
        int hr = N.CreateAppContainerProfile(name, name, "OrgResource verification container", IntPtr.Zero, 0, out sid);
        if (hr != 0) throw new InvalidOperationException("C9W_PROFILE_CREATE 0x" + hr.ToString("X8"));
        profile = true;
        try {
        IntPtr s; if (!N.ConvertSidToStringSidW(sid, out s)) throw new Win32Exception();
        try { SidString = Marshal.PtrToStringUni(s); } finally { N.LocalFree(s); }
        // Persist exact SID before job creation; failure leaves intent/created for reconciliation.
        confirmCreated(SidString);
        job = N.CreateJobObjectW(IntPtr.Zero, null); if (job == IntPtr.Zero) throw new Win32Exception();
        var i = new N.JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
        i.Basic.LimitFlags = 0x2000 | 0x400 | 0x8; // KILL_ON_JOB_CLOSE | DIE_ON_UNHANDLED_EXCEPTION | ACTIVE_PROCESS; no BREAKAWAY flags
        i.Basic.ActiveProcessLimit = 96;
        int n = Marshal.SizeOf(i); IntPtr p = Marshal.AllocHGlobal(n); Marshal.StructureToPtr(i, p, false);
        bool set = N.SetInformationJobObject(job, 9, p, n); Marshal.FreeHGlobal(p); if (!set) throw new Win32Exception();
        } catch { CloseOwnedHandles(); throw; }
    }
    public int ActiveProcesses { get {
        int n = Marshal.SizeOf(typeof(N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION)); IntPtr p = Marshal.AllocHGlobal(n);
        try { if (!N.QueryInformationJobObject(job, 1, p, n, IntPtr.Zero)) throw new Win32Exception();
            return (int)((N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION)Marshal.PtrToStructure(p, typeof(N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION))).ActiveProcesses; }
        finally { Marshal.FreeHGlobal(p); } } }
    static string Quote(string a) {
        if (a.Length > 0 && a.IndexOfAny(new[]{' ','\t','"'}) < 0) return a;
        var b = new StringBuilder("\""); int bs = 0;
        foreach (char c in a) { if (c=='\\') { bs++; continue; } if (c=='"') { b.Append('\\', bs*2+1); b.Append('"'); bs=0; continue; } b.Append('\\', bs); bs=0; b.Append(c); }
        b.Append('\\', bs*2); b.Append('"'); return b.ToString();
    }
    static IntPtr OpenInherit(string path, bool write) {
        var sa = new N.SECURITY_ATTRIBUTES(); sa.nLength = Marshal.SizeOf(sa); sa.bInheritHandle = 1;
        IntPtr h = N.CreateFileW(path, write ? 0x40000000u : 0x80000000u, 1u | (write?0u:0u), ref sa, write ? 2u : 3u, 0x80, IntPtr.Zero);
        if (h == new IntPtr(-1)) throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_OPEN " + Path.GetFileName(path));
        return h;
    }
    public C9Process Start(string role, string exe, string[] args, IDictionary env, string cwd, string stdin, string stdout, string stderr) { return Create(role, exe, args, env, cwd, stdin, stdout, stderr, sid, false, true); }
    // Only Start resumes a process. SelfTest passes resume=false, so no test shape ever executes an instruction.
    C9Process Create(string role, string exe, string[] args, IDictionary env, string cwd, string stdin, string stdout, string stderr, IntPtr useSid, bool capability, bool resume) {
        IntPtr hin = IntPtr.Zero, ho = IntPtr.Zero, he = IntPtr.Zero, capSid = IntPtr.Zero, capArray = IntPtr.Zero;
        IntPtr list = IntPtr.Zero, caps = IntPtr.Zero, hl = IntPtr.Zero, jl = IntPtr.Zero, envp = IntPtr.Zero;
        try {
            hin = OpenInherit(String.IsNullOrEmpty(stdin) ? "NUL" : stdin, false); ho = OpenInherit(String.IsNullOrEmpty(stdout) ? "NUL" : stdout, true); he = stderr == stdout ? ho : OpenInherit(String.IsNullOrEmpty(stderr) ? "NUL" : stderr, true);
            IntPtr size = IntPtr.Zero; N.InitializeProcThreadAttributeList(IntPtr.Zero, 3, 0, ref size);
            list = Marshal.AllocHGlobal(size); if (!N.InitializeProcThreadAttributeList(list, 3, 0, ref size)) throw new Win32Exception();
            var sc = new N.SECURITY_CAPABILITIES(); sc.AppContainerSid = useSid; sc.Capabilities = IntPtr.Zero; sc.CapabilityCount = 0;
            if (capability) { if (!N.ConvertStringSidToSidW("S-1-15-3-1", out capSid)) throw new Win32Exception(); capArray = Marshal.AllocHGlobal(16); Marshal.WriteIntPtr(capArray, capSid); Marshal.WriteInt32(capArray, 8, 4); sc.Capabilities = capArray; sc.CapabilityCount = 1; }
            caps = Marshal.AllocHGlobal(Marshal.SizeOf(sc)); Marshal.StructureToPtr(sc, caps, false);
            if (useSid != IntPtr.Zero && !N.UpdateProcThreadAttribute(list, 0, (IntPtr)0x20009, caps, (IntPtr)Marshal.SizeOf(sc), IntPtr.Zero, IntPtr.Zero)) throw new Win32Exception();
            var hs = new List<IntPtr>(); foreach (var h in new[]{hin,ho,he}) if (!hs.Contains(h)) hs.Add(h);
            hl = Marshal.AllocHGlobal(IntPtr.Size*hs.Count); for (int k=0;k<hs.Count;k++) Marshal.WriteIntPtr(hl, k*IntPtr.Size, hs[k]);
            if (!N.UpdateProcThreadAttribute(list, 0, (IntPtr)0x20002, hl, (IntPtr)(IntPtr.Size*hs.Count), IntPtr.Zero, IntPtr.Zero)) throw new Win32Exception();
            jl = Marshal.AllocHGlobal(IntPtr.Size); Marshal.WriteIntPtr(jl, job);
            if (!N.UpdateProcThreadAttribute(list, 0, (IntPtr)0x2000D, jl, (IntPtr)IntPtr.Size, IntPtr.Zero, IntPtr.Zero)) throw new Win32Exception();
            var si = new N.STARTUPINFOEX(); si.cb = Marshal.SizeOf(si); si.dwFlags = 0x100; si.hStdInput=hin; si.hStdOutput=ho; si.hStdError=he; si.lpAttributeList=list;
            var cmd = new StringBuilder(Quote(exe)); foreach (var a in args) cmd.Append(' ').Append(Quote(a));
            var keys = new List<string>(); foreach (DictionaryEntry e in env) keys.Add((string)e.Key); keys.Sort(StringComparer.OrdinalIgnoreCase);
            var eb = new StringBuilder(); foreach (var k in keys) eb.Append(k).Append('=').Append((string)env[k]).Append('\0'); eb.Append('\0');
            envp = Marshal.StringToHGlobalUni(eb.ToString());
            N.PROCESS_INFORMATION pi;
            if (!N.CreateProcessW(exe, cmd, IntPtr.Zero, IntPtr.Zero, true, 0x80000u|0x400u|0x4u|0x08000000u, envp, cwd, ref si, out pi)) throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_CREATE " + role);
            var proc = new C9Process(); proc.H = pi.hProcess; proc.Pid = pi.dwProcessId; proc.Role = role;
            try { Verify(proc); if (resume && N.ResumeThread(pi.hThread) == 0xFFFFFFFF) throw new Win32Exception(); }
            catch { N.TerminateProcess(pi.hProcess, 138); proc.Wait(5000); proc.Dispose(); throw; }
            finally { N.CloseHandle(pi.hThread); }
            return proc;
        } finally {
            if (hin != IntPtr.Zero) N.CloseHandle(hin); if (ho != IntPtr.Zero) N.CloseHandle(ho); if (he != ho && he != IntPtr.Zero) N.CloseHandle(he);
            if (capSid != IntPtr.Zero) N.LocalFree(capSid); if (capArray != IntPtr.Zero) Marshal.FreeHGlobal(capArray);
            if (list != IntPtr.Zero) { N.DeleteProcThreadAttributeList(list); Marshal.FreeHGlobal(list); }
            foreach (var p in new[]{caps,hl,jl,envp}) if (p != IntPtr.Zero) Marshal.FreeHGlobal(p);
        }
    }
    // Each shape must be refused by Verify before resume; returns the four refusal codes in order.
    public static string SelfTest(C9Container a, C9Container b, string exe, string cwd) {
        var env = new Hashtable(); foreach (var k in new[]{"SystemRoot","SystemDrive"}) env[k] = Environment.GetEnvironmentVariable(k); foreach (var k in new[]{"LOCALAPPDATA","TEMP","TMP","USERPROFILE"}) env[k] = cwd; var r = new List<string>(); var none = new string[0];
        using (var other = b.Create("selftest", exe, new[]{"-n","30","127.0.0.1"}, env, cwd, "", "", "", b.sid, false, false)) { try { a.Verify(other); r.Add("ACCEPTED"); } catch (InvalidOperationException e) { r.Add(e.Message); } other.Terminate(); }
        foreach (var shape in new[]{ Tuple.Create(IntPtr.Zero, false), Tuple.Create(b.sid, false), Tuple.Create(a.sid, true) }) {
            try { using (var p = a.Create("selftest", exe, none, env, cwd, "", "", "", shape.Item1, shape.Item2, false)) { p.Terminate(); r.Add("ACCEPTED"); } } catch (InvalidOperationException e) { r.Add(e.Message); }
        }
        return String.Join(",", r);
    }
    // Fail closed unless the child is in this exact job, in this exact container, with zero capabilities.
    public void Verify(C9Process p) {
        bool inJob; if (!N.IsProcessInJob(p.H, job, out inJob) || !inJob) throw new InvalidOperationException("C9W_NOT_IN_JOB");
        IntPtr t; if (!N.OpenProcessToken(p.H, 8, out t)) throw new Win32Exception();
        try {
            if (Int(t, 29) != 1) throw new InvalidOperationException("C9W_NOT_APPCONTAINER");
            IntPtr b = Info(t, 31, 8); bool same = N.EqualSid(Marshal.ReadIntPtr(b), sid); Marshal.FreeHGlobal(b); if (!same) throw new InvalidOperationException("C9W_WRONG_CONTAINER");
            if (Int(t, 30) != 0) throw new InvalidOperationException("C9W_CAPABILITIES");
        } finally { N.CloseHandle(t); }
    }
    static int Int(IntPtr t, int cls) { IntPtr b = Info(t, cls, 4); int v = Marshal.ReadInt32(b); Marshal.FreeHGlobal(b); return v; }
    static IntPtr Info(IntPtr t, int cls, int min) { int n; N.GetTokenInformation(t, cls, IntPtr.Zero, 0, out n); if (n < min) n = min; IntPtr b = Marshal.AllocHGlobal(n);
        if (!N.GetTokenInformation(t, cls, b, n, out n)) throw new Win32Exception(); return b; }
    public bool KillAll() { if (job == IntPtr.Zero) return true; N.TerminateJobObject(job, 139); for (int i=0;i<100 && ActiveProcesses>0;i++) Thread.Sleep(100); return ActiveProcesses==0; }
    // Handle cleanup is not profile deletion. A failed creation must never roll back by name.
    void CloseOwnedHandles() {
        try {
            if (job != IntPtr.Zero) {
                try { if (!KillAll()) throw new InvalidOperationException("C9W_JOB_KILL"); }
                finally { N.CloseHandle(job); job = IntPtr.Zero; }
            }
        } finally {
            if (sid != IntPtr.Zero) { N.FreeSid(sid); sid = IntPtr.Zero; }
        }
    }
    public void Dispose() { CloseOwnedHandles(); }
    // Only the journal-owning producer may request deletion, after durable removing.
    internal void DeleteConfirmedProfile() {
        if (!profile || job != IntPtr.Zero || sid != IntPtr.Zero)
            throw new InvalidOperationException("C9W_PROFILE_DELETE_STATE");
        int hr = N.DeleteAppContainerProfile(Name);
        if (hr != 0) throw new InvalidOperationException("C9W_PROFILE_DELETE");
        profile = false;
    }
}
static class N {
    [StructLayout(LayoutKind.Sequential)] public struct SECURITY_ATTRIBUTES { public int nLength; public IntPtr sd; public int bInheritHandle; }
    [StructLayout(LayoutKind.Sequential)] public struct SECURITY_CAPABILITIES { public IntPtr AppContainerSid, Capabilities; public uint CapabilityCount, Reserved; }
    [StructLayout(LayoutKind.Sequential)] public struct STARTUPINFOEX { public int cb; public IntPtr r1,r2,r3; public int x,y,xs,ys,xc,yc,fill; public int dwFlags; public short show, r4; public IntPtr r5, hStdInput, hStdOutput, hStdError, lpAttributeList; }
    [StructLayout(LayoutKind.Sequential)] public struct PROCESS_INFORMATION { public IntPtr hProcess, hThread; public int dwProcessId, dwThreadId; }
    [StructLayout(LayoutKind.Sequential)] public struct BASIC { public long a,b; public uint LimitFlags; public UIntPtr c,d; public uint ActiveProcessLimit; public UIntPtr e; public uint f,g; }
    [StructLayout(LayoutKind.Sequential)] public struct IO { public ulong a,b,c,d,e,f; }
    [StructLayout(LayoutKind.Sequential)] public struct JOBOBJECT_EXTENDED_LIMIT_INFORMATION { public BASIC Basic; public IO Io; public UIntPtr a,b,c,d; }
    [StructLayout(LayoutKind.Sequential)] public struct JOBOBJECT_BASIC_ACCOUNTING_INFORMATION { public long a,b,c,d; public uint pf, TotalProcesses, ActiveProcesses, Terminated; }
    [DllImport("userenv", CharSet=CharSet.Unicode)] public static extern int CreateAppContainerProfile(string n,string d,string s,IntPtr c,uint cc,out IntPtr sid);
    [DllImport("userenv", CharSet=CharSet.Unicode)] public static extern int DeleteAppContainerProfile(string n);
    [DllImport("advapi32", SetLastError=true)] public static extern bool ConvertSidToStringSidW(IntPtr sid,out IntPtr s);
    [DllImport("advapi32")] public static extern IntPtr FreeSid(IntPtr s);
    [DllImport("advapi32", SetLastError=true, CharSet=CharSet.Unicode)] public static extern bool ConvertStringSidToSidW(string s,out IntPtr sid);
    [DllImport("advapi32")] public static extern bool EqualSid(IntPtr a,IntPtr b);
    [DllImport("advapi32", SetLastError=true)] public static extern bool OpenProcessToken(IntPtr p,uint a,out IntPtr t);
    [DllImport("advapi32", SetLastError=true)] public static extern bool GetTokenInformation(IntPtr t,int c,IntPtr b,int n,out int r);
    [DllImport("kernel32")] public static extern IntPtr LocalFree(IntPtr p);
    [DllImport("kernel32", SetLastError=true, CharSet=CharSet.Unicode)] public static extern IntPtr CreateJobObjectW(IntPtr a,string n);
    [DllImport("kernel32", SetLastError=true)] public static extern bool SetInformationJobObject(IntPtr j,int c,IntPtr p,int n);
    [DllImport("kernel32", SetLastError=true)] public static extern bool QueryInformationJobObject(IntPtr j,int c,IntPtr p,int n,IntPtr r);
    [DllImport("kernel32", SetLastError=true)] public static extern bool TerminateJobObject(IntPtr j,uint c);
    [DllImport("kernel32", SetLastError=true)] public static extern bool IsProcessInJob(IntPtr p,IntPtr j,out bool r);
    [DllImport("kernel32", SetLastError=true, CharSet=CharSet.Unicode)] public static extern IntPtr CreateFileW(string n,uint a,uint s,ref SECURITY_ATTRIBUTES sa,uint d,uint f,IntPtr t);
    [DllImport("kernel32", SetLastError=true)] public static extern bool InitializeProcThreadAttributeList(IntPtr l,int c,int f,ref IntPtr s);
    [DllImport("kernel32", SetLastError=true)] public static extern bool UpdateProcThreadAttribute(IntPtr l,uint f,IntPtr a,IntPtr v,IntPtr s,IntPtr p,IntPtr r);
    [DllImport("kernel32")] public static extern void DeleteProcThreadAttributeList(IntPtr l);
    [DllImport("kernel32", SetLastError=true, CharSet=CharSet.Unicode)] public static extern bool CreateProcessW(string app,StringBuilder cmd,IntPtr pa,IntPtr ta,bool inh,uint flags,IntPtr env,string cwd,ref STARTUPINFOEX si,out PROCESS_INFORMATION pi);
    [DllImport("kernel32", SetLastError=true)] public static extern uint ResumeThread(IntPtr t);
    [DllImport("kernel32", SetLastError=true)] public static extern bool TerminateProcess(IntPtr p,uint c);
    [DllImport("kernel32")] public static extern uint WaitForSingleObject(IntPtr h,uint ms);
    [DllImport("kernel32", SetLastError=true)] public static extern bool GetExitCodeProcess(IntPtr p,out uint c);
    [DllImport("kernel32")] public static extern bool CloseHandle(IntPtr h);
}

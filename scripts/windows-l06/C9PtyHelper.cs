using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// H06 fixed compiled fictional helper binding. No real editor, SQL or credentials.
// Private parent launcher remains quarantined; building the executable grants no
// authority to run the native fixture. Same-user adversarial isolation is not claimed.
public static partial class C9OwnedJobProbe {
    const string PtyHelperSwitch="--fictional-conpty-v1";
    static readonly string[] PtyHelperFiles={
        "C9PtyHelper.exe","C9PtyHelper.dll","C9PtyHelper.deps.json","C9PtyHelper.runtimeconfig.json"
    };

    [DllImport("kernel32.dll",EntryPoint="GetCurrentProcess",ExactSpelling=true)]
    static extern IntPtr PtyCurrentProcess();
    [DllImport("kernel32.dll",ExactSpelling=true,SetLastError=true)]
    static extern uint GetProcessId(IntPtr process);
    [DllImport("kernel32.dll",ExactSpelling=true,SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool DuplicateHandle(IntPtr sourceProcess,IntPtr source,IntPtr targetProcess,
        out IntPtr target,uint access,[MarshalAs(UnmanagedType.Bool)] bool inherit,uint options);

    static bool PtyHex(string value,int length) {
        return value!=null && value.Length==length &&
            System.Text.RegularExpressions.Regex.IsMatch(value,@"\A[a-f0-9]+\z");
    }
    static string PtyBindingDigest(string[] args) {
        // Windows argv cannot contain embedded NUL; refuse rather than normalize.
        foreach (string arg in args)
            if (arg==null || arg.IndexOf('\0')>=0) throw new InvalidOperationException("C9W_PTY_BINDING_ARG");
        return Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(String.Join("\0",args)))).ToLowerInvariant();
    }
    sealed class PtyLaunchPlan {
        internal string Root,RootId,ParentToken,ParentId,ChildToken,Python,PythonSha,Mode,PackageDigest;
        internal byte[] ParentRecord;
        internal string[] Arguments(IntPtr parentWitness) {
            return new[]{PtyHelperSwitch,Root,RootId,ParentToken,ParentId,ChildToken,Python,PythonSha,
                Mode,PackageDigest,Environment.ProcessId.ToString(CultureInfo.InvariantCulture),
                parentWitness.ToInt64().ToString("x",CultureInfo.InvariantCulture)};
        }
    }

    // Private until approved native surrogate/ConPTY acceptance. Pins the fixed
    // four framework-dependent build outputs, with trusted installed .NET and
    // Python dependency provenance still required outside this bounded slice.
    static Observation RunBoundFictionalHelper(C9ControlLock owner,string helperDir,string[] helperShas,
        string python,string pythonSha,string mode,int timeoutMillis) {
        _=PtyScript(mode); // fixed mode only
        if (!OperatingSystem.IsWindows() || IntPtr.Size!=8 || owner==null ||
            timeoutMillis<15000 || timeoutMillis>30000 || !PtyHex(pythonSha,64) ||
            helperShas==null || helperShas.Length!=PtyHelperFiles.Length ||
            String.IsNullOrEmpty(python) || !Path.IsPathFullyQualified(python) ||
            Path.GetFullPath(python)!=python || !String.Equals(Path.GetFileName(python),"python.exe",StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_PTY_HELPER_ARGUMENT");
        string[] pins=(string[])helperShas.Clone();
        foreach (string pin in pins) if (!PtyHex(pin,64)) throw new InvalidOperationException("C9W_PTY_HELPER_PIN");
        lock (owner) {
            owner.AssertJournalWorkerUsable();
            using var helperDirectory=C9DirectoryIdentity.Open(helperDir,true);
            owner.AssertPtyPackageDirectory(helperDirectory);
            using var pythonDirectory=C9DirectoryIdentity.Open(Path.GetDirectoryName(python));
            var held=new List<FileStream>();
            try {
                void Pin(string path,string expected,string directoryId,bool ownerOnly) {
                    var stream=owner.OpenPtyPinnedFile(path,directoryId,ownerOnly);
                    held.Add(stream);
                    if (Convert.ToHexString(SHA256.HashData(stream)).ToLowerInvariant()!=expected)
                        throw new InvalidOperationException("C9W_PTY_HELPER_PIN");
                }
                // No extra files/subdirectories, including alternative runtime DLLs.
                var entries=Directory.GetFileSystemEntries(helperDir);
                if (entries.Length!=PtyHelperFiles.Length) throw new InvalidOperationException("C9W_PTY_HELPER_LAYOUT");
                for(int i=0;i<PtyHelperFiles.Length;i++)
                    Pin(Path.Combine(helperDir,PtyHelperFiles[i]),pins[i],helperDirectory.Id,true);
                Pin(python,pythonSha,pythonDirectory.Id,false);
                owner.AssertPtyPackageDirectory(helperDirectory);
                string child=Convert.ToHexString(RandomNumberGenerator.GetBytes(6)).ToLowerInvariant();
                if (child==owner.Token) throw new InvalidOperationException("C9W_PTY_CHILD_TOKEN");
                var plan=new PtyLaunchPlan{Root=owner.Root,RootId=owner.RootId,ParentToken=owner.Token,
                    ParentId=owner.ControlId,ChildToken=child,Python=python,PythonSha=pythonSha,
                    Mode=mode,PackageDigest=PtyBindingDigest(pins)};
                // Child must CREATE a distinct scope. Never reopen parent lock or
                // overwrite an existing child directory. Parent markers remain.
                var result=RunPinnedFictionalCore(owner,Path.Combine(helperDir,PtyHelperFiles[0]),
                    mode,timeoutMillis,true,null,plan);
                byte[] outer=C9PtyRecords.Outer(plan.ParentRecord,result.TimedOut,
                    result.RootExitedBeforeStop,result.RootExitCode,result.ActiveBeforeStop,
                    result.TotalProcesses,result.ActiveAfterStop);
                owner.RecordPtyOuter(plan.ParentRecord,outer);
                // Missing/partial child evidence refuses even after an outer
                // terminal observation; the outer record remains retained.
                owner.CrossCheckPtyChild(plan.ParentRecord,outer);
                return result; // bounded observation, not native acceptance
            } finally {
                foreach (var stream in held) stream.Dispose();
            }
        }
    }

    // Entry point of the separately built fixed helper, not a generic launcher.
    // Missing/malformed arguments refuse before any native call. Exit codes
    // intentionally carry no paths, exceptions, transcript or other raw output.
    static int Main(string[] args) {
        if (args==null || args.Length!=12 || args[0]!=PtyHelperSwitch ||
            !PtyHex(args[3],12) || !PtyHex(args[5],12) || args[3]==args[5] ||
            !PtyHex(args[7],64) || !PtyHex(args[9],64) ||
            !UInt32.TryParse(args[10],NumberStyles.None,CultureInfo.InvariantCulture,out uint parentPid) ||
            parentPid==0 || args[10]!=parentPid.ToString(CultureInfo.InvariantCulture) ||
            String.IsNullOrEmpty(args[11]) || args[11].Length>16 || !PtyHex(args[11],args[11].Length) ||
            !Int64.TryParse(args[11],NumberStyles.AllowHexSpecifier,CultureInfo.InvariantCulture,out long handle) ||
            handle<=0 || !OperatingSystem.IsWindows() || IntPtr.Size!=8)
            return 71;
        try {
            _=PtyScript(args[8]);
            byte[] parent=C9PtyRecords.Parent(args[2],args[4],args[3],args[5],args[8],args[9],PtyBindingDigest(args));
            IntPtr witness=new IntPtr(handle);
            // Parent independently checks exact outer membership before resume.
            // Child checks live inherited parent process identity and any-job
            // membership. No inherited job handle or exact child-side job proof.
            try {
                if (!GetHandleInformation(witness,out uint flags) || (flags&1)!=1 ||
                    GetProcessId(witness)!=parentPid || parentPid==Environment.ProcessId ||
                    Signaled(witness,0) ||
                    !N.IsProcessInJob(PtyCurrentProcess(),IntPtr.Zero,out bool inside) || !inside)
                    return 72;
                C9ControlLock.VerifyPtyParentBinding(args[1],parent);
                if (Signaled(witness,0)) return 72;
            } finally { Close(ref witness); }
            using var child=C9ControlLock.Create(args[1],args[5]);
            if (child.RootId!=args[2] || child.ControlId==args[4])
                return 73;
            byte[] identity=child.MarkPtyChildBinding(parent);
            bool returned=false;
            try {
                returned=RunFictionalConPty(child,parent,identity,args[6],args[7],args[8],10000)==0;
            } catch { /* fixed refusal; no raw output */ }
            child.MarkPtyChildResult(parent,identity,returned);
            return returned ? 0 : 74;
        } catch { return 74; }
    }
}

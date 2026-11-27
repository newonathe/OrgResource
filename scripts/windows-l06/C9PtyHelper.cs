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
    const string PtyHelperSwitch="--fictional-conpty-v2";
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

    // Unnamed, pagefile-backed mapping and two one-shot events. Child gets only
    // map-write, ready-signal and ack-wait capabilities via the exact handle list.
    // No job handle, no parent PROCESS_DUP_HANDLE, no name-based reopen.
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode,SetLastError=true)]
    static extern IntPtr CreateFileMappingW(IntPtr file,IntPtr security,uint protect,uint high,uint low,string name);
    [DllImport("kernel32.dll",ExactSpelling=true,SetLastError=true)]
    static extern IntPtr MapViewOfFile(IntPtr map,uint access,uint high,uint low,UIntPtr size);
    [DllImport("kernel32.dll",ExactSpelling=true,SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool UnmapViewOfFile(IntPtr view);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode,SetLastError=true)]
    static extern IntPtr CreateEventW(IntPtr security,[MarshalAs(UnmanagedType.Bool)] bool manual,
        [MarshalAs(UnmanagedType.Bool)] bool initial,string name);
    [DllImport("kernel32.dll",ExactSpelling=true,SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool SetEvent(IntPtr evt);

    // Pure fixed-size frame, never stored as a recoverable process identity.
    // The values are meaningful only while the original helper handle is held.
    public static byte[] EncodePtyClientWitness(long handle,uint pid) {
        if(handle<=0 || pid==0) throw new InvalidOperationException("C9W_PTY_WITNESS_FRAME");
        var b=new byte[16];
        System.Buffers.Binary.BinaryPrimitives.WriteUInt32LittleEndian(b.AsSpan(0,4),0x31573943);
        System.Buffers.Binary.BinaryPrimitives.WriteUInt32LittleEndian(b.AsSpan(4,4),pid);
        System.Buffers.Binary.BinaryPrimitives.WriteInt64LittleEndian(b.AsSpan(8,8),handle);
        return b;
    }
    public static long DecodePtyClientWitness(byte[] b,uint helperPid,out uint pid) {
        pid=0;
        if(b==null || b.Length!=16 || helperPid==0 ||
            System.Buffers.Binary.BinaryPrimitives.ReadUInt32LittleEndian(b.AsSpan(0,4))!=0x31573943)
            throw new InvalidOperationException("C9W_PTY_WITNESS_FRAME");
        uint p=System.Buffers.Binary.BinaryPrimitives.ReadUInt32LittleEndian(b.AsSpan(4,4));
        long h=System.Buffers.Binary.BinaryPrimitives.ReadInt64LittleEndian(b.AsSpan(8,8));
        if(h<=0 || p==0 || p==helperPid) throw new InvalidOperationException("C9W_PTY_WITNESS_FRAME");
        pid=p; return h;
    }
    static IntPtr PtyInheritable(IntPtr original,uint access) {
        IntPtr dup=IntPtr.Zero;
        try {
            if(!DuplicateHandle(PtyCurrentProcess(),original,PtyCurrentProcess(),out dup,access,true,0) ||
               !GetHandleInformation(dup,out uint f) || (f&1)!=1)
                throw Native("C9W_PTY_WITNESS_INHERIT");
            var result=dup; dup=IntPtr.Zero; return result;
        } finally { Close(ref dup); }
    }
    sealed class PtyExchange : IDisposable {
        IntPtr map,ready,ack,view;
        internal IntPtr ChildMap,ChildReady,ChildAck;
        bool accepted;
        internal static PtyExchange Create() {
            var x=new PtyExchange();
            try {
                x.map=CreateFileMappingW(new IntPtr(-1),IntPtr.Zero,4,0,16,null);
                x.ready=CreateEventW(IntPtr.Zero,true,false,null);
                x.ack=CreateEventW(IntPtr.Zero,true,false,null);
                if(x.map==IntPtr.Zero||x.ready==IntPtr.Zero||x.ack==IntPtr.Zero)
                    throw Native("C9W_PTY_WITNESS_CREATE");
                foreach(var h in new[]{x.map,x.ready,x.ack})
                    if(!GetHandleInformation(h,out uint f)||(f&1)!=0)
                        throw Native("C9W_PTY_WITNESS_PARENT_INHERIT");
                x.view=MapViewOfFile(x.map,4,0,0,(UIntPtr)16);
                if(x.view==IntPtr.Zero||Signaled(x.ready,0)||Signaled(x.ack,0))
                    throw new InvalidOperationException("C9W_PTY_WITNESS_INITIAL");
                var zero=new byte[16]; Marshal.Copy(x.view,zero,0,16);
                foreach(byte v in zero) if(v!=0) throw new InvalidOperationException("C9W_PTY_WITNESS_INITIAL");
                x.ChildMap=PtyInheritable(x.map,2);
                x.ChildReady=PtyInheritable(x.ready,2);
                x.ChildAck=PtyInheritable(x.ack,0x100000);
                return x;
            } catch { x.Dispose(); throw; }
        }
        internal void CloseChildCopies() {
            try { Close(ref ChildMap); }
            finally { try { Close(ref ChildReady); } finally { Close(ref ChildAck); } }
        }
        internal bool Ready => Signaled(ready,0);
        internal IntPtr Accept(IntPtr helper,IntPtr job,System.Diagnostics.Stopwatch watch,int timeout) {
            if(accepted||!Ready||Signaled(ack,0)||Signaled(helper,0)||watch.ElapsedMilliseconds>=timeout)
                throw new InvalidOperationException("C9W_PTY_WITNESS_STATE");
            System.Threading.Thread.MemoryBarrier();
            var b=new byte[16]; Marshal.Copy(view,b,0,b.Length);
            uint helperPid=GetProcessId(helper);
            long remote=DecodePtyClientWitness(b,helperPid,out uint pid);
            IntPtr client=IntPtr.Zero;
            try {
                // Source process is the exact original helper handle, not a PID
                // reopen. Only query+wait access on the duplicate is requested.
                if(!DuplicateHandle(helper,new IntPtr(remote),PtyCurrentProcess(),out client,0x100400,false,0))
                    throw Native("C9W_PTY_WITNESS_DUP");
                if(!GetHandleInformation(client,out uint flags)||(flags&1)!=0||
                   GetProcessId(client)!=pid||Signaled(client,0)||Signaled(helper,0)||
                   !N.IsProcessInJob(client,job,out bool inside)||!inside)
                    throw new InvalidOperationException("C9W_PTY_WITNESS_IDENTITY");
                RequireChildRestriction(client);
                var a=Account(job);
                if(a.TotalProcesses!=2||a.ActiveProcesses!=2||watch.ElapsedMilliseconds>=timeout)
                    throw new InvalidOperationException("C9W_PTY_WITNESS_COUNT");
                if(!SetEvent(ack)) throw Native("C9W_PTY_WITNESS_ACK");
                accepted=true;
                var result=client; client=IntPtr.Zero; return result;
            } finally { Close(ref client); }
        }
        public void Dispose() {
            bool failed=false;
            try { CloseChildCopies(); } catch { failed=true; }
            if(view!=IntPtr.Zero) {
                if(!UnmapViewOfFile(view)) failed=true;
                else view=IntPtr.Zero;
            }
            try { Close(ref map); } catch { failed=true; }
            try { Close(ref ready); } catch { failed=true; }
            try { Close(ref ack); } catch { failed=true; }
            if(failed) throw new InvalidOperationException("C9W_PTY_WITNESS_CLOSE");
        }
    }
    sealed class PtyChildExchange : IDisposable {
        IntPtr map,ready,ack;
        bool sent;
        internal PtyChildExchange(IntPtr m,IntPtr r,IntPtr a) {
            map=m; ready=r; ack=a;
            foreach(var h in new[]{map,ready,ack})
                if(!GetHandleInformation(h,out uint f)||(f&1)!=1)
                    throw new InvalidOperationException("C9W_PTY_WITNESS_CHILD_INHERIT");
        }
        internal void Publish(IntPtr client,System.Diagnostics.Stopwatch watch,int timeout) {
            if(sent||Signaled(client,0)||Signaled(ack,0)||watch.ElapsedMilliseconds>=timeout)
                throw new InvalidOperationException("C9W_PTY_WITNESS_STATE");
            var b=EncodePtyClientWitness(client.ToInt64(),GetProcessId(client));
            IntPtr v=MapViewOfFile(map,2,0,0,(UIntPtr)16);
            if(v==IntPtr.Zero) throw Native("C9W_PTY_WITNESS_MAP");
            try {
                var empty=new byte[16]; Marshal.Copy(v,empty,0,16);
                foreach(byte n in empty) if(n!=0) throw new InvalidOperationException("C9W_PTY_WITNESS_REUSE");
                Marshal.Copy(b,0,v,b.Length);
                System.Threading.Thread.MemoryBarrier();
            } finally {
                if(!UnmapViewOfFile(v)) throw Native("C9W_PTY_WITNESS_UNMAP");
            }
            sent=true;
            if(!SetEvent(ready)) throw Native("C9W_PTY_WITNESS_READY");
            long remaining=timeout-watch.ElapsedMilliseconds;
            if(remaining<=0||!Signaled(ack,(int)remaining)||watch.ElapsedMilliseconds>=timeout||Signaled(client,0))
                throw new InvalidOperationException("C9W_PTY_WITNESS_ACK_TIMEOUT");
        }
        public void Dispose() {
            try { Close(ref map); } finally { try { Close(ref ready); } finally { Close(ref ack); } }
        }
    }
    static bool PtyHandleArg(string value,out long handle) {
        handle=0;
        return !String.IsNullOrEmpty(value)&&value.Length<=16&&PtyHex(value,value.Length)&&
            Int64.TryParse(value,NumberStyles.AllowHexSpecifier,CultureInfo.InvariantCulture,out handle)&&
            handle>0&&value==handle.ToString("x",CultureInfo.InvariantCulture);
    }

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
        internal string[] Arguments(IntPtr parentWitness,PtyExchange exchange) {
            return new[]{PtyHelperSwitch,Root,RootId,ParentToken,ParentId,ChildToken,Python,PythonSha,
                Mode,PackageDigest,Environment.ProcessId.ToString(CultureInfo.InvariantCulture),
                parentWitness.ToInt64().ToString("x",CultureInfo.InvariantCulture),
                exchange.ChildMap.ToInt64().ToString("x",CultureInfo.InvariantCulture),
                exchange.ChildReady.ToInt64().ToString("x",CultureInfo.InvariantCulture),
                exchange.ChildAck.ToInt64().ToString("x",CultureInfo.InvariantCulture)};
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
        if (args==null || args.Length!=15 || args[0]!=PtyHelperSwitch ||
            !PtyHex(args[3],12) || !PtyHex(args[5],12) || args[3]==args[5] ||
            !PtyHex(args[7],64) || !PtyHex(args[9],64) ||
            !UInt32.TryParse(args[10],NumberStyles.None,CultureInfo.InvariantCulture,out uint parentPid) ||
            parentPid==0 || args[10]!=parentPid.ToString(CultureInfo.InvariantCulture) ||
            !PtyHandleArg(args[11],out long handle) || !PtyHandleArg(args[12],out long map) ||
            !PtyHandleArg(args[13],out long ready) || !PtyHandleArg(args[14],out long ack) ||
            new HashSet<long>{handle,map,ready,ack}.Count!=4 || !OperatingSystem.IsWindows() || IntPtr.Size!=8)
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
            using var exchange=new PtyChildExchange(new IntPtr(map),new IntPtr(ready),new IntPtr(ack));
            using var child=C9ControlLock.Create(args[1],args[5]);
            if (child.RootId!=args[2] || child.ControlId==args[4])
                return 73;
            byte[] identity=child.MarkPtyChildBinding(parent);
            bool returned=false;
            try {
                returned=RunFictionalConPty(child,parent,identity,args[6],args[7],args[8],10000,exchange)==0;
            } catch { /* fixed refusal; no raw output */ }
            child.MarkPtyChildResult(parent,identity,returned);
            return returned ? 0 : 74;
        } catch { return 74; }
    }
}

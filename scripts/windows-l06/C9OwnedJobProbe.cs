using System;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.Win32.SafeHandles;

// H06 fictional host-side job probe, not a credential producer. No AppContainer,
// role/MySQL job reuse, inherited handles, password, network or arbitrary script.
// Job accounting plus the root's held handle is bounded evidence, not an
// independent per-descendant termination witness or a hard kernel-call deadline.
public static partial class C9OwnedJobProbe {
    const int Limit = 8;
    const int StopMillis = 10000;

    public sealed class Observation {
        public string Mode { get; internal set; }
        public bool TimedOut { get; internal set; }
        public bool RootExitedBeforeStop { get; internal set; }
        public int RootExitCode { get; internal set; }
        public int ActiveBeforeStop { get; internal set; }
        public int TotalProcesses { get; internal set; }
        public int ActiveAfterStop { get; internal set; }
        public long ElapsedMillis { get; internal set; }
    }

    static Exception Native(string code) { return new Win32Exception(Marshal.GetLastWin32Error(),code); }
    static void Close(ref IntPtr handle) {
        if (handle == IntPtr.Zero) return;
        if (!N.CloseHandle(handle)) throw Native("C9W_WATCHDOG_HANDLE_CLOSE");
        handle = IntPtr.Zero;
    }

    static N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION Account(IntPtr job) {
        if (job == IntPtr.Zero) throw new InvalidOperationException("C9W_WATCHDOG_JOB");
        int size = Marshal.SizeOf<N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION>();
        IntPtr buffer = Marshal.AllocHGlobal(size);
        try {
            if (!N.QueryInformationJobObject(job,1,buffer,size,IntPtr.Zero))
                throw Native("C9W_WATCHDOG_ACCOUNT");
            var a = Marshal.PtrToStructure<N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION>(buffer);
            if (a.ActiveProcesses > Limit || a.TotalProcesses < a.ActiveProcesses)
                throw new InvalidOperationException("C9W_WATCHDOG_ACCOUNT");
            return a;
        } finally { Marshal.FreeHGlobal(buffer); }
    }

    static bool Signaled(IntPtr process, int ms) {
        if (process == IntPtr.Zero || ms < 0)
            throw new InvalidOperationException("C9W_WATCHDOG_PROCESS");
        uint result = N.WaitForSingleObject(process,(uint)ms);
        return C9Lifecycle.WaitResult(result,Marshal.GetLastWin32Error());
    }

    static int Exit(IntPtr process) {
        if (!Signaled(process,0))
            throw new InvalidOperationException("C9W_WATCHDOG_ROOT_RUNNING");
        if (!N.GetExitCodeProcess(process,out uint value))
            throw Native("C9W_WATCHDOG_EXIT");
        return unchecked((int)value);
    }

    static N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION Stop(IntPtr job, IntPtr root) {
        // Stop only the exact newly created unnamed Job Object. Never enumerate
        // process names, kill a role job or fall back to a PID/tree guess.
        if (!N.TerminateJobObject(job,139))
            throw Native("C9W_WATCHDOG_TERMINATE");
        var watch = Stopwatch.StartNew();
        while (watch.ElapsedMilliseconds < StopMillis) {
            var a = Account(job);
            if (a.ActiveProcesses == 0 && (root == IntPtr.Zero || Signaled(root,0)))
                return a;
            Thread.Sleep(25);
        }
        throw new InvalidOperationException("C9W_WATCHDOG_STOP_UNKNOWN");
    }

    // Windows quoting: use only known fixed script strings and exact canonical
    // executable path. No shell, no caller-provided arguments or arbitrary code.
    static string Quote(string value) {
        var text = new StringBuilder("\"");
        int slash = 0;
        foreach (char c in value) {
            if (c == '\\') { slash++; continue; }
            if (c == '"') { text.Append('\\',slash*2+1).Append('"'); slash=0; continue; }
            text.Append('\\',slash).Append(c); slash=0;
        }
        return text.Append('\\',slash*2).Append('"').ToString();
    }

    static string Script(string mode) {
        switch (mode) {
            case "zero": return "raise SystemExit(0)";
            case "nonzero": return "raise SystemExit(23)";
            case "root-hang": return "import time; time.sleep(120)";
            case "child-zero": return "import sys,subprocess; raise SystemExit(subprocess.call([sys.executable,'-I','-S','-B','-c','raise SystemExit(0)']))";
            case "descendant-hang": return "import sys,subprocess; subprocess.Popen([sys.executable,'-I','-S','-B','-c','import time; time.sleep(120)']); raise SystemExit(0)";
            case "helper-tree-hang": return "import sys,subprocess,time; child=subprocess.Popen([sys.executable,'-I','-S','-B','-c','import time; time.sleep(120)']); time.sleep(120)";
            default: throw new InvalidOperationException("C9W_WATCHDOG_MODE");
        }
    }

    public static Observation RunFictional(C9ControlLock owner,string python,string expectedSha,
                                           string mode,int timeoutMillis) {
        if (owner == null || mode == "helper-tree-hang")
            throw new InvalidOperationException("C9W_WATCHDOG_ARGUMENT");
        lock (owner) {
            owner.AssertJournalWorkerUsable();
            return RunFictionalCore(owner,python,expectedSha,mode,timeoutMillis,false);
        }
    }

    // The outer supervisor surrogate owns a separate process tree, not the
    // ConPTY worker itself. Retain intent on every launch, including success.
    // Only fixed fictional Python scripts; never accept a command or helper path.
    public static Observation RunFictionalOuterWatchdog(C9ControlLock owner,string python,
                                                string expectedSha,string mode,int timeoutMillis) {
        if (owner == null) throw new InvalidOperationException("C9W_WATCHDOG_ARGUMENT");
        lock (owner) {
            owner.AssertJournalWorkerUsable();
            return RunFictionalCore(owner,python,expectedSha,mode,timeoutMillis,true);
        }
    }

    static Observation RunFictionalCore(C9ControlLock owner,string python,string expectedSha,
                                       string mode,int timeoutMillis,bool retainIntent) {
        string script = Script(mode);
        if (!OperatingSystem.IsWindows() || IntPtr.Size != 8 || owner == null ||
            timeoutMillis < 1000 || timeoutMillis > 30000 ||
            expectedSha == null || !System.Text.RegularExpressions.Regex.IsMatch(expectedSha,@"\A[a-f0-9]{64}\z") ||
            String.IsNullOrEmpty(python) || !Path.IsPathFullyQualified(python) ||
            !String.Equals(Path.GetFullPath(python),python,StringComparison.Ordinal) ||
            !String.Equals(Path.GetFileName(python),"python.exe",StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_WATCHDOG_ARGUMENT");
        owner.AssertWatchdogCustody();
        using var directory = C9DirectoryIdentity.Open(Path.GetDirectoryName(python));
        using var cwd = C9DirectoryIdentity.Open(owner.ControlPath);
        if (cwd.Id != owner.ControlId ||
            ((int)File.GetAttributes(python) & 0x441410) != 0)
            throw new InvalidOperationException("C9W_WATCHDOG_TOOL");
        // Hold executable read-sharing only across launch/observation; interpreter
        // DLL/stdlib provenance remains an H02 prerequisite, not asserted here.
        using var pin = new FileStream(python,FileMode.Open,FileAccess.Read,FileShare.Read);
        if (Convert.ToHexString(SHA256.HashData(pin)).ToLowerInvariant() != expectedSha)
            throw new InvalidOperationException("C9W_WATCHDOG_TOOL");
        return RunPinnedFictionalCore(owner,python,mode,timeoutMillis,retainIntent,
            () => Quote(python)+" -I -S -B -c "+Quote(script),null);
    }

    // Private shared native core. The optional helper plan is built only from
    // the fixed compiled helper contract; never from arbitrary command input.
    static Observation RunPinnedFictionalCore(C9ControlLock owner,string executable,
        string mode,int timeoutMillis,bool retainIntent,Func<string> fixedCommand,PtyLaunchPlan helper) {
        string system = Environment.GetEnvironmentVariable("SystemRoot");
        string drive = Environment.GetEnvironmentVariable("SystemDrive");
        if (String.IsNullOrEmpty(system) || String.IsNullOrEmpty(drive))
            throw new InvalidOperationException("C9W_WATCHDOG_TOOL");

        IntPtr job=IntPtr.Zero, root=IntPtr.Zero, thread=IntPtr.Zero, list=IntPtr.Zero,
               jobList=IntPtr.Zero, environment=IntPtr.Zero,
               parentWitness=IntPtr.Zero, inheritedList=IntPtr.Zero;
        bool initialized=false, assigned=false, terminalObserved=false;
        Observation receipt=null;
        var clock=Stopwatch.StartNew();
        try {
            job=N.CreateJobObjectW(IntPtr.Zero,null); // private, unnamed, noninheritable
            if (job == IntPtr.Zero) throw Native("C9W_WATCHDOG_JOB_CREATE");
            var limits=new N.JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
            limits.Basic.LimitFlags=0x2000|0x400|0x8; // kill-on-close; no breakaway
            limits.Basic.ActiveProcessLimit=Limit;
            int size=Marshal.SizeOf(limits);
            IntPtr buffer=Marshal.AllocHGlobal(size);
            try {
                Marshal.StructureToPtr(limits,buffer,false);
                if (!N.SetInformationJobObject(job,9,buffer,size))
                    throw Native("C9W_WATCHDOG_JOB_LIMIT");
            } finally { Marshal.FreeHGlobal(buffer); }
            var empty=Account(job);
            if (empty.ActiveProcesses != 0 || empty.TotalProcesses != 0)
                throw new InvalidOperationException("C9W_WATCHDOG_JOB_NOT_FRESH");

            IntPtr bytes=IntPtr.Zero;
            int attributeCount=helper==null ? 1 : 2;
            bool first=N.InitializeProcThreadAttributeList(IntPtr.Zero,attributeCount,0,ref bytes);
            int error=Marshal.GetLastWin32Error();
            if (first || error != 122 || bytes.ToInt64() <= 0 || bytes.ToInt64() > 1048576)
                throw new InvalidOperationException("C9W_WATCHDOG_ATTRIBUTE_SIZE");
            list=Marshal.AllocHGlobal(bytes);
            if (!N.InitializeProcThreadAttributeList(list,attributeCount,0,ref bytes))
                throw Native("C9W_WATCHDOG_ATTRIBUTE_INIT");
            initialized=true;
            jobList=Marshal.AllocHGlobal(IntPtr.Size);
            Marshal.WriteIntPtr(jobList,job);
            if (!N.UpdateProcThreadAttribute(list,0,(IntPtr)0x2000D,jobList,(IntPtr)IntPtr.Size,IntPtr.Zero,IntPtr.Zero))
                throw Native("C9W_WATCHDOG_ATTRIBUTE_JOB");
            string[] helperArgs=null;
            if (helper!=null) {
                // Inherit a limited parent-process liveness handle, NEVER a job
                // handle: a suspended or stalled helper cannot keep the job alive.
                if (!DuplicateHandle(PtyCurrentProcess(),PtyCurrentProcess(),PtyCurrentProcess(),
                    out parentWitness,0x101000,true,0) ||
                    !GetHandleInformation(parentWitness,out uint qf) || (qf&1)!=1)
                    throw Native("C9W_PTY_OUTER_PARENT_HANDLE");
                inheritedList=Marshal.AllocHGlobal(IntPtr.Size);
                Marshal.WriteIntPtr(inheritedList,parentWitness);
                if (!N.UpdateProcThreadAttribute(list,0,(IntPtr)0x20002,inheritedList,(IntPtr)IntPtr.Size,IntPtr.Zero,IntPtr.Zero))
                    throw Native("C9W_PTY_OUTER_HANDLES");
                helperArgs=helper.Arguments(parentWitness);
            }
            var si=new N.STARTUPINFOEX { cb=Marshal.SizeOf<N.STARTUPINFOEX>(),lpAttributeList=list };
            var cmd=new StringBuilder(helper==null ? fixedCommand() :
                Quote(executable)+" "+String.Join(" ",Array.ConvertAll(helperArgs,Quote)));
            // Explicit minimal environment; no caller environment, MYSQL_PWD,
            // login-path setting, profile path, or secret.
            environment=Marshal.StringToHGlobalUni("SystemDrive="+drive+"\0SystemRoot="+system+"\0\0");
            owner.AssertWatchdogCustody();
            // Persist the existing exclusive owner-only uncertainty marker before
            // launching the surrogate. No positive result settles or deletes it.
            if (retainIntent) {
                owner.MarkJournalWorkerUncertain();
                if (helper!=null) {
                    helper.ParentRecord=C9PtyRecords.Parent(helper.RootId,helper.ParentId,helper.ParentToken,
                        helper.ChildToken,helper.Mode,helper.PackageDigest,PtyBindingDigest(helperArgs));
                    owner.MarkPtyParentBinding(helper.ParentRecord);
                }
                if (clock.ElapsedMilliseconds >= timeoutMillis)
                    throw new InvalidOperationException("C9W_OUTER_SETUP_TIMEOUT");
            }
            if (!N.CreateProcessW(executable,cmd,IntPtr.Zero,IntPtr.Zero,helper!=null,
                 0x80000u|0x400u|0x4u|0x08000000u,environment,owner.ControlPath,ref si,out var pi))
                throw Native("C9W_WATCHDOG_CREATE");
            root=pi.hProcess; thread=pi.hThread;
            Close(ref parentWitness); // parent retains the sole private job handle
            if (!N.IsProcessInJob(root,job,out bool inJob))
                throw Native("C9W_WATCHDOG_JOB_QUERY");
            if (!inJob) throw new InvalidOperationException("C9W_WATCHDOG_WRONG_JOB");
            assigned=true;
            var initial=Account(job);
            if (initial.ActiveProcesses != 1 || initial.TotalProcesses != 1)
                throw new InvalidOperationException("C9W_WATCHDOG_INITIAL_COUNT");
            if (retainIntent && clock.ElapsedMilliseconds >= timeoutMillis)
                throw new InvalidOperationException("C9W_OUTER_SETUP_TIMEOUT");
            C9Lifecycle.RequireInitialResume(N.ResumeThread(thread));
            Close(ref thread);

            // For the outer path, one deadline includes native setup and execution;
            // bounded termination grace is separate, not a hard kernel deadline.
            var watch=retainIntent ? clock : Stopwatch.StartNew();
            bool rootExited=false, completed=false;
            N.JOBOBJECT_BASIC_ACCOUNTING_INFORMATION current=initial;
            while (watch.ElapsedMilliseconds < timeoutMillis) {
                rootExited=Signaled(root,0);
                current=Account(job);
                if (rootExited && current.ActiveProcesses == 0) { completed=true; break; }
                Thread.Sleep(25);
            }
            // Capture the exact held root and job state at the decision boundary.
            if (!completed) {
                rootExited=Signaled(root,0);
                current=Account(job);
                // Do not convert late completion into success on the outer path.
                completed=!retainIntent && rootExited && current.ActiveProcesses == 0;
            }
            if (retainIntent && watch.ElapsedMilliseconds >= timeoutMillis)
                completed=false;
            int activeBefore=(int)current.ActiveProcesses;
            var last=completed ? current : Stop(job,root);
            terminalObserved=true;
            if (!Signaled(root,0) || last.ActiveProcesses != 0 ||
                last.TotalProcesses < current.TotalProcesses)
                throw new InvalidOperationException("C9W_WATCHDOG_STOP_UNKNOWN");
            receipt=new Observation {
                Mode=mode,TimedOut=!completed,RootExitedBeforeStop=rootExited,
                RootExitCode=Exit(root),ActiveBeforeStop=activeBefore,
                TotalProcesses=checked((int)last.TotalProcesses),ActiveAfterStop=(int)last.ActiveProcesses,
                ElapsedMillis=clock.ElapsedMilliseconds
            };
            owner.AssertWatchdogCustody();
        } finally {
            // No exception is downgraded to success; even a later zero count
            // cannot repair an earlier native failure. If membership is unknown,
            // the returned suspended root is separately stopped by its handle.
            try {
                if (root != IntPtr.Zero && !assigned && !Signaled(root,0)) {
                    if (!N.TerminateProcess(root,139) && !Signaled(root,0))
                        throw Native("C9W_WATCHDOG_ROOT_STOP");
                    if (!Signaled(root,StopMillis))
                        throw new InvalidOperationException("C9W_WATCHDOG_ROOT_STOP_UNKNOWN");
                }
                if (job != IntPtr.Zero && !terminalObserved)
                    Stop(job,root);
            } finally {
                try { Close(ref thread); }
                finally {
                    try { Close(ref root); }
                    finally {
                        try { Close(ref job); }
                        finally {
                            try { Close(ref parentWitness); }
                            finally {
                                if (initialized) N.DeleteProcThreadAttributeList(list);
                                foreach (var p in new[]{list,jobList,inheritedList,environment})
                                    if (p != IntPtr.Zero) Marshal.FreeHGlobal(p);
                            }
                        }
                    }
                }
            }
        }
        return receipt ?? throw new InvalidOperationException("C9W_WATCHDOG_NO_RECEIPT");
    }

    // A single-use fictional transport probe, not the journal publication path.
    // Deliberately retain the existing uncertainty marker on EVERY launch.
    // No positive result authorizes clearing it or starting a second worker.
    [DllImport("kernel32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool CreatePipe(out IntPtr read, out IntPtr write, ref N.SECURITY_ATTRIBUTES sa, uint size);
    [DllImport("kernel32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool SetHandleInformation(IntPtr handle, uint mask, uint flags);
    [DllImport("kernel32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool GetHandleInformation(IntPtr handle, out uint flags);

    [DllImport("kernel32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool GetProcessMitigationPolicy(IntPtr process, int policy, out uint flags, UIntPtr length);

    // ProcessChildProcessPolicy = 13; NoChildProcessCreation = bit 0.
    // Require the actual returned suspended process to be restricted, not audit-only.
    // This is defense in depth for trusted fictional workers, not an adversarial sandbox.
    static void RequireChildRestriction(IntPtr root) {
        if (!GetProcessMitigationPolicy(root,13,out uint flags,(UIntPtr)sizeof(uint)))
            throw Native("C9W_WORKER_CHILD_POLICY_QUERY");
        if (flags != 1)
            throw new InvalidOperationException("C9W_WORKER_CHILD_POLICY");
    }

    static void Pipe(IntPtr[] handles, int at, bool parentWrites) {
        var sa = new N.SECURITY_ATTRIBUTES {
            nLength=Marshal.SizeOf<N.SECURITY_ATTRIBUTES>(), bInheritHandle=0
        };
        if (!CreatePipe(out handles[at],out handles[at+1],ref sa,4096))
            throw Native("C9W_WORKER_PIPE");
        int parent=parentWrites ? at+1 : at;
        int child=parentWrites ? at : at+1;
        // Only the child-side end is ever marked inheritable.
        if (!SetHandleInformation(handles[child],1,1) ||
            !GetHandleInformation(handles[parent],out uint pf) || (pf & 1) != 0 ||
            !GetHandleInformation(handles[child],out uint cf) || (cf & 1) != 1)
            throw Native("C9W_WORKER_PIPE_INHERITANCE");
    }

    static byte[] ReadFictionalPipe(SafeFileHandle handle) {
        using (handle)
        using (var stream=new FileStream(handle,FileAccess.Read,4096,false))
        using (var result=new MemoryStream()) {
            byte[] buf=new byte[128];
            int n;
            while ((n=stream.Read(buf,0,buf.Length)) != 0) {
                if (result.Length+n > 256) throw new InvalidOperationException("C9W_WORKER_PIPE_OVERFLOW");
                result.Write(buf,0,n);
            }
            return result.ToArray();
        }
    }
    static void WriteFictionalPipe(SafeFileHandle handle,byte[] bytes) {
        using (handle)
        using (var stream=new FileStream(handle,FileAccess.Write,4096,false)) {
            stream.Write(bytes,0,bytes.Length);
            stream.Flush();
        }
    }
    static SafeFileHandle TakePipe(IntPtr[] handles,int at) {
        var h=new SafeFileHandle(handles[at],true);
        if (h.IsInvalid) { h.Dispose(); throw new InvalidOperationException("C9W_WORKER_PIPE_HANDLE"); }
        handles[at]=IntPtr.Zero;
        return h;
    }
    static string FictionalTransportScript(string mode) {
        switch (mode) {
            case "echo": return "import sys; sys.stdout.buffer.write(sys.stdin.buffer.read()); sys.stdout.buffer.flush()";
            case "nonzero": return "import sys; sys.stdin.buffer.read(); raise SystemExit(23)";
            case "child-denied": return "import sys,subprocess\n"
                + "sys.stdin.buffer.read()\n"
                + "try:\n"
                + " subprocess.run([sys.executable,'-I','-S','-B','-c','raise SystemExit(0)'],check=True)\n"
                + "except OSError as e:\n"
                + " if getattr(e,'winerror',None)!=367: raise\n"
                + " sys.stdout.buffer.write(b'child-process-blocked-367'); sys.stdout.buffer.flush()\n"
                + "else:\n"
                + " raise SystemExit(24)\n";
            case "overflow": return "import sys,time; sys.stdin.buffer.read(); sys.stdout.buffer.write(b'x'*4096); sys.stdout.buffer.flush(); time.sleep(120)";
            case "hang": return "import time; time.sleep(120)";
            default: throw new InvalidOperationException("C9W_WORKER_MODE");
        }
    }

    public static int RunFictionalJournalTransport(C9ControlLock owner,string python,string expectedSha,
                                                    string mode,int timeoutMillis) {
        string script=FictionalTransportScript(mode);
        if (!OperatingSystem.IsWindows() || IntPtr.Size != 8 || owner == null ||
            timeoutMillis < 1000 || timeoutMillis > 10000 ||
            expectedSha == null || !System.Text.RegularExpressions.Regex.IsMatch(expectedSha,@"\A[a-f0-9]{64}\z") ||
            String.IsNullOrEmpty(python) || !Path.IsPathFullyQualified(python) ||
            !String.Equals(Path.GetFullPath(python),python,StringComparison.Ordinal) ||
            !String.Equals(Path.GetFileName(python),"python.exe",StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_WORKER_ARGUMENT");
        lock(owner) {
            owner.AssertJournalWorkerUsable();
            using var directory=C9DirectoryIdentity.Open(Path.GetDirectoryName(python));
            using var cwd=C9DirectoryIdentity.Open(owner.ControlPath);
            if (cwd.Id != owner.ControlId || ((int)File.GetAttributes(python) & 0x441410) != 0)
                throw new InvalidOperationException("C9W_WORKER_TOOL");
            using var pin=new FileStream(python,FileMode.Open,FileAccess.Read,FileShare.Read);
            if (Convert.ToHexString(SHA256.HashData(pin)).ToLowerInvariant() != expectedSha)
                throw new InvalidOperationException("C9W_WORKER_TOOL");
            string system=Environment.GetEnvironmentVariable("SystemRoot"), drive=Environment.GetEnvironmentVariable("SystemDrive");
            if (String.IsNullOrEmpty(system) || String.IsNullOrEmpty(drive))
                throw new InvalidOperationException("C9W_WORKER_TOOL");

            // Pipe layout: child stdin read, parent stdin write, parent stdout read,
            // child stdout write, parent stderr read, child stderr write.
            var pipes=new IntPtr[6];
            IntPtr job=IntPtr.Zero, root=IntPtr.Zero, thread=IntPtr.Zero,
                   list=IntPtr.Zero, jobList=IntPtr.Zero, handleList=IntPtr.Zero,
                   childPolicy=IntPtr.Zero, environment=IntPtr.Zero;
            bool initialized=false, assigned=false, terminalObserved=false;
            var tasks=new List<Task>();
            int code;
            try {
                job=N.CreateJobObjectW(IntPtr.Zero,null);
                if (job == IntPtr.Zero) throw Native("C9W_WORKER_JOB_CREATE");
                var limits=new N.JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
                limits.Basic.LimitFlags=0x2000|0x400|0x8; // kill on close, no breakaway, active limit
                limits.Basic.ActiveProcessLimit=1; // this worker must not spawn children
                int size=Marshal.SizeOf(limits);
                IntPtr buffer=Marshal.AllocHGlobal(size);
                try {
                    Marshal.StructureToPtr(limits,buffer,false);
                    if (!N.SetInformationJobObject(job,9,buffer,size)) throw Native("C9W_WORKER_JOB_LIMIT");
                } finally { Marshal.FreeHGlobal(buffer); }
                var initial=Account(job);
                if (initial.ActiveProcesses != 0 || initial.TotalProcesses != 0)
                    throw new InvalidOperationException("C9W_WORKER_JOB_NOT_FRESH");

                Pipe(pipes,0,true); Pipe(pipes,2,false); Pipe(pipes,4,false);
                IntPtr bytes=IntPtr.Zero;
                bool first=N.InitializeProcThreadAttributeList(IntPtr.Zero,3,0,ref bytes);
                int error=Marshal.GetLastWin32Error();
                if (first || error != 122 || bytes.ToInt64() <= 0 || bytes.ToInt64() > 1048576)
                    throw new InvalidOperationException("C9W_WORKER_ATTRIBUTE_SIZE");
                list=Marshal.AllocHGlobal(bytes);
                if (!N.InitializeProcThreadAttributeList(list,3,0,ref bytes))
                    throw Native("C9W_WORKER_ATTRIBUTE_INIT");
                initialized=true;
                jobList=Marshal.AllocHGlobal(IntPtr.Size);
                Marshal.WriteIntPtr(jobList,job);
                if (!N.UpdateProcThreadAttribute(list,0,(IntPtr)0x2000D,jobList,(IntPtr)IntPtr.Size,IntPtr.Zero,IntPtr.Zero))
                    throw Native("C9W_WORKER_ATTRIBUTE_JOB");
                handleList=Marshal.AllocHGlobal(3*IntPtr.Size);
                Marshal.WriteIntPtr(handleList,0,pipes[0]);
                Marshal.WriteIntPtr(handleList,IntPtr.Size,pipes[3]);
                Marshal.WriteIntPtr(handleList,2*IntPtr.Size,pipes[5]);
                if (!N.UpdateProcThreadAttribute(list,0,(IntPtr)0x20002,handleList,(IntPtr)(3*IntPtr.Size),IntPtr.Zero,IntPtr.Zero))
                    throw Native("C9W_WORKER_ATTRIBUTE_HANDLES");
                // PROC_THREAD_ATTRIBUTE_CHILD_PROCESS_POLICY (input DWORD, restricted=1).
                // Attribute value 0x2000E: number 14, input flag.
                childPolicy=Marshal.AllocHGlobal(sizeof(uint));
                Marshal.WriteInt32(childPolicy,1);
                if (!N.UpdateProcThreadAttribute(list,0,(IntPtr)0x2000E,childPolicy,(IntPtr)sizeof(uint),IntPtr.Zero,IntPtr.Zero))
                    throw Native("C9W_WORKER_ATTRIBUTE_CHILD_POLICY");
                var si=new N.STARTUPINFOEX {
                    cb=Marshal.SizeOf<N.STARTUPINFOEX>(),dwFlags=0x100,lpAttributeList=list,
                    hStdInput=pipes[0],hStdOutput=pipes[3],hStdError=pipes[5]
                };
                var cmd=new StringBuilder(Quote(python)+" -I -S -B -c "+Quote(script));
                environment=Marshal.StringToHGlobalUni("SystemDrive="+drive+"\0SystemRoot="+system+"\0\0");

                // Record and read back the fixed uncertainty marker BEFORE any process effect.
                // If this fails, CreateProcessW must never be reached.
                owner.MarkJournalWorkerUncertain();
                owner.AssertWatchdogCustody();
                if (!N.CreateProcessW(python,cmd,IntPtr.Zero,IntPtr.Zero,true,
                     0x80000u|0x400u|0x4u|0x08000000u,environment,owner.ControlPath,ref si,out var pi))
                    throw Native("C9W_WORKER_CREATE");
                root=pi.hProcess; thread=pi.hThread;
                if (!N.IsProcessInJob(root,job,out bool inJob)) throw Native("C9W_WORKER_JOB_QUERY");
                if (!inJob) throw new InvalidOperationException("C9W_WORKER_WRONG_JOB");
                assigned=true;
                RequireChildRestriction(root);
                initial=Account(job);
                if (initial.ActiveProcesses != 1 || initial.TotalProcesses != 1)
                    throw new InvalidOperationException("C9W_WORKER_INITIAL_COUNT");

                // Parent must not retain any child-side pipe ends, or EOF is unreliable.
                Close(ref pipes[0]); Close(ref pipes[3]); Close(ref pipes[5]);
                byte[] input=Encoding.ASCII.GetBytes("orgresource-fictional-journal-probe");
                // Transfer one handle at a time. A failed task submission disposes
                // its handle; previously scheduled tasks retain exclusive ownership.
                var stdout=TakePipe(pipes,2);
                Task<byte[]> output;
                try { output=Task.Run(() => ReadFictionalPipe(stdout)); }
                catch { stdout.Dispose(); throw; }
                tasks.Add(output);
                var stderr=TakePipe(pipes,4);
                Task<byte[]> errors;
                try { errors=Task.Run(() => ReadFictionalPipe(stderr)); }
                catch { stderr.Dispose(); throw; }
                tasks.Add(errors);
                var stdin=TakePipe(pipes,1);
                Task writer;
                try { writer=Task.Run(() => WriteFictionalPipe(stdin,input)); }
                catch { stdin.Dispose(); throw; }
                tasks.Add(writer);
                owner.AssertWatchdogCustody();
                C9Lifecycle.RequireInitialResume(N.ResumeThread(thread));
                Close(ref thread);

                var clock=Stopwatch.StartNew();
                while (true) {
                    foreach (var task in tasks)
                        if (task.IsFaulted || task.IsCanceled) task.GetAwaiter().GetResult();
                    var account=Account(job);
                    if (account.TotalProcesses != 1)
                        throw new InvalidOperationException("C9W_WORKER_EXTRA_PROCESS");
                    if (Signaled(root,0) && account.ActiveProcesses == 0 && tasks.TrueForAll(t=>t.IsCompleted)) {
                        foreach (var task in tasks) task.GetAwaiter().GetResult();
                        terminalObserved=true; break;
                    }
                    if (clock.ElapsedMilliseconds >= timeoutMillis)
                        throw new InvalidOperationException("C9W_WORKER_TIMEOUT");
                    Thread.Sleep(20);
                }
                code=Exit(root);
                var actual=output.GetAwaiter().GetResult();
                if (errors.GetAwaiter().GetResult().Length != 0 ||
                    (mode == "echo" ? code != 0 || !actual.AsSpan().SequenceEqual(input) :
                     mode == "nonzero" ? code != 23 || actual.Length != 0 :
                     mode == "child-denied" ? code != 0 ||
                        !actual.AsSpan().SequenceEqual(Encoding.ASCII.GetBytes("child-process-blocked-367")) : true))
                    throw new InvalidOperationException("C9W_WORKER_PROTOCOL");
                owner.AssertWatchdogCustody();
            } finally {
                // No process-name/PID search, no role job, no marker deletion.
                // Native cleanup failure is never converted into a positive receipt.
                bool cleanupFailed=false;
                try {
                    if (root != IntPtr.Zero && !assigned && !Signaled(root,0)) {
                        if (!N.TerminateProcess(root,139) && !Signaled(root,0)) throw Native("C9W_WORKER_ROOT_STOP");
                        if (!Signaled(root,StopMillis)) throw new InvalidOperationException("C9W_WORKER_ROOT_STOP_UNKNOWN");
                    }
                } catch { cleanupFailed=true; }
                try { if (job != IntPtr.Zero && !terminalObserved) Stop(job,root); }
                catch { cleanupFailed=true; }
                for (int i=0;i<pipes.Length;i++) {
                    try { Close(ref pipes[i]); } catch { cleanupFailed=true; }
                }
                // Closing the exact owned job is also the kill-on-close fallback.
                try { Close(ref thread); } catch { cleanupFailed=true; }
                try { Close(ref root); } catch { cleanupFailed=true; }
                try { Close(ref job); } catch { cleanupFailed=true; }
                if (initialized) N.DeleteProcThreadAttributeList(list);
                foreach (var p in new[]{list,jobList,handleList,childPolicy,environment})
                    if (p != IntPtr.Zero) Marshal.FreeHGlobal(p);
                Task combined=Task.WhenAll(tasks);
                try { combined.WaitAsync(TimeSpan.FromSeconds(5)).GetAwaiter().GetResult(); }
                catch { if (!tasks.TrueForAll(t=>t.IsCompleted)) cleanupFailed=true; }
                // Observe late faults, but do not count them as cleanup evidence.
                _=combined.ContinueWith(t=>{ _=t.Exception; },CancellationToken.None,
                    TaskContinuationOptions.OnlyOnFaulted|TaskContinuationOptions.ExecuteSynchronously,TaskScheduler.Default);
                if (cleanupFailed) throw new InvalidOperationException("C9W_WORKER_CLEANUP_UNKNOWN");
            }
            // Marker remains present even on a fully observed fictional exchange.
            return code;
        }
    }

}

public sealed partial class C9ControlLock {
    internal void AssertWatchdogCustody() { CheckCustody(); }
}

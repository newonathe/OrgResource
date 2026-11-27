using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Win32.SafeHandles;

// H06 fixed fictional ConPTY probe only. No editor, actual secret, SQL or role
// transfer. Outer independent supervisor and native acceptance still required;
// this is not a hard deadline for synchronous kernel calls or host failure.
public static partial class C9OwnedJobProbe {
    const string PtyPrompt = "C9W_FIX_PROMPT_51A";
    const string PtyDone = "C9W_FIX_DONE_82B";
    const int PtyOutputLimit = 8192;

    [StructLayout(LayoutKind.Sequential)]
    struct PtyCoord { public short X,Y; }
    [DllImport("kernel32.dll",ExactSpelling=true)]
    static extern int CreatePseudoConsole(PtyCoord size,IntPtr input,IntPtr output,uint flags,out IntPtr console);
    [DllImport("kernel32.dll",ExactSpelling=true)]
    static extern void ClosePseudoConsole(IntPtr console);

    static string PtyScript(string mode) {
        switch(mode) {
            case "normal": return "import sys; print('"+PtyPrompt+"',flush=True); v=input(); print('"+PtyDone+"' if v=='fictional-reply' else 'WRONG',flush=True); raise SystemExit(0 if v=='fictional-reply' else 25)";
            case "duplicate": return "import time; print('"+PtyPrompt+"',flush=True); print('"+PtyPrompt+"',flush=True); time.sleep(120)";
            case "no-prompt": return "raise SystemExit(0)";
            case "hang": return "import time; time.sleep(120)";
            default: throw new InvalidOperationException("C9W_PTY_MODE");
        }
    }

    static int CountPtyMarker(string text,string marker) {
        int count=0, at=0;
        while ((at=text.IndexOf(marker,at,StringComparison.Ordinal))>=0) {
            count++; at+=marker.Length;
        }
        return count;
    }

    static byte[] DrainPty(SafeFileHandle handle,TaskCompletionSource<bool> prompt) {
        try {
            using(handle)
            using(var stream=new FileStream(handle,FileAccess.Read,4096,false))
            using(var buffer=new MemoryStream()) {
                byte[] chunk=new byte[128];
                int n;
                while ((n=stream.Read(chunk,0,chunk.Length))!=0) {
                    if (buffer.Length+n>PtyOutputLimit)
                        throw new InvalidOperationException("C9W_PTY_OUTPUT_LIMIT");
                    buffer.Write(chunk,0,n);
                    // Markers are fixed ASCII. Search accumulated bytes, not individual
                    // reads. Do not print output or interpret arbitrary terminal escapes.
                    string text=Encoding.ASCII.GetString(buffer.GetBuffer(),0,(int)buffer.Length);
                    int prompts=CountPtyMarker(text,PtyPrompt);
                    if (prompts>1) throw new InvalidOperationException("C9W_PTY_DUPLICATE_PROMPT");
                    if (prompts==1) prompt.TrySetResult(true);
                }
                return buffer.ToArray();
            }
        } finally { prompt.TrySetResult(false); }
    }
    static void SendPty(SafeFileHandle handle,TaskCompletionSource<bool> prompt) {
        using(handle)
        using(var stream=new FileStream(handle,FileAccess.Write,4096,false)) {
            if (!prompt.Task.GetAwaiter().GetResult()) return;
            byte[] bytes=Encoding.ASCII.GetBytes("fictional-reply\r");
            stream.Write(bytes,0,bytes.Length);
            stream.Flush();
        }
    }
    static Task<T> PtyThread<T>(Func<T> action) {
        return Task.Factory.StartNew(action,CancellationToken.None,TaskCreationOptions.LongRunning,TaskScheduler.Default);
    }
    static Task PtyThread(Action action) {
        return Task.Factory.StartNew(action,CancellationToken.None,TaskCreationOptions.LongRunning,TaskScheduler.Default);
    }
    static void ObservePty(Task task) {
        if (task!=null && (task.IsFaulted || task.IsCanceled)) task.GetAwaiter().GetResult();
    }

    // Private until a separately reviewed independent outer watchdog calls it.
    static int RunFictionalConPty(C9ControlLock owner,byte[] parent,byte[] identity,string python,string expectedSha,string mode,int timeoutMillis,PtyChildExchange exchange) {
        string script=PtyScript(mode);
        if (!OperatingSystem.IsWindows() || IntPtr.Size!=8 || owner==null || exchange==null ||
            timeoutMillis<1000 || timeoutMillis>10000 ||
            expectedSha==null || !System.Text.RegularExpressions.Regex.IsMatch(expectedSha,@"\A[a-f0-9]{64}\z") ||
            String.IsNullOrEmpty(python) || !Path.IsPathFullyQualified(python) ||
            Path.GetFullPath(python)!=python || !String.Equals(Path.GetFileName(python),"python.exe",StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_PTY_ARGUMENT");
        lock(owner) {
            owner.AssertPtyChildBinding(parent,identity);
            using var directory=C9DirectoryIdentity.Open(Path.GetDirectoryName(python));
            using var cwd=C9DirectoryIdentity.Open(owner.ControlPath);
            if (cwd.Id!=owner.ControlId || ((int)File.GetAttributes(python)&0x441410)!=0)
                throw new InvalidOperationException("C9W_PTY_TOOL");
            using var pin=new FileStream(python,FileMode.Open,FileAccess.Read,FileShare.Read);
            if (Convert.ToHexString(SHA256.HashData(pin)).ToLowerInvariant()!=expectedSha)
                throw new InvalidOperationException("C9W_PTY_TOOL");
            string system=Environment.GetEnvironmentVariable("SystemRoot"),drive=Environment.GetEnvironmentVariable("SystemDrive");
            if (String.IsNullOrEmpty(system)||String.IsNullOrEmpty(drive)) throw new InvalidOperationException("C9W_PTY_TOOL");

            var pipes=new IntPtr[4]; // console input read, host input write, host output read, console output write
            IntPtr hp=IntPtr.Zero,job=IntPtr.Zero,root=IntPtr.Zero,thread=IntPtr.Zero,
                   list=IntPtr.Zero,jobs=IntPtr.Zero,policy=IntPtr.Zero,env=IntPtr.Zero;
            bool initialized=false,assigned=false,settled=false;
            var prompt=new TaskCompletionSource<bool>(TaskCreationOptions.RunContinuationsAsynchronously);
            Task<byte[]> reader=null;
            Task writer=null,closer=null;
            var clock=Stopwatch.StartNew();
            // Transfer HPCON ownership to a distinct close thread exactly once.
            void BeginClose() {
                if (hp==IntPtr.Zero) return;
                IntPtr owned=hp;
                closer=PtyThread(()=>ClosePseudoConsole(owned));
                hp=IntPtr.Zero;
            }

            try {
                job=N.CreateJobObjectW(IntPtr.Zero,null);
                if(job==IntPtr.Zero) throw Native("C9W_PTY_JOB");
                var limits=new N.JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
                limits.Basic.LimitFlags=0x2000|0x400|0x8;
                limits.Basic.ActiveProcessLimit=1;
                int sz=Marshal.SizeOf(limits);
                IntPtr buf=Marshal.AllocHGlobal(sz);
                try {
                    Marshal.StructureToPtr(limits,buf,false);
                    if(!N.SetInformationJobObject(job,9,buf,sz)) throw Native("C9W_PTY_JOB_LIMIT");
                } finally { Marshal.FreeHGlobal(buf); }
                var empty=Account(job);
                if(empty.ActiveProcesses!=0||empty.TotalProcesses!=0) throw new InvalidOperationException("C9W_PTY_JOB_NOT_FRESH");
                var sa=new N.SECURITY_ATTRIBUTES{nLength=Marshal.SizeOf<N.SECURITY_ATTRIBUTES>(),bInheritHandle=0};
                if(!CreatePipe(out pipes[0],out pipes[1],ref sa,4096)||
                   !CreatePipe(out pipes[2],out pipes[3],ref sa,4096)) throw Native("C9W_PTY_PIPE");
                foreach(var h in pipes)
                    if(!GetHandleInformation(h,out uint flags)||(flags&1)!=0) throw Native("C9W_PTY_PIPE_INHERITANCE");

                // The child identity and uncertainty markers were retained before
                // entry. Recheck both immediately before the pseudoconsole effect.
                owner.AssertPtyChildBinding(parent,identity);
                owner.AssertWatchdogCustody();
                int hr=CreatePseudoConsole(new PtyCoord{X=80,Y=25},pipes[0],pipes[3],0,out hp);
                if(hr!=0||hp==IntPtr.Zero) throw new InvalidOperationException("C9W_PTY_CREATE");
                // Start draining before CreateProcess; never put a synchronous read
                // or close on the supervising thread.
                var output=TakePipe(pipes,2);
                try { reader=PtyThread(()=>DrainPty(output,prompt)); }
                catch { output.Dispose(); throw; }
                var input=TakePipe(pipes,1);
                try { writer=PtyThread(()=>SendPty(input,prompt)); }
                catch { input.Dispose(); throw; }

                IntPtr bytes=IntPtr.Zero;
                bool first=N.InitializeProcThreadAttributeList(IntPtr.Zero,3,0,ref bytes);
                int error=Marshal.GetLastWin32Error();
                if(first||error!=122||bytes.ToInt64()<=0||bytes.ToInt64()>1048576)
                    throw new InvalidOperationException("C9W_PTY_ATTRIBUTE_SIZE");
                list=Marshal.AllocHGlobal(bytes);
                if(!N.InitializeProcThreadAttributeList(list,3,0,ref bytes)) throw Native("C9W_PTY_ATTRIBUTE_INIT");
                initialized=true;
                jobs=Marshal.AllocHGlobal(IntPtr.Size); Marshal.WriteIntPtr(jobs,job);
                policy=Marshal.AllocHGlobal(sizeof(uint)); Marshal.WriteInt32(policy,1);
                if(!N.UpdateProcThreadAttribute(list,0,(IntPtr)0x2000D,jobs,(IntPtr)IntPtr.Size,IntPtr.Zero,IntPtr.Zero)||
                   !N.UpdateProcThreadAttribute(list,0,(IntPtr)0x2000E,policy,(IntPtr)sizeof(uint),IntPtr.Zero,IntPtr.Zero)||
                   !N.UpdateProcThreadAttribute(list,0,(IntPtr)0x20016,hp,(IntPtr)IntPtr.Size,IntPtr.Zero,IntPtr.Zero))
                    throw Native("C9W_PTY_ATTRIBUTE");
                var si=new N.STARTUPINFOEX{cb=Marshal.SizeOf<N.STARTUPINFOEX>(),lpAttributeList=list};
                var cmd=new StringBuilder(Quote(python)+" -I -S -B -c "+Quote(script));
                env=Marshal.StringToHGlobalUni("SystemDrive="+drive+"\0SystemRoot="+system+"\0\0");
                owner.AssertWatchdogCustody();
                if(clock.ElapsedMilliseconds>=timeoutMillis) throw new InvalidOperationException("C9W_PTY_TIMEOUT");
                // No ambient handle inheritance or CREATE_NO_WINDOW; the explicit
                // pseudoconsole attribute attaches the character-mode client.
                if(!N.CreateProcessW(python,cmd,IntPtr.Zero,IntPtr.Zero,false,0x80000u|0x400u|0x4u,
                   env,owner.ControlPath,ref si,out var pi)) throw Native("C9W_PTY_CLIENT_CREATE");
                root=pi.hProcess; thread=pi.hThread;
                if(!N.IsProcessInJob(root,job,out bool inJob)||!inJob) throw new InvalidOperationException("C9W_PTY_WRONG_JOB");
                assigned=true; RequireChildRestriction(root);
                var initial=Account(job);
                if(initial.ActiveProcesses!=1||initial.TotalProcesses!=1) throw new InvalidOperationException("C9W_PTY_INITIAL_COUNT");
                // Original suspended client stays held here until the outer
                // supervisor duplicates, validates and acknowledges that object.
                exchange.Publish(root,clock,timeoutMillis);
                Close(ref pipes[0]); Close(ref pipes[3]);
                if(clock.ElapsedMilliseconds>=timeoutMillis) throw new InvalidOperationException("C9W_PTY_TIMEOUT");
                C9Lifecycle.RequireInitialResume(N.ResumeThread(thread)); Close(ref thread);
                while(true) {
                    ObservePty(reader); ObservePty(writer); ObservePty(closer);
                    var current=Account(job);
                    if(current.TotalProcesses!=1) throw new InvalidOperationException("C9W_PTY_EXTRA_PROCESS");
                    if(Signaled(root,0)&&current.ActiveProcesses==0) {
                        prompt.TrySetResult(false);
                        BeginClose();
                        if(reader.IsCompleted&&writer.IsCompleted&&closer!=null&&closer.IsCompleted) {
                            ObservePty(reader); ObservePty(writer); ObservePty(closer);
                            settled=true; break;
                        }
                    }
                    if(clock.ElapsedMilliseconds>=timeoutMillis) throw new InvalidOperationException("C9W_PTY_TIMEOUT");
                    Thread.Sleep(20);
                }
                string transcript=Encoding.ASCII.GetString(reader.GetAwaiter().GetResult());
                if(mode!="normal"||Exit(root)!=0||CountPtyMarker(transcript,PtyPrompt)!=1||
                   CountPtyMarker(transcript,PtyDone)!=1||
                   transcript.IndexOf(PtyPrompt,StringComparison.Ordinal)>=transcript.IndexOf(PtyDone,StringComparison.Ordinal))
                    throw new InvalidOperationException("C9W_PTY_PROTOCOL");
                owner.AssertWatchdogCustody();
                return 0;
            } finally {
                // Refuse on uncertainty; retain marker. Separate cleanup grace
                // is not a hard kernel deadline. Independent outer watchdog pending.
                prompt.TrySetResult(false);
                bool failed=false;
                try {
                    if(root!=IntPtr.Zero&&!assigned&&!Signaled(root,0)) {
                        if(!N.TerminateProcess(root,139)&&!Signaled(root,0)) throw Native("C9W_PTY_ROOT_STOP");
                        if(!Signaled(root,StopMillis)) throw new InvalidOperationException("C9W_PTY_ROOT_UNKNOWN");
                    }
                } catch { failed=true; }
                try { if(job!=IntPtr.Zero&&!settled) Stop(job,root); } catch { failed=true; }
                for(int i=0;i<pipes.Length;i++) try { Close(ref pipes[i]); } catch { failed=true; }
                try { BeginClose(); } catch { failed=true; }
                try { Close(ref thread); } catch { failed=true; }
                try { Close(ref root); } catch { failed=true; }
                try { Close(ref job); } catch { failed=true; }
                if(initialized) N.DeleteProcThreadAttributeList(list);
                foreach(var p in new[]{list,jobs,policy,env}) if(p!=IntPtr.Zero) Marshal.FreeHGlobal(p);
                var tasks=new System.Collections.Generic.List<Task>();
                if(reader!=null) tasks.Add(reader);
                if(writer!=null) tasks.Add(writer);
                if(closer!=null) tasks.Add(closer);
                Task all=Task.WhenAll(tasks);
                try { all.WaitAsync(TimeSpan.FromSeconds(5)).GetAwaiter().GetResult(); }
                catch { if(!tasks.TrueForAll(t=>t.IsCompleted)) failed=true; }
                _=all.ContinueWith(t=>{_=t.Exception;},CancellationToken.None,
                    TaskContinuationOptions.OnlyOnFaulted|TaskContinuationOptions.ExecuteSynchronously,TaskScheduler.Default);
                if(failed) throw new InvalidOperationException("C9W_PTY_CLEANUP_UNKNOWN");
            }
        }
    }
}

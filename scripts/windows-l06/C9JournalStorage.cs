using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Principal;
using System.Text;
using Microsoft.Win32.SafeHandles;

// H03 in-process storage; no child can publish journal state after the lock holder dies.
// The caller must validate the journal schema and transition before calling PublishSnapshot.
// All changes remain under a verified native lock; no generic file/path write interface.
public sealed partial class C9ControlLock {
    const int MaxJournal = 16384;
    bool journalWorkerUncertain;
    const string WorkerMarker = "ownership.worker-uncertain.tmp";

    // The same held lock shares refusal across parent instances.
    // Reopening refuses the retained staging marker through RefuseStaging.
    internal void AssertJournalWorkerUsable() {
        CheckCustody();
        if (journalWorkerUncertain)
            throw new InvalidOperationException("C9W_JOURNAL_WORKER_QUARANTINED");
        RefuseStaging();
    }

    internal void MarkJournalWorkerUncertain() {
        journalWorkerUncertain = true;
        CheckCustody();
        WriteRetainedWorkerMarker(WorkerMarker,Encoding.ASCII.GetBytes("C9W_JOURNAL_WORKER_UNCERTAIN\n"));
    }

    const string PtyParentMarker = "ownership.pty-binding.tmp";
    const string PtyChildMarker = "ownership.pty-child.tmp";
    const string PtyResultMarker = "ownership.pty-result.tmp";
    const string PtyOuterMarker = "ownership.pty-outer.tmp";
    const string PtyCrossMarker = "ownership.pty-crosscheck.tmp";

    void RequirePtyUncertainty() {
        CheckCustody();
        if (!SameSnapshot(ReadJournalFile(Path.Combine(ControlPath,WorkerMarker),false),
            Encoding.ASCII.GetBytes("C9W_JOURNAL_WORKER_UNCERTAIN\n")))
            throw new InvalidOperationException("C9W_PTY_BINDING_NO_INTENT");
    }
    void RequirePtyParent(byte[] record) {
        var f=C9PtyRecords.ReadParent(record);
        RequirePtyUncertainty();
        if (f[0]!=RootId||f[1]!=ControlId||f[2]!=Token)
            throw new InvalidOperationException("C9W_PTY_BINDING_IDENTITY");
    }
    internal void MarkPtyParentBinding(byte[] record) {
        if (!journalWorkerUncertain) throw new InvalidOperationException("C9W_PTY_BINDING_NO_INTENT");
        RequirePtyParent(record);
        WriteRetainedWorkerMarker(PtyParentMarker,record);
    }

    // Read parent intent without reopening its exclusive ownership.lock.
    internal static void VerifyPtyParentBinding(string root,byte[] record) {
        var f=C9PtyRecords.ReadParent(record);
        using var me=WindowsIdentity.GetCurrent();
        using var rootHeld=C9DirectoryIdentity.Open(root,true);
        string parent=Path.Combine(root,"orgresource-c9w-control-"+f[2]);
        using var parentHeld=C9DirectoryIdentity.Open(parent,true);
        AssertOwnerOnly(rootHeld.FinalHandle,me.User);
        AssertOwnerOnly(parentHeld.FinalHandle,me.User);
        if (rootHeld.Id!=f[0]||parentHeld.Id!=f[1])
            throw new InvalidOperationException("C9W_PTY_BINDING_IDENTITY");
        if (!SameSnapshot(ReadJournalFileBound(Path.Combine(parent,WorkerMarker),false,f[1]),
              Encoding.ASCII.GetBytes("C9W_JOURNAL_WORKER_UNCERTAIN\n")) ||
            !SameSnapshot(ReadJournalFileBound(Path.Combine(parent,PtyParentMarker),false,f[1]),record))
            throw new InvalidOperationException("C9W_PTY_BINDING_MISMATCH");
    }

    internal byte[] MarkPtyChildBinding(byte[] parent) {
        AssertJournalWorkerUsable();
        var f=C9PtyRecords.ReadParent(parent);
        if(RootId!=f[0]||Token!=f[3]) throw new InvalidOperationException("C9W_PTY_CHILD_IDENTITY");
        byte[] child=C9PtyRecords.Child(parent,ControlId);
        MarkJournalWorkerUncertain();
        WriteRetainedWorkerMarker(PtyChildMarker,child);
        return child;
    }

    internal void AssertPtyChildBinding(byte[] parent,byte[] child) {
        RequirePtyUncertainty();
        var f=C9PtyRecords.ReadParent(parent);
        if(RootId!=f[0]||Token!=f[3]||ControlId!=C9PtyRecords.ReadChild(parent,child)||
           !SameSnapshot(ReadJournalFile(Path.Combine(ControlPath,PtyChildMarker),false),child))
            throw new InvalidOperationException("C9W_PTY_CHILD_IDENTITY");
    }

    internal void MarkPtyChildResult(byte[] parent,byte[] child,bool returned) {
        AssertPtyChildBinding(parent,child);
        WriteRetainedWorkerMarker(PtyResultMarker,C9PtyRecords.ChildResult(parent,child,returned));
    }

    // Called only after held outer root signal/accounting and handle cleanup.
    // Record the outer observation even if child proof is missing or invalid.
    internal void RecordPtyOuter(byte[] parent,byte[] outer) {
        RequirePtyParent(parent);
        C9PtyRecords.VerifyOuter(parent,outer);
        if (!SameSnapshot(ReadJournalFile(Path.Combine(ControlPath,PtyParentMarker),false),parent))
            throw new InvalidOperationException("C9W_PTY_BINDING_MISMATCH");
        WriteRetainedWorkerMarker(PtyOuterMarker,outer);
    }

    // No recovery execution, deletion, retry or marker removal. Only a
    // distinct child token from the retained parent record is opened.
    internal void CrossCheckPtyChild(byte[] parent,byte[] outer) {
        RequirePtyParent(parent);
        if (!SameSnapshot(ReadJournalFile(Path.Combine(ControlPath,PtyParentMarker),false),parent)||
            !SameSnapshot(ReadJournalFile(Path.Combine(ControlPath,PtyOuterMarker),false),outer))
            throw new InvalidOperationException("C9W_PTY_BINDING_MISMATCH");
        var f=C9PtyRecords.ReadParent(parent);
        using var child=OpenExisting(Root,f[3]);
        byte[] identity=child.ReadJournalFile(Path.Combine(child.ControlPath,PtyChildMarker),false);
        child.AssertPtyChildBinding(parent,identity);
        byte[] result=child.ReadJournalFile(Path.Combine(child.ControlPath,PtyResultMarker),false);
        WriteRetainedWorkerMarker(PtyCrossMarker,C9PtyRecords.CrossCheck(parent,identity,result,outer));
    }

    internal void AssertPtyPackageDirectory(C9DirectoryIdentity dir) {
        CheckCustody();
        using var me=WindowsIdentity.GetCurrent();
        AssertOwnerOnly(dir.FinalHandle,me.User);
    }

    internal FileStream OpenPtyPinnedFile(string path,string directoryId,bool ownerOnly) {
        CheckCustody();
        // OPEN_EXISTING | OPEN_REPARSE_POINT; read and read-control with read-only
        // sharing; native handle shape and final path, not a pre-open path check.
        var h=CreateFileExisting(path,0x80020080,1,IntPtr.Zero,3,0x00200080,IntPtr.Zero);
        try {
            if (h.IsInvalid) throw new Win32Exception(Marshal.GetLastWin32Error(),"C9W_PTY_PIN_OPEN");
            CheckJournalFileBound(h,path,directoryId);
            if (ownerOnly) {
                using var me=WindowsIdentity.GetCurrent();
                AssertOwnerOnly(h,me.User);
            }
            return new FileStream(h,FileAccess.Read);
        } catch { h.Dispose(); throw; }
    }

    void WriteRetainedWorkerMarker(string leaf,byte[] data) {
        string path=Path.Combine(ControlPath,leaf);
        using var me = WindowsIdentity.GetCurrent();
        WithSecurity(me.User,false,sa => {
            using var h = CreateFileNew(path,0xC0020080,0,ref sa,1,0x00200080,IntPtr.Zero);
            if (h.IsInvalid)
                throw new Win32Exception(Marshal.GetLastWin32Error(),"C9W_JOURNAL_WORKER_MARKER_CREATE");
            AssertOwnerOnly(h,me.User);
            CheckJournalFile(h,path);
            using var stream = new FileStream(h,FileAccess.Write);
            stream.Write(data,0,data.Length);
            stream.Flush(true);
        });
        CheckCustody();
        if (!SameSnapshot(ReadJournalFile(path,false),data))
            throw new InvalidOperationException("C9W_JOURNAL_WORKER_MARKER_READBACK");
        // Never automatically remove or overwrite this evidence.
    }
    string JournalPath { get { return Path.Combine(ControlPath, "ownership.json"); } }

    void CheckCustody() {
        AssertHeld();
        using var me = WindowsIdentity.GetCurrent();
        AssertOwnerOnly(rootHandle.FinalHandle, me.User);
        AssertOwnerOnly(controlHandle.FinalHandle, me.User);
        AssertOwnerOnly(lockHandle, me.User);
        CheckLockFile(Path.Combine(ControlPath, "ownership.lock"));
    }

    void RefuseStaging() {
        foreach (var path in Directory.EnumerateFileSystemEntries(ControlPath))
            if (System.Text.RegularExpressions.Regex.IsMatch(Path.GetFileName(path),
                @"\Aownership\..*\.tmp\z", System.Text.RegularExpressions.RegexOptions.IgnoreCase))
                throw new InvalidOperationException("C9W_JOURNAL_STAGING_AMBIGUOUS");
    }

    internal byte[] ReadSnapshot() {
        AssertJournalWorkerUsable();
        return ReadJournalFile(JournalPath, true);
    }

    byte[] ReadJournalFile(string path, bool allowMissing) {
        return ReadJournalFileBound(path,allowMissing,ControlId);
    }
    static byte[] ReadJournalFileBound(string path,bool allowMissing,string controlId) {
        using var h = CreateFileExisting(path, 0x80020080, 0, IntPtr.Zero, 3, 0x00200080, IntPtr.Zero);
        if (h.IsInvalid) {
            int error = Marshal.GetLastWin32Error();
            if (allowMissing && error == 2) return null; // ERROR_FILE_NOT_FOUND only
            throw new Win32Exception(error, "C9W_JOURNAL_OPEN");
        }
        using var me = WindowsIdentity.GetCurrent();
        AssertOwnerOnly(h, me.User);
        CheckJournalFileBound(h,path,controlId);
        using var stream = new FileStream(h, FileAccess.Read);
        long size = stream.Length;
        if (size <= 0 || size > MaxJournal) throw new InvalidOperationException("C9W_JOURNAL_SIZE");
        var bytes = new byte[(int)size];
        stream.ReadExactly(bytes);
        if (stream.ReadByte() != -1 || stream.Length != size)
            throw new InvalidOperationException("C9W_JOURNAL_SIZE");
        return bytes;
    }

    void CheckJournalFile(SafeFileHandle h, string path) {
        CheckJournalFileBound(h,path,ControlId);
    }
    static void CheckJournalFileBound(SafeFileHandle h,string path,string controlId) {
        byte[] tag = new byte[8], standard = new byte[24], id = new byte[24];
        if (!GetFileInformationByHandleEx(h, 9, tag, (uint)tag.Length) ||
            !GetFileInformationByHandleEx(h, 1, standard, (uint)standard.Length) ||
            !GetFileInformationByHandleEx(h, 18, id, (uint)id.Length))
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_JOURNAL_INFO");
        if ((BitConverter.ToUInt32(tag,0) & 0x441410) != 0 ||
            BitConverter.ToUInt32(standard,16) != 1 || standard[20] != 0 || standard[21] != 0 ||
            BitConverter.ToUInt64(id,0).ToString("x16") != controlId.Substring(0,16))
            throw new InvalidOperationException("C9W_JOURNAL_SHAPE");
        var name = new StringBuilder(32768);
        uint count = GetFinalPathNameByHandleW(h, name, (uint)name.Capacity, 0);
        if (count == 0 || count >= name.Capacity ||
            !String.Equals(name.ToString(), @"\\?\" + path, StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_JOURNAL_PATH");
    }

    static bool SameSnapshot(byte[] a, byte[] b) {
        if (a == null || b == null) return a == null && b == null;
        return a.AsSpan().SequenceEqual(b);
    }

    internal void PublishSnapshot(byte[] expected, byte[] replacement) {
        if (replacement == null || replacement.Length == 0 || replacement.Length > MaxJournal ||
            (expected != null && (expected.Length == 0 || expected.Length > MaxJournal)))
            throw new InvalidOperationException("C9W_JOURNAL_SIZE");
        // Serialize through this object's lock. Native parent is single-threaded.
        if (!SameSnapshot(ReadSnapshot(), expected))
            throw new InvalidOperationException("C9W_JOURNAL_STALE");
        string tmp = Path.Combine(ControlPath, "ownership." + Guid.NewGuid().ToString("N") + ".tmp");
        using var me = WindowsIdentity.GetCurrent();
        // Exclusive protected staging, never overwrite a pre-existing file.
        WithSecurity(me.User, false, sa => {
            using var h = CreateFileNew(tmp, 0xC0020080, 0, ref sa, 1, 0x00200080, IntPtr.Zero);
            if (h.IsInvalid) throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_JOURNAL_STAGE");
            AssertOwnerOnly(h, me.User);
            CheckJournalFile(h, tmp);
            using var stream = new FileStream(h, FileAccess.Write);
            stream.Write(replacement, 0, replacement.Length);
            stream.Flush(true);
        });
        // Preserve the staging file on every failure; ambiguous evidence needs owner reconciliation.
        CheckCustody();
        if (!SameSnapshot(ReadJournalFile(tmp, false), replacement) ||
            !SameSnapshot(ReadJournalFile(JournalPath, true), expected))
            throw new InvalidOperationException("C9W_JOURNAL_STALE");
        // Same directory, never COPY_ALLOWED. Initial publication cannot replace an existing name.
        // WRITE_THROUGH is requested; real NTFS crash/power-loss durability remains unverified.
        if (!MoveFileExW(tmp, JournalPath, 8u | (expected == null ? 0u : 1u)))
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_JOURNAL_PUBLISH");
        if (!SameSnapshot(ReadSnapshot(), replacement))
            throw new InvalidOperationException("C9W_JOURNAL_READBACK");
    }

    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool MoveFileExW(string from, string to, uint flags);
}

// Pure canonical records used by the private fictional helper. These are
// observations only, never cleanup authorization or recovery execution.
public static class C9PtyRecords {
    static void Need(bool ok) {
        if(!ok) throw new InvalidOperationException("C9W_PTY_RECORD");
    }
    static bool Match(string s,string pattern) {
        return s!=null && System.Text.RegularExpressions.Regex.IsMatch(s,pattern);
    }
    static bool Token(string s) { return Match(s,@"\A[a-f0-9]{12}\z"); }
    static bool Id(string s) { return Match(s,@"\A[a-f0-9]{16}:[a-f0-9]{32}\z"); }
    static bool Hash(string s) { return Match(s,@"\A[a-f0-9]{64}\z"); }
    static bool Mode(string s) { return s=="normal"||s=="duplicate"||s=="no-prompt"||s=="hang"; }
    static bool DistinctSameVolume(string a,string b) {
        return Id(a)&&Id(b)&&a!=b&&a.Substring(0,16)==b.Substring(0,16);
    }
    static byte[] Pack(string header,params string[] fields) {
        return Encoding.ASCII.GetBytes(header+"\n"+String.Join("\n",fields)+"\n");
    }
    static string[] Unpack(byte[] bytes,string header,int count) {
        Need(bytes!=null&&bytes.Length>0&&bytes.Length<=1024);
        foreach(byte b in bytes) Need(b==10||(b>=33&&b<=126));
        string[] lines=Encoding.ASCII.GetString(bytes).Split('\n');
        Need(lines.Length==count+2&&lines[0]==header&&lines[lines.Length-1]=="");
        string[] fields=new string[count];
        Array.Copy(lines,1,fields,0,count);
        return fields;
    }
    public static string Digest(byte[] bytes) {
        Need(bytes!=null&&bytes.Length>0&&bytes.Length<=1024);
        return Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(bytes)).ToLowerInvariant();
    }
    public static byte[] Parent(string rootId,string parentId,string parentToken,string childToken,
                                string mode,string package,string argv) {
        Need(DistinctSameVolume(rootId,parentId)&&Token(parentToken)&&Token(childToken)&&
            parentToken!=childToken&&Mode(mode)&&Hash(package)&&Hash(argv));
        return Pack("C9W_PTY_PARENT_V2",rootId,parentId,parentToken,childToken,mode,package,argv);
    }
    public static string[] ReadParent(byte[] bytes) {
        var f=Unpack(bytes,"C9W_PTY_PARENT_V2",7);
        Need(bytes.AsSpan().SequenceEqual(Parent(f[0],f[1],f[2],f[3],f[4],f[5],f[6])));
        return f;
    }
    public static byte[] Child(byte[] parent,string childId) {
        var f=ReadParent(parent);
        Need(DistinctSameVolume(f[0],childId)&&DistinctSameVolume(f[1],childId));
        return Pack("C9W_PTY_CHILD_V1",Digest(parent),f[3],childId);
    }
    public static string ReadChild(byte[] parent,byte[] child) {
        var f=Unpack(child,"C9W_PTY_CHILD_V1",3);
        Need(child.AsSpan().SequenceEqual(Child(parent,f[2])));
        return f[2];
    }
    public static byte[] ChildResult(byte[] parent,byte[] child,bool returned) {
        _=ReadChild(parent,child);
        Need(!returned||ReadParent(parent)[4]=="normal");
        return Pack("C9W_PTY_CHILD_RESULT_V1",Digest(child),returned?"RETURNED":"REFUSED");
    }
    public static bool ReadChildResult(byte[] parent,byte[] child,byte[] result) {
        var f=Unpack(result,"C9W_PTY_CHILD_RESULT_V1",2);
        Need(f[1]=="RETURNED"||f[1]=="REFUSED");
        bool returned=f[1]=="RETURNED";
        Need(result.AsSpan().SequenceEqual(ChildResult(parent,child,returned)));
        return returned;
    }
    static string Number(int value) { return value.ToString(System.Globalization.CultureInfo.InvariantCulture); }
    public static byte[] Outer(byte[] parent,bool timeout,bool rootExited,int exit,int before,int total,int after) {
        _=ReadParent(parent);
        Need(total>=1&&before>=0&&before<=total&&after==0&&
            (timeout||(rootExited&&before==0)));
        return Pack("C9W_PTY_OUTER_OBSERVED_V1",Digest(parent),timeout?"1":"0",rootExited?"1":"0",
            Number(exit),Number(before),Number(total),Number(after));
    }
    public static void VerifyOuter(byte[] parent,byte[] outer) {
        var f=Unpack(outer,"C9W_PTY_OUTER_OBSERVED_V1",7);
        Need((f[1]=="0"||f[1]=="1")&&(f[2]=="0"||f[2]=="1"));
        int[] n=new int[4];
        for(int i=0;i<4;i++) Need(Int32.TryParse(f[i+3],System.Globalization.NumberStyles.AllowLeadingSign,
            System.Globalization.CultureInfo.InvariantCulture,out n[i]));
        Need(outer.AsSpan().SequenceEqual(Outer(parent,f[1]=="1",f[2]=="1",n[0],n[1],n[2],n[3])));
    }
    public static byte[] CrossCheck(byte[] parent,byte[] child,byte[] result,byte[] outer) {
        bool returned=ReadChildResult(parent,child,result);
        VerifyOuter(parent,outer);
        var f=Unpack(outer,"C9W_PTY_OUTER_OBSERVED_V1",7);
        // For a timely completion, exit zero iff the child reports success.
        // Timeout can occur after child writes result, before root exits.
        Need(f[1]=="1"||((f[3]=="0")==returned));
        return Pack("C9W_PTY_CROSSCHECK_OBSERVED_V1",Digest(parent),Digest(child),Digest(result),Digest(outer));
    }
}

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
        string path = Path.Combine(ControlPath,WorkerMarker);
        byte[] data = Encoding.ASCII.GetBytes("C9W_JOURNAL_WORKER_UNCERTAIN\n");
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
        using var h = CreateFileExisting(path, 0x80020080, 0, IntPtr.Zero, 3, 0x00200080, IntPtr.Zero);
        if (h.IsInvalid) {
            int error = Marshal.GetLastWin32Error();
            if (allowMissing && error == 2) return null; // ERROR_FILE_NOT_FOUND only
            throw new Win32Exception(error, "C9W_JOURNAL_OPEN");
        }
        using var me = WindowsIdentity.GetCurrent();
        AssertOwnerOnly(h, me.User);
        CheckJournalFile(h, path);
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
        byte[] tag = new byte[8], standard = new byte[24], id = new byte[24];
        if (!GetFileInformationByHandleEx(h, 9, tag, (uint)tag.Length) ||
            !GetFileInformationByHandleEx(h, 1, standard, (uint)standard.Length) ||
            !GetFileInformationByHandleEx(h, 18, id, (uint)id.Length))
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_JOURNAL_INFO");
        if ((BitConverter.ToUInt32(tag,0) & 0x441410) != 0 ||
            BitConverter.ToUInt32(standard,16) != 1 || standard[20] != 0 || standard[21] != 0 ||
            BitConverter.ToUInt64(id,0).ToString("x16") != ControlId.Substring(0,16))
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

using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Principal;
using System.Text;
using Microsoft.Win32.SafeHandles;

// H06 fictional, host-side slot only. No credential read/write, editor,
// SQL, ACL transition to a role, publication, revoke or deletion. A vacancy
// observation is not an atomic reservation of the filename against same-user
// writers. Keep the parent pinned; the real owned editor/job and journal
// integration are still prerequisites. Never target a user's profile login file.
public sealed partial class C9ControlLock {
    public sealed class FictionalLoginSlot : IDisposable {
        readonly C9ControlLock control;
        readonly C9DirectoryIdentity directory;
        readonly string id;
        readonly string path;
        readonly SecurityIdentifier owner;
        SafeFileHandle observed;
        string observedId;
        long observedBytes;
        bool poisoned, disposed;

        internal FictionalLoginSlot(C9ControlLock control, C9DirectoryIdentity directory,
                                    string path, SecurityIdentifier owner) {
            this.control = control;
            this.directory = directory;
            id = directory.Id;
            this.path = path;
            this.owner = owner;
        }

        public string DirectoryId { get { Check(); return id; } }
        public string FilePath { get { Check(); return path; } }

        void Check() {
            if (disposed || poisoned) throw new InvalidOperationException("C9W_LOGIN_SLOT_REFUSED");
            control.CheckCustody();
            using var current = WindowsIdentity.GetCurrent();
            if (!owner.Equals(current.User) || directory.Id != id)
                throw new InvalidOperationException("C9W_LOGIN_SLOT_ID");
            AssertOwnerOnly(directory.FinalHandle, owner);
            using var seen = C9DirectoryIdentity.Open(Path.GetDirectoryName(path), true);
            if (seen.Id != id) throw new InvalidOperationException("C9W_LOGIN_SLOT_ID");
            AssertOwnerOnly(seen.FinalHandle, owner);
        }

        void CheckEntries(bool expectFile) {
            int count = 0;
            foreach (string entry in Directory.EnumerateFileSystemEntries(Path.GetDirectoryName(path))) {
                if (!expectFile || !String.Equals(entry, path, StringComparison.OrdinalIgnoreCase) || ++count != 1)
                    throw new InvalidOperationException("C9W_LOGIN_SLOT_NOT_EMPTY");
            }
            if (expectFile && count != 1)
                throw new InvalidOperationException("C9W_LOGIN_SLOT_NOT_EMPTY");
        }

        // The held parent identity must be checked on either side. Access denied,
        // missing parent, sharing violation and every other error are not absence.
        public void AssertVacant() {
            try {
                Check();
                if (observed != null) throw new InvalidOperationException("C9W_LOGIN_SLOT_ALREADY_OBSERVED");
                using var h = CreateFileExisting(path, 0x20080, 0, IntPtr.Zero, 3, 0x00200080, IntPtr.Zero);
                if (!h.IsInvalid) throw new InvalidOperationException("C9W_LOGIN_FILE_EXISTS");
                int error = Marshal.GetLastWin32Error();
                if (error != 2) throw new Win32Exception(error, "C9W_LOGIN_ABSENCE_UNKNOWN");
                CheckEntries(false);
                Check();
            } catch { poisoned = true; throw; }
        }

        string CheckFile(SafeFileHandle h, out long bytes) {
            AssertOwnerOnly(h, owner);
            byte[] tag = new byte[8], standard = new byte[24], identity = new byte[24];
            if (!GetFileInformationByHandleEx(h,9,tag,(uint)tag.Length) ||
                !GetFileInformationByHandleEx(h,1,standard,(uint)standard.Length) ||
                !GetFileInformationByHandleEx(h,18,identity,(uint)identity.Length))
                throw new Win32Exception(Marshal.GetLastWin32Error(),"C9W_LOGIN_FILE_INFO");
            bytes = BitConverter.ToInt64(standard,8);
            string volume = BitConverter.ToUInt64(identity,0).ToString("x16");
            if ((BitConverter.ToUInt32(tag,0) & 0x441410) != 0 ||
                BitConverter.ToUInt32(standard,16) != 1 || standard[20] != 0 || standard[21] != 0 ||
                bytes <= 0 || bytes > 1048576 || volume != id.Substring(0,16))
                throw new InvalidOperationException("C9W_LOGIN_FILE_SHAPE");
            var name = new StringBuilder(32768);
            uint count = GetFinalPathNameByHandleW(h,name,(uint)name.Capacity,0);
            if (count == 0 || count >= name.Capacity ||
                !String.Equals(name.ToString(), @"\\?\" + path, StringComparison.OrdinalIgnoreCase))
                throw new InvalidOperationException("C9W_LOGIN_FILE_PATH");
            return volume + ":" + Convert.ToHexString(identity,8,16).ToLowerInvariant();
        }

        // Observe metadata only on a fixture-created file. An exclusive held
        // handle prevents ordinary concurrent opens/deletion until disposed.
        // No bytes are read; no credential validity or actual editor claimed.
        public string ObserveFictionalFile() {
            SafeFileHandle candidate = null;
            try {
                Check();
                if (observed != null) throw new InvalidOperationException("C9W_LOGIN_SLOT_ALREADY_OBSERVED");
                candidate = CreateFileExisting(path,0x20080,0,IntPtr.Zero,3,0x00200080,IntPtr.Zero);
                if (candidate.IsInvalid)
                    throw new Win32Exception(Marshal.GetLastWin32Error(),"C9W_LOGIN_FILE_OPEN");
                string found = CheckFile(candidate,out long bytes);
                CheckEntries(true);
                Check();
                if (CheckFile(candidate,out long again) != found || again != bytes)
                    throw new InvalidOperationException("C9W_LOGIN_FILE_CHANGED");
                observed = candidate;
                candidate = null;
                observedId = found;
                observedBytes = bytes;
                return observedId;
            } catch { poisoned = true; throw; }
            finally { if (candidate != null) candidate.Dispose(); }
        }

        public void AssertObserved() {
            try {
                Check();
                if (observed == null || observed.IsClosed ||
                    CheckFile(observed,out long bytes) != observedId || bytes != observedBytes)
                    throw new InvalidOperationException("C9W_LOGIN_FILE_CHANGED");
                CheckEntries(true);
                Check();
            } catch { poisoned = true; throw; }
        }

        public void Dispose() {
            if (disposed) return;
            disposed = true;
            if (observed != null) { observed.Dispose(); observed = null; }
            directory.Dispose();
            // Retain directory/file; no unlink, overwrite, revoke or inferred cleanup.
        }
    }

    // Only the exact four transport pairs observed in stage_original and
    // stage_approved_import. Fixed child name below the existing control
    // directory, not a caller-selected path. No recovery/open-existing mode.
    public FictionalLoginSlot BeginFictionalLoginSlot(string role,string kind) {
        if (!((role == "source" && kind == "backup") ||
              (role == "restore" && (kind == "backup" || kind == "restore")) ||
              (role == "mixed" && kind == "restore")))
            throw new InvalidOperationException("C9W_LOGIN_SLOT_SCOPE");
        CheckCustody();
        using var me = WindowsIdentity.GetCurrent();
        string parent = Path.Combine(ControlPath,"login-fixture-" + role + "-" + kind);
        WithSecurity(me.User,true,sa => {
            if (!CreateDirectoryW(parent,ref sa))
                throw new Win32Exception(Marshal.GetLastWin32Error(),"C9W_LOGIN_SLOT_CREATE");
        });
        var directory = C9DirectoryIdentity.Open(parent,true);
        try {
            AssertOwnerOnly(directory.FinalHandle,me.User);
            if (directory.Id == RootId || directory.Id == ControlId ||
                directory.Id.Substring(0,16) != ControlId.Substring(0,16))
                throw new InvalidOperationException("C9W_LOGIN_SLOT_ID");
            var slot = new FictionalLoginSlot(this,directory,Path.Combine(parent,".mylogin.cnf"),me.User);
            slot.AssertVacant();
            return slot;
        } catch { directory.Dispose(); throw; }
    }
}

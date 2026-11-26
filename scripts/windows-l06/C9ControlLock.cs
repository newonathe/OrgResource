using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.AccessControl;
using System.Security.Principal;
using System.Text;
using Microsoft.Win32.SafeHandles;

// H03 source candidate. Creates only an exclusive per-token control directory and lock,
// never a work directory, profile, mapping, database or recovery effect.
// Control state is outside the work tree and intentionally retained after release.
public sealed partial class C9ControlLock : IDisposable {
    C9DirectoryIdentity rootHandle, controlHandle;
    SafeFileHandle lockHandle;
    public string Root { get; private set; }
    public string Token { get; private set; }
    public string ControlPath { get; private set; }
    public string RootId { get { AssertHeld(); return rootHandle.Id; } }
    public string ControlId { get { AssertHeld(); return controlHandle.Id; } }
    C9ControlLock() {}

    public static C9ControlLock Create(string root, string token) { return Acquire(root, token, true); }
    public static C9ControlLock OpenExisting(string root, string token) { return Acquire(root, token, false); }

    static C9ControlLock Acquire(string root, string token, bool create) {
        if (!OperatingSystem.IsWindows() || token == null ||
            !System.Text.RegularExpressions.Regex.IsMatch(token, @"\A[a-f0-9]{12}\z"))
            throw new InvalidOperationException("C9W_CONTROL_ARGUMENT");
        using var me = WindowsIdentity.GetCurrent();
        if (me.User == null || me.IsSystem ||
            new WindowsPrincipal(me).IsInRole(WindowsBuiltInRole.Administrator))
            throw new InvalidOperationException("C9W_CONTROL_OWNER");
        var c = new C9ControlLock();
        try {
            c.Root = root;
            c.Token = token;
            c.rootHandle = C9DirectoryIdentity.Open(root, true);
            AssertOwnerOnly(c.rootHandle.FinalHandle, me.User);
            c.ControlPath = Path.Combine(root, "orgresource-c9w-control-" + token);
            if (create) {
                // CREATE_DIRECTORY fails on existing paths, including an existing symlink.
                // No rollback deletion if the following steps fail.
                WithSecurity(me.User, true, sa => {
                    if (!CreateDirectoryW(c.ControlPath, ref sa))
                        throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_CONTROL_CREATE");
                });
            }
            c.controlHandle = C9DirectoryIdentity.Open(c.ControlPath, true);
            AssertOwnerOnly(c.controlHandle.FinalHandle, me.User);
            if (c.rootHandle.Id == c.controlHandle.Id ||
                c.rootHandle.Id.Substring(0,16) != c.controlHandle.Id.Substring(0,16))
                throw new InvalidOperationException("C9W_CONTROL_ID");
            string path = Path.Combine(c.ControlPath, "ownership.lock");
            if (create) {
                WithSecurity(me.User, false, sa => {
                    c.lockHandle = CreateFileNew(path, 0xC0020080, 0, ref sa, 1, 0x00200080, IntPtr.Zero);
                    CheckOpen(c.lockHandle);
                });
            } else {
                // OPEN_EXISTING only, never fabricate a missing lock on recovery.
                c.lockHandle = CreateFileExisting(path, 0xC0020080, 0, IntPtr.Zero, 3, 0x00200080, IntPtr.Zero);
                CheckOpen(c.lockHandle);
            }
            c.CheckLockFile(path);
            AssertOwnerOnly(c.lockHandle, me.User);
            AssertOwnerOnly(c.rootHandle.FinalHandle, me.User);
            AssertOwnerOnly(c.controlHandle.FinalHandle, me.User);
            return c;
        } catch { c.Dispose(); throw; }
    }

    static void CheckOpen(SafeFileHandle h) {
        if (h == null || h.IsInvalid)
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_CONTROL_LOCK_REFUSED");
    }

    void CheckLockFile(string path) {
        byte[] tag = new byte[8], standard = new byte[24];
        if (!GetFileInformationByHandleEx(lockHandle, 9, tag, (uint)tag.Length) ||
            !GetFileInformationByHandleEx(lockHandle, 1, standard, (uint)standard.Length))
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_CONTROL_LOCK_INFO");
        // Empty regular file, exactly one link, not delete-pending, no reparse/offline/recall.
        if ((BitConverter.ToUInt32(tag,0) & 0x441410) != 0 ||
            BitConverter.ToInt64(standard,8) != 0 ||
            BitConverter.ToUInt32(standard,16) != 1 ||
            standard[20] != 0 || standard[21] != 0)
            throw new InvalidOperationException("C9W_CONTROL_LOCK_SHAPE");
        var name = new StringBuilder(32768);
        uint count = GetFinalPathNameByHandleW(lockHandle, name, (uint)name.Capacity, 0);
        if (count == 0 || count >= name.Capacity ||
            !String.Equals(name.ToString(), @"\\?\" + path, StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_CONTROL_LOCK_PATH");
    }

    // Evaluate the actual handle security descriptor, not a second path lookup.
    static void AssertOwnerOnly(SafeFileHandle h, SecurityIdentifier owner) {
        IntPtr o, g, d, s, sd;
        uint error = GetSecurityInfo(h, 1, 5, out o, out g, out d, out s, out sd);
        if (error != 0) throw new Win32Exception((int)error, "C9W_CONTROL_ACL");
        try {
            uint size = GetSecurityDescriptorLength(sd);
            if (size < 20 || size > 65536) throw new InvalidOperationException("C9W_CONTROL_ACL");
            byte[] bytes = new byte[size];
            Marshal.Copy(sd, bytes, 0, (int)size);
            var r = new RawSecurityDescriptor(bytes, 0);
            if (!owner.Equals(r.Owner) || r.DiscretionaryAcl == null ||
                (r.ControlFlags & ControlFlags.DiscretionaryAclProtected) == 0 ||
                (r.ControlFlags & ControlFlags.DiscretionaryAclPresent) == 0)
                throw new InvalidOperationException("C9W_CONTROL_ACL");
            int granted = 0;
            foreach (GenericAce ace in r.DiscretionaryAcl) {
                if (!(ace is CommonAce a) || a.IsCallback || a.AceQualifier != AceQualifier.AccessAllowed ||
                    !owner.Equals(a.SecurityIdentifier) || (a.AceFlags & AceFlags.Inherited) != 0)
                    throw new InvalidOperationException("C9W_CONTROL_ACL");
                if ((a.AceFlags & AceFlags.InheritOnly) == 0) granted |= a.AccessMask;
            }
            if ((granted & 0x1f01ff) != 0x1f01ff)
                throw new InvalidOperationException("C9W_CONTROL_ACL");
        } finally { if (sd != IntPtr.Zero) LocalFree(sd); }
    }

    static void WithSecurity(SecurityIdentifier owner, bool directory, Action<SA> action) {
        // Explicit protected owner-only DACL at creation, not a later ACL repair.
        var descriptor = new RawSecurityDescriptor("O:" + owner.Value + "D:P(A;" +
            (directory ? "OICI" : "") + ";FA;;;" + owner.Value + ")");
        byte[] bytes = new byte[descriptor.BinaryLength];
        descriptor.GetBinaryForm(bytes, 0);
        var pin = GCHandle.Alloc(bytes, GCHandleType.Pinned);
        try {
            var sa = new SA { Length = Marshal.SizeOf<SA>(), Descriptor = pin.AddrOfPinnedObject(), Inherit = 0 };
            action(sa);
        } finally { pin.Free(); }
    }

    public void AssertHeld() {
        if (lockHandle == null || lockHandle.IsClosed || lockHandle.IsInvalid ||
            rootHandle == null || controlHandle == null)
            throw new InvalidOperationException("C9W_CONTROL_LOCK_NOT_HELD");
    }

    public void Dispose() {
        if (lockHandle != null) { lockHandle.Dispose(); lockHandle = null; }
        if (controlHandle != null) { controlHandle.Dispose(); controlHandle = null; }
        if (rootHandle != null) { rootHandle.Dispose(); rootHandle = null; }
        // Never delete or unlink the control directory, lock or journal.
    }

    [StructLayout(LayoutKind.Sequential)]
    struct SA { public int Length; public IntPtr Descriptor; public int Inherit; }
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool CreateDirectoryW(string path, ref SA security);
    [DllImport("kernel32.dll", EntryPoint="CreateFileW", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern SafeFileHandle CreateFileNew(string path, uint access, uint share, ref SA security, uint disposition, uint flags, IntPtr template);
    [DllImport("kernel32.dll", EntryPoint="CreateFileW", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern SafeFileHandle CreateFileExisting(string path, uint access, uint share, IntPtr security, uint disposition, uint flags, IntPtr template);
    [DllImport("kernel32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool GetFileInformationByHandleEx(SafeFileHandle h, int infoClass, [Out] byte[] info, uint length);
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern uint GetFinalPathNameByHandleW(SafeFileHandle h, StringBuilder name, uint size, uint flags);
    [DllImport("advapi32.dll", ExactSpelling=true)]
    static extern uint GetSecurityInfo(SafeFileHandle h, int kind, uint info, out IntPtr owner, out IntPtr group, out IntPtr dacl, out IntPtr sacl, out IntPtr descriptor);
    [DllImport("advapi32.dll", ExactSpelling=true)]
    static extern uint GetSecurityDescriptorLength(IntPtr descriptor);
    [DllImport("kernel32.dll", ExactSpelling=true)]
    static extern IntPtr LocalFree(IntPtr p);
}

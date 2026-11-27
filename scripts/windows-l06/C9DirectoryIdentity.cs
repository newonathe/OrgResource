using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.Win32.SafeHandles;

// H03 native source candidate. No creation, deletion, process or mapping operations.
// Keeps all path components open without FILE_SHARE_DELETE until disposed.
// Caller must independently verify owner-only DACL, volume provenance, lock and
// journal state; identities are observations, not proof of authorized creation.
public sealed class C9DirectoryIdentity : IDisposable {
    readonly List<SafeFileHandle> handles = new List<SafeFileHandle>();
    public string Id { get; private set; }
    internal SafeFileHandle FinalHandle { get { return handles[handles.Count - 1]; } }

    C9DirectoryIdentity() {}

    public static C9DirectoryIdentity Open(string path) { return Open(path, false); }

    internal static C9DirectoryIdentity Open(string path, bool readSecurity) {
        if (!OperatingSystem.IsWindows() || String.IsNullOrEmpty(path) ||
            path.Length < 4 || !System.Text.RegularExpressions.Regex.IsMatch(path, @"^[A-Za-z]:\\[^:*?""<>|\x00-\x1f]+$") ||
            !String.Equals(Path.GetFullPath(path), path, StringComparison.Ordinal))
            throw new InvalidOperationException("C9W_DIRECTORY_PATH");
        var parts = path.Substring(3).Split('\\');
        foreach (string part in parts)
            if (part.Length == 0 || part == "." || part == ".." || part.EndsWith(" ") || part.EndsWith("."))
                throw new InvalidOperationException("C9W_DIRECTORY_PATH");
        var drive = new DriveInfo(path.Substring(0, 3));
        if (drive.DriveType != DriveType.Fixed || drive.DriveFormat != "NTFS")
            throw new InvalidOperationException("C9W_DIRECTORY_VOLUME");
        var result = new C9DirectoryIdentity();
        try {
            string current = path.Substring(0, 3);
            result.OpenComponent(current);
            for (int i = 0; i < parts.Length; i++) {
                current = Path.Combine(current, parts[i]);
                result.OpenComponent(current, readSecurity && i == parts.Length - 1);
            }
            return result;
        } catch {
            result.Dispose();
            throw;
        }
    }

    void OpenComponent(string path, bool readSecurity = false) {
        // FILE_LIST_DIRECTORY | FILE_READ_ATTRIBUTES (+ READ_CONTROL for
        // the final security observation); share read/write only, never delete.
        // A metadata-only handle may not participate in normal share checks.
        // Refuse inaccessible ancestors; do not downgrade to metadata-only access.
        // OPEN_EXISTING; BACKUP_SEMANTICS | OPEN_REPARSE_POINT.
        var h = CreateFileW(path, readSecurity ? 0x20081u : 0x81u, 3, IntPtr.Zero, 3, 0x02200000, IntPtr.Zero);
        if (h.IsInvalid) {
            int error = Marshal.GetLastWin32Error();
            h.Dispose();
            throw new Win32Exception(error, "C9W_DIRECTORY_OPEN");
        }
        handles.Add(h);
        byte[] tag = new byte[8];
        // FileAttributeTagInfo = 9. Require a directory and reject reparse/offline/recall.
        if (!GetFileInformationByHandleEx(h, 9, tag, (uint)tag.Length))
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_DIRECTORY_ATTRIBUTES");
        uint attrs = BitConverter.ToUInt32(tag, 0);
        if ((attrs & 0x10) == 0 || (attrs & 0x441400) != 0)
            throw new InvalidOperationException("C9W_DIRECTORY_ATTRIBUTES");
        var name = new StringBuilder(32768);
        uint count = GetFinalPathNameByHandleW(h, name, (uint)name.Capacity, 0);
        if (count == 0 || count >= name.Capacity ||
            !String.Equals(name.ToString(), @"\\?\" + path, StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_DIRECTORY_FINAL_PATH");
        // FileIdInfo = 18; FILE_ID_INFO is uint64 volume serial + 16 opaque ID bytes.
        byte[] id = new byte[24];
        if (!GetFileInformationByHandleEx(h, 18, id, (uint)id.Length))
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_DIRECTORY_ID");
        Id = BitConverter.ToUInt64(id, 0).ToString("x16") + ":" +
            Convert.ToHexString(id, 8, 16).ToLowerInvariant();
    }

    public void Dispose() {
        for (int i = handles.Count - 1; i >= 0; --i) handles[i].Dispose();
        handles.Clear();
        Id = null;
    }

    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern SafeFileHandle CreateFileW(string name, uint access, uint share, IntPtr security, uint disposition, uint flags, IntPtr template);
    [DllImport("kernel32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool GetFileInformationByHandleEx(SafeFileHandle handle, int infoClass, [Out] byte[] info, uint size);
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern uint GetFinalPathNameByHandleW(SafeFileHandle handle, StringBuilder path, uint size, uint flags);
}

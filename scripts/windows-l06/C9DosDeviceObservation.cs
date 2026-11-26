using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Security.Principal;
using Microsoft.Win32.SafeHandles;

// Read-only drive namespace observation. No creation, deletion, privilege or registry effects.
// AuthenticationId is boot-scoped; do NOT use it alone for crash recovery across reboot.
// QueryDosDevice resolves the effective namespace, not a proof of local/global ownership.
// Returned targets and user SID are private operator data; do not print them in public receipts.
public sealed class C9DosDeviceObservation {
    public string Drive { get; private set; }
    public string UserSid { get; private set; }
    public string AuthenticationId { get; private set; }
    public uint SessionId { get; private set; }
    public string LogonTime { get; private set; }
    public string BootId { get { return null; } }
    public string Status { get; private set; }
    public int NativeError { get; private set; }
    readonly string[] targets;
    public string[] Targets { get { return (string[])targets.Clone(); } }

    C9DosDeviceObservation(string drive, C9LogonContext context, string status, int error, string[] targets) {
        Drive = drive; UserSid = context.UserSid; AuthenticationId = context.AuthenticationId;
        SessionId = context.SessionId; LogonTime = context.LogonTime; Status = status;
        NativeError = error; this.targets = targets;
    }

    internal static (string user, string auth) Context() {
        if (!OperatingSystem.IsWindows()) throw new InvalidOperationException("C9W_DOS_CONTEXT");
        using var thread = WindowsIdentity.GetCurrent(true);
        using var me = WindowsIdentity.GetCurrent(false);
        if (thread != null || me.User == null || me.IsSystem ||
            new WindowsPrincipal(me).IsInRole(WindowsBuiltInRole.Administrator))
            throw new InvalidOperationException("C9W_DOS_CONTEXT");
        // TOKEN_STATISTICS, documented Win32 layout (56 bytes, authentication LUID at offset 8).
        // Use a fixed exact-size buffer and refuse unexpected ABI/return lengths.
        byte[] stats = new byte[56], appContainer = new byte[4];
        if (!GetTokenInformation(me.AccessToken, 10, stats, (uint)stats.Length, out uint n) ||
            n != stats.Length || BitConverter.ToInt32(stats,24) != 1)
            throw new InvalidOperationException("C9W_DOS_TOKEN");
        if (!GetTokenInformation(me.AccessToken, 29, appContainer, 4, out uint m) ||
            m != 4 || BitConverter.ToInt32(appContainer,0) != 0)
            throw new InvalidOperationException("C9W_DOS_TOKEN");
        // LUID = low DWORD, high LONG; preserve unsigned bit representation.
        string auth = BitConverter.ToUInt32(stats,12).ToString("x8") +
                      BitConverter.ToUInt32(stats,8).ToString("x8");
        return (me.User.Value, auth);
    }

    internal static string[] ParseTargets(char[] buffer, int count) {
        // Strict MULTI_SZ. Never use only the first current mapping and discard prior mappings.
        if (buffer == null || count < 3 || count > buffer.Length ||
            buffer[count-1] != '\0' || buffer[count-2] != '\0')
            throw new InvalidOperationException("C9W_DOS_SHAPE");
        var values = new List<string>();
        int start = 0;
        for (int i = 0; i < count-1; i++) {
            if (buffer[i] != '\0') continue;
            if (i == start || values.Count >= 1024)
                throw new InvalidOperationException("C9W_DOS_SHAPE");
            values.Add(new string(buffer, start, i-start));
            start = i+1;
        }
        if (start != count-1 || values.Count == 0)
            throw new InvalidOperationException("C9W_DOS_SHAPE");
        return values.ToArray();
    }

    public static C9DosDeviceObservation Read(string drive) {
        if (drive == null || !System.Text.RegularExpressions.Regex.IsMatch(drive, @"\A[A-Z]:\z"))
            throw new InvalidOperationException("C9W_DOS_DRIVE");
        var before = C9LogonContext.Read();
        const int capacity = 65536;
        IntPtr buffer = Marshal.AllocHGlobal(capacity * 2);
        try {
            uint count = QueryDosDeviceW(drive, buffer, capacity);
            int error = count == 0 ? Marshal.GetLastWin32Error() : 0;
            var after = C9LogonContext.Read();
            if (!before.SameContext(after)) throw new InvalidOperationException("C9W_DOS_CONTEXT_CHANGED");
            if (count == 0) {
                // No documented authoritative negative existence oracle here.
                // Retain the numeric error; never treat any failure as permission to create.
                return new C9DosDeviceObservation(drive, before, "UNRESOLVED", error, Array.Empty<string>());
            }
            if (count > capacity) throw new InvalidOperationException("C9W_DOS_SHAPE");
            char[] raw = new char[(int)count];
            Marshal.Copy(buffer, raw, 0, (int)count);
            return new C9DosDeviceObservation(drive, before, "OBSERVED", 0,
                ParseTargets(raw, (int)count));
        } finally { Marshal.FreeHGlobal(buffer); }
    }

    // A comparison of observations only. Never authorization to mutate a mapping.
    public bool SameObservation(C9DosDeviceObservation other) {
        if (other == null || Drive != other.Drive || UserSid != other.UserSid ||
            AuthenticationId != other.AuthenticationId || SessionId != other.SessionId ||
            LogonTime != other.LogonTime || Status != other.Status ||
            NativeError != other.NativeError || targets.Length != other.targets.Length)
            return false;
        for (int i = 0; i < targets.Length; i++)
            if (!String.Equals(targets[i], other.targets[i], StringComparison.Ordinal)) return false;
        return true;
    }

    [DllImport("advapi32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool GetTokenInformation(SafeAccessTokenHandle token, int kind,
        [Out] byte[] data, uint size, out uint returned);
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern uint QueryDosDeviceW(string name, IntPtr target, uint count);
}

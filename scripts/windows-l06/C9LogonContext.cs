using System;
using System.Globalization;
using System.Runtime.InteropServices;
using System.Security.Principal;
using Microsoft.Win32.SafeHandles;

// Read-only current primary logon context. Documented LSA prefix and token session ID.
// LUID + logon FILETIME + session ID are observations, NOT an authoritative boot witness.
// BootId stays null. Do not use equality alone to authorize recovery.
public sealed class C9LogonContext {
    public string UserSid { get; private set; }
    public string AuthenticationId { get; private set; }
    public uint SessionId { get; private set; }
    public string LogonTime { get; private set; }
    public string BootId { get { return null; } }

    C9LogonContext(string user, string auth, uint session, long time) {
        UserSid = user; AuthenticationId = auth; SessionId = session;
        LogonTime = time.ToString(CultureInfo.InvariantCulture);
    }

    static uint ReadSession(string expectedUser) {
        using var me = WindowsIdentity.GetCurrent(false);
        byte[] data = new byte[4];
        if (me.User == null || me.User.Value != expectedUser ||
            !GetTokenInformation(me.AccessToken, 12, data, 4, out uint size) || size != 4)
            throw new InvalidOperationException("C9W_LOGON_SESSION_TOKEN");
        uint session = BitConverter.ToUInt32(data, 0);
        if (session == 0) throw new InvalidOperationException("C9W_LOGON_SESSION_ZERO");
        return session;
    }

    public static C9LogonContext Read() {
        var before = C9DosDeviceObservation.Context();
        uint session = ReadSession(before.user);
        if (before.auth == "0000000000000000")
            throw new InvalidOperationException("C9W_LOGON_ID_ZERO");
        var luid = new Luid {
            High = unchecked((int)uint.Parse(before.auth.Substring(0,8), NumberStyles.HexNumber, CultureInfo.InvariantCulture)),
            Low = uint.Parse(before.auth.Substring(8,8), NumberStyles.HexNumber, CultureInfo.InvariantCulture)
        };
        IntPtr data = IntPtr.Zero;
        try {
            int status = LsaGetLogonSessionData(ref luid, out data);
            if (status != 0 || data == IntPtr.Zero)
                throw new InvalidOperationException("C9W_LOGON_LSA_REFUSED");
            // Read only the documented prefix through LogonTime. Verify returned Size
            // before dereferencing the struct or SID; don't read account/domain strings.
            uint size = unchecked((uint)Marshal.ReadInt32(data));
            int prefix = Marshal.SizeOf<SessionPrefix>();
            if (size < prefix || size > 65536)
                throw new InvalidOperationException("C9W_LOGON_LSA_SHAPE");
            var info = Marshal.PtrToStructure<SessionPrefix>(data);
            if (info.Size != size || info.LogonId.High != luid.High || info.LogonId.Low != luid.Low ||
                info.Session != session || info.Sid == IntPtr.Zero || info.LogonTime <= 0 ||
                !IsValidSid(info.Sid) || new SecurityIdentifier(info.Sid).Value != before.user)
                throw new InvalidOperationException("C9W_LOGON_LSA_BINDING");
            var after = C9DosDeviceObservation.Context();
            if (before != after || ReadSession(after.user) != session)
                throw new InvalidOperationException("C9W_LOGON_CONTEXT_CHANGED");
            return new C9LogonContext(before.user, before.auth, session, info.LogonTime);
        } finally {
            if (data != IntPtr.Zero) LsaFreeReturnBuffer(data);
        }
    }

    public bool SameContext(C9LogonContext other) {
        return other != null && UserSid == other.UserSid && AuthenticationId == other.AuthenticationId &&
            SessionId == other.SessionId && LogonTime == other.LogonTime;
    }

    [StructLayout(LayoutKind.Sequential)]
    struct Luid { public uint Low; public int High; }
    [StructLayout(LayoutKind.Sequential)]
    struct LsaString { public ushort Length; public ushort MaximumLength; public IntPtr Buffer; }
    [StructLayout(LayoutKind.Sequential)]
    struct SessionPrefix {
        public uint Size;
        public Luid LogonId;
        public LsaString UserName, LogonDomain, AuthenticationPackage;
        public uint LogonType, Session;
        public IntPtr Sid;
        public long LogonTime;
    }
    [DllImport("secur32.dll", ExactSpelling=true)]
    static extern int LsaGetLogonSessionData(ref Luid id, out IntPtr data);
    [DllImport("secur32.dll", ExactSpelling=true)]
    static extern int LsaFreeReturnBuffer(IntPtr data);
    [DllImport("advapi32.dll", ExactSpelling=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool IsValidSid(IntPtr sid);
    [DllImport("advapi32.dll", ExactSpelling=true, SetLastError=true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    static extern bool GetTokenInformation(SafeAccessTokenHandle token, int kind,
        [Out] byte[] data, uint size, out uint returned);
}

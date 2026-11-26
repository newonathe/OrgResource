using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Principal;

// Bounded in-memory observation for a profile created by this process.
// Derived SID is deterministic and DOES NOT establish profile existence.
// S_OK on DeleteAppContainerProfile alone is not proof of storage absence.
// No registry keys are guessed, no arbitrary filesystem/profile deletion.
// Current-user, known local Packages layout only; other layouts refuse.
// No handles into the profile storage are retained across DeleteAppContainerProfile.
internal sealed class C9ProfileStorageObservation {
    readonly string name, sid, user, packages, package, appData, packagesId, packageId, appDataId;

    C9ProfileStorageObservation(string name, string sid, string user, string packages,
        string package, string appData, string packagesId, string packageId, string appDataId) {
        this.name = name; this.sid = sid; this.user = user; this.packages = packages;
        this.package = package; this.appData = appData; this.packagesId = packagesId;
        this.packageId = packageId; this.appDataId = appDataId;
    }

    internal static string PrimaryUser() {
        if (!OperatingSystem.IsWindows()) throw new InvalidOperationException("C9W_PROFILE_USER");
        using var thread = WindowsIdentity.GetCurrent(true);
        using var me = WindowsIdentity.GetCurrent(false);
        if (thread != null || me.User == null || me.IsSystem ||
            new WindowsPrincipal(me).IsInRole(WindowsBuiltInRole.Administrator))
            throw new InvalidOperationException("C9W_PROFILE_USER");
        return me.User.Value;
    }

    static string Derive(string name) {
        IntPtr derived = IntPtr.Zero, str = IntPtr.Zero;
        try {
            if (DeriveAppContainerSidFromAppContainerName(name, out derived) != 0 || derived == IntPtr.Zero ||
                !N.ConvertSidToStringSidW(derived, out str) || str == IntPtr.Zero)
                throw new InvalidOperationException("C9W_PROFILE_DERIVE");
            return Marshal.PtrToStringUni(str);
        } finally {
            if (str != IntPtr.Zero) N.LocalFree(str);
            if (derived != IntPtr.Zero) N.FreeSid(derived);
        }
    }

    static string Folder(string sid) {
        IntPtr path = IntPtr.Zero;
        try {
            if (GetAppContainerFolderPath(sid, out path) != 0 || path == IntPtr.Zero)
                throw new InvalidOperationException("C9W_PROFILE_FOLDER");
            return Marshal.PtrToStringUni(path);
        } finally { if (path != IntPtr.Zero) Marshal.FreeCoTaskMem(path); }
    }

    static bool EqualPath(string a, string b) {
        return String.Equals(a, b, StringComparison.OrdinalIgnoreCase);
    }

    static string Packages() {
        string local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        if (String.IsNullOrEmpty(local) || !Path.IsPathFullyQualified(local))
            throw new InvalidOperationException("C9W_PROFILE_FOLDER");
        return Path.Combine(local, "Packages");
    }

    internal static C9ProfileStorageObservation Capture(string name, string sid, string user) {
        if (!System.Text.RegularExpressions.Regex.IsMatch(name, @"\Aorgresource\.c9\.[a-f0-9]{12}\z") ||
            user != PrimaryUser() || sid != Derive(name))
            throw new InvalidOperationException("C9W_PROFILE_BINDING");
        string packages = Packages();
        string package = Path.Combine(packages, name);
        string appData = Path.Combine(package, "AC");
        if (!EqualPath(Folder(sid), appData))
            throw new InvalidOperationException("C9W_PROFILE_FOLDER");
        using var baseId = C9DirectoryIdentity.Open(packages);
        using var packageId = C9DirectoryIdentity.Open(package);
        using var dataId = C9DirectoryIdentity.Open(appData);
        if (baseId.Id == packageId.Id || packageId.Id == dataId.Id || baseId.Id == dataId.Id ||
            baseId.Id.Substring(0,16) != packageId.Id.Substring(0,16) ||
            baseId.Id.Substring(0,16) != dataId.Id.Substring(0,16))
            throw new InvalidOperationException("C9W_PROFILE_STORAGE_ID");
        return new C9ProfileStorageObservation(name, sid, user, packages, package, appData,
            baseId.Id, packageId.Id, dataId.Id);
    }

    void AssertContext() {
        if (user != PrimaryUser() || sid != Derive(name) || !EqualPath(Packages(), packages))
            throw new InvalidOperationException("C9W_PROFILE_BINDING");
    }

    internal void AssertPresent() {
        AssertContext();
        if (!EqualPath(Folder(sid), appData))
            throw new InvalidOperationException("C9W_PROFILE_FOLDER");
        using var baseId = C9DirectoryIdentity.Open(packages);
        using var packageSeen = C9DirectoryIdentity.Open(package);
        using var dataSeen = C9DirectoryIdentity.Open(appData);
        if (baseId.Id != packagesId || packageSeen.Id != packageId || dataSeen.Id != appDataId)
            throw new InvalidOperationException("C9W_PROFILE_STORAGE_ID");
    }

    internal void AssertAbsent() {
        AssertContext();
        using var baseId = C9DirectoryIdentity.Open(packages);
        if (baseId.Id != packagesId)
            throw new InvalidOperationException("C9W_PROFILE_STORAGE_ID");
        uint attrs = GetFileAttributesW(package);
        if (attrs != 0xffffffff)
            throw new InvalidOperationException("C9W_PROFILE_STORAGE_REMAINS");
        int error = Marshal.GetLastWin32Error();
        // Only missing final name under an observed/pinned original parent counts.
        // Access denied, path not found, and all other errors are UNKNOWN, not absence.
        if (error != 2)
            throw new Win32Exception(error, "C9W_PROFILE_STORAGE_UNKNOWN");
        // Deliberately do NOT interpret a folder API error as profile absence.
        // This proves only the observed package-directory absence, not all registry state.
    }

    [DllImport("userenv.dll", CharSet=CharSet.Unicode, ExactSpelling=true)]
    static extern int DeriveAppContainerSidFromAppContainerName(string name, out IntPtr sid);
    [DllImport("userenv.dll", CharSet=CharSet.Unicode, ExactSpelling=true)]
    static extern int GetAppContainerFolderPath(string sid, out IntPtr path);
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode, ExactSpelling=true, SetLastError=true)]
    static extern uint GetFileAttributesW(string name);
}

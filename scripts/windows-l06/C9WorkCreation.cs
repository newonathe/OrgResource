using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Principal;

// Native work creation primitive. Internal, only called after durable work intent.
// No recursive cleanup; returned identity pins the actual directory and ancestors.
public sealed partial class C9ControlLock {
    internal C9DirectoryIdentity CreateWorkAfterIntent() {
        CheckCustody();
        using var me = WindowsIdentity.GetCurrent();
        string path = Path.Combine(Root, "orgresource-c9w-" + Token);
        // CreateDirectoryW is exclusive: refuse existing files/directories/reparse points.
        WithSecurity(me.User, true, sa => {
            if (!CreateDirectoryW(path, ref sa))
                throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_WORK_CREATE");
        });
        var identity = C9DirectoryIdentity.Open(path, true);
        try {
            AssertOwnerOnly(identity.FinalHandle, me.User);
            if (identity.Id == RootId || identity.Id == ControlId ||
                identity.Id.Substring(0,16) != RootId.Substring(0,16))
                throw new InvalidOperationException("C9W_WORK_IDENTITY");
            CheckCustody();
            return identity;
        } catch { identity.Dispose(); throw; }
    }
}

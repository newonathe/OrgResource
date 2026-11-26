using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Security.Principal;
using System.Text;
using System.Text.RegularExpressions;
using Microsoft.Win32.SafeHandles;

// H01-A fictional native staging primitive. No production caller, private input,
// vault access, archive authentication, final commit marker or deletion.
// The method names are scope labels, not a proof that caller-supplied bytes are fictional.
public sealed partial class C9ControlLock {
    const long RetentionOverhead = 54;
    const long RetentionMax = 64L * 1024 * 1024 + RetentionOverhead;
    static readonly byte[] RetentionHeader = Encoding.ASCII.GetBytes("ORGRESOURCE-C5-AES256GCM-1");

    public sealed class StageObservation {
        public string SourceId { get; internal set; }
        public string StageId { get; internal set; }
        public string Sha256 { get; internal set; }
        public long Bytes { get; internal set; }
    }

    static void CheckRetentionExpected(string sha, long bytes) {
        if (sha == null || !Regex.IsMatch(sha, @"\A[a-f0-9]{64}\z") ||
            bytes <= RetentionOverhead || bytes > RetentionMax ||
            RetentionHeader.Length + 28 != RetentionOverhead)
            throw new InvalidOperationException("C9W_RETENTION_EXPECTED");
    }

    static bool SameOrBelow(string path, string ancestor) {
        return String.Equals(path, ancestor, StringComparison.OrdinalIgnoreCase) ||
            path.StartsWith(ancestor + "\\", StringComparison.OrdinalIgnoreCase);
    }

    C9DirectoryIdentity OpenRetentionSource(string sourceDirectory, string expectedDirectoryId) {
        CheckCustody();
        // Open() also refuses noncanonical paths, nonlocal NTFS, and reparse components.
        var dir = C9DirectoryIdentity.Open(sourceDirectory, true);
        try {
            if (SameOrBelow(sourceDirectory, Root) || SameOrBelow(Root, sourceDirectory) ||
                dir.Id != expectedDirectoryId || dir.Id == RootId || dir.Id == ControlId)
                throw new InvalidOperationException("C9W_RETENTION_SOURCE_DIRECTORY");
            using var me = WindowsIdentity.GetCurrent();
            AssertOwnerOnly(dir.FinalHandle, me.User);
            return dir;
        } catch { dir.Dispose(); throw; }
    }

    static SafeFileHandle OpenRetentionFile(string path) {
        var h = CreateFileExisting(path, 0x80020080, 0, IntPtr.Zero, 3, 0x00200080, IntPtr.Zero);
        if (h.IsInvalid) {
            int error = Marshal.GetLastWin32Error();
            h.Dispose();
            throw new Win32Exception(error, "C9W_RETENTION_OPEN");
        }
        return h;
    }

    static string CheckRetentionFile(SafeFileHandle h, string path, string dirId, long expectedBytes) {
        using var me = WindowsIdentity.GetCurrent();
        AssertOwnerOnly(h, me.User);
        byte[] tag = new byte[8], standard = new byte[24], id = new byte[24];
        if (!GetFileInformationByHandleEx(h, 9, tag, (uint)tag.Length) ||
            !GetFileInformationByHandleEx(h, 1, standard, (uint)standard.Length) ||
            !GetFileInformationByHandleEx(h, 18, id, (uint)id.Length))
            throw new Win32Exception(Marshal.GetLastWin32Error(), "C9W_RETENTION_INFO");
        string volume = BitConverter.ToUInt64(id,0).ToString("x16");
        if ((BitConverter.ToUInt32(tag,0) & 0x441410) != 0 ||
            BitConverter.ToUInt32(standard,16) != 1 || standard[20] != 0 || standard[21] != 0 ||
            BitConverter.ToInt64(standard,8) != expectedBytes || volume != dirId.Substring(0,16))
            throw new InvalidOperationException("C9W_RETENTION_FILE_SHAPE");
        var name = new StringBuilder(32768);
        uint count = GetFinalPathNameByHandleW(h, name, (uint)name.Capacity, 0);
        if (count == 0 || count >= name.Capacity ||
            !String.Equals(name.ToString(), @"\\?\" + path, StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("C9W_RETENTION_FILE_PATH");
        return volume + ":" + Convert.ToHexString(id,8,16).ToLowerInvariant();
    }

    static void CheckArchiveStream(FileStream source, long length, string expectedSha, FileStream copy) {
        if (source.Length != length) throw new InvalidOperationException("C9W_RETENTION_LENGTH");
        source.Position = 0;
        using var hash = IncrementalHash.CreateHash(HashAlgorithmName.SHA256);
        byte[] header = new byte[RetentionHeader.Length];
        source.ReadExactly(header);
        if (!header.AsSpan().SequenceEqual(RetentionHeader))
            throw new InvalidOperationException("C9W_RETENTION_HEADER");
        hash.AppendData(header);
        if (copy != null) copy.Write(header,0,header.Length);
        byte[] buffer = new byte[65536];
        long remaining = length - header.Length;
        while (remaining > 0) {
            int n = source.Read(buffer,0,(int)Math.Min(remaining,buffer.Length));
            if (n <= 0) throw new InvalidOperationException("C9W_RETENTION_LENGTH");
            hash.AppendData(buffer,0,n);
            if (copy != null) copy.Write(buffer,0,n);
            remaining -= n;
        }
        if (source.ReadByte() != -1 || source.Length != length)
            throw new InvalidOperationException("C9W_RETENTION_LENGTH");
        if (Convert.ToHexString(hash.GetHashAndReset()).ToLowerInvariant() != expectedSha)
            throw new InvalidOperationException("C9W_RETENTION_HASH");
    }

    // No file effect; the fixture can independently record this opaque ID before staging.
    public string ObserveFictionalArchive(string sourceDirectory, string sourceDirectoryId,
                                          string expectedSha, long expectedBytes) {
        CheckRetentionExpected(expectedSha,expectedBytes);
        using var dir = OpenRetentionSource(sourceDirectory,sourceDirectoryId);
        string path = Path.Combine(sourceDirectory,"third.aesgcm");
        using var h = OpenRetentionFile(path);
        string id = CheckRetentionFile(h,path,dir.Id,expectedBytes);
        using var stream = new FileStream(h,FileAccess.Read);
        CheckArchiveStream(stream,expectedBytes,expectedSha,null);
        if (id != CheckRetentionFile(h,path,dir.Id,expectedBytes))
            throw new InvalidOperationException("C9W_RETENTION_SOURCE_ID");
        CheckCustody();
        return id;
    }

    // Stage only in the separate held owner-only control directory, CREATE_NEW.
    // Reopening detects substitution at that instant; no later use is authorized.
    // Failed or partial staging is retained, not overwritten or automatically disposed.
    public StageObservation StageFictionalArchive(string sourceDirectory, string sourceDirectoryId,
                  string sourceFileId, string expectedSha, long expectedBytes) {
        CheckRetentionExpected(expectedSha,expectedBytes);
        if (sourceFileId == null || !Regex.IsMatch(sourceFileId,@"\A[a-f0-9]{16}:[a-f0-9]{32}\z"))
            throw new InvalidOperationException("C9W_RETENTION_SOURCE_ID");
        using var dir = OpenRetentionSource(sourceDirectory,sourceDirectoryId);
        string sourcePath = Path.Combine(sourceDirectory,"third.aesgcm");
        using var sh = OpenRetentionFile(sourcePath);
        if (CheckRetentionFile(sh,sourcePath,dir.Id,expectedBytes) != sourceFileId)
            throw new InvalidOperationException("C9W_RETENTION_SOURCE_ID");
        using var source = new FileStream(sh,FileAccess.Read);
        CheckArchiveStream(source,expectedBytes,expectedSha,null);
        CheckCustody();
        string stagePath = Path.Combine(ControlPath,"retention.stage.aesgcm");
        string stageId = null;
        using var me = WindowsIdentity.GetCurrent();
        WithSecurity(me.User,false,sa => {
            using var dh = CreateFileNew(stagePath,0xC0020080,0,ref sa,1,0x00200080,IntPtr.Zero);
            if (dh.IsInvalid) throw new Win32Exception(Marshal.GetLastWin32Error(),"C9W_RETENTION_STAGE_CREATE");
            stageId = CheckRetentionFile(dh,stagePath,ControlId,0);
            using var dest = new FileStream(dh,FileAccess.ReadWrite);
            CheckArchiveStream(source,expectedBytes,expectedSha,dest);
            dest.Flush(true);
            CheckArchiveStream(dest,expectedBytes,expectedSha,null);
            if (CheckRetentionFile(dh,stagePath,ControlId,expectedBytes) != stageId)
                throw new InvalidOperationException("C9W_RETENTION_STAGE_ID");
        });
        CheckCustody();
        using var readback = OpenRetentionFile(stagePath);
        if (CheckRetentionFile(readback,stagePath,ControlId,expectedBytes) != stageId)
            throw new InvalidOperationException("C9W_RETENTION_STAGE_ID");
        using var reopened = new FileStream(readback,FileAccess.Read);
        CheckArchiveStream(reopened,expectedBytes,expectedSha,null);
        if (CheckRetentionFile(readback,stagePath,ControlId,expectedBytes) != stageId ||
            CheckRetentionFile(sh,sourcePath,dir.Id,expectedBytes) != sourceFileId)
            throw new InvalidOperationException("C9W_RETENTION_STAGE_ID");
        AssertOwnerOnly(dir.FinalHandle,me.User);
        CheckCustody();
        return new StageObservation { SourceId=sourceFileId, StageId=stageId,
                                      Sha256=expectedSha, Bytes=expectedBytes };
    }
}

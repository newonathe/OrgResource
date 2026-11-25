package c9win;

import java.nio.file.*;
import java.nio.file.attribute.*;

/** Fictional positive and negative checks of OwnerOnlyPosixProvider; prints enums only. */
public final class SelfTest {
    public static void main(String[] a) throws Exception {
        Path good = Path.of(a[0]), broad = Path.of(a[1]);
        FileAttribute<?> rw = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
        if (a.length > 2) { // print the raw principals once so the harness can pin exact names
            Files.createFile(good);
            for (AclEntry e : Files.getFileAttributeView(good, AclFileAttributeView.class).getAcl()) System.out.println("PRINCIPAL " + e.principal().getName());
            return;
        }
        Files.createFile(good, rw);
        if (!PosixFilePermissions.toString(Files.getPosixFilePermissions(good)).equals("rw-------")) throw new Exception("mode");
        System.out.println("C9W_POSIX_PRIVATE_CREATE_OK");
        try { Files.createFile(good, rw); throw new Exception("overwrite"); } catch (FileAlreadyExistsException ok) { System.out.println("C9W_POSIX_OVERWRITE_REFUSED_OK"); }
        if (PosixFilePermissions.toString(Files.getPosixFilePermissions(broad)).equals("rw-------")) throw new Exception("broad accepted");
        System.out.println("C9W_POSIX_BROAD_ACL_REPORTED_OK");
        Path sibling = broad.resolveSibling("created-in-broad.txt");
        try { Files.createFile(sibling, rw); throw new Exception("broad create"); }
        catch (AccessDeniedException ok) { if (Files.exists(sibling)) throw new Exception("left behind"); System.out.println("C9W_POSIX_BROAD_CREATE_REFUSED_OK"); }
        try { Files.createFile(good.resolveSibling("x"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"))); throw new Exception("wide"); }
        catch (UnsupportedOperationException ok) { System.out.println("C9W_POSIX_WIDE_MODE_REFUSED_OK"); }
    }
}

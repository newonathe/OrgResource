package c9win;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Verification-only default file-system provider for the unmodified application JAR on Windows.
 * It adds exactly one capability: POSIX permission bits derived from the REAL NTFS DACL.
 * "rw-------" is reported only when the owner is the current account and every ACE the JDK can decode grants to the
 * current account or to this run's own AppContainer. A create with "rw-------" is kept only if the new file passes.
 * The JDK does not decode conditional or callback ACEs, so the runner also checks the complete DACL with .NET before
 * every command that trusts this provider. It never widens access and fails closed on an unreadable ACL.
 */
public final class OwnerOnlyPosixProvider extends FileSystemProvider {
    private final FileSystemProvider d;
    private final Fs fs;
    private final String account, containerSid;
    private UserPrincipal accountPrincipal;

    public OwnerOnlyPosixProvider(FileSystemProvider parent) {
        d = parent;
        fs = new Fs(parent.getFileSystem(URI.create("file:///")), this);
        String[] v = System.getProperty("c9.acl.allowed", "").split("\\|");
        boolean shape = v.length == 2 && !v[0].isBlank() && v[1].matches("S-1-15-2(-[0-9]{1,10}){7}");
        account = shape ? v[0] : null;
        containerSid = shape ? v[1] : null;
    }

    private boolean ownerOnly(Path p) throws IOException {
        if (account == null) return false;
        if (accountPrincipal == null) accountPrincipal = fs.d.getUserPrincipalLookupService().lookupPrincipalByName(account); // equality is by SID
        if (!accountPrincipal.equals(d.getFileAttributeView(un(p), FileOwnerAttributeView.class, LinkOption.NOFOLLOW_LINKS).getOwner())) return false;
        AclFileAttributeView view = d.getFileAttributeView(un(p), AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (view == null) return false;
        List<AclEntry> acl = view.getAcl();
        if (acl.isEmpty()) return false;
        for (AclEntry e : acl) {
            if (e.type() != AclEntryType.ALLOW) return false;
            if (!accountPrincipal.equals(e.principal()) && !containerSid.equals(e.principal().getName())) return false;
        }
        return true;
    }

    private static Path un(Path p) {
        if (p instanceof P w) return w.d;
        if (p == null) throw new NullPointerException();
        throw new ProviderMismatchException();
    }
    Path wrap(Path p) { return p == null ? null : new P(fs, p); }

    private record Posix(BasicFileAttributes b, boolean privateFile) implements PosixFileAttributes {
        public UserPrincipal owner() { throw new UnsupportedOperationException(); }
        public GroupPrincipal group() { throw new UnsupportedOperationException(); }
        public Set<PosixFilePermission> permissions() {
            return privateFile ? EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                               : EnumSet.allOf(PosixFilePermission.class);
        }
        public FileTime lastModifiedTime() { return b.lastModifiedTime(); }
        public FileTime lastAccessTime() { return b.lastAccessTime(); }
        public FileTime creationTime() { return b.creationTime(); }
        public boolean isRegularFile() { return b.isRegularFile(); }
        public boolean isDirectory() { return b.isDirectory(); }
        public boolean isSymbolicLink() { return b.isSymbolicLink(); }
        public boolean isOther() { return b.isOther(); }
        public long size() { return b.size(); }
        public Object fileKey() { return b.fileKey(); }
    }

    @Override public <A extends BasicFileAttributes> A readAttributes(Path p, Class<A> t, LinkOption... o) throws IOException {
        if (t != PosixFileAttributes.class) return d.readAttributes(un(p), t, o);
        BasicFileAttributes b = d.readAttributes(un(p), BasicFileAttributes.class, o);
        return t.cast(new Posix(b, b.isRegularFile() && ownerOnly(p)));
    }

    @Override public SeekableByteChannel newByteChannel(Path p, Set<? extends OpenOption> o, FileAttribute<?>... attrs) throws IOException {
        List<FileAttribute<?>> rest = new ArrayList<>();
        boolean priv = false;
        for (FileAttribute<?> a : attrs) {
            if (!a.name().equals("posix:permissions")) { rest.add(a); continue; }
            if (!a.value().equals(PosixFilePermissions.fromString("rw-------"))) throw new UnsupportedOperationException("only rw-------");
            priv = true;
        }
        FileAttribute<?>[] r = rest.toArray(new FileAttribute<?>[0]);
        if (!priv) return d.newByteChannel(un(p), o, r);
        if (!o.contains(StandardOpenOption.CREATE_NEW)) throw new UnsupportedOperationException("private create must be new");
        SeekableByteChannel c = d.newByteChannel(un(p), o, r);
        boolean ok = false;
        try { ok = ownerOnly(p); }
        finally { if (!ok) { c.close(); d.deleteIfExists(un(p)); } }
        if (!ok) throw new AccessDeniedException("C9W_PRIVATE_ACL_REQUIRED");
        return c;
    }

    @Override public FileChannel newFileChannel(Path p, Set<? extends OpenOption> o, FileAttribute<?>... a) throws IOException { return d.newFileChannel(un(p), o, a); }
    @Override public java.nio.channels.AsynchronousFileChannel newAsynchronousFileChannel(Path p, Set<? extends OpenOption> o, ExecutorService e, FileAttribute<?>... a) throws IOException { return d.newAsynchronousFileChannel(un(p), o, e, a); }
    @Override public String getScheme() { return "file"; }
    @Override public FileSystem newFileSystem(URI u, Map<String, ?> e) { throw new FileSystemAlreadyExistsException(); }
    @Override public FileSystem getFileSystem(URI u) { return fs; }
    @Override public Path getPath(URI u) { return wrap(d.getPath(u)); }
    @Override public DirectoryStream<Path> newDirectoryStream(Path p, DirectoryStream.Filter<? super Path> f) throws IOException {
        DirectoryStream<Path> s = d.newDirectoryStream(un(p), x -> f.accept(wrap(x)));
        return new DirectoryStream<>() {
            public Iterator<Path> iterator() {
                Iterator<Path> i = s.iterator();
                return new Iterator<>() { public boolean hasNext() { return i.hasNext(); } public Path next() { return wrap(i.next()); } };
            }
            public void close() throws IOException { s.close(); }
        };
    }
    @Override public void createDirectory(Path p, FileAttribute<?>... a) throws IOException { d.createDirectory(un(p), a); }
    @Override public void delete(Path p) throws IOException { d.delete(un(p)); }
    @Override public void copy(Path a, Path b, CopyOption... o) throws IOException { d.copy(un(a), un(b), o); }
    @Override public void move(Path a, Path b, CopyOption... o) throws IOException { d.move(un(a), un(b), o); }
    @Override public boolean isSameFile(Path a, Path b) throws IOException { return d.isSameFile(un(a), un(b)); }
    @Override public boolean isHidden(Path p) throws IOException { return d.isHidden(un(p)); }
    @Override public FileStore getFileStore(Path p) throws IOException { return d.getFileStore(un(p)); }
    @Override public void checkAccess(Path p, AccessMode... m) throws IOException { d.checkAccess(un(p), m); }
    @Override public <V extends FileAttributeView> V getFileAttributeView(Path p, Class<V> t, LinkOption... o) { return d.getFileAttributeView(un(p), t, o); }
    @Override public Map<String, Object> readAttributes(Path p, String a, LinkOption... o) throws IOException { return d.readAttributes(un(p), a, o); }
    @Override public void setAttribute(Path p, String a, Object v, LinkOption... o) throws IOException { d.setAttribute(un(p), a, v, o); }

    private static final class Fs extends FileSystem {
        final FileSystem d; final OwnerOnlyPosixProvider pr;
        Fs(FileSystem d, OwnerOnlyPosixProvider pr) { this.d = d; this.pr = pr; }
        public FileSystemProvider provider() { return pr; }
        public void close() { throw new UnsupportedOperationException(); }
        public boolean isOpen() { return true; }
        public boolean isReadOnly() { return false; }
        public String getSeparator() { return d.getSeparator(); }
        public Iterable<Path> getRootDirectories() { List<Path> l = new ArrayList<>(); for (Path p : d.getRootDirectories()) l.add(pr.wrap(p)); return l; }
        public Iterable<FileStore> getFileStores() { return d.getFileStores(); }
        public Set<String> supportedFileAttributeViews() { return d.supportedFileAttributeViews(); }
        public Path getPath(String f, String... m) { return pr.wrap(d.getPath(f, m)); }
        public PathMatcher getPathMatcher(String s) { PathMatcher m = d.getPathMatcher(s); return p -> m.matches(un(p)); }
        public UserPrincipalLookupService getUserPrincipalLookupService() { return d.getUserPrincipalLookupService(); }
        public WatchService newWatchService() throws IOException { return d.newWatchService(); }
    }

    private record P(Fs f, Path d) implements Path {
        private Path w(Path p) { return p == null ? null : new P(f, p); }
        public FileSystem getFileSystem() { return f; }
        public boolean isAbsolute() { return d.isAbsolute(); }
        public Path getRoot() { return w(d.getRoot()); }
        public Path getFileName() { return w(d.getFileName()); }
        public Path getParent() { return w(d.getParent()); }
        public int getNameCount() { return d.getNameCount(); }
        public Path getName(int i) { return w(d.getName(i)); }
        public Path subpath(int a, int b) { return w(d.subpath(a, b)); }
        public boolean startsWith(Path o) { return o instanceof P p && d.startsWith(p.d); }
        public boolean endsWith(Path o) { return o instanceof P p && d.endsWith(p.d); }
        public Path normalize() { return w(d.normalize()); }
        public Path resolve(Path o) { return w(d.resolve(un(o))); }
        public Path relativize(Path o) { return w(d.relativize(un(o))); }
        public URI toUri() { return d.toUri(); }
        public Path toAbsolutePath() { return w(d.toAbsolutePath()); }
        public Path toRealPath(LinkOption... o) throws IOException { return w(d.toRealPath(o)); }
        public File toFile() { return new File(d.toString()); }
        public WatchKey register(WatchService s, WatchEvent.Kind<?>[] e, WatchEvent.Modifier... m) throws IOException { return d.register(s, e, m); }
        public int compareTo(Path o) { return d.compareTo(un(o)); }
        public boolean equals(Object o) { return o instanceof P p && d.equals(p.d); }
        public int hashCode() { return d.hashCode(); }
        public String toString() { return d.toString(); }
    }
}

"""Windows equivalents of the Linux uid/mode/realpath guards, read from the real token and DACL.

Native allocation and handle lifetimes are bounded. The existing SID-set
policy is unchanged; this is not a complete access-check or path-race proof.
"""
import ctypes, os, pathlib, re
from ctypes import wintypes as w

_a = ctypes.WinDLL("advapi32", use_last_error=True)
_k = ctypes.WinDLL("kernel32", use_last_error=True)
_ptr = ctypes.c_void_p
_MAX_TOKEN = 1048576
_MAX_SD = 65536
_k.GetCurrentProcess.argtypes = []
_k.GetCurrentProcess.restype = w.HANDLE
_k.CloseHandle.argtypes = [w.HANDLE]
_k.CloseHandle.restype = w.BOOL
_k.LocalFree.argtypes = [_ptr]
_k.LocalFree.restype = _ptr
_a.OpenProcessToken.argtypes = [w.HANDLE, w.DWORD, ctypes.POINTER(w.HANDLE)]
_a.OpenProcessToken.restype = w.BOOL
_a.GetTokenInformation.argtypes = [w.HANDLE, ctypes.c_int, _ptr, w.DWORD, ctypes.POINTER(w.DWORD)]
_a.GetTokenInformation.restype = w.BOOL
_a.ConvertSidToStringSidW.argtypes = [_ptr, ctypes.POINTER(_ptr)]
_a.ConvertSidToStringSidW.restype = w.BOOL
_a.IsValidSid.argtypes = [_ptr]
_a.IsValidSid.restype = w.BOOL
_a.GetNamedSecurityInfoW.argtypes = [w.LPCWSTR, ctypes.c_int, w.DWORD, ctypes.POINTER(_ptr), _ptr, ctypes.POINTER(_ptr), _ptr, ctypes.POINTER(_ptr)]
_a.GetNamedSecurityInfoW.restype = w.DWORD
_a.GetSecurityDescriptorLength.argtypes = [_ptr]
_a.GetSecurityDescriptorLength.restype = w.DWORD
_a.IsValidSecurityDescriptor.argtypes = [_ptr]
_a.IsValidSecurityDescriptor.restype = w.BOOL
_a.GetAclInformation.argtypes = [_ptr, _ptr, w.DWORD, ctypes.c_int]
_a.GetAclInformation.restype = w.BOOL
_a.GetAce.argtypes = [_ptr, w.DWORD, ctypes.POINTER(_ptr)]
_a.GetAce.restype = w.BOOL

def _refuse():
    raise ValueError("C9W_GUARD_NATIVE_SHAPE")

def _within(p, base, length, minimum):
    # Integer-only range check before dereferencing a native interior pointer.
    return (type(p) is int and type(base) is int and base > 0 and
            type(length) is int and type(minimum) is int and
            minimum > 0 and length >= minimum and
            base <= p <= base + length - minimum)

def _free(p):
    if p and _k.LocalFree(p):
        raise OSError("C9W_GUARD_LOCAL_FREE")

def _sid(p, base, length):
    if not _within(p, base, length, 8):
        _refuse()
    # SID revision/count bytes precede a six-byte identifier authority.
    count = ctypes.c_ubyte.from_address(p + 1).value
    if count > 15 or not _within(p, base, length, 8 + 4 * count):
        _refuse()
    if not _a.IsValidSid(p):
        _refuse()
    s = _ptr()
    if not _a.ConvertSidToStringSidW(p, ctypes.byref(s)):
        raise ctypes.WinError(ctypes.get_last_error())
    try:
        if not s.value:
            _refuse()
        return ctypes.wstring_at(s.value)
    finally:
        _free(s.value)

def _token(cls, minimum):
    t, n = w.HANDLE(), w.DWORD()
    if not _a.OpenProcessToken(_k.GetCurrentProcess(), 8, ctypes.byref(t)):
        raise ctypes.WinError(ctypes.get_last_error())
    try:
        ok = _a.GetTokenInformation(t, cls, None, 0, ctypes.byref(n))
        error = ctypes.get_last_error()
        if ok or error != 122 or not minimum <= n.value <= _MAX_TOKEN:
            _refuse()
        b = ctypes.create_string_buffer(n.value)
        if not _a.GetTokenInformation(t, cls, b, len(b), ctypes.byref(n)):
            raise ctypes.WinError(ctypes.get_last_error())
        if not minimum <= n.value <= len(b):
            _refuse()
        return b, n.value
    finally:
        if not _k.CloseHandle(t):
            raise ctypes.WinError(ctypes.get_last_error())

def _dword(cls):
    held, _ = _token(cls, ctypes.sizeof(w.DWORD))
    return w.DWORD.from_buffer(held).value

def _token_sid(cls):
    held, actual = _token(cls, ctypes.sizeof(_ptr))
    # TOKEN_USER / TOKEN_APPCONTAINER_INFORMATION start with a SID pointer.
    base = ctypes.addressof(held)
    p = _ptr.from_buffer(held).value
    # Reject a pointer into the structure's pointer header, even if in bounds.
    if not _within(p, base + ctypes.sizeof(_ptr), actual - ctypes.sizeof(_ptr), 8):
        _refuse()
    return _sid(p, base, actual)

def user_sid():
    return _token_sid(1)

def container_sid():
    if _dword(29) != 1: return None
    return _token_sid(31)

def contained_nonadmin():
    """In an AppContainer, with zero capabilities and no elevation (the nonroot + networkless namespace guard)."""
    return (container_sid() is not None and _dword(30) == 0
            and _dword(20) == 0)

def acl_sids(path):
    owner, dacl, sd = _ptr(), _ptr(), _ptr()
    rc = _a.GetNamedSecurityInfoW(str(path), 1, 5, ctypes.byref(owner), None, ctypes.byref(dacl), None, ctypes.byref(sd))
    if rc: raise ctypes.WinError(rc)
    # All returned interior pointers are valid only while the descriptor lives.
    # Always free the one owning allocation, including null/unsupported DACL.
    try:
        if not sd.value or not _a.IsValidSecurityDescriptor(sd):
            _refuse()
        length = _a.GetSecurityDescriptorLength(sd)
        if not 20 <= length <= _MAX_SD:
            _refuse()
        if not dacl.value:
            return None, None
        if not _within(dacl.value, sd.value, length, 8):
            _refuse()
        # ACL header: revision, sbz1, AclSize, AceCount, sbz2.
        size = ctypes.c_ushort.from_address(dacl.value + 2).value
        if not _within(dacl.value, sd.value, length, size) or size < 8:
            _refuse()
        info = (w.DWORD * 3)()
        if not _a.GetAclInformation(dacl, ctypes.byref(info), ctypes.sizeof(info), 2):
            raise ctypes.WinError(ctypes.get_last_error())
        count, used, free = info
        if not 8 <= used <= size or used + free != size or count > (used - 8) // 4:
            _refuse()
        out = []
        cursor = dacl.value + 8
        for i in range(count):
            ace = _ptr()
            if not _a.GetAce(dacl, i, ctypes.byref(ace)):
                raise ctypes.WinError(ctypes.get_last_error())
            if ace.value != cursor or not _within(ace.value, dacl.value, used, 4):
                _refuse()
            ace_type = ctypes.c_ubyte.from_address(ace.value).value
            ace_size = ctypes.c_ushort.from_address(ace.value + 2).value
            if ace_size < 4 or ace_size % 4 or not _within(ace.value, dacl.value, used, ace_size):
                _refuse()
            if ace_type != 0:
                return None, None  # only ALLOW aces are understood
            if ace_size < 16:
                _refuse()
            out.append(_sid(ace.value + 8, ace.value, ace_size))
            cursor += ace_size
        if cursor != dacl.value + used:
            _refuse()
        return _sid(owner.value, sd.value, length), out
    finally:
        _free(sd.value)

def _private(path, extra=()):
    owner, sids = acl_sids(path)
    allowed = {user_sid(), container_sid(), *extra} - {None}
    return owner == user_sid() and bool(sids) and set(sids) <= allowed

def _reparse(path): return bool(os.lstat(path).st_file_attributes & 0x400)
def private_file(path): return path.is_file() and not _reparse(path) and _private(path)
def private_dir(path): return path.is_dir() and not _reparse(path) and _private(path)
def canonical(path):
    s = str(path)
    return (path.exists() and not _reparse(path) and re.match(r"^[A-Za-z]:\\", s) is not None
            and ".." not in pathlib.PureWindowsPath(s).parts and os.path.normpath(s) == s and not s.startswith("\\\\"))

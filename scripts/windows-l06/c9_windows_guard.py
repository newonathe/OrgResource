"""Windows equivalents of the Linux uid/mode/realpath guards, read from the real token and DACL."""
import ctypes, os, pathlib, re
from ctypes import wintypes as w

_a = ctypes.WinDLL("advapi32", use_last_error=True)
_k = ctypes.WinDLL("kernel32", use_last_error=True)
_k.GetCurrentProcess.restype = w.HANDLE
_a.OpenProcessToken.argtypes = [w.HANDLE, w.DWORD, ctypes.POINTER(w.HANDLE)]
_a.GetTokenInformation.argtypes = [w.HANDLE, ctypes.c_int, ctypes.c_void_p, w.DWORD, ctypes.POINTER(w.DWORD)]
_a.ConvertSidToStringSidW.argtypes = [ctypes.c_void_p, ctypes.POINTER(w.LPWSTR)]
_a.GetNamedSecurityInfoW.argtypes = [w.LPCWSTR, ctypes.c_int, w.DWORD, ctypes.POINTER(ctypes.c_void_p), ctypes.c_void_p, ctypes.POINTER(ctypes.c_void_p), ctypes.c_void_p, ctypes.POINTER(ctypes.c_void_p)]
_a.GetNamedSecurityInfoW.restype = w.DWORD
_a.GetAclInformation.argtypes = [ctypes.c_void_p, ctypes.c_void_p, w.DWORD, ctypes.c_int]
_a.GetAce.argtypes = [ctypes.c_void_p, w.DWORD, ctypes.POINTER(ctypes.c_void_p)]

def _sid(p):
    s = w.LPWSTR()
    if not _a.ConvertSidToStringSidW(p, ctypes.byref(s)): raise ctypes.WinError(ctypes.get_last_error())
    return s.value

def _token(cls):
    t, n = w.HANDLE(), w.DWORD()
    if not _a.OpenProcessToken(_k.GetCurrentProcess(), 8, ctypes.byref(t)): raise ctypes.WinError(ctypes.get_last_error())
    try:
        _a.GetTokenInformation(t, cls, None, 0, ctypes.byref(n))
        b = ctypes.create_string_buffer(n.value or 4)
        if not _a.GetTokenInformation(t, cls, b, len(b), ctypes.byref(n)): raise ctypes.WinError(ctypes.get_last_error())
        return b
    finally:
        _k.CloseHandle(t)

def _dword(cls):
    held = _token(cls)
    return ctypes.cast(held, ctypes.POINTER(w.DWORD))[0]

def user_sid():
    held = _token(1)  # the SID pointer is only valid while this buffer lives
    return _sid(ctypes.cast(held, ctypes.POINTER(ctypes.c_void_p))[0])
def container_sid():
    if _dword(29) != 1: return None
    held = _token(31)
    return _sid(ctypes.cast(held, ctypes.POINTER(ctypes.c_void_p))[0])
def contained_nonadmin():
    """In an AppContainer, with zero capabilities and no elevation (the nonroot + networkless namespace guard)."""
    return (container_sid() is not None and _dword(30) == 0
            and _dword(20) == 0)

def acl_sids(path):
    owner, dacl, sd = ctypes.c_void_p(), ctypes.c_void_p(), ctypes.c_void_p()
    rc = _a.GetNamedSecurityInfoW(str(path), 1, 5, ctypes.byref(owner), None, ctypes.byref(dacl), None, ctypes.byref(sd))
    if rc: raise ctypes.WinError(rc)
    if not dacl.value: return None, None
    info = (w.DWORD * 3)()
    if not _a.GetAclInformation(dacl, ctypes.byref(info), 12, 2): raise ctypes.WinError(ctypes.get_last_error())
    out = []
    for i in range(info[0]):
        ace = ctypes.c_void_p()
        if not _a.GetAce(dacl, i, ctypes.byref(ace)): raise ctypes.WinError(ctypes.get_last_error())
        if ctypes.cast(ace, ctypes.POINTER(ctypes.c_ubyte))[0] != 0: return None, None  # only ALLOW aces are understood
        out.append(_sid(ace.value + 8))
    return _sid(owner), out

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

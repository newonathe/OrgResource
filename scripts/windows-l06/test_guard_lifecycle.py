"""Windows-only fictional native-call spies. No real token, ACL or file mutation.
These tests exercise local lifetime/error paths, not actual containment.
"""
import ctypes
import unittest
from unittest.mock import Mock, patch
import c9_windows_guard as g

class GuardLifecycle(unittest.TestCase):
    def setUp(self):
        self.a, self.k = Mock(), Mock()
        self.k.LocalFree.return_value = None
        self.k.CloseHandle.return_value = 1
        self.k.GetCurrentProcess.return_value = 123
        self.a.IsValidSid.return_value = 1
        self.a.IsValidSecurityDescriptor.return_value = 1
        self.enterContext(patch.object(g, "_a", self.a))
        self.enterContext(patch.object(g, "_k", self.k))

    def fake_sid(self, buffer, offset=0):
        p = ctypes.addressof(buffer) + offset
        ctypes.memmove(p, b"\x01\x01\x00\x00\x00\x00\x00\x05\x15\x00\x00\x00", 12)
        return p

    def test_range(self):
        self.assertTrue(g._within(100,100,8,8))
        self.assertFalse(g._within(None,100,8,8))
        self.assertFalse(g._within(99,100,8,8))
        self.assertFalse(g._within(101,100,8,8))
        self.assertFalse(g._within(100,100,8,0))

    def test_sid_allocation_released(self):
        b = ctypes.create_string_buffer(12)
        p = self.fake_sid(b)
        text = ctypes.create_unicode_buffer("S-1-5-21")
        def convert(_, out):
            ctypes.cast(out, ctypes.POINTER(ctypes.c_void_p))[0] = ctypes.addressof(text)
            return 1
        self.a.ConvertSidToStringSidW.side_effect = convert
        self.assertEqual(g._sid(p,p,12),"S-1-5-21")
        self.k.LocalFree.assert_called_once_with(ctypes.addressof(text))

    def test_sid_outside_and_truncated_refused(self):
        b = ctypes.create_string_buffer(12)
        p = self.fake_sid(b)
        for address,length in ((p-1,12),(p,8),(p+12,12)):
            with self.assertRaisesRegex(ValueError,"C9W_GUARD_NATIVE_SHAPE"):
                g._sid(address,p,length)
        self.a.IsValidSid.assert_not_called()
        self.a.ConvertSidToStringSidW.assert_not_called()
        self.k.LocalFree.assert_not_called()

    def token_spy(self, returned=4, second_ok=True):
        self.a.OpenProcessToken.return_value = 1
        def query(t, cls, buf, size, out):
            n = ctypes.cast(out, ctypes.POINTER(g.w.DWORD))
            if buf is None:
                n[0] = 4
                ctypes.set_last_error(122)
                return 0
            n[0] = returned
            ctypes.set_last_error(5)
            return int(second_ok)
        self.a.GetTokenInformation.side_effect = query

    def test_token_releases_success(self):
        self.token_spy()
        self.assertEqual(g._dword(20),0)
        self.k.CloseHandle.assert_called_once()

    def test_token_second_failure_releases(self):
        self.token_spy(second_ok=False)
        with self.assertRaises(OSError): g._dword(20)
        self.k.CloseHandle.assert_called_once()

    def test_token_length_refusal_releases(self):
        self.token_spy(returned=5)
        with self.assertRaisesRegex(ValueError,"C9W_GUARD_NATIVE_SHAPE"): g._dword(20)
        self.k.CloseHandle.assert_called_once()

    def test_token_close_failure_refuses(self):
        self.token_spy()
        self.k.CloseHandle.return_value = 0
        with self.assertRaises(OSError): g._dword(20)

    def descriptor_spy(self, dacl=False):
        b = ctypes.create_string_buffer(64)
        p = ctypes.addressof(b)
        def security(path, kind, info, owner, group, acl, sacl, sd):
            ctypes.cast(sd,ctypes.POINTER(ctypes.c_void_p))[0] = p
            if dacl:
                ctypes.cast(acl,ctypes.POINTER(ctypes.c_void_p))[0] = p + 32
                ctypes.c_ushort.from_address(p + 34).value = 8
            return 0
        self.a.GetNamedSecurityInfoW.side_effect = security
        self.a.GetSecurityDescriptorLength.return_value = 64
        return b

    def test_null_dacl_releases(self):
        b = self.descriptor_spy()
        self.assertEqual(g.acl_sids("fictional"),(None,None))
        self.k.LocalFree.assert_called_once_with(ctypes.addressof(b))

    def test_acl_query_failure_releases(self):
        b = self.descriptor_spy(dacl=True)
        self.a.GetAclInformation.return_value = 0
        with self.assertRaises(OSError): g.acl_sids("fictional")
        self.k.LocalFree.assert_called_once_with(ctypes.addressof(b))

    def test_descriptor_length_refusal_releases(self):
        b = self.descriptor_spy()
        self.a.GetSecurityDescriptorLength.return_value = 65537
        with self.assertRaisesRegex(ValueError,"C9W_GUARD_NATIVE_SHAPE"): g.acl_sids("fictional")
        self.k.LocalFree.assert_called_once_with(ctypes.addressof(b))

    def allow_ace_spy(self, kind=0, bad_address=False):
        b = ctypes.create_string_buffer(64)
        p = ctypes.addressof(b)
        owner = self.fake_sid(b,20)
        self.fake_sid(b,48)
        ctypes.c_ushort.from_address(p+34).value = 28  # ACL header + one ACE
        ctypes.c_ubyte.from_address(p+40).value = kind
        ctypes.c_ushort.from_address(p+42).value = 20  # ACE header/mask + SID
        def security(path, typ, info, own, group, acl, sacl, sd):
            ctypes.cast(own,ctypes.POINTER(ctypes.c_void_p))[0] = owner
            ctypes.cast(acl,ctypes.POINTER(ctypes.c_void_p))[0] = p+32
            ctypes.cast(sd,ctypes.POINTER(ctypes.c_void_p))[0] = p
            return 0
        def info(acl, out, size, cls):
            fields = ctypes.cast(out,ctypes.POINTER(g.w.DWORD))
            fields[0], fields[1], fields[2] = 1,28,0
            return 1
        def get_ace(acl, index, out):
            ctypes.cast(out,ctypes.POINTER(ctypes.c_void_p))[0] = p+64 if bad_address else p+40
            return 1
        self.a.GetNamedSecurityInfoW.side_effect = security
        self.a.GetSecurityDescriptorLength.return_value = 64
        self.a.GetAclInformation.side_effect = info
        self.a.GetAce.side_effect = get_ace
        return b

    def test_allow_ace_and_owner_free_separately(self):
        b = self.allow_ace_spy()
        values = [ctypes.create_unicode_buffer(v) for v in ("S-1-5-21","S-1-5-21")]
        calls = iter(values)
        def convert(_, out):
            ctypes.cast(out,ctypes.POINTER(ctypes.c_void_p))[0] = ctypes.addressof(next(calls))
            return 1
        self.a.ConvertSidToStringSidW.side_effect = convert
        self.assertEqual(g.acl_sids("fictional"),("S-1-5-21",["S-1-5-21"]))
        from unittest.mock import call
        self.assertEqual(self.k.LocalFree.call_args_list,
                         [call(ctypes.addressof(v)) for v in values]+[call(ctypes.addressof(b))])

    def test_unsupported_ace_releases_without_sid_read(self):
        b = self.allow_ace_spy(kind=1)
        self.assertEqual(g.acl_sids("fictional"),(None,None))
        self.a.ConvertSidToStringSidW.assert_not_called()
        self.k.LocalFree.assert_called_once_with(ctypes.addressof(b))

    def test_outside_ace_refusal_releases(self):
        b = self.allow_ace_spy(bad_address=True)
        with self.assertRaisesRegex(ValueError,"C9W_GUARD_NATIVE_SHAPE"):
            g.acl_sids("fictional")
        self.a.ConvertSidToStringSidW.assert_not_called()
        self.k.LocalFree.assert_called_once_with(ctypes.addressof(b))

    def test_free_failure_refuses(self):
        self.k.LocalFree.return_value = 1
        with self.assertRaisesRegex(OSError,"C9W_GUARD_LOCAL_FREE"): g._free(123)

if __name__ == "__main__":
    unittest.main()

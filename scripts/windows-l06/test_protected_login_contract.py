"""Fictional masked strings only; no editor, database, credential or file access."""
import unittest
import protected_login as login

class ProtectedLoginContract(unittest.TestCase):
    def setUp(self):
        self.raw = b"[c9-backup]\nuser = c9_backup\npassword = *****\nhost = 127.0.0.1\n"
    def check(self, output=None, section="c9-backup", user="c9_backup", set_exit=0, print_exit=0):
        return login.validate_editor_result(section,user,set_exit,print_exit,
                                            self.raw if output is None else output)
    def test_exact_masked(self):
        self.check()
        self.check(self.raw.replace(b"\n",b"\r\n"))
        self.check(b'[c9-backup]\n host = "127.0.0.1"\n password = "*****"\n user = "c9_backup"')
        self.check(self.raw.replace(b"backup",b"restore"),"c9-restore","c9_restore")
    def test_exit_failure(self):
        for code in (1,124,-1,None,False,0.0):
            with self.assertRaises(ValueError): self.check(set_exit=code)
            with self.assertRaises(ValueError): self.check(print_exit=code)
    def test_scope(self):
        for section,user in (("client","c9_backup"),("c9-backup","root"),
                             ("c9-restore","c9_backup"),("C9-backup","c9_backup")):
            with self.assertRaises(ValueError): self.check(section=section,user=user)
    def test_unmasked(self):
        for secret in (b"fictional-secret",b"***x",b'"***',b'***"',b""):
            with self.assertRaisesRegex(ValueError,"^C9W_PROTECTED_LOGIN_RESULT_REFUSED$"):
                self.check(self.raw.replace(b"*****",secret))
    def test_wrong_host_user(self):
        for raw in (self.raw.replace(b"127.0.0.1",b"localhost"),
                    self.raw.replace(b"c9_backup",b"root")):
            with self.assertRaises(ValueError): self.check(raw)
    def test_duplicate_extra_missing(self):
        for raw in (self.raw+b"user = c9_backup\n",
                    self.raw+b"[client]\n",
                    self.raw.replace(b"host = 127.0.0.1\n",b"port = 3306\n"),
                    self.raw.replace(b"host = 127.0.0.1\n",b""),
                    self.raw+b"\n",
                    self.raw.replace(b"user =",b"User =")):
            with self.assertRaises(ValueError): self.check(raw)
    def test_malformed_and_bound(self):
        for raw in (b"",b"x"*8193,self.raw+b"\x00",self.raw+b"\xff",
                    self.raw.replace(b"\n",b"\r"),self.raw.decode("ascii")):
            with self.assertRaises(ValueError): self.check(raw)

if __name__=="__main__":
    unittest.main()

"""Fictional masked inventory only; no editor, database, credential or file access."""
import unittest
import protected_login as login

class EditorInventoryContract(unittest.TestCase):
    def raw(self, kind):
        return ("[c9-" + kind + "]\nuser = c9_" + kind +
                "\npassword = *****\nhost = 127.0.0.1\n").encode("ascii")

    def check(self, role="source", kind="backup", selected=None, entire=None,
              set_exit=0, selected_exit=0, all_exit=0):
        raw = self.raw(kind)
        login.validate_editor_inventory(role, kind, set_exit, selected_exit,
                                        raw if selected is None else selected,
                                        all_exit, raw if entire is None else entire)

    def test_four_pairs(self):
        for role, kind in (("source", "backup"), ("restore", "backup"),
                           ("restore", "restore"), ("mixed", "restore")):
            with self.subTest(role=role, kind=kind):
                self.check(role, kind, entire=self.raw(kind).replace(b"\n", b"\r\n"))

    def test_unexpected_default_or_second_group(self):
        raw = self.raw("backup")
        for extra in (b"[client]\nuser = other\n", b"[mysql]\n",
                      b"[mysqldump]\n", self.raw("restore"), raw):
            with self.subTest(extra=extra):
                with self.assertRaisesRegex(ValueError, "^C9W_PROTECTED_LOGIN_RESULT_REFUSED$"):
                    self.check(entire=raw + extra)
                with self.assertRaises(ValueError):
                    self.check(entire=extra + raw)

    def test_independent_exit_and_print_disagreement(self):
        raw = self.raw("backup")
        for code in (1, -1, 124, False, None, 0.0):
            with self.assertRaises(ValueError):
                self.check(all_exit=code)
            with self.assertRaises(ValueError):
                self.check(selected_exit=code)
            with self.assertRaises(ValueError):
                self.check(set_exit=code)
        for bad in (raw.replace(b"*****", b"******"),
                    raw.replace(b"c9_backup", b'"c9_backup"'),
                    self.raw("restore"), b"", raw + b"\n", b"x" * 8193):
            with self.assertRaises(ValueError):
                self.check(entire=bad)

    def test_role_binding(self):
        for role, kind in (("source", "restore"), ("mixed", "backup"),
                           ("Source", "backup"), ("other", "backup")):
            with self.assertRaises(ValueError):
                self.check(role, kind)
        with self.assertRaises(ValueError):
            login.validate_editor_inventory([], "backup", 0, 0, b"", 0, b"")
        with self.assertRaises(ValueError):
            login.validate_editor_inventory("source", [], 0, 0, b"", 0, b"")

if __name__ == "__main__":
    unittest.main()

"""Fictional portable checks; no host tool, private inputs or Windows acceptance."""
from pathlib import Path
import tempfile
import contextlib
import io
import unittest
from unittest.mock import patch
import runtime_copy as r


class RuntimeCopyTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.src = self.root / "src"
        self.dst = self.root / "dst"
        self.src.mkdir()
        (self.src / "nested").mkdir()
        (self.src / "nested" / "a").write_bytes(b"abc")
        (self.src / "empty").mkdir()
        (self.src / "python.exe").write_bytes(b"fictional")

    def test_exact_copy(self):
        self.assertEqual(r.copy_verified(self.src, self.dst, "all"), r.inventory(self.dst))

    def test_cli_receipt(self):
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            self.assertEqual(r.main(["all", str(self.src), str(self.dst)]), 0)
        self.assertRegex(output.getvalue(), r"^entries=4 sha256=[a-f0-9]{64}\n$")

    def test_existing_empty_target(self):
        self.dst.mkdir()
        self.assertEqual(r.copy_verified(self.src, self.dst, "all"), r.inventory(self.dst))

    def test_nonempty_target_refusal(self):
        self.dst.mkdir()
        (self.dst / "sentinel").write_bytes(b"keep")
        with self.assertRaises(ValueError):
            r.copy_verified(self.src, self.dst, "all")
        self.assertEqual((self.dst / "sentinel").read_bytes(), b"keep")

    def test_overlap_refusal(self):
        with self.assertRaises(ValueError):
            r.copy_verified(self.src, self.src / "child", "all")
        self.assertFalse((self.src / "child").exists())

    def test_bin_scope(self):
        r.copy_verified(self.src, self.dst, "bin")
        self.assertEqual(set(r.inventory(self.dst)), {"python.exe"})

    def test_lib_exclusions(self):
        (self.src / "nested" / "__pycache__").mkdir()
        (self.src / "nested" / "__pycache__" / "ignored").write_bytes(b"x")
        r.copy_verified(self.src, self.dst, "lib")
        self.assertFalse((self.dst / "nested" / "__pycache__").exists())
        self.assertTrue((self.dst / "nested" / "a").exists())

    def test_source_mutation(self):
        original = r.shutil.copyfileobj
        def mutate(inp, out):
            original(inp, out)
            (self.src / "python.exe").write_bytes(b"changed")
        with patch.object(r.shutil, "copyfileobj", side_effect=mutate):
            with self.assertRaisesRegex(ValueError, "source changed"):
                r.copy_verified(self.src, self.dst, "all")

    def test_destination_mutation(self):
        original = r.shutil.copyfileobj
        def mutate(inp, out):
            original(inp, out)
            out.write(b"bad")
        with patch.object(r.shutil, "copyfileobj", side_effect=mutate):
            with self.assertRaisesRegex(ValueError, "destination mismatch"):
                r.copy_verified(self.src, self.dst, "all")

    def test_extra_destination_entry(self):
        original = r.shutil.copyfileobj
        def mutate(inp, out):
            original(inp, out)
            (self.dst / "extra").write_bytes(b"x")
        with patch.object(r.shutil, "copyfileobj", side_effect=mutate):
            with self.assertRaisesRegex(ValueError, "destination mismatch"):
                r.copy_verified(self.src, self.dst, "all")

    def test_link_refusal(self):
        (self.src / "link").symlink_to(self.src / "nested", target_is_directory=True)
        with self.assertRaises(ValueError):
            r.copy_verified(self.src, self.dst, "all")
        self.assertFalse(self.dst.exists())

    def test_case_collision(self):
        (self.src / "nested" / "A").write_bytes(b"other")
        if len(list((self.src / "nested").iterdir())) != 2:
            self.skipTest("case-insensitive filesystem")
        with self.assertRaisesRegex(ValueError, "case collision"):
            r.copy_verified(self.src, self.dst, "all")


if __name__ == "__main__":
    unittest.main()

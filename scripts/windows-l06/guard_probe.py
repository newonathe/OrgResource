"""Contained positive and negative checks of c9_windows_guard. Prints one static marker only."""
import pathlib, sys
import c9_windows_guard as g
good, wide, good_dir, wide_dir = map(pathlib.Path, sys.argv[1:])
cases = [g.contained_nonadmin() is True, g.container_sid().startswith("S-1-15-2-"), g.user_sid().startswith("S-1-5-21-"),
         g.private_file(good) is True, g.private_file(wide) is False, g.private_file(good_dir) is False,
         g.private_dir(good_dir) is True, g.private_dir(wide_dir) is False,
         g.canonical(good) is True, g.canonical(good_dir) is True, g.canonical(pathlib.Path(str(good_dir) + "\\..\\x")) is False, g.canonical(pathlib.Path("relative")) is False]
if all(cases): print("C9W_GUARD_PROBE_OK")
else: sys.exit("C9W_GUARD_PROBE_FAILED:" + ",".join(str(i) for i, c in enumerate(cases) if not c))

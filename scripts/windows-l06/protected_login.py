"""Feed mysql_config_editor's console prompt from a one-time environment value (same method as the C6a operator drill)."""
import os, re, subprocess, sys, time
from winpty import PtyProcess
exe, section, user = sys.argv[1:]
password = os.environ.pop("C9W_EDITOR_PASSWORD")
p = PtyProcess.spawn([exe, "set", f"--login-path={section}", "--host=127.0.0.1", f"--user={user}", "--password"], env=os.environ.copy())
time.sleep(0.3)
p.write(password + "\r")
cut = time.monotonic() + 10
while p.isalive() and time.monotonic() < cut:
    time.sleep(0.05)
if p.isalive():
    p.close(force=True)
    sys.exit(1)
p.close()
out = subprocess.run([exe, "print", f"--login-path={section}"], capture_output=True, text=True).stdout
ok = all(re.search(x, out, re.M) for x in (rf"^\[{re.escape(section)}\]\s*$", rf'^[ \t]*user\s*=\s*"?{re.escape(user)}"?\s*$',
                                           r'^[ \t]*password\s*=\s*"?\*+"?\s*$', r'^[ \t]*host\s*=\s*"?127\.0\.0\.1"?\s*$'))
sys.exit(0 if ok else 1)

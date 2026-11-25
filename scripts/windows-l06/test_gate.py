"""Owner-gate checks on a real Windows console (ConPTY) with fictional digests. The approval function itself is only ever
run where it must refuse. The reader and the matcher are tested separately, so no positive approval is produced."""
import base64, os, sys, time
from winpty import PtyProcess

gate, pwsh = sys.argv[1:]
A, B = "1" * 64, "2" * 64
GOOD = f"I VERIFIED ISOLATED RESTORE {A} {B}"

def run(name, script, keys=None, before=None, want=(), forbid=("MATCH=True", "RESULT=True"), env=None, wait=14):
    p = PtyProcess.spawn([pwsh, "-NoProfile", "-Command", f". '{gate}'; try {{ {script} }} catch {{ \"REFUSED=$($_.Exception.Message)\" }}; 'DONE=1'"], env={**os.environ, **(env or {})}, dimensions=(40, 1200))
    if before: p.write(before)
    text, end, sent = "", time.monotonic() + wait, False
    while time.monotonic() < end:
        try: text += p.read(4096)
        except EOFError: break
        if not sent and "READY=1" in text:
            time.sleep(0.8); sent = True
            for ch in keys or "": p.write(ch); time.sleep(0.004)
    if p.isalive(): p.close(force=True)
    flat = "".join(text.split())
    ok = all(w.replace(" ", "") in flat for w in want) and not any(f in flat for f in forbid)
    print(("C9W_GATE_OK:" if ok else "C9W_GATE_FAIL:") + name, flush=True)
    return ok

echo = "'READY=1'; $r = Read-C9Reply 8; 'ECHO=' + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes(\"$r\"))"
match = f"'READY=1'; $r = Read-C9Reply 6; 'MATCH=' + [bool](Test-C9Response '{A}' '{B}' \"$r\")"
b64 = lambda s: "ECHO=" + base64.b64encode(s.encode()).decode()
LONG = "aB3 " * 45 + "END"
results = [
    run("reader_echo_mixed", echo, "Abc XYZ 019-_.!\r", want=[b64("Abc XYZ 019-_.!")]),
    run("reader_echo_backspace", echo, "abcX\x7fd\r", want=[b64("abcd")]) or run("reader_echo_backspace_bs", echo, "abcX\x08d\r", want=[b64("abcd")]),
    run("reader_echo_183_characters", echo, LONG + "\r", want=[b64(LONG)], wait=20),
    run("reader_discards_typed_ahead", "Start-Sleep 2; " + echo, "fresh\r", before="stale", want=[b64("fresh")]),
    run("reader_expires", "'READY=1'; $r = Read-C9Reply 2; 'NULL=' + ($null -eq $r)", None, want=["R06B2_OPERATOR_EXPIRED", "NULL=True"]),
    run("reader_partial_then_expires", "'READY=1'; $r = Read-C9Reply 3; 'NULL=' + ($null -eq $r)", "partial", want=["NULL=True"]),
    run("declined_empty", match, "\r", want=["MATCH=False"]),
    run("wrong_text", match, "yes\r", want=["MATCH=False"]),
    run("trailing_space", match, GOOD + " \r", want=["MATCH=False"]),
    run("lower_case", match, GOOD.lower() + "\r", want=["MATCH=False"]),
    run("swapped_digests", match, f"I VERIFIED ISOLATED RESTORE {B} {A}\r", want=["MATCH=False"]),
    run("approval_refused_in_agent_session", f"$r = Confirm-C9Operator '{A}' '{B}' 5; 'RESULT=' + $r", None, env={"CLAUDECODE": "1"}, want=["REFUSED=R06B2_AGENT_SESSION_REFUSED"]),
    run("approval_refused_in_ci", f"$r = Confirm-C9Operator '{A}' '{B}' 5; 'RESULT=' + $r", None, env={"GITHUB_ACTIONS": "true"}, want=["REFUSED=R06B2_OPERATOR_CI_REFUSED"]),
    run("approval_rejects_long_timeout", f"$r = Confirm-C9Operator '{A}' '{B}' 301; 'RESULT=' + $r", None, want=["REFUSED=R06B2_OPERATOR_ARGUMENTS"]),
]
sys.exit(0 if all(results) else 1)

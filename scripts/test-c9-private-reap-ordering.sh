#!/usr/bin/env bash
# Exercise cleanup ordering with owned short-lived shell children and a
# deterministic guard test double. This is NOT a private JVM/PIDFD stop proof.
set -euo pipefail
umask 077
[[ "${GITHUB_ACTIONS:-}" = true && "${RUNNER_OS:-}" = Linux ]] || exit 1
source scripts/c9-private-mixed-recovered-control.sh
work="$(mktemp -d)"
trap 'rm -rf -- "$work"' EXIT
record="$work/mixed-runtime.pid"
calls=0
mode=late
# Only model the guard result; never signal any PID.
mixed_runtime_pidfd_stop() {
  calls=$((calls+1))
  [[ "$1" = 'net:[123]' ]] || exit 1
  case "$mode" in
    late) ! kill -0 "$mixed_supervisor" 2>/dev/null ;;
    transient) [ "$calls" -gt 1 ] && ! kill -0 "$mixed_supervisor" 2>/dev/null ;;
    persistent) return 1 ;;
    immediate) return 0 ;;
    *) exit 1 ;;
  esac
}
# No record and a child that exits before writing one.
(sleep 0.1) & mixed_supervisor=$!
mixed_runtime_reap 'net:[123]'
[[ "$calls" = 0 && ! -e "$record" ]]
# Deterministic late record: release only after entering the wait path.
# The first -s check has no record; a short child writes before exit.
(sleep 0.2; printf 'fictional record\n' > "$record") & mixed_supervisor=$!
mixed_runtime_reap 'net:[123]'
[[ "$calls" = 1 && -s "$record" ]]
# Present record, transient first guard refusal, successful guarded recheck.
mode=transient; calls=0
(sleep 0.1) & mixed_supervisor=$!
mixed_runtime_reap 'net:[123]'
[[ "$calls" = 2 ]]
# Persistent refusal must fail, leaving record in custody.
mode=persistent; calls=0
(sleep 0.1) & mixed_supervisor=$!
if mixed_runtime_reap 'net:[123]'; then exit 1; fi
[[ "$calls" = 2 && -s "$record" ]]
# Immediate positive guard result still waits for the known child.
mode=immediate; calls=0
(sleep 0.1) & mixed_supervisor=$!
mixed_runtime_reap 'net:[123]'
[[ "$calls" = 1 ]] && ! kill -0 "$mixed_supervisor" 2>/dev/null
# An empty record or dangling link is not mistaken for no record.
mode=persistent; calls=0
rm -f -- "$record"; ln -s "$work/missing" "$record"
(sleep 0.1) & mixed_supervisor=$!
if mixed_runtime_reap 'net:[123]'; then exit 1; fi
[[ "$calls" = 1 && -L "$record" ]]
echo R06B3B_PRIVATE_REAP_ORDERING_OK:late_record=true transient_recheck=true persistent_refusal=true no_numeric_signal=true

#!/usr/bin/env bash
# Sourced helper. Exact matching is deterministic; an interactive operator
# decision is separate. No stdin piping, environment token or CLI yes flag.
c9_operator_challenge() {
  [[ $# -eq 2 && "$1" =~ ^[0-9a-f]{64}$ && "$2" =~ ^[0-9a-f]{64}$ ]] || return 1
  printf 'I VERIFIED ISOLATED RESTORE %s %s\n' "$1" "$2"
}
c9_operator_response_matches() {
  [[ $# -eq 3 ]] || return 1
  local expected
  expected="$(c9_operator_challenge "$1" "$2")" || return 1
  [[ "$3" = "$expected" ]]
}
c9_require_operator_confirmation() {
  local challenge reply
  if [[ "${GITHUB_ACTIONS:-}" = true ]]; then
    echo R06B2_OPERATOR_CI_REFUSED >&2; return 1
  fi
  if ! [[ -t 0 && -t 1 && -t 2 ]]; then
    echo R06B2_OWNER_TERMINAL_REQUIRED >&2; return 1
  fi
  challenge="$(c9_operator_challenge "$@")" || return 1
  printf '\nOWNER ONLY: review the restoration receipt above. To authorize this disposable import, enter the exact line below within 300 seconds. Do not automate this response. Decline or close the terminal to stop.\n%s\n> ' "$challenge" >&2
  IFS= read -r -t 300 reply </dev/tty || return 1
  c9_operator_response_matches "$1" "$2" "$reply"
}

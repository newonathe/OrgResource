#!/usr/bin/env bash
# R06B1 public fixture proof. Never retrieve or use the private full plan in CI.
set -euo pipefail
umask 077
[[ "${GITHUB_ACTIONS:-}" = true && "${RUNNER_OS:-}" = Linux ]] || exit 1
jar="$GITHUB_WORKSPACE/extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar"
source_sha="$(git rev-parse HEAD)"
jar_sha="$(sha256sum "$jar" | cut -d' ' -f1)"
root="$(mktemp -d "$RUNNER_TEMP/c9-mixed-prep-test.XXXXXXXX")"
trap 'rm -rf -- "$root"' EXIT
runner=scripts/verify-c9-local-mixed-preparation.sh
bash -n "$runner"
bash -n scripts/c9-local-operator-gate.sh
bash -n scripts/c9-public-recovered-control.sh
python3 -m py_compile scripts/verify-c9-public-recovered-control.py
node --check scripts/verify-c9-public-recovered-control.mjs
source scripts/c9-local-operator-gate.sh
fp="$(printf '%064d' 1)"
digest="$(printf '%064d' 2)"
expected="I VERIFIED ISOLATED RESTORE $fp $digest"
c9_operator_response_matches "$fp" "$digest" "$expected" || exit 1
for reply in "" "$expected " "yes" "I VERIFIED ISOLATED RESTORE $digest $fp"; do
  if c9_operator_response_matches "$fp" "$digest" "$reply"; then exit 1; fi
done
if c9_operator_response_matches malformed "$digest" "$expected"; then exit 1; fi
if c9_require_operator_confirmation "$fp" "$digest" > "$root/gate-private.log" 2>&1; then exit 1; fi
grep -Fq R06B2_OPERATOR_CI_REFUSED "$root/gate-private.log" || exit 1
echo R06B2_CONFIRMATION_CONTRACT_OK:exact_match=true negative_matches_refused=true actual_ci_operator_gate_refused=true
count_containers() { docker --host=unix:///var/run/docker.sock ps -a --filter 'label=orgresource.c9.local' --format '{{.ID}}' | sort; }
before="$(count_containers)"
refuse() {
  local label="$1" expected="$2"; shift 2
  if "$@" >"$root/test-private.log" 2>&1; then echo "R06B1_UNEXPECTED_SUCCESS:$label" >&2; exit 1; fi
  grep -Fq "$expected" "$root/test-private.log" || { echo "R06B1_WRONG_REFUSAL:$label" >&2; exit 1; }
  [[ "$(count_containers)" = "$before" ]] || { echo R06B1_REFUSAL_CREATED_CONTAINER >&2; exit 1; }
  echo "R06B1_PRECHECK_REFUSAL_OK:$label"
}
mkdir -m 755 "$root/not-private"
refuse root-mode R06B1_PRIVATE_ROOT_REQUIRED bash "$runner" --fixture-only "$root/not-private" "$source_sha" "$jar" "$jar_sha"
refuse wrong-source R06B1_SOURCE_SHA_MISMATCH bash "$runner" --fixture-only "$root" "$(printf '%040d' 0)" "$jar" "$jar_sha"
refuse wrong-jar R06B1_JAR_HASH_MISMATCH bash "$runner" --fixture-only "$root" "$source_sha" "$jar" "$(printf '%064d' 0)"
printf '{"fictional":true}\n' > "$root/wrong-plan.json"
chmod 600 "$root/wrong-plan.json"
refuse wrong-plan R06B1_APPROVED_PLAN_MISMATCH bash "$runner" --approved-plan "$root" "$source_sha" "$jar" "$jar_sha" "$root/wrong-plan.json"
ln -s "$root/wrong-plan.json" "$root/link-plan.json"
refuse plan-link R06B1_PRIVATE_PLAN_REQUIRED bash "$runner" --approved-plan "$root" "$source_sha" "$jar" "$jar_sha" "$root/link-plan.json"
# Sparse synthetic file, never actual approved data; CI must refuse before reading bytes.
truncate -s 19265850 "$root/size-only.json"
chmod 600 "$root/size-only.json"
refuse exact-size-ci R06B1_PRIVATE_PLAN_CI_REFUSED bash "$runner" --approved-plan "$root" "$source_sha" "$jar" "$jar_sha" "$root/size-only.json"
refuse mixed-backup-ci C9_LOCAL_MIXED_CI_REFUSED pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 -Mode Backup -Schema orgresource_c9_local_000000000001_restore -Port 3306 -Archive "$root/no-archive.aesgcm" -LocalMixedAcceptance
refuse mixed-restore-ci C9_LOCAL_MIXED_CI_REFUSED pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 -Mode Restore -Schema orgresource_c9_local_000000000001_mixed -Port 3306 -Archive "$root/no-archive.aesgcm" -LocalMixedAcceptance
[ ! -e "$root/no-archive.aesgcm" ] || exit 1
refuse import-ci R06B2_IMPORT_CI_REFUSED bash "$runner" --approved-import "$root" "$source_sha" "$jar" "$jar_sha" "$root/nonexistent-plan.json"
refuse remote-docker R06B1_LOCAL_DOCKER_REQUIRED env DOCKER_HOST=tcp://127.0.0.1:2375 bash "$runner" --fixture-only "$root" "$source_sha" "$jar" "$jar_sha"
bash "$runner" --fixture-only "$root" "$source_sha" "$jar" "$jar_sha" > "$root/test-private.log" 2>&1 || {
  # Only fixed safe markers and numeric status, never arbitrary JVM/SQL/HTTP logs.
  grep -E '^(R06B[123][AB]?_[A-Z_]+(:[a-zA-Z0-9_= .-]+)?|C9_RUNTIME_PIDFD_(GUARD_OK|FAILURE)(:[a-zA-Z0-9_= .-]+)?)
  exit 1
}
for marker in R06B3B_CONTROL_GRANTS_OK R06B3B_CONTROL_API_LOGIN_OK R06B3B_CONTROL_PERSISTED_SESSION_OK R06B3B_CONTROL_API_LOGOUT_OK R06B3B_CONTROL_BROWSER_OK R06B3B_CONTROL_ROWS_OK R06B3A_THIRD_SERVER_FIXTURE_OK R06B2_IMPORTER_GRANTS_OK R06B1_BOOTSTRAP_OK R06B1_ORIGINAL_RESTORE_OK R06B1_BASELINE_OK R06B1_PREPARATION_OK R06B1_CLEANUP_OK; do
  grep -E "^$marker(:|$)" "$root/test-private.log" || exit 1
done
[[ "$(count_containers)" = "$before" ]] || exit 1
echo R06B1_PUBLIC_FIXTURE_TEST_OK:private_plan_used=false importer_run=false precheck_refusals=10
 "$root/test-private.log" | tail -35 || true
  exit 1
}
for marker in R06B3B_CONTROL_GRANTS_OK R06B3B_CONTROL_API_LOGIN_OK R06B3B_CONTROL_PERSISTED_SESSION_OK R06B3B_CONTROL_API_LOGOUT_OK R06B3B_CONTROL_BROWSER_OK R06B3B_CONTROL_ROWS_OK R06B3A_THIRD_SERVER_FIXTURE_OK R06B2_IMPORTER_GRANTS_OK R06B1_BOOTSTRAP_OK R06B1_ORIGINAL_RESTORE_OK R06B1_BASELINE_OK R06B1_PREPARATION_OK R06B1_CLEANUP_OK; do
  grep -E "^$marker(:|$)" "$root/test-private.log" || exit 1
done
[[ "$(count_containers)" = "$before" ]] || exit 1
echo R06B1_PUBLIC_FIXTURE_TEST_OK:private_plan_used=false importer_run=false precheck_refusals=10

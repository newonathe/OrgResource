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
refuse remote-docker R06B1_LOCAL_DOCKER_REQUIRED env DOCKER_HOST=tcp://127.0.0.1:2375 bash "$runner" --fixture-only "$root" "$source_sha" "$jar" "$jar_sha"
bash "$runner" --fixture-only "$root" "$source_sha" "$jar" "$jar_sha" > "$root/test-private.log" 2>&1 || {
  grep -E '^R06B1_[A-Z_]+(:[a-zA-Z0-9_= .-]+)?$' "$root/test-private.log" | tail -15 || true
  exit 1
}
for marker in R06B1_BOOTSTRAP_OK R06B1_ORIGINAL_RESTORE_OK R06B1_BASELINE_OK R06B1_PREPARATION_OK R06B1_CLEANUP_OK; do
  grep -E "^$marker(:|$)" "$root/test-private.log" || exit 1
done
[[ "$(count_containers)" = "$before" ]] || exit 1
echo R06B1_PUBLIC_FIXTURE_TEST_OK:private_plan_used=false importer_run=false precheck_refusals=6

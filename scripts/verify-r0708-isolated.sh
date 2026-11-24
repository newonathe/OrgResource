#!/usr/bin/env bash
# Dedicated GitHub-hosted test job only; never run against a shared or user-owned DB.
set -euo pipefail
umask 077
[[ "${GITHUB_ACTIONS:-}" == true && "${RUNNER_OS:-}" == Linux ]]
[[ "${ORGRESOURCE_R0708_JOB:-}" == dedicated ]]
[[ "${MYSQL_CONTAINER:-}" =~ ^[a-f0-9]{64}$ ]]
[[ "${EXT_DB_MIGRATION_USER:-}" == root && -n "${EXT_DB_MIGRATION_PASSWORD:-}" ]]
[[ "$(docker inspect --format '{{.Config.Image}}' "$MYSQL_CONTAINER")" == mysql:8.0.46 ]]
[[ "$(docker inspect --format '{{.State.Running}}' "$MYSQL_CONTAINER")" == true ]]
[[ "$(docker port "$MYSQL_CONTAINER" 3306/tcp)" == *:3306* ]]
mysql_ci() { docker exec -i --env MYSQL_PWD="$EXT_DB_MIGRATION_PASSWORD" "$MYSQL_CONTAINER" mysql -uroot "$@"; }
scalar() { mysql_ci --batch --skip-column-names -e "$1"; }
[[ "$(scalar 'SELECT @@version')" == 8.0.46 ]]
[[ "$(scalar "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='orgresource_c1_r0708_seed'")" == 0 ]]
[[ "$(scalar "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='orgresource_c1_r0708_seed'")" == 1 ]]
# Confirm dedicated empty service; reject any unrelated non-system schema.
[[ "$(scalar "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name NOT IN ('mysql','information_schema','performance_schema','sys','orgresource_c1_r0708_seed')")" == 0 ]]
work="$(mktemp -d "$RUNNER_TEMP/orgresource-r0708.XXXXXXXX")"
schema= runtime= schema_created=0 user_created=0
cleanup_case() {
  if [[ "$user_created" == 1 ]]; then mysql_ci -e "DROP USER '$runtime'@'%'" || return 1; user_created=0; fi
  if [[ "$schema_created" == 1 ]]; then mysql_ci -e "DROP DATABASE $schema" || return 1; schema_created=0; fi
}
cleanup() {
  local rc=$?
  trap - EXIT
  if ! cleanup_case; then echo 'R0708_CLEANUP_REFUSED' >&2; exit 1; fi
  rm -rf -- "$work"
  exit "$rc"
}
trap cleanup EXIT
concurrency=C2_CONCURRENCY01_twoApprovalsAndTwoIssuesCannotDoubleCommitStock
rollback=C2_IDEMP02_failureBetweenQuantityAndMovementRollsBackEverything
measure=C8WorkspacePagesReturnEveryResourceAndAllocationWithScopedContinuation
run_case() {
  local label="$1" methods="$2" reverse="$3" perf="$4" phase="$5"
  local suffix password owner_password key url
  suffix="$(openssl rand -hex 6)"
  schema="orgresource_c1_r0708_${suffix}"
  runtime="r0708_${suffix}"
  [[ "$schema" =~ ^orgresource_c1_r0708_[a-f0-9]{12}$ && "$runtime" =~ ^r0708_[a-f0-9]{12}$ ]]
  password="$(openssl rand -hex 24)"; owner_password="$(openssl rand -hex 24)"
  key="$(openssl rand -base64 32 | tr -d '\n')"
  for secret in "$password" "$owner_password" "$key"; do echo "::add-mask::$secret"; done
  mysql_ci -e "CREATE DATABASE $schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci"; schema_created=1
  mysql_ci -e "CREATE USER '$runtime'@'%' IDENTIFIED BY '$password'"; user_created=1
  mysql_ci -e "GRANT SELECT,INSERT,UPDATE,DELETE ON $schema.* TO '$runtime'@'%'"
  [[ "$(scalar "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$schema'")" == 0 ]]
  url="jdbc:mysql://127.0.0.1:3306/$schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  rm -f extended/backend/target/surefire-reports/TEST-edu.orgresource.extended.C1IdentityAcceptanceTest.xml
  local started=$SECONDS
  if ! EXT_DB_URL="$url" EXT_DB_USER="$runtime" EXT_DB_PASSWORD="$password" EXT_DB_MIGRATION_URL="$url" \
      EXT_BOOTSTRAP_PASSWORD="$owner_password" EXT_CONTACT_ENCRYPTION_KEY="$key" \
      ORGRESOURCE_R07_PROBE=true ORGRESOURCE_R07_REVERSE="$reverse" ORGRESOURCE_R08_PROBE="$perf" \
      bash ./backend/mvnw -B -f extended/backend/pom.xml "-Dtest=C1IdentityAcceptanceTest#$methods" "$phase" \
      >"$work/$label.log" 2>&1; then
    grep -E 'Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+' "$work/$label.log" | tail -3 || true
    echo "R0708_CASE_FAILED:$label" >&2; return 1
  fi
  python3 scripts/verify-r0708-receipt.py "$label" "$work/$label.log" \
      extended/backend/target/surefire-reports/TEST-edu.orgresource.extended.C1IdentityAcceptanceTest.xml
  echo "R0708_MAVEN:$label phase=$phase elapsed_s=$((SECONDS-started))"
  [[ "$(scalar "SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema='$schema'")" == 0 ]]
  cleanup_case
  [[ "$(scalar "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='$schema'")" == 0 ]]
  [[ "$(scalar "SELECT COUNT(*) FROM mysql.user WHERE user='$runtime' AND host='%'")" == 0 ]]
  echo "R0708_CASE_CLEANUP_OK:$label"
}
run_case concurrency "$concurrency" false false test
run_case rollback "$rollback" false false test
run_case reversed "$concurrency+$rollback" true false test
run_case measurement "$measure" false true package
jar=extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar
[[ -s "$jar" && -d extended/frontend/dist ]]
echo "R08_ARTIFACTS:jar_bytes=$(stat -c %s "$jar") bundle_file_bytes=$(find extended/frontend/dist -type f -printf '%s\n' | awk '{sum+=$1} END{print sum+0}')"
echo 'R0708_PREPARATION_OK:individual=true reversed=true http=true cleanup=true'

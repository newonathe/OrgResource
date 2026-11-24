#!/usr/bin/env bash
# Stage 1D: run the actual packaged Java/Chromium inside isolated loopback
# network namespaces on fresh MySQL clones. No host DB/ports or external routes.
set -euo pipefail
umask 077
fail() { echo "$1" >&2; exit 1; }
source_schema=orgresource_c8_additive_ci_fixture_intermediate
target_schema=orgresource_c8_additive_ci_fixture_c9_encrypted
[[ "$GITHUB_ACTIONS" = true && "$RUNNER_OS" = Linux && "$EXT_SMS_ENABLED" = false && "$EXT_REMINDER_MODE" = DISABLED ]] ||
  fail C9_RUNTIME_CI_ONLY
[[ "$C9_DB_SCHEMA" = "$source_schema" && "$MYSQL_CONTAINER" =~ ^[0-9a-f]{64}$ &&
   "$GITHUB_RUN_ID" =~ ^[1-9][0-9]{1,17}$ && "$GITHUB_RUN_ATTEMPT" =~ ^[1-9][0-9]{0,3}$ &&
   "$C9_PLAN_SHA" =~ ^[0-9a-f]{64}$ && "$C9_PLAN_PATH" = "$RUNNER_TEMP/c8-additive-intermediate-private.json" &&
   "$C9_RUNTIME_ORG" =~ ^[0-9a-f-]{36}$ && -n "$C9_RUNTIME_OWNER_PASSWORD" &&
   -n "$EXT_CONTACT_ENCRYPTION_KEY" && -n "$EXT_DB_MIGRATION_PASSWORD" ]] ||
  fail C9_RUNTIME_FIXTURE_REQUIRED
[[ "$(sha256sum "$C9_PLAN_PATH" | cut -d' ' -f1)" = "$C9_PLAN_SHA" &&
   "$(docker inspect --format '{{.Config.Image}}' "$MYSQL_CONTAINER")" = mysql:8.0.46 ]] ||
  fail C9_RUNTIME_FIXTURE_IDENTITY_REQUIRED
jar="$GITHUB_WORKSPACE/extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar"
[[ -f "$jar" ]] || fail C9_RUNTIME_PACKAGED_JAR_REQUIRED
java_bin="$(command -v java)"
python_bin="$(command -v python3)"
node_bin="$(command -v node)"
uid="$(id -u)"
gid="$(id -g)"
[[ "$uid" != 0 ]] || fail C9_RUNTIME_UNPRIVILEGED_RUNNER_REQUIRED
sudo -n true || fail C9_RUNTIME_NSENTER_UNAVAILABLE
"$python_bin" scripts/stop-c9-isolated-process.py --self-test

job_container="$MYSQL_CONTAINER"
job_password="$EXT_DB_MIGRATION_PASSWORD"
source_name="orgresource-c9-runtime-source-$GITHUB_RUN_ID-$GITHUB_RUN_ATTEMPT"
target_name="orgresource-c9-restorer-$GITHUB_RUN_ID-$GITHUB_RUN_ATTEMPT"
for name in "$source_name" "$target_name"; do
  if docker container inspect "$name" >/dev/null 2>&1; then fail C9_RUNTIME_EXISTING_CONTAINER_REFUSED; fi
done
work="$(mktemp -d "$RUNNER_TEMP/c9-cross-server.XXXXXXXX")"
chmod 700 "$work"
source_id=
target_id=
app_supervisor=
app_container=
app_pidfile="$work/app.pid"
stop_app() {
  if [ -n "$app_supervisor" ]; then
    local container_pid namespace
    [ -s "$app_pidfile" ] || { echo C9_RUNTIME_PID_RECORD_MISSING >&2; return 1; }
    container_pid="$(docker inspect --format '{{.State.Pid}}' "$app_container")" || return 1
    [[ "$container_pid" =~ ^[1-9][0-9]*$ ]] || return 1
    namespace="$(sudo -n readlink "/proc/$container_pid/ns/net")" || return 1
    "$python_bin" scripts/stop-c9-isolated-process.py "$app_pidfile" "$uid" "$namespace" || return 1
    wait "$app_supervisor" 2>/dev/null || true
    app_supervisor=
    app_container=
    rm -f "$app_pidfile"
  fi
}
cleanup() {
  local result=0
  stop_app || result=1
  for id in "$source_id" "$target_id"; do
    if [ -n "$id" ]; then
      if docker container inspect "$id" >/dev/null 2>&1; then
        docker rm -f "$id" >/dev/null 2>&1 || result=1
      fi
    fi
  done
  rm -rf -- "$work" || result=1
  return "$result"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
source_password="$(openssl rand -hex 24)"
target_password="$(openssl rand -hex 24)"
runtime_password="$(openssl rand -hex 24)"
migration_password="$(openssl rand -hex 24)"
backup_key="$(openssl rand -base64 32 | tr -d '\n')"
for value in "$source_password" "$target_password" "$runtime_password" "$migration_password" "$backup_key"; do
  echo "::add-mask::$value"
done
mysql_at() {
  local id="$1" password="$2"; shift 2
  MYSQL_PWD="$password" docker exec -i --env MYSQL_PWD "$id" mysql --no-defaults --protocol=socket -uroot "$@"
}
scalar() { mysql_at "$1" "$2" --batch --skip-column-names "$3" -e "$4"; }
dump_at() {
  local id="$1" password="$2"; shift 2
  MYSQL_PWD="$password" docker exec -i --env MYSQL_PWD "$id" mysqldump \
    --no-defaults --protocol=socket -uroot --skip-comments --skip-dump-date \
    --hex-blob --skip-lock-tables --skip-add-drop-table --no-tablespaces \
    --set-gtid-purged=OFF --skip-column-statistics --single-transaction --quick "$@"
}
rows_at() {
  dump_at "$1" "$2" --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys "$3"
}
domain_at() {
  dump_at "$1" "$2" --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys \
    "--ignore-table=$3.SPRING_SESSION" "--ignore-table=$3.SPRING_SESSION_ATTRIBUTES" "$3"
}
# Enter only the network namespace, retaining the runner filesystem; drop root
# before Java, Python, Node or Chromium. No host-network app or proxy.
ns_run() {
  local id="$1" pid; shift
  pid="$(docker inspect --format '{{.State.Pid}}' "$id")"
  [[ "$pid" =~ ^[1-9][0-9]*$ ]] || fail C9_RUNTIME_NAMESPACE_PID_REQUIRED
  sudo -n nsenter --target "$pid" --net -- setpriv --reuid "$uid" --regid "$gid" --init-groups \
    env -i "PATH=$PATH" "HOME=$HOME" "LANG=C.UTF-8" C9_RUNTIME_ISOLATED=true "$@"
}
assert_isolated() {
  local id="$1"
  [[ "$(docker inspect --format '{{.HostConfig.NetworkMode}}' "$id")" = none &&
     "$(docker inspect --format '{{.Config.Image}}' "$id")" = mysql:8.0.46 &&
     -z "$(docker port "$id")" ]] || fail C9_RUNTIME_NETWORK_BOUNDARY
  ns_run "$id" bash -ec '
    test "$(id -u)" != 0
    test "$(ip -o link show | wc -l)" = 1
    test -z "$(ip -4 route show)"
    test -z "$(ip -6 route show default)"
    ip -o link show lo >/dev/null
  ' || fail C9_RUNTIME_LOOPBACK_ONLY_REQUIRED
}
start_mysql() {
  local id="$1" password="$2"
  local ready=false
  for _ in $(seq 1 90); do
    if mysql_at "$id" "$password" -N -B -e 'SELECT 1' >/dev/null 2>&1 &&
       ns_run "$id" bash -c 'exec 3<>/dev/tcp/127.0.0.1/3306' >/dev/null 2>&1; then
      ready=true; break
    fi
    sleep 1
  done
  [ "$ready" = true ] || fail C9_RUNTIME_MYSQL_READY_TIMEOUT
  assert_isolated "$id"
}
grant_app() {
  local id="$1" password="$2" schema="$3" table grants
  mysql_at "$id" "$password" <<SQL
CREATE USER 'c9_runtime'@'%' IDENTIFIED BY '$runtime_password';
CREATE USER 'c9_migrator'@'%' IDENTIFIED BY '$migration_password';
GRANT ALL PRIVILEGES ON $schema.* TO 'c9_migrator'@'%';
SQL
  for table in $(scalar "$id" "$password" "$schema" 'SHOW TABLES'); do
    [[ "$table" =~ ^[A-Za-z_]+$ ]] || fail C9_RUNTIME_TABLE_IDENTIFIER
    case "$table" in
      stock_movement|audit_event) grants='SELECT, INSERT' ;;
      flyway_schema_history) grants='SELECT' ;;
      *) grants='SELECT, INSERT, UPDATE, DELETE' ;;
    esac
    mysql_at "$id" "$password" -e "GRANT $grants ON $schema.$table TO 'c9_runtime'@'%';"
  done
}
write_env() {
  local schema="$1" file="$2"
  local url="jdbc:mysql://127.0.0.1:3306/$schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  {
    printf 'export EXT_DB_URL=%q\n' "$url"
    printf 'export EXT_DB_MIGRATION_URL=%q\n' "$url"
    printf 'export EXT_DB_USER=c9_runtime\nexport EXT_DB_PASSWORD=%q\n' "$runtime_password"
    printf 'export EXT_DB_MIGRATION_USER=c9_migrator\nexport EXT_DB_MIGRATION_PASSWORD=%q\n' "$migration_password"
    printf 'export EXT_CONTACT_ENCRYPTION_KEY=%q\n' "$EXT_CONTACT_ENCRYPTION_KEY"
    printf 'export SPRING_PROFILES_ACTIVE=c1-test\nexport EXT_SMS_ENABLED=false\nexport EXT_REMINDER_MODE=DISABLED\n'
    printf 'export EXT_SERVER_PORT=8085\nexport EXT_SCHEDULER_DELAY_MS=600000\n'
    printf 'export C9_RUNTIME_OWNER_PASSWORD=%q\n' "$C9_RUNTIME_OWNER_PASSWORD"
    printf 'export C9_RUNTIME_ORG=%q\nexport C9_RUNTIME_STATE=%q\n' "$C9_RUNTIME_ORG" "$work/state.json"
  } > "$file"
}
run_private() {
  local id="$1" file="$2"; shift 2
  ns_run "$id" bash -ec 'source "$1"; shift; exec "$@"' _ "$file" "$@"
}
start_app() {
  local id="$1" envfile="$2" label="$3"
  app_container="$id"
  # Bound the launcher lifetime even if cleanup must refuse a mismatched PID.
  ns_run "$id" timeout --signal=TERM --kill-after=10s 240s bash -ec '
    source "$1"
    stat_line="$(cat "/proc/$$/stat")"
    read -r -a fields <<< "${stat_line##*) }"
    printf "%s %s\n" "$$" "${fields[19]}" > "$2"
    shift 2
    exec "$@"
  ' _ \
    "$envfile" "$app_pidfile" "$java_bin" -Xmx512m -Duser.timezone=Asia/Manila -jar "$jar" \
    > "$work/$label-app.log" 2>&1 &
  app_supervisor=$!
  local ready=false
  for _ in $(seq 1 100); do
    kill -0 "$app_supervisor" 2>/dev/null || fail C9_RUNTIME_JAR_EXITED
    if ns_run "$id" curl --noproxy '*' --silent --fail --max-time 2 http://127.0.0.1:8085/ > "$work/$label-index.html"; then
      ready=true; break
    fi
    sleep 1
  done
  [ "$ready" = true ] || fail C9_RUNTIME_JAR_START_TIMEOUT
  grep -Eq 'assets/index-[^"]+\.js' "$work/$label-index.html" || fail C9_RUNTIME_ASSETS_MISSING
  echo "C9_RUNTIME_JAR_READY:$label"
}
[[ "$(scalar "$job_container" "$job_password" "$source_schema" "SELECT COUNT(*) FROM organization WHERE demo_plan_sha='$C9_PLAN_SHA'")" = 8 &&
   "$(scalar "$job_container" "$job_password" "$source_schema" 'SELECT COUNT(*) FROM allocation')" = 960 &&
   "$(scalar "$job_container" "$job_password" "$source_schema" 'SELECT COUNT(*) FROM SPRING_SESSION')" = 0 &&
   "$(scalar "$job_container" "$job_password" "$source_schema" 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES')" = 0 ]] ||
  fail C9_RUNTIME_EXACT_INTERMEDIATE_REQUIRED
rows_at "$job_container" "$job_password" "$source_schema" > "$work/job-before.sql"
job_uuid="$(scalar "$job_container" "$job_password" "$source_schema" 'SELECT @@server_uuid')"

source_id="$(docker run -d --rm --network none --name "$source_name" --env "MYSQL_ROOT_PASSWORD=$source_password" \
  mysql:8.0.46 --bind-address=127.0.0.1 --mysqlx=0)"
start_mysql "$source_id" "$source_password"
source_uuid="$(scalar "$source_id" "$source_password" mysql 'SELECT @@server_uuid')"
[[ "$source_uuid" != "$job_uuid" ]] || fail C9_RUNTIME_SOURCE_NOT_INDEPENDENT
mysql_at "$source_id" "$source_password" -e "CREATE DATABASE $source_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
dump_at "$job_container" "$job_password" "$source_schema" | mysql_at "$source_id" "$source_password" --binary-mode "$source_schema"
rows_at "$source_id" "$source_password" "$source_schema" > "$work/clone-before.sql"
cmp -s "$work/job-before.sql" "$work/clone-before.sql" || fail C9_RUNTIME_CLONE_DIFFERENT
grant_app "$source_id" "$source_password" "$source_schema"
write_env "$source_schema" "$work/source.env"
start_app "$source_id" "$work/source.env" source
run_private "$source_id" "$work/source.env" "$python_bin" scripts/verify-c9-recovered-api.py seed
stop_app

operation="$("$python_bin" -c 'import json,sys; print(json.load(open(sys.argv[1]))["operation"])' "$work/state.json")"
old_session="$("$python_bin" -c 'import json,sys; print(json.load(open(sys.argv[1]))["oldSession"])' "$work/state.json")"
resource="$("$python_bin" -c 'import json,sys; print(json.load(open(sys.argv[1]))["resource"])' "$work/state.json")"
for value in "$operation" "$old_session" "$resource"; do
  [[ "$value" =~ ^[0-9a-f-]{36}$ ]] || fail C9_RUNTIME_STATE_IDENTITY
done
echo "::add-mask::$old_session"
# Prove the old cookie genuinely identifies an unexpired persisted session.
[[ "$(scalar "$source_id" "$source_password" "$source_schema" "SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID='$old_session' AND EXPIRY_TIME>UNIX_TIMESTAMP()*1000")" = 1 &&
   "$(scalar "$source_id" "$source_password" "$source_schema" "SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES a JOIN SPRING_SESSION s ON s.PRIMARY_ID=a.SESSION_PRIMARY_ID WHERE s.SESSION_ID='$old_session'")" -gt 0 ]] ||
  fail C9_RUNTIME_OLD_SESSION_NOT_PERSISTED
[[ "$(scalar "$source_id" "$source_password" "$source_schema" "SELECT COUNT(*) FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN('$operation',0)")" = 1 ]] ||
  fail C9_RUNTIME_TEMPORARY_RECEIPT_MISSING
mysql_at "$source_id" "$source_password" "$source_schema" <<SQL
UPDATE idempotency_key SET created_at=UTC_TIMESTAMP(6)-INTERVAL 25 HOUR, expires_at=UTC_TIMESTAMP(6)-INTERVAL 1 HOUR
WHERE operation_public_id=UUID_TO_BIN('$operation',0);
DELETE FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN('$operation',0) AND expires_at<=UTC_TIMESTAMP(6);
SQL
[[ "$(scalar "$source_id" "$source_password" "$source_schema" "SELECT COUNT(*) FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN('$operation',0)")" = 0 &&
   "$(scalar "$source_id" "$source_password" "$source_schema" "SELECT COUNT(*) FROM stock_movement WHERE operation_public_id=UUID_TO_BIN('$operation',0) AND kind='RECEIPT' AND delta_total=1")" = 1 ]] ||
  fail C9_RUNTIME_PERMANENT_RECEIPT_REQUIRED
echo C9_RUNTIME_DURABLE_SOURCE_FIXTURE_OK
rows_at "$source_id" "$source_password" "$source_schema" > "$work/source-before-archive.sql"
domain_at "$source_id" "$source_password" "$source_schema" > "$work/source-domain.sql"
dump_at "$source_id" "$source_password" --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys "$source_schema" flyway_schema_history > "$work/flyway-before.sql"

target_id="$(docker run -d --rm --network none --name "$target_name" \
  --label "orgresource.c9.run=$GITHUB_RUN_ID-$GITHUB_RUN_ATTEMPT" --env "MYSQL_ROOT_PASSWORD=$target_password" \
  mysql:8.0.46 --bind-address=127.0.0.1 --mysqlx=0)"
start_mysql "$target_id" "$target_password"
target_uuid="$(scalar "$target_id" "$target_password" mysql 'SELECT @@server_uuid')"
[[ "$target_uuid" != "$source_uuid" && "$target_uuid" != "$job_uuid" ]] || fail C9_RUNTIME_TARGET_NOT_INDEPENDENT
mysql_at "$target_id" "$target_password" -e "CREATE DATABASE $target_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
archive="$work/runtime.aesgcm"
# The SAME C5 utility, existing Stage 1C guards intact, source clone replaces job
# container only within this invocation. No changes to its crypto or transport.
MYSQL_CONTAINER="$source_id" EXT_DB_MIGRATION_PASSWORD="$source_password" \
  C9_ARCHIVE_DIR="$work" C9_RESTORE_PASSWORD="$target_password" EXT_BACKUP_KEY="$backup_key" \
  pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 \
    -Mode Backup -Schema "$source_schema" -Port 3306 -Archive "$archive" -CiIntermediateAcceptance > "$work/backup.log" 2>&1 ||
  fail C9_RUNTIME_C5_BACKUP_FAILED
MYSQL_CONTAINER="$source_id" EXT_DB_MIGRATION_PASSWORD="$source_password" \
  C9_ARCHIVE_DIR="$work" C9_RESTORE_PASSWORD="$target_password" EXT_BACKUP_KEY="$backup_key" \
  pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 \
    -Mode Restore -Schema "$target_schema" -Port 3306 -Archive "$archive" -CiIntermediateAcceptance > "$work/restore.log" 2>&1 ||
  fail C9_RUNTIME_C5_RESTORE_FAILED
[[ "$(scalar "$target_id" "$target_password" "$target_schema" 'SELECT COUNT(*) FROM SPRING_SESSION')" = 0 &&
   "$(scalar "$target_id" "$target_password" "$target_schema" 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES')" = 0 ]] ||
  fail C9_RUNTIME_HISTORICAL_SESSIONS_RESTORED
domain_at "$target_id" "$target_password" "$target_schema" > "$work/restored-domain.sql"
cmp -s "$work/source-domain.sql" "$work/restored-domain.sql" || fail C9_RUNTIME_ARCHIVE_DOMAIN_MISMATCH
echo "C9_RUNTIME_C5_RESTORE_OK: archive_bytes=$(stat -c%s "$archive") independent_server=true non_session_rows_equal=true sessions_empty=true"

grant_app "$target_id" "$target_password" "$target_schema"
write_env "$target_schema" "$work/target.env"
start_app "$target_id" "$work/target.env" restored
run_private "$target_id" "$work/target.env" "$python_bin" scripts/verify-c9-recovered-api.py recovered
# R08: reuse the already running, isolated packaged JAR and the public synthetic
# restored fixture. Select a tagged workspace whose OWNER and counts are independently
# resolved from the restored database; never measure a user/hosted database.
r08_org="$(scalar "$target_id" "$target_password" "$target_schema" "SELECT BIN_TO_UUID(o.public_id,0) FROM organization o JOIN membership m ON m.org_id=o.id JOIN user_account u ON u.id=m.user_id WHERE o.demo_plan_sha='$C9_PLAN_SHA' AND o.status='ACTIVE' AND m.status='ACTIVE' AND m.role='OWNER' AND u.username_key='additive-original-owner' ORDER BY o.id LIMIT 1")"
[[ "$r08_org" =~ ^[0-9a-f-]{36}$ ]] || fail R08_PACKAGED_ORG_REQUIRED
r08_scope="organization_id=(SELECT id FROM organization WHERE public_id=UUID_TO_BIN('$r08_org',0))"
for table in department project resource resource_requirement allocation; do
  count="$(scalar "$target_id" "$target_password" "$target_schema" "SELECT COUNT(*) FROM $table WHERE $r08_scope")"
  [[ "$count" =~ ^[1-9][0-9]*$ ]] || fail R08_PACKAGED_NONEMPTY_FIXTURE_REQUIRED
  printf '%s\t%s\n' "$table" "$count" >> "$work/r08-counts.tsv"
done
for status in PENDING ISSUED CONSUMED; do
  count="$(scalar "$target_id" "$target_password" "$target_schema" "SELECT COUNT(*) FROM allocation WHERE $r08_scope AND status='$status'")"
  [[ "$count" =~ ^[0-9]+$ ]] || fail R08_PACKAGED_STATUS_COUNT_REQUIRED
  printf '%s\t%s\n' "$status" "$count" >> "$work/r08-counts.tsv"
done
run_private "$target_id" "$work/target.env" env PYTHONDONTWRITEBYTECODE=1 "$python_bin" scripts/verify-r08-packaged-http.py "$r08_org" "$work/r08-counts.tsv"
echo "R08_PACKAGED_ARTIFACT:jar_bytes=$(stat -c%s "$jar") jar_sha256=$(sha256sum "$jar" | cut -d' ' -f1)"
# Existing later all-domain exact row comparison covers every measurement read.
# Browser must see real 503: this is mixed intermediate, not approved mixed full.
ns_run "$target_id" "$node_bin" extended/frontend/e2e/c9-recovered-browser.mjs
stop_app
assert_isolated "$source_id"
assert_isolated "$target_id"
dump_at "$target_id" "$target_password" --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys "$target_schema" flyway_schema_history > "$work/flyway-after.sql"
cmp -s "$work/flyway-before.sql" "$work/flyway-after.sql" || fail C9_RUNTIME_FLYWAY_HISTORY_CHANGED
[[ "$(scalar "$target_id" "$target_password" "$target_schema" "SELECT COUNT(*) FROM idempotency_key WHERE operation_public_id=UUID_TO_BIN('$operation',0)")" = 0 &&
   "$(scalar "$target_id" "$target_password" "$target_schema" "SELECT COUNT(*) FROM stock_movement WHERE operation_public_id=UUID_TO_BIN('$operation',0) AND kind='RECEIPT' AND delta_total=1")" = 1 &&
   "$(scalar "$target_id" "$target_password" "$target_schema" "SELECT total_quantity FROM resource WHERE public_id=UUID_TO_BIN('$resource',0)")" = 1 &&
   "$(scalar "$target_id" "$target_password" "$target_schema" "SELECT COUNT(*) FROM stock_movement m JOIN resource r ON r.id=m.resource_id WHERE r.public_id=UUID_TO_BIN('$resource',0)")" = 1 ]] ||
  fail C9_RUNTIME_DURABLE_SQL_MISMATCH
domain_at "$target_id" "$target_password" "$target_schema" > "$work/target-domain-after.sql"
cmp -s "$work/restored-domain.sql" "$work/target-domain-after.sql" || fail C9_RUNTIME_DENIAL_OR_REPLAY_CHANGED_DOMAIN
rows_at "$source_id" "$source_password" "$source_schema" > "$work/source-after.sql"
cmp -s "$work/source-before-archive.sql" "$work/source-after.sql" || fail C9_RUNTIME_SOURCE_CLONE_CHANGED
rows_at "$job_container" "$job_password" "$source_schema" > "$work/job-after.sql"
cmp -s "$work/job-before.sql" "$work/job-after.sql" || fail C9_RUNTIME_ORIGINAL_FIXTURE_CHANGED
echo "C9_RECOVERED_APPLICATION_OK: packaged_jar=true flyway_history_unchanged=true authenticated_session_retired=true owner_login=true durable_replay=true scoped_denials=true all_non_session_rows_unchanged=true original_fixture_unchanged=true browser_mixed_fail_closed=true normal_authenticated_ui=NOT_RUN"
cleanup
trap - EXIT INT TERM
for id in "$source_id" "$target_id"; do
  if docker container inspect "$id" >/dev/null 2>&1; then fail C9_RUNTIME_CLEANUP_FAILED; fi
done
[ ! -e "$work" ] || fail C9_RUNTIME_CLEANUP_FAILED
echo C9_RUNTIME_CLEANUP_OK

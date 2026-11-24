#!/usr/bin/env bash
# C9 Stage 1B: CI-only fictional SQL recovery onto a *different* MySQL8 server.
# No hosted access, public SQL artifacts, volume mounts, network or port mapping.
set -euo pipefail
source_schema=orgresource_c8_additive_ci_fixture_intermediate
target_schema=orgresource_c8_additive_ci_fixture_c9_cross_server
cap=$((64 * 1024 * 1024))
refuse() { echo "$1" >&2; exit 1; }
# time.monotonic_ns cannot move backwards if the GitHub runner wall clock is adjusted.
monotonic_ms() { python3 -c 'import time; print(time.monotonic_ns() // 1000000)'; }

[ "$(printenv GITHUB_ACTIONS || true)" = true ] || refuse C9_CROSS_SERVER_CI_ONLY
[ "$(printenv C9_DB_SCHEMA || true)" = "$source_schema" ] ||
  refuse C9_CROSS_SERVER_DISPOSABLE_SCHEMA_REQUIRED
[[ "$(printenv MYSQL_CONTAINER || true)" =~ ^[0-9a-f]{64}$ ]] ||
  refuse C9_CROSS_SERVER_SOURCE_CONTAINER_REQUIRED
[[ "$(printenv GITHUB_RUN_ID || true)" =~ ^[1-9][0-9]{1,17}$ ]] ||
  refuse C9_CROSS_SERVER_ACTION_RUN_REQUIRED
[[ "$(printenv GITHUB_RUN_ATTEMPT || true)" =~ ^[1-9][0-9]{0,3}$ ]] ||
  refuse C9_CROSS_SERVER_ACTION_ATTEMPT_REQUIRED
[ -n "$(printenv EXT_DB_MIGRATION_PASSWORD || true)" ] ||
  refuse C9_CROSS_SERVER_SOURCE_ROOT_REQUIRED
[ -n "$(printenv RUNNER_TEMP || true)" ] && [ -d "$RUNNER_TEMP" ] ||
  refuse C9_CROSS_SERVER_RUNNER_TEMP_REQUIRED
[ -n "$(printenv C9_PLAN_PATH || true)" ] && [ -f "$C9_PLAN_PATH" ] ||
  refuse C9_CROSS_SERVER_TEST_PLAN_REQUIRED
[[ "$(printenv C9_PLAN_SHA || true)" =~ ^[0-9a-f]{64}$ ]] ||
  refuse C9_CROSS_SERVER_PLAN_SHA_REQUIRED
[ "$(sha256sum "$C9_PLAN_PATH" | cut -d' ' -f1)" = "$C9_PLAN_SHA" ] ||
  refuse C9_CROSS_SERVER_PLAN_HASH_MISMATCH

source_mysql() {
  docker exec -i --env MYSQL_PWD="$EXT_DB_MIGRATION_PASSWORD" "$MYSQL_CONTAINER" \
    mysql --no-defaults --protocol=socket -uroot "$@"
}
source_dump() {
  docker exec -i --env MYSQL_PWD="$EXT_DB_MIGRATION_PASSWORD" "$MYSQL_CONTAINER" \
    mysqldump --no-defaults --protocol=socket -uroot \
    --skip-comments --skip-dump-date --hex-blob --skip-lock-tables \
    --skip-add-drop-table --no-tablespaces --set-gtid-purged=OFF \
    --skip-column-statistics "$@"
}
source_rows() {
  source_dump --single-transaction --quick --no-create-info \
    --order-by-primary --skip-add-locks --skip-disable-keys "$1"
}
[ "$(source_mysql --batch --skip-column-names "$source_schema" \
    -e 'SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL;')" = 8 ] ||
  refuse C9_CROSS_SERVER_EXACT_EIGHT_ORGS_REQUIRED
[ "$(source_mysql --batch --skip-column-names "$source_schema" \
    -e 'SELECT COUNT(*) FROM allocation;')" = 960 ] ||
  refuse C9_CROSS_SERVER_EXACT_960_ALLOCATIONS_REQUIRED
[ "$(source_mysql --batch --skip-column-names "$source_schema" \
    -e 'SELECT COUNT(*) FROM SPRING_SESSION;')" = 0 ] ||
  refuse C9_CROSS_SERVER_NO_ACTIVE_SESSIONS_REQUIRED
[ "$(source_mysql --batch --skip-column-names -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$source_schema';")" = 18 ] ||
  refuse C9_CROSS_SERVER_EXACT_V7_TABLES_REQUIRED
source_uuid="$(source_mysql --batch --skip-column-names -e 'SELECT @@server_uuid;')"
[[ "$source_uuid" =~ ^[a-f0-9-]{36}$ ]] ||
  refuse C9_CROSS_SERVER_SOURCE_UUID_REQUIRED

workdir="$(mktemp -d "$RUNNER_TEMP/c9-cross-server.XXXXXXXX")"
chmod 700 "$workdir"
restorer_name="orgresource-c9-restorer-$GITHUB_RUN_ID-$GITHUB_RUN_ATTEMPT"
restorer_started=false
cleanup() {
  if [ "$restorer_started" = true ]; then
    docker rm --force "$restorer_name" >/dev/null 2>&1 || true
  fi
  rm -rf -- "$workdir"
}
trap cleanup EXIT
if docker container inspect "$restorer_name" >/dev/null 2>&1; then
  refuse C9_CROSS_SERVER_EXISTING_CONTAINER_REFUSED
fi

main_sql="$workdir/main.sql"
sessions_sql="$workdir/session-ddl.sql"
combined="$workdir/complete.sql"
original_rows="$workdir/original-rows.sql"
restored_rows="$workdir/restored-rows.sql"
start_backup="$(monotonic_ms)"
source_dump --single-transaction --quick \
  "--ignore-table=$source_schema.SPRING_SESSION" \
  "--ignore-table=$source_schema.SPRING_SESSION_ATTRIBUTES" \
  "$source_schema" > "$main_sql"
source_dump --no-data "$source_schema" \
  SPRING_SESSION SPRING_SESSION_ATTRIBUTES > "$sessions_sql"
[ -s "$main_sql" ] && [ -s "$sessions_sql" ] ||
  refuse C9_CROSS_SERVER_EMPTY_SQL_REFUSED
main_bytes="$(stat -c%s "$main_sql")"
session_bytes="$(stat -c%s "$sessions_sql")"
total_bytes=$((main_bytes + session_bytes))
[ "$session_bytes" -le $((1024 * 1024)) ] ||
  refuse C9_CROSS_SERVER_SESSION_DDL_LIMIT_REFUSED
[ "$total_bytes" -le "$cap" ] || refuse C9_CROSS_SERVER_SQL_LIMIT_REFUSED
cat "$main_sql" "$sessions_sql" > "$combined"
[ "$(stat -c%s "$combined")" = "$total_bytes" ] ||
  refuse C9_CROSS_SERVER_LENGTH_MISMATCH
backup_wall_ms=$(( $(monotonic_ms) - start_backup ))
(( backup_wall_ms >= 0 )) || refuse C9_CROSS_SERVER_NEGATIVE_BACKUP_DURATION
digest="$(sha256sum "$combined" | cut -d' ' -f1)"
require_digest() {
  [ "$(sha256sum "$1" | cut -d' ' -f1)" = "$digest" ] || {
    echo C9_CROSS_SERVER_DIGEST_MISMATCH >&2
    return 1
  }
}
cp "$combined" "$workdir/corrupt.sql"
printf '%s\n' '-- corrupted, private disposable backup fixture' >> "$workdir/corrupt.sql"
if require_digest "$workdir/corrupt.sql" > "$workdir/corrupt-negative.log" 2>&1; then
  refuse C9_CROSS_SERVER_TAMPER_WAS_ACCEPTED
fi
grep -q C9_CROSS_SERVER_DIGEST_MISMATCH "$workdir/corrupt-negative.log" ||
  refuse C9_CROSS_SERVER_WRONG_TAMPER_REFUSAL
source_rows "$source_schema" > "$original_rows"
[ -s "$original_rows" ] || refuse C9_CROSS_SERVER_SOURCE_ROWS_EMPTY

# New container: distinct process/data-directory, no mounted volume, NO network.
# Its random root password is used through the UNIX socket inside the container.
restore_password="$(openssl rand -hex 24)"
echo "::add-mask::$restore_password"
docker run --detach --rm --network none --name "$restorer_name" \
  --env "MYSQL_ROOT_PASSWORD=$restore_password" \
  mysql:8.0.46 --skip-networking > /dev/null ||
  refuse C9_CROSS_SERVER_NEW_CONTAINER_FAILED
restorer_started=true
[ "$(docker inspect --format '{{.HostConfig.NetworkMode}}' "$restorer_name")" = none ] ||
  refuse C9_CROSS_SERVER_NETWORK_NOT_DISABLED
[ -z "$(docker port "$restorer_name")" ] ||
  refuse C9_CROSS_SERVER_EXPOSED_PORT_REFUSED
restore_mysql() {
  docker exec -i --env MYSQL_PWD="$restore_password" "$restorer_name" \
    mysql --no-defaults --protocol=socket -uroot "$@"
}
restore_dump() {
  docker exec -i --env MYSQL_PWD="$restore_password" "$restorer_name" \
    mysqldump --no-defaults --protocol=socket -uroot \
    --skip-comments --skip-dump-date --hex-blob --skip-lock-tables \
    --skip-add-drop-table --no-tablespaces --set-gtid-purged=OFF \
    --skip-column-statistics "$@"
}
restore_rows() {
  restore_dump --single-transaction --quick --no-create-info \
    --order-by-primary --skip-add-locks --skip-disable-keys "$1"
}
ready=false
for attempt in $(seq 1 90); do
  if restore_mysql --batch --skip-column-names -e 'SELECT 1;' > /dev/null 2>&1; then
    ready=true
    break
  fi
  sleep 1
done
[ "$ready" = true ] || refuse C9_CROSS_SERVER_STARTUP_TIMEOUT
[ "$(restore_mysql --batch --skip-column-names -e 'SELECT VERSION();')" = 8.0.46 ] ||
  refuse C9_CROSS_SERVER_WRONG_MYSQL_VERSION
restore_uuid="$(restore_mysql --batch --skip-column-names -e 'SELECT @@server_uuid;')"
[[ "$restore_uuid" =~ ^[a-f0-9-]{36}$ ]] && [ "$source_uuid" != "$restore_uuid" ] ||
  refuse C9_CROSS_SERVER_NOT_INDEPENDENT_DATABASE
[ "$(restore_mysql --batch --skip-column-names -e \
  "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='$target_schema';")" = 0 ] ||
  refuse C9_CROSS_SERVER_TARGET_ALREADY_EXISTS
restore_mysql -e "CREATE DATABASE $target_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
require_empty() {
  [ "$(restore_mysql --batch --skip-column-names -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$target_schema';")" = 0 ] || {
    echo C9_CROSS_SERVER_NONEMPTY_TARGET_REFUSED >&2
    return 1
  }
}
require_empty
require_digest "$combined"
start_restore="$(monotonic_ms)"
restore_mysql --binary-mode "$target_schema" < "$combined" \
  > "$workdir/mysql-restore-output.log" 2> "$workdir/mysql-restore-private-error.log" ||
  refuse C9_CROSS_SERVER_SQL_RESTORE_FAILED
restore_wall_ms=$(( $(monotonic_ms) - start_restore ))
(( restore_wall_ms >= 0 )) || refuse C9_CROSS_SERVER_NEGATIVE_RESTORE_DURATION
if require_empty > "$workdir/nonempty-refusal.log" 2>&1; then
  refuse C9_CROSS_SERVER_REPEAT_IMPORT_ALLOWED
fi
grep -q C9_CROSS_SERVER_NONEMPTY_TARGET_REFUSED "$workdir/nonempty-refusal.log" ||
  refuse C9_CROSS_SERVER_BAD_NONEMPTY_REFUSAL
[ "$(restore_mysql --batch --skip-column-names -e \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$target_schema';")" = 18 ] ||
  refuse C9_CROSS_SERVER_TABLE_INVENTORY_DIFFERENT
restore_rows "$target_schema" > "$restored_rows"
cmp -s "$original_rows" "$restored_rows" ||
  refuse C9_CROSS_SERVER_RESTORED_DATA_DIFFERENT
[ "$(restore_mysql --batch --skip-column-names "$target_schema" \
  -e 'SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL;')" = 8 ] ||
  refuse C9_CROSS_SERVER_RESTORED_ORGS_DIFFERENT
[ "$(restore_mysql --batch --skip-column-names "$target_schema" \
  -e 'SELECT COUNT(*) FROM allocation;')" = 960 ] ||
  refuse C9_CROSS_SERVER_RESTORED_ALLOCATIONS_DIFFERENT
source_rows "$source_schema" > "$workdir/source-after.sql"
cmp -s "$original_rows" "$workdir/source-after.sql" ||
  refuse C9_CROSS_SERVER_ORIGINAL_SOURCE_CHANGED

echo "C9_NETWORK_ISOLATED_CROSS_SERVER_RESTORE_OK: \
mysql_version=8.0.46 fictional_orgs=8 allocations=960 \
main_sql_bytes=$main_bytes session_ddl_bytes=$session_bytes \
total_sql_bytes=$total_bytes sql_cap_bytes=$cap \
backup_runner_wall_ms=$backup_wall_ms restore_runner_wall_ms=$restore_wall_ms \
different_mysql_server_uuid=true network_mode=none published_ports=zero \
separate_process_and_data_directory=true row_dump_byte_equal=true \
source_unchanged=true digest_tamper_refused=true nonempty_schema_refused=true \
c5_aes_gcm_archive=NOT_TESTED approved_full_private_96=NOT_RUN \
actual_hosted_db=NOT_TOUCHED"

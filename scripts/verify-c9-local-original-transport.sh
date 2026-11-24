#!/usr/bin/env bash
# R06A public fictional pre-import transport proof; no private plan.
set -euo pipefail
umask 077
fail() { echo "$1" >&2; exit 1; }
[[ "$GITHUB_ACTIONS" = true && "$RUNNER_OS" = Linux && "$EXT_SMS_ENABLED" = false && "$EXT_REMINDER_MODE" = DISABLED ]] || fail C9_LOCAL_TEST_CI_ONLY
[[ "$C9_ORIGINAL_TEST_SCHEMA" = orgresource_c8_additive_ci_fixture_small && "$MYSQL_CONTAINER" =~ ^[a-f0-9]{64}$ && "$C9_ORIGINAL_TEST_ORG" =~ ^[a-f0-9-]{36}$ && "$C9_ORIGINAL_TEST_OWNER" =~ ^[a-f0-9-]{36}$ ]] || fail C9_LOCAL_TEST_FIXTURE
[[ "$(docker inspect --format '{{.Config.Image}}' "$MYSQL_CONTAINER")" = mysql:8.0.46 ]] || fail C9_LOCAL_TEST_IMAGE
[[ "$(id -u)" != 0 ]] && sudo -n true || fail C9_LOCAL_TEST_NSENTER
jar="$GITHUB_WORKSPACE/extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar"
[ -s "$jar" ] || fail C9_LOCAL_TEST_JAR
token="$(openssl rand -hex 6)"
root="$(mktemp -d "$RUNNER_TEMP/c9-local-test.XXXXXXXX")"
work="$root/orgresource-c9-local-$token"
mkdir -m 700 "$work"
source_schema="orgresource_c9_local_${token}_source"
target_schema="orgresource_c9_local_${token}_restore"
source_id= target_id=
cleanup() {
  local result=0
  for id in "$source_id" "$target_id"; do
    if [ -n "$id" ]; then docker rm -f -v "$id" >/dev/null 2>&1 || result=1; fi
  done
  rm -rf -- "$root" || result=1
  return "$result"
}
trap cleanup EXIT
src_password="$(openssl rand -hex 24)"
dst_password="$(openssl rand -hex 24)"
ro_password="$(openssl rand -hex 24)"
backup_key="$(openssl rand -base64 32 | tr -d '\n')"
hmac_key="$(openssl rand -hex 32)"
for v in "$src_password" "$dst_password" "$ro_password" "$backup_key" "$hmac_key"; do echo "::add-mask::$v"; done
mysql_at() {
  local id="$1" password="$2"; shift 2
  MYSQL_PWD="$password" docker exec -i --env MYSQL_PWD "$id" mysql --no-defaults --protocol=socket -uroot "$@"
}
scalar() { mysql_at "$1" "$2" --batch --skip-column-names "$3" -e "$4"; }
dump_at() {
  local id="$1" password="$2"; shift 2
  MYSQL_PWD="$password" docker exec -i --env MYSQL_PWD "$id" mysqldump --no-defaults --protocol=socket -uroot \
    --skip-comments --skip-dump-date --hex-blob --skip-lock-tables --skip-add-drop-table \
    --no-tablespaces --set-gtid-purged=OFF --skip-column-statistics --single-transaction --quick "$@"
}
rows() { dump_at "$1" "$2" --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys "$3"; }
start_server() {
  local role="$1" password="$2" schema="$3" id ready=false
  id="$(docker run -d --rm --network none --name "orgresource-c9-local-$token-$role" \
    --label "orgresource.c9.local=$token" --label "orgresource.c9.role=$role" \
    --env "MYSQL_ROOT_PASSWORD=$password" mysql:8.0.46 --bind-address=127.0.0.1 --mysqlx=0)"
  if [ "$role" = source ]; then source_id="$id"; else target_id="$id"; fi
  for _ in $(seq 1 90); do
    if mysql_at "$id" "$password" -N -B -e 'SELECT 1;' >/dev/null 2>&1 &&
      sudo -n nsenter -t "$(docker inspect --format '{{.State.Pid}}' "$id")" -n -- bash -c 'exec 3<>/dev/tcp/127.0.0.1/3306' >/dev/null 2>&1; then
      ready=true; break
    fi
    sleep 1
  done
  [ "$ready" = true ] || fail C9_LOCAL_TEST_MYSQL_TIMEOUT
  mysql_at "$id" "$password" -e "CREATE DATABASE $schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
}
rows "$MYSQL_CONTAINER" "$EXT_DB_MIGRATION_PASSWORD" "$C9_ORIGINAL_TEST_SCHEMA" > "$work/original-before.sql"
[[ "$(scalar "$MYSQL_CONTAINER" "$EXT_DB_MIGRATION_PASSWORD" "$C9_ORIGINAL_TEST_SCHEMA" 'SELECT COUNT(*) FROM organization')" = 2 &&
 "$(scalar "$MYSQL_CONTAINER" "$EXT_DB_MIGRATION_PASSWORD" "$C9_ORIGINAL_TEST_SCHEMA" 'SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL')" = 0 ]] || fail C9_LOCAL_TEST_NOT_ORIGINAL
start_server source "$src_password" "$source_schema"
start_server restore "$dst_password" "$target_schema"
dump_at "$MYSQL_CONTAINER" "$EXT_DB_MIGRATION_PASSWORD" "$C9_ORIGINAL_TEST_SCHEMA" | mysql_at "$source_id" "$src_password" --binary-mode "$source_schema"
rows "$source_id" "$src_password" "$source_schema" > "$work/source-before.sql"
cmp -s "$work/original-before.sql" "$work/source-before.sql" || fail C9_LOCAL_TEST_CLONE_DIFF
source_uuid="$(scalar "$source_id" "$src_password" "$source_schema" 'SELECT @@server_uuid')"
target_uuid="$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT @@server_uuid')"
original_uuid="$(scalar "$MYSQL_CONTAINER" "$EXT_DB_MIGRATION_PASSWORD" "$C9_ORIGINAL_TEST_SCHEMA" 'SELECT @@server_uuid')"
[[ "$source_uuid" != "$target_uuid" && "$source_uuid" != "$original_uuid" && "$target_uuid" != "$original_uuid" ]] || fail C9_LOCAL_TEST_UUID
export C9_LOCAL_ROOT="$root" C9_LOCAL_TOKEN="$token" C9_LOCAL_CONFIRM="TEST ORIGINAL $token"
export C9_LOCAL_SOURCE_ID="$source_id" C9_LOCAL_RESTORE_ID="$target_id"
export C9_LOCAL_SOURCE_UUID="$source_uuid" C9_LOCAL_RESTORE_UUID="$target_uuid"
export C9_LOCAL_SOURCE_PASSWORD="$src_password" C9_LOCAL_RESTORE_PASSWORD="$dst_password" EXT_BACKUP_KEY="$backup_key"
archive="$work/original.aesgcm"
c5() { pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 -Mode "$1" -Schema "$2" -Port 3306 -Archive "$3" -LocalOriginalAcceptance; }
refused() {
  local label="$1" expected="$2"; shift 2
  if "$@" > "$work/refusal-private.log" 2>&1; then fail "C9_LOCAL_UNEXPECTED_SUCCESS:$label"; fi
  grep -Fq "$expected" "$work/refusal-private.log" || fail "C9_LOCAL_WRONG_REFUSAL:$label"
  [ "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()')" = 0 ] || fail C9_LOCAL_REFUSAL_CHANGED_TARGET
  echo "C9_LOCAL_ORIGINAL_REFUSAL_OK:$label"
}
refused wrong-schema C9_LOCAL_SCHEMA_REQUIRED c5 Backup orgresource_not_this_test "$archive"
refused wrong-confirm C9_LOCAL_IDENTITY_REQUIRED env C9_LOCAL_CONFIRM=no pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 -Mode Backup -Schema "$source_schema" -Port 3306 -Archive "$archive" -LocalOriginalAcceptance
refused outside-root C9_LOCAL_ARCHIVE_PATH_REQUIRED c5 Backup "$source_schema" "$root/outside.aesgcm"
refused wrong-container C9_LOCAL_CONTAINER_REQUIRED env C9_LOCAL_SOURCE_ID="$MYSQL_CONTAINER" pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 -Mode Backup -Schema "$source_schema" -Port 3306 -Archive "$archive" -LocalOriginalAcceptance
refused wrong-uuid C9_LOCAL_SERVER_IDENTITY_REQUIRED env C9_LOCAL_SOURCE_UUID=00000000-0000-0000-0000-000000000001 pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 -Mode Backup -Schema "$source_schema" -Port 3306 -Archive "$archive" -LocalOriginalAcceptance
[ ! -e "$archive" ] || fail C9_LOCAL_UNEXPECTED_ARCHIVE
c5 Backup "$source_schema" "$archive" > "$work/backup-private.log"
[ -s "$archive" ] || fail C9_LOCAL_ARCHIVE_MISSING
archive_hash="$(sha256sum "$archive" | cut -d' ' -f1)"
refused overwrite 'Choose a new archive path' c5 Backup "$source_schema" "$archive"
[ "$(sha256sum "$archive" | cut -d' ' -f1)" = "$archive_hash" ] || fail C9_LOCAL_ARCHIVE_CHANGED
wrong_key="$(openssl rand -base64 32 | tr -d '\n')"
echo "::add-mask::$wrong_key"
refused wrong-key 'Backup authentication failed' env EXT_BACKUP_KEY="$wrong_key" pwsh -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 -Mode Restore -Schema "$target_schema" -Port 3306 -Archive "$archive" -LocalOriginalAcceptance
python3 - "$archive" "$work/tampered.aesgcm" <<'PY'
import pathlib,sys
p=bytearray(pathlib.Path(sys.argv[1]).read_bytes())
p[-1]^=1
pathlib.Path(sys.argv[2]).write_bytes(p)
PY
refused tampered 'Backup authentication failed' c5 Restore "$target_schema" "$work/tampered.aesgcm"
c5 Restore "$target_schema" "$archive" > "$work/restore-private.log"
rows "$target_id" "$dst_password" "$target_schema" > "$work/target.sql"
cmp -s "$work/source-before.sql" "$work/target.sql" || fail C9_LOCAL_ORIGINAL_ROW_MISMATCH
[ "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM SPRING_SESSION')" = 0 ] || fail C9_LOCAL_SESSIONS
[ "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES')" = 0 ] || fail C9_LOCAL_SESSIONS
if c5 Restore "$target_schema" "$archive" > "$work/refusal-private.log" 2>&1; then fail C9_LOCAL_NONEMPTY_ACCEPTED; fi
grep -Fq 'Restore requires a newly created empty schema' "$work/refusal-private.log" || fail C9_LOCAL_NONEMPTY_WRONG_ERROR
rows "$target_id" "$dst_password" "$target_schema" > "$work/target-repeat.sql"
cmp -s "$work/target.sql" "$work/target-repeat.sql" || fail C9_LOCAL_NONEMPTY_CHANGED
echo C9_LOCAL_ORIGINAL_REFUSAL_OK:nonempty
# Real V2 baseline in POSIX namespace, using SELECT-only credential.
mysql_at "$target_id" "$dst_password" -e "CREATE USER 'c9_local_ro'@'%' IDENTIFIED BY '$ro_password'; GRANT SELECT ON $target_schema.* TO 'c9_local_ro'@'%';"
fp="$(printf '%s\0%s' "$target_schema" "$target_uuid" | sha256sum | cut -d' ' -f1)"
pid="$(docker inspect --format '{{.State.Pid}}' "$target_id")"
[[ "$(sudo -n nsenter -t "$pid" -n -- ip -o link show | wc -l)" = 1 && -z "$(sudo -n nsenter -t "$pid" -n -- ip -4 route show)" && -z "$(sudo -n nsenter -t "$pid" -n -- ip -6 route show default)" ]] || fail C9_LOCAL_TEST_NETWORK
baseline="$work/baseline.properties"
{
  printf 'export EXT_DB_URL=%q\n' "jdbc:mysql://127.0.0.1:3306/$target_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  printf 'export EXT_DB_USER=c9_local_ro\nexport EXT_DB_PASSWORD=%q\n' "$ro_password"
  printf 'export EXT_C8_HOSTED_EXPECTED_SCHEMA=%q\nEXT_C8_HOSTED_EXPECTED_FINGERPRINT=%q\n' "$target_schema" "$fp"
  printf 'export EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID=%q\nEXT_C8_HOSTED_OWNER_PUBLIC_ID=%q\n' "$C9_ORIGINAL_TEST_ORG" "$C9_ORIGINAL_TEST_OWNER"
  printf 'export EXT_C8_BASELINE_HMAC_HEX=%q\nEXT_SMS_ENABLED=false\nEXT_REMINDER_MODE=DISABLED\n' "$hmac_key"
} > "$work/ro.env"
base_call() {
  sudo -n nsenter -t "$pid" -n -- setpriv --reuid "$(id -u)" --regid "$(id -g)" --init-groups \
    env -i "PATH=$PATH" "HOME=$HOME" LANG=C.UTF-8 bash -ec 'set -a; source "$1"; set +a; export EXT_C8_BASELINE_MODE="$2" EXT_C8_BASELINE_FILE="$3"; exec java -jar "$4" --c8-hosted-original-baseline' \
    _ "$work/ro.env" "$1" "$2" "$jar"
}
base_call CAPTURE "$baseline" > "$work/baseline-private.log" 2>&1
grep -q C8_ORIGINAL_BASELINE_CAPTURED "$work/baseline-private.log" || fail C9_LOCAL_BASELINE_CAPTURE
[ "$(stat -c '%a' "$baseline")" = 600 ] || fail C9_LOCAL_BASELINE_MODE
base_call VERIFY "$baseline" > "$work/baseline-private.log" 2>&1
grep -q C8_ORIGINAL_BASELINE_READ_ONLY_OK "$work/baseline-private.log" || fail C9_LOCAL_BASELINE_VERIFY
baseline_hash="$(sha256sum "$baseline" | cut -d' ' -f1)"
chmod 644 "$baseline"
if base_call VERIFY "$baseline" > "$work/baseline-private.log" 2>&1; then fail C9_LOCAL_BASELINE_MODE_ACCEPTED; fi
grep -q 'not private mode 0600' "$work/baseline-private.log" || fail C9_LOCAL_BASELINE_WRONG_ERROR
chmod 600 "$baseline"
ln -s "$baseline" "$work/baseline-link.properties"
if base_call VERIFY "$work/baseline-link.properties" > "$work/baseline-private.log" 2>&1; then fail C9_LOCAL_BASELINE_LINK_ACCEPTED; fi
grep -q 'not private mode 0600' "$work/baseline-private.log" || fail C9_LOCAL_BASELINE_WRONG_ERROR
[ "$(sha256sum "$baseline" | cut -d' ' -f1)" = "$baseline_hash" ] || fail C9_LOCAL_BASELINE_CHANGED
base_call VERIFY "$baseline" > "$work/baseline-private.log" 2>&1
grep -q C8_ORIGINAL_BASELINE_READ_ONLY_OK "$work/baseline-private.log" || fail C9_LOCAL_BASELINE_VERIFY
rows "$source_id" "$src_password" "$source_schema" > "$work/source-after.sql"
rows "$target_id" "$dst_password" "$target_schema" > "$work/target-after.sql"
rows "$MYSQL_CONTAINER" "$EXT_DB_MIGRATION_PASSWORD" "$C9_ORIGINAL_TEST_SCHEMA" > "$work/original-after.sql"
cmp -s "$work/source-before.sql" "$work/source-after.sql" && cmp -s "$work/target.sql" "$work/target-after.sql" && cmp -s "$work/original-before.sql" "$work/original-after.sql" || fail C9_LOCAL_SOURCE_CHANGED
echo "C9_LOCAL_ORIGINAL_TRANSPORT_OK: archive_bytes=$(stat -c%s "$archive") original_orgs=2 independent_servers=true row_equal=true no_sessions=true refusals=9"
echo C9_LOCAL_POSIX_BASELINE_OK:actual_v2=true mode_0600=true wrong_mode_refused=true symlink_refused=true readonly=true
cleanup
trap - EXIT
for id in "$source_id" "$target_id"; do if docker inspect "$id" >/dev/null 2>&1; then fail C9_LOCAL_TEST_CLEANUP; fi; done
[ ! -e "$root" ] || fail C9_LOCAL_TEST_CLEANUP
echo C9_LOCAL_ORIGINAL_CLEANUP_OK

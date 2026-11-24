#!/usr/bin/env bash
# Local-only R06B1/B2. Approved import is an owner-terminal-only disposable rehearsal.
# Preparation creates two servers; approved-import adds an independent third recovery server.
set +x
set -euo pipefail
umask 077
fail() { printf '%s\n' "$1" >&2; exit 1; }
phase=intake
trap 'echo "R06B1_FAILURE_PHASE:$phase" >&2' ERR
[[ $# -eq 5 || $# -eq 6 ]] || fail R06B1_ARGUMENTS
mode="$1"; root="$2"; source_sha="$3"; jar="$4"; jar_sha="$5"; plan="${6:-}"
[[ ( "$mode" = --fixture-only && $# -eq 5 ) || ( ( "$mode" = --approved-plan || "$mode" = --approved-import ) && $# -eq 6 ) ]] || fail R06B1_ARGUMENTS
if [ "$mode" = --approved-import ]; then
  [ "${GITHUB_ACTIONS:-}" != true ] || fail R06B2_IMPORT_CI_REFUSED
  [[ -t 0 && -t 1 && -t 2 ]] || fail R06B2_OWNER_TERMINAL_REQUIRED
fi
repo="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
cd "$repo"
[[ "$(uname -s)" = Linux && "$(id -u)" != 0 ]] || fail R06B1_LINUX_NONROOT_REQUIRED
[[ "$root" = /* && -d "$root" && "$(realpath -e -- "$root")" = "$root" && "$(stat -c '%a:%u' -- "$root")" = "700:$(id -u)" ]] || fail R06B1_PRIVATE_ROOT_REQUIRED
case "$root/" in "$repo/"*) fail R06B1_PRIVATE_ROOT_REQUIRED;; esac
case "$repo/" in "$root/"*) fail R06B1_PRIVATE_ROOT_REQUIRED;; esac
[[ "$source_sha" =~ ^[a-f0-9]{40}$ && "$(git rev-parse HEAD)" = "$source_sha" ]] || fail R06B1_SOURCE_SHA_MISMATCH
git diff --quiet HEAD -- || fail R06B1_DIRTY_TRACKED_SOURCE
[[ "$jar" = /* && -f "$jar" && "$(realpath -e -- "$jar")" = "$jar" && "$jar_sha" =~ ^[a-f0-9]{64}$ ]] || fail R06B1_JAR_REQUIRED
[[ "$(sha256sum -- "$jar" | cut -d' ' -f1)" = "$jar_sha" ]] || fail R06B1_JAR_HASH_MISMATCH
python3 -I - "$repo" "$source_sha" "$jar" <<'PY'
import pathlib, subprocess, sys, zipfile, re
repo, sha, jar = sys.argv[1:]
try:
    prefix="extended/backend/src/main/resources/db/migration/"
    paths=subprocess.check_output(["git","-C",repo,"ls-tree","-r","--name-only",sha,"--",prefix],text=True).splitlines()
    versions=[re.fullmatch(r"V([1-7])__.+\.sql",pathlib.PurePosixPath(p).name) for p in paths]
    if not (len(paths)==7 and all(versions) and {m[1] for m in versions}==set("1234567")):
        raise ValueError("migration inventory")
    with zipfile.ZipFile(jar) as z:
        names=z.namelist()
        actual=[n for n in names if n.startswith("BOOT-INF/classes/db/migration/") and n.endswith(".sql")]
        expected=["BOOT-INF/classes/db/migration/"+pathlib.PurePosixPath(p).name for p in paths]
        if sorted(actual)!=sorted(expected):
            raise ValueError("jar migrations")
        for path, member in zip(paths, expected):
            if z.read(member)!=subprocess.check_output(["git","-C",repo,"cat-file","blob",sha+":"+path]):
                raise ValueError("migration bytes")
        if names.count("BOOT-INF/classes/static/index.html")!=1 or not any(n.startswith("BOOT-INF/classes/static/assets/") and n.endswith(".js") for n in names):
            raise ValueError("packaged assets")
except Exception:
    print("R06B1_JAR_MIGRATION_OR_ASSET_MISMATCH",file=sys.stderr)
    sys.exit(1)
PY
approved_sha=3b0f54da85cadb77cd7d8ab2d50c1fdb4b4ee36710b803cf904227e13f08927c
if [ "$mode" != --fixture-only ]; then
  [[ "$plan" = /* && -f "$plan" && "$(realpath -e -- "$plan")" = "$plan" && "$(stat -c '%a:%u' -- "$plan")" = "600:$(id -u)" ]] || fail R06B1_PRIVATE_PLAN_REQUIRED
  case "$plan" in "$repo/"*) fail R06B1_PRIVATE_PLAN_REQUIRED;; esac
  [[ "$(stat -c '%s' -- "$plan")" = 19265850 ]] || fail R06B1_APPROVED_PLAN_MISMATCH
  # Even an exact-size candidate must never be opened or copied in public CI.
  [ "${GITHUB_ACTIONS:-}" != true ] || fail R06B1_PRIVATE_PLAN_CI_REFUSED
  [[ "$(sha256sum -- "$plan" | cut -d' ' -f1)" = "$approved_sha" ]] || fail R06B1_APPROVED_PLAN_MISMATCH
fi
socket=unix:///var/run/docker.sock
[[ -z "${DOCKER_HOST:-}" || "$DOCKER_HOST" = "$socket" ]] || fail R06B1_LOCAL_DOCKER_REQUIRED
[[ -z "${DOCKER_CONTEXT:-}" || "$DOCKER_CONTEXT" = default ]] || fail R06B1_LOCAL_DOCKER_REQUIRED
[[ -z "${DOCKER_TLS_VERIFY:-}" && -z "${DOCKER_CERT_PATH:-}" && -S /var/run/docker.sock ]] || fail R06B1_LOCAL_DOCKER_REQUIRED
unset DOCKER_HOST DOCKER_CONTEXT DOCKER_TLS_VERIFY DOCKER_CERT_PATH
for tool in docker pwsh java python3 openssl sudo nsenter setpriv ip timeout; do command -v "$tool" >/dev/null || fail R06B1_TOOL_MISSING; done
sudo -n true >/dev/null 2>&1 || fail R06B1_NSENTER_UNAVAILABLE
docker_local() { docker --host="$socket" "$@"; }
docker_local image inspect mysql:8.0.46 >/dev/null 2>&1 || fail R06B1_LOCAL_IMAGE_MISSING
java_bin="$(command -v java)"
pwsh_bin="$(command -v pwsh)"
"$java_bin" -version 2>&1 | grep -Eq 'version "21[."]' || fail R06B1_JAVA21_REQUIRED
"$pwsh_bin" -NoProfile -NonInteractive -Command 'if ($PSVersionTable.PSVersion -lt [version]"7.4") {exit 1}' || fail R06B1_POWERSHELL_REQUIRED
echo R06B1_PREFLIGHT_OK:source_hash=true jar_hash=true seven_migration_bytes=true packaged_assets=true local_docker=true
token="$(openssl rand -hex 6)"
work="$root/orgresource-c9-local-$token"
mkdir -m 700 -- "$work"
source_id= target_id= mixed_id= receipt_dir=
cleanup() {
  local result=0 id role remaining
  docker_local info >/dev/null 2>&1 || return 1
  for role in source restore mixed; do
    case "$role" in source) id="$source_id";; restore) id="$target_id";; mixed) id="$mixed_id";; esac
    if [ -n "$id" ] && docker_local container inspect "$id" >/dev/null 2>&1; then
      if [ "$(docker_local inspect --format '{{index .Config.Labels "orgresource.c9.local"}}' "$id")" = "$token" ] &&
         [ "$(docker_local inspect --format '{{index .Config.Labels "orgresource.c9.role"}}' "$id")" = "$role" ]; then
        docker_local rm -f -v "$id" >/dev/null 2>&1 || result=1
      else
        result=1
      fi
    fi
  done
  remaining="$(docker_local ps -a --filter "label=orgresource.c9.local=$token" --format '{{.ID}}')" || result=1
  [ -z "$remaining" ] || result=1
  if [ "$result" = 0 ]; then rm -rf -- "$work" || result=1; fi
  return "$result"
}
trap 'code=$?; cleanup || { echo R06B1_CLEANUP_FAILED >&2; code=1; }; if [ -n "$receipt_dir" ]; then printf "exit=%s\nphase=%s\n" "$code" "$phase" > "$receipt_dir/status.txt"; fi; exit "$code"' EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
mkdir -m 700 -- "$work/home"
# Snapshot the checked binary; no subsequent use of a mutable external JAR path.
cp -- "$jar" "$work/verified.jar"
chmod 600 "$work/verified.jar"
[[ "$(sha256sum -- "$work/verified.jar" | cut -d' ' -f1)" = "$jar_sha" ]] || fail R06B1_JAR_HASH_MISMATCH
jar="$work/verified.jar"
if [ "$mode" != --fixture-only ]; then
  # Snapshot the approved bytes into private custody; recheck before database work.
  cp -- "$plan" "$work/approved-plan.json"
  chmod 600 "$work/approved-plan.json"
  [[ "$(stat -c '%s' "$work/approved-plan.json")" = 19265850 && "$(sha256sum "$work/approved-plan.json" | cut -d' ' -f1)" = "$approved_sha" ]] || fail R06B1_APPROVED_PLAN_MISMATCH
fi
source_schema="orgresource_c9_local_${token}_source"
target_schema="orgresource_c9_local_${token}_restore"
src_password="$(openssl rand -hex 24)"
dst_password="$(openssl rand -hex 24)"
bootstrap_password="$(openssl rand -hex 24)"
migrate_password="$(openssl rand -hex 24)"
owner_password="$(openssl rand -hex 24)"
ro_password="$(openssl rand -hex 24)"
backup_key="$(openssl rand -base64 32 | tr -d '\n')"
contact_key="$(openssl rand -base64 32 | tr -d '\n')"
hmac_key="$(openssl rand -hex 32)"
if [ "${GITHUB_ACTIONS:-}" = true ]; then
  for value in "$src_password" "$dst_password" "$bootstrap_password" "$migrate_password" "$owner_password" "$ro_password" "$backup_key" "$contact_key" "$hmac_key"; do echo "::add-mask::$value"; done
fi
# Only newly created test-owned IDs are accepted by helper functions.
assert_id() {
  local id="$1" role
  [[ "$id" =~ ^[a-f0-9]{64}$ ]] || fail R06B1_CONTAINER_ID
  if [ "$id" = "$source_id" ]; then role=source; elif [ "$id" = "$target_id" ]; then role=restore; elif [ "$id" = "$mixed_id" ]; then role=mixed; else fail R06B1_CONTAINER_ID; fi
  [[ "$(docker_local inspect --format '{{.Id}}' "$id")" = "$id" &&
     "$(docker_local inspect --format '{{.Name}}' "$id")" = "/orgresource-c9-local-$token-$role" &&
     "$(docker_local inspect --format '{{.Config.Image}}' "$id")" = mysql:8.0.46 &&
     "$(docker_local inspect --format '{{.HostConfig.NetworkMode}}' "$id")" = none &&
     "$(docker_local inspect --format '{{index .Config.Labels "orgresource.c9.local"}}' "$id")" = "$token" &&
     "$(docker_local inspect --format '{{index .Config.Labels "orgresource.c9.role"}}' "$id")" = "$role" &&
     -z "$(docker_local port "$id")" ]] || fail R06B1_CONTAINER_BOUNDARY
}
ns_run() {
  local id="$1" pid; shift
  assert_id "$id"
  pid="$(docker_local inspect --format '{{.State.Pid}}' "$id")"
  [[ "$pid" =~ ^[1-9][0-9]*$ ]] || fail R06B1_NAMESPACE_PID
  sudo -n nsenter -t "$pid" -n -- setpriv --reuid "$(id -u)" --regid "$(id -g)" --clear-groups --no-new-privs \
    env -i "PATH=$PATH" "HOME=$work/home" LANG=C.UTF-8 bash -ec '
      test "$(id -u)" != 0
      test "$(ip -o link show | wc -l)" = 1
      ip -o link show lo >/dev/null
      test -z "$(ip -4 route show)"
      test -z "$(ip -6 route show default)"
      exec "$@"
    ' _ "$@"
}
mysql_at() {
  local id="$1" password="$2"; shift 2
  assert_id "$id"
  MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$id" mysql --no-defaults --protocol=socket -uroot "$@"
}
scalar() { mysql_at "$1" "$2" -N -B "$3" -e "$4"; }
rows_at() {
  local id="$1" password="$2" schema="$3"
  assert_id "$id"
  MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$id" mysqldump --no-defaults --protocol=socket -uroot \
    --skip-comments --skip-dump-date --hex-blob --skip-lock-tables --skip-add-drop-table \
    --no-tablespaces --set-gtid-purged=OFF --skip-column-statistics --single-transaction --quick \
    --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys "$schema"
}
start_server() {
  local role="$1" password="$2" schema="$3" id ready=false
  id="$(MYSQL_ROOT_PASSWORD="$password" docker_local run -d --rm --pull never --network none \
    --name "orgresource-c9-local-$token-$role" --label "orgresource.c9.local=$token" \
    --label "orgresource.c9.role=$role" --env MYSQL_ROOT_PASSWORD mysql:8.0.46 --bind-address=127.0.0.1 --mysqlx=0)"
  case "$role" in source) source_id="$id";; restore) target_id="$id";; mixed) mixed_id="$id";; *) fail R06B1_CONTAINER_ID;; esac
  for _ in $(seq 1 90); do
    if mysql_at "$id" "$password" -N -B -e 'SELECT 1' >/dev/null 2>&1 &&
       ns_run "$id" bash -c 'exec 3<>/dev/tcp/127.0.0.1/3306' >/dev/null 2>&1; then ready=true; break; fi
    sleep 1
  done
  [ "$ready" = true ] || fail R06B1_MYSQL_READY_TIMEOUT
  [[ "$(mysql_at "$id" "$password" -N -B -e 'SELECT VERSION()')" = 8.0.46 ]] || fail R06B1_MYSQL_VERSION
  mysql_at "$id" "$password" -e "CREATE DATABASE $schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
}
run_java() {
  local id="$1" settings="$2" command="$3" limit=240s
  case "$command" in --c8-hosted-demo-import) limit=1800s;; --c8-hosted-demo-postflight) limit=900s;; esac
  ns_run "$id" timeout --signal=TERM --kill-after=10s "$limit" bash -ec 'set -a; source "$1"; set +a; shift; exec "$@"' \
    _ "$settings" "$java_bin" -Xmx768m -jar "$jar" "$command"
}
phase=bootstrap
start_server source "$src_password" "$source_schema"
start_server restore "$dst_password" "$target_schema"
source_uuid="$(scalar "$source_id" "$src_password" "$source_schema" 'SELECT @@server_uuid')"
target_uuid="$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT @@server_uuid')"
[[ "$source_uuid" != "$target_uuid" ]] || fail R06B1_SERVER_UUID
# Bootstrap-only account on the fresh schema, removed after initialization.
# Not a web/runtime identity; elevated Flyway user is separate.
mysql_at "$source_id" "$src_password" <<SQL
CREATE USER 'c9_bootstrap'@'%' IDENTIFIED BY '$bootstrap_password';
CREATE USER 'c9_migrator'@'%' IDENTIFIED BY '$migrate_password';
GRANT SELECT,INSERT,UPDATE,DELETE ON $source_schema.* TO 'c9_bootstrap'@'%';
GRANT ALL PRIVILEGES ON $source_schema.* TO 'c9_migrator'@'%';
SQL
source_url="jdbc:mysql://127.0.0.1:3306/$source_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
{
  printf 'EXT_DB_URL=%q\nEXT_DB_MIGRATION_URL=%q\n' "$source_url" "$source_url"
  printf 'EXT_DB_USER=c9_bootstrap\nEXT_DB_PASSWORD=%q\nEXT_DB_MIGRATION_USER=c9_migrator\nEXT_DB_MIGRATION_PASSWORD=%q\n' "$bootstrap_password" "$migrate_password"
  printf 'EXT_SMS_ENABLED=false\nEXT_REMINDER_MODE=DISABLED\nEXT_CONTACT_ENCRYPTION_KEY=%q\n' "$contact_key"
  printf 'EXT_BOOTSTRAP_SLUG=isolated-original-workspace\nEXT_BOOTSTRAP_ORG_NAME=%q\n' 'Isolated Original Workspace'
  printf 'EXT_BOOTSTRAP_USERNAME=additive-original-owner\nEXT_BOOTSTRAP_DISPLAY_NAME=%q\nEXT_BOOTSTRAP_PASSWORD=%q\n' 'Fictional Original Owner' "$owner_password"
} > "$work/bootstrap.env"
run_java "$source_id" "$work/bootstrap.env" --bootstrap > "$work/bootstrap-private.log" 2>&1 || fail R06B1_BOOTSTRAP_FAILED
grep -Fq 'owner initialized' "$work/bootstrap-private.log" || fail R06B1_BOOTSTRAP_MARKER
rm -f -- "$work/bootstrap-private.log" "$work/bootstrap.env"
mysql_at "$source_id" "$src_password" -e "DROP USER 'c9_bootstrap'@'%','c9_migrator'@'%';"
mysql_at "$source_id" "$src_password" "$source_schema" <<'SQL'
INSERT INTO department (organization_id,name,name_key,version)
SELECT id,'Existing fictional operations','existing fictional operations',0 FROM organization WHERE slug='isolated-original-workspace';
INSERT INTO resource (public_id,organization_id,name,name_key,type,total_quantity,version)
SELECT UUID_TO_BIN(UUID(),0),id,'Existing fictional projector','existing fictional projector','REUSABLE',0,0
FROM organization WHERE slug='isolated-original-workspace';
INSERT INTO organization(public_id,slug,display_name,status,version,created_at,updated_at)
VALUES(UUID_TO_BIN(UUID(),0),'isolated-other-original','Other Original Org','ACTIVE',0,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
INSERT INTO user_account(public_id,username_key,display_name,status,credential_version,version,created_at,updated_at)
VALUES(UUID_TO_BIN(UUID(),0),'other-original-user','Other Original User','ACTIVE',0,0,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
INSERT INTO membership(public_id,org_id,user_id,role,status,version,created_at,updated_at)
SELECT UUID_TO_BIN(UUID(),0),o.id,u.id,'MEMBER','ACTIVE',0,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)
FROM organization o CROSS JOIN user_account u WHERE o.slug='isolated-other-original' AND u.username_key='other-original-user';
INSERT INTO department(organization_id,name,name_key,version)
SELECT id,'Other original department','other original department',0 FROM organization WHERE slug='isolated-other-original';
SQL
original_org="$(scalar "$source_id" "$src_password" "$source_schema" "SELECT BIN_TO_UUID(public_id,0) FROM organization WHERE slug='isolated-original-workspace'")"
owner_id="$(scalar "$source_id" "$src_password" "$source_schema" "SELECT BIN_TO_UUID(public_id,0) FROM user_account WHERE username_key='additive-original-owner'")"
[[ "$original_org" =~ ^[a-f0-9-]{36}$ && "$owner_id" =~ ^[a-f0-9-]{36}$ &&
   "$(scalar "$source_id" "$src_password" "$source_schema" 'SELECT COUNT(*) FROM organization')" = 2 &&
   "$(scalar "$source_id" "$src_password" "$source_schema" "SELECT COUNT(*) FROM audit_event WHERE action='BOOTSTRAP_OWNER_CREATED'")" = 1 &&
   "$(scalar "$source_id" "$src_password" "$source_schema" 'SELECT COUNT(*) FROM SPRING_SESSION')" = 0 &&
   "$(scalar "$source_id" "$src_password" "$source_schema" 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES')" = 0 ]] || fail R06B1_ORIGINAL_FIXTURE
echo R06B1_BOOTSTRAP_OK:original_orgs=2 actual_bootstrap=true bootstrap_audit=true sessions_empty=true
phase=original_restore
rows_at "$source_id" "$src_password" "$source_schema" > "$work/source-before.sql"
archive="$work/original.aesgcm"
c5() {
  env -i "PATH=$PATH" "HOME=$work/home" LANG=C.UTF-8 EXT_SMS_ENABLED=false EXT_REMINDER_MODE=DISABLED \
    C9_LOCAL_ROOT="$root" C9_LOCAL_TOKEN="$token" C9_LOCAL_CONFIRM="TEST ORIGINAL $token" \
    C9_LOCAL_SOURCE_ID="$source_id" C9_LOCAL_RESTORE_ID="$target_id" C9_LOCAL_SOURCE_UUID="$source_uuid" C9_LOCAL_RESTORE_UUID="$target_uuid" \
    C9_LOCAL_SOURCE_PASSWORD="$src_password" C9_LOCAL_RESTORE_PASSWORD="$dst_password" EXT_BACKUP_KEY="$backup_key" \
    "$pwsh_bin" -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 \
      -Mode "$1" -Schema "$2" -Port 3306 -Archive "$archive" -LocalOriginalAcceptance
}
c5 Backup "$source_schema" > "$work/backup-private.log" 2>&1 || fail R06B1_BACKUP_FAILED
c5 Restore "$target_schema" > "$work/restore-private.log" 2>&1 || fail R06B1_RESTORE_FAILED
rows_at "$target_id" "$dst_password" "$target_schema" > "$work/target-before.sql"
cmp -s "$work/source-before.sql" "$work/target-before.sql" || fail R06B1_RESTORED_ROWS_MISMATCH
echo "R06B1_ORIGINAL_RESTORE_OK:archive_bytes=$(stat -c '%s' "$archive") distinct_servers=true exact_rows=true sessions_empty=true"
phase=baseline
mysql_at "$target_id" "$dst_password" -e "CREATE USER 'c9_local_ro'@'%' IDENTIFIED BY '$ro_password'; GRANT SELECT ON $target_schema.* TO 'c9_local_ro'@'%';"
fp="$(printf '%s\0%s' "$target_schema" "$target_uuid" | sha256sum | cut -d' ' -f1)"
baseline="$work/baseline.properties"
{
  printf 'EXT_DB_URL=%q\n' "jdbc:mysql://127.0.0.1:3306/$target_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  printf 'EXT_DB_USER=c9_local_ro\nEXT_DB_PASSWORD=%q\n' "$ro_password"
  printf 'EXT_C8_HOSTED_EXPECTED_SCHEMA=%q\nEXT_C8_HOSTED_EXPECTED_FINGERPRINT=%q\n' "$target_schema" "$fp"
  printf 'EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID=%q\nEXT_C8_HOSTED_OWNER_PUBLIC_ID=%q\n' "$original_org" "$owner_id"
  printf 'EXT_C8_BASELINE_HMAC_HEX=%q\nEXT_C8_BASELINE_FILE=%q\nEXT_C8_BASELINE_MODE=CAPTURE\n' "$hmac_key" "$baseline"
  printf 'EXT_SMS_ENABLED=false\nEXT_REMINDER_MODE=DISABLED\n'
  if [ "$mode" != --fixture-only ]; then printf 'EXT_C8_DEMO_PLAN=%q\n' "$work/approved-plan.json"; fi
} > "$work/ro.env"
if [ "$mode" != --fixture-only ]; then
  phase=approved_preflight
  run_java "$target_id" "$work/ro.env" --c8-hosted-demo-preflight > "$work/preflight-private.log" 2>&1 || fail R06B1_APPROVED_PREFLIGHT_FAILED
  for line in HOSTED_DEMO_PREFLIGHT_READ_ONLY_OK "target_fingerprint_sha256=$fp" original_organization_count=2 flyway_version=7 approved_full_plan=96_workspaces_21060_requests database_writes=0; do
    grep -Fxq "$line" "$work/preflight-private.log" || fail R06B1_APPROVED_PREFLIGHT_MARKER
  done
  echo R06B1_APPROVED_PREFLIGHT_OK:exact_plan=true fingerprint_match=true read_only=true
else
  echo R06B1_APPROVED_PREFLIGHT_NOT_RUN:public_fixture_only
fi
phase=baseline
run_java "$target_id" "$work/ro.env" --c8-hosted-original-baseline > "$work/baseline-private.log" 2>&1 || fail R06B1_CAPTURE_FAILED
grep -q C8_ORIGINAL_BASELINE_CAPTURED "$work/baseline-private.log" || fail R06B1_CAPTURE_MARKER
[[ "$(stat -c '%a:%u' "$baseline")" = "600:$(id -u)" ]] || fail R06B1_BASELINE_MODE
printf '\nEXT_C8_BASELINE_MODE=VERIFY\n' >> "$work/ro.env"
run_java "$target_id" "$work/ro.env" --c8-hosted-original-baseline > "$work/baseline-private.log" 2>&1 || fail R06B1_VERIFY_FAILED
grep -q C8_ORIGINAL_BASELINE_READ_ONLY_OK "$work/baseline-private.log" || fail R06B1_VERIFY_MARKER
rows_at "$source_id" "$src_password" "$source_schema" > "$work/source-after.sql"
rows_at "$target_id" "$dst_password" "$target_schema" > "$work/target-after.sql"
cmp -s "$work/source-before.sql" "$work/source-after.sql" && cmp -s "$work/target-before.sql" "$work/target-after.sql" || fail R06B1_PREPARATION_CHANGED_ROWS
echo R06B1_BASELINE_OK:actual_v2=true readonly=true mode_0600=true source_and_target_rows_unchanged=true
echo R06B1_PREPARATION_OK:importer_run=false attestation_created=false

# Fixture-only scope for a real grant test and the eventual private importer.
# No account credential writes or ledger mutation/delete privileges.
grant_importer() {
  local password="$1" table
  mysql_at "$target_id" "$dst_password" -e "CREATE USER 'c9_local_importer'@'%' IDENTIFIED BY '$password'; GRANT SELECT ON $target_schema.* TO 'c9_local_importer'@'%';"
  for table in organization user_account membership department project resource resource_requirement allocation; do
    mysql_at "$target_id" "$dst_password" -e "GRANT INSERT,UPDATE ON $target_schema.$table TO 'c9_local_importer'@'%';"
  done
  for table in stock_movement audit_event; do
    mysql_at "$target_id" "$dst_password" -e "GRANT INSERT ON $target_schema.$table TO 'c9_local_importer'@'%';"
  done
}
importer_sql() {
  local password="$1" query="$2"
  assert_id "$target_id"
  MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$target_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 -uc9_local_importer -N -B "$target_schema" -e "$query"
}
check_importer_grants() {
  local password="$1" query
  [[ "$(importer_sql "$password" 'SELECT COUNT(*) FROM organization')" = 2 ]] || fail R06B2_IMPORTER_SELECT
  # SQL has no qualifying rows; a privilege regression is a failure even if
  # a zero-row write would not change data. Require actual MySQL 1142.
  for query in 'UPDATE stock_movement SET id=id WHERE 1=0' 'UPDATE audit_event SET id=id WHERE 1=0' 'DELETE FROM allocation WHERE 1=0' 'INSERT INTO local_credential SELECT * FROM local_credential WHERE 1=0'; do
    if importer_sql "$password" "$query" >"$work/grant-private.log" 2>&1; then fail R06B2_UNEXPECTED_GRANT; fi
    grep -Eq 'ERROR 1142 .*command denied' "$work/grant-private.log" || fail R06B2_WRONG_GRANT_REFUSAL
  done
  echo R06B2_IMPORTER_GRANTS_OK:select=true ledger_update_denied=true delete_denied=true credential_insert_denied=true
}
if [ "$mode" = --fixture-only ]; then
  phase=importer_grants
  importer_password="$(openssl rand -hex 24)"
  if [ "${GITHUB_ACTIONS:-}" = true ]; then echo "::add-mask::$importer_password"; fi
  grant_importer "$importer_password"
  check_importer_grants "$importer_password"
  mysql_at "$target_id" "$dst_password" -e "DROP USER 'c9_local_importer'@'%';"
  rows_at "$target_id" "$dst_password" "$target_schema" > "$work/target-grant-after.sql"
  cmp -s "$work/target-before.sql" "$work/target-grant-after.sql" || fail R06B2_GRANT_TEST_CHANGED_ROWS
elif [ "$mode" = --approved-import ]; then
  phase=operator_confirmation
  # This receipt is generated from actual completed recovery and preflight,
  # not a fabricated claim. It is local-only and contains no raw rows/keys.
  archive_sha="$(sha256sum "$archive" | cut -d' ' -f1)"
  baseline_sha="$(sha256sum "$baseline" | cut -d' ' -f1)"
  row_sha="$(sha256sum "$work/source-before.sql" | cut -d' ' -f1)"
  restore_receipt="$work/restore-receipt.txt"
  {
    printf 'scope=LOCAL_DISPOSABLE_ONLY\nsource_sha=%s\njar_sha256=%s\nplan_sha256=%s\n' "$source_sha" "$jar_sha" "$approved_sha"
    printf 'source_server_uuid=%s\nrestore_server_uuid=%s\ntarget_fingerprint_sha256=%s\n' "$source_uuid" "$target_uuid" "$fp"
    printf 'archive_sha256=%s\narchive_bytes=%s\noriginal_rows_sha256=%s\nrestored_rows_sha256=%s\nbaseline_sha256=%s\n' "$archive_sha" "$(stat -c '%s' "$archive")" "$row_sha" "$row_sha" "$baseline_sha"
    printf 'actual_c5_restore=true\nindependent_server=true\nexact_rows_equal=true\nsessions_empty=true\nreadonly_preflight=true\nbaseline_verify=true\noriginal_source_unchanged=true\nhosted_import=false\noff_host_restore=false\n'
  } > "$restore_receipt"
  receipt_sha="$(sha256sum "$restore_receipt" | cut -d' ' -f1)"
  source scripts/c9-local-operator-gate.sh
  cat "$restore_receipt"
  printf 'restore_receipt_sha256=%s\n' "$receipt_sha"
  c9_require_operator_confirmation "$fp" "$receipt_sha" || fail R06B2_OPERATOR_DECLINED
  # Bind the operator's decision to this same live, quiescent target.
  [[ "$(scalar "$source_id" "$src_password" "$source_schema" 'SELECT @@server_uuid')" = "$source_uuid" &&
     "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT @@server_uuid')" = "$target_uuid" &&
     "$(sha256sum "$restore_receipt" | cut -d' ' -f1)" = "$receipt_sha" &&
     "$(sha256sum "$archive" | cut -d' ' -f1)" = "$archive_sha" &&
     "$(sha256sum "$baseline" | cut -d' ' -f1)" = "$baseline_sha" &&
     "$(sha256sum "$work/approved-plan.json" | cut -d' ' -f1)" = "$approved_sha" &&
     "$(sha256sum "$jar" | cut -d' ' -f1)" = "$jar_sha" ]] || fail R06B2_APPROVED_IDENTITY_CHANGED
  rows_at "$source_id" "$src_password" "$source_schema" > "$work/source-confirmed.sql"
  rows_at "$target_id" "$dst_password" "$target_schema" > "$work/target-confirmed.sql"
  cmp -s "$work/source-before.sql" "$work/source-confirmed.sql" && cmp -s "$work/target-before.sql" "$work/target-confirmed.sql" || fail R06B2_APPROVED_ROWS_CHANGED
  run_java "$target_id" "$work/ro.env" --c8-hosted-original-baseline > "$work/confirm-baseline-private.log" 2>&1 || fail R06B2_APPROVED_BASELINE_CHANGED
  grep -q C8_ORIGINAL_BASELINE_READ_ONLY_OK "$work/confirm-baseline-private.log" || fail R06B2_APPROVED_BASELINE_CHANGED
  # Retain restricted diagnostics/receipt outside the deleted test workdir.
  # The root is owner-only 0700; never publish these raw logs.
  candidate_receipt_dir="$root/orgresource-c9-receipt-$token"
  mkdir -m 700 -- "$candidate_receipt_dir"
  receipt_dir="$candidate_receipt_dir"
  cp -- "$restore_receipt" "$receipt_dir/restore-receipt.txt"
  printf 'operator_confirmation_received=true\nrestore_receipt_sha256=%s\nscope=LOCAL_DISPOSABLE_ONLY\n' "$receipt_sha" > "$receipt_dir/operator-receipt.txt"
  phase=importer_grants
  importer_password="$(openssl rand -hex 24)"
  grant_importer "$importer_password"
  check_importer_grants "$importer_password"
  cp -- "$work/ro.env" "$work/import.env"
  {
    printf '\nEXT_DB_USER=c9_local_importer\nEXT_DB_PASSWORD=%q\n' "$importer_password"
    printf 'EXT_CONTACT_ENCRYPTION_KEY=%q\n' "$contact_key"
    printf 'EXT_C8_HOSTED_CONFIRM=%q\nEXT_C8_HOSTED_BACKUP_ID=%q\n' "IMPORT 96 INTO $fp" "local-original-$token"
    printf 'EXT_C8_HOSTED_RESTORE_EVIDENCE_SHA256=%q\nEXT_C8_HOSTED_RESTORE_ATTESTATION=%q\n' "$receipt_sha" 'I VERIFIED ISOLATED RESTORE'
  } >> "$work/import.env"
  phase=import
  import_exit=0
  import_started="$(date +%s)"
  # Exactly one real import attempt. Never retry an incomplete cohort.
  run_java "$target_id" "$work/import.env" --c8-hosted-demo-import > "$receipt_dir/import-private.log" 2>&1 || import_exit=$?
  import_seconds="$(( $(date +%s) - import_started ))"
  rm -f -- "$work/import.env"
  mysql_at "$target_id" "$dst_password" -e "DROP USER 'c9_local_importer'@'%';"
  printf 'import_exit=%s\nimport_elapsed_seconds=%s\n' "$import_exit" "$import_seconds" > "$receipt_dir/import-result.txt"
  phase=post_import_baseline
  # Independent SELECT-only verification even if the command timed out/failed.
  run_java "$target_id" "$work/ro.env" --c8-hosted-original-baseline > "$receipt_dir/baseline-private.log" 2>&1 || fail R06B2_ORIGINAL_BASELINE_FAILED
  grep -q C8_ORIGINAL_BASELINE_READ_ONLY_OK "$receipt_dir/baseline-private.log" || fail R06B2_ORIGINAL_BASELINE_FAILED
  rows_at "$source_id" "$src_password" "$source_schema" > "$work/source-post-import.sql"
  cmp -s "$work/source-before.sql" "$work/source-post-import.sql" || fail R06B2_ORIGINAL_SOURCE_CHANGED
  [ "$import_exit" = 0 ] || fail R06B2_IMPORT_INCOMPLETE_NO_RETRY
  grep -q '^HOSTED_IMPORT_COMPLETE:' "$receipt_dir/import-private.log" || fail R06B2_IMPORT_MARKER_MISSING
  phase=postflight
  rows_at "$target_id" "$dst_password" "$target_schema" > "$work/imported-before-postflight.sql"
  [[ "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM organization')" = 98 &&
     "$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT COUNT(*) FROM organization WHERE demo_plan_sha='$approved_sha'")" = 96 &&
     "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM SPRING_SESSION')" = 0 &&
     "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES')" = 0 ]] || fail R06B2_MIXED_COUNTS
  run_java "$target_id" "$work/ro.env" --c8-hosted-demo-postflight > "$receipt_dir/postflight-private.log" 2>&1 || fail R06B2_POSTFLIGHT_FAILED
  grep -Fq 'HOSTED_DEMO_POSTFLIGHT_READ_ONLY_OK organizations=96 allocations=21060 events=67370;' "$receipt_dir/postflight-private.log" || fail R06B2_POSTFLIGHT_MARKER
  # Preflight must reject the now-populated cohort before a second write.
  # Do not invoke the importer a second time.
  if run_java "$target_id" "$work/ro.env" --c8-hosted-demo-preflight > "$receipt_dir/repeat-refusal-private.log" 2>&1; then fail R06B2_POPULATED_PREFLIGHT_ACCEPTED; fi
  grep -Fq 'existing or partial demo cohort' "$receipt_dir/repeat-refusal-private.log" || fail R06B2_WRONG_REPEAT_REFUSAL
  rows_at "$target_id" "$dst_password" "$target_schema" > "$work/imported-after-postflight.sql"
  cmp -s "$work/imported-before-postflight.sql" "$work/imported-after-postflight.sql" || fail R06B2_POSTFLIGHT_CHANGED_ROWS
  printf 'R06B2_LOCAL_MIXED_IMPORT_OK:organizations=98 tagged=96 allocations=21060 events=67370 original_baseline=true independent_readonly_postflight=true populated_preflight_refused=true import_seconds=%s\n' "$import_seconds" | tee "$receipt_dir/result.txt"
  phase=mixed_recovery
  # A third freshly initialized server, independent of both original servers.
  mixed_schema="orgresource_c9_local_${token}_mixed"
  mixed_password="$(openssl rand -hex 24)"
  mixed_ro_password="$(openssl rand -hex 24)"
  mixed_key="$(openssl rand -base64 32 | tr -d '\n')"
  start_server mixed "$mixed_password" "$mixed_schema"
  mixed_uuid="$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT @@server_uuid')"
  [[ "$mixed_uuid" != "$source_uuid" && "$mixed_uuid" != "$target_uuid" ]] || fail R06B3A_SERVER_UUID
  mixed_archive="$work/mixed.aesgcm"
  mixed_c5() {
    env -i "PATH=$PATH" "HOME=$work/home" LANG=C.UTF-8 EXT_SMS_ENABLED=false EXT_REMINDER_MODE=DISABLED \
      C9_LOCAL_ROOT="$root" C9_LOCAL_TOKEN="$token" C9_LOCAL_CONFIRM="TEST MIXED $token" \
      C9_LOCAL_SOURCE_ID="$target_id" C9_LOCAL_RESTORE_ID="$mixed_id" C9_LOCAL_SOURCE_UUID="$target_uuid" C9_LOCAL_RESTORE_UUID="$mixed_uuid" \
      C9_LOCAL_SOURCE_PASSWORD="$dst_password" C9_LOCAL_RESTORE_PASSWORD="$mixed_password" EXT_BACKUP_KEY="$mixed_key" \
      C9_LOCAL_MIXED_ARCHIVE_SHA="${mixed_archive_sha:-}" \
      "$pwsh_bin" -NoProfile -NonInteractive -File scripts/extended-c-backup.ps1 \
        -Mode "$1" -Schema "$2" -Port 3306 -Archive "$mixed_archive" -LocalMixedAcceptance
  }
  backup_start="$(date +%s)"
  mixed_c5 Backup "$target_schema" > "$receipt_dir/mixed-backup-private.log" 2>&1 || fail R06B3A_MIXED_BACKUP_FAILED
  mixed_backup_seconds="$(( $(date +%s) - backup_start ))"
  mixed_archive_sha="$(sha256sum "$mixed_archive" | cut -d' ' -f1)"
  restore_start="$(date +%s)"
  mixed_c5 Restore "$mixed_schema" > "$receipt_dir/mixed-restore-private.log" 2>&1 || fail R06B3A_MIXED_RESTORE_FAILED
  mixed_restore_seconds="$(( $(date +%s) - restore_start ))"
  [[ "$(sha256sum "$mixed_archive" | cut -d' ' -f1)" = "$mixed_archive_sha" ]] || fail R06B3A_ARCHIVE_CHANGED
  rows_at "$mixed_id" "$mixed_password" "$mixed_schema" > "$work/mixed-restored.sql"
  cmp -s "$work/imported-after-postflight.sql" "$work/mixed-restored.sql" || fail R06B3A_MIXED_ROWS_MISMATCH
  [[ "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM SPRING_SESSION')" = 0 &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES')" = 0 ]] || fail R06B3A_SESSIONS
  # Actual separate SQL/JDBC comparator on the recovered server. Do not
  # re-CAPTURE an original baseline after import or pretend the old fingerprint
  # applies to this server: exact all-row equality transfers the verified rows.
  mysql_at "$mixed_id" "$mixed_password" -e "CREATE USER 'c9_mixed_ro'@'%' IDENTIFIED BY '$mixed_ro_password'; GRANT SELECT ON $mixed_schema.* TO 'c9_mixed_ro'@'%';"
  mixed_fp="$(printf '%s\0%s' "$mixed_schema" "$mixed_uuid" | sha256sum | cut -d' ' -f1)"
  {
    printf 'EXT_DB_URL=%q\nEXT_DB_USER=c9_mixed_ro\nEXT_DB_PASSWORD=%q\n' "jdbc:mysql://127.0.0.1:3306/$mixed_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" "$mixed_ro_password"
    printf 'EXT_C8_HOSTED_EXPECTED_SCHEMA=%q\nEXT_C8_HOSTED_EXPECTED_FINGERPRINT=%q\n' "$mixed_schema" "$mixed_fp"
    printf 'EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID=%q\nEXT_C8_HOSTED_OWNER_PUBLIC_ID=%q\n' "$original_org" "$owner_id"
    printf 'EXT_SMS_ENABLED=false\nEXT_REMINDER_MODE=DISABLED\nEXT_C8_DEMO_PLAN=%q\n' "$work/approved-plan.json"
  } > "$work/mixed-ro.env"
  run_java "$mixed_id" "$work/mixed-ro.env" --c8-hosted-demo-postflight > "$receipt_dir/mixed-postflight-private.log" 2>&1 || fail R06B3A_MIXED_POSTFLIGHT_FAILED
  grep -Fq 'HOSTED_DEMO_POSTFLIGHT_READ_ONLY_OK organizations=96 allocations=21060 events=67370;' "$receipt_dir/mixed-postflight-private.log" || fail R06B3A_MIXED_POSTFLIGHT_MARKER
  rows_at "$mixed_id" "$mixed_password" "$mixed_schema" > "$work/mixed-after-postflight.sql"
  rows_at "$target_id" "$dst_password" "$target_schema" > "$work/imported-after-recovery.sql"
  rows_at "$source_id" "$src_password" "$source_schema" > "$work/original-after-recovery.sql"
  cmp -s "$work/mixed-restored.sql" "$work/mixed-after-postflight.sql" &&
    cmp -s "$work/imported-after-postflight.sql" "$work/imported-after-recovery.sql" &&
    cmp -s "$work/source-before.sql" "$work/original-after-recovery.sql" || fail R06B3A_ROWS_CHANGED
  printf 'R06B3A_FULL_MIXED_RECOVERY_OK:archive_bytes=%s backup_seconds=%s restore_seconds=%s three_independent_servers=true exact_rows=true sessions_empty=true readonly_postflight=true sources_unchanged=true\n' "$(stat -c '%s' "$mixed_archive")" "$mixed_backup_seconds" "$mixed_restore_seconds" | tee -a "$receipt_dir/result.txt"
  printf 'post_import_c5=VERIFIED_LOCAL\nnormal_recovered_browser=NOT_RUN\nhosted_import=NOT_RUN\nindependent_reviewer=OPEN\n' >> "$receipt_dir/result.txt"
fi

phase=cleanup
cleanup || fail R06B1_CLEANUP_FAILED
trap - EXIT INT TERM
for id in "$source_id" "$target_id" "$mixed_id"; do if [ -n "$id" ] && docker_local inspect "$id" >/dev/null 2>&1; then fail R06B1_CLEANUP_FAILED; fi; done
[[ ! -e "$work" ]] || fail R06B1_CLEANUP_FAILED
echo R06B1_CLEANUP_OK
if [ -n "$receipt_dir" ]; then
  printf "exit=0\nphase=cleanup\n" > "$receipt_dir/status.txt"
  echo R06B2_PRIVATE_RECEIPT_RETAINED:owner_local_only
fi

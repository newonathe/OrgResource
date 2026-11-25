#!/usr/bin/env bash
# C9 PREPARATORY test on GitHub Actions' disposable 8-org C8 fixture ONLY.
# SQL dump/restore capacity and fidelity; AES-GCM archive itself not exercised.
set -euo pipefail
source_schema=orgresource_c8_additive_ci_fixture_intermediate
restore_schema=orgresource_c8_additive_ci_fixture_c9_restored
sql_limit=$((64 * 1024 * 1024))
session_limit=$((1024 * 1024))
refuse() { echo "$1" >&2; exit 1; }

[ "$(printenv GITHUB_ACTIONS || true)" = true ] || refuse C9_BACKUP_CI_ONLY
[ "$(printenv C9_DB_SCHEMA || true)" = "$source_schema" ] ||
  refuse C9_BACKUP_DISPOSABLE_SCHEMA_REQUIRED
[[ "$(printenv MYSQL_CONTAINER || true)" =~ ^[0-9a-f]{64}$ ]] ||
  refuse C9_BACKUP_CONTAINER_REQUIRED
[ -n "$(printenv EXT_DB_MIGRATION_PASSWORD || true)" ] ||
  refuse C9_BACKUP_TEST_ROOT_REQUIRED
[ -n "$(printenv C9_PLAN_PATH || true)" ] && [ -f "$C9_PLAN_PATH" ] ||
  refuse C9_BACKUP_TEST_PLAN_REQUIRED
[[ "$(printenv C9_PLAN_SHA || true)" =~ ^[0-9a-f]{64}$ ]] ||
  refuse C9_BACKUP_PLAN_DIGEST_REQUIRED
[ "$(sha256sum "$C9_PLAN_PATH" | cut -d' ' -f1)" = "$C9_PLAN_SHA" ] ||
  refuse C9_BACKUP_PLAN_DIGEST_MISMATCH
[[ "$(printenv C9_ORIGINAL_ORG_ID || true)" =~ ^[0-9a-f-]{36}$ ]] ||
  refuse C9_BACKUP_ORIGINAL_ID_REQUIRED
[[ "$(printenv C9_OWNER_ID || true)" =~ ^[0-9a-f-]{36}$ ]] ||
  refuse C9_BACKUP_OWNER_ID_REQUIRED
[ "$(printenv C9_READONLY_USER || true)" = c8_add_ro_intermediate ] ||
  refuse C9_BACKUP_READER_ID_REQUIRED
[ -n "$(printenv C9_READONLY_PASSWORD || true)" ] ||
  refuse C9_BACKUP_READER_PASSWORD_REQUIRED
[ -n "$(printenv RUNNER_TEMP || true)" ] && [ -d "$RUNNER_TEMP" ] ||
  refuse C9_BACKUP_RUNNER_DIRECTORY_REQUIRED

mysql_ci() {
  docker exec -i --env MYSQL_PWD="$EXT_DB_MIGRATION_PASSWORD" "$MYSQL_CONTAINER" \
    mysql --no-defaults -uroot "$@"
}
dump_ci() {
  docker exec -i --env MYSQL_PWD="$EXT_DB_MIGRATION_PASSWORD" "$MYSQL_CONTAINER" \
    mysqldump --no-defaults -uroot --skip-comments --skip-dump-date --hex-blob \
    --skip-lock-tables --skip-add-drop-table --no-tablespaces \
    --set-gtid-purged=OFF --skip-column-statistics "$@"
}
[ "$(mysql_ci --batch --skip-column-names "$source_schema" \
  -e 'SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL;')" = 8 ] ||
  refuse C9_BACKUP_EIGHT_ORG_FIXTURE_REQUIRED
[ "$(mysql_ci --batch --skip-column-names "$source_schema" \
  -e 'SELECT COUNT(*) FROM allocation;')" = 960 ] ||
  refuse C9_BACKUP_960_ALLOCATIONS_REQUIRED
[ "$(mysql_ci --batch --skip-column-names "$source_schema" \
  -e 'SELECT COUNT(*) FROM SPRING_SESSION;')" = 0 ] ||
  refuse C9_BACKUP_EXPECTED_NO_LIVE_SESSIONS
[ "$(mysql_ci --batch --skip-column-names -e \
  "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='$restore_schema';")" = 0 ] ||
  refuse C9_BACKUP_RESTORE_SCHEMA_ALREADY_EXISTS

workdir="$(mktemp -d "$RUNNER_TEMP/c9-backup-restored.XXXXXXXX")"
chmod 700 "$workdir"
trap 'rm -rf -- "$workdir"' EXIT
data="$workdir/data.sql"
sessions="$workdir/sessions.sql"
combined="$workdir/complete.sql"
original_rows="$workdir/original-rows.sql"
restored_rows="$workdir/restored-rows.sql"

# Match the SQL dump flags and two-part content of C5 backup.ps1. These are
# plain SQL bytes in a disposable temp directory: no public Actions artifact.
dump_ci --single-transaction --quick \
  "--ignore-table=$source_schema.SPRING_SESSION" \
  "--ignore-table=$source_schema.SPRING_SESSION_ATTRIBUTES" \
  "$source_schema" > "$data"
dump_ci --no-data "$source_schema" SPRING_SESSION SPRING_SESSION_ATTRIBUTES > "$sessions"
[ -s "$data" ] && [ -s "$sessions" ] || refuse C9_BACKUP_EMPTY_SQL
data_bytes="$(stat -c%s "$data")"
session_bytes="$(stat -c%s "$sessions")"
total_bytes=$((data_bytes + session_bytes))
[ "$session_bytes" -le "$session_limit" ] || refuse C9_BACKUP_SESSION_DDL_TOO_LARGE
[ "$total_bytes" -le "$sql_limit" ] || refuse C9_BACKUP_64MIB_LIMIT_EXCEEDED
cat "$data" "$sessions" > "$combined"
[ "$(stat -c%s "$combined")" = "$total_bytes" ] || refuse C9_BACKUP_LENGTH_MISMATCH

backup_digest="$(sha256sum "$combined" | cut -d' ' -f1)"
require_digest() {
  [ "$(sha256sum "$1" | cut -d' ' -f1)" = "$backup_digest" ] || {
    echo C9_BACKUP_DIGEST_MISMATCH >&2
    return 1
  }
}
cp "$combined" "$workdir/corrupted.sql"
printf '%s\n' '-- tampered: intentionally, disposable CI only' >> "$workdir/corrupted.sql"
if require_digest "$workdir/corrupted.sql" > "$workdir/corrupt-refusal.log" 2>&1; then
  refuse C9_BACKUP_CORRUPTED_SQL_ACCEPTED
fi
grep -q C9_BACKUP_DIGEST_MISMATCH "$workdir/corrupt-refusal.log" ||
  refuse C9_BACKUP_UNEXPECTED_CORRUPTION_REFUSAL
require_digest "$combined"

# Compare all 18 tables through deterministic ordered INSERT bytes, without
# disclosing original/synthetic row values, SQL, checksums, or user credentials.
row_dump_ci() {
  dump_ci --single-transaction --quick --no-create-info --order-by-primary \
    --skip-add-locks --skip-disable-keys "$1"
}
row_dump_ci "$source_schema" > "$original_rows"
[ -s "$original_rows" ] || refuse C9_BACKUP_SOURCE_PROOF_EMPTY

mysql_ci -e "CREATE DATABASE $restore_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
ensure_empty() {
  [ "$(mysql_ci --batch --skip-column-names -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$restore_schema';")" = 0 ] || {
    echo C9_BACKUP_NONEMPTY_TARGET_REFUSED >&2
    return 1
  }
}
ensure_empty
require_digest "$combined"
mysql_ci --binary-mode "$restore_schema" < "$combined"
if ensure_empty > "$workdir/nonempty-refusal.log" 2>&1; then
  refuse C9_BACKUP_REPEAT_RESTORE_ALLOWED
fi
grep -q C9_BACKUP_NONEMPTY_TARGET_REFUSED "$workdir/nonempty-refusal.log" ||
  refuse C9_BACKUP_UNEXPECTED_NONEMPTY_REFUSAL

row_dump_ci "$restore_schema" > "$restored_rows"
cmp -s "$original_rows" "$restored_rows" || refuse C9_BACKUP_RESTORED_ROWS_DIFFER
[ "$(mysql_ci --batch --skip-column-names "$restore_schema" \
  -e 'SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL;')" = 8 ] ||
  refuse C9_BACKUP_RESTORED_ORG_COUNT_MISMATCH
[ "$(mysql_ci --batch --skip-column-names "$restore_schema" \
  -e 'SELECT COUNT(*) FROM allocation;')" = 960 ] ||
  refuse C9_BACKUP_RESTORED_ALLOCATION_COUNT_MISMATCH

# Reconcile the restored DB against the original fictional plan using the
# application's existing SELECT-only C8 postflight, not counts alone.
mysql_ci -e "GRANT SELECT ON $restore_schema.* TO '$C9_READONLY_USER'@'%';"
EXT_C8_PROOF_SCHEMA="$restore_schema" \
  EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $restore_schema" \
  EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$C9_PLAN_PATH" \
  EXT_C8_DEMO_PLAN_SHA256="$C9_PLAN_SHA" \
  EXT_C8_PROOF_ORIGINAL_ORG_ID="$C9_ORIGINAL_ORG_ID" \
  EXT_C8_PROOF_OWNER_ACCOUNT_ID="$C9_OWNER_ID" \
  EXT_DB_USER="$C9_READONLY_USER" EXT_DB_PASSWORD="$C9_READONLY_PASSWORD" \
  java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
    --c8-additive-demo-isolated-postflight

row_dump_ci "$source_schema" > "$workdir/source-after.sql"
cmp -s "$original_rows" "$workdir/source-after.sql" ||
  refuse C9_BACKUP_SOURCE_CHANGED
header_bytes="$(printf %s ORGRESOURCE-C5-AES256GCM-1 | wc -c)"
container_estimate=$((total_bytes + header_bytes + 12 + 16))
echo "C9_ISOLATED_BACKUP_RESTORE_OK: fictional_orgs=8 allocations=960 \
data_sql_bytes=$data_bytes session_ddl_bytes=$session_bytes \
combined_sql_bytes=$total_bytes sql_capacity_bytes=$sql_limit \
estimated_c5_cipher_container_bytes=$container_estimate \
row_dump_byte_equal=true source_unchanged=true plan_postflight=passed \
corruption_digest_refused=true nonempty_target_refused=true \
encrypted_c5_archive=NOT_TESTED separate_mysql_instance=NOT_TESTED \
private_full_96=NOT_RUN hosted_database=NOT_TOUCHED"

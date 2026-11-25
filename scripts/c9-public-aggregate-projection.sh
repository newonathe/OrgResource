# Sourced only after the original-only control and initial third-server proof.
public_aggregate_projection() {
  [[ "${GITHUB_ACTIONS:-}" = true && "${RUNNER_OS:-}" = Linux && "$mode" = --fixture-only ]] || fail R06B3B_AGGREGATE_CI_ONLY
  assert_id "$mixed_id"
  [[ "$mixed_id" != "$source_id" && "$mixed_id" != "$target_id" &&
     "$mixed_uuid" != "$source_uuid" && "$mixed_uuid" != "$target_uuid" &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT @@server_uuid')" = "$mixed_uuid" &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()')" = 0 ]] || fail R06B3B_AGGREGATE_BOUNDARY
  # Copy actual V1-V7 table definitions only; never copy original rows or private plan.
  assert_id "$target_id"
  MYSQL_PWD="$dst_password" docker_local exec -i --env MYSQL_PWD "$target_id" mysqldump --no-defaults --protocol=socket -uroot \
    --no-data --skip-comments --skip-dump-date --skip-lock-tables --no-tablespaces --set-gtid-purged=OFF --skip-column-statistics "$target_schema" > "$work/aggregate-schema.sql"
  mysql_at "$mixed_id" "$mixed_password" "$mixed_schema" < "$work/aggregate-schema.sql"
  mysql_at "$mixed_id" "$mixed_password" "$mixed_schema" < scripts/c9-public-aggregate-fixture.sql
  [[ "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL')" = 0 &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM stock_movement')" = 9 &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM allocation')" = 6 ]] || fail R06B3B_AGGREGATE_FIXTURE
  local password
  password="$(openssl rand -hex 24)"
  echo "::add-mask::$password"
  mysql_at "$mixed_id" "$mixed_password" -e "CREATE USER 'c9_aggregate_ro'@'%' IDENTIFIED BY '$password'; GRANT SELECT ON $mixed_schema.* TO 'c9_aggregate_ro'@'%';"
  rows_at "$mixed_id" "$mixed_password" "$mixed_schema" > "$work/aggregate-before.sql"
  if MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$mixed_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 -uc9_aggregate_ro "$mixed_schema" -e 'UPDATE resource SET total_quantity=0 WHERE 1=0' > "$work/aggregate-denial.log" 2>&1; then fail R06B3B_AGGREGATE_UNEXPECTED_GRANT; fi
  grep -Eq 'ERROR 1142 .*command denied' "$work/aggregate-denial.log" || fail R06B3B_AGGREGATE_WRONG_REFUSAL
  MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$mixed_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 -uc9_aggregate_ro --batch --raw --skip-column-names --default-character-set=utf8mb4 "$mixed_schema" < scripts/c9-private-mixed-expectations.sql > "$work/aggregate-projection.jsonl" 2> "$work/aggregate-projection.log"
  python3 -B scripts/test-c9-public-sql-aggregates.py "$work/aggregate-projection.jsonl" "$mixed_schema" "$mixed_uuid" || fail R06B3B_AGGREGATE_ORACLE
  rows_at "$mixed_id" "$mixed_password" "$mixed_schema" > "$work/aggregate-after.sql"
  cmp -s "$work/aggregate-before.sql" "$work/aggregate-after.sql" || fail R06B3B_AGGREGATE_CHANGED_ROWS
  mysql_at "$mixed_id" "$mixed_password" -e "DROP USER 'c9_aggregate_ro'@'%';"
  control_domain > "$work/aggregate-restored-after.sql"
  rows_at "$source_id" "$src_password" "$source_schema" > "$work/aggregate-original-after.sql"
  cmp -s "$work/control-after.sql" "$work/aggregate-restored-after.sql" &&
    cmp -s "$work/source-before.sql" "$work/aggregate-original-after.sql" || fail R06B3B_AGGREGATE_OTHER_SERVER_CHANGED
  echo R06B3B_PUBLIC_SQL_AGGREGATE_ROWS_OK:select_only=true original_restored_unchanged=true fixture_unchanged=true
}

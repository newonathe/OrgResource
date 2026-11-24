# Sourced solely by the guarded CI fixture runner; never run a private import here.
control_supervisor=
control_pidfile="$work/control-app.pid"
control_stop() {
  if [ -n "$control_supervisor" ]; then
    local pid namespace
    assert_id "$target_id"
    pid="$(docker_local inspect --format '{{.State.Pid}}' "$target_id")" || return 1
    namespace="$(sudo -n readlink "/proc/$pid/ns/net")" || return 1
    if [ ! -s "$control_pidfile" ]; then
      # Never signal an unverified numeric PID. Supervisor has a bounded timeout.
      wait "$control_supervisor" 2>/dev/null || true
      [ ! -s "$control_pidfile" ] || return 1
    else
      python3 scripts/stop-c9-isolated-process.py "$control_pidfile" "$(id -u)" "$namespace" || return 1
      wait "$control_supervisor" 2>/dev/null || true
    fi
    control_supervisor=
    rm -f -- "$control_pidfile"
  fi
}
control_domain() {
  MYSQL_PWD="$dst_password" docker_local exec -i --env MYSQL_PWD "$target_id" mysqldump --no-defaults --protocol=socket -uroot \
    --skip-comments --skip-dump-date --hex-blob --skip-lock-tables --skip-add-drop-table \
    --no-tablespaces --set-gtid-purged=OFF --skip-column-statistics --single-transaction --quick \
    --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys \
    "--ignore-table=$target_schema.SPRING_SESSION" "--ignore-table=$target_schema.SPRING_SESSION_ATTRIBUTES" "$target_schema"
}
control_run() {
  [[ "${GITHUB_ACTIONS:-}" = true && "${RUNNER_OS:-}" = Linux && "$mode" = --fixture-only ]] || fail R06B3B_CONTROL_CI_ONLY
  assert_id "$target_id"
  local password foreign resource table query session node_bin ready=false
  node_bin="$(command -v node)" || fail R06B3B_CONTROL_NODE
  python3 scripts/stop-c9-isolated-process.py --self-test || fail R06B3B_CONTROL_PIDFD_SELF_TEST
  echo R06B3B_CONTROL_PIDFD_SELF_TEST_OK
  [[ "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM organization')" = 2 &&
     "$(scalar "$target_id" "$dst_password" "$target_schema" 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL')" = 0 ]] || fail R06B3B_CONTROL_FIXTURE
  foreign="$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT BIN_TO_UUID(public_id,0) FROM organization WHERE slug='isolated-other-original'")"
  resource="$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT BIN_TO_UUID(public_id,0) FROM resource WHERE name='Existing fictional projector'")"
  [[ "$foreign" =~ ^[a-f0-9-]{36}$ && "$resource" =~ ^[a-f0-9-]{36}$ ]] || fail R06B3B_CONTROL_IDS
  password="$(openssl rand -hex 24)"
  echo "::add-mask::$password"
  mysql_at "$target_id" "$dst_password" -e "CREATE USER 'c9_observer'@'%' IDENTIFIED BY '$password'; GRANT SELECT ON $target_schema.* TO 'c9_observer'@'%';"
  for table in SPRING_SESSION SPRING_SESSION_ATTRIBUTES; do
    mysql_at "$target_id" "$dst_password" -e "GRANT INSERT,UPDATE,DELETE ON $target_schema.$table TO 'c9_observer'@'%';"
  done
  # InventoryService's read path takes PESSIMISTIC_WRITE locks on org/member.
  # Prove the SELECT-only limitation on the exact disposable observer account,
  # then permit locking reads only within this disposable schema. Never grant
  # domain INSERT/UPDATE/DELETE. No shared/hosted permission change.
  local original_db owner_membership locking_sql expected_locks
  original_db="$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT id FROM organization WHERE slug='isolated-original-workspace'")"
  owner_membership="$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT m.id FROM membership m JOIN user_account u ON u.id=m.user_id WHERE m.org_id=$original_db AND u.username_key='additive-original-owner'")"
  [[ "$original_db" =~ ^[1-9][0-9]*$ && "$owner_membership" =~ ^[1-9][0-9]*$ ]] || fail R06B3B_CONTROL_LOCK_FIXTURE
  locking_sql="START TRANSACTION; SELECT id FROM organization WHERE id=$original_db FOR UPDATE; SELECT id FROM membership WHERE id=$owner_membership FOR UPDATE; ROLLBACK;"
  if MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$target_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 -uc9_observer -N -B "$target_schema" -e "$locking_sql" >"$work/control-lock-private.log" 2>&1; then
    fail R06B3B_CONTROL_LOCK_WITHOUT_PRIVILEGE
  fi
  grep -Eq 'ERROR 1142 .*command denied' "$work/control-lock-private.log" || fail R06B3B_CONTROL_LOCK_WRONG_REFUSAL
  echo R06B3B_CONTROL_LOCKING_READ_REFUSED_OK
  assert_id "$target_id"
  mysql_at "$target_id" "$dst_password" -e "GRANT LOCK TABLES ON $target_schema.* TO 'c9_observer'@'%';"
  expected_locks="$(printf '%s\n%s' "$original_db" "$owner_membership")"
  [[ "$(MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$target_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 -uc9_observer -N -B "$target_schema" -e "$locking_sql" 2>"$work/control-lock-private.log")" = "$expected_locks" ]] || fail R06B3B_CONTROL_LOCKING_READ
  echo R06B3B_CONTROL_LOCKING_READ_OK:schema_lock_only=true no_domain_dml_granted=true
  for query in 'UPDATE organization SET id=id WHERE 1=0' 'UPDATE allocation SET id=id WHERE 1=0' 'DELETE FROM idempotency_key WHERE 1=0' 'INSERT INTO audit_event SELECT * FROM audit_event WHERE 1=0' 'UPDATE stock_movement SET id=id WHERE 1=0' 'UPDATE membership SET id=id WHERE 1=0' 'DELETE FROM membership WHERE 1=0'; do
    if MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$target_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 -uc9_observer -N -B "$target_schema" -e "$query" >"$work/control-grants.log" 2>&1; then fail R06B3B_CONTROL_UNEXPECTED_GRANT; fi
    grep -Eq 'ERROR 1142 .*command denied' "$work/control-grants.log" || fail R06B3B_CONTROL_WRONG_REFUSAL
  done
  echo R06B3B_CONTROL_GRANTS_OK:session_dml_only=true seven_domain_denials=true schema_lock_only=true
  control_domain > "$work/control-before.sql"
  {
    printf 'export EXT_DB_URL=%q\nEXT_DB_USER=c9_observer\nexport EXT_DB_USER\nexport EXT_DB_PASSWORD=%q\n' "jdbc:mysql://127.0.0.1:3306/$target_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" "$password"
    printf 'export EXT_CONTACT_ENCRYPTION_KEY=%q\n' "$contact_key"
    printf 'export SPRING_PROFILES_ACTIVE=c1-test\nexport EXT_SMS_ENABLED=false\nexport EXT_REMINDER_MODE=DISABLED\nexport EXT_SERVER_PORT=8085\nexport EXT_SCHEDULER_DELAY_MS=600000\n'
    printf 'export C9_RUNTIME_ISOLATED=true\nexport C9_CONTROL_PUBLIC_ONLY=true\nexport C9_RUNTIME_OWNER_PASSWORD=%q\n' "$owner_password"
    printf 'export C9_RUNTIME_STATE=%q\nexport C9_CONTROL_ORIGINAL=%q\nexport C9_CONTROL_FOREIGN=%q\nexport C9_CONTROL_RESOURCE=%q\n' "$work/control-session.json" "$original_org" "$foreign" "$resource"
  } > "$work/control.env"
  # Observation-only launcher: Flyway is disabled explicitly because prior actual
  # bootstrap/restore verified V1-V7. Hibernate validation remains enabled.
  ns_run "$target_id" timeout --signal=TERM --kill-after=10s 240s bash -ec '
    source "$1"
    stat_line="$(cat "/proc/$$/stat")"
    read -r -a fields <<< "${stat_line##*) }"
    printf "%s %s\n" "$$" "${fields[19]}" > "$2"
    shift 2
    exec "$@"
  ' _ "$work/control.env" "$control_pidfile" "$java_bin" -Xmx512m -Duser.timezone=Asia/Manila -jar "$jar" \
    --spring.flyway.enabled=false > "$work/control-app.log" 2>&1 &
  control_supervisor=$!
  for _ in $(seq 1 100); do
    kill -0 "$control_supervisor" 2>/dev/null || fail R06B3B_CONTROL_JAR_EXITED
    if ns_run "$target_id" curl --noproxy '*' --silent --fail --max-time 2 http://127.0.0.1:8085/ > "$work/control-index.html"; then ready=true; break; fi
    sleep 1
  done
  [ "$ready" = true ] && grep -Eq 'assets/index-[^"]+\.js' "$work/control-index.html" || fail R06B3B_CONTROL_JAR_READY
  control_private() {
    ns_run "$target_id" bash -ec 'source "$1"; shift; exec "$@"' _ "$work/control.env" "$@"
  }
  echo R06B3B_CONTROL_JAR_READY_OK
  control_private python3 scripts/verify-c9-public-recovered-control.py login || fail R06B3B_CONTROL_API_LOGIN
  session="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["cookie"])' "$work/control-session.json")"
  [[ "$session" =~ ^[a-f0-9-]{36}$ ]] || fail R06B3B_CONTROL_SESSION_ID
  echo "::add-mask::$session"
  [[ "$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID='$session' AND EXPIRY_TIME>UNIX_TIMESTAMP()*1000")" = 1 &&
     "$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES a JOIN SPRING_SESSION s ON s.PRIMARY_ID=a.SESSION_PRIMARY_ID WHERE s.SESSION_ID='$session'")" -gt 0 ]] || fail R06B3B_CONTROL_SESSION_NOT_PERSISTED
  echo R06B3B_CONTROL_PERSISTED_SESSION_OK
  control_private python3 scripts/verify-c9-public-recovered-control.py logout || fail R06B3B_CONTROL_API_LOGOUT
  [[ "$(scalar "$target_id" "$dst_password" "$target_schema" "SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID='$session'")" = 0 ]] || fail R06B3B_CONTROL_SESSION_NOT_RETIRED
  # Chromium is preinstalled by CI. Use runner's actual cache, not a download.
  control_private env "PLAYWRIGHT_BROWSERS_PATH=${PLAYWRIGHT_BROWSERS_PATH:-$HOME/.cache/ms-playwright}" "$node_bin" scripts/verify-c9-public-recovered-control.mjs || fail R06B3B_CONTROL_BROWSER
  control_stop || fail R06B3B_CONTROL_PIDFD_STOP
  control_domain > "$work/control-after.sql"
  cmp -s "$work/control-before.sql" "$work/control-after.sql" || fail R06B3B_CONTROL_DOMAIN_CHANGED
  rows_at "$source_id" "$src_password" "$source_schema" > "$work/control-original-after.sql"
  cmp -s "$work/source-before.sql" "$work/control-original-after.sql" || fail R06B3B_CONTROL_SOURCE_CHANGED
  # Record bounded scheduler refusal count, not raw logs. No domain DML allowed.
  local denied
  denied="$(grep -Eic 'command denied|receipt cleanup failed|Expiry processing failed|Stale reminder claim recovery failed' "$work/control-app.log" || true)"
  echo "R06B3B_CONTROL_ROWS_OK:non_session_rows_equal=true original_rows_equal=true scheduler_denial_log_matches=$denied flyway_runtime_disabled=true"
  mysql_at "$target_id" "$dst_password" -e "DROP USER 'c9_observer'@'%';"
}

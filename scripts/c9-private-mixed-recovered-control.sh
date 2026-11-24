# Sourced only by the guarded --approved-import parent. Do not invoke standalone.
mixed_runtime_guard() {
  [[ "$mode" = --approved-import && "${GITHUB_ACTIONS:-}" != true && "$(id -u)" != 0 && -t 0 && -t 1 && -t 2 ]] ||
    fail R06B3B_PRIVATE_OWNER_LOCAL_ONLY
}
mixed_runtime_preflight() {
  mixed_runtime_guard
  local tool
  for tool in node curl; do command -v "$tool" >/dev/null || fail R06B3B_PRIVATE_TOOL_MISSING; done
  mixed_node="$(command -v node)"
  [[ "$mixed_node" = /* && -x "$mixed_node" ]] || fail R06B3B_PRIVATE_NODE
  mixed_browser_cache="${PLAYWRIGHT_BROWSERS_PATH:-$HOME/.cache/ms-playwright}"
  [[ "$mixed_browser_cache" = /* && -d "$mixed_browser_cache" && "$(realpath -e -- "$mixed_browser_cache")" = "$mixed_browser_cache" ]] ||
    fail R06B3B_PRIVATE_BROWSER_CACHE
  python3 -B scripts/stop-c9-private-mixed-process.py --self-test || fail R06B3B_PRIVATE_PIDFD_PREFLIGHT
  # Prove preinstalled Chromium starts inside the actual isolated namespace,
  # before consent or import, without an HTTP request or a browser download.
  ns_run "$target_id" timeout --signal=TERM --kill-after=5s 45s env "PLAYWRIGHT_BROWSERS_PATH=$mixed_browser_cache" "$mixed_node" -e '
    const {createRequire}=require("node:module");
    const {chromium}=createRequire(process.cwd()+"/extended/frontend/package.json")("@playwright/test");
    (async()=>{let browser;try{browser=await chromium.launch({headless:true});const p=await browser.newPage();await p.goto("about:blank");}
    finally{if(browser)await browser.close();}})().catch(()=>{process.exitCode=1;});
  ' > "$work/mixed-browser-preflight.log" 2>&1 || fail R06B3B_PRIVATE_BROWSER_PREFLIGHT
  echo R06B3B_PRIVATE_RUNTIME_PREFLIGHT_OK:pidfd_child=true isolated_chromium=true
}
mixed_runtime_stop() {
  if [ -n "$mixed_supervisor" ]; then
    local pid namespace
    assert_id "$mixed_id"
    [[ "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT @@server_uuid')" = "$mixed_uuid" ]] || return 1
    pid="$(docker_local inspect --format '{{.State.Pid}}' "$mixed_id")" || return 1
    namespace="$(sudo -n readlink "/proc/$pid/ns/net")" || return 1
    [[ "$namespace" = "$mixed_runtime_namespace" ]] || return 1
    if [ ! -s "$work/mixed-runtime.pid" ]; then
      wait "$mixed_supervisor" 2>/dev/null || true
      [[ ! -e "$work/mixed-runtime.pid" ]] || return 1
    else
      python3 -B scripts/stop-c9-private-mixed-process.py "$work" "$namespace" "$java_bin" || return 1
      wait "$mixed_supervisor" 2>/dev/null || true
    fi
    mixed_supervisor=
    rm -f -- "$work/mixed-runtime.pid"
  fi
}
mixed_domain() {
  assert_id "$mixed_id"
  MYSQL_PWD="$mixed_password" docker_local exec -i --env MYSQL_PWD "$mixed_id" mysqldump --no-defaults --protocol=socket -uroot \
    --skip-comments --skip-dump-date --hex-blob --skip-lock-tables --skip-add-drop-table \
    --no-tablespaces --set-gtid-purged=OFF --skip-column-statistics --single-transaction --quick \
    --no-create-info --order-by-primary --skip-add-locks --skip-disable-keys \
    "--ignore-table=$mixed_schema.SPRING_SESSION" "--ignore-table=$mixed_schema.SPRING_SESSION_ATTRIBUTES" "$mixed_schema"
}
mixed_runtime_run() {
  mixed_runtime_guard
  [[ "${mixed_owner_confirmed:-}" = true && -n "$receipt_dir" && -s "$receipt_dir/operator-receipt.txt" ]] || fail R06B3B_PRIVATE_CONFIRMATION_REQUIRED
  grep -Fxq operator_confirmation_received=true "$receipt_dir/operator-receipt.txt" || fail R06B3B_PRIVATE_CONFIRMATION_REQUIRED
  grep -Fq 'HOSTED_DEMO_POSTFLIGHT_READ_ONLY_OK organizations=96 allocations=21060 events=67370;' "$receipt_dir/mixed-postflight-private.log" ||
    fail R06B3B_PRIVATE_POSTFLIGHT_REQUIRED
  assert_id "$mixed_id"
  [[ "$mixed_id" != "$source_id" && "$mixed_id" != "$target_id" &&
     "$mixed_uuid" != "$source_uuid" && "$mixed_uuid" != "$target_uuid" &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT @@server_uuid')" = "$mixed_uuid" ]] || fail R06B3B_PRIVATE_TARGET
  [[ "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM SPRING_SESSION')" = 0 &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" 'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES')" = 0 ]] || fail R06B3B_PRIVATE_SESSION_BASELINE
  local password table query original_db member_db lock_sql expected_locks ready=false session denied pid
  # SELECT-only projection. No root queries for expected domain values.
  MYSQL_PWD="$mixed_ro_password" docker_local exec -i --env MYSQL_PWD "$mixed_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 \
    -uc9_mixed_ro --batch --raw --skip-column-names --default-character-set=utf8mb4 "$mixed_schema" \
    < scripts/c9-private-mixed-expectations.sql > "$work/mixed-projection.jsonl" 2> "$receipt_dir/mixed-projection-private.log" ||
    fail R06B3B_PRIVATE_PROJECTION
  env -i "PATH=$PATH" "HOME=$work/home" LANG=C.UTF-8 C9_CONTROL_PRIVATE_MIXED=true \
    "C9_MIXED_PROJECTION=$work/mixed-projection.jsonl" "C9_MIXED_EXPECTED=$work/mixed-expected.json" \
    "C9_MIXED_SCHEMA=$mixed_schema" "C9_MIXED_SERVER_UUID=$mixed_uuid" "C9_MIXED_OWNER=$owner_id" \
    "C9_MIXED_ORIGINAL=$original_org" "C9_MIXED_FINGERPRINT=$mixed_fp" \
    python3 -B scripts/build-c9-private-mixed-expectations.py || fail R06B3B_PRIVATE_EXPECTATIONS
  password="$(openssl rand -hex 24)"
  mysql_at "$mixed_id" "$mixed_password" -e "CREATE USER 'c9_mixed_observer'@'%' IDENTIFIED BY '$password'; GRANT SELECT ON $mixed_schema.* TO 'c9_mixed_observer'@'%';"
  for table in SPRING_SESSION SPRING_SESSION_ATTRIBUTES; do
    mysql_at "$mixed_id" "$mixed_password" -e "GRANT INSERT,UPDATE,DELETE ON $mixed_schema.$table TO 'c9_mixed_observer'@'%';"
  done
  mixed_observer_sql() {
    assert_id "$mixed_id"
    MYSQL_PWD="$password" docker_local exec -i --env MYSQL_PWD "$mixed_id" mysql --no-defaults --protocol=tcp --host=127.0.0.1 -uc9_mixed_observer -N -B "$mixed_schema" -e "$1"
  }
  original_db="$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" "SELECT id FROM organization WHERE slug='isolated-original-workspace'")"
  [[ "$original_db" =~ ^[1-9][0-9]*$ ]] || fail R06B3B_PRIVATE_LOCK_ID
  member_db="$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" "SELECT m.id FROM membership m JOIN user_account u ON u.id=m.user_id WHERE m.org_id=$original_db AND u.username_key='additive-original-owner'")"
  [[ "$member_db" =~ ^[1-9][0-9]*$ ]] || fail R06B3B_PRIVATE_LOCK_ID
  lock_sql="START TRANSACTION; SELECT id FROM organization WHERE id=$original_db FOR UPDATE; SELECT id FROM membership WHERE id=$member_db FOR UPDATE; ROLLBACK;"
  if mixed_observer_sql "$lock_sql" > "$work/mixed-grants.log" 2>&1; then fail R06B3B_PRIVATE_LOCK_WITHOUT_GRANT; fi
  grep -Eq 'ERROR 1142 .*command denied' "$work/mixed-grants.log" || fail R06B3B_PRIVATE_WRONG_REFUSAL
  mysql_at "$mixed_id" "$mixed_password" -e "GRANT LOCK TABLES ON $mixed_schema.* TO 'c9_mixed_observer'@'%';"
  expected_locks="$(printf '%s\n%s' "$original_db" "$member_db")"
  [[ "$(mixed_observer_sql "$lock_sql" 2>"$work/mixed-grants.log")" = "$expected_locks" ]] || fail R06B3B_PRIVATE_LOCK_READ
  for query in 'UPDATE organization SET id=id WHERE 1=0' 'UPDATE allocation SET id=id WHERE 1=0' 'DELETE FROM idempotency_key WHERE 1=0' 'INSERT INTO audit_event SELECT * FROM audit_event WHERE 1=0' 'UPDATE stock_movement SET id=id WHERE 1=0' 'UPDATE membership SET id=id WHERE 1=0' 'DELETE FROM membership WHERE 1=0'; do
    if mixed_observer_sql "$query" > "$work/mixed-grants.log" 2>&1; then fail R06B3B_PRIVATE_UNEXPECTED_GRANT; fi
    grep -Eq 'ERROR 1142 .*command denied' "$work/mixed-grants.log" || fail R06B3B_PRIVATE_WRONG_REFUSAL
  done
  echo R06B3B_PRIVATE_GRANTS_OK:lock_refusal_positive=true seven_domain_denials=true session_dml_only=true
  # Reconfirm entire recovered data before runtime (including empty sessions),
  # then capture non-session baseline including Flyway history.
  rows_at "$mixed_id" "$mixed_password" "$mixed_schema" > "$work/mixed-before-runtime-all.sql"
  cmp -s "$work/mixed-after-postflight.sql" "$work/mixed-before-runtime-all.sql" || fail R06B3B_PRIVATE_PRE_RUNTIME_ROWS
  mixed_domain > "$work/mixed-before-runtime-domain.sql"
  {
    printf 'export EXT_DB_URL=%q\nexport EXT_DB_USER=c9_mixed_observer\nexport EXT_DB_PASSWORD=%q\n' "jdbc:mysql://127.0.0.1:3306/$mixed_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" "$password"
    printf 'export EXT_CONTACT_ENCRYPTION_KEY=%q\n' "$contact_key"
    printf 'export SPRING_PROFILES_ACTIVE=c1-test\nexport EXT_SMS_ENABLED=false\nexport EXT_REMINDER_MODE=DISABLED\nexport EXT_SERVER_PORT=8085\nexport EXT_SCHEDULER_DELAY_MS=600000\n'
    printf 'export C9_RUNTIME_ISOLATED=true\nexport C9_CONTROL_PRIVATE_MIXED=true\nexport C9_RUNTIME_OWNER_PASSWORD=%q\n' "$owner_password"
    printf 'export C9_RUNTIME_STATE=%q\nexport C9_MIXED_EXPECTED=%q\nexport PLAYWRIGHT_BROWSERS_PATH=%q\n' "$work/mixed-session.json" "$work/mixed-expected.json" "$mixed_browser_cache"
  } > "$work/mixed-runtime.env"
  pid="$(docker_local inspect --format '{{.State.Pid}}' "$mixed_id")"
  mixed_runtime_namespace="$(sudo -n readlink "/proc/$pid/ns/net")"
  [[ "$mixed_runtime_namespace" =~ ^net:\[[0-9]+\]$ && "$mixed_runtime_namespace" != "$(readlink /proc/self/ns/net)" ]] || fail R06B3B_PRIVATE_NAMESPACE
  ns_run "$mixed_id" timeout --signal=TERM --kill-after=10s 480s bash -ec '
    umask 077
    source "$1"
    stat_line="$(cat "/proc/$$/stat")"
    read -r -a fields <<< "${stat_line##*) }"
    (set -o noclobber; printf "%s %s\n" "$$" "${fields[19]}" > "$2")
    shift 2
    exec "$@"
  ' _ "$work/mixed-runtime.env" "$work/mixed-runtime.pid" "$java_bin" -Xmx768m -Duser.timezone=Asia/Manila -jar "$jar" \
    --spring.flyway.enabled=false > "$receipt_dir/mixed-runtime-private.log" 2>&1 &
  mixed_supervisor=$!
  for _ in $(seq 1 100); do
    kill -0 "$mixed_supervisor" 2>/dev/null || fail R06B3B_PRIVATE_JAR_EXITED
    if ns_run "$mixed_id" curl --noproxy '*' --silent --fail --max-time 2 http://127.0.0.1:8085/ > "$work/mixed-index.html"; then ready=true; break; fi
    sleep 1
  done
  [[ "$ready" = true ]] && grep -Eq 'assets/index-[^"]+\.js' "$work/mixed-index.html" || fail R06B3B_PRIVATE_JAR_READY
  mixed_private() {
    ns_run "$mixed_id" timeout --signal=TERM --kill-after=5s 150s bash -ec 'umask 077; source "$1"; shift; exec "$@"' _ "$work/mixed-runtime.env" "$@"
  }
  mixed_private python3 -B scripts/verify-c9-private-mixed-api.py login > "$receipt_dir/mixed-api-private.log" 2>&1 || fail R06B3B_PRIVATE_API
  session="$(python3 -B -c 'import json,sys; print(json.load(open(sys.argv[1]))["cookie"])' "$work/mixed-session.json")"
  [[ "$session" =~ ^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$ ]] || fail R06B3B_PRIVATE_SESSION_ID
  [[ "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" "SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID='$session' AND EXPIRY_TIME>UNIX_TIMESTAMP()*1000")" = 1 &&
     "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" "SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES a JOIN SPRING_SESSION s ON s.PRIMARY_ID=a.SESSION_PRIMARY_ID WHERE s.SESSION_ID='$session'")" -gt 0 ]] || fail R06B3B_PRIVATE_SESSION_NOT_PERSISTED
  echo R06B3B_PRIVATE_PERSISTED_SESSION_OK
  mixed_private python3 -B scripts/verify-c9-private-mixed-api.py logout >> "$receipt_dir/mixed-api-private.log" 2>&1 || fail R06B3B_PRIVATE_LOGOUT
  [[ "$(scalar "$mixed_id" "$mixed_password" "$mixed_schema" "SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID='$session'")" = 0 ]] || fail R06B3B_PRIVATE_SESSION_NOT_RETIRED
  mixed_private "$mixed_node" scripts/verify-c9-private-mixed-browser.mjs > "$receipt_dir/mixed-browser-private.log" 2>&1 || fail R06B3B_PRIVATE_BROWSER
  grep -Fq R06B3B_MIXED_API_LOGIN_OK "$receipt_dir/mixed-api-private.log" &&
    grep -Fq R06B3B_MIXED_API_LOGOUT_OK "$receipt_dir/mixed-api-private.log" &&
    grep -Fq R06B3B_MIXED_BROWSER_OK "$receipt_dir/mixed-browser-private.log" || fail R06B3B_PRIVATE_MARKERS
  mixed_runtime_stop || fail R06B3B_PRIVATE_PIDFD_STOP
  mixed_domain > "$work/mixed-after-runtime-domain.sql"
  cmp -s "$work/mixed-before-runtime-domain.sql" "$work/mixed-after-runtime-domain.sql" || fail R06B3B_PRIVATE_DOMAIN_CHANGED
  denied="$(grep -Eic 'command denied|receipt cleanup failed|Expiry processing failed|Stale reminder claim recovery failed' "$receipt_dir/mixed-runtime-private.log" || true)"
  mysql_at "$mixed_id" "$mixed_password" -e "DROP USER 'c9_mixed_observer'@'%';"
  run_java "$mixed_id" "$work/mixed-ro.env" --c8-hosted-demo-postflight > "$receipt_dir/mixed-after-runtime-postflight-private.log" 2>&1 || fail R06B3B_PRIVATE_POSTFLIGHT
  grep -Fq 'HOSTED_DEMO_POSTFLIGHT_READ_ONLY_OK organizations=96 allocations=21060 events=67370;' "$receipt_dir/mixed-after-runtime-postflight-private.log" || fail R06B3B_PRIVATE_POSTFLIGHT_MARKER
  mixed_domain > "$work/mixed-after-runtime-postflight-domain.sql"
  rows_at "$target_id" "$dst_password" "$target_schema" > "$work/target-after-runtime.sql"
  rows_at "$source_id" "$src_password" "$source_schema" > "$work/source-after-runtime.sql"
  cmp -s "$work/mixed-before-runtime-domain.sql" "$work/mixed-after-runtime-postflight-domain.sql" &&
    cmp -s "$work/imported-after-postflight.sql" "$work/target-after-runtime.sql" &&
    cmp -s "$work/source-before.sql" "$work/source-after-runtime.sql" || fail R06B3B_PRIVATE_SOURCE_OR_DOMAIN_CHANGED
  printf 'R06B3B_PRIVATE_MIXED_RUNTIME_OK:metadata_owner_scope=true api_browser=true persisted_logout=true non_session_rows_equal=true sources_unchanged=true readonly_postflight=true scheduler_denial_log_matches=%s flyway_runtime_disabled=true\n' "$denied" | tee -a "$receipt_dir/result.txt"
}

#!/usr/bin/env bash
# Isolated Java/MySQL proof ONLY. Hard stop for any non-disposable remote target.
set -euo pipefail
mode="${1:-small}"
inject_failure=0
case "$mode" in
  small) profile=small; expected_workspaces=3; expected_allocations=36 ;;
  intermediate) profile=intermediate; expected_workspaces=8; expected_allocations=960 ;;
  small-partial) profile=small; expected_workspaces=3; expected_allocations=36; inject_failure=1 ;;
  *) echo "Only disposable small, intermediate, or small-partial profiles are accepted" >&2; exit 1 ;;
esac
suffix="${mode//-/_}"
schema="orgresource_c8_additive_ci_fixture_$suffix"
runtime_user="c8_add_run_$suffix"
migrator_user="c8_add_mig_$suffix"
runtime_password="$(openssl rand -hex 24)"
migration_password="$(openssl rand -hex 24)"
owner_password="$(openssl rand -hex 24)"
for secret in "$runtime_password" "$migration_password" "$owner_password"; do
  echo "::add-mask::$secret"
done
: "${MYSQL_CONTAINER:?A disposable GitHub Actions MySQL container is required}"
: "${EXT_DB_MIGRATION_PASSWORD:?The disposable CI root credential is required}"
mysql_ci() {
  docker exec -i --env MYSQL_PWD="$EXT_DB_MIGRATION_PASSWORD" "$MYSQL_CONTAINER" mysql -uroot "$@"
}
mysql_ci <<SQL
CREATE DATABASE $schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER '$runtime_user'@'%' IDENTIFIED BY '$runtime_password';
CREATE USER '$migrator_user'@'%' IDENTIFIED BY '$migration_password';
GRANT SELECT,INSERT,UPDATE,DELETE ON $schema.* TO '$runtime_user'@'%';
GRANT ALL PRIVILEGES ON $schema.* TO '$migrator_user'@'%';
SQL
url="jdbc:mysql://127.0.0.1:3306/$schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
EXT_DB_URL="$url" EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
  EXT_DB_MIGRATION_URL="$url" EXT_DB_MIGRATION_USER="$migrator_user" \
  EXT_DB_MIGRATION_PASSWORD="$migration_password" \
  EXT_BOOTSTRAP_SLUG=isolated-original-workspace \
  EXT_BOOTSTRAP_ORG_NAME='Isolated Original Workspace' \
  EXT_BOOTSTRAP_USERNAME=additive-original-owner \
  EXT_BOOTSTRAP_DISPLAY_NAME='Fictional Original Owner' \
  EXT_BOOTSTRAP_PASSWORD="$owner_password" \
  java -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar --bootstrap \
  > "$RUNNER_TEMP/c8-additive-bootstrap-private.log" 2>&1
original_org="$(mysql_ci --batch --skip-column-names "$schema" \
  -e "SELECT BIN_TO_UUID(public_id,0) FROM organization WHERE slug='isolated-original-workspace';")"
owner_id="$(mysql_ci --batch --skip-column-names "$schema" \
  -e "SELECT BIN_TO_UUID(public_id,0) FROM user_account WHERE username_key='additive-original-owner';")"
[ -n "$original_org" ] && [ -n "$owner_id" ]
# Seed existing fictional domain rows so preservation is meaningful beyond account identity.
mysql_ci "$schema" <<'SQL'
INSERT INTO department (organization_id,name,name_key,version)
SELECT id,'Existing fictional operations','existing fictional operations',0
FROM organization WHERE slug='isolated-original-workspace';
INSERT INTO resource (public_id,organization_id,name,name_key,type,total_quantity,version)
SELECT UUID_TO_BIN(UUID(),0),id,'Existing fictional projector','existing fictional projector','REUSABLE',0,0
FROM organization WHERE slug='isolated-original-workspace';
SQL
original_domain_snapshot() {
  mysql_ci --batch --skip-column-names "$schema" -e "
  SELECT HEX(o.public_id),d.name,d.name_key,d.version,HEX(r.public_id),r.name,r.name_key,r.type,r.total_quantity,r.version
  FROM organization o JOIN department d ON d.organization_id=o.id
  JOIN resource r ON r.organization_id=o.id WHERE o.slug='isolated-original-workspace';"     | sha256sum | cut -d' ' -f1
}
original_domain_sha_before="$(original_domain_snapshot)"
# Add a second original organization/account BEFORE the HMAC baseline, ensuring
# the safeguard also covers originals outside the nominated owner/org.
mysql_ci "$schema" <<'SQL'
INSERT INTO organization(public_id,slug,display_name,status,version,created_at,updated_at)
VALUES(UUID_TO_BIN(UUID(),0),'isolated-other-original','Other Original Org','ACTIVE',0,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
INSERT INTO user_account(public_id,username_key,display_name,status,credential_version,version,created_at,updated_at)
VALUES(UUID_TO_BIN(UUID(),0),'other-original-user','Other Original User','ACTIVE',0,0,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
INSERT INTO membership(public_id,org_id,user_id,role,status,version,created_at,updated_at)
SELECT UUID_TO_BIN(UUID(),0),o.id,u.id,'MEMBER','ACTIVE',0,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)
FROM organization o CROSS JOIN user_account u WHERE o.slug='isolated-other-original'
AND u.username_key='other-original-user';
INSERT INTO department(organization_id,name,name_key,version)
SELECT id,'Other original department','other original department',0
FROM organization WHERE slug='isolated-other-original';
SQL
# Independent 15-table original-data HMAC manifest. Remains in RUNNER_TEMP, never
# uploaded or written to the application schema. Verification cannot rewrite it.
baseline_file="$RUNNER_TEMP/c8-private-original-$suffix.properties"
baseline_key="$(openssl rand -hex 32)"
echo "::add-mask::$baseline_key"
original_baseline() {
  local mode="$1"
  local fp
  fp="$(printf '%s\0%s' "$schema" "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT @@server_uuid;')" | sha256sum | cut -d' ' -f1)"
  EXT_C8_BASELINE_MODE="$mode" EXT_C8_BASELINE_FILE="$baseline_file" \
    EXT_C8_BASELINE_HMAC_HEX="$baseline_key" \
    EXT_C8_HOSTED_EXPECTED_SCHEMA="$schema" EXT_C8_HOSTED_EXPECTED_FINGERPRINT="$fp" \
    EXT_C8_HOSTED_ORIGINAL_ORG_PUBLIC_ID="$original_org" EXT_C8_HOSTED_OWNER_PUBLIC_ID="$owner_id" \
    EXT_DB_URL="jdbc:mysql://127.0.0.1:3306/$schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
    EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
    java -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
    --c8-hosted-original-baseline
}
original_baseline CAPTURE
# Hash the *full original owner/org/credential/membership row values*, not just counts.
# Never emit the credential material or the hash to public CI logs.
original_snapshot_sql="SELECT HEX(o.public_id),o.slug,o.display_name,o.status,o.version,o.created_at,o.updated_at,\
HEX(u.public_id),u.username_key,u.display_name,u.status,u.credential_version,u.version,u.created_at,u.updated_at,\
HEX(m.public_id),m.role,m.status,m.version,m.created_at,m.updated_at,c.password_hash,c.changed_at \
FROM organization o JOIN membership m ON m.org_id=o.id \
JOIN user_account u ON u.id=m.user_id JOIN local_credential c ON c.user_id=u.id \
WHERE o.slug='isolated-original-workspace' AND u.username_key='additive-original-owner';"
snapshot_original() {
  mysql_ci --batch --skip-column-names "$schema" -e "$original_snapshot_sql" | sha256sum | cut -d' ' -f1
}
original_sha_before="$(snapshot_original)"
plan="$RUNNER_TEMP/c8-additive-$suffix-private.json"
python3 scripts/generate-c8-demo.py --profile "$profile" --seed c8-fixture-proof-v1 --output "$plan"
digest="$(sha256sum "$plan" | cut -d' ' -f1)"
check_postflight() {
  EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
    EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
    EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
    EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
    java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
    --c8-additive-demo-isolated-postflight
}
if [ "$inject_failure" -eq 1 ]; then
  # Prove a pre-import backup can be restored independently into a NEW database.
  # Only the disposable CI/MySQL container is used, never a live Northflank DB.
  backup_file="$RUNNER_TEMP/c8-original-disposable-before-partial.sql"
  docker exec -i --env MYSQL_PWD="$EXT_DB_MIGRATION_PASSWORD" "$MYSQL_CONTAINER" \
    mysqldump -uroot --single-transaction --skip-lock-tables --hex-blob \
    --set-gtid-purged=OFF "$schema" > "$backup_file"
  [ -s "$backup_file" ]
  backup_digest="$(sha256sum "$backup_file" | cut -d' ' -f1)"
  # This branch is an intentionally aborted 3-org CI fixture in its own disposable DB.
  failed_log="$RUNNER_TEMP/c8-additive-partial-injected-private.log"
  if EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
    EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
    EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
    EXT_C8_PROOF_FAIL_AFTER=1 EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
    java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
    --c8-additive-demo-isolated-proof > "$failed_log" 2>&1; then
    echo "An intentionally interrupted fixture unexpectedly completed." >&2
    exit 1
  fi
  grep -q 'INJECTED_ISOLATED_PARTIAL_FAILURE_AFTER_1_COMMITTED_WORKSPACES' "$failed_log"
  [ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL;')" = "1" ]
  [ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM allocation;')" = "12" ]
  if check_postflight > "$RUNNER_TEMP/c8-additive-partial-postflight-refusal.log" 2>&1; then
    echo "Independent postflight unexpectedly accepted the incomplete demo." >&2
    exit 1
  fi
  [ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM user_account WHERE username_key="c8-demo-historical-actor";')" = "1" ]
  [ "$(snapshot_original)" = "$original_sha_before" ]
  [ "$(original_domain_snapshot)" = "$original_domain_sha_before" ]
  original_baseline VERIFY
  # The next attempt must refuse the partial database and leave every row intact.
  before="$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT (SELECT COUNT(*) FROM organization),(SELECT COUNT(*) FROM allocation),(SELECT COUNT(*) FROM stock_movement);')"
  if EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
    EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
    EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
    EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
    java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
    --c8-additive-demo-isolated-proof > "$RUNNER_TEMP/c8-additive-partial-repeat-private.log" 2>&1; then
    echo "Retry against a partial database unexpectedly succeeded." >&2
    exit 1
  fi
  after="$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT (SELECT COUNT(*) FROM organization),(SELECT COUNT(*) FROM allocation),(SELECT COUNT(*) FROM stock_movement);')"
  [ "$before" = "$after" ]
  [ "$(snapshot_original)" = "$original_sha_before" ]
  [ "$(original_domain_snapshot)" = "$original_domain_sha_before" ]
  # The backup must be restored to an isolated target without overwriting
  # or deleting the partially imported original; then replay must succeed there.
  recovered_schema=orgresource_c8_additive_ci_fixture_recovered
  mysql_ci <<SQL
CREATE DATABASE $recovered_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
GRANT SELECT,INSERT,UPDATE,DELETE ON $recovered_schema.* TO '$runtime_user'@'%';
SQL
  [ "$(sha256sum "$backup_file" | cut -d' ' -f1)" = "$backup_digest" ]
  mysql_ci "$recovered_schema" < "$backup_file"
  source_schema="$schema"
  schema="$recovered_schema"
  [ "$(snapshot_original)" = "$original_sha_before" ]
  [ "$(original_domain_snapshot)" = "$original_domain_sha_before" ]
  [ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL;')" = "0" ]
  EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
    EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
    EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
    EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
    java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
    --c8-additive-demo-isolated-proof
  [ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL;')" = "3" ]
  [ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM allocation;')" = "36" ]
  [ "$(snapshot_original)" = "$original_sha_before" ]
  [ "$(original_domain_snapshot)" = "$original_domain_sha_before" ]
  schema="$source_schema"
  original_baseline VERIFY
  [ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NOT NULL;')" = "1" ]
  [ "$(snapshot_original)" = "$original_sha_before" ]
  [ "$(original_domain_snapshot)" = "$original_domain_sha_before" ]
  echo "ISOLATED_PARTIAL_RESTORE_PROOF_PASSED: original preserved, retry refused; independent restored copy imported 3 fictional workspaces, partial source left untouched."
  exit 0
fi
EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
  EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
  EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
  EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
  java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
  --c8-additive-demo-isolated-proof
counts="$(mysql_ci --batch --skip-column-names "$schema" -e \
  'SELECT (SELECT COUNT(*) FROM organization),(SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL),(SELECT COUNT(*) FROM allocation),(SELECT COUNT(*) FROM stock_movement),(SELECT COUNT(*) FROM local_credential);')"
expected_movement_count="$(python3 - "$plan" <<'PY'
import json, sys
plan = json.load(open(sys.argv[1], encoding='utf-8'))
statuses = plan['final_states']
print(plan['counts']['resources'] + statuses['CONSUMED'] + statuses['ISSUED'] + 2 * statuses['RETURNED'])
PY
)"
expected="$(printf '%s\t%s\t%s\t%s\t1' "$((expected_workspaces+2))" "$expected_workspaces" "$expected_allocations" "$expected_movement_count")"
[ "$counts" = "$expected" ]
owner_count="$(mysql_ci --batch --skip-column-names "$schema" -e \
  "SELECT COUNT(*) FROM membership m JOIN user_account u ON u.id=m.user_id JOIN organization o ON o.id=m.org_id WHERE u.username_key='additive-original-owner' AND m.role='OWNER' AND o.demo_plan_sha IS NOT NULL;")"
[ "$owner_count" = "$expected_workspaces" ]
# The independent reader must reject tampering and accept the repaired disposable fixture.
if [ "$profile" = small ]; then
  mysql_ci "$schema" -e "UPDATE organization SET display_name='Tampered Fictional Label' WHERE demo_reference_key='fictional-small-01';"
  if check_postflight > "$RUNNER_TEMP/c8-postflight-invalid-label.log" 2>&1; then
    echo "Independent postflight accepted the wrong directory label" >&2
    exit 1
  fi
  mysql_ci "$schema" -e "UPDATE organization SET display_name='Fictional Workspace 01' WHERE demo_reference_key='fictional-small-01';"
  audit_id="$(mysql_ci --batch --skip-column-names "$schema" -e "
    SELECT MIN(a.id) FROM audit_event a JOIN organization o ON o.id=a.org_id
    WHERE o.demo_plan_sha IS NOT NULL AND a.action='C8_DEMO_REQUEST';")"
  [ -n "$audit_id" ] && [ "$audit_id" != NULL ]
  mysql_ci "$schema" -e "UPDATE audit_event SET action='C8_DEMO_TAMPER' WHERE id=$audit_id;"
  if check_postflight > "$RUNNER_TEMP/c8-postflight-invalid-audit.log" 2>&1; then
    echo "Independent postflight accepted a tampered simulated audit action" >&2
    exit 1
  fi
  mysql_ci "$schema" -e "UPDATE audit_event SET action='C8_DEMO_REQUEST' WHERE id=$audit_id;"
  # Alter a timestamp without changing its month or action count. Only the
  # exact plan-based audit comparator should detect this.
  mysql_ci "$schema" -e "UPDATE audit_event SET created_at=DATE_ADD(created_at, INTERVAL 1 SECOND) WHERE id=$audit_id;"
  if check_postflight > "$RUNNER_TEMP/c8-postflight-invalid-timestamp.log" 2>&1; then
    echo "Independent postflight accepted a tampered in-month historical timestamp" >&2
    exit 1
  fi
  mysql_ci "$schema" -e "UPDATE audit_event SET created_at=DATE_SUB(created_at, INTERVAL 1 SECOND) WHERE id=$audit_id;"
  allocation_id="$(mysql_ci --batch --skip-column-names "$schema" -e "
    SELECT MIN(a.id) FROM allocation a JOIN organization o ON o.id=a.organization_id
    WHERE o.demo_plan_sha IS NOT NULL;")"
  [ -n "$allocation_id" ] && [ "$allocation_id" != NULL ]
  mysql_ci "$schema" -e "UPDATE allocation SET quantity=quantity+1 WHERE id=$allocation_id;"
  if check_postflight > "$RUNNER_TEMP/c8-postflight-invalid-allocation.log" 2>&1; then
    echo "Independent postflight accepted a tampered Allocation quantity" >&2
    exit 1
  fi
  mysql_ci "$schema" -e "UPDATE allocation SET quantity=quantity-1 WHERE id=$allocation_id;"
  original_baseline VERIFY
  # Even when demo records are valid, changing the *original* organization
  # must cause the separate private before/after HMAC manifest to refuse.
  mysql_ci "$schema" -e "UPDATE organization SET display_name='Tampered Original Workspace' WHERE slug='isolated-original-workspace';"
  if original_baseline VERIFY > "$RUNNER_TEMP/c8-baseline-original-tamper-refusal.log" 2>&1; then
    echo "Original-data baseline accepted an altered original organization" >&2
    exit 1
  fi
  grep -q 'C8_ORIGINAL_BASELINE_MISMATCH' "$RUNNER_TEMP/c8-baseline-original-tamper-refusal.log"
  mysql_ci "$schema" -e "UPDATE organization SET display_name='Isolated Original Workspace' WHERE slug='isolated-original-workspace';"
  original_baseline VERIFY
  # A modified original password hash must be rejected independently of
  # synthetic Project/Allocation consistency; restore exact bytes afterward.
  mysql_ci "$schema" -e "UPDATE local_credential SET password_hash=CONCAT(password_hash,'x')
    WHERE user_id=(SELECT id FROM user_account WHERE username_key='additive-original-owner');"
  if original_baseline VERIFY > "$RUNNER_TEMP/c8-baseline-credential-tamper-refusal.log" 2>&1; then
    echo "Original-data baseline accepted a changed original credential hash" >&2
    exit 1
  fi
  grep -q 'C8_ORIGINAL_BASELINE_MISMATCH' "$RUNNER_TEMP/c8-baseline-credential-tamper-refusal.log"
  mysql_ci "$schema" -e "UPDATE local_credential SET password_hash=LEFT(password_hash,CHAR_LENGTH(password_hash)-1)
    WHERE user_id=(SELECT id FROM user_account WHERE username_key='additive-original-owner');"
  original_baseline VERIFY
  mysql_ci "$schema" -e "UPDATE organization SET display_name='Tampered Other Original' WHERE slug='isolated-other-original';"
  if original_baseline VERIFY > "$RUNNER_TEMP/c8-other-org-tamper.log" 2>&1; then
    echo "Original-data verifier accepted another original organization mutation" >&2
    exit 1
  fi
  grep -q 'C8_ORIGINAL_BASELINE_MISMATCH' "$RUNNER_TEMP/c8-other-org-tamper.log"
  mysql_ci "$schema" -e "UPDATE organization SET display_name='Other Original Org' WHERE slug='isolated-other-original';"
  mysql_ci "$schema" -e "UPDATE user_account SET display_name='Tampered Other User' WHERE username_key='other-original-user';"
  if original_baseline VERIFY > "$RUNNER_TEMP/c8-other-account-tamper.log" 2>&1; then
    echo "Original-data verifier accepted another original account mutation" >&2
    exit 1
  fi
  grep -q 'C8_ORIGINAL_BASELINE_MISMATCH' "$RUNNER_TEMP/c8-other-account-tamper.log"
  mysql_ci "$schema" -e "UPDATE user_account SET display_name='Other Original User' WHERE username_key='other-original-user';"
  original_baseline VERIFY
  check_postflight
fi
# Full source-derived SQL conservation checks across every imported tenant.
expected_events="$(python3 - "$plan" <<'PY'
import json,sys
with open(sys.argv[1],encoding='utf-8') as stream: print(json.load(stream)['counts']['events'])
PY
)"
actual_events="$(mysql_ci --batch --skip-column-names "$schema" -e "
SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id
WHERE o.demo_plan_sha IS NOT NULL AND a.action LIKE 'C8_DEMO_%';")"
[ "$actual_events" = "$expected_events" ]
for state in PENDING APPROVED ISSUED RETURNED CONSUMED CANCELLED; do
  state_expected="$(python3 - "$plan" "$state" <<'PY'
import json,sys
with open(sys.argv[1],encoding='utf-8') as stream: print(json.load(stream)['final_states'][sys.argv[2]])
PY
)"
  state_actual="$(mysql_ci --batch --skip-column-names "$schema" -e "
SELECT COUNT(*) FROM allocation a JOIN organization o ON o.id=a.organization_id
WHERE o.demo_plan_sha IS NOT NULL AND a.status='$state';")"
  [ "$state_actual" = "$state_expected" ]
done
sms_count="$(mysql_ci --batch --skip-column-names "$schema" -e "
SELECT COUNT(*) FROM allocation a JOIN organization o ON o.id=a.organization_id
WHERE o.demo_plan_sha IS NOT NULL AND (a.sms_consent OR a.phone_ciphertext IS NOT NULL);")"
[ "$sms_count" = "0" ]
invalid_stock="$(mysql_ci --batch --skip-column-names "$schema" <<'SQL'
SELECT COUNT(*) FROM resource r
JOIN organization o ON o.id=r.organization_id AND o.demo_plan_sha IS NOT NULL
LEFT JOIN (SELECT organization_id,resource_id,SUM(delta_total) total,SUM(delta_issued) issued
  FROM stock_movement GROUP BY organization_id,resource_id) m
  ON m.organization_id=r.organization_id AND m.resource_id=r.id
LEFT JOIN (SELECT a.organization_id,a.resource_id,
    SUM(CASE WHEN a.status='ISSUED' THEN a.quantity ELSE 0 END) issued,
    SUM(CASE WHEN a.status='APPROVED' AND p.end_at>'2026-11-19 00:00:00'
      THEN a.quantity ELSE 0 END) holds
  FROM allocation a JOIN resource_requirement q ON q.id=a.requirement_id AND q.organization_id=a.organization_id
  JOIN project p ON p.id=q.project_id AND p.organization_id=a.organization_id
  GROUP BY a.organization_id,a.resource_id) a
  ON a.organization_id=r.organization_id AND a.resource_id=r.id
WHERE r.total_quantity<>COALESCE(m.total,0) OR COALESCE(m.issued,0)<>COALESCE(a.issued,0)
  OR COALESCE(a.issued,0)+COALESCE(a.holds,0)>r.total_quantity;
SQL
)"
[ "$invalid_stock" = "0" ]
invalid_requirement="$(mysql_ci --batch --skip-column-names "$schema" <<'SQL'
SELECT COUNT(*) FROM resource_requirement q
JOIN organization o ON o.id=q.organization_id AND o.demo_plan_sha IS NOT NULL
JOIN (SELECT a.organization_id,a.requirement_id,SUM(a.quantity) covered
    FROM allocation a
    JOIN resource_requirement rr ON rr.id=a.requirement_id AND rr.organization_id=a.organization_id
    JOIN project p ON p.id=rr.project_id AND p.organization_id=a.organization_id
    WHERE a.status IN ('ISSUED','RETURNED','CONSUMED')
      OR (a.status='APPROVED' AND p.end_at>'2026-11-19 00:00:00')
    GROUP BY a.organization_id,a.requirement_id) c
  ON c.organization_id=q.organization_id AND c.requirement_id=q.id
WHERE c.covered>q.quantity;
SQL
)"
[ "$invalid_requirement" = "0" ]
[ "$(original_domain_snapshot)" = "$original_domain_sha_before" ]
# Fictional historical events must never be attributed to the real current owner.
simulated_history="$(mysql_ci --batch --skip-column-names "$schema" -e "SELECT COUNT(*) FROM audit_event a JOIN user_account u ON u.id=a.actor_user_id WHERE u.username_key='c8-demo-historical-actor' AND a.action='C8_DEMO_REQUEST';")"
[ "$simulated_history" = "$expected_allocations" ]
real_owner_history="$(mysql_ci --batch --skip-column-names "$schema" -e "SELECT COUNT(*) FROM audit_event a JOIN user_account u ON u.id=a.actor_user_id WHERE u.username_key='additive-original-owner' AND a.action LIKE 'C8_DEMO_%';")"
[ "$real_owner_history" = "0" ]
[ "$(snapshot_original)" = "$original_sha_before" ]
if EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
  EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
  EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
  EXT_DB_USER="$runtime_user" EXT_DB_PASSWORD="$runtime_password" \
  java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
  --c8-additive-demo-isolated-proof > "$RUNNER_TEMP/c8-additive-repeat-refusal.log" 2>&1; then
  echo "Repeated additive demo import unexpectedly succeeded" >&2
  exit 1
fi
[ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM allocation;')" = "$expected_allocations" ]
[ "$(snapshot_original)" = "$original_sha_before" ]
[ "$(original_domain_snapshot)" = "$original_domain_sha_before" ]
echo "ISOLATED_ADDITIVE_PROOF_PASSED: original owner/domain unchanged; $expected_workspaces workspaces, $expected_allocations allocations, $expected_events synthetic events; stock/requirements/states clean, repeat refused; no hosted DB used."

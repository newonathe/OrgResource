#!/usr/bin/env bash
# Isolated Java/MySQL proof ONLY. Hard stop for any non-disposable remote target.
set -euo pipefail
schema=orgresource_c8_additive_ci_fixture
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
CREATE USER 'c8_additive_runtime'@'%' IDENTIFIED BY '$runtime_password';
CREATE USER 'c8_additive_migrator'@'%' IDENTIFIED BY '$migration_password';
GRANT SELECT,INSERT,UPDATE,DELETE ON $schema.* TO 'c8_additive_runtime'@'%';
GRANT ALL PRIVILEGES ON $schema.* TO 'c8_additive_migrator'@'%';
SQL
url="jdbc:mysql://127.0.0.1:3306/$schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
EXT_DB_URL="$url" EXT_DB_USER=c8_additive_runtime EXT_DB_PASSWORD="$runtime_password" \
  EXT_DB_MIGRATION_URL="$url" EXT_DB_MIGRATION_USER=c8_additive_migrator \
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
plan="$RUNNER_TEMP/c8-additive-small-private.json"
python3 scripts/generate-c8-demo.py --profile small --seed c8-fixture-proof-v1 --output "$plan"
digest="$(sha256sum "$plan" | cut -d' ' -f1)"
EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
  EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
  EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
  EXT_DB_USER=c8_additive_runtime EXT_DB_PASSWORD="$runtime_password" \
  java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
  --c8-additive-demo-isolated-proof
counts="$(mysql_ci --batch --skip-column-names "$schema" -e \
  'SELECT (SELECT COUNT(*) FROM organization),(SELECT COUNT(*) FROM organization WHERE demo_plan_sha IS NOT NULL),(SELECT COUNT(*) FROM allocation),(SELECT COUNT(*) FROM stock_movement),(SELECT COUNT(*) FROM local_credential);')"
[ "$counts" = $'4\t3\t36\t40\t1' ]
owner_count="$(mysql_ci --batch --skip-column-names "$schema" -e \
  "SELECT COUNT(*) FROM membership m JOIN user_account u ON u.id=m.user_id JOIN organization o ON o.id=m.org_id WHERE u.username_key='additive-original-owner' AND m.role='OWNER' AND o.demo_plan_sha IS NOT NULL;")"
[ "$owner_count" = "3" ]
# Fictional historical events must never be attributed to the real current owner.
simulated_history="$(mysql_ci --batch --skip-column-names "$schema" -e "SELECT COUNT(*) FROM audit_event a JOIN user_account u ON u.id=a.actor_user_id WHERE u.username_key='c8-demo-historical-actor' AND a.action='C8_DEMO_REQUEST';")"
[ "$simulated_history" = "36" ]
real_owner_history="$(mysql_ci --batch --skip-column-names "$schema" -e "SELECT COUNT(*) FROM audit_event a JOIN user_account u ON u.id=a.actor_user_id WHERE u.username_key='additive-original-owner' AND a.action LIKE 'C8_DEMO_%';")"
[ "$real_owner_history" = "0" ]
[ "$(snapshot_original)" = "$original_sha_before" ]
if EXT_C8_PROOF_SCHEMA="$schema" EXT_C8_PROOF_CONFIRM="TEST ADDITIVE $schema" \
  EXT_C8_PROOF_PORT=3306 EXT_C8_DEMO_PLAN="$plan" EXT_C8_DEMO_PLAN_SHA256="$digest" \
  EXT_C8_PROOF_ORIGINAL_ORG_ID="$original_org" EXT_C8_PROOF_OWNER_ACCOUNT_ID="$owner_id" \
  EXT_DB_USER=c8_additive_runtime EXT_DB_PASSWORD="$runtime_password" \
  java -Xmx768m -jar extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar \
  --c8-additive-demo-isolated-proof > "$RUNNER_TEMP/c8-additive-repeat-refusal.log" 2>&1; then
  echo "Repeated additive demo import unexpectedly succeeded" >&2
  exit 1
fi
[ "$(mysql_ci --batch --skip-column-names "$schema" -e 'SELECT COUNT(*) FROM allocation;')" = "36" ]
[ "$(snapshot_original)" = "$original_sha_before" ]
echo "ISOLATED_ADDITIVE_PROOF_PASSED: 1 original and 3 fictional workspaces, repeat refused; no hosted DB used."

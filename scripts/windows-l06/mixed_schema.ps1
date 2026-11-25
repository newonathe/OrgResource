# Same assertions as Assert-LocalMixedSchema in the pinned extended-c-backup.ps1 (its Docker-only mode cannot run on Windows).
function Get-MixedChecks {
    return [ordered]@{
        'SELECT COUNT(*) FROM organization' = '98'
        "SELECT COUNT(*) FROM organization WHERE demo_dataset_id='$approvedDataset' AND demo_source_sha='$approvedSourceSha' AND demo_plan_sha='$approvedPlanSha' AND demo_as_of='2026-11-19' AND demo_reference_key IS NOT NULL" = '96'
        'SELECT COUNT(DISTINCT demo_reference_key) FROM organization WHERE demo_dataset_id IS NOT NULL' = '96'
        "SELECT COUNT(*) FROM organization WHERE demo_dataset_id IS NULL AND demo_source_sha IS NULL AND demo_plan_sha IS NULL AND demo_as_of IS NULL AND demo_reference_key IS NULL AND slug IN ('isolated-original-workspace','isolated-other-original')" = '2'
        "SELECT COUNT(*) FROM membership m JOIN organization o ON o.id=m.org_id JOIN user_account u ON u.id=m.user_id JOIN local_credential c ON c.user_id=u.id WHERE u.username_key='additive-original-owner' AND u.status='ACTIVE' AND o.status='ACTIVE' AND m.role='OWNER' AND m.status='ACTIVE' AND (o.demo_plan_sha='$approvedPlanSha' OR o.slug='isolated-original-workspace')" = '97'
        'SELECT COUNT(*) FROM allocation' = '21060'
        "SELECT COUNT(*) FROM audit_event a JOIN organization o ON o.id=a.org_id WHERE o.demo_plan_sha='$approvedPlanSha' AND a.action LIKE 'C8_DEMO_%'" = '67370'
        'SELECT COUNT(*) FROM SPRING_SESSION' = '0'
        'SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES' = '0'
    }
}
function Assert-MixedSchema([string] $Role) {
    $required = @('account_recovery_code', 'allocation', 'audit_event', 'department', 'flyway_schema_history', 'idempotency_key', 'invitation', 'local_credential', 'membership', 'organization', 'ownership_transfer',
        'project', 'resource', 'resource_requirement', 'spring_session', 'spring_session_attributes', 'stock_movement', 'user_account')
    $tables = (Get-Scalar $Role 'SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY LOWER(table_name)') -split '\n'
    if (@(Compare-Object $required $tables).Count) { Fail 'R06B3A_MIXED_SCHEMA_REQUIRED' }
    $checks = Get-MixedChecks
    foreach ($sql in $checks.Keys) { if ((Get-Scalar $Role $sql) -cne $checks[$sql]) { Fail 'R06B3A_MIXED_FIXTURE_REQUIRED' } }
}

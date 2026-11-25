-- Private projection only. Execute once on the guarded recovered server with
-- SELECT-only credentials, after exact V7/postflight and before launching JVM.
-- mysql --batch --raw --skip-column-names; stdout must be a private 0600 file.
START TRANSACTION READ ONLY;
SELECT JSON_OBJECT('kind','server','schema',DATABASE(),'uuid',@@server_uuid,'version',VERSION());
SELECT JSON_OBJECT('kind','owner','id',LOWER(BIN_TO_UUID(u.public_id,0)),
    'status',u.status,'credentialed',IF(c.user_id IS NULL,0,1))
FROM user_account u LEFT JOIN local_credential c ON c.user_id=u.id
WHERE u.username_key='additive-original-owner';
SELECT JSON_OBJECT('kind','org','id',LOWER(BIN_TO_UUID(o.public_id,0)),
    'slug',o.slug,'displayName',o.display_name,'status',o.status,
    'dataset',o.demo_dataset_id,'reference',o.demo_reference_key,
    'source',o.demo_source_sha,'plan',o.demo_plan_sha,'asOf',DATE_FORMAT(o.demo_as_of,'%Y-%m-%d'),
    'ownerRole',m.role,'ownerStatus',m.status)
FROM organization o LEFT JOIN membership m ON m.org_id=o.id AND
    m.user_id=(SELECT id FROM user_account WHERE username_key='additive-original-owner')
ORDER BY o.id;
-- No date sampling is needed for the chosen detail: require zero APPROVED
-- allocations of any date, so elapsed wall time cannot alter its balance.
WITH movements AS (
    SELECT organization_id,resource_id,SUM(delta_total) AS ledger_total,SUM(delta_issued) AS ledger_issued
    FROM stock_movement GROUP BY organization_id,resource_id
), allocated AS (
    SELECT organization_id,resource_id,
        SUM(CASE WHEN status='ISSUED' THEN quantity ELSE 0 END) AS issued,
        SUM(CASE WHEN status='APPROVED' THEN 1 ELSE 0 END) AS approved_count
    FROM allocation GROUP BY organization_id,resource_id
)
SELECT JSON_OBJECT('kind','resource','org',LOWER(BIN_TO_UUID(o.public_id,0)),
    'dbId',r.id,'id',LOWER(BIN_TO_UUID(r.public_id,0)),'name',r.name,'type',r.type,
    'totalQuantity',r.total_quantity,'version',r.version,
    'ledgerTotal',CAST(COALESCE(m.ledger_total,0) AS SIGNED),
    'ledgerIssued',CAST(COALESCE(m.ledger_issued,0) AS SIGNED),
    'issued',CAST(COALESCE(a.issued,0) AS SIGNED),
    'approvedCount',CAST(COALESCE(a.approved_count,0) AS SIGNED))
FROM resource r JOIN organization o ON o.id=r.organization_id
LEFT JOIN movements m ON m.organization_id=r.organization_id AND m.resource_id=r.id
LEFT JOIN allocated a ON a.organization_id=r.organization_id AND a.resource_id=r.id
ORDER BY r.id;
ROLLBACK;

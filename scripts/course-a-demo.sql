-- OrgResource Course A demonstration walkthrough (read-only).
-- Select a local OrgResource schema in the MySQL client before running this file.
-- For a clean presentation use the new local orgresource schema; the protected
-- orgresource_test schema is only for technical acceptance and accumulates fixtures.
-- This file contains no INSERT, UPDATE, DELETE, DDL, or reset statements.
-- Allocation borrower names, phone numbers, and provider IDs are never selected.

SELECT DATABASE() AS selected_database;

-- The application has exactly five JPA business-entity tables.
SHOW TABLES;

-- Confirm the expected tables use InnoDB.
SELECT table_name, engine
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('department', 'project', 'resource', 'resource_requirement', 'allocation')
ORDER BY FIELD(table_name, 'department', 'project', 'resource', 'resource_requirement', 'allocation');

-- Exact visible row counts.
SELECT 'department' AS entity, COUNT(*) AS row_count FROM department
UNION ALL SELECT 'project', COUNT(*) FROM project
UNION ALL SELECT 'resource', COUNT(*) FROM resource
UNION ALL SELECT 'resource_requirement', COUNT(*) FROM resource_requirement
UNION ALL SELECT 'allocation', COUNT(*) FROM allocation;

-- Foreign-key relationships, without displaying unrelated schema metadata.
SELECT table_name, column_name, referenced_table_name, referenced_column_name
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND referenced_table_name IS NOT NULL
  AND table_name IN ('project', 'resource_requirement', 'allocation')
ORDER BY FIELD(table_name, 'project', 'resource_requirement', 'allocation'), ordinal_position;

-- A few rows for an academic demonstration. Add fictional records in the UI first.
SELECT id, name FROM department ORDER BY id DESC LIMIT 5;

SELECT p.id, p.name AS project_name, d.name AS department_name, p.start_at, p.end_at
FROM project AS p
JOIN department AS d ON d.id = p.department_id
ORDER BY p.id DESC
LIMIT 5;

SELECT id, name, type, total_quantity FROM resource ORDER BY id DESC LIMIT 5;

SELECT rr.id, p.name AS project_name, r.name AS resource_name, rr.quantity AS required_quantity
FROM resource_requirement AS rr
JOIN project AS p ON p.id = rr.project_id
JOIN resource AS r ON r.id = rr.resource_id
ORDER BY rr.id DESC
LIMIT 5;

SELECT a.id, a.requirement_id, a.quantity, a.status,
       CASE WHEN a.sms_consent = 1 THEN 'yes' ELSE 'no' END AS sms_consent,
       (a.phone_number IS NOT NULL) AS has_contact_on_file
FROM allocation AS a
ORDER BY a.id DESC
LIMIT 5;

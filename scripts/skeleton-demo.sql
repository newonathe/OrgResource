-- OrgResource O1 skeleton database walkthrough (read-only).
-- Safe to run repeatedly. This file contains no INSERT, UPDATE, DELETE, DDL, or reset statements.
-- Fictional/demo values only. Allocation contact values and borrower names are deliberately omitted.

USE orgresource;

SELECT DATABASE() AS selected_database;

-- The O1 skeleton uses these five entity tables.
SHOW TABLES;

-- Confirm storage engine for each entity table. Exact counts follow below.
SELECT table_name, engine
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('department', 'project', 'resource', 'resource_requirement', 'allocation')
ORDER BY FIELD(table_name, 'department', 'project', 'resource', 'resource_requirement', 'allocation');

-- Actual row counts (COUNT(*) is exact for the rows visible to this connection).
SELECT 'department' AS entity, COUNT(*) AS row_count FROM department
UNION ALL SELECT 'project', COUNT(*) FROM project
UNION ALL SELECT 'resource', COUNT(*) FROM resource
UNION ALL SELECT 'resource_requirement', COUNT(*) FROM resource_requirement
UNION ALL SELECT 'allocation', COUNT(*) FROM allocation;

-- Relationship map: Project -> Department; Requirement -> Project and Resource; Allocation -> Requirement.
SELECT table_name, column_name, referenced_table_name, referenced_column_name
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND referenced_table_name IS NOT NULL
  AND table_name IN ('project', 'resource_requirement', 'allocation')
ORDER BY FIELD(table_name, 'project', 'resource_requirement', 'allocation'), ordinal_position;

-- A few fictional organization and inventory rows. LIMIT keeps the walkthrough compact.
SELECT id, name FROM department ORDER BY id LIMIT 5;

SELECT p.id, p.name AS project_name, d.name AS department_name, p.start_at, p.end_at
FROM project AS p
JOIN department AS d ON d.id = p.department_id
ORDER BY p.id
LIMIT 5;

SELECT id, name, type, total_quantity
FROM resource
ORDER BY id
LIMIT 5;

SELECT rr.id, p.name AS project_name, r.name AS resource_name, rr.quantity AS required_quantity
FROM resource_requirement AS rr
JOIN project AS p ON p.id = rr.project_id
JOIN resource AS r ON r.id = rr.resource_id
ORDER BY rr.id
LIMIT 5;

-- No borrower names, phone numbers, provider IDs, or other contact details are selected.
SELECT a.id, a.requirement_id, a.quantity, a.status,
       CASE WHEN a.sms_consent = 1 THEN 'yes' ELSE 'no' END AS sms_consent,
       (a.phone_number IS NOT NULL) AS has_contact_on_file
FROM allocation AS a
ORDER BY a.id
LIMIT 5;

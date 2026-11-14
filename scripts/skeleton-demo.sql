-- OrgResource O1: READ-ONLY demonstration SQL. Names confirmed from committed JPA entity mappings.
-- This script was NOT executed by ChatGPT against your private local MySQL database.
-- No DROP/TRUNCATE/DELETE, and no sensitive phone/contact columns.
USE orgresource;

-- A: Show MySQL version and five JPA tables.
SELECT VERSION() AS mysql_server_version;
SHOW TABLES;

-- B: Verify rows in each of the five tables.
SELECT 'department' AS entity_name, COUNT(*) AS row_count FROM department
UNION ALL SELECT 'project', COUNT(*) FROM project
UNION ALL SELECT 'resource', COUNT(*) FROM resource
UNION ALL SELECT 'resource_requirement', COUNT(*) FROM resource_requirement
UNION ALL SELECT 'allocation', COUNT(*) FROM allocation;

-- C: Show the actual four foreign keys linking the five entities.
SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'orgresource'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- D: Display a few fictional records only. Do not show raw phone_number or passwords.
SELECT id, name FROM department ORDER BY id LIMIT 5;
SELECT id, department_id, name, start_at, end_at FROM project ORDER BY id LIMIT 5;
SELECT id, name, type, total_quantity FROM resource ORDER BY id LIMIT 5;
SELECT id, project_id, resource_id, quantity FROM resource_requirement ORDER BY id LIMIT 5;
SELECT id, requirement_id, quantity, status, created_at FROM allocation ORDER BY id LIMIT 5;

-- Optional: show relationship constraints in more detail.
-- SHOW CREATE TABLE resource_requirement;

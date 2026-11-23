ALTER TABLE organization
    ADD COLUMN demo_dataset_id VARCHAR(96) NULL,
    ADD COLUMN demo_reference_key VARCHAR(128) NULL,
    ADD COLUMN demo_source_sha CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    ADD COLUMN demo_plan_sha CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    ADD COLUMN demo_as_of DATE NULL,
    ADD UNIQUE KEY uk_organization_demo_reference (demo_dataset_id, demo_reference_key),
    ADD CONSTRAINT ck_organization_demo_provenance CHECK (
        (demo_dataset_id IS NULL AND demo_reference_key IS NULL AND demo_source_sha IS NULL
            AND demo_plan_sha IS NULL AND demo_as_of IS NULL)
        OR (demo_dataset_id IS NOT NULL AND demo_reference_key IS NOT NULL AND demo_source_sha IS NOT NULL
            AND demo_plan_sha IS NOT NULL AND demo_as_of IS NOT NULL)
    );

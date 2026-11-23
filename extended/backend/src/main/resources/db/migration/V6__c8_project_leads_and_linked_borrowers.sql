ALTER TABLE project ADD COLUMN public_id BINARY(16) NULL;
UPDATE project SET public_id = UUID_TO_BIN(UUID(), 0) WHERE public_id IS NULL;
ALTER TABLE project
    MODIFY COLUMN public_id BINARY(16) NOT NULL,
    ADD UNIQUE KEY uk_project_public_id (public_id),
    ADD COLUMN lead_membership_id BIGINT NULL,
    ADD KEY ix_project_assigned_lead (organization_id, lead_membership_id, id),
    ADD CONSTRAINT fk_project_lead_scope FOREIGN KEY (organization_id, lead_membership_id)
        REFERENCES membership (org_id, id) ON DELETE RESTRICT;

ALTER TABLE resource_requirement ADD COLUMN public_id BINARY(16) NULL;
UPDATE resource_requirement SET public_id = UUID_TO_BIN(UUID(), 0) WHERE public_id IS NULL;
ALTER TABLE resource_requirement
    MODIFY COLUMN public_id BINARY(16) NOT NULL,
    ADD UNIQUE KEY uk_requirement_public_id (public_id);

ALTER TABLE allocation
    MODIFY COLUMN borrower_name VARCHAR(120) NOT NULL,
    ADD COLUMN borrower_membership_id BIGINT NULL,
    ADD COLUMN requested_by_membership_id BIGINT NULL,
    ADD KEY ix_allocation_linked_borrower (organization_id, borrower_membership_id, id),
    ADD CONSTRAINT fk_allocation_borrower_scope FOREIGN KEY (organization_id, borrower_membership_id)
        REFERENCES membership (org_id, id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_allocation_requester_scope FOREIGN KEY (organization_id, requested_by_membership_id)
        REFERENCES membership (org_id, id) ON DELETE RESTRICT,
    ADD CONSTRAINT ck_allocation_link_has_requester CHECK
        (borrower_membership_id IS NULL OR requested_by_membership_id IS NOT NULL);

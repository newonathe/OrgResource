CREATE TABLE organization (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    slug VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    status VARCHAR(16) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_organization_public_id (public_id),
    UNIQUE KEY uk_organization_slug (slug),
    CONSTRAINT ck_organization_status CHECK (status IN ('ACTIVE','FROZEN'))
) ENGINE=InnoDB;

CREATE TABLE user_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    username_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    status VARCHAR(16) NOT NULL,
    credential_version BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_public_id (public_id),
    UNIQUE KEY uk_user_username_key (username_key),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE','DISABLED')),
    CONSTRAINT ck_user_credential_version CHECK (credential_version >= 0)
) ENGINE=InnoDB;

CREATE TABLE local_credential (
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    changed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_credential_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE account_recovery_code (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    code_hash BINARY(32) NOT NULL,
    batch_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    revoked_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recovery_hash (code_hash),
    KEY ix_recovery_user_live (user_id, used_at, revoked_at, expires_at),
    CONSTRAINT fk_recovery_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE RESTRICT,
    CONSTRAINT ck_recovery_expiry CHECK (expires_at > created_at)
) ENGINE=InnoDB;

CREATE TABLE membership (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    org_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_membership_public_id (public_id),
    UNIQUE KEY uk_membership_org_user (org_id, user_id),
    UNIQUE KEY uk_membership_org_id_id (org_id, id),
    KEY ix_membership_user_status (user_id, status, org_id),
    CONSTRAINT fk_membership_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE RESTRICT,
    CONSTRAINT ck_membership_role CHECK (role IN ('OWNER','ADMIN','CUSTODIAN','LEAD','MEMBER')),
    CONSTRAINT ck_membership_status CHECK (status IN ('ACTIVE','SUSPENDED','LEFT'))
) ENGINE=InnoDB;

CREATE TABLE invitation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    org_id BIGINT NOT NULL,
    invited_by_membership_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    token_hash BINARY(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    accepted_by_user_id BIGINT NULL,
    expires_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invitation_public_id (public_id),
    UNIQUE KEY uk_invitation_token_hash (token_hash),
    KEY ix_invitation_org_status (org_id, status, expires_at),
    CONSTRAINT fk_invitation_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT fk_invitation_grantor FOREIGN KEY (org_id, invited_by_membership_id)
        REFERENCES membership (org_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_invitation_acceptor FOREIGN KEY (accepted_by_user_id) REFERENCES user_account (id) ON DELETE RESTRICT,
    CONSTRAINT ck_invitation_role CHECK (role IN ('ADMIN','CUSTODIAN','LEAD','MEMBER')),
    CONSTRAINT ck_invitation_status CHECK (status IN ('INVITED','ACCEPTED','REVOKED','EXPIRED')),
    CONSTRAINT ck_invitation_expiry CHECK (expires_at > created_at)
) ENGINE=InnoDB;

CREATE TABLE ownership_transfer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    org_id BIGINT NOT NULL,
    from_membership_id BIGINT NOT NULL,
    to_membership_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    pending_org_id BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'PENDING' THEN org_id ELSE NULL END) STORED,
    version BIGINT NOT NULL DEFAULT 0,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    accepted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transfer_public_id (public_id),
    UNIQUE KEY uk_transfer_one_pending_per_org (pending_org_id),
    KEY ix_transfer_org_status (org_id, status, expires_at),
    CONSTRAINT fk_transfer_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_from_member FOREIGN KEY (org_id, from_membership_id)
        REFERENCES membership (org_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_to_member FOREIGN KEY (org_id, to_membership_id)
        REFERENCES membership (org_id, id) ON DELETE RESTRICT,
    CONSTRAINT ck_transfer_status CHECK (status IN ('PENDING','ACCEPTED','REVOKED','EXPIRED')),
    CONSTRAINT ck_transfer_distinct_members CHECK (from_membership_id <> to_membership_id),
    CONSTRAINT ck_transfer_expiry CHECK (expires_at > created_at)
) ENGINE=InnoDB;

CREATE TABLE audit_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    org_id BIGINT NULL,
    actor_user_id BIGINT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(64) NULL,
    outcome VARCHAR(16) NOT NULL,
    request_id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_audit_org_created (org_id, created_at, id),
    KEY ix_audit_actor_created (actor_user_id, created_at),
    CONSTRAINT fk_audit_org FOREIGN KEY (org_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES user_account (id) ON DELETE RESTRICT,
    CONSTRAINT ck_audit_outcome CHECK (outcome IN ('SUCCESS','DENIED','FAILURE'))
) ENGINE=InnoDB;

CREATE TABLE department (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_key VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_org_name (organization_id, name_key),
    UNIQUE KEY uk_department_id_org (id, organization_id),
    CONSTRAINT fk_department_org FOREIGN KEY (organization_id) REFERENCES organization (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE project (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_id_org (id, organization_id),
    KEY ix_project_org_department (organization_id, department_id),
    CONSTRAINT fk_project_org FOREIGN KEY (organization_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT fk_project_department_scope FOREIGN KEY (department_id, organization_id)
        REFERENCES department (id, organization_id) ON DELETE RESTRICT,
    CONSTRAINT ck_project_period CHECK (end_at > start_at)
) ENGINE=InnoDB;

CREATE TABLE resource (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_key VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    type VARCHAR(20) NOT NULL,
    total_quantity INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resource_org_name (organization_id, name_key),
    UNIQUE KEY uk_resource_id_org (id, organization_id),
    CONSTRAINT fk_resource_org FOREIGN KEY (organization_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT ck_resource_total_nonnegative CHECK (total_quantity BETWEEN 0 AND 1000000)
) ENGINE=InnoDB;

CREATE TABLE resource_requirement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_requirement_project_resource (organization_id, project_id, resource_id),
    UNIQUE KEY uk_requirement_id_org (id, organization_id),
    CONSTRAINT fk_requirement_org FOREIGN KEY (organization_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT fk_requirement_project_scope FOREIGN KEY (project_id, organization_id)
        REFERENCES project (id, organization_id) ON DELETE RESTRICT,
    CONSTRAINT fk_requirement_resource_scope FOREIGN KEY (resource_id, organization_id)
        REFERENCES resource (id, organization_id) ON DELETE RESTRICT,
    CONSTRAINT ck_requirement_quantity_positive CHECK (quantity BETWEEN 1 AND 1000000)
) ENGINE=InnoDB;

CREATE TABLE allocation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    requirement_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    borrower_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NULL,
    sms_consent BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    approved_at DATETIME(6) NULL,
    issued_at DATETIME(6) NULL,
    cancelled_at DATETIME(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY ix_allocation_org_requirement_status (organization_id, requirement_id, status),
    CONSTRAINT fk_allocation_org FOREIGN KEY (organization_id) REFERENCES organization (id) ON DELETE RESTRICT,
    CONSTRAINT fk_allocation_requirement_scope FOREIGN KEY (requirement_id, organization_id)
        REFERENCES resource_requirement (id, organization_id) ON DELETE RESTRICT,
    CONSTRAINT ck_allocation_quantity_positive CHECK (quantity BETWEEN 1 AND 1000000),
    CONSTRAINT ck_allocation_status CHECK (status IN ('PENDING','APPROVED','ISSUED','RETURNED','CONSUMED','CANCELLED'))
) ENGINE=InnoDB;

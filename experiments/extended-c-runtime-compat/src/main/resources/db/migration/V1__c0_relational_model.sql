CREATE TABLE department (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_key VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_org_name (organization_id, name_key),
    UNIQUE KEY uk_department_id_org (id, organization_id)
) ENGINE=InnoDB;

CREATE TABLE project (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_id_org (id, organization_id),
    KEY ix_project_org_department (organization_id, department_id),
    CONSTRAINT fk_project_department_scope
        FOREIGN KEY (department_id, organization_id)
        REFERENCES department (id, organization_id),
    CONSTRAINT ck_project_period CHECK (end_at > start_at)
) ENGINE=InnoDB;

CREATE TABLE resource (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_key VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    total_quantity INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resource_org_name (organization_id, name_key),
    UNIQUE KEY uk_resource_id_org (id, organization_id),
    CONSTRAINT ck_resource_total_nonnegative CHECK (total_quantity >= 0)
) ENGINE=InnoDB;

CREATE TABLE resource_requirement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_requirement_project_resource (organization_id, project_id, resource_id),
    UNIQUE KEY uk_requirement_id_org (id, organization_id),
    CONSTRAINT fk_requirement_project_scope
        FOREIGN KEY (project_id, organization_id)
        REFERENCES project (id, organization_id),
    CONSTRAINT fk_requirement_resource_scope
        FOREIGN KEY (resource_id, organization_id)
        REFERENCES resource (id, organization_id),
    CONSTRAINT ck_requirement_quantity_positive CHECK (quantity > 0)
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
    PRIMARY KEY (id),
    KEY ix_allocation_org_requirement_status (organization_id, requirement_id, status),
    CONSTRAINT fk_allocation_requirement_scope
        FOREIGN KEY (requirement_id, organization_id)
        REFERENCES resource_requirement (id, organization_id),
    CONSTRAINT ck_allocation_quantity_positive CHECK (quantity > 0)
) ENGINE=InnoDB;

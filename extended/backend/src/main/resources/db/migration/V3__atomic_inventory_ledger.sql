-- Refuse an upgrade that would require inventing opening-balance history.
DELIMITER $$
DROP PROCEDURE IF EXISTS orgresource_c2_v3_assert_stock_history_reconciled$$
CREATE PROCEDURE orgresource_c2_v3_assert_stock_history_reconciled()
BEGIN
    IF EXISTS (SELECT 1 FROM resource WHERE total_quantity <> 0 LIMIT 1)
       OR EXISTS (SELECT 1 FROM allocation WHERE status IN ('ISSUED','RETURNED','CONSUMED') LIMIT 1) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'LEGACY_STOCK_HISTORY_UNRECONCILED';
    END IF;
END$$
DELIMITER ;

CALL orgresource_c2_v3_assert_stock_history_reconciled();
DROP PROCEDURE orgresource_c2_v3_assert_stock_history_reconciled;

ALTER TABLE resource
    ADD COLUMN public_id BINARY(16) NULL;
UPDATE resource SET public_id = UUID_TO_BIN(UUID(), 0) WHERE public_id IS NULL;
ALTER TABLE resource
    MODIFY COLUMN public_id BINARY(16) NOT NULL,
    ADD UNIQUE KEY uk_resource_public_id (public_id);

ALTER TABLE allocation
    ADD COLUMN public_id BINARY(16) NULL,
    ADD COLUMN resource_id BIGINT NULL,
    ADD COLUMN returned_at DATETIME(6) NULL,
    ADD COLUMN consumed_at DATETIME(6) NULL;
UPDATE allocation a
JOIN resource_requirement q
  ON q.id = a.requirement_id AND q.organization_id = a.organization_id
SET a.resource_id = q.resource_id,
    a.public_id = UUID_TO_BIN(UUID(), 0);
ALTER TABLE allocation
    MODIFY COLUMN public_id BINARY(16) NOT NULL,
    MODIFY COLUMN resource_id BIGINT NOT NULL,
    ADD UNIQUE KEY uk_allocation_public_id (public_id),
    ADD UNIQUE KEY uk_allocation_org_id_resource (organization_id, id, resource_id),
    ADD CONSTRAINT fk_allocation_resource_scope
        FOREIGN KEY (resource_id, organization_id)
        REFERENCES resource (id, organization_id) ON DELETE RESTRICT;

CREATE TABLE stock_movement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    organization_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    allocation_id BIGINT NULL,
    actor_membership_id BIGINT NOT NULL,
    operation_public_id BINARY(16) NOT NULL,
    request_fingerprint BINARY(32) NOT NULL,
    kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    quantity INT NOT NULL,
    delta_total BIGINT NOT NULL,
    delta_issued BIGINT NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_movement_public_id (public_id),
    UNIQUE KEY uk_stock_movement_operation (organization_id, operation_public_id),
    UNIQUE KEY uk_stock_movement_allocation_kind (organization_id, allocation_id, kind),
    KEY ix_stock_movement_resource_page (organization_id, resource_id, occurred_at, id),
    CONSTRAINT fk_stock_movement_resource_scope
        FOREIGN KEY (resource_id, organization_id)
        REFERENCES resource (id, organization_id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_movement_allocation_resource_scope
        FOREIGN KEY (organization_id, allocation_id, resource_id)
        REFERENCES allocation (organization_id, id, resource_id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_movement_actor_membership_scope
        FOREIGN KEY (organization_id, actor_membership_id)
        REFERENCES membership (org_id, id) ON DELETE RESTRICT,
    CONSTRAINT ck_stock_movement_quantity CHECK (quantity BETWEEN 1 AND 1000000),
    CONSTRAINT ck_stock_movement_kind CHECK (kind IN ('INITIAL','RECEIPT','ADJUST','ISSUE','RETURN','CONSUME')),
    CONSTRAINT ck_stock_movement_deltas CHECK (
        (kind IN ('INITIAL','RECEIPT') AND allocation_id IS NULL
            AND delta_total = quantity AND delta_issued = 0)
        OR (kind = 'ADJUST' AND allocation_id IS NULL
            AND delta_total <> 0 AND ABS(delta_total) = quantity AND delta_issued = 0)
        OR (kind = 'ISSUE' AND allocation_id IS NOT NULL
            AND delta_total = 0 AND delta_issued = quantity)
        OR (kind = 'RETURN' AND allocation_id IS NOT NULL
            AND delta_total = 0 AND delta_issued = -quantity)
        OR (kind = 'CONSUME' AND allocation_id IS NOT NULL
            AND delta_total = -quantity AND delta_issued = 0)
    )
) ENGINE=InnoDB;

CREATE TABLE idempotency_key (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    route_fingerprint VARCHAR(160) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    key_hash BINARY(32) NOT NULL,
    body_hash BINARY(32) NOT NULL,
    state VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    operation_public_id BINARY(16) NOT NULL,
    http_status SMALLINT NOT NULL,
    response_json JSON NOT NULL,
    response_etag VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotency_request (organization_id, actor_user_id, route_fingerprint, key_hash),
    UNIQUE KEY uk_idempotency_operation (organization_id, operation_public_id),
    KEY ix_idempotency_expiry (expires_at, id),
    CONSTRAINT fk_idempotency_actor_membership_scope
        FOREIGN KEY (organization_id, actor_user_id)
        REFERENCES membership (org_id, user_id) ON DELETE RESTRICT,
    CONSTRAINT ck_idempotency_route CHECK (CHAR_LENGTH(route_fingerprint) BETWEEN 1 AND 160),
    CONSTRAINT ck_idempotency_state CHECK (state IN ('PROCESSING','COMPLETED')),
    CONSTRAINT ck_idempotency_status CHECK (http_status BETWEEN 200 AND 299),
    CONSTRAINT ck_idempotency_expiry CHECK (expires_at > created_at)
) ENGINE=InnoDB;

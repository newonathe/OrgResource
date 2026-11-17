-- Fail closed instead of copying any legacy plaintext contact or consent into a new schema.
DELIMITER $$
CREATE PROCEDURE orgresource_c3_v4_assert_no_legacy_contact()
BEGIN
    IF EXISTS (SELECT 1 FROM allocation WHERE phone_number IS NOT NULL OR sms_consent = TRUE LIMIT 1) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'LEGACY_CONTACT_DATA_REQUIRES_EXPLICIT_SECURE_MIGRATION';
    END IF;
END$$
DELIMITER ;

CALL orgresource_c3_v4_assert_no_legacy_contact();
DROP PROCEDURE orgresource_c3_v4_assert_no_legacy_contact;

ALTER TABLE allocation
    ADD COLUMN phone_ciphertext VARBINARY(512) NULL,
    ADD COLUMN phone_nonce BINARY(12) NULL,
    ADD COLUMN phone_key_id VARCHAR(64) NULL,
    ADD COLUMN cancellation_reason VARCHAR(24) NULL,
    ADD COLUMN reminder_claimed_at DATETIME(6) NULL,
    ADD COLUMN reminder_outcome VARCHAR(24) NOT NULL DEFAULT 'NONE',
    ADD COLUMN reminder_http_status INT NULL,
    ADD COLUMN provider_message_sid VARCHAR(64) NULL,
    DROP COLUMN phone_number,
    ADD CONSTRAINT ck_allocation_contact_envelope CHECK (
        (sms_consent = FALSE AND phone_ciphertext IS NULL AND phone_nonce IS NULL AND phone_key_id IS NULL)
        OR
        (sms_consent = TRUE AND phone_ciphertext IS NOT NULL AND phone_nonce IS NOT NULL AND phone_key_id IS NOT NULL)
    ),
    ADD CONSTRAINT ck_allocation_reminder_outcome CHECK (
        reminder_outcome IN ('NONE','CLAIMED','SUPPRESSED','SIMULATED','PROVIDER_ACCEPTED','REJECTED','UNKNOWN')
    ),
    ADD CONSTRAINT ck_allocation_cancellation_reason CHECK (
        cancellation_reason IS NULL OR cancellation_reason IN ('USER_REQUEST','EXPIRED')
    ),
    ADD KEY ix_allocation_reminder (status, reminder_outcome, reminder_claimed_at, id);

ALTER TABLE resource
    ADD COLUMN image_bytes MEDIUMBLOB NULL,
    ADD COLUMN image_content_type VARCHAR(16) NULL,
    ADD CONSTRAINT ck_resource_image_content_type CHECK (
        (image_bytes IS NULL AND image_content_type IS NULL)
        OR
        (image_bytes IS NOT NULL AND image_content_type IN ('image/png','image/jpeg'))
    );

CREATE INDEX ix_project_org_end ON project (organization_id, end_at, id);

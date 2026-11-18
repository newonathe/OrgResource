ALTER TABLE allocation
    DROP CHECK ck_allocation_reminder_outcome,
    ADD CONSTRAINT ck_allocation_reminder_outcome CHECK (
        reminder_outcome IN ('NONE','CLAIMED','DISPATCHING','KEY_UNAVAILABLE','INVALID_CONTACT',
            'SUPPRESSED','SIMULATED','PROVIDER_ACCEPTED','REJECTED','UNKNOWN')
    );

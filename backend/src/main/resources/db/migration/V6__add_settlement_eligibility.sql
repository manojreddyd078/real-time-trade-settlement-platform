ALTER TABLE instruments
    ADD COLUMN settlement_supported BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE trades
    ADD COLUMN eligibility_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN eligibility_rejection_reason VARCHAR(1000),
    ADD COLUMN eligibility_checked_at TIMESTAMPTZ;

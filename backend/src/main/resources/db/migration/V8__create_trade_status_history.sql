CREATE TABLE trade_status_history (
    id BIGSERIAL PRIMARY KEY,
    trade_id UUID NOT NULL REFERENCES trades(id),
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    reason VARCHAR(1000),
    correlation_id VARCHAR(100),
    changed_by VARCHAR(100) NOT NULL DEFAULT 'system',
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trade_status_history_trade_changed
    ON trade_status_history(trade_id, changed_at ASC, id ASC);

ALTER TABLE trades DROP CONSTRAINT IF EXISTS trades_status_check;
UPDATE trades SET status = 'RECEIVED' WHERE status = 'VALIDATING';
UPDATE trades SET status = 'ELIGIBLE' WHERE status = 'READY_FOR_SETTLEMENT';
UPDATE trades SET status = 'REJECTED' WHERE status = 'CANCELLED';
ALTER TABLE trades ADD CONSTRAINT chk_trade_status CHECK (status IN (
    'RECEIVED', 'VALIDATED', 'ENRICHED', 'ELIGIBLE',
    'SETTLEMENT_PENDING', 'SETTLED', 'FAILED', 'REJECTED'
));

INSERT INTO trade_status_history (trade_id, from_status, to_status, reason, changed_by, changed_at)
SELECT id, NULL, status, 'Initial lifecycle status migrated from existing trade', 'migration', created_at
FROM trades;

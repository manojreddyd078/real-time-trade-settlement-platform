CREATE TABLE processed_events (
    event_id UUID NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    trade_id UUID,
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    received_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ,
    PRIMARY KEY (event_id, consumer_name)
);

CREATE INDEX idx_processed_events_trade_id ON processed_events(trade_id);
CREATE INDEX idx_processed_events_received_at ON processed_events(received_at DESC);

ALTER TABLE trades ADD COLUMN business_key VARCHAR(128);
UPDATE trades SET business_key = 'LEGACY:' || id::text WHERE business_key IS NULL;
ALTER TABLE trades ALTER COLUMN business_key SET NOT NULL;
ALTER TABLE trades ADD CONSTRAINT uk_trades_business_key UNIQUE (business_key);

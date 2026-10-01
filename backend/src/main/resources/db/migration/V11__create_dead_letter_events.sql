CREATE TABLE dead_letter_events (
    id UUID PRIMARY KEY,
    event_id UUID,
    trade_id UUID,
    trade_reference VARCHAR(64),
    event_type VARCHAR(50),
    correlation_id VARCHAR(100),
    original_topic VARCHAR(100) NOT NULL,
    original_partition INTEGER NOT NULL,
    original_offset BIGINT NOT NULL,
    original_consumer_group VARCHAR(100),
    original_timestamp BIGINT,
    original_key VARCHAR(200),
    dlq_topic VARCHAR(100) NOT NULL,
    dlq_partition INTEGER NOT NULL,
    dlq_offset BIGINT NOT NULL,
    failure_class VARCHAR(300),
    failure_reason VARCHAR(2000) NOT NULL,
    payload_json TEXT,
    headers_json TEXT,
    received_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_dead_letter_original_record UNIQUE (
        original_topic, original_partition, original_offset, original_consumer_group
    )
);

CREATE INDEX idx_dead_letter_events_trade ON dead_letter_events(trade_id, received_at DESC);
CREATE INDEX idx_dead_letter_events_received ON dead_letter_events(received_at DESC);

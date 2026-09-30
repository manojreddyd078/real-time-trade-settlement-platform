CREATE TABLE event_retry_attempts (
    event_id UUID NOT NULL,
    retry_key VARCHAR(200) NOT NULL,
    trade_id UUID,
    topic VARCHAR(100) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_failure VARCHAR(1000),
    first_attempt_at TIMESTAMPTZ NOT NULL,
    last_attempt_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (event_id, retry_key)
);

CREATE INDEX idx_event_retry_attempts_trade ON event_retry_attempts(trade_id, last_attempt_at DESC);

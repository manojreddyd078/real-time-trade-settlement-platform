ALTER TABLE processed_events ADD COLUMN correlation_id VARCHAR(100);
ALTER TABLE processed_events ADD COLUMN event_occurred_at TIMESTAMPTZ;
CREATE INDEX idx_processed_events_correlation ON processed_events(correlation_id);

ALTER TABLE settlements ADD COLUMN correlation_id VARCHAR(100);
CREATE INDEX idx_settlements_correlation ON settlements(correlation_id);

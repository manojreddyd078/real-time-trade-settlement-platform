CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL UNIQUE REFERENCES trades(id),
    trade_reference VARCHAR(64) NOT NULL,
    instruction_reference VARCHAR(80) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(27, 8) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    settled_at TIMESTAMPTZ,
    failure_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_settlements_status_requested ON settlements(status, requested_at DESC);

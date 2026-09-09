CREATE TABLE trades (
    id UUID PRIMARY KEY,
    trade_reference VARCHAR(64) NOT NULL UNIQUE,
    external_reference VARCHAR(100),
    trade_type VARCHAR(16) NOT NULL CHECK (trade_type IN ('BUY', 'SELL')),
    status VARCHAR(32) NOT NULL CHECK (status IN (
        'RECEIVED', 'VALIDATING', 'VALIDATED', 'ENRICHED',
        'READY_FOR_SETTLEMENT', 'SETTLEMENT_PENDING', 'SETTLED',
        'FAILED', 'CANCELLED'
    )),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    buyer_counterparty_id UUID NOT NULL REFERENCES counterparties(id),
    seller_counterparty_id UUID NOT NULL REFERENCES counterparties(id),
    quantity NUMERIC(19, 4) NOT NULL CHECK (quantity > 0),
    price NUMERIC(19, 8) NOT NULL CHECK (price >= 0),
    currency_code CHAR(3) NOT NULL REFERENCES currencies(code),
    trade_date DATE NOT NULL,
    settlement_date DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_trade_counterparties_distinct
        CHECK (buyer_counterparty_id <> seller_counterparty_id),
    CONSTRAINT chk_settlement_not_before_trade
        CHECK (settlement_date >= trade_date)
);

CREATE INDEX idx_trades_status ON trades(status);
CREATE INDEX idx_trades_settlement_date ON trades(settlement_date);
CREATE INDEX idx_trades_instrument_id ON trades(instrument_id);
CREATE INDEX idx_trades_buyer_counterparty ON trades(buyer_counterparty_id);
CREATE INDEX idx_trades_seller_counterparty ON trades(seller_counterparty_id);

ALTER TABLE trades
    ADD COLUMN instrument_code VARCHAR(50),
    ADD COLUMN instrument_name VARCHAR(200),
    ADD COLUMN buyer_counterparty_code VARCHAR(50),
    ADD COLUMN buyer_counterparty_name VARCHAR(200),
    ADD COLUMN seller_counterparty_code VARCHAR(50),
    ADD COLUMN seller_counterparty_name VARCHAR(200),
    ADD COLUMN enriched_at TIMESTAMPTZ;

-- Align remaining fixed-length code columns with the current String mappings.
-- Existing values, nullability, keys, and indexes are preserved by ALTER TYPE.
ALTER TABLE settlement.counterparties
    ALTER COLUMN lei TYPE VARCHAR(20)
    USING lei::text::VARCHAR(20);

ALTER TABLE settlement.instruments
    ALTER COLUMN isin TYPE VARCHAR(12)
    USING isin::text::VARCHAR(12),
    ALTER COLUMN currency_code TYPE VARCHAR(3)
    USING currency_code::text::VARCHAR(3);

ALTER TABLE settlement.trades
    ALTER COLUMN currency_code TYPE VARCHAR(3)
    USING currency_code::text::VARCHAR(3);

ALTER TABLE settlement.settlements
    ALTER COLUMN currency_code TYPE VARCHAR(3)
    USING currency_code::text::VARCHAR(3);

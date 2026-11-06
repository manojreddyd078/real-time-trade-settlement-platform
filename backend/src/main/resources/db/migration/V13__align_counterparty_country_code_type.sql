-- Align the reference-data column with Counterparty.countryCode's VARCHAR(2) mapping.
-- The cast preserves existing country codes while removing CHAR padding semantics.
ALTER TABLE settlement.counterparties
    ALTER COLUMN country_code TYPE VARCHAR(2)
    USING country_code::text::VARCHAR(2);

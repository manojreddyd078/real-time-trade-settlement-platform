CREATE TABLE currencies (
    code CHAR(3) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    minor_units SMALLINT NOT NULL CHECK (minor_units BETWEEN 0 AND 8),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE counterparties (
    id UUID PRIMARY KEY,
    counterparty_code VARCHAR(50) NOT NULL UNIQUE,
    legal_name VARCHAR(200) NOT NULL,
    lei CHAR(20) UNIQUE,
    country_code CHAR(2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE instruments (
    id UUID PRIMARY KEY,
    instrument_code VARCHAR(50) NOT NULL UNIQUE,
    isin CHAR(12) UNIQUE,
    name VARCHAR(200) NOT NULL,
    instrument_class VARCHAR(30) NOT NULL,
    currency_code CHAR(3) NOT NULL REFERENCES currencies(code),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE settlement_calendars (
    id BIGSERIAL PRIMARY KEY,
    market_code VARCHAR(20) NOT NULL,
    calendar_date DATE NOT NULL,
    business_day BOOLEAN NOT NULL,
    description VARCHAR(200),
    UNIQUE (market_code, calendar_date)
);

INSERT INTO currencies (code, name, minor_units) VALUES
    ('USD', 'US Dollar', 2),
    ('EUR', 'Euro', 2),
    ('GBP', 'Pound Sterling', 2),
    ('JPY', 'Japanese Yen', 0)
ON CONFLICT (code) DO NOTHING;

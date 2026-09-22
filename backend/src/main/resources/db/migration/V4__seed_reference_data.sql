INSERT INTO instruments (id, instrument_code, isin, name, instrument_class, currency_code) VALUES
    ('11111111-1111-4111-8111-111111111111', 'UST-10Y', 'US91282CJL6', 'US Treasury Note 10Y', 'BOND', 'USD'),
    ('22222222-2222-4222-8222-222222222222', 'AAPL', 'US0378331005', 'Apple Inc.', 'EQUITY', 'USD'),
    ('33333333-3333-4333-8333-333333333333', 'MSFT', 'US5949181045', 'Microsoft Corporation', 'EQUITY', 'USD')
ON CONFLICT (id) DO NOTHING;

INSERT INTO counterparties (id, counterparty_code, legal_name, lei, country_code) VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'NORTHSTAR', 'Northstar Capital LLC', '549300NORTHSTAR00001', 'US'),
    ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'MERIDIAN', 'Meridian Securities Ltd', '549300MERIDIAN000002', 'GB'),
    ('cccccccc-cccc-4ccc-8ccc-cccccccccccc', 'APEX', 'Apex Markets Inc.', '549300APEXMARKET0003', 'US')
ON CONFLICT (id) DO NOTHING;

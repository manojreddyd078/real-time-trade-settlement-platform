from pyspark.sql import functions as F
from schema import TRADE_FIELDS

SUPPORTED_CURRENCIES = ('USD', 'EUR', 'GBP')

def normalize_trade_fields(trades):
    result = trades.withColumn('raw_record', F.to_json(F.struct(*[F.col(name) for name in TRADE_FIELDS])))
    for name in TRADE_FIELDS:
        value = F.trim(F.col(name).cast('string'))
        result = result.withColumn(name, F.when(value == '', None).otherwise(value))
    for name in ('trade_id', 'instrument_code', 'currency'):
        result = result.withColumn(name, F.upper(F.col(name)))
    for name in ('quantity', 'price'):
        result = result.withColumn(name, F.col(name).cast('decimal(18,4)'))
    for name in ('trade_date', 'settlement_date'):
        result = result.withColumn(name, F.to_date(name, 'yyyy-MM-dd'))
    return result

def derive_settlement_fields(trades):
    return (trades.withColumn('notional', F.col('quantity') * F.col('price'))
            .withColumn('settlement_amount', F.col('notional'))
            .withColumn('settlement_currency', F.col('currency'))
            .withColumn('days_to_settlement', F.datediff('settlement_date', 'trade_date'))
            .withColumn('same_day_settlement', F.col('days_to_settlement') == 0)
            .withColumn('settlement_class', F.when(F.col('days_to_settlement') == 0, 'SAME_DAY')
                        .when(F.col('days_to_settlement') > 0, 'FUTURE_DATED').otherwise('INVALID_DATE')))

def apply_business_rules(trades):
    checks = [(F.col('trade_id').isNull(), 'trade_id is required'),
        (F.col('instrument_code').isNull(), 'instrument_code is required'),
        (F.col('quantity').isNull() | (F.col('quantity') <= 0), 'quantity must be numeric and positive'),
        (F.col('price').isNull() | (F.col('price') < 0), 'price must be numeric and non-negative'),
        (F.col('currency').isNull() | ~F.col('currency').isin(*SUPPORTED_CURRENCIES), 'unsupported or missing currency'),
        (F.col('trade_date').isNull(), 'invalid or missing trade_date'),
        (F.col('settlement_date').isNull(), 'invalid or missing settlement_date'),
        (F.col('settlement_date') < F.col('trade_date'), 'settlement_date precedes trade_date')]
    if '_corrupt_record' in trades.columns:
        checks.append((F.col('_corrupt_record').isNotNull(), 'malformed CSV record'))
    return (trades.withColumn('validation_errors', F.filter(F.array(*[F.when(c, F.lit(r)) for c, r in checks]), lambda r: r.isNotNull()))
            .withColumn('valid', F.size('validation_errors') == 0))

def validate_transformed_output(trades):
    required = ['trade_id', 'instrument_code', 'quantity', 'price', 'currency', 'trade_date', 'settlement_date', 'settlement_amount', 'settlement_currency']
    condition = F.col('valid') & F.col('notional').isNotNull() & F.col('settlement_amount').isNotNull()
    for name in required:
        condition = condition & F.col(name).isNotNull()
    return trades.filter(condition)


def _legacy_prepare_trades(trades):
    result = trades.withColumn('raw_record', F.to_json(F.struct(*[F.col(name) for name in TRADE_FIELDS])))
    for name in TRADE_FIELDS:
        value = F.trim(F.col(name).cast('string'))
        result = result.withColumn(name, F.when(value == '', None).otherwise(value))
    for name in ('trade_id', 'instrument_code', 'currency'):
        result = result.withColumn(name, F.upper(F.col(name)))
    for name in ('quantity', 'price'):
        result = result.withColumn(name, F.col(name).cast('decimal(18,4)'))
    for name in ('trade_date', 'settlement_date'):
        result = result.withColumn(name, F.to_date(F.col(name), 'yyyy-MM-dd'))
    checks = [
        (F.col('trade_id').isNull(), 'trade_id is required'),
        (F.col('instrument_code').isNull(), 'instrument_code is required'),
        (F.col('quantity').isNull() | (F.col('quantity') <= 0), 'quantity must be numeric and positive'),
        (F.col('price').isNull() | (F.col('price') < 0), 'price must be numeric and non-negative'),
        (F.col('currency').isNull() | ~F.col('currency').isin('USD', 'EUR', 'GBP'), 'unsupported or missing currency'),
        (F.col('trade_date').isNull(), 'invalid or missing trade_date'),
        (F.col('settlement_date').isNull(), 'invalid or missing settlement_date'),
        (F.col('settlement_date') < F.col('trade_date'), 'settlement_date precedes trade_date'),
    ]
    if '_corrupt_record' in result.columns:
        checks.append((F.col('_corrupt_record').isNotNull(), 'malformed CSV record'))
    return (result.withColumn('validation_errors', F.filter(
                F.array(*[F.when(condition, F.lit(reason)) for condition, reason in checks]),
                lambda reason: reason.isNotNull()))
            .withColumn('valid', F.size('validation_errors') == 0)
            .withColumn('notional', F.col('quantity') * F.col('price')))


def prepare_trades(trades):
    """Run normalization, settlement derivation, and business rules."""
    return apply_business_rules(derive_settlement_fields(normalize_trade_fields(trades)))

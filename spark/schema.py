from pyspark.sql.types import StructType, StructField, StringType

TRADE_FIELDS = ('trade_id', 'instrument_code', 'quantity', 'price', 'currency', 'trade_date', 'settlement_date')
TRADE_INPUT_SCHEMA = StructType([StructField(name, StringType(), True) for name in TRADE_FIELDS]
                              + [StructField('_corrupt_record', StringType(), True)])

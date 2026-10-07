from pyspark.sql import functions as F


def prepare_trades(trades):
    return (trades
            .withColumn("trade_date", F.to_date("trade_date"))
            .withColumn("settlement_date", F.to_date("settlement_date"))
            .withColumn("notional", F.col("quantity") * F.col("price"))
            .withColumn("valid", (
                F.col("trade_id").isNotNull()
                & (F.col("quantity") > 0)
                & (F.col("price") >= 0)
                & F.col("currency").isin("USD", "EUR", "GBP")
                & (F.col("settlement_date") >= F.col("trade_date"))
            )))

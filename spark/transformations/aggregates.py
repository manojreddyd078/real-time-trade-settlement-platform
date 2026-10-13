"""Reusable settlement dashboard aggregations."""

from pyspark.sql import functions as F


def aggregate_trades(trades):
    """Aggregate volume and settlement outcomes by instrument and currency."""
    status = F.upper(F.coalesce(F.col("status"), F.lit(""))) if "status" in trades.columns else F.lit("")
    return (
        trades.withColumn("_status", status)
        .groupBy("instrument_code", "currency")
        .agg(
            F.count("*").alias("trade_count"),
            F.coalesce(F.sum("quantity"), F.lit(0)).alias("trade_volume"),
            F.coalesce(F.sum("notional"), F.lit(0)).alias("notional_value"),
            F.sum(F.when(F.col("_status").isin("SETTLED", "SETTLEMENT_COMPLETE"), 1).otherwise(0)).alias("settlement_count"),
            F.sum(F.when(F.col("_status").isin("FAILED", "REJECTED"), 1).otherwise(0)).alias("failed_rejected_count"),
        )
        .orderBy("instrument_code", "currency")
    )


def summarize_settlement_metrics(trades):
    """Return dashboard-level counts for all, settled, pending, failed, rejected."""
    status = F.upper(F.coalesce(F.col("status"), F.lit(""))) if "status" in trades.columns else F.lit("")
    return trades.select(
        F.count("*").alias("trade_count"),
        F.sum(F.when(status.isin("SETTLED", "SETTLEMENT_COMPLETE"), 1).otherwise(0)).alias("settled_count"),
        F.sum(F.when(status.isin("FAILED", "REJECTED"), 1).otherwise(0)).alias("failed_rejected_count"),
        F.sum(F.when(status.isin("SETTLEMENT_PENDING", "PENDING"), 1).otherwise(0)).alias("pending_count"),
    )

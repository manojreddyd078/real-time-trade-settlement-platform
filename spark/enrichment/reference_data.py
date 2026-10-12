"""Reference-data joins and enrichment validation for Spark trade rows."""

from pyspark.sql import functions as F


def _normalized_reference(frame, key, columns):
    result = frame
    for column in [key, *columns]:
        if column in result.columns:
            result = result.withColumn(
                column, F.upper(F.trim(F.col(column).cast("string")))
            )
    return result


def enrich_trades(trades, instruments, counterparties=None):
    """Left-join instrument and optional counterparty reference data."""
    instrument_ref = _normalized_reference(
        instruments, "instrument_code", ["instrument_name"]
    ).select("instrument_code", "instrument_name")
    result = trades.join(instrument_ref, "instrument_code", "left")
    result = result.withColumn("instrument_matched", F.col("instrument_name").isNotNull())

    id_columns = ["buyer_counterparty_id", "seller_counterparty_id"]
    if counterparties is None or not any(column in trades.columns for column in id_columns):
        return (
            result.withColumn("buyer_counterparty_name", F.lit(None).cast("string"))
            .withColumn("seller_counterparty_name", F.lit(None).cast("string"))
            .withColumn("buyer_counterparty_matched", F.lit(None).cast("boolean"))
            .withColumn("seller_counterparty_matched", F.lit(None).cast("boolean"))
            .withColumn(
                "enrichment_errors",
                F.array_remove(
                    F.array(
                        F.when(~F.col("instrument_matched"), F.lit("instrument reference not found"))
                    ),
                    None,
                ),
            )
        )

    counterparty_ref = _normalized_reference(
        counterparties, "counterparty_id", ["counterparty_name"]
    ).select("counterparty_id", "counterparty_name")
    for side in ("buyer", "seller"):
        id_column = f"{side}_counterparty_id"
        alias = f"{side}_counterparty_ref"
        result = result.join(
            counterparty_ref.alias(alias),
            F.col(id_column) == F.col(f"{alias}.counterparty_id"),
            "left",
        ).withColumn(
            f"{side}_counterparty_name", F.col(f"{alias}.counterparty_name")
        ).withColumn(
            f"{side}_counterparty_matched", F.col(f"{alias}.counterparty_name").isNotNull()
        ).drop(f"{alias}.counterparty_id", f"{alias}.counterparty_name")

    errors = F.array(
        F.when(~F.col("instrument_matched"), F.lit("instrument reference not found")),
        F.when(~F.col("buyer_counterparty_matched"), F.lit("buyer counterparty reference not found")),
        F.when(~F.col("seller_counterparty_matched"), F.lit("seller counterparty reference not found")),
    )
    return result.withColumn("enrichment_errors", F.array_remove(errors, None))


def validate_enriched_output(enriched):
    """Return enriched rows that passed trade and reference-data validation."""
    condition = (
        F.col("valid")
        & F.col("instrument_matched")
        & F.coalesce(F.col("buyer_counterparty_matched"), F.lit(True))
        & F.coalesce(F.col("seller_counterparty_matched"), F.lit(True))
        & (F.size(F.col("enrichment_errors")) == 0)
    )
    return enriched.filter(condition)

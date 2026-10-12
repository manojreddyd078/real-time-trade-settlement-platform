"""Run trade normalization, reference-data joins, and enrichment validation."""

import argparse
from pathlib import Path

from pyspark.sql import functions as F

from config import create_session
from enrichment.reference_data import enrich_trades, validate_enriched_output
from schema import RAW_TRADE_SCHEMA
from transformations.trades import prepare_trades, validate_transformed_output


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", default="data/samples/dirty_trades.csv")
    parser.add_argument("--instruments", default="data/samples/instruments.csv")
    parser.add_argument("--counterparties", default="data/samples/counterparties.csv")
    parser.add_argument("--output", default="output/enriched")
    args = parser.parse_args()

    spark = create_session("trade-reference-enrichment")
    try:
        trades = prepare_trades(
            spark.read.option("header", True).schema(RAW_TRADE_SCHEMA).csv(args.input)
        )
        instruments = spark.read.option("header", True).csv(args.instruments)
        counterparties = spark.read.option("header", True).csv(args.counterparties)
        candidates = validate_transformed_output(trades)
        enriched = enrich_trades(candidates, instruments, counterparties)
        accepted = validate_enriched_output(enriched)
        unmatched = enriched.filter(
            (~F.col("instrument_matched")) | (F.size("enrichment_errors") > 0)
        )
        output = Path(args.output)
        accepted.write.mode("overwrite").json(str(output / "accepted"))
        unmatched.write.mode("overwrite").json(str(output / "unmatched"))
        print(
            {
                "candidates": candidates.count(),
                "accepted": accepted.count(),
                "unmatched_reference": unmatched.count(),
            }
        )
    finally:
        spark.stop()


if __name__ == "__main__":
    main()

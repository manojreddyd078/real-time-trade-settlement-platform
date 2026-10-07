"""Run with: python -m jobs.sample_trade_batch (from spark/)."""
import argparse
import json
from pathlib import Path
from config import create_session
from enrichment.reference_data import enrich_trades
from transformations.trades import prepare_trades

ROOT = Path(__file__).resolve().parents[1]


def run():
    parser = argparse.ArgumentParser(description="Local sample trade batch")
    parser.add_argument("--data-dir", type=Path, default=ROOT / "data" / "samples")
    parser.add_argument("--output", type=Path, default=ROOT / "output" / "summary.json")
    args = parser.parse_args()
    spark = create_session()
    spark.sparkContext.setLogLevel("ERROR")
    try:
        schema = "trade_id string, instrument_code string, quantity decimal(18,4), price decimal(18,4), currency string, trade_date string, settlement_date string"
        trades = spark.read.option("header", True).schema(schema).csv(str(args.data_dir / "trades.csv"))
        instruments = spark.read.option("header", True).schema("instrument_code string, instrument_name string").csv(str(args.data_dir / "instruments.csv"))
        enriched = enrich_trades(prepare_trades(trades), instruments).cache()
        summary = {
            "spark_version": spark.version,
            "total_trades": enriched.count(),
            "valid_trades": enriched.filter("valid = true").count(),
            "invalid_trades": enriched.filter("valid = false OR valid IS NULL").count(),
            "missing_instruments": enriched.filter("instrument_name IS NULL").count(),
        }
        enriched.orderBy("trade_id").show(truncate=False)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(summary, indent=2))
    finally:
        spark.stop()


if __name__ == "__main__":
    run()

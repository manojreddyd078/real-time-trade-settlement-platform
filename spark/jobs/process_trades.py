"""Local CSV cleaning with accepted/rejected JSONL output."""
import argparse
import json
from pathlib import Path
from config import create_session
from schema import TRADE_FIELDS, TRADE_INPUT_SCHEMA
from transformations.trades import prepare_trades, validate_transformed_output

ROOT = Path(__file__).resolve().parents[1]


def read_trades(spark, path):
    return (spark.read.schema(TRADE_INPUT_SCHEMA).option('header', True)
            .option('enforceSchema', False).option('mode', 'PERMISSIVE')
            .option('columnNameOfCorruptRecord', '_corrupt_record').csv(str(path)))


def write_rows(frame, path):
    # Stream JSON from Spark to a local file without collecting the whole dataset.
    with path.open('w', encoding='utf-8') as output:
        for row in frame.toJSON().toLocalIterator():
            output.write(row + '\n')


def run():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--input', type=Path, default=ROOT / 'data/samples/dirty_trades.csv')
    parser.add_argument('--output-dir', type=Path, default=ROOT / 'output/processed')
    args = parser.parse_args()
    if not args.input.exists():
        parser.error('Input dataset does not exist')
    spark = create_session()
    spark.sparkContext.setLogLevel('ERROR')
    processed = None
    try:
        processed = prepare_trades(read_trades(spark, args.input)).cache()
        accepted = validate_transformed_output(processed)
        rejected = processed.filter('NOT valid')
        counts = {'total': processed.count(), 'accepted': accepted.count(), 'rejected': rejected.count()}
        args.output_dir.mkdir(parents=True, exist_ok=True)
        write_rows(accepted.select(*TRADE_FIELDS, 'notional', 'settlement_amount',
                                    'settlement_currency', 'days_to_settlement',
                                    'same_day_settlement', 'settlement_class'),
                   args.output_dir / 'accepted.jsonl')
        write_rows(rejected, args.output_dir / 'rejected.jsonl')
        (args.output_dir / 'summary.json').write_text(json.dumps(counts, indent=2) + '\n', encoding='utf-8')
        print(json.dumps(counts, indent=2))
    finally:
        if processed is not None:
            processed.unpersist()
        spark.stop()


if __name__ == '__main__':
    run()

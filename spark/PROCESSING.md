# Trade dataset processing

From `spark/`, set JAVA_HOME to JDK 11 or 17 and run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-11.0.32.101-hotspot'
$env:SPARK_LOCAL_IP = '127.0.0.1'
.venv/Scripts/python.exe -m jobs.process_trades
```

Use `--input <csv-file-or-directory>` and `--output-dir <directory>` for custom
datasets. CSV headers must follow `schema.TRADE_FIELDS` in order. Raw fields are
nullable strings so malformed economics and dates remain available for auditing.
Cleaning trims whitespace, maps blanks to null, uppercases identifiers and currency,
casts quantity/price to decimal(18,4), and parses ISO dates. Required fields are
never filled with invented values. Supported currencies: USD/EUR/GBP. Quantity
must be positive, price non-negative, and settlement date cannot precede trade date.

Accepted records are written to `output/processed/accepted.jsonl`. Invalid rows
are excluded and retained in `rejected.jsonl` with raw values and rejection reasons.
Counts are written to `summary.json`. These three files are overwritten each run.
The dirty sample contains seven rows: two accepted and five rejected.

JSONL output streams through the driver for local Windows portability. Production
distributed exports should use partitioned Spark writers. Reference existence and
business duplicate detection are outside this cleaning job.

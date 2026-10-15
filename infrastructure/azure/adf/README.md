# Azure Data Factory workflow

`pipeline-trade-processing.json` defines the cloud workflow:

1. ADF receives a Blob input filename.
2. A Databricks Spark notebook runs the reusable PySpark trade processing job.
3. Processed JSON output is copied into Azure SQL.

Create linked services named `ls_azure_databricks`, `ds_processed_trade_blob`,
and `ds_trade_settlement_sql` in the target Data Factory. Store secrets in
Key Vault-backed parameters; do not put connection strings in this repository.
Use a Blob `BlobEventsTrigger` for production and pass the event file path as
the `inputFile` pipeline parameter.

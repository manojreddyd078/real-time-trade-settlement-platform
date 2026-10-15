# Azure deployment

The Bicep template provisions a private Blob Storage account with `input-trades`
and `processed-trades` containers, plus an Azure SQL logical server and the
`trade_settlement` database. It does not contain credentials or upload data.

```powershell
az group create --name rg-trade-settlement-dev --location eastus
az deployment group create --resource-group rg-trade-settlement-dev `
  --template-file infrastructure/azure/main.bicep `
  --parameters environmentName=trade-settlement sqlAdminLogin=$env:AZURE_SQL_ADMIN `
  sqlAdminPassword=$env:AZURE_SQL_ADMIN_PASSWORD
```

Upload a trade file only after deployment and access review:

```powershell
az storage blob upload --account-name <storage-account> --container-name input-trades `
  --name trades.csv --file .\spark\data\samples\trades.csv --auth-mode login
```

For Spring Boot, set `SPRING_PROFILES_ACTIVE=azure` and provide
`AZURE_SQL_JDBC_URL`, `AZURE_SQL_USERNAME`, `AZURE_SQL_PASSWORD`, and
`KAFKA_BOOTSTRAP_SERVERS`. The Azure profile runs the existing Flyway migrations,
which create the trade, settlement-status, audit, idempotency, and reference
tables. Verify connectivity with `GET /actuator/health` and inspect the Flyway
startup logs; no production credentials belong in source control.

## Event-driven processing

The companion workflow files in `adf/` and `function/` connect Blob events to
the processing pipeline. Deploy the Function first, configure its Blob trigger,
then import `adf/pipeline-trade-processing.json` and create the three linked
services described in `adf/README.md`. Test with a non-sensitive sample file in
`input-trades`; confirm a successful Spark activity, an output file in the
processed container, and inserted rows in Azure SQL before enabling production
event triggers.

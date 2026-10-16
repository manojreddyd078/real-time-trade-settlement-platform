param(
  [Parameter(Mandatory = $true)][string]$StorageAccount,
  [Parameter(Mandatory = $true)][string]$ResourceGroup,
  [Parameter(Mandatory = $true)][string]$DataFactory,
  [Parameter(Mandatory = $true)][string]$PipelineName,
  [Parameter(Mandatory = $true)][string]$SqlServer,
  [Parameter(Mandatory = $true)][string]$SqlDatabase,
  [string]$InputFile = "trades.csv"
)

$ErrorActionPreference = 'Stop'
Write-Host "Uploading test input to Blob Storage..."
az storage blob upload --auth-mode login --account-name $StorageAccount --container-name input-trades --name $InputFile --file "spark/data/samples/$InputFile" --overwrite true | Out-Null

Write-Host "Starting Data Factory pipeline..."
$parameters = @{ inputFile = $InputFile } | ConvertTo-Json -Compress
$runId = az datafactory pipeline create-run --resource-group $ResourceGroup --factory-name $DataFactory --name $PipelineName --parameters $parameters --query runId -o tsv
az datafactory pipeline-run query-by-factory --resource-group $ResourceGroup --factory-name $DataFactory --run-id $runId --query "{status:status,runId:runId}" -o table

Write-Host "Checking Azure SQL connectivity..."
az sql db show --resource-group $ResourceGroup --server $SqlServer --name $SqlDatabase --query "{name:name,status:status}" -o table
Write-Host "Verify processed-trades output and row counts in Azure SQL before production use."

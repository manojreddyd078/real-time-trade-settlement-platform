param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome 'bin/java.exe'))) {
    throw 'Set JAVA_HOME to a JDK 11 or 17 installation or pass -JavaHome.'
}
$env:JAVA_HOME = $JavaHome
$env:SPARK_LOCAL_IP = '127.0.0.1'
$python = Join-Path $PSScriptRoot '.venv/Scripts/python.exe'
if (-not (Test-Path $python)) { throw 'Create .venv and install requirements.txt first.' }
$env:PYSPARK_PYTHON = $python
Push-Location $PSScriptRoot
try {
    & $python -m jobs.sample_trade_batch
    if ($LASTEXITCODE -ne 0) { throw 'Spark job failed.' }
} finally { Pop-Location }

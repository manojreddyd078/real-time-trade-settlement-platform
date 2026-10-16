# Cloud pipeline and application verification

## Pipeline

Run `verify-cloud-pipeline.ps1` with an authenticated Azure CLI session. The
script uploads a sample file, starts the ADF pipeline, reports the pipeline run
status, and confirms the SQL database is online. Then verify the processed Blob
output and compare its row count with the inserted Azure SQL rows. Test an
invalid file separately and confirm the pipeline fails with a visible activity
error while the original input remains available for replay.

## Application hosting

`app-service.bicep` provisions a Linux App Service for the Spring Boot container
and an Azure Static Web App for the React build. Pass a fully qualified backend
container image and parameterized SQL/CORS values during deployment. Store the
SQL password in Key Vault or a deployment secret, never in source control.

Set the frontend `VITE_API_BASE_URL` to the emitted backend URL, rebuild the
React application, and configure the backend `CORS_ALLOWED_ORIGINS` to the
frontend URL. Verify `/actuator/health`, load the frontend, submit a test trade,
and confirm the response and status history are returned from the deployed API.

No deployment is performed by this repository change.

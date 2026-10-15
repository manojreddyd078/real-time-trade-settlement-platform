# Trade file Azure Function

Deploy this function with the Python v2 Azure Functions runtime. Configure
`AzureWebJobsStorage` through the Function App connection settings and set
`ADF_PIPELINE_URL` to the authenticated ADF `createRun` endpoint. Managed
identity is recommended for calling ADF; the sample keeps the endpoint and
credentials outside source control.

The Blob trigger watches `input-trades/{name}`, forwards the filename to ADF,
and fails visibly when the pipeline cannot be started so the platform can retry.

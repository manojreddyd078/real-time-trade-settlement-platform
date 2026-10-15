import json
import logging
import os

import azure.functions as func
import requests

app = func.FunctionApp()


@app.blob_trigger(
    arg_name="input_blob",
    path="input-trades/{name}",
    connection="AzureWebJobsStorage",
)
def trade_file_trigger(input_blob: func.InputStream):
    """Start the ADF pipeline when a trade file lands in Blob Storage."""
    file_name = input_blob.name.rsplit("/", 1)[-1]
    pipeline_url = os.environ["ADF_PIPELINE_URL"]
    payload = {"inputFile": file_name}
    response = requests.post(
        pipeline_url,
        params={"api-version": "2018-06-01"},
        headers={"Content-Type": "application/json"},
        data=json.dumps(payload),
        timeout=30,
    )
    response.raise_for_status()
    logging.info("Started ADF trade pipeline for %s", file_name)

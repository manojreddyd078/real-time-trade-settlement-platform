"""Shared local Spark configuration."""
import os
import sys
from pyspark.sql import SparkSession


def create_session():
    os.environ.setdefault("PYSPARK_PYTHON", sys.executable)
    return (SparkSession.builder
            .appName("trade-settlement-local")
            .master(os.environ.get("SPARK_MASTER", "local[2]"))
            .config("spark.sql.shuffle.partitions", "2")
            .config("spark.sql.session.timeZone", "UTC")
            .config("spark.driver.bindAddress", "127.0.0.1")
            .config("spark.driver.host", "127.0.0.1")
            .getOrCreate())

import os
import unittest

from pyspark.sql import functions as F

from config import create_session
from transformations.aggregates import aggregate_trades, summarize_settlement_metrics


@unittest.skipIf(os.name == "nt", "Spark worker is unavailable in this Windows environment")
class AggregateTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.spark = create_session("aggregate-tests")

    @classmethod
    def tearDownClass(cls):
        cls.spark.stop()

    def test_grouped_volume_and_outcomes(self):
        rows = [("AAPL", "USD", 10, 100, "SETTLED"), ("AAPL", "USD", 5, 100, "REJECTED")]
        frame = self.spark.createDataFrame(rows, "instrument_code string, currency string, quantity int, notional int, status string")
        result = aggregate_trades(frame).first()
        self.assertEqual(result.trade_volume, 15)
        self.assertEqual(result.settlement_count, 1)
        self.assertEqual(result.failed_rejected_count, 1)

    def test_dashboard_summary(self):
        frame = self.spark.createDataFrame([("SETTLED",), ("FAILED",), ("PENDING",)], ["status"])
        result = summarize_settlement_metrics(frame).first()
        self.assertEqual((result.trade_count, result.settled_count, result.failed_rejected_count, result.pending_count), (3, 1, 1, 1))


if __name__ == "__main__":
    unittest.main()

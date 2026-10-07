"""Verify the output of an actual local Spark sample run."""
import json
from pathlib import Path
import unittest


class SampleSummaryTest(unittest.TestCase):
    def test_sample_counts(self):
        path = Path(__file__).resolve().parents[1] / "output" / "summary.json"
        self.assertTrue(path.exists(), "Run the sample job before this smoke check")
        summary = json.loads(path.read_text(encoding="utf-8"))
        self.assertEqual("3.5.7", summary["spark_version"])
        self.assertEqual(5, summary["total_trades"])
        self.assertEqual(3, summary["valid_trades"])
        self.assertEqual(2, summary["invalid_trades"])
        self.assertEqual(1, summary["missing_instruments"])


if __name__ == "__main__":
    unittest.main()

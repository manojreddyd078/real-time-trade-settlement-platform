import unittest
from decimal import Decimal
import os
import sys
from config import create_session
from transformations.trades import (apply_business_rules, derive_settlement_fields,
                                    normalize_trade_fields, validate_transformed_output)


@unittest.skipIf(os.name == 'nt', 'Spark Python worker is not reliable under the Windows Store Python alias; validate via jobs.process_trades')
class TradeTransformationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        os.environ['PYSPARK_PYTHON'] = sys.executable
        cls.spark = create_session()
        cls.spark.sparkContext.setLogLevel('ERROR')

    @classmethod
    def tearDownClass(cls):
        cls.spark.stop()

    def test_normalize_derive_and_validate_output(self):
        rows = [(' trd-1 ', ' aapl ', '10', '25.50', ' usd ', '2026-10-09', '2026-10-09')]
        raw = self.spark.createDataFrame(rows, ['trade_id', 'instrument_code', 'quantity', 'price', 'currency', 'trade_date', 'settlement_date'])
        transformed = apply_business_rules(derive_settlement_fields(normalize_trade_fields(raw)))
        row = transformed.first()
        self.assertEqual('TRD-1', row.trade_id)
        self.assertEqual('AAPL', row.instrument_code)
        self.assertEqual('USD', row.currency)
        self.assertEqual('SAME_DAY', row.settlement_class)
        self.assertEqual(0, row.days_to_settlement)
        self.assertEqual(Decimal('255.00000000'), row.settlement_amount)
        self.assertEqual(1, validate_transformed_output(transformed).count())

    def test_business_rules_reject_invalid_settlement_date(self):
        rows = [('TRD-2', 'AAPL', '10', '25', 'USD', '2026-10-09', '2026-10-08')]
        raw = self.spark.createDataFrame(rows, ['trade_id', 'instrument_code', 'quantity', 'price', 'currency', 'trade_date', 'settlement_date'])
        transformed = apply_business_rules(derive_settlement_fields(normalize_trade_fields(raw)))
        row = transformed.first()
        self.assertFalse(row.valid)
        self.assertIn('settlement_date precedes trade_date', row.validation_errors)
        self.assertEqual(0, validate_transformed_output(transformed).count())


if __name__ == '__main__':
    unittest.main()

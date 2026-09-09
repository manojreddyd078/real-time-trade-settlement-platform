package com.tradesettlement.entity;

public enum TradeStatus {
    RECEIVED,
    VALIDATING,
    VALIDATED,
    ENRICHED,
    READY_FOR_SETTLEMENT,
    SETTLEMENT_PENDING,
    SETTLED,
    FAILED,
    CANCELLED
}

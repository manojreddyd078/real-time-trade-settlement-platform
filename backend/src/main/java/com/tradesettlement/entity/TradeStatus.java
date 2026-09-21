package com.tradesettlement.entity;

public enum TradeStatus {
    RECEIVED,
    VALIDATING,
    VALIDATED,
    REJECTED,
    ENRICHED,
    READY_FOR_SETTLEMENT,
    SETTLEMENT_PENDING,
    SETTLED,
    FAILED,
    CANCELLED
}

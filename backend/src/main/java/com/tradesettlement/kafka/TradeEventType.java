package com.tradesettlement.kafka;

public enum TradeEventType {
    TRADE_ACCEPTED,
    VALIDATION_COMPLETED,
    VALIDATION_REJECTED,
    ENRICHMENT_COMPLETED,
    SETTLEMENT_REQUESTED,
    STATUS_CHANGED
}

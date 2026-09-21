package com.tradesettlement.exception;

import java.util.UUID;

public class TradeNotFoundException extends RuntimeException {

    public TradeNotFoundException(UUID tradeId) {
        super("Trade was not found: " + tradeId);
    }
}

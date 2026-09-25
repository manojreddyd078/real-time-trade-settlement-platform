package com.tradesettlement.exception;

import com.tradesettlement.entity.TradeStatus;

public class InvalidTradeStatusTransitionException extends RuntimeException {
    public InvalidTradeStatusTransitionException(TradeStatus from, TradeStatus to) {
        super("Invalid trade status transition: " + from + " -> " + to);
    }
}

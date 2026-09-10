package com.tradesettlement.exception;

public class DuplicateTradeException extends RuntimeException {
    public DuplicateTradeException(String tradeReference) {
        super("Trade reference already exists: " + tradeReference);
    }
}

package com.tradesettlement.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;

public class TradeSubmissionResponse {

    private final UUID id;
    private final String tradeReference;
    private final TradeStatus status;
    private final OffsetDateTime createdAt;

    public TradeSubmissionResponse(UUID id, String tradeReference, TradeStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.tradeReference = tradeReference;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static TradeSubmissionResponse from(Trade trade) {
        return new TradeSubmissionResponse(trade.getId(), trade.getTradeReference(), trade.getStatus(), trade.getCreatedAt());
    }

    public UUID getId() { return id; }
    public String getTradeReference() { return tradeReference; }
    public TradeStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

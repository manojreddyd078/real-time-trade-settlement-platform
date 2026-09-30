package com.tradesettlement.dto;

import java.util.List;
import java.util.UUID;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;

public class TradeLifecycleStatusResponse {
    private final UUID tradeId; private final String tradeReference; private final TradeStatus currentStatus;
    private final List<TradeStatusHistoryResponse> history;
    private final TradeRetryStatusResponse retry;
    public TradeLifecycleStatusResponse(Trade trade, List<TradeStatusHistoryResponse> history) {
        this(trade, history, TradeRetryStatusResponse.from(java.util.Collections.emptyList()));
    }
    public TradeLifecycleStatusResponse(Trade trade, List<TradeStatusHistoryResponse> history, TradeRetryStatusResponse retry) {
        this.tradeId = trade.getId(); this.tradeReference = trade.getTradeReference();
        this.currentStatus = trade.getStatus(); this.history = history; this.retry = retry;
    }
    public UUID getTradeId() { return tradeId; } public String getTradeReference() { return tradeReference; }
    public TradeStatus getCurrentStatus() { return currentStatus; } public List<TradeStatusHistoryResponse> getHistory() { return history; }
    public TradeRetryStatusResponse getRetry() { return retry; }
}

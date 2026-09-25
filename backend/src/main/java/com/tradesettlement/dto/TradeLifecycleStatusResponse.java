package com.tradesettlement.dto;

import java.util.List;
import java.util.UUID;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;

public class TradeLifecycleStatusResponse {
    private final UUID tradeId; private final String tradeReference; private final TradeStatus currentStatus;
    private final List<TradeStatusHistoryResponse> history;
    public TradeLifecycleStatusResponse(Trade trade, List<TradeStatusHistoryResponse> history) {
        this.tradeId = trade.getId(); this.tradeReference = trade.getTradeReference();
        this.currentStatus = trade.getStatus(); this.history = history;
    }
    public UUID getTradeId() { return tradeId; } public String getTradeReference() { return tradeReference; }
    public TradeStatus getCurrentStatus() { return currentStatus; } public List<TradeStatusHistoryResponse> getHistory() { return history; }
}

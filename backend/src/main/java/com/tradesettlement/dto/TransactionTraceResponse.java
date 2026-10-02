package com.tradesettlement.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.tradesettlement.entity.Trade;

public class TransactionTraceResponse {
    private final UUID tradeId; private final String tradeReference; private final String currentStatus;
    private final List<String> correlationIds; private final List<UUID> eventIds;
    private final OffsetDateTime startedAt; private final OffsetDateTime lastUpdatedAt;
    private final Long processingDurationMs; private final List<TransactionTraceEntry> timeline;
    public TransactionTraceResponse(Trade trade, List<String> correlationIds, List<UUID> eventIds,
                                    OffsetDateTime startedAt, OffsetDateTime lastUpdatedAt,
                                    Long processingDurationMs, List<TransactionTraceEntry> timeline) {
        this.tradeId = trade.getId(); this.tradeReference = trade.getTradeReference();
        this.currentStatus = trade.getStatus().name(); this.correlationIds = correlationIds; this.eventIds = eventIds;
        this.startedAt = startedAt; this.lastUpdatedAt = lastUpdatedAt;
        this.processingDurationMs = processingDurationMs; this.timeline = timeline;
    }
    public UUID getTradeId() { return tradeId; } public String getTradeReference() { return tradeReference; }
    public String getCurrentStatus() { return currentStatus; } public List<String> getCorrelationIds() { return correlationIds; }
    public List<UUID> getEventIds() { return eventIds; } public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getLastUpdatedAt() { return lastUpdatedAt; } public Long getProcessingDurationMs() { return processingDurationMs; }
    public List<TransactionTraceEntry> getTimeline() { return timeline; }
}

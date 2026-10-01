package com.tradesettlement.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.tradesettlement.entity.DeadLetterEvent;
import com.tradesettlement.entity.EventRetryAttempt;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatusHistory;

public class FailedTradeResponse {
    private final UUID id; private final UUID tradeId; private final String tradeReference;
    private final String tradeStatus; private final String eventType; private final String failureReason;
    private final int retryCount; private final int maxRetries; private final String retryStatus;
    private final String dlqStatus; private final String originalTopic; private final Integer originalPartition;
    private final Long originalOffset; private final String correlationId; private final OffsetDateTime failedAt;

    private FailedTradeResponse(UUID id, UUID tradeId, String tradeReference, String tradeStatus, String eventType,
                                String failureReason, TradeRetryStatusResponse retry, String dlqStatus,
                                String originalTopic, Integer originalPartition, Long originalOffset,
                                String correlationId, OffsetDateTime failedAt) {
        this.id = id; this.tradeId = tradeId; this.tradeReference = tradeReference; this.tradeStatus = tradeStatus;
        this.eventType = eventType; this.failureReason = failureReason; this.retryCount = retry.getRetryCount();
        this.maxRetries = retry.getMaxRetries(); this.retryStatus = retry.getStatus(); this.dlqStatus = dlqStatus;
        this.originalTopic = originalTopic; this.originalPartition = originalPartition; this.originalOffset = originalOffset;
        this.correlationId = correlationId; this.failedAt = failedAt;
    }

    public static FailedTradeResponse deadLetter(DeadLetterEvent deadLetter, Trade trade, List<EventRetryAttempt> attempts) {
        return new FailedTradeResponse(deadLetter.getId(), deadLetter.getTradeId(), deadLetter.getTradeReference(),
                trade == null || trade.getStatus() == null ? null : trade.getStatus().name(), deadLetter.getEventType(),
                deadLetter.getFailureReason(), TradeRetryStatusResponse.from(attempts), "ROUTED",
                deadLetter.getOriginalTopic(), deadLetter.getOriginalPartition(), deadLetter.getOriginalOffset(),
                deadLetter.getCorrelationId(), deadLetter.getReceivedAt());
    }

    public static FailedTradeResponse businessFailure(Trade trade, TradeStatusHistory latest,
                                                       List<EventRetryAttempt> attempts) {
        return new FailedTradeResponse(trade.getId(), trade.getId(), trade.getTradeReference(), trade.getStatus().name(),
                null, latest == null ? "Trade processing failed" : latest.getReason(),
                TradeRetryStatusResponse.from(attempts), "NOT_ROUTED", null, null, null,
                latest == null ? null : latest.getCorrelationId(), latest == null ? trade.getUpdatedAt() : latest.getChangedAt());
    }

    public UUID getId() { return id; } public UUID getTradeId() { return tradeId; }
    public String getTradeReference() { return tradeReference; } public String getTradeStatus() { return tradeStatus; }
    public String getEventType() { return eventType; } public String getFailureReason() { return failureReason; }
    public int getRetryCount() { return retryCount; } public int getMaxRetries() { return maxRetries; }
    public String getRetryStatus() { return retryStatus; } public String getDlqStatus() { return dlqStatus; }
    public String getOriginalTopic() { return originalTopic; } public Integer getOriginalPartition() { return originalPartition; }
    public Long getOriginalOffset() { return originalOffset; } public String getCorrelationId() { return correlationId; }
    public OffsetDateTime getFailedAt() { return failedAt; }
}

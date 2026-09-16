package com.tradesettlement.kafka;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;

public class TradeLifecycleEvent {

    private UUID eventId;
    private UUID tradeId;
    private String tradeReference;
    private TradeType tradeType;
    private TradeStatus status;
    private TradeEventType eventType;
    private OffsetDateTime occurredAt;
    private String correlationId;

    public TradeLifecycleEvent() {
    }

    public TradeLifecycleEvent(UUID eventId, UUID tradeId, String tradeReference, TradeType tradeType,
                               TradeStatus status, TradeEventType eventType, OffsetDateTime occurredAt,
                               String correlationId) {
        this.eventId = eventId;
        this.tradeId = tradeId;
        this.tradeReference = tradeReference;
        this.tradeType = tradeType;
        this.status = status;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID tradeId) { this.tradeId = tradeId; }
    public String getTradeReference() { return tradeReference; }
    public void setTradeReference(String tradeReference) { this.tradeReference = tradeReference; }
    public TradeType getTradeType() { return tradeType; }
    public void setTradeType(TradeType tradeType) { this.tradeType = tradeType; }
    public TradeStatus getStatus() { return status; }
    public void setStatus(TradeStatus status) { this.status = status; }
    public TradeEventType getEventType() { return eventType; }
    public void setEventType(TradeEventType eventType) { this.eventType = eventType; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}

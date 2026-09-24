package com.tradesettlement.kafka;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.entity.Settlement;

public class TradeEvent {

    private UUID eventId;
    private UUID tradeId;
    private String tradeReference;
    private TradeType tradeType;
    private TradeStatus status;
    private TradeEventType eventType;
    private OffsetDateTime occurredAt;
    private String correlationId;
    private List<String> validationErrors = new ArrayList<>();
    private List<String> eligibilityReasons = new ArrayList<>();
    private UUID settlementId;
    private String settlementReference;
    private String settlementFailureReason;

    public TradeEvent() {
    }

    public TradeEvent(UUID eventId, UUID tradeId, String tradeReference, TradeType tradeType,
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

    public TradeEvent(UUID eventId, UUID tradeId, String tradeReference, TradeType tradeType,
                      TradeStatus status, TradeEventType eventType, OffsetDateTime occurredAt,
                      String correlationId, List<String> validationErrors) {
        this(eventId, tradeId, tradeReference, tradeType, status, eventType, occurredAt, correlationId);
        this.validationErrors = validationErrors == null ? new ArrayList<>() : new ArrayList<>(validationErrors);
    }

    public static TradeEvent accepted(Trade trade, String correlationId) {
        return new TradeEvent(
                UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.TRADE_ACCEPTED, OffsetDateTime.now(ZoneOffset.UTC),
                correlationId);
    }

    public static TradeEvent validationCompleted(Trade trade, String correlationId) {
        return new TradeEvent(
                UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.VALIDATION_COMPLETED, OffsetDateTime.now(ZoneOffset.UTC),
                correlationId);
    }

    public static TradeEvent validationRejected(Trade trade, String correlationId, List<String> validationErrors) {
        return new TradeEvent(
                UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.VALIDATION_REJECTED, OffsetDateTime.now(ZoneOffset.UTC),
                correlationId, validationErrors);
    }

    public static TradeEvent enrichmentCompleted(Trade trade, String correlationId) {
        return new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.ENRICHMENT_COMPLETED, OffsetDateTime.now(ZoneOffset.UTC), correlationId);
    }

    public static TradeEvent enrichmentFailed(Trade trade, String correlationId, List<String> errors) {
        return new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.ENRICHMENT_FAILED, OffsetDateTime.now(ZoneOffset.UTC), correlationId, errors);
    }

    public static TradeEvent eligibilityConfirmed(Trade trade, String correlationId) {
        return new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.ELIGIBILITY_CONFIRMED, OffsetDateTime.now(ZoneOffset.UTC), correlationId);
    }

    public static TradeEvent eligibilityRejected(Trade trade, String correlationId, List<String> reasons) {
        TradeEvent event = new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.ELIGIBILITY_REJECTED, OffsetDateTime.now(ZoneOffset.UTC), correlationId);
        event.setEligibilityReasons(reasons);
        return event;
    }

    public static TradeEvent settlementCompleted(Trade trade, Settlement settlement, String correlationId) {
        TradeEvent event = new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.SETTLEMENT_COMPLETED, OffsetDateTime.now(ZoneOffset.UTC), correlationId);
        event.setSettlement(settlement, null);
        return event;
    }

    public static TradeEvent settlementFailed(Trade trade, Settlement settlement, String correlationId) {
        TradeEvent event = new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.SETTLEMENT_FAILED, OffsetDateTime.now(ZoneOffset.UTC), correlationId);
        event.setSettlement(settlement, settlement.getFailureReason());
        return event;
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
    public List<String> getValidationErrors() { return Collections.unmodifiableList(validationErrors); }
    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors == null ? new ArrayList<>() : new ArrayList<>(validationErrors);
    }
    public List<String> getEligibilityReasons() { return Collections.unmodifiableList(eligibilityReasons); }
    public void setEligibilityReasons(List<String> eligibilityReasons) {
        this.eligibilityReasons = eligibilityReasons == null ? new ArrayList<>() : new ArrayList<>(eligibilityReasons);
    }
    public UUID getSettlementId() { return settlementId; }
    public void setSettlementId(UUID settlementId) { this.settlementId = settlementId; }
    public String getSettlementReference() { return settlementReference; }
    public void setSettlementReference(String settlementReference) { this.settlementReference = settlementReference; }
    public String getSettlementFailureReason() { return settlementFailureReason; }
    public void setSettlementFailureReason(String settlementFailureReason) { this.settlementFailureReason = settlementFailureReason; }
    private void setSettlement(Settlement settlement, String failureReason) {
        this.settlementId = settlement.getId(); this.settlementReference = settlement.getInstructionReference();
        this.settlementFailureReason = failureReason;
    }
}

package com.tradesettlement.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.tradesettlement.entity.Settlement;
import com.tradesettlement.entity.SettlementStatus;

public class SettlementStatusResponse {
    private final UUID id; private final UUID tradeId; private final String tradeReference;
    private final String instructionReference; private final SettlementStatus status;
    private final BigDecimal amount; private final String currencyCode; private final OffsetDateTime requestedAt;
    private final OffsetDateTime settledAt; private final String failureReason;
    private SettlementStatusResponse(Settlement value) {
        id = value.getId(); tradeId = value.getTradeId(); tradeReference = value.getTradeReference();
        instructionReference = value.getInstructionReference(); status = value.getStatus(); amount = value.getAmount();
        currencyCode = value.getCurrencyCode(); requestedAt = value.getRequestedAt(); settledAt = value.getSettledAt();
        failureReason = value.getFailureReason();
    }
    public static SettlementStatusResponse from(Settlement value) { return new SettlementStatusResponse(value); }
    public UUID getId() { return id; } public UUID getTradeId() { return tradeId; }
    public String getTradeReference() { return tradeReference; } public String getInstructionReference() { return instructionReference; }
    public SettlementStatus getStatus() { return status; } public BigDecimal getAmount() { return amount; }
    public String getCurrencyCode() { return currencyCode; } public OffsetDateTime getRequestedAt() { return requestedAt; }
    public OffsetDateTime getSettledAt() { return settledAt; } public String getFailureReason() { return failureReason; }
}

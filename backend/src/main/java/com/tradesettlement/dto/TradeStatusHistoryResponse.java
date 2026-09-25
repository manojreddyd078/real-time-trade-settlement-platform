package com.tradesettlement.dto;

import java.time.OffsetDateTime;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeStatusHistory;

public class TradeStatusHistoryResponse {
    private final TradeStatus fromStatus; private final TradeStatus toStatus; private final String reason;
    private final String correlationId; private final String changedBy; private final OffsetDateTime changedAt;
    private TradeStatusHistoryResponse(TradeStatusHistory value) {
        fromStatus = value.getFromStatus(); toStatus = value.getToStatus(); reason = value.getReason();
        correlationId = value.getCorrelationId(); changedBy = value.getChangedBy(); changedAt = value.getChangedAt();
    }
    public static TradeStatusHistoryResponse from(TradeStatusHistory value) { return new TradeStatusHistoryResponse(value); }
    public TradeStatus getFromStatus() { return fromStatus; } public TradeStatus getToStatus() { return toStatus; }
    public String getReason() { return reason; } public String getCorrelationId() { return correlationId; }
    public String getChangedBy() { return changedBy; } public OffsetDateTime getChangedAt() { return changedAt; }
}

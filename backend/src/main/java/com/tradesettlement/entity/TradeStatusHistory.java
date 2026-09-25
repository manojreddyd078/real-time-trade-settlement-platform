package com.tradesettlement.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "trade_status_history", schema = "settlement")
public class TradeStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "trade_id", nullable = false) private UUID tradeId;
    @Enumerated(EnumType.STRING) @Column(name = "from_status", length = 32) private TradeStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(name = "to_status", nullable = false, length = 32) private TradeStatus toStatus;
    @Column(length = 1000) private String reason;
    @Column(name = "correlation_id", length = 100) private String correlationId;
    @Column(name = "changed_by", nullable = false, length = 100) private String changedBy;
    @Column(name = "changed_at", nullable = false) private OffsetDateTime changedAt;

    public Long getId() { return id; } public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID value) { tradeId = value; }
    public TradeStatus getFromStatus() { return fromStatus; } public void setFromStatus(TradeStatus value) { fromStatus = value; }
    public TradeStatus getToStatus() { return toStatus; } public void setToStatus(TradeStatus value) { toStatus = value; }
    public String getReason() { return reason; } public void setReason(String value) { reason = value; }
    public String getCorrelationId() { return correlationId; } public void setCorrelationId(String value) { correlationId = value; }
    public String getChangedBy() { return changedBy; } public void setChangedBy(String value) { changedBy = value; }
    public OffsetDateTime getChangedAt() { return changedAt; } public void setChangedAt(OffsetDateTime value) { changedAt = value; }
}

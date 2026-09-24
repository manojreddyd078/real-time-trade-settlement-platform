package com.tradesettlement.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "settlements", schema = "settlement")
public class Settlement {
    @Id private UUID id;
    @Column(name = "trade_id", nullable = false, unique = true) private UUID tradeId;
    @Column(name = "trade_reference", nullable = false, length = 64) private String tradeReference;
    @Column(name = "instruction_reference", nullable = false, unique = true, length = 80) private String instructionReference;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private SettlementStatus status;
    @Column(nullable = false, precision = 27, scale = 8) private BigDecimal amount;
    @Column(name = "currency_code", nullable = false, length = 3) private String currencyCode;
    @Column(name = "requested_at", nullable = false) private OffsetDateTime requestedAt;
    @Column(name = "settled_at") private OffsetDateTime settledAt;
    @Column(name = "failure_reason", length = 1000) private String failureReason;
    @Version @Column(nullable = false) private long version;

    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public UUID getTradeId() { return tradeId; } public void setTradeId(UUID tradeId) { this.tradeId = tradeId; }
    public String getTradeReference() { return tradeReference; } public void setTradeReference(String value) { this.tradeReference = value; }
    public String getInstructionReference() { return instructionReference; }
    public void setInstructionReference(String value) { this.instructionReference = value; }
    public SettlementStatus getStatus() { return status; } public void setStatus(SettlementStatus status) { this.status = status; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrencyCode() { return currencyCode; } public void setCurrencyCode(String value) { this.currencyCode = value; }
    public OffsetDateTime getRequestedAt() { return requestedAt; } public void setRequestedAt(OffsetDateTime value) { this.requestedAt = value; }
    public OffsetDateTime getSettledAt() { return settledAt; } public void setSettledAt(OffsetDateTime value) { this.settledAt = value; }
    public String getFailureReason() { return failureReason; } public void setFailureReason(String value) { this.failureReason = value; }
    public long getVersion() { return version; }
}

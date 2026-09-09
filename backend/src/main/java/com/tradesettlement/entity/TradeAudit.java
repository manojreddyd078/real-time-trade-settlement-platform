package com.tradesettlement.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "trade_audit", schema = "settlement")
public class TradeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    protected TradeAudit() {
    }

    public Long getId() {
        return id;
    }

    public UUID getTradeId() {
        return tradeId;
    }

    public String getOperation() {
        return operation;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }
}

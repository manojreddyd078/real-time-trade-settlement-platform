package com.tradesettlement.repository;

import java.util.List;
import java.util.UUID;

import com.tradesettlement.entity.TradeAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeAuditRepository extends JpaRepository<TradeAudit, Long> {

    List<TradeAudit> findByTradeIdOrderByChangedAtDesc(UUID tradeId);
}

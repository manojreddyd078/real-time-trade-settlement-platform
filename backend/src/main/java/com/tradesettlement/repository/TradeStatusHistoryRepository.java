package com.tradesettlement.repository;

import java.util.List;
import java.util.UUID;
import com.tradesettlement.entity.TradeStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeStatusHistoryRepository extends JpaRepository<TradeStatusHistory, Long> {
    List<TradeStatusHistory> findByTradeIdOrderByChangedAtAscIdAsc(UUID tradeId);
}

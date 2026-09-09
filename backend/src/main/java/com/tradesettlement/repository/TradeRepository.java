package com.tradesettlement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, UUID> {

    Optional<Trade> findByTradeReference(String tradeReference);

    List<Trade> findByStatusOrderByCreatedAtAsc(TradeStatus status);

    List<Trade> findBySettlementDateAndStatus(LocalDate settlementDate, TradeStatus status);
}

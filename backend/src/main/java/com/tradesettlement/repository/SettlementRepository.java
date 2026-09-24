package com.tradesettlement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tradesettlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    Optional<Settlement> findByTradeId(UUID tradeId);
    List<Settlement> findAllByOrderByRequestedAtDesc();
}

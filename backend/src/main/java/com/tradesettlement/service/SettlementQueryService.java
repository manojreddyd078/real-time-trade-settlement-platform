package com.tradesettlement.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.tradesettlement.dto.SettlementStatusResponse;
import com.tradesettlement.exception.TradeNotFoundException;
import com.tradesettlement.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementQueryService {
    private final SettlementRepository settlements;
    public SettlementQueryService(SettlementRepository settlements) { this.settlements = settlements; }
    @Transactional(readOnly = true)
    public List<SettlementStatusResponse> list() {
        return settlements.findAllByOrderByRequestedAtDesc().stream().map(SettlementStatusResponse::from).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public SettlementStatusResponse getByTradeId(UUID tradeId) {
        return settlements.findByTradeId(tradeId).map(SettlementStatusResponse::from)
                .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }
}

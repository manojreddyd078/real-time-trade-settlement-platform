package com.tradesettlement.service;

import java.util.UUID;

import com.tradesettlement.dto.TradeSubmissionResponse;
import com.tradesettlement.dto.TradeDetailsResponse;
import com.tradesettlement.dto.TradeLifecycleStatusResponse;
import com.tradesettlement.dto.TradeStatusHistoryResponse;
import com.tradesettlement.repository.TradeStatusHistoryRepository;
import java.util.stream.Collectors;
import com.tradesettlement.exception.TradeNotFoundException;
import com.tradesettlement.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeQueryService {

    private final TradeRepository tradeRepository;
    private final TradeStatusHistoryRepository statusHistory;

    public TradeQueryService(TradeRepository tradeRepository, TradeStatusHistoryRepository statusHistory) {
        this.tradeRepository = tradeRepository;
        this.statusHistory = statusHistory;
    }

    @Transactional(readOnly = true)
    public TradeSubmissionResponse get(UUID tradeId) {
        return tradeRepository.findById(tradeId)
                .map(TradeSubmissionResponse::from)
                .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Transactional(readOnly = true)
    public TradeDetailsResponse getDetails(UUID tradeId) {
        return tradeRepository.findById(tradeId).map(TradeDetailsResponse::from)
                .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Transactional(readOnly = true)
    public TradeLifecycleStatusResponse getStatus(UUID tradeId) {
        com.tradesettlement.entity.Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new TradeNotFoundException(tradeId));
        java.util.List<TradeStatusHistoryResponse> history = statusHistory
                .findByTradeIdOrderByChangedAtAscIdAsc(tradeId).stream()
                .map(TradeStatusHistoryResponse::from).collect(Collectors.toList());
        return new TradeLifecycleStatusResponse(trade, history);
    }
}

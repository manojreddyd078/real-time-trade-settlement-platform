package com.tradesettlement.service;

import java.util.UUID;

import com.tradesettlement.dto.TradeSubmissionResponse;
import com.tradesettlement.dto.TradeDetailsResponse;
import com.tradesettlement.exception.TradeNotFoundException;
import com.tradesettlement.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeQueryService {

    private final TradeRepository tradeRepository;

    public TradeQueryService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
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
}

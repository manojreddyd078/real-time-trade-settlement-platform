package com.tradesettlement.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.tradesettlement.dto.FailedTradeResponse;
import com.tradesettlement.entity.DeadLetterEvent;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeStatusHistory;
import com.tradesettlement.repository.DeadLetterEventRepository;
import com.tradesettlement.repository.EventRetryAttemptRepository;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.repository.TradeStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FailureQueryService {
    private final DeadLetterEventRepository deadLetters; private final TradeRepository trades;
    private final EventRetryAttemptRepository retries; private final TradeStatusHistoryRepository history;
    public FailureQueryService(DeadLetterEventRepository deadLetters, TradeRepository trades,
                               EventRetryAttemptRepository retries, TradeStatusHistoryRepository history) {
        this.deadLetters = deadLetters; this.trades = trades; this.retries = retries; this.history = history;
    }

    @Transactional(readOnly = true)
    public List<FailedTradeResponse> list() {
        List<FailedTradeResponse> result = new ArrayList<>(); Set<UUID> includedTrades = new HashSet<>();
        for (DeadLetterEvent deadLetter : deadLetters.findAllByOrderByReceivedAtDesc()) {
            Trade trade = deadLetter.getTradeId() == null ? null : trades.findById(deadLetter.getTradeId()).orElse(null);
            result.add(FailedTradeResponse.deadLetter(deadLetter, trade, deadLetter.getEventId() == null
                    ? java.util.Collections.emptyList() : retries.findByEventIdOrderByLastAttemptAtDesc(deadLetter.getEventId())));
            if (deadLetter.getTradeId() != null) includedTrades.add(deadLetter.getTradeId());
        }
        for (Trade trade : trades.findByStatusInOrderByUpdatedAtDesc(Arrays.asList(TradeStatus.FAILED, TradeStatus.REJECTED))) {
            if (includedTrades.add(trade.getId())) {
                List<TradeStatusHistory> entries = history.findByTradeIdOrderByChangedAtAscIdAsc(trade.getId());
                TradeStatusHistory latest = entries.isEmpty() ? null : entries.get(entries.size() - 1);
                result.add(FailedTradeResponse.businessFailure(trade, latest,
                        retries.findByTradeIdOrderByLastAttemptAtDesc(trade.getId())));
            }
        }
        result.sort(java.util.Comparator.comparing(FailedTradeResponse::getFailedAt,
                java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));
        return result;
    }
}

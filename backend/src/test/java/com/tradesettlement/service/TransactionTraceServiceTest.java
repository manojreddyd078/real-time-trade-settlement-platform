package com.tradesettlement.service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import com.tradesettlement.dto.TransactionTraceResponse;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeStatusHistory;
import com.tradesettlement.repository.DeadLetterEventRepository;
import com.tradesettlement.repository.EventRetryAttemptRepository;
import com.tradesettlement.repository.ProcessedEventRepository;
import com.tradesettlement.repository.SettlementRepository;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.repository.TradeStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionTraceServiceTest {
    @Test
    void buildsChronologicalTraceWithCorrelationIdentifiers() {
        UUID tradeId = UUID.randomUUID(); Trade trade = new Trade(); ReflectionTestUtils.setField(trade, "id", tradeId);
        trade.setTradeReference("TRD-TRACE-1"); trade.setStatus(TradeStatus.VALIDATED);
        TradeStatusHistory received = history(tradeId, TradeStatus.RECEIVED, "correlation-1", "2026-10-02T10:00:00Z");
        TradeStatusHistory validated = history(tradeId, TradeStatus.VALIDATED, "correlation-1", "2026-10-02T10:00:02Z");
        TradeRepository trades = mock(TradeRepository.class); TradeStatusHistoryRepository statuses = mock(TradeStatusHistoryRepository.class);
        ProcessedEventRepository events = mock(ProcessedEventRepository.class); EventRetryAttemptRepository retries = mock(EventRetryAttemptRepository.class);
        DeadLetterEventRepository deadLetters = mock(DeadLetterEventRepository.class); SettlementRepository settlements = mock(SettlementRepository.class);
        when(trades.findById(tradeId)).thenReturn(Optional.of(trade)); when(statuses.findByTradeIdOrderByChangedAtAscIdAsc(tradeId)).thenReturn(java.util.Arrays.asList(received, validated));
        when(events.findByTradeIdOrderByReceivedAtAsc(tradeId)).thenReturn(Collections.emptyList());
        when(retries.findByTradeIdOrderByLastAttemptAtDesc(tradeId)).thenReturn(Collections.emptyList());
        when(deadLetters.findByTradeIdOrderByReceivedAtAsc(tradeId)).thenReturn(Collections.emptyList());
        when(settlements.findByTradeId(tradeId)).thenReturn(Optional.empty());

        TransactionTraceResponse result = new TransactionTraceService(trades, statuses, events, retries, deadLetters, settlements).trace(tradeId);

        assertEquals(tradeId, result.getTradeId()); assertEquals(2, result.getTimeline().size());
        assertEquals("RECEIVED", result.getTimeline().get(0).getStatus());
        assertEquals(Collections.singletonList("correlation-1"), result.getCorrelationIds());
        assertEquals(2000L, result.getProcessingDurationMs()); assertTrue(result.getEventIds().isEmpty());
    }

    private TradeStatusHistory history(UUID tradeId, TradeStatus status, String correlation, String timestamp) {
        TradeStatusHistory value = new TradeStatusHistory(); value.setTradeId(tradeId); value.setToStatus(status);
        value.setReason("Moved to " + status); value.setCorrelationId(correlation); value.setChangedBy("test");
        value.setChangedAt(OffsetDateTime.parse(timestamp)); return value;
    }
}

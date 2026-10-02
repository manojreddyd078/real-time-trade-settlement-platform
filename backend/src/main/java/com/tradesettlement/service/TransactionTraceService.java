package com.tradesettlement.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.tradesettlement.dto.TransactionTraceEntry;
import com.tradesettlement.dto.TransactionTraceResponse;
import com.tradesettlement.entity.DeadLetterEvent;
import com.tradesettlement.entity.EventRetryAttempt;
import com.tradesettlement.entity.ProcessedEvent;
import com.tradesettlement.entity.Settlement;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatusHistory;
import com.tradesettlement.exception.TradeNotFoundException;
import com.tradesettlement.repository.DeadLetterEventRepository;
import com.tradesettlement.repository.EventRetryAttemptRepository;
import com.tradesettlement.repository.ProcessedEventRepository;
import com.tradesettlement.repository.SettlementRepository;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.repository.TradeStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionTraceService {
    private static final Logger log = LoggerFactory.getLogger(TransactionTraceService.class);
    private final TradeRepository trades; private final TradeStatusHistoryRepository statuses;
    private final ProcessedEventRepository events; private final EventRetryAttemptRepository retries;
    private final DeadLetterEventRepository deadLetters; private final SettlementRepository settlements;
    public TransactionTraceService(TradeRepository trades, TradeStatusHistoryRepository statuses,
                                   ProcessedEventRepository events, EventRetryAttemptRepository retries,
                                   DeadLetterEventRepository deadLetters, SettlementRepository settlements) {
        this.trades = trades; this.statuses = statuses; this.events = events; this.retries = retries;
        this.deadLetters = deadLetters; this.settlements = settlements;
    }

    @Transactional(readOnly = true)
    public TransactionTraceResponse trace(UUID tradeId) {
        Trade trade = trades.findById(tradeId).orElseThrow(() -> new TradeNotFoundException(tradeId));
        MDC.put("tradeId", tradeId.toString());
        try {
            List<TransactionTraceEntry> timeline = new ArrayList<>(); Set<String> correlations = new LinkedHashSet<>();
            Set<UUID> eventIds = new LinkedHashSet<>();
            for (TradeStatusHistory value : statuses.findByTradeIdOrderByChangedAtAscIdAsc(tradeId)) {
                add(correlations, value.getCorrelationId());
                timeline.add(entry(value.getChangedAt(), "STATUS", "trade-lifecycle", value.getToStatus().name(),
                        null, value.getCorrelationId(), value.getReason(), Map.of(
                                "fromStatus", value.getFromStatus() == null ? "NONE" : value.getFromStatus().name(),
                                "changedBy", value.getChangedBy())));
            }
            for (ProcessedEvent value : events.findByTradeIdOrderByReceivedAtAsc(tradeId)) {
                add(correlations, value.getCorrelationId()); eventIds.add(value.getEventId());
                timeline.add(entry(value.getReceivedAt(), "EVENT", value.getConsumerName(), value.getStatus(),
                        value.getEventId(), value.getCorrelationId(), "Kafka event " + value.getEventType() + " processed",
                        metadata("eventType", value.getEventType(), "occurredAt", string(value.getEventOccurredAt()),
                                "processedAt", string(value.getProcessedAt()))));
            }
            for (EventRetryAttempt value : retries.findByTradeIdOrderByLastAttemptAtDesc(tradeId)) {
                eventIds.add(value.getEventId());
                timeline.add(entry(value.getLastAttemptAt(), "RETRY", value.getTopic(), value.getStatus(),
                        value.getEventId(), null, value.getLastFailure(), metadata("retryCount", String.valueOf(value.getRetryCount()),
                                "maxRetries", String.valueOf(value.getMaxRetries()), "retryKey", value.getRetryKey())));
            }
            for (DeadLetterEvent value : deadLetters.findByTradeIdOrderByReceivedAtAsc(tradeId)) {
                add(correlations, value.getCorrelationId()); if (value.getEventId() != null) eventIds.add(value.getEventId());
                timeline.add(entry(value.getReceivedAt(), "DLQ", value.getOriginalTopic(), "ROUTED",
                        value.getEventId(), value.getCorrelationId(), value.getFailureReason(), metadata(
                                "partition", String.valueOf(value.getOriginalPartition()), "offset", String.valueOf(value.getOriginalOffset()),
                                "consumerGroup", value.getOriginalConsumerGroup(), "failureClass", value.getFailureClass())));
            }
            Settlement settlement = settlements.findByTradeId(tradeId).orElse(null);
            if (settlement != null) {
                add(correlations, settlement.getCorrelationId());
                timeline.add(entry(settlement.getRequestedAt(), "SETTLEMENT", "settlement", settlement.getStatus().name(),
                        null, settlement.getCorrelationId(), "Settlement instruction created", metadata(
                                "settlementId", string(settlement.getId()), "instructionReference", settlement.getInstructionReference())));
                if (settlement.getSettledAt() != null) timeline.add(entry(settlement.getSettledAt(), "SETTLEMENT", "settlement",
                        "SETTLED", null, settlement.getCorrelationId(), "Settlement completed", null));
            }
            timeline.sort(Comparator.comparing(TransactionTraceEntry::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
            OffsetDateTime started = timeline.isEmpty() ? trade.getCreatedAt() : timeline.get(0).getTimestamp();
            OffsetDateTime updated = timeline.isEmpty() ? trade.getUpdatedAt() : timeline.get(timeline.size() - 1).getTimestamp();
            Long duration = started == null || updated == null ? null : Duration.between(started, updated).toMillis();
            log.info("transaction_trace_created tradeReference={} entryCount={} eventCount={} correlationCount={}",
                    trade.getTradeReference(), timeline.size(), eventIds.size(), correlations.size());
            return new TransactionTraceResponse(trade, new ArrayList<>(correlations), new ArrayList<>(eventIds),
                    started, updated, duration, timeline);
        } finally { MDC.remove("tradeId"); }
    }

    private TransactionTraceEntry entry(OffsetDateTime timestamp, String category, String stage, String status,
                                        UUID eventId, String correlationId, String description, Map<String, String> metadata) {
        return new TransactionTraceEntry(timestamp, category, stage, status, eventId, correlationId, description, metadata);
    }
    private void add(Set<String> values, String value) { if (value != null && !value.isBlank()) values.add(value); }
    private String string(Object value) { return value == null ? null : value.toString(); }
    private Map<String, String> metadata(String... values) {
        return java.util.stream.IntStream.range(0, values.length / 2).boxed().collect(Collectors.toMap(
                index -> values[index * 2], index -> values[index * 2 + 1] == null ? "" : values[index * 2 + 1],
                (left, right) -> left, java.util.LinkedHashMap::new));
    }
}

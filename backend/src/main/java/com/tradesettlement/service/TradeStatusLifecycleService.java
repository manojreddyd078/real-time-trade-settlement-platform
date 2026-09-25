package com.tradesettlement.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeStatusHistory;
import com.tradesettlement.exception.InvalidTradeStatusTransitionException;
import com.tradesettlement.repository.TradeStatusHistoryRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class TradeStatusLifecycleService {
    private static final Map<TradeStatus, EnumSet<TradeStatus>> ALLOWED = transitions();
    private final TradeStatusHistoryRepository history; private final Clock clock;
    public TradeStatusLifecycleService(TradeStatusHistoryRepository history, Clock clock) { this.history = history; this.clock = clock; }

    public void recordInitial(Trade trade, String correlationId) {
        if (trade.getStatus() != TradeStatus.RECEIVED) throw new InvalidTradeStatusTransitionException(null, trade.getStatus());
        history.save(entry(trade, null, TradeStatus.RECEIVED, "Trade received", correlationId));
    }

    public void transition(Trade trade, TradeStatus target, String reason, String correlationId) {
        TradeStatus current = trade.getStatus();
        if (current == target) return;
        if (!ALLOWED.getOrDefault(current, EnumSet.noneOf(TradeStatus.class)).contains(target)) {
            throw new InvalidTradeStatusTransitionException(current, target);
        }
        trade.setStatus(target);
        history.save(entry(trade, current, target, reason, correlationId));
    }

    private TradeStatusHistory entry(Trade trade, TradeStatus from, TradeStatus to, String reason, String correlationId) {
        TradeStatusHistory value = new TradeStatusHistory(); value.setTradeId(trade.getId()); value.setFromStatus(from);
        value.setToStatus(to); value.setReason(reason); value.setCorrelationId(correlationId);
        String actor = MDC.get("requestId"); value.setChangedBy(actor == null ? "system" : actor);
        value.setChangedAt(OffsetDateTime.now(clock)); return value;
    }

    private static Map<TradeStatus, EnumSet<TradeStatus>> transitions() {
        Map<TradeStatus, EnumSet<TradeStatus>> value = new EnumMap<>(TradeStatus.class);
        value.put(TradeStatus.RECEIVED, EnumSet.of(TradeStatus.VALIDATED, TradeStatus.REJECTED, TradeStatus.FAILED));
        value.put(TradeStatus.VALIDATED, EnumSet.of(TradeStatus.ENRICHED, TradeStatus.FAILED));
        value.put(TradeStatus.ENRICHED, EnumSet.of(TradeStatus.ELIGIBLE, TradeStatus.REJECTED, TradeStatus.FAILED));
        value.put(TradeStatus.ELIGIBLE, EnumSet.of(TradeStatus.SETTLEMENT_PENDING, TradeStatus.FAILED));
        value.put(TradeStatus.SETTLEMENT_PENDING, EnumSet.of(TradeStatus.SETTLED, TradeStatus.FAILED));
        return value;
    }
}

package com.tradesettlement.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.tradesettlement.entity.Settlement;
import com.tradesettlement.entity.SettlementEligibilityStatus;
import com.tradesettlement.entity.SettlementStatus;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.exception.TradeProcessingException;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.SettlementRepository;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.settlement.SettlementExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {
    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);
    private final TradeRepository trades; private final SettlementRepository settlements;
    private final SettlementExecutor executor; private final ApplicationEventPublisher eventPublisher; private final Clock clock;

    public SettlementService(TradeRepository trades, SettlementRepository settlements, SettlementExecutor executor,
                             ApplicationEventPublisher eventPublisher, Clock clock) {
        this.trades = trades; this.settlements = settlements; this.executor = executor;
        this.eventPublisher = eventPublisher; this.clock = clock;
    }

    @Transactional
    public void process(TradeEvent event) {
        if (event.getEventType() != TradeEventType.ELIGIBILITY_CONFIRMED || event.getTradeId() == null) {
            throw new TradeProcessingException("Unsupported settlement event");
        }
        Trade trade = trades.findById(event.getTradeId())
                .orElseThrow(() -> new TradeProcessingException("Trade was not found: " + event.getTradeId()));
        Settlement settlement = settlements.findByTradeId(trade.getId()).orElse(null);
        if (settlement != null && (settlement.getStatus() == SettlementStatus.SETTLED
                || settlement.getStatus() == SettlementStatus.FAILED)) {
            log.info("settlement_already_processed settlementId={} status={}", settlement.getId(), settlement.getStatus());
            return;
        }
        if (trade.getEligibilityStatus() != SettlementEligibilityStatus.ELIGIBLE
                || (trade.getStatus() != TradeStatus.READY_FOR_SETTLEMENT && trade.getStatus() != TradeStatus.SETTLEMENT_PENDING)) {
            throw new TradeProcessingException("Trade is not eligible for settlement");
        }

        if (settlement == null) {
            settlement = createSettlement(trade);
            settlements.saveAndFlush(settlement);
        }
        settlement.setStatus(SettlementStatus.PROCESSING);
        trade.setStatus(TradeStatus.SETTLEMENT_PENDING);
        settlements.saveAndFlush(settlement); trades.saveAndFlush(trade);
        log.info("settlement_processing_started settlementId={} instructionReference={}",
                settlement.getId(), settlement.getInstructionReference());

        try {
            executor.execute(trade, settlement);
            settlement.setStatus(SettlementStatus.SETTLED);
            settlement.setSettledAt(OffsetDateTime.now(clock));
            settlement.setFailureReason(null);
            trade.setStatus(TradeStatus.SETTLED);
            settlements.saveAndFlush(settlement); trades.saveAndFlush(trade);
            log.info("settlement_completed settlementId={} instructionReference={}",
                    settlement.getId(), settlement.getInstructionReference());
            eventPublisher.publishEvent(TradeEvent.settlementCompleted(trade, settlement, event.getCorrelationId()));
        } catch (RuntimeException exception) {
            settlement.setStatus(SettlementStatus.FAILED);
            settlement.setFailureReason(safeMessage(exception));
            trade.setStatus(TradeStatus.FAILED);
            settlements.saveAndFlush(settlement); trades.saveAndFlush(trade);
            log.error("settlement_failed settlementId={} instructionReference={} reason={}", settlement.getId(),
                    settlement.getInstructionReference(), settlement.getFailureReason(), exception);
            eventPublisher.publishEvent(TradeEvent.settlementFailed(trade, settlement, event.getCorrelationId()));
        }
    }

    private Settlement createSettlement(Trade trade) {
        Settlement value = new Settlement(); UUID id = UUID.randomUUID(); value.setId(id); value.setTradeId(trade.getId());
        value.setTradeReference(trade.getTradeReference());
        value.setInstructionReference("STL-" + id); value.setStatus(SettlementStatus.PENDING);
        BigDecimal quantity = trade.getQuantity() == null ? BigDecimal.ZERO : trade.getQuantity();
        BigDecimal price = trade.getPrice() == null ? BigDecimal.ZERO : trade.getPrice();
        value.setAmount(quantity.multiply(price)); value.setCurrencyCode(trade.getCurrencyCode());
        value.setRequestedAt(OffsetDateTime.now(clock)); return value;
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "Settlement execution failed" : message.substring(0, Math.min(message.length(), 1000));
    }
}

package com.tradesettlement.service;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.exception.TradeProcessingException;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.validation.TradeProcessingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TradeProcessingService.class);

    private final TradeRepository tradeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TradeProcessingValidator validator;
    private final TradeStatusLifecycleService statusLifecycle;

    public TradeProcessingService(TradeRepository tradeRepository, ApplicationEventPublisher eventPublisher,
                                  TradeProcessingValidator validator, TradeStatusLifecycleService statusLifecycle) {
        this.tradeRepository = tradeRepository;
        this.eventPublisher = eventPublisher;
        this.validator = validator;
        this.statusLifecycle = statusLifecycle;
    }

    @Transactional
    public void processAcceptedTrade(TradeEvent event) {
        validateEvent(event);
        Trade trade = tradeRepository.findById(event.getTradeId())
                .orElseThrow(() -> new TradeProcessingException("Trade was not found: " + event.getTradeId()));

        if (trade.getStatus() == TradeStatus.VALIDATED || trade.getStatus() == TradeStatus.REJECTED) {
            log.info("trade_event_already_processed status={}", trade.getStatus());
            return;
        }
        if (trade.getStatus() != TradeStatus.RECEIVED) {
            throw new TradeProcessingException("Trade cannot be validated from status " + trade.getStatus());
        }
        if (!trade.getTradeReference().equals(event.getTradeReference())) {
            throw new TradeProcessingException("Event trade reference does not match the persisted trade");
        }

        java.util.List<String> validationErrors = validator.validate(trade);
        if (!validationErrors.isEmpty()) {
            statusLifecycle.transition(trade, TradeStatus.REJECTED, String.join("; ", validationErrors), event.getCorrelationId());
            tradeRepository.saveAndFlush(trade);
            log.warn("trade_validation_rejected errorCount={} errors={}", validationErrors.size(), validationErrors);
            eventPublisher.publishEvent(TradeEvent.validationRejected(trade, event.getCorrelationId(), validationErrors));
            return;
        }

        statusLifecycle.transition(trade, TradeStatus.VALIDATED, "Trade validation completed", event.getCorrelationId());
        tradeRepository.saveAndFlush(trade);
        log.info("trade_processing_status_updated previousStatus=RECEIVED status=VALIDATED");

        eventPublisher.publishEvent(TradeEvent.validationCompleted(trade, event.getCorrelationId()));
    }

    private void validateEvent(TradeEvent event) {
        if (event.getEventType() != TradeEventType.TRADE_ACCEPTED) {
            throw new TradeProcessingException("Unsupported event type: " + event.getEventType());
        }
        if (event.getEventId() == null || event.getTradeId() == null || event.getTradeReference() == null) {
            throw new TradeProcessingException("Trade event is missing required identifiers");
        }
    }
}

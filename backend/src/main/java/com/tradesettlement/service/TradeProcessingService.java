package com.tradesettlement.service;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.exception.TradeProcessingException;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.TradeRepository;
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

    public TradeProcessingService(TradeRepository tradeRepository, ApplicationEventPublisher eventPublisher) {
        this.tradeRepository = tradeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void processAcceptedTrade(TradeEvent event) {
        validateEvent(event);
        Trade trade = tradeRepository.findById(event.getTradeId())
                .orElseThrow(() -> new TradeProcessingException("Trade was not found: " + event.getTradeId()));

        if (trade.getStatus() == TradeStatus.VALIDATED) {
            log.info("trade_event_already_processed status={}", trade.getStatus());
            return;
        }
        if (trade.getStatus() != TradeStatus.RECEIVED && trade.getStatus() != TradeStatus.VALIDATING) {
            throw new TradeProcessingException("Trade cannot be validated from status " + trade.getStatus());
        }
        if (!trade.getTradeReference().equals(event.getTradeReference())) {
            throw new TradeProcessingException("Event trade reference does not match the persisted trade");
        }

        TradeStatus previousStatus = trade.getStatus();
        trade.setStatus(TradeStatus.VALIDATING);
        tradeRepository.saveAndFlush(trade);
        log.info("trade_processing_status_updated previousStatus={} status=VALIDATING", previousStatus);

        trade.setStatus(TradeStatus.VALIDATED);
        tradeRepository.saveAndFlush(trade);
        log.info("trade_processing_status_updated previousStatus=VALIDATING status=VALIDATED");

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

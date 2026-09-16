package com.tradesettlement.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tradesettlement.dto.TradeSubmissionRequest;
import com.tradesettlement.dto.TradeSubmissionResponse;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.exception.DuplicateTradeException;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.kafka.TradeLifecycleEvent;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.validation.TradeSubmissionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(TradeSubmissionService.class);

    private final TradeRepository tradeRepository;
    private final TradeSubmissionValidator validator;
    private final ApplicationEventPublisher eventPublisher;

    public TradeSubmissionService(TradeRepository tradeRepository, TradeSubmissionValidator validator,
                                  ApplicationEventPublisher eventPublisher) {
        this.tradeRepository = tradeRepository;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TradeSubmissionResponse submit(TradeSubmissionRequest request) {
        MDC.put("tradeReference", request.getTradeReference());
        try {
            log.info("trade_submission_started");
            if (tradeRepository.findByTradeReference(request.getTradeReference()).isPresent()) {
                throw new DuplicateTradeException(request.getTradeReference());
            }

            validator.validate(request);
            Trade trade = toEntity(request);
            Trade savedTrade = tradeRepository.saveAndFlush(trade);
            MDC.put("tradeId", savedTrade.getId().toString());
            eventPublisher.publishEvent(new TradeLifecycleEvent(
                    UUID.randomUUID(), savedTrade.getId(), savedTrade.getTradeReference(), savedTrade.getTradeType(),
                    savedTrade.getStatus(), TradeEventType.TRADE_SUBMITTED, OffsetDateTime.now(), MDC.get("requestId")));
            log.info("trade_submission_completed");
            return TradeSubmissionResponse.from(savedTrade);
        } catch (RuntimeException exception) {
            log.warn("trade_submission_failed", exception);
            throw exception;
        } finally {
            MDC.remove("tradeId");
            MDC.remove("tradeReference");
        }
    }

    private Trade toEntity(TradeSubmissionRequest request) {
        Trade trade = new Trade();
        trade.setTradeReference(request.getTradeReference());
        trade.setExternalReference(request.getExternalReference());
        trade.setTradeType(request.getTradeType());
        trade.setStatus(TradeStatus.RECEIVED);
        trade.setInstrumentId(request.getInstrumentId());
        trade.setBuyerCounterpartyId(request.getBuyerCounterpartyId());
        trade.setSellerCounterpartyId(request.getSellerCounterpartyId());
        trade.setQuantity(request.getQuantity());
        trade.setPrice(request.getPrice());
        trade.setCurrencyCode(request.getCurrencyCode());
        trade.setTradeDate(request.getTradeDate());
        trade.setSettlementDate(request.getSettlementDate());
        return trade;
    }
}

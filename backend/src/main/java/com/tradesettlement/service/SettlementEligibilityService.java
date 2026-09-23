package com.tradesettlement.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tradesettlement.entity.Counterparty;
import com.tradesettlement.entity.Instrument;
import com.tradesettlement.entity.SettlementEligibilityStatus;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.exception.TradeProcessingException;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.CounterpartyRepository;
import com.tradesettlement.repository.InstrumentRepository;
import com.tradesettlement.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementEligibilityService {
    private static final Logger log = LoggerFactory.getLogger(SettlementEligibilityService.class);
    private final TradeRepository trades; private final InstrumentRepository instruments;
    private final CounterpartyRepository counterparties; private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public SettlementEligibilityService(TradeRepository trades, InstrumentRepository instruments,
                                        CounterpartyRepository counterparties, ApplicationEventPublisher eventPublisher,
                                        Clock clock) {
        this.trades = trades; this.instruments = instruments; this.counterparties = counterparties;
        this.eventPublisher = eventPublisher; this.clock = clock;
    }

    @Transactional
    public void evaluate(TradeEvent event) {
        if (event.getEventType() != TradeEventType.ENRICHMENT_COMPLETED || event.getTradeId() == null) {
            throw new TradeProcessingException("Unsupported settlement eligibility event");
        }
        Trade trade = trades.findById(event.getTradeId())
                .orElseThrow(() -> new TradeProcessingException("Trade was not found: " + event.getTradeId()));
        if (trade.getEligibilityStatus() != SettlementEligibilityStatus.PENDING) {
            log.info("settlement_eligibility_already_evaluated eligibilityStatus={}", trade.getEligibilityStatus());
            return;
        }

        List<String> reasons = evaluateRules(trade);
        trade.setEligibilityCheckedAt(OffsetDateTime.now(clock));
        if (reasons.isEmpty()) {
            trade.setEligibilityStatus(SettlementEligibilityStatus.ELIGIBLE);
            trade.setEligibilityRejectionReason(null);
            trade.setStatus(TradeStatus.READY_FOR_SETTLEMENT);
            trades.saveAndFlush(trade);
            log.info("settlement_eligibility_confirmed settlementDate={} instrumentCode={}",
                    trade.getSettlementDate(), trade.getInstrumentCode());
            eventPublisher.publishEvent(TradeEvent.eligibilityConfirmed(trade, event.getCorrelationId()));
            return;
        }

        trade.setEligibilityStatus(SettlementEligibilityStatus.INELIGIBLE);
        trade.setEligibilityRejectionReason(String.join("; ", reasons));
        trades.saveAndFlush(trade);
        log.warn("settlement_eligibility_rejected reasonCount={} reasons={}", reasons.size(), reasons);
        eventPublisher.publishEvent(TradeEvent.eligibilityRejected(trade, event.getCorrelationId(), reasons));
    }

    private List<String> evaluateRules(Trade trade) {
        List<String> reasons = new ArrayList<>();
        if (trade.getStatus() != TradeStatus.ENRICHED) reasons.add("Trade has not completed validation and enrichment");
        LocalDate today = LocalDate.now(clock);
        if (trade.getSettlementDate() == null) reasons.add("Settlement date is missing");
        else if (trade.getSettlementDate().isAfter(today)) reasons.add("Settlement date is not yet due");

        Instrument instrument = instruments.findByIdAndActiveTrue(trade.getInstrumentId()).orElse(null);
        if (instrument == null) reasons.add("Instrument reference data is missing or inactive");
        else if (!instrument.isSettlementSupported()) reasons.add("Instrument is not supported for settlement");
        Counterparty buyer = counterparties.findByIdAndActiveTrue(trade.getBuyerCounterpartyId()).orElse(null);
        Counterparty seller = counterparties.findByIdAndActiveTrue(trade.getSellerCounterpartyId()).orElse(null);
        if (buyer == null) reasons.add("Buyer counterparty reference data is missing or inactive");
        if (seller == null) reasons.add("Seller counterparty reference data is missing or inactive");
        return reasons;
    }
}

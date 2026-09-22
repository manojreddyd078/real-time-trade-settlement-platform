package com.tradesettlement.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.tradesettlement.entity.Counterparty;
import com.tradesettlement.entity.Instrument;
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
public class TradeEnrichmentService {
    private static final Logger log = LoggerFactory.getLogger(TradeEnrichmentService.class);
    private final TradeRepository trades;
    private final InstrumentRepository instruments;
    private final CounterpartyRepository counterparties;
    private final ApplicationEventPublisher eventPublisher;

    public TradeEnrichmentService(TradeRepository trades, InstrumentRepository instruments,
                                  CounterpartyRepository counterparties, ApplicationEventPublisher eventPublisher) {
        this.trades = trades;
        this.instruments = instruments;
        this.counterparties = counterparties;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void enrich(TradeEvent event) {
        if (event.getEventType() != TradeEventType.VALIDATION_COMPLETED || event.getTradeId() == null) {
            throw new TradeProcessingException("Unsupported enrichment event");
        }
        Trade trade = trades.findById(event.getTradeId())
                .orElseThrow(() -> new TradeProcessingException("Trade was not found: " + event.getTradeId()));
        if (trade.getStatus() == TradeStatus.ENRICHED || trade.getStatus() == TradeStatus.FAILED) {
            log.info("trade_enrichment_already_processed status={}", trade.getStatus());
            return;
        }
        if (trade.getStatus() != TradeStatus.VALIDATED) {
            throw new TradeProcessingException("Trade cannot be enriched from status " + trade.getStatus());
        }

        Instrument instrument = instruments.findByIdAndActiveTrue(trade.getInstrumentId()).orElse(null);
        Counterparty buyer = counterparties.findByIdAndActiveTrue(trade.getBuyerCounterpartyId()).orElse(null);
        Counterparty seller = counterparties.findByIdAndActiveTrue(trade.getSellerCounterpartyId()).orElse(null);
        List<String> errors = missingReferenceErrors(instrument, buyer, seller);
        if (!errors.isEmpty()) {
            trade.setStatus(TradeStatus.FAILED);
            trades.saveAndFlush(trade);
            log.warn("trade_enrichment_failed errorCount={} errors={}", errors.size(), errors);
            eventPublisher.publishEvent(TradeEvent.enrichmentFailed(trade, event.getCorrelationId(), errors));
            return;
        }

        trade.setInstrumentCode(instrument.getInstrumentCode());
        trade.setInstrumentName(instrument.getName());
        trade.setBuyerCounterpartyCode(buyer.getCounterpartyCode());
        trade.setBuyerCounterpartyName(buyer.getLegalName());
        trade.setSellerCounterpartyCode(seller.getCounterpartyCode());
        trade.setSellerCounterpartyName(seller.getLegalName());
        trade.setEnrichedAt(OffsetDateTime.now(ZoneOffset.UTC));
        trade.setStatus(TradeStatus.ENRICHED);
        trades.saveAndFlush(trade);
        log.info("trade_enrichment_completed instrumentCode={} buyerCode={} sellerCode={}",
                trade.getInstrumentCode(), trade.getBuyerCounterpartyCode(), trade.getSellerCounterpartyCode());
        eventPublisher.publishEvent(TradeEvent.enrichmentCompleted(trade, event.getCorrelationId()));
    }

    private List<String> missingReferenceErrors(Instrument instrument, Counterparty buyer, Counterparty seller) {
        List<String> errors = new ArrayList<>();
        if (instrument == null) errors.add("instrument is missing or inactive");
        if (buyer == null) errors.add("buyer counterparty is missing or inactive");
        if (seller == null) errors.add("seller counterparty is missing or inactive");
        return errors;
    }
}

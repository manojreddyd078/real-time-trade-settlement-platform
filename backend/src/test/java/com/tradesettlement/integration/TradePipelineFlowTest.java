package com.tradesettlement.integration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.tradesettlement.dto.TradeSubmissionRequest;
import com.tradesettlement.dto.TradeSubmissionResponse;
import com.tradesettlement.entity.Counterparty;
import com.tradesettlement.entity.Instrument;
import com.tradesettlement.entity.Settlement;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeStatusHistory;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.CounterpartyRepository;
import com.tradesettlement.repository.InstrumentRepository;
import com.tradesettlement.repository.SettlementRepository;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.repository.TradeStatusHistoryRepository;
import com.tradesettlement.service.SettlementEligibilityService;
import com.tradesettlement.service.SettlementService;
import com.tradesettlement.service.TradeEnrichmentService;
import com.tradesettlement.service.TradeProcessingService;
import com.tradesettlement.service.TradeStatusLifecycleService;
import com.tradesettlement.service.TradeSubmissionService;
import com.tradesettlement.settlement.SettlementExecutor;
import com.tradesettlement.validation.TradeProcessingValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Component-level pipeline contract: each event emitted by one stage is the input for the next.
 * Kafka transport, database, and HTTP adapters are separately covered by their focused tests.
 */
class TradePipelineFlowTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void processesSuccessfulTradeFromSubmissionThroughSettlementWithOneCorrelationId() {
        Pipeline fixture = new Pipeline();
        MDC.put("correlationId", fixture.correlationId);
        try {

            TradeSubmissionResponse submitted = fixture.submission.submit(fixture.request());
            TradeEvent accepted = fixture.nextEvent();
            fixture.processing.processAcceptedTrade(accepted);
            TradeEvent validated = fixture.nextEvent();
            fixture.enrichment.enrich(validated);
            TradeEvent enriched = fixture.nextEvent();
            fixture.eligibility.evaluate(enriched);
            TradeEvent eligible = fixture.nextEvent();
            fixture.settlement.process(eligible);
            TradeEvent settled = fixture.nextEvent();

            assertEquals(TradeStatus.SETTLED, fixture.trade().getStatus());
            assertEquals(TradeEventType.SETTLEMENT_COMPLETED, settled.getEventType());
            assertEquals(submitted.getId(), settled.getTradeId());
            assertEquals(fixture.correlationId, accepted.getCorrelationId());
            assertTrue(fixture.events.stream().allMatch(event -> fixture.correlationId.equals(event.getCorrelationId())));
            assertEquals(List.of(TradeStatus.RECEIVED, TradeStatus.VALIDATED, TradeStatus.ENRICHED,
                            TradeStatus.ELIGIBLE, TradeStatus.SETTLEMENT_PENDING, TradeStatus.SETTLED),
                    fixture.history.stream().map(TradeStatusHistory::getToStatus).collect(java.util.stream.Collectors.toList()));
            assertFalse(fixture.events.stream().map(TradeEvent::getEventId).anyMatch(java.util.Objects::isNull));
        } finally {
            MDC.clear();
        }
    }

    private static class Pipeline {
        final String correlationId = "flow-correlation-42";
        final AtomicReference<Trade> stored = new AtomicReference<>();
        final List<TradeEvent> events = new ArrayList<>();
        final List<TradeStatusHistory> history = new ArrayList<>();
        final TradeRepository trades = mock(TradeRepository.class);
        final InstrumentRepository instruments = mock(InstrumentRepository.class);
        final CounterpartyRepository counterparties = mock(CounterpartyRepository.class);
        final SettlementRepository settlements = mock(SettlementRepository.class);
        final TradeStatusHistoryRepository histories = mock(TradeStatusHistoryRepository.class);
        final ApplicationEventPublisher publisher = event -> events.add((TradeEvent) event);
        final TradeStatusLifecycleService lifecycle = new TradeStatusLifecycleService(histories, CLOCK);
        final TradeSubmissionService submission;
        final TradeProcessingService processing;
        final TradeEnrichmentService enrichment;
        final SettlementEligibilityService eligibility;
        final SettlementService settlement;

        Pipeline() {
            when(trades.findByTradeReference(anyString())).thenReturn(Optional.empty());
            when(trades.findByBusinessKey(anyString())).thenReturn(Optional.empty());
            when(trades.saveAndFlush(any(Trade.class))).thenAnswer(invocation -> {
                Trade trade = invocation.getArgument(0);
                if (trade.getId() == null) ReflectionTestUtils.setField(trade, "id", UUID.randomUUID());
                stored.set(trade);
                return trade;
            });
            when(trades.findById(any(UUID.class))).thenAnswer(invocation -> Optional.ofNullable(stored.get()));
            when(histories.save(any(TradeStatusHistory.class))).thenAnswer(invocation -> {
                TradeStatusHistory entry = invocation.getArgument(0); history.add(entry); return entry;
            });
            when(settlements.findByTradeId(any(UUID.class))).thenReturn(Optional.empty());
            when(settlements.saveAndFlush(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Instrument instrument = new Instrument(); instrument.setInstrumentCode("UST-10Y"); instrument.setName("US Treasury Note"); instrument.setSettlementSupported(true);
            Counterparty buyer = counterparty("BUYER", "Buyer Bank"); Counterparty seller = counterparty("SELLER", "Seller Bank");
            when(instruments.findByIdAndActiveTrue(any(UUID.class))).thenReturn(Optional.of(instrument));
            when(counterparties.findByIdAndActiveTrue(storedId(0))).thenReturn(Optional.of(buyer));
            when(counterparties.findByIdAndActiveTrue(storedId(1))).thenReturn(Optional.of(seller));
            TradeProcessingValidator validator = mock(TradeProcessingValidator.class);
            when(validator.validate(any(Trade.class))).thenReturn(List.of());
            SettlementExecutor executor = mock(SettlementExecutor.class);

            submission = new TradeSubmissionService(trades, publisher, lifecycle);
            processing = new TradeProcessingService(trades, publisher, validator, lifecycle);
            enrichment = new TradeEnrichmentService(trades, instruments, counterparties, publisher, lifecycle);
            eligibility = new SettlementEligibilityService(trades, instruments, counterparties, publisher, CLOCK, lifecycle);
            settlement = new SettlementService(trades, settlements, executor, publisher, CLOCK, lifecycle);
        }

        private UUID storedId(int index) {
            // Match the request IDs without coupling the repository stubs to generated entity state.
            return index == 0 ? REQUEST_BUYER : REQUEST_SELLER;
        }

        TradeEvent nextEvent() { return events.get(events.size() - 1); }
        Trade trade() { return stored.get(); }
        TradeSubmissionRequest request() {
            TradeSubmissionRequest request = new TradeSubmissionRequest();
            request.setTradeReference("TRD-FLOW-1001"); request.setTradeType(TradeType.BUY);
            request.setInstrumentId(REQUEST_INSTRUMENT); request.setBuyerCounterpartyId(REQUEST_BUYER); request.setSellerCounterpartyId(REQUEST_SELLER);
            request.setQuantity(new BigDecimal("100")); request.setPrice(new BigDecimal("25.00")); request.setCurrencyCode("USD");
            request.setTradeDate(LocalDate.of(2026, 10, 6)); request.setSettlementDate(LocalDate.of(2026, 10, 6));
            return request;
        }
    }

    private static final UUID REQUEST_INSTRUMENT = UUID.fromString("ac38d8e2-6e41-4458-b901-43851cbf19e6");
    private static final UUID REQUEST_BUYER = UUID.fromString("4b5588ed-c391-4079-b14c-34c49a21949a");
    private static final UUID REQUEST_SELLER = UUID.fromString("5e884423-f880-4534-89d2-526542164975");

    private static Counterparty counterparty(String code, String name) {
        Counterparty value = new Counterparty(); value.setCounterpartyCode(code); value.setLegalName(name); return value;
    }
}

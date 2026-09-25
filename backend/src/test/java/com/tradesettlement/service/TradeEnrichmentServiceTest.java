package com.tradesettlement.service;

import java.util.Optional;
import java.util.UUID;
import com.tradesettlement.entity.Counterparty;
import com.tradesettlement.entity.Instrument;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.CounterpartyRepository;
import com.tradesettlement.repository.InstrumentRepository;
import com.tradesettlement.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeEnrichmentServiceTest {
    @Test
    void enrichesTradeAndPublishesCompletedEvent() {
        Fixture fixture = new Fixture();
        Instrument instrument = instrument(); Counterparty buyer = counterparty("BUYER", "Buyer Bank");
        Counterparty seller = counterparty("SELLER", "Seller Bank");
        when(fixture.instruments.findByIdAndActiveTrue(fixture.trade.getInstrumentId())).thenReturn(Optional.of(instrument));
        when(fixture.counterparties.findByIdAndActiveTrue(fixture.trade.getBuyerCounterpartyId())).thenReturn(Optional.of(buyer));
        when(fixture.counterparties.findByIdAndActiveTrue(fixture.trade.getSellerCounterpartyId())).thenReturn(Optional.of(seller));
        fixture.service.enrich(fixture.event);
        assertEquals(TradeStatus.ENRICHED, fixture.trade.getStatus());
        assertEquals("UST-10Y", fixture.trade.getInstrumentCode());
        assertEquals("Buyer Bank", fixture.trade.getBuyerCounterpartyName());
        assertNotNull(fixture.trade.getEnrichedAt());
        verify(fixture.trades).saveAndFlush(fixture.trade);
        assertEquals(TradeEventType.ENRICHMENT_COMPLETED, fixture.published().getEventType());
    }

    @Test
    void marksTradeFailedAndPublishesMissingReferences() {
        Fixture fixture = new Fixture();
        fixture.service.enrich(fixture.event);
        assertEquals(TradeStatus.FAILED, fixture.trade.getStatus());
        TradeEvent published = fixture.published();
        assertEquals(TradeEventType.ENRICHMENT_FAILED, published.getEventType());
        assertEquals(3, published.getValidationErrors().size());
    }

    private static Instrument instrument() { Instrument v = new Instrument(); v.setInstrumentCode("UST-10Y"); v.setName("US Treasury Note 10Y"); return v; }
    private static Counterparty counterparty(String code, String name) { Counterparty v = new Counterparty(); v.setCounterpartyCode(code); v.setLegalName(name); return v; }
    private static class Fixture {
        final TradeRepository trades = mock(TradeRepository.class); final InstrumentRepository instruments = mock(InstrumentRepository.class);
        final CounterpartyRepository counterparties = mock(CounterpartyRepository.class); final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        final TradeStatusLifecycleService lifecycle = new TradeStatusLifecycleService(
                mock(com.tradesettlement.repository.TradeStatusHistoryRepository.class), java.time.Clock.systemUTC());
        final TradeEnrichmentService service = new TradeEnrichmentService(trades, instruments, counterparties, publisher, lifecycle);
        final Trade trade = trade();
        final TradeEvent event = new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(), TradeStatus.VALIDATED, TradeEventType.VALIDATION_COMPLETED, java.time.OffsetDateTime.now(), "correlation-123");
        Fixture() { when(trades.findById(trade.getId())).thenReturn(Optional.of(trade)); }
        TradeEvent published() { ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class); verify(publisher).publishEvent(captor.capture()); return (TradeEvent) captor.getValue(); }
        private static Trade trade() { Trade v = new Trade(); ReflectionTestUtils.setField(v, "id", UUID.randomUUID()); v.setTradeReference("TRD-1"); v.setTradeType(TradeType.BUY); v.setStatus(TradeStatus.VALIDATED); v.setInstrumentId(UUID.randomUUID()); v.setBuyerCounterpartyId(UUID.randomUUID()); v.setSellerCounterpartyId(UUID.randomUUID()); return v; }
    }
}

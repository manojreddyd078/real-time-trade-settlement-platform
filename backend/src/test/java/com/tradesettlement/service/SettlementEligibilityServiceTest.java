package com.tradesettlement.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import com.tradesettlement.entity.Counterparty;
import com.tradesettlement.entity.Instrument;
import com.tradesettlement.entity.SettlementEligibilityStatus;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettlementEligibilityServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-23T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void confirmsEligibleValidatedDueTradeWithSupportedReferences() {
        Fixture fixture = new Fixture();
        fixture.enableReferences(true);

        fixture.service.evaluate(fixture.event);

        assertEquals(SettlementEligibilityStatus.ELIGIBLE, fixture.trade.getEligibilityStatus());
        assertEquals(TradeStatus.READY_FOR_SETTLEMENT, fixture.trade.getStatus());
        assertEquals(TradeEventType.ELIGIBILITY_CONFIRMED, fixture.published().getEventType());
    }

    @Test
    void rejectsFutureDatedUnsupportedInstrument() {
        Fixture fixture = new Fixture();
        fixture.trade.setSettlementDate(LocalDate.of(2026, 9, 24));
        fixture.enableReferences(false);

        fixture.service.evaluate(fixture.event);

        assertEquals(SettlementEligibilityStatus.INELIGIBLE, fixture.trade.getEligibilityStatus());
        assertEquals(TradeStatus.ENRICHED, fixture.trade.getStatus());
        assertTrue(fixture.trade.getEligibilityRejectionReason().contains("Settlement date is not yet due"));
        assertTrue(fixture.trade.getEligibilityRejectionReason().contains("Instrument is not supported"));
        TradeEvent result = fixture.published();
        assertEquals(TradeEventType.ELIGIBILITY_REJECTED, result.getEventType());
        assertEquals(2, result.getEligibilityReasons().size());
    }

    @Test
    void rejectsMissingReferenceData() {
        Fixture fixture = new Fixture();

        fixture.service.evaluate(fixture.event);

        assertEquals(SettlementEligibilityStatus.INELIGIBLE, fixture.trade.getEligibilityStatus());
        assertEquals(3, fixture.published().getEligibilityReasons().size());
    }

    private static class Fixture {
        final TradeRepository trades = mock(TradeRepository.class);
        final InstrumentRepository instruments = mock(InstrumentRepository.class);
        final CounterpartyRepository counterparties = mock(CounterpartyRepository.class);
        final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        final SettlementEligibilityService service = new SettlementEligibilityService(trades, instruments, counterparties, publisher, CLOCK);
        final Trade trade = trade();
        final TradeEvent event = new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(),
                trade.getStatus(), TradeEventType.ENRICHMENT_COMPLETED, java.time.OffsetDateTime.now(CLOCK), "correlation-123");
        Fixture() { when(trades.findById(trade.getId())).thenReturn(Optional.of(trade)); }
        void enableReferences(boolean supported) {
            Instrument instrument = new Instrument(); instrument.setSettlementSupported(supported);
            when(instruments.findByIdAndActiveTrue(trade.getInstrumentId())).thenReturn(Optional.of(instrument));
            when(counterparties.findByIdAndActiveTrue(trade.getBuyerCounterpartyId())).thenReturn(Optional.of(new Counterparty()));
            when(counterparties.findByIdAndActiveTrue(trade.getSellerCounterpartyId())).thenReturn(Optional.of(new Counterparty()));
        }
        TradeEvent published() {
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(publisher).publishEvent(captor.capture()); return (TradeEvent) captor.getValue();
        }
        static Trade trade() {
            Trade value = new Trade(); ReflectionTestUtils.setField(value, "id", UUID.randomUUID());
            value.setTradeReference("TRD-ELIGIBLE-1"); value.setTradeType(TradeType.BUY); value.setStatus(TradeStatus.ENRICHED);
            value.setInstrumentId(UUID.randomUUID()); value.setBuyerCounterpartyId(UUID.randomUUID()); value.setSellerCounterpartyId(UUID.randomUUID());
            value.setSettlementDate(LocalDate.of(2026, 9, 23)); return value;
        }
    }
}

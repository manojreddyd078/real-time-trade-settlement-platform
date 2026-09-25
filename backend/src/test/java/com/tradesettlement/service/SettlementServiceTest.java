package com.tradesettlement.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import com.tradesettlement.entity.Settlement;
import com.tradesettlement.entity.SettlementEligibilityStatus;
import com.tradesettlement.entity.SettlementStatus;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.repository.SettlementRepository;
import com.tradesettlement.repository.TradeRepository;
import com.tradesettlement.settlement.SettlementExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettlementServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-24T14:00:00Z"), ZoneOffset.UTC);

    @Test
    void settlesEligibleTradeAndPublishesCompletion() {
        Fixture fixture = new Fixture();

        fixture.service.process(fixture.event);

        assertEquals(TradeStatus.SETTLED, fixture.trade.getStatus());
        TradeEvent result = fixture.published();
        assertEquals(TradeEventType.SETTLEMENT_COMPLETED, result.getEventType());
        assertNotNull(result.getSettlementId());
        assertNotNull(result.getSettlementReference());
        ArgumentCaptor<Settlement> settlement = ArgumentCaptor.forClass(Settlement.class);
        verify(fixture.executor).execute(org.mockito.ArgumentMatchers.eq(fixture.trade), settlement.capture());
        assertEquals(SettlementStatus.SETTLED, settlement.getValue().getStatus());
        assertEquals(new BigDecimal("2500.00"), settlement.getValue().getAmount());
    }

    @Test
    void persistsFailureAndPublishesFailureEvent() {
        Fixture fixture = new Fixture();
        doThrow(new IllegalStateException("custodian unavailable")).when(fixture.executor)
                .execute(org.mockito.ArgumentMatchers.eq(fixture.trade), org.mockito.ArgumentMatchers.any(Settlement.class));

        fixture.service.process(fixture.event);

        assertEquals(TradeStatus.FAILED, fixture.trade.getStatus());
        TradeEvent result = fixture.published();
        assertEquals(TradeEventType.SETTLEMENT_FAILED, result.getEventType());
        assertEquals("custodian unavailable", result.getSettlementFailureReason());
    }

    @Test
    void doesNotRepeatTerminalSettlement() {
        Fixture fixture = new Fixture(); Settlement existing = new Settlement(); existing.setStatus(SettlementStatus.SETTLED);
        existing.setId(UUID.randomUUID()); when(fixture.settlements.findByTradeId(fixture.trade.getId())).thenReturn(Optional.of(existing));

        fixture.service.process(fixture.event);

        verify(fixture.executor, never()).execute(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(fixture.publisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    private static class Fixture {
        final TradeRepository trades = mock(TradeRepository.class); final SettlementRepository settlements = mock(SettlementRepository.class);
        final SettlementExecutor executor = mock(SettlementExecutor.class); final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        final TradeStatusLifecycleService lifecycle = new TradeStatusLifecycleService(
                mock(com.tradesettlement.repository.TradeStatusHistoryRepository.class), CLOCK);
        final SettlementService service = new SettlementService(trades, settlements, executor, publisher, CLOCK, lifecycle);
        final Trade trade = trade();
        final TradeEvent event = new TradeEvent(UUID.randomUUID(), trade.getId(), trade.getTradeReference(), trade.getTradeType(), trade.getStatus(), TradeEventType.ELIGIBILITY_CONFIRMED, java.time.OffsetDateTime.now(CLOCK), "correlation-123");
        Fixture() { when(trades.findById(trade.getId())).thenReturn(Optional.of(trade)); when(settlements.findByTradeId(trade.getId())).thenReturn(Optional.empty()); }
        TradeEvent published() { ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class); verify(publisher).publishEvent(captor.capture()); return (TradeEvent) captor.getValue(); }
        static Trade trade() { Trade value = new Trade(); ReflectionTestUtils.setField(value, "id", UUID.randomUUID()); value.setTradeReference("TRD-STL-1"); value.setTradeType(TradeType.BUY); value.setStatus(TradeStatus.ELIGIBLE); value.setEligibilityStatus(SettlementEligibilityStatus.ELIGIBLE); value.setQuantity(new BigDecimal("100")); value.setPrice(new BigDecimal("25.00")); value.setCurrencyCode("USD"); return value; }
    }
}

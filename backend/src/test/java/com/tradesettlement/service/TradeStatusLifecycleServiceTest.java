package com.tradesettlement.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeStatusHistory;
import com.tradesettlement.exception.InvalidTradeStatusTransitionException;
import com.tradesettlement.repository.TradeStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TradeStatusLifecycleServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-25T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void persistsAllowedTransition() {
        TradeStatusHistoryRepository repository = mock(TradeStatusHistoryRepository.class);
        TradeStatusLifecycleService service = new TradeStatusLifecycleService(repository, CLOCK);
        Trade trade = trade(TradeStatus.RECEIVED);

        service.transition(trade, TradeStatus.VALIDATED, "Validation passed", "correlation-1");

        assertEquals(TradeStatus.VALIDATED, trade.getStatus());
        ArgumentCaptor<TradeStatusHistory> history = ArgumentCaptor.forClass(TradeStatusHistory.class);
        verify(repository).save(history.capture());
        assertEquals(TradeStatus.RECEIVED, history.getValue().getFromStatus());
        assertEquals(TradeStatus.VALIDATED, history.getValue().getToStatus());
        assertEquals("Validation passed", history.getValue().getReason());
        assertEquals("correlation-1", history.getValue().getCorrelationId());
    }

    @Test
    void rejectsInvalidTransitionWithoutChangingTrade() {
        TradeStatusHistoryRepository repository = mock(TradeStatusHistoryRepository.class);
        TradeStatusLifecycleService service = new TradeStatusLifecycleService(repository, CLOCK);
        Trade trade = trade(TradeStatus.RECEIVED);

        assertThrows(InvalidTradeStatusTransitionException.class,
                () -> service.transition(trade, TradeStatus.SETTLED, "skip stages", "correlation-1"));

        assertEquals(TradeStatus.RECEIVED, trade.getStatus());
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Trade trade(TradeStatus status) {
        Trade value = new Trade(); ReflectionTestUtils.setField(value, "id", UUID.randomUUID()); value.setStatus(status); return value;
    }
}

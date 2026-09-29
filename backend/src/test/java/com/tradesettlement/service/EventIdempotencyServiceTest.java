package com.tradesettlement.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventIdempotencyServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-29T14:00:00Z"), ZoneOffset.UTC);

    @Test
    void processesAndCompletesNewEvent() {
        ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
        TradeEvent event = event();
        Runnable handler = mock(Runnable.class);
        when(repository.claim(any(), anyString(), any(), anyString(), any())).thenReturn(1);

        boolean processed = new EventIdempotencyService(repository, CLOCK).processOnce(event, "validation", handler);

        assertTrue(processed);
        verify(handler).run();
        verify(repository).complete(event.getEventId(), "validation", java.time.OffsetDateTime.now(CLOCK));
    }

    @Test
    void skipsEventAlreadyClaimedByConsumer() {
        ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
        TradeEvent event = event();
        Runnable handler = mock(Runnable.class);
        when(repository.claim(any(), anyString(), any(), anyString(), any())).thenReturn(0);

        boolean processed = new EventIdempotencyService(repository, CLOCK).processOnce(event, "validation", handler);

        assertFalse(processed);
        verify(handler, never()).run();
        verify(repository, never()).complete(any(), anyString(), any());
    }

    @Test
    void doesNotCompleteFailedHandlerSoTransactionCanRetry() {
        ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
        TradeEvent event = event();
        when(repository.claim(any(), anyString(), any(), anyString(), any())).thenReturn(1);

        assertThrows(IllegalStateException.class, () -> new EventIdempotencyService(repository, CLOCK)
                .processOnce(event, "validation", () -> { throw new IllegalStateException("temporary failure"); }));

        verify(repository, never()).complete(any(), anyString(), any());
    }

    private TradeEvent event() {
        return new TradeEvent(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "TRD-IDEMPOTENT-1",
                TradeType.BUY, TradeStatus.RECEIVED, TradeEventType.TRADE_ACCEPTED,
                java.time.OffsetDateTime.now(CLOCK), "correlation-1");
    }
}

package com.tradesettlement.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import com.tradesettlement.repository.EventRetryAttemptRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class EventRetryServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-30T15:00:00Z"), ZoneOffset.UTC);

    @Test
    void persistsTransientFailureCounter() {
        EventRetryAttemptRepository attempts = mock(EventRetryAttemptRepository.class);
        UUID eventId = UUID.randomUUID(); UUID tradeId = UUID.randomUUID();
        RuntimeException failure = new RuntimeException("connection timed out");

        new EventRetryService(attempts, CLOCK).recordFailure(eventId, "group:topic", tradeId,
                "topic", 2, 3, failure);

        verify(attempts).recordFailure(eventId, "group:topic", tradeId, "topic", 2, 3,
                "connection timed out", java.time.OffsetDateTime.now(CLOCK));
    }

    @Test
    void marksExhaustedAndRecoveredStates() {
        EventRetryAttemptRepository attempts = mock(EventRetryAttemptRepository.class);
        UUID eventId = UUID.randomUUID(); EventRetryService service = new EventRetryService(attempts, CLOCK);
        service.markExhausted(eventId); service.markRecovered(eventId);
        verify(attempts).updateStatus(eventId, "EXHAUSTED");
        verify(attempts).updateStatus(eventId, "RECOVERED");
    }

    @Test
    void ignoresRetryUpdatesWithoutAnEventId() {
        EventRetryAttemptRepository attempts = mock(EventRetryAttemptRepository.class);
        EventRetryService service = new EventRetryService(attempts, CLOCK);

        service.recordFailure(null, "group:topic", UUID.randomUUID(), "topic", 1, 3,
                new RuntimeException("failure"));
        service.markExhausted(null);
        service.markRecovered(null);

        verify(attempts, never()).recordFailure(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
        verify(attempts, never()).updateStatus(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());
    }
}

package com.tradesettlement.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import com.tradesettlement.repository.EventRetryAttemptRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}

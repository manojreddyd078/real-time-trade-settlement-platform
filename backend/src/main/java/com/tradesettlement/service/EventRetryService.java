package com.tradesettlement.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.tradesettlement.repository.EventRetryAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventRetryService {
    private final EventRetryAttemptRepository attempts; private final Clock clock;
    public EventRetryService(EventRetryAttemptRepository attempts, Clock clock) { this.attempts = attempts; this.clock = clock; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID eventId, String retryKey, UUID tradeId, String topic,
                              int retryCount, int maxRetries, Throwable failure) {
        if (eventId == null) return;
        attempts.recordFailure(eventId, retryKey, tradeId, topic, retryCount, maxRetries,
                safeMessage(failure), OffsetDateTime.now(clock));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markExhausted(UUID eventId) { if (eventId != null) attempts.updateStatus(eventId, "EXHAUSTED"); }

    @Transactional
    public void markRecovered(UUID eventId) { if (eventId != null) attempts.updateStatus(eventId, "RECOVERED"); }

    private String safeMessage(Throwable failure) {
        String message = failure == null ? null : failure.getMessage();
        if (message == null || message.isBlank()) message = failure == null ? "Unknown failure" : failure.getClass().getSimpleName();
        return message.substring(0, Math.min(message.length(), 1000));
    }
}

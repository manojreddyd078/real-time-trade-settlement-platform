package com.tradesettlement.dto;

import java.time.OffsetDateTime;
import java.util.List;
import com.tradesettlement.entity.EventRetryAttempt;

public class TradeRetryStatusResponse {
    private final int retryCount; private final int maxRetries; private final String status;
    private final String lastFailure; private final OffsetDateTime lastAttemptAt;

    private TradeRetryStatusResponse(int retryCount, int maxRetries, String status,
                                     String lastFailure, OffsetDateTime lastAttemptAt) {
        this.retryCount = retryCount; this.maxRetries = maxRetries; this.status = status;
        this.lastFailure = lastFailure; this.lastAttemptAt = lastAttemptAt;
    }

    public static TradeRetryStatusResponse from(List<EventRetryAttempt> attempts) {
        if (attempts.isEmpty()) return new TradeRetryStatusResponse(0, 0, "NOT_RETRIED", null, null);
        int count = attempts.stream().mapToInt(EventRetryAttempt::getRetryCount).sum();
        int limit = attempts.stream().mapToInt(EventRetryAttempt::getMaxRetries).max().orElse(0);
        String status = attempts.stream().anyMatch(value -> "EXHAUSTED".equals(value.getStatus())) ? "EXHAUSTED"
                : attempts.stream().anyMatch(value -> "RETRYING".equals(value.getStatus())) ? "RETRYING" : "RECOVERED";
        EventRetryAttempt latest = attempts.get(0);
        return new TradeRetryStatusResponse(count, limit, status, latest.getLastFailure(), latest.getLastAttemptAt());
    }

    public int getRetryCount() { return retryCount; } public int getMaxRetries() { return maxRetries; }
    public String getStatus() { return status; } public String getLastFailure() { return lastFailure; }
    public OffsetDateTime getLastAttemptAt() { return lastAttemptAt; }
}

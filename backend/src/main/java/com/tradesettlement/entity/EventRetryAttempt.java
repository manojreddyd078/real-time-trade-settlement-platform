package com.tradesettlement.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "event_retry_attempts", schema = "settlement")
@IdClass(EventRetryAttempt.Key.class)
public class EventRetryAttempt {
    @Id @Column(name = "event_id") private UUID eventId;
    @Id @Column(name = "retry_key", length = 200) private String retryKey;
    @Column(name = "trade_id") private UUID tradeId;
    @Column(length = 100) private String topic;
    @Column(name = "retry_count") private int retryCount;
    @Column(name = "max_retries") private int maxRetries;
    @Column(length = 20) private String status;
    @Column(name = "last_failure", length = 1000) private String lastFailure;
    @Column(name = "first_attempt_at") private OffsetDateTime firstAttemptAt;
    @Column(name = "last_attempt_at") private OffsetDateTime lastAttemptAt;

    public UUID getEventId() { return eventId; } public String getRetryKey() { return retryKey; }
    public UUID getTradeId() { return tradeId; } public String getTopic() { return topic; }
    public int getRetryCount() { return retryCount; } public int getMaxRetries() { return maxRetries; }
    public String getStatus() { return status; } public String getLastFailure() { return lastFailure; }
    public OffsetDateTime getFirstAttemptAt() { return firstAttemptAt; } public OffsetDateTime getLastAttemptAt() { return lastAttemptAt; }

    public static class Key implements Serializable {
        private UUID eventId; private String retryKey;
        public Key() { } public Key(UUID eventId, String retryKey) { this.eventId = eventId; this.retryKey = retryKey; }
        @Override public boolean equals(Object value) { if (this == value) return true; if (!(value instanceof Key)) return false; Key key = (Key) value; return Objects.equals(eventId, key.eventId) && Objects.equals(retryKey, key.retryKey); }
        @Override public int hashCode() { return Objects.hash(eventId, retryKey); }
    }
}

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
@Table(name = "processed_events", schema = "settlement")
@IdClass(ProcessedEvent.Key.class)
public class ProcessedEvent {
    @Id @Column(name = "event_id", nullable = false) private UUID eventId;
    @Id @Column(name = "consumer_name", nullable = false, length = 100) private String consumerName;
    @Column(name = "trade_id") private UUID tradeId;
    @Column(name = "event_type", nullable = false, length = 50) private String eventType;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "received_at", nullable = false) private OffsetDateTime receivedAt;
    @Column(name = "processed_at") private OffsetDateTime processedAt;

    public UUID getEventId() { return eventId; }
    public String getConsumerName() { return consumerName; }
    public UUID getTradeId() { return tradeId; }
    public String getEventType() { return eventType; }
    public String getStatus() { return status; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public OffsetDateTime getProcessedAt() { return processedAt; }

    public static class Key implements Serializable {
        private UUID eventId; private String consumerName;
        public Key() { }
        public Key(UUID eventId, String consumerName) { this.eventId = eventId; this.consumerName = consumerName; }
        @Override public boolean equals(Object value) { if (this == value) return true; if (!(value instanceof Key)) return false; Key key = (Key) value; return Objects.equals(eventId, key.eventId) && Objects.equals(consumerName, key.consumerName); }
        @Override public int hashCode() { return Objects.hash(eventId, consumerName); }
    }
}

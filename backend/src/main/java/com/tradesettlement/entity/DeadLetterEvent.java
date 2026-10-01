package com.tradesettlement.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dead_letter_events", schema = "settlement")
public class DeadLetterEvent {
    @Id private UUID id;
    @Column(name = "event_id") private UUID eventId;
    @Column(name = "trade_id") private UUID tradeId;
    @Column(name = "trade_reference", length = 64) private String tradeReference;
    @Column(name = "event_type", length = 50) private String eventType;
    @Column(name = "correlation_id", length = 100) private String correlationId;
    @Column(name = "original_topic", nullable = false, length = 100) private String originalTopic;
    @Column(name = "original_partition", nullable = false) private int originalPartition;
    @Column(name = "original_offset", nullable = false) private long originalOffset;
    @Column(name = "original_consumer_group", length = 100) private String originalConsumerGroup;
    @Column(name = "original_timestamp") private Long originalTimestamp;
    @Column(name = "original_key", length = 200) private String originalKey;
    @Column(name = "dlq_topic", nullable = false, length = 100) private String dlqTopic;
    @Column(name = "dlq_partition", nullable = false) private int dlqPartition;
    @Column(name = "dlq_offset", nullable = false) private long dlqOffset;
    @Column(name = "failure_class", length = 300) private String failureClass;
    @Column(name = "failure_reason", nullable = false, length = 2000) private String failureReason;
    @Column(name = "payload_json", columnDefinition = "TEXT") private String payloadJson;
    @Column(name = "headers_json", columnDefinition = "TEXT") private String headersJson;
    @Column(name = "received_at", nullable = false) private OffsetDateTime receivedAt;

    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public UUID getEventId() { return eventId; } public void setEventId(UUID value) { eventId = value; }
    public UUID getTradeId() { return tradeId; } public void setTradeId(UUID value) { tradeId = value; }
    public String getTradeReference() { return tradeReference; } public void setTradeReference(String value) { tradeReference = value; }
    public String getEventType() { return eventType; } public void setEventType(String value) { eventType = value; }
    public String getCorrelationId() { return correlationId; } public void setCorrelationId(String value) { correlationId = value; }
    public String getOriginalTopic() { return originalTopic; } public void setOriginalTopic(String value) { originalTopic = value; }
    public int getOriginalPartition() { return originalPartition; } public void setOriginalPartition(int value) { originalPartition = value; }
    public long getOriginalOffset() { return originalOffset; } public void setOriginalOffset(long value) { originalOffset = value; }
    public String getOriginalConsumerGroup() { return originalConsumerGroup; } public void setOriginalConsumerGroup(String value) { originalConsumerGroup = value; }
    public Long getOriginalTimestamp() { return originalTimestamp; } public void setOriginalTimestamp(Long value) { originalTimestamp = value; }
    public String getOriginalKey() { return originalKey; } public void setOriginalKey(String value) { originalKey = value; }
    public String getDlqTopic() { return dlqTopic; } public void setDlqTopic(String value) { dlqTopic = value; }
    public int getDlqPartition() { return dlqPartition; } public void setDlqPartition(int value) { dlqPartition = value; }
    public long getDlqOffset() { return dlqOffset; } public void setDlqOffset(long value) { dlqOffset = value; }
    public String getFailureClass() { return failureClass; } public void setFailureClass(String value) { failureClass = value; }
    public String getFailureReason() { return failureReason; } public void setFailureReason(String value) { failureReason = value; }
    public String getPayloadJson() { return payloadJson; } public void setPayloadJson(String value) { payloadJson = value; }
    public String getHeadersJson() { return headersJson; } public void setHeadersJson(String value) { headersJson = value; }
    public OffsetDateTime getReceivedAt() { return receivedAt; } public void setReceivedAt(OffsetDateTime value) { receivedAt = value; }
}

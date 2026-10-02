package com.tradesettlement.dto;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class TransactionTraceEntry {
    private final OffsetDateTime timestamp; private final String category; private final String stage;
    private final String status; private final UUID eventId; private final String correlationId;
    private final String description; private final Map<String, String> metadata;
    public TransactionTraceEntry(OffsetDateTime timestamp, String category, String stage, String status,
                                 UUID eventId, String correlationId, String description, Map<String, String> metadata) {
        this.timestamp = timestamp; this.category = category; this.stage = stage; this.status = status;
        this.eventId = eventId; this.correlationId = correlationId; this.description = description;
        this.metadata = metadata == null ? Collections.emptyMap() : metadata;
    }
    public OffsetDateTime getTimestamp() { return timestamp; } public String getCategory() { return category; }
    public String getStage() { return stage; } public String getStatus() { return status; }
    public UUID getEventId() { return eventId; } public String getCorrelationId() { return correlationId; }
    public String getDescription() { return description; } public Map<String, String> getMetadata() { return metadata; }
}

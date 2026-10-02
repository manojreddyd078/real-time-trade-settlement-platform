package com.tradesettlement.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.tradesettlement.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEvent.Key> {
    @Modifying
    @Query(value = "INSERT INTO settlement.processed_events (event_id, consumer_name, trade_id, event_type, correlation_id, event_occurred_at, status, received_at) " +
            "VALUES (:eventId, :consumerName, :tradeId, :eventType, :correlationId, :eventOccurredAt, 'PROCESSING', :receivedAt) " +
            "ON CONFLICT (event_id, consumer_name) DO NOTHING", nativeQuery = true)
    int claim(@Param("eventId") UUID eventId, @Param("consumerName") String consumerName,
              @Param("tradeId") UUID tradeId, @Param("eventType") String eventType,
              @Param("correlationId") String correlationId, @Param("eventOccurredAt") OffsetDateTime eventOccurredAt,
              @Param("receivedAt") OffsetDateTime receivedAt);

    @Modifying
    @Query("UPDATE ProcessedEvent event SET event.status = 'COMPLETED', event.processedAt = :processedAt " +
            "WHERE event.eventId = :eventId AND event.consumerName = :consumerName")
    int complete(@Param("eventId") UUID eventId, @Param("consumerName") String consumerName,
                 @Param("processedAt") OffsetDateTime processedAt);

    java.util.List<ProcessedEvent> findByTradeIdOrderByReceivedAtAsc(UUID tradeId);
}

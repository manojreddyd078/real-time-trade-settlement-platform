package com.tradesettlement.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradesettlement.entity.DeadLetterEvent;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.kafka.TradeEventType;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.repository.DeadLetterEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeadLetterServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneOffset.UTC);

    @Test
    void storesFailureAndOriginalEventMetadata() {
        DeadLetterEventRepository repository = mock(DeadLetterEventRepository.class);
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        TradeEvent event = new TradeEvent(UUID.randomUUID(), UUID.randomUUID(), "TRD-DLQ-1", TradeType.BUY,
                TradeStatus.ENRICHED, TradeEventType.ENRICHMENT_COMPLETED,
                java.time.OffsetDateTime.now(CLOCK), "correlation-dlq-1");
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("trade-dlq", 2, 91L, "trade-key", event);
        record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, "trade-enrichment".getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.DLT_ORIGINAL_PARTITION, ByteBuffer.allocate(4).putInt(1).array());
        record.headers().add(KafkaHeaders.DLT_ORIGINAL_OFFSET, ByteBuffer.allocate(8).putLong(44L).array());
        record.headers().add(KafkaHeaders.DLT_ORIGINAL_CONSUMER_GROUP, "settlement-service".getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.DLT_EXCEPTION_FQCN, "java.sql.SQLTransientException".getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.DLT_EXCEPTION_MESSAGE, "database unavailable".getBytes(StandardCharsets.UTF_8));

        DeadLetterEvent result = new DeadLetterService(repository,
                new ObjectMapper().findAndRegisterModules(), CLOCK).store(record);

        assertEquals(event.getEventId(), result.getEventId());
        assertEquals(event.getTradeId(), result.getTradeId());
        assertEquals("trade-enrichment", result.getOriginalTopic());
        assertEquals(1, result.getOriginalPartition()); assertEquals(44L, result.getOriginalOffset());
        assertEquals("settlement-service", result.getOriginalConsumerGroup());
        assertEquals("database unavailable", result.getFailureReason());
        assertEquals("java.sql.SQLTransientException", result.getFailureClass());
        assertEquals(java.time.OffsetDateTime.now(CLOCK), result.getReceivedAt());
        assertTrue(result.getPayloadJson().contains(event.getEventId().toString()));
        assertTrue(result.getHeadersJson().contains(KafkaHeaders.DLT_ORIGINAL_TOPIC));
        verify(repository).saveAndFlush(result);
    }

    @Test
    void doesNotStoreSameOriginalRecordTwice() {
        DeadLetterEventRepository repository = mock(DeadLetterEventRepository.class);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("trade-dlq", 0, 1L, "key", null);
        DeadLetterEvent existing = new DeadLetterEvent(); existing.setId(UUID.randomUUID());
        when(repository.existsByOriginalTopicAndOriginalPartitionAndOriginalOffsetAndOriginalConsumerGroup(
                "trade-dlq", 0, 1L, "unknown-group")).thenReturn(true);
        when(repository.findByOriginalTopicAndOriginalPartitionAndOriginalOffsetAndOriginalConsumerGroup(
                "trade-dlq", 0, 1L, "unknown-group")).thenReturn(java.util.Optional.of(existing));

        DeadLetterEvent result = new DeadLetterService(repository,
                new ObjectMapper().findAndRegisterModules(), CLOCK).store(record);

        assertEquals(existing.getId(), result.getId());
        verify(repository, org.mockito.Mockito.never()).saveAndFlush(any());
    }
}

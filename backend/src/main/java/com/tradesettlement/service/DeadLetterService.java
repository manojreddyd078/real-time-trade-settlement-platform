package com.tradesettlement.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradesettlement.entity.DeadLetterEvent;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.repository.DeadLetterEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeadLetterService {
    private final DeadLetterEventRepository deadLetters; private final ObjectMapper objectMapper; private final Clock clock;
    public DeadLetterService(DeadLetterEventRepository deadLetters, ObjectMapper objectMapper, Clock clock) {
        this.deadLetters = deadLetters; this.objectMapper = objectMapper; this.clock = clock;
    }

    @Transactional
    public DeadLetterEvent store(ConsumerRecord<String, Object> record) {
        String originalTopic = stringHeader(record, KafkaHeaders.DLT_ORIGINAL_TOPIC, record.topic());
        int originalPartition = intHeader(record, KafkaHeaders.DLT_ORIGINAL_PARTITION, record.partition());
        long originalOffset = longHeader(record, KafkaHeaders.DLT_ORIGINAL_OFFSET, record.offset());
        String consumerGroup = stringHeader(record, KafkaHeaders.DLT_ORIGINAL_CONSUMER_GROUP, "unknown-group");
        if (deadLetters.existsByOriginalTopicAndOriginalPartitionAndOriginalOffsetAndOriginalConsumerGroup(
                originalTopic, originalPartition, originalOffset, consumerGroup)) {
            return deadLetters.findByOriginalTopicAndOriginalPartitionAndOriginalOffsetAndOriginalConsumerGroup(
                    originalTopic, originalPartition, originalOffset, consumerGroup).orElse(null);
        }
        TradeEvent event = record.value() instanceof TradeEvent ? (TradeEvent) record.value() : null;
        DeadLetterEvent value = new DeadLetterEvent(); value.setId(UUID.randomUUID());
        if (event != null) {
            value.setEventId(event.getEventId()); value.setTradeId(event.getTradeId());
            value.setTradeReference(event.getTradeReference());
            value.setEventType(event.getEventType() == null ? null : event.getEventType().name());
            value.setCorrelationId(event.getCorrelationId());
        }
        value.setOriginalTopic(originalTopic); value.setOriginalPartition(originalPartition);
        value.setOriginalOffset(originalOffset); value.setOriginalConsumerGroup(consumerGroup);
        value.setOriginalTimestamp(nullableLongHeader(record, KafkaHeaders.DLT_ORIGINAL_TIMESTAMP));
        value.setOriginalKey(record.key()); value.setDlqTopic(record.topic());
        value.setDlqPartition(record.partition()); value.setDlqOffset(record.offset());
        value.setFailureClass(stringHeader(record, KafkaHeaders.DLT_EXCEPTION_FQCN, "UnknownFailure"));
        value.setFailureReason(stringHeader(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE, "Event processing permanently failed"));
        value.setPayloadJson(json(record.value())); value.setHeadersJson(headersJson(record));
        value.setReceivedAt(OffsetDateTime.now(clock)); return deadLetters.saveAndFlush(value);
    }

    private String headersJson(ConsumerRecord<?, ?> record) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (Header header : record.headers()) headers.computeIfAbsent(header.key(), ignored -> new ArrayList<>())
                .add(header.value() == null ? null : Base64.getEncoder().encodeToString(header.value()));
        return json(headers);
    }
    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { return String.valueOf(value); }
    }
    private String stringHeader(ConsumerRecord<?, ?> record, String name, String fallback) {
        Header header = record.headers().lastHeader(name);
        return header == null || header.value() == null ? fallback : new String(header.value(), StandardCharsets.UTF_8);
    }
    private int intHeader(ConsumerRecord<?, ?> record, String name, int fallback) {
        Long value = nullableLongHeader(record, name); return value == null ? fallback : value.intValue();
    }
    private long longHeader(ConsumerRecord<?, ?> record, String name, long fallback) {
        Long value = nullableLongHeader(record, name); return value == null ? fallback : value;
    }
    private Long nullableLongHeader(ConsumerRecord<?, ?> record, String name) {
        Header header = record.headers().lastHeader(name);
        if (header == null || header.value() == null) return null;
        byte[] bytes = header.value();
        if (bytes.length == Integer.BYTES) return (long) ByteBuffer.wrap(bytes).getInt();
        if (bytes.length == Long.BYTES) return ByteBuffer.wrap(bytes).getLong();
        try { return Long.parseLong(new String(bytes, StandardCharsets.UTF_8)); }
        catch (NumberFormatException ignored) { return null; }
    }
}

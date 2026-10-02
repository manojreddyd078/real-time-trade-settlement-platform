package com.tradesettlement.kafka;

import com.tradesettlement.service.EventIdempotencyService;
import com.tradesettlement.service.DeadLetterService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

@Component
public class TradeEventConsumers {

    private static final Logger log = LoggerFactory.getLogger(TradeEventConsumers.class);
    private final EventIdempotencyService idempotency;
    private final DeadLetterService deadLetters;

    public TradeEventConsumers(EventIdempotencyService idempotency, DeadLetterService deadLetters) {
        this.idempotency = idempotency; this.deadLetters = deadLetters;
    }

    @KafkaListener(topics = {"${app.kafka.topics.trade-settlement}", "${app.kafka.topics.trade-status}"},
            groupId = "${app.kafka.consumer-groups.status}")
    public void consumeForStatus(TradeEvent event) {
        if (event == null) throw new IllegalArgumentException("Trade event payload cannot be null");
        if (event.getEventId() != null) MDC.put("eventId", event.getEventId().toString());
        if (event.getTradeId() != null) MDC.put("tradeId", event.getTradeId().toString());
        if (event.getCorrelationId() != null) MDC.put("correlationId", event.getCorrelationId());
        try { idempotency.processOnce(event, "trade-status", () -> log.info("trade_event_received stage=status timestamp={}", event.getOccurredAt())); }
        finally { MDC.remove("eventId"); MDC.remove("tradeId"); MDC.remove("correlationId"); }
    }

    @KafkaListener(topics = "${app.kafka.topics.trade-dlq}", groupId = "${app.kafka.consumer-groups.dlq}")
    public void monitorDeadLetter(ConsumerRecord<String, Object> record) {
        com.tradesettlement.entity.DeadLetterEvent stored = deadLetters.store(record);
        log.error("trade_event_dead_lettered deadLetterId={} key={} partition={} offset={} valueType={}",
                stored == null ? null : stored.getId(),
                record.key(), record.partition(), record.offset(),
                record.value() == null ? "unavailable" : record.value().getClass().getName());
    }
}

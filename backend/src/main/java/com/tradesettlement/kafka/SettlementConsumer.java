package com.tradesettlement.kafka;

import com.tradesettlement.service.SettlementService;
import com.tradesettlement.service.EventIdempotencyService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

@Component
public class SettlementConsumer {
    private static final Logger log = LoggerFactory.getLogger(SettlementConsumer.class);
    private final SettlementService service;
    private final EventIdempotencyService idempotency;
    public SettlementConsumer(SettlementService service, EventIdempotencyService idempotency) {
        this.service = service; this.idempotency = idempotency;
    }

    @KafkaListener(topics = "${app.kafka.topics.trade-settlement}", groupId = "${app.kafka.consumer-groups.settlement}-processor")
    public void consume(ConsumerRecord<String, TradeEvent> record) {
        TradeEvent event = record.value();
        if (event == null) throw new IllegalArgumentException("Trade event payload cannot be null");
        putContext(event);
        try {
            log.info("kafka_event_received stage=settlement-processor topic={} partition={} offset={} timestamp={}",
                    record.topic(), record.partition(), record.offset(), record.timestamp());
            idempotency.processOnce(event, "settlement-processor", () -> {
                if (event.getEventType() == TradeEventType.ELIGIBILITY_REJECTED) {
                    log.info("settlement_skipped reason=ineligible"); return;
                }
                service.process(event);
            });
        } finally { clearContext(); }
    }
    private void putContext(TradeEvent event) { if (event.getEventId() != null) MDC.put("eventId", event.getEventId().toString()); if (event.getTradeId() != null) MDC.put("tradeId", event.getTradeId().toString()); if (event.getCorrelationId() != null) MDC.put("correlationId", event.getCorrelationId()); }
    private void clearContext() { MDC.remove("eventId"); MDC.remove("tradeId"); MDC.remove("correlationId"); }
}

package com.tradesettlement.kafka;

import com.tradesettlement.service.TradeEnrichmentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TradeEnrichmentConsumer {
    private static final Logger log = LoggerFactory.getLogger(TradeEnrichmentConsumer.class);
    private final TradeEnrichmentService enrichmentService;
    public TradeEnrichmentConsumer(TradeEnrichmentService enrichmentService) { this.enrichmentService = enrichmentService; }

    @KafkaListener(topics = "${app.kafka.topics.trade-validation}", groupId = "${app.kafka.consumer-groups.enrichment}")
    public void consume(ConsumerRecord<String, TradeEvent> record) {
        TradeEvent event = record.value();
        if (event == null) throw new IllegalArgumentException("Trade event payload cannot be null");
        if (event.getEventType() == TradeEventType.VALIDATION_REJECTED) {
            log.info("trade_enrichment_skipped reason=validation_rejected tradeId={}", event.getTradeId());
            return;
        }
        putContext(event);
        try {
            log.info("trade_enrichment_started topic={} partition={} offset={}", record.topic(), record.partition(), record.offset());
            enrichmentService.enrich(event);
        } catch (RuntimeException exception) {
            log.error("trade_enrichment_consumer_failed topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset(), exception);
            throw exception;
        } finally {
            MDC.remove("eventId"); MDC.remove("tradeId"); MDC.remove("correlationId");
        }
    }

    private void putContext(TradeEvent event) {
        if (event.getEventId() != null) MDC.put("eventId", event.getEventId().toString());
        if (event.getTradeId() != null) MDC.put("tradeId", event.getTradeId().toString());
        if (event.getCorrelationId() != null) MDC.put("correlationId", event.getCorrelationId());
    }
}

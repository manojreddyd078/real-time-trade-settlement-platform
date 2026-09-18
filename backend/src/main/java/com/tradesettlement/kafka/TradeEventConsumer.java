package com.tradesettlement.kafka;

import com.tradesettlement.service.TradeProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TradeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TradeEventConsumer.class);

    private final TradeProcessingService processingService;

    public TradeEventConsumer(TradeProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(topics = "${app.kafka.topics.trade-events}", groupId = "${app.kafka.consumer-groups.validation}")
    public void consume(ConsumerRecord<String, TradeEvent> record) {
        TradeEvent event = record.value();
        if (event == null) {
            throw new IllegalArgumentException("Trade event payload cannot be null");
        }

        putEventContext(event);
        try {
            log.info("trade_event_processing_started topic={} partition={} offset={} eventType={}",
                    record.topic(), record.partition(), record.offset(), event.getEventType());
            processingService.processAcceptedTrade(event);
            log.info("trade_event_processing_completed topic={} partition={} offset={} eventType={}",
                    record.topic(), record.partition(), record.offset(), event.getEventType());
        } catch (RuntimeException exception) {
            log.error("trade_event_processing_failed topic={} partition={} offset={} eventType={}",
                    record.topic(), record.partition(), record.offset(), event.getEventType(), exception);
            throw exception;
        } finally {
            clearEventContext();
        }
    }

    private void putEventContext(TradeEvent event) {
        if (event.getEventId() != null) MDC.put("eventId", event.getEventId().toString());
        if (event.getTradeId() != null) MDC.put("tradeId", event.getTradeId().toString());
        if (event.getCorrelationId() != null) MDC.put("correlationId", event.getCorrelationId());
    }

    private void clearEventContext() {
        MDC.remove("eventId");
        MDC.remove("tradeId");
        MDC.remove("correlationId");
    }
}

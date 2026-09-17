package com.tradesettlement.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TradeEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TradeEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String tradeEventsTopic;
    private final String validationTopic;
    private final String enrichmentTopic;
    private final String settlementTopic;
    private final String statusTopic;

    public TradeEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${app.kafka.topics.trade-events}") String tradeEventsTopic,
                              @Value("${app.kafka.topics.trade-validation}") String validationTopic,
                              @Value("${app.kafka.topics.trade-enrichment}") String enrichmentTopic,
                              @Value("${app.kafka.topics.trade-settlement}") String settlementTopic,
                              @Value("${app.kafka.topics.trade-status}") String statusTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.tradeEventsTopic = tradeEventsTopic;
        this.validationTopic = validationTopic;
        this.enrichmentTopic = enrichmentTopic;
        this.settlementTopic = settlementTopic;
        this.statusTopic = statusTopic;
    }

    public void publishAccepted(TradeEvent event) { send(tradeEventsTopic, event); }
    public void publishValidation(TradeEvent event) { send(validationTopic, event); }
    public void publishEnrichment(TradeEvent event) { send(enrichmentTopic, event); }
    public void publishSettlement(TradeEvent event) { send(settlementTopic, event); }
    public void publishStatus(TradeEvent event) { send(statusTopic, event); }

    private void send(String topic, TradeEvent event) {
        try {
            kafkaTemplate.send(topic, event.getTradeId().toString(), event).addCallback(
                    result -> log.info("kafka_event_published topic={} eventId={} tradeId={}", topic, event.getEventId(), event.getTradeId()),
                    error -> log.error("kafka_event_publish_failed topic={} eventId={} tradeId={}", topic, event.getEventId(), event.getTradeId(), error));
        } catch (RuntimeException error) {
            log.error("kafka_event_publish_failed topic={} eventId={} tradeId={}",
                    topic, event.getEventId(), event.getTradeId(), error);
        }
    }
}

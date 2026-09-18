package com.tradesettlement.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TradeEventConsumers {

    private static final Logger log = LoggerFactory.getLogger(TradeEventConsumers.class);

    @KafkaListener(topics = "${app.kafka.topics.trade-validation}", groupId = "${app.kafka.consumer-groups.enrichment}")
    public void consumeForEnrichment(TradeEvent event) {
        log.info("trade_event_received stage=enrichment eventId={} tradeId={}", event.getEventId(), event.getTradeId());
    }

    @KafkaListener(topics = "${app.kafka.topics.trade-enrichment}", groupId = "${app.kafka.consumer-groups.settlement}")
    public void consumeForSettlement(TradeEvent event) {
        log.info("trade_event_received stage=settlement eventId={} tradeId={}", event.getEventId(), event.getTradeId());
    }

    @KafkaListener(topics = {"${app.kafka.topics.trade-settlement}", "${app.kafka.topics.trade-status}"},
            groupId = "${app.kafka.consumer-groups.status}")
    public void consumeForStatus(TradeEvent event) {
        log.info("trade_event_received stage=status eventId={} tradeId={}", event.getEventId(), event.getTradeId());
    }

    @KafkaListener(topics = "${app.kafka.topics.trade-dlq}", groupId = "${app.kafka.consumer-groups.dlq}")
    public void monitorDeadLetter(ConsumerRecord<String, Object> record) {
        log.error("trade_event_dead_lettered key={} partition={} offset={} valueType={}",
                record.key(), record.partition(), record.offset(),
                record.value() == null ? "unavailable" : record.value().getClass().getName());
    }
}

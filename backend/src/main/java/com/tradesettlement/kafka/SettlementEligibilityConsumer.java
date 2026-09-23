package com.tradesettlement.kafka;

import com.tradesettlement.service.SettlementEligibilityService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SettlementEligibilityConsumer {
    private static final Logger log = LoggerFactory.getLogger(SettlementEligibilityConsumer.class);
    private final SettlementEligibilityService service;
    public SettlementEligibilityConsumer(SettlementEligibilityService service) { this.service = service; }

    @KafkaListener(topics = "${app.kafka.topics.trade-enrichment}", groupId = "${app.kafka.consumer-groups.settlement}")
    public void consume(ConsumerRecord<String, TradeEvent> record) {
        TradeEvent event = record.value();
        if (event == null) throw new IllegalArgumentException("Trade event payload cannot be null");
        if (event.getEventType() == TradeEventType.ENRICHMENT_FAILED) {
            log.info("settlement_eligibility_skipped reason=enrichment_failed tradeId={}", event.getTradeId());
            return;
        }
        service.evaluate(event);
    }
}

package com.tradesettlement.kafka;

import com.tradesettlement.service.SettlementService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SettlementConsumer {
    private static final Logger log = LoggerFactory.getLogger(SettlementConsumer.class);
    private final SettlementService service;
    public SettlementConsumer(SettlementService service) { this.service = service; }

    @KafkaListener(topics = "${app.kafka.topics.trade-settlement}", groupId = "${app.kafka.consumer-groups.settlement}-processor")
    public void consume(ConsumerRecord<String, TradeEvent> record) {
        TradeEvent event = record.value();
        if (event == null) throw new IllegalArgumentException("Trade event payload cannot be null");
        if (event.getEventType() == TradeEventType.ELIGIBILITY_REJECTED) {
            log.info("settlement_skipped reason=ineligible tradeId={}", event.getTradeId()); return;
        }
        service.process(event);
    }
}

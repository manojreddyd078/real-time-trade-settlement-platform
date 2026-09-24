package com.tradesettlement.kafka;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TradeKafkaPublisher {

    private final TradeEventProducer producer;

    public TradeKafkaPublisher(TradeEventProducer producer) {
        this.producer = producer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(TradeEvent event) {
        if (event.getEventType() == TradeEventType.TRADE_ACCEPTED) {
            producer.publishAccepted(event);
        } else if (event.getEventType() == TradeEventType.VALIDATION_COMPLETED
                || event.getEventType() == TradeEventType.VALIDATION_REJECTED) {
            producer.publishValidation(event);
        } else if (event.getEventType() == TradeEventType.ENRICHMENT_COMPLETED
                || event.getEventType() == TradeEventType.ENRICHMENT_FAILED) {
            producer.publishEnrichment(event);
        } else if (event.getEventType() == TradeEventType.ELIGIBILITY_CONFIRMED
                || event.getEventType() == TradeEventType.ELIGIBILITY_REJECTED) {
            producer.publishSettlement(event);
        } else if (event.getEventType() == TradeEventType.SETTLEMENT_COMPLETED
                || event.getEventType() == TradeEventType.SETTLEMENT_FAILED) {
            producer.publishStatus(event);
        }
    }
}

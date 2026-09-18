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
        } else if (event.getEventType() == TradeEventType.VALIDATION_COMPLETED) {
            producer.publishValidation(event);
        }
    }
}

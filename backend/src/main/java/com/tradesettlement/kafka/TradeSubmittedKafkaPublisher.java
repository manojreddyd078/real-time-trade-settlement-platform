package com.tradesettlement.kafka;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TradeSubmittedKafkaPublisher {

    private final TradeEventProducer producer;

    public TradeSubmittedKafkaPublisher(TradeEventProducer producer) {
        this.producer = producer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(TradeLifecycleEvent event) {
        if (event.getEventType() == TradeEventType.TRADE_SUBMITTED) {
            producer.publishSubmitted(event);
        }
    }
}

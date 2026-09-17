package com.tradesettlement.kafka;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TradeSubmittedKafkaPublisherTest {

    @Test
    void publishesAcceptedEvent() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeSubmittedKafkaPublisher publisher = new TradeSubmittedKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);

        publisher.publishAfterCommit(event);

        verify(producer).publishAccepted(event);
    }

    @Test
    void ignoresOtherLifecycleEvents() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeSubmittedKafkaPublisher publisher = new TradeSubmittedKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.VALIDATION_COMPLETED);

        publisher.publishAfterCommit(event);

        verify(producer, never()).publishAccepted(event);
    }
}

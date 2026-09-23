package com.tradesettlement.kafka;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TradeKafkaPublisherTest {

    @Test
    void publishesAcceptedEvent() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeKafkaPublisher publisher = new TradeKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);

        publisher.publishAfterCommit(event);

        verify(producer).publishAccepted(event);
    }

    @Test
    void publishesValidationCompletedEvent() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeKafkaPublisher publisher = new TradeKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.VALIDATION_COMPLETED);

        publisher.publishAfterCommit(event);

        verify(producer).publishValidation(event);
    }

    @Test
    void ignoresEventsOwnedByLaterProcessingStages() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeKafkaPublisher publisher = new TradeKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ENRICHMENT_COMPLETED);

        publisher.publishAfterCommit(event);

        verify(producer, never()).publishAccepted(event);
        verify(producer, never()).publishValidation(event);
    }

    @Test
    void publishesRejectedValidationResult() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeKafkaPublisher publisher = new TradeKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.VALIDATION_REJECTED);

        publisher.publishAfterCommit(event);

        verify(producer).publishValidation(event);
    }

    @Test
    void publishesEnrichmentResult() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeKafkaPublisher publisher = new TradeKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ENRICHMENT_COMPLETED);
        publisher.publishAfterCommit(event);
        verify(producer).publishEnrichment(event);
    }

    @Test
    void publishesEligibilityResult() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradeKafkaPublisher publisher = new TradeKafkaPublisher(producer);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ELIGIBILITY_CONFIRMED);
        publisher.publishAfterCommit(event);
        verify(producer).publishSettlement(event);
    }
}

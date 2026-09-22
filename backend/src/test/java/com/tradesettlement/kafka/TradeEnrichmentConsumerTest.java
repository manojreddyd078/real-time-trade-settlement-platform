package com.tradesettlement.kafka;

import com.tradesettlement.service.TradeEnrichmentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TradeEnrichmentConsumerTest {
    @Test void delegatesValidatedTrade() {
        TradeEnrichmentService service = mock(TradeEnrichmentService.class); TradeEnrichmentConsumer consumer = new TradeEnrichmentConsumer(service);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.VALIDATION_COMPLETED);
        consumer.consume(new ConsumerRecord<>("trade-validation", 0, 2L, "key", event)); verify(service).enrich(event);
    }
    @Test void skipsRejectedValidationResult() {
        TradeEnrichmentService service = mock(TradeEnrichmentService.class); TradeEnrichmentConsumer consumer = new TradeEnrichmentConsumer(service);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.VALIDATION_REJECTED);
        consumer.consume(new ConsumerRecord<>("trade-validation", 0, 3L, "key", event)); verify(service, never()).enrich(event);
    }
}

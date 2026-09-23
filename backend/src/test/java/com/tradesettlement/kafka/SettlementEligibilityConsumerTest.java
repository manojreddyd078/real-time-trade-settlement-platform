package com.tradesettlement.kafka;

import com.tradesettlement.service.SettlementEligibilityService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SettlementEligibilityConsumerTest {
    @Test void evaluatesEnrichedTrade() {
        SettlementEligibilityService service = mock(SettlementEligibilityService.class);
        SettlementEligibilityConsumer consumer = new SettlementEligibilityConsumer(service);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ENRICHMENT_COMPLETED);
        consumer.consume(new ConsumerRecord<>("trade-enrichment", 0, 1L, "key", event));
        verify(service).evaluate(event);
    }
    @Test void skipsFailedEnrichment() {
        SettlementEligibilityService service = mock(SettlementEligibilityService.class);
        SettlementEligibilityConsumer consumer = new SettlementEligibilityConsumer(service);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ENRICHMENT_FAILED);
        consumer.consume(new ConsumerRecord<>("trade-enrichment", 0, 1L, "key", event));
        verify(service, never()).evaluate(event);
    }
}

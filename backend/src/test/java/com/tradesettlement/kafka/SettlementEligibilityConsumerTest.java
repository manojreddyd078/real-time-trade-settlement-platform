package com.tradesettlement.kafka;

import com.tradesettlement.service.SettlementEligibilityService;
import com.tradesettlement.service.EventIdempotencyService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;

class SettlementEligibilityConsumerTest {
    @Test void evaluatesEnrichedTrade() {
        SettlementEligibilityService service = mock(SettlementEligibilityService.class);
        SettlementEligibilityConsumer consumer = new SettlementEligibilityConsumer(service, passThrough());
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ENRICHMENT_COMPLETED);
        consumer.consume(new ConsumerRecord<>("trade-enrichment", 0, 1L, "key", event));
        verify(service).evaluate(event);
    }
    @Test void skipsFailedEnrichment() {
        SettlementEligibilityService service = mock(SettlementEligibilityService.class);
        SettlementEligibilityConsumer consumer = new SettlementEligibilityConsumer(service, passThrough());
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ENRICHMENT_FAILED);
        consumer.consume(new ConsumerRecord<>("trade-enrichment", 0, 1L, "key", event));
        verify(service, never()).evaluate(event);
    }
    private EventIdempotencyService passThrough() { EventIdempotencyService value = mock(EventIdempotencyService.class); doAnswer(call -> { call.<Runnable>getArgument(2).run(); return true; }).when(value).processOnce(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()); return value; }
}

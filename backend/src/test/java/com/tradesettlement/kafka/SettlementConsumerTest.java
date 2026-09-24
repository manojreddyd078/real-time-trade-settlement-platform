package com.tradesettlement.kafka;

import com.tradesettlement.service.SettlementService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SettlementConsumerTest {
    @Test void processesEligibleTrade() {
        SettlementService service = mock(SettlementService.class); SettlementConsumer consumer = new SettlementConsumer(service);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ELIGIBILITY_CONFIRMED);
        consumer.consume(new ConsumerRecord<>("trade-settlement", 0, 1L, "key", event)); verify(service).process(event);
    }
    @Test void skipsIneligibleTrade() {
        SettlementService service = mock(SettlementService.class); SettlementConsumer consumer = new SettlementConsumer(service);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.ELIGIBILITY_REJECTED);
        consumer.consume(new ConsumerRecord<>("trade-settlement", 0, 1L, "key", event)); verify(service, never()).process(event);
    }
}

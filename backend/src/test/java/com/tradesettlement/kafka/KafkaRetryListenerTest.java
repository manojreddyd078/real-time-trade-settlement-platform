package com.tradesettlement.kafka;

import com.tradesettlement.service.EventRetryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaRetryListenerTest {
    @AfterEach void clearGroup() { KafkaUtils.clearConsumerGroupId(); }

    @Test
    void recordsRetryCountAndConsumerIdentity() {
        EventRetryService retries = mock(EventRetryService.class);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        ConsumerRecord<String, TradeEvent> record = new ConsumerRecord<>("trade-events", 1, 42L, "key", event);
        IllegalStateException failure = new IllegalStateException("database unavailable");
        KafkaUtils.setConsumerGroupId("validation-service");

        new KafkaRetryListener(retries, 3).failedDelivery(record, failure, 2);

        verify(retries).recordFailure(event.getEventId(), "validation-service:trade-events", event.getTradeId(),
                "trade-events", 2, 3, failure);
    }

    @Test
    void marksEventExhaustedAfterRetryLimit() {
        EventRetryService retries = mock(EventRetryService.class);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        ConsumerRecord<String, TradeEvent> record = new ConsumerRecord<>("trade-events", 0, 7L, "key", event);

        new KafkaRetryListener(retries, 3).recovered(record, new IllegalStateException("still unavailable"));

        verify(retries).markExhausted(event.getEventId());
    }
}

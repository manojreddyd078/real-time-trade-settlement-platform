package com.tradesettlement.kafka;

import com.tradesettlement.entity.DeadLetterEvent;
import com.tradesettlement.service.DeadLetterService;
import com.tradesettlement.service.EventIdempotencyService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeEventConsumersTest {
    @Test
    void persistsDeadLetterRecord() {
        DeadLetterService deadLetters = mock(DeadLetterService.class);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("trade-dlq", 1, 10L, "key",
                TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED));
        DeadLetterEvent stored = new DeadLetterEvent(); stored.setId(java.util.UUID.randomUUID());
        when(deadLetters.store(record)).thenReturn(stored);

        new TradeEventConsumers(mock(EventIdempotencyService.class), deadLetters).monitorDeadLetter(record);

        verify(deadLetters).store(record);
    }
}

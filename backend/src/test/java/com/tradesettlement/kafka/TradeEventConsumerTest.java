package com.tradesettlement.kafka;

import com.tradesettlement.service.TradeProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TradeEventConsumerTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void delegatesTradeEventToProcessingService() {
        TradeProcessingService processingService = mock(TradeProcessingService.class);
        TradeEventConsumer consumer = new TradeEventConsumer(processingService);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        ConsumerRecord<String, TradeEvent> record =
                new ConsumerRecord<>("trade-events", 1, 42L, event.getTradeId().toString(), event);

        consumer.consume(record);

        verify(processingService).processAcceptedTrade(event);
        assertEventContextCleared();
    }

    @Test
    void propagatesProcessingFailureSoKafkaCanRetryAndRouteToDlq() {
        TradeProcessingService processingService = mock(TradeProcessingService.class);
        TradeEventConsumer consumer = new TradeEventConsumer(processingService);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        ConsumerRecord<String, TradeEvent> record =
                new ConsumerRecord<>("trade-events", 0, 7L, event.getTradeId().toString(), event);
        IllegalStateException failure = new IllegalStateException("database unavailable");
        doThrow(failure).when(processingService).processAcceptedTrade(event);

        assertThrows(IllegalStateException.class, () -> consumer.consume(record));

        assertEventContextCleared();
    }

    @Test
    void rejectsNullPayload() {
        TradeProcessingService processingService = mock(TradeProcessingService.class);
        TradeEventConsumer consumer = new TradeEventConsumer(processingService);
        ConsumerRecord<String, TradeEvent> record = new ConsumerRecord<>("trade-events", 0, 1L, "key", null);

        assertThrows(IllegalArgumentException.class, () -> consumer.consume(record));
    }

    private void assertEventContextCleared() {
        assertNull(MDC.get("eventId"));
        assertNull(MDC.get("tradeId"));
        assertNull(MDC.get("correlationId"));
    }
}

package com.tradesettlement.kafka;

import com.tradesettlement.service.TradeProcessingService;
import com.tradesettlement.service.EventIdempotencyService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class TradeEventConsumerTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void delegatesTradeEventToProcessingService() {
        TradeProcessingService processingService = mock(TradeProcessingService.class);
        EventIdempotencyService idempotency = passThroughIdempotency();
        TradeEventConsumer consumer = new TradeEventConsumer(processingService, idempotency);
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
        TradeEventConsumer consumer = new TradeEventConsumer(processingService, passThroughIdempotency());
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
        TradeEventConsumer consumer = new TradeEventConsumer(processingService, passThroughIdempotency());
        ConsumerRecord<String, TradeEvent> record = new ConsumerRecord<>("trade-events", 0, 1L, "key", null);

        assertThrows(IllegalArgumentException.class, () -> consumer.consume(record));
    }

    @Test
    void doesNotProcessAnEventAlreadyClaimedByTheConsumerGroup() {
        TradeProcessingService processingService = mock(TradeProcessingService.class);
        EventIdempotencyService idempotency = mock(EventIdempotencyService.class);
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        when(idempotency.processOnce(org.mockito.ArgumentMatchers.eq(event),
                org.mockito.ArgumentMatchers.eq("trade-validation"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(false);

        new TradeEventConsumer(processingService, idempotency).consume(
                new ConsumerRecord<>("trade-events", 0, 8L, event.getTradeId().toString(), event));

        verify(processingService, never()).processAcceptedTrade(event);
        assertEventContextCleared();
    }

    private void assertEventContextCleared() {
        assertNull(MDC.get("eventId"));
        assertNull(MDC.get("tradeId"));
        assertNull(MDC.get("correlationId"));
    }

    private EventIdempotencyService passThroughIdempotency() {
        EventIdempotencyService service = mock(EventIdempotencyService.class);
        doAnswer(invocation -> { invocation.<Runnable>getArgument(2).run(); return true; })
                .when(service).processOnce(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
        return service;
    }
}

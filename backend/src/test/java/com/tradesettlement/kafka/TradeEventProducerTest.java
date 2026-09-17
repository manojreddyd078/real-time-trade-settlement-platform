package com.tradesettlement.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeEventProducerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private TradeEventProducer producer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        producer = new TradeEventProducer(kafkaTemplate, "trade-events", "trade-validation",
                "trade-enrichment", "trade-settlement", "trade-status");
    }

    @Test
    void publishesAcceptedTradeUsingTradeIdAsKey() {
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        SettableListenableFuture<SendResult<String, Object>> future = new SettableListenableFuture<>();
        when(kafkaTemplate.send(eq("trade-events"), eq(event.getTradeId().toString()), eq(event))).thenReturn(future);

        producer.publishAccepted(event);

        verify(kafkaTemplate).send("trade-events", event.getTradeId().toString(), event);
    }

    @Test
    void containsSynchronousProducerFailure() {
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        when(kafkaTemplate.send(any(), any(), any())).thenThrow(new IllegalStateException("broker unavailable"));

        assertDoesNotThrow(() -> producer.publishAccepted(event));
    }

    @Test
    void handlesAsynchronousProducerFailure() {
        TradeEvent event = TradeEventSerializationTest.event(TradeEventType.TRADE_ACCEPTED);
        SettableListenableFuture<SendResult<String, Object>> future = new SettableListenableFuture<>();
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        producer.publishAccepted(event);

        assertDoesNotThrow(() -> future.setException(new IllegalStateException("send failed")));
    }
}

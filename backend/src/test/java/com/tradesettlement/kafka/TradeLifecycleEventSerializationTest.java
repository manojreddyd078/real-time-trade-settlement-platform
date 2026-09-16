package com.tradesettlement.kafka;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeLifecycleEventSerializationTest {

    @Test
    void roundTripsTypedJsonEvent() {
        TradeLifecycleEvent source = new TradeLifecycleEvent(
                UUID.randomUUID(), UUID.randomUUID(), "TRD-2026-12001", TradeType.BUY,
                TradeStatus.RECEIVED, TradeEventType.TRADE_SUBMITTED,
                OffsetDateTime.parse("2026-09-16T14:00:00Z"), "request-123");
        RecordHeaders headers = new RecordHeaders();
        JsonSerializer<TradeLifecycleEvent> serializer = new JsonSerializer<>();
        JsonDeserializer<TradeLifecycleEvent> deserializer = new JsonDeserializer<>(TradeLifecycleEvent.class);
        deserializer.addTrustedPackages("com.tradesettlement.kafka");

        byte[] payload = serializer.serialize("trade-events", headers, source);
        TradeLifecycleEvent result = deserializer.deserialize("trade-events", headers, payload);

        assertEquals(source.getEventId(), result.getEventId());
        assertEquals(source.getTradeId(), result.getTradeId());
        assertEquals(source.getTradeReference(), result.getTradeReference());
        assertEquals(TradeEventType.TRADE_SUBMITTED, result.getEventType());
        assertEquals(TradeStatus.RECEIVED, result.getStatus());
        assertEquals("request-123", result.getCorrelationId());
    }
}

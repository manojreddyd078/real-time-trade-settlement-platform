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

class TradeEventSerializationTest {

    @Test
    void roundTripsTypedJsonEvent() {
        TradeEvent source = event(TradeEventType.TRADE_ACCEPTED);
        RecordHeaders headers = new RecordHeaders();
        JsonSerializer<TradeEvent> serializer = new JsonSerializer<>();
        JsonDeserializer<TradeEvent> deserializer = new JsonDeserializer<>(TradeEvent.class);
        deserializer.addTrustedPackages("com.tradesettlement.kafka");

        byte[] payload = serializer.serialize("trade-events", headers, source);
        TradeEvent result = deserializer.deserialize("trade-events", headers, payload);

        assertEquals(source.getEventId(), result.getEventId());
        assertEquals(source.getTradeId(), result.getTradeId());
        assertEquals(TradeEventType.TRADE_ACCEPTED, result.getEventType());
        assertEquals(TradeStatus.RECEIVED, result.getStatus());
        assertEquals("request-123", result.getCorrelationId());
        assertEquals(OffsetDateTime.parse("2026-09-17T14:00:00Z"), result.getOccurredAt());
    }

    static TradeEvent event(TradeEventType type) {
        return new TradeEvent(
                UUID.randomUUID(), UUID.randomUUID(), "TRD-2026-12001", TradeType.BUY,
                TradeStatus.RECEIVED, type, OffsetDateTime.parse("2026-09-17T14:00:00Z"), "request-123");
    }
}

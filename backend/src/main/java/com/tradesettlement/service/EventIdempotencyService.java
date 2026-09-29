package com.tradesettlement.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import com.tradesettlement.kafka.TradeEvent;
import com.tradesettlement.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventIdempotencyService {
    private static final Logger log = LoggerFactory.getLogger(EventIdempotencyService.class);
    private final ProcessedEventRepository processedEvents; private final Clock clock;
    public EventIdempotencyService(ProcessedEventRepository processedEvents, Clock clock) {
        this.processedEvents = processedEvents; this.clock = clock;
    }

    @Transactional
    public boolean processOnce(TradeEvent event, String consumerName, Runnable handler) {
        if (event.getEventId() == null) throw new IllegalArgumentException("Event ID is required for idempotent processing");
        if (event.getEventType() == null) throw new IllegalArgumentException("Event type is required for idempotent processing");
        int claimed = processedEvents.claim(event.getEventId(), consumerName, event.getTradeId(),
                event.getEventType().name(), OffsetDateTime.now(clock));
        if (claimed == 0) {
            log.info("duplicate_event_skipped consumer={} eventId={} tradeId={}", consumerName, event.getEventId(), event.getTradeId());
            return false;
        }
        handler.run();
        processedEvents.complete(event.getEventId(), consumerName, OffsetDateTime.now(clock));
        return true;
    }
}

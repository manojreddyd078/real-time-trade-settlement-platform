package com.tradesettlement.kafka;

import com.tradesettlement.service.EventRetryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.support.KafkaUtils;

public class KafkaRetryListener implements RetryListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaRetryListener.class);
    private final EventRetryService retries; private final int maxRetries;
    public KafkaRetryListener(EventRetryService retries, int maxRetries) { this.retries = retries; this.maxRetries = maxRetries; }

    @Override
    public void failedDelivery(ConsumerRecord<?, ?> record, Exception exception, int deliveryAttempt) {
        TradeEvent event = record.value() instanceof TradeEvent ? (TradeEvent) record.value() : null;
        int retryCount = Math.min(deliveryAttempt, maxRetries);
        String consumerGroup = KafkaUtils.getConsumerGroupId();
        String retryKey = (consumerGroup == null ? "unknown-group" : consumerGroup) + ":" + record.topic();
        retries.recordFailure(event == null ? null : event.getEventId(), retryKey,
                event == null ? null : event.getTradeId(), record.topic(), retryCount, maxRetries, exception);
        log.warn("kafka_event_retry topic={} partition={} offset={} eventId={} retryCount={} maxRetries={} reason={}",
                record.topic(), record.partition(), record.offset(), event == null ? null : event.getEventId(),
                retryCount, maxRetries, exception.getMessage());
    }

    @Override
    public void recovered(ConsumerRecord<?, ?> record, Exception exception) {
        TradeEvent event = record.value() instanceof TradeEvent ? (TradeEvent) record.value() : null;
        retries.markExhausted(event == null ? null : event.getEventId());
        log.error("kafka_event_retries_exhausted topic={} partition={} offset={} eventId={} maxRetries={}",
                record.topic(), record.partition(), record.offset(), event == null ? null : event.getEventId(), maxRetries, exception);
    }
}

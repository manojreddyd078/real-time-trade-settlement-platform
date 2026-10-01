package com.tradesettlement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.KafkaException;
import com.tradesettlement.kafka.KafkaRetryListener;
import com.tradesettlement.service.EventRetryService;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic tradeEventsTopic(@Value("${app.kafka.topics.trade-events}") String name) {
        return topic(name);
    }

    @Bean
    NewTopic tradeValidationTopic(@Value("${app.kafka.topics.trade-validation}") String name) {
        return topic(name);
    }

    @Bean
    NewTopic tradeEnrichmentTopic(@Value("${app.kafka.topics.trade-enrichment}") String name) {
        return topic(name);
    }

    @Bean
    NewTopic tradeSettlementTopic(@Value("${app.kafka.topics.trade-settlement}") String name) {
        return topic(name);
    }

    @Bean
    NewTopic tradeStatusTopic(@Value("${app.kafka.topics.trade-status}") String name) {
        return topic(name);
    }

    @Bean
    NewTopic tradeDlqTopic(@Value("${app.kafka.topics.trade-dlq}") String name) {
        return topic(name);
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                          EventRetryService retryService,
                                          @Value("${app.kafka.topics.trade-dlq}") String dlqTopic,
                                          @Value("${app.kafka.retry.delay-ms:1000}") long retryDelayMs,
                                          @Value("${app.kafka.retry.max-retries:3}") int maxRetries) {
        if (retryDelayMs < 0) throw new IllegalArgumentException("Kafka retry delay must not be negative");
        if (maxRetries < 0) throw new IllegalArgumentException("Kafka retry limit must not be negative");
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (record, exception) -> new TopicPartition(dlqTopic, record.partition()));
        recoverer.setAppendOriginalHeaders(true);
        recoverer.setStripPreviousExceptionHeaders(false);
        recoverer.setFailIfSendResultIsError(true);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(retryDelayMs, maxRetries));
        errorHandler.defaultFalse();
        errorHandler.addRetryableExceptions(TransientDataAccessException.class, KafkaException.class);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, DeserializationException.class);
        errorHandler.setRetryListeners(new KafkaRetryListener(retryService, maxRetries));
        return errorHandler;
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}

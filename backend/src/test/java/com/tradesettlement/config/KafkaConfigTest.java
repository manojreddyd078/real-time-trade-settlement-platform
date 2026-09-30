package com.tradesettlement.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import org.springframework.kafka.core.KafkaTemplate;
import com.tradesettlement.service.EventRetryService;

class KafkaConfigTest {

    @Test
    void definesAllPipelineTopicsWithThreePartitions() {
        KafkaConfig config = new KafkaConfig();
        List<NewTopic> topics = Stream.of(
                config.tradeEventsTopic("trade-events"),
                config.tradeValidationTopic("trade-validation"),
                config.tradeEnrichmentTopic("trade-enrichment"),
                config.tradeSettlementTopic("trade-settlement"),
                config.tradeStatusTopic("trade-status"),
                config.tradeDlqTopic("trade-dlq"))
                .collect(Collectors.toList());

        assertEquals(6, topics.size());
        assertEquals(6, topics.stream().map(NewTopic::name).distinct().count());
        topics.forEach(topic -> assertEquals(3, topic.numPartitions()));
    }

    @Test
    void rejectsInvalidRetryConfiguration() {
        KafkaConfig config = new KafkaConfig();
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        EventRetryService retries = mock(EventRetryService.class);
        assertThrows(IllegalArgumentException.class,
                () -> config.kafkaErrorHandler(template, retries, "trade-dlq", -1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> config.kafkaErrorHandler(template, retries, "trade-dlq", 1000, -1));
    }
}

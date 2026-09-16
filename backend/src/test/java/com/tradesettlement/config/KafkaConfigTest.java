package com.tradesettlement.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}

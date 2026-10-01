package com.tradesettlement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.tradesettlement.entity.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, UUID> {
    List<DeadLetterEvent> findAllByOrderByReceivedAtDesc();
    Optional<DeadLetterEvent> findFirstByTradeIdOrderByReceivedAtDesc(UUID tradeId);
    boolean existsByOriginalTopicAndOriginalPartitionAndOriginalOffsetAndOriginalConsumerGroup(
            String topic, int partition, long offset, String consumerGroup);
    Optional<DeadLetterEvent> findByOriginalTopicAndOriginalPartitionAndOriginalOffsetAndOriginalConsumerGroup(
            String topic, int partition, long offset, String consumerGroup);
}

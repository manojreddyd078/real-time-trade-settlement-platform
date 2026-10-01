package com.tradesettlement.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.tradesettlement.entity.EventRetryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRetryAttemptRepository extends JpaRepository<EventRetryAttempt, EventRetryAttempt.Key> {
    @Modifying
    @Query(value = "INSERT INTO settlement.event_retry_attempts (event_id, retry_key, trade_id, topic, retry_count, max_retries, status, last_failure, first_attempt_at, last_attempt_at) " +
            "VALUES (:eventId, :retryKey, :tradeId, :topic, :retryCount, :maxRetries, 'RETRYING', :failure, :attemptedAt, :attemptedAt) " +
            "ON CONFLICT (event_id, retry_key) DO UPDATE SET retry_count = EXCLUDED.retry_count, max_retries = EXCLUDED.max_retries, " +
            "status = 'RETRYING', last_failure = EXCLUDED.last_failure, last_attempt_at = EXCLUDED.last_attempt_at", nativeQuery = true)
    int recordFailure(@Param("eventId") UUID eventId, @Param("retryKey") String retryKey,
                      @Param("tradeId") UUID tradeId, @Param("topic") String topic,
                      @Param("retryCount") int retryCount, @Param("maxRetries") int maxRetries,
                      @Param("failure") String failure, @Param("attemptedAt") OffsetDateTime attemptedAt);

    @Modifying
    @Query("UPDATE EventRetryAttempt attempt SET attempt.status = :status WHERE attempt.eventId = :eventId")
    int updateStatus(@Param("eventId") UUID eventId, @Param("status") String status);

    List<EventRetryAttempt> findByTradeIdOrderByLastAttemptAtDesc(UUID tradeId);
    List<EventRetryAttempt> findByEventIdOrderByLastAttemptAtDesc(UUID eventId);
}

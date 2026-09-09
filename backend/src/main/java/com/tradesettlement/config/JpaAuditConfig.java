package com.tradesettlement.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "utcDateTimeProvider")
public class JpaAuditConfig {

    @Bean
    AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }

    @Bean
    DateTimeProvider utcDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}

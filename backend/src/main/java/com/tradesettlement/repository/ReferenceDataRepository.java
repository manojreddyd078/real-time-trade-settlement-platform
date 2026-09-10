package com.tradesettlement.repository;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReferenceDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReferenceDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean activeInstrumentExists(UUID id) {
        return exists("SELECT EXISTS (SELECT 1 FROM settlement.instruments WHERE id = ? AND active)", id);
    }

    public boolean activeCounterpartyExists(UUID id) {
        return exists("SELECT EXISTS (SELECT 1 FROM settlement.counterparties WHERE id = ? AND active)", id);
    }

    public boolean activeCurrencyExists(String code) {
        return exists("SELECT EXISTS (SELECT 1 FROM settlement.currencies WHERE code = ? AND active)", code);
    }

    private boolean exists(String sql, Object value) {
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, value);
        return Boolean.TRUE.equals(result);
    }
}

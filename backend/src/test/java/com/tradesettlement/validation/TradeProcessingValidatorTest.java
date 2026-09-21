package com.tradesettlement.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeType;
import com.tradesettlement.repository.ReferenceDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeProcessingValidatorTest {

    private ReferenceDataRepository referenceDataRepository;
    private TradeProcessingValidator validator;

    @BeforeEach
    void setUp() {
        referenceDataRepository = mock(ReferenceDataRepository.class);
        validator = new TradeProcessingValidator(referenceDataRepository);
    }

    @Test
    void acceptsValidTradeAndActiveReferenceData() {
        enableAllReferenceData();

        assertTrue(validator.validate(validTrade()).isEmpty());
    }

    @Test
    void reportsRequiredNumericCurrencyAndDateFailuresTogether() {
        enableAllReferenceData();
        Trade trade = validTrade();
        trade.setTradeReference(" ");
        trade.setQuantity(BigDecimal.ZERO);
        trade.setPrice(new BigDecimal("-0.01"));
        trade.setCurrencyCode("usd");
        trade.setSettlementDate(trade.getTradeDate().minusDays(1));

        List<String> errors = validator.validate(trade);

        assertEquals(5, errors.size());
        assertTrue(errors.contains("tradeReference is required"));
        assertTrue(errors.contains("quantity must be greater than zero"));
        assertTrue(errors.contains("price cannot be negative"));
        assertTrue(errors.contains("currencyCode must be a three-letter uppercase code"));
        assertTrue(errors.contains("settlementDate cannot be before tradeDate"));
    }

    @Test
    void rejectsInactiveCurrencyAndReferenceData() {
        Trade trade = validTrade();

        List<String> errors = validator.validate(trade);

        assertTrue(errors.contains("instrument is missing or inactive"));
        assertTrue(errors.contains("buyer counterparty is missing or inactive"));
        assertTrue(errors.contains("seller counterparty is missing or inactive"));
        assertTrue(errors.contains("currency is missing or inactive"));
    }

    private void enableAllReferenceData() {
        when(referenceDataRepository.activeInstrumentExists(any())).thenReturn(true);
        when(referenceDataRepository.activeCounterpartyExists(any())).thenReturn(true);
        when(referenceDataRepository.activeCurrencyExists(any())).thenReturn(true);
    }

    private Trade validTrade() {
        Trade trade = new Trade();
        trade.setTradeReference("TRD-2026-12001");
        trade.setTradeType(TradeType.BUY);
        trade.setInstrumentId(UUID.randomUUID());
        trade.setBuyerCounterpartyId(UUID.randomUUID());
        trade.setSellerCounterpartyId(UUID.randomUUID());
        trade.setQuantity(new BigDecimal("100"));
        trade.setPrice(new BigDecimal("25.50"));
        trade.setCurrencyCode("USD");
        trade.setTradeDate(LocalDate.of(2026, 9, 21));
        trade.setSettlementDate(LocalDate.of(2026, 9, 23));
        return trade;
    }
}

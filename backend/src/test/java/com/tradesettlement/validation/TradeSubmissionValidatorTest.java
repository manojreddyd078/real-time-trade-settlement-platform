package com.tradesettlement.validation;

import java.time.LocalDate;
import java.util.UUID;

import com.tradesettlement.dto.TradeSubmissionRequest;
import com.tradesettlement.exception.TradeValidationException;
import com.tradesettlement.repository.ReferenceDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class TradeSubmissionValidatorTest {

    private TradeSubmissionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TradeSubmissionValidator(mock(ReferenceDataRepository.class));
    }

    @Test
    void rejectsSameBuyerAndSeller() {
        UUID counterpartyId = UUID.randomUUID();
        TradeSubmissionRequest request = request(counterpartyId, counterpartyId,
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12));

        assertThrows(TradeValidationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsSettlementBeforeTradeDate() {
        TradeSubmissionRequest request = request(UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 9));

        assertThrows(TradeValidationException.class, () -> validator.validate(request));
    }

    private TradeSubmissionRequest request(UUID buyerId, UUID sellerId, LocalDate tradeDate, LocalDate settlementDate) {
        TradeSubmissionRequest request = new TradeSubmissionRequest();
        request.setBuyerCounterpartyId(buyerId);
        request.setSellerCounterpartyId(sellerId);
        request.setTradeDate(tradeDate);
        request.setSettlementDate(settlementDate);
        return request;
    }
}

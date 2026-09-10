package com.tradesettlement.validation;

import com.tradesettlement.dto.TradeSubmissionRequest;
import com.tradesettlement.exception.ReferenceDataNotFoundException;
import com.tradesettlement.exception.TradeValidationException;
import com.tradesettlement.repository.ReferenceDataRepository;
import org.springframework.stereotype.Component;

@Component
public class TradeSubmissionValidator {

    private final ReferenceDataRepository referenceDataRepository;

    public TradeSubmissionValidator(ReferenceDataRepository referenceDataRepository) {
        this.referenceDataRepository = referenceDataRepository;
    }

    public void validate(TradeSubmissionRequest request) {
        if (request.getBuyerCounterpartyId().equals(request.getSellerCounterpartyId())) {
            throw new TradeValidationException("Buyer and seller counterparties must be different");
        }
        if (request.getSettlementDate().isBefore(request.getTradeDate())) {
            throw new TradeValidationException("Settlement date cannot be before trade date");
        }
        if (!referenceDataRepository.activeInstrumentExists(request.getInstrumentId())) {
            throw new ReferenceDataNotFoundException("Active instrument was not found: " + request.getInstrumentId());
        }
        if (!referenceDataRepository.activeCounterpartyExists(request.getBuyerCounterpartyId())) {
            throw new ReferenceDataNotFoundException("Active buyer counterparty was not found: " + request.getBuyerCounterpartyId());
        }
        if (!referenceDataRepository.activeCounterpartyExists(request.getSellerCounterpartyId())) {
            throw new ReferenceDataNotFoundException("Active seller counterparty was not found: " + request.getSellerCounterpartyId());
        }
        if (!referenceDataRepository.activeCurrencyExists(request.getCurrencyCode())) {
            throw new ReferenceDataNotFoundException("Active currency was not found: " + request.getCurrencyCode());
        }
    }
}

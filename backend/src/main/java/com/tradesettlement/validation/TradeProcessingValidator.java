package com.tradesettlement.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.tradesettlement.entity.Trade;
import com.tradesettlement.repository.ReferenceDataRepository;
import org.springframework.stereotype.Component;

@Component
public class TradeProcessingValidator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ReferenceDataRepository referenceDataRepository;

    public TradeProcessingValidator(ReferenceDataRepository referenceDataRepository) {
        this.referenceDataRepository = referenceDataRepository;
    }

    public List<String> validate(Trade trade) {
        List<String> errors = new ArrayList<>();

        required(trade.getTradeReference(), "tradeReference is required", errors);
        if (trade.getTradeType() == null) errors.add("tradeType is required");
        if (trade.getInstrumentId() == null) errors.add("instrumentId is required");
        if (trade.getBuyerCounterpartyId() == null) errors.add("buyerCounterpartyId is required");
        if (trade.getSellerCounterpartyId() == null) errors.add("sellerCounterpartyId is required");
        if (trade.getQuantity() == null) errors.add("quantity is required");
        else if (trade.getQuantity().compareTo(ZERO) <= 0) errors.add("quantity must be greater than zero");
        if (trade.getPrice() == null) errors.add("price is required");
        else if (trade.getPrice().compareTo(ZERO) < 0) errors.add("price cannot be negative");
        required(trade.getCurrencyCode(), "currencyCode is required", errors);
        if (trade.getCurrencyCode() != null && !trade.getCurrencyCode().matches("^[A-Z]{3}$")) {
            errors.add("currencyCode must be a three-letter uppercase code");
        }
        if (trade.getTradeDate() == null) errors.add("tradeDate is required");
        if (trade.getSettlementDate() == null) errors.add("settlementDate is required");
        if (trade.getTradeDate() != null && trade.getSettlementDate() != null
                && trade.getSettlementDate().isBefore(trade.getTradeDate())) {
            errors.add("settlementDate cannot be before tradeDate");
        }
        if (trade.getBuyerCounterpartyId() != null
                && trade.getBuyerCounterpartyId().equals(trade.getSellerCounterpartyId())) {
            errors.add("buyer and seller counterparties must be different");
        }

        validateReferenceData(trade, errors);
        return errors;
    }

    private void validateReferenceData(Trade trade, List<String> errors) {
        if (trade.getInstrumentId() != null && !referenceDataRepository.activeInstrumentExists(trade.getInstrumentId())) {
            errors.add("instrument is missing or inactive");
        }
        if (trade.getBuyerCounterpartyId() != null
                && !referenceDataRepository.activeCounterpartyExists(trade.getBuyerCounterpartyId())) {
            errors.add("buyer counterparty is missing or inactive");
        }
        if (trade.getSellerCounterpartyId() != null
                && !referenceDataRepository.activeCounterpartyExists(trade.getSellerCounterpartyId())) {
            errors.add("seller counterparty is missing or inactive");
        }
        if (trade.getCurrencyCode() != null && trade.getCurrencyCode().matches("^[A-Z]{3}$")
                && !referenceDataRepository.activeCurrencyExists(trade.getCurrencyCode())) {
            errors.add("currency is missing or inactive");
        }
    }

    private void required(String value, String message, List<String> errors) {
        if (value == null || value.isBlank()) errors.add(message);
    }
}

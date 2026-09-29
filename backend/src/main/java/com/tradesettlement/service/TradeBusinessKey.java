package com.tradesettlement.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import com.tradesettlement.dto.TradeSubmissionRequest;

final class TradeBusinessKey {
    private TradeBusinessKey() { }

    static String from(TradeSubmissionRequest request) {
        String externalReference = request.getExternalReference();
        String canonical = externalReference != null && !externalReference.trim().isEmpty()
                ? "EXTERNAL|" + externalReference.trim().toUpperCase(Locale.ROOT)
                : String.join("|", "ECONOMIC", request.getTradeType().name(), request.getInstrumentId().toString(),
                request.getBuyerCounterpartyId().toString(), request.getSellerCounterpartyId().toString(),
                request.getQuantity().stripTrailingZeros().toPlainString(), request.getPrice().stripTrailingZeros().toPlainString(),
                request.getCurrencyCode(), request.getTradeDate().toString(), request.getSettlementDate().toString());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}

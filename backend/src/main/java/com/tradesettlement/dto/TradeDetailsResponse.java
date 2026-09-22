package com.tradesettlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.entity.TradeType;

public class TradeDetailsResponse {
    private final UUID id; private final String tradeReference; private final String externalReference;
    private final TradeType tradeType; private final TradeStatus status; private final BigDecimal quantity;
    private final BigDecimal price; private final String currencyCode; private final LocalDate tradeDate;
    private final LocalDate settlementDate; private final String instrumentCode; private final String instrumentName;
    private final String buyerCounterpartyCode; private final String buyerCounterpartyName;
    private final String sellerCounterpartyCode; private final String sellerCounterpartyName;
    private final OffsetDateTime enrichedAt;

    private TradeDetailsResponse(Trade trade) {
        id = trade.getId(); tradeReference = trade.getTradeReference(); externalReference = trade.getExternalReference();
        tradeType = trade.getTradeType(); status = trade.getStatus(); quantity = trade.getQuantity(); price = trade.getPrice();
        currencyCode = trade.getCurrencyCode(); tradeDate = trade.getTradeDate(); settlementDate = trade.getSettlementDate();
        instrumentCode = trade.getInstrumentCode(); instrumentName = trade.getInstrumentName();
        buyerCounterpartyCode = trade.getBuyerCounterpartyCode(); buyerCounterpartyName = trade.getBuyerCounterpartyName();
        sellerCounterpartyCode = trade.getSellerCounterpartyCode(); sellerCounterpartyName = trade.getSellerCounterpartyName();
        enrichedAt = trade.getEnrichedAt();
    }
    public static TradeDetailsResponse from(Trade trade) { return new TradeDetailsResponse(trade); }
    public UUID getId() { return id; } public String getTradeReference() { return tradeReference; }
    public String getExternalReference() { return externalReference; } public TradeType getTradeType() { return tradeType; }
    public TradeStatus getStatus() { return status; } public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; } public String getCurrencyCode() { return currencyCode; }
    public LocalDate getTradeDate() { return tradeDate; } public LocalDate getSettlementDate() { return settlementDate; }
    public String getInstrumentCode() { return instrumentCode; } public String getInstrumentName() { return instrumentName; }
    public String getBuyerCounterpartyCode() { return buyerCounterpartyCode; }
    public String getBuyerCounterpartyName() { return buyerCounterpartyName; }
    public String getSellerCounterpartyCode() { return sellerCounterpartyCode; }
    public String getSellerCounterpartyName() { return sellerCounterpartyName; }
    public OffsetDateTime getEnrichedAt() { return enrichedAt; }
}

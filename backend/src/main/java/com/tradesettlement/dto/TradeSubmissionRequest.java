package com.tradesettlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.tradesettlement.entity.TradeType;

public class TradeSubmissionRequest {

    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$", message = "must contain only letters, numbers, dots, underscores, or hyphens")
    private String tradeReference;

    @Size(max = 100)
    private String externalReference;

    @NotNull
    private TradeType tradeType;

    @NotNull
    private UUID instrumentId;

    @NotNull
    private UUID buyerCounterpartyId;

    @NotNull
    private UUID sellerCounterpartyId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 15, fraction = 4)
    private BigDecimal quantity;

    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = 11, fraction = 8)
    private BigDecimal price;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "must be a three-letter uppercase currency code")
    private String currencyCode;

    @NotNull
    private LocalDate tradeDate;

    @NotNull
    private LocalDate settlementDate;

    public String getTradeReference() { return tradeReference; }
    public void setTradeReference(String tradeReference) { this.tradeReference = tradeReference; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public TradeType getTradeType() { return tradeType; }
    public void setTradeType(TradeType tradeType) { this.tradeType = tradeType; }
    public UUID getInstrumentId() { return instrumentId; }
    public void setInstrumentId(UUID instrumentId) { this.instrumentId = instrumentId; }
    public UUID getBuyerCounterpartyId() { return buyerCounterpartyId; }
    public void setBuyerCounterpartyId(UUID buyerCounterpartyId) { this.buyerCounterpartyId = buyerCounterpartyId; }
    public UUID getSellerCounterpartyId() { return sellerCounterpartyId; }
    public void setSellerCounterpartyId(UUID sellerCounterpartyId) { this.sellerCounterpartyId = sellerCounterpartyId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
}

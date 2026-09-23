package com.tradesettlement.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class InstrumentRequest {
    @NotBlank @Size(max = 50) private String instrumentCode;
    @Size(min = 12, max = 12) private String isin;
    @NotBlank @Size(max = 200) private String name;
    @NotBlank @Size(max = 30) private String instrumentClass;
    @NotBlank @Pattern(regexp = "^[A-Z]{3}$") private String currencyCode;
    private boolean active = true;
    private boolean settlementSupported = true;
    public String getInstrumentCode() { return instrumentCode; }
    public void setInstrumentCode(String instrumentCode) { this.instrumentCode = instrumentCode; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInstrumentClass() { return instrumentClass; }
    public void setInstrumentClass(String instrumentClass) { this.instrumentClass = instrumentClass; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isSettlementSupported() { return settlementSupported; }
    public void setSettlementSupported(boolean settlementSupported) { this.settlementSupported = settlementSupported; }
}

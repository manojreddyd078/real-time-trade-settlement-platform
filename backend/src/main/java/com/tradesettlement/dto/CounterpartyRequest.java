package com.tradesettlement.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CounterpartyRequest {
    @NotBlank @Size(max = 50) private String counterpartyCode;
    @NotBlank @Size(max = 200) private String legalName;
    @Size(min = 20, max = 20) private String lei;
    @NotBlank @Pattern(regexp = "^[A-Z]{2}$") private String countryCode;
    private boolean active = true;
    public String getCounterpartyCode() { return counterpartyCode; }
    public void setCounterpartyCode(String counterpartyCode) { this.counterpartyCode = counterpartyCode; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getLei() { return lei; }
    public void setLei(String lei) { this.lei = lei; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

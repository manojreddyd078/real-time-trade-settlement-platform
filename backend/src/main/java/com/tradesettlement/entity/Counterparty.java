package com.tradesettlement.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "counterparties", schema = "settlement")
public class Counterparty {
    @Id private UUID id;
    @Column(name = "counterparty_code", nullable = false, unique = true, length = 50) private String counterpartyCode;
    @Column(name = "legal_name", nullable = false, length = 200) private String legalName;
    @Column(length = 20, unique = true) private String lei;
    @Column(name = "country_code", nullable = false, length = 2) private String countryCode;
    @Column(nullable = false) private boolean active = true;

    public Counterparty() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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

package com.tradesettlement.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "instruments", schema = "settlement")
public class Instrument {
    @Id private UUID id;
    @Column(name = "instrument_code", nullable = false, unique = true, length = 50) private String instrumentCode;
    @Column(length = 12, unique = true) private String isin;
    @Column(nullable = false, length = 200) private String name;
    @Column(name = "instrument_class", nullable = false, length = 30) private String instrumentClass;
    @Column(name = "currency_code", nullable = false, length = 3) private String currencyCode;
    @Column(nullable = false) private boolean active = true;

    public Instrument() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
}

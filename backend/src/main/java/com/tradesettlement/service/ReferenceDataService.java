package com.tradesettlement.service;

import java.util.List;
import java.util.UUID;

import com.tradesettlement.dto.CounterpartyRequest;
import com.tradesettlement.dto.InstrumentRequest;
import com.tradesettlement.entity.Counterparty;
import com.tradesettlement.entity.Instrument;
import com.tradesettlement.exception.ReferenceDataNotFoundException;
import com.tradesettlement.repository.CounterpartyRepository;
import com.tradesettlement.repository.InstrumentRepository;
import com.tradesettlement.repository.ReferenceDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferenceDataService {
    private final InstrumentRepository instruments;
    private final CounterpartyRepository counterparties;
    private final ReferenceDataRepository referenceData;

    public ReferenceDataService(InstrumentRepository instruments, CounterpartyRepository counterparties,
                                ReferenceDataRepository referenceData) {
        this.instruments = instruments;
        this.counterparties = counterparties;
        this.referenceData = referenceData;
    }

    @Transactional(readOnly = true)
    public List<Instrument> instruments() { return instruments.findAllByOrderByInstrumentCodeAsc(); }
    @Transactional(readOnly = true)
    public List<Counterparty> counterparties() { return counterparties.findAllByOrderByCounterpartyCodeAsc(); }

    @Transactional
    public Instrument saveInstrument(UUID id, InstrumentRequest request) {
        if (!referenceData.activeCurrencyExists(request.getCurrencyCode())) {
            throw new ReferenceDataNotFoundException("Active currency was not found: " + request.getCurrencyCode());
        }
        Instrument value = id == null ? new Instrument() : instruments.findById(id)
                .orElseThrow(() -> new ReferenceDataNotFoundException("Instrument was not found: " + id));
        if (id == null) value.setId(UUID.randomUUID());
        value.setInstrumentCode(request.getInstrumentCode());
        value.setIsin(request.getIsin());
        value.setName(request.getName());
        value.setInstrumentClass(request.getInstrumentClass());
        value.setCurrencyCode(request.getCurrencyCode());
        value.setActive(request.isActive());
        value.setSettlementSupported(request.isSettlementSupported());
        return instruments.save(value);
    }

    @Transactional
    public Counterparty saveCounterparty(UUID id, CounterpartyRequest request) {
        Counterparty value = id == null ? new Counterparty() : counterparties.findById(id)
                .orElseThrow(() -> new ReferenceDataNotFoundException("Counterparty was not found: " + id));
        if (id == null) value.setId(UUID.randomUUID());
        value.setCounterpartyCode(request.getCounterpartyCode());
        value.setLegalName(request.getLegalName());
        value.setLei(request.getLei());
        value.setCountryCode(request.getCountryCode());
        value.setActive(request.isActive());
        return counterparties.save(value);
    }
}

package com.tradesettlement.controller;

import java.util.List;
import java.util.UUID;
import javax.validation.Valid;

import com.tradesettlement.dto.CounterpartyRequest;
import com.tradesettlement.dto.InstrumentRequest;
import com.tradesettlement.entity.Counterparty;
import com.tradesettlement.entity.Instrument;
import com.tradesettlement.service.ReferenceDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference-data")
public class ReferenceDataController {
    private final ReferenceDataService service;
    public ReferenceDataController(ReferenceDataService service) { this.service = service; }

    @GetMapping("/instruments") public List<Instrument> instruments() { return service.instruments(); }
    @PostMapping("/instruments") @ResponseStatus(HttpStatus.CREATED)
    public Instrument createInstrument(@Valid @RequestBody InstrumentRequest request) { return service.saveInstrument(null, request); }
    @PutMapping("/instruments/{id}")
    public Instrument updateInstrument(@PathVariable UUID id, @Valid @RequestBody InstrumentRequest request) {
        return service.saveInstrument(id, request);
    }
    @GetMapping("/counterparties") public List<Counterparty> counterparties() { return service.counterparties(); }
    @PostMapping("/counterparties") @ResponseStatus(HttpStatus.CREATED)
    public Counterparty createCounterparty(@Valid @RequestBody CounterpartyRequest request) { return service.saveCounterparty(null, request); }
    @PutMapping("/counterparties/{id}")
    public Counterparty updateCounterparty(@PathVariable UUID id, @Valid @RequestBody CounterpartyRequest request) {
        return service.saveCounterparty(id, request);
    }
}

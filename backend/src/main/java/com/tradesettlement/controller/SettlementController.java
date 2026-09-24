package com.tradesettlement.controller;

import java.util.List;
import java.util.UUID;
import com.tradesettlement.dto.SettlementStatusResponse;
import com.tradesettlement.service.SettlementQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementQueryService service;
    public SettlementController(SettlementQueryService service) { this.service = service; }
    @GetMapping public List<SettlementStatusResponse> list() { return service.list(); }
    @GetMapping("/trades/{tradeId}") public SettlementStatusResponse byTrade(@PathVariable UUID tradeId) {
        return service.getByTradeId(tradeId);
    }
}

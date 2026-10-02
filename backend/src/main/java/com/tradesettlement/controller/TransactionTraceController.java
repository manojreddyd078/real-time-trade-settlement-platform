package com.tradesettlement.controller;

import java.util.UUID;
import com.tradesettlement.dto.TransactionTraceResponse;
import com.tradesettlement.service.TransactionTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traces")
@Tag(name = "Transaction traces", description = "End-to-end trade and event observability")
public class TransactionTraceController {
    private final TransactionTraceService traces;
    public TransactionTraceController(TransactionTraceService traces) { this.traces = traces; }

    @GetMapping("/{tradeId}")
    @Operation(summary = "Trace a transaction", description = "Returns an ordered timeline of status, event, retry, DLQ, and settlement activity")
    public TransactionTraceResponse trace(@PathVariable UUID tradeId) { return traces.trace(tradeId); }
}

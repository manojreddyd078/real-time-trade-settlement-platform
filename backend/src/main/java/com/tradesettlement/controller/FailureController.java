package com.tradesettlement.controller;

import java.util.List;
import com.tradesettlement.dto.FailedTradeResponse;
import com.tradesettlement.service.FailureQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/failed-trades")
@Tag(name = "Failures", description = "Failed trade and dead-letter operations")
public class FailureController {
    private final FailureQueryService failures;
    public FailureController(FailureQueryService failures) { this.failures = failures; }

    @GetMapping
    @Operation(summary = "List failed trades", description = "Returns business failures and permanently failed events routed to the DLQ")
    public List<FailedTradeResponse> list() { return failures.list(); }
}

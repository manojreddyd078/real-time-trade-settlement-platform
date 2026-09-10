package com.tradesettlement.controller;

import java.net.URI;

import javax.validation.Valid;

import com.tradesettlement.dto.ApiErrorResponse;
import com.tradesettlement.dto.TradeSubmissionRequest;
import com.tradesettlement.dto.TradeSubmissionResponse;
import com.tradesettlement.service.TradeSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trades", description = "Trade capture and lifecycle operations")
public class TradeController {

    private final TradeSubmissionService tradeSubmissionService;

    public TradeController(TradeSubmissionService tradeSubmissionService) {
        this.tradeSubmissionService = tradeSubmissionService;
    }

    @PostMapping
    @Operation(summary = "Submit a trade", description = "Validates reference data and persists a new trade with RECEIVED status")
    @ApiResponse(responseCode = "201", description = "Trade accepted and persisted")
    @ApiResponse(responseCode = "400", description = "Request or business validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Trade reference already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Referenced data does not exist or is inactive",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<TradeSubmissionResponse> submit(@Valid @RequestBody TradeSubmissionRequest request) {
        TradeSubmissionResponse response = tradeSubmissionService.submit(request);
        return ResponseEntity.created(URI.create("/api/trades/" + response.getId())).body(response);
    }
}

package com.tradesettlement.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tradesettlement.dto.TradeSubmissionResponse;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.service.TradeSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeSubmissionService tradeSubmissionService;

    @Test
    void submitsValidTrade() throws Exception {
        UUID tradeId = UUID.fromString("7a0172dc-99c9-4cda-86f3-1b76ed537e1d");
        when(tradeSubmissionService.submit(any())).thenReturn(
                new TradeSubmissionResponse(tradeId, "TRD-2026-10001", TradeStatus.RECEIVED, OffsetDateTime.parse("2026-09-10T14:30:00Z")));

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trades/" + tradeId))
                .andExpect(jsonPath("$.tradeReference").value("TRD-2026-10001"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void rejectsInvalidTradeRequest() throws Exception {
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.tradeReference").exists())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    private String validRequest() {
        return "{"
                + "\"tradeReference\":\"TRD-2026-10001\","
                + "\"tradeType\":\"BUY\","
                + "\"instrumentId\":\"ac38d8e2-6e41-4458-b901-43851cbf19e6\","
                + "\"buyerCounterpartyId\":\"4b5588ed-c391-4079-b14c-34c49a21949a\","
                + "\"sellerCounterpartyId\":\"5e884423-f880-4534-89d2-526542164975\","
                + "\"quantity\":1000,\"price\":125.75,\"currencyCode\":\"USD\","
                + "\"tradeDate\":\"2026-09-10\",\"settlementDate\":\"2026-09-12\"}"
                ;
    }
}

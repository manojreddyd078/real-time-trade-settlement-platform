package com.tradesettlement.controller;

import java.util.Collections;
import java.util.UUID;
import com.tradesettlement.dto.TransactionTraceResponse;
import com.tradesettlement.entity.Trade;
import com.tradesettlement.entity.TradeStatus;
import com.tradesettlement.service.TransactionTraceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionTraceController.class)
class TransactionTraceControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private TransactionTraceService traces;

    @Test
    void returnsTransactionTrace() throws Exception {
        UUID tradeId = UUID.randomUUID(); Trade trade = new Trade(); ReflectionTestUtils.setField(trade, "id", tradeId);
        trade.setTradeReference("TRD-TRACE-1"); trade.setStatus(TradeStatus.RECEIVED);
        when(traces.trace(tradeId)).thenReturn(new TransactionTraceResponse(trade, Collections.singletonList("corr-1"),
                Collections.singletonList(UUID.randomUUID()), null, null, null, Collections.emptyList()));
        mockMvc.perform(get("/api/traces/{tradeId}", tradeId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId").value(tradeId.toString()))
                .andExpect(jsonPath("$.correlationIds[0]").value("corr-1"))
                .andExpect(jsonPath("$.timeline").isArray());
    }
}

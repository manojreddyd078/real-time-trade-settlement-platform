package com.tradesettlement.controller;

import com.tradesettlement.service.FailureQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FailureController.class)
class FailureControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private FailureQueryService failures;

    @Test
    void returnsFailedTradeQueue() throws Exception {
        when(failures.list()).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/failed-trades"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }
}

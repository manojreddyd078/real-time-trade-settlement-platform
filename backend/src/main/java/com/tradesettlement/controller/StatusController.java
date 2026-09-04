package com.tradesettlement.controller;

import com.tradesettlement.dto.ApiStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public ApiStatusResponse status() {
        return new ApiStatusResponse("UP", "trade-settlement-backend");
    }
}

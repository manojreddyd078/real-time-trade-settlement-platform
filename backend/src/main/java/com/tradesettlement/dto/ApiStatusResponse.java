package com.tradesettlement.dto;

public class ApiStatusResponse {

    private final String status;
    private final String service;

    public ApiStatusResponse(String status, String service) {
        this.status = status;
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public String getService() {
        return service;
    }
}

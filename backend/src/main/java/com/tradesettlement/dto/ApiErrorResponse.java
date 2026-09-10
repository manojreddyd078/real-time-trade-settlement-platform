package com.tradesettlement.dto;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

public class ApiErrorResponse {

    private final OffsetDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;
    private final String requestId;
    private final Map<String, String> fieldErrors;

    public ApiErrorResponse(int status, String code, String message, String path, String requestId, Map<String, String> fieldErrors) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
        this.fieldErrors = fieldErrors == null ? Collections.emptyMap() : fieldErrors;
    }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getRequestId() { return requestId; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}

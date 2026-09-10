package com.tradesettlement.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.tradesettlement.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleRequestValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_FAILED", "Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler(TradeValidationException.class)
    ResponseEntity<ApiErrorResponse> handleTradeValidation(TradeValidationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "TRADE_VALIDATION_FAILED", exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateTradeException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateTrade(DuplicateTradeException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "DUPLICATE_TRADE", exception.getMessage(), request, null);
    }

    @ExceptionHandler(ReferenceDataNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleReferenceData(ReferenceDataNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "REFERENCE_DATA_NOT_FOUND", exception.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("trade_persistence_constraint_failed", exception);
        return response(HttpStatus.CONFLICT, "DATA_CONFLICT", "The trade conflicts with existing data", request, null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("unhandled_api_error", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message,
                                                       HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiErrorResponse body = new ApiErrorResponse(status.value(), code, message, request.getRequestURI(), MDC.get("requestId"), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}

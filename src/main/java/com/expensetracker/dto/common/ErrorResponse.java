package com.expensetracker.dto.common;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> validationErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(false, status, error, message, path, Instant.now(), null);
    }

    public static ErrorResponse withValidationErrors(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponse(false, status, error, message, path, Instant.now(), validationErrors);
    }
}

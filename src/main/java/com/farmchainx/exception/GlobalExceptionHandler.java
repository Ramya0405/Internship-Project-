package com.farmchainx.exception;

import com.farmchainx.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers.
 * Thymeleaf controllers handle their own errors via model attributes.
 */
@RestControllerAdvice(basePackages = "com.farmchainx.restcontroller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 404 Not Found ────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    // ── 409 Conflict ─────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "DUPLICATE_EMAIL"));
    }

    // ── 403 Forbidden ────────────────────────────────────────────────────────

    @ExceptionHandler({UnauthorizedAccessException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "ACCESS_DENIED"));
    }

    // ── 401 Unauthorized ─────────────────────────────────────────────────────

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required", "UNAUTHORIZED"));
    }

    // ── 400 Bad Request – validation ─────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", "VALIDATION_ERROR"));
    }

    // ── 400 Bad Request – invalid status transition ───────────────────────────

    @ExceptionHandler(InvalidBatchStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStatus(InvalidBatchStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_STATUS_TRANSITION"));
    }

    // ── 400 Bad Request – illegal argument ───────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "BAD_REQUEST"));
    }

    // ── 500 Internal Server Error ─────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later.",
                        "INTERNAL_ERROR"));
    }
}

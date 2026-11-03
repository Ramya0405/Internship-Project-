package com.farmchainx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an authenticated user attempts an operation outside their role.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}

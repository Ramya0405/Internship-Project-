package com.farmchainx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an invalid batch-status transition is attempted.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidBatchStatusException extends RuntimeException {

    public InvalidBatchStatusException(String message) {
        super(message);
    }
}

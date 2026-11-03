package com.farmchainx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user attempts to register with an email that already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("An account already exists with email: " + email);
    }
}

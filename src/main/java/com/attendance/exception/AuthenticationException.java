package com.attendance.exception;

/**
 * Exception for authentication failures (401)
 */
public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message, "AUTH_FAILED", 401);
    }
}

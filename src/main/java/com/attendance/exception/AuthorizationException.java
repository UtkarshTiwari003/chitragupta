package com.attendance.exception;

/**
 * Exception for authorization failures (403)
 */
public class AuthorizationException extends ApplicationException {
    public AuthorizationException(String message) {
        super(message, "AUTH_FORBIDDEN", 403);
    }
}

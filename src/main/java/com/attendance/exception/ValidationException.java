package com.attendance.exception;

/**
 * Exception for validation errors (400)
 */
public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
    }
}

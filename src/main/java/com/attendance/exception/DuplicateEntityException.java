package com.attendance.exception;

/**
 * Exception for duplicate resources (409)
 */
public class DuplicateEntityException extends ApplicationException {
    public DuplicateEntityException(String message) {
        super(message, "DUPLICATE_ENTRY", 409);
    }
}

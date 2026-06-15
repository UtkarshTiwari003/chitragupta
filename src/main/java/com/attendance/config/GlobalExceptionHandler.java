package com.attendance.config;

import com.attendance.dto.ApiResponse;
import com.attendance.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Global exception handler for all endpoints
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<?>> handleApplicationException(ApplicationException ex, WebRequest request) {
        log.warn("Application exception: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponse.error(ex.getHttpStatus(), ex.getMessage());
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponse.error(404, ex.getMessage());
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(404).body(response);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponse.error(401, ex.getMessage());
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(401).body(response);
    }
    
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthorizationException(AuthorizationException ex, WebRequest request) {
        log.warn("Authorization failed: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponse.error(403, ex.getMessage());
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(403).body(response);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(ValidationException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponse.error(400, ex.getMessage());
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(400).body(response);
    }
    
    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateEntity(DuplicateEntityException ex, WebRequest request) {
        log.warn("Duplicate entry: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponse.error(409, ex.getMessage());
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(409).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error: ", ex);
        ApiResponse<?> response = ApiResponse.error(500, "Internal server error");
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(500).body(response);
    }
}

package com.alaCarte.exception;

import com.alaCarte.dto.common.ApiResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException exception) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), "RESOURCE_NOT_FOUND", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException exception) {
        return buildError(HttpStatus.FORBIDDEN, exception.getMessage(), "ACCESS_DENIED", null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException exception) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), "DUPLICATE_RESOURCE", null);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(ValidationException exception) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), "VALIDATION_ERROR", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_ERROR", errors);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleJwt(JwtException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage(), "UNAUTHORIZED", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception exception) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), "INTERNAL_SERVER_ERROR", null);
    }

    private ResponseEntity<ApiResponse<Object>> buildError(
            HttpStatus status,
            String message,
            String errorCode,
            Object error
    ) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, errorCode, error));
    }
}

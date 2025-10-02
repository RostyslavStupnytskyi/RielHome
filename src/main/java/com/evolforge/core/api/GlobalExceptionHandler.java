package com.evolforge.core.api;

import com.evolforge.core.auth.exception.AuthException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiError> handleAuthException(AuthException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiError.of(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, Object> details = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return ResponseEntity.unprocessableEntity()
                .body(ApiError.of("validation_error", "Validation failed", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, Object> details = exception.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage(), (a, b) -> a));
        return ResponseEntity.unprocessableEntity()
                .body(ApiError.of("validation_error", "Validation failed", details));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException exception) {
        HttpStatusCode status = exception.getStatusCode();
        String message = Optional.ofNullable(exception.getReason())
                .filter(reason -> !reason.isBlank())
                .orElseGet(status::toString);
        return ResponseEntity.status(status)
                .body(ApiError.of("http_" + status.value(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception exception, ServerWebExchange exchange) throws Exception {
        if (exchange.getResponse().isCommitted()) {
            throw exception;
        }
        String message = Optional.ofNullable(exception.getMessage())
                .filter(reason -> !reason.isBlank())
                .orElse("An unexpected error occurred");
        return ResponseEntity.internalServerError()
                .body(ApiError.of("internal_error", message));
    }
}

package com.crm.app.web.error.exception_handler;

import com.crm.app.dto.ApiError;
import com.crm.app.web.error.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DuplicateCheckExceptionHandler {
    @ExceptionHandler(DuplicateCheckNotAllowedException.class)
    public ResponseEntity<ApiError> handleDuplicateCheckNotAllowed(DuplicateCheckNotAllowedException ex,
                                                                   HttpServletRequest request) {
        log.warn("Duplicate check not allowed {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "DUPLICATE_CHECK_NOT_ALLOWED"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateCheckMissingProductException.class)
    public ResponseEntity<ApiError> handleDuplicateCheckMissingProduct(DuplicateCheckMissingProductException ex,
                                                                       HttpServletRequest request) {
        log.warn("Duplicate check missing product {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "DUPLICATE_CHECK_MISSING_PRODUCT"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateCheckAlreadyInProgressException.class)
    public ResponseEntity<ApiError> handleDuplicateCheckAlreadyInProgress(DuplicateCheckAlreadyInProgressException ex,
                                                                          HttpServletRequest request) {
        log.warn("Duplicate check  already in progress {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "DUPLICATE_CHECK_ALREADY_IN_PROGRESS"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateCheckInvalidDataException.class)
    public ResponseEntity<ApiError> handleDuplicateCheckInvalidData(DuplicateCheckInvalidDataException ex,
                                                                    HttpServletRequest request) {
        log.warn("Duplicate check invalid data {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "DUPLICATE_CHECK_INVALID_DATA"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateCheckPermissionDeniedException.class)
    public ResponseEntity<ApiError> handleDuplicatecCheckPermissionDenied(DuplicateCheckPermissionDeniedException ex,
                                                                          HttpServletRequest request) {
        log.warn("Duplicate check permission denied {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "DUPLICATE_CHECK_PERMISSION_DENIED"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}

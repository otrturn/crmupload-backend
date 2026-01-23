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
public class CustomerExceptionHandler {
    @ExceptionHandler(CustomerBlockedException.class)
    public ResponseEntity<ApiError> handleCustomerBlocked(CustomerBlockedException ex,
                                                          HttpServletRequest request) {
        log.warn("Customer is blocked {}: {}", request.getRequestURI(), ex.getMessage());

        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CUSTOMER_BLOCKED"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleCustomerAlreadyExists(CustomerAlreadyExistsException ex,
                                                                HttpServletRequest request) {
        log.warn("Customer already exists on {}: {}", request.getRequestURI(), ex.getMessage());

        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CUSTOMER_ALREADY_EXISTS"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiError> handleCustomerNotFound(CustomerNotFoundException ex,
                                                           HttpServletRequest request) {
        log.warn("Customer not found {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CUSTOMER_NOT_FOUND"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CustomerAcknowledgementInvalidException.class)
    public ResponseEntity<ApiError> handleCustomerAcknowledgementInvalid(CustomerAcknowledgementInvalidException ex,
                                                                         HttpServletRequest request) {
        log.warn("Customer acknowledgement information invalid {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CUSTOMER_ACKNOWLEDGEMENT_INFORMATION_INVALID"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CustomerTermsVersionInvalidException.class)
    public ResponseEntity<ApiError> handleCustomerTermsVersionInvalid(CustomerTermsVersionInvalidException ex,
                                                                      HttpServletRequest request) {
        log.warn("Customer terms version invalid/unknown {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CUSTOMER_TERMS_VERSION_INVALID"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CustomerProductInvalidException.class)
    public ResponseEntity<ApiError> handleProductInvalid(CustomerProductInvalidException ex,
                                                         HttpServletRequest request) {
        log.warn("Customer product invalid/unknown {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CUSTOMER_PRODUCT_INVALID"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}

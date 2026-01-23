package com.crm.app.web.error.exception_handler;

import com.crm.app.dto.ApiError;
import com.crm.app.web.error.RegisterRequestInvalidCustomerDataException;
import com.crm.app.web.error.RegisterRequestInvalidTaxIdException;
import com.crm.app.web.error.RegisterRequestInvalidVatIdException;
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
public class RegistrationExceptionHandler {
    @ExceptionHandler(RegisterRequestInvalidCustomerDataException.class)
    public ResponseEntity<ApiError> handleRegisterInvalidCustomerData(RegisterRequestInvalidCustomerDataException ex,
                                                                      HttpServletRequest request) {
        log.warn("Register validation error (InvalidCustomerData) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "REGISTER_INVALID_CUSTOMER_DATA"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RegisterRequestInvalidTaxIdException.class)
    public ResponseEntity<ApiError> handleRegisterInvalidTaxId(RegisterRequestInvalidTaxIdException ex,
                                                               HttpServletRequest request) {
        log.warn("Register validation error (InvalidTaxId) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "REGISTER_INVALID_TAX_ID"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RegisterRequestInvalidVatIdException.class)
    public ResponseEntity<ApiError> handleRegisterInvalidTVatId(RegisterRequestInvalidVatIdException ex,
                                                                HttpServletRequest request) {
        log.warn("Register validation error  (InvalidVatId)on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "REGISTER_INVALID_VAT_ID"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

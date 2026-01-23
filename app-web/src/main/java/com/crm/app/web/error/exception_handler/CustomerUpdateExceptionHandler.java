package com.crm.app.web.error.exception_handler;

import com.crm.app.dto.ApiError;
import com.crm.app.web.error.UpdateRequestInvalidCustomerDataException;
import com.crm.app.web.error.UpdateRequestInvalidTaxIdException;
import com.crm.app.web.error.UpdateRequestInvalidVatIdException;
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
public class CustomerUpdateExceptionHandler {
    @ExceptionHandler(UpdateRequestInvalidCustomerDataException.class)
    public ResponseEntity<ApiError> handleUpdateInvalidCustomerData(UpdateRequestInvalidCustomerDataException ex,
                                                                    HttpServletRequest request) {
        log.warn("Update validation error (InvalidCustomerData) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "UPDATE_INVALID_CUSTOMER_DATA"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UpdateRequestInvalidTaxIdException.class)
    public ResponseEntity<ApiError> handleUpdateInvalidTaxId(UpdateRequestInvalidTaxIdException ex,
                                                             HttpServletRequest request) {
        log.warn("Update validation error (InvalidTaxId) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "UPDATE_INVALID_TAX_ID"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UpdateRequestInvalidVatIdException.class)
    public ResponseEntity<ApiError> handleUpdateInvalidVatId(UpdateRequestInvalidVatIdException ex,
                                                             HttpServletRequest request) {
        log.warn("Update validation error (InvalidVatId) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "UPDATE_INVALID_VAT_ID"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

package com.crm.app.web.error;

import com.crm.app.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class RegisterValidationExceptionHandler {

    @ExceptionHandler(RegisterRequestInvalidCustomerDataException.class)
    public ResponseEntity<ApiError> handleInvalidCustomerData(RegisterRequestInvalidCustomerDataException ex,
                                                              HttpServletRequest request) {
        log.warn("Register validation error (InvalidCustomerData) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = ApiErrorFactory.build(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request, "REGISTER_INVALID_CUSTOMER_DATA"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RegisterRequestInvalidTaxIdException.class)
    public ResponseEntity<ApiError> handleInvalidTaxId(RegisterRequestInvalidTaxIdException ex,
                                                       HttpServletRequest request) {
        log.warn("Register validation error (InvalidTaxId) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = ApiErrorFactory.build(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request, "REGISTER_INVALID_TAX_ID"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RegisterRequestInvalidVatIdException.class)
    public ResponseEntity<ApiError> handleInvalidVatId(RegisterRequestInvalidVatIdException ex,
                                                       HttpServletRequest request) {
        log.warn("Register validation error (InvalidVatId) on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = ApiErrorFactory.build(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request, "REGISTER_INVALID_VAT_ID"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

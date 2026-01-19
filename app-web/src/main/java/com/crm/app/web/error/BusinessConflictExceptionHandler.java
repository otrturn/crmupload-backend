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
public class BusinessConflictExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex,
                                                       HttpServletRequest request) {
        log.warn("Business error on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = ApiErrorFactory.build(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request,
                "BUSINESS_CONFLICT"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({
            CustomerBlockedException.class,
            CustomerAlreadyExistsException.class,
            CustomerNotFoundException.class,
            CustomerAcknowledgementInvalidException.class,
            CustomerTermsVersionInvalidException.class,
            CustomerProductInvalidException.class
    })
    public ResponseEntity<ApiError> handleCustomerBusinessConflicts(RuntimeException ex,
                                                                    HttpServletRequest request) {
        String code = mapCustomerConflictCode(ex);
        log.warn("Customer/business conflict on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = ApiErrorFactory.build(HttpStatus.CONFLICT, ex.getMessage(), request, code);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private static String mapCustomerConflictCode(RuntimeException ex) {
        if (ex instanceof CustomerBlockedException) return "CUSTOMER_BLOCKED";
        if (ex instanceof CustomerAlreadyExistsException) return "CUSTOMER_ALREADY_EXISTS";
        if (ex instanceof CustomerNotFoundException) return "CUSTOMER_NOT_FOUND";
        if (ex instanceof CustomerAcknowledgementInvalidException)
            return "CUSTOMER_ACKNOWLEDGEMENT_INFORMATION_INVALID";
        if (ex instanceof CustomerTermsVersionInvalidException) return "CUSTOMER_TERMS_VERSION_INVALID";
        if (ex instanceof CustomerProductInvalidException) return "CUSTOMER_PRODUCT_INVALID";
        return "BUSINESS_CONFLICT";
    }
}

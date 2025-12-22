package com.crm.app.web.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleIllegalState(IllegalStateException ex,
                                                                       HttpServletRequest request) {
        log.warn(String.format("Business error on %s: %s", request.getRequestURI(), ex.getMessage()));
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "BUSINESS_CONFLICT"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleAuthentication(AuthenticationException ex,
                                                                         HttpServletRequest request) {
        log.warn(String.format("Authentication failed on %s: %s", request.getRequestURI(), ex.getMessage()));
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid username or password",
                request.getRequestURI(),
                Instant.now(),
                "AUTH_INVALID_CREDENTIALS"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleGeneric(Exception ex,
                                                                  HttpServletRequest request) {
        StackTraceElement origin = ex.getStackTrace().length > 0
                ? ex.getStackTrace()[0]
                : null;
        String originInfo = (origin != null)
                ? String.format("%s#%s (line %d)",
                origin.getClassName(),
                origin.getMethodName(),
                origin.getLineNumber())
                : "unknown origin";
        log.error(String.format("Unexpected error on %s in %s", request.getRequestURI(), originInfo), ex);
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected server error",
                request.getRequestURI(),
                Instant.now(),
                "GENERIC_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(RegisterRequestInvalidCustomerDataException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleRegisterInvalid(RegisterRequestInvalidCustomerDataException ex,
                                                                          HttpServletRequest request) {
        log.warn("Register validation error on {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "REGISTER_INVALID_CUSTOMER_DATA"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCustomerAlreadyExists(CustomerAlreadyExistsException ex,
                                                                                HttpServletRequest request) {
        log.warn("Customer already exists on {}: {}", request.getRequestURI(), ex.getMessage());

        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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
    public ResponseEntity<com.crm.app.dto.ApiError> handleNotFound(CustomerNotFoundException ex,
                                                                   HttpServletRequest request) {
        log.warn("Customer not found {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CUSTOMER_NOT_FOUND"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateCheckNotAllowedException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleDuplicateCheckNotAllowed(DuplicateCheckNotAllowedException ex,
                                                                                   HttpServletRequest request) {
        log.warn("Duplicate check not allowed {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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
    public ResponseEntity<com.crm.app.dto.ApiError> handleDuplicateCheckMissingProduct(DuplicateCheckMissingProductException ex,
                                                                                       HttpServletRequest request) {
        log.warn("Duplicate check missing product {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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
    public ResponseEntity<com.crm.app.dto.ApiError> handleDuplicateCheckAlreadyInProgress(DuplicateCheckAlreadyInProgressException ex,
                                                                                          HttpServletRequest request) {
        log.warn("Duplicate check  already in progress {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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
    public ResponseEntity<com.crm.app.dto.ApiError> handleDuplicatecheckInvalidData(DuplicateCheckInvalidDataException ex,
                                                                                    HttpServletRequest request) {
        log.warn("Duplicate check invalid data {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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
    public ResponseEntity<com.crm.app.dto.ApiError> handleDuplicatecheckPermissionDenied(DuplicateCheckPermissionDeniedException ex,
                                                                                         HttpServletRequest request) {
        log.warn("Duplicate check permission denied {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "DUPLICATE_CHECK_PERMISSION_DENIED"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CrmUploadNotAllowedException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCrmUploadNotAllowed(CrmUploadNotAllowedException ex,
                                                                              HttpServletRequest request) {
        log.warn("CRM-Upload not allowed {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CRM_UPLOAD_NOT_ALLOWED"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CrmUploadMissingProductException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCrmUploadMissingProduct(CrmUploadMissingProductException ex,
                                                                                  HttpServletRequest request) {
        log.warn("CRM-Upload missing product {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CRM_UPLOAD_MISSING_PRODUCT"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CrmUploadAlreadyInProgressException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCrmUploadAlreadyInProgress(CrmUploadAlreadyInProgressException ex,
                                                                                     HttpServletRequest request) {
        log.warn("CRM-Upload already in progress {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CRM_UPLOAD_ALREADY_IN_PROGRESS"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CrmUploadInvalidDataException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCrmUploadInvalidData(CrmUploadInvalidDataException ex,
                                                                               HttpServletRequest request) {
        log.warn("CRM-Upload invalid data {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CRM_UPLOAD_INVALID_DATA"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CrmUploadPermissionDeniedException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCrmUploadPermissionDenied(CrmUploadPermissionDeniedException ex,
                                                                                    HttpServletRequest request) {
        log.warn("CRM-Upload permission denied {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CRM_UPLOAD_PERMISSION_DENIED"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CrmUploadForbiddenUseException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCrmUploadForbiddenUse(CrmUploadForbiddenUseException ex,
                                                                                HttpServletRequest request) {
        log.warn("CRM-Upload forbidden use {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "CRM_UPLOAD_FORBIDDEN_USE"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CustomerAcknowledgementInvalidException.class)
    public ResponseEntity<com.crm.app.dto.ApiError> handleCustomerAcknowledgementInvalid(CustomerAcknowledgementInvalidException ex,
                                                                                         HttpServletRequest request) {
        log.warn("Customer acknowledgement information invalid {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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
    public ResponseEntity<com.crm.app.dto.ApiError> handleCustomerTermsVersionInvalid(CustomerTermsVersionInvalidException ex,
                                                                                      HttpServletRequest request) {
        log.warn("Customer terms version invalid/unknown {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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
    public ResponseEntity<com.crm.app.dto.ApiError> handleProductInvalid(CustomerProductInvalidException ex,
                                                                         HttpServletRequest request) {
        log.warn("Customer product invalid/unknown {}: {}", request.getRequestURI(), ex.getMessage());
        com.crm.app.dto.ApiError body = new com.crm.app.dto.ApiError(
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

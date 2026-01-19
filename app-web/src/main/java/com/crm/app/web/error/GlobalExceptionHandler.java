package com.crm.app.web.error;

import com.crm.app.dto.ApiError;
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
@SuppressWarnings("squid:S6539")
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex,
                                                       HttpServletRequest request) {
        log.warn(String.format("Business error on %s: %s", request.getRequestURI(), ex.getMessage()));
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex,
                                                         HttpServletRequest request) {
        log.warn(String.format("Authentication failed on %s: %s", request.getRequestURI(), ex.getMessage()));
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleGeneric(Exception ex,
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
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleUpdateInvalidTVatId(UpdateRequestInvalidVatIdException ex,
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
    public ResponseEntity<ApiError> handleDuplicatecCeckPermissionDenied(DuplicateCheckPermissionDeniedException ex,
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

    @ExceptionHandler(CrmUploadNotAllowedException.class)
    public ResponseEntity<ApiError> handleCrmUploadNotAllowed(CrmUploadNotAllowedException ex,
                                                              HttpServletRequest request) {
        log.warn("CRM-Upload not allowed {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleCrmUploadMissingProduct(CrmUploadMissingProductException ex,
                                                                  HttpServletRequest request) {
        log.warn("CRM-Upload missing product {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleCrmUploadAlreadyInProgress(CrmUploadAlreadyInProgressException ex,
                                                                     HttpServletRequest request) {
        log.warn("CRM-Upload already in progress {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleCrmUploadInvalidData(CrmUploadInvalidDataException ex,
                                                               HttpServletRequest request) {
        log.warn("CRM-Upload invalid data {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleCrmUploadPermissionDenied(CrmUploadPermissionDeniedException ex,
                                                                    HttpServletRequest request) {
        log.warn("CRM-Upload permission denied {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
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
    public ResponseEntity<ApiError> handleCrmUploadForbiddenUse(CrmUploadForbiddenUseException ex,
                                                                HttpServletRequest request) {
        log.warn("CRM-Upload forbidden use {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
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

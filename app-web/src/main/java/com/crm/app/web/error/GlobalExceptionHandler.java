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

    @ExceptionHandler(RegisterRequestInvalidDataException.class)
    public ResponseEntity<ApiError> handleRegisterInvalid(RegisterRequestInvalidDataException ex,
                                                          HttpServletRequest request) {
        log.warn("Register validation error on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "REGISTER_INVALID_DATA"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
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
    public ResponseEntity<ApiError> handleNotFound(CustomerNotFoundException ex,
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

    @ExceptionHandler(UploadAlreadyInProgressException.class)
    public ResponseEntity<ApiError> handleUploadAlreadyInProgress(UploadAlreadyInProgressException ex,
                                                                  HttpServletRequest request) {
        log.warn("Upload already in progress {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "UPLOAD_ALREADY_IN_PROGRESS"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(UploadNotAllowedException.class)
    public ResponseEntity<ApiError> handleUploadNotAllowed(UploadNotAllowedException ex,
                                                           HttpServletRequest request) {
        log.warn("Upload not allowed {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                "UPLOAD_NOT_ALLOWED"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CustomerAcknowledgementInvalidException.class)
    public ResponseEntity<ApiError> handleCustomerAcknowledgementInvalid(UploadNotAllowedException ex,
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
}

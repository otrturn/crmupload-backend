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
public class CrmUploadExceptionHandler {

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

}

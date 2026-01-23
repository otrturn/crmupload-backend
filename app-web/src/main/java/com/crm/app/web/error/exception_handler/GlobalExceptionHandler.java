package com.crm.app.web.error.exception_handler;

import com.crm.app.dto.ApiError;
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
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

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

}

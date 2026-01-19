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
public class DuplicateCheckExceptionHandler {

    @ExceptionHandler({
            DuplicateCheckNotAllowedException.class,
            DuplicateCheckMissingProductException.class,
            DuplicateCheckAlreadyInProgressException.class,
            DuplicateCheckInvalidDataException.class,
            DuplicateCheckPermissionDeniedException.class
    })
    public ResponseEntity<ApiError> handleDuplicateCheck(RuntimeException ex,
                                                         HttpServletRequest request) {
        String code = mapDuplicateCheckCode(ex);
        log.warn("Duplicate check error on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = ApiErrorFactory.build(HttpStatus.CONFLICT, ex.getMessage(), request, code);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private static String mapDuplicateCheckCode(RuntimeException ex) {
        if (ex instanceof DuplicateCheckNotAllowedException) return "DUPLICATE_CHECK_NOT_ALLOWED";
        if (ex instanceof DuplicateCheckMissingProductException) return "DUPLICATE_CHECK_MISSING_PRODUCT";
        if (ex instanceof DuplicateCheckAlreadyInProgressException) return "DUPLICATE_CHECK_ALREADY_IN_PROGRESS";
        if (ex instanceof DuplicateCheckInvalidDataException) return "DUPLICATE_CHECK_INVALID_DATA";
        if (ex instanceof DuplicateCheckPermissionDeniedException) return "DUPLICATE_CHECK_PERMISSION_DENIED";
        return "DUPLICATE_CHECK_ERROR";
    }
}

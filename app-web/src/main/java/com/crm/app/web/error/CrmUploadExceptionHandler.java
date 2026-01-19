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
public class CrmUploadExceptionHandler {

    @ExceptionHandler({
            CrmUploadNotAllowedException.class,
            CrmUploadMissingProductException.class,
            CrmUploadAlreadyInProgressException.class,
            CrmUploadInvalidDataException.class,
            CrmUploadPermissionDeniedException.class,
            CrmUploadForbiddenUseException.class
    })
    public ResponseEntity<ApiError> handleCrmUpload(RuntimeException ex,
                                                    HttpServletRequest request) {
        String code = mapCrmUploadCode(ex);
        log.warn("CRM-Upload error on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = ApiErrorFactory.build(HttpStatus.CONFLICT, ex.getMessage(), request, code);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private static String mapCrmUploadCode(RuntimeException ex) {
        if (ex instanceof CrmUploadNotAllowedException) return "CRM_UPLOAD_NOT_ALLOWED";
        if (ex instanceof CrmUploadMissingProductException) return "CRM_UPLOAD_MISSING_PRODUCT";
        if (ex instanceof CrmUploadAlreadyInProgressException) return "CRM_UPLOAD_ALREADY_IN_PROGRESS";
        if (ex instanceof CrmUploadInvalidDataException) return "CRM_UPLOAD_INVALID_DATA";
        if (ex instanceof CrmUploadPermissionDeniedException) return "CRM_UPLOAD_PERMISSION_DENIED";
        if (ex instanceof CrmUploadForbiddenUseException) return "CRM_UPLOAD_FORBIDDEN_USE";
        return "CRM_UPLOAD_ERROR";
    }
}

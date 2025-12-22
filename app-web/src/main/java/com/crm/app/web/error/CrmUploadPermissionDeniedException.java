package com.crm.app.web.error;

public class CrmUploadPermissionDeniedException extends RuntimeException {
    public CrmUploadPermissionDeniedException(String message) {
        super(message);
    }
}

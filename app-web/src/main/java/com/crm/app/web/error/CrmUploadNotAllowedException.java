package com.crm.app.web.error;

public class CrmUploadNotAllowedException extends RuntimeException {
    public CrmUploadNotAllowedException(String message) {
        super(message);
    }
}

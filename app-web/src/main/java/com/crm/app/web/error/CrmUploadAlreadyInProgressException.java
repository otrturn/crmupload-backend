package com.crm.app.web.error;

public class CrmUploadAlreadyInProgressException extends RuntimeException {
    public CrmUploadAlreadyInProgressException(String message) {
        super(message);
    }
}

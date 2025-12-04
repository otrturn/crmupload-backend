package com.crm.app.web.error;

public class UploadAlreadyInProgressException extends RuntimeException {
    public UploadAlreadyInProgressException(String message) {
        super(message);
    }
}

package com.crm.app.web.error;

public class UploadNotAllowedException extends RuntimeException {
    public UploadNotAllowedException(String message) {
        super(message);
    }
}

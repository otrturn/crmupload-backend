package com.crm.app.web.error;

public class DuplicateCheckMissingProductException extends RuntimeException {
    public DuplicateCheckMissingProductException(String message) {
        super(message);
    }
}

package com.crm.app.web.error;

public class DuplicateCheckAlreadyInProgressException extends RuntimeException {
    public DuplicateCheckAlreadyInProgressException(String message) {
        super(message);
    }
}

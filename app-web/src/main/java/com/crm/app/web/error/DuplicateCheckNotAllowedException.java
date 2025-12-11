package com.crm.app.web.error;

public class DuplicateCheckNotAllowedException extends RuntimeException {
    public DuplicateCheckNotAllowedException(String message) {
        super(message);
    }
}

package com.crm.app.web.error;

public class DuplicateCheckPermissionDeniedException extends RuntimeException {
    public DuplicateCheckPermissionDeniedException(String message) {
        super(message);
    }
}

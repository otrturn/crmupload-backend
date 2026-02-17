package com.crm.app.duplicate_check_common.error;

public class DuplicateCheckGpuException extends RuntimeException {
    public DuplicateCheckGpuException(String message) {
        super(message);
    }

    public DuplicateCheckGpuException(String message, Exception e) {
        super(message, e);
    }
}

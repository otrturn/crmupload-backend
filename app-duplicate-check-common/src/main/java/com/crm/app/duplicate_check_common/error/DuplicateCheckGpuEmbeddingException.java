package com.crm.app.duplicate_check_common.error;

public class DuplicateCheckGpuEmbeddingException extends RuntimeException {
    public DuplicateCheckGpuEmbeddingException(String message) {
        super(message);
    }

    public DuplicateCheckGpuEmbeddingException(String message, Exception e) {
        super(message, e);
    }
}

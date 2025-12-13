package com.crm.app.worker_duplicate_check_gpu.error;

public class WorkerDuplicateCheckGpuException extends RuntimeException {
    public WorkerDuplicateCheckGpuException(String message) {
        super(message);
    }

    public WorkerDuplicateCheckGpuException(String message, Exception e) {
        super(message, e);
    }
}

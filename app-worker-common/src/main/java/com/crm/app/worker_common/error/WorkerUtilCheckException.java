package com.crm.app.worker_common.error;

public class WorkerUtilCheckException extends RuntimeException {
    public WorkerUtilCheckException(String message) {
        super(message);
    }
    public WorkerUtilCheckException(String message, Exception e) {
        super(message,e);
    }
}

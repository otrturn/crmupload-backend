package com.crm.app.duplicate_check_common.error;

public class WorkbookUtilCheckException extends RuntimeException {
    public WorkbookUtilCheckException(String message) {
        super(message);
    }

    public WorkbookUtilCheckException(String message, Exception e) {
        super(message, e);
    }
}

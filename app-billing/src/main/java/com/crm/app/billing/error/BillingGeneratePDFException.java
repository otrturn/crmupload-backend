package com.crm.app.billing.error;

public class BillingGeneratePDFException extends RuntimeException {
    public BillingGeneratePDFException(String message) {
        super(message);
    }

    public BillingGeneratePDFException(String message, Exception e) {
        super(message, e);
    }
}

package com.crm.app.billing.error;

public class BillingZUGFeRDException extends RuntimeException {
    public BillingZUGFeRDException(String message) {
        super(message);
    }

    public BillingZUGFeRDException(String message, Exception e) {
        super(message, e);
    }
}

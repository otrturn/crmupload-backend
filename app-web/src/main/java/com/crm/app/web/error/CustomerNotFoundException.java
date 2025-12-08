package com.crm.app.web.error;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String emailAddress) {
        super("No customer found for email: " + emailAddress);
    }
}

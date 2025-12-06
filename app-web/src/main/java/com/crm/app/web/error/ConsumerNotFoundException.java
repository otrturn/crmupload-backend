package com.crm.app.web.error;

public class ConsumerNotFoundException extends RuntimeException {
    public ConsumerNotFoundException(String emailAddress) {
        super("No consumer found for email: " + emailAddress);
    }
}

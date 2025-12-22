package com.crm.app.web.error;

public class CustomerProductInvalidException extends RuntimeException {
    public CustomerProductInvalidException(String message) {
        super(message);
    }
}

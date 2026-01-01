package com.crm.app.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomerBlockedException extends RuntimeException {
    public CustomerBlockedException(String message) {
        super(message);
    }
}

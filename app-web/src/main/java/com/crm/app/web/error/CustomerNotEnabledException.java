package com.crm.app.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomerNotEnabledException extends RuntimeException {
    public CustomerNotEnabledException(String message) {
        super(message);
    }
}

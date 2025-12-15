package com.crm.app.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RegisterRequestInvalidDataException extends RuntimeException {

    private final String errorCode;

    public RegisterRequestInvalidDataException(String message) {
        super(message);
        this.errorCode = "REGISTER_INVALID_DATA";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
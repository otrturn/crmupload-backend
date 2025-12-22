package com.crm.app.web.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RegisterRequestInvalidCustomerDataException extends RuntimeException {

    private final String errorCode;

    public RegisterRequestInvalidCustomerDataException(String message) {
        super(message);
        this.errorCode = "REGISTER_INVALID_CUSTOMER_DATA";
    }

}
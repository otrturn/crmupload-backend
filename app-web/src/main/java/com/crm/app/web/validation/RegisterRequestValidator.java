package com.crm.app.web.validation;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.web.error.RegisterRequestInvalidDataException;

public final class RegisterRequestValidator {

    private RegisterRequestValidator() {
    }

    public static void assertValid(RegisterRequest request) {
        if (request == null) {
            throw new RegisterRequestInvalidDataException("registration: request must not be null");
        }

        String emailAddress = request.email_address(); // für Logging/Fehlermeldung

        boolean invalid =
                (!stringIsFilled(request.firstname()) || !stringIsFilled(request.lastname()))
                        && !stringIsFilled(request.company_name());

        if (invalid) {
            throw new RegisterRequestInvalidDataException(
                    String.format(
                            "registration: Customer %s firstName/lastName/company_name invalid",
                            emailAddress
                    )
            );
        }
    }

    private static boolean stringIsFilled(String value) {
        return value != null && !value.isBlank();
    }
}


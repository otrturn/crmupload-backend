package com.crm.app.web.validation;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.util.CheckAddress;
import com.crm.app.web.error.CustomerAcknowledgementInvalidException;
import com.crm.app.web.error.RegisterRequestInvalidCustomerDataException;

public final class RegisterRequestValidator {

    private RegisterRequestValidator() {
    }

    public static void assertValid(RegisterRequest request) {
        /*
        NULL
         */
        if (request == null) {
            throw new RegisterRequestInvalidCustomerDataException("registration: request must not be null");
        }

        /*
        Email address
         */
        if (stringIsEmpty(request.email_address())) {
            throw new RegisterRequestInvalidCustomerDataException("registration: Customer with no e-mail address");
        }

        String emailAddress = request.email_address();

        /*
        Names
         */
        boolean invalid =
                (stringIsEmpty(request.firstname()) || stringIsEmpty(request.lastname()))
                        && stringIsEmpty(request.company_name());

        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format(
                            "registration: Customer %s firstName/lastName/company_name invalid",
                            emailAddress
                    )
            );
        }

        /*
        Address
         */
        invalid = stringIsEmpty(request.adrline1()) || stringIsEmpty(request.postalcode()) || stringIsEmpty(request.city()) || stringIsEmpty(request.country());

        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format(
                            "registration: Customer %s AdrLine1/postlCode/city/country invalid",
                            emailAddress
                    )
            );
        }

        /*
        Postcalcode
         */
        invalid = !CheckAddress.checkPostalCode(request.country(), request.postalcode());

        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format(
                            "registration: Customer %s postalCode for country invalid",
                            emailAddress
                    )
            );
        }

        /*
        phone number
         */
        if (stringIsEmpty(request.phone_number())) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format(
                            "registration: Customer %s phone number invalid",
                            emailAddress
                    )
            );
        }

        /*
        Password
         */
        if (stringIsEmpty(request.password()))
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format(
                            "registration: Customer %s password invalid",
                            emailAddress
                    )
            );
        /*
        Acknowledgement
         */
        if (!request.agb_accepted()
                || !request.is_entrepreneur()
                || !request.request_immediate_service_start()
                || !request.acknowledge_withdrawal_loss()) {
            String msg = String.format(
                    "Customer with email %s -> invalid acknowledgement information [agb_accepted][is_entrepreneur][request_immediate_service_start][acknowledge_withdrawal_loss] [%s][%s][%s][%s]",
                    emailAddress, request.agb_accepted(), request.is_entrepreneur(), request.request_immediate_service_start(), !request.acknowledge_withdrawal_loss());
            throw new CustomerAcknowledgementInvalidException(msg);
        }

    }

    private static boolean stringIsEmpty(String value) {
        return value == null || value.isBlank();
    }
}


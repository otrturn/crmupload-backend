package com.crm.app.web.validation;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.util.CheckAddress;
import com.crm.app.web.error.CustomerAcknowledgementInvalidException;
import com.crm.app.web.error.RegisterRequestInvalidCustomerDataException;
import com.crm.app.web.error.RegisterRequestInvalidTaxIdException;
import com.crm.app.web.error.RegisterRequestInvalidVatIdException;

import static com.crm.app.web.validation.RequestValidator.*;

public final class RegisterRequestValidator {

    private RegisterRequestValidator() {
    }

    public static void assertValid(RegisterRequest request) {
        requireRequest(request);

        final String emailAddress = requireEmail(request);

        requireNamesOrCompany(request, emailAddress);
        requireAddress(request, emailAddress);
        requireValidPostalCode(request, emailAddress);
        requirePhoneNumber(request, emailAddress);
        requireValidTaxId(request, emailAddress);
        requireValidVatIdIfPresent(request, emailAddress);
        requirePassword(request, emailAddress);
        requireProducts(request, emailAddress);
        requireAcknowledgement(request, emailAddress);
    }

    private static void requireRequest(RegisterRequest request) {
        if (request == null) {
            throw new RegisterRequestInvalidCustomerDataException("registration: request must not be null");
        }
    }

    private static String requireEmail(RegisterRequest request) {
        if (stringIsEmpty(request.email_address())) {
            throw new RegisterRequestInvalidCustomerDataException("registration: Customer with no e-mail address");
        }
        return request.email_address();
    }

    private static void requireNamesOrCompany(RegisterRequest request, String emailAddress) {
        boolean invalid =
                (stringIsEmpty(request.firstname()) || stringIsEmpty(request.lastname()))
                        && stringIsEmpty(request.company_name());

        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s firstName/lastName/company_name invalid", emailAddress)
            );
        }
    }

    private static void requireAddress(RegisterRequest request, String emailAddress) {
        boolean invalid = stringIsEmpty(request.adrline1())
                || stringIsEmpty(request.postalcode())
                || stringIsEmpty(request.city())
                || stringIsEmpty(request.country());

        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s AdrLine1/postlCode/city/country invalid", emailAddress)
            );
        }
    }

    private static void requireValidPostalCode(RegisterRequest request, String emailAddress) {
        boolean invalid = !CheckAddress.checkPostalCode(request.country(), request.postalcode());
        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s postalCode for country invalid", emailAddress)
            );
        }
    }

    private static void requirePhoneNumber(RegisterRequest request, String emailAddress) {
        if (stringIsEmpty(request.phone_number())) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s phone number invalid", emailAddress)
            );
        }
    }

    private static void requireValidTaxId(RegisterRequest request, String emailAddress) {
        if ("DE".equals(request.country()) && (stringIsEmpty(request.tax_id()) || !isValidGermanTaxId(request.tax_id()))) {
            throw new RegisterRequestInvalidTaxIdException(
                    String.format("registration: Customer %s taxId invalid", emailAddress)
            );
        }
    }

    private static void requireValidVatIdIfPresent(RegisterRequest request, String emailAddress) {
        if ("DE".equals(request.country()) && !stringIsEmpty(request.vat_id()) && !isValidGermanVatId(request.vat_id())) {
            throw new RegisterRequestInvalidVatIdException(
                    String.format("registration: Customer %s vatId invalid", emailAddress)
            );
        }
    }

    private static void requirePassword(RegisterRequest request, String emailAddress) {
        if (stringIsEmpty(request.password())) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s password invalid", emailAddress)
            );
        }
    }

    private static void requireProducts(RegisterRequest request, String emailAddress) {
        if (request.products() == null || request.products().isEmpty()) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s no products", emailAddress)
            );
        }
    }

    private static void requireAcknowledgement(RegisterRequest request, String emailAddress) {
        if (!request.agb_accepted()
                || !request.is_entrepreneur()
                || !request.request_immediate_service_start()
                || !request.acknowledge_withdrawal_loss()) {

            String msg = String.format(
                    "Customer with email %s -> invalid acknowledgement information " +
                            "[agb_accepted][is_entrepreneur][request_immediate_service_start][acknowledge_withdrawal_loss] " +
                            "[%s][%s][%s][%s]",
                    emailAddress,
                    request.agb_accepted(),
                    request.is_entrepreneur(),
                    request.request_immediate_service_start(),
                    request.acknowledge_withdrawal_loss()
            );
            throw new CustomerAcknowledgementInvalidException(msg);
        }
    }
}

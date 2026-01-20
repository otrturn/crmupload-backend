package com.crm.app.web.validation;

import com.crm.app.dto.CustomerProfile;
import com.crm.app.util.CheckAddress;
import com.crm.app.web.error.UpdateRequestInvalidCustomerDataException;
import com.crm.app.web.error.UpdateRequestInvalidTaxIdException;
import com.crm.app.web.error.UpdateRequestInvalidVatIdException;

import static com.crm.app.web.validation.RequestValidator.isValidGermanTaxId;
import static com.crm.app.web.validation.RequestValidator.isValidGermanVatId;

public final class UpdateRequestValidator {

    private UpdateRequestValidator() {
    }

    public static void assertValid(CustomerProfile customerProfile) {
        /*
        NULL
         */
        if (customerProfile == null) {
            throw new UpdateRequestInvalidCustomerDataException("updateCustomer: customerProfile must not be null");
        }

        /*
        Email address
         */
        if (stringIsEmpty(customerProfile.email_address())) {
            throw new UpdateRequestInvalidCustomerDataException("updateCustomer: Customer with no e-mail address");
        }

        String emailAddress = customerProfile.email_address();

        /*
        Names
         */
        boolean invalid =
                (stringIsEmpty(customerProfile.firstname()) || stringIsEmpty(customerProfile.lastname()))
                        && stringIsEmpty(customerProfile.company_name());

        if (invalid) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s firstName/lastName/company_name invalid",
                            emailAddress
                    )
            );
        }

        /*
        Address
         */
        invalid = stringIsEmpty(customerProfile.adrline1()) || stringIsEmpty(customerProfile.postalcode()) || stringIsEmpty(customerProfile.city()) || stringIsEmpty(customerProfile.country());

        if (invalid) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s AdrLine1/postlCode/city/country invalid",
                            emailAddress
                    )
            );
        }

        /*
        Postalcode
         */
        invalid = !CheckAddress.checkPostalCode(customerProfile.country(), customerProfile.postalcode());

        if (invalid) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s postalCode for country invalid",
                            emailAddress
                    )
            );
        }

        /*
        Phone number
         */
        if (stringIsEmpty(customerProfile.phone_number())) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s phone number invalid",
                            emailAddress
                    )
            );
        }

        /*
        Tax Id - Steuernummer
         */
        if ("DE".equals(customerProfile.country()) && (stringIsEmpty(customerProfile.tax_id()) || !isValidGermanTaxId(customerProfile.tax_id()))) {
            throw new UpdateRequestInvalidTaxIdException(
                    String.format(
                            "registration: Customer %s taxId invalid",
                            emailAddress
                    )
            );
        }

        /*
        Vat Id - Ust-IdNr.
         */
        if ("DE".equals(customerProfile.country()) && !stringIsEmpty(customerProfile.vat_id()) && !isValidGermanVatId(customerProfile.vat_id())) {
            throw new UpdateRequestInvalidVatIdException(
                    String.format(
                            "registration: Customer %s vatId invalid",
                            emailAddress
                    )
            );
        }

    }

    private static boolean stringIsEmpty(String value) {
        return value == null || value.isBlank();
    }
}


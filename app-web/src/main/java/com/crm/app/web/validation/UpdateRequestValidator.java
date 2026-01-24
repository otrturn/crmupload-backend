package com.crm.app.web.validation;

import com.crm.app.dto.CustomerProfile;
import com.crm.app.util.CheckAddress;
import com.crm.app.web.error.UpdateRequestInvalidCustomerDataException;
import com.crm.app.web.error.UpdateRequestInvalidTaxIdException;
import com.crm.app.web.error.UpdateRequestInvalidVatIdException;

import static com.crm.app.web.util.WebUtils.stringIsEmpty;
import static com.crm.app.web.validation.RequestValidator.isNotValidGermanVatId;

public final class UpdateRequestValidator {

    private UpdateRequestValidator() {
    }

    public static void assertValid(CustomerProfile customerProfile) {
        requireCustomerProfile(customerProfile);
        final String emailAddress = requireEmail(customerProfile);
        requireNamesOrCompany(customerProfile, emailAddress);
        requireAddress(customerProfile, emailAddress);
        requireValidPostalCode(customerProfile, emailAddress);
        requirePhoneNumber(customerProfile, emailAddress);
        requireValidTaxId(customerProfile, emailAddress);
        requireValidVatIdIfPresent(customerProfile, emailAddress);
    }

    private static void requireCustomerProfile(CustomerProfile customerProfile) {
        if (customerProfile == null) {
            throw new UpdateRequestInvalidCustomerDataException("updateCustomer: customerProfile must not be null");
        }
    }

    private static void requireValidTaxId(CustomerProfile customerProfile, String emailAddress) {
        if ("DE".equals(customerProfile.country()) && (stringIsEmpty(customerProfile.taxId()))) {
            throw new UpdateRequestInvalidTaxIdException(
                    String.format("updateCustomer: Customer %s taxId empty", emailAddress)
            );
        }
    }

    private static void requireValidVatIdIfPresent(CustomerProfile customerProfile, String emailAddress) {
        if ("DE".equals(customerProfile.country()) && !stringIsEmpty(customerProfile.vatId()) && isNotValidGermanVatId(customerProfile.vatId())) {
            throw new UpdateRequestInvalidVatIdException(
                    String.format(
                            "updateCustomer: Customer %s vatId empty or invalid",
                            emailAddress
                    )
            );
        }
    }

    private static void requirePhoneNumber(CustomerProfile customerProfile, String emailAddress) {
        if (stringIsEmpty(customerProfile.phoneNumber())) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s phone number invalid",
                            emailAddress
                    )
            );
        }
    }

    private static void requireValidPostalCode(CustomerProfile customerProfile, String emailAddress) {
        boolean invalid;
        invalid = !CheckAddress.checkPostalCode(customerProfile.country(), customerProfile.postalcode());

        if (invalid) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s postalCode for country invalid",
                            emailAddress
                    )
            );
        }
    }

    private static void requireAddress(CustomerProfile customerProfile, String emailAddress) {
        boolean invalid;
        invalid = stringIsEmpty(customerProfile.adrline1()) || stringIsEmpty(customerProfile.postalcode()) || stringIsEmpty(customerProfile.city()) || stringIsEmpty(customerProfile.country());

        if (invalid) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s AdrLine1/postalCode/city/country invalid",
                            emailAddress
                    )
            );
        }
    }

    private static void requireNamesOrCompany(CustomerProfile customerProfile, String emailAddress) {
        boolean invalid =
                (stringIsEmpty(customerProfile.firstname()) || stringIsEmpty(customerProfile.lastname()))
                        && stringIsEmpty(customerProfile.companyName());

        if (invalid) {
            throw new UpdateRequestInvalidCustomerDataException(
                    String.format(
                            "updateCustomer: Customer %s firstName/lastName/companyName invalid",
                            emailAddress
                    )
            );
        }
    }

    private static String requireEmail(CustomerProfile customerProfile) {
        if (stringIsEmpty(customerProfile.emailAddress())) {
            throw new UpdateRequestInvalidCustomerDataException("updateCustomer: Customer with no e-mail address");
        }
        return customerProfile.emailAddress();
    }

}


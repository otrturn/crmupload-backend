package com.crm.app.web.validation;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.util.CheckAddress;
import com.crm.app.web.error.CustomerAcknowledgementInvalidException;
import com.crm.app.web.error.RegisterRequestInvalidCustomerDataException;
import com.crm.app.web.error.RegisterRequestInvalidTaxIdException;
import com.crm.app.web.error.RegisterRequestInvalidVatIdException;

import static com.crm.app.web.util.WebUtils.stringIsEmpty;
import static com.crm.app.web.validation.RequestValidator.isNotValidGermanVatId;

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
        if (stringIsEmpty(request.getEmailAddress())) {
            throw new RegisterRequestInvalidCustomerDataException("registration: Customer with no e-mail address");
        }
        return request.getEmailAddress();
    }

    private static void requireNamesOrCompany(RegisterRequest request, String emailAddress) {
        boolean invalid =
                (stringIsEmpty(request.getFirstname()) || stringIsEmpty(request.getLastname()))
                        && stringIsEmpty(request.getCompanyName());

        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s firstName/lastName/companyName invalid", emailAddress)
            );
        }
    }

    private static void requireAddress(RegisterRequest request, String emailAddress) {
        System.out.println("[" + request.getAdrline1() + "][" + request.getPostalcode() + "][" + request.getCity());
        boolean invalid = stringIsEmpty(request.getAdrline1())
                || stringIsEmpty(request.getPostalcode())
                || stringIsEmpty(request.getCity())
                || stringIsEmpty(request.getCountry());

        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s adrLine1/postalCode/city/country invalid", emailAddress)
            );
        }
    }

    private static void requireValidPostalCode(RegisterRequest request, String emailAddress) {
        boolean invalid = !CheckAddress.checkPostalCode(request.getCountry(), request.getPostalcode());
        if (invalid) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s postalCode for country invalid", emailAddress)
            );
        }
    }

    private static void requirePhoneNumber(RegisterRequest request, String emailAddress) {
        if (stringIsEmpty(request.getPhoneNumber())) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s phone number invalid", emailAddress)
            );
        }
    }

    private static void requireValidTaxId(RegisterRequest request, String emailAddress) {
        if ("DE".equals(request.getCountry()) && (stringIsEmpty(request.getTaxId()))) {
            throw new RegisterRequestInvalidTaxIdException(
                    String.format("registration: Customer %s taxId empty", emailAddress)
            );
        }
    }

    private static void requireValidVatIdIfPresent(RegisterRequest request, String emailAddress) {
        if ("DE".equals(request.getCountry()) && !stringIsEmpty(request.getVatId()) && isNotValidGermanVatId(request.getVatId())) {
            throw new RegisterRequestInvalidVatIdException(
                    String.format("registration: Customer %s vatId empty or invalid", emailAddress)
            );
        }
    }

    private static void requirePassword(RegisterRequest request, String emailAddress) {
        if (stringIsEmpty(request.getPassword())) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s password invalid", emailAddress)
            );
        }
    }

    private static void requireProducts(RegisterRequest request, String emailAddress) {
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new RegisterRequestInvalidCustomerDataException(
                    String.format("registration: Customer %s no products", emailAddress)
            );
        }
    }

    private static void requireAcknowledgement(RegisterRequest request, String emailAddress) {
        if (!request.isAgbAccepted()
                || !request.isEntrepreneur()
                || !request.isRequestImmediateServiceStart()
                || !request.isAcknowledgeWithdrawalLoss()) {

            String msg = String.format(
                    "Customer with email %s -> invalid acknowledgement information " +
                            "[agbAccepted][isEntrepreneur][requestImmediateServiceStart][acknowledgeWithdrawalLoss] " +
                            "[%s][%s][%s][%s]",
                    emailAddress,
                    request.isAgbAccepted(),
                    request.isEntrepreneur(),
                    request.isRequestImmediateServiceStart(),
                    request.isAcknowledgeWithdrawalLoss()
            );
            throw new CustomerAcknowledgementInvalidException(msg);
        }
    }
}

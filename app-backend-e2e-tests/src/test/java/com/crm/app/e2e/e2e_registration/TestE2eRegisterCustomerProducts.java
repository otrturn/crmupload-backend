package com.crm.app.e2e.e2e_registration;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterCustomerResult;
import com.crm.app.e2e.config.E2eProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eRegisterCustomerProducts extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_customerProducts() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest baseRequest = baseRegisterRequest();

        RegisterCustomerResult result;
        RegisterCustomerResult.Failure failure;

        /*
         * Product NULL
         */
        RegisterRequest invalidDataRequest = new RegisterRequest(
                baseRequest.getFirstname(),
                baseRequest.getLastname(),
                baseRequest.getCompanyName(),
                baseRequest.getEmailAddress(),
                baseRequest.getPhoneNumber(),
                baseRequest.getAdrline1(),
                baseRequest.getAdrline2(),
                baseRequest.getPostalcode(),
                baseRequest.getCity(),
                baseRequest.getCountry(),
                baseRequest.getTaxId(),
                baseRequest.getVatId(),
                baseRequest.getPassword(),
                null,
                baseRequest.isAgbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.isRequestImmediateServiceStart(),
                baseRequest.isAcknowledgeWithdrawalLoss(),
                baseRequest.getTermsVersion()
        );
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Product Empty
         */
        invalidDataRequest = new RegisterRequest(
                baseRequest.getFirstname(),
                baseRequest.getLastname(),
                baseRequest.getCompanyName(),
                baseRequest.getEmailAddress(),
                baseRequest.getPhoneNumber(),
                baseRequest.getAdrline1(),
                baseRequest.getAdrline2(),
                baseRequest.getPostalcode(),
                baseRequest.getCity(),
                baseRequest.getCountry(),
                baseRequest.getTaxId(),
                baseRequest.getVatId(),
                baseRequest.getPassword(),
                new ArrayList<>(),
                baseRequest.isAgbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.isRequestImmediateServiceStart(),
                baseRequest.isAcknowledgeWithdrawalLoss(),
                baseRequest.getTermsVersion()
        );
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Product unknown
         */
        invalidDataRequest = new RegisterRequest(
                baseRequest.getFirstname(),
                baseRequest.getLastname(),
                baseRequest.getCompanyName(),
                baseRequest.getEmailAddress(),
                baseRequest.getPhoneNumber(),
                baseRequest.getAdrline1(),
                baseRequest.getAdrline2(),
                baseRequest.getPostalcode(),
                baseRequest.getCity(),
                baseRequest.getCountry(),
                baseRequest.getTaxId(),
                baseRequest.getVatId(),
                baseRequest.getPassword(),
                List.of("helgoland"),
                baseRequest.isAgbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.isRequestImmediateServiceStart(),
                baseRequest.isAcknowledgeWithdrawalLoss(),
                baseRequest.getTermsVersion()
        );
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(409);
        assertThat(failure.error().code()).isEqualTo("CUSTOMER_PRODUCT_INVALID");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Product multiple identical entries
         */
        invalidDataRequest = new RegisterRequest(
                baseRequest.getFirstname(),
                baseRequest.getLastname(),
                baseRequest.getCompanyName(),
                baseRequest.getEmailAddress(),
                baseRequest.getPhoneNumber(),
                baseRequest.getAdrline1(),
                baseRequest.getAdrline2(),
                baseRequest.getPostalcode(),
                baseRequest.getCity(),
                baseRequest.getCountry(),
                baseRequest.getTaxId(),
                baseRequest.getVatId(),
                baseRequest.getPassword(),
                List.of(AppConstants.PRODUCT_CRM_UPLOAD, AppConstants.PRODUCT_CRM_UPLOAD),
                baseRequest.isAgbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.isRequestImmediateServiceStart(),
                baseRequest.isAcknowledgeWithdrawalLoss(),
                baseRequest.getTermsVersion()
        );
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(409);
        assertThat(failure.error().code()).isEqualTo("CUSTOMER_PRODUCT_INVALID");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
    }
}

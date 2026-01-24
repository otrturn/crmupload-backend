package com.crm.app.e2e.e2e_registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterCustomerResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
@Slf4j
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eRegisterCustomerTermsVersion extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_customerTermsVersion() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest baseRequest = baseRegisterRequest();

        RegisterCustomerResult result;
        RegisterCustomerResult.Success success;
        RegisterCustomerResult.Failure failure;

        RegisterRequest termsVersionValid = new RegisterRequest(
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
                baseRequest.getProducts(),
                true,
                true,
                true,
                true,
                "21.12.2025"
        );

        /*
         * Terms version valid
         */
        result = client.register(termsVersionValid);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Success.class);

        success = (RegisterCustomerResult.Success) result;
        assertThat(success.response().token()).isNotBlank();

        /*
         * Terms version invalid
         */
        baseRequest = baseRegisterRequest();
        RegisterRequest termsVersionInvalid = new RegisterRequest(
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
                baseRequest.getProducts(),
                true,
                true,
                true,
                true,
                "22.12.2025"
        );

        result = client.register(termsVersionInvalid);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(409);
        assertThat(failure.error().code()).isEqualTo("CUSTOMER_TERMS_VERSION_INVALID");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
    }
}

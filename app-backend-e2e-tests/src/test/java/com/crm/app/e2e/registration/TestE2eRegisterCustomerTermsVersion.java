package com.crm.app.e2e.registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
@Slf4j
class TestE2eRegisterCustomerTermsVersion extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_customerTermsVersion() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest baseRequest = baseRegisterRequest();

        RegisterResult result;
        RegisterResult.Success success;
        RegisterResult.Failure failure;

        RegisterRequest termsVersionValid = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
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

        assertThat(result).isInstanceOf(RegisterResult.Success.class);

        success = (RegisterResult.Success) result;
        assertThat(success.response().token()).isNotBlank();

        /*
         * Terms version invalid
         */
        baseRequest = baseRegisterRequest();
        RegisterRequest termsVersionInvalid = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                true,
                true,
                true,
                true,
                "22.12.2025"
        );

        result = client.register(termsVersionInvalid);

        assertThat(result).isInstanceOf(RegisterResult.Failure.class);

        failure = (RegisterResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(409);
        assertThat(failure.error().code()).isEqualTo("CUSTOMER_TERMS_VERSION_INVALID");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
    }
}
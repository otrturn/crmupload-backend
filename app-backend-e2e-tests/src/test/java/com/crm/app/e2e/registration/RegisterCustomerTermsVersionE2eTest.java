package com.crm.app.e2e.registration;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.AbstractE2eTest;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
@Slf4j
class RegisterCustomerTermsVersionE2eTest extends AbstractE2eTest {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_success() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest baseRequest = new RegisterRequest(
                "Jürgen", "Becker", null,
                "ralf+" + System.currentTimeMillis() + "@test.de",
                "01702934959",
                "Teichgarten 17", null,
                "60333", "Frankfurt", "DE",
                "test123",
                List.of(
                        AppConstants.PRODUCT_CRM_UPLOAD,
                        AppConstants.PRODUCT_DUPLICATE_CHECK
                ),
                true, true, true, true,
                "21.12.2025"
        );

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
        RegisterResult result = client.register(termsVersionValid);

        assertThat(result).isInstanceOf(RegisterResult.Success.class);

        RegisterResult.Success success = (RegisterResult.Success) result;
        assertThat(success.response().token()).isNotBlank();

        /*
         * Terms version invalid
         */
        RegisterRequest termsVersionInvalid = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                "ralf+" + System.currentTimeMillis() + "@test.de",
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

        RegisterResult.Failure failure = (RegisterResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(409);
        assertThat(failure.error().code()).isEqualTo("CUSTOMER_TERMS_VERSION_INVALID");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
    }
}
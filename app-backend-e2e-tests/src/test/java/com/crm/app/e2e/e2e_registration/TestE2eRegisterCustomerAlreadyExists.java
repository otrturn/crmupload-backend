package com.crm.app.e2e.e2e_registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterCustomerResult;
import com.crm.app.e2e.config.E2eProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eRegisterCustomerAlreadyExists extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_customerAlreadyExists() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest request = baseRegisterRequest();

        RegisterCustomerResult result;
        RegisterCustomerResult.Success success;
        RegisterCustomerResult.Failure failure;

        /*
         * Register once
         */
        result = client.register(request);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Success.class);

        success = (RegisterCustomerResult.Success) result;
        assertThat(success.response().token()).isNotBlank();

        /*
         * Register twice
         */

        result = client.register(request);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(409);
        assertThat(failure.error().code()).isEqualTo("CUSTOMER_ALREADY_EXISTS");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
    }
}


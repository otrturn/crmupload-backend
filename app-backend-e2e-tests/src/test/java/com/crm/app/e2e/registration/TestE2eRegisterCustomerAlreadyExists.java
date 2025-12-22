package com.crm.app.e2e.registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterResult;
import com.crm.app.e2e.config.E2eProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
class TestE2eRegisterCustomerAlreadyExists extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_customerAlreadyExists() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest request = baseRegisterRequest();

        /*
        Register once
         */
        RegisterResult result = client.register(request);

        assertThat(result).isInstanceOf(RegisterResult.Success.class);

        RegisterResult.Success success = (RegisterResult.Success) result;
        assertThat(success.response().token()).isNotBlank();

        /*
        Register twice
         */

        result = client.register(request);

        assertThat(result).isInstanceOf(RegisterResult.Failure.class);

        RegisterResult.Failure failure = (RegisterResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(409);
        assertThat(failure.error().code()).isEqualTo("CUSTOMER_ALREADY_EXISTS");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
    }
}


package com.crm.app.e2e.e2e_registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterCustomerResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eRegisterCustomerSuccess extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_customerSuccess() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest request = baseRegisterRequest();

        RegisterCustomerResult result = client.register(request);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Success.class);

        RegisterCustomerResult.Success success = (RegisterCustomerResult.Success) result;
        assertThat(success.response().token()).isNotBlank();
    }
}
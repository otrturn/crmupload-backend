package com.crm.app.e2e.registration;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.AbstractE2eTest;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
class RegisterCustomerSuccessE2eTest extends AbstractE2eTest {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_success() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest request = new RegisterRequest(
                "Jürgen", "Becker", null,
                "ralf+" + System.currentTimeMillis() + "@test.de", // eindeutig!
                "01702934959",
                "Teichgarten 17", null,
                "60333", "Frankfurt", "DE",
                "test123",
                java.util.List.of(
                        AppConstants.PRODUCT_CRM_UPLOAD,
                        AppConstants.PRODUCT_DUPLICATE_CHECK
                ),
                true, true, true, true,
                "21.12.2025"
        );

        RegisterResult result = client.register(request);

        assertThat(result).isInstanceOf(RegisterResult.Success.class);

        RegisterResult.Success success = (RegisterResult.Success) result;
        assertThat(success.response().token()).isNotBlank();
    }
}
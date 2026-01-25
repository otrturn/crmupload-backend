package com.crm.app.e2e.e2e_registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterCustomerResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.database.CustomerHandling;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eRegisterCustomerTaxIdAndTaxVatId extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_taxId() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterCustomerResult result;
        RegisterCustomerResult.Failure failure;

        /*
         * Tax Id
         */
        RegisterRequest invalidDataRequest = new RegisterRequest(baseRegisterRequest());
        invalidDataRequest.setTaxId(null);
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_TAX_ID");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Tax Id
         */
        invalidDataRequest = new RegisterRequest(baseRegisterRequest());
        invalidDataRequest.setTaxId("");
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

        failure = (RegisterCustomerResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_TAX_ID");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Tax Id
         */
        invalidDataRequest = new RegisterRequest(baseRegisterRequest());
        invalidDataRequest.setTaxId("1.2.3.");
        result = client.register(invalidDataRequest);
        assertThat(result).isInstanceOf(RegisterCustomerResult.Success.class);
        Long verificationTaskId = CustomerHandling.getVerificationTaskId(
                dataSource,
                invalidDataRequest.getEmailAddress(),
                invalidDataRequest.getTaxId()
        );
        assertThat(verificationTaskId).isGreaterThan(0L);

        /*
         * Vat Id
         */
        for (String vatId : List.of("DE", "DE123", "DE1234567890")) {
            invalidDataRequest = new RegisterRequest(baseRegisterRequest());
            invalidDataRequest.setVatId(vatId);
            result = client.register(invalidDataRequest);

            assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

            failure = (RegisterCustomerResult.Failure) result;

            assertThat(failure.error().status()).isEqualTo(400);
            assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_VAT_ID");
            assertThat(failure.error().message()).isNotBlank();
            assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
        }
    }
}

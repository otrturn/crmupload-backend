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

        RegisterRequest baseRequest = baseRegisterRequest();

        RegisterCustomerResult result;
        RegisterCustomerResult.Failure failure;

        /*
         * Tax Id
         */
        RegisterRequest invalidDataRequest = new RegisterRequest(
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
                null,
                baseRequest.vat_id(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        );
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
        invalidDataRequest = new RegisterRequest(
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
                "",
                baseRequest.vat_id(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        );
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
        invalidDataRequest = new RegisterRequest(
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
                "123",
                baseRequest.vat_id(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        );
        result = client.register(invalidDataRequest);
        assertThat(result).isInstanceOf(RegisterCustomerResult.Success.class);

        /*
         * Vat Id
         */
        for (String vatId : List.of("DE", "DE123", "DE1234567890")) {
            invalidDataRequest = new RegisterRequest(
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
                    baseRequest.tax_id(),
                    vatId,
                    baseRequest.password(),
                    baseRequest.products(),
                    baseRequest.agb_accepted(),
                    baseRequest.is_entrepreneur(),
                    baseRequest.request_immediate_service_start(),
                    baseRequest.acknowledge_withdrawal_loss(),
                    baseRequest.terms_version()
            );
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

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

        RegisterRequest baseRequest = baseRegisterRequest();

        RegisterCustomerResult result;
        RegisterCustomerResult.Failure failure;

        /*
         * Tax Id
         */
        RegisterRequest invalidDataRequest = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                null,
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
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
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                "",
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
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
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                "1.2.3",
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
        );
        result = client.register(invalidDataRequest);
        assertThat(result).isInstanceOf(RegisterCustomerResult.Success.class);
        Long verificationTaskId = CustomerHandling.getVerificationTaskId(dataSource, invalidDataRequest.emailAddress(), invalidDataRequest.taxId());
        assertThat(verificationTaskId).isGreaterThan(0L);

        /*
         * Vat Id
         */
        for (String vatId : List.of("DE", "DE123", "DE1234567890")) {
            invalidDataRequest = new RegisterRequest(
                    baseRequest.firstname(),
                    baseRequest.lastname(),
                    baseRequest.companyName(),
                    baseRequest.emailAddress(),
                    baseRequest.phoneNumber(),
                    baseRequest.adrline1(),
                    baseRequest.adrline2(),
                    baseRequest.postalcode(),
                    baseRequest.city(),
                    baseRequest.country(),
                    baseRequest.taxId(),
                    vatId,
                    baseRequest.password(),
                    baseRequest.products(),
                    baseRequest.agbAccepted(),
                    baseRequest.isEntrepreneur(),
                    baseRequest.requestImmediateServiceStart(),
                    baseRequest.acknowledgeWithdrawalLoss(),
                    baseRequest.termsVersion()
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

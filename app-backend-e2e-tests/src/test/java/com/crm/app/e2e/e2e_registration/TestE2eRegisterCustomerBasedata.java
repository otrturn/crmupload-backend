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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eRegisterCustomerBasedata extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_customerBasedata() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest baseRequest = baseRegisterRequest();

        RegisterCustomerResult result;
        RegisterCustomerResult.Failure failure;

        /*
         * Email address
         */
        RegisterRequest invalidDataRequest = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                null,
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.taxId(),
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
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Names
         */
        invalidDataRequest = new RegisterRequest(
                null,
                null,
                null,
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.taxId(),
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
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Address
         */
        for (RegisterRequest request : invalidAddressesRequests()) {
            result = client.register(request);

            assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

            failure = (RegisterCustomerResult.Failure) result;

            assertThat(failure.error().status()).isEqualTo(400);
            assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
            assertThat(failure.error().message()).isNotBlank();
            assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
        }

        /*
         * Phone number
         */
        invalidDataRequest = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                null,
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.taxId(),
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
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
         * Password
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
                baseRequest.taxId(),
                baseRequest.vatId(),
                null,
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
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

    }

    private List<RegisterRequest> invalidAddressesRequests() {
        List<RegisterRequest> result = new ArrayList<>();
        RegisterRequest baseRequest = baseRegisterRequest();

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                null,
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.taxId(),
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                null,
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.taxId(),
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                null,
                baseRequest.country(),
                baseRequest.taxId(),
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                null,
                baseRequest.taxId(),
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.companyName(),
                baseRequest.emailAddress(),
                baseRequest.phoneNumber(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                "1234",
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.taxId(),
                baseRequest.vatId(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.requestImmediateServiceStart(),
                baseRequest.acknowledgeWithdrawalLoss(),
                baseRequest.termsVersion()
        ));
        return result;
    }

}

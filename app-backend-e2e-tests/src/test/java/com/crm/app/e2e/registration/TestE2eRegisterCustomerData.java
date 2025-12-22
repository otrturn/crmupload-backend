package com.crm.app.e2e.registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterResult;
import com.crm.app.e2e.config.E2eProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
class TestE2eRegisterCustomerData extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_customerData() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest baseRequest = baseRequest();

        /*
        Email address
         */
        RegisterRequest invalidDataRequest = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                null,
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        );
        RegisterResult result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterResult.Failure.class);

        RegisterResult.Failure failure = (RegisterResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
        Names
         */
        invalidDataRequest = new RegisterRequest(
                null,
                null,
                null,
                baseRequest.email_address(),
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        );

        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterResult.Failure.class);

        failure = (RegisterResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
        Address
         */
        for (RegisterRequest request : invalidAddressesRequests()) {
            result = client.register(request);

            assertThat(result).isInstanceOf(RegisterResult.Failure.class);

            failure = (RegisterResult.Failure) result;

            assertThat(failure.error().status()).isEqualTo(400);
            assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
            assertThat(failure.error().message()).isNotBlank();
            assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
        }

        /*
        Phone number
         */
        invalidDataRequest = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                null,
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        );
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterResult.Failure.class);

        failure = (RegisterResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

        /*
        Password
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
                null,
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        );
        result = client.register(invalidDataRequest);

        assertThat(result).isInstanceOf(RegisterResult.Failure.class);

        failure = (RegisterResult.Failure) result;

        assertThat(failure.error().status()).isEqualTo(400);
        assertThat(failure.error().code()).isEqualTo("REGISTER_INVALID_CUSTOMER_DATA");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/register-customer");

    }

    private List<RegisterRequest> invalidAddressesRequests() {
        List<RegisterRequest> result = new ArrayList<>();
        RegisterRequest baseRequest = baseRequest();

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                baseRequest.phone_number(),
                null,
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                null,
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                null,
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                baseRequest.postalcode(),
                baseRequest.city(),
                null,
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        ));

        result.add(new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                baseRequest.email_address(),
                baseRequest.phone_number(),
                baseRequest.adrline1(),
                baseRequest.adrline2(),
                "1234",
                baseRequest.city(),
                baseRequest.country(),
                baseRequest.password(),
                baseRequest.products(),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version()
        ));
        return result;
    }

}

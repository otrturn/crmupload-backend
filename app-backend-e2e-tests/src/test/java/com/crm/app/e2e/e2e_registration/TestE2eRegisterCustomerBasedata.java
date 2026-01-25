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

        RegisterCustomerResult result;
        RegisterCustomerResult.Failure failure;

        /*
         * Email address
         */
        RegisterRequest invalidDataRequest = new RegisterRequest(baseRegisterRequest());
        invalidDataRequest.setEmailAddress(null);

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
        invalidDataRequest = new RegisterRequest(baseRegisterRequest());
        invalidDataRequest.setFirstname(null);
        invalidDataRequest.setLastname(null);
        invalidDataRequest.setCompanyName(null);

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
        invalidDataRequest = new RegisterRequest(baseRegisterRequest());
        invalidDataRequest.setPhoneNumber(null);
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
        invalidDataRequest = new RegisterRequest(baseRegisterRequest());
        invalidDataRequest.setPassword(null);
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

        RegisterRequest a1 = new RegisterRequest(baseRegisterRequest());
        a1.setAdrline1(null);
        result.add(a1);

        RegisterRequest a2 = new RegisterRequest(baseRegisterRequest());
        a2.setPostalcode(null);
        result.add(a2);

        RegisterRequest a3 = new RegisterRequest(baseRegisterRequest());
        a3.setCity(null);
        result.add(a3);

        RegisterRequest a4 = new RegisterRequest(baseRegisterRequest());
        a4.setCountry(null);
        result.add(a4);

        RegisterRequest a5 = new RegisterRequest(baseRegisterRequest());
        a5.setPostalcode("1234");
        result.add(a5);

        return result;
    }
}

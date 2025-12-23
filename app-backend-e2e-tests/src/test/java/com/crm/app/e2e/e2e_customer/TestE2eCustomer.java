package com.crm.app.e2e.e2e_customer;

import com.crm.app.dto.CustomerProfile;
import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.*;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import com.crm.app.e2e.database.CustomerHandling;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
class TestE2eCustomer extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_customer() {

        RegisterCustomerClient registerClient = new RegisterCustomerClient(e2eProperties);
        LoginClient loginClient = new LoginClient(e2eProperties);
        ActivationClient activationClient = new ActivationClient(e2eProperties);
        CustomerStatusClient customerStatusClient = new CustomerStatusClient(e2eProperties);
        CustomerMeClient customerMeClient = new CustomerMeClient(e2eProperties);
        UpdateCustomerClient updateCustomerClient = new UpdateCustomerClient(e2eProperties);

        RegisterCustomerResult registerCustomerResult;
        LoginRequest loginRequest;
        LoginResult loginResult;
        LoginResult.Success loginSuccess;
        String token;
        CustomerStatusResult customerStatusResult;
        CustomerStatusResult.Success customerStatusSuccess;
        CustomerMeResult customerMeResult;
        CustomerMeResult.Success customerMeSuccess;
        UpdateCustomerResult updateCustomerResult;
        UpdateCustomerResult.Success updateCustomerSuccess;

        /*
        Register
         */
        RegisterRequest baseRequest = baseRegisterRequest();

        registerCustomerResult = registerClient.register(baseRequest);
        assertThat(registerCustomerResult).isInstanceOf(RegisterCustomerResult.Success.class);
        RegisterCustomerResult.Success registrationSuccess = (RegisterCustomerResult.Success) registerCustomerResult;
        assertThat(registrationSuccess.response().token()).isNotBlank();

        /*
        Activate customer
         */
        token = CustomerHandling.getActivationToken(dataSource, baseRequest.email_address());
        ActivationResult activationResult = activationClient.activate(token);
        assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);
        ActivationResult.Success activationSuccess = (ActivationResult.Success) activationResult;
        assertThat(activationSuccess.response()).isNotBlank();

        /*
        Login, customer enabled
         */
        loginRequest = new LoginRequest(baseRequest.email_address(), baseRequest.password());
        loginResult = loginClient.login(loginRequest);
        assertThat(loginResult).isInstanceOf(LoginResult.Success.class);
        loginSuccess = (LoginResult.Success) loginResult;
        assertThat(loginSuccess.response().token()).isNotBlank();
        assertTrue(loginSuccess.response().enabled());

        /*
        Get status
         */
        customerStatusResult = customerStatusClient.getStatus(baseRequest.email_address(), loginSuccess.response().token());
        assertThat(customerStatusResult).isInstanceOf(CustomerStatusResult.Success.class);
        customerStatusSuccess = (CustomerStatusResult.Success) customerStatusResult;
        assertTrue(customerStatusSuccess.response().enabled());

        /*
        Get Me, original
         */
        customerMeResult = customerMeClient.getMe(baseRequest.email_address(), loginSuccess.response().token());
        assertThat(customerMeResult).isInstanceOf(CustomerMeResult.Success.class);
        customerMeSuccess = (CustomerMeResult.Success) customerMeResult;
        assertEquals(customerMeSuccess.response().firstname(), baseRequest.firstname());

        /*
        Update
         */
        CustomerProfile customerProfile = new CustomerProfile(customerMeSuccess.response().customer_number(),
                "Hugo",
                "Walter",
                customerMeSuccess.response().company_name(),
                customerMeSuccess.response().email_address(),
                customerMeSuccess.response().phone_number(),
                customerMeSuccess.response().adrline1(),
                customerMeSuccess.response().adrline2(),
                customerMeSuccess.response().postalcode(),
                customerMeSuccess.response().city(),
                customerMeSuccess.response().country(),
                null);
        updateCustomerResult = updateCustomerClient.updateCustomer(baseRequest.email_address(), customerProfile, loginSuccess.response().token());
        assertThat(updateCustomerResult).isInstanceOf(UpdateCustomerResult.Success.class);

        /*
        Get Me, updated
         */
        customerMeResult = customerMeClient.getMe(baseRequest.email_address(), loginSuccess.response().token());
        assertThat(customerMeResult).isInstanceOf(CustomerMeResult.Success.class);
        customerMeSuccess = (CustomerMeResult.Success) customerMeResult;
        assertEquals("Hugo", customerMeSuccess.response().firstname());
        assertEquals("Walter",customerMeSuccess.response().lastname());
    }
}
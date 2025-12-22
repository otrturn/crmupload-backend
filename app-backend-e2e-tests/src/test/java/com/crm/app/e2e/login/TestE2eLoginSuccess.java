package com.crm.app.e2e.login;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
class TestE2eLoginSuccess extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_customerSuccess() {

        RegisterCustomerClient registerClient = new RegisterCustomerClient(e2eProperties);
        LoginClient loginClient = new LoginClient(e2eProperties);
        ActivationClient activationClient = new ActivationClient(e2eProperties);

        RegisterResult registerResult;
        LoginRequest loginRequest;
        LoginResult loginResult;
        LoginResult.Failure failure;
        LoginResult.Success loginSuccess;
        String token;

        /*
        Register
         */
        RegisterRequest baseRequest = baseRegisterRequest();
        RegisterRequest registerRequest = new RegisterRequest(
                baseRequest.firstname(),
                baseRequest.lastname(),
                baseRequest.company_name(),
                "ralf+00@test.de",
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

        registerResult = registerClient.register(registerRequest);
        assertThat(registerResult).isInstanceOf(RegisterResult.Success.class);
        RegisterResult.Success registrationSuccess = (RegisterResult.Success) registerResult;
        assertThat(registrationSuccess.response().token()).isNotBlank();

        /*
        Login, wrong password
         */
        loginRequest = new LoginRequest(registerRequest.email_address(), "test321");
        loginResult = loginClient.login(loginRequest);

        assertThat(loginResult).isInstanceOf(LoginResult.Failure.class);

        failure = (LoginResult.Failure) loginResult;

        assertThat(failure.error().status()).isEqualTo(401);
        assertThat(failure.error().code()).isEqualTo("AUTH_INVALID_CREDENTIALS");
        assertThat(failure.error().message()).isNotBlank();
        assertThat(failure.error().path()).isEqualTo("/auth/login");

        /*
        Login, customer not yet enabled
         */
        loginRequest = new LoginRequest(registerRequest.email_address(), registerRequest.password());
        loginResult = loginClient.login(loginRequest);
        assertThat(loginResult).isInstanceOf(LoginResult.Success.class);
        loginSuccess = (LoginResult.Success) loginResult;
        assertThat(loginSuccess.response().token()).isNotBlank();
        assertFalse(loginSuccess.response().enabled());

        /*
        Activate customer
         */
        token = CustomerHandling.getActivationToken(dataSource, registerRequest.email_address());
        ActivationResult activationResult = activationClient.activate(token);
        assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);
        ActivationResult.Success activationSuccess = (ActivationResult.Success) activationResult;
        assertThat(activationSuccess.response()).isNotBlank();

        /*
        Login, customer enabled
         */
        loginRequest = new LoginRequest(registerRequest.email_address(), registerRequest.password());
        loginResult = loginClient.login(loginRequest);
        assertThat(loginResult).isInstanceOf(LoginResult.Success.class);
        loginSuccess = (LoginResult.Success) loginResult;
        assertThat(loginSuccess.response().token()).isNotBlank();
        assertTrue(loginSuccess.response().enabled());
    }
}
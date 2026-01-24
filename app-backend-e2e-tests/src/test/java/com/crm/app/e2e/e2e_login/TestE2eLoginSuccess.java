package com.crm.app.e2e.e2e_login;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.*;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import com.crm.app.e2e.database.CustomerHandling;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eLoginSuccess extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_loginSuccess() {

        RegisterCustomerClient registerClient = new RegisterCustomerClient(e2eProperties);
        LoginClient loginClient = new LoginClient(e2eProperties);
        ActivationClient activationClient = new ActivationClient(e2eProperties);

        RegisterCustomerResult registerCustomerResult;
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
                baseRequest.getFirstname(),
                baseRequest.getLastname(),
                baseRequest.getCompanyName(),
                "ralf+00@test.de",
                baseRequest.getPhoneNumber(),
                baseRequest.getAdrline1(),
                baseRequest.getAdrline2(),
                baseRequest.getPostalcode(),
                baseRequest.getCity(),
                baseRequest.getCountry(),
                baseRequest.getTaxId(),
                baseRequest.getVatId(),
                baseRequest.getPassword(),
                baseRequest.getProducts(),
                baseRequest.isAgbAccepted(),
                baseRequest.isEntrepreneur(),
                baseRequest.isRequestImmediateServiceStart(),
                baseRequest.isAcknowledgeWithdrawalLoss(),
                baseRequest.getTermsVersion()
        );

        registerCustomerResult = registerClient.register(registerRequest);
        assertThat(registerCustomerResult).isInstanceOf(RegisterCustomerResult.Success.class);
        RegisterCustomerResult.Success registrationSuccess = (RegisterCustomerResult.Success) registerCustomerResult;
        assertThat(registrationSuccess.response().token()).isNotBlank();

        /*
        Login, wrong password
         */
        loginRequest = new LoginRequest(registerRequest.getEmailAddress(), "test321");
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
        loginRequest = new LoginRequest(registerRequest.getEmailAddress(), registerRequest.getPassword());
        loginResult = loginClient.login(loginRequest);
        assertThat(loginResult).isInstanceOf(LoginResult.Success.class);
        loginSuccess = (LoginResult.Success) loginResult;
        assertThat(loginSuccess.response().token()).isNotBlank();
        assertFalse(loginSuccess.response().enabled());

        /*
        Activate customer
         */
        token = CustomerHandling.getActivationToken(dataSource, registerRequest.getEmailAddress());
        ActivationResult activationResult = activationClient.activate(token);
        assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);
        ActivationResult.Success activationSuccess = (ActivationResult.Success) activationResult;
        assertThat(activationSuccess.response()).isNotBlank();

        /*
        Login, customer enabled
         */
        loginRequest = new LoginRequest(registerRequest.getEmailAddress(), registerRequest.getPassword());
        loginResult = loginClient.login(loginRequest);
        assertThat(loginResult).isInstanceOf(LoginResult.Success.class);
        loginSuccess = (LoginResult.Success) loginResult;
        assertThat(loginSuccess.response().token()).isNotBlank();
        assertTrue(loginSuccess.response().enabled());
    }
}

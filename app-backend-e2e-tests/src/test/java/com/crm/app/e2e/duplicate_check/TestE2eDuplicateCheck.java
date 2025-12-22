package com.crm.app.e2e.duplicate_check;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.*;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.database.CustomerHandling;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("e2e")
class TestE2eDuplicateCheck extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_duplicate_check() {
        RegisterRequest baseRequest = baseRegisterRequest();
        RegisterCustomerClient registerclient = new RegisterCustomerClient(e2eProperties);
        RegisterResult registerResult = registerclient.register(baseRequest);
        Assertions.assertThat(registerResult).isInstanceOf(RegisterResult.Success.class);

        LoginClient loginClient;
        LoginRequest loginRequest;
        LoginResult loginResult;
        LoginResult.Success loginSuccess;

        loginClient = new LoginClient(e2eProperties);
        loginRequest = new LoginRequest(baseRequest.email_address(), baseRequest.password());
        loginResult = loginClient.login(loginRequest);
        loginSuccess = (LoginResult.Success) loginResult;

        DuplicateCheckClient duplicateCheckClient = new DuplicateCheckClient(e2eProperties);
        String sourceSystem;
        Resource file;
        CrmUploadResult.Failure failure;
        CrmUploadResult uploadResult;

        /*
         * Permission denied
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_00001.xlsx");
        uploadResult = duplicateCheckClient.duplicateCheck(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                file
        );
        failure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(failure.error().status()).isEqualTo(409);
        Assertions.assertThat(failure.error().code()).isEqualTo("DUPLICATE_CHECK_PERMISSION_DENIED");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/duplicate-check");

        /*
         * Activate & login again
         */
        ActivationClient activationClient = new ActivationClient(e2eProperties);
        String token = CustomerHandling.getActivationToken(dataSource, baseRequest.email_address());
        ActivationResult activationResult = activationClient.activate(token);
        Assertions.assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);

        loginClient = new LoginClient(e2eProperties);
        loginRequest = new LoginRequest(baseRequest.email_address(), baseRequest.password());
        loginResult = loginClient.login(loginRequest);
        loginSuccess = (LoginResult.Success) loginResult;

        /*
         * Wrong source system
         */
        sourceSystem = "LEXWARE";
        file = new ClassPathResource("files/Lexware_Generated_00001.xlsx");
        uploadResult = duplicateCheckClient.duplicateCheck(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                file
        );
        failure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(failure.error().status()).isEqualTo(409);
        Assertions.assertThat(failure.error().code()).isEqualTo("DUPLICATE_CHECK_INVALID_DATA");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/duplicate-check");

        /*
         * Correct request
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_00001.xlsx");
        uploadResult = duplicateCheckClient.duplicateCheck(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                file
        );

        assertThat(uploadResult).isInstanceOf(CrmUploadResult.Success.class);

        /*
         * Already in progress
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_00001.xlsx");
        uploadResult = duplicateCheckClient.duplicateCheck(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                file
        );

        failure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(failure.error().status()).isEqualTo(409);
        Assertions.assertThat(failure.error().code()).isEqualTo("DUPLICATE_CHECK_ALREADY_IN_PROGRESS");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/duplicate-check");
    }

}

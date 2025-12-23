package com.crm.app.e2e.e2e_upload;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.*;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.database.CustomerHandling;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eCrmUpload extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_crmUpload() {
        RegisterRequest baseRequest = baseRegisterRequest();
        RegisterCustomerClient registerclient = new RegisterCustomerClient(e2eProperties);
        RegisterCustomerResult registerCustomerResult = registerclient.register(baseRequest);
        Assertions.assertThat(registerCustomerResult).isInstanceOf(RegisterCustomerResult.Success.class);

        LoginClient loginClient;
        LoginRequest loginRequest;
        LoginResult loginResult;
        LoginResult.Success loginSuccess;
        CustomerStatusResult customerStatusResult;
        CustomerStatusResult.Success customerStatusSuccess;

        loginClient = new LoginClient(e2eProperties);
        loginRequest = new LoginRequest(baseRequest.email_address(), baseRequest.password());
        loginResult = loginClient.login(loginRequest);
        loginSuccess = (LoginResult.Success) loginResult;

        CrmUploadClient uploadclient = new CrmUploadClient(e2eProperties);
        CustomerStatusClient customerStatusClient = new CustomerStatusClient(e2eProperties);

        String sourceSystem;
        Resource file;
        CrmUploadResult.Failure failure;
        CrmUploadResult uploadResult;

        /*
         * Permission denied
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                "EspoCRM",
                "http://host.docker.internal:8080",
                "CUST-123",
                "7a124718fbcde7a4a096396cb61fa80e",
                file
        );
        failure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(failure.error().status()).isEqualTo(409);
        Assertions.assertThat(failure.error().code()).isEqualTo("CRM_UPLOAD_PERMISSION_DENIED");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/crm-upload");

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
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                "EspoCRM",
                "http://host.docker.internal:8080",
                "CUST-123",
                "7a124718fbcde7a4a096396cb61fa80e",
                file
        );
        failure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(failure.error().status()).isEqualTo(409);
        Assertions.assertThat(failure.error().code()).isEqualTo("CRM_UPLOAD_INVALID_DATA");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/crm-upload");

        /*
         *  Wrong crm system
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                "ESPOCRM",
                "http://host.docker.internal:8080",
                "CUST-123",
                "7a124718fbcde7a4a096396cb61fa80e",
                file
        );
        failure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(failure.error().status()).isEqualTo(409);
        Assertions.assertThat(failure.error().code()).isEqualTo("CRM_UPLOAD_INVALID_DATA");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/crm-upload");

        /*
         * Correct request
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                "EspoCRM",
                "http://host.docker.internal:8080",
                "CUST-123",
                "7a124718fbcde7a4a096396cb61fa80e",
                file
        );

        assertThat(uploadResult).isInstanceOf(CrmUploadResult.Success.class);

        /*
         *   Get status
         */
        customerStatusResult = customerStatusClient.getStatus(baseRequest.email_address(), loginSuccess.response().token());
        Assertions.assertThat(customerStatusResult).isInstanceOf(CustomerStatusResult.Success.class);
        customerStatusSuccess = (CustomerStatusResult.Success) customerStatusResult;
        assertTrue(customerStatusSuccess.response().enabled());
        assertTrue(customerStatusSuccess.response().hasOpenCrmUploads());

        /*
         * Already in progress
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                "EspoCRM",
                "http://host.docker.internal:8080",
                "CUST-123",
                "7a124718fbcde7a4a096396cb61fa80e",
                file
        );

        failure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(failure.error().status()).isEqualTo(409);
        Assertions.assertThat(failure.error().code()).isEqualTo("CRM_UPLOAD_ALREADY_IN_PROGRESS");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/crm-upload");
    }

}

package com.crm.app.e2e.e2e_upload;

import com.crm.app.dto.CrmUploadHistory;
import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.*;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.database.CustomerHandling;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Comparator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-await")
class TestE2eCrmUploadAwaitAnswer extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_crmUploadAwaitAnswerSuccessful() {
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
        CrmUploadResult.Failure crmUploadFailure;
        CrmUploadHistoryResult crmUploadHistoryResult;
        CrmUploadHistoryResult.Success crmUploadHistorySuccess;

        /*
         * Login
         */
        loginClient = new LoginClient(e2eProperties);
        loginRequest = new LoginRequest(baseRequest.getEmailAddress(), baseRequest.getPassword());
        loginResult = loginClient.login(loginRequest);
        loginSuccess = (LoginResult.Success) loginResult;

        CrmUploadClient uploadclient = new CrmUploadClient(e2eProperties);
        CustomerStatusClient customerStatusClient = new CustomerStatusClient(e2eProperties);
        CrmUploadHistoryClient crmUploadHistoryClient = new CrmUploadHistoryClient(e2eProperties);

        String sourceSystem;
        Resource file;
        CrmUploadResult uploadResult;

        /*
         * Activate
         */
        ActivationClient activationClient = new ActivationClient(e2eProperties);
        String token = CustomerHandling.getActivationToken(dataSource, baseRequest.getEmailAddress());
        ActivationResult activationResult = activationClient.activate(token);
        Assertions.assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);

        /*
         * Request with correct file
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.getEmailAddress(),
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
         *  Get status
         */
        customerStatusResult = customerStatusClient.getStatus(baseRequest.getEmailAddress(), loginSuccess.response().token());
        Assertions.assertThat(customerStatusResult).isInstanceOf(CustomerStatusResult.Success.class);
        customerStatusSuccess = (CustomerStatusResult.Success) customerStatusResult;
        assertTrue(customerStatusSuccess.response().enabled());
        assertTrue(customerStatusSuccess.response().hasOpenCrmUploads());

        /*
         *  Wait for completion
         */
        Awaitility.await("Upload check finished")
                .atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    CustomerStatusResult r = customerStatusClient.getStatus(baseRequest.getEmailAddress(), loginSuccess.response().token());
                    assertThat(r).isInstanceOf(CustomerStatusResult.Success.class);

                    CustomerStatusResult.Success s = (CustomerStatusResult.Success) r;

                    assertThat(s.response().hasOpenCrmUploads()).isFalse();
                });

        /*
         *  History
         */
        crmUploadHistoryResult = crmUploadHistoryClient.getCrmUploadHistory(baseRequest.getEmailAddress(), loginSuccess.response().token());
        Assertions.assertThat(crmUploadHistoryResult).isInstanceOf(CrmUploadHistoryResult.Success.class);
        crmUploadHistorySuccess = (CrmUploadHistoryResult.Success) crmUploadHistoryResult;
        assertFalse(crmUploadHistorySuccess.response().crmUploadHistory().isEmpty());
        crmUploadHistorySuccess.response().crmUploadHistory().sort(Comparator.comparing(CrmUploadHistory::getTs).reversed());
        assertEquals("done", crmUploadHistorySuccess.response().crmUploadHistory().get(0).getStatus());

        /*
         * Request with invalid file
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_Invalid.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.getEmailAddress(),
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
         *  Get status
         */
        customerStatusResult = customerStatusClient.getStatus(baseRequest.getEmailAddress(), loginSuccess.response().token());
        Assertions.assertThat(customerStatusResult).isInstanceOf(CustomerStatusResult.Success.class);
        customerStatusSuccess = (CustomerStatusResult.Success) customerStatusResult;
        assertTrue(customerStatusSuccess.response().enabled());
        assertTrue(customerStatusSuccess.response().hasOpenCrmUploads());

        /*
         * Wait for completion
         */
        Awaitility.await("Upload check finished")
                .atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    CustomerStatusResult r = customerStatusClient.getStatus(baseRequest.getEmailAddress(), loginSuccess.response().token());
                    assertThat(r).isInstanceOf(CustomerStatusResult.Success.class);

                    CustomerStatusResult.Success s = (CustomerStatusResult.Success) r;

                    assertThat(s.response().hasOpenCrmUploads()).isFalse();
                });

        /*
         *  History
         */
        crmUploadHistoryResult = crmUploadHistoryClient.getCrmUploadHistory(baseRequest.getEmailAddress(), loginSuccess.response().token());
        Assertions.assertThat(crmUploadHistoryResult).isInstanceOf(CrmUploadHistoryResult.Success.class);
        crmUploadHistorySuccess = (CrmUploadHistoryResult.Success) crmUploadHistoryResult;
        assertFalse(crmUploadHistorySuccess.response().crmUploadHistory().isEmpty());
        crmUploadHistorySuccess.response().crmUploadHistory().sort(Comparator.comparing(CrmUploadHistory::getTs).reversed());
        assertEquals("failed", crmUploadHistorySuccess.response().crmUploadHistory().get(0).getStatus());

        /*
         * Correct request with different crm system
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                baseRequest.getEmailAddress(),
                loginSuccess.response().token(),
                sourceSystem,
                "Pipedrive",
                "http://host.docker.internal:8080",
                "CUST-123",
                "7a124718fbcde7a4a096396cb61fa80e",
                file
        );

        assertThat(uploadResult).isInstanceOf(CrmUploadResult.Failure.class);
        crmUploadFailure = (CrmUploadResult.Failure) uploadResult;
        Assertions.assertThat(crmUploadFailure.error().code()).isEqualTo("CRM_UPLOAD_FORBIDDEN_USE");
    }
}

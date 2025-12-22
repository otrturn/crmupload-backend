package com.crm.app.e2e.upload;

import com.crm.app.dto.AppConstants;
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

import java.util.List;

@ActiveProfiles("e2e")
class TestE2eCrmUploadMissingProduct extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_crmUpload() {
        RegisterRequest baseRequest = baseRegisterRequest();
        RegisterRequest invalidRequest = new RegisterRequest(
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
                baseRequest.password(),
                List.of(AppConstants.PRODUCT_DUPLICATE_CHECK),
                baseRequest.agb_accepted(),
                baseRequest.is_entrepreneur(),
                baseRequest.request_immediate_service_start(),
                baseRequest.acknowledge_withdrawal_loss(),
                baseRequest.terms_version());

        RegisterCustomerClient registerclient = new RegisterCustomerClient(e2eProperties);
        RegisterResult registerResult = registerclient.register(invalidRequest);
        Assertions.assertThat(registerResult).isInstanceOf(RegisterResult.Success.class);

        ActivationClient activationClient = new ActivationClient(e2eProperties);
        String token = CustomerHandling.getActivationToken(dataSource, invalidRequest.email_address());
        ActivationResult activationResult = activationClient.activate(token);
        Assertions.assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);

        LoginClient loginClient = new LoginClient(e2eProperties);
        LoginRequest loginRequest = new LoginRequest(invalidRequest.email_address(), invalidRequest.password());
        LoginResult loginResult = loginClient.login(loginRequest);
        LoginResult.Success loginSuccess = (LoginResult.Success) loginResult;

        CrmUploadClient uploadclient = new CrmUploadClient(e2eProperties);
        String sourceSystem;
        Resource file;
        CrmUploadResult.Failure failure;
        CrmUploadResult uploadResult;

        /*
         *  Missing product
         */
        sourceSystem = "Lexware";
        file = new ClassPathResource("files/Lexware_Generated_00001.xlsx");
        uploadResult = uploadclient.crmUpload(
                invalidRequest.email_address(),
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
        Assertions.assertThat(failure.error().code()).isEqualTo("CRM_UPLOAD_MISSING_PRODUCT");
        Assertions.assertThat(failure.error().message()).isNotBlank();
        Assertions.assertThat(failure.error().path()).isEqualTo("/api/crm-upload");
    }

}

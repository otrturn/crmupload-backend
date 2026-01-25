package com.crm.app.e2e.e2e_upload;

import com.crm.app.dto.AppConstants;
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

import java.util.List;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eCrmUploadMissingProduct extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_crmUploadMissingProduct() {
        RegisterRequest invalidRequest = new RegisterRequest(baseRegisterRequest());
        invalidRequest.setProducts(List.of(AppConstants.PRODUCT_DUPLICATE_CHECK));

        RegisterCustomerClient registerclient = new RegisterCustomerClient(e2eProperties);
        RegisterCustomerResult registerCustomerResult = registerclient.register(invalidRequest);
        Assertions.assertThat(registerCustomerResult).isInstanceOf(RegisterCustomerResult.Success.class);

        ActivationClient activationClient = new ActivationClient(e2eProperties);
        String token = CustomerHandling.getActivationToken(dataSource, invalidRequest.getEmailAddress());
        ActivationResult activationResult = activationClient.activate(token);
        Assertions.assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);

        LoginClient loginClient = new LoginClient(e2eProperties);
        LoginRequest loginRequest = new LoginRequest(invalidRequest.getEmailAddress(), invalidRequest.getPassword());
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
        file = new ClassPathResource("files/Lexware_Generated_Correct.xlsx");
        uploadResult = uploadclient.crmUpload(
                invalidRequest.getEmailAddress(),
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

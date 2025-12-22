package com.crm.app.e2e.upload;

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
class TestE2eRegisterCrmUpload extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_crmUpload() {
        RegisterRequest baseRequest = baseRegisterRequest();
        RegisterCustomerClient registerclient = new RegisterCustomerClient(e2eProperties);
        RegisterResult registerResult = registerclient.register(baseRequest);
        Assertions.assertThat(registerResult).isInstanceOf(RegisterResult.Success.class);

        ActivationClient activationClient = new ActivationClient(e2eProperties);
        String token = CustomerHandling.getActivationToken(dataSource, baseRequest.email_address());
        ActivationResult activationResult = activationClient.activate(token);
        Assertions.assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);

        LoginClient loginClient = new LoginClient(e2eProperties);
        LoginRequest loginRequest = new LoginRequest(baseRequest.email_address(), baseRequest.password());
        LoginResult loginResult = loginClient.login(loginRequest);
        LoginResult.Success loginSuccess = (LoginResult.Success) loginResult;

        String sourceSystem = "Lexware";
        CrmUploadClient uploadclient = new CrmUploadClient(e2eProperties);
        Resource file = new ClassPathResource("files/Lexware_Generated_00001.xlsx");
        CrmUploadResult uploadResult = uploadclient.upload(
                baseRequest.email_address(),
                loginSuccess.response().token(),
                sourceSystem,
                "EspoCRM",
                "http://host.docker.internal:8080",
                "CUST-123",
                "7a124718fbcde7a4a096396cb61fa80e",
                file
        );
        //CrmUploadResult.Failure failure = (CrmUploadResult.Failure) uploadResult;
        //System.out.println(failure.error().code());

        assertThat(uploadResult).isInstanceOf(CrmUploadResult.Success.class);
    }

}

package com.crm.app.e2e.e2e_duplicate_check;

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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("e2e")
@Tag("e2e-all")
@Tag("e2e-await")
class TestE2eDuplicateCheckAwaitAnswerSuccessful extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_conflict_duplicateCheckAwaitAnswerSuccessful() {
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
        DuplicateCheckHistoryResult duplicateCheckHistoryResult;
        DuplicateCheckHistoryResult.Success duplicateCheckHistorySuccess;

        /*
         * Login
         */
        loginClient = new LoginClient(e2eProperties);
        loginRequest = new LoginRequest(baseRequest.email_address(), baseRequest.password());
        loginResult = loginClient.login(loginRequest);
        loginSuccess = (LoginResult.Success) loginResult;

        DuplicateCheckClient duplicateCheckClient = new DuplicateCheckClient(e2eProperties);
        CustomerStatusClient customerStatusClient = new CustomerStatusClient(e2eProperties);
        DuplicateCheckHistoryClient duplicateCheckHistoryClient = new DuplicateCheckHistoryClient(e2eProperties);

        String sourceSystem;
        Resource file;
        CrmUploadResult uploadResult;

        /*
         * Activate
         */
        ActivationClient activationClient = new ActivationClient(e2eProperties);
        String token = CustomerHandling.getActivationToken(dataSource, baseRequest.email_address());
        ActivationResult activationResult = activationClient.activate(token);
        Assertions.assertThat(activationResult).isInstanceOf(ActivationResult.Success.class);

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
        Get status
         */
        customerStatusResult = customerStatusClient.getStatus(baseRequest.email_address(), loginSuccess.response().token());
        Assertions.assertThat(customerStatusResult).isInstanceOf(CustomerStatusResult.Success.class);
        customerStatusSuccess = (CustomerStatusResult.Success) customerStatusResult;
        assertTrue(customerStatusSuccess.response().enabled());
        assertTrue(customerStatusSuccess.response().hasOpenDuplicateChecks());

        /*
        Wait for completion
         */
        AtomicReference<CustomerStatusResult.Success> last = new AtomicReference<>();

        Awaitility.await("Duplicate check finished")
                .atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    CustomerStatusResult r = customerStatusClient.getStatus(baseRequest.email_address(), loginSuccess.response().token());
                    assertThat(r).isInstanceOf(CustomerStatusResult.Success.class);

                    CustomerStatusResult.Success s = (CustomerStatusResult.Success) r;
                    last.set(s);

                    assertThat(s.response().hasOpenDuplicateChecks()).isFalse();
                });

        assertThat(last.get().response().hasOpenDuplicateChecks()).isFalse();

        /*
        History
         */
        duplicateCheckHistoryResult = duplicateCheckHistoryClient.getDuplicateCheckHistory(baseRequest.email_address(), loginSuccess.response().token());
        Assertions.assertThat(duplicateCheckHistoryResult).isInstanceOf(DuplicateCheckHistoryResult.Success.class);
        duplicateCheckHistorySuccess = (DuplicateCheckHistoryResult.Success) duplicateCheckHistoryResult;
        assertFalse(duplicateCheckHistorySuccess.response().duplicateCheckHistory().isEmpty());
        assertEquals("done", duplicateCheckHistorySuccess.response().duplicateCheckHistory().get(0).getStatus());
    }

}

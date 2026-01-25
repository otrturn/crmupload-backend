package com.crm.app.e2e.e2e_registration;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterCustomerResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
@Slf4j
@Tag("e2e-all")
@Tag("e2e-fast")
class TestE2eRegisterCustomerAcknowledgement extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_customerAcknowledgement() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        List<RegisterRequest> requests = new ArrayList<>();

        for (int mask = 0; mask < 16; mask++) {
            boolean agbAccepted = (mask & 1) != 0;
            boolean isEntrepreneur = (mask & 2) != 0;
            boolean requestImmediateServiceStart = (mask & 4) != 0;
            boolean acknowledgeWithdrawalLoss = (mask & 8) != 0;

            RegisterRequest registerRequest = new RegisterRequest(baseRegisterRequest());
            registerRequest.setAgbAccepted(agbAccepted);
            registerRequest.setEntrepreneur(isEntrepreneur);
            registerRequest.setRequestImmediateServiceStart(requestImmediateServiceStart);
            registerRequest.setAcknowledgeWithdrawalLoss(acknowledgeWithdrawalLoss);
            requests.add(registerRequest);
        }

        List<RegisterRequest> invalidRequests =
                requests.stream()
                        // alles außer "alle true"
                        .filter(r -> !(
                                r.isAgbAccepted()
                                        && r.isEntrepreneur()
                                        && r.isRequestImmediateServiceStart()
                                        && r.isAcknowledgeWithdrawalLoss()
                        ))
                        .toList();

        for (RegisterRequest request : invalidRequests) {
            log.info(String.format(" %s %s %s %s",
                    request.isAgbAccepted(),
                    request.isEntrepreneur(),
                    request.isRequestImmediateServiceStart(),
                    request.isAcknowledgeWithdrawalLoss()
            ));

            RegisterCustomerResult result = client.register(request);

            assertThat(result).isInstanceOf(RegisterCustomerResult.Failure.class);

            RegisterCustomerResult.Failure failure = (RegisterCustomerResult.Failure) result;

            assertThat(failure.error().status()).isEqualTo(409);
            assertThat(failure.error().code()).isEqualTo("CUSTOMER_ACKNOWLEDGEMENT_INFORMATION_INVALID");
            assertThat(failure.error().message()).isNotBlank();
            assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
        }

        RegisterRequest allTrueRequest = new RegisterRequest(baseRegisterRequest());
        allTrueRequest.setAgbAccepted(true);
        allTrueRequest.setEntrepreneur(true);
        allTrueRequest.setRequestImmediateServiceStart(true);
        allTrueRequest.setAcknowledgeWithdrawalLoss(true);

        RegisterCustomerResult result = client.register(allTrueRequest);

        assertThat(result).isInstanceOf(RegisterCustomerResult.Success.class);

        RegisterCustomerResult.Success success = (RegisterCustomerResult.Success) result;
        assertThat(success.response().token()).isNotBlank();
    }
}

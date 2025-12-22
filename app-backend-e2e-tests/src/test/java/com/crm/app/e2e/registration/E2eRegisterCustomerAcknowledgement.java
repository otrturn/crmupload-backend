package com.crm.app.e2e.registration;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.RegisterCustomerClient;
import com.crm.app.e2e.client.RegisterResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import lombok.extern.slf4j.Slf4j;
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
class E2eRegisterCustomerAcknowledgement extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_success() {

        RegisterCustomerClient client = new RegisterCustomerClient(e2eProperties);

        RegisterRequest baseRequest = new RegisterRequest(
                "Jürgen", "Becker", null,
                "ralf+" + System.currentTimeMillis() + "@test.de",
                "01702934959",
                "Teichgarten 17", null,
                "60333", "Frankfurt", "DE",
                "test123",
                java.util.List.of(
                        AppConstants.PRODUCT_CRM_UPLOAD,
                        AppConstants.PRODUCT_DUPLICATE_CHECK
                ),
                true, true, true, true,
                "21.12.2025"
        );

        List<RegisterRequest> requests = new ArrayList<>();

        for (int mask = 0; mask < 16; mask++) {
            boolean agbAccepted = (mask & 1) != 0;
            boolean isEntrepreneur = (mask & 2) != 0;
            boolean requestImmediateServiceStart = (mask & 4) != 0;
            boolean acknowledgeWithdrawalLoss = (mask & 8) != 0;

            requests.add(new RegisterRequest(
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
                    baseRequest.products(),
                    agbAccepted,
                    isEntrepreneur,
                    requestImmediateServiceStart,
                    acknowledgeWithdrawalLoss,
                    baseRequest.terms_version()
            ));
        }

        List<RegisterRequest> invalidRequests =
                requests.stream()
                        // alles außer "alle true"
                        .filter(r -> !(
                                r.agb_accepted()
                                        && r.is_entrepreneur()
                                        && r.request_immediate_service_start()
                                        && r.acknowledge_withdrawal_loss()
                        ))
                        .toList();

        for (RegisterRequest request : invalidRequests) {
            log.info(String.format(" %s %s %s %s", request.agb_accepted(), request.is_entrepreneur(), request.request_immediate_service_start(), request.acknowledge_withdrawal_loss()));

            RegisterResult result = client.register(request);

            assertThat(result).isInstanceOf(RegisterResult.Failure.class);

            RegisterResult.Failure failure = (RegisterResult.Failure) result;

            assertThat(failure.error().status()).isEqualTo(409);
            assertThat(failure.error().code()).isEqualTo("CUSTOMER_ACKNOWLEDGEMENT_INFORMATION_INVALID");
            assertThat(failure.error().message()).isNotBlank();
            assertThat(failure.error().path()).isEqualTo("/auth/register-customer");
        }

        RegisterRequest allTrueRequest = new RegisterRequest(
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
                baseRequest.products(),
                true,
                true,
                true,
                true,
                baseRequest.terms_version()
        );

        RegisterResult result = client.register(allTrueRequest);

        assertThat(result).isInstanceOf(RegisterResult.Success.class);

        RegisterResult.Success success = (RegisterResult.Success) result;
        assertThat(success.response().token()).isNotBlank();
    }
}
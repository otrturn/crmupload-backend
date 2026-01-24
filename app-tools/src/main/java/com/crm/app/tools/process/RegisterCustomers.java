package com.crm.app.tools.process;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.port.customer.CustomerActivationRepositoryPort;
import com.crm.app.tools.config.AppToolsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterCustomers {

    private final AppToolsConfig appToolsConfig;
    private final CustomerActivationRepositoryPort activationRepository;

    public void process(int n, RegisterRequest requestTemplate) {
        log.info(String.format("registerCustomers:baseUrl=%s", appToolsConfig.getBaseUrl()));

        WebClient client = WebClient.builder()
                .baseUrl(appToolsConfig.getBaseUrl())
                .build();

        for (int i = 0; i < n; i++) {
            RegisterRequest req = generateRequestForIndex(requestTemplate, i);

            try {
                RegisterResponse registerResponse = client.post()
                        .uri("/auth/register-customer")
                        .bodyValue(req)
                        .retrieve()
                        .bodyToMono(RegisterResponse.class)
                        .block(); // synchron

                if (registerResponse == null || registerResponse.token() == null || registerResponse.token().isBlank()) {
                    throw new IllegalStateException("Register did not return an activation token");
                }

                Optional<UUID> activationToken = activationRepository.getTokenByEmail(req.emailAddress());

                if (activationToken.isEmpty()) {
                    throw new IllegalStateException("no activation token for " + req.emailAddress());
                }

                String activationText = client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/auth/activate")
                                .queryParam("token", activationToken.get())
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("Activate: {}", activationText);

            } catch (Exception ex) {
                log.error(String.format("Error in request %d: %s", i, ex.getMessage()), ex);
            }
        }
    }

    public record RegisterResponse(String token) {
    }

    private RegisterRequest generateRequestForIndex(RegisterRequest base, int index) {
        String email = base.emailAddress().replace("@", "+" + index + "@");

        return new RegisterRequest(
                base.firstname(),
                base.lastname(),
                base.companyName(),
                email,
                base.phoneNumber(),
                base.adrline1(),
                base.adrline2(),
                base.postalcode(),
                base.city(),
                base.country(),
                base.taxId(),
                base.vatId(),
                base.password(),
                List.of(AppConstants.PRODUCT_CRM_UPLOAD, AppConstants.PRODUCT_DUPLICATE_CHECK),
                true, true, true, true, "21.12.2025"
        );
    }
}

package com.crm.app.tools.process;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.tools.config.AppToolsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterConsumers {
    private final AppToolsConfig appToolsConfig;

    public void process(int n, RegisterRequest requestTemplate) {
        log.info("registerConsumers:baseUrl=" + appToolsConfig.getBaseUrl());

        WebClient client = WebClient.builder()
                .baseUrl(appToolsConfig.getBaseUrl())
                .build();

        for (int i = 0; i < n; i++) {
            RegisterRequest req = generateRequestForIndex(requestTemplate, i);

            try {
                String response = client.post()
                        .uri("/auth/register-consumer")
                        .bodyValue(req)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(); // synchron

                log.info("Response " + i + ": " + response);

            } catch (Exception ex) {
                System.err.println("Error in request " + i + ": " + ex.getMessage());
                log.error("Error in request {} {}", i, ex.getMessage(), ex);
            }
        }
    }

    private RegisterRequest generateRequestForIndex(RegisterRequest base, int index) {
        String email = base.email_address().replace("@", "+" + index + "@");

        return new RegisterRequest(
                base.firstname(),
                base.lastname(),
                email,
                base.phone_number(),
                base.adrline1(),
                base.adrline2(),
                base.postalcode(),
                base.city(),
                base.country(),
                base.password()
        );
    }

}

package com.crm.app.tools.process;

import com.crm.app.dto.AppConstants;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.tools.config.AppToolsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterCustomers {

    private final AppToolsConfig appToolsConfig;

    public void process(int n, RegisterRequest requestTemplate) {
        log.info(String.format("registerCustomers:baseUrl=%s", appToolsConfig.getBaseUrl()));

        WebClient client = WebClient.builder()
                .baseUrl(appToolsConfig.getBaseUrl())
                .build();

        for (int i = 0; i < n; i++) {
            RegisterRequest req = generateRequestForIndex(requestTemplate, i);

            try {
                String response = client.post()
                        .uri("/auth/register-customer")
                        .bodyValue(req)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(); // synchron

                log.info(String.format("Response %d: %s", i, response));

            } catch (Exception ex) {
                log.error(String.format("Error in request %d: %s", i, ex.getMessage()), ex);
            }
        }
    }

    private RegisterRequest generateRequestForIndex(RegisterRequest base, int index) {
        String email = base.email_address().replace("@", "+" + index + "@");

        return new RegisterRequest(
                base.firstname(),
                base.lastname(),
                base.company_name(),
                email,
                base.phone_number(),
                base.adrline1(),
                base.adrline2(),
                base.postalcode(),
                base.city(),
                base.country(),
                base.password(),
                List.of(AppConstants.PRODUCT_CRM_UPLOAD, AppConstants.PRODUCT_DUPLICATE_CHECK),
                true, true, true, "21.12.2025"
        );
    }
}

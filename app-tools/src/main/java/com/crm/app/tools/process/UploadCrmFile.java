package com.crm.app.tools.process;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import com.crm.app.tools.config.AppToolsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCrmFile {
    private final AppToolsConfig appToolsConfig;

    public void process(Path filePath, int n, String sourceSystem, String crmSystem) {
        String url = appToolsConfig.getBaseUrl() + "/api/crm-upload";

        log.info("uploadCustomerFile:baseUrl=" + appToolsConfig.getBaseUrl());
        log.info("uploadCustomerFile:url=" + url);

        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < n; i++) {
            try {
                String email = "ralf@test.de".replace("@", "+" + i + "@");
                LoginRequest loginRequest = new LoginRequest(email, "test123");
                LoginResponse loginResponse = login(appToolsConfig.getBaseUrl(), loginRequest);

                // Multipart-Datei
                FileSystemResource fileResource = new FileSystemResource(filePath);

                // Multipart-Formulardaten
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("emailAddress", email);
                body.add("sourceSystem", sourceSystem);
                body.add("crmSystem", crmSystem);
                body.add("crmUrl", "https://crmupload.de:8180/");
                body.add("crmCustomerId", "CUST-" + i);
                body.add("crmApiKey", "API-KEY-TEST-" + i);
                body.add("file", fileResource);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.set("Authorization", "Bearer " + loginResponse.token());

                HttpEntity<MultiValueMap<String, Object>> requestEntity =
                        new HttpEntity<>(body, headers);

                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                log.info("Upload " + i + " OK");

            } catch (Exception ex) {
                log.error("Upload " + i + " FAILED → " + ex.getMessage());
            }
        }
    }

    public LoginResponse login(String baseUrl, LoginRequest request) {

        String url = baseUrl + "/auth/login";

        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<LoginResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            LoginResponse.class
                    );

            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            // 4xx / 5xx
            log.error("Login failed [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Login failed: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error during login", ex);
            throw new IllegalStateException("Unexpected login error: " + ex.getMessage(), ex);
        }
    }
}

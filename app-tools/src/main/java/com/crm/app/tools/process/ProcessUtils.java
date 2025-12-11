package com.crm.app.tools.process;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class ProcessUtils {

    private ProcessUtils() {
    }

    public static LoginResponse login(String baseUrl, LoginRequest request) {

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

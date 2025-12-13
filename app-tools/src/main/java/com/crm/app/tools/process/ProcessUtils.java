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
            log.error(String.format("Login failed [%s]: %s", ex.getStatusCode(), ex.getResponseBodyAsString()), ex);
            throw new IllegalStateException(String.format("Login failed: %s", ex.getMessage()), ex);
        } catch (Exception ex) {
            log.error(String.format("Unexpected error during login: %s", ex.getMessage()), ex);
            throw new IllegalStateException(String.format("Unexpected login error: %s", ex.getMessage()), ex);
        }
    }
}

package com.crm.app.tools.process;

import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import com.crm.app.tools.config.AppToolsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;

import static com.crm.app.tools.process.ProcessUtils.login;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCrmFile {
    private final AppToolsConfig appToolsConfig;

    public void process(Path filePath, int n, String sourceSystem, String crmSystem) {
        String url = appToolsConfig.getBaseUrl() + "/api/crm-upload";

        log.info("uploadCustomerFile:baseUrl=" + appToolsConfig.getBaseUrl());
        log.info("uploadCustomerFile:url=" + url);
        log.info("uploadCustomerFile:file=" + filePath.toString());

        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < n; i++) {
            try {
                String email = "ralf@test.de".replace("@", "+" + i + "@");
                LoginRequest loginRequest = new LoginRequest(email, "test1234");
                LoginResponse loginResponse = login(appToolsConfig.getBaseUrl(), loginRequest);

                // Multipart-Datei
                FileSystemResource fileResource = new FileSystemResource(filePath);

                // Multipart-Formulardaten
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("emailAddress", email);
                body.add("sourceSystem", sourceSystem);
                body.add("crmSystem", crmSystem);
                body.add("crmUrl", "EspoCRM".equalsIgnoreCase(crmSystem) ? "http://host.docker.internal:8080" : null);
                body.add("crmCustomerId", "Pipedrive".equalsIgnoreCase(crmSystem) ? "CUST-" + i : null);
                body.add("crmApiKey", "7a124718fbcde7a4a096396cb61fa80e");
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

}

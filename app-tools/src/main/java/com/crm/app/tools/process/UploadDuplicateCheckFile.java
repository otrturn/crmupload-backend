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
public class UploadDuplicateCheckFile {
    private final AppToolsConfig appToolsConfig;

    public void process(Path filePath, int n, String sourceSystem) {
        String url = appToolsConfig.getBaseUrl() + "/api/duplicate-check";

        log.info("duplicateCheckFile:baseUrl=" + appToolsConfig.getBaseUrl());
        log.info("duplicateCheckFile:url=" + url);
        log.info("duplicateCheckFile:file=" + filePath.toString());

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

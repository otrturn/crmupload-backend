package com.crm.app.tools.process;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;

public class UploadConsumerFile {
    private UploadConsumerFile() {
    }

    public static void uploadConsumerFile(Path filePath, int n, String sourceSystem, String crmSystem) {
        String url = "http://localhost:8086/api/consumer-upload";

        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < n; i++) {
            try {
                // Multipart-Datei
                FileSystemResource fileResource = new FileSystemResource(filePath);

                // Multipart-Formulardaten
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                String email = "ralf@test.de".replace("@", "+" + i + "@");
                body.add("emailAddress", email);
                body.add("sourceSystem", sourceSystem);
                body.add("crmSystem", crmSystem);
                body.add("crmCustomerId", "CUST-" + i);
                body.add("crmApiKey", "API-KEY-TEST-" + i);
                body.add("file", fileResource);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                HttpEntity<MultiValueMap<String, Object>> requestEntity =
                        new HttpEntity<>(body, headers);

                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                System.out.println("Upload " + i + " OK");

            } catch (Exception ex) {
                System.err.println("Upload " + i + " FAILED → " + ex.getMessage());
            }
        }
    }
}

package com.crm.app.tools;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("squid:S6437")
public class AppBulkUpload {
    public static void main(String[] args) {
        uploadConsumerFile(Paths.get("/home/ralf/espocrm-demo/Bexio_Generated.xlsx"), 10);
    }

    public static void uploadConsumerFile(Path filePath, int n) {
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
                body.add("sourceSystem", "Bexio");
                body.add("crmSystem", "EspoCRM");
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

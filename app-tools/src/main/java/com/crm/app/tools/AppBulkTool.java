package com.crm.app.tools;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.tools.config.AppToolsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SuppressWarnings("squid:S6437")
@SpringBootApplication(scanBasePackages = "com.crm.app.tools")
@RequiredArgsConstructor
public class AppBulkTool implements CommandLineRunner, ExitCodeGenerator {
    private final AppToolsConfig appToolsConfig;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppBulkTool.class);
        int code = SpringApplication.exit(app.run(args));
        System.exit(code);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length != 1) {
            log.error("args.length is {}", args.length);
            return;
        }
        log.info("Starte Batch-Prozess…");
        switch (args[0]) {
            case "--registerConsumer" -> {
                RegisterRequest request = new RegisterRequest("Ralf", "Scholler", "ralf@test.de", "01702934959",
                        "Am Dorfplatz 6", null, "57610", "Ingelbach", "Deutschland", "test123");
                registerConsumers(10, request);
            }
            case "--BexioToEspo" ->
                    uploadConsumerFile(Paths.get("/home/ralf/espocrm-demo/Bexio_Generated.xlsx"), 10, "Bexio", "EspoCRM");
            case "--MyExcelToEspo" ->
                    uploadConsumerFile(Paths.get("/home/ralf/espocrm-demo/MyExcelKunden_V001.xlsx"), 10, "MyExcel", "EspoCRM");
            default -> log.error("Unknown command {}", args[0]);
        }
        log.info("Batch-Prozess beendet.");
    }

    @Override
    public int getExitCode() {
        return 0;
    }

    public void registerConsumers(int n, RegisterRequest requestTemplate) {
        log.info("registerConsumers:baseUrl=" + appToolsConfig.getBaseUrl());

        WebClient client = WebClient.builder()
                .baseUrl(appToolsConfig.getBaseUrl())
                .build();

        for (int i = 0; i < n; i++) {
            RegisterRequest req = generateRequestForIndex(requestTemplate, i);

            try {
                String response = client.post()
                        .uri("/auth/registerConsumer")
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

    public void uploadConsumerFile(Path filePath, int n, String sourceSystem, String crmSystem) {
        log.info("uploadConsumerFile:baseUrl=" + appToolsConfig.getBaseUrl());

        String url = appToolsConfig.getBaseUrl() + "/api/consumer-upload";

        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < n; i++) {
            try {
                // Multipart-Datei
                FileSystemResource fileResource = new FileSystemResource(filePath);

                // Multipart-Formulardaten
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                String email = "ralf@test.de" .replace("@", "+" + i + "@");
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

package com.crm.app.tools.BulkRegister;

import com.crm.app.dto.RegisterRequest;
import com.crm.app.tools.config.AppToolsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@SuppressWarnings("squid:S6437")
@SpringBootApplication(scanBasePackages = "com.crm.app.tools")
@RequiredArgsConstructor
public class AppBulkRegister implements CommandLineRunner, ExitCodeGenerator {
    private final AppToolsConfig appToolsConfig;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppBulkRegister.class);
        int code = SpringApplication.exit(app.run(args));
        System.exit(code);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starte Batch-Prozess…");
        RegisterRequest request = new RegisterRequest("Ralf", "Scholler", "ralf@test.de", "01702934959",
                "Am Dorfplatz 6", null, "57610", "Ingelbach", "Deutschland", "test123");
        registerConsumers(10, request);
        log.info("Batch-Prozess beendet.");
    }

    public void registerConsumers(int n, RegisterRequest requestTemplate) {

        log.info("baseUrl=" + appToolsConfig.getBaseUrl());

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

    private static RegisterRequest generateRequestForIndex(RegisterRequest base, int index) {
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

    @Override
    public int getExitCode() {
        return 0;
    }
}

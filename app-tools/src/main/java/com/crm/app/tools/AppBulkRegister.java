package com.crm.app.tools;

import com.crm.app.dto.RegisterRequest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import static com.crm.app.tools.util.Constants.BASE_URL;

@SuppressWarnings("squid:S6437")
@SpringBootApplication(scanBasePackages = "com.crm.app.tools")
public class AppBulkRegister {
    public static void main(String[] args) {
        RegisterRequest request = new RegisterRequest("Ralf", "Scholler", "ralf@test.de", "01702934959",
                "Am Dorfplatz 6", null, "57610", "Ingelbach", "Deutschland", "test123");
        registerConsumers(10, request);
    }

    public static void registerConsumers(int n, RegisterRequest requestTemplate) {

        System.out.println("baseUrl=" + BASE_URL);

        WebClient client = WebClient.builder()
                .baseUrl(BASE_URL)
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

                System.out.println("Response " + i + ": " + response);

            } catch (Exception ex) {
                System.err.println("Error in request " + i + ": " + ex.getMessage());
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

}

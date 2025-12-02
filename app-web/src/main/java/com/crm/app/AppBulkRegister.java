package com.crm.app;

import com.crm.app.web.register.RegisterRequest;
import org.springframework.web.reactive.function.client.WebClient;

@SuppressWarnings("squid:S6437")
public class AppBulkRegister {
    public static void main(String[] args) {
        RegisterRequest request = new RegisterRequest("Ralf", "Scholler", "ralf@test.de", "01702934959",
                "Am Dorfplatz 6", null, "57610", "Ingelbach", "Deutschland", "test123");
        registerConsumers(10, request);
    }

    public static void registerConsumers(int n, RegisterRequest requestTemplate) {

        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8086")
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

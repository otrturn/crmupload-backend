package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.CustomerProfile;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

public class CustomerMeClient {

    private final WebClient webClient;

    public CustomerMeClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public CustomerMeResult getMe(String emailAddress, String loginToken) {

        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/customer/me/{emailAddress}")
                                .build(emailAddress)
                )
                .headers(headers ->
                        headers.setBearerAuth(loginToken)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(CustomerMeResult.Failure::new)
                                .flatMap(f ->
                                        reactor.core.publisher.Mono.error(
                                                new CustomertMeFailedException(f)
                                        )
                                )
                )
                .bodyToMono(CustomerProfile.class)
                .map(CustomerMeResult.Success::new)
                .map(CustomerMeResult.class::cast)
                .onErrorResume(CustomertMeFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class CustomertMeFailedException extends RuntimeException {
        private final CustomerMeResult.Failure result;

        CustomertMeFailedException(CustomerMeResult.Failure result) {
            this.result = result;
        }

        CustomerMeResult.Failure result() {
            return result;
        }
    }
}

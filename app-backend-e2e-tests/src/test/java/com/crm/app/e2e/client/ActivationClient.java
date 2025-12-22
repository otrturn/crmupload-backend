package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

public class ActivationClient {

    private final WebClient webClient;

    public ActivationClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public ActivationResult activate(String token) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/activate")
                        .queryParam("token", token)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(ActivationResult.Failure::new)
                                .flatMap(failure ->
                                        reactor.core.publisher.Mono.error(
                                                new ActivationFailedException(failure)
                                        )
                                )
                )
                .bodyToMono(String.class)
                .map(ActivationResult.Success::new)
                .map(ActivationResult.class::cast)
                .onErrorResume(ActivationFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class ActivationFailedException extends RuntimeException {
        private final ActivationResult.Failure result;

        ActivationFailedException(ActivationResult.Failure result) {
            this.result = result;
        }

        ActivationResult.Failure result() {
            return result;
        }
    }
}

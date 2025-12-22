package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.CustomerStatusResponse;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

public class CustomerStatusClient {

    private final WebClient webClient;

    public CustomerStatusClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public CustomerStatusResult getStatus(String emailAddress, String loginToken) {

        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/customer/get-status/{emailAddress}")
                                .build(emailAddress)
                )
                .headers(headers ->
                        headers.setBearerAuth(loginToken)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(CustomerStatusResult.Failure::new)
                                .flatMap(f ->
                                        reactor.core.publisher.Mono.error(
                                                new CustomerStatusFailedException(f)
                                        )
                                )
                )
                .bodyToMono(CustomerStatusResponse.class)
                .map(CustomerStatusResult.Success::new)
                .map(CustomerStatusResult.class::cast)
                .onErrorResume(CustomerStatusFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class CustomerStatusFailedException extends RuntimeException {
        private final CustomerStatusResult.Failure result;

        CustomerStatusFailedException(CustomerStatusResult.Failure result) {
            this.result = result;
        }

        CustomerStatusResult.Failure result() {
            return result;
        }
    }
}

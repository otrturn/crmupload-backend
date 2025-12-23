package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.DuplicateCheckHistoryResponse;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

public class DuplicateCheckHistoryClient {

    private final WebClient webClient;

    public DuplicateCheckHistoryClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public DuplicateCheckHistoryResult getDuplicateCheckHistory(String emailAddress, String loginToken) {

        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                // wichtig: gleicher Pfad wie Controller
                                .path("/api/customer/get-duplicate-check-history/{emailAddress}")
                                .build(emailAddress)
                )
                .headers(headers -> headers.setBearerAuth(loginToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(DuplicateCheckHistoryResult.Failure::new)
                                .flatMap(f ->
                                        reactor.core.publisher.Mono.error(
                                                new DuplicateCheckHistoryFailedException(f)
                                        )
                                )
                )
                .bodyToMono(DuplicateCheckHistoryResponse.class)
                .map(DuplicateCheckHistoryResult.Success::new)
                .map(DuplicateCheckHistoryResult.class::cast)
                .onErrorResume(DuplicateCheckHistoryFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class DuplicateCheckHistoryFailedException extends RuntimeException {
        private final DuplicateCheckHistoryResult.Failure result;

        DuplicateCheckHistoryFailedException(DuplicateCheckHistoryResult.Failure result) {
            this.result = result;
        }

        DuplicateCheckHistoryResult result() {
            return result;
        }
    }
}

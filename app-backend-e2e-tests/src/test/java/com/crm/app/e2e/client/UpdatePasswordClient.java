package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.UpdatePasswordRequest;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class UpdatePasswordClient {

    private final WebClient webClient;

    public UpdatePasswordClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public UpdatePasswordResult updatePassword(String emailAddress, UpdatePasswordRequest request, String loginToken) {

        return webClient.put()
                .uri("/api/customer/update-password/{emailAddress}", emailAddress)
                .headers(headers ->
                        headers.setBearerAuth(loginToken)
                ).bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(UpdatePasswordResult.Failure::new)
                                .flatMap(failure ->
                                        Mono.error(new UpdateFailedException(failure))
                                )
                )
                // 204 No Content -> kein Response-Body
                .toBodilessEntity()
                .thenReturn((UpdatePasswordResult) new UpdatePasswordResult.Success())
                .onErrorResume(UpdateFailedException.class,
                        ex -> Mono.just(ex.result())
                )
                .block();
    }

    private static class UpdateFailedException extends RuntimeException {
        private final UpdatePasswordResult.Failure result;

        UpdateFailedException(UpdatePasswordResult.Failure result) {
            this.result = result;
        }

        UpdatePasswordResult.Failure result() {
            return result;
        }
    }
}

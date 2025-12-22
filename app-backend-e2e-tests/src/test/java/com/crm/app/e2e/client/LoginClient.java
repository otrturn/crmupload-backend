package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.LoginRequest;
import com.crm.app.dto.LoginResponse;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

public class LoginClient {

    private final WebClient webClient;

    public LoginClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public LoginResult login(LoginRequest request) {

        return webClient.post()
                .uri("/auth/login")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(LoginResult.Failure::new)
                                .flatMap(failure ->
                                        reactor.core.publisher.Mono.error(new LoginFailedException(failure))
                                )
                )
                .bodyToMono(LoginResponse.class)
                .map(LoginResult.Success::new)
                .map(LoginResult.class::cast)
                .onErrorResume(LoginFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class LoginFailedException extends RuntimeException {
        private final LoginResult.Failure result;

        LoginFailedException(LoginResult.Failure result) {
            this.result = result;
        }

        LoginResult.Failure result() {
            return result;
        }
    }
}

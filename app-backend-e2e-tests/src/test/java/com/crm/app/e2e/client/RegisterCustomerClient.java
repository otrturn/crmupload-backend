package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.RegisterRequest;
import com.crm.app.dto.RegisterResponse;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

public class RegisterCustomerClient {

    private final WebClient webClient;

    public RegisterCustomerClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public RegisterResult register(RegisterRequest request) {

        return webClient.post()
                .uri("/auth/register-customer")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(RegisterResult.Failure::new)
                                .flatMap(failure ->
                                        reactor.core.publisher.Mono.error(new RegisterFailedException(failure))
                                )
                )
                .bodyToMono(RegisterResponse.class)
                .map(RegisterResult.Success::new)
                .map(RegisterResult.class::cast)
                .onErrorResume(RegisterFailedException.class,
                        ex -> reactor.core.publisher.Mono.just(ex.result())
                )
                .block();
    }

    private static class RegisterFailedException extends RuntimeException {
        private final RegisterResult.Failure result;

        RegisterFailedException(RegisterResult.Failure result) {
            this.result = result;
        }

        RegisterResult.Failure result() {
            return result;
        }
    }
}

package com.crm.app.e2e.client;

import com.crm.app.dto.ApiError;
import com.crm.app.dto.CustomerProfile;
import com.crm.app.e2e.config.E2eProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class UpdateCustomerClient {

    private final WebClient webClient;

    public UpdateCustomerClient(E2eProperties props) {
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    public UpdateCustomerResult updateCustomer(String emailAddress, CustomerProfile request, String loginToken) {

        return webClient.put()
                .uri("/api/customer/update-customer/{emailAddress}", emailAddress)
                .headers(headers ->
                        headers.setBearerAuth(loginToken)
                ) .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ApiError.class)
                                .map(UpdateCustomerResult.Failure::new)
                                .flatMap(failure ->
                                        Mono.error(new UpdateFailedException(failure))
                                )
                )
                // 204 No Content -> kein Response-Body
                .toBodilessEntity()
                .thenReturn((UpdateCustomerResult) new UpdateCustomerResult.Success())
                .onErrorResume(UpdateFailedException.class,
                        ex -> Mono.just(ex.result())
                )
                .block();
    }

    private static class UpdateFailedException extends RuntimeException {
        private final UpdateCustomerResult.Failure result;

        UpdateFailedException(UpdateCustomerResult.Failure result) {
            this.result = result;
        }

        UpdateCustomerResult.Failure result() {
            return result;
        }
    }
}
